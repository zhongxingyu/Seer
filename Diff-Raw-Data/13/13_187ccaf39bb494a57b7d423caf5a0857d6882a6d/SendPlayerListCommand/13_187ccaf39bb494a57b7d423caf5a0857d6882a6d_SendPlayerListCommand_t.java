 package commands;
 
 import java.util.ArrayList;
 import java.util.List;
 
import message.data.Card;
 import message.data.Player;
 import client.ClientController;
 import client.ClientModel;
 import client.ClientSidePlayer;
 import client.State;
 
 /**
  * Sends list of all players to a Client
  * @author Aleksey
  *
  */
 
 @SuppressWarnings("serial")
 public class SendPlayerListCommand implements Command {
 
 	private List<Player> list;
 	/**
 	 *  Creates the command
 	 * @param list - player list
 	 */
 	public SendPlayerListCommand(List <Player>list) {
 		this.list = list;
 	}
 	
 	public void execute(ClientModel model, ClientController controller) {
 		List<ClientSidePlayer> playList = new ArrayList<ClientSidePlayer>();
 		
 		for(Player i: list) {
 			ClientSidePlayer tempPlayer = new ClientSidePlayer(i);
 			tempPlayer.addObserver(controller);
 			playList.add(tempPlayer);
 		}
 		
 		model.setPlayerList(playList);	
 		model.changeState(State.PLAYING);
		Card[] newField = new Card[] {null, null, null, null, null};
		model.changeFieldCards(newField);
 	}
 
 }
