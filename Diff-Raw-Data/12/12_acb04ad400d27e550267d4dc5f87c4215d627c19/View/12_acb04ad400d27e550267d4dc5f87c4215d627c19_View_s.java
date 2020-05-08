 package keytool.view;
 
 import keytool.model.Model;
 
 public class View {
 	
 	private MainWindow mainWindow;
 	private FileOpenWindow fileOpenWindow;
 	
 	public View(Model model) {
 		this.mainWindow = new MainWindow(model);
 		this.mainWindow.setVisible(true);
 		
 		this.fileOpenWindow = new FileOpenWindow();
 		this.fileOpenWindow.setVisible(false);
 	}
 
 	public MainWindow getMainWindow() {
 		return this.mainWindow;
 	}
 	
 	public void showMainWindow() {
 		this.mainWindow.setVisible(true);
 	}
 
 	public void hideMainWindow() {
 		this.mainWindow.setVisible(false);
 	}
 
 	public FileOpenWindow getFileOpenWindow() {
 		return this.fileOpenWindow;
 	}
 	
 	public void showFileOpenWindow() {
 		this.fileOpenWindow.setVisible(true);
 	}
 
 	public void hideFileOpenWindow() {
 		this.fileOpenWindow.setVisible(false);
 	}
 }
