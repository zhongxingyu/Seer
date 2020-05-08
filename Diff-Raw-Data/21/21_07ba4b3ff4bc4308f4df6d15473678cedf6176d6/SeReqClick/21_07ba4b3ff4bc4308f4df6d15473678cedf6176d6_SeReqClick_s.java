 package net.selenate.common.comms.req;
 
 import java.io.Serializable;
 
 public class SeReqClick implements Serializable {
   private static final long serialVersionUID = 1L;
 
   public final SeReqSelectMethod method;
   public final String            selector;
 
   public SeReqClick(
       final SeReqSelectMethod method,
       final String            selector) {
     if (method == null) {
      throw new IllegalArgumentException("method cannot be null!");
     }
     if (selector == null) {
      throw new IllegalArgumentException("selector cannot be null!");
     }
 
     this.method   = method;
     this.selector = selector;
   }
 }
