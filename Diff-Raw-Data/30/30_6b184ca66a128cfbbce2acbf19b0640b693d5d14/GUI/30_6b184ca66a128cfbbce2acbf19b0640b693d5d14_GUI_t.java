 import java.util.ArrayList;
 import javax.swing.JOptionPane;
 import static javax.swing.JOptionPane.*;
 
 class GUI {
 	public static void main(String[] args) throws Exception {
     ClientMethods methods = new ClientMethods();
 
     String[] choices = {"Customer menu", "Order menu", "Dish menu",  "Stockpile menu", "Economic menu", "Exit"};
     String[] cust = {"Add customer", "Find customer", "Edit customer", "Back"};
    String[] ord = {"List orders", "Add order", "Edit order", "Back"};
     String[] dish = {"List dishes", "Add dish", "Find dish", "Back"};
     String[] stock = {"List stockpile", "Add ingredient", "Update stockpile", "Back"};
     String[] eco = {"Check funds", "Withdraw", "Deposit", "Check profit"};
 
     boolean meny1 = true;
     while(meny1) {
     int choice = showOptionDialog(null, "Choose Sub-menu: ", "Main Menu: CateringBuddy", 0, PLAIN_MESSAGE, null, choices, choices[0]);
     switch(choice) {
 		case 0: //Customers
 			int cuschoice = showOptionDialog(null, "Choose Customer function: ", "CateringBuddy", 0, PLAIN_MESSAGE, null, cust, cust[0]);
 			switch(cuschoice){
 				case 0: //Add Customer
 					boolean check = methods.regNewCustomer();
 					if(check){
 						showMessageDialog(null,"Registration successful.");
 					}else{
 						showMessageDialog(null,"No update was comitted."); //meldingen m utvides elnz
 					}
 					break;
 				case 1: //Find Customer
 					String kundeid = showInputDialog(null,"Enter customer identification number:");
 					int kid = Integer.parseInt(kundeid);
 					Customer hanher = methods.getCustomer(kid);
 					String kundeinfo = hanher.toString();
 					System.out.println(kundeinfo);
 					break;
 				case 2: //Edit Customer
 					kid = Integer.parseInt(showInputDialog(null,"Enter the customers telephone number: "));
 					if(methods.editCustomer(kid)) {
 						showMessageDialog(null,"The changes are saved successfully!");
 					}else{
 						showMessageDialog(null,"No update saved."); //meldingen m utvides elnz
 					}
 					break;
 				case 3:
 					break;
 				}
 			break;
 		case 1: //Orders
 			int ordchoice = showOptionDialog(null, "Choose Order function: ", "CateringBuddy", 0, PLAIN_MESSAGE, null, ord, ord[0]);
 			switch(ordchoice){
 				case 0: //List orders
 					tableView.createAndShowGUI();
 					break;
 				case 1: //add order
					int phone;
					try {
						phone = Integer.parseInt(showInputDialog(null,"Input the customers phone number"));
					} catch(NumberFormatException e) {
						break;
					}
 					boolean check = methods.addOrder(phone);
 					if(check){
 						showMessageDialog(null,"Registration successful.");
 					}else{
 						showMessageDialog(null,"No update was comitted."); //meldingen m utvides elnz
 					}
 					break;
 				case 2: //edit order
 					//find ordesronCustomer //
 					/*String customerphone = showInputDialog(null,"Enter customers phone number:");
 					int phone2 = Integer.parseInt(customerphone);
 					Customer mrdude = methods.getCustomer(phone2);
 				    int mrdudeKid = mrdude.getKid();
 				    ArrayList<Order> mrdudesOrders =  methods.listOrdersOnCustomer(mrdudeKid);
 				    */
 				    // n skal forhpentligvis alle ordre p nsket kunde vre i en liste //
 				    // s m man kunne redigere //
 				    
 					
 					
 					break;
 			}
 			break;
 		case 2:
 			int dischoice = showOptionDialog(null, "Choose Dish function: ", "CateringBuddy", 0, PLAIN_MESSAGE, null, dish, dish[0]);
 			switch(dischoice){
 				case 0: // list dishes //
 					ArrayList<Dish> dishes = methods.listDishes();
 					for(Dish aDish : dishes) {
 						System.out.println(aDish);
 					}
 					break;
 				case 1: // add dish //
 					boolean check = methods.addDish();
 					if(check){
 						showMessageDialog(null, "New dish is added to the menu.");
 					}else{
 						showMessageDialog(null, "The registtration failed. No dish added.");
 					}
 					break;
 					
 				case 2: // find dish//
 					String rettnavn = showInputDialog(null,"Skriv inn rettens navn:");
 					Dish retten = methods.findDish(rettnavn);
 					String rettinfo = retten.toString();
 					System.out.println(rettinfo);
 					break;
 				}
 				break;
 		case 3:
 			int stochoice = showOptionDialog(null, "Choose Stockpile function: ", "CateringBuddy", 0, PLAIN_MESSAGE, null, stock, stock[0]);
 			switch(stochoice){
 				case 0: // list stockpile //
 					stockpileView.createAndShowGUI();
 					break;
 				case 1: // add ingredient //
 					boolean check = methods.addIngredient();
 					if(check){
 						showMessageDialog(null,"Registration successful.");
 					}else{
 						showMessageDialog(null,"No update was comitted."); 
 					}
 					break;
 					
 				case 2: // update stockpile//
 
 				case 3: //
 					break;
 				}
 				break;
 		case 4:
 			int ecochoice = showOptionDialog(null, "Choose Economic function: ", "CateringBuddy", 0, PLAIN_MESSAGE, null, eco, eco[0]);
 			switch(ecochoice){
 				case 0:
 				}
 		case 5:
 		case -1:
 			System.exit(0);
 		default:
 			JOptionPane.showMessageDialog(null, "Unexpected Choice " + choice);
 		}
 	}
 	}
 }
 
 	/*
 	case 100:
 		//find ordesronCustomer //
 		String customerphone = showInputDialog(null,"Enter customers phone number:");
 		int phone = Integer.parseInt(customerphone);
 		Customer mrdude = methods.getCustomer(phone);
 	    String mrdudeKid = mrdude.getKid();
 	    ArrayList <Order> mrdudesOrders =  mrdude.listordersOnCustomer(mrdudeKid);
 	    
 	    // n skal forhpentligvis alle ordre p nsket kunde vre i en liste //
 	    // s m man kunne redigere //
 	*/ 
 	    
 		
 				
 		
 		
 		
 		
