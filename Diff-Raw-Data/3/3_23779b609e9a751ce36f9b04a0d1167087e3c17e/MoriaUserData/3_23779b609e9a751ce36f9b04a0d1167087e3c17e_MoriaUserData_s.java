 /**
  * Copyright (C) 2003 FEIDE
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 
 package no.feide.mellon;
 
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Vector;
 import java.util.Iterator;
 
 
import no.feide.login.moria.Authentication.Attribute;
 
 /**
  * @author Lars Preben S. Arnesen
  *
  * This class provides an API for accessing the user data retrieved from Moria.
  * Objects of this class can be used for converting the data or for temporary
  * storage in memory.
  */
 public class MoriaUserData {
 	
 	/** The user data. */
 	private HashMap userData = null;
 	
 	/** 
 	 * Constructor. Converts a set of user data, delivered from Moria, to an
 	 * internal data structure.
 	 * @param moriaUserData
 	 */
 	public MoriaUserData(Attribute[] moriaUserData) {
 		
 		if (moriaUserData == null)
 			return;
 		
 		HashMap userData = new HashMap(moriaUserData.length);
 		for (int i=0; i<moriaUserData.length; i++) {
 			String[] oldVals = moriaUserData[i].getValues();
 			Vector newVals = new Vector(oldVals.length);
 			for (int j=0; j<oldVals.length; j++)
 				newVals.add(oldVals[j]);
 			userData.put(moriaUserData[i].getName(), newVals);
 		}
 		this.userData = userData;
 	}
 	
 	
 	/**
 	 * Returns true if the user has been authenticated. If no data has been
 	 * retreieved from Moria,
 	 * @return true/false
 	 */
 	public boolean isAuthenticated() {
 		return userData != null;
 	}
 	
 	
 	/**
 	 * Return a single value for a given attribute. The method asumes that the
 	 * attribute has a single value. If it is a multi value attribute, only the
 	 * first element is returned.
 	 * @param attributeName Name of the attribute that contains the value.
 	 * @return 
 	 */
 	public Object getSingleValueAttribute(String attributeName) {
 		Vector values = (Vector) userData.get(attributeName);
 		if (values == null)
 			return null;
 		
 		return values.elementAt(0);
 	}
 	
 	
 	/**
 	 * Return a multivalue attribute. Single value attributes are returned as
 	 * a Vector with only one element. Multi value attribtues are returned as
 	 * a Vector with all attributes in it.
 	 * @param attributeName
 	 * @return The Vector with all values for a attribtue.
 	 */
 	public Vector getMultiValueAttribute(String attributeName) {
 		return (Vector) userData.get(attributeName);
 	}
 
 	
 	/**
 	 * Return true if a given attribute contains a given value. Since the values
 	 * can be of different data types, a string comparison is done by calling
 	 * the object's toString method.
 	 * @param attributeName
 	 * @param value
 	 * @return
 	 */
 	public boolean attributeContainsValue(String attributeName, String value) {
 		Vector values = (Vector) userData.get(attributeName);
 		if (values == null)
 			return false;
 		
 		/* Loop through all values for an attribute. */
 		for (Enumeration e = values.elements(); e.hasMoreElements(); ) {
 			if (e.nextElement().toString().equalsIgnoreCase(value))
 				return true;
 		}
 		return false;
 	}
 	
 	
 	/**
 	 * Print out all user data to console. Only ment for debugging.
 	 */
 	public void debugPrintUserData() {
 		if (!isAuthenticated())
 			System.err.println("There user has not been authenticated.");
 		
 		else {
 			System.out.println("The user has been authenticated.");
 		
 			/* Loop though all attribute names */
 			for (Iterator it = userData.keySet().iterator(); it.hasNext(); ) {
 				String key = (String) it.next();
 				System.out.println("");
 				System.out.print(key+": ");
 				Vector attributes = (Vector) userData.get(key);
 				
 				/* For each attribute, loop through multi values */
 				for (Enumeration e = attributes.elements(); e.hasMoreElements(); ) {
 					Object attrValue = e.nextElement();
 					System.out.print(attrValue.toString());
 					if(e.hasMoreElements())
 						System.out.print("; ");
 				}
 				System.out.println("");
 			}
 		}
 	}
 	
 }
