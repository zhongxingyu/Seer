 /**
  * 
  */
 package ehospital.server;
 
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.rmi.NotBoundException;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.rmi.server.ExportException;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Timer;
 import java.util.Vector;
 
 import javax.crypto.spec.SecretKeySpec;
 
 import remote.obj.EmergencyAccessHandler;
 
 import ehospital.server.db.DBManager;
 import ehospital.server.db.Logger;
 import ehospital.server.db.TmpUserChecker;
 import ehospital.server.handler.RegisterHandler;
 import ehospital.server.remote.impl.AuthHandlerImpl;
 import ehospital.server.remote.impl.DataHandlerImpl;
 import ehospital.server.remote.impl.EmergencyAccessHandlerImpl;
 import ehospital.server.remote.impl.ProgramAuthHandlerImpl;
 /**
  * new console.
  * @author mc, Gilbert
  *
  */
 public class RMIConsole {
 
 	public static final byte[] key = {-19, -11, 122, 111, -37, -13, 16, -47, -65, 78, -126, -128, -88, 54, 101, 86};
 	public static final SecretKeySpec ProgramKey = new SecretKeySpec(key, "AES");
 	public static ArrayList<String> cmdList;
 	public static Registry reg;
 	/**
 	 * @author Gilbert
 	 */
 	public RMIConsole() {
 	}
 	
 	/**
 	 * Server Main Class
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		
 		//start timer thread
 		Timer t = new Timer();
 		t.schedule(new TmpUserChecker(), 60000);
 		
 		cmdList = new ArrayList<String>();
 		cmdList.add("exit");
 		cmdList.add("start");
 		cmdList.add("shutdown");
 		cmdList.add("register");
 		cmdList.add("register temp");
 		cmdList.add("testauth");
 		cmdList.add("threadchk");
 		cmdList.add("status");
 		cmdList.add("show client");
 		cmdList.add("help");
 		
 		String cmd = "";
 		BufferedReader cmdreader = new BufferedReader(new InputStreamReader(System.in));
 		try {
 			for (int i = 0 ; i < RMIConsole.cmdList.size(); i++ ) {
 				System.out.println(i+ ". " + RMIConsole.cmdList.get(i));
 			}
 			System.out.print("~>");
 			while ((cmd = cmdreader.readLine()) != null)
 			{
 				if (cmd.equalsIgnoreCase("exit"))
 				{
 					Logger.log("admin", "Server exit.");
 					System.exit(0);
 				}
 				else if (cmd.equalsIgnoreCase("start"))
 				{
 					try {
 						reg = LocateRegistry.createRegistry(1099);
 					} catch (ExportException e) {
 						reg = LocateRegistry.getRegistry(1099);
 					}
 					try {
 			            String name = "AuthHandler";
 			            remote.obj.AuthHandler engine1 = new AuthHandlerImpl();
 			            reg.rebind(name, engine1);
 			            
 			            name = "DataHandler";
 			            remote.obj.DataHandler engine2 = new DataHandlerImpl();
 			            reg.rebind(name, engine2);
 			            
 			            name = "ProgramAuthHandler";
 			            remote.obj.ProgramAuthHandler engine3 = new ProgramAuthHandlerImpl();
 			            reg.rebind(name, engine3);
 			            
 			            name = "EmergencyAccessHandler";
 			            EmergencyAccessHandler engine4 = new EmergencyAccessHandlerImpl();
 			            reg.rebind(name, engine4);
			            System.out.println("Service online.");
 			            
 			        } catch (Exception e) {
 			            System.err.println("Service exception:");
 			            e.printStackTrace();
 			            Logger.log("server", "Service cannot be started due to" + e.getMessage());
 			        }
 			        
 			        System.out.println("Server Started");
 					Logger.log("admin", "Server starts");
 				}
 				
 				else if (cmd.equalsIgnoreCase("shutdown")) {
 					shutdown();
 				}
 				
 				else if (cmd.equalsIgnoreCase("register"))
 				{
 					System.out.print("Role? (");
 					ResultSet rs = null;
 					DBManager dbm = new DBManager();
 					if (dbm.connect()) {
						rs = dbm.query("select `Role` from `privilege`;"); 
 					}
 					ArrayList<String> roleList = new ArrayList<String>();
 					if (rs != null && rs.first()) {
 						do {
							roleList.add(rs.getString("Role"));
 						} while (rs.next());
 					}
 					
 					for ( int i = 0; i < roleList.size() - 1; i ++) {
 						System.out.print(roleList.get(i)+ " / ");
 					}
					System.out.print(roleList.get(roleList.size()-1) + ")");
 					cmd = cmdreader.readLine();
 					System.out.print("User name? ");
 					
 					
 					String cmd2 = cmdreader.readLine();
 					
 					RegisterHandler rh = new RegisterHandler(cmd, cmd2);
 					if(rh.register() == -1) {
 						System.out.println("Username Exists!");
 						
 						Logger.log(cmd2, "failed to register "+cmd+"as existing user");
 					}
 					
 				}
 				else if (cmd.equalsIgnoreCase("register temp")) {
 					RegisterHandler rh = new RegisterHandler();
 					if(rh.regTmpUser() == 0) {
 						System.out.println("Register Temp Card Success!");
 					} else {
 						System.out.println("Register Temp Card Failed!");
 					}
 				}
 				
 				else if (cmd.equalsIgnoreCase("status"))
 				{
 					if (reg != null) {
 						String[] s = reg.list();
 						if (s != null) {
 							for(int i = 0; i < s.length; i++) {
 								System.out.println(s[i]);
 							}
 						} else {
 							System.out.println("Server Not Started");
 						}
 					} else {
 						System.out.println("Server Not Started");
 					}
 				}
 				else if (cmd.equalsIgnoreCase("show client"))
 				{
 					Vector<Session> s = SessionList.clientList;
 					for (int i = 0; i < s.size(); i++) {
 						System.out.println(s.get(i).getUsername());
 					}
 				}
 				else if (cmd.equalsIgnoreCase("testauth"))	
 				{
 					System.out.println("Username? ");
 					
 					String username = cmdreader.readLine();
 					System.out.println("Password? ");
 					String pwd = cmdreader.readLine();
 					DBManager dbm = new DBManager();
 					ehospital.server.handler.AuthHandler ah = new ehospital.server.handler.AuthHandler(username,pwd,dbm);
 					if (ah.authenticate()) {
 						System.out.println("User found and authenticated!");
 						Logger.log(username, "is tested authenticated by admin.");
 						
 					} else {
 						System.out.println("Authenticate failed");
 						Logger.log(username, "is tested failed to authenticate by admin.");
 					}
 					
 				} else if (cmd.equalsIgnoreCase("help")) {
 					for (int i = 0 ; i < RMIConsole.cmdList.size(); i++ ) {
 						System.out.println(RMIConsole.cmdList.get(i));
 					}
 				} else if (cmd.equalsIgnoreCase("testsql"))
 				{
 					DBManager dbm = new DBManager();
 					ehospital.server.db.DBManager.Param[] pList = new ehospital.server.db.DBManager.Param[1];
 					pList[0] = dbm.new Param("name", "medicine A';");
 					ehospital.server.db.DBManager.Param[] pList2 = new ehospital.server.db.DBManager.Param[1];
 					pList2[0] = dbm.new Param("id", "1");
 					dbm.update("Update medicine", pList, pList2);
 				}
 				System.out.print("~>");
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		shutdown();
 	}
 
 	private static void shutdown() {
 		try {
 			reg.unbind("AuthHandler");
 			reg.unbind("DataHandler");
 			reg.unbind("ProgramAuthHandler");
 			reg.unbind("EmergencyAccessHandler");
 			System.out.println("Server is shut down");
 			Logger.log("admin", "Server is shut down");
 			SessionList.clientList = new Vector<Session>();
 		} catch (NotBoundException e) {
 			System.out.println("Some of the Handlers are not bind.");
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		}
 	}
 	
 }
