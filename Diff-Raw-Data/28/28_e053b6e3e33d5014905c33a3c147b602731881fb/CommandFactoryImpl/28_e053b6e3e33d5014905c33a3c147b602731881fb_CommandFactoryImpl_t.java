 package org.jenkinsci.plugins.remote_terminal_access.lease;
 
 import hudson.Extension;
 import org.apache.sshd.server.Command;
 import org.jenkinsci.main.modules.sshd.SshCommandFactory;
 
 /**
  * Adds the lease related SSH commands
  *
  * @author Kohsuke Kawaguchi
  */
 @Extension
 public class CommandFactoryImpl extends SshCommandFactory {
     @Override
     public Command create(CommandLine commandLine) {
         String cmd = commandLine.get(0);
        if (cmd.equals("lease-ssh"))
             return new LeaseSshCommand(commandLine);
         if (cmd.equals("lease-tunnel"))
             return new LeaseTunnelCommand(commandLine);
         return null;
     }
 }
