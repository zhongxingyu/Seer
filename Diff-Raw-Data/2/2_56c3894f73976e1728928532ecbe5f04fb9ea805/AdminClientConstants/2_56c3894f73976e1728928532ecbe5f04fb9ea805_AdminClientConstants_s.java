 /*
  * SCI-Flex: Flexible Integration of SOA and CEP
  * Copyright (C) 2008, 2009  http://sci-flex.org
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, write to the Free Software Foundation, Inc.,
  * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
  */
 
 package org.sciflex.plugins.synapse.esper.client;
 
 /**
  * Admin Web Client Constants.
  */
 public class AdminClientConstants {
 
     /**
      * Key identifying the URL of the underlying server
      */
     public static String SERVER_URL = "ServerURL";
 
     /**
      * Key identifying the Configuration Context used
      */
     public static final String CONFIGURATION_CONTEXT = "ConfigurationContext";
 
     /**
      * Key of the admin service cookie used
      */
     public static final String ADMIN_SERVICE_COOKIE = "wso2carbon.admin.service.cookie";
 
     /**
      * Default length of a string made short.
      */
     public static final int DEFAULT_SHORT_STRING_LENGTH = 32;
 
     /**
      * The default width of a graph dipicting a statistic.
      */
     public static final int DEFAULT_GRAPH_WIDTH = 500;
 
     /**
      * The default minimum scale of X axis for plots.
      */
     public static final int DEFAULT_GRAPH_X_SCALE_MIN = 5;
 
     /**
      * The default maximum scale of X axis for plots.
      */
     public static final int DEFAULT_GRAPH_X_SCALE_MAX = 500;
 
     /**
      * The default scale of X axis for plots.
      */
     public static final int DEFAULT_GRAPH_X_SCALE = 50;
 
     /**
      * The default factor of the X axis for plots.
      */
     public static final int DEFAULT_GRAPH_X_SCALE_FACTOR = 5;
 
     /**
      * Default interval for refreshing statistics.
      */
     public static final int DEFAULT_STATISTICS_REFRESH_INTERVAL = 6000;
 
     /**
      * Default minimum interval for refreshing statistics.
      */
     public static final int DEFAULT_STATISTICS_REFRESH_INTERVAL_MIN = 1000;
 
     /**
      * Default maximum interval for refreshing statistics.
      */
     public static final int DEFAULT_STATISTICS_REFRESH_INTERVAL_MAX = 600000;
 
     /**
      * Default query page.
      */
     public static final int DEFAULT_QUERY_PAGE = 0;
 
     /**
      * Default query page size.
      */
     public static final int DEFAULT_QUERY_PAGE_SIZE = 8;
 
    /**
      * Default mediator page.
      */
     public static final int DEFAULT_MEDIATOR_PAGE = 0;
 
     /**
      * Default Mediator page size.
      */
     public static final int DEFAULT_MEDIATOR_PAGE_SIZE = 9;
 
     /**
      * The level of load (%) that will generate a warning.
      */
     public static final int LOAD_WARNING_LEVEL = 60;
 
     /**
      * The level of load (%) that will generate a critical alert.
      */
     public static final int LOAD_CRITICAL_LEVEL = 90;
 
     /**
      * Value corresponding to a Amber colored graph.
      */
     public static final int FLOT_GRAPH_COLOR_AMBER = 0;
 
     /**
      * Value corresponding to a Blue colored graph.
      */
     public static final int FLOT_GRAPH_COLOR_BLUE = 1;
 
     /**
      * Value corresponding to a Red colored graph.
      */
     public static final int FLOT_GRAPH_COLOR_RED = 2;
 
     /**
      * Value corresponding to a Green colored graph.
      */
     public static final int FLOT_GRAPH_COLOR_GREEN = 3;
 
     /**
      * Default graph tick count.
      */
     public static final int DEFAULT_GRAPH_TICKS = 10;
 
     /**
      * Default graph tick starting coordinate.
      */
     public static final int DEFAULT_GRAPH_START = 0;
 
     /**
      * Rate at which configuration parts are refreshed with
      * respect to statistics.
      */
     public static final int CONFIGURATION_PARTS_REFRESH_FACTOR = 10;
 
     /**
      * Rate at which dashboard parts are refreshed with
      * respect to statistics.
      */
     public static final int DASHBOARD_PARTS_REFRESH_FACTOR = 20;
 
     /**
      * The javascript environment update script index
      */
     public static final String UPDATE_SCRIPT = "updateScript";
 
     /**
      * Stores instance of a {@link SEPluginAdminClient}
      */
     public static final String CLIENT_INSTANCE = "clientInstance";
 
     /**
      * Stores instance of a statistics object
      */
     public static final String STATISTICS_OBJECT_INSTANCE = "statisticsObjectInstance";
 
     /**
      * Identifies a part which is dynamically populated with a report.
      */
     public static final String REPORT_PART_ID = "partID";
 
     /**
      * Key of a question to be asked on the client.
      */
     public static final String CLIENT_QUESTION_KEY = "clientQuestionKey";
 
     /**
      * Unique Identifier of the Mediator.
      */
     public static final String MEDIATOR_UID = "mediatorUID";
 
     /**
      * Mediator page offset
      */
     public static final String MEDIATOR_PAGE_OFFSET = "mediatorPageOffset";
 
     /**
      * Query page offset
      */
     public static final String QUERY_PAGE_OFFSET = "queryPageOffset";
 
     /**
      * ID of a Mediator Operation.
      */
     public static final String MEDIATOR_OPERATION_ID = "operationID";
 
     /**
      * Interval for refreshing statistics
      */
    public static final String STATISTICS_REFRESH_INTERVAL = "statRefreshInterval";
 
     /**
      * Scale of X axis for plot of percentage load on Mediator.
      */
     public static final String LOAD_PERCENTAGE_X_SCALE = "loadPercentageXScale";
 
     /**
      * Whether to show plot of percentage load on Mediator.
      */
     public static final String SHOW_LOAD_PERCENTAGE_GRAPH = "showLoadPercentageGraph";
 
     /**
      * Scale of X axis for average request/response time plots.
      */
     public static final String REQUEST_RESPONSE_TIME_X_SCALE = "requestResponseTimeXScale";
 
     /**
      * Whether to show plot of request time vs time.
      */
     public static final String SHOW_REQUEST_TIME_GRAPH = "showRequestTimeGraph";
 
     /**
      * Whether to show plot of response time vs time.
      */
     public static final String SHOW_RESPONSE_TIME_GRAPH = "showResponseTimeGraph";
 
     /**
      * Width of Requests vs Time and Response vs Time graphs.
      */
     public static final String REQUEST_RESPONSE_TIME_GRAPH_WIDTH = "requestResponseTimeGraphWidth";
 
     /**
      * Width of percentage load on mediator graph.
      */
     public static final String LOAD_PERCENTAGE_GRAPH_WIDTH = "loadPercentageGraghWidth";
 
     /**
      * URI of the CEP Instance
      */
     public static final String CEP_INSTANCE_URI = "cepInstanceURI";
 
     /**
      * Registry Key of EPL
      */
     public static final String REGISTRY_KEY = "registryKey";
 
     /**
      * Static EPL Query
      */
     public static final String STATIC_EPL = "staticEPL";
 
     /**
      * Dynamic EPL Query
      */
     public static final String DYNAMIC_EPL = "dynamicEPL";
 
     /**
      * Name of Admin Service
      */
     public static final String SERVICE_NAME = "SEPluginAdmin";
 
     /**
      * Internationalization Resource Bundle Name
      */
     public static final String BUNDLE = "org.sciflex.plugins.synapse.esper.client.i18n.Resources";
 
     /**
      * Type of the Axiom Mediator
      */
     public static final String AXIOM_MEDIATOR_BASE_TYPE = "org.sciflex.plugins.synapse.esper.mediators.AxiomMediator";
 
     /**
      * Type of the XML Mediator
      */
     public static final String XML_MEDIATOR_BASE_TYPE = "org.sciflex.plugins.synapse.esper.mediators.XMLMediator";
 
 }
