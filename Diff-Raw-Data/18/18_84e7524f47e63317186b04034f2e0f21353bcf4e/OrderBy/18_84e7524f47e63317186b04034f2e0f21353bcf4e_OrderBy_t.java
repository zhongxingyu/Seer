 package supersql.dataconstructor;
 
 import supersql.common.Log;
 import supersql.extendclass.ExtList;
 import supersql.parser.Preprocessor;
 
 public class OrderBy {
 	
 	private ExtList set = new ExtList();
 
 	public ExtList sort(String info, ExtList sch, ExtList<ExtList<String>> data_info) {
 		
 		int a, key;
 		
 		String target;
 		String way;
 		
 		ExtList buffer;
 		String x;
 		String y;
 		
 		a = info.toString().indexOf(" ");
 		target = info.toString().substring(0, a);
 		way = info.toString().substring(a+1);
 			
 		/* find the key upon which to sort 'data_info' */
 		key = findKey(target, sch);
 
 		Log.out("    target : " + info);
 
 		/* bubble sort */
 		for (int i = 0; i < data_info.size(); i++) {
 			
 				x = data_info.get(i).get(key);	
 			
 			for (int j = i + 1; j < data_info.size(); j++) {
 			
 				y = data_info.get(j).get(key);
 				
 				/* ascending order */
 				if (way.equalsIgnoreCase("asc")) {
 					/* attribute whose value is null */
 					if (x.length() == 0 || y.length() == 0) {
 						if (x.toString().length() == 0 && y.toString().length() != 0) {
 							if (needToSort(sch, set, 
 									data_info.get(i), 
 									data_info.get(j))) {
 	
 								buffer = (ExtList)(data_info.get(i));
 								data_info.set(i, (data_info.get(j)));
 								data_info.set(j, buffer);
 		
 								x = data_info.get(i).get(key);
 							}
 						}
 					
 					/* "aggregate functions" attribute */		
 					} else if (isAggregate(target)) {
						if (Float.parseFloat(x.toString()) > Float.parseFloat(y.toString())) {
 							if (needToSort(sch, set, 
 											(ExtList)(data_info.get(i)), 
 											(ExtList)(data_info.get(j)))) {
 			
 								buffer = (ExtList)(data_info.get(i));
 								data_info.set(i, (data_info.get(j)));
 								data_info.set(j, buffer);
 				
 								x = data_info.get(i).get(key);
 							}
 						}
 					
 					/* normal attribute which is numeric */
 					} else if (isNumeric(x) && isNumeric(y)) {
						if (Float.parseFloat(x.toString()) > Float.parseFloat(y.toString())) {
 							if (needToSort(sch, set, 
 											(ExtList)(data_info.get(i)), 
 											(ExtList)(data_info.get(j)))) {
 												buffer = (ExtList)(data_info.get(i));
 								data_info.set(i, (data_info.get(j)));
 								data_info.set(j, buffer);
 				
 								x = data_info.get(i).get(key);
 							}
 						}
 						
 					/* normal attribute which is string */
 					} else if (x.toString().compareTo(y.toString()) > 0) {
 						if (needToSort(sch, set, 
 									(ExtList)(data_info.get(i)), 
 									(ExtList)(data_info.get(j)))) {
 				
 							buffer = (ExtList)(data_info.get(i));
 							data_info.set(i, (data_info.get(j)));
 							data_info.set(j, buffer);
 						
 							x = data_info.get(i).get(key);
 						
 						}
 					}
 			
 				/* descending order */
 				} else if (way.equalsIgnoreCase("desc")) {
 				
 					/* attribute whose value is null */
					if (x.toString().length() == 0 || y.toString().length() == 0) {
						if (x.toString().length() != 0 && y.toString().length() == 0) {
 							if (needToSort(sch, set, 
 									(ExtList)(data_info.get(i)), 
 									(ExtList)(data_info.get(j)))) {
 	
 								buffer = (ExtList)(data_info.get(i));
 								data_info.set(i, (data_info.get(j)));
 								data_info.set(j, buffer);
 		
 								x = data_info.get(i).get(key);
 							}
 						}
 						
 					/* "aggregate functions" attribute */		
 					} else if (isAggregate(target)) {
						if (Float.parseFloat(x.toString()) < Float.parseFloat(y.toString())) {
 							if (needToSort(sch, set, 
 											(ExtList)(data_info.get(i)), 
 											(ExtList)(data_info.get(j)))) {
 		
 								buffer = (ExtList)(data_info.get(i));
 								data_info.set(i, (data_info.get(j)));
 								data_info.set(j, buffer);
 				
 								x = data_info.get(i).get(key);
 							}
 						}
 	
 					/* normal attribute which is numeric */
					} else if (isNumeric(x.toString()) && isNumeric(y.toString())) {
						if (Float.parseFloat(x.toString()) < Float.parseFloat(y.toString())) {
 							if (needToSort(sch, set, 
 											(ExtList)(data_info.get(i)), 
 											(ExtList)(data_info.get(j)))) {
 												buffer = (ExtList)(data_info.get(i));
 								data_info.set(i, (data_info.get(j)));
 								data_info.set(j, buffer);
 				
 								x = data_info.get(i).get(key);
 							}
 						}
 						
 					/* normal attribute which is string */
 					} else if (x.toString().compareTo(y.toString()) < 0) {
 						if (needToSort(sch, set, 
 									(ExtList)(data_info.get(i)), 
 									(ExtList)(data_info.get(j)))) {
 				
 							buffer = (ExtList)(data_info.get(i));
 							data_info.set(i, (data_info.get(j)));
 							data_info.set(j, buffer);
 						
 							x = data_info.get(i).get(key);
 						
 						}
 					}
 
 				}
 				
 			}
 				
 		}
 		set.add(target);
 		return data_info;
 	}
 
 	/* find the key upon which to sort */
 	private int findKey(String target, ExtList sch) {
 		
 		int key = 0;
 		
 		for (int i = 0; i < sch.size(); i++) {
 			if ((sch.get(i).toString()).equals(target)) {
 				key = i;
 				break;
 			}
 		}
 		return key;
 	}
 	
 	/* check out whether to sort it or not */
 	private boolean needToSort(ExtList sch, ExtList set, ExtList x, ExtList y) {
 		
 		int key = 0;
 		
 		for (int i = 0; i < set.size(); i++) {
 			
 			for (int j = 0; j < sch.size(); j++) {
 				if ((sch.get(j).toString()).equals(set.get(i))) {
 					key = j;
 					break;
 				}
 			}
 			
 			if (!x.get(key).toString().equals(y.get(key).toString())) {
 				return false;
 			}
 			
 		}
 		return true;
 		
 	}
 	
 	/* list "order by" in a proper order */
 	public static ExtList tableToList(ExtList table) {
 		boolean done = false;
 		int j, index_of_bracket;
 		ExtList info = new ExtList();
 		
 		j = 1;
 		while (table.size() != 0) {
 			for (int i = 0; i < table.size(); i++) {
 				if (table.get(i).toString().toLowerCase().startsWith("asc")) {
 					index_of_bracket = table.get(i).toString().indexOf("[");
 					if (Integer.parseInt(table.get(i).toString().substring(3, index_of_bracket)) == j) {
 						info.add(table.get(i).toString().substring(index_of_bracket+1, table.get(i).toString().length()-1) + " asc");
 						done = true;
 					}
 				} 
 				if (table.get(i).toString().toLowerCase().startsWith("desc")) {
 					index_of_bracket = table.get(i).toString().indexOf("[");
 					if (Integer.parseInt(table.get(i).toString().substring(4, index_of_bracket)) == j) {
 						info.add(table.get(i).toString().substring(index_of_bracket+1, table.get(i).toString().length()-1) + " desc");
 						done = true;
 					}
 				}
 				if (done) {
 					table.remove(i--);
 					done = false;
 				}
 			}
 			j++;
 		}
 
 		return info;
 	
 	}
 	
 	private boolean isAggregate(String target) {
 		
 		for (int i = 0; i < Preprocessor.getAggregateList().size(); i++) {
 			if (target.equals(Preprocessor.getAggregateList().get(i).toString().substring(0, 1))) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private boolean isNumeric(String target) {
 		
 		for (int i = 0; i < target.length(); i++) {
 			if (!((target.charAt(i) >= '0' && target.charAt(i) <= '9') || target.charAt(i) == '.')) {
 				return false;
 			}
 		}
 		return true;
 	}
 	
 }
