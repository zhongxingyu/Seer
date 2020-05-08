 package org.jenkinsci.plugins.remote_terminal_access.lease;
 
 import hudson.AbortException;
 import hudson.model.Executor;
 import org.jenkinsci.main.modules.sshd.SshCommandFactory.CommandLine;
 import org.jenkinsci.plugins.remote_terminal_access.ProcessWithPtyLauncher;
 import org.jenkinsci.plugins.remote_terminal_access.ssh.AbstractRemoteSshCommand;
 import org.kohsuke.args4j.CmdLineException;
 
 import java.io.IOException;
 import java.util.List;
 
 /**
  * Executes the specified command in the node where the given lease is running.
  *
  * @author Kohsuke Kawaguchi
  */
 public class LeaseSshCommand extends AbstractRemoteSshCommand {
     String leaseId;
 
     String alias;
 
     public LeaseSshCommand(CommandLine cmdLine) {
         super(cmdLine);
     }
 
     @Override
     protected void configure(ProcessWithPtyLauncher pb) throws IOException, InterruptedException {
 
         LeaseContext lease = LeaseContext.getById(leaseId);
         if (lease==null)
             throw new AbortException("Invalid lease ID: "+leaseId);
         lease.checkOwner();
 
         Executor x = lease.get(alias);
         if (x==null)
             throw new AbortException("Invalid alias: "+alias);
 
         pb.ws(x.getOwner().getNode().getRootPath());
     }
 
     @Override
     protected int main(List<String> args) throws IOException, InterruptedException, CmdLineException {
        if (args.size()<2)
            throw new CmdLineException("Not enough arguments. Expecting LEASEID ALIAS [cmd...]");

        leaseId = args.get(0);
        alias = args.get(1);
 
        return run(args.subList(2,args.size()));
     }
 }
