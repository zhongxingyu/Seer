 package models;
 
 import java.util.Date;
 
 /**
  * Created: 11-12-2012
  * Filename: Log.java
  * Description:
  */
 
 public class Log
 {
 
     private long _logId;
     private User _user;
     private String _userDetails;
     private String _exception;
     private String _exceptionLocation;
     private Date _createdDate;
 
     public long getLogId()
     { return _logId; }
 
    public User getUser()
     { return _user; }
 
     public String getUserDetails()
     { return _userDetails; }
 
     public String getException()
     { return _exception; }
 
     public String getExceptionLocation()
     { return _exceptionLocation; }
 
     public Date getCreatedDate()
     { return _createdDate; }
 
     public Log(User user, String userDetails, String exception, String exceptionLocation, Date createdDate)
     {
         _user = user;
         _userDetails = userDetails;
         _exception = exception;
         _exceptionLocation = exceptionLocation;
         _createdDate = createdDate;
     }
 
     public Log(long logId, User user, String userDetails, String exception, String exceptionLocation, Date createdDate)
     {
         _logId = logId;
         _user = user;
         _userDetails = userDetails;
         _exception = exception;
         _exceptionLocation = exceptionLocation;
         _createdDate = createdDate;
     }
 }
