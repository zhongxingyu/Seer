 /*
  * Copyright 2011 Future Systems
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.araqne.logdb;
 
 import java.net.Inet4Address;
 import java.util.Comparator;
 import java.util.Date;
 
 import org.araqne.logdb.query.command.NumberUtil;
 
 public class ObjectComparator implements Comparator<Object> {
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	@Override
 	public int compare(Object o1, Object o2) {
 		if (o1 == null && o2 == null)
 			return 0;
 		else if (o1 == null && o2 != null)
 			return -1;
		else if (o1 != null && o2 == null)
			return 1;
 
 		if (o1.equals(o2))
 			return 0;
 
 		if (o1.getClass() == o2.getClass()) {
 			if (o1 instanceof String) {
 				return ((String) o1).compareToIgnoreCase((String) o2);
 			} else if (o1 instanceof Comparable) {
 				return ((Comparable) o1).compareTo(o2);
 			} else if (o1 instanceof Inet4Address) {
 				byte[] ip1 = ((Inet4Address) o1).getAddress();
 				byte[] ip2 = ((Inet4Address) o2).getAddress();
 				for (int i = 0; i < 4; i++) {
 					int c = ip1[i] - ip2[i];
 					if (c != 0)
 						return c;
 				}
 				return 0;
 			} else if (o1 instanceof Object[] && o2 instanceof Object[]) {
 				Object[] arr1 = (Object[]) o1;
 				Object[] arr2 = (Object[]) o2;
 				int min = Math.min(arr1.length, arr2.length);
 				for (int i = 0; i < min; i++) {
 					int cmp = compare(arr1[i], arr2[i]);
 					if (cmp != 0)
 						return cmp;
 				}
 
 				return arr1.length - arr2.length;
 			}
 		} else {
 			if (o1 instanceof Number && o2 instanceof Number) {
 				if (!NumberUtil.isFloat(o1) && !NumberUtil.isFloat(o2)) {
 					long lhs = 0;
 					if (o1 instanceof Long)
 						lhs = (Long) o1;
 					else if (o1 instanceof Integer)
 						lhs = (Integer) o1;
 					else if (o1 instanceof Short)
 						lhs = (Short) o1;
 
 					long rhs = 0;
 					if (o2 instanceof Long)
 						rhs = (Long) o2;
 					else if (o2 instanceof Integer)
 						rhs = (Integer) o2;
 					else if (o2 instanceof Short)
 						rhs = (Short) o2;
 
 					return (int) (lhs - rhs);
 				} else {
 					double lhs = 0;
 					if (o1 instanceof Double)
 						lhs = (Double) o1;
 					else if (o1 instanceof Integer)
 						lhs = (Integer) o1;
 					else if (o1 instanceof Long)
 						lhs = (Long) o1;
 					else if (o1 instanceof Short)
 						lhs = (Short) o1;
 					else if (o1 instanceof Float)
 						lhs = (Float) o1;
 
 					double rhs = 0;
 					if (o2 instanceof Double)
 						rhs = (Double) o2;
 					else if (o2 instanceof Integer)
 						rhs = (Integer) o2;
 					else if (o2 instanceof Long)
 						rhs = (Long) o2;
 					else if (o2 instanceof Short)
 						rhs = (Short) o2;
 					else if (o2 instanceof Float)
 						rhs = (Float) o2;
 
 					return Double.compare(lhs, rhs);
 				}
 			} else if (o1 instanceof Date && o2 instanceof Date) {
 				long lhs = ((Date) o1).getTime();
 				long rhs = ((Date) o2).getTime();
				return (int) (lhs - rhs);
 			}
 		}
 
 		// undefined behavior (cannot compare)
 		return -1;
 	}
 }
