 package commands.customGame;
 
 import tablePackage.CustomTable;
 import clientPackage.Client;
 import commands.Command;
 
 public class ForceGetUp extends Command {
 
 	/**
 	 * client: table host
 	 * params[0] : actual command
 	 * params[1] : username to get up
 	 */
 	
 	private static boolean enabled = true;
 	private static int counter = 0;
 	
 	@Override
 	public void execute(Client client, String... params) {
 		Client forcedClient = Client.getClient(params[1]);
 		if (forcedClient != null){
 			CustomTable table = (CustomTable) client.getTable();
			if (table != null)
 				table.smnGotUp(forcedClient);
			forcedClient.send("3e1R~\n");
 		}
 	}
 	
 	@Override
 	public void increaseCounter() {
 		counter++;
 	}
 
 	@Override
 	public long getCounter() {
 		return counter;
 	}
 
 	@Override
 	public String getCode() {
 		return "3e1";
 	}
 
 	@Override
 	public void setEnabled(boolean enabled) {
 		ForceGetUp.enabled = enabled;
 	}
 
 	@Override
 	public boolean isEnabled() {
 		return ForceGetUp.enabled;
 	}
 
 }
