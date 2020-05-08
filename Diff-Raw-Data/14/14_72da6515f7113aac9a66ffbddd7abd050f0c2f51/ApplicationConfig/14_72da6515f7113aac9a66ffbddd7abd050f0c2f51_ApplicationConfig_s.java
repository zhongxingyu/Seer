 /**
  * Copyright (C) 2011
  *   Michael Mosmann <michael@mosmann.de>
  *   Jan Bernitt <unknown@email.de>
  *
  * with contributions from
  * 	nobody yet
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package de.flapdoodle.drug.config;
 
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import com.google.inject.AbstractModule;
 
 import de.flapdoodle.drug.persistence.config.Logging;
 import de.flapdoodle.drug.persistence.config.mongo.NoInstallDatabase;
 import de.flapdoodle.drug.persistence.config.mongo.Persistence;
 import de.flapdoodle.drug.persistence.config.mongo.PreviewDatabase;
 import de.flapdoodle.drug.persistence.config.mongo.ProductionDatabase;
 
 public class ApplicationConfig extends AbstractModule {
 
 	private static final Logger _logger = Logger.getLogger(ApplicationConfig.class.getName());
 
 	@Override
 	protected void configure() {
 		Profile p=getProfile();
 		install(new Logging());
 		install(new Persistence(p));
 		bind(Profile.class).toInstance(p);
 	}
 	
 	private Profile getProfile() {
 		String hostname = getHostname();
 		if ("ubuntu606m".equals(hostname)) {
 			return Profile.Production;
 		}
 		if ("bumblebee".equals(hostname)) {
 			return Profile.Local;
 		}
 		return Profile.NoInstall;
 	}
 
 	static String getHostname() {
 		String hostname = System.getProperty("deployment.hostname");
 		if (hostname == null) {
 			try {
 				hostname = InetAddress.getLocalHost().getHostName();
 				_logger.info("Hostname: " + hostname);
 			} catch (UnknownHostException e) {
 				_logger.log(Level.SEVERE, "hostname", e);
 			}
 		} else {
 			_logger.info("Hostname: " + hostname + " (from System.Properties)");
 		}
 		return hostname;
 	}
 
 	static String getUsername() {
 		String ret = System.getProperty("deployment.username");
 		if (ret == null) {
 			ret = System.getProperty("user.name");
 			_logger.info("Username: " + ret);
 		} else {
 			_logger.info("Username: " + ret + " (from System.Properties)");
 		}
 		return ret;
 	}
 
 }
