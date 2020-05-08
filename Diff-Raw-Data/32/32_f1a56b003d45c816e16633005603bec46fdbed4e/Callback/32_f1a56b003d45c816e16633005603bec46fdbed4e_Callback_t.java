 /*
    Copyright (C) 2012-2013 Anton Lobov <zhuravlik> <ahmad200512[at]yandex.ru>
 
    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 3 of the License, or (at your option) any later version.
 
    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.
 
    You should have received a copy of the GNU Lesser General
    Public License along with this library; if not, write to the
    Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
    Boston, MA 02110-1301 USA
  */
 
 package zhuravlik.ant.vix;
 
 import com.sun.jna.Pointer;
 import org.apache.tools.ant.Project;
 
 /**
  * Created with IntelliJ IDEA.
  * User: anton
  * Date: 4/21/13
  * Time: 12:04 PM
  * To change this template use File | Settings | File Templates.
  */
 public class Callback implements VixEventProcCallback {
 
     public interface SetPropertiesProc {
         public void setProperties(int handle, int eventType, int moreEventInfo, Pointer clientData);
     }
 
     private final int eventType;
     private final String targetName;
     private Project project;
     private SetPropertiesProc proc;
 
     public Callback(int eventType, String targetName, Project project) {
         this(eventType, targetName, project, null);
     }
 
     public Callback(int eventType, String targetName, Project project, SetPropertiesProc proc) {
         this.eventType = eventType;
         this.targetName = targetName;
         this.project = project;
         this.proc = proc;
     }
 
 
     @Override
     public void VixEventProc(int handle, int eventType, int moreEventInfo, Pointer clientData) {
        if (eventType != this.eventType)
             return;
 
         if (proc != null)
             proc.setProperties(handle, eventType, moreEventInfo, clientData);

        if (targetName != null)
            project.executeTarget(targetName);
     }
 }
