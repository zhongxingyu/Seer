 package ch.cern.atlas.apvs.domain;
 
 import java.io.Serializable;
 import java.util.Date;
 
 public class Report implements Message, Serializable {
 
 	private static final long serialVersionUID = -2218198847285152101L;
 
 	private String ptuId;
 	private Number batteryLevel;
 	private boolean cameraHandheld;
 	private boolean cameraHelmet;
 	private boolean audio;
 	private Date date;
 
 	public Report() {
 	}
 
 	public Report(String ptuId, double batteryLevel, boolean cameraHandheld, boolean cameraHelmet, boolean audio, Date date) {
 		this.ptuId = ptuId;
 		this.batteryLevel = batteryLevel;
 		this.cameraHandheld = cameraHandheld;
 		this.cameraHelmet = cameraHelmet;
 		this.audio = audio;
 		this.date = date;
 	}
 
     @Override
 	public String getPtuId() {
 		return ptuId;
 	}
 
 	public Number getBatteryLevel() {
 		return batteryLevel;
 	}
 
 	public boolean getCameraHandheld() {
 		return cameraHandheld;
 	}
 
 	public boolean getCameraHelmet() {
 		return cameraHelmet;
 	}
 
 	public boolean getAudio() {
 		return audio;
 	}
 
 	public Date getDate() {
 		return date;
 	}
 
 	@Override
 	public String getType() {
		return "report";
 	}
 }
