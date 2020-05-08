 /*
 The contents of this file are subject to the Jbilling Public License
 Version 1.1 (the "License"); you may not use this file except in
 compliance with the License. You may obtain a copy of the License at
 http://www.jbilling.com/JPL/
 
 Software distributed under the License is distributed on an "AS IS"
 basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 License for the specific language governing rights and limitations
 under the License.
 
 The Original Code is jbilling.
 
 The Initial Developer of the Original Code is Emiliano Conde.
 Portions created by Sapienter Billing Software Corp. are Copyright 
 (C) Sapienter Billing Software Corp. All Rights Reserved.
 
 Contributor(s): ______________________________________.
 */
 
 /*
  * Created on Jan 15, 2005
  *
  */
 package com.sapienter.jbilling.server.user;
 
 import com.sapienter.jbilling.server.util.EventLogger;
 
 /**
  * @author Emil
  *
  */
 public interface UserSQL {
     static final String findActiveWithOpenInvoices =
         "SELECT a.id "+ 
         "FROM base_user a, customer c "+
         "WHERE a.status_id = 1 "+
         "AND c.exclude_aging = 0 "+
         "AND a.deleted = 0 " +
         "AND a.id = c.user_id " +
         "AND exists (" +
         "    select 1 " +
         "      from invoice i" +
         "     where i.to_process = 1 " +
         "       and i.user_id = a.id " +
         "       and i.is_review = 0  " +
         "       and i.deleted = 0 " +
         "    )";
     
     static final String findUserTransitions =
      	"SELECT el.id, el.old_str, el.create_datetime, el.old_num, el.user_id" +
         " FROM event_log el" +
         " WHERE el.module_id = " + EventLogger.MODULE_USER_MAINTENANCE  + 
        " AND el.message_id = " + EventLogger.SUBSCRIPTION_STATUS_CHANGE+ " AND el.entity_id = ?";
     
     static final String findUserTransitionsByIdSuffix =
     	  " AND el.id > ?";
     
     static final String findUserTransitionsByDateSuffix =
     	  " AND el.create_datetime >= ?";
     
     static final String findUserTransitionsUpperDateSuffix =
     	  " AND el.create_datetime <= ?";
 }
