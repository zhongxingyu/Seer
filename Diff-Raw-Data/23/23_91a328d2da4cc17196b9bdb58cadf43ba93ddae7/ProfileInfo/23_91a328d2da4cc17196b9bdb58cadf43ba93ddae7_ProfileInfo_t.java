 package org.netvogue.server.webmvc.domain;
 
 import java.util.Set;
 
 public class ProfileInfo {
 	String name		 	= "";
 	String profileid 	= "";
 	String networkstatus= "";
 	String aboutus 		= "";
	ImageURLsResponse profilepic;
 	ContactInfo contactinfo ;
 	Set<ProductLine> productlines; 	//netvogue.productline 
 	Set<BrandsCarried> brandscarried;	//netvogue.brandscarried
 	
 	boolean status = false;
 	
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public ProfileInfo() {
 	}
 
 	public String getProfileid() {
 		return profileid;
 	}
 
 	public void setProfileid(String profileid) {
 		this.profileid = profileid;
 	}
 
	public ImageURLsResponse getProfilepic() {
 		return profilepic;
 	}
 
	public void setProfilepic(ImageURLsResponse profilepic) {
 		this.profilepic = profilepic;
 	}
 
 	public String getNetworkstatus() {
 		return networkstatus;
 	}
 
 	public void setNetworkstatus(String networkstatus) {
 		this.networkstatus = networkstatus;
 	}
 
 	public String getAboutus() {
 		return aboutus;
 	}
 
 	public void setAboutus(String aboutus) {
 		this.aboutus = aboutus;
 	}
 
 	public ContactInfo getContactinfo() {
 		return contactinfo;
 	}
 
 	public void setContactinfo(ContactInfo contactinfo) {
 		this.contactinfo = contactinfo;
 	}
 
 	public Set<ProductLine> getProductlines() {
 		return productlines;
 	}
 
 	public void setProductlines(Set<ProductLine> productlines) {
 		this.productlines = productlines;
 	}
 
 	public Set<BrandsCarried> getBrandscarried() {
 		return brandscarried;
 	}
 
 	public void setBrandscarried(Set<BrandsCarried> brandscarried) {
 		this.brandscarried = brandscarried;
 	}
 
 	public boolean getStatus() {
 		return status;
 	}
 
 	public void setStatus(boolean status) {
 		this.status = status;
 	}
 	
 }
