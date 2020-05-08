 package cz.zcu.kiv.kc.plugin.createDir;
 
 import java.io.File;
 import java.util.List;
 
 import cz.zcu.kiv.kc.plugin.AbstractPlugin;
 
 public class CreateDirPlugin extends AbstractPlugin{
 
 	@Override
 	public void executeAction(List<File> selectedFiles, String destinationPath,
 			String sourcePath) {
 		String name = askForName("New directory name:");
 		new File(sourcePath + File.separator + name).mkdir();
 		sendEvent(sourcePath);		
 	}
 
 	@Override
 	public String getName() {
 		return "New directory";
 	}
 
 }
