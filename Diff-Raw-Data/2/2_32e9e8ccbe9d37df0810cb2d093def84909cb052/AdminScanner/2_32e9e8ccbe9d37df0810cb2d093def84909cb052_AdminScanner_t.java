 package client;
 
 import java.rmi.RemoteException;
 
 import remote.IAdminMode;
 
 public class AdminScanner implements ICommandScanner {
 
 	private IAdminMode admin;
 
 	public AdminScanner(IAdminMode loggedInAdmin) {
 		admin = loggedInAdmin;
 	}
 
 	@Override
 	public void readCommand(String[] cmd) throws RemoteException {
 		if (cmd[0].equals("!getPricingCurve")) {
 			if(!checkNoOfArgs(cmd, 0)) {
 				return;
 			}
 			System.out.println(admin.getPricingCurve());
 		} else if (cmd[0].equals("!setPriceStep")) {
 			if(!checkNoOfArgs(cmd, 2)) {
 				return;
 			}
 			int taskCount = Integer.parseInt(cmd[1]);
			double percent = Double.parseDouble(cmd[2]);
 			admin.setPriceStep(taskCount, percent);
 			System.out.println("Successfully inserted price step.");
 		} else {
 			System.out.println("Invalid command");
 		}
 
 	}
 
 	private boolean checkNoOfArgs(String[] cmd, int noOfSupposedArgs) {
 		if((cmd.length - 1) != noOfSupposedArgs) {
 			System.out.println("Supposed number of arguments: " + noOfSupposedArgs);
 			return false;
 		}
 		return true;
 	}
 
 	@Override
 	public void logout() throws RemoteException {
 		admin.logout();
 	}
 }
