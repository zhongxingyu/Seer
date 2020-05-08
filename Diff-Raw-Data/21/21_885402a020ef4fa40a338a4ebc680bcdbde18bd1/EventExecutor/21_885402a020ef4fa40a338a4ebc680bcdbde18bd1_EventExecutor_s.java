 /**
  *  This file is part of DORS API.
  * 
  *  DORS API - Java is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  DORS API - Java is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with  DORS API - Java.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.dors.event;
 
 import com.dors.Job;
 import com.dors.util.JobList;
 import java.util.List;
 
 /**
  *
  * @author Faris
  */
 public class EventExecutor {
     
     public static List<Event> activeEvents = new JobList();
     public static List<Event> incomingEvents = new JobList();
     public static List<Event> idleEvents = new JobList();
     
     public void executeEvents(){
         if(activeEvents.isEmpty())
             return;
         for(Event event : activeEvents){
             if(event.execute() == Job.EXECUTOR_FINALIZE){
                 event.finalise();
             }
         }
     }
     
     public void syncIncomingEvents(){
         if(incomingEvents.isEmpty())
             return;
         activeEvents.addAll(incomingEvents);
         incomingEvents.clear();
     }
 
 }
