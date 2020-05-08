 /**
  * at.mkweb.android.simpleshoppinglist.Registry
  * 
  * LICENSE:
  *
  * This file is part of SimpleShoppingList, an Android app to create very simple shopping lists (http://android.mk-web.at/app/simpleshoppinglist.html).
  *
  * SimpleShoppingList is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
  * License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any
  * later version.
  *
 * SimpleShoppingList is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
  * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along with software.  If not, see
  * <http://www.gnu.org/licenses/>.
  *
  * @author Mario Klug <mario.klug@mk-web.at>
  * @package at.mkweb.android.simpleshoppinglist
  * 
  * @license http://www.gnu.org/licenses/gpl.html
  */
 
 package at.mkweb.android.simpleshoppinglist;
 
 import java.util.HashMap;
 
 public class Registry {
 
 	public static String DATABASE = "db";
 	
 	private static HashMap<String, Object> objects = new HashMap<String, Object>();
 	
 	public static boolean add(String key, Object o) {
 		
 		if(objects.containsKey(key) == false) {
 			
 			objects.put(key, o);
 			return true;
 		}
 		
 		return false;
 	}
 	
 	public static Object get(String key) {
 		
 		if(objects.containsKey(key)) {
 			
 			return objects.get(key);
 		}
 		
 		return null;
 	}
 	
 }
