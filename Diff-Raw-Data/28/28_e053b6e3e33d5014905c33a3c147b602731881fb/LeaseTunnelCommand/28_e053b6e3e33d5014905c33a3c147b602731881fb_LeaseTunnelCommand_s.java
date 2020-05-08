 package org.jenkinsci.plugins.remote_terminal_access.lease;
 
 import org.apache.sshd.common.Factory;
 import org.apache.sshd.server.Command;
 import org.apache.sshd.server.CommandFactory;
 import org.jenkinsci.main.modules.sshd.SshCommandFactory.CommandLine;
 import org.jenkinsci.plugins.remote_terminal_access.ssh.AbstractTunnelCommand;
 import org.jenkinsci.plugins.remote_terminal_access.ssh.DiagnoseCommand;
 import org.kohsuke.args4j.Argument;
 import org.kohsuke.args4j.CmdLineParser;
 
 import java.util.List;
 
 /**
  * @author Kohsuke Kawaguchi
  */
 public class LeaseTunnelCommand extends AbstractTunnelCommand {
     @Argument(metaVar="LEASEID",index=0,required=true)
     String leaseId;
 
     @Argument(metaVar="ALIAS",index=1,required=true)
     String alias;
 
     public LeaseTunnelCommand(CommandLine cmdLine) {
         super(cmdLine);
     }
 
     @Override
     protected int run() throws Exception {
         List<String> args = getCmdLine().subList(1, getCmdLine().size());
         new CmdLineParser(this).parseArgument(args);
 
         return super.run();
     }
 
     @Override
     protected CommandFactory getCommandFactory() {
         return new CommandFactory() {
             public Command createCommand(String command) {
                return new DiagnoseCommand(new CommandLine(String.format("lease-ssh '%s' '%s' %s",leaseId,alias,command)));
             }
         };
     }
 
     @Override
     protected Factory<Command> getShellFactory() {
         return new Factory<Command>() {
             public Command create() {
                return new DiagnoseCommand(new CommandLine(String.format("lease-ssh '%s' '%s'",leaseId,alias)));
             }
         };
     }
 }
