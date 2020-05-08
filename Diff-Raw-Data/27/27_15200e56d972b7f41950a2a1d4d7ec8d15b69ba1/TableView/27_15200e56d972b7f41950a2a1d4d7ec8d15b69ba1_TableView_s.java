 import javax.naming.NoPermissionException;
 import javax.swing.DefaultCellEditor;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JProgressBar;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableCellRenderer;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.Label;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.ArrayList;
 
 class ProgressBarRenderer extends JProgressBar implements TableCellRenderer {
 
 	  public ProgressBarRenderer() {
 	    super();
 	  }
 
 	  public Component getTableCellRendererComponent(JTable table, Object value,
 	      boolean isSelected, boolean hasFocus, int row, int column) {
 		  setValue(50);
 		  setStringPainted(true);
 		  return this;
 	  }
 }
  
 class ButtonRenderer extends JButton implements TableCellRenderer {
 
 	  public ButtonRenderer() {
 	    setOpaque(true);
 	  }
 
 	  public Component getTableCellRendererComponent(JTable table, Object value,
 	      boolean isSelected, boolean hasFocus, int row, int column) {
 	    if (isSelected) {
 	      setForeground(table.getSelectionForeground());
 	      setBackground(table.getSelectionBackground());
 	    } else {
 	      setForeground(table.getForeground());
 	      setBackground(UIManager.getColor("Button.background"));
 	    }
 	    setText((value == null) ? "" : value.toString());
 	    return this;
 	  }
 }
 
 class ButtonEditor extends DefaultCellEditor {
 	  protected JButton button;
 
 	  private String label;
 
 	  private boolean isPushed;
 
 	  public ButtonEditor(JCheckBox checkBox) {
 	    super(checkBox);
 	    button = new JButton();
 	    button.setOpaque(true);
 	    button.addActionListener(new ActionListener() {
 	      public void actionPerformed(ActionEvent e) {
 	        fireEditingStopped();
 	      }
 	    });
 	  }
 }
 
 public class TableView extends JPanel {
 
 	private DefaultTableModel model = new DefaultTableModel();		//model tabel
     ArrayList<Object[]> bodyTable;
 	private Object[] headTable = { "Produs/Serviciu", "Status" ,"Furnizori", "StatusLicitatie", "Pret", "Progress Bar"};
 
 	public JTable table;  
 	MainWindow mainFrame;
 	User userInfo;
 	   	    
 	private JPopupMenu popupMenuProductsList;//nefolosit in cazul furnizorilor
 	private JPopupMenu popupMenuUsersList;
 
 	private JButton bLogout = new JButton("Logout");
 	
 	public TableView(User userInfo, String usersNameColumn, MainWindow frame)//TODO: datele vin ca parametri in constructor! 
 	{
 		super();
 		this.userInfo = userInfo;
 		headTable[2] = usersNameColumn;
 		
 		bodyTable = new ArrayList<Object[]>();
 		System.out.println(userInfo.products.size());
 		for(int i=0; i<userInfo.products.size(); i++) {
 			Product product = userInfo.products.get(i);
 			ArrayList<Object> objects = new ArrayList<Object>();
 			objects.add(product.name);
 			objects.add(product.status);
 			objects.add("");//user
 			objects.add(product.statusLicitatie.getName());
 			objects.add(product.pret);
 			objects.add(product.progressBar);
 			bodyTable.add(objects.toArray());
 		}
 		
 		init(userInfo.username, userInfo.uType);
 	}
    	  
    	 public void init(String username, UserType type)
    	 {
     	this.setLayout(new BorderLayout());
    	    JPanel tablePanel = new JPanel(new GridLayout(1, 0));
    	    JPanel bottomPanel = new JPanel(new GridLayout(0, 6));
    	    Object[][] body = new Object[bodyTable.size()][];
    	    bodyTable.toArray(body);
    	    model.setDataVector(body, headTable);
 
    	    table = new JTable(model);
         table.getColumn("Progress Bar").setCellRenderer(new ProgressBarRenderer());
 
         //adaugare popupMenu la right-click pe coloana produs sau user
         switch(type)
         {
         	case BUYER:
         	{
            	    popupMenuProductsList = new CustomPopupMenu(
            	    		"Launch Offer request",
            	    		"Drop Offer request",
            	    		this);
            	    popupMenuUsersList = new CustomPopupMenu(
         	    		"Accept Offer",
         	    		"Refuse Offer",
         	    		this);
            	    break;
         	}
         	case SELLER:
         	{ 
         		popupMenuUsersList = new CustomPopupMenu(
     	    		"Make offer",
     	    		"Drop auction",
     	    		this);
         		break;
         	}
         }
    	    table.addMouseListener(new TableMouseSelect(type, table));
    	    
    	    bLogout.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				//TODO - de verificat daca este furnizor si este intr-o licitatie NU are voie
 				System.exit(1);
 			}
 		});
 
 		JScrollPane scroll = new JScrollPane(table);
 		scroll.setPreferredSize(new Dimension(500, 100));
 		tablePanel.add(scroll);
 		//dummy panels
 		bottomPanel.add(new JPanel()); bottomPanel.add(new JPanel());
 		bottomPanel.add(new JPanel()); bottomPanel.add(new JPanel());
 		bottomPanel.add(new Label(username));
 		bottomPanel.add(bLogout);
 		this.add(bottomPanel, BorderLayout.SOUTH);
 		this.add(tablePanel, BorderLayout.CENTER);
 	}
 
      private class TableMouseSelect extends MouseAdapter {
     	 UserType type;
     	 JTable table;
     	 public TableMouseSelect(UserType type, JTable table) {
 			this.type = type;
 			this.table = table;
 		}
 		public void mouseReleased(MouseEvent evt) {  
 			if(evt.getSource() == table) {  
 				selectTableRow(evt,table, type);  
 			}  
 		}  
 		public void mousePressed(MouseEvent evt) {  
 			if(evt.getSource() == table) {  
 				selectTableRow(evt,table, type);  
 			}  
 		}  
 		public void mouseClicked(MouseEvent evt) {  
 			if(evt.getSource() == table) {  
 				selectTableRow(evt,table, type);
 			}  
 		}  
 	}    
      
 	private void selectTableRow(MouseEvent evt, JTable table, UserType type)
 	{
 		if (SwingUtilities.isRightMouseButton(evt))
 		{
 			Point p = evt.getPoint();
 			int row = table.rowAtPoint(p);  
 			int col = table.columnAtPoint(p);  
 			table.setRowSelectionInterval(row, row);
 			table.setColumnSelectionInterval(col,col);
 			
 			//coloana produs/serviciu
 			if(col == 0 && type.equals(UserType.BUYER)){
 				popupMenuProductsList.show(table, p.x, p.y);				
 			}
 			//coloana useri (furnizori/cumparatori)
 			else if(col == 2){
 				popupMenuUsersList.show(table, p.x, p.y);				
 			}	
 		}  
 	}
 	
 }
