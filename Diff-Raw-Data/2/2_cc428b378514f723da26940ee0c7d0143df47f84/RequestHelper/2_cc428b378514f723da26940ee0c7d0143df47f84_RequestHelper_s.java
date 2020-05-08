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
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  * USA
  *
  * $Id$
  *
  */
 package org.ow2.sirocco.cimi.server.request;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.ws.rs.core.MultivaluedMap;
 import javax.xml.bind.DatatypeConverter;
 
 import org.ow2.sirocco.cimi.domain.CimiData;
 import org.ow2.sirocco.cimi.server.resource.RestResourceAbstract;
 import org.ow2.sirocco.cimi.server.utils.Constants;
 
 /**
  * Utility to build CIMI Request with the data of REST request.
  * 
  * @see CimiRequest
  */
 public class RequestHelper {
 
     public static CimiRequest buildRequest(final RestResourceAbstract.JaxRsRequestInfos infos, final IdRequest ids,
         final CimiData cimiData) {
         CimiRequest request = new CimiRequest();
         request.setParams(RequestHelper.buildRequestHeader(infos));
         request.setIds(ids);
         request.setCimiData(cimiData);
         request.setBaseUri(infos.getUriInfo().getBaseUri().toString());
         request.setPath(infos.getUriInfo().getPath());
         request.setMethod(infos.getRequest().getMethod());
         return request;
     }
 
     public static String[] decode(String auth) {
         auth = auth.replaceFirst("[B|b]asic ", "");
         byte[] decodedBytes = DatatypeConverter.parseBase64Binary(auth);
         if (decodedBytes == null || decodedBytes.length == 0) {
             return null;
         }
         return new String(decodedBytes).split(":", 2);
     }
 
     private static RequestParams buildRequestHeader(final RestResourceAbstract.JaxRsRequestInfos infos) {
         RequestParams requestHeader = new RequestParams();
         List<String> versions = infos.getHeaders().getRequestHeader(Constants.HEADER_CIMI_VERSION);
         if ((null != versions) && (versions.size() > 0)) {
             requestHeader.setVersion(versions.get(0));
         }
 
         requestHeader.setCimiSelect(new CimiSelect(RequestHelper.transformQueryParamToList(Constants.PARAM_CIMI_SELECT, infos
             .getUriInfo().getQueryParameters())));
         requestHeader.setCimiExpand(new CimiExpand(RequestHelper.transformQueryParamToList(Constants.PARAM_CIMI_EXPAND, infos
             .getUriInfo().getQueryParameters())));
         requestHeader.setCimiFilter(new CimiFilter(RequestHelper.transformQueryParamToList(Constants.PARAM_CIMI_FILTER, infos
            .getUriInfo().getQueryParameters())));
         requestHeader.setCimiFirst(new CimiIntegerParam(RequestHelper.transformQueryParamToString(Constants.PARAM_CIMI_FIRST,
             infos.getUriInfo().getQueryParameters())));
         requestHeader.setCimiLast(new CimiIntegerParam(RequestHelper.transformQueryParamToString(Constants.PARAM_CIMI_LAST,
             infos.getUriInfo().getQueryParameters())));
 
         List<String> siroccoInfoTestsId = infos.getHeaders().getRequestHeader(Constants.HEADER_SIROCCO_INFO_TEST_ID);
         if ((null != siroccoInfoTestsId) && (siroccoInfoTestsId.size() > 0)) {
             requestHeader.setSiroccoInfoTestId(siroccoInfoTestsId.get(0));
         }
 
         return requestHeader;
     }
 
     private static List<String> transformQueryParamToList(final String paramName,
         final MultivaluedMap<String, String> queryParameters) {
         List<String> params = new ArrayList<String>();
         params = queryParameters.get(paramName);
         return params;
     }
 
     private static String transformQueryParamToString(final String paramName,
         final MultivaluedMap<String, String> queryParameters) {
         List<String> params = RequestHelper.transformQueryParamToList(paramName, queryParameters);
         String param = null;
         if ((null != params) && (params.size() > 0)) {
             param = params.get(0);
         }
         return param;
     }
 
 }
