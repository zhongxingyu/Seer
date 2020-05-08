 package com.google.codes.dryvalidator;
 
import java.io.File;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.net.URL;
 
import org.apache.commons.io.FileUtils;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.mozilla.javascript.Context;
 import org.mozilla.javascript.Script;
 import org.mozilla.javascript.Scriptable;
 import org.mozilla.javascript.tools.shell.Global;
 import org.mozilla.javascript.tools.shell.Main;
 
 public class FormTest {
 	Context ctx;
 	Scriptable scope;
 	@Before
 	public void setUp() {
 		ctx = Context.enter();
 		ctx.setOptimizationLevel(-1);
 		Global global = Main.getGlobal();
 		global.init(ctx);
		scope = ctx.initStandardObjects(global);
 	}
 	@Test
	public void createForm() throws IOException, URISyntaxException {
		ctx.evaluateString(scope, FileUtils.readFileToString(new File(
				"src/main/resources/com/google/codes/dryvalidator/joose.js")), "joose.js", 1, null);
		ctx.evaluateString(scope, FileUtils.readFileToString(new File(
				"src/main/resources/com/google/codes/dryvalidator/dry-validator.js"), "UTF-8"), "dry-validator.js", 1, null);
		ctx.evaluateString(scope, FileUtils.readFileToString(new File(
				"src/test/resources/env.rhino.js")), "env.rhino.js", 1, null);
 		URL url = getClass().getClassLoader().getResource("validation.html");
 		eval("Envjs('"+url.toString().replaceFirst("file:", "file://")+"');");
		ctx.evaluateString(scope, FileUtils.readFileToString(new File(
				"src/test/resources/form-test.js"), "UTF-8"), "form-test.js", 1, null);

 	}
 
 	@After
 	public void tearDown() {
 		Context.exit();
 	}
 
 	private Object eval(String text) {
 		Script script = ctx.compileString(text, "<cmd>", 1, null);
		return script.exec(ctx, scope);
 	}
 }
