 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.oneandone.idev.johanna.protocol.impl;
 
 import java.util.logging.Logger;
 import org.oneandone.idev.johanna.protocol.Response;
 import org.oneandone.idev.johanna.store.AbstractSession;
 import org.oneandone.idev.johanna.store.SessionStore;
 
 /**
  *
  * @author kiesel
  */
 public class VarReadRequest extends SessionKeyBasedRequest {
     private static final Logger LOG = Logger.getLogger(VarReadRequest.class.getName());
 
     public VarReadRequest(String command) {
         super(command);
     }
 
     @Override
     protected Response processSessionKey(SessionStore store, AbstractSession s, String name) {
         if (!s.hasValue(name)) return Response.NOKEY;
         
        return new Response(true, name);
     }
 }
