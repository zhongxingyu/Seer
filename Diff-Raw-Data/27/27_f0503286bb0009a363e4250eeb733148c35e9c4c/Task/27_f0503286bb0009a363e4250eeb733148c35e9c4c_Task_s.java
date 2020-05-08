 package com.CMPUT301F12T07.crowdsource.taskmodeldb;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 
 public class Task { 
 	
 	/** private variables */
     Long _tid; 
     String _uid;
     String _title;
     String _description; 
     long _dateCreate; 
     long _dateDue;
     String _type;
     int _visibility; // 1 or 0
     int _quantity;
     int _qty_filled;
     int _followed; // 1 or 0
     int _num_followed;
     String _user_email;
     String _wid;
     
     public static final int TASK_REMOTE = 1;
     public static final int TASK_LOCAL = 0;
     
     /** Empty constructor */
     public Task(){ } 
    
     /** constructor with wid */
     public Task(String webid, String title, long dateDue, int quantity, int qty_filled, String type, String uid) { 
     	this._wid = webid;
     	this._title = title;
     	this._dateDue = dateDue;
     	this._quantity = quantity;
     	this._qty_filled = qty_filled;
     	this._type = type;
     	this._uid = uid;
     }
     
     /** constructor without tid */ 
     public Task(String uid, String title, String description, String dateCreate, 
     		String dateDue, String type, int visibility, int quantity, int qty_filled, int followed, int num_followed, String user_email){ 
         this._uid = uid;
         this._title = title;
         this._description = description; 
         set_dateCreate(dateCreate, TASK_LOCAL); 
         set_dateDue(dateDue, TASK_LOCAL);
         this._type = type;
         this._visibility = visibility;
         this._quantity = quantity;
         this._qty_filled = qty_filled;
         this._followed = followed;
         this._num_followed = num_followed;
         this._user_email = user_email;
     }
     
     /** constructor with tid */ 
     public Task( long tid, String uid, String title, String description, String dateCreate, 
     		String dateDue, String type, int visibility, int quantity, int qty_filled, int followed, int num_followed, String user_email){ 
     	this._tid = tid;
         this._uid = uid;
         this._title = title;
         this._description = description; 
         set_dateCreate(dateCreate, TASK_LOCAL); 
         set_dateDue(dateDue, TASK_LOCAL);
         this._type = type;
         this._visibility = visibility;
         this._quantity = quantity;
         this._qty_filled = qty_filled;
         this._followed = followed;
         this._num_followed = num_followed;
         this._user_email = user_email;
     }
     
     /** constructor with tid & wid */ 
     public Task( long tid, String uid, String title, String description, String dateCreate, 
     		String dateDue, String type, int visibility, int quantity, int qty_filled, int followed, int num_followed, String user_email, String webid){ 
     	this._tid = tid;
         this._uid = uid;
         this._title = title;
         this._description = description; 
         set_dateCreate(dateCreate, TASK_LOCAL); 
         set_dateDue(dateDue, TASK_LOCAL);
         this._type = type;
         this._visibility = visibility;
         this._quantity = quantity;
         this._qty_filled = qty_filled;
         this._followed = followed;
         this._num_followed = num_followed;
         this._user_email = user_email;
         this._wid = webid;
     }
 
     /** getter and setter */
 	public Long get_tid() {
 		return _tid;
 	}
 
 	public void set_tid(Long _tid) {
 		this._tid = _tid;
 	}
 
 	public String get_uid() {
 		return _uid;
 	}
 
 	public void set_uid(String _uid) {
 		this._uid = _uid;
 	}
 
 	public String get_title() {
 		return _title;
 	}
 
 	public void set_title(String _title) {
 		this._title = _title;
 	}
 
 	public String get_description() {
 		return _description;
 	}
 
 	public void set_description(String _description) {
 		this._description = _description;
 	}
 
 	public String get_dateCreate() {
 		Long dateLong = _dateCreate;
 		if (dateLong == 0){
 			return dateLong.toString();
 		} else {
 			Date date = new Date(_dateCreate);
 	    	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
 			return format.format(date).toString();
 		}
 	}
 
 	public void set_dateCreate(String _dateCreate, int taskFlag) {
 		if (taskFlag == TASK_REMOTE){
 			this._dateCreate = Long.parseLong(_dateCreate);
 		} else {
 			Calendar cal = Calendar.getInstance();
 			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
 			Date date = null;
 			try {
 				date = format.parse(_dateCreate);
 			} catch (ParseException e) {
 				e.printStackTrace();
 			}
 			cal.setTime(date);
 			this._dateCreate = cal.getTime().getTime();
 		}
 	}
 
 	public String get_dateDue() {
 		Date date = new Date(_dateDue);
 	    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
 	    return format.format(date).toString();
 	}
 
 	public void set_dateDue(String _dateDue, int taskFlag) {
 		if (taskFlag == TASK_REMOTE){
			this._dateCreate = Long.parseLong(_dateDue);
 		} else {
 			Calendar cal = Calendar.getInstance();
 			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
 			Date date = null;
 			try {
 				date = format.parse(_dateDue);
 			} catch (ParseException e) {
 				e.printStackTrace();
 			}
 			cal.setTime(date);
 			this._dateDue = cal.getTime().getTime();
 		}
 	}
 
 	public String get_type() {
 		return _type;
 	}
 
 	public void set_type(String _type) {
 		this._type = _type;
 	}
 	
 	public int get_visibility() {
 		return _visibility;
 	}
 
 	public void set_visibility(int _visibility) {
 		this._visibility = _visibility;
 	}
 	
 	public int get_quantity() {
 		return _quantity;
 	}
 
 	public void set_quantity(int _quantity) {
 		this._quantity = _quantity;
 	}
 	
 	public int get_qty_filled() {
 		return _qty_filled;
 	}
 	
 	public void set_qty_filled(int _qty_filled) {
 		this._qty_filled = _qty_filled;
 	}
 	
 	public int get_followed() {
 		return _followed;
 	}
 	
 	public void set_followed(int _followed) {
 		this._followed = _followed;
 	}
 	
 	public int get_num_followed() {
 		return _num_followed;
 	}
 	
 	public void set_num_followed(int _num_followed) {
 		this._num_followed = _num_followed;
 	}
 	
 	public String get_user_email() {
 		return _user_email;
 	}
 	
 	public void set_user_email(String user_email) {
 		this._user_email = user_email;
 	}
 	
 	public String get_wid() {
 		return _wid;
 	}
 	
 	public void set_wid(String webid) {
 		this._wid = webid;
 	}
 }
