 /*
  * OpenRemote, the Home of the Digital Home.
  * Copyright 2008-2012, OpenRemote Inc.
  *
  * See the contributors.txt file in the distribution for a
  * full listing of individual contributors.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.openremote.controller.servlet;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.Logger;
 import org.openremote.controller.Constants;
 import org.openremote.controller.ControllerConfiguration;
 import org.openremote.controller.service.ClientService;
 import org.openremote.controller.service.ConfigurationService;
 import org.openremote.controller.spring.SpringContext;
 import org.openremote.controller.utils.AuthenticationUtil;
 import org.openremote.controller.utils.ResultSetUtil;
 
 import freemarker.template.Configuration;
 import freemarker.template.ObjectWrapper;
 import freemarker.template.Template;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * This servlet is used to show the list of clients, 
  * manage the clients (accept, revoke and putting the client in a group)
  * 
  * @author <a href="mailto:melroy.van.den.berg@tass.nl">Melroy van den Berg</a> 2012
  * 
  */
 @SuppressWarnings("serial")
 public class AdministratorServlet extends HttpServlet
 {
   private final static Logger logger = Logger.getLogger(Constants.SERVLET_LOG_CATEGORY);
 
   private static ClientService clientService = (ClientService) SpringContext.getInstance().getBean("clientService");
   private static ConfigurationService configurationService = (ConfigurationService) SpringContext.getInstance().getBean("configurationService");
  
   /**
    * Set the result set into a collection
    * 
    * @param resultSet ResultSet
    * @return Collection
    */
   @SuppressWarnings("rawtypes")
   private Collection resultSetToCollection(ResultSet resultSet)
   {    
      Collection collection = null;
           
      try 
      {
         collection = ResultSetUtil.getMaps(resultSet);
      } 
      catch (SQLException e)
      {
          logger.error("SQLException: " + e.getMessage());
      }
      return collection;
   }
 
   /**
    * Set the result sets in a hash map and give it to Freemarker
    * 
    * @return String a HTML template with the data
    */
    @SuppressWarnings("rawtypes")
    private String setDataInTemplate() 
    {
       Map<String, Object> root = new HashMap<String, Object>();
       String result = "";
       String errorString = "";
       int numClients = 0;
       Collection clientCollection = null, settingCollection = null;
       
       ResultSet clients = clientService.getClients();     
       numClients = clientService.getNumClients();
       clientCollection = resultSetToCollection(clients);
       clientService.free();
       
       ResultSet settings = configurationService.getAllItems();
       settingCollection = resultSetToCollection(settings);
       configurationService.free();
       
       try 
       {     
          if(clientCollection != null && settingCollection != null)
          {
             if(numClients > 0)
             {
                root.put( "clients", clientCollection );
             }
             else
             {
               errorString = "No clients in the database.";
             }
             root.put( "configurations", settingCollection );
          }
          else
          {
             errorString = "Database problem!";
          }
          
          if(configurationService.shouldReboot())
          {
             errorString += "<br /><b><font color='#FFA500'>Do NOT forget to restart your Tomcat server manually to apply the changes.</font></b>";
          }
          
          if(!errorString.isEmpty())
          {
             root.put( "errorMessage", errorString);
          }
          
          result = freemarkerDo(root, "administrator.ftl");
       }
       catch(Exception e) 
       {
          result = "<h1>Template Exception</h1>";
          result += e.getLocalizedMessage();
          logger.error(e.getMessage());
       }
       return result;
    }
   
   /**
    * Process a template using FreeMarker and print the results
    *  
    * @param root HashMap with data
    * @param template the name of the template file
    * @return HTML template with the specified data
    * @throws Exception FreeMarker exception
    */
   @SuppressWarnings("rawtypes")
   static String freemarkerDo(Map root, String template) throws Exception
   {
      Configuration cfg = new Configuration();
      // Read the XML file and process the template using FreeMarker
      ControllerConfiguration configuration = ControllerConfiguration.readXML();
      
      cfg.setDirectoryForTemplateLoading(new File(configuration.getResourcePath()));
      cfg.setObjectWrapper(ObjectWrapper.DEFAULT_WRAPPER);
      Template temp = cfg.getTemplate(template);
      StringWriter out = new StringWriter();
      temp.process( root, out );
 
      return out.getBuffer().toString();
   }
 
   
   /**
    * Get request handler for the administrator URI
    * @param request the request from HTTP servlet
    * @param reponse where the respond can be written to
    */  
   @Override 
   protected void doGet(HttpServletRequest request, HttpServletResponse response)
                                                               throws ServletException, IOException
   {
      if(!AuthenticationUtil.isAuth(request)){
         response.sendRedirect("/controller/login");
         return;
      }
      
      PrintWriter printWriter = response.getWriter();     
      try
      {
         printWriter.print(setDataInTemplate());
         response.setStatus(200);
      }
      catch (NullPointerException e)
      {
         response.setStatus(406);
         logger.error("NullPointer in Administrator Servlet: ", e);
      }
   }
 }
