 package de.unisiegen.tpml.graphics.bigstep;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FontMetrics;
 import java.awt.Point;
 
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPopupMenu;
 
 import de.unisiegen.tpml.core.ProofRule;
 import de.unisiegen.tpml.core.bigstep.BigStepProofModel;
 import de.unisiegen.tpml.core.bigstep.BigStepProofNode;
 import de.unisiegen.tpml.core.expressions.Expression;
 import de.unisiegen.tpml.core.expressions.Location;
 import de.unisiegen.tpml.core.languages.LanguageTranslator;
 import de.unisiegen.tpml.graphics.components.CompoundExpression;
 import de.unisiegen.tpml.graphics.components.MenuButton;
 import de.unisiegen.tpml.graphics.components.MenuButtonListener;
 import de.unisiegen.tpml.graphics.components.MenuGuessItem;
 import de.unisiegen.tpml.graphics.components.MenuRuleItem;
 import de.unisiegen.tpml.graphics.components.MenuTranslateItem;
 import de.unisiegen.tpml.graphics.renderer.AbstractRenderer;
 import de.unisiegen.tpml.graphics.tree.TreeNodeComponent;
 
 public class BigStepNodeComponent extends JComponent implements TreeNodeComponent {
 
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -2050381804392542081L;
 
 	/**
 	 * The {@link BigStepProofModel} that will get used to apply the 
 	 * actions on.
 	 */
 	private BigStepProofModel														proofModel;
 	
 	/**
 	 * The origin {@link BigStepProofNode}. Contains the information this
 	 * node gives a graphics representation.
 	 */
 	private BigStepProofNode														proofNode;
 	
 	/**
 	 * Contains the information of the size of the Component.
 	 * When {@link #placeElements(int)} is called <i>dimension</i> is
 	 * filled with proppert values.
 	 */
 	private Dimension																		dimension;
 	
 	/**
 	 * Amount of pixels that will be left free between the elements of the
 	 * node.
 	 */
 	private int 																				spacing;
 	
 	/**
 	 * Label that will contain the index at the front.
 	 */
 	private JLabel																			indexLabel;
 	
 	/**
 	 * Component containing the expression and the store.
 	 */
 	private CompoundExpression<Location, Expression>		expression;
 	
 	/**
 	 * Label containing the down-directed double-arrow separating the 
 	 * Expression and the Resul-Expression.
 	 */
 	private JLabel																			downArrowLabel;
 	
 	/**
 	 * Component containing the result-expression and the result-store.
 	 */
 	private CompoundExpression<Location, Expression>		resultExpression;
 	
 	/**
 	 * The Button with its DropDownMenu used to perform the userinteraction.
 	 */
 	private MenuButton																	ruleButton;
 
 	/**
 	 * Label that will be used to show the evaluated rule.
 	 */
 	private JLabel																			ruleLabel;
 	
 	/**
 	 * The "Translate to core syntax" item in the context menu.
 	 */
 	private MenuTranslateItem														menuTranslateItem;
 	
 	/**
 	 * The translator will be used to determine whether the expression 
 	 * contains syntactical sugar.
 	 */
 	private LanguageTranslator													translator;
 
 	/**
 	 * 
 	 * @param node				The node that should be represented
 	 * @param model				The model 
 	 * @param translator  The translator from the model
 	 */
 	public BigStepNodeComponent (BigStepProofNode		node,
 															 BigStepProofModel	model,
 															 LanguageTranslator	translator) {
 		super ();
 		
 		this.proofNode					= node;
 		this.proofModel					= model;
 		this.translator					= translator;
 		
 		this.dimension					= new Dimension (0, 0);
 		this.spacing						= 10;
 		
 		
 		/*
 		 * Create and add the components needed to render this node
 		 */
 		this.indexLabel					= new JLabel ();
 		add (this.indexLabel);
 		
 		this.expression					= new CompoundExpression<Location, Expression> ();
 		add (this.expression);
 		
 		this.downArrowLabel			= new JLabel ();
 		add (this.downArrowLabel);
 		this.downArrowLabel.setText(" \u21d3 "); // \u21d3 is the double arrow down
 		
 		this.resultExpression		= new CompoundExpression<Location, Expression>();
 		add (this.resultExpression);
 		this.resultExpression.setAlternativeColor(Color.LIGHT_GRAY);
 		
 		this.ruleButton 				= new MenuButton ();
 		add (this.ruleButton);
 		this.ruleButton.setVisible (true);
 		
 		this.ruleLabel					= new JLabel ();
 		add (this.ruleLabel);
 		this.ruleLabel.setVisible(false);
 		
 		/*
 		 * Create the PopupMenu for the menu button
 		 */
 		JPopupMenu menu = new JPopupMenu ();
 		
 		ProofRule[] rules = this.proofModel.getRules();
 		if (rules.length > 0) {
 			int group = rules[0].getGroup();
 			for (ProofRule r : rules) {
 				if (r.getGroup() != group) {
 					menu.addSeparator();
 				}
 				menu.add(new MenuRuleItem(r));
 				group = r.getGroup();
 			}
 		}
 		menu.addSeparator();
 		menu.add (new MenuGuessItem ());
 		menu.add (this.menuTranslateItem = new MenuTranslateItem ());
 		
 		this.ruleButton.setMenu(menu);
 
 		/*
 		 * Connect the handling of the ruleButton
 		 */
 		this.ruleButton.addMenuButtonListener(new MenuButtonListener() {
 			public void menuClosed (MenuButton button) { } 
 			public void menuItemActivated (MenuButton button, JMenuItem source) {
 				BigStepNodeComponent.this.handleMenuActivated (source);
 			}
 		});
 		
 		changeNode ();
 	}
 	
 	/**
 	 * Adds a new {@link BigStepNodeListener} to the <i>SmallStepNodeComponent</i>
 	 * 
 	 * @param listener The listener to be added
 	 */
 	public void addBigStepNodeListener (BigStepNodeListener listener) {
 		this.listenerList.add (BigStepNodeListener.class, listener);
 	}
 	
 	/**
 	 * Removes a {@link BigStepNodeListener} from the <i>SmallStepNodeComponent</i>
 	 * 
 	 * @param listener The listener to be removed.
 	 */
 	
 	public void removeBigStepNodeListener (BigStepNodeListener listener) {
 		this.listenerList.remove(BigStepNodeListener.class, listener);
 	}
 	
 	/**
 	 * Calls the {@link BigStepNodeListener#nodeChanged(BigStepNodeComponent)} of
 	 * all listeners.
 	 */
 	private void fireNodeChanged () {
 		Object[] listeners = this.listenerList.getListenerList();
 		for (int i=0; i<listeners.length; i+=2) {
 			if (listeners [i] != BigStepNodeListener.class) {
 				continue;
 			}
 			
 			((BigStepNodeListener)listeners [i+1]).nodeChanged(this);
 		}
 	}
 
 	/**
 	 * Hndles 
 	 * @param item
 	 */
 	private void handleMenuActivated (JMenuItem item) {
 		if (item instanceof MenuRuleItem) {
 			MenuRuleItem ruleItem = (MenuRuleItem)item;
 			ProofRule rule = ruleItem.getRule();
 			
 			try {
 				this.proofModel.prove(rule, this.proofNode);
 				this.ruleButton.setToolTipText(null);
			} catch (Throwable e) {
 				
 				// when the node could not be prooven with the selected
 				// rule the menu button gets labeled with the given rule 
 				// and will be displayed in red
 				this.ruleButton.setText("(" + rule.getName() + ")");
 				this.ruleButton.setTextColor(Color.RED);
 				
				// determine the error message for the tooltip
				e.printStackTrace();
				this.ruleButton.setToolTipText(e.getMessage());
 			}
 		}
 		else if (item instanceof MenuTranslateItem) {
 			int answer = 1;
 			if (this.proofModel.containsSyntacticSugar(this.proofNode, false)) {
 				String[] answers = { "Outermost only", "Whole expression", "Cancel" };
 				answer = JOptionPane.showOptionDialog(getTopLevelAncestor(), "Do you want to translate all syntactic " +
 																						  "sugar contained within this expression,\nor only the " +
 																							"outermost expression?",
 																						  "Translate to core syntax",
 																						  JOptionPane.YES_NO_CANCEL_OPTION,
 																						  JOptionPane.QUESTION_MESSAGE,
 																						  null,
 																						  answers,
 																						  answers[0]);
 			}
 			switch (answer) {
 			case 0:
 				this.proofModel.translateToCoreSyntax(this.proofNode, false);
 				break;
 			case 1:
 				this.proofModel.translateToCoreSyntax(this.proofNode, true);
 				break;
 			case 2:
 				break;
 			}
 			fireNodeChanged();
 		}
 		else if (item instanceof MenuGuessItem) {
 			try {
 				this.proofModel.guess(this.proofNode);
 			}
 			catch (Exception exc) {
 				exc.printStackTrace();
 			}
 		}
 		
 		fireNodeChanged();
 		
 
 	}
 	
 	
 	public void setIndex (int index) {
 		this.indexLabel.setText("(" + index + ")");
 	}
 	
 	
 	public void changeNode () {
 		
 		this.expression.setExpression (this.proofNode.getExpression());
 		
 		// only if memory is enabled set the store because the
 		// store is always valid
 		if (this.proofModel.isMemoryEnabled()) {
 			this.expression.setEnvironment (this.proofNode.getStore());
 		}
 		else {
 			this.expression.setEnvironment (null);
 		}
 
 		if (this.proofNode.getResult() != null) {
 			this.resultExpression.setExpression(this.proofNode.getResult().getValue());
 			if (this.proofModel.isMemoryEnabled()) {
 				this.resultExpression.setEnvironment(this.proofNode.getResult().getStore());
 			}
 			else {
 				this.resultExpression.setEnvironment(null);
 			}
 		}
 		else {
 			this.resultExpression.setExpression(null);
 			this.resultExpression.setEnvironment(null);
 		}
 	}
 	
 	private void placeElements (int maxWidth) {
 		// get the size for the index at the beginning: (x)
 		FontMetrics fm = AbstractRenderer.getTextFontMetrics();
 		Dimension labelSize = new Dimension (fm.stringWidth(this.indexLabel.getText()), fm.getHeight());
 		this.dimension.setSize(labelSize.width, labelSize.height);
 
 		// there will be a bit spacing between the index label and the expression
 		this.dimension.width += this.spacing;
 		
 		// the index shrinkens the max size for the expression
 		maxWidth -= labelSize.width;
 		
 		//  get the needed size for the expression
 		Dimension expSize = this.expression.getNeededSize(maxWidth);
 		this.dimension.width += expSize.width;
 		this.dimension.height = Math.max(expSize.height, this.dimension.height);
 
 		Dimension arrowSize = this.downArrowLabel.getPreferredSize();
 		this.dimension.width += arrowSize.width;
 		this.dimension.height = Math.max(arrowSize.height, this.dimension.height);
 
 		// the result should never be wrapped so we use 
 		// the Integer.MAX_VALUE to prevent linewrapping
 		Dimension resultSize = this.resultExpression.getNeededSize(Integer.MAX_VALUE);
 		this.dimension.width += resultSize.width;
 		this.dimension.height = Math.max(resultSize.height, this.dimension.height);
 				
 		// now place the elements
 		int posX = 0;
 		
 		this.indexLabel.setBounds(posX, 0, labelSize.width, this.dimension.height);
 		posX += labelSize.width + this.spacing;
 		
 		this.expression.setBounds(posX, 0, expSize.width, this.dimension.height);
 		posX += expSize.width;
 		
 		this.downArrowLabel.setBounds (posX, 0, arrowSize.width, this.dimension.height);
 		posX += arrowSize.width;
 		
 		this.resultExpression.setBounds(posX, 0, resultSize.width, this.dimension.height);
 		
 		
 		/*
 		 * Check whether this is node is evaluated.
 		 * 
 		 * If it is evaluated only the Label needs to get placed,
 		 * if it is not evaluated yet the MenuButton needs to get placed.
 		 */
 		posX = labelSize.width + this.spacing;
 		
 		if (this.proofNode.getRule() != null) {
 			this.ruleLabel.setText ("(" + this.proofNode.getRule() + ")");
 			Dimension ruleLabelSize = this.ruleLabel.getPreferredSize();
 			
 			this.ruleLabel.setBounds(posX, this.dimension.height + this.spacing, ruleLabelSize.width, ruleLabelSize.height);
 			
 			this.dimension.height += this.spacing + ruleLabelSize.height;
 			this.dimension.width = Math.max(this.dimension.width, ruleLabelSize.width + posX);
 			
 			//  display only the label not the button
 			this.ruleLabel.setVisible (true);
 			this.ruleButton.setVisible (false);
 		}
 		else {
 			// place the menu button
 			Dimension buttonSize = this.ruleButton.getNeededSize();
 			this.ruleButton.setBounds(posX, this.dimension.height + this.spacing, buttonSize.width, buttonSize.height);
 			
 			this.dimension.height += this.spacing + buttonSize.height;
 			this.dimension.width = Math.max(this.dimension.width, buttonSize.width + posX);
 			
 			// display only the button not the label
 			this.ruleLabel.setVisible (false);
 			this.ruleButton.setVisible (true);
 		}
 
 	}
 	
 	/* 
 	 * Implementation of the TreeNodeComponent interface
 	 */
 	public Dimension update(int maxWidth) {
 		placeElements (maxWidth);
 		this.menuTranslateItem.setEnabled(this.translator.containsSyntacticSugar(this.proofNode.getExpression(), true));
 		
 		return this.dimension;
 
 	}
 	
 	
 	public Point getBottomArrowConnection() {
 		return new Point (this.getX() + this.indexLabel.getWidth() / 2, this.getY() + this.indexLabel.getHeight());
 	}
 
 	public Point getLeftArrowConnection() {
 		return new Point (this.getX (), this.getY() + this.indexLabel.getY() + this.indexLabel.getHeight() / 2);
 	}
 
 
 	public int getIndentationWidth() {
 		// XXX: calculate the indentation
 		return this.indexLabel.getWidth();
 	}
 	
 
 	@Override
 	public void setBounds (int x, int y, int width, int height) {
 		super.setBounds (x, y, width, height);
 	}
 
 }
