 /**
  * 
  * Copyright 2002 - 2007 NCHELP
  * 
  * Author:	Tim Bornholtz, The Bornholtz Group
  * 			Priority Technologies, Inc.
  * 
  * 
  * This code is part of the Meteor system as defined and specified 
  * by the National Council of Higher Education Loan Programs, Inc. 
  * (NCHELP) and the Meteor Sponsors, and developed by Priority 
  * Technologies, Inc. (PTI). 
  *
  * 
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *	
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *	
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  ********************************************************************************/
 
 package org.nchelp.meteor.provider.access;
 
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.nchelp.meteor.message.MeteorIndexResponse;
 import org.nchelp.meteor.provider.DataProvider;
 import org.nchelp.meteor.provider.DataProviderList;
 import org.nchelp.meteor.provider.DistributedRegistry;
 import org.nchelp.meteor.provider.IndexProvider;
 import org.nchelp.meteor.provider.MeteorParameters;
 import org.nchelp.meteor.registry.Directory;
 import org.nchelp.meteor.registry.DirectoryFactory;
 import org.nchelp.meteor.security.SecurityToken;
 import org.nchelp.meteor.util.Cache;
 import org.nchelp.meteor.util.Messages;
 import org.nchelp.meteor.util.exception.AssertionException;
 import org.nchelp.meteor.util.exception.DirectoryException;
 import org.nchelp.meteor.util.exception.IndexException;
 import org.nchelp.meteor.util.exception.ParsingException;
 
 
 /**
  * IndexProviderService.java This is the only class in the entire Access
  * Provider software that knows anything about the entire notion of Index
  * Providers
  * 
  * @since May 13, 2003
  */
 public class IndexProviderService
 {
 	private static final Log log = LogFactory.getLog(IndexProviderService.class);
 	private static Cache iProviderCache = new Cache();
 
 	/**
 	 * @deprecated
 	 */
 	public DataProviderList getDataProviders (DistributedRegistry registry, ResponseData response, MeteorParameters params)
 	{
 		log.error("Calling the deprecated method IndexProviderService.getDataProviders() with no LoanLocator object.");
 		return this.getDataProviders(registry, response, params, null);
 	}
 	
 	public DataProviderList getDataProviders (DistributedRegistry registry, ResponseData response, MeteorParameters params, LoanLocator loanLocator)
 	{
 		List iProviders = null;
 		DataProviderList dProviders = new DataProviderList();
 
 		iProviders = this.getIndexProviderList(registry);
 		if (iProviders == null)
 		{
 			iProviders = new ArrayList();
 		}
 
 		if (iProviders.isEmpty())
 		{
 			// There's really no point in going on here
 			dProviders.addMessage(Messages.getMessage("registry.noindex"));
 			return dProviders;
 		}
 
 		// now request a list of data providers from each index provider.
 		Iterator iterator = iProviders.iterator();
 		IndexProvider iProvider = null;
 
 		while (iterator.hasNext())
 		{
 			iProvider = (IndexProvider)iterator.next();
 			MeteorIndexResponse ipResp = null;
 			try
 			{
 				if (iProvider == null)
 				{
 					log.error("The IndexProvider object is null");
 					continue;
 				}
 				ipResp = iProvider.getDataProviders(params);
 			}
 			catch (IndexException ex)
 			{
 				// Is this really something we want to show to the user?!?!
 				// I think not.
 				log.error(ex);
 
 				dProviders.addMessage(Messages.getMessage("index.noresponse"));
 			}
 			catch (AssertionException ex)
 			{
 				log.error(ex);
 				dProviders.addMessage(Messages.getMessage("security.tokenexpired"));			
 			}
 			if (ipResp != null)
 			{
 				IndexProvider currentIP = ipResp.getIndexProvider();
 				dProviders = this.aggregateList(currentIP, dProviders,
 				                                ipResp.getDataProviderList(), params,
 				                                loanLocator);
 
 				loanLocator.setIndexProvider(currentIP);
 
 				String message = ipResp.getErrorMessage();
 				if (message != null && message.length() > 0)
 				{
 					dProviders.addMessage(message);
 				}
 			}
 		}
 
 		return dProviders;
 	}
 
 	/**
 	 * Get the list of Index Providers. Cache them in this method if necessary
 	 * 
 	 * @param registry
 	 * @return List
 	 */
 	private List getIndexProviderList (DistributedRegistry registry)
 	{
 		List iProviders = (List)iProviderCache.cached("");
 
 		if (iProviders == null || iProviders.isEmpty())
 		{
 			iProviders = registry.getIndexProviders();
 			if (iProviders != null)
 			{
 				iProviderCache.add("", iProviders);
 			}
 		}
 
 		return iProviders;
 	}
 
 	/**
 	 * As each of the calls to AccessProvider.getDataProviders() returns call this
 	 * method to eliminate any of the duplicate Data Providers
 	 * 
 	 * @param dataProviders
 	 * @return List
 	 */
 	private DataProviderList aggregateList (IndexProvider currentIndexProvider,
 	                                        DataProviderList dataProviders, 
 	                                        List newDataProviders, 
 	                                        MeteorParameters params,
 	                                        LoanLocator loanLocator)
 	{
 		// easiest way to do this is to cast this to a Set and add them then turn it
 		// back into a List
 		boolean unknownDPSent = false;
 		boolean error = false;
 		DataProviderList removedList = new DataProviderList();
 
 		if (newDataProviders == null)
 		{
 			return dataProviders;
 		}
 
 		if (dataProviders == null)
 		{
 			dataProviders = new DataProviderList();
 		}
 
 		// Loop through the newDataProviders and make sure they are real data
 		// providers
 		Iterator iter = newDataProviders.iterator();
 		Map dpMap = new HashMap();
 		while (iter.hasNext())
 		{
 			//make sure error flag is false for next data provider in list
 			error = false;
 
 			DataProvider dp = (DataProvider)iter.next();
 
 			Directory dir;
 			try
 			{
 				dir = DirectoryFactory.getInstance().getDirectory();
 			}
 			catch (DirectoryException ex)
 			{
 				log.error("Error connecting to the registry: " + ex.getMessage());
 				continue;
 			}
 
 			String status = null;
 			try
 			{
 				status = dir.getStatus(dp.getId(), Directory.TYPE_DATA_PROVIDER);
 			}
 			catch (DirectoryException ex)
 			{
 				//
 				// this exception will most often be caused by a data provider not being found in the registry, which is
 				// something expected
 				log.info("DirectoryException while validating the status of the Data Provider: " + dp.getId() +
 				         ". This Data Provider is being removed from the list of providers. Message: " + ex.getMessage());
 				removedList.add(dp);
 				iter.remove();
 				error = true;
 			}
 
 			if (!error)  
 			{
 				if (!"AC".equals(status))
 				{
 					log.info("Data Provider: " + dp.getId() + " does not have a status of 'AC' (Active).  It has a status of " + status +
 					         ".  It is being removed from the list of Data Providers returned by the Index Provider");
 					removedList.add(dp);
 					iter.remove();
 					error = true;
 				}
 				else
 				{
 					URL url = null;
 					try
 					{
 						url = dir.getProviderURL(dp.getId(), Directory.TYPE_DATA_PROVIDER);
 					}
 					catch (DirectoryException ex)
 					{
 						log.error("DirectoryException while retrieving the URL of the Data Provider: " + dp.getId() +
 						          ". This Data Provider is being removed from the list of providers. Message: " + ex.getMessage());
 						removedList.add(dp);
 						iter.remove();
 						error = true;
 						url = null;
 					}
 					if (!error)
 					{
 						if (url != null)
 						{
 							dp.setURL(url);
 							String urlString = url.toString();
 							Object object = dpMap.get(urlString);
 
 							if (object == null)
 							{
 								// Load new dp into hashmap since not already present
 								dpMap.put(urlString, dp.getId());
 								log.debug("data provider " + dp.getId() + " added with URL: " + urlString);
 							}
 							else
 							{
 								// Remove dp if url already present in map
 								iter.remove();
 								log.debug("duplicate URL from data provider " + dp.getId() + " with URL: " + urlString);
 							}
 						}
 						else
 						{
 							removedList.add(dp);
 							iter.remove();
 							log.warn("URL missing in the registry for Data Provider: " + dp.getId() + " - removing data provider from list");
 							error = true;
 						}
 					}
 				}
 
 				if (!error)
 				{
 					try
 					{
 						// I don't actually care what the value is here. This method will
 						// throw an exception when the value isn't found
 						// There is logic elsewhere to determine if this level is high enough
 						// or not. For now we just don't care.
 						dir.getAuthenticationLevel(dp.getId(), "1", Directory.TYPE_DATA_PROVIDER, params.getRole());
 					}
 					catch (DirectoryException ex)
 					{
 						// If the DirectoryException is thrown then the value we are looking
 						// for does not exist in the registry
 						log.info("Data Provider: " + dp.getId() + " does not support the role: " + params.getRole() +
 						         " and is being removed from the list of available providers");
 						removedList.add(dp);
 						iter.remove();
 						error = true;
 					}
 				}
 			}
 
 		}
 
 		// If an error occurred but this Data Provider hasn't been added to the
 		// response yet then
 		//	put this DataProvider into the master response document to be
 		// displayed to the user
 		if (removedList.size() > 0 &&
 		    !SecurityToken.roleAPCSR.equals(params.getRole()) &&
 		    !SecurityToken.roleLENDER.equals(params.getRole()))
 		{
 			if(loanLocator != null){
 				loanLocator.addDataProvider(removedList);
 			}
 		}
 
 		dataProviders.addAll(newDataProviders);
 		return dataProviders;
 	}
 
 }
 
