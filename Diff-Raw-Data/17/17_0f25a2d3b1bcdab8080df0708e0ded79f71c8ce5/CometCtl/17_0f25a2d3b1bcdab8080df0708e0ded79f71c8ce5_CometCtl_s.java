 package org.cc.ctl;
 
 import org.cc.response.CloudResponse;
 import org.cc.ent.RequestStatus;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import java.util.List;
 
 /**
  * Client calls this method get responses.

  * Daneel Yaitskov
  */
 public class CometCtl {
 
     public RequestStatus getStatus(@RequestParam("request") int requestId) {
         return RequestStatus.IN_QUEUE;
     }
 
     /**
      * @param lastId last request id that client got in previous response
      * @return list of responses
      */
     public List<CloudResponse> getNew(@RequestParam("last") int lastId) {
         return null;
     }
 }
