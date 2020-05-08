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
 package com.sapienter.jbilling.server.util.api;
 
 import java.io.IOException;
 import java.util.Properties;
 
 public final class JbillingAPIFactory {
     static private JbillingAPI api = null;
     static private Properties config = null;
     
     private JbillingAPIFactory() {}; // a factory should not be instantiated
     
     static public JbillingAPI getAPI() 
             throws JbillingAPIException, IOException {
     	if (api == null) {
             config = new Properties();
             config.load(JbillingAPI.class.getResourceAsStream("/jbilling_api.properties"));
 
             String default_api = config.getProperty("default", "axis");
             // check for AXIS
             if (default_api.compareToIgnoreCase("axis") == 0) {
                 String userName = config.getProperty("user_name");
                 String password = config.getProperty("password");
                 String endPoint = config.getProperty("end_point");
                 if (userName == null || password == null || endPoint == null) {
                     throw new JbillingAPIException("properties user_name " +
                             "password end_point are required for AXIS");
                 }
                api =  new AxisAPI(userName,password,endPoint);
             } else {
                 throw new JbillingAPIException("api [" + default_api + "] is not supported");
             }
         }
     	return api;
     }
 }
