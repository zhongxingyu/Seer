 package com.jobhound.datasource;
 
 import com.j256.ormlite.field.DatabaseField;
 
 public class DiaryEntry {
 
 	public static final String DATE = "date";
 	public static final String ACTION = "action"; 
 	public static final String EMPLOYER = "employer";
 	public static final String FURTHERACTION = "furtherAction";
	public static final String FDATE = "fDate";
 	public static final String SUITABILITY = "suitablility";
 	public static final String REASON = "reason";
 	public static final String COMMENTS = "comments";
 	
 	@DatabaseField(columnName=DATE)
 	private String date;
 	@DatabaseField(columnName=ACTION)
 	private String action;
 	@DatabaseField(columnName=EMPLOYER)
 	private String employer;
 	@DatabaseField(columnName=FURTHERACTION)
 	private String furtherAction;
 	@DatabaseField(columnName=FDATE)
 	private String fDate;
 	@DatabaseField(columnName=SUITABILITY)
 	private String suitability;
 	@DatabaseField(columnName=REASON)
 	private String reason;
 	@DatabaseField(columnName=COMMENTS)
 	private String comments;
 	
 	public DiaryEntry(String date, String action, String employer,String furtherAction, String fDate, String suitability, String reason, String comments)
 	{
 		this.date = date;
 		this.action=action;
 		this.employer=employer;
 		this.furtherAction=furtherAction;
 		this.fDate=fDate;
 		this.suitability=suitability;
 		this.reason=reason;
 		this.comments=comments;
 	}
 	
 	public DiaryEntry()
 	{
 		
 	}
 	
 	public String getAction()
 	{
 		return action;
 	}
 	
 	public void setAction(String action)
 	{
 		this.action=action;
 	}
 	
 	public String getEmployer()
 	{
 		return employer;
 	}
 	
 	public void setEmployer(String employer)
 	{
 		this.employer=employer;
 	}
 	
 	public String getDate()
 	{
 		return date;
 	}
 	
 	public void setDate(String date)
 	{
 		this.date=date;
 	}
 	
 	public String getFurtherAction()
 	{
 		return furtherAction;
 	}
 	
 	public void setFurtherAction(String furtherAction)
 	{
 		this.furtherAction=furtherAction;
 	}
 	
 	public String getFDate()
 	{
 		return fDate;
 	}
 	
 	public void setFDate(String fDate)
 	{
 		this.fDate=fDate;
 	}
 	
 	public String getSuitability()
 	{
 		return suitability;
 	}
 	
 	public void setSuitability(String suitability)
 	{
 		this.suitability=suitability;
 	}
 	
 	public String getReason()
 	{
 		return reason;
 	}
 	
 	public void setReason(String reason)
 	{
 		this.reason=reason;
 	}
 	
 	public String getComments()
 	{
 		return comments;
 	}
 	
 	public void setComments(String comments)
 	{
 		this.comments=comments;
 	}
 }
