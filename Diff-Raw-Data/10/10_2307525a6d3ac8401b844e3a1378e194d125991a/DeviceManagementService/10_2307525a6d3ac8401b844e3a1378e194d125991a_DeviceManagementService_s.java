 package com.meadowhawk.homepi.service.business;
 
 import java.util.List;
 import java.util.UUID;
 
 import javax.persistence.EntityExistsException;
 import javax.persistence.NoResultException;
 import javax.ws.rs.core.Response.Status;
 
 import org.joda.time.DateTime;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import com.meadowhawk.homepi.dao.PiProfileDAO;
 import com.meadowhawk.homepi.exception.HomePiAppException;
 import com.meadowhawk.homepi.model.PiProfile;
 import com.meadowhawk.homepi.util.StringUtil;
 import com.meadowhawk.homepi.util.service.ApiKeyRequired;
 import com.meadowhawk.homepi.util.service.ApiKeyRequiredBeforeException;
 import com.meadowhawk.homepi.util.service.ApiKeyRequiredException;
 import com.meadowhawk.homepi.util.service.AuthRequiredBeforeException;
 
 /**
  * Service that is used for providing data specifically to the Raspberry Pi device. All Calls should enforce an API verification for security reasons. 
  * @author lee
  */
 @Component
 public class DeviceManagementService {
 
 	@Autowired
 	PiProfileDAO piProfileDao;
 	
 	/**
 	 * @param serialId
 	 * @return
 	 * @throws HomePiAppException
 	 */
 	protected PiProfile getPiProfile(String serialId) throws HomePiAppException {
 		try{
 			return piProfileDao.findByPiSerialId(serialId);
 		} catch(NoResultException nre){
 			throw new HomePiAppException(Status.NOT_FOUND, "No matching serial ID was found.");
 		} catch(Exception e){
 			throw new HomePiAppException(Status.BAD_REQUEST, e);
 		}
 	}
 
 	@ApiKeyRequired
 	public PiProfile getPiProfile(String deviceId, String apiKey) {
 		return getPiProfile(deviceId);
 	}
 
 	/**
 	 * @return
 	 * @throws HomePiAppException
 	 */
 //	@ApiKeyRequired
 	//TODO: EVAL: NOt currently in use. I can't think of a reason for Pis to know about other Pis's...  yet.  Also API keys, only grant access to one profile.
 	@Deprecated
 	public List<PiProfile> getAllPiProfiles() throws HomePiAppException {
 		try{
 			return piProfileDao.findAll();
 		} catch(NoResultException nre){
 			throw new HomePiAppException(Status.NOT_FOUND, "No matching serial ID was found.");
 		} catch(Exception e){
 			throw new HomePiAppException(Status.BAD_REQUEST, e);
 		}
 	}	
 	
 	/**
 	 * The create inserts a new row if none is present and then returns the result populated with what data the request provided. 
 	 * @param piSerialId
 	 * @param ipAddress
 	 * @return
 	 * @throws HomePiAppException
 	 */
 	public PiProfile createPiProfile(String piSerialId, String ipAddress) throws HomePiAppException {
 		if(StringUtil.isNullOrEmpty(piSerialId)){
 			throw new HomePiAppException(Status.BAD_REQUEST, "Invalid Pi Serial ID.");
 		}
 		try{
 			PiProfile piProfile = new PiProfile();
 			piProfile.setPiSerialId(piSerialId);
 			piProfile.setCreateTime(new DateTime());
 			piProfile.setIpAddress(ipAddress);
 			//TODO: Fix once User Auth complete!!!
 			piProfile.setUserId(1L);
 			
 			piProfileDao.save(piProfile);
 			
 			//Add apiKey to the entry.
 			piProfileDao.updateUUID(piProfile);
 			
 			return piProfileDao.findOne(piProfile.getPiId());
 		} catch(EntityExistsException eee){
 			throw new HomePiAppException(Status.BAD_REQUEST, piSerialId + ": This PI has already been registered");
 		} catch(Exception e){
 			throw new HomePiAppException(Status.BAD_REQUEST, e);
 		}
 	}
 
 	/**
 	 * Update Pi Profile
 	 * @param piProfile
 	 * @return
 	 * @throws HomePiAppException
 	 */
 	@ApiKeyRequiredBeforeException
 	public int updatePiProfile(PiProfile piProfile) throws HomePiAppException {
 		try{
 			piProfileDao.update(piProfile);
 			return 1;
 		} catch(Exception e){
 			throw new HomePiAppException(Status.NOT_MODIFIED, e);
 		}
 	}
 
 	/**
 	 * Request new ApiKey using current api key credentials. Expected use by the Pi.
 	 * @param apiKey - apiKey
 	 * @param piSerialId - piSerialId
 	 * @throws Exception if update fails
 	 */
 	@ApiKeyRequiredBeforeException
	public void updateApiKey(String apiKey, String piSerialId) {
 		try{
 			PiProfile profile = piProfileDao.findByPiSerialId(piSerialId);
 			profile.setApiKey(UUID.fromString(apiKey));
 			int stat = piProfileDao.updateUUID(profile);
 			if(stat != 1){
 				throw new HomePiAppException(Status.NOT_MODIFIED, "Update failed");
 			}
 		}
 		catch (NoResultException nre) {
 			throw new HomePiAppException(Status.NOT_FOUND, piSerialId + ": is not valid");
 		} catch (HomePiAppException h) {
 			throw h;
 		}	catch (Exception e) {
 			throw new HomePiAppException(Status.BAD_REQUEST, e);
 		}
 	}
 
 	/**
 	 * Request new ApiKey using User credentials. Expected use by the UI.
 	 * @param userName - userName
 	 * @param authToken - authToken
 	 * @param piSerialId - piSerialId
 	 * @throws Exception if failed.
 	 */
 	@AuthRequiredBeforeException
 	public void updateApiKey(String userName, String authToken, String piSerialId) {
 		try{
 			PiProfile profile = piProfileDao.findByPiSerialId(piSerialId);
 			int stat = piProfileDao.updateUUID(profile);
 			if(stat != 1){
 				throw new HomePiAppException(Status.NOT_MODIFIED, "Update failed");
 			}
 		}
 		catch (NoResultException nre) {
 			throw new HomePiAppException(Status.NOT_FOUND, piSerialId + ": is not valid");
 		} catch (HomePiAppException h) {
 			throw h;
 		} catch (Exception e) {
 			throw new HomePiAppException(Status.BAD_REQUEST, e);
 		}
 	}
 	
 	/**
 	 * Verifies that a given serialId and ApiKey Match.
 	 * @param piSerialId
 	 * @param apiKey
 	 * @return - true if valid
 	 */
 	public boolean validateApiKey(String piSerialId, String apiKey) {
 		try{
 			PiProfile profile = this.getPiProfile(piSerialId, apiKey);
 			return (profile.getApiKey().equals(apiKey));
 		} catch(Exception e){
 			return false;
 		}
 	}
 	
 }
