 package org.esgi.java.grabbergui.models;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.SortedSet;
 
 import org.esgi.java.grabber.Grabber;
 import org.esgi.java.grabber.GrabberListener;
 
 /**
  * 
  * @author gwadaboug
  * @author Julien Massot
  */
 public class ProjectGrabber implements GrabberListener{
 	//---------------------------------------------------------------------------------------------
 	// Static constants
 	//---------------------------------------------------------------------------------------------
 	public enum State{
 		PROJECT_PAUSE,
 		PROJECT_RUN,
 		PROJECT_END,
 		PROJECT_STOP,
 		PROJECT_UNKNOWN
 	}
 	//---------------------------------------------------------------------------------------------
 	
 	public enum LimitMethod{
 		FILENUMBER,
 		SIZEDOWLOADED,
 		UNLIMITED
 	}
 	//-------------------------------pool.pause()--------------------------------------------------------------
 	// Private variables
 	//---------------------------------------------------------------------------------------------
 	
 	private String name;
 	private String url;
 	private State status;
 	private String downloaddir;
 	private LimitMethod limitmethod;
 	private int limit;
 
 	List<ProjectListener> listeners = new ArrayList<ProjectListener>();
 	private ArrayList<FileGrabber> activefiles;
 	private Grabber grabber;
 	//---------------------------------------------------------------------------------------------
 	// Constructors
 	//---------------------------------------------------------------------------------------------
 	/**
 	 * 
 	 */
 	public ProjectGrabber() {
 		this._initProjectGrabber("", "", State.PROJECT_PAUSE, "", LimitMethod.FILENUMBER, 500);
 	}
 	
 	/**
 	 * 
 	 * @param name
 	 * @param url
 	 */
 	public ProjectGrabber(String name, String url) {
 		this._initProjectGrabber(name, url, State.PROJECT_PAUSE, "", LimitMethod.FILENUMBER, 500);
 	}
 	
 	/**
 	 * 
 	 * @param name
 	 * @param url
 	 * @param status
 	 */
 	public ProjectGrabber(String name, String url, State status, String projectdir, 
 			LimitMethod limitmethod, int limit) {
 		this._initProjectGrabber(name, url, status, projectdir, limitmethod, limit);
 	}
 
 	//---------------------------------------------------------------------------------------------
 	// Getter and Setter
 	//---------------------------------------------------------------------------------------------
 	/**
 	 * 
 	 * @return return the name of the project
 	 */
 	public String getName() {
 		return name;
 	}
 	
 	/**
 	 * 
 	 * @return return the download directory
 	 */
 	public String getDownloaddir() {
 		return downloaddir;
 	}
 	
 	/**
 	 * 
 	 * @param name
 	 */
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	/**
 	 * 
 	 * @return return the base url to grab
 	 */
 	public String getUrl() {
 		return url;
 	}
 
 	/**
 	 * 
 	 * @return a list of files not downloaded for this project
  	 */
 	
 	public int getdowloadedfiles() {
 		return this.grabber.getFiles();
 	}
 	
 	/**
 	 * 
 	 * @return the list of files which are downloading
 	 */
 	public ArrayList<FileGrabber> getActiveFiles() {
 		return activefiles;
 	}
 	
 	/**
 	 * 
 	 * @return a list of files not downloaded for this project
  	 */
 	public List<String> getTodownload() {
 		return grabber.getTodownload();
 	}
 	
 	/**
 	 * 
 	 * @return a list of files ever downloaded for this project
  	 */
 	public SortedSet<String> getHistory() {
 		return grabber.getHistory();
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public State getStatus() {
 		return status;
 	}
 	
 	public String getStatusStr() {
 		return state2string(status);
 	}
 
 	/**
 	 * 
 	 * @param status
 	 */
 	public void setStatus(State status) {
 		if (status == State.PROJECT_PAUSE)
 			this.grabber.pause();			
 		else if (status == State.PROJECT_RUN)
 			this.grabber.start();
 		else if (status == State.PROJECT_STOP) {
 			this.grabber.pause();
 		}
 		this.status = status;
 	}
 	
 	public String state2string(State status) {
 		switch (status) {
 		case PROJECT_PAUSE:
 			return "paused";
 		case PROJECT_RUN:
 			return "downloading";
 		case PROJECT_END:
 			return "done";
 		case PROJECT_STOP:
 			return "stopped";
 		}
 		return null;
 	}
 	
	public String getProgress() {
 		if (this.limitmethod == LimitMethod.FILENUMBER)
			return ((getdowloadedfiles() * 100) / limit) + "%";
 		else if (this.limitmethod == LimitMethod.SIZEDOWLOADED)
			return (int) ((getDowloadedsize() *100)/(limit*1024*1024)) + "%";
		return "unlimitted";
 	}
 	
 	public void pause() {
 		setStatus(State.PROJECT_PAUSE);
 	}
 	
 	public void stop() {
 		setStatus(State.PROJECT_STOP);
 	}
 	
 	public void start() {
 		setStatus(State.PROJECT_RUN);
 	}
 	
 	public State string2state(String status) {
 		if (status == "paused") 
 			return State.PROJECT_PAUSE;
 		else if (status == "downloading") 
 			return State.PROJECT_RUN;
 		else if (status == "done")
 			return State.PROJECT_END;
 		else if (status == "stopped")
 			return State.PROJECT_STOP;
 		return State.PROJECT_UNKNOWN;
 	}
 	
 	public void addActiveFile(String url, int size) {
 		FileGrabber fgrabber = new FileGrabber(this, url, size);
 		fgrabber.setStatus(fgrabber.IN_DOWNLOADED);
 		activefiles.add(fgrabber);
 		for (ProjectListener listener : listeners)
 			listener.newActiveFile(fgrabber);
 	}
 	
 	public void remActiveFile(String url) {
 		for ( FileGrabber fgrabber : activefiles)
 			if (fgrabber.getUrl() == url) {
 				for (ProjectListener listener : listeners)
 					listener.remActiveFile(fgrabber);
 				activefiles.remove(fgrabber);
 				return;
 			}
 	}
 	
 	/**
 	 * 
 	 * @return return the size in octet downloaded for this project
 	 */
 	public long getDowloadedsize() {
 		return this.grabber.getDownloaded();
 	}
 	
 	//---------------------------------------------------------------------------------------------
 	// Private methods
 	//---------------------------------------------------------------------------------------------
 	/**
 	 *  
 	 * 
 	 */
 	private void _initProjectGrabber(String name, String url, State state, 
 			String downloadldir, LimitMethod limitmethod, int limit) {
 		this.name   = name;
 		this.url    = url;
 		this.status = state;
 		this.downloaddir = downloadldir;
 		this.activefiles = new ArrayList<FileGrabber>();
 		this.grabber = new Grabber(name, this.downloaddir + "/" + this.name, limitmethod, limit);
 		this.grabber.register(this);
 		this.grabber.addtodownload(url);
 		this.limit = limit;
 		this.limitmethod = limitmethod;
 		if ( this.status == State.PROJECT_RUN )
 			this.grabber.start();
 	}
 
 	@Override
 	public void incDownloaded(String URL, int length) {
 		for ( FileGrabber file : activefiles)
 			if (file.getUrl() == url) {
 				file.incDownloaded(length);
 				return;
 			} 
 	}
 
 	@Override
 	public void grabberEnd(Grabber grabber) {
 		// TODO Auto-generated method stub
 		System.out.println("ok end");
 		this.status = State.PROJECT_END;
 	}
 
 	public void register(ProjectListener listener) {
 		listeners.add(listener);
 	}
 
 	public void unregister(ProjectListener listener) {
 		listeners.remove(listener);
 	}
 }
