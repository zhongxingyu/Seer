 package managementClient.commands;
 
 import managementClient.db.EventDATABASE;
 import command.ICommand;
 
 public class Auto implements ICommand {
 
 	@Override
 	public int numberOfParams() {
 		return 0;
 	}
 
 	@Override
 	public String execute(String[] params) {
 		boolean auto=EventDATABASE.getInstance().isAuto();
 		
 		if(!auto){
			EventDATABASE.getInstance().setAuto(true);
 			return "!print "+" in !auto-mode!";
 		}
 		else{
 			return "!print "+" already in !auto-mode!";
 		}
 		
 		//return "!print "+" no longer in auto mode!";
 	}
 
 	@Override
 	public boolean needsRegistration() {
 		return true;
 	}
 
 }
