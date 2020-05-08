 package com.meadowhawk.homepi.dao;
 
 import org.joda.time.DateTime;
 import org.springframework.stereotype.Component;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.meadowhawk.homepi.model.PiProfile;
 
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
 
 	public boolean validateApiKey(String piSerialId, String apiKey) {
		//select count(*) from pi_profile p where p.pi_serial_id = :piSerialId and p.apiKey = :apiKey
//		TODO: TEST this
		Long ct = 1L;//(Long) entityManager.createNamedQuery("select count(*) from pi_profile p where p.pi_serial_id = :piSerialId and p.api_key = :apiKey").setParameter("piSerialId", piSerialId).setParameter("apiKey", apiKey).getSingleResult();
//		Long ct = entityManager.createNamedQuery("PiProfile.findByPiSerialIdApiToken",Long.class).setParameter("piSerialId", piSerialId).setParameter("apiKey", apiKey).getSingleResult();
		return (ct==1)?true:false;
 	}
 }
