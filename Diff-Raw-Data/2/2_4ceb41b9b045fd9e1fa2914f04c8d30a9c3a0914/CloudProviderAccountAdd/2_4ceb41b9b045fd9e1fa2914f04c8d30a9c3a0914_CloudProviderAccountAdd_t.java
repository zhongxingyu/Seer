 /**
  *
  * SIROCCO
  * Copyright (C) 2011 France Telecom
  * Contact: sirocco@ow2.org
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
  * USA
  *
  *  $Id$
  *
  */
 
 package org.ow2.sirocco.apis.rest.cimi.tools;
 
 import javax.naming.Context;
 import javax.naming.InitialContext;
 
 import org.ow2.sirocco.apis.rest.cimi.sdk.CimiClient;
 import org.ow2.sirocco.cloudmanager.core.api.ICloudProviderManager;
 import org.ow2.sirocco.cloudmanager.core.api.IRemoteCloudProviderManager;
 
 import com.beust.jcommander.Parameter;
 import com.beust.jcommander.Parameters;
 
 @Parameters(commandDescription = "add cloud provider account to user")
 public class CloudProviderAccountAdd implements Command {
     public static String COMMAND_NAME = "cloud-provider-account-add";
 
     @Parameter(names = "-account", description = "account id", required = true)
     private String accountId;
 
    @Parameter(names = "-user", description = "user id", required = true)
     private String userId;
 
     @Override
     public String getName() {
         return CloudProviderAccountAdd.COMMAND_NAME;
     }
 
     @Override
     public void execute(final CimiClient cimiClient) throws Exception {
         Context context = new InitialContext();
         IRemoteCloudProviderManager cloudProviderManager = (IRemoteCloudProviderManager) context
             .lookup(ICloudProviderManager.EJB_JNDI_NAME);
 
         cloudProviderManager.addCloudProviderAccountToUser(this.userId, this.accountId);
 
     }
 }
