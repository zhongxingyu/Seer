 /**
  * 
  */
 package coed.collab.server;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.Vector;
import java.util.Map.Entry;
 
 import coed.base.data.TextModification;
 import coed.base.data.TextPortion;
 
 /**
  * Class for representing a file on a server. It contains the locks, listeners,
  * and a ChangeQueue with all recent changes on this file. Also contains the file itself,
  * the current live version.
  * 
  * @author Neobi
  *
  */
 public class ServerFile {
 
 	private String path;
 	private Integer Id;
 	/**
 	 * StringBuffer containing the contents of the file.
 	 */
 	private StringBuffer contents = new StringBuffer();
 	
 	/**
 	 * HashMap containing the session registered for this serverfile
 	 * and the corresponding pointers (index into ChangeQueue) for
 	 * the first change that was not sent to this session. (pointer value equal with
 	 * the top attribute of changeQueue means an up-to-date file).
 	 */
 	
 	private HashMap<Session,Integer> sessions; 
 	private HashMap<Session, FileChangedListener> listeners;
 	
 	private ChangeQueue queue;
 	
 	/**
 	 * Vector containing all the locks on this file
 	 */
 	private Vector<ServerLock> locks;
 	
 	public ServerFile(String path, Integer Id){
 		this.path = path;
 		this.Id = Id;
 		this.queue = new ChangeQueue(this);
 		sessions = new HashMap<Session,Integer>();
 		locks = new Vector<ServerLock>();
 		listeners = new HashMap<Session,FileChangedListener>();
 	}
 
 	public String getPath() {
 		return path;
 	}
 
 	public void setPath(String path) {
 		this.path = path;
 	}
 	
 	public synchronized void changeContents(String contents) throws IOException{
 	   this.contents.delete(0, this.contents.capacity());
 	   this.contents.append(contents);
 	   
 	   //notify listeners about the changes occured
 	   Collection<FileChangedListener> listen = listeners.values();
 	   Iterator<FileChangedListener> it = listen.iterator();
 	   while (it.hasNext()){
 		   it.next().update(true);
 	   }
 	}
 	
 	public Integer getId() {
 		return Id;
 	}
 
 	public void setId(Integer id) {
 		Id = id;
 	}
 
 	/**
 	 * Adds a new session to the sessions registered for this ServerFile. 
 	 * Also, sets the change pointer pointer to topIndex. (i.e. the session has the 
 	 * up-to-date version).
 	 * @param s Session
 	 */
 	public void addSession(Session s){
 		if (! sessions.containsKey(s))
 			sessions.put(s,new Integer(queue.getTopIndex()));
 	}
 	
 	public void removeSession(Session s){
 		if (sessions.containsKey(s))
 			sessions.remove(s);
 	}
 	
 	public int getNrOfSessions(){
 		return sessions.size();
 	}
 	
 	public void addChangeListener(Session s, FileChangedListener list){
 		listeners.put(s, list);
 	}
 	
 	public void removeChangeListener(Session s){
 		listeners.remove(s);
 	}
 	
 	/**
 	 * sets the change pointer for a session.
 	 * if the value specified is greater than the top
 	 * of the changequeue, it will be set to that value of
 	 * the top.
 	 * @param s
 	 * @param p
 	 */
 	public void setChangePointer(Session s, int p){
 		if (sessions.containsKey(s))
 			sessions.put(s,new Integer(p>queue.getTopIndex()?queue.getTopIndex():p));
 	}
 	
 	/**
 	 * Returns the changepointer of a specified session.
 	 * if the session is not found, -1 is returned
 	 * @param s
 	 * @return
 	 */
 	public int getChangePointer(Session s){
 		assert sessions.containsKey(s);
 		return ((Integer)sessions.get(s)).intValue();
 	}
 	
 	/**
 	 * Add a change to the serverfile. updates the offsets in the changequeue,
 	 * calculates global offset, and makes change in the stringbuffer.
 	 * If session is not registered to this serverfile, the method invocation has no effect
 	 * @param change
 	 * @param s
 	 */
 	public void addChange(TextModification change, Session s){
 		//update the change offset, and the offsets of all other changes
 		//that will be affected by this change
 		
 		if (sessions.containsKey(s)){
 			//just registered sessions can add changes
 			updateChangeOffset(change,s);
 			//put the change into the ChangeQueue
 			queue.enQueueChange(new CoedFileChange(change,new Date()));
 			//insert the change into the file
 			contents.insert(change.getOffset(),change.getText());
 		
 			//notify listeners
 			Collection<FileChangedListener> listen = listeners.values();
 			Iterator<FileChangedListener> it = listen.iterator();
 			System.out.println("number of listeners: "+listen.size());
 			while (it.hasNext()){
 				FileChangedListener list = it.next();
 				if ( list.getSession() != s)
 					list.update(true);
 			}
 		}
 	}
 	
 	public String[] getActiveUsers(){
 		String[] result = new String[sessions.size()];
 		Set<Session> s = sessions.keySet();
 		Iterator<Session> it = s.iterator();
 		
 		int k = 0;
 		while (it.hasNext()){
 			result[k] = it.next().getUserName();
 			k++;
 		}
 		
 		return result;
 	}
 	
 	public String getCurrentContents(){
 		return this.contents.substring(0);
 	}
 	
 	/**
 	 * Request for lock. If the session is not registered, false is returned.
 	 * else, we compute global offset, and try to put lock. if possible, update
 	 * other locks' offset, and return true, else return false
 	 * @param text
 	 * @param s
 	 * @return
 	 */
 	public boolean RequestLock(TextPortion text, Session s){
 		if (sessions.containsKey(s)){ //if session is registered
 		int startIndex = getChangePointer(s);
 		TextModification chg;
 		
 		//calculate global offset
 		for (int i=startIndex; i<queue.getTopIndex(); i++){
 			chg = queue.getChangeAt(i);
 			if (chg.getOffset() < text.getOffset()){
 				if ( ! (s.getUserName().equals(chg.getMetaInfo()) ))
 					text.setOffset(text.getOffset()+chg.getLength());
 			}
 		}
 		
 		// now we have the correct offset in the TextModification, see if we can place lock
 		boolean canLock = true;
 		ServerLock myLock = new ServerLock(text,new Date(),s);
 		ServerLock lock;
 		int i = 0;
 		while ((i<locks.size()) && (canLock)){
 			lock = locks.get(i);
 			if (lock.overlaps(myLock)){
 				//overlapping locks
 				if ( ! lock.getSession().equals(s))
 					//and not from the same user
 					canLock = false;
 			}
 			i++;
 		}
 		
 		if (! canLock)
 			return false;
 		else{
 			//we can put the lock
 			locks.add(myLock);
 			return true;
 		}
 		}
 		else return false;
 	}
 	
 	public void ReleaseLock(TextPortion portion, Session s){
 		int startIndex = getChangePointer(s);
 		TextModification chg;
 		
 		//calculate global offset
 		for (int i=startIndex; i<queue.getTopIndex(); i++){
 			chg = queue.getChangeAt(i);
 			if (chg.getOffset() < portion.getOffset()){
 				if ( ! (s.getUserName().equals(chg.getMetaInfo()) ))
 					portion.setOffset(portion.getOffset()+chg.getLength());
 			}
 		}
 		
 		//search for this lock, and delete id
 		boolean found = false;
 		int i = 0;
 		ServerLock lock;
 		while ((i<locks.size()) && (! found)){
 			lock = locks.get(i);
 			if ((lock.getSession().equals(s)) && (lock.getOffset() == portion.getOffset())
 					&& (lock.getLength() == portion.getLength())){
 				locks.remove(i);
 				found = true;
 			}
 			i++;
 		}
 		
 	}
 	
 	
 	/**
 	 * This private method will be used to convert the offset 
 	 * of the textmodification given as parameter to the corresponding
 	 * global offset, with respect to the version on the server.
 	 */
 	private void updateChangeOffset(TextModification text, Session s){
 		
 		int startIndex = getChangePointer(s);
 		TextModification chg;
 		/**
 		 * for all the changes that were not received by this session
 		 * verify if it modifies its offset (i.e. some other change was written
 		 * to an offset less then this change. In this case, we should adjust 
 		 * our offset)
 		 */
 		for (int i=startIndex; i<queue.getTopIndex(); i++){
 			chg = queue.getChangeAt(i);
 			if (chg.getOffset() < text.getOffset()){
 				if ( ! (text.getMetaInfo().equals(chg.getMetaInfo()) ))
 					text.setOffset(text.getOffset()+chg.getLength());
 			}
 		}
 		/**
 		 * Now the offset is relative to the version on the server. 
 		 * We should update now all offsets of those modifications 
 		 * in the ChangeQueue, that were issued on an offset after this
 		 * current offset. (i.e. all the modifications that are after 
 		 * the current modification with respect to the offsets should be 
 		 * shifted with the length of this current modification)
 		 */
 		//System.out.println("updated offset is:"+text.getOffset().toString());
 		for (int i=0; i<queue.getTopIndex(); i++){
 			chg = queue.getChangeAt(i);
 			//System.out.println("chg is here "+chg.getOffset().toString()+chg.getText());
 			if (chg.getOffset() >= text.getOffset()){
 				//probably the same if with the users?
 					chg.setOffset(chg.getOffset() + text.getLength());
 				
 				//System.out.println("chg is hererrrrr "+chg.getOffset().toString());
 			}
 			
 		}
 		
 		/**
 		 * now we should update in the same manner those locks' offsets
 		 * that are after this textmodification.
 		 */
 		ServerLock lock;
 		for ( int i=0; i<locks.size(); i++){
 			lock = locks.get(i);
 			if (lock.getOffset() > text.getOffset())
 				lock.setOffset(lock.getOffset()+text.getLength());
 			//! note that we do not verify here if the same user issued the lock
 			//because even in this case, the offsets should be shifted on the SERVER!!!
 			System.out.println("lock at:"+lock.getOffset()+" wiht length: "+lock.getLength());
 		}
 		
 		
 		//for (int i=0; i<queue.getTopIndex(); i++)
 		//	System.out.println("queue["+i+"] = "+queue.getChangeAt(i).getOffset().toString());
 	}
 	
 	public TextModification[] getChangesFor(Session s){
 		TextModification[] result = queue.getChangesFor(s);
 		setChangePointer(s,queue.getTopIndex());
 		if (listeners.get(s) != null )
 			listeners.get(s).setStatus(false);
 		return result;	
 	}
 	
 	public List<Session> getAllSessions(){
 		ArrayList<Session> result = new ArrayList<Session>();
 		Set sess = sessions.entrySet();
		Iterator<Entry> it = sess.iterator();
 		while (it.hasNext()){
			result.add((Session)(it.next().getKey()));
 		}
 		return result;
 	}
 	
 }
