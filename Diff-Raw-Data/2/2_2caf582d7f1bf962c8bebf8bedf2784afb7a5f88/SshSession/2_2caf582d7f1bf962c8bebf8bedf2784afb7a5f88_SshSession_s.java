 package org.ccci.ssh;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.nio.charset.Charset;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.log4j.Logger;
 
 import ch.ethz.ssh2.ChannelCondition;
 import ch.ethz.ssh2.Connection;
 import ch.ethz.ssh2.SCPClient;
 import ch.ethz.ssh2.ServerHostKeyVerifier;
 import ch.ethz.ssh2.Session;
 import ch.ethz.ssh2.StreamGobbler;
 
 import com.google.common.base.Charsets;
 import com.google.common.io.CharStreams;
 
 public class SshSession
 {
     private Logger log = Logger.getLogger(getClass());
     
     private final int sshSocketConnectTimeout = 10;
     
     private final SshEndpoint endpoint;
     private final  ServerHostKeyVerifier verifier;
     private Connection connection;
     
     public SshSession(SshEndpoint endpoint, ServerHostKeyVerifier verifier)
     {
         this.endpoint = endpoint;
         this.verifier = verifier;
     }
     
     Charset charset = Charsets.UTF_8;
 
     public void connect() throws IOException
     {
         connection = new Connection(endpoint.getHostName());
         
         connection.connect(verifier, (int) TimeUnit.SECONDS.toMillis(sshSocketConnectTimeout), 0);
         boolean success = connection.authenticateWithPassword(endpoint.getUsername(), endpoint.getPassword());
         if (!success)
            throw new IOException("ssh password authentication to " + endpoint.getHostName() + "for user " + endpoint.getUsername() + " refused");
     }
 
 
     
     
     public void close()
     {
         connection.close();
     }
 
     
     private void disconnectSessionAndLogExceptions(Session session )
     {
         try
         {
             session.close();
         }
         catch (Exception e)
         {
             log.error("exception while disconnecting session", e);
         }
     }
 
     /**
      * Executes a single command.  If the exit status is 0 (success), the output (written to stdout) is returned.
      * Otherwise, a {@link RemoteExecutionFailureException} is thrown that contains stdout and stderr output, as well as the exit 
      * code.
      * 
      * @throws RemoteExecutionFailureException if the command fails
      * @throws IOException if the sftp transport fails
      */
     public String executeSingleCommand(String command) throws IOException
     {
         Session session = connection.openSession();
         try
         {
             session.requestDumbPTY();
             InputStream stdout = new StreamGobbler(session.getStdout());
             InputStream stderr = new StreamGobbler(session.getStderr());
             session.execCommand(command);
             
             String output = CharStreams.toString(new InputStreamReader(stdout, Charsets.UTF_8));
             String errorOutput = CharStreams.toString(new InputStreamReader(stderr, Charsets.UTF_8));
             
             session.waitForCondition(ChannelCondition.EXIT_STATUS, TimeUnit.SECONDS.toMillis(1));
             
             Integer exitStatus = session.getExitStatus();
             if (exitStatus == null)
                 log.warn("unable to determine exit status for command '" + command + "'");
             else if (exitStatus > 0)
                 throw new RemoteExecutionFailureException(command, exitStatus, output, errorOutput);
             return output;
         }
         finally
         {
             disconnectSessionAndLogExceptions(session);
         }
     }
 
     /**
      * Enables the convenient execution of a command as a different user.  This prepends "sudo -u {requiredUser}"
      * to each command executed via {@link #executeSingleCommand(String)}, as long as the requiredUser
      * is different from the currently logged in user.  Otherwise, the command is executed exactly as given,
      * without a prefix.
      * 
      * <p>Example:
      * <code><pre>
      * session.as("jboss").executeSingleCommand("jboss.sh start");
      * </pre></code>
      * 
      * @throws RemoteExecutionFailureException if the query to find the current user fails
      * @throws IOException if the sftp transport fails
      */
     public AsUser as(String requiredUser) throws IOException
     {
         return new AsUser(requiredUser);
     }
     
     public class AsUser
     {
         private final String prefix;
 
         private AsUser(String requiredUser) throws IOException
         {
             this.prefix = getSwitchUserPrefixIfNecessary(requiredUser);
         }
 
         /**
          * Exactly like {@link SshSession#executeSingleCommand(String)}, except will be run
          * as the user specified in {@link SshSession#as(String)}
          */
         public String executeSingleCommand(String command) throws IOException
         {
             return SshSession.this.executeSingleCommand(prefix + command);
         }
 
         private String getSwitchUserPrefixIfNecessary(String requiredUser) throws IOException
         {
             String username = SshSession.this.executeSingleCommand("id -un").trim();
             
             String prefix;
             if (username.equals(requiredUser))
             {
                 prefix = "";
             }
             else
             {
                 prefix = "sudo -u " + requiredUser + " ";
             }
             return prefix;
         }
 
     }
     
     public void sendFile(String localFilePath, String destinationDirectory, String destinationFileName, String mode) throws IOException
     {
         SCPClient scpClient = connection.createSCPClient();
         scpClient.put(localFilePath, destinationFileName, destinationDirectory, mode);
     }
     
 }
