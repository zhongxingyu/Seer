 /**
  * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
  * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
  * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations under the License.
  * 
  * @author dmyersturnbull
  */
 package org.structnetalign.util;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Properties;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 import org.biojava.bio.structure.scop.ScopDatabase;
 import org.biojava.bio.structure.scop.ScopFactory;
 
 /**
  * A collection of static utilities to handle interaction with databases of the <a
  * href="http://scop.berkeley.edu/">Structural Classification of Proteins</a>. On initialization, reads the file
  * {@code src/main/resources/databases.properties} and creates a new ScopDatabase.
  * 
  * @author dmyersturnbull
  * 
  */
 public class BasicScop {
 
 	private static final Logger logger = LogManager.getLogger("org.structnetalign");
 
 	static {
 		Properties props = new Properties();
 		ClassLoader loader = Thread.currentThread().getContextClassLoader();
 		InputStream stream = loader.getResourceAsStream("databases.properties");
 		try {
 			props.load(stream);
 		} catch (IOException e) {
 			throw new RuntimeException("Couldn't open databases property file", e);
 		}
 		String scopVersion = props.getProperty("scop_version");
 		logger.info("Setting SCOP to " + scopVersion);
 		ScopFactory.setScopDatabase(scopVersion);
 	}
 
 	public static ScopDatabase getScop() {
		return ScopFactory.getSCOP();
 	}
 
 }
