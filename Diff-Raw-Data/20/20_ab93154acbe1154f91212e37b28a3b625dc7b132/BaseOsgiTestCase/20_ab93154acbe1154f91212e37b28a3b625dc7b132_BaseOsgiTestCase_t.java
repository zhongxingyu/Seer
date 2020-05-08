 /*******************************************************************************
  * Copyright (c) 2008, 2009 SOPERA GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     SOPERA GmbH - initial API and implementation
  *******************************************************************************/
 package org.eclipse.swordfish.core.test.util.base;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.osgi.framework.ServiceRegistration;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.osgi.test.AbstractConfigurableBundleCreatorTests;
 import org.springframework.util.Assert;
 
 public class BaseOsgiTestCase extends AbstractConfigurableBundleCreatorTests {
     protected Logger LOG = LoggerFactory.getLogger(getClass());
 
     private List<ServiceRegistration> regirstrationsToCancel = new ArrayList<ServiceRegistration>();
     protected void addRegistrationToCancel(ServiceRegistration serviceRegistration) {
         regirstrationsToCancel.add(serviceRegistration);
     }
     @Override
     protected void onTearDown() throws Exception {
         Assert.notNull(regirstrationsToCancel);
         for(ServiceRegistration serviceRegistration : regirstrationsToCancel) {
             try {
                 serviceRegistration.unregister();
             } catch (IllegalStateException ex) {
                 LOG.error("Unregistering service exception", ex);
             }
         }
         regirstrationsToCancel.clear();
         super.onTearDown();
     }
 
 
     static {
         try {
             System.setProperty("org.osgi.vendor.framework",
                     "org.eclipse.swordfish.core.test.util.base");
             System.setProperty("eclipse.ignoreApp", "true");
             System.setProperty("osgi.clean", "true");
             System.setProperty("osgi.console", "22763");
             System.setProperty("servicemix.base", ".");
             System.setProperty("org.osgi.framework.system.packages", "com.sun.jimi.core, com.sun.net.ssl, com.sun.net.ssl.internal.ssl, com.sun.org.apache.xalan.internal, com.sun.org.apache.xalan.internal.res, com.sun.org.apache.xalan.internal.xsltc.trax, com.sun.org.apache.xerces.internal.dom, com.sun.org.apache.xerces.internal.jaxp, com.sun.org.apache.xerces.internal.xni, com.sun.org.apache.xml.internal, com.sun.org.apache.xml.internal.utils, com.sun.org.apache.xpath.internal, com.sun.org.apache.xpath.internal.jaxp, com.sun.org.apache.xpath.internal.objects, com.sun.xml.fastinfoset.dom, com.sun.xml.fastinfoset.sax, com.sun.xml.fastinfoset.stax, javax.annotation, javax.annotation.security, javax.crypto, javax.crypto.interfaces, javax.crypto.spec, javax.imageio, javax.imageio.metadata, javax.imageio.stream, javax.jms, javax.management, javax.management.loading, javax.management.modelmbean, javax.management.remote, javax.naming, javax.naming.directory, javax.naming.spi, javax.net, javax.net.ssl, javax.rmi, javax.security.auth, javax.security.auth.callback, javax.security.auth.login, javax.security.auth.spi, javax.security.auth.x500, javax.security.cert, javax.security.sasl, javax.servlet, javax.sql, javax.swing, javax.swing.event,  org.jvnet.fastinfoset, org.jvnet.staxex, org.omg.CORBA, org.omg.CosNaming,  sun.misc, sun.security.provider,javax.activation, org.omg.CORBA.TypeCodePackage, org.omg.CORBA.portable, org.omg.PortableServer, org.omg.PortableServer.POAPackage, javax.swing.border, javax.swing.tree, javax.management.remote.rmi");
         } catch (Throwable t) {
         }
     }
 
     @Override
     protected String getManifestLocation() {
         return "classpath:org/eclipse/swordfish/test/META-INF/MANIFEST.MF";
         //return "classpath:org/eclipse/swordfish/core/planner/test/MANIFEST.MF";
     }
 
 
 }
