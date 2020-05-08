 //////////////////////////////////////////////////////////////////////////////
 // Clirr: compares two versions of a java library for binary compatibility
 // Copyright (C) 2003  Lars Khne
 //
 // This library is free software; you can redistribute it and/or
 // modify it under the terms of the GNU Lesser General Public
 // License as published by the Free Software Foundation; either
 // version 2.1 of the License, or (at your option) any later version.
 //
 // This library is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 // Lesser General Public License for more details.
 //
 // You should have received a copy of the GNU Lesser General Public
 // License along with this library; if not, write to the Free Software
 // Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 //////////////////////////////////////////////////////////////////////////////
 
 package net.sf.clirr.ant;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import net.sf.clirr.event.ApiDifference;
 import net.sf.clirr.event.DiffListenerAdapter;
 import net.sf.clirr.event.Severity;
 import org.apache.tools.ant.Project;
 import org.apache.tools.ant.Task;
 
 final class AntLogger extends DiffListenerAdapter
 {
     private Task task;
 
     private Map severityPrioMap = new HashMap(3);
 
     AntLogger(Task task)
     {
         this.task = task;
         severityPrioMap.put(Severity.INFO, new Integer(Project.MSG_INFO));
         severityPrioMap.put(Severity.WARNING, new Integer(Project.MSG_WARN));
         severityPrioMap.put(Severity.ERROR, new Integer(Project.MSG_ERR));
     }
 
     public void reportDiff(ApiDifference difference)
     {
         final Severity severity = difference.getSeverity();
         final Integer prio = (Integer) severityPrioMap.get(severity);
        task.log(severity.toString() + ": " + difference.getReport(), prio.intValue());
     }
 }
