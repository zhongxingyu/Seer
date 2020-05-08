 package org.eclipse.dltk.rhino.dbgp;
 
 import java.io.DataInputStream;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.lang.reflect.Array;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.WeakHashMap;
 
 import org.mozilla.javascript.Context;
 import org.mozilla.javascript.Function;
 import org.mozilla.javascript.NativeJavaArray;
 import org.mozilla.javascript.NativeJavaClass;
 import org.mozilla.javascript.NativeJavaObject;
 import org.mozilla.javascript.Scriptable;
 import org.mozilla.javascript.ScriptableObject;
 import org.mozilla.javascript.Undefined;
 import org.mozilla.javascript.UniqueTag;
 import org.mozilla.javascript.debug.DebugFrame;
 import org.mozilla.javascript.debug.DebuggableScript;
 import org.mozilla.javascript.debug.Debugger;
 import org.mozilla.javascript.debug.IDeguggerWithWatchPoints;
 
 public class DBGPDebugger extends Thread implements Debugger, Observer,
 		IDeguggerWithWatchPoints {
 
 	private Socket socket;
 	private PrintStream out;
 	private HashMap strategies = new HashMap();
 	HashMap properties = new HashMap();
 	String runTransctionId;
 
 	static abstract class Command {
 		abstract void parseAndExecute(String command, HashMap options);
 	}
 
 	void printResponse(String response) {
 		String encodeString = response;
 		out.print(encodeString.length());
 		out.write(0);
 		out.print(encodeString);
 		out.write(0);
 	}
 
 	DBGPStackManager stackmanager;
 	public boolean isInited;
 
 	public void setContext(Context cx) {
 		DBGPStackManager manager = DBGPStackManager.getManager(cx);
 		manager.setDebugger(this);
 		this.stackmanager = manager;
 	}
 
 	public DBGPDebugger(Socket socket, String file, String string, Context ct)
 			throws IOException {
 		super();
 		this.socket = socket;
 
 		stackmanager = DBGPStackManager.getManager(ct);
 		stackmanager.suspend();
 		out = new PrintStream(socket.getOutputStream());
 		stackmanager.setDebugger(this);
 		String response = "<init appid=\"APPID\"\r\n" + "      idekey=\""
 				+ string + "\"\r\n" + "      session=\"" + string + "\"\r\n"
 				+ "      thread=\"THREAD_ID\"\r\n"
 				+ "      parent=\"PARENT_APPID\"\r\n"
 				+ "      language=\"javascript\"\r\n"
 				+ "      protocol_version=\"1.0\"\r\n"
 				+ "      fileuri=\"file://" + file + "\"\r\n" + "/>";
 		printResponse(response);
 		strategies.put("feature_get", new FeatureGetCommand(this));
 		strategies.put("feature_set", new FeatureSetCommand(this));
 		strategies.put("stdin", new StdInCommand(this));
 		strategies.put("stdout", new StdOutCommand(this));
 		strategies.put("stderr", new StdErrCommand(this));
 		strategies.put("run", new RunCommand(this));
 		strategies.put("context_names", new ContextNamesCommand(this));
 		strategies.put("stop", new StopCommand(this));
 		strategies.put("step_over", new StepOverCommand(this));
 		strategies.put("step_into", new StepIntoCommand(this));
 		strategies.put("step_out", new StepOutCommand(this));
 		strategies.put("breakpoint_get", new GetBreakPointCommand(this));
 		strategies.put("breakpoint_set", new SetBreakPointCommand(this));
 		strategies.put("breakpoint_remove", new RemoveBreakPointCommand(this));
 		strategies.put("breakpoint_update", new UpdateBreakPointCommand(this));
 		strategies.put("context_get", new ContextGetCommand(this));
 		strategies.put("property_set", new PropertySetCommand(this));
 		strategies.put("eval", new EvalCommand(this));
 		strategies.put("property_get", new PropertyGetCommand(this));
 		strategies.put("break", new BreakCommand(this));
 		strategies.put("stack_depth", new StackDepthCommand(this));
 		strategies.put("stack_get", new StackGetCommand(this));
 		out.flush();
 	}
 
 	protected void printProperty(String id, String fullName, Object value,
 			StringBuffer properties, int level, boolean addChilds) {
 		boolean hasChilds = false;
 
 		int numC = 0;
 		String vlEncoded;
 		String name_of_object_class = "";
 		String data_type = "Object";
 		if (value instanceof Function) {
 			data_type = "function";
 		} else if (value instanceof NativeJavaObject) {
 			data_type = "javaobject";
 		} else if (value instanceof NativeJavaClass) {
 			data_type = "javaclass";
 		} else if (value instanceof NativeJavaArray) {
 			data_type = "javaarray";
 		}
 		if (value instanceof Scriptable) {
 			hasChilds = true;
 			StringBuffer stringBuffer = new StringBuffer();
 			Scriptable p = (Scriptable) value;
 			value = stringBuffer;
 			Object[] ids = p.getIds();
 			numC = ids.length;
 			String nv = p.getClassName();
 			name_of_object_class = nv;
 			if (p instanceof NativeJavaObject) {
 
 				NativeJavaObject obj = (NativeJavaObject) p;
 
 				Object unwrap = obj.unwrap();
 				if (unwrap instanceof Class) {
 					nv = ((Class) unwrap).getName();
 				} else if (unwrap.getClass().isArray()) {
 					String string = unwrap.getClass().getName();
 					int len = Array.getLength(unwrap);
 					int q = string.indexOf('[');
 					if (q != -1)
 						string = string.substring(0, q);
 					int q1 = string.indexOf(']');
 					nv = string + "[" + len + "]";
 					if (q1 != -1)
 						nv += string.substring(q1);
 				} else {
 					if (unwrap instanceof String) {
 						nv = "JavaString " + '"' + unwrap.toString() + '"';
 					} else {
 						String string = unwrap.toString();
 
 						nv = unwrap.getClass().getName() + "(" + string + ")";
 					}
 				}
 
 			}
 
 			stringBuffer.append(Base64Helper.encodeString(nv));
 			if (addChilds) {
 				for (int a = 0; a < ids.length; a++) {
 					Object pvalue;
 					if (ids[a] instanceof Integer) {
 						pvalue = p.get(((Integer) ids[a]).intValue(), p);
 					} else
 						pvalue = p.get(ids[a].toString(), p);
 					printProperty(ids[a].toString(), fullName + "." + ids[a],
 							pvalue, stringBuffer, level + 1, false);
 				}
 			}
 			vlEncoded = stringBuffer.toString();
 		} else {
 			if (!(value instanceof Undefined)) {
 				if (value == UniqueTag.NOT_FOUND) {
 					vlEncoded = "";
 				} else
 					vlEncoded = Base64Helper.encodeString(value != null ? value
 							.toString() : "null");
 			} else {
 				vlEncoded = Base64Helper.encodeString("Undefined");
 			}
 			if (value != null)
 				name_of_object_class = value.getClass().getName();
 		}
 
 		properties.append("<property\r\n" + "    name=\"" + id + "\"\r\n"
 				+ "    fullname=\"" + fullName + "\"\r\n" + "    type=\""
 				+ data_type + "\"\r\n" + "    classname=\""
 				+ name_of_object_class + "\"\r\n" + "    constant=\"0\"\r\n"
 				+ "    children=\"" + (hasChilds ? 1 : 0) + "\"\r\n"
 				+ "    encoding=\"base64\"\r\n" + "    numchildren=\"" + numC
 				+ "\">\r\n" + vlEncoded + "</property>\r\n");
 	}
 
 	public void run() {
 		try {
 			DataInputStream ds = new DataInputStream(socket.getInputStream());
 			StringBuffer buf = new StringBuffer();
 			Context.enter();
 			while (ds.available() >= 0) {
 				int c = ds.read();
 				if (c < 0)
 					break;
 				if (c < 32) {
 					String s = buf.toString();
 
 					int indexOf = s.indexOf(' ');
 					if (indexOf != -1)
 
 					{
 						String commandId = buf.substring(0, indexOf);
 						Command object = (Command) strategies.get(commandId);
 						if (object == null) {
 							System.err.println(commandId);
 							continue;
 						}
 						HashMap options = new HashMap();
 
 						String result = buf.substring(indexOf + 1);
 						String[] split = result.split(" ");
 
 						try {
 							for (int a = 0; a < split.length; a++) {
 								options.put(split[a], split[++a]);
 							}
 						} catch (Exception e) {
 
 						}
 						object.parseAndExecute(result, options);
 					}
 					buf = new StringBuffer();
 				} else {
 					buf.append((char) c);
 				}
 
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	public DebugFrame getFrame(Context cx, DebuggableScript fnOrScript) {
 		return new DBGPDebugFrame(cx, fnOrScript);
 	}
 
 	public void handleCompilationDone(Context cx, DebuggableScript fnOrScript,
 			String source) {
 	}
 
 	public void update(Observable arg0, Object arg1) {
 		if (runTransctionId != null)
 			printResponse("<response command=\"run\"\r\n" + "status=\"break\""
 					+ " reason=\"ok\"" + " transaction_id=\"" + runTransctionId
 					+ "\">\r\n" + "</response>\r\n" + "");
 	}
 
 	public void notifyEnd() {
 		printResponse("<response command=\"run\"\r\n" + "status=\"stopped\""
 				+ " reason=\"ok\"" + " transaction_id=\"" + runTransctionId
 				+ "\">\r\n" + "</response>\r\n" + "");
 		System.exit(0);
 	}
 
 	public void access(String property, ScriptableObject object) {
 		ArrayList list = (ArrayList) stackmanager.getManager().getWatchPoints(
 				property);
 		if (list != null) {
 			int size = list.size();
 
 			for (int a = 0; a < size; a++) {
 				BreakPoint watchPoint = (BreakPoint) list.get(a);
 				if (watchPoint != null) {
 					if (watchPoint.enabled)
 						if (watchPoint.isAccess) {
 							String wkey = watchPoint.file + watchPoint.line;
 							String s = (String) cache.get(object);
 							if ((s != null) && (s.equals(wkey))) {
 								stackmanager.getObserver().update(null, this);
 								stackmanager.waitForNotify();
 							}
 						}
 				}
 			}
 		}
 	}
 
 	WeakHashMap cache = new WeakHashMap();
 
 	public void modification(String property, ScriptableObject object) {
 
 		ArrayList list = (ArrayList) stackmanager.getManager().getWatchPoints(
 				property);
		if (list != null) {
 			int size = list.size();
 			for (int a = 0; a < size; a++) {
 
 				BreakPoint watchPoint = (BreakPoint) list.get(a);
 				if (watchPoint != null) {
 					if (watchPoint.enabled) {
 						String sn = stackmanager.getStackFrame(0)
 								.getSourceName();
 						int ln = stackmanager.getStackFrame(0).getLineNumber();
 						String key = sn + ln;
 						String wkey = watchPoint.file + watchPoint.line;
 						if (key.equals(wkey)) {
 							cache.put(object, wkey);
 						}
 						if (watchPoint.isModification) {
 							Object object2 = cache.get(object);
 							if (object2 != null)
 								if (object2.equals(wkey)) {
 
 									stackmanager.getObserver().update(null,
 											this);
 									synchronized (this.stackmanager) {
 										try {
 											this.stackmanager.wait();
 										} catch (InterruptedException e) {
 											e.printStackTrace();
 										}
 									}
 								}
 						}
 					}
 				}
 			}
 		}
 	}
 
 	public void setProperty(String name, String value) {
 
 	}
 
 	public void setSuspendOnExit(boolean parseBoolean) {
 		stackmanager.setSuspendOnExit(parseBoolean);
 	}
 
 	public void setSuspendOnEntry(boolean parseBoolean) {
 		stackmanager.setSuspendOnEntry(parseBoolean);
 	}
 }
