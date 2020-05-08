 /*-
  * Copyright (c) 2010, Derek Konigsberg
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer. 
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution. 
  * 3. Neither the name of the project nor the names of its
  *    contributors may be used to endorse or promote products derived
  *    from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
  * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
  * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
  * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
  * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
  * OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.logicprobe.LogicMail;
 
 import net.rim.device.api.applicationcontrol.ApplicationPermissions;
 import net.rim.device.api.applicationcontrol.ApplicationPermissionsManager;
 import net.rim.device.api.applicationcontrol.ReasonProvider;
 import net.rim.device.api.i18n.ResourceBundle;
 import net.rim.device.api.system.ApplicationDescriptor;
 import net.rim.device.api.system.ControlledAccessException;
 
 /**
  * Handles all permissions-related tasks within the application.
  */
 public class PermissionsHandler {
     private static ResourceBundle resources = ResourceBundle.getBundle(LogicMailResource.BUNDLE_ID, LogicMailResource.BUNDLE_NAME);
     
     private static ReasonProvider reasonProvider = new ReasonProvider() {
         public String getMessage(int permissionID) {
             switch(permissionID) {
             case ApplicationPermissions.PERMISSION_INTER_PROCESS_COMMUNICATION:
                 return resources.getString(LogicMailResource.PERMISSION_INTER_PROCESS_COMMUNICATION);
             case ApplicationPermissions.PERMISSION_FILE_API:
                 return resources.getString(LogicMailResource.PERMISSION_FILE_API);
             case ApplicationPermissions.PERMISSION_INTERNAL_CONNECTIONS:
                 return resources.getString(LogicMailResource.PERMISSION_INTERNAL_CONNECTIONS);
             case ApplicationPermissions.PERMISSION_EXTERNAL_CONNECTIONS:
                 return resources.getString(LogicMailResource.PERMISSION_EXTERNAL_CONNECTIONS);
             case ApplicationPermissions.PERMISSION_WIFI:
                 return resources.getString(LogicMailResource.PERMISSION_WIFI);
             case ApplicationPermissions.PERMISSION_PIM:
                 return resources.getString(LogicMailResource.PERMISSION_PIM);
             default:
                 return null;
             }
         }
     };
 
     public static void registerReasonProvider() {
         try {
             ApplicationPermissionsManager.getInstance().addReasonProvider(
                     ApplicationDescriptor.currentApplicationDescriptor(),
                     reasonProvider);
         } catch (ControlledAccessException e) {
             // Apparently lack of IPC permissions makes this call non-functional
         }
     }
     
     public static void unregisterReasonProvider() {
         try {
             ApplicationPermissionsManager.getInstance().removeReasonProvider(reasonProvider);
         } catch (ControlledAccessException e) {
             // Apparently lack of IPC permissions makes this call non-functional
         }
     }
     
     /**
      * Check application startup permissions, prompting if necessary.
      *
      * @param checkExtended true, if extended permissions should be verified
      * @return true, if the application has sufficient permissions to start
      */
     public static boolean checkStartupPermissions(boolean checkExtended) {
         ApplicationPermissionsManager permissionsManager = ApplicationPermissionsManager.getInstance();
         ApplicationPermissions originalPermissions = permissionsManager.getApplicationPermissions();
         
        boolean permissionsUsable;
        if(checkExtended) {
            permissionsUsable = hasMinimumPermissions(originalPermissions) && hasExtendedPermissions(originalPermissions);
         }
        else {
            permissionsUsable = hasMinimumPermissions(originalPermissions);
         }
         
        if(permissionsUsable) { return true; }
        
         // Create a permissions request containing a generous set of things we can use
         ApplicationPermissions permRequest = new ApplicationPermissions();
         permRequest.addPermission(ApplicationPermissions.PERMISSION_INTER_PROCESS_COMMUNICATION);
         permRequest.addPermission(ApplicationPermissions.PERMISSION_FILE_API);
         permRequest.addPermission(ApplicationPermissions.PERMISSION_INTERNAL_CONNECTIONS);
         permRequest.addPermission(ApplicationPermissions.PERMISSION_EXTERNAL_CONNECTIONS);
         permRequest.addPermission(ApplicationPermissions.PERMISSION_WIFI);
         permRequest.addPermission(ApplicationPermissions.PERMISSION_PIM);
         
         // Request that the user change permissions
         boolean acceptance = permissionsManager.invokePermissionsRequest(permRequest);
         if(!acceptance) {
             // If the complete request was not accepted, make sure we at least
             // got the minimum required permissions before starting.
             return hasMinimumPermissions(permissionsManager.getApplicationPermissions());
         }
         else {
             return true;
         }
     }
 
     private static boolean hasMinimumPermissions(ApplicationPermissions permissions) {
         return permissions.getPermission(ApplicationPermissions.PERMISSION_INTER_PROCESS_COMMUNICATION) == ApplicationPermissions.VALUE_ALLOW;
     }
 
     private static boolean hasExtendedPermissions(ApplicationPermissions permissions) {
         return permissions.getPermission(ApplicationPermissions.PERMISSION_FILE_API) == ApplicationPermissions.VALUE_ALLOW
                 && permissions.getPermission(ApplicationPermissions.PERMISSION_INTERNAL_CONNECTIONS) == ApplicationPermissions.VALUE_ALLOW
                 && permissions.getPermission(ApplicationPermissions.PERMISSION_EXTERNAL_CONNECTIONS) == ApplicationPermissions.VALUE_ALLOW
                 && permissions.getPermission(ApplicationPermissions.PERMISSION_WIFI) == ApplicationPermissions.VALUE_ALLOW
                 && permissions.getPermission(ApplicationPermissions.PERMISSION_PIM) == ApplicationPermissions.VALUE_ALLOW;
     }
 }
