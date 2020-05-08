 import java.util.ArrayList;
 import java.util.Scanner;
 
 import javax.swing.JTable;
 
 
 public class Mediator {
 	
 	static ArrayList<UserThread> users = new ArrayList<UserThread>(); 
 	
 	Mediator(){		
 	}
 	
	void sendRequest(String msg, String product, String client, TableView userPanel){
 		
 		System.out.println("!! "+msg+" "+product);
 		
 		if(msg.equals("Launch Offer request")){
 			for(int i=0;i<users.size();i++){
 				MainWindow user = users.get(i).gui; 
 				if(user.uType == UserType.SELLER){
 					//parcurg tabela
 					JTable table = user.tableView.table;
 					for(int j=0;j<table.getRowCount();j++){
 						if(table.getModel().getValueAt(j, 0).toString().equals(product)){
 							table.getModel().setValueAt(client,j, 2);//!! hardcodat 
 						}
 					}
 				}
 			}
 		}
 		
 	}
 	
 	
 	public static void main(String[] args)
 	{		
 		
 		Mediator mediator = new Mediator();
 		Scanner sc = new Scanner(System.in);
 		while(true){
 			System.out.print("Type command (launch/exit): ");
 			String input = sc.nextLine();
 			if(input.equals("exit")){
 				System.exit(1);
 			}
 			if(input.equals("l")){
 				UserThread t = new UserThread(mediator);
 			    t.start();
 			    users.add(t);
 			}
 		}
 	}
 
 	public User readUserInformation(String username, String password, UserType uType)
 	{	
 		return InputParser.readInfoAboutUser(username, password, uType);
 	}
 
 }
