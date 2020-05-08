 package org.protege.owl.server.deploy;
 
 import java.io.File;
 import java.io.IOException;
 
 public class LinuxInstaller extends UnixInstaller {
	public static final String UPDATE_RCD_BIN = "/usr/sbin/update-rc.d";
	public static final String CHKCONFIG_BIN  = "/usr/sbin/chkconfig";
	
 	private File initdFile = new File("/etc/init.d/protege");
 	private File protegeDefaults  = new File("/etc/default/protege");
 	
 	public LinuxInstaller(Configuration configuration) {
 		super(configuration);
 	}
 
 	@Override
 	protected void postInstall() throws IOException {
 	    installUnixScripts();
 	}
 	
 	@Override
 	protected void postDeploy() throws IOException {
 		log("Configuring the /etc/init.d and /etc/rc*.d scripts");
         getConfiguration().copyWithReplacements(getResource("unix/protege.defaults"), protegeDefaults);
         getConfiguration().copyWithReplacements(getResource("unix/protege"), initdFile);
         makeExecutable(initdFile);
        if (new File(UPDATE_RCD_BIN).exists()) {
        	run(null, UPDATE_RCD_BIN, "protege", "defaults");
        }
        else if (new File(CHKCONFIG_BIN).exists()) {
        	run(null, CHKCONFIG_BIN, "--add", "protege");
        }
         run(null, "/etc/init.d/protege", "start");
 	}
 
 	@Override
 	protected void doUndeploy() throws IOException {
 		if (initdFile.exists()) {
 			log("Stopping Protege OWL Server");
 	        run(null, "/etc/init.d/protege", "stop");
 	        log("Deleting /etc/init.d and /etc/rc*.d Protege OWL Server scripts");
 	        initdFile.delete();
 	        protegeDefaults.delete();
	        if (new File(UPDATE_RCD_BIN).exists()) {
	        	run(null, UPDATE_RCD_BIN, "protege", "remove");
	        }
	        else if (new File(CHKCONFIG_BIN).exists()) {
	        	run(null, CHKCONFIG_BIN, "--del", "protege");
	        }
 	        File logDir  = new File(getConfiguration().getParameterValue(Parameter.LOG_PREFIX));
 	        logDir.delete();
 		}
 	}
 
 
 }
