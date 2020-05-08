 package polly.core.users;
 
 import java.io.Serializable;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.ElementCollection;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.ManyToMany;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.Transient;
 
 import polly.util.Hashes;
 
 
 import de.skuzzle.polly.sdk.exceptions.UnknownAttributeException;
 
 
 @Entity
 @NamedQueries({
     @NamedQuery(
         name  = "USER_BY_NAME", 
         query = "SELECT u FROM User u WHERE u.name=?1"),
     @NamedQuery(
         name  = "ALL_USERS", 
         query = "SELECT u FROM User u")
 })
 public class User implements de.skuzzle.polly.sdk.model.User, Serializable {
     
     private static final long serialVersionUID = 1L;
     
     public final static String ALL_USERS = "ALL_USERS";
     
    
     @Id@GeneratedValue(strategy = GenerationType.TABLE)
     private int id;
     
     private int userLevel;
     
     @Column(unique = true, columnDefinition = "VARCHAR(255)")
     private String name;
     
     @Column(columnDefinition = "VARCHAR(32)")
     private String password;
     
     @ElementCollection(targetClass=String.class)
     @ManyToMany(cascade = CascadeType.ALL)
     private Map<String, String> attributes;
     
     @Transient
     private String currentNickName;
     
     @Transient
     private long lastActionTimeStamp;
     
     @Transient
     private long loginTime;

     
     User() {
         this("", "", 0);
     }
     
     
     
     User(String name, String password, int userLevel) {
         this.name = name;
         this.password = Hashes.md5(password);
         this.userLevel = userLevel;
         this.attributes = new HashMap<String, String>();
     }
     
     
     
     public int getId() {
         return this.id;
     }
     
     
 
     @Override
     public int getUserLevel() {
         return this.userLevel;
     }
     
     
 
     @Override
     public String getCurrentNickName() {
         return this.currentNickName;
     }
 
 
     
     @Override
     public boolean checkPassword(String password) {
         return this.password.equals(Hashes.md5(password));
     }
     
     
 
     @Override
     public void setPassword(String password) {
         this.password = Hashes.md5(password);
     }
     
     
 
     @Override
     public String getName() {
         return this.name;
     }
 
     
     
     @Override
     public void setName(String name) {
         this.name = name;
     }
 
     @Override
     public void setCurrentNickName(String nickName) {
         this.currentNickName = nickName;
     }
 
 
     @Override
     public String getHashedPassword() {
         return this.password;
     }
     
     
     @Override
     public void setHashedPassword(String password) {
         this.password = password;
     }
 
 
 
     @Override
     public void setUserLevel(int level) {
         this.userLevel = level;
     }
     
     
     @Override
     public String toString() {
         return this.name + "(Level " + this.userLevel + ")";
     }
 
     
     
     public Map<String, String> getAttributes() {
         return this.attributes;
     }
     
     
     
     @Override
     public Set<String> getAttributeNames() {
         return Collections.unmodifiableSet(this.attributes.keySet());
     }
 
 
     
     @Override
     public String getAttribute(String name) {
         if (!this.attributes.containsKey(name)) {
             throw new UnknownAttributeException(name);
         }
         return this.attributes.get(name);
     }
     
     
     
     public void setAttribute(String name, String value) {
         if (!this.attributes.containsKey(name)) {
             throw new UnknownAttributeException(name);
         }
         this.attributes.put(name, value);
     }
     
     
     
     public void setLastMessageTime(long timeStamp) {
         this.lastActionTimeStamp = timeStamp;
     }
     
     
     
     @Override
     public long getLastMessageTime() {
         return this.lastActionTimeStamp;
     }
 
     
     
     @Override
     public boolean isIdle() {
         return System.currentTimeMillis() - this.lastActionTimeStamp > IDLE_AFTER;
     }
     
     
     
     @Override
     public long getLoginTime() {
         return this.loginTime;
     }
 
     
     
     public void setLoginTime(long timeStamp) {
         this.loginTime = timeStamp;
     }
     
     
     @Override
     public int compareTo(de.skuzzle.polly.sdk.model.User o) {
         int i = new Integer(o.getUserLevel()).compareTo(this.getUserLevel());
         if (i == 0) {
             return o.getName().compareTo(this.getName());
         }
         return i;
     }
 }
