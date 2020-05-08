 /*
  * Jabox Open Source Version
  * Copyright (C) 2009-2010 Dimitris Kapanidis                                                                                                                          
  * 
  * This file is part of Jabox
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see http://www.gnu.org/licenses/.
  */
 package org.jabox.environment;
 
 import java.io.File;
 
 public class Environment {
 
 	private static final String HUDSON_ENV = "HUDSON_HOME";
 	private static final String HUDSON_PROPERTY = "HUDSON_HOME";
 	private static final String HUDSON_DIR = ".hudson";
 
 	public static String getBaseDir() {
		return getHomeDir() + File.separatorChar + ".jabox"
				+ File.separatorChar;
 	}
 
 	public static File getBaseDirFile() {
 		return new File(getBaseDir());
 	}
 
 	public static String getHudsonHomeDir() {
 		String env = System.getenv(HUDSON_ENV);
 		String property = System.getProperty(HUDSON_PROPERTY);
 		if (env != null) {
 			return env;
 		} else if (property != null) {
 			return property;
 		} else {
 			return Environment.getBaseDir() + HUDSON_DIR;
 		}
 	}
 
 	public static File getTmpDirFile() {
		File tmpDir = new File(getBaseDir(), "tmp");
 
 		if (!tmpDir.exists()) {
 			tmpDir.mkdirs();
 		}
 
 		return tmpDir;
 	}
 
 	protected static String getHomeDir() {
		return System.getProperty("user.home");
 	}
 
 	public static void configureEnvironmentVariables() {
 		configBaseDir(HUDSON_ENV, HUDSON_PROPERTY, HUDSON_DIR);
 		configBaseDir("ARTIFACTORY_HOME", "artifactory.home", ".artifactory/");
 		configBaseDir("NEXUS_HOME", "plexus.nexus-work", ".nexus/");
 	}
 
 	private static void configBaseDir(final String env, final String property,
 			final String subdir) {
 		if (System.getenv(env) == null && System.getProperty(property) == null) {
 			System.setProperty(property, Environment.getBaseDir() + subdir);
 		}
 	}
 }
