package otanga;
 
 import java.util.Map;
 import com.google.appengine.api.datastore.Entity;
 
 public final class DatastoreEntityProfile extends Profile {
 
 	private final Entity _profileEntity;
 	
 	public DatastoreEntityProfile(Entity profileEntity) {
 		if (profileEntity == null)
 			throw new IllegalArgumentException();
 			
 		if (!profileEntity.hasProperty("userId"))
 			throw new IllegalArgumentException();
 
 		_profileEntity = profileEntity;
 	}
 	
 	public final Entity getEntity() {
 		return _profileEntity;
 	}
 	
 	@Override
 	public Map<String, Object> getProperties() {
 		return _profileEntity.getProperties();
 	}
 
 	@Override
 	public boolean hasProperty(String propertyName) {
 		return _profileEntity.hasProperty(propertyName);
 	}
 
 	@Override
 	public Object getProperty(String propertyName) {
 		return _profileEntity.getProperty(propertyName);
 	}
 
 	@Override
 	public void setProperty(String propertyName, Object value) {
 		_profileEntity.setProperty(propertyName, value);		
 	}
 
 	@Override
 	public void removeProperty(String propertyName) {
 		_profileEntity.removeProperty(propertyName);
 	}
 
 }
