 package com.mymed.model.data;
 
 /**
  * This class represent an user profile
  * 
  * @author lvanni
  */
 public final class MUserBean extends AbstractMBean {
 
 	// Unique Identifier of the user in myMed
 	private String mymedID = null;
 	// ID of the user in the social network
 	private String socialNetworkID = null;
 	// Name of the social network used by the user
 	private String socialNetworkName = null;
 	private String email = null;
 	private String name = null;
 	private String firstName = null;
 	private String lastName = null;
 	private String link = null;
	private long birthday;
 	private String hometown = null;
 	private String gender = null;
	private long lastConnection;
 	private String buddyListID = null;
 	private String subscribtionListID = null;
 	private String reputationID = null;
 	private String sessionID = null;
 	private String interactionListID = null;
 
 	private String profilePicture = null;
 
 	public MUserBean() {
 		super();
 	}
 
 	@Override
 	public String toString() {
 		return "User:\n" + super.toString();
 	}
 
 	public String getMymedID() {
 		return mymedID;
 	}
 
 	public void setMymedID(final String mymedID) {
 		this.mymedID = mymedID;
 	}
 
 	public String getSocialNetworkID() {
 		return socialNetworkID;
 	}
 
 	public void setSocialNetworkID(final String socialNetworkID) {
 		this.socialNetworkID = socialNetworkID;
 	}
 
 	public String getSocialNetworkName() {
 		return socialNetworkName;
 	}
 
 	public void setSocialNetworkName(final String socialNetworkName) {
 		this.socialNetworkName = socialNetworkName;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(final String name) {
 		this.name = name;
 	}
 
 	public String getFirstName() {
 		return firstName;
 	}
 
 	public void setFirstName(final String firstName) {
 		this.firstName = firstName;
 	}
 
 	public String getLastName() {
 		return lastName;
 	}
 
 	public void setLastName(final String lastName) {
 		this.lastName = lastName;
 	}
 
 	public String getLink() {
 		return link;
 	}
 
 	public void setLink(final String link) {
 		this.link = link;
 	}
 
	public long getBirthday() {
 		return birthday;
 	}
 
	public void setBirthday(final long birthday) {
 		this.birthday = birthday;
 	}
 
 	public String getHometown() {
 		return hometown;
 	}
 
 	public void setHometown(final String hometown) {
 		this.hometown = hometown;
 	}
 
 	public String getGender() {
 		return gender;
 	}
 
 	public void setGender(final String gender) {
 		this.gender = gender;
 	}
 
 	public String getProfilePicture() {
 		return profilePicture;
 	}
 
 	public void setProfilePicture(final String profilePicture) {
 		this.profilePicture = profilePicture;
 	}
 
 	public String getBuddyListID() {
 		return buddyListID;
 	}
 
 	public void setBuddyListID(final String buddyList) {
 		buddyListID = buddyList;
 	}
 
 	public String getSubscribtionListID() {
 		return subscribtionListID;
 	}
 
 	public void setSubscribtionListID(final String subscribtionList) {
 		subscribtionListID = subscribtionList;
 	}
 
 	public String getSessionID() {
 		return sessionID;
 	}
 
 	public void setSessionID(final String session) {
 		sessionID = session;
 	}
 
 	public String getEmail() {
 		return email;
 	}
 
 	public void setEmail(final String email) {
 		this.email = email;
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
 	 * @return the reputationID
 	 */
 	public String getReputationID() {
 		return reputationID;
 	}
 
 	/**
 	 * @param reputationID
 	 *            the reputationID to set
 	 */
 	public void setReputationID(final String reputationID) {
 		this.reputationID = reputationID;
 	}
 
 	/**
 	 * @return the interactionListID
 	 */
 	public String getInteractionListID() {
 		return interactionListID;
 	}
 
 	/**
 	 * @param interactionListID
 	 *            the interactionListID to set
 	 */
 	public void setInteractionListID(final String interactionListID) {
 		this.interactionListID = interactionListID;
 	}
 
 	@Override
 	public boolean equals(final Object object) {
 
 		boolean returnValue = true;
 
 		if (!(object instanceof MUserBean)) {
 			returnValue = false;
 		} else {
 			final MUserBean comparable = (MUserBean) object;
 
 			/*
 			 * We compare only a subsets of the field to check that two
 			 * MUserBean objects are the same
 			 */
			returnValue &= getBirthday() == comparable.getBirthday();
 			returnValue &= getEmail().equals(comparable.getEmail());
 			returnValue &= getFirstName().equals(comparable.getFirstName());
 			returnValue &= getGender().equals(comparable.getGender());
 			returnValue &= getLastName().equals(comparable.getLastName());
 			returnValue &= getMymedID().equals(comparable.getMymedID());
 			returnValue &= getName().equals(comparable.getName());
 		}
 
 		return returnValue;
 	}
 }
