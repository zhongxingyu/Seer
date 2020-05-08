 package com.github.pepe79.jats.js;
 
 import java.io.FileNotFoundException;
 
 import javax.script.ScriptException;
 
 import junit.framework.Assert;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class IntegrationTest extends ScriptTest
 {
 
 	@Before
 	public void setup() throws FileNotFoundException, ScriptException
 	{
 		engine = createScriptEngine(new String[]
 		{ "target/test-classes/underscore.js", "target/classes/js/jats.js", "target/test-js/project.js" });
 	}
 
 	@Test
 	public void testJsLoad() throws FileNotFoundException, ScriptException
 	{
 		Assert.assertTrue((Boolean) engine.eval("typeof JATS==='function'"));
 		Assert.assertTrue((Boolean) engine.eval("typeof JATS.fromJson === 'function'"));
 	}
 
 	@Test
 	public void testFromJsonReference() throws FileNotFoundException, ScriptException
 	{
 		engine.eval("var product=JATS.fromJson({id:1, jatsType:Product})");
 		Assert.assertTrue((Boolean) engine.eval("product.getId()===1"));
 	}
 }
