 package models;
 
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.*;
 
 import play.Logger;
 import play.Play;
 import play.db.jpa.GenericModel;
 import play.libs.Crypto;
 
 import com.google.gson.annotations.Expose;
 
 import ext.Formula;
 
 /**
  * @author <a href="mailto:alexander.hanschke@gmail.com">Alexander Hanschke</a>
  */
@Table(name="Player")
 @Entity
 public class User extends GenericModel {
 	
 	@Id
 	@Expose
 	@GeneratedValue
 	public Long id;
 	public String token;
 
     public Species species;
     public Role role;
 	
 	public Date firstlogin;
 	public Date lastlogin;
 	
 	public Double sight;
 	
 	public String email;
 	public String name;
 	public String password;
 	
 	public Long health;
 	public Long maxhealth;
 	public Long rage;
 	public Long maxrage;
 	public Long blood;
 	public Long maxblood;
 	
 	public Long xp;
 	public Long gold;
 	public Long level;
 	public Long threshold;
 	public Long skillpoints;
 
     public Boolean logout;
     public Boolean suspended;
 
     public Integer healthCooldown;
     public Integer rageCooldown;
     public Integer bloodCooldown;
 
     public Boolean rageJobRunning;
     public Boolean healthJobRunning;
     public Boolean bloodJobRunning;
 	
 	@Embedded
 	public Location location = Location.fromRadians(0.0, 0.0);
 
 	public List<User> nearby() {
 		Location[] locations = location.boundingCoordinates(sight);
 		boolean greenwhichMeridianIncluded = locations[0].longitude > locations[1].longitude;
 		
 		List<User> users = User.find(
 			"(latitude >= ? and latitude <= ?) and (longitude >= ? " + (greenwhichMeridianIncluded ? "or" : "and") + " longitude <= ?) AND " +
 			"acos(sin(?) * sin(latitude) + cos(?) * cos(latitude) * cos(longitude - ?)) <= ?",
 			locations[0].latitude, locations[1].latitude, locations[0].longitude, locations[1].longitude, location.latitude,
 			location.latitude, location.longitude, (sight / Location.EARTH_RADIUS_KM)).fetch();
 		
 		return users;
 	}
 
     public boolean canSee(User enemy) {
         return location.distanceTo(enemy.location) <= sight;
     }
 	
 	public static boolean isRegistered(String mail) {
 		return find("byMail", mail).first() != null;
 	}
 	
 	public static boolean authenticate(String name, String password) {
 		return null !=
 			find("byNameAndPassword", name, Crypto.passwordHash(password)).first();
 	}
 	
 	public static boolean nameExists(String name) {
 		return find("byName", name).first() != null;
 	}
 	
 	public static boolean mailExists(String mail) {
 		return find("byEmail", mail).first() != null;
 	}
 	
 	@Override
 	public String toString() {
 		return name;
 	}
 	
 	public static User make(String name, String email, String password) {
 		User user = new User();
 		user.name = name;
 		user.email = email;
 		user.password = Crypto.passwordHash(password);
 
         user.role = Role.PLAYER;
 		
 		user.sight = Double.valueOf(Play.configuration.getProperty("user.sight.initial"));
 		user.maxblood = Long.valueOf(Play.configuration.getProperty("user.blood.initial"));
 		user.blood = user.maxblood;
 		user.maxrage = Long.valueOf(Play.configuration.getProperty("user.rage.initial"));
 		user.rage = user.maxrage;
 		user.maxhealth = Long.valueOf(Play.configuration.getProperty("user.health.initial"));
 		user.health = user.maxhealth;
 		user.gold = Long.valueOf(100);
 		user.threshold = Long.valueOf(5);
 		user.xp = Long.valueOf(0);
 		user.level = Long.valueOf(1);
 		user.skillpoints = Long.valueOf(0);
 
         user.lastlogin =  new Date();
         user.firstlogin = new Date();
 
         user.logout =    Boolean.FALSE;
         user.suspended = Boolean.FALSE;
 
         user.rageJobRunning =   Boolean.FALSE;
         user.bloodJobRunning =  Boolean.FALSE;
         user.healthJobRunning = Boolean.FALSE;
 
         user.rageCooldown =   Integer.valueOf(Play.configuration.getProperty("user.rage.cooldown"));
         user.bloodCooldown =  Integer.valueOf(Play.configuration.getProperty("user.blood.cooldown"));
         user.healthCooldown = Integer.valueOf(Play.configuration.getProperty("user.health.cooldown"));
 		
 		return user;
 	}
 	
 	public void perform(Job job) {
 		this.blood = this.blood - job.blood;
 		this.gold  = this.gold  + job.gold;
 		gain(job.xp);
 	}
 	
 	public void gain(Long gain) {
 		if (this.xp + gain >= this.threshold) {
 			long overflow = gain - (this.threshold - this.xp);
 			levelUp();
 			this.xp = Long.valueOf(0);
 			gain(overflow);
 		} else {
 			this.xp = this.xp + gain;
 		}
 	}
 	
 	private void levelUp() {
 		this.level++;
 		this.skillpoints++;
 		
 		this.threshold = this.threshold + Formula.levelThreshold(this.level);
 		
 		recover();
 	}
 	
 	private void recover() {
 		this.health = this.maxhealth;
 		this.rage   = this.maxrage;
 		this.blood  = this.maxblood;
 	}
 
     public boolean isFacebookConnected() {
         return token != null;
     }
 
     public boolean hasSkillpoints() {
         return skillpoints > 0;
     }
 
     public boolean needsHealth() {
         return this.health < this.maxhealth;
     }
 
     public boolean needsBlood() {
         return this.blood < this.maxblood;
     }
 
     public boolean needsRage() {
         return this.rage < this.maxrage;
     }
 	
 
 }
