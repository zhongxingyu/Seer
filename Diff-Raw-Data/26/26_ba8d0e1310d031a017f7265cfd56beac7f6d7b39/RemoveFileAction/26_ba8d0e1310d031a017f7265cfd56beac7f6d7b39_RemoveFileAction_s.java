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
			new File(Config.getWorkDir(), getFileName()).delete();
 		}
 	}
 
 }
