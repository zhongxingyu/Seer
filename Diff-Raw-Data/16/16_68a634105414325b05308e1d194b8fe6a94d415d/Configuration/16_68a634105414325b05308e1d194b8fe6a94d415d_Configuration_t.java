 package org.mdissjava.mdisscore.model.pojo;
 
 import javax.persistence.Column;
 import javax.persistence.Embeddable;
 
 @Embeddable
 public class Configuration {
 	
 	@Column(name="shownick")
 	private boolean showNick;
 	
 	@Column(name="showname")
	private boolean showName;
 	
 	@Column(name="showsurname")
 	private boolean showSurname;
 	
 	@Column(name="showemail")
 	private boolean showEmail;
 	
 	@Column(name="showbirthdate")
 	private boolean showBirthdate;
 	
 	@Column(name="showphone")
 	private boolean showPhone;
 	
 	@Column(name="showgender")
 	private boolean showGender;
 	
 	@Column(name="showcity")	
 	private boolean showCity;
 	
 	@Column(name="showcountry")
 	private boolean showCountry;
 	
 	@Column(name="showaddress")	
 	private boolean showAddress;	
 	
 	@Column(name="showzip")
 	private boolean showZip;	
 	
 	@Column(name="showcameras")
 	private boolean showCameras;
 	
 	@Column(name="isprivate")
 	private boolean isPrivate;
 	
 	public Configuration(){
 		showNick = true;
 		showName = true;
 		showSurname = true;
 		showEmail = false;
 		showBirthdate = false;
 		showPhone = false;
 		showGender = true;
 		showCity = false;
 		showCountry = false;
 		showAddress = false;
 		showZip = false;
 		showCameras = false;
 		isPrivate = false;
 	}	
 		
 	public boolean isShowNick() {
 		return showNick;
 	}
 	public void setShowNick(boolean showNick) {
 		this.showNick = showNick;
 	}
 	public boolean isShowName() {
 		return showName;
 	}
 	public void setShowName(boolean showName) {
 		this.showName = showName;
 	}
 	public boolean isShowSurname() {
 		return showSurname;
 	}
 	public void setShowSurname(boolean showSurname) {
 		this.showSurname = showSurname;
 	}
 	public boolean isShowEmail() {
 		return showEmail;
 	}
 	public void setShowEmail(boolean showEmail) {
 		this.showEmail = showEmail;
 	}
 	public boolean isShowBirthdate() {
 		return showBirthdate;
 	}
 	public void setShowBirthdate(boolean showBirthDate) {
 		showBirthdate = showBirthDate;
 	}
 	public boolean isShowPhone() {
 		return showPhone;
 	}
 	public void setShowPhone(boolean showPhone) {
 		this.showPhone = showPhone;
 	}
 	public boolean isShowGender() {
 		return showGender;
 	}
 	public void setShowGender(boolean showGender) {
 		this.showGender = showGender;
 	}
 	public boolean isShowCity() {
 		return showCity;
 	}
 	public void setShowCity(boolean showCity) {
 		this.showCity = showCity;
 	}
 	public boolean isShowCountry() {
 		return showCountry;
 	}
 	public void setShowCountry(boolean showCountry) {
 		this.showCountry = showCountry;
 	}
 	public boolean isShowAddress() {
 		return showAddress;
 	}
 	public void setShowAddress(boolean showAddress) {
 		this.showAddress = showAddress;
 	}
 	public boolean isShowZip() {
 		return showZip;
 	}
 	public void setShowZip(boolean showZip) {
 		this.showZip = showZip;
 	}
 	public boolean isShowCameras() {
 		return showCameras;
 	}
 	public void setShowCameras(boolean showCameras) {
 		this.showCameras = showCameras;
 	}
	public boolean isIsPrivate() {
 		return isPrivate;
 	}
	public void setIsPrivate(boolean isPrivate) {
 		this.isPrivate = isPrivate;
 	}	
 }
