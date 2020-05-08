 package com.russia.meetster;
 
 import java.util.ArrayList;
 import java.util.Date;
 
 import android.content.ContentValues;
 import android.location.Location;
 
 public class MeetsterEvent extends YLSQLRow {
 	private Long id;
 	private Long creatorId;
 	
 	// We also plan to allow users to just enter a colloquial location description
 	private Location location;
 	private String locationDescription;
 	
 	private MeetsterCategory category;
 	private String description;
 	
 	private Date creationTime;
 	
 	// Length 2 array: {start, end}.
 	private Date[] timeRange;
 	
 	private ArrayList<MeetsterFriend> invitees;
 	
 	public MeetsterEvent(Long id, Long creatorId, Location location,
 			String locationDescription, MeetsterCategory category,
 			String description, Date creationTime, Date[] timeRange,
 			ArrayList<MeetsterFriend> invitees) {
 		this.id = id;
 		this.creatorId = creatorId;
 		this.location = location;
 		this.locationDescription = locationDescription;
 		this.category = category;
 		this.description = description;
 		this.creationTime = creationTime;
 		this.timeRange = timeRange;
 		this.invitees = invitees;
 	}
 	
 	private String inviteesToString() {
 		if (this.invitees == null) {
 			return null;
 		}
 		String inviteesString = "";
 		for (int i = 0; i < this.invitees.size(); ++i) {
 			inviteesString += i;
 			if (i < this.invitees.size() - 1) {
 				inviteesString += ",";
 			}
 		}
 		return inviteesString;
 	}
 	
 	private Long getCategoryId() {
 		return (this.category == null) ? null : this.category.getId();
 	}
 	
 	public ContentValues toValues() {
 		ContentValues vals = new ContentValues();
 		
 		vals.put(MeetsterContract.Events.CREATORID, this.creatorId);
 		vals.put(MeetsterContract.Events.CATEGORY, this.getCategoryId());
 		vals.put(MeetsterContract.Events.INVITEE_IDS, inviteesToString());
 		vals.put(MeetsterContract.Events.DESCRIPTION, this.description);
 		vals.put(MeetsterContract.Events.START_TIME, dateToSQLTimestamp(getStartTime()));
 		vals.put(MeetsterContract.Events.END_TIME, dateToSQLTimestamp(getEndTime()));
		if (hasLocation()) {
			vals.put(MeetsterContract.Events.LATITUDE, getLatitude());
			vals.put(MeetsterContract.Events.LONGITUDE, getLongitude());
		}
 		vals.put(MeetsterContract.Events.LOCATION_DESCRIPTION, this.locationDescription);
 
 		return vals;
 	}
 
 	public ArrayList<MeetsterFriend> getInvitees() {
 		return invitees;
 	}
 	
 	public void setInvitees(ArrayList<MeetsterFriend> invitees) {
 		this.invitees = invitees;
 	}
 	
 	public long getId() {
 		return id;
 	}
 
 	public void setId(long id) {
 		this.id = id;
 	}
 
 	public long getCreatorId() {
 		return creatorId;
 	}
 
 	public void setCreatorId(long creatorId) {
 		this.creatorId = creatorId;
 	}
 
 	public Location getLocation() {
 		return location;
 	}
 
 	public void setLocation(Location location) {
 		this.location = location;
 	}
 	
	public boolean hasLocation() {
		return (this.location != null);
	}
	
 	public Double getLatitude() {
 		return (this.location == null) ? null : this.location.getLatitude();
 	}
 	
 	public Double getLongitude() {
 		return (this.location == null) ? null : this.location.getLongitude();
 	}
 
 	public String getLocationDescription() {
 		return locationDescription;
 	}
 
 	public void setLocationDescription(String locationDescription) {
 		this.locationDescription = locationDescription;
 	}
 
 	public MeetsterCategory getCategory() {
 		return category;
 	}
 
 	public void setCategory(MeetsterCategory category) {
 		this.category = category;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public Date getCreationTime() {
 		return creationTime;
 	}
 
 	public void setCreationTime(Date creationTime) {
 		this.creationTime = creationTime;
 	}
 
 	public Date[] getTimeRange() {
 		return timeRange;
 	}
 
 	public void setTimeRange(Date[] timeRange) {
 		this.timeRange = timeRange;
 	}
 	
 	public Date getStartTime() {
 		return (timeRange == null) ? null : timeRange[0];
 	}
 	
 	public Date getEndTime() {
 		return (timeRange == null) ? null : timeRange[1];
 	}
 }
