 /*
  * Dispatcher.java
  * Copyright (C) 2011,2012 Wannes De Smet
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.xenmaster.controller;
 
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.xenmaster.connectivity.Connections;
 
 /**
  * 
  * @created Oct 2, 2011
  * @author double-u
  */
 public class Dispatcher {
 
     protected Connections conn;
 
     public Dispatcher(URL xen) {
         this.conn = new Connections(xen);
     }
 
     public Dispatcher(Connections conn) {
         this.conn = conn;
     }
 
     public Object dispatch(String methodName, Object[] params) throws BadAPICallException {
         return dispatch(methodName, params, 0);
     }
 
     public Object dispatch(String methodName, Object[] params, int connection) throws BadAPICallException {
         ArrayList list = new ArrayList();
         CollectionUtils.addAll(list, params);
 
         return execute(methodName, list, connection);
     }
 
     protected Object execute(String methodName, List params, int connection) throws BadAPICallException {
         // Preflight check
         for (Object o : params) {
             if (o == null) {
                 throw new BadAPICallException(methodName, params, "Illegal argument", "A null argument has been passed");
             }
         }
 
         Map result = this.conn.executeCommand(methodName, params, connection);
         if (result == null) {
             throw new BadAPICallException(methodName, params);
         }
 
         switch (result.get("Status").toString()) {
             case "Success":
                 return result.get("Value");
             case "Failure":
                 Object[] info = (Object[]) result.get("ErrorDescription");
                 ArrayList<String> errInfo = new ArrayList<>();
                 for (Object o : info) {
                     if (o instanceof String) {
                         errInfo.add((String) o);
                     }
                 }
 
                
                String prettyMessage = errInfo.get(0).toLowerCase().replace("_", " ");
                if( prettyMessage.length() > 0 )
                    prettyMessage = prettyMessage.substring(0,1).toUpperCase() + prettyMessage.substring(1);
                
                throw new BadAPICallException(methodName, params, prettyMessage, errInfo);
             default:
                 return null;
         }
     }
 
     public Object dispatchWithSession(String methodName, Object[] params) throws BadAPICallException {
         return dispatchWithSession(methodName, params, 0);
     }
 
     public Object dispatchWithSession(String methodName, Object[] params, int connection) throws BadAPICallException {
         if (conn.getSession().getReference() == null) {
             throw new Error("Session has not been initialized");
         }
         ArrayList list = new ArrayList();
         list.add(conn.getSession().getReference());
         CollectionUtils.addAll(list, params);
 
         return execute(methodName, list, connection);
     }
 
     public Connections getConnections() {
         return conn;
     }
 }
