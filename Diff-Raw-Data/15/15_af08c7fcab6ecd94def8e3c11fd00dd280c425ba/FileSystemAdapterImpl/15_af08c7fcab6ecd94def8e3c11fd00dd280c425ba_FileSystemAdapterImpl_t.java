 package no.f12.agiledeploy.deployer;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.springframework.stereotype.Component;
 
 @Component
 public class FileSystemAdapterImpl implements FileSystemAdapter {
 
 	public static final String DEFAULT_SYMLINKCOMMAND = "ln -s %1$s %2$s";
 	private String symLinkCommand = DEFAULT_SYMLINKCOMMAND;
 
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
			Runtime.getRuntime().exec(command);
 		} catch (IOException e) {
 			throw new IllegalStateException("Could not create symlink", e);
 		}
 	}
 
 	public void setSymLinkCommand(String command) {
 		this.symLinkCommand = command;
 	}
 
 }
