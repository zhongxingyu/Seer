 /*******************************************************************************
  * Copyright (c) 2012 Bryan Hunt.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Bryan Hunt - initial API and implementation
  *******************************************************************************/
 
 package org.eclipselabs.emongo.components;
 
 import java.util.Map;
 
 /**
  * @author bhunt
  * 
  */
 public class MongoDatabaseConfigurationProviderComponent extends AbstractComponent implements MongoAuthenticatedDatabaseConfigurationProvider
 {
 	private volatile String alias;
 	private volatile String databaseName;
 	private volatile String clientId;
 	private volatile String user;
 	private volatile String password;
 
 	public void activate(Map<String, Object> properties)
 	{
 		alias = (String) properties.get(MongoAuthenticatedDatabaseConfigurationProvider.PROP_ALIAS);
 		clientId = (String) properties.get(MongoAuthenticatedDatabaseConfigurationProvider.PROP_CLIENT_ID);
 		databaseName = (String) properties.get(MongoAuthenticatedDatabaseConfigurationProvider.PROP_DATABASE);
 		user = (String) properties.get(MongoAuthenticatedDatabaseConfigurationProvider.PROP_USER);
 		password = (String) properties.get(MongoAuthenticatedDatabaseConfigurationProvider.PROP_PASSWORD);
 
 		if (alias == null || alias.isEmpty())
 			handleIllegalConfiguration("The database alias was not found in the configuration properties");
 
 		if (clientId == null || clientId.isEmpty())
 			handleIllegalConfiguration("The MongoDB client id was not found in the configuration properties");
 
 		if (databaseName == null || databaseName.isEmpty())
			handleIllegalConfiguration("The MongoDB database name was not found in the configuration properties");
 	}
 
 	@Override
 	public String getAlias()
 	{
 		return alias;
 	}
 
 	@Override
 	public String getDatabaseName()
 	{
 		return databaseName;
 	}
 
 	@Override
 	public String getClientId()
 	{
 		return clientId;
 	}
 
 	@Override
 	public String getUser()
 	{
 		return user;
 	}
 
 	@Override
 	public String getPassword()
 	{
 		return password;
 	}
 }
