 /**
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright (c) 2008 Sun Microsystems Inc. All Rights Reserved
  *
  * The contents of this file are subject to the terms
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
 * $Id: ServerConfigBase.java,v 1.1 2008-09-19 23:37:14 beomsuk Exp $
  *
  */
 
 package com.sun.identity.cli.serverconfig;
 
 import com.sun.identity.cli.AuthenticatedCommand;
 import com.sun.identity.cli.CLIException;
 import com.sun.identity.cli.IArgument;
 import com.sun.identity.cli.RequestContext;
 
 public class ServerConfigBase extends AuthenticatedCommand {
     /**
      * Services a Commandline Request.
      *
      * @param rc Request Context.
      * @throw CLIException if the request cannot serviced.
      */
     public void handleRequest(RequestContext rc)
         throws CLIException {
         super.handleRequest(rc);
         String serverName = getStringOptionValue(IArgument.SERVER_NAME);
         if ((serverName != null) && (serverName.trim().length() > 0)) {
             System.setProperty("com.sun.identity.jaxrpc.url",
                 serverName + "/jaxrpc");
         }
     }
 }
