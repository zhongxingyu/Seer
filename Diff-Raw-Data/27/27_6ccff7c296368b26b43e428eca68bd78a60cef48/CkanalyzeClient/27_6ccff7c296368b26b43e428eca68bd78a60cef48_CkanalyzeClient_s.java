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
 import eu.trentorise.opendata.ckanalyze.model.JSONIZEDException;
 import eu.trentorise.opendata.ckanalyze.model.Status;
 import eu.trentorise.opendata.ckanalyze.model.catalog.CatalogueStat;
 import eu.trentorise.opendata.ckanalyze.model.configuration.ScheduleResponse;
 import eu.trentorise.opendata.ckanalyze.model.resources.ResourceStat;
 /**
  * Client main class
  * @author Alberto Zanella <a.zanella@trentorise.eu>
  *Last modified by azanella On 31/lug/2013
  */
 public class CkanalyzeClient {
 	private String basePath;
 	private Client client;
	private final String JSON = "application/json";
	private final String UTF8 = "UTF-8";
	private final int REQUEST_OK = 200;
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
 	 * Provide catalogue statistics
 	 * @param catalogueName -- name of the catalogue (URL)
 	 * @return object containing catalogue statistics or null if exceptions are throws
 	 * 
 	 * 
 	 */
 	public CatalogueStat getCatalogueStat(String catalogueName)
 	{
 		CatalogueStat retval = null;
 		try {
 			if (catalogueName.isEmpty()) {
 				emptyCatalogue();
 			}
 			String par = URLEncoder.encode(catalogueName, UTF8);
 			String url = basePath + "/stats?catalogue=" + par;
 			WebResource resource = client.resource(url);
 			ClientResponse response = resource.accept(JSON).get(
 					ClientResponse.class);
 			if (response.getStatus() != REQUEST_OK) {
 				throw new CkanalyzeClientRemoteException(response.getEntity(
 						JSONIZEDException.class).getErrorDescription());
 			} else {
 				retval = response.getEntity(CatalogueStat.class);
 			}
 		} catch (UnsupportedEncodingException e) {
 			unsupportedEncoding(e);
 		}
 		return retval;
 	}
 
 	/**
 	 * Provide resource statistics
 	 * @param catalogueName -- name of the catalogue (URL)
 	 * @param resourceId -- CKAN-Id of the required resource
 	 * @return an object containing Resource statistics or null if exceptions are throws
 	 * 
 	 * 
 	 */
 	public ResourceStat getResourceStat(String catalogueName, String resourceId)
 	{
 		ResourceStat retval = null;
 		try {
 			if (catalogueName.isEmpty()) {
 				emptyCatalogue();
 			}
 			if (resourceId.isEmpty()) {
 				emptyResource();
 			}
 			String catEsc = URLEncoder.encode(catalogueName, UTF8);
 			String residEsc = URLEncoder.encode(resourceId, UTF8);
 			String url = basePath + "/resource-stats?catalogue=" + catEsc
 					+ "&idResource=" + residEsc;
 			WebResource resource = client.resource(url);
 			ClientResponse response = resource.accept(JSON).get(
 					ClientResponse.class);
 			if (response.getStatus() != REQUEST_OK) {
 				throw new CkanalyzeClientRemoteException(response.getEntity(
 						JSONIZEDException.class).getErrorDescription());
 			} else {
 				retval = response.getEntity(ResourceStat.class);
 			}
 		} catch (UnsupportedEncodingException e) {
 			unsupportedEncoding(e);
 		}
 		return retval;
 	}
 	
 	/**
 	 * Provide information about scheduled catalogues
 	 * @param catalogueName -- catalogue name (URL)
 	 * @return true if the specified catalogue is already scheduled, false otherwise.
 	 */
 	public boolean isScheduledCatalogue(String catalogueName)
 	{
 		Status retval;
 		try {
 			if (catalogueName.isEmpty()) {
 				emptyCatalogue();
 			}
 			String par = URLEncoder.encode(catalogueName, UTF8);
 			String url = basePath + "/is-available?catalogue=" + par;
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
 	 * Schedule a new catalogue
 	 * @param catalogueName
 	 * @return
 	 */
 	public ScheduleResponse scheduleCatalog(String catalogueName)
 	{
 		ScheduleResponse retval = null;
 		try {
 			if (catalogueName.isEmpty()) {
 				emptyCatalogue();
 			}
 			String par = URLEncoder.encode(catalogueName, UTF8);
 			String url = basePath + "/schedule-catalogue?catalogue=" + par;
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
 	
 	private void emptyCatalogue()
 	{
 		throw new CkanalyzeClientLocalException(
 				"Empty parameter catalogueName");
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
