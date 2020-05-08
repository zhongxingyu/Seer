 import java.awt.Component;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JTable;
 import javax.swing.event.PopupMenuEvent;
 import javax.swing.event.PopupMenuListener;
 
 /**
  * Clasa ce implementeaza un popup menu cu 2 itemi
  * @author silvia
  *
  */
 public class CustomPopupMenu extends JPopupMenu{
 	JTable table;
 	TableView panel;
 	int tableRow;
 	int tableColumn;
 	
 	public CustomPopupMenu(String item1, String item2, final TableView panel) {
 		
 		super();
 		this.table = panel.table;
 		this.panel = panel;
 
    	    // Item 1
 	    JMenuItem menuItem1 = new JMenuItem(item1);
 	    menuItem1.addActionListener(new ActionListener() {			
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				JMenuItem it = (JMenuItem)arg0.getSource();
 				panel.mainFrame.mediator.sendRequest(
 						it.getText(),
						table.getModel().getValueAt(tableRow, tableColumn).toString(),						
						panel.userInfo.username,
						panel);
 			}
 		});
 	    this.add(menuItem1);
 
 	    // Item 2
 	    JMenuItem menuItem2 = new JMenuItem(item2);
 	    menuItem2.addActionListener(new ActionListener() {			
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				JMenuItem it = (JMenuItem)arg0.getSource();
 				System.out.println(it.getText());		
				panel.mainFrame.mediator.sendRequest(
						it.getText(),
						table.getModel().getValueAt(tableRow, tableColumn).toString(),
						panel.userInfo.username,
						panel);
 			}
 		});	    
 	    this.add(menuItem2); 
 	}
 	
 	public void show(Component invoker, int x, int y){
 		super.show(invoker,x,y);
 
 		Point p = new Point(x, y);
 		tableRow = table.rowAtPoint(p);  
 		tableColumn = table.columnAtPoint(p); 
 		System.out.println("Row: " + tableRow + "; Col: " + tableColumn);
 	}
 }
