 package uk.ac.cam.dashboard.models;
 
 import java.math.BigInteger;
 import java.security.SecureRandom;
 import java.util.HashSet;
 import java.util.Map;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 
 import org.hibernate.annotations.GenericGenerator;
 
 import uk.ac.cam.dashboard.helpers.LDAPQueryHelper;
 
 import com.google.common.collect.ImmutableMap;
 
 @Entity
 @Table(name = "API")
 public class Api implements Mappable{
 
 	@Id
 	@GeneratedValue(generator="increment")
 	@GenericGenerator(name="increment", strategy="increment")
 	private int id;
 	
 	@ManyToOne
 	@JoinColumn(name="USER_CRSID")
 	private User user;
 	
 	private String key;
 	
 	public Api() {
 		String generatedKey = this.generateNewApiKey();
 		this.setKey(generatedKey);
 	}
 	
 	private String generateNewApiKey() {
 		SecureRandom random = new SecureRandom();
 		return new BigInteger(200, random).toString(32);
 	}
 
 	public int getId() { return id; }
 	public String getKey() { return key; }
 	public void setKey(String key) { this.key = key; }
 	public User getUser() { return user; }
 	public void setUser(User user) { this.user = user;}
 	
 	@Override
 	public Map<String, ?> toMap(){
 		return ImmutableMap.of("id", id, "key", key);
 	}
 	
 }
 
 
