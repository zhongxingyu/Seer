 /*
  * Copyright © 2010 Red Hat, Inc.
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
 package com.redhat.rhevm.api.command.base;
 
 import java.io.InputStream;
 import java.util.Collection;
 import java.util.List;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.Unmarshaller;
 import javax.ws.rs.core.Response;
 
 import org.apache.cxf.BusFactory;
 import org.apache.cxf.binding.BindingFactoryManager;
 import org.apache.cxf.jaxrs.JAXRSBindingFactory;
 
 import org.apache.cxf.jaxrs.client.WebClient;
 
 import com.redhat.rhevm.api.model.Link;
 
 /**
  * Client to make RESTful invocations via a direct API (to avoid leakage
  * of the URI structure, as would occur via a proxy-based API).
  */
 public class BaseClient {
 
     protected String baseUrl;
 
     public String getBaseUrl() {
         return baseUrl;
     }
 
     public void setBaseUrl(String baseUrl) {
         this.baseUrl = baseUrl;
     }
 
     public <S> S getCollection(String rel, Class<S> clz) throws Exception {
         S ret = clz.newInstance();
         Response r = null;
         Exception failure = null;
         String top = getTopLink(rel);
 
         if (top != null) {
             try {
                 WebClient get = WebClient.create(top);
                 r = get.path("/").accept("application/xml").get();
             } catch (Exception e) {
                 failure = e;
             }
 
             if (failure != null || r.getStatus() != 200) {
                 String baseError = "cannot follow " + top + ", failed with ";
                diagnose(baseError, failure, r);
             } else {
                 InputStream is = (InputStream)r.getEntity();
                 JAXBContext context = JAXBContext.newInstance(clz);
                 Unmarshaller unmarshaller = context.createUnmarshaller();
                 ret = clz.cast(unmarshaller.unmarshal(is));
             }
         }
         return ret;
     }
 
     public void doAction(String action, Link link) throws Exception {
         Response r = null;
         Exception failure = null;
 
         try {
             WebClient post = WebClient.create(link.getHref());
             r = post.path("/").post(null);
         } catch (Exception e) {
             failure = e;
         }
 
         if (failure != null || r.getStatus() != 204) {
            diagnose(action + " failed with", failure, r);
         } else {
             System.out.println(action + " succeeded");
         }
     }
 
     protected String getTopLink(String rel) {
         String ret = null;
         Response links = null;
         Exception failure = null;
 
         BindingFactoryManager manager =
             BusFactory.getDefaultBus().getExtension(BindingFactoryManager.class);
         manager.registerBindingFactory(JAXRSBindingFactory.JAXRS_BINDING_ID,
                                        new JAXRSBindingFactory());
 
         try {
             WebClient head = WebClient.create(getBaseUrl());
             links = head.path("/").head();
             ret = links.getStatus() == 200 ? getLink(links, rel) : null;
         } catch (Exception e) {
             failure = e;
         }
 
         if (failure != null || links.getStatus() != 200) {
             String baseError = "cannot follow " + getBaseUrl() + ", failed with ";
            diagnose(baseError, failure, links);
         }
 
         return ret;
     }
 
     private String getLink(Response r, String rel) {
         String ret = null;
         List<Object> links = r.getMetadata().get("Link");
         for (Object o : links) {
             for (String l : ((String)o).split(",")) {
                 Link link = Link.valueOf(l);
                 if (rel.equals((link.getRel()))) {
                     ret = link.getHref();
                 }
             }
         }
         return ret;
     }
 
    private void diagnose(String baseError, Exception failure, Response r) {
         if (failure != null) {
             System.err.println(baseError + failure.getMessage());
        } else if (r.getStatus() != 201) {
             System.err.println(baseError + getStatus(r));
         }
     }
 
     private String getStatus(Response r) {
         Response.Status status = Response.Status.fromStatusCode(r.getStatus());
         return new StringBuffer(Integer.toString(r.getStatus()))
                        .append(" ").append(status.toString())
                        .append(" (").append(status.getFamily()).append(")")
                        .toString();
     }
 
 }
