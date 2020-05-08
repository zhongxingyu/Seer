 package org.eclipse.dltk.javascript.internal.launching;
 
 import com.xored.org.mozilla.javascript.BaseFunction;
 import com.xored.org.mozilla.javascript.Context;
 import com.xored.org.mozilla.javascript.Function;
 import com.xored.org.mozilla.javascript.NativeFunction;
 import com.xored.org.mozilla.javascript.Scriptable;
 import com.xored.org.mozilla.javascript.ScriptableObject;
 import com.xored.org.mozilla.javascript.SimpleIntrospector;
 import com.xored.org.mozilla.javascript.Undefined;
 import com.xored.org.mozilla.javascript.UniqueTag;
 
 public class BuiltinsResolver {
 
 	static String init = init();
 	private static Context enter;
 	private static ScriptableObject initStandardObjects;
 
 	public static String init() {
 		StringBuffer buf = new StringBuffer();
 		enter = Context.enter();
 		initStandardObjects = enter.initStandardObjects();
 		Object[] allIds = initStandardObjects.getAllIds();
 
 		for (int a = 0; a < allIds.length; a++) {
 			String string = allIds[a].toString();
 			try {
 				Object object = initStandardObjects.get(string,
 						initStandardObjects);
 				if (string.equals("XML")) {
 					System.out.println(string);
 				}
 				appendObject(buf, string, object);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		return buf.toString();
 	}
 
 	private static void appendObject(StringBuffer buf, String string,
 			Object object) {
 		if (object instanceof Function) {
 			String[] args = new String[0];
 			Function c = (Function) object;
 			String name = "";
 			Object object2 = c.get("arguments", c);
 			if (c instanceof NativeFunction) {
 				NativeFunction function = (NativeFunction) c;
 				args = SimpleIntrospector.getParameterNames(function);
 				function.getFunctionName();
 			}
 			if (c instanceof BaseFunction) {
 				name = ((BaseFunction) c).getFunctionName();
 			}
 			appendFunction(buf, name, args, c);
 		} else {
 			if (object instanceof UniqueTag)
 				return;
 			if (object instanceof Undefined)
 				return;
 			if (object instanceof ScriptableObject)
 			{
 			appendObject(buf, string, (ScriptableObject) object);
 			}
 		}
 	}
 
 	public static void appendFunction(StringBuffer buf, String name,
 			String[] argNames, Function c) {
 		ScriptableObject sm = (ScriptableObject) c;
 		buf.append("function " + name + "(");
 
 		for (int a = 0; a < argNames.length; a++) {
 			buf.append(argNames[a]);
 			if (a != argNames.length - 1) {
 				buf.append(',');
 			}
 		}
 		buf.append(')');
 		buf.append("{");
 
 		try {
 			ScriptableObject construct = (ScriptableObject) c.construct(enter,
 					c, new Object[] {});

 			Scriptable classPrototype = ScriptableObject.getClassPrototype(c,
 					construct.getClassName());
 			Object[] elements = construct.getAllIds();
 			appendObjects(buf, construct, elements);
			Object[] members = ((ScriptableObject) c).getAllIds();
			appendObjects(buf, (ScriptableObject) c, members);
 			Object[] cpObjects = ScriptableObject.getPropertyIds(construct);
 			appendObjects(buf, (ScriptableObject) classPrototype, cpObjects);
 		} catch (Exception e) {
 		}
 		;
 		buf.append("/* Native built in function*/}\n");
 	}
 
 	private static void appendObjects(StringBuffer buf,
 			ScriptableObject construct, Object[] elements) {
 		for (int a = 0; a < elements.length; a++) {
 			String string = elements[a].toString();
 			Object object2 = construct.get(string, construct);
 			String val = "1;";
 			if (object2 instanceof Function) {
 				val = "function (){};";
 			}
 			buf.append("this." + string + "=");
 			buf.append(val);
 			buf.append("\n");
 		}
 	}
 
 	public static void appendObject(StringBuffer buf, String name,
 			ScriptableObject scriptableObject) {
 		buf.append("var " + name + "={");
 		ScriptableObject sc = (ScriptableObject) scriptableObject;
 
 		Object[] allIds2 = sc.getAllIds();
 		for (int a = 0; a < allIds2.length; a++) {
 			Object object = allIds2[a];
 			Object object2 = sc.get(allIds2[a].toString(), sc);
 			String value = "1";
 			if (object2 instanceof Function) {
 				value = "function(){}";
 			}
 			buf.append(object.toString() + ":" + value);
 			if (a != allIds2.length - 1)
 				buf.append(",");
 			buf.append('\n');
 		}
 		buf.append("};\n");
 	}
 
 	public static void main(String[] args) {
 		String init2 = init();
 		System.out.println(init2);
 	}
 }
