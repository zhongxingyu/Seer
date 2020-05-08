 /*
  * Copyright (C) 2013 IEL of ISCAS
  * Project:CSDTP
  * Author: voidmain
  * Create Date: Apr 23, 201311:43:59 PM
  */
 package cn.ac.iscas.iel.csdtp.data;
 
 import java.util.Date;
 
 import cn.ac.iscas.iel.csdtp.controller.Device;
 
 /**
  * The frame that will be sent to server side
  * 
  * @Project CSDTP
  * @Package cn.ac.iscas.iel.csdtp.data
  * @Class Frame
  * @Date Apr 23, 2013 11:43:59 PM
  * @author voidmain
  */
 public abstract class Frame {
 	/**
 	 * Display name of the device the frame comes
 	 */
 	protected String mDeviceName;
 
 	/**
 	 * The inner UUID used to identify the device
 	 */
 	protected String mInnerUUID;
 
 	public Frame(Device theDevice) {
 		mDeviceName = theDevice.getDeviceName();
 		mInnerUUID = theDevice.getInnerUUID();
 		mTimestamp = new Date();
 	}
 	
 	public String getDeviceName() {
 		return mDeviceName;
 	}
 	
 	public void setDeviceName(String deviceName) {
 		mDeviceName = deviceName;
 	}
 	
 	public String getInnerUUID() {
 		return mInnerUUID;
 	}
 	
 	public void setInnerUUID(String innerUUID) {
 		mInnerUUID = innerUUID;
 	}
 	
 	protected Date mTimestamp;
 
 	public Date getTimestamp() {
 		return mTimestamp;
 	}
 
 	public void setTimestamp(Date timestamp) {
 		this.mTimestamp = timestamp;
 	}
 
 }
