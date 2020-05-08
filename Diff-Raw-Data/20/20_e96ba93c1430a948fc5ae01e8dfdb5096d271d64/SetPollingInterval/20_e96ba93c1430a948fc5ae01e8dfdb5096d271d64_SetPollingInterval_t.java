 package org.glite.authz.pap.ui.cli.papmanagement;
 
 import java.rmi.RemoteException;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 
 public class SetPollingInterval extends PAPManagementCLI {
 
     private static final String[] commandNameValues = { "set-polling-interval", "spi" };
     private static final String DESCRIPTION = "Set the polling interval in seconds.";
     private static final String USAGE = "<seconds>";
 
     public SetPollingInterval() {
         super(commandNameValues, USAGE, DESCRIPTION, null);
     }
 
     @Override
     protected Options defineCommandOptions() {
         return null;
     }
 
     @Override
     protected int executeCommand(CommandLine commandLine) throws ParseException, RemoteException {
 
         String[] args = commandLine.getArgs();

         if (args.length != 2) {
             throw new ParseException("Wrong number of arguments: specify the new polling interval in seconds");
         }
 
        try {

            float pollingInterval = Float.valueOf(args[1]);

            papMgmtClient.setPollingInterval(pollingInterval);

        } catch (NumberFormatException e) {
            System.out.println("Invalid number: \"" + args[1]
                    + "\". Specify the new polling interval (integer number) in seconds.");
            return ExitStatus.PARSE_ERROR.ordinal();
        }

         return ExitStatus.SUCCESS.ordinal();
     }
 }
