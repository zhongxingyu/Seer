 /*
  *  Straight - A system to manage financial demands for small and decentralized
  *  organizations.
  *  Copyright (C) 2011  Octahedron 
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package br.octahedron.commons.database;
 
 import java.util.logging.Logger;
 
 import com.google.appengine.api.NamespaceManager;
 
 /**
  * This entity is responsible performs the namespace management.
  * 
  * @author Erick Moreno
  */
 public class NamespaceCommons {
	
	private static final String GLOBAL_NAMESPACE = ""; 
 	private static final ThreadLocal<String> previousNamespaces = new ThreadLocal<String>();
	private static Logger logger = Logger.getLogger(NamespaceCommons.class.getName());
 
 
 	/**
 	 * Changes current namespace to the given one, storing the actual namespace to be restored
 	 * later.
 	 */
 	public static void changeToNamespace(String namespace) {
 		logger.fine("Changing namespace to namespace: " + namespace);
 		previousNamespaces.set((NamespaceManager.get() != null) ? NamespaceManager.get() : "");
 		NamespaceManager.set(namespace);
 	}
 
 	/**
 	 * Changes current namespace to global namespace, storing the actual namespace to be restored
 	 * later.
 	 */
 	public static void changeToGlobalNamespace() {
		changeToNamespace(GLOBAL_NAMESPACE);
 
 	}
 
 	/**
 	 * Changes the current namespace to the previous one namespace
 	 */
 	public static void changeToPreviousNamespace() {
 		String previous = previousNamespaces.get();
 		if (previous!= null) {
 			logger.fine("Changing namespace from global to original namespace: " + previous);
 			NamespaceManager.set(previous);
 		} else {
 			logger.fine("No previous namespace stored, keeping the actual one.");
 		}
 	}
 }
