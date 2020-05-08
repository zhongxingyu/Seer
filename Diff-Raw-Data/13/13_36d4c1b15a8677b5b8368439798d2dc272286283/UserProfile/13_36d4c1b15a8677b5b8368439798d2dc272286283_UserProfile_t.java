 package uk.frequency.glance.server.model.user;
 
 import java.util.Date;
 
 import javax.persistence.Embeddable;
import javax.persistence.Lob;
 
 @Embeddable
 public class UserProfile {
 
 	String firstName;
 	String middleName;
 	String fullName;
 	
 	String gender;
	
	@Lob String bio;
	@Lob String about;
 	String birthday;
 	
 	String hometown;
 	String location;
 	String locale;
 	Double timezone;
 	
 	String fbUsername;
 	String email;
 	String website;
 	String fbLink;
 	
 	String imageUrl;
 	String bgImageUrl;
 	
 	Date updatedTime;
 
 	public String getFirstName() {
 		return firstName;
 	}
 
 	public void setFirstName(String firstName) {
 		this.firstName = firstName;
 	}
 
 	public String getMiddleName() {
 		return middleName;
 	}
 
 	public void setMiddleName(String middleName) {
 		this.middleName = middleName;
 	}
 
 	public String getFullName() {
 		return fullName;
 	}
 
 	public void setFullName(String fullName) {
 		this.fullName = fullName;
 	}
 
 	public String getGender() {
 		return gender;
 	}
 
 	public void setGender(String gender) {
 		this.gender = gender;
 	}
 
 	public String getBio() {
 		return bio;
 	}
 
 	public void setBio(String bio) {
 		this.bio = bio;
 	}
 
 	public String getAbout() {
 		return about;
 	}
 
 	public void setAbout(String about) {
 		this.about = about;
 	}
 
 	public String getBirthday() {
 		return birthday;
 	}
 
 	public void setBirthday(String birthday) {
 		this.birthday = birthday;
 	}
 
 	public String getHometown() {
 		return hometown;
 	}
 
 	public void setHometown(String hometown) {
 		this.hometown = hometown;
 	}
 
 	public String getLocation() {
 		return location;
 	}
 
 	public void setLocation(String location) {
 		this.location = location;
 	}
 
 	public String getLocale() {
 		return locale;
 	}
 
 	public void setLocale(String locale) {
 		this.locale = locale;
 	}
 
 	public Double getTimezone() {
 		return timezone;
 	}
 
 	public void setTimezone(Double timezone) {
 		this.timezone = timezone;
 	}
 
 	public String getFbUsername() {
 		return fbUsername;
 	}
 
 	public void setFbUsername(String fbUsername) {
 		this.fbUsername = fbUsername;
 	}
 
 	public String getEmail() {
 		return email;
 	}
 
 	public void setEmail(String email) {
 		this.email = email;
 	}
 
 	public String getWebsite() {
 		return website;
 	}
 
 	public void setWebsite(String website) {
 		this.website = website;
 	}
 
 	public String getFbLink() {
 		return fbLink;
 	}
 
 	public void setFbLink(String fbLink) {
 		this.fbLink = fbLink;
 	}
 
 	public String getImageUrl() {
 		return imageUrl;
 	}
 
 	public void setImageUrl(String imageUrl) {
 		this.imageUrl = imageUrl;
 	}
 
 	public String getBgImageUrl() {
 		return bgImageUrl;
 	}
 
 	public void setBgImageUrl(String bgImageUrl) {
 		this.bgImageUrl = bgImageUrl;
 	}
 
 	public Date getUpdatedTime() {
 		return updatedTime;
 	}
 
 	public void setUpdatedTime(Date updatedTime) {
 		this.updatedTime = updatedTime;
 	}
 
 	@Override
 	public String toString() {
 		return fullName;
 	}
 	
 }
