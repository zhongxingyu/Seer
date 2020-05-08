 /*
 Copyright (c) 2013 J. L. Canales Gasco
  
 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.
  
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
  
 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA}]
 */
 package org.rotarysource.standalone;
 
 
 import java.nio.charset.Charset;
 
 import org.rotarysource.core.CepEngine;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 import org.springframework.jms.listener.AbstractMessageListenerContainer;
 
 
 //import com.espertech.esper.example.servershell.jmx.EPServiceProviderJMX;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 public class ServerShellMain
 {
     private static Log log = LogFactory.getLog(ServerShellMain.class);
 
     private boolean isShutdown;
 
     public static void main(String[] args) throws Exception
     {
         try
         {
         	log.info("System properties information : file.encoding=" + Charset.defaultCharset().name());
              new ServerShellMain();
         }
         catch (Exception t)
         {
             log.error("Error starting server shell: " + t.getMessage(), t);
             System.exit(-1);
         }
     }
 
     public ServerShellMain() throws Exception
     {
     	log.info("CEP System version: 5.0.0-SNAPSHOT");
     	log.info("Starting server shell");
 
     	isShutdown = false;
     	
         // Initialize engine
         log.info("Getting Esper engine instance");
        ApplicationContext springAppContext = new ClassPathXmlApplicationContext("AppContext.xml");
         
        CepEngine statementEngine = (CepEngine) springAppContext.getBean("cepEngine");
      
 		// Connect to JMS
 		log.info("Connecting to JMS server through a Listener Container");
 		AbstractMessageListenerContainer jmsListenerContainer  = (AbstractMessageListenerContainer) springAppContext.getBean("listenerContainer");
 		
 		jmsListenerContainer.start();
 
 
         // Register shutdown hook
         Runtime.getRuntime().addShutdownHook(new Thread()
         {
             public void run()
             {
                 isShutdown = true;
             }
         });
 
 		log.info("Statements Processor started.");
 
         do
         {
             // sleep
             Thread.sleep(1000);
 
         }
         while (!isShutdown);
 
     	log.info("EVENT DRIVEN CONTROL SYSTEM: Starting Shutdown server process");
     	log.info("Disconecting Input queues"); 
     	jmsListenerContainer.shutdown();
     	log.info("Shutting down CEP Engine"); 
     	statementEngine.shutdown();
 
         log.info("Exiting");
         System.exit(-1);
     }
 
 }
