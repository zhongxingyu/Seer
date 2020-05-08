 /* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.servlets.wms;
 
 import org.vfny.geoserver.requests.readers.KvpRequestReader;
 import org.vfny.geoserver.requests.readers.XmlRequestReader;
 import org.vfny.geoserver.requests.readers.wms.GetMapKvpReader;
 import org.vfny.geoserver.responses.Response;
 import org.vfny.geoserver.responses.wms.GetMapResponse;
 import org.vfny.geoserver.servlets.WMService;
import java.io.IOException;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
 
 
 /**
  * WMS service wich returns request and response handlers to manage a GetMap
  * request
  *
  * @author Gabriel Roldn
 * @version $Id: GetMap.java,v 1.7 2004/03/30 11:12:40 cholmesny Exp $
  */
 public class GetMap extends WMService {
     /**
      * Creates a new GetMap object.
      */
     public GetMap() {
     }
 
    public void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
         doGet(request, response);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      */
     protected Response getResponseHandler() {
         return new GetMapResponse();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      *
      * @throws java.lang.UnsupportedOperationException DOCUMENT ME!
      */
     protected XmlRequestReader getXmlRequestReader() {
         /**
          * @todo Implement this org.vfny.geoserver.servlets.AbstractService
          *       abstract method
          */
         throw new java.lang.UnsupportedOperationException(
             "Method getXmlRequestReader() not yet implemented.");
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param params DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      */
     protected KvpRequestReader getKvpReader(Map params) {
         return new GetMapKvpReader(params);
     }
 }
