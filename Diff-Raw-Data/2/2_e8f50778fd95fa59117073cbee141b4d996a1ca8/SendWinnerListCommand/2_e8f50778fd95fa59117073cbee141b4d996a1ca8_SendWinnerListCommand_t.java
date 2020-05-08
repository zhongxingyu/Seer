 package commands;
 
 import java.util.List;
 
 import client.ClientController;
 import client.ClientModel;
 
 @SuppressWarnings("serial")
 public class SendWinnerListCommand implements Command {
 
 	private final List<Tuple> winnerList;
 	
 	public SendWinnerListCommand(List<Tuple> list) {
 		this.winnerList = list;
 	}
 	
	public static class Tuple {
 		public final int id;
 		public final int cash;
 		
 		public Tuple(int id, int cash) {
 			this.id = id;
 			this.cash = cash;
 		}
 	}
 
 	
 	public void execute(ClientModel model, ClientController controller) {
 		
 		// Add implementation
 	}
 }
