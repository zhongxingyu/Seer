 package com.meadowhawk.homepi.service.business;
 
 import java.util.List;
 
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
 import com.meadowhawk.homepi.util.service.MaskData;
 
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
 	public PiProfile getPiProfile(String serialId) throws HomePiAppException {
 		try{
 			return piProfileDao.findByPiSerialId(serialId);
 		} catch(NoResultException nre){
 			throw new HomePiAppException(Status.NOT_FOUND, "No matching serial ID was found.");
 		} catch(Exception e){
 			throw new HomePiAppException(Status.BAD_REQUEST, e);
 		}
 	}
 
 	/**
 	 * @return
 	 * @throws HomePiAppException
 	 */
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
 	 * @param piProfile
 	 * @return
 	 * @throws HomePiAppException
 	 */
 	public int updatePiProfile(PiProfile piProfile) throws HomePiAppException {
 		try{
 			piProfileDao.update(piProfile);
 			return 1;
 		} catch(Exception e){
 			throw new HomePiAppException(Status.NOT_MODIFIED, e);
 		}
 	}
 
 	/**
 	 * @param piSerialId
 	 * @param apiKey
 	 * @return
 	 */
 	public PiProfile updateApiKey(String piSerialId, String apiKey) {
 		try{
 			PiProfile profile = piProfileDao.findByPiSerialId(piSerialId);
 			if(StringUtil.isNullOrEmpty(apiKey) || StringUtil.isNullOrEmpty( profile.getApiKey()) || !profile.getApiKey().equals(apiKey)){
 				throw new HomePiAppException(Status.BAD_REQUEST, "Invalid API Key.");
 			}
 			piProfileDao.updateUUID(profile);
 			
 			return piProfileDao.findByPiSerialId(piSerialId);
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
 	 * Verifies that a given serialId and ApiKey Match.
 	 * @param piSerialId
 	 * @param apiKey
 	 * @return - true if valid
 	 */
 	public boolean validateApiKey(String piSerialId, String apiKey) {
		return piProfileDao.validateApiKey(piSerialId,apiKey);
 	}
 	
 }
