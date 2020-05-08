 package ch.cern.atlas.apvs.domain;
 
 import java.io.Serializable;
 import java.util.Date;
 
 public class Error implements Message, Serializable {
 
 	
 	private static final long serialVersionUID = -7278595154796058932L;
 
 	private String ptuId;
 	private Integer errorNo;
 	private String description;
 	private String criticality;
 	private Date date;
 
 	public Error() {
 	}
 
 	public Error(String ptuId, Integer errorNo, String description, String criticality, Date date) {
 		this.ptuId = ptuId;
 		this.errorNo = errorNo;
 		this.description = description;
 		this.criticality = criticality;
 		this.date = date;
 	}
 
     @Override
 	public String getPtuId() {
 		return ptuId;
 	}
 
 	public Integer getErrorNo() {
 		return errorNo;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public String getCriticality() {
 		return criticality;
 	}
 
 	public Date getDate() {
 		return date;
 	}
 
 	@Override
 	public String getType() {
		return "Error";
 	}
 }
