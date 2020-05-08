 package com.google.codes.dryvalidator;
 
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.net.URL;
 
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
		scope = ctx.initStandardObjects();
 	}
 	@Test
	public void test() throws IOException, URISyntaxException {
		Main.processSource(ctx, "src/main/resources/com/google/codes/dryvalidator/joose.js");
		Main.processSource(ctx, "src/main/resources/com/google/codes/dryvalidator/dry-validator.js");
		Main.processSource(ctx, "src/test/resources/env.rhino.js");
 		URL url = getClass().getClassLoader().getResource("validation.html");
 		eval("Envjs('"+url.toString().replaceFirst("file:", "file://")+"');");
		Main.processSource(ctx, "src/test/resources/form-test.js");
 	}
 
 	@After
 	public void tearDown() {
 		Context.exit();
 	}
 
 	private Object eval(String text) {
 		Script script = ctx.compileString(text, "<cmd>", 1, null);
		return Main.evaluateScript(script, ctx, Main.getGlobal());
 	}
 }
