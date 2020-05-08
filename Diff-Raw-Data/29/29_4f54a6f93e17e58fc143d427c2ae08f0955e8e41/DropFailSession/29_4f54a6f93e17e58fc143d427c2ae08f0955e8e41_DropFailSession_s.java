 /**
  * Appia: Group communication and protocol composition framework library
  * Copyright 2006 University of Lisbon
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License. 
  *
  * Initial developer(s): Alexandre Pinto and Hugo Miranda.
  * Contributor(s): See Appia web page for a list of contributors.
  */
  /**
  * 
  */
 package org.continuent.appia.test.perf.vsyncvalid;
 
 import java.net.InetSocketAddress;
 import java.net.UnknownHostException;
 import java.text.ParseException;
 
 import org.continuent.appia.core.AppiaEventException;
 import org.continuent.appia.core.Direction;
 import org.continuent.appia.core.Event;
 import org.continuent.appia.core.Layer;
 import org.continuent.appia.core.Session;
 import org.continuent.appia.protocols.utils.ParseUtils;
 import org.continuent.appia.test.perf.PerfCastEvent;
 import org.continuent.appia.xml.interfaces.InitializableSession;
 import org.continuent.appia.xml.utils.SessionProperties;
 
 
 /**
  * @author alexp
  *
  */
 public class DropFailSession extends Session implements InitializableSession {
 
   /**
    * Default number of messages before failing.
    */
   public static final int DEFAULT_MSGS_TO_FAIL=50;
   
   /**
    * Deafult number of messages after failing after wich it exits.
    */
   public static final int DEFAULT_MSGS_TO_EXIT=5;
     
   /**
    * @param layer
    */
   public DropFailSession(Layer layer) {
     super(layer);
   }
   
   private int msgs_to_fail=DEFAULT_MSGS_TO_FAIL;
   private int msgs_to_exit=DEFAULT_MSGS_TO_EXIT;
   private int countMsgs=0;
   private InetSocketAddress destination=null;
 
   public void init(SessionProperties params) {
     if (params.containsKey("fail"))
       msgs_to_fail=params.getInt("fail");
     if (params.containsKey("exit"))
       msgs_to_exit=params.getInt("exit");
     if (params.containsKey("destination")) {
       try {
        destination=ParseUtils.parseInetWithPort(params.getString("destination"), null, -1);
       } catch (UnknownHostException e) {
         e.printStackTrace();
         System.exit(1);
       } catch (ParseException e) {
         e.printStackTrace();
         System.exit(1);
       }
     }
     
     if (destination == null) {
       System.err.println("Require destination address of messages to drop.");
       System.exit(1);
     }
   }
 
   public void handle(Event event) {
     if (event instanceof PerfCastEvent)
       handlePerfCastEvent((PerfCastEvent)event);
     else
       try { event.go(); } catch (AppiaEventException ex) { ex.printStackTrace(); }
   }
 
   private void handlePerfCastEvent(PerfCastEvent event) {
     if (event.getDir() == Direction.UP) {
       try { event.go(); } catch (AppiaEventException ex) { ex.printStackTrace(); }
       return;
     }
     
     if ((destination != null) && event.dest.equals(destination)) {
       ++countMsgs;
       System.out.println("msgs to "+event.dest+" = "+countMsgs+"("+msgs_to_fail+")");
       if (countMsgs >= msgs_to_fail) {
         System.out.println("Droping Message");
         if (countMsgs >= msgs_to_fail+msgs_to_exit) {
           System.out.println("Exiting");
           System.exit(0);
         }
         return;      
       }
     }
     try { event.go(); } catch (AppiaEventException ex) { ex.printStackTrace(); }
   }
 
 }
