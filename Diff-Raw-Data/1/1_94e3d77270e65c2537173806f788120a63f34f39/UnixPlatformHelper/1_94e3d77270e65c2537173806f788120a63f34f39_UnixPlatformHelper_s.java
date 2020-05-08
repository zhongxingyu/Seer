 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package helper.unix;
 
 import helper.unix.parser.DetectPlatformPP;
 import helper.unix.parser.DetectDistributionVersionPP;
 import helper.unix.parser.DetectDistributionNamePP;
 import helper.unix.parser.ListPackagePP;
 import helper.PlatformHelper;
 import helper.ProcessParser;
 import helper.unix.parser.DetectHostPP;
 import helper.parser.SimpeOutputPP;
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.List;
 import models.AppPackage;
 import models.Distribution;
 import models.Host;
 import models.Platform;
 import play.Logger;
 
 /**
  *
  * @author philipp
  */
 public class UnixPlatformHelper implements PlatformHelper {
 
     private Host host = null;
     private Platform platform = null;
     private Distribution distribution = null;
     private Runtime r;
     private String sshPrefix;
 
     //private static UnixPlatformHelper instance = new UnixPlatformHelper();
     private UnixPlatformHelper() {
         r = Runtime.getRuntime();
     }
 
     public static UnixPlatformHelper getInstance() {
         //return instance;
         return new UnixPlatformHelper();
     }
 
     /**
      * First character: The possible value for the first character. The first character signifies the desired state, like we (or some user) is marking the package for installation
     
     u: Unknown (an unknown state)
     i: Install (marked for installation)
     r: Remove (marked for removal)
     p: Purge (marked for purging)
     h: Hold
     
     Second Character: The second character signifies the current state, whether it is installed or not. The possible values are
     
     n: Not- The package is not installed
     i: Inst – The package is successfully installed
     c: Cfg-files – Configuration files are present
     u: Unpacked- The package is stilled unpacked
     f: Failed-cfg- Failed to remove configuration files
     h: Half-inst- The package is only partially installed
     W: trig-aWait
     t: Trig-pend
     
      */
     // TODO needs to add a timeout.
     private void runCommand(String command, ProcessParser pp) {
         if (this.host == null) {
             Logger.error("Host needs to be set!");
         }
         try {
             Logger.info("running: " + this.sshPrefix + " " + command);
             Process p = r.exec(this.sshPrefix + " " + command);
             InputStream in = p.getInputStream();
             BufferedInputStream buf = new BufferedInputStream(in);
             InputStreamReader inread = new InputStreamReader(buf);
             BufferedReader bufferedreader = new BufferedReader(inread);
             pp.parse(bufferedreader);
             try {
                 if (p.waitFor() != 0) {
                     Logger.info("exit value = " + p.exitValue());
                 }
             } catch (InterruptedException e) {
                 System.err.println(e);
             } finally {
                 // Close the InputStream
                 bufferedreader.close();
                 inread.close();
                 buf.close();
                 in.close();
             }
         } catch (IOException ex) {
             Logger.error(ex.getLocalizedMessage());
         }
     }
 
     public List<AppPackage> listPackages() {
         ListPackagePP pp = new ListPackagePP();
         pp.setDistribution(this.distribution);
         runCommand("dpkg -l", pp);
         return pp.getPackages();
     }
 
     private String showArray(String parts[]) {
         StringBuilder sb = new StringBuilder();
         for (int i = 0; i < parts.length; i++) {
             sb.append(i);
             sb.append(" => ");
             sb.append(parts[i]);
             sb.append(" ");
         }
         return sb.toString();
     }
 
     public List<String> searchPackage(String query) {   
         SimpeOutputPP op = new SimpeOutputPP();
         op.startToken = "Paketlisten werden gelesen...";
         runCommand("apt-get update && apt-cache search "+query, op);
         return op.getOutput();    
     }
 
     public boolean installPackage(String packageName) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public boolean removePackage(String packageName) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public void updateRepository() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     /**
      * cat /etc/issue
     -a, --all
     print all information, in the following order, except omit -p and -i if unknown:
     
     -s, --kernel-name
     print the kernel name
     
     -n, --nodename
     print the network node hostname
     
     -r, --kernel-release
     print the kernel release
     
     -v, --kernel-version
     print the kernel version
     
     -m, --machine
     print the machine hardware name
     
     -p, --processor
     print the processor type or "unknown"
     
     -i, --hardware-platform
     print the hardware platform or "unknown"
     
     -o, --operating-system
     print the operating system
      */
     public Platform detectPlatform() {
         DetectPlatformPP dp = new DetectPlatformPP();
         String command = "uname -v && "
                 + "uname -m && "
                 + "uname -o && "
                 + "uname -r";
         runCommand(command, dp);
         this.platform = dp.getPlatform();
         this.platform.distribution = this.distribution;
         this.host.platform = this.platform;
        this.distribution.save();
         return this.platform;
     }
 
     public void setHost(Host host) {
         this.host = host;
         /**
          * thx to http://linuxcommando.blogspot.com/2008/10/how-to-disable-ssh-host-key-checking.html.
          */
         this.sshPrefix = " ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no " + host.user + "@" + host.ip;
     }
 
     public Host getHost() {
         return this.host;
     }
 
     public Distribution dectectDistribution() {
         String command = "ls -a1 /etc/";
         DetectDistributionNamePP ddn = new DetectDistributionNamePP();
         runCommand(command, ddn);
         DetectDistributionVersionPP ddv = new DetectDistributionVersionPP(ddn.getName());
         runCommand(ddv.getCommand(), ddv);
         this.distribution = Distribution.findOrCreateByNameAndVersion(ddn.getName(), ddv.getVersion());
         return this.distribution;
     }
 
     public Host detectHost() {
         DetectHostPP dh = new DetectHostPP(host);
         String command = "hostname && "
                 + "dnsdomainname";
         runCommand(command, dh);
         this.host = dh.getHost();
         return this.host;
     }
 
     public List<String> updatedPackages() {
         SimpeOutputPP op = new SimpeOutputPP();
         op.startToken = "Abhängigkeitsbaum wird aufgebaut...";
         runCommand("apt-get update && apt-get upgrade -s", op);
         return op.getOutput();
     }
 
     public List<String> upgradeDistribution() {
         SimpeOutputPP op = new SimpeOutputPP();
         op.startToken = "Abhängigkeitsbaum wird aufgebaut...";
         runCommand("apt-get update && apt-get dist-upgrade -s", op);
         return op.getOutput();
     }
 }
