 /*
  * $Id$
  *
  * Copyright 2003-2006 Orange Nederland Breedband B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.server.frontend;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import org.xins.common.collections.PropertyReader;
 import org.xins.common.text.DateConverter;
 
 import org.xins.common.xml.ElementBuilder;
 import org.xins.common.spec.FunctionSpec;
 import org.xins.common.spec.InvalidSpecificationException;
 
 import org.xins.server.API;
 import org.xins.server.FunctionResult;
 
 /**
  * Result for the Control command.
  *
  * @version $Revision$ $Date$
  * @author <a href="mailto:anthony.goubard@orange-ft.com">Anthony Goubard</a>
  */
 class ControlResult extends FunctionResult {
 
    /**
     * Creates the result for the Control command.
     *
     * @param api
     *    the API, cannot be <code>null</code>.
     *
     * @param sessionManager
     *    the sessionManager, cannot be <code>null</code>.
     *
     * @param redirectionMap
     *    the redirection map, cannot be <code>null</code>.
     */
    ControlResult(API api, SessionManager sessionManager, Map redirectionMap) {
 
       // The versions
       param("xinsCommonVersion", org.xins.common.Library.getVersion());
       param("xinsServerVersion", org.xins.server.Library.getVersion());
       param("apiName", api.getName());
       param("apiStartUpTime", DateConverter.toDateString(api.getStartupTimestamp()));
 
       // Some bootstrap properties
       PropertyReader bootstrapProps = api.getBootstrapProperties();
       param("apiVersion", bootstrapProps.get("org.xins.api.version"));
       param("apiBuildVersion", bootstrapProps.get("org.xins.api.build.version"));
       param("apiBuildTime", bootstrapProps.get("org.xins.api.build.time"));
 
       // The commands
       try {
          Map functions = api.getAPISpecification().getFunctions();
          Iterator itFunctions = functions.entrySet().iterator();
          while (itFunctions.hasNext()) {
             Map.Entry nextFunction = (Map.Entry) itFunctions.next();
             FunctionSpec functionSpec = (FunctionSpec) nextFunction.getValue();
             ElementBuilder builder = new ElementBuilder("command");
             builder.setAttribute("name", (String) nextFunction.getKey());
             builder.setAttribute("description", functionSpec.getDescription());
             add(builder.createElement());
          }
       } catch (InvalidSpecificationException isex) {
          isex.printStackTrace();
       }
       Iterator itVirtualFunctions = redirectionMap.keySet().iterator();
       while (itVirtualFunctions.hasNext()) {
          String nextFunction = (String) itVirtualFunctions.next();
          ElementBuilder builder = new ElementBuilder("command");
          builder.setAttribute("name", nextFunction);
          add(builder.createElement());
       }
 
       // The sessions
       ElementBuilder builder = new ElementBuilder("sessionproperties");
       Map sessionProperties = sessionManager.getProperties();
       Iterator itSessions = sessionProperties.entrySet().iterator();
       while (itSessions.hasNext()) {
          Map.Entry nextSession = (Map.Entry) itSessions.next();
          String nextKey = (String) nextSession.getKey();
          Object nextValue = nextSession.getValue();
          ElementBuilder builder2 = new ElementBuilder("property");
          builder2.setAttribute("name", nextKey);
          builder2.setText(nextValue.toString());
          builder.addChild(builder2.createElement());
       }
       add(builder.createElement());
    }
 
    /**
     * Return the XSLT to use to display the data of the Control command.
     *
     * @return
     *    the XSLT to display the Control result, never <code>null</code>.
     */
    static String getControlTemplate() {
       String result = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
           "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">\n" +
           "<xsl:template match=\"commandresult\">\n" +
           "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\">\n" +
             "<body>\n" +
               "<h2>\n" +
                 "<xsl:value-of select=\"parameter[@name='apiName']/text()\" />\n" +
                 "<xsl:text> Application</xsl:text>\n" +
               "</h2>\n" +
               "<h3>Versions</h3>\n" +
               "<xsl:text>API: </xsl:text>\n" +
               "<xsl:value-of select=\"parameter[@name='apiVersion']/text()\" />\n" +
               "<br />\n" +
               "<xsl:text>XINS server: </xsl:text>\n" +
               "<xsl:value-of select=\"parameter[@name='xinsServerVersion']/text()\" />\n" +
               "<br />\n" +
               "<xsl:text>XINS common: </xsl:text>\n" +
               "<xsl:value-of select=\"parameter[@name='xinsCommonVersion']/text()\" />\n" +
               "<br />\n" +
               "<xsl:text>XINS build version: </xsl:text>\n" +
               "<xsl:value-of select=\"parameter[@name='apiBuildVersion']/text()\" />\n" +
               "<br />\n" +
               "<xsl:text>XINS build time: </xsl:text>\n" +
               "<xsl:value-of select=\"parameter[@name='apiBuildTime']/text()\" />\n" +
               "<br />\n" +
               "<xsl:text>API start-up time: </xsl:text>\n" +
               "<xsl:value-of select=\"parameter[@name='apiStartUpTime']/text()\" />\n" +
               "<br />\n" +
               "<h3>Functions</h3>\n" +
               "<xsl:for-each select=\"data/command\">\n" +
                 "<xsl:if test=\"position() > 1\">\n" +
                   "<xsl:text>, </xsl:text>\n" +
                 "</xsl:if>\n" +
                 "<a href=\"?command={@name}\">\n" +
                   "<xsl:value-of select=\"@name\" />\n" +
                 "</a>\n" +
               "</xsl:for-each>\n" +
               "<h3>Session properties</h3>\n" +
               "<table>\n" +
                 "<tr><td><strong>Key</strong></td><td><strong>Value</strong></td></tr>\n" +
                 "<xsl:for-each select=\"data/sessionproperties/property\">\n" +
                   "<tr><td>\n" +
                     "<xsl:value-of select=\"@name\" />\n" +
                   "</td><td>\n" +
                     "<xsl:value-of select=\"text()\" />\n" +
                   "</td></tr>\n" +
                 "</xsl:for-each>\n" +
               "</table>\n" +
               "<h3>Actions</h3>\n" +
               "<p>Command template cache management:\n" +
                 "<a href=\"?command=Control&amp;action=FlushCommandTemplateCache\">Flush</a>\n" +
                 "<a href=\"?command=Control&amp;action=RefreshCommandTemplateCache\">Refresh</a>\n" +
               "</p>\n" +
               "<p>Session properties management:\n" +
                 "<a href=\"?command=Control&amp;action=RemoveSessionProperties\">Remove all Session Properties</a>\n" +
               "</p>\n" +
               "<p>XINS meta functions: \n" +
                 "<a href=\"?_function=_GetVersion&amp;_convention=_xins-std\">Version</a>,\n" +
                 "<a href=\"?_function=_GetStatistics&amp;detailed=true&amp;_convention=_xins-std\">Statistics</a>,\n" +
                 "<a href=\"?_function=_GetSettings&amp;_convention=_xins-std\">Settings</a>\n" +
               "</p>\n" +
             "</body>\n" +
           "</html>\n" +
         "</xsl:template>\n" +
         "</xsl:stylesheet>";
       return result;
    }
 }
