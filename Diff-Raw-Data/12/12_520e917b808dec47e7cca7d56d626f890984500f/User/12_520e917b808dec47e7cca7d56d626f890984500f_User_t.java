 package com.sound.model;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bson.types.ObjectId;
 import org.codehaus.jackson.annotate.JsonIgnore;
 import org.codehaus.jackson.annotate.JsonIgnoreProperties;
 
 import com.github.jmkgreen.morphia.annotations.Embedded;
 import com.github.jmkgreen.morphia.annotations.Entity;
 import com.github.jmkgreen.morphia.annotations.Id;
 import com.github.jmkgreen.morphia.annotations.Reference;
 import com.github.jmkgreen.morphia.annotations.Serialized;
 import com.sound.model.enums.GenderEnum;
 
 @Entity(noClassnameStored= true)
 @JsonIgnoreProperties(ignoreUnknown = true)
 public class User extends BaseModel
 {
 	@Id 
 	private ObjectId id;
 
 	@Embedded
 	private UserProfile profile;
 
 	@Embedded
 	private UserExternal external;
 
 	@Embedded
 	private List<UserEmail> emails;
 
 	@Reference(lazy=true)
 	private UserAuth auth;
 
 	@Reference(lazy=true)
 	private List<Group> groups;
 	
 	@Reference(lazy=true)
 	private UserSocial social;
 
 	@JsonIgnore
 	public ObjectId getId() {
 		return id;
 	}
 
 	public void setId(ObjectId id) {
 		this.id = id;
 	}
 
 	public UserProfile getProfile() {
 		return profile;
 	}
 
 	public void setProfile(UserProfile profile) {
 		this.profile = profile;
 	}
 
 	public List<Group> getGroups() {
 		return groups;
 	}
 
 	public void setGroups(List<Group> groups) {
 		this.groups = groups;
 	}
 
 	public UserExternal getExternal() {
 		return external;
 	}
 	
 
 	public UserSocial getSocial() {
 		return social;
 	}
 
 	public void setSocial(UserSocial social) {
 		this.social = social;
 	}
 
 	public void setExternal(UserExternal external) {
 		this.external = external;
 	}
 
 	public List<UserEmail> getEmails() {
 		return emails;
 	}
 
 	public void setEmails(List<UserEmail> emails) {
 		this.emails = emails;
 	}
 
 	public void addEmail(UserEmail email)
 	{
 		this.emails = (null == this.emails)? new ArrayList<UserEmail>() : this.emails;
 		this.emails.add(email);
 	}
 
 	public UserAuth getAuth() {
 		return auth;
 	}
 
 	public void setAuth(UserAuth auth) {
 		this.auth = auth;
 	}
 	
 	public void addGroup(Group group)
 	{
 		this.groups = (null == this.groups)? new ArrayList<Group>():this.groups;
 		
 		for(Group oneGroup: this.groups)
 		{
 			if (oneGroup == group)
 			{
 				return ;
 			}
 		}
 		this.groups.add(group);
 	}
 	
 	public void removeGroup(Group group)
 	{
 		this.groups = (null == this.groups)? new ArrayList<Group>():this.groups;
 		this.groups.remove(group);
 	}
 
 	@Entity
 	public static class UserProfile
 	{
 		@Embedded
 		private ProfileAvator avator;
 		private String alias;
 		private String password;
 		private String firstName;
 		private String lastName;
 		private String location;
 		private String description;
 		private int age;
 		private boolean gender;
 
 		@JsonIgnore
 		public String getPassword() {
 			return password;
 		}
 
 		public void setPassword(String password) {
 			this.password = password;
 		}
 
 		public ProfileAvator getAvator() {
 			return avator;
 		}
 
 		public void setAvator(ProfileAvator avator) {
 			this.avator = avator;
 		}
 
 		public String getAlias() {
 			return alias;
 		}
 
 		public void setAlias(String alias) {
 			this.alias = alias;
 		}
 
 		public String getFirstName() {
 			return firstName;
 		}
 
 		public void setFirstName(String firstName) {
 			this.firstName = firstName;
 		}
 
 		public String getLastName() {
 			return lastName;
 		}
 
 		public void setLastName(String lastName) {
 			this.lastName = lastName;
 		}
 
 		public String getLocation() {
 			return location;
 		}
 
 		public void setLocation(String location) {
 			this.location = location;
 		}
 
 		public int getAge() {
 			return age;
 		}
 
 		public String getDescription() {
 			return description;
 		}
 
 		public void setDescription(String description) {
 			this.description = description;
 		}
 
 		public void setAge(int age) {
 			this.age = age;
 		}
 
 		public String getGender() {
 			return GenderEnum.getGenderName(gender);
 		}
 
 		public void setGender(String gender) {
 			this.gender = GenderEnum.getGenderValue(gender);
 		}
 
 		public class ProfileAvator
 		{
 			@Serialized
 			private byte[] avator;
 
 			private String extension;
 
 			public byte[] getAvator() {
 				return avator;
 			}
 
 			public void setAvator(byte[] avator) {
 				this.avator = avator;
 			}
 
 			public String getExtension() {
 				return extension;
 			}
 
 			public void setExtension(String extension) {
 				this.extension = extension;
 			}
 
 		}
 
 		@Override
 		public int hashCode() {
 			final int prime = 31;
 			int result = 1;
 			result = prime * result + ((alias == null) ? 0 : alias.hashCode());
 			return result;
 		}
 
 		@Override
 		public boolean equals(Object obj) {
 			if (this == obj)
 				return true;
 			if (obj == null)
 				return false;
 			if (getClass() != obj.getClass())
 				return false;
 			UserProfile other = (UserProfile) obj;
 			if (alias == null) {
 				if (other.alias != null)
 					return false;
 			} else if (!alias.equals(other.alias))
 				return false;
 			return true;
 		}
 		
 	}
 	
 	@Entity
 	public static class UserSocial
 	{
 
		@Id
		private ObjectId id;
		
 		private Long following;
 
 		private Long followed;
 
		public ObjectId getId() {
			return id;
		}

		public void setId(ObjectId id) {
			this.id = id;
		}

 		public Long getFollowing() {
 			return following;
 		}
 
 		public void setFollowing(Long following) {
 			this.following = following;
 		}
 
 		public Long getFollowed() {
 			return followed;
 		}
 
 		public void setFollowed(Long followed) {
 			this.followed = followed;
 		}
 	}
 	
 	public static class UserExternal
 	{
 	}
 
 	public static class UserEmail
 	{
 		private String emailAddress;
 
 		public String getEmailAddress() {
 			return emailAddress;
 		}
 
 		public void setEmailAddress(String emailAddress) {
 			this.emailAddress = emailAddress;
 		}
 
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((profile == null) ? 0 : profile.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		User other = (User) obj;
 		if (profile == null) {
 			if (other.profile != null)
 				return false;
 		} else if (!profile.equals(other.profile))
 			return false;
 		return true;
 	}
  
 }
