 /*
  *  LICENSE
  *
  * "THE BEER-WARE LICENSE" (Revision 43):
  * "Sven Strittmatter" <weltraumschaf@googlemail.com> wrote this file.
  * As long as you retain this notice you can do whatever you want with
  * this stuff. If we meet some day, and you think this stuff is worth it,
  * you can buy me a non alcohol-free beer in return.
  *
  * Copyright (C) 2012 "Sven Strittmatter" <weltraumschaf@googlemail.com>
  */
 package org.lafayette.server.testing;
 
 import com.google.common.collect.Maps;
 import com.sun.jersey.test.framework.JerseyTest;
 import com.sun.jersey.test.framework.WebAppDescriptor;
 import com.sun.jersey.test.framework.spi.container.TestContainerFactory;
 import com.sun.jersey.test.framework.spi.container.grizzly2.web.GrizzlyWebTestContainerFactory;
 import de.weltraumschaf.commons.Version;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
 import javax.servlet.ServletContextListener;
 import org.junit.After;
import org.junit.AfterClass;
import org.junit.Rule;
 import org.junit.rules.TemporaryFolder;
 
 /**
  * Base class for test which tests Jersey resources.
  *
  * https://blog.codecentric.de/en/2012/05/writing-lightweight-rest-integration-tests-with-the-jersey-test-framework/
  * http://jersey.java.net/nonav/documentation/latest/test-framework.html
  * http://usna86-techbits.blogspot.pt/2013/03/increase-quality-and-productivity-with.html
  * https://jersey.java.net/documentation/1.17/test-framework.html
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 public abstract class ResourceTestCase extends JerseyTest {
 
     /**
      * Testing stage.
      */
     protected static final String STAGE = "TESTING";
     /**
      * Location of version file.
      */
     private static final String VERSION_FILE = "/org/lafayette/server/version.properties";
     /**
      * Package names where root resources where found.
      */
     private static final String[] RESOURCE_PACKAGES = new String[] {"org.lafayette.server.webapp.api.resources"};
     /**
      * Context path of web application.
      */
     private static final String CONTEXT_PATH = "/api";
     /**
      * Used as server base directory.
      *
      * Did not use rule annotation, because the fucking base clause was implemented by idiots with work in constructor
      * which leads to NPE.
      */
     private TemporaryFolder tmp;
 
     /**
      * Delete manually after test.
      *
      * This leads to an file not found exception on context destroyed.
      */
     @After
     public void after() {
         tmp.delete();
     }
 
     @Override
     public WebAppDescriptor configure() {
         tmp = new TemporaryFolder();
 
         try {
             tmp.create();
         } catch (final IOException ex) {
             throw new RuntimeException(ex);
         }
 
         final WebAppDescriptor.Builder builder = new WebAppDescriptor.Builder(RESOURCE_PACKAGES);
 
         for (final Class<? extends ServletContextListener> l : determineContextListeners()) {
             builder.contextListenerClass(l);
         }
 
         configureContextParams(builder);
         configueInitParams(builder);
         return builder.contextPath(CONTEXT_PATH).build();
     }
 
     @Override
     public TestContainerFactory getTestContainerFactory() {
         return new GrizzlyWebTestContainerFactory();
     }
 
     /**
      * Extracts the context listener classes from test case annotation.
      *
      * @return never {@code null}
      */
     private Collection<Class<? extends ServletContextListener>> determineContextListeners() {
         final ConetxtListeners annotation = getClass().getAnnotation(ConetxtListeners.class);
 
         if (annotation == null) {
             return Collections.emptyList();
         }
 
         return Arrays.asList(annotation.value());
     }
 
     /**
      * Get the current version.
      *
      * @return never {@code null}
      * @throws IOException if version file can't be loaded
      */
     protected String getVersion() throws IOException {
         final Version version = new Version(VERSION_FILE);
         version.load();
         return version.toString();
     }
 
     /**
      * Sets the context parameters.
      *
      * @param builder must not be {@code nul}
      */
     private void configureContextParams(final WebAppDescriptor.Builder builder) {
         final Map<String, String> contextParams = Maps.newHashMap();
         contextParams.put("org.lafayette.server.webapp.api.realm", "Lafayette restricted area.");
         contextParams.put("org.lafayette.server.webapp.api.baseDirectory", tmp.getRoot().getAbsolutePath());
         contextParams.put("contextConfigLocation", "classpath:**/testContext.xml");
 
         for (final Map.Entry<String, String> contextParam : contextParams.entrySet()) {
             builder.contextParam(contextParam.getKey(), contextParam.getValue());
         }
     }
 
     /**
      * Sets the initial parameters.
      *
      * @param builder must not be {@code nul}
      */
     private void configueInitParams(final WebAppDescriptor.Builder builder) {
         final Map<String, String> initParams = Maps.newHashMap();
         initParams.put("com.sun.jersey.api.json.POJOMappingFeature", "true");
         initParams.put("com.sun.jersey.spi.container.ResourceFilters",
                 "org.lafayette.server.webapp.api.filter.AuthenticationResourceFilters");
         initParams.put("com.sun.jersey.config.property.WebPageContentRegex", "/(img|css|dtd)/.*");
 
         for (final Map.Entry<String, String> initParam : initParams.entrySet()) {
             builder.initParam(initParam.getKey(), initParam.getValue());
         }
     }
 
 }
