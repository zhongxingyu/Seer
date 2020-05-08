 /*
  * Copyright (c) 2010 StockPlay development team
  * All rights reserved.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 package com.kapti.backend.xmlrpc;
 
 import com.kapti.backend.security.SessionsHandler;
 import com.kapti.data.persistence.StockPlayDAO;
 import org.apache.xmlrpc.XmlRpcRequest;
 import org.apache.xmlrpc.common.XmlRpcHttpRequestConfig;
 import org.apache.xmlrpc.server.AbstractReflectiveHandlerMapping.AuthenticationHandler;
 
 /**
  * \brief   Authenticatie-module voor de XML-RPC server.
  *
  * Deze module biedt de nodige functionaliteit om verschillende gebruikers
  * toe te laten op eenzelfde XML-RPC servlet. Daarbij vraagt de server aan
  * deze AuthHandler of een bepaalde user/password combinatie klopt (en de
  * gebruiker de correcte rechten heeft om de servlet te benaderen), en gebruikt
  * de return value van die isAuthorized aanroep om toegang tot de servlet
  * toe te staan of eventueel te blokkeren.
  */
 public class AuthHandler implements AuthenticationHandler {
     //
     // Dataleden
     //
     
     private StockPlayDAO mDAO;
     private SessionsHandler mSessions;
     
     
     //
     // Constructie
     //
 
     public AuthHandler(StockPlayDAO iDAO, SessionsHandler iSessions) {
         super();
         mDAO = iDAO;
         mSessions = iSessions;
     }
 
 
     //
     // Methoden
     //
 
    public boolean isAuthorized(XmlRpcRequest pRequest) {
         // Haal credentials op
         XmlRpcHttpRequestConfig config = (XmlRpcHttpRequestConfig) pRequest.getConfig();
         String sessionid = config.getBasicUserName();
 
         // Genereer algemene functienaam
         String tMethod = pRequest.getMethodName();
 
         // Genereer specifieke functienaam
         StringBuilder tMethodFullBuilder = new StringBuilder(pRequest.getMethodName());
         tMethodFullBuilder.append('(');
         for (int i = 0; i < pRequest.getParameterCount(); i++) {
             tMethodFullBuilder.append(pRequest.getParameter(i).getClass().getName());
             if (i < pRequest.getParameterCount() - 1)
                 tMethodFullBuilder.append(',');
         }
         tMethodFullBuilder.append(')');
         String tMethodFull = tMethodFullBuilder.toString();
 
         // Controleer
         if (mSessions.containsDefinition(tMethodFull)) {
             return mSessions.verifyRequest(sessionid, tMethodFull);
         } else if (mSessions.containsDefinition(tMethod)) {
             return mSessions.verifyRequest(sessionid, tMethod);
         } else {
             return false;
         }
     }
 }
