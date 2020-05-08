 /**
  *
  */
 package org.ocha.hdx.persistence.entity.configs;
 
 import javax.persistence.Column;
 import javax.persistence.MappedSuperclass;
 
import org.hibernate.annotations.Index;

 /**
  * @author alexandru-m-g
  *
  */
 @MappedSuperclass
 public abstract class AbstractConfigEntry {
 
 	@Column(name = "entry_key", unique = false, nullable = false, updatable = false)
	@Index(name = "keyIndex")
 	private String entryKey;
 
 	@Column(name = "entry_value", unique = false, nullable = false, updatable = true)
 	private String entryValue;
 
 	public AbstractConfigEntry() {}
 
 	public AbstractConfigEntry(final String key, final String value) {
 		super();
 		this.entryKey = key;
 		this.entryValue = value;
 	}
 
 	public String getEntryKey() {
 		return this.entryKey;
 	}
 
 	public void setEntryKey(final String key) {
 		this.entryKey = key;
 	}
 
 	public String getEntryValue() {
 		return this.entryValue;
 	}
 
 	public void setEntryValue(final String value) {
 		this.entryValue = value;
 	}
 
 
 
 }
