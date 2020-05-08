 package edu.rpi.phil.legup.newgui;
 
 import edu.rpi.phil.legup.BoardState;
 import edu.rpi.phil.legup.Justification;
 import edu.rpi.phil.legup.Legup;
 import edu.rpi.phil.legup.Selection;
 
 import java.awt.BorderLayout;
 import java.awt.GridLayout;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.Point;
 
 import java.util.ArrayList;
 import java.util.Date;
 
 import javax.swing.AbstractAction;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JToolBar;
 import javax.swing.KeyStroke;
 
 import javax.swing.BorderFactory; 
 //import javax.swing.border.Border;
 import javax.swing.border.TitledBorder;
 
 public class Tree extends JPanel implements JustificationAppliedListener, TreeSelectionListener, BoardDataChangeListener
 {
 	private static final long serialVersionUID = -2304281047341398965L;
 
 	private class TreeToolbar extends JPanel implements ActionListener {
 		private static final long serialVersionUID = 8572197337878587284L;
 
 		JButton addChild = new JButton(new ImageIcon("images/AddChild.png"));
 		JButton delChild = new JButton(new ImageIcon("images/DelChild.png"));
 		JButton merge = new JButton(new ImageIcon("images/Merge.png"));
 		JButton collapse = new JButton(new ImageIcon("images/Collapse.png"));
 
 		TreeToolbar()
 		{
 			this.setLayout(new GridLayout(2,2));
 			add(addChild);
 			addChild.addActionListener(this);
 			addChild.setEnabled(true);
 			addChild.setToolTipText("Add node");
 			//addChild.setEnabled(false);
 			//addChild.setToolTipText("Add node (select justification first)");
 			add(delChild);
 			delChild.addActionListener(this);
 			delChild.setToolTipText("Remove currently selected node");
 			add(merge);
 			merge.addActionListener(this);
 			merge.setToolTipText("Merge nodes");
 			add(collapse);
 			collapse.addActionListener(this);
 			collapse.setToolTipText("Collapse nodes");
 		}
 
 		public void actionPerformed(ActionEvent e){
 			if( e.getSource() == addChild ){
 				addChildAtCurrentState();
 			} else if( e.getSource() == delChild ){
 				delChildAtCurrentState();
 			} else if( e.getSource() == merge ){
 				mergeStates();
 			} else if( e.getSource() == collapse ){
				collapseStates();
 			}
 		}
 
 	}
 
 	private TreeToolbar toolbar = new TreeToolbar();
 	TreePanel treePanel = new TreePanel();
 	private LEGUP_Gui gui;
 	
 	private JLabel status = new JLabel();
 	
 	Tree( LEGUP_Gui gui ){
 //		super("LEGUP");
 		
 		this.gui = gui;
 		
 		JPanel main = new JPanel();
 		
 		main.setLayout( new BorderLayout() );
 		
 		main.add(toolbar,BorderLayout.WEST);
 		main.add(treePanel,BorderLayout.CENTER);
 		
 		//status.setPreferredSize(new Dimension(150,20));
 		main.add(status,BorderLayout.SOUTH);
 				
 		TitledBorder title = BorderFactory.createTitledBorder("Tree");
 		title.setTitleJustification(TitledBorder.CENTER);
 		main.setBorder(title);
 		
 		setLayout( new BorderLayout() );
 		add(main);
 		
 		// listeners
 		JustificationFrame.addJustificationAppliedListener(this);
 		gui.legupMain.getSelections().addTreeSelectionListener(this);
 		BoardState.addCellChangeListener(this);
 		setupKeyEvents();
 	}
 	
 	/**
 	 * Initializes key receptors on this and children components
 	 */
 	private void setupKeyEvents()
 	{
 		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0);
 		this.getInputMap(javax.swing.JComponent.WHEN_FOCUSED).put(stroke, "KeyEvent.VK_UP");
 		this.getActionMap().put("KeyEvent.VK_UP", new AbstractAction() {private static final long serialVersionUID = -2304281047341398965L; public void actionPerformed(ActionEvent event) {navigateTree(KeyEvent.VK_UP);}});
 		
 		stroke = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0);
 		this.getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(stroke, "KeyEvent.VK_DOWN");
 		this.getActionMap().put("KeyEvent.VK_DOWN", new AbstractAction() {private static final long serialVersionUID = -2304281047341398965L; public void actionPerformed(ActionEvent event) {navigateTree(KeyEvent.VK_DOWN);}});
 		
 		stroke = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0);
 		this.getInputMap(javax.swing.JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(stroke, "KeyEvent.VK_LEFT");
 		this.getActionMap().put("KeyEvent.VK_LEFT", new AbstractAction() {private static final long serialVersionUID = -2304281047341398965L; public void actionPerformed(ActionEvent event) {navigateTree(KeyEvent.VK_LEFT);}});
 		
 		stroke = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0);
 		this.getInputMap(javax.swing.JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(stroke, "KeyEvent.VK_RIGHT");
 		this.getActionMap().put("KeyEvent.VK_RIGHT", new AbstractAction() {private static final long serialVersionUID = -2304281047341398965L; public void actionPerformed(ActionEvent event) {navigateTree(KeyEvent.VK_RIGHT);}});
 		/*
 		stroke = KeyStroke.getKeyStroke(KeyEvent.VK_UP, 2);
 		this.getInputMap(javax.swing.JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(stroke,"Ctrl-KeyEvent.VK_UP");
 		this.getActionMap().put("Ctrl-KeyEvent.VK_UP",new AbstractAction(){});
 		
 		stroke = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 2);
 		this.getInputMap(javax.swing.JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(stroke,"Ctrl-KeyEvent.VK_DOWN");
 		this.getActionMap().put("Ctrl-KeyEvent.VK_DOWN",new AbstractAction(){});
 		
 		stroke = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 2);
 		this.getInputMap(javax.swing.JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(stroke,"Ctrl-KeyEvent.VK_LEFT");
 		this.getActionMap().put("Ctrl-KeyEvent.VK_LEFT",new AbstractAction(){});
 		
 		stroke = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 2);
 		this.getInputMap(javax.swing.JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(stroke,"Ctrl-KeyEvent.VK_RIGHT");
 		this.getActionMap().put("Ctrl-KeyEvent.VK_RIGHT",new AbstractAction(){});*/
 		
 	}
 	
 	
 	/**
 	 * Add a child to the sate that is currently selected
 	 *
 	 */
 	public void addChildAtCurrentState()
 	{
 		treePanel.addChildAtCurrentState(currentJustificationApplied);
 	}
 	
 	/**
 	 * Collapse states in the tree view
 	 */
 	public void collapseStates()
 	{
 		treePanel.collapseCurrentState();
 	}
 	
 	/**
 	 * Merge the selected states
 	 *
 	 */
 	public void mergeStates()
 	{
 		treePanel.mergeStates();
 	}
 	
 	/**
 	 * Delete the child subtree starting at the current state
 	 */
 	public void delChildAtCurrentState()
 	{
 		treePanel.delChildAtCurrentState();
 	}
 	
 	/**
 	 * Delete the current state and reposition the children
 	 */
 	public void delCurrentState()
 	{
 		treePanel.delCurrentState();
 	}
 
 	public void justificationApplied(BoardState state, Justification j)
 	{
 		System.out.println(j);
 		//comments to remove graying of addnode button, due to glitches caused - Avi
 		/*if (j == null){
 			toolbar.addChild.setEnabled(false);
 			toolbar.addChild.setToolTipText("Add node (select justification first)");
 		} else {
 			toolbar.addChild.setEnabled(true);
 			toolbar.addChild.setToolTipText("Add node");
 		}*/
 		currentJustificationApplied = j;
 		j = null;
 		repaint();
 	}
 	
 	public Justification getCurrentJustificationApplied(){
 		return currentJustificationApplied;
 	}
 	private Justification currentJustificationApplied = null;
 	
 	public void treeSelectionChanged(ArrayList <Selection> newSelectionList)
 	{
 		System.out.println("tree select changed");
 		updateStatus();
 	}
 	
 	public void boardDataChanged(BoardState state)
 	{
 		System.out.println("board data changed");
 		updateStatus();
 	}
 	
 	public void updateStatus()
 	{
 		ArrayList <Selection> newSelectionList = gui.legupMain.getSelections().getCurrentSelection();
 		
 		if (newSelectionList != null && newSelectionList.size() == 1 
 				&& newSelectionList.get(0).isState())
 		{
 			Selection newSelection = newSelectionList.get(0);
 			BoardState newState = newSelection.getState();
 			this.status.setText("States: " + newState.countStates() + " Branches: " + newState.countLeaves() + " Max Depth: " + newState.countDepth());
 		}
 		else
 		{
 			this.status.setText("");
 		}
 	}
 	
 	
 	private long keyPressTime = 0;
 	private int lastKeyDirection = -1;
 	private void navigateTree(int direction)
 	{
 		Date now = new Date();
 		if(now.getTime() < keyPressTime + 200 && lastKeyDirection == direction)
 		{
 			return;
 		}
 		keyPressTime = now.getTime();
 		lastKeyDirection = direction;
 		
 		ArrayList<Selection> s = gui.legupMain.getSelections().getCurrentSelection();
 		if(s == null)
 		{
 			return;
 		}
 		
 		if(s.size() != 1 || s.get(0).isTransition())
 		{
 			return;
 		}
 		BoardState state = s.get(0).getState();
 		
 		if(direction == KeyEvent.VK_UP)
 		{
 			BoardState parent = state.getSingleParentState();
 			if(parent != null)
 			{
 				gui.legupMain.getSelections().setSelection(new Selection(parent, false));
 			}
 		}
 		else if(direction == KeyEvent.VK_DOWN)
 		{
 			if(state.getTransitionsFrom().size() > 0)
 			{
 				BoardState child = state.getTransitionsFrom().get(0);
 				if(child != null)
 				{
 					gui.legupMain.getSelections().setSelection(new Selection(child, false));
 				}
 			}
 		}
 		else if(direction == KeyEvent.VK_RIGHT)
 		{
 			BoardState parent = state.getSingleParentState();
 			if(parent != null)
 
 			{
 				if(parent.getTransitionsFrom().size() > 1)
 				{
 					for(int x = 0; x < parent.getTransitionsFrom().size() - 1; ++x)
 					{
 						if(parent.getTransitionsFrom().get(x) == state)
 						{
 							BoardState sib = parent.getTransitionsFrom().get(x + 1);
 							if(sib != null)
 							{
 								gui.legupMain.getSelections().setSelection(new Selection(sib, false));
 							}
 						}
 					}
 				}
 			}
 		}
 		else if(direction == KeyEvent.VK_LEFT)
 		{
 			BoardState parent = state.getSingleParentState();
 			if(parent != null)
 			{
 				if(parent.getTransitionsFrom().size() > 1)
 				{
 					for(int x = parent.getTransitionsFrom().size() - 1; x > 0; --x)
 					{
 						if(parent.getTransitionsFrom().get(x) == state)
 						{
 							BoardState sib = parent.getTransitionsFrom().get(x - 1);
 							if(sib != null)
 							{
 								gui.legupMain.getSelections().setSelection(new Selection(sib, false));
 							}
 						}
 					}
 				}
 			}
 		}
 		
 		// TODO snap to current selection
 		Point draw = (Point)s.get(0).getState().getLocation().clone();
 		double scale = treePanel.getScale();
 		treePanel.moveX = (treePanel.getWidth()/(scale*2))-draw.x;
 		treePanel.moveY = (treePanel.getHeight()/(scale*2))-draw.y;
 		
 		treePanel.repaint();
 		Legup.getInstance().refresh();
 		
 	
 	}
 }
