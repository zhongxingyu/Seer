 /*
  * Copyright 2012 Danylo Vashchilenko
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package org.key2gym;
 
 import com.googlecode.flyway.core.Flyway;
 import com.mchange.v2.c3p0.DataSources;
 import java.awt.EventQueue;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.sql.SQLException;
 import java.util.List;
 import java.util.Locale;
 import java.util.Properties;
 import java.util.logging.Level;
 import javax.swing.UnsupportedLookAndFeelException;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 import org.key2gym.business.StorageService;
 import org.key2gym.presentation.MainFrame;
 import org.key2gym.persistence.connections.configurations.ConnectionConfiguration;
 import org.key2gym.persistence.connections.ConnectionConfigurationsManager;
 import org.key2gym.presentation.dialogs.AbstractDialog;
 import org.key2gym.presentation.dialogs.ConnectionsManagerDialog;
 import org.key2gym.persistence.connections.factories.PersistenceFactory;
 
 /**
  * This is the main class of the application.
  *
  * It's responsible for the following tasks:
  * <p/>
  *
  * <ul> <li> Initializing Logging system </li> <li> Reading and applying
  * application properties. <li> Launching MainFrame </li> </ul>
  *
  * @author Danylo Vashchilenko
  */
 public class Starter {
 
     private static final Logger logger = Logger.getLogger(Starter.class.getName());
     private static final Properties properties = new Properties();
 
     /**
      * The main method which performs all task as described in class
      * description.
      *
      * @param args an array of arguments
      */
     public static void main(String[] args) {
         /*
          * Configures the logger using 'etc/log.properties' which should be on
          * the class path.
          */
         try {
            PropertyConfigurator.configure(new FileInputStream("etc/logging.properties"));
         } catch (FileNotFoundException ex) {
             java.util.logging.Logger.getLogger(Starter.class.getName()).log(Level.SEVERE, null, ex);
             return;
         }
 
         logger.info("Starting...");
 
         /*
          * The array contains the names of all expected arguments.
          */
         String[] expectedArgumentsNames = new String[]{"connection"};
 
         /*
          * Parses the arguments looking for expected arguments. The arguments
          * are in the '--ARGUMENTNAME=ARGUMENTVALUE' format.
          */
         for (String arg : args) {
             for (String expectedArgumentName : expectedArgumentsNames) {
                 String preffix = "--" + expectedArgumentName + "=";
                 if (arg.startsWith(preffix)) {
                     properties.put(expectedArgumentName, arg.substring(preffix.length()));
                 }
             }
         }
 
         try (FileInputStream input = new FileInputStream("etc/application.properties")) {
             properties.load(input);
         } catch (IOException ex) {
             logger.fatal(ex);
         }
 
         Locale.setDefault(new Locale((String) properties.get("locale.language"), (String) properties.get("locale.country")));
 
         String ui = (String) properties.get("ui");
         try {
             for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                 if (ui.equals(info.getName())) {
                     javax.swing.UIManager.setLookAndFeel(info.getClassName());
                     break;
                 }
             }
         } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
             logger.fatal("Failed to change the L&F!");
         }
 
         ConnectionConfigurationsManager connectionsManager = new ConnectionConfigurationsManager();
 
         if (!properties.containsKey("connection")) {
             ConnectionsManagerDialog dialog = new ConnectionsManagerDialog(connectionsManager);
 
             dialog.setVisible(true);
 
             /*
              * Quits if the user clicked cancel.
              */
             if (dialog.getResult().equals(AbstractDialog.Result.CANCEL)) {
                 return;
             }
 
             /*
              * Logs an exception and quits, if the connections manager encountered
              * an exception.
              */
             if (dialog.getResult().equals(AbstractDialog.Result.EXCEPTION)) {
                 logger.fatal("The connections manager encountered an exception.", dialog.getException());
                 return;
             }
         } else {
             /*
              * Attemps to find a connection with a code name passed in the command line.
              */
             List<ConnectionConfiguration> connections = connectionsManager.getConnections();
             for (ConnectionConfiguration connection : connections) {
                 if (connection.getCodeName().equals(properties.getProperty("connection"))) {
                     connectionsManager.selectConnection(connection);
                 }
             }
 
             /*
              * Reports and terminates, if the connection was not found.
              */
             if (connectionsManager.getSelectedConnection() == null) {
                 logger.fatal("Missing connection specified in the arguments: " + properties.getProperty("connection"));
                 return;
             }
         }
 
         ConnectionConfiguration connection = connectionsManager.getSelectedConnection();
 
         /*
          * Attempts to load the properties factory class. 
          */
         String propertiesFactoryClassBinaryName = PersistenceFactory.class.getPackage().getName() + "." + connection.getType() + "PersistenceFactory";
         Class<? extends PersistenceFactory> propertiesFactoryClass;
         try {
             propertiesFactoryClass = (Class<? extends PersistenceFactory>) Starter.class.getClassLoader().loadClass(propertiesFactoryClassBinaryName);
         } catch (ClassNotFoundException ex) {
             logger.fatal("Missing persistence factory for connection type: " + connection.getType(), ex);
             return;
         } catch (ClassCastException ex) {
             logger.fatal("Persistence factory for connection type '" + connection.getType() + "' is of the wrong type.", ex);
             return;
         }
 
         PersistenceFactory persistenceFactory;
 
         /*
          * Attempts to instantiate the properties factory.
          */
         try {
             persistenceFactory = propertiesFactoryClass.getConstructor(connection.getClass()).newInstance(connection);
         } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException | InstantiationException ex) {
             logger.fatal("Failed to instantiate the persistence properties factory for connection type: " + connection.getType(), ex);
             return;
         }
                 
         /*
          * Initializes the storage service with the persistence properties generated
          * from the selected connection.
          */
 
         logger.info("Initializing the storage service with the connection: " + connection.getCodeName());
         try {
             StorageService.initialize(persistenceFactory.getEntityManagerFactory());
         } catch (Exception ex) {
             logger.fatal("Failed to initializes the storage service.", ex);
             return;
         }
 
         logger.info("Started!");
 
         try {
             EventQueue.invokeAndWait(new Runnable() {
 
                 @Override
                 public void run() {
                     MainFrame.getInstance().setVisible(true);
                 }
             });
         } catch (InterruptedException | InvocationTargetException ex) {
             Logger.getLogger(Starter.class.getName()).error("Unexpected Exception!", ex);
         }
 
         synchronized (MainFrame.getInstance()) {
             while (MainFrame.getInstance().isVisible()) {
                 try {
                     MainFrame.getInstance().wait();
                 } catch (InterruptedException ex) {
                     Logger.getLogger(Starter.class.getName()).error("Unexpected Exception!", ex);
                 }
             }
         }
 
         Logger.getLogger(Starter.class.getName()).info("Shutting down!");
         
         StorageService.getInstance().destroy();
         
         try {
             DataSources.destroy(persistenceFactory.getDataSource());
         } catch(SQLException ex) {
             logger.error("Failed to destroy the data source.", ex);
             return;
         }
     }
 
     public static Properties getProperties() {
         return properties;
     }
 }
