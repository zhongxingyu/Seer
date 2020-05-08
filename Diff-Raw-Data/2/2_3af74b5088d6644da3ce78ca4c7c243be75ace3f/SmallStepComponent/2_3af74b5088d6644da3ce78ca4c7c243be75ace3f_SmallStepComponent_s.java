 package de.unisiegen.tpml.graphics.smallstep;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Point;
 import java.awt.Rectangle;
 
 import javax.swing.Scrollable;
 import javax.swing.SwingUtilities;
 import javax.swing.event.TreeModelEvent;
 
 import de.unisiegen.tpml.core.ProofNode;
 import de.unisiegen.tpml.core.smallstep.SmallStepProofModel;
 import de.unisiegen.tpml.core.smallstep.SmallStepProofNode;
 import de.unisiegen.tpml.graphics.AbstractProofComponent;
 
 public class SmallStepComponent extends AbstractProofComponent implements Scrollable {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1022005553523956037L;
 
 
 	private SmallStepProofModel			model;
 	
 	private int											border;
 	
 	private int											spacing;
 	
 	private int											availableWidth;
 	
 	private ProofNode								jumpNode;
 	
 	public SmallStepComponent (SmallStepProofModel proofModel) {
 		super (proofModel);
 		
 		
 		this.currentlyLayouting	= false;
 		
 		setLayout (null);
 		
 		this.model 		= proofModel;
 		this.border		= 20;
 		this.spacing	= 10;
 		
 		
 		// trigger the first layouting
 		relayout ();
 	}
 	
 	@Override
 	public void setAvailableWidth (int availableWidth) {
 		this.availableWidth = availableWidth;
 		relayout ();
 	}
 	
 	
 	
 	private SmallStepProofNode getFirstChild (SmallStepProofNode node) {
 		try {
 			return node.getFirstChild();
 		}
 		catch (Exception e) { }
 		return null;
 	}
 	
 	/**
 	 * Traversing the ProofTree recursivly and adds a SmallStepNodeComponent
 	 * where none is.<br>
 	 * <br>
 	 * Usualy only at newly added nodes the SmallStepNodeComponent is missing.
 	 * 
 	 * @param node When calling this method: the rootNode of the tree.
 	 */
 	private void checkForUserObject (SmallStepProofNode node) {
 		if (node == null) {
 			return;
 		}
 		
 		SmallStepNodeComponent nodeComponent = (SmallStepNodeComponent)node.getUserObject();
 		if (nodeComponent == null) {
 			
 			// create the noded that has not been there yet
 			nodeComponent = new SmallStepNodeComponent (node, this.model, this.translator, this.spacing);
 			
 			// add the needed listener
 			nodeComponent.addSmallStepNodeListener(new SmallStepNodeListener() {
 				public void nodeChanged (SmallStepNodeComponent node) {
 					SmallStepComponent.this.relayout();
 				}
 				public void repaintAll () {
 					SmallStepComponent.this.repaint();
 				}
 			});
 			
 			nodeComponent.update();
 			
 			// save it to the node
 			node.setUserObject(nodeComponent);
 			
 			// and add the SmallStepNodeComponent to the gui
 			add (nodeComponent);
 		}
 		
 		checkForUserObject (getFirstChild (node));
 	}
 	
 	/**
 	 * Traverses the ProofTree recursivly and checks the needed size for
 	 * the rule combo on the left-hand-side.<br>
 	 * <br>
 	 * The <i>currentWidth</i> is the current maximum width that has been
 	 * evaluated. When calling this function this should be somthing small.
 	 * Just set it to <b>0</b>. 
 	 * 
 	 * @param node When calling this method: the rootNode of the tree.
 	 * @param currentWidth Used internaly. Should be set to <b>0</b>.
 	 * @return
 	 */
 	private int checkMaxRuleWidth (SmallStepProofNode node, int currentWidth) {
 		if (node == null) {
 			return currentWidth;
 		}
 		
 		// get the size of the current node
 		SmallStepNodeComponent nodeComponent = (SmallStepNodeComponent)node.getUserObject();
 		int nodeWidth = nodeComponent.getMinRuleSize().width;
 		
 		// only the maximum width is of interest
 		currentWidth = Math.max(currentWidth, nodeWidth);
 
 		// return the recursive result of the next node
 		
 		return checkMaxRuleWidth (getFirstChild (node), currentWidth);
 	}
 	
 	/**
 	 * Traverses the ProofTree recursivly and informing every node for
 	 * the maximum size of the rule combo on the left-hand-side.
 	 *  
 	 * @param node When calling this method: the rootNode of the tree.
 	 * @param maxRuleWidth The maximum Width of the rules.
 	 */
 	private void updateMaxRuleWidth (SmallStepProofNode node, int maxRuleWidth) {
 		if (node == null) {
 			return;
 		}
 		
 		// inform the node of the max rule width
 		SmallStepNodeComponent nodeComponent = (SmallStepNodeComponent)node.getUserObject();
 		nodeComponent.setMaxRuleWidth(maxRuleWidth);
 		
 		// proceed with the next child node
 		updateMaxRuleWidth (getFirstChild (node), maxRuleWidth);
 	}
 	
 	/**
 	 * Traverses the ProofTree recursivly and checks the size of
 	 * the expression for every node.
 	 * 
 	 * @param node When calling this method: the rootNode of the tree.
 	 */
 	private void checkExpressionSize (SmallStepProofNode node) {
 		if (node == null) {
 			return;
 		}
 		
 		SmallStepNodeComponent nodeComponent = (SmallStepNodeComponent)node.getUserObject();
 		nodeComponent.checkNeededExpressionSize(this.availableWidth - this.border);
 
 		
 		// proceed with the next child
 		checkExpressionSize (getFirstChild (node));
 	}
 	
 	
 	/**
 	 * Iterates through the entire tree an places every node.<br>
 	 * <br>
 	 * There are some things to take care of when the nodes get placed:<br>
 	 * <br>
 	 * The expression and the rules of the parent node are in one row so
 	 * when the nodes get placed the actualHeight of each node must be the 
 	 * maximum of both. They are placed together in on step. So the rules
 	 * of each node are placed together with the expression of its child node,
 	 * if there is one.<br>
 	 * If there is no parent node (that would be the first node, the root node),
 	 * only the expression needs to get places.<br>
 	 * If the node has no child (that would be the last node in the tree), 
 	 * the rules must be placed directly because there is no child node that
 	 * would place them.
 	 * 
 	 * @param node The rootNode
 	 * @param x The horizontal start position
 	 * @param y Ther vertical start position
 	 * @return The size needed to show all the nodes.
 	 */
 	private Dimension placeNode (SmallStepProofNode node, int x, int y) {
 		Dimension size = new Dimension (0, 0);
 
 		while (node != null) {
 			SmallStepNodeComponent nodeComponent = (SmallStepNodeComponent)node.getUserObject();
 			
 			// set the origin of this node
 			nodeComponent.setOrigion(new Point (x, y));
 			
 			// if the node has no parent node it appears to be the rootNode
 			// 
 			// the expression of the rootNode can be placed without checking anything
 			if (node.getParent() == null) {
 				nodeComponent.placeExpression();
 				
 				
 				// move the positioning
 				y += nodeComponent.getRuleTop();
 				
 				// evaluate the new dimensions
 				size.height		= y;
 				
 			}
 			else {
 				// evaluate the max size of this nodes expression and the parent
 				// nodes rules
 				SmallStepProofNode 			parentNode = node.getParent();
 				SmallStepNodeComponent 	parentNodeComponent = (SmallStepNodeComponent)parentNode.getUserObject();
 				
 				
 				Dimension expSize 	= nodeComponent.getExpressionSize();
 				Dimension ruleSize 	= parentNodeComponent.getRuleSize();
 	
 				int maxHeight = Math.max(expSize.height, ruleSize.height);
 				
 				// inform both component about the actual height they should use to
 				// place them
 				parentNodeComponent.setActualRuleHeight(maxHeight);
 				nodeComponent.setActualExpressionHeight(maxHeight);
 				
 				// let both components place theire elements
 				parentNodeComponent.placeRules();
 				nodeComponent.placeExpression();
 				
 				
 				// this finishes the parentNode so it can be placed
 				parentNodeComponent.setBounds();
 				
 				// the additional height come from the actual node
 				y += nodeComponent.getRuleTop();
 				
 				// evaluate the new dimensions
 				size.height = y;
 
 				// the actual width of the entire component can now be checked
 				// on the finshed node. the parent node
 				size.width	= Math.max(size.width, x + parentNodeComponent.getSize().width);
 			}
 		
 			// if the node has no children the rules need to get
 			// placed here with the expression
 			if (getFirstChild (node) == null) {
 				
 				if (this.model.isFinished()) {
 					nodeComponent.hideRules ();
 					nodeComponent.setBounds();
 				}
 				else {
 					// the rules can savely be positioned
 					nodeComponent.placeRules();
 					
 					// and the node itself can be placed
 					nodeComponent.setBounds();
 					
 					// evaluate the new dimension
 					size.height 	+= nodeComponent.getActualRuleHeight();
 					
 					// the actual width of the entire component can now be checked
 					// on the finshed node. 
 					size.width	= Math.max(size.width, x + nodeComponent.getSize().width);
 				}
 				
 				return size;
 			}
 			
 			node = node.getFirstChild();
 		}
 		
 		return size;
 	}
 	
 	/**
 	 * Does the entire layouting of the SmallStepComponent.<br>
 	 * <br>
 	 * All nodes in the tree will get a SmallStepNodeComponent, the size 
 	 * of the widest rule combo on the left-hand-site is evaluated. <br>
 	 * In order to render all expression alligned every node is informed of
 	 * this width.
 	 *
 	 */
 	@Override
 	protected void relayout () {
 		if (this.currentlyLayouting) {
 			return;
 		}
 		
 		this.currentlyLayouting = true;
 		
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run () {
 		
 				// get the rootNode it will be used many time
 				SmallStepProofNode rootNode = (SmallStepProofNode)SmallStepComponent.this.proofModel.getRoot();
 				
 				
 				// check if all nodes have a propper SmallStepNodeComponent
 				checkForUserObject (rootNode);
 				
 				// find the maximum width of the rules and inform the entire tree
 				int maxRuleWidth = checkMaxRuleWidth (rootNode, 0);
 				updateMaxRuleWidth (rootNode, maxRuleWidth);
 				
 				// evaluate the the sizes of the expression 
 				checkExpressionSize(rootNode);
 				
 				// now that the basics for the nodes are found, 
 				// they can be placed
 				Dimension size = placeNode (rootNode, SmallStepComponent.this.border, SmallStepComponent.this.border);
 				
 				// the needed size evaluaded by placing the nodes gets
 				// widened a bit to have a nice border around the component
 				size.width 	+= SmallStepComponent.this.border;
 				size.height += SmallStepComponent.this.border;
 				
 				// this size is used to determin all the sizes of the component
 				setPreferredSize (size);
 				setSize (size);
 				setMinimumSize (size);
 				setMaximumSize (size);
 				
 				SmallStepComponent.this.currentlyLayouting = false;
 				SmallStepComponent.this.jumpToNodeVisible();
 			}
 		});
 	}
 	
 	@Override
 	protected void nodesInserted (TreeModelEvent event) {
 		Object [] children = event.getChildren();
 		
 		if (children != null) {
 			this.jumpNode = (ProofNode)children [0];
 		}
 		else {
 			this.jumpNode = null;
 		}
 	}
 	
 	@Override
 	protected void nodesChanged (TreeModelEvent event) {
 		boolean relayout = false;
 		Object[] children = event.getChildren();
 		if (children == null) {
 			
 			// if the children are null and the path only contains one element
 			// this element is the root node.
 			if (event.getPath().length == 1) {
 				SmallStepProofNode proofNode = (SmallStepProofNode)event.getPath()[0];
 				SmallStepNodeComponent nodeComponent = (SmallStepNodeComponent)proofNode.getUserObject();
 				if (nodeComponent != null) {
 					nodeComponent.update ();
 					relayout = true;
 				}
 				
 			}
 		}
 		else {
 			for (int i=0; i<children.length; i++) {
 				if (children[i] instanceof ProofNode) {
 					SmallStepProofNode proofNode = (SmallStepProofNode)children[i];
 					
 					SmallStepNodeComponent nodeComponent = (SmallStepNodeComponent)proofNode.getUserObject();
 					if (nodeComponent != null) {
 						nodeComponent.update ();
 						relayout = true;
 					}
 				}
 			}
 		}
 		if (relayout) {
 			relayout ();
 		}
 	}
 	
 	@Override
 	protected void nodesRemoved (TreeModelEvent event) { 
 		Object[] children = event.getChildren();
 		if (children == null) {
 			return;
 		}
 		for (int i=0; i<children.length; i++) {
 			if (children[i] instanceof ProofNode) {
 				SmallStepProofNode proofNode = (SmallStepProofNode)children[i];
 				
 				SmallStepNodeComponent nodeComponent = (SmallStepNodeComponent)proofNode.getUserObject();
 				if (nodeComponent != null) {
 					remove (nodeComponent);
 					proofNode.setUserObject(null);
 				}
 			}
 		}
 
 	}
 	
 	@Override
 	protected void treeContentChanged () {
 		relayout ();
 	}
 	
 	public void guess () {
 		SmallStepProofNode node = (SmallStepProofNode)this.model.getRoot();
 		while (node != null) {
 			try {
 				SmallStepProofNode cNode = node.getFirstChild();
 				node = cNode;
 			} catch (Exception e) { break;}
 		}
 		if (node == null) {
 			return;
 		}
 		try {
 			this.model.guess(node);
 		} catch (Exception e) { }
 	}
 	
 	private void jumpToNodeVisible () {
 		if (this.jumpNode == null) {
 			return;
 		}
 		
 		// get the Component nodes to evaluate the positions
 		// on the viewport
 		SmallStepNodeComponent node = (SmallStepNodeComponent)this.jumpNode.getUserObject();
 		if (node == null) {
 			return;
 		}
 		
 		// get the visible rect to ensure the x coordinate is in the 
 		// visible area. only vertical scolling is requested
 		Rectangle visibleRect = this.getVisibleRect();
 		
 		Rectangle rect = new Rectangle ();
 		rect.x 			= visibleRect.x;
 		rect.y 			= node.getY ();
 		rect.width 	= 1;
 		rect.height = node.getHeight ();
 		
 		this.scrollRectToVisible(rect);
 
 		this.jumpNode = null;
 	}
 	//
 	// Methods for painting purposes
 	//
 	@Override
 	protected void paintComponent (Graphics gc) {
 		gc.setColor (Color.WHITE);
 		gc.fillRect(0, 0, getWidth () - 1, getHeight () - 1);
 	}
 	
 	
 	
 	// 
 	// Implementation of the Scrollable interface
 	//
 
 	public Dimension getPreferredScrollableViewportSize() {
 		return getPreferredSize ();
 	}
 
 	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
 		return 10;
 	}
 
 	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
 		return 25;
 	}
 
 	public boolean getScrollableTracksViewportWidth() {
 		return false;
 	}
 
 	public boolean getScrollableTracksViewportHeight() {
 		return false;
 	}
 
 	
 }
