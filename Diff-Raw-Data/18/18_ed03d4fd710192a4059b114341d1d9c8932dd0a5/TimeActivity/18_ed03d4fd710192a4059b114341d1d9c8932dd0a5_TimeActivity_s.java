 package com.linqcan.mytime;
 
 import java.io.Serializable;
 import java.util.Date;
 
 public class TimeActivity implements Serializable{
 	
 	private static final long serialVersionUID = -7763321659962185241L;
 	private long id;
 	private Date created_on; //Internal time stamp
 	private Date activity_date; //Date the activity is valid for
 	private Date start_date; //Date the activity started
 	private Date end_date; //Date the activity ended
 	private long duration = 0;
 	private String description;
	private long label_id;
 	private boolean ongoing;
 	
 	public final static String TABLE_NAME = "activities";
 	public final static String COLUMN_LABEL = "label_id";
 	public final static String COLUMN_DURATION = "duration";
 	public final static String COLUMN_CREATED_ON = "created_on";
 	public final static String COLUMN_ACTIVITY_DATE = "activity_date";
 	public final static String COLUMN_START_DATE = "start_date";
 	public final static String COLUMN_END_DATE = "end_date";
 	public final static String COLUMN_DESCRIPTION = "description";
 	public final static String COLUMN_ONGOING = "ongoing";
 	public final static String DATABASE_TABLE_CREATE = "CREATE TABLE "+ TABLE_NAME +
 			" (_id INTEGER PRIMARY KEY AUTOINCREMENT, "+
 			COLUMN_CREATED_ON+" INTEGER NOT NULL DEFAULT (strftime('%s',current_timestamp)), " +
 			COLUMN_ACTIVITY_DATE+" INTEGER NOT NULL, " +
 			COLUMN_START_DATE+" INTEGER NOT NULL, " +
 			COLUMN_END_DATE+" INTEGER NOT NULL, " +
 			COLUMN_DURATION+" INTEGER, " +
 			COLUMN_DESCRIPTION+" TEXT, " +
 			COLUMN_ONGOING+" INTEGER, " +
 			COLUMN_LABEL+" INTEGER, " +
 			"FOREIGN KEY("+COLUMN_LABEL+") REFERENCES "+Label.TABLE_NAME+"(_id));";
 	
 
 	public TimeActivity(){
 	}
 	
 	public long getId() {
 		return id;
 	}
 	public void setId(long id) {
 		this.id = id;
 	}
 	public long getLabel_id() {
 		return label_id;
 	}
 	public void setLabel_id(long label_id) {
 		this.label_id = label_id;
 	}
 	public long getDuration() {
 		return this.end_date.getTime() - this.start_date.getTime();
 		//return duration;
 	}
 //	public void setDuration(long duration) {
 //		this.duration = duration;
 //	}
 	public Date getCreated_on() {
 		return created_on;
 	}
 	public void setCreated_on(Date created_on){
 		this.created_on = created_on;
 	}
 	public String getDescription() {
 		return description;
 	}
 	public void setDescription(String description) {
 		this.description = description;
 	}
 	public Date getActivity_date() {
 		return activity_date;
 	}
 	public void setActivity_date(Date activity_date) {
 		this.activity_date = activity_date;
 	}
 	public boolean getOngoing(){
 		return this.ongoing;
 	}
 	public void setOngoing(boolean ongoing){
 		this.ongoing = ongoing;
 	}
 	public Date getStart_date() {
 		return start_date;
 	}
 	public void setStart_date(Date start_date) {
 		this.start_date = start_date;
 	}
 	public Date getEnd_date() {
 		return end_date;
 	}
 	public void setEnd_date(Date end_date) {
 		this.end_date = end_date;
 	}
 }
