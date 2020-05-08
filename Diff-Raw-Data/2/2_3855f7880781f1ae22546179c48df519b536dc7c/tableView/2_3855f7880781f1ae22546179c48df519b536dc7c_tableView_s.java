 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.ArrayList;
 
 
 public class tableView extends JPanel {
 	
 	
 	tableView() throws Exception{
 	ClientMethods methods = new ClientMethods();
 	ArrayList<Order> orders = methods.listOrders();
 	Object[][] data = new Object[orders.size()][8];
 	String[] columnNames = {"Order ID", "KID", "Status", "Time ordered", "Deliverytime", "Deliveryadress", "dishes", "Price"};
 	
 	
 	
 	for(Order aOrder : orders){
 	for(int i=0;i<orders.size();i++){
 		for(int y=0;y<8;y++){
 			if(y==0){
 				data[i][y]=aOrder.getOrderid();
 			} else if(y==1){
 				data[i][y]=aOrder.getKid();
 			} else if(y==2){
 				data[i][y]=aOrder.getStatus();
 			} else if(y==3){
 				data[i][y]=aOrder.getOrderTime();
 			} else if(y==4){
 				data[i][y]=aOrder.getDeliveryTime();
 			} else if(y==5){
 				data[i][y]=aOrder.getDeliveryAdress();
 			} else if(y==6){
 				data[i][y]=aOrder.getOrderContent();
 			} else if(y==7){
 				data[i][y]=aOrder.getPrice();	
 			} else {
 				System.out.println("Feil!");
 			}
 		}
 	 }
 	}
 
 	
 	final JTable table = new JTable(data, columnNames);
    table.setPreferredScrollableViewportSize(new Dimension(500, 70));
     table.setFillsViewportHeight(true);
     
     JScrollPane scrollPane = new JScrollPane(table);
     
 	add(scrollPane);
 	}
 	
     static void createAndShowGUI() throws Exception {
         //Create and set up the window.
         JFrame frame = new JFrame("Orderlist");
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  
         //Create and set up the content pane.
         tableView newContentPane = new tableView();
         newContentPane.setOpaque(true); //content panes must be opaque
         frame.setContentPane(newContentPane);
  
         //Display the window.
         frame.pack();
         frame.setVisible(true);
     }
  
     public static void main(String[] args) {
         //Schedule a job for the event-dispatching thread:
         //creating and showing this application's GUI.
         javax.swing.SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 try {
 					createAndShowGUI();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
             }
         });
     }
 }
