 package ru.alepar.tdt.gwt.client.action.gcal;
 
 import ru.alepar.tdt.gwt.client.action.core.TdtAction;
 import ru.alepar.tdt.gwt.client.action.core.TdtResponse;
 
 /**
  * User: alepar
  * Date: Aug 9, 2010
  * Time: 12:22:23 AM
  */
 public class GetGoogleCalAuthUrl implements TdtAction<GetGoogleCalAuthUrl.Response> {
 
    public class Response implements TdtResponse {
 
         public Response() { //used by serialization
         }
     }
 
     public GetGoogleCalAuthUrl() { //used by serialization
     }
 }
