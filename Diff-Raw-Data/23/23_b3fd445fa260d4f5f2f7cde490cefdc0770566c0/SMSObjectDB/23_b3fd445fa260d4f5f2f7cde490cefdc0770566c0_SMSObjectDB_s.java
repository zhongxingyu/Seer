 /* The contents of this file are subject to the terms
  * of the Common Development and Distribution License
  * (the License). You may not use this file except in
  * compliance with the License.
  *
  * You can obtain a copy of the License at
  * https://opensso.dev.java.net/public/CDDLv1.0.html or
  * opensso/legal/CDDLv1.0.txt
  * See the License for the specific language governing
  * permission and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL
  * Header Notice in each file and include the License file
  * at opensso/legal/CDDLv1.0.txt.
  * If applicable, add the following below the CDDL Header,
  * with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
 * $Id: SMSObjectDB.java,v 1.1 2006-12-15 01:14:12 goodearth Exp $
  *
  * Copyright 2005 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.sm;
 
 import com.iplanet.services.ldap.*;
 
import com.iplanet.ums.DataLayer;

 import com.iplanet.ums.IUMSConstants;
 
 import com.iplanet.ums.UMSException;
 
 
 
 /**
  * Abstract class that needs to be implemented to get root suffix 
  * configuration data from a datastore.
  */
 public abstract class SMSObjectDB extends SMSObject {
 
    static DataLayer dlayer;

     static String amsdkbaseDN;
 
     /**
      * Returns the AMSDK BaseDN for the UM objects.
      * This is the root suffix.
      */
     public String getAMSdkBaseDN() {
         try {
            // Get UMS datalayer

           //After Directory Server support is given, uncomment this.
          
            /*dlayer = DataLayer.getInstance();
            if (debug().messageEnabled()) {
                debug().message("SMSObjectDB: DataLayer instance obtained.");
            }
             // Use puser id just to get the baseDN from serverconfig.xml
             // from "default" server group.
             ServerInstance serverInstanceForUM = null;
             DSConfigMgr mgr = DSConfigMgr.getDSConfigMgr();
             if (mgr != null) {
                 serverInstanceForUM =
                     mgr.getServerInstance(LDAPUser.Type.AUTH_PROXY);
             }
             if (serverInstanceForUM != null) {
                 amsdkbaseDN = serverInstanceForUM.getBaseDN();
             }
            if ((mgr == null) || (dlayer == null) ||
                 (serverInstanceForUM == null)) {
                 debug().error("SMSObject: Unable to initialize LDAP");
                 throw (new SMSException(IUMSConstants.UMS_BUNDLE_NAME,
                     IUMSConstants.CONFIG_MGR_ERROR, null));
             }
             if (debug().messageEnabled()) {
                 debug().message("SMSObjectDB: amsdkbasedn: "+amsdkbaseDN);
            }*/
 
         } catch (Exception e) {
             // Unable to initialize (trouble!!)
             debug().error("SMSObject:getAMSdkBaseDN(): Unable to initalize(exception):", e);
 
         }
         return (amsdkbaseDN);
     }
 }
