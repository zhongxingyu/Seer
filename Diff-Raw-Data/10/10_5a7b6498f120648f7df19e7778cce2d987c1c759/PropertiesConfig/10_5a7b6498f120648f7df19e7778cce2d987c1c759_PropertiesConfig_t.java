 /**
  * SAHARA Rig Client
  * 
  * Software abstraction of physical rig to provide rig session control
  * and rig device control. Automatically tests rig hardware and reports
  * the rig status to ensure rig goodness.
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
  * @author Michael Diponio mdiponio
  * @date 5th October 2009
  *
  * Changelog:
  * - 05/10/2009 - mdiponio - Initial file creation.
  * - 21/10/2009 - mdiponio - Implmented the <code>reload</code>, <code>serialise</code>
  *                           and <code>removeProperty</code> methods.
  */
 package au.edu.uts.eng.remotelabs.rigclient.util;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 
 /**
  * Java properties configuration. By default uses
  * <code>config/rigclient.properties</code>.
  */
 public class PropertiesConfig implements IConfig
 {
     /** Location of default properties file. */
     private static final String DEFAULT_CONFIG_FILE = "config/rigclient.properties";
 
     /** Java Properties class. */
     private Properties prop;
     
     /** Properties file input stream. */
     private FileInputStream propStream;
     
     /** Loaded configuration file. */
     private String configFile;
 
     /**
      * Loads configuration from the <code>config/rigclient.properties</code>
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
         this.configFile = filename;
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
 
     /* 
      * @see au.edu.uts.eng.remotelabs.rigclient.util.IConfig#getProperty(String key)
      */
     @Override
     public String getProperty(final String key)
     {
        String val = this.prop.getProperty(key);
        if (val == null)
        {
            return val;    
        }
        return val.trim();
     }
     
     /* 
      * @see au.edu.uts.eng.remotelabs.rigclient.util.IConfig#getProperty(java.lang.String, java.lang.String)
      */
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
     
     /* 
      * @see au.edu.uts.eng.remotelabs.rigclient.util.IConfig#getAllProperties()
      */
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
 
     /* 
      * @see au.edu.uts.eng.remotelabs.rigclient.util.IConfig#setProperty(String key, String value)
      */
     @Override
     public void setProperty(final String key, final String value)
     {
         this.prop.setProperty(key, value);
     }
     
     /* 
      * @see au.edu.uts.eng.remotelabs.rigclient.util.IConfig#removeProperty(String key)
      */
     @Override
     public void removeProperty(final String key)
     {
         this.prop.remove(key);
     }
 
     /* 
      * @see au.edu.uts.eng.remotelabs.rigclient.util.IConfig#reload()
      */
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
 
     /* 
      * @see au.edu.uts.eng.remotelabs.rigclient.util.IConfig#serialise()
      */
     @Override
     public synchronized void serialise()
     {
         OutputStream out = null;
         InputStream in = null;
         try
         {
             this.propStream.close();
             
             /* First create a backup of the old file which has the name standard config name with a timestamp
              * appended to it. */
             final Calendar cal = Calendar.getInstance();
             final StringBuilder backup = new StringBuilder(this.configFile);
             backup.append(".old.");
             backup.append(cal.get(Calendar.DAY_OF_MONTH));
             backup.append('-');
             backup.append(cal.get(Calendar.DATE) + 1);
             backup.append('-');
             backup.append(cal.get(Calendar.YEAR));
             backup.append('T');
             backup.append(Calendar.HOUR);
             backup.append('-');
             backup.append(Calendar.MINUTE);
             backup.append('_');
             backup.append(Calendar.SECOND);
             backup.append('.');
             backup.append(Calendar.MILLISECOND);
             
             out = new BufferedOutputStream(new FileOutputStream(backup.toString(), true));
             in = new BufferedInputStream(new FileInputStream(this.configFile));
             byte buf[] = new byte[1024];
             int r;
             while ((r = in.read(buf)) > 0)
             {
                 out.write(buf, 0, r);
             }
             out.close();
             in.close();
             
             /* Then store the new results. */
             out = new FileOutputStream(this.configFile);
             this.prop.store(out, "Sahara Rig Client Properties Configuration File");
             out.flush();
             out.close();
             
             this.propStream = new FileInputStream(this.configFile);
             this.prop = new Properties();
             this.prop.load(this.propStream);
         }
         catch (IOException e)
         {
             System.err.println("Failed to seroalise configuration file (" + this.configFile + "). The error is of type" +
                     e.getClass().getCanonicalName() + " with message " + e.getMessage() + ".");
         }
         finally
         {
             try
             {
                 if (in != null) in.close();
                 if (out != null) out.close();
             }
             catch (IOException ex) { /* Closing failed. */ }            
         }
     }
 
     /* 
      * @see au.edu.uts.eng.remotelabs.rigclient.util.IConfig#getConfigurationInfomation()
      */
     @Override
     public String getConfigurationInfomation()
     {
         return "properties configuration file: " + this.configFile;
     }
 
     /* 
      * @see au.edu.uts.eng.remotelabs.rigclient.util.IConfig#dumpConfiguration()
      */
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
