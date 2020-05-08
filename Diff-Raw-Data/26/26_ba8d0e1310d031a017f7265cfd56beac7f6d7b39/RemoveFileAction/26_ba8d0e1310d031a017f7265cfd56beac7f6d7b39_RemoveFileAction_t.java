 package ggit.status.unknown;
 
 import java.io.File;
 
 import org.eclipse.jface.dialogs.MessageDialog;
 
 import ggit.Config;
 import ggit.status.FileAction;
 
 public class RemoveFileAction extends FileAction {
 
 	public RemoveFileAction(String filename) {
 		super(filename, "Remove file");
 	}
 
 	@Override
 	public void run() {
 		if (MessageDialog.openConfirm(null, "Warning",
 				"Changes in file will be lost")) {
			File file = new File(Config.getWorkDir(), getFileName());
			deleteFile(file);
		}
	}

	private void deleteFile(File file) {
		if (file.isDirectory()) {
			File[] listFiles = file.listFiles();
			for (File file2 : listFiles) {
				deleteFile(file2);
			}
		}else
		{
			file.delete();
 		}
 	}
 
 }
