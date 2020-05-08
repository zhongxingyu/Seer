 /*
  * Servlet.java
  * StockPlay - XML-RPC servletimplementatie.
  *
  * Copyright (c) 2010 StockPlay development team
  * All rights reserved.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 package com.kapti.backend.xmlrpc;
 
 import com.kapti.data.persistence.StockPlayDAO;
 import com.kapti.data.persistence.StockPlayDAOFactory;
 import com.kapti.exceptions.StockPlayException;
 import java.io.IOException;
 import java.net.URL;
 import java.util.Date;
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import org.apache.log4j.Logger;
 import org.apache.xmlrpc.XmlRpcException;
 import org.apache.xmlrpc.server.AbstractReflectiveHandlerMapping.AuthenticationHandler;
 import org.apache.xmlrpc.server.PropertyHandlerMapping;
 import org.apache.xmlrpc.server.XmlRpcHandlerMapping;
 import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
 import org.apache.xmlrpc.webserver.XmlRpcServlet;
import org.apache.xmlrpc.webserver.XmlRpcServletServer;
 
 /**
  * \brief XML-RPC servletimplementatie.
  *
  * Dit is een uitbreiding van de standaard XML-RPC servlet. Deze voorziet
  * in een aantal extra features, namelijk:
  *  - Registratie van een authentication handler
  *  - Doorgeven van persistente data aan method handlers
  *  - Flexibele method mapping via property-file
  *  - Gebruik van customized XML-RPC server voor exception trapping
  */
 public class Servlet extends XmlRpcServlet {
     //
     // Dataleden
     //
 
     private StockPlayDAO mDAO;
     static Logger mLogger = Logger.getLogger(Servlet.class);
     private static Date mDateStart = new Date();
 
     //
     // Constructie
     //
 
     public Servlet() throws XmlRpcException {
         super();
         try {
             mDAO = StockPlayDAOFactory.getDAO();
         }
         catch (StockPlayException e) {
             mLogger.error("Can't start StockPlayDAOFactory");
             throw new RuntimeException("Can't start StockPlayDAOFactory");
         }
     }
 
 
     //
     // Methoden
     //
     
     @Override
     public void init(ServletConfig pConfig) throws ServletException {
         super.init(pConfig);
         XmlRpcServerConfigImpl config = new XmlRpcServerConfigImpl();
         config.setEnabledForExtensions(true);
         getXmlRpcServletServer().setConfig(config);
     }
 
    /**
     * Door deze methode te overschrijven kunnen we een eigen XmlRpcServer
     * gebruiken.
     */
    @Override
    protected XmlRpcServletServer newXmlRpcServer(ServletConfig iConfig) throws XmlRpcException {
        // Configuratie wordt niet gebruikt?
        XmlRpcServletServer oServer = new ServletServer();
        return oServer;
    }

 
     /**
      * Deze methode wordt gebruikt bij het configureren van de servlet server,
      * en zorgt voor de connectie tussen aangevraagde methodes en hun lokale
      * tegenhanger. Om een flexibele manier van werken te bekomen, gebruiken we
      * hier een property handler in plaats van een statische configuratie.
      * Ook registreren we hier de authenticatie-handler, verantwoordelijk voor
      * het  controleren van doorgegeven credentials.
      */
     @Override
     protected XmlRpcHandlerMapping newXmlRpcHandlerMapping() throws XmlRpcException {
         // Property-handler registreren
         PropertyHandlerMapping oMapping = null;
         URL tUrl = Thread.currentThread().getContextClassLoader().getResource("XmlRpcServlet.properties");
         if (tUrl == null) {
             throw new XmlRpcException("Failed to locate resource XmlRpcServlet.properties");
         }
         try {
             oMapping = newPropertyHandlerMapping(tUrl);
         } catch (IOException e) {
             throw new XmlRpcException("Failed to load resource " + tUrl + ": " + e.getMessage(), e);
         }
 
         // Authenticatie-handler registreren
         AuthenticationHandler tHandler = new AuthHandler(mDAO);
         oMapping.setAuthenticationHandler(tHandler);
 
         return oMapping;
     }
 
 
     /**
      * Deze methode haalt een property-handler op, gebaseerd op een property-
      * bestand. Normaal worden de mappings tussen een property name en een
      * property handler statisch verzorgd, maar door het gebruik van een
      * eigen property handler maken we dit proces flexibel en makkelijk
      * aanpasbaar. We bekomen dit door de opgehaalde property handler
      * dusdanig aan te passen dat een eigen handler-factory gebruikt wordt (dit
      * is nodig om voor het verwerken van een request de persistente data
      * te kunnen doorgeven aan de handler in kwestie).
      */
     @Override
     protected PropertyHandlerMapping newPropertyHandlerMapping(URL iUrl) throws IOException, XmlRpcException {
         // Property-handler ophalen in superklasse, dit doen we niet zelf
         PropertyHandlerMapping oMapping = super.newPropertyHandlerMapping(iUrl);
 
         // Property-handler aanpassen naar onze wensen
         ProcessorFactory factory = new ProcessorFactory(mDAO);
         oMapping.setRequestProcessorFactoryFactory(factory);
         oMapping.load(Thread.currentThread().getContextClassLoader(), iUrl);
 
         return oMapping;
     }
 
     /**
      * Ophalen van uptime.
      * 
      * @return
      */
     public static double getUptime() {
         return (double)(((new Date()).getTime() - mDateStart.getTime())/1000.0);
     }
 }
