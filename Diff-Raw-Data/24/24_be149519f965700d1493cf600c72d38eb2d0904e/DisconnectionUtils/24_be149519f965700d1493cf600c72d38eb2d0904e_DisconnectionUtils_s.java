 package org.cloudifysource.quality.iTests.framework.utils;
 
 import org.cloudifysource.quality.iTests.test.AbstractTestSupport;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.services.byon.ByonCloudService;
 
 import java.util.concurrent.TimeUnit;
 
 /**
  * Created with IntelliJ IDEA.
  * User: elip
  * Date: 3/13/13
  * Time: 3:55 PM
  * To change this template use File | Settings | File Templates.
  */
 public class DisconnectionUtils {
 
     public static void restartMachineAndWait(final String machine) throws Exception {
         restartMachine(machine);
         LogUtils.log("Sleeping for 30 seconds...");
         Thread.sleep(30 * 1000);
         LogUtils.log("Waiting for SSH service to restore...");
         AssertUtils.repetitive(new IRepetitiveRunnable() {
             @Override
             public void run() throws Exception {
                 SSHUtils.validateSSHUp(machine, ByonCloudService.BYON_CLOUD_USER, ByonCloudService.BYON_CLOUD_PASSWORD);
             }
         }, (int) AbstractTestSupport.OPERATION_TIMEOUT);
         LogUtils.log("SSH restored. Sleeping for 30 seconds...");
         Thread.sleep(30 * 1000);
     }
 
     public static void restartMachine(String toKill) {
        LogUtils.log(SSHUtils.runCommand(toKill, TimeUnit.SECONDS.toMillis(30),
                "sudo shutdown now -r", ByonCloudService.BYON_CLOUD_USER, ByonCloudService.BYON_CLOUD_PASSWORD));
     }
 }
