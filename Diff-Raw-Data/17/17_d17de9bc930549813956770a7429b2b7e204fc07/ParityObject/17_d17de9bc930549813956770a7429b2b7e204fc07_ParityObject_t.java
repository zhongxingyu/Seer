 /*
  * Feb 18, 2005
  */
 package com.thinkparity.model.parity.api;
 
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Properties;
 import java.util.UUID;
 import java.util.Vector;
 
 import com.thinkparity.codebase.assertion.Assert;
 
 import com.thinkparity.model.parity.IParityModelConstants;
 import com.thinkparity.model.parity.model.note.Note;
 
 /**
  * ParityObject
  * @author raykroeker@gmail.com
  * @version 1.5.2.7
  */
 public abstract class ParityObject implements IParityModelConstants {
 
 	/**
 	 * The parity username of the person who created the parity object.
 	 */
 	private String createdBy;
 
 	/**
 	 * The date\time of creation of the parity object.
 	 */
 	private Calendar createdOn;
 
 	/**
 	 * The custom properties of the parity object.
 	 */
 	private Properties customProperties;
 
 	/**
 	 * The description of the parity object.
 	 */
 	private String description;
 
 	/**
 	 * Universally unique id for the object.
 	 */
 	private UUID id;
 
 	/**
 	 * The name of the parity object.
 	 */
 	private String name;
 
 	/**
 	 * List of notes associated with the parity object.
 	 */
 	private Collection<Note> notes;
 
 	/**
 	 * Reference to the parent parity object.
 	 */
 	private UUID parentId;
 
 	/**
 	 * Create a ParityObject.
 	 * 
 	 * @param parentId
 	 *            The parent id (Optional).
 	 * @param name
 	 *            The name.
 	 * @param description
 	 *            The description.
 	 * @param createdOn
 	 *            The creation date.
 	 * @param createdBy
 	 *            The creator.
 	 * @param id
 	 *            The id.
 	 */
 	protected ParityObject(final UUID parentId, final String name,
 			final String description, final Calendar createdOn,
 			final String createdBy, final UUID id) {
 		super();
 		this.parentId = parentId;
 		this.name = name;
 		this.description = description;
 		this.createdOn = createdOn;
 		this.createdBy = createdBy;
 		this.notes = new Vector<Note>(7);
 		this.customProperties = new Properties(createDefaultCustomProperties(name, description));
 		this.id = id;
 	}
 
 	public final void add(final Note note) {
 		Assert.assertTrue("Cannot add the same note more than once.", !notes
 				.contains(note));
 		notes.add(note);
 	}
 
 	/**
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	public boolean equals(Object obj) {
		if(obj instanceof ParityObject) {
			return id.equals(((ParityObject) obj).getId());
		}
		return false;
 	}
 
 	public final String getCreatedBy() { return createdBy; }
 
 	public final Calendar getCreatedOn() { return createdOn; }
 
 	/**
 	 * Obtain the custom description for the parity object.  The custom
 	 * description is set on a per-user basis.
 	 * @return <code>java.lang.String</code>
 	 */
 	public final String getCustomDescription() {
 		return getCustomProperty("description");
 	}
 
 	/**
 	 * Obtain the custom name for the parity object.  The custom name is set on
 	 * a per-user basis.
 	 * @return <code>java.lang.String</code>
 	 */
 	public final String getCustomName() { return getCustomProperty("name"); }
 
 	/**
 	 * Obtain all of the custom properties for the parity object.  Custom
 	 * properties are set on a per-user basis.
 	 * @return <code>java.util.Properties</code>
 	 */
 	public final Properties getCustomProperties() { return customProperties; }
 
 	/**
 	 * Obtain a named custom property for the parity object.  The custom
 	 * property is set on a per-user basis.
 	 * @param customPropertyName <code>java.lang.String</code>
 	 * @return <code>java.lang.String</code>
 	 */
 	public final String getCustomProperty(final String customPropertyName) {
 		return customProperties.getProperty(customPropertyName);
 	}
 
 	/**
 	 * Obtain a named custom property for the parity object, and if the property
 	 * is not set, return the default value.  The custom property is set on a
 	 * per-user basis.
 	 * @param customPropertyName <code>java.lang.String</code>
 	 * @param customPropertyDefaultValue <code>java.lang.String</code>
 	 * @return <code>java.lang.String</code>
 	 */
 	public final String getCustomProperty(final String customPropertyName,
 			final String customPropertyDefaultValue) {
 		return customProperties.getProperty(customPropertyName,
 				customPropertyDefaultValue);
 	}
 
 	/**
 	 * Obtain the description for the parity object.
 	 * @return <code>java.lang.String</code>
 	 */
 	public final String getDescription() { return description; }
 
 	/**
 	 * Obtain the value of id.
 	 * @return <code>UUID</code>
 	 */
 	public UUID getId() { return id; }
 
 	/**
 	 * Obtain the name for the parity object.
 	 * @return <code>java.lang.String</code>
 	 */
 	public final String getName() { return name; }
 
 	public final Collection<Note> getNotes() { return notes; }
 
 	/**
 	 * Obtain the parent id.
 	 * 
 	 * @return The parent id.
 	 */
 	public UUID getParentId() { return parentId; }
 
 	/**
 	 * Obtain the type of parity object.
 	 * 
 	 * @return <code>com.thinkparity.model.parity.api.ParityObjectType</code>
 	 */
 	public abstract ParityObjectType getType();
 
 	/**
 	 * @see java.lang.Object#hashCode()
 	 */
 	public int hashCode() { return id.hashCode(); }
 
 	/**
 	 * Determine whether or not the parent is set.
 	 * @return Boolean</code>
 	 */
 	public final Boolean isSetParentId() { return null != parentId; }
 
 	public final void remove(final Note note) {
 		if(notes.contains(note))
 			notes.remove(note);
 	}
 
 	/**
 	 * Set the custom description for the parity object.  The description is set
 	 * on a per-user basis.
 	 * @param description <code>java.lang.String</code>
 	 */
 	public final void setCustomDescription(final String description) {
 		customProperties.setProperty("description", description);
 	}
 
 	/**
 	 * Set the custom name for the parity object.  The name is set on a per-user
 	 * basis.
 	 * @param name <code>java.lang.String</code>
 	 */
 	public final void setCustomName(final String name) {
 		customProperties.setProperty("name", name);
 	}
 
 	/**
 	 * Set a named custom property.  The custom property is set on a per-user
 	 * basis.
 	 * @param customPropertyName <code>java.lang.String</code>
 	 * @param customPropertyValue <code>java.lang.String</code>
 	 */
 	public final void setCustomProperty(final String customPropertyName,
 			final String customPropertyValue) {
 		customProperties.setProperty(customPropertyName, customPropertyValue);
 	}
 
 	/**
 	 * Set the id of the parent.
 	 * 
 	 * @param parentId
 	 *            The parent id.
 	 */
 	public void setParentId(final UUID parentId) { this.parentId = parentId; }
 
 	/**
 	 * Create a default instance of the custom properties.
 	 * 
 	 * @return The default custom properties.
 	 */
 	private Properties createDefaultCustomProperties(final String name,
 			final String description) {
 		if(null == name) { throw new NullPointerException(); }
 		final Properties customProperties = new Properties();
 		customProperties.setProperty("name", name);
 		if(null != description)
 			customProperties.setProperty("description", description);
 		return customProperties;
 	}
 }
