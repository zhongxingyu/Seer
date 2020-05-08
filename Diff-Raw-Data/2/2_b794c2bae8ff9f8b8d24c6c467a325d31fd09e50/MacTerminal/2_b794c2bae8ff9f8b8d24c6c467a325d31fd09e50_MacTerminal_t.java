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
       String scr  = script.getCanonicalPath();      
       StringBuffer cmdbuf = new StringBuffer();
 
       cmdbuf.append(" ");
       if(identity != null) {
         cmdbuf.append("-i ");
        cmdbuf.append(identity);
         cmdbuf.append(" ");
       }
       cmdbuf.append(hostname);
       String cmdline = cmdbuf.toString();
 
       getRuntime().exec( scr + cmdline );
     }
 
     private File createScript() throws IOException {
         File dir = _launchpad.getSafeDirectory();
 
         File script = new File(dir, "RightScale_SSH_Launcher");
 
         if(script.exists()) {
             script.delete();
         }
         
         FileOutputStream fos = new FileOutputStream(script);
         BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
 
         InputStream in = getClass().getResourceAsStream("/RightScale_SSH_Launcher.scpt");
         BufferedReader br = new BufferedReader(new InputStreamReader(in));
 
         String crlf = System.getProperty("line.separator");
         String line = null;
 
         do {
             line = br.readLine();
             if(line != null) {
                 bw.write(line); bw.write(crlf);
             }
         } while(line != null);
 
         br.close();
         bw.close();
         
         getRuntime().exec("chmod 0700 " + script.getCanonicalPath());
 
         //HACK - sleep for a bit so the JVM picks up the file's change of perms
         try {
             Thread.sleep(250);
         }
         catch(InterruptedException e) {}
 
         return script;
     }
 }
