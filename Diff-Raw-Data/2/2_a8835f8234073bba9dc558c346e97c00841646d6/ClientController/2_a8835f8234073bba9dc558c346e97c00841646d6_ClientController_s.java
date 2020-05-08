 package client;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 
 import client.ClientModel.Bet;
 import message.data.ClientTurn;
 import commands.SendWinnerListCommand.Tuple;
 import poker.GUI.ClientView;
 import message.data.Card;
 
 /**
  * Monitors changes in the {@link ClientModel} and toggles associated
  * changes in {@link ClientView}
  * @author Aleksey
  *
  */
 
 public class ClientController implements Observer {
 
     /**Associated ClientModel and ClientView to send and receive messages */
     private ClientModel model;
     private ClientView view;
 
     /**
      * Constructs a Controller
      *
      * @param model
      * The associated ClientModel
      * @param view
      * The associated ClientView
      */
     public ClientController(ClientModel model, ClientView view) {
         this.model = model;
         this.view = view;
     }
 
 
     /**
      * Sends the view information about cash won by players
      *
      * @param list
      * The list of player id's and cash they have won
      */
 
 
     public void sendViewWinners(List<Tuple> list) {
         //view.getWinners((ArrayList<Tuple>) list);
         view.broadcastWinner((ArrayList<Tuple>) list);
 
     }
 
     /**
      * ClientModel change handler
      *
      * @param model
      * 	The model that changed
      * @param arg
      * 	The argument that changed
      */
 
     public void update(ClientModel model, Object arg) {
         if(arg instanceof Card[] ) {
             view.emptyTableCards();
             view.tableCards();
 
         } else if(arg instanceof State) {
             if(model.getState() == State.READY){
                 view.stateReady();
             }
             if(model.getState() == State.INPUTCHECK){
                 view.stateInputCheck();
             }
             if(model.getState() == State.INPUTCALL){
                 view.stateInputCall();
             }
             if(model.getState() == State.PLAYING){
                 view.statePlaying();
             }
         } else if(arg instanceof Integer) {
             // id or Blind has changed
         } else if(arg instanceof List) {
 
         	view.placePlayers(model.getPlayerList());
             view.emptyTableCards();
         }
 			
 			
 	/*	private Card fieldcards[];
 		private State state;
 		private List<ClientSidePlayer> players;	
 		private int id;
 	*/
 	}
 	/**
 	 * Player change handler
 	 * 
 	 * @param player 
 	 * 	The changed Player
 	 * @param arg 
 	 * 	The argument that changed
 	 */
 	
 	public void update(ClientSidePlayer player, Object arg) {
 		if(arg instanceof ClientTurn) {
             switch(player.getLastTurn()){
 
                 case CALL:
                     view.displayBroadcast().setText(view.displayBroadcast().getText() + "\n" + player.getNick() + " has called for $" + (model.getPlayerBet(player.getId())));
 
                     break;
                 case CHECK:
                 	view.displayBroadcast().setText(view.displayBroadcast().getText() + "\n" + player.getNick() + " has checked");
 
                     break;
                 case EXIT:
                 	view.displayBroadcast().setText(view.displayBroadcast().getText() + "\n" + player.getNick() + " has gone offline");
 
                     break;
                 case FOLD:
                 	view.displayBroadcast().setText(view.displayBroadcast().getText() + "\n" + player.getNick() + " has folded");
 
                     break;
                 case RAISE:
                	view.displayBroadcast().setText(view.displayBroadcast().getText() + "\n" + player.getNick() + " has raised $" + (model.getPlayerBet(player.getId())));
 
                     break;
                 case BLIND:
                 	view.displayBroadcast().setText(view.displayBroadcast().getText() + "\n" + model.getPlayer(model.getDealer()).getNick() + " is on a BIG BLIND");
 
                     break;
                 default:
                 	break;
                 }
             view.setNewPot();
             view.setNewCash(model.getPlayerList());
             view.setNums(0);
 
 		}   else if(arg instanceof Card) {
             view.giveCards(model.getPlayerList());
 
         }	 else if(arg instanceof Integer) {
             view.setNewCash(model.getPlayerList());
         }
 	}
 	
 	public void update(Bet bet, Object arg) {
 		int newBet = bet.getBet();
 		if(newBet > model.getBlind()){
 			view.setNums(newBet);
 		} 
 
 		//Bet was updated
 	}
 	
 	/**
 	 * Dispatches changes to appropriate handlers
 	 */
 	public void update(Observable obj, Object arg) {
 		if(obj instanceof ClientSidePlayer) {
 			this.update((ClientSidePlayer) obj, arg);
 		} else if(obj instanceof ClientModel) {
 			this.update((ClientModel) obj, arg);
 		} else if(obj instanceof Bet) {
 			this.update((Bet)obj, arg);
 		} else {
 			System.out.println("Not a valid object" + obj);
 		}
 		
 		view.updateView();
 	}
 	
 }
