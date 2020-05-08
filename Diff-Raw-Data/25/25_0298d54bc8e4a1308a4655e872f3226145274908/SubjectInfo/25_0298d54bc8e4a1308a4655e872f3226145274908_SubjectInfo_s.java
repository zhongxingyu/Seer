 package org.ibcb.xnat.redaction.database;
 
 public class SubjectInfo {
 	
 	private String subjectid;
	private String fakephidata;
 	private String requestid;
 	private String projectid;
 	
 	
	public SubjectInfo(String sub,String phi,String req,String proj)
 	{
 		this.subjectid=sub;
		this.fakephidata=phi;
 		this.requestid=req;
 		this.projectid=proj;
 	}
 	public String getSubjectid() {
 		return subjectid;
 	}
 	public void setSubjectid(String subjectid) {
 		this.subjectid = subjectid;
 	}
	public String getFakephidata() {
 		return fakephidata;
 	}
	public void setFakephidata(String fakephidata) {
		this.fakephidata = fakephidata;
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
