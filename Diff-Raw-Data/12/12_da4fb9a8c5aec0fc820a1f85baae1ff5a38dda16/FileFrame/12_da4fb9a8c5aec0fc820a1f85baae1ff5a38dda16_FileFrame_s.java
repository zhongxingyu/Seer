 package gui;
 
 import java.io.File;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Shell;
 
 public class FileFrame extends Frame implements MiscFrame {
 	private Shell shell;
 	private File chosenFile;
 
 	public FileFrame(Shell parentShell, int def) {
 		shell = new Shell(parentShell);
 		chosenFile = null;
 		String fileName = null;
 
 		FileDialog dlg = new FileDialog(shell, def);
 		dlg.setOverwrite(true);
 		fileName = dlg.open();
 		if (fileName != null) {
 			File file = new File(fileName);
			if((def == SWT.OPEN && file.canRead()) || (def == SWT.SAVE && file.canWrite())) {
 				chosenFile = file;
 			}
 		}
 	}
	
 	public File getChosenFile() {
 		return chosenFile;
 	}
 
 	@Override
 	public Button getSaveButton() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Button getCloseButton() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void close() {
 		this.shell.close();
 	}
 }
