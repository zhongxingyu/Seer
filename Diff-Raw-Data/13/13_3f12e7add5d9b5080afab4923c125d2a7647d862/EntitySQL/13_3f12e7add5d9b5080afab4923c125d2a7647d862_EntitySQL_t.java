 /*
  * JBILLING CONFIDENTIAL
  * _____________________
  *
  * [2003] - [2012] Enterprise jBilling Software Ltd.
  * All Rights Reserved.
  *
  * NOTICE:  All information contained herein is, and remains
  * the property of Enterprise jBilling Software.
  * The intellectual and technical concepts contained
  * herein are proprietary to Enterprise jBilling Software
  * and are protected by trade secret or copyright law.
  * Dissemination of this information or reproduction of this material
  * is strictly forbidden.
  */
 
 /*
  * Created on Jul 9, 2005
  *
  */
 package com.sapienter.jbilling.server.user;
 
 /**
  * @author Emil
  *
  */
 public interface EntitySQL {
     // needed for the billing process, to avoid starting a transaction
     // since J2EE Collections have always to be in a transaction :(
     static final String listAll = 
         "select id" +
         "  from entity" +
         " order by 1";
     
     // another query that should not exist. Please remove when entities
     // are replaced by JPAs
     static final String getTables = 
         "select name, id " +
         "  from jbilling_table";
  
     static final String findRoot = 
         "select id " +
         "  from base_user b, user_role_map m" +
         " where entity_id = ? " +
         "   and m.user_id = b.id " +
        "   and m.role_id = ? " +
         " order by 1";
 }
