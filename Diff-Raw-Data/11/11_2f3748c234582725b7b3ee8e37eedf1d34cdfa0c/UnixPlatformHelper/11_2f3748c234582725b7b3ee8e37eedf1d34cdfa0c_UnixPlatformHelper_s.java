 /**
  * UnixPlatformHelper
  * 29.03.2012
  * @author Philipp Haussleiter
  *
  */
 package helper.unix;
 
 import helper.unix.parser.DetectPlatformPP;
 import helper.unix.parser.DetectDistributionVersionPP;
 import helper.unix.parser.DetectDistributionNamePP;
 import helper.unix.parser.ListPackagePP;
 import helper.PlatformHelper;
 import helper.SystemHelper;
 import helper.unix.parser.DetectHostPP;
 import helper.unix.parser.SearchPackagePP;
 import helper.unix.parser.UpdatePackagePP;
 import helper.unix.parser.UpgradePackagePP;
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
 public class UnixPlatformHelper extends SystemHelper implements PlatformHelper {
 
     //private static UnixPlatformHelper instance = new UnixPlatformHelper();
     private UnixPlatformHelper() {
         super();
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
     public List<AppPackage> listPackages() {
         ListPackagePP pp = new ListPackagePP();
         pp.setDistribution(distribution);
         pp.setHost(host);
         runCommand(pp.getCommand(), pp);
         return pp.getPackages();
     }
 
     public List<String> searchPackage(String query) {
         SearchPackagePP sp = new SearchPackagePP(distribution);
         runCommand(sp.getCommand(query), sp);
         return sp.getOutput();
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
         DetectPlatformPP dp = new DetectPlatformPP(this.distribution);
         runCommand(dp.getCommand(), dp);
         platform = dp.getPlatform();
         platform.distribution = distribution.save();
         Logger.info("Platform: " + platform);
         return platform.save();
     }
 
     public Distribution dectectDistribution() {
         String command = "ls -a1 /etc/";
         DetectDistributionNamePP ddn = new DetectDistributionNamePP();
         runCommand(command, ddn);
         DetectDistributionVersionPP ddv = new DetectDistributionVersionPP(ddn.getName());
         runCommand(ddv.getCommand(), ddv);
         distribution = Distribution.findOrCreateByNameAndVersion(ddn.getName(), ddv.getVersion());
         Logger.info("Distribution: " + distribution);
         return distribution.save();
     }
 
     public Host detectHost() {
         DetectHostPP dh = new DetectHostPP(host);
         String command = "hostname && "
                 + "dnsdomainname";
         runCommand(command, dh);
         host = dh.getHost();
         Logger.info("Host: " + host);
         return host;
     }
 
     public List<String> updatedPackages() {
         UpdatePackagePP up = new UpdatePackagePP(this.distribution);
         runCommand(up.getCommand(), up);
         return up.getOutput();
     }
 
     public List<String> upgradeDistribution() {
         UpgradePackagePP up = new UpgradePackagePP(this.distribution);
         runCommand(up.getCommand(), up);
         return up.getOutput();
     }
 
     public void setHost(Host host) {
         this.host = host;
         platform = host.platform;
         distribution = host.getDistribution();
         /**
          * thx to http://linuxcommando.blogspot.com/2008/10/how-to-disable-ssh-host-key-checking.html.
          */
         sshCmdPrefix = " ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no " + host.user + "@" + host.ip;
         /**
          * thx to http://freeunixtips.com/2009/03/ssh-pw-prompt/
          */
        sshCheckPrefix = "ssh " + this.host.user + "@" + this.host.ip + " -qo PasswordAuthentication=no echo 0 || echo 1";
     }
 
     public Host getHost() {
         host.platform = platform.save();
         return host.save();
     }
 
     public Distribution getDistribution() {
         return this.distribution;
     }
 }
