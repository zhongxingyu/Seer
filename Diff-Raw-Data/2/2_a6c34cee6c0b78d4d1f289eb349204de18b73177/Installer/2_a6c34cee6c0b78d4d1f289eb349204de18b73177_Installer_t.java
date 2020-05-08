 package fedora.utilities.install;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.Writer;
 import java.util.Properties;
 
 import org.apache.log4j.PropertyConfigurator;
 
 import fedora.utilities.FileUtils;
 import fedora.utilities.Zip;
 import fedora.utilities.install.container.Container;
 import fedora.utilities.install.container.ContainerFactory;
 import fedora.utilities.install.container.FedoraWebXML;
 
 public class Installer {
 	static {
 		//send all log4j (WARN only) output to STDOUT
 		Properties props = new Properties();
 		props.setProperty("log4j.appender.STDOUT",
 		         "org.apache.log4j.ConsoleAppender");
         props.setProperty("log4j.appender.STDOUT.layout", 
                 "org.apache.log4j.PatternLayout");
        props.setProperty("log4j.appender.STDOUT.layout.ConversionPattern",
                 "%p (%c{1}) %m%n");
 		props.setProperty("log4j.rootLogger", "WARN, STDOUT");
 		PropertyConfigurator.configure(props);
 		
 		//tell commons-logging to use log4j
 		final String pfx = "org.apache.commons.logging.";
 		if (System.getProperty(pfx + "LogFactory") == null) {
 			System.setProperty(pfx + "LogFactory", pfx + "impl.Log4jFactory");
 			System.setProperty(pfx + "Log", pfx + "impl.Log4JLogger");
 		}
 	}
 	
     private Distribution _dist;
     private InstallOptions _opts;
     
     private File fedoraHome;
     private File installDir;
     
     public Installer(Distribution dist,
                      InstallOptions opts) {
         _dist = dist;
         _opts = opts;
         fedoraHome = new File(_opts.getValue(InstallOptions.FEDORA_HOME));
         installDir = new File(fedoraHome, "install" + File.separator);
     }
 
     /**
      * Install the distribution based on the options.
      */
     public void install() throws InstallationFailedException {
     	installDir.mkdirs();
     	new FedoraHome(_dist, _opts).install();
     	
     	Container container = ContainerFactory.getContainer(_dist, _opts);
     	container.install();
 		container.deploy(buildWAR());
 		if (_opts.getBooleanValue(InstallOptions.DEPLOY_LOCAL_SERVICES, true)) {
 			deployLocalService(container, Distribution.FOP_WAR);
 			deployLocalService(container, Distribution.IMAGEMANIP_WAR);
 			deployLocalService(container, Distribution.SAXON_WAR);
 			deployLocalService(container, Distribution.DEMO_WAR);
 		}
 		
 		Database database = new Database(_dist, _opts);
 		database.install();
 		
 		// Write out the install options used to a properties file in the install directory
 		try {
 			OutputStream out = new FileOutputStream(new File(installDir, "install.properties"));
 			_opts.dump(out);
 			out.close();
 		} catch (Exception e) {
 			throw new InstallationFailedException(e.getMessage(), e);
 		}
 		System.out.println("Installation complete.");
     }
     
     
     private File buildWAR() throws InstallationFailedException {
     	System.out.println("Preparing fedora.war...");
 		// build a staging area in FEDORA_HOME
     	try {
 			File warStage = new File(installDir, "fedorawar" + File.separator);
 			warStage.mkdirs();
 			Zip.unzip(_dist.get(Distribution.FEDORA_WAR), warStage);
 			
 			// modify web.xml
 			System.out.println("Processing web.xml");
 	        File distWebXML = new File(warStage, "WEB-INF/web.xml");
 	        FedoraWebXML webXML = new FedoraWebXML(distWebXML.getAbsolutePath(), _opts);
 	        Writer outputWriter = new BufferedWriter(new FileWriter(distWebXML));
 	        webXML.write(outputWriter);
 	        outputWriter.close();
 
 	        File fedoraWar = new File(installDir, Distribution.FEDORA_WAR);
 	        Zip.zip(fedoraWar, warStage.listFiles());
 	        return fedoraWar;
 
     	} catch (FileNotFoundException e) {
 			throw new InstallationFailedException(e.getMessage(), e);
     	} catch(IOException e) {
     		throw new InstallationFailedException(e.getMessage(), e);
     	}
     }
     
     private void deployLocalService(Container container, String filename) throws InstallationFailedException {
     	try {
 			File war = new File(installDir, filename);
 			if (!FileUtils.copy(_dist.get(filename), new FileOutputStream(war))) {
 				throw new InstallationFailedException("Copy to " + 
 	        			war.getAbsolutePath() + " failed.");
 			}
 			container.deploy(war);
 		} catch (IOException e) {
 			throw new InstallationFailedException(e.getMessage(), e);
 		}
     }
     
     /**
      * Command-line entry point.
      */
     public static void main(String[] args) {
 
         try {
             Distribution dist = new ClassLoaderDistribution();
             InstallOptions opts = null;
 
             if (args.length == 0) {
                 opts = new InstallOptions(dist);
             } else if (args.length == 1) {
                 Properties props = FileUtils.loadProperties(new File(args[0]));
                 opts = new InstallOptions(dist, props);
             } else {
                 System.err.println("ERROR: Too many arguments.");
                 System.err.println("Usage: java -jar fedora-install.jar [options-file]");
                 System.exit(1);
             }
 
             new Installer(dist, opts).install();
 
         } catch (Exception e) {
             printException(e);
             System.exit(1);
         }
     }
 
     /**
      * Print a message appropriate for the given exception
      * in as human-readable way as possible.
      */
     private static void printException(Exception e) {
 
         if (e instanceof InstallationCancelledException) {
             System.out.println("Installation cancelled.");
             return;
         }
 
         boolean recognized = false;
         String msg = "ERROR: ";
         if (e instanceof InstallationFailedException) {
             msg += "Installation failed: " + e.getMessage();
             recognized = true;
         } else if (e instanceof OptionValidationException) {
             OptionValidationException ove = (OptionValidationException) e;
             msg += "Bad value for '" + ove.getOptionId() + "': " + e.getMessage();
             recognized = true;
         }
         
         if (recognized) {
             System.err.println(msg);
             if (e.getCause() != null) {
                 System.err.println("Caused by: ");
                 e.getCause().printStackTrace(System.err);
             }
         } else {
             System.err.println(msg + "Unexpected error; installation aborted.");
             e.printStackTrace();
         }
     }
 }
