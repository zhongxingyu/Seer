 package net.selenate.common.comms.req;
 
 import java.io.Serializable;
 
 public class SeReqGet implements Serializable {
   private static final long serialVersionUID = 1L;
 
   public final String url;
 
   public SeReqGet(final String url) {
     if (url == null) {
      throw new IllegalArgumentException("Url cannot be null!");
     }
 
     this.url = url;
   }
 }
