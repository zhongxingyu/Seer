 package org.fedorahosted.beaker.cli.commands;
 
 import org.apache.xmlrpc.XmlRpcException;
 import org.fedorahosted.beaker.client.Command;
 import org.fedorahosted.beaker.remote_model.BeakerTask;
 import org.fedorahosted.beaker.remote_model.TaskStatus;
 import org.kohsuke.args4j.Option;
 
 @CliCommand(name="timeRemains", requireAuth=true)
 public class RemainingTimeCommand extends Command {
 
     @Option(name="-id", metaVar="<id>", usage="Task ID", required=true)
     //@Argument(index=1,required=true)
     public String taskId;
     
     public void execute() {
         BeakerTask task = new BeakerTask(taskId,beakerClient);
         try {
             TaskStatus state = task.getInfo().getState();
            if(!TaskStatus.Running.equals(state)) {
                 System.out.println("Task " + taskId + " is not running. Task is in state " + state);
                 return;
             }
             int time =  task.getRemainingTime();
             System.out.println("Reamining time for " + taskId + ": " + time);
         } catch(XmlRpcException e) {
             System.out.println("Cannot get remaining time for task " + taskId + " from Beaker (" + e.getCause() + ")");
             e.printStackTrace();
         }
     }
 }
