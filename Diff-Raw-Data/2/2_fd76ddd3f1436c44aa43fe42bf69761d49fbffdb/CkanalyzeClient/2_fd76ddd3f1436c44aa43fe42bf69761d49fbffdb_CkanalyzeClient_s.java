 /**
  * *****************************************************************************
  * Copyright 2012-2013 Trento Rise (www.trentorise.eu/)
  *
  * All rights reserved. This program and the accompanying materials are made
  * available under the terms of the GNU Lesser General Public License (LGPL)
  * version 2.1 which accompanies this distribution, and is available at
  *
  * http://www.gnu.org/licenses/lgpl-2.1.html
  *
  * This library is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  * details.
  *
  *******************************************************************************
  */
 
 package eu.trentorise.opendata.ckanalyze.client;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.WebResource;
 
 import eu.trentorise.opendata.ckanalyze.client.exceptions.CkanalyzeClientLocalException;
 import eu.trentorise.opendata.ckanalyze.client.exceptions.CkanalyzeClientRemoteException;
 import eu.trentorise.opendata.ckanalyze.client.exceptions.CkanalyzeClientResourceNotFoundException;
 import eu.trentorise.opendata.ckanalyze.model.JSONIZEDException;
 import eu.trentorise.opendata.ckanalyze.model.Status;
 import eu.trentorise.opendata.ckanalyze.model.catalog.CatalogStats;
 import eu.trentorise.opendata.ckanalyze.model.configuration.ScheduleResponse;
 import eu.trentorise.opendata.ckanalyze.model.resources.ResourceStats;
 /**
  * Client main class
  * @author Alberto Zanella <a.zanella@trentorise.eu>
  *Last modified by azanella On 31/lug/2013
  */
 public class CkanalyzeClient {
 	private String basePath;
 	private Client client;
 	private static final String JSON = "application/json";
 	private static final String RES_NOT_FOUND = "resource id not found";
 	private static final String UTF8 = "UTF-8";
 	private static final int REQUEST_OK = 200;
 	/**
 	 * 
 	 * @param basePath -- the baseURL (domain) i.e. http://localhost:8080/ckanalyze-web
 	 */
 	public CkanalyzeClient(String basePath) {
 		super();
 		this.basePath = basePath + "/rest".replaceAll("//", "/");
 		this.client = Client.create();
 	}
 
 	/**
 	 * Provide catalog statistics
 	 * @param catalogName -- name of the catalog (URL)
	 * @return object containing catalog statistics or null if exceptions are throws
 	 * 
 	 * 
 	 */
 	public CatalogStats getCatalogStats(String catalogName)
 	{
 		CatalogStats retval = null;
 		try {
 			if (catalogName.isEmpty()) {
 				emptyCatalog();
 			}
 			String par = URLEncoder.encode(catalogName, UTF8);
 			String url = basePath + "/stats?catalog=" + par;
 			WebResource resource = client.resource(url);
 			ClientResponse response = resource.accept(JSON).get(
 					ClientResponse.class);
 			if (response.getStatus() != REQUEST_OK) {
 				throw new CkanalyzeClientRemoteException(response.getEntity(
 						JSONIZEDException.class).getErrorDescription());
 			} else {
 				retval = response.getEntity(CatalogStats.class);
 			}
 		} catch (UnsupportedEncodingException e) {
 			unsupportedEncoding(e);
 		}
 		return retval;
 	}
 
 	/**
 	 * Provide resource statistics . This method could throw specific CkanResourceNotFoundException
 	 * @param catalogName -- name of the catalog (URL)
 	 * @param resourceId -- CKAN-Id of the required resource
 	 * @return an object containing Resource statistics or null if  UnsupportedEncodingException  is thrown
 	 * 
 	 * 
 	 */
 	public ResourceStats getResourceStats(String catalogName, String resourceId)
 	{
 		ResourceStats retval = null;
 		try {
 			if (catalogName.isEmpty()) {
 				emptyCatalog();
 			}
 			if (resourceId.isEmpty()) {
 				emptyResource();
 			}
 			String catEsc = URLEncoder.encode(catalogName, UTF8);
 			String residEsc = URLEncoder.encode(resourceId, UTF8);
 			String url = basePath + "/resource-stats?catalog=" + catEsc
 					+ "&idResource=" + residEsc;
 			WebResource resource = client.resource(url);
 			ClientResponse response = resource.accept(JSON).get(
 					ClientResponse.class);
 			if (response.getStatus() != REQUEST_OK) {
 				if(response.getEntity(
 						JSONIZEDException.class).getErrorDescription().contains(RES_NOT_FOUND))
 				{
 					throw new CkanalyzeClientResourceNotFoundException();
 				}
 				throw new CkanalyzeClientRemoteException(response.getEntity(
 						JSONIZEDException.class).getErrorDescription());
 			} else {
 				retval = response.getEntity(ResourceStats.class);
 			}
 		} catch (UnsupportedEncodingException e) {
 			unsupportedEncoding(e);
 		}
 		return retval;
 	}
 	
 	/**
 	 * Provide information about scheduled catalogs
 	 * @param catalogName -- catalog name (URL)
 	 * @return true if the specified catalog is already scheduled, false otherwise.
 	 */
 	public boolean isScheduledCatalog(String catalogName)
 	{
 		Status retval;
 		try {
 			if (catalogName.isEmpty()) {
 				emptyCatalog();
 			}
 			String par = URLEncoder.encode(catalogName, UTF8);
 			String url = basePath + "/is-available?catalog=" + par;
 			WebResource resource = client.resource(url);
 			ClientResponse response = resource.accept(JSON).get(
 					ClientResponse.class);
 			if (response.getStatus() != REQUEST_OK) {
 				throw new CkanalyzeClientRemoteException(response.getEntity(
 						JSONIZEDException.class).getErrorDescription());
 			} else {
 				retval = response.getEntity(Status.class);
 			}
 			return retval.getStatus();
 		} catch (UnsupportedEncodingException e) {
 			unsupportedEncoding(e);
 			return false;
 		}
 	}
 	
 	/**
 	 * Schedule a new catalog
 	 * @param catalogName
 	 * @return
 	 */
 	public ScheduleResponse scheduleCatalog(String catalogName)
 	{
 		ScheduleResponse retval = null;
 		try {
 			if (catalogName.isEmpty()) {
 				emptyCatalog();
 			}
 			String par = URLEncoder.encode(catalogName, UTF8);
 			String url = basePath + "/schedule-catalog?catalog=" + par;
 			WebResource resource = client.resource(url);
 			ClientResponse response = resource.accept(JSON).get(
 					ClientResponse.class);
 			if (response.getStatus() != REQUEST_OK) {
 				throw new CkanalyzeClientRemoteException(response.getEntity(
 						JSONIZEDException.class).getErrorDescription());
 			} else {
 				retval = response.getEntity(ScheduleResponse.class);
 			}
 		} catch (UnsupportedEncodingException e) {
 			unsupportedEncoding(e);
 		}
 		return retval;
 	}
 	
 	private void emptyCatalog()
 	{
 		throw new CkanalyzeClientLocalException(
 				"Empty parameter catalogName");
 	}
 	
 	private void emptyResource()
 	{
 		throw new CkanalyzeClientLocalException(
 				"Empty parameter resourceId");
 	}
 	
 	private void unsupportedEncoding(Throwable e)
 	{
 		throw new CkanalyzeClientLocalException(
 				"Unsupported parameter encoding", e);
 	}
 }
