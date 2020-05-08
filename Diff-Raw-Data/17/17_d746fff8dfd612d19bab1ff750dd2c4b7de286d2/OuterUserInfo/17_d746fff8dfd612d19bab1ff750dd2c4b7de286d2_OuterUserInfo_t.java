 package com.lucas.designPatterns.adapterPattern;
 
 import java.util.Map;
 
 public class OuterUserInfo extends OuterUser implements IUserInfo {
 
 	/**
 	 * @uml.property name="baseInfo"
 	 */
 	private Map<String, String> baseInfo;
 	/**
 	 * @uml.property name="homeInfo"
 	 */
 	private Map<String, String> homeInfo;
 
 	/**
 	 * @uml.property name="officeInfo"
 	 */
 	private Map<String, String> officeInfo;
 
 	
 	public OuterUserInfo() {
 		super();
 		this.baseInfo = super.getUserBaseInfo();
 		this.homeInfo = super.getUserHomeInfo();
 		this.officeInfo = super.getUserOfficeInfo();
 	}
 
 	@Override
 	public String getUserName() {
 		// TODO Auto-generated method stub
 		String userName = this.baseInfo.get("userName");
 		System.out.println(userName);
 		return userName;
 	}
 
 	@Override
 	public String getHomeAddress() {
 		// TODO Auto-generated method stub
 		String homeAddress = this.homeInfo.get("homeAddress");
 		System.out.println(homeAddress);
 		return homeAddress;
 	}
 
 	@Override
 	public String getMobileNumber() {
 		// TODO Auto-generated method stub
 		String mobileNumber = this.baseInfo.get("mobileNumber");
 		System.out.println(mobileNumber);
 		return mobileNumber;
 	}
 
 	@Override
 	public String getOfficeTelNumber() {
 		// TODO Auto-generated method stub
 		String officeNumber = this.officeInfo.get("userOfficeNum");
 		System.out.println(officeNumber);
 		return officeNumber;
 	}
 
 	@Override
 	public String getJobPosition() {
 		// TODO Auto-generated method stub
 		String jobPosition = this.officeInfo.get("userPosition");
 		System.out.println(jobPosition);
 		return jobPosition;
 	}
 
 	@Override
 	public String getHomeTelNumber() {
 		// TODO Auto-generated method stub
 		String homeTelNumber = this.homeInfo.get("homeTelNumber");
 		System.out.println(homeTelNumber);
 		return homeTelNumber;
 	}
 
 	/**
 	 * Getter of the property <tt>baseInfo</tt>
 	 * 
 	 * @return Returns the baseInfo.
 	 * @uml.property name="baseInfo"
 	 */
	public Map<String ,String> getBaseInfo() {
 		return baseInfo;
 	}
 
 	/**
 	 * Setter of the property <tt>baseInfo</tt>
 	 * 
 	 * @param baseInfo
 	 *            The baseInfo to set.
 	 * @uml.property name="baseInfo"
 	 */
	public void setBaseInfo(Map<String ,String> baseInfo) {
 		this.baseInfo = baseInfo;
 	}
 
 	/**
 	 * Getter of the property <tt>homeInfo</tt>
 	 * 
 	 * @return Returns the homeInfo.
 	 * @uml.property name="homeInfo"
 	 */
	public Map<String ,String> getHomeInfo() {
 		return homeInfo;
 	}
 
 	/**
 	 * Setter of the property <tt>homeInfo</tt>
 	 * 
 	 * @param homeInfo
 	 *            The homeInfo to set.
 	 * @uml.property name="homeInfo"
 	 */
	public void setHomeInfo(Map<String ,String> homeInfo) {
 		this.homeInfo = homeInfo;
 	}
 
 	/**
 	 * Getter of the property <tt>officeInfo</tt>
 	 * 
 	 * @return Returns the officeInfo.
 	 * @uml.property name="officeInfo"
 	 */
	public Map<String ,String> getOfficeInfo() {
 		return officeInfo;
 	}
 
 	/**
 	 * Setter of the property <tt>officeInfo</tt>
 	 * 
 	 * @param officeInfo
 	 *            The officeInfo to set.
 	 * @uml.property name="officeInfo"
 	 */
	public void setOfficeInfo(Map<String ,String> officeInfo) {
 		this.officeInfo = officeInfo;
 	}
 
 }
