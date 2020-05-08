 /**
  *** This software is licensed under the GNU Affero General Public License, version 3.
  *** See http://www.gnu.org/licenses/agpl.html for full details of the license terms.
  *** Copyright 2012 Andrew Heald.
  */
 
 package uk.org.sappho.applications.configuration.service;
 
 public class ConfigurationException extends Exception {
 
     public ConfigurationException(String message) {
 
         super(message);
     }
 
     public ConfigurationException(String message, Throwable throwable) {
 
        super(message, throwable);
     }
 }
