 package com.ericsson.ecom.cli;
 
 import android.content.Intent;
 import android.util.Log;
 import com.ericsson.ecom.app.EcomApplication;
 import com.ericsson.ecom.ssh.SshSession;
 import com.ericsson.ecom.ui.MainActivity;
 
 /**
  * Created with IntelliJ IDEA.
  * User: eranibl
  * Date: 2013-05-07
  * Time: 12:46
  * To change this template use File | Settings | File Templates.
  */
 public class CliSession {
     private static final String LOG = "eCOM - Cli";
     private static final String SSH_SUBSYSTEM = "cli";   // use this for COM CLI
 
     public static final String CLI_UPARROW = "\u001B\u005B\u0041";		// Esc + [ + A
     public static final String CLI_DOWNARROW = "\u001B\u005B\u0042";	// Esc + [ + B
     public static final String CLI_RIGHTARROW = "\u001B\u005B\u0043";	// Esc + [ + C
     public static final String CLI_LEFTARROW = "\u001B\u005B\u0044";	// Esc + [ + D
     public static final String CLI_ERASEDATA = "\u001B\u005B\u004A";	// Esc + [ + J
     public static final String CLI_BACKSPACE = "\u0008";  				// backspace
     public static final String CLI_CTRL_C = "\u0003";  					// Ctrl + C
 
     protected static final String DEFAULT_PROMPT = "(?<!\\w)>\\s*$";
     protected static final int DEFAULT_CLI_PORT = 22;
 
    public CliSession(String server, int port, String user, String password){
         Log.d(LOG, "LOGIN: " + server + ":" + port);
 
         SshSession sshSession = new SshSession(server,SSH_SUBSYSTEM ,user,password);
 
 

 
             app.setSession("ssh", sshSession);
         }
         Log.d(LOG, "LOGIN: " + server + ":" + port);
         sshSession.login();
 
         LoginActivity ctx = LoginActivity.this;
         ctx.startActivity(new Intent(ctx, MainActivity.class))
 
         cliSession =
 

     }
 
 
 
 
 
 
 }
