 package com.interactuamovil.contactosms.api;
 
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.interactuamovil.contactosms.api.responses.ActionMessageResponse;
 import com.interactuamovil.contactosms.api.responses.InboxMessageResponse;
 import com.interactuamovil.contactosms.api.responses.ListResponse;
 import com.interactuamovil.contactosms.api.responses.MessageResponse;
 import com.interactuamovil.contactosms.api.responses.MessageToGroupResponse;
 import com.interactuamovil.contactosms.api.responses.Response;
 import com.interactuamovil.contactosms.api.responses.ScheduleMessageResponse;
 import java.io.Serializable;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 public class Messages extends Request {
     public Messages(String apiKey, String secretKey, String apiUri) {
         super(apiKey, secretKey, apiUri);
     }
 
     private String getDateFormat(Date d) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyy-MM-dd");
         return formatter.format(d);
     }
 
     /**
      * Gets log message list
      *
      * @param startDate
      * @param endDate
      * @param start
      * @param limit
      * @param msisdn
      * @return
      */
     public ListResponse<MessageResponse> getList(Date startDate, Date endDate, int start, int limit, String msisdn) {
         Map<String, Serializable> urlParameters = new LinkedHashMap<String, Serializable>();
         ListResponse<MessageResponse> response = new ListResponse<MessageResponse>();
         List<MessageResponse> messageResponse;
 
 
         if (!(startDate == null) && !(endDate == null)) {
             urlParameters.put("start_date", getDateFormat(startDate));
             urlParameters.put("end_date", getDateFormat(endDate));
         }
         if (start != -1)
             urlParameters.put("start", start);
         if (limit != -1)
             urlParameters.put("limit", limit);
         if (msisdn != null)
             urlParameters.put("msisdn", msisdn);
 
         try {
             String serverResponse = doRequest("messages", "get", urlParameters, null, true);
             ObjectMapper mapper = new ObjectMapper();
 
             messageResponse = mapper.readValue(serverResponse, List.class);
             response.setResult(messageResponse);
         } catch (Exception e) {
             response.setHasError(true);
             response.setErrorMessage(e.getMessage());
         }
         return response;
     }
 
     /**
      * Sends a message to a group array list
      *
      * @param short_name
      * @param message
      * @return
      */
     public Response<MessageToGroupResponse> sendToGroups(String[] short_name, String message) {
         Map<String, Serializable> params = new LinkedHashMap<String, Serializable>();
         Response<MessageToGroupResponse> response = new Response<MessageToGroupResponse>();
         MessageToGroupResponse messageResponse;
 
         params.put("groups", short_name);
         params.put("message", message);
 
         try {
             String serverResponse = doRequest("messages/send", "post", null, params, false);
             ObjectMapper mapper = new ObjectMapper();
 
             messageResponse = mapper.readValue(serverResponse, MessageToGroupResponse.class);
             response.setResult(messageResponse);
         } catch (Exception e) {
             response.setError(true);
             response.setErrorMessage(e.getMessage());
         }
         return response;
     }
 
     /**
      * Sends a message to a specific contact
      *
      * @param msisdn
      * @param message
      * @return
      */
     public Response<MessageToGroupResponse> sendToContact(String msisdn, String message) {
         Map<String, Serializable> params = new LinkedHashMap<String, Serializable>();
         Response<MessageToGroupResponse> response = new Response<MessageToGroupResponse>();
         MessageToGroupResponse messageResponse;
 
         params.put("msisdn", msisdn);
         params.put("message", message);
 
         try {
             String serverResponse = doRequest("messages/send_to_contact", "post", null, params, false);
             ObjectMapper mapper = new ObjectMapper();
 
             messageResponse = mapper.readValue(serverResponse, MessageToGroupResponse.class);
             response.setResult(messageResponse);
         } catch (Exception e) {
             response.setError(true);
             response.setErrorMessage(e.getMessage());
         }
         return response;
     }
 
     /**
      * Gets schedule messsages
      *
      * @return
      */
     public ListResponse<ScheduleMessageResponse> getSchedule() {
         ListResponse<ScheduleMessageResponse> response = new ListResponse<ScheduleMessageResponse>();
         List<ScheduleMessageResponse> messageResponse;
 
         try {
             String serverResponse = doRequest("messages/scheduled", "get", null, null, false);
             ObjectMapper mapper = new ObjectMapper();
 
             messageResponse = mapper.readValue(serverResponse, List.class);
             response.setResult(messageResponse);
         } catch (Exception e) {
             response.setHasError(true);
             response.setErrorMessage(e.getMessage());
         }
         return response;
     }
 
     /**
      * Removes a schedule messsage
      *
      * @param messageId
      * @return
      */
     public Response<ActionMessageResponse> deleteSchedule(int messageId) {
         Response<ActionMessageResponse> response = new Response<ActionMessageResponse>();
         Map<String, Serializable> params = new LinkedHashMap<String, Serializable>();
         ActionMessageResponse messageResponse;
 
         params.put("message_id", messageId);
 
         try {
             String serverResponse = doRequest("messages/scheduled", "delete", null, params, false);
             ObjectMapper mapper = new ObjectMapper();
 
             messageResponse = mapper.readValue(serverResponse, ActionMessageResponse.class);
             response.setResult(messageResponse);
         } catch (Exception e) {
             response.setError(true);
             response.setErrorMessage(e.getMessage());
         }
         return response;
     }
 
     /**
      * Adds a new schedule messsage
      *
      * @param startDate
      * @param endDate
      * @param message
      * @param time
      * @param frequency
      * @param groups
      * @return
      */
    public Response<ActionMessageResponse> addSchedule(Date startDate, Date endDate, String message, String time, String frequency, String[] groups) {
         Map<String, Serializable> params = new LinkedHashMap<String, Serializable>();
         Response<ActionMessageResponse> response = new Response<ActionMessageResponse>();
         ActionMessageResponse messageResponse;
 
         params.put("start_date", getDateFormat(startDate));
         params.put("end_date", getDateFormat(endDate));
         params.put("message", message);
        params.put("time", time);
        params.put("frequency", frequency);
         params.put("groups", groups);
 
         try {
             String serverResponse = doRequest("messages/scheduled", "post", null, params, false);
             ObjectMapper mapper = new ObjectMapper();
 
             messageResponse = mapper.readValue(serverResponse, ActionMessageResponse.class);
             response.setResult(messageResponse);
         } catch (Exception e) {
             response.setError(true);
             response.setErrorMessage(e.getMessage());
         }
         return response;
     }
 
     /**
      * Gets the inbox messages
      *
      * @param startDate
      * @param endDate
      * @param start
      * @param limit
      * @param msisdn
      * @param status
      * @return
      */
     public ListResponse<InboxMessageResponse> inbox(Date startDate, Date endDate, int start, int limit, String msisdn, int status) {
         ListResponse<InboxMessageResponse> response = new ListResponse<InboxMessageResponse>();
         Map<String, Serializable> params = new LinkedHashMap<String, Serializable>();
         List<InboxMessageResponse> messageResponse;
         int _status = decodeStatus(status);
 
         if (!(startDate == null || endDate == null)) {
             params.put("start_date", getDateFormat(startDate));
             params.put("end_date", getDateFormat(endDate));
         }
         if (start >= 0)
             params.put("start", start);
         if (limit >= 0)
             params.put("limit", limit);
         if (!(msisdn == null) && msisdn.length() > 3)
             params.put("msisdn", msisdn);
         if (_status > -1)
             params.put("status", _status);
 
         try {
             String serverResponse = doRequest("messages/messages_inbox", "get", params, null, true);
             ObjectMapper mapper = new ObjectMapper();
 
             messageResponse = mapper.readValue(serverResponse, List.class);
             response.setResult(messageResponse);
         } catch (Exception e) {
             response.setHasError(true);
             response.setErrorMessage(e.getMessage());
         }
         return response;
     }
 
     private int decodeStatus(int type) {
         int result;
 
         switch (type) {
             case 0:
                 result = 0;
                 break;
             case 1:
                 result = 1;
                 break;
             case 2:
                 result = 2;
                 break;
             default:
                 result = -1;
         }
         return result;
     }
 
 }
