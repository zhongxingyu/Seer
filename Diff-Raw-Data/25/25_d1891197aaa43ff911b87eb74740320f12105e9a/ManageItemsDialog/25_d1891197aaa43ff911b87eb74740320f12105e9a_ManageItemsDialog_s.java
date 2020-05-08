 package program;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.util.ArrayList;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JTable;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.JToolBar;
 import javax.swing.event.TableModelEvent;
 import javax.swing.event.TableModelListener;
 import javax.swing.table.AbstractTableModel;
 import javax.swing.table.TableColumn;
 import progAdmin.AddItemDialog;
 import net.miginfocom.swing.MigLayout;
 import reviewItems.ItemToReview;
 import reviewItems.ReadItemDialog;
 import utilities.FileHelper;
 import utilities.SwingHelper;
 import utilities.xml.XmlParser;
 
 /**
  *
  */
 public class ManageItemsDialog  extends JDialog implements MouseListener{
 	private static final long serialVersionUID = 1L;
 	JFrame parent;
 	ItemToReview item;
 	JTextField titleField;
 	JTextArea textArea;
 	ArrayList<ItemToReview> items;
 	final JTable table=new JTable();
 	final JPanel itemsPanel = new JPanel();
 //-----------------------------------------------------------------------------
 	public ManageItemsDialog(final JFrame parent){
 		super(parent, "Manage Review Items", true);
 		this.parent=parent;
 		
 		this.setPreferredSize(new Dimension(700,700));
 		this.setSize(new Dimension(700,700));
 
 		//Put the form in the middle of the screen
 		this.setLocationRelativeTo(null);
 
 		//Make sure that if the user hits the 'x', the window calls the closeAndCancel method
 		this.addWindowListener(new WindowAdapter() {
 			public void windowClosing(WindowEvent e) {
 				closeAndCancel();
 			}
 		});
 
 		final JPanel mainPanel = new JPanel(new MigLayout());
 		
 		JButton addNewItemButton = 
 				SwingHelper.createImageButton("Add Item", "icons/plusSign_48.png");
 		addNewItemButton.addActionListener(new ActionListener() {
 			AddItemDialog itemDialog = new AddItemDialog(parent);
 			public void actionPerformed(ActionEvent e) {
 				itemDialog.setVisible(true);
 				itemDialog.setModal(true);
 				refreshList();
 				
 			}
 		});
 		
 		JButton deleteItemButton = SwingHelper
 				.createImageButton("Delete Item", "icons/delete_48.png");
 		deleteItemButton.addActionListener(new ActionListener() {
 			//AddItemDialog itemDialog = new AddItemDialog(parent);
 			public void actionPerformed(ActionEvent e) {
 				int rowIndex = table.getSelectedRow();
 				if(rowIndex>=0){
 					((ItemsTableModel)table.getModel()).deleteRow(rowIndex);
 				}
 				items.remove(rowIndex);
 				try{
 					XmlParser.saveItemsToReviewList(items, FileHelper.getItemsToReviewFile());
 				}catch(Exception ex){
 					ex.printStackTrace();
 				}
 				refreshList();
 				//itemDialog.setVisible(true);
 				//itemDialog.setModal(true);
 			}
 		});
 		
 		JButton editItemButton = SwingHelper
 				.createImageButton("Edit Item", "icons/edit_48.png");
 		editItemButton.addActionListener(new ActionListener() {
 			//AddItemDialog itemDialog = new AddItemDialog(parent);
 			public void actionPerformed(ActionEvent e) {
 				int rowIndex = table.getSelectedRow();
 				if(rowIndex>=0){
 					//show the ReadItemDialog to display the item
 		            ReadItemDialog readItem = new ReadItemDialog(parent, items.get(rowIndex));
 		            readItem.makeEditable();
 		            readItem.setVisible(true);
 		            
 		            //wait on the ReadItemDialog to be closed
 		            readItem.setModal(true);
 				}
 			}
 		});
 		
 		mainPanel.add(addNewItemButton);
 
 		JToolBar toolbar = new JToolBar("Toolbar", JToolBar.HORIZONTAL);
 		toolbar.add(addNewItemButton);
 		toolbar.add(deleteItemButton);
 		toolbar.add(editItemButton);
 		
 		items = XmlParser.loadItemsToReviewList();
 		itemsPanel.add(createItemsPanel(items));
 		
 		//Create a split pane with the two scroll panes in it
 		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
 				mainPanel, itemsPanel);
 		
 		splitPane.setOneTouchExpandable(true);
 		splitPane.setDividerLocation(400);

		//Provide minimum sizes for the two components in the split pane
		//Dimension minimumSize = new Dimension(690, 50);
		//mainPanel.setMinimumSize(minimumSize);
		//itemsPanel.setMinimumSize(minimumSize);

 	    Container contentPane = getContentPane();
 	    contentPane.add(toolbar, BorderLayout.NORTH);
 	    contentPane.add(splitPane, BorderLayout.CENTER);
 	}
 //-----------------------------------------------------------------------------
 	public void refreshList(){
 		items = XmlParser.loadItemsToReviewList();
 		System.out.println("ManageItemsDialog: addNewItemButton ActionListener called");
 		System.out.println("ManageItemsDialog: addNewItemButton ActionListener: items.size() = " + items.size());
		//itemsPanel.removeAll();
		//itemsPanel.add(createItemsPanel(items));
 		((AbstractTableModel) table.getModel()).fireTableDataChanged();
		//repaint();
 	}
 //-----------------------------------------------------------------------------
 	private void saveAndClose(){
 		this.dispose();
 	}
 //-----------------------------------------------------------------------------
 	private void closeAndCancel(){
 
 		this.dispose();
 	}	
 //-----------------------------------------------------------------------------
 	private JPanel createItemsPanel(ArrayList<ItemToReview> items){
 		JPanel itemsPanel = new JPanel();		
 		
 		//Create initially empty table
 	    table.setShowGrid(true);
 	    table.setGridColor(Color.black);
 	    table.setPreferredScrollableViewportSize(new Dimension(675, 300));
 	    table.setFillsViewportHeight(true);
 	    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
 
 	    //Put the table in a scroll pane
 	    JScrollPane tableScrollPane = new JScrollPane();
 	    tableScrollPane.setViewportView(table);
 	    itemsPanel.setLayout(new BorderLayout());
 	    itemsPanel.add(tableScrollPane,BorderLayout.CENTER);
 	    tableScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 	    
 	    /*
 	     * Set the table model to be the one custom created for this table
 	     * and passing in the list of names for the shift
 	     */
 	    ItemsTableModel tableModel = new ItemsTableModel(items);
 	    tableModel.addTableModelListener(tableModel);
 	    table.setModel(tableModel);
 	    table.addMouseListener(this);
 
 	    //Resize the columns
 	    TableColumn col;
 	    int[] sizes = {100, 330, 240};
 	    
 	    for(int i=0; i<sizes.length; i++){
 		    col = table.getColumnModel().getColumn(i);
 		    col.setPreferredWidth(sizes[i]);
 		    col.setWidth(sizes[i]);
 	    }
 	    
 		return itemsPanel;
 	}
 //-----------------------------------------------------------------------------
 	private void refreshItemsPanel(){		
 
 	}
 //-----------------------------------------------------------------------------
 	@Override
 	public void mouseClicked(MouseEvent e){
 		//checks if it was a double click
         if (e.getComponent().isEnabled() && e.getButton() == 
         		MouseEvent.BUTTON1 && e.getClickCount() == 2){
         	
             Point p = e.getPoint();
             int row = table.rowAtPoint(p);
             
             //show the ReadItemDialog to display the item
             ReadItemDialog readItem = new ReadItemDialog(parent, items.get(row));
             readItem.setVisible(true);
             
             //wait on the ReadItemDialog to be closed
             readItem.setModal(true);
         }
     }
 //-----------------------------------------------------------------------------
 	@Override
 	public void mousePressed(MouseEvent e) {
 		
 	}
 //-----------------------------------------------------------------------------
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		
 	}
 //-----------------------------------------------------------------------------
 	@Override
 	public void mouseEntered(MouseEvent e) {		
 		
 	}
 //-----------------------------------------------------------------------------
 	@Override
 	public void mouseExited(MouseEvent e) {
 		
 	}
 //=============================================================================
 	private class ItemsTableModel extends AbstractTableModel implements TableModelListener {
 		private static final long serialVersionUID = 1L;
 		private String[] columnNames = {"Reviewed",
                                         "  ",
                                         "  "
                                         };
 		
         private Object[][] data = new Object[0][0];
 //-----------------------------------------------------------------------------
         public ItemsTableModel(ArrayList<ItemToReview> items) {
 
         	for (ItemToReview item : items) {
         		addRow(item);
         	}
         }
 //-----------------------------------------------------------------------------
         public int getColumnCount() {
         	return columnNames.length;
         }
 //-----------------------------------------------------------------------------
         public int getRowCount() {
             return data.length;
         }
 //-----------------------------------------------------------------------------
         public String getColumnName(int col) {
             return columnNames[col];
 	    }
 //-----------------------------------------------------------------------------
 	    public Object getValueAt(int row, int col) {
 	    	return data[row][col];
 	    }
 //-----------------------------------------------------------------------------
 	    /*
 	     * JTable uses this method to determine the default renderer/
 	     * editor for each cell.  If we didn't implement this method,
 	     * then the 'present' column would contain text ("true"/"false"),
 	     * rather than a check box.
 	     */
 	    @SuppressWarnings({ "unchecked", "rawtypes" })
 		public Class getColumnClass(int c) {
 	        return getValueAt(0, c).getClass();
 	    }
 //-----------------------------------------------------------------------------
 	    /*
 	     * Don't need to implement this method unless your table's
 	     * data can change.
 	     */
 	    public void setValueAt(Object value, int row, int col) {
 	
 	        data[row][col] = value;
 	
 	        if(col==1){
 	        	if(!((Boolean)value)){
 	        		data[row][2]="";
 	        	}
 	        }
 	        
 	        this.fireTableDataChanged(); 
 
 	    }
 //-----------------------------------------------------------------------------  
 		public void addRow(ItemToReview item) {  
 			Boolean reviewed;
 		    	
 	    	if(item.isReviewed()){
 	    		reviewed = new Boolean(true);
 	    	}else{
 	    		reviewed = new Boolean(false);
 	    	}
 		    	
 	        Object[] newRow = { reviewed, item.getTitle(), item.getDetails() };
 	
 	        int curLength = this.getRowCount();
 	        Object[][] newData = new Object[curLength+1][];
 	        System.arraycopy(data, 0, newData, 0, curLength);
 	        data=newData;
 	        data[curLength]=newRow;
 	        this.fireTableDataChanged();  
 	    }   
 //-----------------------------------------------------------------------------  
         public void deleteRow(int row) {  
        	System.out.println("\ndelet roow issss callled!");
 		    int curLength = this.getRowCount();
 		    Object[][] newData = new Object[curLength-1][];
 		    System.arraycopy(data, 0, newData, 0, row);
 		    System.arraycopy(data, row+1, newData, row, ((data.length-1)-row));
 		    data=newData;
 		    this.fireTableDataChanged();  
         }          
 //-----------------------------------------------------------------------------        
        public void tableChanged(TableModelEvent e) {
     	   if(table.getRowCount()<items.size()){
     		   this.addRow(items.get(items.size()-1));
     	   }
     	   
        }
  //-----------------------------------------------------------------------------
 	}
 //=============================================================================
 }
