 package com.aimluck.model;
 
 import java.util.Date;
 
 import org.slim3.datastore.Attribute;
 import org.slim3.datastore.Model;
 
 import com.aimluck.lib.util.Encrypter;
 import com.google.appengine.api.datastore.Key;
 
@Model(kind = "uD", schemaVersion = 1, schemaVersionName = "sV")
 public class Republish {
 	private static final long serialVersionUID = 1L;
 
 	  @Attribute(primaryKey = true, name = "k")
 	  private Key key;
 
 	  @Attribute(version = true, name = "v")
 	  private Long version;
 
 	  @Attribute(name = "m", unindexed = true)
 	  private String mail;
 
 	  @Attribute(name = "uId")
 	  private String userId;
 
 	  /**
 	   * Returns the key.
 	   * 
 	   * @return the key
 	   */
 	  public Key getKey() {
 	    return key;
 	  }
 
 	  /**
 	   * Sets the key.
 	   * 
 	   * @param key
 	   *          the key
 	   */
 	  public void setKey(Key key) {
 	    this.key = key;
 	  }
 
 	  /**
 	   * Returns the version.
 	   * 
 	   * @return the version
 	   */
 	  public Long getVersion() {
 	    return version;
 	  }
 
 	  /**
 	   * Sets the version.
 	   * 
 	   * @param version
 	   *          the version
 	   */
 	  public void setVersion(Long version) {
 	    this.version = version;
 	  }
 
 	  @Override
 	  public int hashCode() {
 	    final int prime = 31;
 	    int result = 1;
 	    result = prime * result + ((key == null) ? 0 : key.hashCode());
 	    return result;
 	  }
 
 	  public String getMail() {
 	    return mail;
 	  }
 
 	  public void setMail(String mail) {
 	    this.mail = mail;
 	  }
 
 	  /**
 	   * @return userId
 	   */
 	  public String getUserId() {
 	    return userId;
 	  }
 
 	  /**
 	   * @param userId
 	   *          セットする userId
 	   */
 	  public void setUserId(String userId) {
 	    this.userId = userId;
 	  }
 	
 }
