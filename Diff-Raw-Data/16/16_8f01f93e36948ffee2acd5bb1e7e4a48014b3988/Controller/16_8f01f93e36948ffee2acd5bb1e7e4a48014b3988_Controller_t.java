 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.table.DefaultTableModel;
 
 
 public class Controller {
 
 	public static void main(String[] args) {
 		
 		new Controller();
 		
 	}
 	
 	private View view;
 	
 	public Controller() {
 		
 		view = new View();
		view.setVisible(true);
		
		view.getTable().setModel(new DefaultTableModel(new Object[][] {}, new Object[] {"Name", "Email", "Number"}));
 		
 		view.getBtnAdd().addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				
 				String name = view.getTxtName().getText();
 				String email = view.getTxtEmail().getText();
				String number = view.getTxtNumber().getText();
 				
				DefaultTableModel model = (DefaultTableModel)view.getTable().getModel();
				model.addRow(new Object[] {name, email, number});
 				
 			}
 		});
 		
 	}
 
 }
