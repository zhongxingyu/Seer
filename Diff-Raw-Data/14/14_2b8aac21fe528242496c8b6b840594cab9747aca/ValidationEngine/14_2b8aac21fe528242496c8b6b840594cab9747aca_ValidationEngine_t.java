 package com.google.codes.dryvalidator;
 
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 import net.arnx.jsonic.JSON;
 
 import org.apache.commons.io.IOUtils;
 import org.mozilla.javascript.Context;
 import org.mozilla.javascript.ContextFactory;
 import org.mozilla.javascript.NativeArray;
 import org.mozilla.javascript.NativeObject;
 import org.mozilla.javascript.Script;
 import org.mozilla.javascript.Scriptable;
 import org.mozilla.javascript.ScriptableObject;
 
 import com.google.codes.dryvalidator.dto.FormItem;
 
 public class ValidationEngine {
 	private static final String DIGEST_ALGORITHM = "SHA-1";
 
 	Context ctx;
 	ScriptableObject global;
 	Script getValidatorScript;
 	Script doValidateScript;
 
 	// コンパイル済みのJSをキャッシュしておく
 	static final ConcurrentHashMap<String, Script> scriptCache = new ConcurrentHashMap<String, Script>(
 			10);
 
 	public static class Console {
 		public void log(Object obj) {
 			System.out.println(Context.toString(obj));
 		}
 	}
 
 	public ValidationEngine() {
 	}
 
 	public ValidationEngine setup(String customScriptPath) {
 		ContextFactory contextFactory = ContextFactory.getGlobal();
 		ctx = contextFactory.enterContext();
 		global = ctx.initStandardObjects();
 		try {
 			loadScript("com/google/codes/dryvalidator/joose.js");
 			loadScript("com/google/codes/dryvalidator/dry-validator.js");
 			if (customScriptPath != null)
 				loadScript(customScriptPath);
 
 			ScriptableObject.putProperty(global, "console", Context.javaToJS(
 					new Console(), global));
 			executeScript("var executor = new DRYValidator.Executor();", global);
 		} catch (IOException e) {
 			throw new ResourceNotFoundException(e);
 		}
 		return this;
 	}
 	public ValidationEngine setup() {
 		return setup(null);
 	}
 
 	public Context getContext() {
 		return this.ctx;
 	}
 
 	public ScriptableObject getGlobalScope() {
 		return this.global;
 	}
 
 	public void register() {
 		Scriptable local = ctx.newObject(global);
 		local.setPrototype(global);
 		local.setParentScope(null);
 		Object wrappedValidation = Context.javaToJS("String", local);
 		ScriptableObject.putProperty(local, "formItem", wrappedValidation);
 		executeScript(
 				"var validation = {label: formItem.label};\n"
 						+ "Joose.A.each(formItem.validations.validation, function(v) { validation[v.name] = v.value; });\n"
 						+ "executor.addValidator(formItem.id, DRYValidator.CompositeValidator.make(validation));\n",
 				local);
 	}
 
 	public void register(FormItem formItem) {
 		Scriptable local = ctx.newObject(global);
 		local.setPrototype(global);
 		local.setParentScope(null);
 		Object wrappedValidation = Context.javaToJS(formItem, local);
 		ScriptableObject.putProperty(local, "formItem", wrappedValidation);
 		executeScript(
 				"var validation = {label: formItem.label};\n"
 						+ "Joose.A.each(formItem.validations.validation, function(v) { validation[v.name] = v.value; });\n"
 						+ "executor.addValidator(formItem.id, DRYValidator.CompositeValidator.make(validation));\n",
 				local);
 	}
 
 	public Map<String, List<String>> exec(Map<String, Object> formValues) {
 		Scriptable local = ctx.newObject(global);
 		local.setPrototype(global);
 		local.setParentScope(null);
 		NativeObject formValuesObj = new NativeObject();
 		for(Map.Entry<String, Object> entry : formValues.entrySet()) {
			Object value = null;
			if (entry.getValue() instanceof String || entry.getValue() instanceof Number || entry.getValue() instanceof Boolean ) {
				value = entry.getValue();
			} else {
				System.out.println(entry.getValue().getClass());
				value = ctx.evaluateString(local, JSON.encode(entry.getValue()), "<JSON>", 1, null);
			}
 			formValuesObj.defineProperty(
 					entry.getKey(),
 					value,
 					NativeObject.READONLY);
 		}
 		ScriptableObject.putProperty(local, "values", formValuesObj);
 		Object obj = executeScript("executor.execute(values)", local);
 
 		Map<String, List<String>> messages = new HashMap<String, List<String>>();
 		if (obj instanceof NativeObject) {
 			NativeObject nobj = NativeObject.class.cast(obj);
 			nobj.entrySet();
 			for (Map.Entry<Object, Object> e : nobj.entrySet()) {
 				if (e.getValue() instanceof NativeArray) {
 					NativeArray values = NativeArray.class.cast(e.getValue());
 					List<String> thisMessages = new ArrayList<String>();
 					for (int i = 0; i < values.getLength(); i++) {
 						thisMessages.add(Context.toString(values.get(i, local)));
 					}
 					messages.put(e.getKey().toString(), thisMessages);
 				}
 			}
 		}
 
 		return messages;
 	}
 
 	public List<String> exec(String id, Object value) {
 		Scriptable local = ctx.newObject(global);
 		local.setPrototype(global);
 		local.setParentScope(null);
 		ScriptableObject.putProperty(local, "id", Context.javaToJS(id, local));
 		ScriptableObject.putProperty(local, "value", Context.javaToJS(value,
 				local));
 
 		Object obj = executeScript("executor.validators[id].validate(value);", local);
 
 		List<String> messages = new ArrayList<String>();
 		if (obj instanceof NativeArray) {
 			NativeArray array = (NativeArray) obj;
 			for (int i = 0; i < array.getLength(); i++) {
 				String msg = Context.toString(array.get(i, local));
 				messages.add(msg);
 			}
 		}
 		return messages;
 	}
 
 	private Object executeScript(String scriptText, Scriptable scope) {
 		String cacheKey = digest(scriptText);
 		Script script = scriptCache.get(cacheKey);
 		if (script == null) {
 			script = ctx.compileString(scriptText, "<cmd>", 0, null);
 			scriptCache.putIfAbsent(cacheKey, script);
 		}
 		return script.exec(ctx, scope);
 	}
 
 	public void loadScript(String path) throws IOException {
 		Script script = scriptCache.get(path);
 		if (script == null) {
 			InputStreamReader in = null;
 
 			try {
 				in = new InputStreamReader(Thread.currentThread()
 						.getContextClassLoader().getResourceAsStream(path),
 						"UTF-8");
 				script = ctx.compileReader(in, path, 0, null);
 				scriptCache.putIfAbsent(path, script);
 			} finally {
 				IOUtils.closeQuietly(in);
 			}
 		}
 		script.exec(ctx, global);
 	}
 
 	public void dispose() {
 		if (ctx != null) {
 			Context.exit();
 		}
 	}
 
 	public void unregisterAll() {
 		ctx.evaluateString(global, "executor.validators = {};", "<cmd>", 1, null);
 	}
 
 	private String digest(String input) {
 		StringBuilder sb = new StringBuilder();
 		try {
 			MessageDigest md = MessageDigest.getInstance(DIGEST_ALGORITHM);
 			md.reset();
 			md.update(input.getBytes());
 			byte[] digest = md.digest();
 			for (int i = 0; i < digest.length; i++) {
 				int d = digest[i] & 0xff;
 				String hex = Integer.toHexString(d);
 				if (hex.length() == 1) {
 					sb.append("0");
 				}
 				sb.append(hex);
 			}
 		} catch (NoSuchAlgorithmException e) {
 			// ignore
 		}
 		return sb.toString();
 	}
 }
