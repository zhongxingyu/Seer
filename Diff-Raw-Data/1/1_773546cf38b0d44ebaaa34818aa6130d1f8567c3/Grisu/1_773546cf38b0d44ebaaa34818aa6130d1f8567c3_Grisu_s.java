 import com.beust.jcommander.JCommander;
 import com.google.common.collect.Maps;
 import grisu.control.ServiceInterface;
 import grisu.frontend.control.login.LoginManager;
 import grisu.frontend.view.cli.*;
 import grith.jgrith.cred.Cred;
 
 import java.util.Map;
 
 /**
  * Project: grisu
  * <p/>
  * Written by: Markus Binsteiner
  * Date: 5/08/13
  * Time: 3:32 PM
  */
 public class Grisu extends GrisuCliClient<GrisuMultiCliParameters> {
 
     public static ServiceInterface serviceInterface = null;
     public static Cred credential = null;
     //    public static List<String> cli_parameters;
     public final String[] args;
     final JCommander jc;
     final GrisuMultiCliParameters mainParams;
     final Map<String, GrisuCliCommand> commands;
 
     final String command;
 
     public Grisu(GrisuMultiCliParameters params, Map<String, GrisuCliCommand> commands, String[] args) throws Exception {
         super(params, args);
 
         this.args = args;
         this.mainParams = params;
         this.commands = commands;
 
         jc = new JCommander(params);
         jc.setProgramName("grisu");
         for ( String key : this.commands.keySet() ) {
             jc.addCommand(key, this.commands.get(key));
         }
 
         jc.parse(args);
 
         command = jc.getParsedCommand();
 
     }
 
     public static void main(String[] args) {
 
         LoginManager.initGrisuClient("grisu");
 
         GrisuMultiCliParameters params = new GrisuMultiCliParameters();
 
         Map<String, GrisuCliCommand> commands = Maps.newHashMap();
         commands.put("submit", new GrisuSubmitCliParameters());
         commands.put("list", new GrisuListCliParameters());
         commands.put("wait",  new GrisuWaitParameters());
         commands.put("status", new GrisuStatusCliParameters());
         commands.put("view", new GrisuViewCliParameters());
 
 
         Grisu s = null;
         try {
             s = new Grisu(params, commands, args);
         } catch (Exception e) {
             e.printStackTrace();
             System.err.println("Could not execute command: "
                     + e.getLocalizedMessage());
             System.exit(1);
         }
         s.run();
 
     }
 
     private GrisuCliCommand getCommand(String command) {
         return this.commands.get(command);
     }
 
     @Override
     protected void run() {
 
         try {
 
             if (getLoginParameters().isNologin()) {
 
                 System.out.println("Doing nothing...");
                 System.exit(0);
             } else {
 
                 credential = getCredential();
                 serviceInterface = getServiceInterface();
 
                 GrisuCliCommand c = commands.get(command);
 
                 if ( c == null ) {
                     System.out.println("No command: " + command);
                     jc.usage();
                     System.exit(1);
                 } else {
                     c.execute();
                 }
 
                 System.exit(0);
 
             }
         } catch (Exception e) {
             System.err.println("Error: " + e.getLocalizedMessage());
 //            e.printStackTrace();
             System.exit(2);
         }
 
         System.exit(0);
     }
 
 }
