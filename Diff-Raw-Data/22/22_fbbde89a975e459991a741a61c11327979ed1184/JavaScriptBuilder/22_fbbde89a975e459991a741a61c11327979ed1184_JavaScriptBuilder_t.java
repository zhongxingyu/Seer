 package org.openrdf.repository.object.compiler.source;
 
 import static org.openrdf.repository.object.RDFObject.GET_CONNECTION;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.script.Invocable;
 import javax.script.ScriptEngine;
 import javax.script.ScriptEngineManager;
 import javax.script.ScriptException;
 
 import org.openrdf.model.URI;
 import org.openrdf.model.impl.URIImpl;
 import org.openrdf.model.vocabulary.OWL;
 import org.openrdf.repository.object.compiler.JavaNameResolver;
 import org.openrdf.repository.object.compiler.model.RDFClass;
 import org.openrdf.repository.object.compiler.model.RDFEntity;
 import org.openrdf.repository.object.compiler.model.RDFProperty;
 import org.openrdf.repository.object.exceptions.BehaviourException;
 import org.openrdf.repository.object.exceptions.ObjectStoreConfigException;
 import org.openrdf.repository.object.vocabulary.OBJ;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class JavaScriptBuilder extends JavaMessageBuilder {
 	private static final String BEHAVIOUR = BehaviourException.class.getName();
 	private static final String JAVA_NS = "java:";
 	private static final String INVOKE = "_$invokeFunction581e1f711f9dbeca9959cb418e5a044b";
 	private static final URI NOTHING = new URIImpl(OWL.NAMESPACE + "Nothing");
 	private static final Pattern KEYWORDS = Pattern.compile("\\.(break|case|catch|const|continue|default|delete|do|else|export|finally|for|function|if|in|instanceof|import|new|return|switch|this|throw|try|typeof|var|void|while|with)\b");
 	private static final Map<String, String> conversion = new HashMap<String, String>();
 	static {
 		conversion.put(Byte.class.getName(), "byteValue");
 		conversion.put(Double.class.getName(), "doubleValue");
 		conversion.put(Float.class.getName(), "floatValue");
 		conversion.put(Integer.class.getName(), "intValue");
 		conversion.put(Long.class.getName(), "longValue");
 		conversion.put(Short.class.getName(), "shortValue");
 	}
 	private Logger logger = LoggerFactory.getLogger(JavaScriptBuilder.class);
 
 	public JavaScriptBuilder(File source, JavaNameResolver resolver)
 			throws FileNotFoundException {
 		super(source, resolver);
 	}
 
 	public void engine(String simple, RDFClass method, String code,
 			Map<String, String> namespaces) throws ObjectStoreConfigException {
 		// load the script engine now, to import any binary libraries
 		new ScriptEngineManager().getEngineByName("ECMAScript");
 		String field = "scriptEngine";
 		String methodName = resolver.getMethodName(method.getURI());
 		String fileName = method.getURI().stringValue();
 		staticField(imports(ScriptEngine.class), field, "null");
 		staticField(imports(Exception.class), "exception", "null");
 		code("\tstatic {\n\t\t");
 		code("java.lang.ClassLoader previously = ");
 		code("java.lang.Thread.currentThread().getContextClassLoader();\n\t\t");
 		code("java.lang.Thread.currentThread().setContextClassLoader");
 		code("(").code(simple).code(".class.getClassLoader());\n\t\t");
 		code(field).code(" = new ").code(imports(ScriptEngineManager.class));
 		code("().getEngineByName(\"ECMAScript\");\n\t\t");
 		code("java.lang.Thread.currentThread().setContextClassLoader");
 		code("(previously);\n\t\t");
 		code("try {\n\t\t\t").code(field).code(".put(");
 		code(quote(ScriptEngine.FILENAME)).code(", ");
 		code(quote(fileName)).code(");\n\t\t\t");
 		code(field).code(".eval(");
 		code(quote(buildScriptFunction(method, methodName, code))).code(");");
 		code("\n\t\t} catch (");
 		code(imports(ScriptException.class)).code(" exc) {\n\t\t\t");
 		code("exception = exc;\n\t\t\t");
 		code(LoggerFactory.class.getName()).code(".getLogger(").code(simple);
 		code(".class).error(exc.getMessage());\n\t\t");
 		code("}\n\t");
 		code("}\n");
 	}
 
 	public JavaScriptBuilder script(RDFClass msg, RDFClass method, String code,
 			Map<String, String> namespaces) throws ObjectStoreConfigException {
 		String field = "scriptEngine";
 		String methodName = resolver.getMethodName(method.getURI());
 		RDFProperty response = msg.getResponseProperty();
 		boolean isVoid = NOTHING.equals(method.getRange(response).getURI());
 		String objectRange = getRangeObjectClassName(msg, response);
 		String range = getRangeClassName(msg, response);
 		boolean isPrimitive = !objectRange.equals(range) && msg.isFunctional(response);
 		StringBuilder out = new StringBuilder();
 		out.append("if (exception != null) throw exception;\n\t\t\t");
 		out.append("java.lang.Object result = ");
 		out.append("((").append(imports(Invocable.class)).append(")");
 		out.append(field).append(").invokeFunction(").append(quote(INVOKE));
 		out.append(", ").append(quote(methodName)).append(", msg);\n\t\t\t");
 		out.append("if (result instanceof ").append(BEHAVIOUR).append(") {\n\t\t\t\t");
 		out.append("if (((").append(BEHAVIOUR).append(") result).getCause()");
 		out.append(" instanceof java.lang.RuntimeException) {\n\t\t\t\t\t");
 		out.append("throw (java.lang.RuntimeException) ((").append(BEHAVIOUR);
 		out.append(") result).getCause();\n\t\t\t\t");
 		out.append("}\n\t\t\t\t");
 		out.append("throw (").append(BEHAVIOUR).append(") result;\n\t\t\t");
 		out.append("}\n\t\t\t");
 		if (!isVoid) {
 			out.append("return ");
 			if (isPrimitive && isNumber(objectRange)) {
 				out.append("((").append(imports(Number.class)).append(") ");
 			} else if (isPrimitive) {
 				out.append("((").append(imports(objectRange)).append(") ");
 			} else if (isNumber(range) && msg.isFunctional(response)) {
 				out.append("new ").append(imports(range)).append("(((");
 				out.append(imports(Number.class.getName())).append(")");
 			} else if (msg.isFunctional(response)) {
 				out.append("(").append(imports(range)).append(") ");
 			} else {
 				out.append("(").append(imports(Set.class)).append(") ");
 			}
 			out.append("result");
 			if (isNumber(range) && msg.isFunctional(response)) {
 				out.append(").").append(conversion.get(range)).append("())");
 			} else if (isPrimitive) {
 				out.append(").").append(range).append("Value()");
 			}
 			out.append(";");
 		}
 		message(msg, method, false, out.toString());
 		return this;
 	}
 
 	private String buildScriptFunction(RDFClass method,
 			String methodName, String code) throws ObjectStoreConfigException {
 		String iri = method.getURI().stringValue();
 		StringBuilder script = new StringBuilder();
 		Set<? extends RDFEntity> imports = method.getRDFClasses(OBJ.IMPORTS);
 		for (RDFEntity imp : imports) {
 			URI uri = imp.getURI();
 			boolean isJava = uri.getNamespace().equals(JAVA_NS);
 			if (isJava || imp.isA(OWL.CLASS)) {
 				String className = getClassName(uri);
 				String cn = className;
 				if (cn.lastIndexOf('.') > 0) {
 					cn = cn.substring(cn.lastIndexOf('.') + 1);
 				}
 				warnIfKeywordUsed(className);
 				script.append("importClass(Packages.").append(className).append(");");
 			}
 		}
 		warnIfKeywordUsed(code);
		script.append("function ").append(methodName).append("(msg) {try{");
 		importVariables(script, method);
 		script.append("with(this) { with(msg) {");
		script.append(code).append("\n} }\n\t");
		script.append("} catch (e if e instanceof java.lang.Throwable) {\n\t\t\t");
 		script.append("return new Packages.").append(BEHAVIOUR);
 		script.append("(e, ").append(quote(iri)).append(");\n\t\t");
		script.append("} catch (e if e.javaException instanceof java.lang.Throwable) {\n\t\t\t");
 		script.append("return new Packages.").append(BEHAVIOUR);
 		script.append("(e.javaException, ").append(quote(iri)).append(");\n\t\t");
		script.append("} }\n");
		script.append("function ").append(INVOKE).append("(funcName, msg) {\n\t");
		script.append("return this[funcName].call(msg.target, msg);\n\t");
		script.append("}\n");
 		return script.toString();
 	}
 
 	private void warnIfKeywordUsed(String className) {
 		Matcher m = KEYWORDS.matcher(className);
 		if (m.find()) {
 			logger.warn("{} is a ECMA script keyword", m.group(1));
 		}
 	}
 
 	private boolean isNumber(String objectRange) {
 		return conversion.containsKey(objectRange);
 	}
 
 	private String quote(String string) {
 		return "\""
 				+ string.replace("\\", "\\\\").replace("\"", "\\\"").replace(
 						"\n", "\\n") + "\"";
 	}
 
 	private void importVariables(StringBuilder out, RDFEntity method)
 			throws ObjectStoreConfigException {
 		Set<? extends RDFEntity> imports = method.getRDFClasses(OBJ.IMPORTS);
 		for (RDFEntity imp : imports) {
 			URI subj = imp.getURI();
 			if (!imp.getURI().getNamespace().equals(JAVA_NS)
 					&& !imp.isA(OWL.CLASS) && subj != null) {
 				String name = var(resolver.getSimpleName(subj));
 				out.append("var ").append(name);
 				out.append(" = msg.getTarget().");
 				out.append(GET_CONNECTION).append("().getObject(\"");
 				out.append(subj.stringValue()).append("\"); ");
 			}
 		}
 	}
 
 }
