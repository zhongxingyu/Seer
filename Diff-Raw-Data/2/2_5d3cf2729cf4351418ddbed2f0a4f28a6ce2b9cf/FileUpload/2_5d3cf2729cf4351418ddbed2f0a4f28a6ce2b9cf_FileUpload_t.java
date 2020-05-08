 /*
  * de.jwic.controls.FileUploadControl
  */
 package de.jwic.controls;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import de.jwic.base.Control;
 import de.jwic.base.Field;
 import de.jwic.base.IControlContainer;
 import de.jwic.base.IFileReciever;
 import de.jwic.base.JavaScriptSupport;
 import de.jwic.events.FileReceivedEvent;
 import de.jwic.events.FileReceivedListener;
 import de.jwic.upload.UploadFile;
 
 /**
  * This control displays a field to upload a file. The file is saved and a resource 
  * is created when a file is recieved from the client.<br>
  * 
  * A user of the control must set the temporary flag to <b>false</b> if the file is
  * used and should not be deleted when the FileUploadControl is destroyed.
  * 
  * @author Florian Lippisch
  */
 @JavaScriptSupport
 public class FileUpload extends Control implements IFileReciever {
 
 	private static final long serialVersionUID = 1L;
 
 	protected UploadFile fileHandle = null;
 	/** List of listender to inform */
 	protected List<FileReceivedListener> selectionListeners = null;
 
	private int width = 0;
 	
 	/**
 	 * @param container
 	 */
 	public FileUpload(IControlContainer container) {
 		this(container, null);
 	}
 	/**
 	 * @param container
 	 * @param name
 	 */
 	public FileUpload(IControlContainer container, String name) {
 		super(container, name);
 		new Field(this, "file"); // create a field that stores the filename.
 	}
 	/**
 	 * Add a listener to the file received event.
 	 * @param listener
 	 */
 	public void addFileReceivedListener(FileReceivedListener listener) {
 		if (selectionListeners == null) {
 			selectionListeners = new ArrayList<FileReceivedListener>();
 		}
 		selectionListeners.add(listener);
 	}
 	/**
 	 * Send the file received event to the registerd listeners.
 	 */
 	protected void sendFileReceivedEvent() {
 		
 		if (selectionListeners != null) {
 			FileReceivedEvent e = new FileReceivedEvent(this, fileHandle);
 			for (Iterator<FileReceivedListener> it = selectionListeners.iterator(); it.hasNext(); ) {
 				FileReceivedListener listener = it.next();
 				listener.fileReceived(e);
 			}
 		}
 
 	}	
 	/**
 	 * Handle the UploadFile.
 	 * @param req
 	 * @param filename
 	 * @param file
 	 */
 	public void handleFile(String fieldname, UploadFile file) {
 		log.debug("handleFile (" + fieldname + ", " + file.getName() + ")");
 		// destroy previous uploaded file
 		reset();
 		fileHandle = file;
 		sendFileReceivedEvent();
 	}
 	/**
 	 * Returns the name of the file that has been uploaded to this control. Returns
 	 * <code>null</code> if no file has been uploaded yet.
 	 * @return
 	 */
 	public String getFileName() {
 		return fileHandle != null ? fileHandle.getName() : null;
 	}
 	/**
 	 * Returns an InputStream of the uploaded file.
 	 * <code>null</code> if no file has been uploaded yet.
 	 * @return
 	 * @throws IOException
 	 */
 	public InputStream getInputStream() throws IOException {
 		return fileHandle != null ? fileHandle.getInputStream() : null;		
 	}
 	/* (non-Javadoc)
 	 * @see de.jwic.base.Control#actionPerformed(java.lang.String, java.lang.String)
 	 */
 	public void actionPerformed(String actionId, String parameter) {
 
 		if (actionId.equals("discard") && fileHandle != null) {
 			reset();
 		}
 	}
 	/**
 	 * Resets this FileUploadControl.
 	 */
 	public void reset() {
 		if (fileHandle != null) {
 			fileHandle.destroy();
 			fileHandle = null;
 			requireRedraw();
 		}
 	}
 	/* (non-Javadoc)
 	 * @see de.jwic.base.IControl#destroy()
 	 */
 	public void destroy() {
 		reset();
 		super.destroy();
 	}
 	/**
 	 * @return Returns the width.
 	 */
 	public int getWidth() {
 		return width;
 	}
 	/**
 	 * The width of the text field next to the upload button. The width of the upload button
 	 * is defined in the styles, but has usually a width of 124px.
 	 *  
 	 * @param width The width to set.
 	 */
 	public void setWidth(int width) {
 		if (width != this.width) {
 			this.width = width;
 			requireRedraw();
 		}
 	}
 	
 	/**
 	 * Returns the size of the uploaded file or -1 if no file has been uploaded yet.
 	 * @return
 	 */
 	public long getFileSize() {
 		return fileHandle != null ? fileHandle.length() : -1;
 	}
 	
 	/**
 	 * Returns true if a file has been assigned to this control.
 	 * @return
 	 */
 	public boolean isFileUploaded() {
 		return fileHandle != null;
 	}
 }
