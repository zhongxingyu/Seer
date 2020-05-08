 package com.bearkachu.detectingcommongestures;
 
 public class CustomedTouchPad {
 	private long downTime;
 	private long eventTime;
 	private float x, y;
 	private float pressure;
 	private float size;
 	private int metaState;
 	private float xPrecision;
 	private float yPrecision;
 	private int deviceId;
 	private int edgeFlags;
 	
     //Constructor
 	public CustomedTouchPad() {
 		// Not necessarily implemented here 
 	}
     
 	//Methods
     public void setDowndTime(long downTime) {
     	this.downTime = downTime;
     }
     public long getDowndTime() {
     	return this.downTime;
     }
     public void setEventTime(long eventTime) {
     	this.eventTime = eventTime;
     } 
     public long getEventTime() {
     	return this.eventTime;
     }
     public void setX(float x) {
     	this.x = x; 
     }
     public float getX() {
     	return this.x;
     }
     public void setY(float y) {
     	this.y = y;
     }
     public float getY() {
     	return this.y;
     }
     public void setPressure(float pressure) {
     	this.pressure = pressure;
     }
     public float getPressure() {
     	return this.pressure;
     }
     public void setSize(float size) {
     	this.size = size;
     }
     public float getSize() {
     	return this.size;
     }
     public void setMetaState(int metaState) {
     	this.metaState = metaState;
     }
     public int getMetaState() {
     	return this.metaState;
     }
     public void setXPrecision(float xPrecision) {
     	this.xPrecision = xPrecision;
     } 
     public float getXPrecision() {
     	return this.xPrecision;
     }
     public void setYPrecision(float yPrecision) {
     	this.yPrecision = yPrecision;
     } 
     public float getYPrecision() {
     	return this.yPrecision;
     }
     public void setDeviceId(int deviceId) {
     	this.deviceId = deviceId;
     }
     public int getDeviceId() {
     	return this.deviceId;
     }
     public void setEdgeFlags(int edgeFlags) {
     	this.edgeFlags = edgeFlags;
     }
     public int getEdgeFlags() {
     	return this.edgeFlags;
     }
 }
