 /**
  * This file is part of Horaz.
  *
  * Horaz is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Horaz is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Horaz.  If not, see <http://www.gnu.org/licenses/>.
  *
  * Copyright Joe's App Factory UG (haftungsbeschränkt)
  */
 
 package com.horaz.client.model;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * abstracts a filter pattern, that can be used for filter {@link DataStore}s.
  *
  * {@code
  * 	ds.setFilter(new Filter().whereEquals("id", 5).whereNotEquals("name", "bus"));
  * }
  *
  * @see http://www.dev-horaz.com/dev-guide/datastore
  */
 public class Filter {
 	private final Map<String, Object> whereEquals = new HashMap<String, Object>();
 	private final Map<String, Object> whereNotEquals = new HashMap<String, Object>();
 
 	public String getSQLStatement() {
 		StringBuffer sql = new StringBuffer();
 		for (Map.Entry<String, Object> entry : whereEquals.entrySet()) {
 			sql.append("AND " + entry.getKey()+"=? ");
 		}
 
 		// not where
 		for (Map.Entry<String, Object> entry : whereNotEquals.entrySet()) {
 			sql.append("AND " + entry.getKey()+"!=? ");
 		}
 
 		return sql.substring(4);
 	}
 
 	public Object[] getValues() {
 		List<Object> values = new ArrayList<Object>();
 		for (Map.Entry<String, Object> entry : whereEquals.entrySet()) {
 			values.add(entry.getValue());
 		}
 		for (Map.Entry<String, Object> entry : whereNotEquals.entrySet()) {
 			values.add(entry.getValue());
 		}
 
 		return values.toArray(new Object[values.size()]);
 	}
 
 	public boolean match(BaseModel mdl) {
 		// whereEquals
 		for (Map.Entry<String, Object> entry : whereEquals.entrySet()) {
 			if (!mdl.getField(entry.getKey()).equals(entry.getValue())) {
 				return false;
 			}
 		}
 
 		// whereNotEquals
 		for (Map.Entry<String, Object> entry : whereNotEquals.entrySet()) {
 			if (mdl.getField(entry.getKey()).equals(entry.getValue())) {
 				return false;
 			}
 		}
 
 		return true;
 	}
 
 	/**
 	 * adds a where-equals clause ("field == value")
 	 * @param field
 	 * @param value
 	 * @return itself
 	 */
 	public Filter whereEquals(String field, Object value) {
 		whereEquals.put(field, value);
 		return this;
 	}
 
 	/**
 	 * adds a where-not-equals clause ("field != value")
 	 * @param field
 	 * @param value
 	 * @return itself
 	 */
 	public Filter whereNotEquals(String field, Object value) {
 		whereNotEquals.put(field, value);
 		return this;
 	}
 }
