 package com.practo.bc;
 
 import com.practo.entity.TicketCriteria;
 import com.practo.entity.ZendeskTicket;
 import com.practo.enums.ReportType;
 import com.practo.enums.TicketPriority;
 import com.practo.enums.TicketStatus;
 import com.practo.utils.DateUtil;
 import com.practo.utils.HttpUtil;
 import com.practo.utils.Urls;
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 import org.json.simple.JSONValue;
 
 import java.net.URL;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 /**
  * Company: AcStack
  * User: Shwetanka
  * Date: Apr 7, 2012
  * Time: 12:57:27 PM
  */
 public class ZendeskApiBc {
   public List<ZendeskTicket> getTicketsByCriteria(TicketCriteria criteria){
     String response;
     URL url;
     try{
       if(criteria.getStatus()==TicketStatus.SOLVED){
         url = new URL(Urls.SOLVED_TICKETS_URL);
         response = HttpUtil.getResponse(url);
       }else {
         url = new URL(Urls.ALL_TICKETS_URL);
         response = HttpUtil.getResponse(url);
       }
     }catch (Exception e){
       System.out.println("Error: "+e);
       return null;
     }
     if(response!=null){
       JSONArray array = (JSONArray) JSONValue.parse(response);
       List<ZendeskTicket> tickets = new ArrayList<ZendeskTicket>();
       if(array!=null && array.size()>0){
         for(int i=0;i<array.size();i++){
           JSONObject jb = (JSONObject) array.get(i);
           ZendeskTicket ticket = new ZendeskTicket();
           copy(ticket, jb);
           tickets.add(ticket);
         }
         List<ZendeskTicket> result = processTickets(tickets, criteria);
         return result;
       }
     }
     return null;
   }
 
   private List<ZendeskTicket> processTickets(List<ZendeskTicket> tickets, TicketCriteria criteria){
     List<ZendeskTicket> result = new ArrayList<ZendeskTicket>();
     boolean status = false;
     boolean priority;
     boolean type = false;
     for(ZendeskTicket ticket : tickets){
       if(criteria.getStatus()!=null){
         if(ticket.getStatus().equals(criteria.getStatus())){
           status = true;
         }
       }else {
         status = true;
       }
       if(criteria.getType()!=null){
         if(criteria.getType().equals(ReportType.DAILY)){
           if(DateUtil.isToday(ticket.getStatusUpdateTime())){
             type = true;
           }
         }else if(criteria.getType().equals(ReportType.WEEKLY)){
           if(DateUtil.isInWeek(ticket.getStatusUpdateTime())){
             type = true;
           }
         }
       }else {
         type = true;
       }
       if(type && status){
         result.add(ticket);
        type=false;
        status = false;
       }
     }
     return result.size()>0?result:null;
   }
 
   private void copy(ZendeskTicket ticket, JSONObject jb){
     ticket.setNiceId((Long) jb.get("nice_id"));
     ticket.setAssigneeId((Long) jb.get("assignee_id"));
     ticket.setCreationTime(getDate((String)jb.get("initially_assigned_at")));
     ticket.setDescription((String) jb.get("description"));
     Long l = (Long) jb.get("priority_id");
     ticket.setPriority(TicketPriority.getPriorityByValue(l.intValue()));
     l = (Long)jb.get("status_id");
     ticket.setStatus(TicketStatus.getStatusByValue(l.intValue()));
     ticket.setSubject((String)jb.get("subject"));
     ticket.setSubmitterId((Long) jb.get("submitter_id"));
     ticket.setStatusUpdateTime(getDate((String) jb.get("status_updated_at")));
   }
   private Date getDate(String date){ // This expect date in yyyy/MM/dd hh:mm:ss Z
     SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss z");
 
     try{
       return sdf.parse(date);
     } catch (ParseException pe){
       return null;
     }
 
   }
 }
 
