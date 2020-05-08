 package no.f12.agiledeploy.deployer;
 
 import java.io.File;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 @Component
 public class ConfigurationServiceImpl implements ConfigurationService {
 
 	private static final Logger LOG = Logger.getLogger(ConfigurationServiceImpl.class);
 
 	@Autowired(required = true)
 	private FileSystemAdapter fileSystemAdapter;
 
 	@Override
 	public void configure(File environmentDirectory, String environment) {
 		File propDir = getPropertiesDirectory(environmentDirectory);
 		File environmentPropDir = getEnvironmentPropertiesDirectory(environment, environmentDirectory);
 
 		LOG.info("Updating configuration");
 		installProperties(environmentDirectory, environmentPropDir);
 		installProperties(environmentDirectory, propDir);
 
 		LOG.info("Creating links");
 		createDataLinks(environmentDirectory);
 		createPropertyLinks(environmentDirectory);
 	}
 
 	private void createPropertyLinks(File environmentDirectory) {
 		File installationDirectory = getLatestVersionInstallationDirectory(environmentDirectory);
 		File[] files = environmentDirectory.listFiles();
 		for (File file : files) {
 			if (!file.isDirectory()) {
 				try {
 					linkInto(file, installationDirectory);
 				} catch (IllegalStateException e) {
 					this.fileSystemAdapter.copyFile(file, new File(installationDirectory, file.getName()));
 					LOG.info("Unable to link. Copied " + file);
 				}
 			}
 		}
 	}
 
 	private void linkInto(File realFile, File installationDirectory) {
 		File link = new File(installationDirectory, realFile.getName());
		if (link.exists()) {
			boolean deleted = link.delete();
			if (!deleted) {
				LOG.warn("Tried to create symlink, but it already existed and could not be deleted: " + link);
				return;
			}
 		}
		this.fileSystemAdapter.createSymbolicLink(realFile, link);
		LOG.info("Created link for " + realFile + " at " + link);
 	}
 
 	private File getDataDirectory(File environmentDirectory) {
 		return new File(environmentDirectory, "data");
 	}
 
 	private File getEnvironmentPropertiesDirectory(String environment, File environmentDirectory) {
 		return new File(getPropertiesDirectory(environmentDirectory), environment);
 	}
 
 	private File getPropertiesDirectory(File environmentDirectory) {
 		return new File(getLatestVersionInstallationDirectory(environmentDirectory), "properties");
 	}
 
 	private File getLatestVersionInstallationDirectory(File environmentDirectory) {
 		return new File(environmentDirectory, "current");
 	}
 
 	private void createDataLinks(File environmentDirectory) {
 		File dataDir = getDataDirectory(environmentDirectory);
 		File installationDirectory = getLatestVersionInstallationDirectory(environmentDirectory);
 		if (!dataDir.exists()) {
 			dataDir.mkdirs();
 		}
 
 		
 		try {
 			linkInto(dataDir, installationDirectory);
 		} catch (IllegalStateException e) {
 			LOG.warn("Could not create sym link to data directory at " + dataDir
 					+ ". Please make sure your application does not write to a data dir under your root.", e);
 		}
 	}
 
 	private void installProperties(File environmentDirectory, File propDir) {
 		if (propDir.exists()) {
 			File[] files = propDir.listFiles();
 			for (File source : files) {
 				File target = new File(environmentDirectory, source.getName());
 				if (!target.exists() && source.isFile()) {
 					fileSystemAdapter.copyFile(source, target);
 					LOG.info("Installed configuration file: " + source);
 				} else {
 					LOG.debug("File already exists, skipping " + source);
 				}
 			}
 
 		}
 	}
 
 	public void setFileSystemAdapter(FileSystemAdapter fsAdapter) {
 		this.fileSystemAdapter = fsAdapter;
 	}
 
 }
