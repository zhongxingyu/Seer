 package org.dada.core;
 
 public interface Session extends SessionManager {
 
     boolean ping();
     long getLastPingTime();
     void close() throws Exception;
  
     Data<Object> registerView(Model<Object, Object> model, View<Object> view);
     Data<Object> deregisterView(Model<Object, Object> model, View<Object> view);
     Model<Object, Object> find(Model<Object, Object> model, Object key);
 }
