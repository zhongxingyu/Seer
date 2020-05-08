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
 package org.ow2.sirocco.apis.rest.cimi.resource.serialization;
 
import java.util.TimeZone;

 import org.custommonkey.xmlunit.XMLUnit;
 import org.junit.BeforeClass;
 import org.ow2.sirocco.apis.rest.cimi.server.SiroccoRestCimiApplication;
 import org.springframework.web.context.ContextLoaderListener;
 
 import com.sun.jersey.api.json.JSONConfiguration;
 import com.sun.jersey.spi.spring.container.servlet.SpringServlet;
 import com.sun.jersey.test.framework.AppDescriptor;
 import com.sun.jersey.test.framework.JerseyTest;
 import com.sun.jersey.test.framework.WebAppDescriptor;
 import com.sun.jersey.test.framework.spi.container.TestContainerException;
 
 public class SerializationTestBase extends JerseyTest {
 
     /**
      * {@inheritDoc}
      * 
      * @see com.sun.jersey.test.framework.JerseyTest#configure()
      */
     @Override
     protected AppDescriptor configure() {
         return new WebAppDescriptor.Builder("javax.ws.rs.Application", SiroccoRestCimiApplication.class.getName())
             .initParam(JSONConfiguration.FEATURE_POJO_MAPPING, "true").contextPath("sirocco-rest")
             .servletClass(SpringServlet.class).contextListenerClass(ContextLoaderListener.class)
             .contextParam("contextConfigLocation", "classpath:context/serializationResourcesContext.xml").build();
     }
 
     /**
      * @throws Exception In case of error
      */
     @BeforeClass
     public static void setUpBeforeClass() throws Exception {
         XMLUnit.setIgnoreWhitespace(true);
         XMLUnit.setIgnoreComments(true);
         XMLUnit.setIgnoreAttributeOrder(true);
        TimeZone.setDefault(TimeZone.getTimeZone("Zulu"));
     }
 
     @Override
     protected int getPort(final int defaultPort) {
         int port = super.getPort(defaultPort);
         String propPort = System.getProperty("grizzly.port");
         if (null != propPort) {
             try {
                 port = Integer.parseInt(propPort);
             } catch (NumberFormatException e) {
                 throw new TestContainerException("grizzly.port with a value of \"" + propPort + "\" is not a valid integer.", e);
             }
         }
         return port;
     }
 }
