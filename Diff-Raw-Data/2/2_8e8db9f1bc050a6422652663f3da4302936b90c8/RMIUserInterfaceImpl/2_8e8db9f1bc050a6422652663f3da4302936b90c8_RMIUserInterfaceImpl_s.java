 package herbstJennrichLehmannRitter.ui.impl;
 
 import herbstJennrichLehmannRitter.engine.model.Card;
 import herbstJennrichLehmannRitter.engine.model.Data;
 import herbstJennrichLehmannRitter.ui.RMIUsertInterface;
 import herbstJennrichLehmannRitter.ui.UserInterface;
 
 import java.rmi.RemoteException;
 import java.rmi.server.UnicastRemoteObject;
 import java.util.Collection;
 
 /**	Description of RMIUserInterface Class
  * This class implements the UserInterface for playing between two computers
  */
 
 public class RMIUserInterfaceImpl extends UnicastRemoteObject implements RMIUsertInterface {
 
 	private static final long serialVersionUID = 8417370027923988891L;
 	
 	private final UserInterface userInterface;
 	
 	public RMIUserInterfaceImpl(UserInterface userInterface) throws RemoteException {
 		this.userInterface = userInterface;
 	}
 	
 	@Override
 	public void setData(Data data) throws RemoteException {
 		this.userInterface.setData(data);
 	}
 
 	@Override
 	public void twoPlayerFound() throws RemoteException {
 		this.userInterface.twoPlayerFound();
 	}
 
 	@Override
 	public void nextTurn() throws RemoteException {
		this.userInterface.twoPlayerFound();
 	}
 
 	@Override
 	public void playAnotherCard() throws RemoteException {
 		this.userInterface.playAnotherCard();
 	}
 
 	@Override
 	public void enemyPlayedCard(Card card) throws RemoteException {
 		this.userInterface.enemyPlayedCard(card);
 	}
 
 	@Override
 	public void onPlayCard(Card card) throws RemoteException {
 		this.userInterface.onPlayCard(card);
 	}
 
 	@Override
 	public void onDiscardCard(Card card) throws RemoteException {
 		this.userInterface.onDiscardCard(card);
 	}
 
 	@Override
 	public void youLost() throws RemoteException {
 		this.userInterface.youLost();
 	}
 
 	@Override
 	public void youWon() throws RemoteException {
 		this.userInterface.youWon();
 	}
 
 	@Override
 	public void abort(String reason) throws RemoteException {
 		this.userInterface.abort(reason);
 	}
 
 	@Override
 	public String getName() throws RemoteException {
 		return this.userInterface.getName();
 	}
 
 	@Override
 	public Collection<String> getCards() throws RemoteException {
 		return this.userInterface.getCards();
 	}
 
 }
