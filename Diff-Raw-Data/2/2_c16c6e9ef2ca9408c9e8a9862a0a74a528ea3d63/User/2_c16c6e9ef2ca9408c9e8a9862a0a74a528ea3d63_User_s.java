 package org.iucn.sis.shared.api.models;
 /**
  * "Visual Paradigm: DO NOT MODIFY THIS FILE!"
  * 
  * This is an automatic generated file. It will be regenerated every time 
  * you generate persistence class.
  * 
  * Modifying its content may cause the program not work, or your work may lost.
  */
 
 /**
  * Licensee: 
  * License Type: Evaluation
  */
 import java.io.Serializable;
 
 import com.solertium.lwxml.shared.NativeElement;
 import com.solertium.lwxml.shared.NativeNode;
 import com.solertium.lwxml.shared.NativeNodeList;
 import com.solertium.util.portable.XMLWritingUtils;
 public class User implements Serializable {
 	
 	private static final long serialVersionUID = 1L;
 	
 	/* THINGS I HAVE ADDED... IF YOU REGENERATE, MUST ALSO COPY THIS*/
 	public final static String ROOT_TAG = "user";
 	public static final int DELETED = -1;
 	public static final int ACTIVE = 0;
 	
 	public int state;
 	public int getState() {
 		return state;
 	}
 	public void setState(int state) {
 		this.state = state;
 	}
 	
 	public String toXML() {
 		StringBuilder xml = new StringBuilder("<" + ROOT_TAG + ">");
 		xml.append(XMLWritingUtils.writeTag("id", getId()+""));
 		xml.append(XMLWritingUtils.writeTag("state", getState()+"", true));
 		xml.append(XMLWritingUtils.writeCDATATag("username", username));
 		xml.append(XMLWritingUtils.writeCDATATag("firstName", firstName, true));
 		xml.append(XMLWritingUtils.writeCDATATag("lastName", lastName, true));
 		xml.append(XMLWritingUtils.writeCDATATag("initials", initials, true));
 		xml.append(XMLWritingUtils.writeCDATATag("affiliation", affiliation, true));
 		xml.append(XMLWritingUtils.writeCDATATag("sisUser", getSisUser() == null ? null : getSisUser()+"", true));
 		xml.append(XMLWritingUtils.writeCDATATag("rapidListUser", getRapidlistUser() == null ? null : getRapidlistUser()+"", true));
 		xml.append(XMLWritingUtils.writeCDATATag("email", email, true));
 		xml.append("</" + ROOT_TAG + ">");
 		return xml.toString();		
 	}
 	
 	public String toFullXML() {
 		StringBuilder xml = new StringBuilder("<" + ROOT_TAG + ">");
 		xml.append(XMLWritingUtils.writeTag("id", getId()+""));
 		xml.append(XMLWritingUtils.writeTag("state", getState()+"", true));
 		xml.append(XMLWritingUtils.writeCDATATag("username", username));
 		xml.append(XMLWritingUtils.writeCDATATag("firstName", firstName, true));
 		xml.append(XMLWritingUtils.writeCDATATag("lastName", lastName, true));
 		xml.append(XMLWritingUtils.writeCDATATag("initials", initials, true));
 		xml.append(XMLWritingUtils.writeCDATATag("affiliation", affiliation, true));
 		xml.append(XMLWritingUtils.writeCDATATag("sisUser", getSisUser() == null ? null : getSisUser()+"", true));
 		xml.append(XMLWritingUtils.writeCDATATag("rapidListUser", getRapidlistUser() == null ? null : getRapidlistUser()+"", true));
 		xml.append(XMLWritingUtils.writeCDATATag("email", email, true));
 		
 		for (PermissionGroup group : getPermissionGroups()) {
 			xml.append(group.toXML());
 		}
 		
 		xml.append("</" + ROOT_TAG + ">");
 		
 		return xml.toString();	
 	}
 	
 	public String toBasicXML() {
 		StringBuilder xml = new StringBuilder("<" + ROOT_TAG + ">");
 		xml.append("<id>" + getId() + "</id>");
 		xml.append("<username><![CDATA[" + getUsername() + "]]></username>");
 		xml.append("<email><![CDATA[" + getEmail() + "]]></email>");
 		xml.append("</" + ROOT_TAG + ">");
 		return xml.toString();
 	}
 	
 	
 	public static User fromXML(NativeElement element) {
 		final User user = new User();
 		
 		fromXML(element, user);
 		
 		return user;
 	}
 	
 	public static void fromXML(NativeElement element, User user) {
 		final NativeNodeList nodes = element.getChildNodes();
 		for (int i = 0; i < nodes.getLength(); i++) {
 			final NativeNode node = nodes.item(i);
 			final String name = node.getNodeName();
 			final String value = node.getTextContent();
 			if ("id".equals(name))
 				user.setId(Integer.valueOf(value));
 			else if ("username".equals(name))
 				user.setUsername(value);
 			else if ("email".equals(name))
 				user.setEmail(value);
 			else if ("affiliation".equals(name))
 				user.setAffiliation(value);
 			else if ("state".equals(name))
 				user.setState(Integer.valueOf(value));
 			else if ("firstName".equals(name))
 				user.setFirstName(value);
 			else if ("lastName".equals(name))
 				user.setLastName(value);
 			else if ("initials".equals(name))
 				user.setInitials(value);
 			else if ("sisUser".equals(name))
 				user.setSisUser("true".equals(value));
 			else if ("rapidListUser".equals(name))
 				user.setRapidlistUser("true".equals(value));
 		}
 		
 		//FULL XML
 		NativeNodeList permGroups = element.getElementsByTagName(PermissionGroup.ROOT_TAG);
 		for (int i = 0; i < permGroups.getLength(); i++) {
 			user.getPermissionGroups().add(PermissionGroup.fromXML(permGroups.elementAt(i)));
 		}
 	}
 	
 	public String getCitationName() {
 		String firstName;
 		if (initials != null && !initials.equalsIgnoreCase(""))
 			firstName = ", " + initials;
 		else if (this.firstName != null && this.firstName.length() > 0)
 			firstName = ", " + this.firstName.charAt(0) + ".";
 		else
 			firstName = "";
 
 		return lastName == null ? firstName : lastName + firstName;
 	}
 	
 	public String getDisplayableName() {
 		if (firstName != null && lastName != null)
 			return firstName + " " + lastName;
 		else if (firstName == null && lastName == null)
 			return email;
 		else if (firstName == null)
 			return lastName;
 		else if (lastName == null)
 			return firstName;
 		else
 			return "(No name supplied)"; //Unreachable
     }
 	
 	public boolean isSISUser() {
 		return getSisUser();
 	}
 	
 	public boolean isRapidlistUser() {
 		return getRapidlistUser();
 	}
 	
 	/**
 	 * Can only be called if you have the permission groups 
 	 * attached at time of calling.
 	 * 
 	 * Returns to you the quickgroup string
 	 * @return
 	 */
 	public String getQuickGroupString() {
 		
 		if (!getPermissionGroups().isEmpty()) {
 			StringBuilder csv = new StringBuilder();
 			for (PermissionGroup group : getPermissionGroups())
 				csv.append(group.getName() + ",");
 			return csv.substring(0, csv.length()-1);
 		}
 		return "";
 		
 	}
 	
 	private String password;
 	
 	public String getPassword() {
 		return password;
 	}
 	
 	public void setPassword(String password) {
 		this.password = password;
 	}
 	
 	/* THINGS I HAVE ADDED... IF YOU REGENERATE, MUST ALSO COPY THIS*/
 	
 	public User() {
 		this.state = ACTIVE;
 	}
 	
 	private int id;
 	
 	private String username;
 	
 	private String firstName;
 	
 	private String lastName;
 	
 	private String initials;
 	
 	private String affiliation;
 	
 	private boolean sisUser;
 	
 	private boolean rapidlistUser;
 	
 	private String email;
 	
 	private java.util.Set<WorkingSet> subscribedWorkingSets = new java.util.HashSet<WorkingSet>();
 	
 	private java.util.Set<PermissionGroup> permissionGroups = new java.util.HashSet<PermissionGroup>();
 	
 	private java.util.Set<WorkingSet> ownedWorkingSets = new java.util.HashSet<WorkingSet>();
 	
 	private java.util.Set<Edit> edit = new java.util.HashSet<Edit>();
 	
 	
 	public void setId(int value) {
 		this.id = value;
 	}
 	
 	public int getId() {
 		return id;
 	}
 	
 	public int getORMID() {
 		return getId();
 	}
 	
 	public void setUsername(String value) {
 		this.username = value;
 	}
 	
 	public String getUsername() {
 		return username;
 	}
 	
 	public void setFirstName(String value) {
 		this.firstName = nullIfStated(value);
 	}
 	
 	public String getFirstName() {
 		return firstName;
 	}
 	
 	public void setLastName(String value) {
 		this.lastName = nullIfStated(value);
 	}
 	
 	public String getLastName() {
 		return lastName;
 	}
 	
 	public void setInitials(String value) {
 		this.initials = nullIfStated(value);
 	}
 	
 	public String getInitials() {
 		return initials;
 	}
 	
 	public void setAffiliation(String value) {
 		this.affiliation = nullIfStated(value);
 	}
 	
 	public String getAffiliation() {
 		return affiliation;
 	}
 	
 	public void setSisUser(boolean value) {
 		this.sisUser = value;
 	}
 	
 	public Boolean getSisUser() {
 		return sisUser;
 	}
 	
 	public void setRapidlistUser(Boolean value) {
 		this.rapidlistUser = value;
 	}
 	
 	public Boolean getRapidlistUser() {
 		return rapidlistUser;
 	}
 	
 	public void setEmail(String value) {
 		this.email = value;
 	}
 	
 	public String getEmail() {
 		return email;
 	}
 	
 	public void setSubscribedWorkingSets(java.util.Set<WorkingSet> value) {
 		this.subscribedWorkingSets = value;
 	}
 	
 	public java.util.Set<WorkingSet> getSubscribedWorkingSets() {
 		return subscribedWorkingSets;
 	}
 	
 	
 	public void setPermissionGroups(java.util.Set<PermissionGroup> value) {
 		this.permissionGroups = value;
 	}
 	
 	public java.util.Set<PermissionGroup> getPermissionGroups() {
 		return permissionGroups;
 	}
 	
 	
 	public void setOwnedWorkingSets(java.util.Set<WorkingSet> value) {
 		this.ownedWorkingSets = value;
 	}
 	
 	public java.util.Set<WorkingSet> getOwnedWorkingSets() {
 		return ownedWorkingSets;
 	}
 	
 	
 	public void setEdit(java.util.Set<Edit> value) {
 		this.edit = value;
 	}
 	
 	public java.util.Set<Edit> getEdit() {
 		return edit;
 	}
 		
 	
 	public String toString() {
 		return String.valueOf(getId());
 	}
 	
 	private String nullIfStated(String value) {
		return value == null || "null".equals(value) ? null : value;
 	}
 	
 }
