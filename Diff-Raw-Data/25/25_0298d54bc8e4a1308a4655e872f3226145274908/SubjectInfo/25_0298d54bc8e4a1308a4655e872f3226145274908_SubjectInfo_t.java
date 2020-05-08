 package org.ibcb.xnat.redaction.database;
 
import java.util.HashMap;

 public class SubjectInfo {
 	
 	private String subjectid;
	//private String fakephidata;
 	private String requestid;
 	private String projectid;
	private HashMap<String,String> fakephidata;
 	
 	
	public SubjectInfo(String sub,String phi_key,String phi_value,String req,String proj)
 	{
 		this.subjectid=sub;
 		this.requestid=req;
 		this.projectid=proj;
		this.fakephidata=new HashMap<String,String>();
		fakephidata.put(phi_key, phi_value);
 	}
 	public String getSubjectid() {
 		return subjectid;
 	}
 	public void setSubjectid(String subjectid) {
 		this.subjectid = subjectid;
 	}
	public HashMap<String,String> getFakephidata() {
 		return fakephidata;
 	}
	public void setFakephidata(String phi_key,String phi_value) {
		this.fakephidata = new HashMap<String,String>();
		fakephidata.put(phi_key, phi_value);
		
 	}
 	public String getRequestid() {
 		return requestid;
 	}
 	public void setRequestid(String requestid) {
 		this.requestid = requestid;
 	}
 	public String getProjectid() {
 		return projectid;
 	}
 	public void setProjectid(String projectid) {
 		this.projectid = projectid;
 	}
 
 	
 }
