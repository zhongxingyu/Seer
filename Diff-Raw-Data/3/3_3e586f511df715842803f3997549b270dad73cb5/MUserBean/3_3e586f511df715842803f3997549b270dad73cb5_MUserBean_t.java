 package com.mymed.model.data.user;
 
 import com.mymed.model.data.AbstractMBean;
 
 /**
  * This class represent an user profile
  * 
  * @author lvanni
  */
 public final class MUserBean extends AbstractMBean {
 
 	/* --------------------------------------------------------- */
 	/* Attributes */
 	/* --------------------------------------------------------- */
 	/**
 	 * Used for the hash code
 	 */
 	private static final int PRIME = 31;
 
 	/** USER_ID */
 	private String id = null;
 	/** AUTHENTICATION_ID */
 	private String login = null;
 	private String email = null;
 	private String name = null;
 	private String firstName = null;
 	private String lastName = null;
 	private String link = null;
 	private String birthday;
 	private String hometown = null;
 	private String gender = null;
 	private long lastConnection;
 	private String profilePicture = null;
 	/** USER_LIST_ID */
 	private String buddyList = null;
 	/** APPLICATION_LIST_ID */
 	private String subscribtionList = null;
 	/** REPUTATION_ID */
 	private String reputation = null;
 	/** SESSION_ID || null */
 	private String session = null;
 	/** INTERACTION_LIST_ID */
 	private String interactionList = null;
 	private String socialNetworkID = null;
 	private String socialNetworkName = null;
 
 	/* --------------------------------------------------------- */
 	/* Constructors */
 	/* --------------------------------------------------------- */
 	/**
 	 * Copy constructor.
 	 * <p>
 	 * Provide a clone of the passed MUserBean
 	 * 
 	 * @param toClone
 	 *            the user bean to clone
 	 */
 	protected MUserBean(final MUserBean toClone) {
 		super();
 
 		id = toClone.getId();
 		login = toClone.getLogin();
 		email = toClone.getEmail();
 		name = toClone.getName();
 		firstName = toClone.getFirstName();
 		lastName = toClone.getLastName();
 		link = toClone.getLink();
 		birthday = toClone.getBirthday();
 		hometown = toClone.getHometown();
 		gender = toClone.getGender();
 		lastConnection = toClone.getLastConnection();
 		buddyList = toClone.getBuddyList();
 		subscribtionList = toClone.getSubscribtionList();
 		reputation = toClone.getReputation();
 		session = toClone.getSession();
 		interactionList = toClone.getInteractionList();
 		socialNetworkID = toClone.getSocialNetworkID();
 		socialNetworkName = toClone.getSocialNetworkName();
 	}
 
 	@Override
 	public MUserBean clone() {
		return new MUserBean(this);
 	}
 
 	/**
 	 * Create a new empty MUserBean
 	 */
 	public MUserBean() {
 		// Empty constructor, needed because of the copy constructor
 		super();
 	}
 
 	/* --------------------------------------------------------- */
 	/* Override methods */
 	/* --------------------------------------------------------- */
 	/**
 	 * @see java.lang.Object#equals()
 	 */
 	@Override
 	public boolean equals(final Object object) {
 
 		boolean equal = false;
 
 		if (this == object) {
 			equal = true;
 		} else if (object instanceof MUserBean) {
 			final MUserBean comparable = (MUserBean) object;
 
 			/*
 			 * We compare only a subsets of the field to check that two
 			 * MUserBean objects are the same. These should be values that are
 			 * set for sure, and not null.
 			 */
 			equal = true;
 
 			if (email == null && comparable.getEmail() != null) {
 				equal &= false;
 			} else {
 				equal &= email.equals(comparable.getEmail());
 			}
 
 			if (firstName == null && comparable.getFirstName() != null) {
 				equal &= false;
 			} else {
 				equal &= firstName.equals(comparable.getFirstName());
 			}
 
 			if (lastName == null && comparable.getLastName() != null) {
 				equal &= false;
 			} else {
 				equal &= lastName.equals(comparable.getLastName());
 			}
 
 			if (id == null && comparable.getId() != null) {
 				equal &= false;
 			} else {
 				equal &= id.equals(comparable.getId());
 			}
 
 			if (name == null && comparable.getName() != null) {
 				equal &= false;
 			} else {
 				equal &= name.equals(comparable.getName());
 			}
 		}
 
 		return equal;
 	}
 
 	/**
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		int result = 1;
 
 		result = PRIME * result + (email == null ? 0 : email.hashCode());
 		result = PRIME * result + (firstName == null ? 0 : firstName.hashCode());
 		result = PRIME * result + (lastName == null ? 0 : lastName.hashCode());
 		result = PRIME * result + (id == null ? 0 : id.hashCode());
 		result = PRIME * result + (name == null ? 0 : name.hashCode());
 
 		return result;
 	}
 
 	@Override
 	public String toString() {
 		return "User:\n" + super.toString();
 	}
 
 	/* --------------------------------------------------------- */
 	/* GETTER AND SETTER */
 	/* --------------------------------------------------------- */
 	/**
 	 * @return the id
 	 */
 	public String getId() {
 		return id;
 	}
 
 	/**
 	 * @param id
 	 *            the id to set
 	 */
 	public void setId(final String id) {
 		this.id = id;
 	}
 
 	/**
 	 * @return the login
 	 */
 	public String getLogin() {
 		return login;
 	}
 
 	/**
 	 * @param login
 	 *            the login to set
 	 */
 	public void setLogin(final String login) {
 		this.login = login;
 	}
 
 	/**
 	 * @return the email
 	 */
 	public String getEmail() {
 		return email;
 	}
 
 	/**
 	 * @param email
 	 *            the email to set
 	 */
 	public void setEmail(final String email) {
 		this.email = email;
 	}
 
 	/**
 	 * @return the name
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * @param name
 	 *            the name to set
 	 */
 	public void setName(final String name) {
 		this.name = name;
 	}
 
 	/**
 	 * @return the firstName
 	 */
 	public String getFirstName() {
 		return firstName;
 	}
 
 	/**
 	 * @param firstName
 	 *            the firstName to set
 	 */
 	public void setFirstName(final String firstName) {
 		this.firstName = firstName;
 	}
 
 	/**
 	 * @return the lastName
 	 */
 	public String getLastName() {
 		return lastName;
 	}
 
 	/**
 	 * @param lastName
 	 *            the lastName to set
 	 */
 	public void setLastName(final String lastName) {
 		this.lastName = lastName;
 	}
 
 	/**
 	 * @return the link
 	 */
 	public String getLink() {
 		return link;
 	}
 
 	/**
 	 * @param link
 	 *            the link to set
 	 */
 	public void setLink(final String link) {
 		this.link = link;
 	}
 
 	/**
 	 * @return the birthday
 	 */
 	public String getBirthday() {
 		return birthday;
 	}
 
 	/**
 	 * @param birthday
 	 *            the birthday to set
 	 */
 	public void setBirthday(final String birthday) {
 		this.birthday = birthday;
 	}
 
 	/**
 	 * @return the hometown
 	 */
 	public String getHometown() {
 		return hometown;
 	}
 
 	/**
 	 * @param hometown
 	 *            the hometown to set
 	 */
 	public void setHometown(final String hometown) {
 		this.hometown = hometown;
 	}
 
 	/**
 	 * @return the gender
 	 */
 	public String getGender() {
 		return gender;
 	}
 
 	/**
 	 * @param gender
 	 *            the gender to set
 	 */
 	public void setGender(final String gender) {
 		this.gender = gender;
 	}
 
 	/**
 	 * @return the lastConnection
 	 */
 	public long getLastConnection() {
 		return lastConnection;
 	}
 
 	/**
 	 * @param lastConnection
 	 *            the lastConnection to set
 	 */
 	public void setLastConnection(final long lastConnection) {
 		this.lastConnection = lastConnection;
 	}
 
 	/**
 	 * 
 	 * @return the profile picture
 	 */
 	public String getProfilePicture() {
 		return profilePicture;
 	}
 
 	/**
 	 * 
 	 * @param profilePicture
 	 */
 	public void setProfilePicture(final String profilePicture) {
 		this.profilePicture = profilePicture;
 	}
 
 	/**
 	 * @return the buddyList
 	 */
 	public String getBuddyList() {
 		return buddyList;
 	}
 
 	/**
 	 * @param buddyList
 	 *            the buddyList to set
 	 */
 	public void setBuddyList(final String buddyList) {
 		this.buddyList = buddyList;
 	}
 
 	/**
 	 * @return the subscriptionList
 	 */
 	public String getSubscribtionList() {
 		return subscribtionList;
 	}
 
 	/**
 	 * @param subscribtionList
 	 *            the subscriptionList to set
 	 */
 	public void setSubscribtionList(final String subscribtionList) {
 		this.subscribtionList = subscribtionList;
 	}
 
 	/**
 	 * @return the reputation
 	 */
 	public String getReputation() {
 		return reputation;
 	}
 
 	/**
 	 * @param reputation
 	 *            the reputation to set
 	 */
 	public void setReputation(final String reputation) {
 		this.reputation = reputation;
 	}
 
 	/**
 	 * @return the session
 	 */
 	public String getSession() {
 		return session;
 	}
 
 	/**
 	 * @param session
 	 *            the session to set
 	 */
 	public void setSession(final String session) {
 		this.session = session;
 	}
 
 	/**
 	 * @return the interactionList
 	 */
 	public String getInteractionList() {
 		return interactionList;
 	}
 
 	/**
 	 * @param interactionList
 	 *            the interactionList to set
 	 */
 	public void setInteractionList(final String interactionList) {
 		this.interactionList = interactionList;
 	}
 
 	/**
 	 * @return the socialNetworkID
 	 */
 	public String getSocialNetworkID() {
 		return socialNetworkID;
 	}
 
 	/**
 	 * @param socialNetworkID
 	 *            the socialNetworkID to set
 	 */
 	public void setSocialNetworkID(final String socialNetworkID) {
 		this.socialNetworkID = socialNetworkID;
 	}
 
 	/**
 	 * @return the socialNetworkName
 	 */
 	public String getSocialNetworkName() {
 		return socialNetworkName;
 	}
 
 	/**
 	 * @param socialNetworkName
 	 *            the socialNetworkName to set
 	 */
 	public void setSocialNetworkName(final String socialNetworkName) {
 		this.socialNetworkName = socialNetworkName;
 	}
 }
