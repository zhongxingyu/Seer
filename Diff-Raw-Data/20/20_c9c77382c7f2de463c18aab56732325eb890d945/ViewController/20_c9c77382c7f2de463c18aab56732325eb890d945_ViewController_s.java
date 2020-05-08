 package edu.wustl.cab2b.client.ui.dag;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JMenuItem;
 import javax.swing.JPopupMenu;
 
 import org.netbeans.graph.api.control.builtin.DefaultViewController;
 import org.netbeans.graph.api.model.GraphEvent;
 import org.netbeans.graph.api.model.IGraphLink;
 import org.netbeans.graph.api.model.IGraphNode;
 import org.netbeans.graph.api.model.IGraphPort;
 import org.netbeans.graph.api.model.builtin.GraphPort;
 
 import edu.wustl.cab2b.client.ui.util.ClientConstants;
 import edu.wustl.common.querysuite.queryobject.IExpressionId;
 
 public class ViewController extends DefaultViewController implements ActionListener
 {
 	private JPopupMenu singleNodePopup;
 	private JMenuItem miDelete;
 	private JMenuItem miEditProp;
 	private JMenuItem miAddToView;
 	private JMenuItem miDeleteFromView;
 	private JMenuItem miViewAssProp;
 		
 	private JPopupMenu m_logicalOperatorMenu;
 	private JMenuItem m_andOperator;
 	private JMenuItem m_orOperator;
 	
 	private IGraphNode m_currentNode;
 	
 	private MainDagPanel m_mainPanel;
 
 	public ViewController(MainDagPanel mainPanel)
 	{
 		m_mainPanel = mainPanel;
 	
 		//	TODO Auto-generated constructor stub
 		singleNodePopup = new JPopupMenu();
         
 		if (!mainPanel.isDAGForView())
 		{
 			miEditProp = new JMenuItem("Edit Limits");
 			miEditProp.addActionListener(this);
 			singleNodePopup.add(miEditProp);
 
 			miDelete = new JMenuItem("Delete Limits");
 			miDelete.addActionListener(this);
 			singleNodePopup.add(miDelete);
 
 			miViewAssProp = new JMenuItem("Show associations");
 			miViewAssProp.addActionListener(this);
 			singleNodePopup.add(miViewAssProp);
 			miViewAssProp.setEnabled(false);
 
 			m_logicalOperatorMenu = new JPopupMenu();
 			m_andOperator = new JMenuItem("AND");
 			m_andOperator.addActionListener(this);
 			m_logicalOperatorMenu.add(m_andOperator);
 
 			m_orOperator = new JMenuItem("OR");
 			m_orOperator.addActionListener(this);
 			m_logicalOperatorMenu.add(m_orOperator);
 			m_logicalOperatorMenu.setBackground(Color.WHITE);
 		}
 		else
 		{
 			miAddToView = new JMenuItem("Add to View");
 			miAddToView.addActionListener(this);
 			singleNodePopup.add(miAddToView);
 
 			miDeleteFromView = new JMenuItem("Delete from View");
 			miDeleteFromView.addActionListener(this);
 			singleNodePopup.add(miDeleteFromView);
 		}
 		
      
     }
 	
 	protected boolean keyPressed(int modifiersEx, int keyCode)
 	{
 		if(keyCode == KeyEvent.VK_DELETE)
 		{
 			Object[] objects = getHelper().getSelectedComponents();
 			List<IGraphNode> deleteNodes = new ArrayList<IGraphNode>();
 			List<IGraphLink> deleteLinks = new ArrayList<IGraphLink>();
 			for(int i=0; i<objects.length; i++)
 			{
 				// check if object is instance of link
 				// then put it in delete links structure or else if it is instance
 				// of node put it in delete nodes structure
 				if(objects[i] instanceof IGraphLink)
 				{
 					deleteLinks.add((IGraphLink)objects[i]);
 				}
 				else if(objects[i] instanceof IGraphNode)
 				{
 					deleteNodes.add((IGraphNode)objects[i]);
 				}
 			}
 			// Remove associations associated with the links to delete
 			for(int i=0; i<deleteLinks.size(); i++)
 			{
 				deleteLink(deleteLinks.get(i));
 			}
 			//	Then Remove nodes
 			for(int i=0; i<deleteNodes.size(); i++)
 			{
 				deleteNode(deleteNodes.get(i));
 			}
 		}
 		return false;
 	}
 	/**
 	 * Handle node deletion activity
 	 * @param node
 	 */
 	private void deleteNode(IGraphNode node)
 	{
 		IExpressionId expressionId = ((ClassNode)node.getLookup().lookup(ClassNode.class)).getExpressionId();
 		ClassNodeType nodeType = m_mainPanel.getNodeType(expressionId);
 		if(m_mainPanel.isDAGForView())
 		{
 			if(nodeType.equals(ClassNodeType.ConstraintViewNode))
 			{
 				ClassNode classNode = (ClassNode)node.getLookup().lookup(ClassNode.class);
 				m_mainPanel.removeExpressionFromView(classNode.getExpressionId());
 				classNode.setType(m_mainPanel.getNodeType(classNode.getExpressionId()));
 				getHelper().scheduleNodeToLayout(m_currentNode);
 				getHelper().recalculate();
 			}
 			else if(nodeType.equals(ClassNodeType.ViewOnlyNode))
 			{
 				deleteNodeFromView(node);
 		        //	      Delete node from query object
 				m_mainPanel.deleteExpression(expressionId);
 			}
 			
 		}
 		else
 		{
 			deleteNodeFromView(node);
 	        //	      Delete node from query object
 			m_mainPanel.deleteExpression(expressionId);
 		}
 	}
 
 	private void deleteNodeFromView(IGraphNode node)
 	{
 		IGraphPort[] ports = node.getPorts();
 		if(ports != null)
 		{
 			IGraphLink[] links = null;
 			for(int i=0; i< ports.length; i++)
 			{
 				links = ports[i].getLinks();
 				if((links != null)&& (links.length != 0))
 				{
 					deleteLink(links[0]);
 				}
 			}
 		}
 				
 		final IGraphNode[] _nodes = new IGraphNode[] {node};
 		final IGraphLink[] _links = new IGraphLink[]{};
 		m_mainPanel.getDocument().removeComponents(GraphEvent.create(_nodes, _links));
 	}
 	/**
 	 * DElete link between two nodes
 	 * @param link Link to delete
 	 */
 	private void deleteLink(IGraphLink link)
 	{
 		IGraphPort sourcePort = link.getSourcePort();
 		IGraphPort targetPort = link.getTargetPort();
 		ClassNode sourceNode = (ClassNode)sourcePort.getNode().getLookup().lookup(ClassNode.class);
 		ClassNode destinationNode = (ClassNode)targetPort.getNode().getLookup().lookup(ClassNode.class);
 		if(m_mainPanel.isDAGForView())
 		{
 			ClassNodeType sourceNodeType = m_mainPanel.getNodeType(sourceNode.getExpressionId());
 			ClassNodeType destNodeType = m_mainPanel.getNodeType(destinationNode.getExpressionId());
 			if(sourceNodeType.equals(ClassNodeType.ViewOnlyNode) || destNodeType.equals(ClassNodeType.ViewOnlyNode))
 			{
 				deleteLinkData(link, sourcePort, targetPort, sourceNode, destinationNode);
 			}
 		}
 		else
 		{
 			deleteLinkData(link, sourcePort, targetPort, sourceNode, destinationNode);
 		}
 	}
 
 	private void deleteLinkData(IGraphLink link, IGraphPort sourcePort, IGraphPort targetPort, ClassNode sourceNode, ClassNode destinationNode)
 	{
 		// Clear the port related data structures of ClassNode class
 		sourceNode.removeSourcePort(sourcePort);
 		destinationNode.deleteTargetPort(targetPort);
 		
 		// Delete association from query object
 		m_mainPanel.deletePath(link.getLookup().lookup(PathLink.class));
 		getHelper().scheduleNodeToLayout(sourceNode);
 		getHelper().recalculate();
 		final IGraphNode[] _nodes = new IGraphNode[] {};
         final IGraphLink[] _links = new IGraphLink[]{link};
         sourceNode.removePort((GraphPort)sourcePort.getLookup().lookup(GraphPort.class));
         destinationNode.removePort((GraphPort)targetPort.getLookup().lookup(GraphPort.class));
         m_mainPanel.getDocument().removeComponents(GraphEvent.create(_nodes, _links));
 	}
 	/**
 	 * Clear all the paths associated with all the nodes
 	 *
 	 */
 	public void clearAllPaths()
 	{
 		// Get all the nodes
 		IGraphNode[] nodes = getHelper().getNodes();
 		for(int nodeCnt=0; nodeCnt<nodes.length; nodeCnt++)
 		{
 			IGraphPort[] ports = nodes[nodeCnt].getPorts();
 			IGraphLink[] links = null;
			for(int i=0; i< ports.length; i++)
			{
				links = ports[i].getLinks();
				if((links != null)&& (links.length != 0))
				{
					deleteLink(links[0]);
				}
			}
 		}
 	}
 	/**
      * Called when an user clicks on the view.
      * @param component the component (IGraphNode, IGraphLink, or IGraphPort instance), null if user clicks on the background
      * @param point the point where an user clicked
      */
     protected boolean componentClicked (Object component, Point point)
     {
     	m_currentNode = null;
     	if(component instanceof IGraphNode)
     	{
     		IGraphNode node = (IGraphNode)component;
     		m_currentNode = node;
     		ClassNodeRenderer nodeRenderer = (ClassNodeRenderer)getHelper().getNodeRenderer(node);
     		if(true == nodeRenderer.isMaxMinClick(node, point))
     		{
     			getHelper().scheduleNodeToLayout(node);
     			getHelper().recalculate();
     		}
     		else if(true == nodeRenderer.isOptionPopupShow(node, point))
     		{
     			Rectangle r = getHelper().getBounds(node);
     			ClassNode classNode = (ClassNode)node.getLookup().lookup(ClassNode.class);
     			if(m_mainPanel.isDAGForView())
     			{
 	    	        if(classNode.getType().equals(ClassNodeType.ViewOnlyNode) || classNode.getType().equals(ClassNodeType.ConstraintViewNode))
 	    	        {
 	    	        	miAddToView.setEnabled(false);
 	    	        	miDeleteFromView.setEnabled(true);
 	    	        }
 	    	        else
 	    	        {
 	    	        	miAddToView.setEnabled(true);
 	    	        	miDeleteFromView.setEnabled(false);
 	    	        }
     			}
     			singleNodePopup.show (getViewPresenter().getView(), r.x + r.width+2, r.y );
     		}
     		else if (true == nodeRenderer.isAssociationPopupShow(node, point))
     		{
     			Point lpoint = nodeRenderer.getLocationOfPopup(node);
     			m_logicalOperatorMenu.setSize(new Dimension(50, 40));
     			m_logicalOperatorMenu.show (getViewPresenter().getView(), lpoint.x-m_logicalOperatorMenu.getWidth(), lpoint.y );
     		}
        	}
        	return false;
     }
     /**
      * Method to get currently selected node
      * @return
      */
     public List<ClassNode> getCurrentNodeSelection()
     {
     	//  delete nodes from m_selectedNodes which are not selected 
     	GraphEvent event = m_mainPanel.getDocument().getSelectedComponents();
     	IGraphNode[] selectedNodes = event.getNodes();
 		List<ClassNode> tempList = new ArrayList<ClassNode>();
 		for(int j=0; j<selectedNodes.length; j++)
 		{
 			ClassNode tempNode = (ClassNode)selectedNodes[j].getLookup().lookup(ClassNode.class);
 			tempList.add(tempNode);
 		}
     	return tempList;
     }
       
     /**
      * Called when an user double clicks on the view.
      * @param component the component (IGraphNode, IGraphLink, or IGraphPort instance), null if user clicks on the background
      * @param point the point where an user clicked
      */
     protected boolean componentDoubleClicked (Object component, Point point)
     {
     	m_currentNode = null;
     	if(component instanceof IGraphNode)
     	{
     		m_currentNode = (IGraphNode)component;
     		ClassNode classNode = (ClassNode)m_currentNode.getLookup().lookup(ClassNode.class);
     		// Send control to parent panel to display details of
     		// this node in upper panel
     		m_mainPanel.showNodeDetails(classNode);
        	}	
     	return false;
     }
     
     /**
      * 
      */
 	public void actionPerformed(ActionEvent event)
 	{
 		// TODO Auto-generated method stub
 		//Selected item is AND operator
 		if(event.getSource() == m_andOperator)
 		{
 			ClassNode classNode = (ClassNode)m_currentNode.getLookup().lookup(ClassNode.class);
 			ClassNodeRenderer nodeRenderer = (ClassNodeRenderer)getHelper().getNodeRenderer(m_currentNode);
     		int index = nodeRenderer.getSelectedAssocitationIdx();
     		if(index == 0)// Operator between constraints and rule has been changed
     		{
     			classNode.setOperatorBetAttrAndAss(ClientConstants.OPERATOR_AND);
     			m_mainPanel.updateLogicalOperatorForAttributes(classNode, ClientConstants.OPERATOR_AND);
     		}
     		else
     		{
     			classNode.setLogicalOperator(index, ClientConstants.OPERATOR_AND);
     			m_mainPanel.updateLogicalOperatorForAssociation(classNode, classNode.getLinkForSourcePort(classNode.getSourcePortAt(index)),ClientConstants.OPERATOR_AND );
     		}
     		getHelper().scheduleNodeToLayout(m_currentNode);
 			getHelper().recalculate();
 		}
 		else if(event.getSource() == m_orOperator)
 		{
 			ClassNode classNode = (ClassNode)m_currentNode.getLookup().lookup(ClassNode.class);
 			ClassNodeRenderer nodeRenderer = (ClassNodeRenderer)getHelper().getNodeRenderer(m_currentNode);
     		int index = nodeRenderer.getSelectedAssocitationIdx();
     		if(index == 0) // Operator between constraints and rule has been changed
     		{
     			classNode.setOperatorBetAttrAndAss(ClientConstants.OPERATOR_OR);
     			m_mainPanel.updateLogicalOperatorForAttributes(classNode, ClientConstants.OPERATOR_OR);
     		}
     		else
     		{
     			classNode.setLogicalOperator(index, ClientConstants.OPERATOR_OR);
     			m_mainPanel.updateLogicalOperatorForAssociation(classNode, classNode.getLinkForSourcePort(classNode.getSourcePortAt(index)),ClientConstants.OPERATOR_OR );
     		}
     		getHelper().scheduleNodeToLayout(m_currentNode);
 			getHelper().recalculate();
 		}
 		else if (event.getSource() == miDelete)
 		{
 			// Delete the selected node
 			Object[] objects = getHelper().getSelectedComponents();
 			for(int i=0; i<objects.length; i++)
 			{
 				if(objects[i] instanceof IGraphNode)
 				{
 					deleteNode((IGraphNode)objects[i]);
 				}
 			}
 		}
 		else if (event.getSource() == miEditProp)
 		{
 			// Edit the selected node
 			ClassNode classNode = (ClassNode)m_currentNode.getLookup().lookup(ClassNode.class);
 			m_mainPanel.showNodeDetails(classNode);
 		}
 		else if (event.getSource() == miAddToView)
 		{
 			// Edit the selected node
 			ClassNode classNode = (ClassNode)m_currentNode.getLookup().lookup(ClassNode.class);
 			m_mainPanel.addExpressionToView(classNode.getExpressionId());
 			classNode.setType(m_mainPanel.getNodeType(classNode.getExpressionId()));
 			getHelper().scheduleNodeToLayout(m_currentNode);
 			getHelper().recalculate();
 		}
 		else if (event.getSource() == miDeleteFromView)
 		{
 			deleteNode(m_currentNode);
 		}
 	}
 	/**
 	 * Delete All the nodes form the panel as well as from the query
 	 * This method is called when reset button is clicked on dialog-box
 	 */
 	public void deleteAllNodes()
 	{
		//		 Get all the nodes
 		IGraphNode[] nodes = getHelper().getNodes();
 		for(int nodeCnt=0; nodeCnt<nodes.length; nodeCnt++)
 		{
 			deleteNode(nodes[nodeCnt]);
 		}
 	}
 }
