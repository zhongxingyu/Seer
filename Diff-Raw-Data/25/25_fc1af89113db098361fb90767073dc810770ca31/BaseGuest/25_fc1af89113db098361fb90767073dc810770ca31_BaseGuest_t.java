 package fr.fastconnect.cloudify.driver.provisioning.virtualbox.api.guest;
 
 import java.util.Arrays;
 import java.util.List;
 import java.util.concurrent.locks.ReentrantLock;
 import java.util.logging.Level;
 
 import org.apache.commons.lang.StringUtils;
 import org.virtualbox_4_2.IConsole;
 import org.virtualbox_4_2.IGuest;
 import org.virtualbox_4_2.IGuestProcess;
 import org.virtualbox_4_2.IGuestSession;
 import org.virtualbox_4_2.IMachine;
 import org.virtualbox_4_2.ISession;
 import org.virtualbox_4_2.ProcessCreateFlag;
 import org.virtualbox_4_2.ProcessStatus;
 import org.virtualbox_4_2.ProcessWaitForFlag;
 import org.virtualbox_4_2.VirtualBoxManager;
 
 import fr.fastconnect.cloudify.driver.provisioning.virtualbox.api.VirtualBoxException;
 
 public abstract class BaseGuest implements VirtualBoxGuest {
     
     private static final java.util.logging.Logger logger = java.util.logging.Logger
             .getLogger(BaseGuest.class.getName());
     
     private static final ReentrantLock mutex = new ReentrantLock();
     
    protected VirtualBoxManager virtualBoxManager;
     
     protected BaseGuest(VirtualBoxManager virtualBoxManager) {
         this.virtualBoxManager = virtualBoxManager;
     }
     
     public long executeCommand(String machineGuid, String login, String password, String command, List<String> args, List<String> envs, long endTime) throws Exception {
         
         logger.log(Level.INFO, "Trying to execute command on machine '"+machineGuid+"': "+command+" "+StringUtils.join(args, ' '));
         
         IMachine m = virtualBoxManager.getVBox().findMachine(machineGuid);
         
         mutex.lock();
         
         try{
             ISession session = virtualBoxManager.openMachineSession(m);
             m = session.getMachine();
             IConsole console = session.getConsole();
             IGuest guest = console.getGuest();
             
             IGuestSession guestSession = guest.createSession(login, password, "", "");
             
             try{
                 
                 long timeLeft = endTime - System.currentTimeMillis();
                 IGuestProcess process = guestSession.processCreate(
                         command, 
                         args, 
                         envs,
                         Arrays.asList(ProcessCreateFlag.None),
                         timeLeft);
                 
                 timeLeft = endTime - System.currentTimeMillis();
                 //process.waitForArray(Arrays.asList(ProcessWaitForFlag.Terminate, ProcessWaitForFlag.StdErr, ProcessWaitForFlag.StdOut), 60l*1000);
                 //process.waitForArray(Arrays.asList(ProcessWaitForFlag.Terminate), 60l*1000);
                 process.waitFor(new Long(ProcessWaitForFlag.Terminate.value()), timeLeft);
                 
                 if(process.getStatus() != ProcessStatus.TerminatedNormally){
                     
                     throw new VirtualBoxException("Unable to execute command '"+command+" "+StringUtils.join(args, ' ')+"': Status "+process.getStatus() +" ExitCode "+process.getExitCode());
                 }
                 
                 // TODO: get the stdout/stderr with this webservice... 
                 
                 return process.getPID();
             }
             finally{
                 guestSession.close();
                 this.virtualBoxManager.closeMachineSession(session);
             }
         }
         finally{
             mutex.unlock();
         }
     }
 }
