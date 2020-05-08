 /**
  * SAHARA Scheduling Server
  *
  * Schedules and assigns local laboratory rigs.
  *
  * @license See LICENSE in the top level directory for complete license terms.
  *
  * Copyright (c) 2009, University of Technology, Sydney
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without 
  * modification, are permitted provided that the following conditions are met:
  *
  *  * Redistributions of source code must retain the above copyright notice, 
  *    this list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright 
  *    notice, this list of conditions and the following disclaimer in the 
  *    documentation and/or other materials provided with the distribution.
  *  * Neither the name of the University of Technology, Sydney nor the names 
  *    of its contributors may be used to endorse or promote products derived from 
  *    this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE 
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * @author Michael Diponio (mdiponio)
  * @date 28th December 2009
  */
 
 package au.edu.uts.eng.remotelabs.schedserver.config.impl;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 
 import au.edu.uts.eng.remotelabs.schedserver.config.Config;
 
 /**
  * Java properties configuration. By default uses
  * <code>conf/schedulingserver.properties</code>.
  */
 public class PropertiesConfig implements Config
 {
     /** Location of default properties file. */
     public static final String DEFAULT_CONFIG_FILE = "conf/schedulingserver.properties";
 
     /** Java Properties class. */
     private Properties prop;
     
     /** Properties file input stream. */
     private FileInputStream propStream;
     
     /** Loaded configuration file. */
     private String configFile;
 
     /**
      * Loads configuration from the <code>config/schedulingserver.properties</code>
      * file.
      */
     public PropertiesConfig()
     {
         this(PropertiesConfig.DEFAULT_CONFIG_FILE);
     }
     
     /**
      * Loads configuration from the <code>filename</code> parameter.
      *  
      * @param filename properties file
      */
     public PropertiesConfig(final String filename)
     {
         this.configFile = new File(filename).getAbsolutePath();
         this.prop = new Properties();
         try
         {
             this.propStream = new FileInputStream(filename);
             this.prop.load(this.propStream);
         }
         catch (Exception ex)
         {
             System.err.println("Failed to load configuration file (" + this.configFile + "). The error is of type" +
                     ex.getClass().getCanonicalName() + " with message " + ex.getMessage() + ".");
         }
     }
 
     @Override
     public String getProperty(final String key)
     {
         final String val = this.prop.getProperty(key);
         if (val == null)
         {
             return val;
         }
         return val.trim();
     }
 
     @Override
     public String getProperty(final String key, final String defaultValue)
     {
         final String val = this.prop.getProperty(key);
         if (val == null)
         {
             return defaultValue;
         }
         return val.trim();
     }
 
     @Override
     public Map<String, String> getAllProperties()
     {
         final Map<String, String> all = new HashMap<String, String>();
         
         for (Object key : this.prop.keySet())
         {
             final Object val = this.prop.get(key);
             if (key instanceof String && val instanceof String)
             {
                 all.put((String)key, (String)val);
             }
         }
         
         return all;
     }
 
     @Override
     public void setProperty(final String key, final String value)
     {
         this.prop.setProperty(key, value);
     }
 
    @Override
     public void removeProperty(final String key)
     {
         this.prop.remove(key);
     }
 
     @Override
     public synchronized void reload()
     {
         try
         {
             this.propStream.close();
             this.propStream = new FileInputStream(this.configFile);
             this.prop = new Properties();
             this.prop.load(this.propStream);
         }
         catch (IOException e)
         {
             System.err.println("Failed to reload configuration file (" + this.configFile + "). The error is of type" +
             		e.getClass().getCanonicalName() + " with message " + e.getMessage() + ".");
         }
     }
 
     @Override
     public synchronized void serialise()
     {
         try
         {
             this.propStream.close();
             final FileOutputStream output = new FileOutputStream(this.configFile);
             this.prop.store(output, "Sahara Rig Client Properties Configuration File");
             output.flush();
             output.close();
             
             this.propStream = new FileInputStream(this.configFile);
             this.prop = new Properties();
             this.prop.load(this.propStream);
         }
         catch (IOException e)
         {
             System.err.println("Failed to seroalise configuration file (" + this.configFile + "). The error is of type" +
                     e.getClass().getCanonicalName() + " with message " + e.getMessage() + ".");
         }
     }
 
     @Override
     public String getConfigurationInfomation()
     {
         return "properties configuration file: " + this.configFile;
     }
 
     @Override
     public String dumpConfiguration()
     {
         final StringBuffer buf = new StringBuffer();
         for (Object o : this.prop.keySet())
         {
             buf.append(o);
             buf.append(' ');
             buf.append(this.prop.get(o));
             buf.append('\n');
         }
         return buf.toString();
     }
 }
