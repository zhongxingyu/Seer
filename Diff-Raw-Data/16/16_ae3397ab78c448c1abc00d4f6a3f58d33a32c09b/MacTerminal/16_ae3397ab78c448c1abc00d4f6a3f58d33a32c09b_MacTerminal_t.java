 package com.rightscale.ssh.launchers.osx;
 
 import com.rightscale.ssh.*;
 import com.rightscale.ssh.launchers.*;
 import com.rightscale.util.*;
 import java.io.*;
 
 public class MacTerminal extends SimpleLauncher {
     private Launchpad _launchpad = null;
 
     public MacTerminal(Launchpad l) {
         _launchpad = l;
         
         if( !isPlatform("Mac") || !canInvoke("open -h") ) {
             throw new RuntimeException("Wrong OS, or 'open' command not found.");
         }
     }
 
     public void run(String username, String hostname, File identity) throws IOException {
       File script = createScript();
       writeParams(username, hostname, identity);
 
       String scr = script.getCanonicalPath();
       
       Process p       = null;
       boolean success = false;
 
 // iTerm seems to have a bug: it won't actually open the files it's passed via "open"
 //      try {
 //          p = getRuntime().exec( "open -a iTerm " + scr );
 //          success = ( 0 == p.waitFor() );
 //      } catch(InterruptedException e) {
 //      }
 //
       if(!success) {
          p = getRuntime().exec( "open -a Terminal " + scr );
       }
     }
 
     private void writeParams(String username, String hostname, File identity)
             throws IOException
     {
         File dir = _launchpad.getSafeDirectory();
         File scriptParams = new File(dir, "RightScale_SSH_Launcher.params");
 
         String params =
               "SSH_USERHOST=\"" + username + "@" + hostname + "\"\n";
 
         if(identity != null) {
             params = params +
                   "SSH_IDENTITY=\"" + identity.getCanonicalPath() + "\"\n";
         }
 
 
         FileUtils.writeText(params, scriptParams);
 
         getRuntime().exec("chmod 0600 " + scriptParams.getCanonicalPath());
     }
 
     private File createScript() throws IOException {
         File dir = _launchpad.getSafeDirectory();
 
         File script = new File(dir, "RightScale_SSH_Launcher");
         File scriptParams = new File(dir, "RightScale_SSH_Launcher.params");
 
         if(!script.isFile() || !script.canRead()) {
           FileOutputStream fos = new FileOutputStream(script);
           BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
 
           bw.write("#!/bin/sh\n");
           bw.write("clear\n");
           bw.write(". " + scriptParams.getCanonicalPath() + "\n\n");
           bw.write("if [ x$SSH_IDENTITY != x ];\n");
           bw.write("then\n");
           bw.write("    ssh -i $SSH_IDENTITY $SSH_USERHOST\n");
           bw.write("else\n");
           bw.write("    ssh $SSH_USERHOST\n");
           bw.write("fi\n");
           bw.close();
 
           getRuntime().exec("chmod 0700 " + script.getCanonicalPath());
         }
 
         return script;
     }
 }
