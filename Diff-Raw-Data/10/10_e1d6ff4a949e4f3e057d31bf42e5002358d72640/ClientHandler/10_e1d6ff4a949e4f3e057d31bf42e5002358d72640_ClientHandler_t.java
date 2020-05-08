 package server;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.net.Socket;
 import java.net.SocketException;
 import java.util.concurrent.ExecutorService;
 
 import API.Wrapper;
 import ClientServerRequests.AccountRequest;
 import ClientServerRequests.KitchenRequest;
 import ClientServerRequests.NewAccountRequest;
 import ClientServerRequests.NewKitchenRequest;
 import ClientServerRequests.Request;
 import ClientServerRequests.RequestReturn;
 import ClientServerRequests.StoreAccountRequest;
 import ClientServerRequests.StoreKitchenRequest;
 import ClientServerRequests.UpdateKitchenRequest;
 import Database.DBHelper;
 
 import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
 
 /**
  * Encapsulate IO for the given client.
  * Runs an input loop and adds task to taskPool
  * based on the request. 
  * Sends RequestReturn object to handler.
  * Modified ClientHandler from lab4
  */
 public class ClientHandler extends Thread {
 	private ClientPool _pool;
 	private Socket _client;
 	private ObjectInputStream _objectIn;
 	private ObjectOutputStream _objectOut;
 	private ExecutorService _taskPool;
 	private String _clientID;
 	private DBHelper _helper;
 	private KitchenPool _activeKitchens;
 	private boolean _running;
 	private AutocorrectEngines _autocorrect;
 	
 	/**
 	 * Thread for the client. Handles input and launches requests.
 	 */
 	public ClientHandler(ClientPool pool, Socket client, ExecutorService taskPool, KitchenPool kitchens, DBHelper helper, AutocorrectEngines autocorrect) throws IOException {
 		if (pool == null || client == null) {
 			throw new IllegalArgumentException("Cannot accept null arguments.");
 		}
 		_autocorrect = autocorrect;
 		_helper = helper;
 		_pool = pool;
 		_client = client;
 		_taskPool = taskPool;
 		_activeKitchens = kitchens;
 		_objectIn = new ObjectInputStream(client.getInputStream());
 		_objectOut = new ObjectOutputStream(client.getOutputStream()) {
 			@Override
 			public void close() {
 				try {
 					super.close();
 				} catch (IOException e) {
 					//TODO??
 				}
 			}
 		};
 	}
 	
 	/**
 	 * Receive data from the client. Input is in the form 
 	 * of a string. Launch request accordingly.
 	 */
 	public void run(){
 		try {
 				_running = true;
 				Request request;
 				int type;
 				while(_running && _client.isConnected()) {
 					if((request = (Request) _objectIn.readObject()) != null){
 						type = request.getType();
 						
 						switch (type){
 							case 1:  //verify account
 								checkPassword(request);
 								break;
 							case 2:  //getKitchen
 								getKitchen(request);
 								break;
 							//case 3 -- 10 are update kitchens (handled by default
 							case 11: //store Account
 								storeAccount(request);
 								break;
 							case 12: //close client
 								kill();
 								break;
 							case 13: //create new account	
 								createNewUser(request);
 								break;
 							case 14: //create new Kitchen
 								createNewKitchen(request);	
 								break;
 							case 15: //invite to kitchen
 								invite(request);
 								break;
 							default:
 								updateKitchen(request);
 								break;
 						}
 					}
 
 			}
 		} catch (IOException | ClassNotFoundException  e) {
 				try {
 					kill();
 				} catch (IOException e1) {
 					//try again
 					try {
 						kill();
 					} catch (IOException e2) {
 						//Ignore
 					}
 				}
 		} 
 	}
 
 
 	/**
 	 * Send a RequestReturn to the client via the socket
 	 */
 	public synchronized void send(RequestReturn toReturn) {
 		if(toReturn != null){
 			try {
 				_objectOut.writeObject(toReturn);
 				_objectOut.flush();
 				_objectOut.reset();
 			} catch (IOException e) {
//				try {
//					kill();
//				} catch (IOException e1) {
//					e1.printStackTrace();
//				}
 			}
 		}
 	}
 
 	/**
 	 * Close this socket and its related streams.
 	 */
 	public void kill() throws IOException {
 		System.out.println("killing myself (client handler of clinet " + _clientID + ")" );
 		_objectIn.close();
 		try{
 			_objectOut.close();
 		} catch(SocketException e){
 			
 		}
 		_activeKitchens.removeUser(_clientID);
 		_pool.remove(this);
 		_client.close();
 	}	
 	
 	
 	public void checkPassword(Request request){
 		if(_helper.checkUsernamePassword(request.getUsername(), request.getPassword())){
 			System.out.println("executing task");
 			_taskPool.execute(new AccountRequest(this, request.getUsername(), _helper, _activeKitchens, _autocorrect));
 		}
 		else{
 			RequestReturn toReturn = new RequestReturn(1);
 			toReturn.setCorrect(false);
 			send(toReturn);
 		}
 	}
 
 	public void getKitchen(Request request){
 		_taskPool.execute(new KitchenRequest(this, request.getKitchenID(), _activeKitchens));
 	}
 	
 	public void updateKitchen(Request request){
 		_taskPool.execute(new UpdateKitchenRequest(_activeKitchens, request));
 	}
 	
 	public void createNewUser(Request request){
 		_taskPool.execute(new NewAccountRequest(this, request, _helper));
 	}
 	
 	private void createNewKitchen(Request request) {
 		_taskPool.execute(new NewKitchenRequest(this, request, _helper, _activeKitchens));
 	}
 
 	private void storeAccount(Request request) {
 		_taskPool.execute(new StoreAccountRequest(request.getAccount(), _helper));
 	}
 	
 	private void storeKitchen(Request request) {
 		_taskPool.execute(new StoreKitchenRequest(request.getKitchen(), _helper));
 	}
 	
 	private void invite(Request request) {
 		// TODO Auto-generated method stub
 		
 	}
 	
     private static String toString( Serializable o ) throws IOException {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ObjectOutputStream oos = new ObjectOutputStream( baos );
         oos.writeObject( o );
         oos.close();
         return new String( Base64.encode( baos.toByteArray() ) );
     }
 	
 	public String getID(){
 		return _clientID;
 	}
 	
 	public void setID(String id){
 		_clientID = id;
 	}
 	
 }
