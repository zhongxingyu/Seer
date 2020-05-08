 package org.specksensor.applications;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import edu.cmu.ri.createlab.util.commandline.BaseCommandLineApplication;
 import org.apache.log4j.Level;
 import org.apache.log4j.LogManager;
 import org.apache.log4j.Logger;
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 import org.specksensor.RemoteStorageCredentials;
 import org.specksensor.RemoteStorageCredentialsImpl;
 import org.specksensor.SpeckConfig;
 
 /**
  * @author Chris Bartley (bartley@cmu.edu)
  */
 final class SpeckGatewayCommandLine extends BaseCommandLineApplication
    {
    private static final Logger LOG = Logger.getLogger(SpeckGatewayCommandLine.class);
 
    @NotNull
    private final SpeckGatewayHelper helper;
 
    SpeckGatewayCommandLine(@Nullable final String pathToConfigFile)
       {
       super(new BufferedReader(new InputStreamReader(System.in)));
 
       helper = new SpeckGatewayHelper(
             new SpeckGatewayHelper.EventListener()
             {
             @Override
             public void handleConnectionEvent(@NotNull final SpeckConfig speckConfig, @NotNull final String portName)
                {
                // nothing to do
                }
 
             @Override
             public void handlePingFailureEvent()
                {
                // nothing to do
                }
             },
             pathToConfigFile);
 
       registerActions();
       }
 
    private final Runnable connectToSpeckAction =
          new Runnable()
          {
          public void run()
             {
             helper.scanAndConnect();
             }
          };
 
    private static void logInfo(@NotNull final String message)
       {
       LOG.info(message);
       println(message);
       }
 
    private final Runnable printStatisticsAction =
          new Runnable()
          {
          public void run()
             {
             final String statistics = helper.getStatistics();
             if (statistics != null)
                {
                println(statistics);
                }
             else
                {
                println("You are not connected to a Speck.");
                }
             }
          };
 
    private final Runnable setLoggingLevelAction =
          new Runnable()
          {
          public void run()
             {
             println("Choose the logging level for the log file:");
             println("   1: TRACE");
             println("   2: DEBUG (default)");
             println("   3: INFO");
             final Integer loggingLevelChoice = readInteger("Logging level (1-3): ");
             if (loggingLevelChoice != null)
                {
                final Level chosenLevel;
                switch (loggingLevelChoice)
                   {
                   case (1):
                      chosenLevel = Level.TRACE;
                      break;
 
                   case (2):
                      chosenLevel = Level.DEBUG;
                      break;
 
                   case (3):
                      chosenLevel = Level.INFO;
                      break;
 
                   default:
                      chosenLevel = null;
                   }
 
                if (chosenLevel == null)
                   {
                   println("Invalid choice.");
                   }
                else
                   {
                   LogManager.getRootLogger().setLevel(chosenLevel);
                   logInfo("Logging level now set to '" + chosenLevel + "'.");
                   }
                }
             else
                {
                println("Invalid choice.");
                }
             }
          };
 
    private final Runnable defineDataStorageCredentials =
          new Runnable()
          {
          public void run()
             {
             if (helper.areDataStorageCredentialsSet())
                {
                println("The host and login details can only be defined once per Speck connection.");
                }
             else
                {
                final String hostNameAndPortStr = readString("Host name (and optional port, colon delimited): ");
 
                if (isNotNullAndNotEmpty(hostNameAndPortStr))
                   {
                   final String hostName;
                   final String hostPortStr;
                   if (hostNameAndPortStr.contains(":"))
                      {
                      final String[] hostNameAndPort = hostNameAndPortStr.split(":");
                      hostName = hostNameAndPort[0].trim();
                      hostPortStr = hostNameAndPort[1].trim();
                      }
                   else
                      {
                      hostName = hostNameAndPortStr.trim();
                      hostPortStr = "80";
                      }
 
                   int hostPort = -1;
                   try
                      {
                      hostPort = Integer.parseInt(hostPortStr, 10);
                      }
                   catch (NumberFormatException ignored)
                      {
                      LOG.error("NumberFormatException while trying to convert port [" + hostPortStr + "] to an integer");
                      }
 
                   if (hostName.length() <= 0 || hostPort <= 0)
                      {
                      println("Invalid host name and/or port.");
                      }
                   else
                      {
                      final String usernameStr = readString("Username: ");
                      if (isNotNullAndNotEmpty(usernameStr))
                         {
                         final String username = usernameStr.trim();
 
                         final String passwordStr = readString("Password: ");
                         if (isNotNullAndNotEmpty(passwordStr))
                            {
                            final String password = passwordStr.trim();
 
                            final String deviceNameStr = readString("Device Name: ");
                            if (isNotNullAndNotEmpty(deviceNameStr))
                               {
                               final String deviceName = deviceNameStr.trim();
                               final RemoteStorageCredentials remoteStorageCredentials = new RemoteStorageCredentialsImpl(hostName, hostPort, username, password, deviceName);
                               helper.validateAndSetDataStorageCredentials(remoteStorageCredentials);
                               }
                            else
                               {
                               println("Invalid device name.");
                               }
                            }
                         else
                            {
                            println("Invalid password.");
                            }
                         }
                      else
                         {
                         println("Invalid username.");
                         }
                      }
                   }
                else
                   {
                   println("Invalid host name and port.");
                   }
                }
             }
          };
 
    private boolean isNotNullAndNotEmpty(@Nullable final String str)
       {
       if (str != null)
          {
          final String trimmedStr = str.trim();
          return trimmedStr.length() > 0;
          }
       return false;
       }
 
    private final Runnable disconnectFromDeviceAction =
          new Runnable()
          {
          public void run()
             {
             disconnect();
             }
          };
 
    private final Runnable quitAction =
          new Runnable()
          {
          public void run()
             {
             LOG.debug("SpeckGatewayCommandLine.run(): Quit requested by user.");
             disconnect();
             println("Bye!");
             }
          };
 
    private void registerActions()
       {
       registerAction("c", connectToSpeckAction);
       registerAction("u", defineDataStorageCredentials);
       registerAction("s", printStatisticsAction);
       registerAction("l", setLoggingLevelAction);
       registerAction("d", disconnectFromDeviceAction);
 
       registerAction(QUIT_COMMAND, quitAction);
       }
 
    @Override
    protected void startup()
       {
       println("");
       println(SpeckGatewayHelper.APPLICATION_NAME_AND_VERSION_NUMBER);
       println("");
       }
 
    private void disconnect()
       {
       helper.disconnect();
       }
 
    protected final void menu()
       {
       println("COMMANDS -----------------------------------");
       println("");
       println("c         Connect to the Speck");
       println("u         Specify host and login credentials for uploads");
      println("s         Print statistics for samples downloaded, uploaded, and deleted");
       println("l         Set the logging level for the log file (has no effect on console logging)");
       println("d         Disconnect from the device");
       println("");
       println("q         Quit");
       println("");
       println("--------------------------------------------");
       }
    }
