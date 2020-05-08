 /*
  * Created on 2005/03/01 by hanki
  */
 package supersql.dataconstructor;
 
 import supersql.common.Log;
 import supersql.extendclass.ExtList;
 
 public class Aggregate {
 
 	/* navigate the whole schema in this method */
 	public ExtList aggregate(ExtList criteria_set, ExtList info, ExtList sch, ExtList tuples) {
 		
 		boolean is_aggregate = false;
 		
 		ExtList criteria_set_buffer = new ExtList();
 		
 		ExtList process_set = new ExtList();
 		ExtList deep_set = new ExtList();
 
 		Log.out(" * aggregate at the schema level " + sch + " *");
 		
 		/* current schema level */
 		for (int i = 0; i < sch.size(); i++) {
 			
 			/* attribute found in this current level */
 			if (!(sch.get(i) instanceof ExtList)) {
 				for (int j = 0; j < info.size(); j++) {
 					
 					/* "aggregate functions" found */
 					if (info.get(j).toString().substring(0, 1).equals(sch.get(i).toString())) {
 
 						Log.out("    aggregate found : " + sch.get(i) + " with " + info.get(j).toString().substring(2));			
 						
 						is_aggregate = true;
 						process_set.add(info.get(j));
 					
 					}
 				}
 				
 				/* push it into criteria_set, if "aggregates functions" not found */
 				if (!is_aggregate) {
 					criteria_set.add(sch.get(i));
 				}
 			
 			/* inner level found in this current level */
 			} else {
 				
 				deep_set.add(sch.get(i));
 				
 			}
 			
 		}
 
 		/* calculate "aggregate functions" in this current level, if there is any */
 		while (process_set.size() > 0) {
 			
 			tuples = calculate(criteria_set, process_set.get(0), tuples);
 
 			Log.out("    aggregate process : " + process_set.get(0).toString().substring(0, 1) + " with " + process_set.get(0).toString().substring(2));			
 			
 			criteria_set_buffer.add(process_set.get(0).toString().substring(0, 1));
 			process_set.remove(0);
 						
 		}
 		
 		/* update criteria_set */
 		for (int i = 0; i < criteria_set_buffer.size(); i++) {
 			criteria_set.add(criteria_set_buffer.get(i));
 		}
 		
 		Log.out("    set : " + criteria_set);
 		
 		/* calculate each inner level of this current level by recursion */
 		while (deep_set.size() > 0) {
 
 			Aggregate aggregate = new Aggregate();
 
 			tuples = aggregate.aggregate(criteria_set, info, (ExtList)(deep_set.get(0)), tuples);
 			
 			deep_set.remove(0);
 					
 		}
 		
 		return tuples;
 		
 	}
 
 	/* calculate in units of groups having the same contents in criteria_set */
 	private ExtList calculate(ExtList criteria, Object process, ExtList tuples) {
 
 		ExtList buffer = new ExtList();
 		ExtList tuples_buffer = new ExtList();
 		
 		ExtList x;
 		ExtList y;
 		boolean flag = true;
 				
 		String target;
 		String way;
 				
 		target = process.toString().substring(0, 1);
 		way = process.toString().substring(2);
 		
 		while (tuples.size() > 0) {
 			
 			/* find tuples with the same criteria */
 			x = (ExtList)(tuples.get(0));
 			
 			for (int i = 1; i < tuples.size(); i++) {
 				
 				y = (ExtList)(tuples.get(i));
 				
 				for (int k = 0; k < criteria.size(); k++) {
 					if (!(x.get(Integer.parseInt(criteria.get(k).toString())).equals
 					     (y.get(Integer.parseInt(criteria.get(k).toString()))))) {
 						flag = false;
 					}
 				}
 				
 				if (flag) {
 					buffer.add(y);
 					tuples.remove(i--);
 				} else {
 					flag = true;
 				}
 				
 			}
 			
 			buffer.add(x);
 			tuples.remove(0);
 
 			/* calculate "max" */
 			if (way.equals("max")) {
 				
 				/* obtain the maximum value */				
 				int max = Integer.parseInt(((ExtList)(buffer.get(0))).get(Integer.parseInt(target)).toString().substring(4));
 			
 				for (int i = 1; i < buffer.size(); i++) {
 					if (Integer.parseInt(((ExtList)(buffer.get(i))).get(Integer.parseInt(target)).toString().substring(4)) > max) {
 						max = Integer.parseInt(((ExtList)(buffer.get(i))).get(Integer.parseInt(target)).toString().substring(4));
 					}
 				}
 				
 				/* write the maximum value */
 				ExtList tmp1 = new ExtList();
 				
 				for (int i = 0; i < buffer.size(); i++) {
 					tmp1 = replace((ExtList)(buffer.get(i)), max, Integer.parseInt(target), "max");
 					buffer.set(i, tmp1.get(0));
 				}
 
 
 			/* calculate "min" */	
 			} else if (way.equals("min")) {
 				
 				/* obtain the minimum value */
 				int min = Integer.parseInt(((ExtList)(buffer.get(0))).get(Integer.parseInt(target)).toString().substring(4));
 				
 				for (int i = 1; i < buffer.size(); i++) {
 					if (Integer.parseInt(((ExtList)(buffer.get(i))).get(Integer.parseInt(target)).toString().substring(4)) < min) {
 						min = Integer.parseInt(((ExtList)(buffer.get(i))).get(Integer.parseInt(target)).toString().substring(4));
 					}
 				}
 					
 				/* write the minimum value */
 				ExtList tmp1 = new ExtList();
 				
 				for (int i = 0; i < buffer.size(); i++) {
 					tmp1 = replace((ExtList)(buffer.get(i)), min, Integer.parseInt(target), "min");
 					buffer.set(i, tmp1.get(0));
 				}
 				
 			/* calculate "sum" */
 			} else if (way.equals("sum")) {
 			
 				/* obtain the summation value */
 				int sum = 0;
 				
 				for (int i = 0; i < buffer.size(); i++) {
 					sum += Integer.parseInt(((ExtList)(buffer.get(i))).get(Integer.parseInt(target)).toString().substring(4));
 				}
 				
 				/* write the summation value */
 				ExtList tmp1 = new ExtList();
 				
 				for (int i = 0; i < buffer.size(); i++) {
 					tmp1 = replace((ExtList)(buffer.get(i)), sum, Integer.parseInt(target), "sum");
 					buffer.set(i, tmp1.get(0));
 				}
 				
 			/* calculate "avg" */
 			} else if (way.equals("avg")) {
 				
 				/* obtain the average value */
 				int sum = 0;
 				
 				for (int i = 0; i < buffer.size(); i++) {
					sum += Integer.parseInt(((ExtList)(buffer.get(i))).get(Integer.parseInt(target)).toString());
 				}
 				
 				float avg = sum / buffer.size();
 				
 				/* write the average value */
 				ExtList tmp1 = new ExtList();
 				
 				for (int i = 0; i < buffer.size(); i++) {
 					tmp1 = replace((ExtList)(buffer.get(i)), avg, Integer.parseInt(target), "avg");
 					buffer.set(i, tmp1.get(0));
 				}
 			
 			/* calculate "count" */
 			} else if (way.equals("count")) {
 
 				/* obtain the number of counts */
 				int count = buffer.size();
 				
 				/* write the count value */
 				ExtList tmp1 = new ExtList();
 				
 				for (int i = 0; i < buffer.size(); i++) {
 					tmp1 = replace((ExtList)(buffer.get(i)), count, Integer.parseInt(target), "count");
 					buffer.set(i, tmp1.get(0));
 				}
 				
 			}
 			
 //			/* "slideshow" */	//added by goto 20130122
 //			} else if (way.equals("slideshow")) {
 //				
 //				System.out.println("slideshow!!");
 //				
 //				for (int i = 0; i < buffer.size(); i++) {
 //					//sum += Integer.parseInt(((ExtList)(buffer.get(i))).get(Integer.parseInt(target)).toString().substring(4));
 //					System.out.println(((ExtList)(buffer.get(i))).get(Integer.parseInt(target)).toString().substring(4));
 //					buffer.set(i, ((ExtList)(buffer.get(i))).get(Integer.parseInt(target)).toString().substring(4));
 //					//HTMLFunction.class  html_env.code.append(
 //				}
 //				
 //
 //				/* obtain the number of counts */
 //				int count = buffer.size();
 //				
 //				/* write the count value */
 //				ExtList tmp1 = new ExtList();
 //				
 ////				for (int i = 0; i < buffer.size(); i++) {
 ////					tmp1 = replace((ExtList)(buffer.get(i)), count, Integer.parseInt(target), "count");
 ////					buffer.set(i, tmp1.get(0));
 ////				}
 ////				
 //			}
 
 			tuples_buffer.addAll(buffer);
 			buffer.clear();
 
 		}
 
 		return tuples_buffer;
 	
 	}
 
 	/* replace for "max", "min", "sum", "count" */
 	private ExtList replace(ExtList tuple, int value, int position, String way) {
 
 		ExtList target;
 		StringBuffer tmp = new StringBuffer();
 		ExtList result = new ExtList();
 		
 		if (way.equals("count")) {
 			//chie commentout
 			//tmp.append("cnt");
 		} else {
 			tmp.append(way);	
 		}
 		
 		tmp.append(" ");
 		tmp.append(value);
 		target = new ExtList();
 		target.add(tmp.toString());
 		
 		tuple.set(position, target);
 		
 		result.add(tuple);
 		
 		return result;
 	
 	}
 	
 	/* replace for "avg" */
 	private ExtList replace(ExtList tuple, float value, int position, String way) {
 
 		ExtList target;
 		StringBuffer tmp = new StringBuffer();
 		ExtList result = new ExtList();
 		
 		tmp.append(way); tmp.append(" "); tmp.append(value);
 		target = new ExtList();		
 		target.add(tmp.toString());
 		
 		tuple.set(position, target);
 		
 		result.add(tuple);
 		
 		return result;
 	
 	}
 
 }
