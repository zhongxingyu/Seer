 package client;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.Socket;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 import java.util.HashMap;
 import java.util.HashSet;
import java.util.List;
 import java.util.Set;
 
 import server.AutocorrectEngines;
 import ClientServerRequests.Request;
 import ClientServerRequests.RequestReturn;
 import GUI.LoginWindow;
 import GUI2.GUI2Frame;
 import UserInfo.Account;
 import UserInfo.Ingredient;
 import UserInfo.Invitation;
 import UserInfo.Kitchen;
 import UserInfo.KitchenEvent;
 import UserInfo.KitchenName;
 import UserInfo.Recipe;
 
 /**
  * Client class opens login window and then home window. Contains socket for communication with server.
  * @author esfriedm
  *
  */
 public class Client extends Thread {
 	
     private Socket _kkSocket = null;
     private ObjectOutputStream _out = null;
     private ObjectInputStream _in = null;
     private LoginWindow _login = null;
     private GUI2Frame _gui = null;
     private HashMap<KitchenName, Kitchen> _kitchens;
     private HashMap<String,KitchenName> _kitchenIdToName;
     private Set<String> _kitchenNames;
     private boolean _running;
     private AutocorrectEngines _autocorrect;
     private KitchenName _currentKitchen;
     private String _id;
     private String _newKitchen = "";
     private boolean _verified;
 	
     public Client(String hostname, int port) throws IOException {
     	///System.out.println("IN CLIENT CONSTRUCTOR");
         try {
             _kkSocket = new Socket(hostname, port);
             _out = new ObjectOutputStream(_kkSocket.getOutputStream());
             _in = new ObjectInputStream(_kkSocket.getInputStream());
             _login = new LoginWindow(this);
             _running = true;
             this.run();
 
         } catch (UnknownHostException e) {
             System.err.println("Don't know about host: " + hostname);
             System.exit(1);
         } catch (IOException e) {
             System.err.println("Couldn't get I/O for the connection to: " + hostname);
             System.exit(1);
         }       
 
     }
     
     /**
      * Sends object to server.
      * @param o : object to send to server.
      * @throws IOException
      */
     public void send(Object o){
     	try{
     		//System.out.println("SENDING: " + o);
     		_out.writeObject(o);
         	_out.flush();
         	_out.reset();
     	} catch(IOException e){
     		System.out.println("ERROR: Could not send request from client to server. " + e.getMessage());
     	}
     }
     
     /**
      * Sends username and password to server to retrieve existing account or create new one.
      * @param username
      * @param password
      * @throws IOException
      */
     public void checkPassword(String username, String password) throws IOException{
     	Request userPass;
     	if (_login.isNewAccount()){
     		//System.out.println("NEW ACCOUNT");
     		userPass = new Request(13);
     	} else {
     		userPass = new Request(1);
     	}
     	userPass.setUsername(username);
     	userPass.setPasword(password);
     	//System.out.println("about to send " + username + " x " + password);
     	send(userPass);
     }
 
     /**
      * Loop to receive communication from the server.
      */
     public void run(){
     	try {
 			RequestReturn response;
 			String input;
 			_verified = false;
 			//Wait to receive account verification
 			while (!_verified){
 				//Response will have 
 				//TODO: Deal with catching if someone tries to open the same account from somewhere else.
 				response = (RequestReturn) _in.readObject();
 				//System.out.println("received  password response");
 				if (response != null){
 					int type = response.getType();
 					//System.out.println("got response");
 					assert(type == 1);
 					if (response.getCorrect()){
 						if (_login.isNewAccount()){
 							//System.out.println("read as new account");
 							_login.dispose();
 							_login = new LoginWindow(this);
 						} else {
 							_verified = true;	//successful login
 							System.out.println("read as login");
 							_autocorrect = response.getAPIInfo();
 							_login.dispose();
 							_kitchens = response.getKitchenMap();
 							_kitchenIdToName = kitchenIdMap(_kitchens);
 							_kitchenNames = kitchenNameSet(_kitchens);
 							_id = response.getAccount().getID();
 							System.out.println("SHOULD CREATE NEW GUI");
 							_gui = new GUI2Frame(this, response.getAccount(), _kitchens, _autocorrect);
 							
 						
 						}
 					} else {
 						System.out.println("SHOULD DISPLAY: " + response.getErrorMessage());
 						
 						_login.displayIncorrect(response.getErrorMessage());
 					}
 					
 				} else {
 					System.out.println("is server disconnected??");
 					_login.displayIncorrect("Sorry! The server is down.");
 				}
 			}
 			while(_running){
 				try{
 					response = (RequestReturn) _in.readObject();
 					if (response != null){
 						int type = response.getType();
 						if(type == 2){
 							Kitchen k = response.getKitchen();
 							System.out.println("got new kitchen: " + k.getName());
 							_kitchens.put(k.getKitchenName(), k);
 							_kitchenIdToName.put(k.getKitchenName().getID(), k.getKitchenName());
 							System.out.println(_kitchenIdToName);
 							_kitchenNames.add(k.getKitchenName().getName());
 							_gui.updateKitchenDropDown();
 							_gui.refreshSearchAccordian();
 							System.out.println("well I'm here");
 							if(_currentKitchen != null || k.getKitchenName().getName().equals(_newKitchen)){
 								System.out.println("got Kitchen: " + k.getName());
 								if(_currentKitchen != null){
 									if(_currentKitchen.equals(k.getKitchenName())){
 										System.out.println("new kitchen is gui's current!!!");
 										_gui.updateKitchen(); 
 									} else if (k.getKitchenName().getName().equals(_newKitchen)){
 										System.out.println("this is the new kitchen");
 										System.out.println(_kitchenIdToName);
 										_gui.passNewKitchen(k);
 										_gui.displayNewKitchen(k);
 									} //TODO: this is ugly as shit. Better way to do it? I'm too tired to think.
 								} else if (k.getKitchenName().getName().equals(_newKitchen)){
 									System.out.println("this is the new kitchen");
 									_gui.displayNewKitchen(k);
 								}
 							}
 						}
 						else if(type == 3){
 							_gui.sendInvite(response.getInvitation());
 						}
 						else if(type == 4){
 							System.out.println("TYPE IS 19 IN CLIENT");
 							//If the user exists, then it is not a valid user.
 							boolean userInDatabase = (response.getUserInDatabase());
 							_gui.sendEmail(userInDatabase);
 						}
 						else if(type == 5){
 							System.out.println("PASSWORDS MATCH!!!!!!");
 							boolean passWordsMatch = (response.getPasswordsMatch());
 							_gui.changePasswords(passWordsMatch);
 						}
 					}
 				}
 				catch(SocketException se){
 					//Socket closed, probably due to close being called!
 					//If on error, server will handle clean up
 				}
 			}
 			
     	} catch (Exception e) {
     		if(!_verified){
 				_login.displayIncorrect("We're sorry but the server is down. Apologies!");
 
     		}
     		else{
     			//TODO: GUI POP UP THAT SERVER IS DOWN
     			e.printStackTrace();
     		}
     	}
 
     	
     }
     
     /*
      * Below are methods to create requests to send to the server.
      * The number scheme below represents requests numberings.
 	 * 1 = verify account 
 	 * 2 = get kitchen
 	 * 3 = add kitchen user
 	 * 4 = remove kitchen user
 	 * 5 = add event
 	 * 6 = remove event
 	 * 7 = add recipe
 	 * 8 = remove recipe
 	 * 9 = store add ingredient to kitchen fridge
 	 * 10 = remove ingredient from kitchen fridge
 	 * 11 = store account
 	 * 12 = close client
 	 * 13 = make account
 	 * 14 = make kitchen
 	 * 15 = invite User ToKitchen
 	 * 16 = declineInvitation
 	 * 17 = add Ingredient to kitchen Event
 	 * 18 = add Message to kitchen Event
 	 */
 
     public void createNewKitchen(String kitchenName, Account account){
     	System.out.println("Creating new kitchen");
     	Request r = new Request(14);
     	r.setKitchenName(kitchenName);
     	r.setAccount(account);
     	send(r);
     }
     
     public void addMessageToEvent(String eventName, String message, String kitchenId){
     	System.out.println("sending message request");
     	Request r = new Request(20);
     	r.setEventName(eventName);
     	r.setKitchenID(kitchenId);
     	System.out.println("messages in event: " + message);
     	r.setMessages(message);
     	send(r);
     }
     
     public void addIngToEventShopping(String eventName, String kId, Ingredient ing){
     	System.out.println("sending event ing request");
     	Request r = new Request(17);
     	r.setEventName(eventName);
     	r.setKitchenID(kId);
     	r.setIngredient(ing);
     	send(r);
     }
     
     public void getKitchen(String id){
     	Request r = new Request(2);
     	r.setKitchenID(id);
     	send(r);
     }
     
     public void userInDatabase(String username){
     	Request r = new Request(19);
     	r.setUsername(username);
     	send(r);
     }
     
     public void passwordMatches(String username, String password){
     	Request r = new Request(21);
     	r.setPassToCheck(password);
     	r.setUsername(username);
     	send(r);
     }
     
     
     
     public void addActiveKitchenUser(String id, Account account){
     	Request r = new Request(3);
     	r.setKitchenID(id);
     	r.setKitchenUserID(_id);
     	r.setAccount(account);
     	send(r);
     }
     
     public void removeActiveKitchenUser(String id){
     	Request r = new Request(4);
     	r.setKitchenID(id);
     	r.setKitchenUserID(_id);
     	send(r);
     }
     
     public void addEvent(String id, KitchenEvent event){
     	System.out.println("We are ADDING AN EVENT");
     	System.out.println("eeeeeee " + event.toString());
     	Request r = new Request(5);
     	r.setKitchenID(id);
     	r.setEvent(event);
     	send(r);
     }
     
     public void removeEvent(String id, KitchenEvent event){
     	Request r = new Request(6);
     	r.setKitchenID(id);
     	r.setEvent(event);
     	send(r);
     }
     
     public void addRecipe(String id, Recipe rec){
     	Request r = new Request(7);
     	r.setKitchenID(id);
     	r.setRecipe(rec);
     	send(r);
     }
     
     public void removeRecipe(String id, Recipe rec){
     	Request r = new Request(8);
     	r.setKitchenID(id);
     	r.setRecipe(rec);
     	send(r);
     }
     
     public void addIngredient(String id, Ingredient ing){
     	Request r = new Request(9);
     	r.setKitchenID(id);
     	r.setIngredient(ing);
     	r.setUsername(_id);
     	send(r);
     }
     
     public void addIngredientList(String id, Set<Ingredient> ings){
     	Request r = new Request(34);
     	r.setKitchenID(id);
     	r.setIngredientList(ings);
     	r.setUsername(_id);
     	send(r);
     }
     
     public void removeIngredient(String id, Ingredient ing){
     	Request r = new Request(10);
     	r.setKitchenID(id);
     	r.setIngredient(ing);
     	r.setUsername(_id);
     	send(r);
     }
     
     public void addRequestedKitchenUser(String toId, String fromName, KitchenName kitchenName){
     	Request r = new Request(15);
     	r.setKitchenID(kitchenName.getID());
     	Invitation invite = new Invitation(_id, fromName, toId, kitchenName); 	
     	r.setInvitation(invite);
     	send(r);
     }
     
   
     public void removeRequestedKitchenUser(String id){
     	Request r = new Request(16);
     	r.setKitchenID(id);
     	r.setUsername(_id);
     	send(r);
     }
     
   
     public void storeAccount(Account account, int type, String restricAl){
     	System.out.println("making account store of type: " + type);
     	System.out.println("change item: " + restricAl);
     	Request r = new Request(11);
     	r.setAccount(account);
     	r.setChangeType(type);
     	r.setRestrictAllergy(restricAl);
     	send(r);
     }
     
     public void storeAccount(Account account, Ingredient ing){
     	Request r = new Request(11);
     	r.setAccount(account);
     	r.setChangeType(1);
     	r.setIngredient(ing);
     	send(r);
     }
     
     public void storeAccount(Account account, String kID){
     	Request r = new Request(11);
     	r.setAccount(account);
     	r.setChangeType(6);
     	r.setKitchenID(kID);
     	send(r);
     }
     
     public void storeAccount(Account account){
     	Request r = new Request(11);
     	r.setAccount(account);
     	r.setChangeType(0);
     	send(r);
     }
     
     public void closeAccount(){
     	Request r = new Request(12);
     	send(r);
     }
     
     public void makeKitchen(String id){
     	Request r = new Request(14);
     	r.setKitchenID(id);
     	send(r);
     }
     
     public void changePassword(String email, String password){
     	System.out.println("CHANGE PASSWORD IN CLIENT");
     	Request r = new Request(18);
     	r.setUsername(email);
     	r.setPasword(password);
     	send(r);
     }
     
     public void updatePassword(String email){
     	System.out.println("CLIENT IN UPDATE PASSWORD.");
     	Request r = new Request(22);
     	r.setUsername(email);
     	send(r);
     }
     
     public void setCurrentKitchen(KitchenName kitchen){
     	_currentKitchen = kitchen;
     }
     
     public KitchenName getCurrentKitchen(){
     	return _currentKitchen;
     	
     }
     
    
     
     
     
     /**
      * Method to shut down streams and socket.
      */
     public void close(){
     	System.out.println("CLOSING CLINET");
     	_running = false;
         try {
         	closeAccount();
         	_out.close();
             _in.close();
 			_kkSocket.close();
 		} catch (IOException e) {
 			//System.err.println("ERROR trying to close client resources");
 			System.exit(1);
 		}
     }
     
     public HashMap<KitchenName, Kitchen> getKitchens(){
     	return _kitchens;
     }
     
     public void removeKitchen(KitchenName kn){
     	 _kitchens.remove(kn); 
 		 _kitchenIdToName = kitchenIdMap(_kitchens);
 		 _kitchenNames = kitchenNameSet(_kitchens);
 		 _gui.updateKitchenDropDown();
 		 _gui.refreshSearchAccordian();
     }
     
     public HashMap<String,KitchenName> getKitchenIdMap(){
     	return _kitchenIdToName;
     }
     
     public Set<String> getKitchenNameSet(){
     	return _kitchenNames;
     }
     
     private HashMap<String,KitchenName> kitchenIdMap(HashMap<KitchenName, Kitchen> kitchenMap){
     	HashMap<String,KitchenName> map = new HashMap<String,KitchenName>();
     	for (KitchenName item : kitchenMap.keySet()){
     		map.put(item.getID(), item);
     	}
     	return map; 
     }
     
     private Set<String> kitchenNameSet(HashMap<KitchenName, Kitchen> kitchenMap){
     	Set<String> names = new HashSet<>();
     	for (KitchenName item : kitchenMap.keySet()) {
     		names.add(item.getName());
     	}
     	return names;
     }
     
     public void setNewKitchen(String newK){
     	_newKitchen = newK;
     }
 }
 
