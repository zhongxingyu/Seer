 package com.tuvistavie.meetup.event.model;
 
 import com.tuvistavie.meetup.model.AbstractEntity;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.util.Date;
 
 /**
  * Created by daniel on 8/31/13.
  */
 public class EventDate extends AbstractEntity {
     private Date startDateTime;
     private Date endDateTime;
 
     public EventDate(long timeStamp) {
         this(new Date(timeStamp));
     }
 
     public EventDate(Date startDate) {
         this(startDate, new Date(startDate.getTime() + 3600));
     }
 
     public EventDate(Date startDate, Date endDate) {
         this.startDateTime = startDate;
         this.endDateTime = endDate;
     }
 
     public EventDate(JSONObject jsonObject) {
         super(jsonObject);
     }
 
     @Override
     public void fromJSON(JSONObject jsonObject) {
         try {
            startDateTime = new Date(jsonObject.getLong("start"));
            endDateTime = new Date(jsonObject.getLong("end"));
         } catch (JSONException e) {
             e.printStackTrace();
         }
     }
 
     @Override
     public JSONObject toJSONObject() {
         JSONObject jsonDate = new JSONObject();
         try {
            jsonDate.put("start", startDateTime.getTime());
            jsonDate.put("end", endDateTime.getTime());
             return jsonDate;
         } catch (JSONException e) {
             e.printStackTrace();
             return null;
         }
     }
 
     public Date getStartDateTime() {
         return startDateTime;
     }
 
     public Date getEndDateTime() {
         return endDateTime;
     }
 }
