 package org.eclipse.dltk.rhino.dbgp;
 
 import java.util.ArrayList;
 import java.util.Observer;
 import java.util.WeakHashMap;
 
 import org.mozilla.javascript.Context;
 
 public class DBGPStackManager {
 
 	protected static WeakHashMap map = new WeakHashMap();
 
 	private ArrayList stack = new ArrayList();
 
 	private static boolean breakpointsThreadLocal;
 
 	private boolean needSuspend;
 
 	private DBGPDebugger observer;
 
 	private BreakPointManager manager = null;
 
 	private static BreakPointManager gmanager = null;
 
 	private boolean suspendOnExit;
 
 	private boolean suspendOnEntry;
 
 	private boolean suspenOnChangeLine;
 
 	public BreakPointManager getManager() {
 		return manager;
 	}
 
 	private DBGPStackManager() {
 		if (isBreakpointsThreadLocal()) {
 			manager = new BreakPointManager();
 		} else {
 			synchronized (DBGPStackManager.class) {
 				if (gmanager == null)
 					gmanager = new BreakPointManager();
 			}
 			manager = gmanager;
 		}
 	}
 
 	public static DBGPStackManager getManager(Context cx) {
 		DBGPStackManager object = (DBGPStackManager) map.get(cx);
 		if (object != null)
 			return object;
 		object = new DBGPStackManager();
 		map.put(cx, object);
 		return object;
 	}
 
 	public static void removeManager(Context cx) {
 		map.remove(cx);
 	}
 
 	public void enter(DBGPDebugFrame debugFrame) {
 		stack.add(debugFrame);
 		if (suspendOnEntry) {
 			if (debugFrame.getWhere().equals("module")) {
 				observer.update(null, this);
 				synchronized (this) {
 					try {
 						this.wait();
 					} catch (InterruptedException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 			} else
 				suspenOnChangeLine = true;
 		}
 	}
 
 	public void exit(DBGPDebugFrame debugFrame) {
 		if (needSuspend || suspendOnExit) {
 
 			observer.update(null, this);
 			synchronized (this) {
 				try {
 					this.wait();
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		}
 		String sn = debugFrame.getWhere();
 
 		if (sn != null) {
 			BreakPoint hit = manager.hitExit(sn);
 			if (hit != null)
 				checkBreakpoint(debugFrame, hit);
 		}
 		stack.remove(debugFrame);
 
 	}
 
 	public void changeLine(DBGPDebugFrame frame, int lineNumber) {
 		if (suspenOnChangeLine) {
 			suspenOnChangeLine = false;
 			observer.update(null, this);
 			synchronized (this) {
 				try {
 					this.wait();
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		}
 		if (frame.isSuspend()) {
 			needSuspend = true;
 		}
 		BreakPoint hit = manager.hit(frame.getSourceName(), lineNumber);
 		checkBreakpoint(frame, hit);
 	}
 
 	private void checkBreakpoint(DBGPDebugFrame frame, BreakPoint hit) {
 
 		if (hit != null) {
 			if (hit.isEnabled()) {
 				if (hit.expression != null) {
 					Object eval = frame.eval(hit.expression);
 					if (eval != null) {
 						if (eval.equals(Boolean.TRUE)) {
 							needSuspend = true;
 						} else
 							needSuspend = false;
 					} else
 						needSuspend = false;
 				} else
 					needSuspend = true;
 				// observer.update(null, hit);
 			}
 		}
 		if (needSuspend) {
 			observer.update(null, this);
 			synchronized (this) {
 				try {
 					this.wait();
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	public void exceptionThrown(Throwable ex) {
 
 	}
 
 	public void suspend() {
 		needSuspend = true;
 	}
 
 	public int getStackDepth() {
 		return stack.size();
 	}
 
 	public DBGPDebugFrame getStackFrame(int parseInt) {
 		return (DBGPDebugFrame) stack.get(stack.size() - parseInt - 1);
 	}
 
 	public int getLineNumber(String level) {
 		return getStackFrame(0).getLineNumber();
 	}
 
 	public void registerBreakPoint(BreakPoint p) {
 		manager.addBreakPoint(p);
 	}
 
 	public void setDebugger(DBGPDebugger debugger) {
 		this.observer = debugger;
 	}
 
 	public synchronized void resume() {
 		this.needSuspend = false;
 		for (int a = 0; a < this.getStackDepth(); a++) {
 			this.getStackFrame(a).setSuspend(false);
 		}
 		this.notify();
 	}
 
 	public synchronized void stepOver() {
 		if (this.getStackDepth() > 1) {
 			getStackFrame(0).setSuspend(true);
 			getStackFrame(1).setSuspend(true);
 			this.needSuspend = false;
 		}
 		this.notify();
 	}
 
 	public synchronized void stepIn() {
 		this.needSuspend = true;
 		this.notify();
 	}
 
 	public synchronized void stepOut() {
 		getStackFrame(0).setSuspend(false);
 		this.needSuspend = false;
 		if (this.getStackDepth() > 1) {
 			getStackFrame(1).setSuspend(true);
 		}
 		this.notify();
 	}
 
 	public void removeBreakpoint(String id) {
 		this.manager.removeBreakPoint(id);
 	}
 
 	public void updateBreakpoint(String id, String newState, String newLine,
 			String hitValue, String hitCondition, String condExpr) {
 		this.manager.updateBreakpoint(id, newState, newLine, hitValue,
 				hitCondition, condExpr);
 	}
 
 	public Observer getObserver() {
 		return observer;
 	}
 
 	public BreakPoint getBreakpoint(String id) {
 		return this.manager.getBreakpoint(id);
 	}
 
 	public void setSuspendOnExit(boolean parseBoolean) {
 		this.suspendOnExit = parseBoolean;
 	}
 
 	public void setSuspendOnEntry(boolean parseBoolean) {
		this.suspendOnExit = parseBoolean;
 	}
 
 	public static boolean isBreakpointsThreadLocal() {
 		return breakpointsThreadLocal;
 	}
 
 	public static void setBreakpointsThreadLocal(boolean breakpointsThreadLocal) {
 		DBGPStackManager.breakpointsThreadLocal = breakpointsThreadLocal;
 	}
 
 }
