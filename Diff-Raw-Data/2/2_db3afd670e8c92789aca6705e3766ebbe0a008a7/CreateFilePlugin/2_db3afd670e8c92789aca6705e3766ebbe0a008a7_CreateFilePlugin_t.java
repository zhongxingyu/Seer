 package cz.zcu.kiv.kc.plugin.create;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
 public class CreateFilePlugin implements ICreateFilePlugin  {
 
 	@Override
 	public void executeAction(List<File> selectedFiles, String destinationPath,
 			String sourcePath) {
 		try {
			new File(destinationPath + File.separator + System.nanoTime()).createNewFile();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public String getId() {
 		return "create";
 	}
 
 	@Override
 	public String getName() {
 		return "Create";
 	}
 
 }
