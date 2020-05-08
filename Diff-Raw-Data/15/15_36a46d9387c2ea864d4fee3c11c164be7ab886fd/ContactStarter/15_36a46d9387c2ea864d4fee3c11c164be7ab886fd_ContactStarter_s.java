 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 
 public class ContactStarter {
 	
 	private ContactManagerImpl newContactManager = new ContactManagerImpl();
 	
 	public static void main(String[] args) {
         ContactStarter launcher = new ContactStarter();
         launcher.launch();
     }
 
     public void launch() {
     	
     	newContactManager.readIn();
     	welcome();
     }
 
 	public void welcome(){
 		
 		
		String str = "";
 		
 		int selection = 0;
 		
         System.out.println("------------------------------------------------------");
 		System.out.println("Welcome to the new and improved Contact Manager Pro!");
 		System.out.println("What would you like to do?");
 		System.out.println("");
 		System.out.println("Enter 1 to add a new contact");
 		System.out.println("Enter 2 to add a new meeting");
 		System.out.println("Enter 3 to get contact information");
 		System.out.println("Enter 4 to get meeting information");
 		System.out.println("Enter 5 to edit meeting notes");
 		System.out.println("Enter EXIT to exit the program");		
 		System.out.println("");
 
 		while(selection == 0){
 			
 			str = getInput();
 			
 			if(str.equals("exit")){
 				newContactManager.flush();
 				break;
 			}
 			
 			try{
 				selection = Integer.parseInt(str);
 
 			} catch (NumberFormatException ex){
 				System.out.println("That was not an integer! Please try again.");
 			}
 			
 			if(selection > 5 || selection < 1){
 				System.out.println("That was not an option, please try again.");
 				selection = 0;
 				System.out.println("Selection is zero");
 			}
 		}
 
 		mainMenu(selection);
 	}
 
 	public void mainMenu(int a){
 
 		if(a == 1){//Adds contact
 			System.out.println("");
 			System.out.print("You have selected add new Contact, begin by entering the contacts name or type  EXIT to return to the main menu: ");
 			String name = getInput();
 			if(name.equals("exit")){
 				welcome();
 			}
 			System.out.print("Now please enter any notes that you may have regarding the contact: ");
 			String notes = getInput();
 
 			newContactManager.addNewContact(name, notes);
 
 			System.out.println("");
 			System.out.println("Thank you, your new contact has now been added to the database, you will now be returned to the main menu.");
 			System.out.println("");
 			welcome();
 		}
 
 
 		if(a == 2){//Adds meeting
 			System.out.println("");
 			newContactManager.makeMeeting();
 			welcome();
 		}
 		
 		if(a == 3){//returns selected contact
 			
 			System.out.println("");
 			System.out.println("Please enter the contact name or each contact id you require one by one, enter F to finish: ");
 			String getContact = getInput();
 			ArrayList<Integer> contactIDs = new ArrayList<Integer>();
 			
 			if(Character.isDigit(getContact.charAt(0))){
 				while(!getContact.equals("f")){
 					int newID = getInt(getContact);
 					contactIDs.add(newID);
 					getContact = getInput();
 				}
 				
 				int[] idsToPass = new int[contactIDs.size()];
 				for(int i = 0; i < idsToPass.length; i++){
 					
 					idsToPass[i] = contactIDs.get(i).intValue();
 				}
 				
 				newContactManager.getContacts(idsToPass);
 				
 			} else {	
 				if(newContactManager.getContacts(getContact).equals(null)){
 					System.out.println("that contact does not exist!");
 				} else {
 					newContactManager.getContacts(getContact);
 				}
 			}
 			welcome();
 		}
 		
 		if(a == 4){//returns selected meeting/meetings
 			
 			System.out.println("");
 			System.out.println("Do you wish to access a past(P) or a future(F) meeting? ");
 			String str = getInput();
 			
 			while(str!= "p" || str!= "f"){
 				if(str.equals("p")){
 					accessPastMeeting();
 					break;
 				} else if(str.equals("f")){
 					accessFutureMeeting();
 					break;
 				} else {
 					System.out.println("That was not an option, please try again.");
 				}
 				str = getInput();
 			}
 			welcome();
 		}
 		
 		if(a == 5){//adds meeting notes
 			
 			System.out.println("");
 			System.out.println("You have selected add meeting notes, please enter the ID of the meeting you wish to change: ");
 			String str = getInput();
 			int iD = getInt(str);
 			
 			System.out.println("Now please enter the notes you wish to add: ");
 			String newNotes = getInput();
 			newContactManager.addMeetingNotes(iD, newNotes);
 			
 			welcome();
 		}
 	}
 
 	public String getInput(){
 		String str = "";
 		String returnStr = null;
 		while(str == ""){
 			try{
 				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
 				str = bufferedReader.readLine();
 			} catch (IOException ex){
 				ex.printStackTrace();
 			}
 			returnStr = str.toLowerCase();
 		}
 		return returnStr;
 	}
 	
 	public void accessFutureMeeting(){
 		System.out.println("You have selected access future meeting, now please either the ID of the meeting you would like to view, or alternatively enter a contact name or a date(DD/MM/YYYY) to view all associated meetings: ");
 		String str = getInput();
 		
 		if(Character.isDigit(str.charAt(0)) && str.length() == 10){
 			newContactManager.getFutureMeetingList(newContactManager.getDate(str));
 		} else if(Character.isDigit(str.charAt(0))){
 			int id = getInt(str);
 			newContactManager.getFutureMeeting(id);
 		} else {
 			newContactManager.getFutureMeetingList(newContactManager.getContact(str));
 		}
 		
 		welcome();
 	}
 	
 	public void accessPastMeeting(){
 		System.out.println("You have selected access past meeting, now please either the ID of the meeting you would like to view, or alternatively enter a contact name to view all associated meetings: ");
 		String str = getInput();
 		
 		try{
 			if(Character.isDigit(str.charAt(0))){
 				int id = getInt(str);
 				newContactManager.getPastMeeting(id);
 			
 			} else {
 				newContactManager.getPastMeetingList(newContactManager.getContact(str));
 			}
 		} catch(NumberFormatException ex){
 			System.out.println("that was not an number or a name, please try again.");
 		}
 		welcome();
 	}
 	
 	public int getInt(String input){
 		int returnInt = Integer.parseInt(input);
 		return returnInt;
 	}
 	
 }
