 /*******************************************************************************
  * Copyright (c) 2008, 2010 VMware Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   VMware Inc. - initial contribution
  *******************************************************************************/
 
 package org.eclipse.virgo.snaps.core.internal.webapp.config;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.virgo.snaps.core.internal.webapp.config.FilterDefinition;
 import org.eclipse.virgo.snaps.core.internal.webapp.config.FilterDispatcherType;
 import org.eclipse.virgo.snaps.core.internal.webapp.config.FilterMappingDefinition;
 import org.eclipse.virgo.snaps.core.internal.webapp.config.MutableWebXml;
 import org.eclipse.virgo.snaps.core.internal.webapp.config.ServletDefinition;
 import org.eclipse.virgo.snaps.core.internal.webapp.config.ServletMappingDefinition;
 import org.eclipse.virgo.snaps.core.internal.webapp.config.ServletNameFilterMappingDefinition;
 import org.eclipse.virgo.snaps.core.internal.webapp.config.UrlPatternFilterMappingDefinition;
 import org.eclipse.virgo.snaps.core.internal.webapp.config.WebXml;
 import org.eclipse.virgo.snaps.core.internal.webapp.config.WebXmlParser;
 import org.junit.Test;
 
 
 public class WebXmlParserTests {
 
     @Test
     public void parseServletDefinitions() throws Exception {
         WebXml webXml = parse();
         ServletDefinition[] servletDefinitions = webXml.getServletDefinitions();
         
         assertEquals(2, servletDefinitions.length);
         
         assertEquals("one", servletDefinitions[0].getServletName());
         assertEquals("test.One", servletDefinitions[0].getServletClassName());
         assertEquals("two", servletDefinitions[1].getServletName());
         assertEquals("test.Two", servletDefinitions[1].getServletClassName());
         
         Map<String, String> initParameters = servletDefinitions[0].getInitParameters();
         assertEquals(1, initParameters.size());
         assertEquals("/WEB-INF/spring/*.xml", initParameters.get("contextConfigLocation"));
     }
     
     @Test
     public void parseServletMappingDefinitions() throws Exception {
         WebXml webXml = parse();
         ServletMappingDefinition[] servletMappingDefinitions = webXml.getServletMappingDefinitions();
         
         assertEquals("one", servletMappingDefinitions[0].getServletName());
         assertEquals("/one", servletMappingDefinitions[0].getUrlPattern());
         assertEquals("two", servletMappingDefinitions[1].getServletName());
         assertEquals("/two", servletMappingDefinitions[1].getUrlPattern());
     }
     
     @Test
     public void parseFilterDefinitions() throws Exception {
         WebXml webXml = parse();
         FilterDefinition[] filterDefinitions = webXml.getFilterDefinitions();
         
         assertEquals(2, filterDefinitions.length);
         
         assertEquals("one", filterDefinitions[0].getFilterName());
         assertEquals("filter.One", filterDefinitions[0].getFilterClassName());
         assertEquals("two", filterDefinitions[1].getFilterName());
         assertEquals("filter.Two", filterDefinitions[1].getFilterClassName());
         
         Map<String, String> initParameters = filterDefinitions[0].getInitParameters();
         assertEquals(1, initParameters.size());
         assertEquals("10", initParameters.get("cacheSize"));
     }
     
     @Test
     public void parseFilterMappingDefinitions() throws Exception {
         WebXml webXml = parse();
         
         UrlPatternFilterMappingDefinition[] urlPatternFilterMappingDefinitions = webXml.getUrlPatternFilterMappingDefinitions();
         ServletNameFilterMappingDefinition[] servletNameFilterMappingDefinitions = webXml.getServletNameFilterMappingDefinitions();
         
         assertEquals(3, urlPatternFilterMappingDefinitions.length);
         
         assertEquals("one", urlPatternFilterMappingDefinitions[0].getFilterName());
         assertEquals("/foo/*", urlPatternFilterMappingDefinitions[0].getUrlPattern());
         assertDispatcherConfiguration(urlPatternFilterMappingDefinitions[0], false, false, false, true);
         
         assertEquals("one", urlPatternFilterMappingDefinitions[1].getFilterName());
         assertEquals("/bar/*", urlPatternFilterMappingDefinitions[1].getUrlPattern());
         assertDispatcherConfiguration(urlPatternFilterMappingDefinitions[1], false, false, false, true);
                 
         assertEquals("two", urlPatternFilterMappingDefinitions[2].getFilterName());
         assertEquals("*.jsp", urlPatternFilterMappingDefinitions[2].getUrlPattern());
         assertDispatcherConfiguration(urlPatternFilterMappingDefinitions[2], true, true, true, true);
         
         assertEquals(3, servletNameFilterMappingDefinitions.length);      
         
         assertEquals("one", servletNameFilterMappingDefinitions[0].getFilterName());
         assertEquals("Servlet1", servletNameFilterMappingDefinitions[0].getServletName());
         assertDispatcherConfiguration(servletNameFilterMappingDefinitions[0], false, false, false, true);
         
         assertEquals("one", servletNameFilterMappingDefinitions[1].getFilterName());
         assertEquals("Servlet2", servletNameFilterMappingDefinitions[1].getServletName());
         assertDispatcherConfiguration(servletNameFilterMappingDefinitions[1], false, false, false, true);
         
         assertEquals("two", servletNameFilterMappingDefinitions[2].getFilterName());
         assertEquals("one", servletNameFilterMappingDefinitions[2].getServletName());
         assertDispatcherConfiguration(servletNameFilterMappingDefinitions[2], false, true, true, false);
     }
 
     private void assertDispatcherConfiguration(FilterMappingDefinition urlPatternFilterMappingDefinition, boolean error, boolean forward, boolean include, boolean request) {
         Set<FilterDispatcherType> dispatcherTypes = urlPatternFilterMappingDefinition.getFilterDispatcherTypes();
         assertEquals(error, dispatcherTypes.contains(FilterDispatcherType.ERROR));
         assertEquals(forward, dispatcherTypes.contains(FilterDispatcherType.FORWARD));
         assertEquals(include, dispatcherTypes.contains(FilterDispatcherType.INCLUDE));
         assertEquals(request, dispatcherTypes.contains(FilterDispatcherType.REQUEST));
     }
 
     private WebXml parse() throws FileNotFoundException {
         WebXmlParser parser = new WebXmlParser();
         MutableWebXml webXml = new MutableWebXml();
         parser.parse(new FileInputStream("src/test/resources/dummy-web.xml"), webXml);
         return webXml;
     }
 }
