 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
import javax.swing.JTable;
 import javax.swing.table.DefaultTableModel;
 
 
 public class Controller {
 
 	public static void main(String[] args) {
 		
 		new Controller();
 		
 	}
 	
 	private View view;
 	
 	public Controller() {
 		
 		view = new View();

		view.getTable().setModel(new DefaultTableModel());
 		
 		view.getBtnAdd().addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				
 				String name = view.getTxtName().getText();
				String number = view.getTxtNumber().getText();
 				String email = view.getTxtEmail().getText();
 				
				DefaultTableModel model = view.getTable().getModel();
				model.addRow(new Object[] {name, number, email});
 				
 			}
 		});
 		
 	}
 
 }
