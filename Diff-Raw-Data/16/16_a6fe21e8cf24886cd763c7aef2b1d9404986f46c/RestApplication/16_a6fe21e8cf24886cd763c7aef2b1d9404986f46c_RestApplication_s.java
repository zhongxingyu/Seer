 /**
  * Copyright (C) 2007-2008 Elastic Grid, LLC.
  * 
  * This file is part of Elastic Grid.
  * 
  * Elastic Grid is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or any later version.
  * 
  * Elastic Grid is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with Elastic Grid.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.elasticgrid.rest;
 
 import org.restlet.data.Protocol;
 import org.restlet.data.Reference;
 import org.restlet.data.Request;
 import org.restlet.data.Response;
 import org.restlet.ext.spring.SpringComponent;
 import org.restlet.ext.wadl.ApplicationInfo;
 import org.restlet.ext.wadl.GrammarsInfo;
 import org.restlet.ext.wadl.IncludeInfo;
 import org.restlet.ext.wadl.WadlApplication;
 import org.springframework.beans.factory.DisposableBean;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 import java.rmi.RMISecurityManager;
 import java.security.Permission;
 
 public class RestApplication extends WadlApplication implements InitializingBean, DisposableBean {
     private SpringComponent component;
 
     @Override
     public ApplicationInfo getApplicationInfo(Request request, Response response) {
         ApplicationInfo appInfo = super.getApplicationInfo(request, response);
         appInfo.getNamespaces().put("urn:elastic-grid:eg", "eg");
         GrammarsInfo grammar = new GrammarsInfo();
         IncludeInfo include = new IncludeInfo();
        include.setTargetRef(new Reference("http://www.elastic-grid.com/schemas/elastic-grid-0.8.2.xsd"));
 //        include.setTargetRef(new Reference("applications/rest-api/src/main/resources/com/elasticgrid/rest/elastic-grid.xsd"));
 //        ClassLoader cl = getClass().getClassLoader();
 //        URL resource = cl.getResource("elastic-grid.xsd");
 //        include.setTargetRef(new Reference(resource.toString()));
         grammar.getIncludes().add(include);
         appInfo.setGrammars(grammar);
         return appInfo;
     }
 
     @Override
     public String getTitle() {
         return "Elastic Grid REST API";
     }
 
     public void afterPropertiesSet() throws Exception {
         try {
             // Create a new Component.
             component = new SpringComponent();
 
             // Add a new HTTP server listening on port 8182.
             component.getServers().add(Protocol.HTTP, 8182);
 
             // Attach the sample application.
             component.getDefaultHost().attach(this);
 
             // Start the component.
             component.start();
         } catch (Exception e) {
             // Something is wrong.
             e.printStackTrace();
         }
     }
 
     public void destroy() throws Exception {
         component.stop();
     }
 
     public static void main(String[] args) throws Exception {
         // setup Jini security model
         System.setSecurityManager(new RMISecurityManager() {
             @Override
             public void checkPermission(Permission perm) {
                 // do nothing -- allow everything
             }
         });
 
         ApplicationContext ctx = new ClassPathXmlApplicationContext("/com/elasticgrid/rest/applicationContext.xml");
 //        SLF4JBridgeHandler.install();
     }
 
 }
