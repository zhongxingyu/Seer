 /**
  * SamHelper
  * 29.03.2012
  * @author Philipp Haussleiter
  *
  */
 package helper.sam;
 
 import helper.SystemHelper;
 import helper.parser.SimpeOutputPP;
 import models.Host;
 
 /**
  *
  * @author philipp
  */
 public class SamHelper extends SystemHelper {
 
     private static SamHelper instance = new SamHelper();
 
     private SamHelper() {
         super();
         this.host = Host.findOrCreateByUserAndIp("root", "127.0.0.1");
        this.sshCmdPrefix =   " ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no " + this.host.user + "@" + this.host.ip;
        this.sshCheckPrefix = " ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no " + this.host.user + "@" + this.host.ip + " -qo PasswordAuthentication=no echo 0 || echo 1";
     }
 
     public static SamHelper getInstance() {
         return instance;
     }
 
     public String getPublicSSHKey() {
         SimpeOutputPP so = new SimpeOutputPP();
         String command = "cat ~/.ssh/id_rsa.pub";
         runCommand(command, so);
         if (!so.getOutput().isEmpty()) {
             return so.getOutput().get(0);
         }
         return "";
     }
 }
