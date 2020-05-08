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
 * $Id: ClientsManager.java,v 1.2 2005-12-08 01:16:14 veiming Exp $
  *
  * Copyright 2005 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.iplanet.services.cdm;
 
 import java.util.Iterator;
 import java.util.Map;
 
 import com.iplanet.am.util.AMClientDetector;
 
 /**
  * Provides common access to client data.
  * <p>
  * <p>
  * Client data is accessed for a particular client type. The underlying client
  * data is stored in the profile service, but this interface should always used
  * for accessing it (not by accessing the profile directly).
  * 
  * <p>
  * Client does not allow clients to modify the Client Data unless they have a
  * valid user sso token. clients trying to modify Client Data should explicitly
  * call store() to save changes into SMS. versions of the getInstance methods
  * that accept a SSO token should be used if the client code wishes to set
  * client data.
  * @supported.api
  */
 public class ClientsManager {
 
     protected static ClientTypesManager clientTypesManager = AMClientDetector
             .getClientTypesManagerInstance();
 
     /**
      * Constructor
      */
     public ClientsManager() {
     }
 
     /**
     * Get Client instance for a specific client type.
      * 
      * @param clientType
      *            Client Type.
      * @return Requested Client instance.
      * @throws ClientException
      *             if specified client type is null or not defined.
      * @supported.api
      */
     public static Client getInstance(String clientType) throws ClientException {
         if (clientType == null) {
             throw new ClientException(CDMBundle.getString("null_clientType"));
         }
        Client client = clientTypesManager.getClientInstance(clientType);
         if (client == null) {
            throw new ClientException(CDMBundle.getString("unknown_clientType")
                    + " :: " + clientType);
         }
         return client;
     }
 
     /**
      * Get a Client instance for the default client type
      * 
      * @return The Client instance corresponding to the default client type
      * @supported.api
      */
     public static Client getDefaultInstance() {
         String def = clientTypesManager.getDefaultClientType();
         return clientTypesManager.getClientInstance(def);
     }
 
     /**
      * Returns an iterator of Client objects for all known client types.
      * 
      * @return Iterator of Client objects
      * @supported.api
      */
     public static Iterator getAllInstances() {
         Map allInstances = clientTypesManager.getAllClientInstances();
         return allInstances.values().iterator();
     }
 }
