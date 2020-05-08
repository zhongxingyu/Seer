 package presentation.model;
 
 import java.awt.datatransfer.Clipboard;
 import java.awt.datatransfer.ClipboardOwner;
 import java.awt.datatransfer.Transferable;
 import java.util.Observable;
 
 /**
  * Manages the current status of the main window with its active user, tab,
  * menubar and status panel.
  */
 public class MainWindowModel extends Observable implements ClipboardOwner {
 
 	private static final String PROGRAM_NAME = " - BücherBox";
 //	private ModelController controller;
	private String title = "Recherche - BücherBox";
 
 	public MainWindowModel(ModelController controller) {
 //		this.controller = controller;
 	}
 
 	public String getTitle() {
 		return title + " " + PROGRAM_NAME;
 	}
 
 	public void setTitle(String title) {
 		this.title = title;
 		setChanged();
 		notifyObservers();
 	}
 
 	public void lostOwnership(Clipboard clipboard, Transferable contents) {
 		// TODO Auto-generated method stub
 	}
 }
