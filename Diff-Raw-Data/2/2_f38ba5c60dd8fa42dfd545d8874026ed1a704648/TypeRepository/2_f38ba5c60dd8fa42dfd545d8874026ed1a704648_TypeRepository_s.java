 /* Copyright (c) 2001 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root 
  * application directory.
  */
 package org.vfny.geoserver.config;
 
 import java.io.*;
 import java.util.*;
 import java.util.logging.Logger;
 import org.geotools.filter.Filter;
 import org.geotools.data.DataSource;
 import org.geotools.data.QueryImpl;
 import org.geotools.data.Query;
 import org.geotools.data.DataSourceException;
 import org.geotools.feature.Feature;
 import org.geotools.feature.AttributeType;
 import org.geotools.feature.FeatureCollection;
 import org.vfny.geoserver.requests.TransactionRequest;
 import org.vfny.geoserver.requests.SubTransactionRequest;
 import org.vfny.geoserver.requests.UpdateRequest;
 import org.vfny.geoserver.requests.DeleteRequest;
 import org.vfny.geoserver.responses.WfsException;
 import org.vfny.geoserver.responses.WfsTransactionException;
 
 
 /**
  * Reads all necessary feature type information to abstract away from servlets.
  * 
  * @author Rob Hranac, TOPP
  * @author Chris Holmes, TOPP
  * @version $VERSION$
  * @tasks TODO: Rethink synchronization.  Just wanted to get things with locks
  * working for this version, but obviously we need to examine synchronization
  * completely if this class is to be useful at all.
  */
 public class TypeRepository {
         
     /** The character after a namespace prefix. */ 
     private static final String PREFIX_DELIMITER = ":";
     
     /** Class logger */
     private static Logger LOG = Logger.getLogger("org.vfny.geoserver.config");
 
     /** Castor-specified type to hold all the  */
     private static TypeRepository repository = null;
 
     /** the singleton that has all the user and service configuration. */
     private static ConfigInfo config = ConfigInfo.getInstance();
 
     /** to generate the lockIds.*/
     private Random keyMaster = null;
 
     /** keeps track of the TypeInfo objects.*/
     private Map types = new HashMap();
 
     /** A map of all locked features to the internal locks that hold them.*/
     //REVISIT: Synchronization, should this whole map be synchronized?  This
     //could be inefficient.  An improvement could be to iterate through the 
     //InternalLocks held, asking them if they have the locked features.  This
     //could be optimized in the InternalLock by first checking the correct
     //typename (a typename map would have to be added), and if it matches then
     //checking the actual locked feature.
     private Map lockedFeatures = new HashMap();
 
     //This could probably be eliminated, if we had the lock operation return 
     //InternalLocks, but keeping them private seems like a good idea.
     private Map locks = new HashMap();
     
     /** Initializes the database and request handler. */ 
     private TypeRepository() {
         Date seed = new Date();
         keyMaster = new Random(seed.getTime());
     }
     
     /**
      * Initializes the database and request handler.
      * @param featureTypeName The query from the request object.
      */ 
     public static TypeRepository getInstance() {
         if(repository == null) {
             File startDir = new File(config.getTypeDir());
             repository = new TypeRepository();
 	    LOG.finer("about to read types at " + startDir);
             repository.readTypes(startDir, config);
         }
         return repository;
     }
 
 
     /**
      * Gets the internal typename (prefix plus typeName, such as topp:rail),
      * given a typename and uri.
      *
      * @param typeName the type to find the prefix for.
      * @param uri the uri of the namespace the type lives in.
      * @return the prefixed typeName, null if the type was not found.
      */
     public String getInternalTypeName(String typeName, String uri){
 	LOG.finer("looking for internal type: " + typeName + ", " + uri);
 	for(Iterator i = getAllTypeNames().iterator(); i.hasNext();){
 	    String curTypeName = i.next().toString();
 	    TypeInfo curType = getType(curTypeName); 
 	    LOG.finest("looking at type " + curType.getName() + 
 		       ", " + curType.getXmlns());
 	    if (curType.getName().equals(typeName) &&
 		curType.getXmlns().equals(uri)){
 		LOG.finer("returning internal typename: " 
 			  + curType.getPrefix() + ":" + typeName);
 		return curType.getPrefix() + ":" + typeName;
 	    }
 	}
 	return null;
     }
 
     /**
      * Returns a capabilities XML fragment for a specific feature type.
      * @param version The version of the request (0.0.14 or 0.0.15)
      */ 
     //TODO: throw wfs exception here if type is not found? this would reduce
     //duplicate code. 
     public TypeInfo getType(String typeName) {
 	int prefixDelimPos = typeName.lastIndexOf(PREFIX_DELIMITER);
 	if (prefixDelimPos < 0) {
 	    //for backwards compatibility.  Only works if all 
 	    //featureTypes have the same prefix.
 	    typeName = config.getDefaultNSPrefix() + 
 		PREFIX_DELIMITER + typeName;
 	} 
 	LOG.finest("getting type " + typeName);
         return (TypeInfo) types.get(typeName);
     }
 
     /**
      * Adds a type to the repository, reading from the path given.
      * @param pathToTypeInfo the path to an info.xml file.
      */ 
     private void addType(String pathToTypeInfo) { 
 	TypeInfo type = new TypeInfo(pathToTypeInfo);
 	if (type.getName() != null) {
 	    types.put(type.getFullName(), type);
 	} else {
 	    LOG.warning("Geoserver did not successfull read the feature" +
 			" info.xml file at " + pathToTypeInfo + ".  Please " + 
 			"make sure all elements are in info.xml");
 	}
     }
 
     /**
      * Checks that the collection of featureTypeNames all have the same prefix.
      * Used to determine if their schemas are all in the same namespace or if
      * imports need to be done.
      *
      * @param featureTypeNames list of featureTypes, generally from a 
      * DescribeFeatureType request.
      * @return true if all the typenames in the collection have the same prefix.
      */ 
     public boolean allSameType(Collection featureTypeNames) 
 	throws WfsException {
 	Iterator nameIter = featureTypeNames.iterator();
 	boolean sameType = true;
 	if (!nameIter.hasNext()) {
 	    return false;
 	}
 	String firstPrefix = getPrefix(nameIter.next().toString());
 	while (nameIter.hasNext()){
 	    if (!firstPrefix.equals
 		(getPrefix(nameIter.next().toString()))) {
 		return false;
 	    }
 	}
 	return sameType;
     }
 
     /**
      * gets the prefix for the featureType */
     private String getPrefix(String featureTypeName) throws WfsException {
     	TypeInfo typeInfo = getType(featureTypeName);
     	if (typeInfo == null) {
     	    throw new WfsException("Feature Type " + featureTypeName + " does "
     				   + "not exist on this server");
     	}
     	return typeInfo.getPrefix();
     }
 
     /**
      * Gets a list of all the typeNames held by this repository.
      *
      * @return Strings of all typenames.
      */
     public List getAllTypeNames(){
 	return new ArrayList(types.keySet());
     }
 
     /**
      * Frees the resources of all the types held by the repository.
      * @REVISIT: this does nothing now, as we no longer control connections
      * We need some connection control from datasource interface, or
      * else a connection manager.
      */
     public void closeTypeResources() {
 	for(Iterator i = getAllTypeNames().iterator(); i.hasNext();){
 	    String curType = i.next().toString(); 
 	    getType(curType).close();
 	}
     }
 
         /**
      * This function lists all files in HTML for the meta-data pages.
      * 
      * Simple recursive function to read through all subdirectories and 
      * add all XML files with the name 'info.XXX' to the repository.
      * @param currentFile The top directory from which to start 
      * reading files.
      */
     private void readTypes(File currentFile, ConfigInfo config) {
         LOG.finest("examining: " + currentFile.getAbsolutePath());
         LOG.finest("is dir: " + currentFile.isDirectory());
         if(currentFile.isDirectory()) {
             File[] file = currentFile.listFiles();
             for(int i = 0, n = file.length; i < n; i++) {
                 readTypes(file[i], config);
             } 
         } else if(isInfoFile(currentFile, config)) {
 	    String curPath = currentFile.getAbsolutePath();
             LOG.finest("adding: " + curPath);
 	    addType(curPath);
 	    
 
         }
     }
 
     /**
      * tests whether a given file is a file containing type information.
      *
      * @param testFile the file to test.
      * @param config holds information as to the info file format.
      */
     private static boolean isInfoFile(File testFile, ConfigInfo config){
         String testName = testFile.getAbsolutePath();
         int start = testName.length() - config.INFO_FILE.length();
         int end = testName.length();
         return testName.substring(start, end).equals(config.INFO_FILE);
     }
 
     /**
      * Returns a capabilities XML fragment for a specific feature type.
      */
     public int typeCount() {        
         return types.size();
     }
 
     /********************************************************************
      * Locking methods.
      ********************************************************************/
 
     public boolean isLocked(String typeName) throws WfsException {
 	return isLocked(typeName, null, null);
     }
 
     /**
      * Inidicates whether a featureType has been locked.
      * @param typeName the name of the featureType to check for a lock.
      * @param filter the filter of the features to check.  If null all are
      * checked.
      * @param lockId the id to check with.  Each feature is locked with a 
      * certain lockId, and if the lockId supplied matches the one the feature
      * is locked with then isLocked will still return false for that feature.  
      * If the lockId is null than any locked feature will return false for this
      * method.
      * @return true if a feature that matches the filter is locked and the
      * lockId does not match the id of the locked feature, false otherwise.
      */ 
     public boolean isLocked(String typeName, Filter filter, String lockId) 
 	throws WfsException {  
 	LOG.finer("checking isLocked on: " + typeName + ", " + lockId + ", " + filter);
 	List features = getFidFeatures(typeName, filter);    
 	//LOG.finer("locked features = " + lockedFeatures);
 	for (Iterator i = features.iterator(); i.hasNext();){
 	    String curFid = i.next().toString();
 	    LOG.finest("checking feature: " + curFid);
 	    if (lockedFeatures.containsKey(curFid)){
 		if (lockId == null) {
 		    return true;
 		} else {
 		    String targetLockId = 
 			((InternalLock)lockedFeatures.get(curFid)).getId();
 		    if (!targetLockId.equals(lockId)){
 			return true;
 		    }
 		    
 		}
 	    }
 	}
 	return false;//lockedFeatures.containsKey(typeName);
     }
 
     /**
      * gets a list of the feature ids for the given typename and filter.
      *
      * @param typeName the featureType to query.
      * @param filter which features of the type to return.  If null then all.
      * @return a list of fid strings.
      * @throws WfsException if there were any datasource problems, like the 
      * typeName not existing, or problems with the connection.
      */
     private List getFidFeatures(String typeName, Filter filter) 
 	throws WfsException {
 	TypeInfo typeInfo = getType(typeName);
 	if (typeInfo == null) {
 	    throw new WfsException("could not find feature type: " + typeName 
 				   + ", information not in data folder");
 	}
 	DataSource data = 
 	    getType(typeName).getDataSource();
 	Feature[] features = null;
 	try {
	    QueryImpl query = new QueryImpl(null, new AttributeType[0]); 
 	    features = data.getFeatures(query).getFeatures();
 	} catch(DataSourceException e) {
 	    throw new WfsException(e, "DataSource problem checking lock: ",
 				   "at featureType: " + typeName);
 	} 	
 	List fids = new ArrayList(features.length);
 	for (int i = 0; i < features.length; i++) {
 	    //LOG.finer("adding feature " + features[i]);
 	    fids.add(features[i].getId());
 	}
 
 	return fids;
     }
 
     /**
      * Gets rid of all lockedFeatures held by this repository.  Used as a
      * convenience method for unit tests, but should eventually be used by
      * an admin tool to clear unclosed lockedFeatures.
      */
     public void clearLocks(){
 	lockedFeatures = new HashMap();
 	locks = new HashMap();
     }
 
     /**
      * A convenience method to lock all the features in a given typeName for 
      * the default expiry length of time.
      *
      * @param typeName the name of the featureType to lock.
      * @return  the id string of the lock, if successful, null otherwise.
      */
     public synchronized String lock(String typeName) throws WfsException{
 	return lock(typeName, null, true, -1);
     }
 
     /**
      * Locks the given typeName for a length of expiry minutes.
      *
      * @param typeName the name of the featureType to lock.
      * @param filter which features to lock.  If null than all in typeName.
      * @param lockAll the lock action.  If true and all can not be locked
      * then the lock operation fails.  If false then as many features not
      * all ready locked are locked.
      * @param expiry the length in minutes to lock the featureType for.
      * @return the id string of the lock, if successful, null otherwise.
      * @throws WfsException if the lock fails.
      */ 
     public synchronized String lock(String typeName, Filter filter, 
 					  boolean lockAll, int expiry) 
 	throws WfsException {       
 	String lockId = 
 	    String.valueOf(keyMaster.nextInt(Integer.MAX_VALUE));
 	InternalLock lock = new InternalLock(lockId, expiry);
 	List features =  getFidFeatures(typeName, filter);
 	if (!lock.addFeatures(lockAll, features) && lockAll){
 	    throw new WfsException("all features were not able to be locked," +
 				   " and lockAction is ALL, so lock failed");
 	}
 	//locks.put(lockId, lock); - now done in InternalLock constructor.
 	//lockedFeatures.put(typeName, lock);
 	LOG.fine("locked " + typeName + " with: " + lock);
 	return lockId;
         
     }
 
 
     /**
      * Performs a lock operation with an already assigned lockValue.  This
      * is for cases when more than one Lock is requested in a LockFeature
      * request, as all must have the same lock.
      *
      * @param typeName the name of the Feature Type to lock.
      * @param filter which features to lock.  If null than all in typeName.
      * @param lockAll the lock action.  If true and all can not be locked
      * then the lock operation fails.  If false then as many features not
      * all ready locked are locked.
      * @param lockId the id string of the lock to be added to.
      * @return the value of the lock assigned.  Will be the same as the 
      * lockValue argument.  Null if that type was already locked.
      *  @throws WfsException if the lock fails.
      */
     public synchronized String addToLock(String typeName, Filter filter, 
 				     boolean lockAll, String lockId) 
 	throws WfsException {   
 	InternalLock lock = (InternalLock)locks.get(lockId);
 	lock.addFeatures(lockAll, getFidFeatures(typeName, filter));
 	LOG.finest("locked " + typeName + " with: " + lock.getId());
 	return lockId;
     }
 
     /**
      * convenience unlock that assumes releaseAction ALL.  This completely
      * releases the lock.
      * 
      * @param lockId the id string of the lock to release
      */
     public synchronized boolean unlock(String lockId) {
 	 InternalLock lock = (InternalLock)locks.get(lockId);
 	 if (lock != null) {
 	     return lock.unlock(null, true);
 	 } else {
 	     return false;
 	 }
 	//return unlock(typeName, lockId, true);
     }
 
     /**
      * Unlocks a completed transaction request.  This should only be called
      * after the transaction is committed.  It performs the proper release
      * action for each of the sub-requests.  This method should be called
      * by transactions, as they are the only way to release a lock, which is
      * why a WfsTransactionException is thrown here.
      * @param completed a successfully completed transaction request.
      * @return true if there were features released, false otherwise.
      * @throws WfsTransactionException if there was trouble with the backend.
      */ 
     public synchronized boolean unlock(TransactionRequest completed)
 	throws WfsTransactionException {//String typeName, String lockId, 
 				       //boolean releaseAll) {       
  	String lockId = completed.getLockId();
 	InternalLock lock = (InternalLock)locks.get(lockId);
 	if (lock == null) {
 	    return false;
 	} else {
 	    boolean releaseAll = completed.getReleaseAll();
 	    if (releaseAll) {
 		return lock.unlock(null, true);
 	    } else {
 		List unlockFeatures = new ArrayList();
 		for(int i = 0, n = completed.getSubRequestSize(); i < n; i++) {  
 		    SubTransactionRequest sub = completed.getSubRequest(i);
 		    String typeName = sub.getTypeName(); 
 		    Filter filter = null;
 		    try {
 		    if (sub.getOpType() == SubTransactionRequest.DELETE){
 			filter = ((DeleteRequest)sub).getFilter();
 			unlockFeatures.addAll(getFidFeatures(typeName, filter));
 		    } else if (sub.getOpType() == SubTransactionRequest.UPDATE){
 			filter = ((UpdateRequest)sub).getFilter();
 			unlockFeatures.addAll(getFidFeatures(typeName, filter));
 		    }
 		    } catch (WfsException e){
 			//this should never really happen, as the transaction should
 			//have already gone through.
 			throw new WfsTransactionException(e, sub.getHandle(),
 							  completed.getHandle());
 		    }
 		}
 		lock.unlock(unlockFeatures, releaseAll);
 	    }
             return true;
 	    }
 	}
 
     /**
      * gets the set of features locked by the given lockId.
      * @param lockId the id of the lock to query.
      * @return a Set of fids that this lock holds.
      */
     public Set getLockedFeatures(String lockId){
 	InternalLock lock = (InternalLock)locks.get(lockId);
 	return lock.getLockedFeatures();
     }
 
     /**
      * gets the set of features that the lock attempted to lock but
      * failed at.
      * @param lockId the id of the lock to query.
      * @return a Set of fids that the lock tried to lock but did not go through.
      */
     public Set getNotLockedFeatures(String lockId){
 	InternalLock lock = (InternalLock)locks.get(lockId);
 	return lock.getNotLockedFeatures();
     }
 
     
     /**
      * Override of toString method. */
     public String toString() {
         StringBuffer returnString = new StringBuffer("\n  TypeRepository:");
         Collection typeList = types.values();
         Iterator i = typeList.iterator();
         while(i.hasNext()) {
             TypeInfo type = (TypeInfo) i.next();
             returnString.append("\n   " + type.getName());
             if(lockedFeatures.containsValue(type.getName())) {
                 returnString.append(" (" + type.getName() + ")");
             }
         }
         return returnString.toString();
     }
 
     /**
      * Acts as the lock on features, managing the expiry and the releasing of
      * feature information.
      * 
      * @author Chris Holmes, TOPP
      * @version $VERSION$
      */
     private class InternalLock {
         
 	/** If no expiry is set then default to a day.*/
 	//REVISIT: would be nice to have an admin tool to clear the
 	//locks.
 	public static final int DEFAULT_EXPIRY = 1440;
 	
 	/** to turn from expiry minutes to timer milliseconds */
 	private static final int MILLISECONDS_PER_MINUTE = 1000 * 60;
 
 	/** The Timer that keeps track of when to expire */
 	private Timer expiryTimer = new Timer(true);
 	
 	/** The task that performs the expire when the time is up*/
 	private ExpireTask expiry = new ExpireTask();
 
 	/** The length of the expiry*/
 	private int expiryTime;
 
 	/** The string used to access this lock. */
 	private String lockId;
 	
 	/** The feature Types held by this lock. */
 	private Set features = new HashSet();
 
 	/** The features that matched the lock but were already locked.*/
 	private Set notLockedFeatures = new HashSet();
 
 	
     /** Initializes a lock with the number of minutes until it should expire*/ 
 	public InternalLock(String lockId, int expiryMinutes) {
 	    LOG.finer("created new lock with id " + lockId + " and expiry "
 			 + expiryMinutes);
 	    this.lockId = lockId;
 	    if (expiryMinutes < 0) {
 		expiryMinutes = DEFAULT_EXPIRY;
 	    } 
 	    this.expiryTime = expiryMinutes * MILLISECONDS_PER_MINUTE;
 	    expiryTimer.schedule(expiry, expiryTime);
 	    locks.put(lockId, this);
 	}
 
 	/** returns the set of features locked.*/
 	public Set getLockedFeatures(){
 	    return features;
 	}
 
 	/** returns the set of features not locked.*/
 	//this is a little weird, as it just depends on attempted lockings.
 	//I guess if locks are ever used twice we can clear out the field.
 	public Set getNotLockedFeatures(){
 	    return notLockedFeatures;
 	}
 	
 	/** 
 	 * Gets the lockId string
 	 * @return the lockId */
 	public String getId(){
 	    return lockId;
 	}
 	
 	/**
 	 * adds the given fids to this lock, if they are not already held by
 	 * another lock.  Follows the lockAll action.
 	 * @param lockAll if true and all features can not be locked then this
 	 * lock is released, false returned.  If false then all features that
 	 * are not held by other locks are added.
 	 * @param addFeatures the features to attempt to add.
 	 * @return true if the features were successfully added, false 
 	 * otherwise.  Should only be false when lockAll is true and one
 	 * or more addFeatures is already held by another lock.
 	 */
 	public boolean addFeatures(boolean lockAll, List addFeatures){
 	    for (Iterator i = addFeatures.iterator(); i.hasNext();){
 		String curFid = i.next().toString();
 		if (lockedFeatures.containsKey(curFid)){
 		    if (lockAll) {
 			release();
 			return false;
 		    } else {
 			notLockedFeatures.add(curFid);
 		    }
 		} else {
 		    features.add(curFid);
 		    lockedFeatures.put(curFid, this);
 		}
 		
 	    }
 	    return true;
 	}
 
 	/**
 	 * clears this lock and everything that it holds.
 	 */
 	private void release(){
 	    Iterator featureIter = features.iterator();
 	    while(featureIter.hasNext()){
 		lockedFeatures.remove(featureIter.next().toString());
 	    }
 	    locks.remove(this.lockId);
 	    expiryTimer.cancel();
 	    
 	}
 
 	/**
 	 * unlocks based on the releaseAll action.  If true then everything
 	 * held by this lock is released, if not, then only the typeName
 	 * featureType is.
 	 *
 	 * @param typeName the name of the featureType to unlock.
 	 * @param releaseAll if true then get rid of this lock, if false then
 	 * this only unlocks the feature of typeName.
 	 */
  	public boolean unlock(List releaseFeatures, boolean releaseAll){
 	    if (releaseAll) {
 		this.release();
 		return true;
 	    } else {
 		boolean featuresReleased = false;
 		for (Iterator i = releaseFeatures.iterator(); i.hasNext();){
 		    String curFid = i.next().toString();
 		    if (this.features.contains(curFid)){
 			featuresReleased=true;
 			lockedFeatures.remove(curFid);
 			this.features.remove(curFid);
 		    }
 		}
 		if (this.features.size() == 0) {
 		    locks.remove(this.lockId);
 		} else {
 		    expiry.cancel();
 		    expiry = new ExpireTask();
 		    expiryTimer.schedule(expiry, expiryTime);
 		}
 		return featuresReleased;
 	    }
 	}
 	
 	/** task to expire a lock, calls release and cancels the timer.*/
 	private class ExpireTask extends TimerTask {
 	    public void run() {
 		LOG.fine("expiring lock: " + lockId);
 		release();
 		expiryTimer.cancel(); //Terminate the timer thread
 	    }
 	}
     }
 	
 
 }
