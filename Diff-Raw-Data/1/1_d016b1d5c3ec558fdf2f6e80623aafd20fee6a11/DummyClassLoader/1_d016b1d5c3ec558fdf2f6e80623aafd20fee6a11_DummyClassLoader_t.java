 /*******************************************************************************
  * Copyright (C) 2011 by Harry Blauberg
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  * 
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package org.jaml.patches;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.Enumeration;
 
 /**
  * The class DummyClassLoader can not load any class given and also does not use
  * the default Java class loader.
  */
 public class DummyClassLoader extends ClassLoader {
 
 	@Override
 	protected Class<?> findClass(String name) throws ClassNotFoundException {
 		return null;
 	}
 
 	@Override
 	protected String findLibrary(String libname) {
 		return null;
 	}
 
 	@Override
 	protected URL findResource(String name) {
 		return null;
 	}
 
 	@Override
 	protected Enumeration<URL> findResources(String name) throws IOException {
 		return null;
 	}
 
 	protected Object getClassLoadingLock(String className) {
 		return null;
 	}
 
 	@Override
 	protected Package getPackage(String name) {
 		return null;
 	}
 
 	@Override
 	protected Package[] getPackages() {
 		return null;
 	}
 
 	@Override
 	public URL getResource(String name) {
 		return null;
 	}
 
 	@Override
 	public InputStream getResourceAsStream(String name) {
 		return null;
 	}
 
 	@Override
 	public Enumeration<URL> getResources(String name) throws IOException {
 		return null;
 	}
 
 	@Override
 	protected Class<?> loadClass(String name, boolean resolve)
 			throws ClassNotFoundException {
 		return null;
 	}
 
 	@Override
 	public Class<?> loadClass(String name) throws ClassNotFoundException {
 		return null;
 	}
 }
