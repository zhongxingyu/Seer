 package de.lbe.sandbox.openejb;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.Table;
 
 /**
  * 
  */
 @Entity
 @Table(name = "languages")
 public class Language implements Serializable {
 
 	/** The Constant PROPERTY_ID. */
 	public static final String PROPERTY_ID = "id";
 
 	/** The Constant PROPERTY_STATUS. */
 	public static final String PROPERTY_STATUS = "status";
 
 	/** The Constant PROPERTY_ISO639_ALPHA2_CODE. */
 	public static final String PROPERTY_ISO639_ALPHA2_CODE = "iso639Alpha2Code";
 
 	/** The Constant PROPERTY_ISO639_ALPHA3_CODE. */
 	public static final String PROPERTY_ISO639_ALPHA3_CODE = "iso639Alpha3Code";
 
 	public static final String PROPERTY_ISO3166_ALPHA2_CODE = "iso3166Alpha2Code";
 
 	private static final long serialVersionUID = 1234567890L;
 
 	private int id;
 	private String name;
 	private int status;
 	private String iso639Alpha2Code;
 	private String iso639Alpha3Code;
 	private String iso3166Alpha2Code;
 
 	/**
 	 * The getter for id.
 	 * 
 	 * @return the id
 	 */
 	@Id
 	@GeneratedValue(strategy = GenerationType.AUTO)
 	@Column(name = "language_id", unique = true, nullable = false)
 	public int getId() {
 		return id;
 	}
 
 	/**
 	 * The getter for iso3166Alpha2Code.
 	 * 
 	 * @return the iso3166Alpha2Code
 	 */
 	@Column(name = "iso3166_a2", nullable = false, length = 2)
 	public String getIso3166Alpha2Code() {
 		return iso3166Alpha2Code;
 	}
 
 	/**
 	 * The getter for iso639Alpha2Code.
 	 * 
 	 * @return the iso639Alpha2Code
 	 */
 	@Column(name = "iso639_a2", nullable = false, length = 2)
 	public String getIso639Alpha2Code() {
 		return iso639Alpha2Code;
 	}
 
 	/**
 	 * The getter for iso639Alpha3Code.
 	 * 
 	 * @return the iso639Alpha3Code
 	 */
 	@Column(name = "iso639_a3", nullable = false, length = 3)
 	public String getIso639Alpha3Code() {
 		return iso639Alpha3Code;
 	}
 
 	/**
 	 * The getter for name.
 	 * 
 	 * @return the name
 	 */
 	@Column(name = "name_eng", nullable = false, length = 50)
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * The getter for status.
 	 * 
 	 * @return the status
 	 */
 	@Column(name = "status", nullable = false)
 	public int getStatus() {
 		return status;
 	}
 
 	/**
 	 * The setter for id.
 	 * 
 	 * @param id the id to set
 	 */
 	public void setId(int id) {
 		this.id = id;
 	}
 
 	/**
 	 * The setter for iso3166Alpha2Code.
 	 * 
 	 * @param id the id to iso3166Alpha2Code
 	 */
 	public void setIso3166Alpha2Code(String iso3166Alpha2Code) {
 		this.iso3166Alpha2Code = iso3166Alpha2Code;
 	}
 
 	/**
 	 * The setter for iso639Alpha2Code.
 	 * 
 	 * @param iso639Alpha2Code the iso639Alpha2Code to set
 	 */
 	public void setIso639Alpha2Code(String iso639Alpha2Code) {
 		this.iso639Alpha2Code = iso639Alpha2Code;
 	}
 
 	/**
 	 * The setter for iso639Alpha3Code.
 	 * 
 	 * @param iso639Alpha3Code the iso639Alpha3Code to set
 	 */
 	public void setIso639Alpha3Code(String iso639Alpha3Code) {
 		this.iso639Alpha3Code = iso639Alpha3Code;
 	}
 
 	/**
 	 * The setter for name.
 	 * 
 	 * @param name the name to set
 	 */
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	/**
 	 * The setter for status.
 	 * 
 	 * @param status the status to set
 	 */
 	public void setStatus(int status) {
 		this.status = status;
 	}
 
 	/**
 	 * Only for test purposes.
 	 */
 	private void writeObject(ObjectOutputStream oos) throws IOException {
 		oos.defaultWriteObject();
 	}
 
 	/**
 	 * Only for test purposes.
 	 */
 	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
 		ois.defaultReadObject();
 	}
 
	@Override
 	public String toString() {
 		return getClass().getSimpleName() + "[id=" + getId() + "]";
 	}
 
 }
