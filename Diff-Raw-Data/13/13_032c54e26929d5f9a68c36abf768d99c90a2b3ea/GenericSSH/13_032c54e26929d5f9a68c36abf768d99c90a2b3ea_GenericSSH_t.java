 package com.rightscale.ssh.launchers.windows;
 
 import com.rightscale.ssh.*;
 import com.rightscale.ssh.launchers.unix.*;
 import com.rightscale.ssh.launchers.*;
 import javax.swing.JOptionPane;
 import java.io.*;
 
 public class GenericSSH extends SimpleWindowsLauncher {
     public GenericSSH(Launchpad l) {
         if( !isPlatform("Windows") || findExecutable("ssh") == null ) {
             throw new RuntimeException("Wrong OS, or 'ssh' command not found.");
         }
     }
 
     public String getFriendlyName() {
         return "Generic SSH";
     }
 
     public void run(String user, String host, File id) throws IOException {
       File exe = findExecutable("ssh");
      String msg = exe.getCanonicalPath() + "\n" +
                   "It looks like this file may be an SSH client.\n" +
                   "Do you want me to invoke it?";
       String title = "Security Warning";
       int shouldRun =
        JOptionPane.showConfirmDialog(null, msg, title, JOptionPane.YES_NO_OPTION);
 
       if(JOptionPane.YES_OPTION == shouldRun) {
           String[] command = {
             "cmd.exe",
             "/c",
             "start",
             "ssh",
             "-i",
             id.getCanonicalPath(),
             user + "@" + host
           };
 
           debugExec(command);
       }
     }
 }
