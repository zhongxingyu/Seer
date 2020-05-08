 package jp.ddo.haselab.timerecoder.dataaccess;
 
 import jp.ddo.haselab.timerecoder.util.RecodeDateTime;
 
 public class Recode {
 
     private long rowId;
     private RecodeDateTime dateTime;
     private int    eventId;
     private String memo;
 
 
     public Recode(final RecodeDateTime argRecodeDateTime,
 		  final int argEventId,
 		  final String argMemo){
 
 	rowId    = 0;
 	dateTime = argRecodeDateTime;
 	eventId  = argEventId;
 	memo     = argMemo;
     }
 
    public void setRowId(final long arg){
 	rowId = arg;
     }
 
     public long getRowId(){
 	return rowId;
     }
 
     public RecodeDateTime getDateTime() {
 	return dateTime;
     }
 
     public int  getEventId() {
 	return eventId;
     }
     
     public String getMemo() {
 	return memo;
     }
 
     @Override
 	public String toString() {
 	return "rowId[" + rowId + "]" +
 	    "dateTime[" + dateTime + "]" +
 	    "eventId[" + eventId + "]" +
 	    "memo[" + memo +"]";
     }
 }
