 package com.photon.phresco.commons;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.collections.CollectionUtils;
 
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.framework.model.LockDetail;
 import com.photon.phresco.util.Constants;
 
 public class LockUtil implements Constants {
	public static List<LockDetail> LOCK_DETAILS = new ArrayList<LockDetail>();
 	public static LockDetail getLockDetail(String appid, String actionType, String displayName, String uniqueKey) throws PhrescoException {
 		try {
 			LockDetail lockDetail = new LockDetail(appid, actionType, displayName, uniqueKey);
 			return lockDetail;
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 	}
 	
 	 public static void generateLock(List<LockDetail> lockDetails, boolean toGenerate) throws PhrescoException {
 		try {
 			List<LockDetail> newLockDetails = new ArrayList<LockDetail>();
 			newLockDetails.addAll(lockDetails);
 			List<LockDetail> availableLockDetails = getLockDetails();
 			if (toGenerate && CollectionUtils.isNotEmpty(availableLockDetails)) {
 				newLockDetails.addAll(availableLockDetails);
 			}
			LOCK_DETAILS.addAll(newLockDetails);
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
     }
 	
 	/**
 	 * To remove the lock once the initiated operation has been completed
 	 * @throws PhrescoException
 	 */
 	public  static void removeLock(String uniqueKey) throws PhrescoException {
 		try {
 			List<LockDetail> lockDetails = getLockDetails();
 			if (CollectionUtils.isNotEmpty(lockDetails)) {
 				List<LockDetail> availableLockDetails = new ArrayList<LockDetail>();
 				for (LockDetail lockDetail : lockDetails) {
 					if (!lockDetail.getUniqueKey().equalsIgnoreCase(uniqueKey)) {
 						availableLockDetails.add(lockDetail);
 					} 
 				}
				LOCK_DETAILS.clear();
				LOCK_DETAILS.addAll(availableLockDetails);
 			}
 		} catch (PhrescoException e) {
 			throw new PhrescoException(e);
 		}
 	}
 	
 	public static List<LockDetail> getLockDetails() throws PhrescoException {
		return LOCK_DETAILS;
 	}
 }
