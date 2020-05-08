 package org.jenkinsci.plugins.remote_terminal_access.lease;
 
 import hudson.AbortException;
 import hudson.model.Executor;
 import org.jenkinsci.main.modules.sshd.SshCommandFactory.CommandLine;
 import org.jenkinsci.plugins.remote_terminal_access.ProcessWithPtyLauncher;
 import org.jenkinsci.plugins.remote_terminal_access.ssh.AbstractRemoteSshCommand;
 import org.kohsuke.args4j.Argument;
 import org.kohsuke.args4j.CmdLineException;
 import org.kohsuke.args4j.CmdLineParser;
 import org.kohsuke.args4j.Option;
 import org.kohsuke.args4j.spi.StopOptionHandler;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Executes the specified command in the node where the given lease is running.
  *
  * @author Kohsuke Kawaguchi
  */
 public class LeaseSshCommand extends AbstractRemoteSshCommand {
     @Argument(metaVar="LEASEID",index=0,required=true)
     String leaseId;
 
     @Argument(metaVar="ALIAS",index=1,required=true)
     String alias;
 
    @Argument(index=2)
     @Option(name="--",handler=StopOptionHandler.class)
     final List<String> rest = new ArrayList<String>();
 
     public LeaseSshCommand(CommandLine cmdLine) {
         super(cmdLine);
     }
 
     @Override
     protected void configure(ProcessWithPtyLauncher pb) throws IOException, InterruptedException {
 
         LeaseContext lease = LeaseContext.getById(leaseId);
         if (lease==null)
             throw new AbortException("Invalid lease ID: "+leaseId);
         Executor x = lease.get(alias);
         if (x==null)
             throw new AbortException("Invalid alias: "+alias);
 
         pb.ws(x.getOwner().getNode().getRootPath());
     }
 
     @Override
     protected int main(List<String> args) throws IOException, InterruptedException, CmdLineException {
         new CmdLineParser(this).parseArgument(args);
 
         return run(rest);
     }
 }
