 package cz.zcu.kiv.kc.plugin.move;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.nio.file.Files;
 import java.util.List;
 
 import javax.swing.UIManager;
 
 import cz.zcu.kiv.kc.interfaces.IMovePlugin;
 import cz.zcu.kiv.kc.plugin.AbstractPlugin;
 
 public class MoveFilePlugin extends AbstractPlugin implements IMovePlugin {
 
 	public MoveFilePlugin()
 	{
 		super();
 		UIManager.put("ClassLoader", getClass().getClassLoader());
 	}
 	
 	@Override
 	public void executeAction(List<File> selectedFiles, String destinationPath,
 			String sourcePath) {
 		for (File file : selectedFiles) {
 			move(file, destinationPath);
 			sendEvent(destinationPath);
 			sendEvent(sourcePath);
 		}
 	}
 
 	private void moveFile(File file, String destinationPath) {
 		try {
 			FileOutputStream fileOutputStream = new FileOutputStream(
 					destinationPath + File.separator + file.getName());
 			Files.copy(file.toPath(), fileOutputStream);
 			fileOutputStream.close();
 			file.delete();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
 	private void moveDir(File file, String destinationPath) {
 		String destDir = destinationPath + File.separator + file.getName();
 		new File(destDir).mkdir();
 		if (file.listFiles().length > 0) {
 			for (File childFile : file.listFiles()) {
 				move(childFile, destDir);
 			}
 		}
 		deleteFile(file);
 	}
 	private void deleteFile(File file) {
 		if (file.isDirectory() && file.listFiles().length > 0) {
 			for (File childFile : file.listFiles()) {
 				deleteFile(childFile);
 			}
 		}
 		file.delete();
 		sendEvent(file.getParent());
 	}
 
 	private void move(File file, String destinationPath) {
 		if (file.isDirectory()) {
 			moveDir(file, destinationPath);
 		} else {
 			moveFile(file, destinationPath);
 		}
 	}
 
 	@Override
 	public String getName() {
 		return "Move";
 	}
 
 }
