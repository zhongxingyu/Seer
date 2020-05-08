 package gui.mapper;
 
 import java.awt.BorderLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.Iterator;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JScrollPane;
 import javax.swing.ListSelectionModel;
 
 import gui.menus.NodeOptions;
 import gui.Main.GUI;
 import hypeerweb.Node;
 
 /**
  *  Gui component for listing nodes in a HyperPeerWeb 
  *  
  * @author Matthew Smith
  *
  */
 public class NodeListing extends JPanel {
 	
 	/* Root of the GUI */
 	private GUI main;
 
 	/* Container for the List Panel */
 	private JScrollPane listPane;
 	
 	/* JList of nodes */
 	private JList nodeList;
 	
 	/* List model used to manipulate the list */
 	private DefaultListModel nodeListModel;
 	
 	/** When pushed it removes all nodes except an initial 0 node.*/
 	private JButton clearButton;
 	
 	public static final int MAX_NUMBER_OF_NODES = 128;
 	
	private int listSize = 1;
 	
 	/**
 	 * Creates and intializes a Node Listing
 	 * 
 	 * @param main
 	 */
 	public NodeListing(GUI main) {
 		this.main = main;
 		
 		init();
 		
 		initList();
 	}
 	
 	/**
 	 * Initializes GUI components
 	 */
 	public void init()
 	{
 		this.setLayout(new BorderLayout());
 		
 		nodeListModel = new DefaultListModel();
 
 		
 		nodeList = new JList(nodeListModel);
 		nodeList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
 		nodeList.setCellRenderer(new NodeCellRenderer(main));
 		nodeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		nodeList.addMouseListener(new PopupListener(new NodeOptions(main)));
 		
 		listPane = new JScrollPane(nodeList);
 		
 		//Build the clear button
 		clearButton = new JButton("Clear");
 		clearButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e) {
 				clearButtonPressed();				
 			} });
 		clearButton.setMnemonic(KeyEvent.VK_K);
 		add(listPane, BorderLayout.CENTER);
 		add(clearButton, BorderLayout.SOUTH);
 	}
 	
 	public int listSize(){
 		return listSize;
 	}
 	
 	public int getSelectedIndex(){
 		return nodeList.getSelectedIndex();
 	}
 	
 	public int getSelectedNodeId(){
         int index = nodeList.getSelectedIndex();
        
         if (index >= 0 && listSize != 0)
            return new Integer((String) nodeListModel.get(index));
         
         return index;
     }
 	
 	/**
 	 * updates the list of components used by the node list
 	 */
 	public void initList()
 	{
 		listSize = main.getHyPeerWeb().size();
 		/*for(int i = 0; i < MAX_NUMBER_OF_NODES; i++){
 			if(i >= listSize){
 				nodeListModel.addElement("");
 			} else {
 				nodeListModel.addElement(Integer.toString(i));
 			}
 		}*/
 		
 		//This does not guarantee order.
 		Iterator<Node> iter = main.getHyPeerWeb().copyNodeSet().iterator();
 		for(int i = 0; i < MAX_NUMBER_OF_NODES; i++)
 		{
             if (iter.hasNext())
             {
                 nodeListModel.addElement(iter.next().getWebId() + "");
             }
             else
             {
                 nodeListModel.addElement("");
             }
         }
 		
 	}
 	
 	/*public void increaseListSize(){
 		nodeListModel.set(listSize,Integer.toString(listSize));
 		listSize++;
 	}
 	
 	public void decreaseListSize(){
 		if(listSize > 1){
 			nodeListModel.set(listSize-1, "");
 			listSize--;
 		}
 	}*/
 	
 	public void updateList()
 	{
 	    listSize = main.getHyPeerWeb().size();
         Iterator<Node> iter = main.getHyPeerWeb().copyNodeSet().iterator();
         for(int i = 0; i < MAX_NUMBER_OF_NODES; i++)
         {
             if (iter.hasNext())
             {
                 nodeListModel.set(i,iter.next().getWebId() + "");
             }
             else
             {
                 nodeListModel.set(i,"");
             }
         }
 	}
 	
 	private void clearButtonPressed(){
 		main.getHyPeerWeb().clear();
 		for(int i = 1; i < listSize; i++){
 			nodeListModel.set(i,"");
 		}
		listSize = 1;
 	}
 	
 	/**
 	 * Helper class for displaying the popup menu on a selected object
 	 * 
 	 * You may want to design the popup menu listener work better with your implementation
 	 * 
 	 * @author Matthew Smith
 	 *
 	 */
 	private class PopupListener extends MouseAdapter {
 		
 		JPopupMenu cellOptions;
 		
 		public PopupListener (JPopupMenu cellOptions)
 		{
 			this.cellOptions = cellOptions;
 		}
 		
 	    public void mousePressed(MouseEvent e) {
 	        	maybeShowPopup(e);
 	    }
 
 	    public void mouseReleased(MouseEvent e) {
 	        	maybeShowPopup(e);
 	    }
 
 	    private void maybeShowPopup(MouseEvent e) {
 	    	int index = nodeList.locationToIndex(e.getPoint());
 	        if (e.isPopupTrigger()) {  
 
 	        	//A right click won't set the selectedIndex but this routing will be called,
 	        	//so this sets the index.
 	            nodeList.setSelectedIndex( nodeList.locationToIndex(e.getPoint()) );
 
 	        	cellOptions.show(e.getComponent(), e.getX(), e.getY());
 	        }
 	    }
 	}
 
 }
