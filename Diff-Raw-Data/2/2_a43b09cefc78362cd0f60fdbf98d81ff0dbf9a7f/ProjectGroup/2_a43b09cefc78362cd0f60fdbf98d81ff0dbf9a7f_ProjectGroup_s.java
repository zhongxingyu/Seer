 package com.svend.dab.core.beans.groups;
 
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 import org.springframework.data.annotation.Transient;
 
 import com.google.common.base.Strings;
 import com.svend.dab.core.beans.Location;
 import com.svend.dab.core.beans.PhotoAlbum;
 import com.svend.dab.core.beans.groups.GroupParticipant.ROLE;
 import com.svend.dab.core.beans.projects.Project.STATUS;
 import com.svend.dab.core.beans.projects.SelectedTheme;
 
 import controllers.BeanProvider;
 
 public class ProjectGroup {
 
	private static final String DEFAULT_GROUP_IMAGE = "/defaultGroupImage.jpg";;
 
 	private String id;
 
 	private boolean isActive = true;
 	private String name;
 	private String description;
 	private Date creationDate;
 
 	private List<Location> location;
 	private List<SelectedTheme> themes;
 	private List<String> tags;
 
 	private List<GroupParticipant> participants;
 	
 	@Transient
 	private List<GroupParticipant> activeParticipants;
 	
 	private List<GroupProjectParticipant> projectParticipants;
 	
 	@Transient
 	private List<GroupProjectParticipant> startedProjectParticipants;
 
 	private PhotoAlbum photoAlbum;
 	
 	// ////////////////////
 
 	public ProjectGroup() {
 		super();
 	}
 
 	public ProjectGroup(String id, String name, String description, List<Location> location, List<SelectedTheme> themes, List<String> tags, Date creationDate) {
 		super();
 		this.id = id;
 		this.name = name;
 		this.description = description;
 		this.location = location;
 		this.themes = themes;
 		this.tags = tags;
 		this.creationDate = creationDate;
 	}
 
 	// ////////////////////////////////////////
 
 	public void generatePhotoLinks(Date expirationdate) {
 
 		getPhotoAlbum().generatePhotoLinks(expirationdate);
 		
 		if (participants != null) {
 			for (GroupParticipant participant : participants) {
 				participant.generatePhotoLinks(expirationdate);
 			}
 		}
 		
 		if (projectParticipants != null) {
 			for (GroupProjectParticipant projectParticipant: projectParticipants) {
 				projectParticipant.getProjet().generatePhotoLink(expirationdate);
 			}
 		}
 	}
 	
 
 	/**
 	 * @return the {@link PhotoAlbum} for this project (never null)
 	 */
 	public PhotoAlbum getPhotoAlbum() {
 		if (photoAlbum == null) {
 			synchronized (this) {
 				if (photoAlbum == null) {
 					photoAlbum = new PhotoAlbum();
 				}
 			}
 		}
 		
 		// setting the transient properties of the photo album every time
 		photoAlbum.setPhotoS3RootFolder("/groups/" + id + "/photos/");
 		photoAlbum.setThumbS3RootFolder("/groups/" + id + "/thumbs/");
 		photoAlbum.setMaxNumberOfPhotos(BeanProvider.getConfig().getMaxNumberOfPhotosInGroup());
 		photoAlbum.setDefaultMainPhoto(BeanProvider.getResourceLocator().getDabImagesPath() + DEFAULT_GROUP_IMAGE);
 		
 		return photoAlbum;
 	}
 
 	
 
 	public ROLE findRoleOfUser(String userId) {
 
 		// TODO: add some caching here (this method is queried several times for each page refresh, for the same user)
 
 		if (Strings.isNullOrEmpty(userId) || participants == null || participants.isEmpty()) {
 			return null;
 		}
 
 		for (GroupParticipant participant : participants) {
 			// user not yet accepted are still concidered with no role
 			if (userId.equals(participant.getUser().getUserName()) && participant.isAccepted()) {
 				return participant.getRole();
 			}
 		}
 
 		return null;
 	}
 
 	public void updateUserParticipantRole(String upgradedUser, ROLE role) {
 
 		if (!Strings.isNullOrEmpty(upgradedUser)) {
 			for (GroupParticipant participant : participants) {
 				// user not yet accepted are still concidered with no role
 				if (upgradedUser.equals(participant.getUser().getUserName())) {
 					participant.setRole(role);
 					return;
 				}
 			}
 		}
 	}
 
 	public void removeParticipant(String removedUser) {
 
 		if (removedUser != null) {
 
 			GroupParticipant removed = null;
 			for (GroupParticipant participant : participants) {
 				// user not yet accepted are still concidered with no role
 				if (removedUser.equals(participant.getUser().getUserName())) {
 					removed = participant;
 				}
 			}
 			
 			if (removed != null) {
 				participants.remove(removed);
 			}
 		}
 	}
 
 	public boolean hasAppliedForGroupMembership(String userId) {
 
 		if (Strings.isNullOrEmpty(userId) || participants == null || participants.isEmpty()) {
 			return false;
 		}
 
 		for (GroupParticipant participant : participants) {
 			// user not yet accepted are still concidered with no role
 			if (userId.equals(participant.getUser().getUserName()) && !participant.isAccepted()) {
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	public int getNumberOfAdmins() {
 
 		if (participants == null || participants.isEmpty()) {
 			return 0;
 		}
 
 		int numberOfAdmins = 0;
 
 		for (GroupParticipant participant : participants) {
 			if (participant.getRole() == ROLE.admin) {
 				numberOfAdmins++;
 			}
 		}
 
 		return numberOfAdmins;
 	}
 
 	public boolean isMemberOrHasALreadyApplied(String userId) {
 
 		if (Strings.isNullOrEmpty(userId) || participants == null || participants.isEmpty()) {
 			return false;
 		}
 
 		for (GroupParticipant participant : participants) {
 			// user not yet accepted are still concidered with no role
 			if (userId.equals(participant.getUser().getUserName())) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public void addParticipant(GroupParticipant groupParticipant) {
 		if (participants == null) {
 			participants = new LinkedList<GroupParticipant>();
 		}
 		participants.add(groupParticipant);
 	}
 
 	public int getNumberOfParticipants() {
 		if (participants == null) {
 			return 0;
 		} else {
 			int numberOfAcceptedParticipants = 0;
 
 			for (GroupParticipant participant : participants) {
 				if (participant.isAccepted()) {
 					numberOfAcceptedParticipants++;
 				}
 			}
 
 			return numberOfAcceptedParticipants;
 		}
 	}
 
 	public int getNumberOfProjects() {
 		return 0;
 	}
 	
 	public GroupProjectParticipant findProjectParticipant(String projectId) {
 		
 		if (Strings.isNullOrEmpty(projectId) || projectParticipants == null) {
 			return null;
 		} else {
 			for (GroupProjectParticipant participant : projectParticipants) {
 				if (projectId.equals(participant.getProjet().getProjectId())) {
 					return participant;
 				}
 			}
 		}
 		return null;
 	}
 
 	
 
 	public void replaceLocations(Set<Location> newLocations) {
 		if (location == null) {
 			location = new LinkedList<Location>();
 		} else {
 			location.clear();
 		}
 		location.addAll(newLocations);
 	}
 
 	public void replaceThemes(Set<SelectedTheme> newThemes) {
 		if (themes == null) {
 			themes = new LinkedList<SelectedTheme>();
 		} else {
 			themes.clear();
 		}
 		themes.addAll(newThemes);
 	}
 
 	public void replaceTags(Set<String> newTags) {
 		if (tags == null) {
 			tags = new LinkedList<String>();
 		} else {
 			tags.clear();
 		}
 		tags.addAll(newTags);
 	}
 	
 	
 	
 	
 	public boolean isProjectMemberOfGroupOrHasAlreadyApplied(String projectId) {
 		
 		if (Strings.isNullOrEmpty(projectId)) {
 			return false;
 		}
 		
 		if (projectParticipants == null) {
 			return false;
 		}
 		
 		for (GroupProjectParticipant participant : projectParticipants) {
 			if (projectId.equals(participant.getProjet().getProjectId())) {
 				return true;
 			}
 		}
 		
 		return false;
 	}
 
 
 	// /////////////////////////////////
 
 	public String getId() {
 		return id;
 	}
 
 	public void setId(String id) {
 		this.id = id;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public List<Location> getLocation() {
 		return location;
 	}
 
 	public void setLocation(List<Location> location) {
 		this.location = location;
 	}
 
 	public List<SelectedTheme> getThemes() {
 		return themes;
 	}
 
 	public void setThemes(List<SelectedTheme> themes) {
 		this.themes = themes;
 	}
 
 	public List<String> getTags() {
 		return tags;
 	}
 
 	public void setTags(List<String> tags) {
 		this.tags = tags;
 	}
 
 	public List<GroupParticipant> getParticipants() {
 		return participants;
 	}
 
 	public void setParticipants(List<GroupParticipant> participants) {
 		this.participants = participants;
 	}
 	
 	public List<GroupParticipant> getActiveParticipants() {
 		if (activeParticipants == null) {
 			activeParticipants = new LinkedList<GroupParticipant>();
 			
 			if (participants != null) {
 				for (GroupParticipant participant : participants) {
 					if (participant.getUser().isProfileActive()) {
 						activeParticipants.add(participant);
 					}
 				}
 			}
 		}
 		
 		return activeParticipants;
 	}
 	
 
 	public Date getCreationDate() {
 		return creationDate;
 	}
 
 	public void setCreationDate(Date creationDate) {
 		this.creationDate = creationDate;
 	}
 
 	public boolean isActive() {
 		return isActive;
 	}
 
 	public void setActive(boolean isActive) {
 		this.isActive = isActive;
 	}
 
 	public List<GroupProjectParticipant> getProjectParticipants() {
 		return projectParticipants;
 	}
 	
 	public void setProjectParticipants(List<GroupProjectParticipant> projectParticipants) {
 		this.projectParticipants = projectParticipants;
 	}
 	
 	public List<GroupProjectParticipant> getStartedProjectParticipants() {
 		
 		if (startedProjectParticipants == null) {
 			startedProjectParticipants = new LinkedList<GroupProjectParticipant>();
 			
 			if (projectParticipants != null) {
 				for (GroupProjectParticipant participant : projectParticipants) {
 					if (participant.getProjet().getStatus() == STATUS.started) {
 						startedProjectParticipants.add(participant);
 					}
 				}
 			}
 		}
 		
 		return startedProjectParticipants;
 	}
 
 }
