 /**
 * Copyright (C) 2007-2008 Elastic Grid, LLC.
 * 
 * This file is part of Elastic Grid.
 * 
 * Elastic Grid is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 * 
 * Elastic Grid is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
  * along with Elastic Grid.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.elasticgrid.utils.amazon;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class AWSUtils {
     public static final String AWS_ACCESS_ID = "aws.accessId";
     public static final String AWS_SECRET_KEY = "aws.secretKey";
     private static final Logger logger = Logger.getLogger(AWSUtils.class.getName());
 
     public static Properties loadEC2Configuration() throws IOException {
         // try to load properties from $HOME/.eg/aws.properties
         Properties awsProperties = new Properties();
         File awsPropertiesFile = new File(System.getProperty("user.home") + File.separatorChar + ".eg",
                 "aws.properties");
         InputStream stream = null;
         try {
             stream = new FileInputStream(awsPropertiesFile);
             awsProperties.load(stream);
         } catch (Exception e) {
             // do nothing -- this is expected behaviour
         } finally {
             try {
                 if (stream != null)
                     stream.close();
             } catch (IOException e) {
                 logger.log(Level.SEVERE, "Could not close stream", e);
             }
         }
         // try to load properties from $EG_HOME/eg.properties
         if (awsProperties.size() == 0) {
             awsProperties = new Properties();
             awsPropertiesFile = new File(System.getProperty("EG_HOME") + File.separatorChar + "config",
                     "eg.properties");
             try {
                 stream = new FileInputStream(awsPropertiesFile);
                 awsProperties.load(stream);
             } catch (Exception e) {
                 // do nothing -- this is expected behaviour
             } finally {
                 try {
                     if (stream != null)
                         stream.close();
                 } catch (IOException e) {
                     logger.log(Level.SEVERE, "Could not close stream", e);
                 }
             }
         }
 
         return awsProperties;
     }
 
 }
