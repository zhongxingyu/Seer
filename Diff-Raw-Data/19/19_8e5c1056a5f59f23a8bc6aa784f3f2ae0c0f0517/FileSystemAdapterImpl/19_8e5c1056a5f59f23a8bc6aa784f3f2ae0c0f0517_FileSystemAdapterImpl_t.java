 package no.f12.agiledeploy.deployer;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.IOException;
 
import org.apache.log4j.Logger;
 import org.springframework.stereotype.Component;
 
 @Component
 public class FileSystemAdapterImpl implements FileSystemAdapter {
 
 	public static final String DEFAULT_SYMLINKCOMMAND = "ln -s %1$s %2$s";
 	private String symLinkCommand = DEFAULT_SYMLINKCOMMAND;
	
	private static final Logger LOG = Logger.getLogger(FileSystemAdapterImpl.class);
 
 	@Override
 	public void copyFile(File source, File target) {
 		FileUtil.copyFile(source, target);
 	}
 
 	@Override
 	public void deleteDir(File directory) {
 		FileUtil.deleteDir(directory);
 	}
 
 	@Override
 	public void moveOneUp(File directory) {
 		FileUtil.moveOneUp(directory);
 	}
 
 	@Override
 	public void createSymbolicLink(File source, File symLink) {
 		try {
 			String command = String.format(symLinkCommand, source.getCanonicalPath(), symLink.getCanonicalPath());
 			Process proc = Runtime.getRuntime().exec(command);
 			int returnCode = proc.waitFor();
 			if (returnCode != 0) {
 				throw new IllegalStateException("Could not create symlink, process returned " + returnCode);
 			}
			LOG.debug("Created symlink with command: " + command);
 		} catch (IOException e) {
 			throw new IllegalStateException("Could not create symlink", e);
 		} catch (InterruptedException e) {
 			throw new IllegalStateException("Could not create symlink", e);
 		}
 	}
 
 	public void setSymLinkCommand(String command) {
 		this.symLinkCommand = command;
 	}
 
 	public void deleteDir(File dir, FileFilter filter) {
 		FileUtil.deleteDir(dir, filter);
 	}
 
 }
