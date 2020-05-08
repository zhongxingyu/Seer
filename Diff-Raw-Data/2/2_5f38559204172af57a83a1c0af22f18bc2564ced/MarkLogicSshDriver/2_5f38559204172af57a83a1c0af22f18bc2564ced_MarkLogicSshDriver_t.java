 package io.cloudsoft.marklogic;
 
 import brooklyn.entity.basic.AbstractSoftwareProcessSshDriver;
 import brooklyn.entity.basic.EntityLocal;
 import brooklyn.location.basic.SshMachineLocation;
 import brooklyn.util.MutableMap;
 
 import java.io.File;
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import static brooklyn.util.ssh.CommonCommands.dontRequireTtyForSudo;
 import static brooklyn.util.ssh.CommonCommands.sudo;
 
 public class MarkLogicSshDriver extends AbstractSoftwareProcessSshDriver implements MarkLogicDriver {
 
     public final static AtomicInteger counter = new AtomicInteger(2);
     private final int nodeId;
 
     public MarkLogicSshDriver(EntityLocal entity, SshMachineLocation machine) {
         super(entity, machine);
         this.nodeId = counter.getAndIncrement();
     }
 
     public String getDownloadFilename() {
         // TODO To support other platforms, need to customize this based on OS
         return "MarkLogic-" + getVersion() + ".x86_64.rpm";
     }
 
     public int getNodeId() {
         return nodeId;
     }
 
     public int getFcount() {
         return entity.getConfig(MarkLogicNode.FCOUNT);
     }
     
     public String getWebsiteUsername() {
         return entity.getConfig(MarkLogicNode.WEBSITE_USERNAME);
     }
     
     public String getWebsitePassword() {
         return entity.getConfig(MarkLogicNode.WEBSITE_PASSWORD);
     }
 
     public String getUser() {
         return entity.getConfig(MarkLogicNode.USER);
     }
 
     public String getPassword() {
         return entity.getConfig(MarkLogicNode.PASSWORD);
     }
 
     public String getLicenseKey() {
         return entity.getConfig(MarkLogicNode.LICENSE_KEY);
     }
 
     public String getAwsAccessKey() {
         return entity.getConfig(MarkLogicNode.AWS_ACCESS_KEY);
     }
 
     public String getAwsSecretKey() {
         return entity.getConfig(MarkLogicNode.AWS_SECRET_KEY);
     }
 
     public String getLicensee() {
         return entity.getConfig(MarkLogicNode.LICENSEE).replace(" ", "%20");
     }
 
     public String getCluster() {
         return entity.getConfig(MarkLogicNode.CLUSTER).replace(" ", "%20");
     }
 
     public String getMasterAddress() {
         return entity.getConfig(MarkLogicNode.MASTER_ADDRESS);
     }
 
     public boolean isMaster() {
         return entity.getConfig(MarkLogicNode.IS_MASTER);
     }
 
     public File getBrooklynMarkLogicHome() {
         String home = System.getenv("BROOKLYN_MARKLOGIC_HOME");
         if (home == null) {
             home = System.getProperty("user.dir");
             log.warn("BROOKLYN_MARKLOGIC_HOME not found in environment, defaulting to [{}]", home);
         }
         return new File(home);
     }
 
     public File getScriptDirectory() {
         return new File(getBrooklynMarkLogicHome(), "scripts");
     }
 
     public File getUploadDirectory() {
         return new File(getBrooklynMarkLogicHome(), "upload");
     }
 
 
     @Override
     public void install() {
         boolean master = isMaster();
         if (master) {
             log.info("Starting installation of MarkLogic master " + getHostname());
              uploadFiles();
         } else {
             log.info("Slave " + getHostname() + " waiting for master to be up");
             uploadFiles();
 
             //a very nasty hack to wait on the service up from the
 
             entity.getConfig(MarkLogicNode.IS_BACKUP_EBS);
             log.info("Starting installation of MarkLogic slave " + getHostname());
         }
 
         File installScriptFile = new File(getScriptDirectory(), "install.txt");
         String installScript = processTemplate(installScriptFile);
         List<String> commands = new LinkedList<String>();
         commands.add(dontRequireTtyForSudo());
         commands.add(installScript);
         newScript(INSTALLING)
                 .failOnNonZeroResultCode()
                 .setFlag("allocatePTY", true)
                 .body.append(commands)
                 .execute();
 
         if (master) {
             log.info("Finished installation of MarkLogic master " + getHostname());
         } else {
             log.info("Finished installation of MarkLogic slave " + getHostname());
         }
     }
 
     private void uploadFiles() {
         log.info("Starting upload to" + getHostname());
 
 
         File uploadDirectory = getUploadDirectory();
         String targetDirectory = "./";
         uploadFiles(uploadDirectory, targetDirectory);
 
         log.info("Finished upload to " + getHostname());
     }
 
     private void uploadFiles(File dir, String targetDirectory) {
         getLocation().exec(Arrays.asList("mkdir -p "+targetDirectory), MutableMap.of());
 
         for (File file : dir.listFiles()) {
             final String targetLocation = targetDirectory + "/" + file.getName();
             if (file.isDirectory()) {
                 uploadFiles(file, targetLocation);
            } else if (file.isFile() && !file.getName().equals(".DS_Store")) {
                 log.info("Copying file: "+targetLocation);
                 getLocation().copyTo(file, targetLocation);
             }
         }
     }
 
     @Override
     public void customize() {
         // no-op; everything done in install()
     }
 
     @Override
     public void launch() {
         List<String> commands = new LinkedList<String>();
         commands.add(sudo("/etc/init.d/MarkLogic start"));
         commands.add("sleep 10"); // Have seen cases where startup takes some time
 
         //// TODO Where does clusterJoin.py etc come from?
         //if (entity.getConfig(MarkLogicNode.IS_MASTER)) {
         //    // TODO
         //} else {
         //    //String masterInstance = entity.getConfig(MarkLogicNode.MASTER_ADDRESS);
         //    //commands.add(sudo(format("python clusterJoin.py -n hosts.txt -u ec2-user -l license.txt -c %s > init_ml", masterInstance)));
         //    // TODO More stuff like this
         //}
 
         newScript(LAUNCHING)
                 .failOnNonZeroResultCode()
                 .body.append(commands)
                 .execute();
 
         if (isMaster()) {
             log.info("Successfully launched MarkLogic master " + getHostname());
         } else {
             log.info("Successfully launched MarkLogic slave " + getHostname());
         }
     }
 
     @Override
     public void postLaunch() {
         entity.setAttribute(MarkLogicNode.URL, String.format("http://%s:%s", getHostname(), 8001));
     }
 
     public boolean isRunning() {
         int exitStatus = newScript(LAUNCHING)
                 .failOnNonZeroResultCode()
                 .body.append(sudo("/etc/init.d/MarkLogic status | grep running"))
                 .execute();
         return exitStatus == 0;
     }
 
     @Override
     public void stop() {
         newScript(LAUNCHING)
                 .failOnNonZeroResultCode()
                 .body.append(sudo("/etc/init.d/MarkLogic stop"))
                 .execute();
     }
 }
