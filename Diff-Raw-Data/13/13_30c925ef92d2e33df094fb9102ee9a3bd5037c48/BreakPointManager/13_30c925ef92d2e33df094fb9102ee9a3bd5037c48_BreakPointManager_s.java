 package org.eclipse.dltk.rhino.dbgp;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 public class BreakPointManager {
 
 	private HashMap fileMap = new HashMap();
 	private HashMap ids = new HashMap();
 	private HashMap names = new HashMap();
 	private HashMap watchpoints = new HashMap();
 
 	public void removeBreakPoint(String id) {
 		BreakPoint object = (BreakPoint) ids.get(id);
 		if (object != null)
 			removeBreakPoint(object);
 	}
 
 	public final void addBreakPoint(BreakPoint point) {
 		if (point.isReturn) {
 			names.put(point.method, point);
 		}
 
 		if (point.isWatch) {
 			ArrayList object = (ArrayList) watchpoints.get(point.expression);
 			if (object == null) {
 				object = new ArrayList();
 				watchpoints.put(point.expression, object);
 			}
 			object.add(point);
 
 		}
 		HashMap object = (HashMap) fileMap.get(point.file);
 		if (object == null) {
 			object = new HashMap();
 			fileMap.put(point.file, object);
 		}
 		object.put(new Integer(point.line), point);
 		ids.put("p" + point.id, point);
 	}
 
 	public void removeBreakPoint(BreakPoint point) {
 		if (point.isReturn) {
 			names.remove(point.method);
 		}
 		if (point.isWatch) {
 
 			watchpoints.remove(point.expression);
 		}
 		HashMap object = (HashMap) fileMap.get(point.file);
 		if (object == null) {
 			return;
 		}
 		object.remove(new Integer(point.line));
 		ids.remove("p" + point.id);
 	}
 
 	public BreakPoint hit(String sourcePath, int lineNumber) {
 		HashMap q = (HashMap) fileMap.get(sourcePath);
 
 		if (q == null)
 			return null;
 		Integer lnNumber = new Integer(lineNumber);
 		BreakPoint point = (BreakPoint) q.get(lnNumber);
 
 		if (point == null)
 			return null;
 		point.currentHitCount++;
 		if (point.hitValue > 0) {
 
 			if (point.hitCondition == 1) {
 				if (point.hitValue >= point.currentHitCount)
 					return null;
 			}
 			if (point.hitCondition == 2) {
 				if (point.hitValue != point.currentHitCount)
 					return null;
 			}
 			if (point.hitCondition == 3) {
 				if (point.currentHitCount % point.hitValue != 0)
 					return null;
 			}
 		}
 		if (point.isTemporary)
 			q.remove(lnNumber);
 		return point;
 	}
 
 	public void updateBreakpoint(String id, String newState, String newLine,
 			String hitValue, String hitCondition, String condexpression) {
 		BreakPoint p = (BreakPoint) ids.get(id);
 		if (p != null) {
 			if (newState != null) {
 				newState = newState.trim();
 
 				if (newState.equals("enabled")) {
 
 					p.setEnabled(true);
 				} else if (newState.equals("disabled")) {
 
 					p.setEnabled(false);
 				}
 			}
 			if (newLine != null) {
 				HashMap map = (HashMap) fileMap.get(p.file);
 				Integer nl = new Integer(p.line);
 				BreakPoint po = (BreakPoint) map.get(nl);
 				if (po != p) {
 					System.out.println("Error");
 				} else {
 					map.remove(nl);
 					map.put(new Integer(Integer.parseInt(newLine)), p);
 				}
 			}
 			if (hitValue != null) {
 				p.hitValue = Integer.parseInt(hitValue);
 			}
 			if (hitCondition != null) {
 				p.setHitCondition(hitCondition);
 			}
 			if (!p.isWatch) {
 				p.expression = condexpression;
 			}
 
 		}
 	}
 
 	public BreakPoint hitExit(String sn) {
 
 		return (BreakPoint) names.get(sn);
 	}
 
 	public List getWatchPoints(String property) {
 		return (List) watchpoints.get(property);
 	}
 
 	public BreakPoint getBreakpoint(String id) {
 		return (BreakPoint) ids.get(id);
 	}
 
 }
