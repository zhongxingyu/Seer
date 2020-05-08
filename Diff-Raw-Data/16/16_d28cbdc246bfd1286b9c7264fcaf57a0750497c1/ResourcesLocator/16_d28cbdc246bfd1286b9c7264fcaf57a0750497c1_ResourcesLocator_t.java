 /*==============================================================================
  * File $Id$
  * Project: ProWim
  *
  * $LastChangedDate: 2010-06-30 13:50:09 +0200 (Mi, 30 Jun 2010) $
  * $LastChangedBy: specht $
  * $HeadURL: https://repository.ebcot.info/wivu/trunk/prowim-server/src/de/ebcot/prowim/resources/ResourcesLocator.java $
  * $LastChangedRevision: 4173 $
  *------------------------------------------------------------------------------
  * (c) 10.06.2009 Ebcot Business Solutions GmbH. More info: http://www.ebcot.de
  * All rights reserved. Use is subject to license terms.
  *==============================================================================
  *
  *This file is part of ProWim.
 
 ProWim is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 ProWim is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with ProWim.  If not, see <http://www.gnu.org/licenses/>.
 
 Diese Datei ist Teil von ProWim.
 
 ProWim ist Freie Software: Sie können es unter den Bedingungen
 der GNU General Public License, wie von der Free Software Foundation,
 Version 3 der Lizenz oder (nach Ihrer Option) jeder späteren
 veröffentlichten Version, weiterverbreiten und/oder modifizieren.
 
 ProWim wird in der Hoffnung, dass es nützlich sein wird, aber
 OHNE JEDE GEWÄHELEISTUNG, bereitgestellt; sogar ohne die implizite
 Gewährleistung der MARKTFÄHIGKEIT oder EIGNUNG FÜR EINEN BESTIMMTEN ZWECK.
 Siehe die GNU General Public License für weitere Details.
 
 Sie sollten eine Kopie der GNU General Public License zusammen mit diesem
 Programm erhalten haben. Wenn nicht, siehe <http://www.gnu.org/licenses/>.
  */
 package org.prowim.resources;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Properties;
 
 import org.prowim.datamodel.algernon.AlgernonConstants;
 
 import de.ebcot.tools.logging.Logger;
 
 
 /**
  * This class is used to find resources on the jboss instalation path. Resources are :<br/>
  * 1. The protege-project-file placed in JBOSS_HOME/server/$running-config/etc/ontology/protege-projectfile.pprj <br/>
  * NOTE: developer , who want to change the used protege file can do that, by changing KNOWLEDGE_BASE_FILE_LOCATION attribute<br/>
  * and the name in the properties-file in JBOSS_HOME/server/$running-config/conf/protege.properties
  * 
  * @author Saad Wardi
  * @version $Revision: 4173 $
  */
 public final class ResourcesLocator
 {
     /**
      * Description.
      */
     public static final String      PROWIM_APPSERVER_URL_PROPERTY_KEY  = "prowim.appserver.url";
 
     /** the JNLP openfile. */
     static final String             KNOWLEDGEBASE_PROPERTIES_JNLP_PATH = "JNLPPath";
 
     /**
      * The default value for the value of {@value #PROWIM_SERVER_PORT_PROP}.
      */
     private static final String     PROWIM_DEFAULT_SERVER_PORT         = "80";
 
     /**
      * Description.
      */
     private static final String     JBOSS_SERVER_HOME_DIR_PROPERTY_KEY = "jboss.server.home.dir";
 
     private final static String     PROWIM_SERVER_PROPERTIES_FILE      = "prowim.properties";
 
    private final static String     PROWIM_SERVER_PORT_PROP            = "org.prowim.server.port";
 
     private final static String     REPOSITORY_PATH                    = "/server";
     private final static String     REPOSITORY_DIR                     = "/etc/repository";
     private final static String     REPOSITORY_PROCESSES_CHART_DIR     = REPOSITORY_DIR + "/processes_charts";
     private final static String     CONFIG_DIR                         = "/conf/";
     private final static String     RESOURCES_TEMP_DIR                 = "/etc/resources/temp/";
 
     private final static Logger     LOG                                = Logger.getLogger(ResourcesLocator.class);
 
     // JNLP Property.
     private static final String     JNLP_PATH_PROP                     = "de.ebcot.prowim.jnlp.openfile";
 
     /** Server version. */
     private final static String     SERVER_VERSION_FILE                = "./version.txt";
 
     /** Model-Editor version. */
     private static final String     MODEL_VERSION_FILE                 = getRepositoryDir() + "/editor/version.txt";
 
     private static final Properties PROWIM_PROPERTIES                  = new Properties();
 
     static
     {
         try
         {
             FileInputStream stream = new FileInputStream(System.getProperty(JBOSS_SERVER_HOME_DIR_PROPERTY_KEY) + CONFIG_DIR
                     + PROWIM_SERVER_PROPERTIES_FILE);
 
             PROWIM_PROPERTIES.load(stream);
             stream.close();
         }
         catch (IOException e)
         {
             throw new RuntimeException("Unable to load " + PROWIM_SERVER_PROPERTIES_FILE);
         }
     }
 
     /**
      * Constructor.
      */
     private ResourcesLocator()
     {
     }
 
     /**
      * Gets the server hostname.
      * 
      * @return the server hostname.
      */
     public static String getServerHost()
     {
         return System.getProperty(PROWIM_APPSERVER_URL_PROPERTY_KEY);
 
     }
 
     /**
      * Gets the server port.
      * 
      * @return the server port.
      */
     public static String getServerPort()
     {
         String port = System.getProperty(PROWIM_SERVER_PORT_PROP);
 
         // default value
         if (port == null)
         {
             port = PROWIM_DEFAULT_SERVER_PORT;
         }
         return port;
     }
 
     /**
      * Gets the path of the temp folder used to store resources temporarly.
      * 
      * @return the resources temp diroctory.
      */
     public static String getResourcesTempDir()
     {
         return System.getProperty(JBOSS_SERVER_HOME_DIR_PROPERTY_KEY) + RESOURCES_TEMP_DIR;
     }
 
     /**
      * Reads the server version from the version.txt file.
      * 
      * @return not null server version.
      */
     private static String readVersion(String file)
     {
         LOG.debug("LOADING ProWim-Server VERSION DETAILS ...   ");
         final InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(file);
         String version = AlgernonConstants.ERROR;
         if (is != null)
         {
             /** Load from the file. */
             try
             {
                 int x;
                 LinkedList<Integer> bl = new LinkedList<Integer>();
                 while ((x = is.read()) != -1)
                 {
                     bl.add(new Integer(x));
                 }
                 byte[] text = new byte[bl.size()];
                 for (int i = 0; i < bl.size(); i++)
                 {
                     text[i] = bl.get(i).byteValue();
                 }
                 version = new String(text);
 
             }
             catch (Exception e)
             {
                 LOG.error("Could not load the version details from file : ", e);
             }
             return version;
         }
         else
         {
             throw new IllegalStateException("Could not read version.txt file ");
         }
 
     }
 
     /**
      * Gets the ProWim-Server version .
      * 
      * @return not null server version.
      */
     public static String getServerVersion()
     {
         String version = readVersion(SERVER_VERSION_FILE);
         LOG.info("The ProWim server version is  :  " + version);
         return version;
     }
 
     /**
      * Gets the ProWim-Server version .
      * 
      * @return not null server version.
      */
     public static String getModelEditorVersion()
     {
         InputStream is = null;
         try
         {
             is = new FileInputStream(MODEL_VERSION_FILE);
         }
         catch (FileNotFoundException e)
         {
             LOG.error("Could not load the version details from file : ", e);
         }
         String version = AlgernonConstants.ERROR;
         if (is != null)
         {
             /** Load from the file. */
             try
             {
                 int x;
                 LinkedList<Integer> bl = new LinkedList<Integer>();
                 while ((x = is.read()) != -1)
                 {
                     bl.add(new Integer(x));
                     System.out.print(x);
                 }
                 byte[] text = new byte[bl.size()];
                 for (int i = 0; i < bl.size(); i++)
                 {
                     text[i] = bl.get(i).byteValue();
                 }
                 version = new String(text);
             }
             catch (Exception e)
             {
                 LOG.error("Could not load the version details from file : ", e);
             }
             LOG.info("modeleditor    " + MODEL_VERSION_FILE);
             LOG.info("The ProWim server version is  :  " + version);
             return version;
         }
         else
         {
             throw new IllegalStateException("Could not read version.txt file ");
         }
     }
 
     /**
      * Returns $JBOSS_HOME/etc/repository directory path.
      * 
      * @return not null directory path of the repository.
      */
     private static String getRepositoryDir()
     {
         return System.getProperty(JBOSS_SERVER_HOME_DIR_PROPERTY_KEY) + REPOSITORY_DIR;
     }
 
     /**
      * Returns $JBOSS_HOME/etc/repository/processes_charts/ directory path.
      * 
      * @return not null directory path of the repository.
      */
     public static String getRepositoryProcessChartDir()
     {
         String dir = null;
         URL url = ResourcesLocator.class.getResource(".");
         String input = url.getPath();
         List<String> list = Arrays.asList(input.split(REPOSITORY_PATH));
         dir = list.get(0);
 
         return dir + REPOSITORY_PROCESSES_CHART_DIR;
     }
 
     /**
      * Returns the JNLP Path.
      * 
      * @return not null JNLP Path.
      */
     public static String getJNLPPath()
     {
         return PROWIM_PROPERTIES.getProperty(JNLP_PATH_PROP);
     }
 
 }
