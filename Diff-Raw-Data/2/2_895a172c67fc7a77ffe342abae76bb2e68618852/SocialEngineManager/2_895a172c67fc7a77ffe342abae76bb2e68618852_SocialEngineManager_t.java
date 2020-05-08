 package eu.trentorise.smartcampus.ac.provider.managers;
 
 import it.unitn.disi.sweb.webapi.client.WebApiException;
 import it.unitn.disi.sweb.webapi.client.smartcampus.SCWebApiClient;
 import it.unitn.disi.sweb.webapi.model.entity.Entity;
 import it.unitn.disi.sweb.webapi.model.entity.EntityBase;
 import it.unitn.disi.sweb.webapi.model.entity.EntityType;
 import it.unitn.disi.sweb.webapi.model.smartcampus.social.User;
 
 import java.util.Locale;
 
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.stereotype.Component;
 
 @Component
 public class SocialEngineManager {
 
 	@Value("${socialEngine.host}")
 	private String socialEngineHost;
 	@Value("${socialEngine.port}")
 	private int socialEnginePort;
 
 	public long createUser(eu.trentorise.smartcampus.ac.provider.model.User user)
 			throws SocialEngineException {
 		long socialId = -1;
 		SCWebApiClient client = SCWebApiClient.getInstance(Locale.ENGLISH,
 				socialEngineHost, socialEnginePort);
 		if (!client.ping())
 			throw new SocialEngineException("Social engine not available");
 		EntityBase eb = null;
 		String userId = user.getId().toString();
 		Long entityBaseId = null;
 		Long entityId = null;
 		try {
 			eb = new EntityBase();
			eb.setLabel("SC_USER_" + System.currentTimeMillis());
 			entityBaseId = client.create(eb);
 			// Re-read to get the ID of the default KB
 			eb = client.readEntityBase(entityBaseId);
 
 			EntityType person = client
 					.readEntityType("person", eb.getKbLabel());
 			Entity entity = new Entity();
 			entity.setEntityBase(eb);
 			entity.setEtype(person);
 			entity.setExternalId(userId);
 
 			entityId = client.create(entity);
 
 			User socialUser = new User();
 			socialUser.setName("SC_" + System.currentTimeMillis());
 			socialUser.setEntityBaseId(entityBaseId);
 			socialUser.setPersonEntityId(entityId);
 			socialId = client.create(socialUser);
 
 		} catch (Exception e) {
 			try {
 				if (eb != null) {
 					client.deleteEntityBase(eb.getId());
 				}
 
 				if (entityId != null) {
 					client.deleteEntity(entityId);
 				}
 			} catch (WebApiException e1) {
 				throw new SocialEngineException();
 			}
 
 			throw new SocialEngineException();
 		}
 
 		return socialId;
 	}
 
 	private Entity findEntityByExternalId(SCWebApiClient client, String extId) {
 		return null;
 	}
 }
