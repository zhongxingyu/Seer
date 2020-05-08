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
 
 package eu.trentorise.opendata.ckanalyze.services;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 
 import eu.trentorise.opendata.ckanalyze.controller.CatalogAnalysis;
 import eu.trentorise.opendata.ckanalyze.controller.ResourceAnalysis;
 import eu.trentorise.opendata.ckanalyze.exceptions.WebAPIException;
 import eu.trentorise.opendata.ckanalyze.model.resources.ResourceStats;
 import eu.trentorise.opendata.ckanalyze.utility.QueryBuilder;
 
 /**
  * Serivice which exposes resource statistics
  * 
  * @author Alberto Zanella <a.zanella@trentorise.eu> Last modified by azanella
  *         On 30/lug/2013
  */
 @Path("/resource-stats")
 public class ResourceService {
 	/**
 	 * 
 	 * @param catName
 	 *            name of the catalog
 	 * @param resid
 	 *            id of the required resource
 	 * @return statistics about the required resource
 	 * @throws WebAPIException
 	 *             if the resourceId or the catalog paameter are missing or if
 	 *             they are invalid
 	 */
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	public ResourceStats getResourceStats(
 			@QueryParam("catalog") String catName,
 			@QueryParam("idResource") String resid) throws WebAPIException {
 		if ((resid == null) || (resid.isEmpty())) {
 			throw new WebAPIException("idResource parameter not specified");
 		}
 		if ((catName == null) || (catName.isEmpty())) {
 			throw new WebAPIException("catalog parameter not specified");
 		}
 		if(QueryBuilder.isUpdating(catName))
 		{
 			throw new WebAPIException("Catalog " + catName + " is not available at the moment for updating process");
 		}
		if (!CatalogAnalysis.isValidCatalog(catName))
 		{
 			throw new WebAPIException("Catalog " + catName + " not found");
 		}
 		ResourceAnalysis rsa = new ResourceAnalysis();
 		if (rsa.isValidResource(catName, resid)) {
 			return rsa.getResourceStats(resid);
 		} else {
 			throw new WebAPIException("resource id not found");
 		}
 	}
 }
