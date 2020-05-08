 package org.iucn.sis.server.api.locking;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.hibernate.Session;
 import org.iucn.sis.server.api.io.AssessmentIO;
 import org.iucn.sis.server.api.locking.LockRepository.LockInfo;
 import org.iucn.sis.shared.api.debug.Debug;
 import org.iucn.sis.shared.api.models.Assessment;
 import org.iucn.sis.shared.api.models.Edit;
 import org.iucn.sis.shared.api.models.User;
 import org.restlet.data.Status;
 
 public class FileLocker {
 	
 	protected static final int SAVE_LOCK_EXPIRY_MS = 3 * 60 * 1000;
 	
 	private final ConcurrentHashMap<String, SimpleLock> locks;
 	private final LockRepository assessmentLocks;
 
 	public boolean verboseOutput = true;
 	
 	/**
 	 * Should only be one instantiation of this, called from SIS API
 	 */
 	public FileLocker() {
 		//assessmentLocks = new ConcurrentHashMap<String, Lock>();
 		//assessmentLocks = new PersistentLockRepository();
 		//assessmentLocks = new HibernateLockRepository();
 		
 		this(new HibernateLockRepository());
 	}
 	
 	public FileLocker(LockRepository assessmentLocks) {
 		this.assessmentLocks = assessmentLocks;
 		this.locks = new ConcurrentHashMap<String, SimpleLock>();
 	}
 
 	public boolean acquireLock(String url, String owner, boolean autoExpire) {
 		boolean acquiredLock = false;
 
 		synchronized (locks) {
 			SimpleLock existing = locks.get(url);
 			if (existing == null) {
 				locks.put(url, new SimpleLock(url, owner, autoExpire));
 				acquiredLock = true;
 			}
 			else if (existing.owner.equals(owner)) {
 				existing.restartTimer();
 				acquiredLock = true;
 			}
 		}
 
 		return acquiredLock;
 	}
 
 	public boolean acquireWithRetry(String url, String owner, boolean autoExpire, int maxTries) {
 		boolean locked = acquireLock(url, owner, autoExpire);
 		if (!locked) {
 			int count = 0;
 			do {
 				try {
 					Thread.sleep(250);
 				} catch (Exception e) {
 				}
 				locked = acquireLock(url, owner, autoExpire);
 				count++;
 			} while (count < maxTries && !locked);
 		}
 		return locked;
 	}
 
 	/**
 	 * Checks if the assessment is locked.
 	 * 
 	 * @param id
 	 * @param type
 	 * @return true, if it's locked
 	 */
 	public boolean isAssessmentPersistentLocked(Integer id) {
 		return assessmentLocks.isAssessmentPersistentLocked(id);
 	}
 	
 	/**
 	 * Checks if the assessment is locked.
 	 * 
 	 * @param id
 	 * @param type
 	 * @return true, if it's locked
 	 */
 	public LockInfo getAssessmentPersistentLock(Integer assessmentID) throws LockException {
 		return assessmentLocks.getLockedAssessment(assessmentID);		
 	}
 
 	/**
 	 * Checks if the assessment is locked or has been changed, based on its id,
 	 * type and modDate. Returns the XML of the updated version, if needed.
 	 * Otherwise, returns null.
 	 * 
 	 * @param id
 	 * @param modDate
 	 * @param session TODO
 	 * @param type
 	 * @param username
 	 * @return either lock XML if locked,
 	 * * assessment XML if not locked and user version out of date, or null if not locked and user version is valid
 	 */
 	public String checkAssessmentAvailability(Integer id, String modDate, User user, Session session) throws LockException {
 		if (isAssessmentPersistentLocked(id)) {
 			LockRepository.LockInfo lock = assessmentLocks.getLockedAssessment(id);
 			return lock.toXML();
 		} else {
 			AssessmentIO io = new AssessmentIO(session);
 			Assessment assessment = io.getAssessment(id);
 			if (assessment != null) {
 				Edit lastEdit = assessment.getLastEdit();
 				long lastModified = lastEdit == null ? 0 : lastEdit.getCreatedDate().getTime();
 				if (lastModified > Long.parseLong(modDate)) {
 					String ret = "<update owner=\""
 							+ lastEdit.getUser().getUsername() + "\">\n";
 					String xml = assessment.toXML();
 					xml = xml.replaceAll(
 							"<\\?xml\\s*(version=.*)?\\s*(encoding=.*)?\\?>",
 							"");
 					xml = xml
 							.replaceAll(
 									"(<dateModified>.*?</dateModified>)|(<dateModified\\s*/>)",
 									"<dateModified>" + lastModified
 											+ "</dateModified>");
 					ret += xml;
 					ret += "</update>";
 
 					return ret;
 				} else {
 					return null;
 				}
 			} else {
 				return null;
 			}
 
 		}
 	}
 	
 	/**
 	 * Tries to lock all assessments.  If unable to lock all of them, then returns status returned by persistentLockAssessment.  
 	 * Otherwise returns success ok.
 	 * 
 	 * 
 	 * @param assessments
 	 * @param lockType
 	 * @param owner
 	 * @return
 	 */
 	public synchronized Status persistentLockAssessments(Collection<Assessment> assessments, LockType lockType, User owner) {
 		List<Assessment> locked = new ArrayList<Assessment>();
 		Status status = null;
 		for (Assessment assessment : assessments) {
 			Status result = persistentLockAssessment(assessment.getId(), lockType, owner);
 			if (result.isSuccess())
 				locked.add(assessment);
 			else {
 				status = result;
 				break;
 			}
 		}
 		
 		if (status != null) {
 			for (Assessment assessment : locked) {
 				try {
 					persistentEagerRelease(assessment.getId(), owner);
 				} catch (LockException e) {
 					Debug.println(e);
 				}
 			}
 		} else
 			status = Status.SUCCESS_OK;
 		
 		return status;
 	}
 
 	/**
 	 * Actually does locking. Checks to make sure lock type is valid (fail:
 	 * return Bad Request), that it's not already locked by someone else (fail:
 	 * return Forbidden), then locks it (or restarts the timer) and returns
 	 * Success_OK.
 	 * 
 	 * @param id
 	 * @param assessmentType
 	 * @param lockType
 	 * @param owner
 	 * @return a Status - see above
 	 */
 	public synchronized Status persistentLockAssessment(Integer assessmentID, LockType lockType, User owner) {
 		return persistentLockAssessment(assessmentID, lockType, owner, null);
 	}
 	
 	public synchronized Status persistentLockAssessment(Integer assessmentID, LockType lockType, User owner, String group) {
 		if (LockType.SAVE_LOCK.equals(lockType)) {
 			boolean hasLock = acquireLock("assessment/" + assessmentID, owner.getUsername(), true);
 			
 			return hasLock ? Status.SUCCESS_OK : Status.CLIENT_ERROR_FORBIDDEN; 
 		}
 		
 		if (assessmentLocks.isAssessmentPersistentLocked(assessmentID)) {
 			LockRepository.LockInfo l;
 			try {
 				l = assessmentLocks.getLockedAssessment(assessmentID);
 			} catch (LockException e) {
 				Debug.println(e);
 				return Status.SERVER_ERROR_INTERNAL;
 			}
 			if (l.getUsername().equalsIgnoreCase(owner.getUsername())) {
 				return Status.SUCCESS_OK;
 			} else {
 				if( verboseOutput )
 					Debug.println("You can't have the lock " + owner + ", it's already owned by " + l.username);
 				if (LockType.SAVE_LOCK.equals(l.getLockType()))
 					return Status.CLIENT_ERROR_FORBIDDEN;
 				else
 					return Status.CLIENT_ERROR_LOCKED;
 			}
 		} else {
 			//assessmentLocks.put(id + assessmentType, new Lock(id + assessmentType, owner, lockType));
 			LockInfo lock = null;
 			try {
 				lock = assessmentLocks.lockAssessment(assessmentID, owner, lockType, group);
 			} catch (LockException e) {
 				Debug.println(e);
 				return Status.SERVER_ERROR_INTERNAL;
 			}
 			return lock == null ? Status.SERVER_ERROR_INTERNAL : Status.SUCCESS_OK;
 		}
 	}
 	
 	
 
 	/**
 	 * Eagerly releases a lock.
 	 * 
 	 * @param id
 	 * @param assessmentType
 	 * @param owner
 	 * @return a Status - see above
 	 */
 	public synchronized Status persistentEagerRelease(Integer id, User owner) throws LockException {
 		if (assessmentLocks.isAssessmentPersistentLocked(id)) {
 			LockRepository.LockInfo l = assessmentLocks.getLockedAssessment(id);
 			if (l.getUsername().equalsIgnoreCase(owner.getUsername())) {
				assessmentLocks.removeLockByID(l.getLockID());
 				return Status.SUCCESS_OK;
 			} else {
 				return Status.CLIENT_ERROR_FORBIDDEN;
 			}
 		} else {
 			return Status.CLIENT_ERROR_NOT_FOUND;
 		}
 	}
 	
 	public void persistentClearGroup(String groupID) throws LockException {
 		assessmentLocks.clearGroup(groupID);
 	}
 
 	public void releaseLock(String url) {
 		synchronized (locks) {
 			locks.remove(url);
 		}
 	}
 	
 	public class SimpleLock {
 		
 		private class LockExpiry implements Runnable {
 
 			private SimpleLock l;
 			
 			public LockExpiry(SimpleLock lock) {
 				this.l = lock;
 			}
 
 			public void run() {
 				while (l.restart) {
 					l.restart = false;
 
 					try {
 						Thread.sleep(SAVE_LOCK_EXPIRY_MS);
 						releaseLock(l.url);
 						
 //						if( verboseOutput )
 //							System.out.println("Removing lock: " + l.toString());
 					} catch (InterruptedException e) {
 						if (!l.restart) {
 							releaseLock(l.url);
 							
 //							if( verboseOutput )
 //								System.out.println("Removing lock: " + l.toString());
 						}
 					} finally {
 						if (!l.restart)
 							releaseLock(l.url);
 					}
 				}
 			}
 		}
 
 		Date date;
 		boolean restart = true;
 		Thread expiry;
 		String url;
 		String owner;
 
 		public SimpleLock(String url, String owner) {
 			this(url, owner, false);
 		}
 		
 		public SimpleLock(String url, String owner, boolean expires) {
 			this.url = url;
 			this.owner = owner;
 			this.date = new Date();
 			if (expires) {
 				expiry = new Thread(new LockExpiry(this));
 				expiry.start();
 			}
 		}
 		
 		public void forceExpiration(LockRepository owner) {
 			if (expiry != null)
 				expiry.interrupt();
 			else
 				releaseLock(url);
 		}
 
 		public void restartTimer() {
 			if (expiry != null) {
 				restart = true;
 				expiry.interrupt();
 			}
 		}
 		
 	}
 
 }
