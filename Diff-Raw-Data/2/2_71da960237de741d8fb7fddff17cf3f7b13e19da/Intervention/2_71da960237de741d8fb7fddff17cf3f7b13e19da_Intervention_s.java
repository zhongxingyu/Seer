Fpackage ch.cern.atlas.apvs.client.domain;
 
 import java.io.Serializable;
 import java.util.Date;
 
 import javax.validation.constraints.NotNull;
 
 import com.google.gwt.user.client.rpc.IsSerializable;
 
 //NOTE: implements IsSerializable in case serialization file cannot be found
 public class Intervention implements Serializable, IsSerializable {
 
 	private static final long serialVersionUID = 2578285814293336298L;
 
 	@NotNull
 	private Integer userId;
 	@NotNull
 	private Integer deviceId;
 
 	private int id;
 	private String fname;
 	private String lname;
 	private String ptuId;
 	private Date startTime;
 	private Date endTime;
 	private String impactNumber;
 	private Double recStatus;
 	private String description;
 
 	public Intervention() {
 	}
 	
 	public Intervention(int id, int userId, String fname, String lname, int deviceId, String ptuId, Date startTime,
 			Date endTime, String impactNumber, Double recStatus, String description) {
 		this(userId, deviceId, startTime, impactNumber, recStatus, description);
 		this.id = id;
 		this.fname = fname;
 		this.lname = lname;
 		this.ptuId = ptuId;
 		this.endTime = endTime;
 	}
 
 	public Intervention(Integer userId, Integer deviceId, Date startTime, String impactNumber, Double recStatus, String description) {
 		this.id = 0;
 		this.userId = userId;
 		this.deviceId = deviceId;
 		this.startTime = startTime;
 		this.impactNumber = impactNumber;
 		this.recStatus = recStatus;
 		this.description = description;
 	}
 
 	public Integer getDeviceId() {
 		return deviceId;
 	}
 
 	public Integer getUserId() {
 		return userId;
 	}
 	
 	public String getName() {		
 		if (((fname == null) || fname.equals("")) && ((lname == null) || lname.equals(""))) {
 			return "";
 		}
 		
 		if ((fname == null) || fname.equals("")) {
 			return lname;
 		}
 		
 		if ((lname == null) || lname.equals("")) {
 			return fname;
 		}
 		
 		return fname+" "+lname;
 	}
 
 	public String getPtuId() {
 		return ptuId;
 	}
 	
 	public String getImpactNumber() {
 		return impactNumber;
 	}
 	
 	public void setImpactNumber(String impactNumber) {
 		this.impactNumber = impactNumber;
 	}
 	
 	public Double getRecStatus() {
 		return recStatus;
 	}
 	
 	public void setRecStatus(Double recStatus) {
 		this.recStatus = recStatus;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public Date getStartTime() {
 		return startTime;
 	}
 
 	public Date getEndTime() {
 		return endTime;
 	}
 
 	public void setEndTime(Date endTime) {
 		this.endTime = endTime;
 	}
 
 	public int getId() {
 		return id;
 	}
 	
 	@Override
 	public int hashCode() {
 		return id;
 	}
 	
 	@Override
 	public boolean equals(Object obj) {
 		if (obj != null && obj instanceof Intervention) {
 			return getId() == ((Intervention)obj).getId();
 		}
 		return super.equals(obj);
 	}
 
 
 	public boolean equalIntervention(Intervention obj){
 				
 			//Description test
 			if((obj.getDescription()==null && this.getDescription()==null))
 				;
 			else if (obj.getDescription().equals(this.getDescription()))
 					;
 				else{
 					return false;
 				}
 			
 			//DeviceId test
 			if((obj.getDeviceId()==null && this.getDeviceId()==null))
 				;
 			else if (obj.getDeviceId().equals(this.getDeviceId()))
 					;
 				else{
 					return false;
 				}
 				
 			//End Time test
 			if((obj.getEndTime()==null && this.getEndTime()==null))	
 					;
 			else if (obj.getEndTime().equals(this.getEndTime()))
 						;
 				else{
 					return false;
 				}
 			
 			
 			
 			//Start Time test
 			if((obj.getStartTime()==null && this.getStartTime()==null))
 					;
 			else if (obj.getStartTime().equals(this.getStartTime()))
 					;
 				else{
 					return false;
 				}
 			
 			
 			//ID test
 			if((obj.getId() == this.getId()))
 				;
 			else{
 				return false;
 				}
 			
 			
 			
 			//Impact Number test
 			if((obj.getImpactNumber()==null && this.getImpactNumber()==null))
 					;
 			else if (obj.getImpactNumber().equals(this.getImpactNumber()))
 						;
 				else{
 					return false;
 				}
 				
 			
 			//Name test
 			if((obj.getName()==null && this.getName()==null))
 					;
 			else if (obj.getName().equals(this.getName()))
 						;
 				else{
 					return false;
 				}
 				
 			
 			//PTU test
 			if((obj.getPtuId()==null && this.getPtuId()==null))
 					;
 			else if (obj.getPtuId().equals(this.getPtuId()))
 						;
 				else{
 					return false;
 				}
 				
 			
 			//REC Status test
 			if((obj.getRecStatus()==null && this.getRecStatus()==null))
 				;
 			else if (obj.getRecStatus().equals(this.getRecStatus()))
 					;
 				else{
 					return false;
 				}
 				
 			//User ID test
 			if((obj.getUserId()==null && this.getUserId()==null))
 					;
 			else if (obj.getUserId().equals(this.getUserId()))
 						;
 				else{
 					return false;
 				}
 		
 		return true;
 	}
 	
 	
 	
 }
