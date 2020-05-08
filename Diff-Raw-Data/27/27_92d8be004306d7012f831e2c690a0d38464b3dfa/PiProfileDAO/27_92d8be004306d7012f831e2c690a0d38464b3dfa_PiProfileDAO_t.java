 package com.meadowhawk.homepi.dao;
 
 import org.joda.time.DateTime;
 import org.springframework.stereotype.Component;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.meadowhawk.homepi.model.PiProfile;
import com.meadowhawk.homepi.util.StringUtil;
 
 /**
  * @author lee
  */
 @Component
 public class PiProfileDAO extends AbstractJpaDAO< PiProfile >{
 	
 	public PiProfileDAO(){
 		setClazz(PiProfile.class);
 	}
 	
 	public PiProfile findByPiSerialId(String piSerialId){
 		return entityManager.createNamedQuery("PiProfile.findByPiSerialId",this.clazz).setParameter("piSerialId", piSerialId).getSingleResult();
 	}
 	
 	@Override
 	@Transactional
 	public void update(PiProfile entity) {
 		entity.setUpdateTime(new DateTime());
 		super.update(entity);
 	}
 	
 	@Transactional
 	public int updateUUID(PiProfile entity){
 		return entityManager.createNativeQuery("update PI_PROFILE set api_key = uuid_generate_v4() where pi_id = " +entity.getPiId()).executeUpdate();
 	}
 
	/**
	 * Validate ApiKey for a given Pi Serial ID.
	 * @param piSerialId
	 * @param apiKey
	 * @return true if valid
	 */
 	public boolean validateApiKey(String piSerialId, String apiKey) {
		if(StringUtil.isNullOrEmpty(piSerialId) || StringUtil.isNullOrEmpty(apiKey)){
			return false;
		}
		PiProfile profile = findByPiSerialId(piSerialId);
		return (profile.getApiKey().equals(apiKey))?true:false;
 	}
 }
