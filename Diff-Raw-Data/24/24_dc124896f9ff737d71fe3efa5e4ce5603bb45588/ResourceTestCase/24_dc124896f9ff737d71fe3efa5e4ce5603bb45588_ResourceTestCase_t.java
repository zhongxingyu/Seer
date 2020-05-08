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
 import javax.servlet.ServletContextListener;
 
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
      * Servlet context parameters.
      */
    private static final Map<String, String> CONTEXT_PARAMS = Maps.newHashMap();
     /**
      * Servlet init parameters.
      */
    private static final Map<String, String> INIT_PARAMS = Maps.newHashMap();
     /**
      * Package names where root resources where found.
      */
     private static final String[] RESOURCE_PACKAGES = new String[] {"org.lafayette.server.webapp.api.resources"};
     /**
      * Context path of web application.
      */
     private static final String CONTEXT_PATH = "/api";
 
     static {
        CONTEXT_PARAMS.put("org.lafayette.server.webapp.api.realm", "Lafayette restricted area.");
         // TODO Configure test databse as HSQLDB
//        CONTEXT_PARAMS.put("contextConfigLocation", "classpath:**/testContext.xml");
        INIT_PARAMS.put("com.sun.jersey.api.json.POJOMappingFeature", "true");
        INIT_PARAMS.put("com.sun.jersey.spi.container.ResourceFilters",
                 "org.lafayette.server.webapp.api.filter.AuthenticationResourceFilters");
        INIT_PARAMS.put("com.sun.jersey.config.property.WebPageContentRegex", "/(img|css|dtd)/.*");
     }
 
     @Override
     public WebAppDescriptor configure() {
         final WebAppDescriptor.Builder builder = new WebAppDescriptor.Builder(RESOURCE_PACKAGES);
 
         for (final Class<? extends ServletContextListener> l : determineContextListeners()) {
             builder.contextListenerClass(l);
         }
 
        for (final Map.Entry<String, String> contextParam : CONTEXT_PARAMS.entrySet()) {
             builder.contextParam(contextParam.getKey(), contextParam.getValue());
         }
 
        for (final Map.Entry<String, String> initParam : INIT_PARAMS.entrySet()) {
             builder.initParam(initParam.getKey(), initParam.getValue());
         }
 
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
 }
