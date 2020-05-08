 /*
  * Copyright (C) 2010 eXo Platform SAS.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package org.exoplatform.services.rest.ext.proxy;
 
 import java.io.DataInputStream;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Enumeration;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.exoplatform.common.http.client.Codecs;
 import org.exoplatform.common.http.client.HTTPConnection;
 import org.exoplatform.common.http.client.HTTPResponse;
 import org.exoplatform.common.http.client.ModuleException;
 import org.exoplatform.common.http.client.NVPair;
 import org.exoplatform.common.http.client.ParseException;
 import org.exoplatform.common.http.client.ProtocolNotSuppException;
 import org.exoplatform.services.log.ExoLogger;
 import org.exoplatform.services.log.Log;
 
 /**
  * @author <a href="mailto:max.shaposhnik@exoplatform.com">Max Shaposhnik</a>
  * @version $Id$
  */
 public class BaseConnector extends Connector
 {
 
    /** The connection. */
    private HTTPConnection conn;
 
    /** The HTTPResponse. */
    HTTPResponse resp = null;
 
    /** The form_data array. */
    NVPair[] form_data;
 
    /** The headers array. */
    NVPair[] headers;
    
    /** Logger. */
    private static final Log LOG = ExoLogger.getLogger(BaseConnector.class);
 
    /**
     * {@inheritDoc}
     */
    @Override
    public HTTPResponse fetchGet(HttpServletRequest httpRequest, String url) throws MalformedURLException, ProtocolNotSuppException,
    IOException, ModuleException,ParseException
    {
       URL url_obj = null;
       url_obj = new URL(url);
 
          conn = new HTTPConnection(url_obj);
          conn.setTimeout(DEFAULT_CONNECT_TIMEOUT_MS);
          prepareRequestHeaders(httpRequest);
          prepareFormParams(url_obj);
          
          resp = conn.Get(url_obj.getProtocol() + "://" + url_obj.getAuthority() + url_obj.getPath(), form_data, headers);
 
          if (resp.getStatusCode() >= 300)
          {
             System.err.println("Received Error: " + resp.getReasonLine());
             System.err.println(resp.getText());
          }
       return resp;
    }
 
    /**
     * {@inheritDoc}
     */
    @Override
    public HTTPResponse fetchPost(HttpServletRequest httpRequest, String url) throws MalformedURLException, ProtocolNotSuppException,
    IOException, ModuleException,ParseException
    {
       URL url_obj = null;
          url_obj = new URL(url);
 
          conn = new HTTPConnection(url_obj);
          conn.setTimeout(DEFAULT_CONNECT_TIMEOUT_MS);
          prepareRequestHeaders(httpRequest);
 
          byte[] body = new byte[httpRequest.getContentLength()];;
          new DataInputStream(httpRequest.getInputStream()).readFully(body);
          resp = conn.Post(url_obj.getProtocol() + "://" + url_obj.getAuthority() + url_obj.getPath(), body, headers);
          if (resp.getStatusCode() >= 300)
          {
             System.err.println("Received Error: " + resp.getReasonLine());
             System.err.println(resp.getText());
          }
       return resp;
    }
 
    /**
     * Prepares request headers.
     * 
     * @param httpRequest the http request
     */
    private void prepareRequestHeaders(HttpServletRequest httpRequest)
    {
       ArrayList<NVPair> hds = new ArrayList<NVPair>();
       for (Enumeration<String> en = httpRequest.getHeaderNames(); en.hasMoreElements();)
       {
          NVPair pair = null;
          String headerName = (String)en.nextElement();
          for (Enumeration<String> en2 = httpRequest.getHeaders(headerName); en2.hasMoreElements();)
          {
             pair = new NVPair(headerName, en2.nextElement());
          }
          hds.add(pair);
          this.headers = new NVPair[hds.size()];
          this.headers = hds.toArray(headers);
       }
    }
 
    /**
     * Prepares form params.
     * 
     * @param url the url
     */
    private void prepareFormParams(URL url)
    {
       String query = url.getQuery();
       if (query != null)
       {
          try
          {
             this.form_data = Codecs.query2nv(query);
          }
          catch (ParseException e)
          {
             System.err.println(e.getMessage());
          }
       }
    }
 }
