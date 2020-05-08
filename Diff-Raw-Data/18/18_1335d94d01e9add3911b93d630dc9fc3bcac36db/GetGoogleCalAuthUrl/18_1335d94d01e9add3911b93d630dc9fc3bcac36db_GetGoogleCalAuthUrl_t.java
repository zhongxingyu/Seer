 package ru.alepar.tdt.gwt.client.action.gcal;
 
import ru.alepar.tdt.backend.action.core.MapTo;
import ru.alepar.tdt.backend.action.gcal.GetGoogleCalAuthUrlHandler;
 import ru.alepar.tdt.gwt.client.action.core.TdtAction;
 import ru.alepar.tdt.gwt.client.action.core.TdtResponse;
 
 /**
  * User: alepar
  * Date: Aug 9, 2010
  * Time: 12:22:23 AM
  */
@MapTo(GetGoogleCalAuthUrlHandler.class)
 public class GetGoogleCalAuthUrl implements TdtAction<GetGoogleCalAuthUrl.Response> {
 
    public static class Response implements TdtResponse {

        private String authUrl;
 
         public Response() { //used by serialization
         }

        public String getAuthUrl() {
            return authUrl;
        }

        public void setAuthUrl(String authUrl) {
            this.authUrl = authUrl;
        }
     }
 
     public GetGoogleCalAuthUrl() { //used by serialization
     }
 }
