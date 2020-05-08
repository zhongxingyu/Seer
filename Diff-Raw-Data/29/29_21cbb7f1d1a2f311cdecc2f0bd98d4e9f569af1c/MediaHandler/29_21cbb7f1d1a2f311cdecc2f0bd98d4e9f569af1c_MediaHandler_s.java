 package de.netprojectev.MediaHandler;
 
 import java.util.LinkedList;
 
 import de.netprojectev.GUI.FileManagerModel;
 import de.netprojectev.GUI.ManagerFrame;
 import de.netprojectev.Media.MediaFile;
 import de.netprojectev.Misc.Constants;
 
 public class MediaHandler {
 
 	private static MediaHandler instance = null;
 
 	private DisplayHandler displayHandler;
 	
 	private LinkedList<MediaFile> mediaFiles;
 	
 	private ManagerFrame managerFrame;
 
 	
 	/**
 	 * MediaHandler ist im Singleton Pattern geschrieben.
 	 * Zuständig für das Handling aller geladenen Dateien.
 	 * Synchronisiert sich mit der GUI (dem Kontrollfenster)
 	 */
 
 	private MediaHandler() {
 
 		mediaFiles = new LinkedList<MediaFile>();
 		displayHandler = DisplayHandler.getInstance();
 		displayHandler.setMediaHandler(this);
 		
 		
 	}
 
 	public static MediaHandler getInstance() {
 
 		if (instance == null) {
 			instance = new MediaHandler();
 		}
 		return instance;
 	}
 
 	public static void reset() {
 		instance = new MediaHandler();
 	}
 
 	/**
 	 * Fügt die übergebenen MediaFiles in die Dateiliste ein und übergibt dem DisplayHandler den Auftrag sich entsprechend zu synchronisieren.
 	 * @param files
 	 */
 	public void add(MediaFile[] files) {
 
 		for (int i = 0; i < files.length; i++) {
 
 			if (!mediaFiles.contains(files[i])) {
 				mediaFiles.add(files[i]);
 			}
 
 		}
 		
 		refreshDataModel();
 		displayHandler.add(files);
 
 	}
 
 	
 	/**
 	 * Entfernt die übergebenen MediaFiles aus der Dateiliste und übergibt dem DisplayHandler den Auftrag sich entsprechend zu synchronisieren.
 	 * @param files
 	 */
 	public void remove(MediaFile[] files) {
 		
 		//TODO prevent curFile from removing
 		//oder ne andere loesung fuer das problem einfallen lassen dass wenn das aktuelle file entfernt wird next und prev nicht funktionieren
 		
 		for (int i = 0; i < files.length; i++) {
 			mediaFiles.remove(files[i]);
 		}
 		displayHandler.remove(files);
 		
 		refreshDataModel();
 	}
 	
 	/**
 	 *  Die übergebenen Dateien werden zum kleinsten Index der übergebenen Dateien gestaucht und der Index jeder Datei um eins erhöht
 	 *  Der DisplayHandler wird angewiesen sich gegebenenfalls zu synchronisieren
 	 * @param files
 	 */
 	public void down(MediaFile[] files) {
 
 		int firstIndex = mediaFiles.indexOf(files[0]) + 1;
 		if(firstIndex > mediaFiles.size() - 1) {
 			firstIndex = mediaFiles.size() - 1;
 		}
 		
 		remove(files);
 		
 		for (int i = files.length -1; i >= 0 ; i--) {
 			mediaFiles.add(firstIndex, files[i]);
 
 		}
 		
 		refreshDataModel();
 		displayHandler.down(files);
 		
 	}
 	
 	/**
 	 *	Die übergebenen Dateien werden zum kleinsten Index der übergebenen Dateien gestaucht und der Index jeder Datei um eins gesenkt
 	 *  Der DisplayHandler wird angewiesen sich gegebenenfalls zu synchronisieren
 	 *  @param files
 	 */
 	public void up(MediaFile[] files) {
 		
 		int firstIndex = mediaFiles.indexOf(files[0]) - 1;
 		remove(files);
 		
 		if(firstIndex < 0) {
 			firstIndex = 0;
 		}
 		
 		for (int i = files.length -1; i >= 0 ; i--) {
 
 			mediaFiles.add(firstIndex, files[i]);
 
 		}
 		
 		refreshDataModel();
 		displayHandler.up(files);
 		
 	}
 	
 	/**
 	 * Ruft unter den noetigen Bedingungen einen refresh auf dem Model der JTable auf.
 	 * Das Model bearbeitet diesen Aufruf intern entsprechend.
 	 */
 	public void refreshDataModel() {
 		
		if(!Constants.UNIT_TESTING) {
		
			if(managerFrame != null) {
				((FileManagerModel) managerFrame.getjTableFileManager().getModel()).updateModel();
			}
			
 		}
 	}
 
 
 	public LinkedList<MediaFile> getMediaFiles() {
 		return this.mediaFiles;
 	}
 	public void setMediaFiles(LinkedList<MediaFile> mediaFiles) {
 		this.mediaFiles = mediaFiles;
 	}
 
 	public ManagerFrame getManagerFrame() {
 		return managerFrame;
 	}
 
 	public void setManagerFrame(ManagerFrame managerFrame) {
 		this.managerFrame = managerFrame;
 	}
 
 	public DisplayHandler getDisplayHandler() {
 		return displayHandler;
 	}
 
 	public void setDisplayHandler(DisplayHandler displayHandler) {
 		this.displayHandler = displayHandler;
 	}
 	
 
 }
