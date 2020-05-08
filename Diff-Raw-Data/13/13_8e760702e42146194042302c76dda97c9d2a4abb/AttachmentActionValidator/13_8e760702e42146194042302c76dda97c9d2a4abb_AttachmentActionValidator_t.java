 /*
  * Copyright © 2010 Red Hat, Inc.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package com.redhat.rhevm.api.common.resource;
 
 import com.redhat.rhevm.api.model.ActionValidator;
 import com.redhat.rhevm.api.model.Attachment;
 
 public class AttachmentActionValidator implements ActionValidator {
     private Attachment attachment;
 
     public AttachmentActionValidator(Attachment attachment) {
         this.attachment = attachment;
     }
 
     @Override
     public boolean validateAction(String action) {
         switch (attachment.getStatus()) {
         case ACTIVE:
             return action.equals("deactivate");
         case INACTIVE:
             return action.equals("activate");
         case UNINITIALIZED:
         case UNATTACHED:
         case LOCKED:
         case MIXED:
         default:
             assert false : attachment.getStatus();
             return false;
         }
     }
 }
