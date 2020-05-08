 package org.coderthoughts.cloud.framework.service.impl;
 
import java.util.Arrays;

 import org.apache.cxf.dosgi.dsw.ClientInfo;
 import org.coderthoughts.cloud.framework.service.api.FrameworkStatus;
 import org.osgi.service.monitor.MonitorAdmin;
 import org.osgi.util.tracker.ServiceTracker;
 
 public class FrameworkStatusImpl implements FrameworkStatus {
     private final ClientInfo client;
     private final ServiceTracker monitorAdminTracker;
 
     public FrameworkStatusImpl(ClientInfo client, ServiceTracker monitorAdminST) {
         this.client = client;
         this.monitorAdminTracker = monitorAdminST;
     }
 
     @Override
     public String getFrameworkVariable(String name) {
         System.out.println("*** Obtaining framework variable: " + name + " requester IP:" + client);
         if (FV_AVAILABLE_MEMORY.equals(name)) {
             return "" + Runtime.getRuntime().freeMemory();
         }
         throw new IllegalArgumentException(name);
     }
 
 
     @Override
     public String[] getFrameworkVariableNames() {
         return new String [] {FV_AVAILABLE_MEMORY}; // TODO take FrameworkStatusAddition into account
     }
 
     @Override
     public String getServiceVariable(long serviceID, String name) {
         System.out.println("*** Obtaining info: " + name + " for " + serviceID + " requester IP:" + client);
         MonitorAdmin ma = (MonitorAdmin) monitorAdminTracker.getService();
         if (ma == null)
             return SERVICE_STATUS_SERVER_ERROR;
 
         String monitorableId = MONITORABLE_SERVICE_PID_PREFIX + serviceID;
        String variableName = name + "." + client.getHostIPAddress();
         boolean variableFound = false;
        /* */ System.out.println("@@@ monitorableId: " + monitorableId + " variableName: " + variableName);
        /* */ System.out.println("@@@ " + Arrays.toString(ma.getStatusVariableNames(monitorableId)));
         for (String var : ma.getStatusVariableNames(monitorableId)) {
             // A little awkward to have to go through all the names, but array traversal is quite fast
             if (variableName.equals(var)) {
                 variableFound = true;
                 break;
             }
         }
         if (!variableFound) {
            System.out.println("*** Status variable not found");
             // the service is not yet tracked for the client, we assume that new clients can invoke.
             return SERVICE_STATUS_OK;
         }
 
        String result = ma.getStatusVariable(monitorableId + "/" + variableName).getString();
        System.out.println("*** Status variable returns: " + result);
        return result;
     }
 }
