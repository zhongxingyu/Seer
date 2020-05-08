 package herbstJennrichLehmannRitter.ui;
 
 import herbstJennrichLehmannRitter.engine.model.Card;
 import herbstJennrichLehmannRitter.engine.model.Data;
 
 import java.rmi.Remote;
 import java.rmi.RemoteException;
 import java.util.Collection;
 
 /**	Description of RMIUsertInterface Class
  *  This Class extends the Remote Interface and implements the methods for exceptions
  */
 
 public interface RMIUsertInterface extends Remote {
 	
 	public void setData(Data data) throws RemoteException;
 	public void twoPlayerFound() throws RemoteException;
 	public void nextTurn() throws RemoteException;
 	public void playAnotherCard() throws RemoteException;
 	public void enemyPlayedCard(Card card) throws RemoteException;
 	public void onPlayCard(Card card) throws RemoteException;
 	public void onDiscardCard(Card card) throws RemoteException;
 	public void youLost() throws RemoteException;
 	public void youWon() throws RemoteException;
 	public void abort(String reason) throws RemoteException;
 	public String getName() throws RemoteException;
 	public Collection<String> getCards() throws RemoteException;
 	
	public void unexport();
 }
