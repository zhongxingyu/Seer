 package com.github.pepe79.jats.js;
 
 import java.io.FileNotFoundException;
 
 import javax.script.ScriptException;
 
 import junit.framework.Assert;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class JatsTest extends ScriptTest
 {
 
 	@Before
 	public void setup() throws FileNotFoundException, ScriptException
 	{
 		engine = createScriptEngine(new String[]
 		{ "target/test-classes/underscore.js", "target/classes/js/jats.js" });
 	}
 
 	@Test
 	public void testJsLoad() throws FileNotFoundException, ScriptException
 	{
 		Assert.assertTrue((Boolean) engine.eval("typeof JATS==='function'"));
 		Assert.assertTrue((Boolean) engine.eval("typeof JATS.fromJson === 'function'"));
 
 		engine.eval("var obj=JATS.fromJson({id:1})");
 		Assert.assertTrue((Boolean) engine.eval("obj.id===1"));
 	}
 
 	@Test
 	public void testFromJsonSimple() throws FileNotFoundException, ScriptException
 	{
 		Assert.assertTrue((Boolean) engine.eval("JATS.fromJson({id:1}).id===1"));
 	}
 
 	@Test
 	public void testFromJsonReference() throws FileNotFoundException, ScriptException
 	{
 		engine.eval("var obj=JATS.fromJson({id:1, me:{$ref:1}})");
 		Assert.assertTrue((Boolean) engine.eval("obj.me===obj"));
 	}
 
 	@Test
 	public void testFromJsonReferenceArray() throws FileNotFoundException, ScriptException
 	{
 		engine.eval("var obj=JATS.fromJson({id:1, mes:[{$ref:1},{$ref:1}]})");
 		Assert.assertTrue((Boolean) engine.eval("obj.mes[0]===obj && obj.mes[1]===obj"));
 	}
 
 	@Test
 	public void testFromJsonReferenceMap() throws FileNotFoundException, ScriptException
 	{
 		engine.eval("var obj=JATS.fromJson({id:1, parent:{me:{$ref:1}}})");
 		Assert.assertTrue((Boolean) engine.eval("obj.parent.me===obj"));
 	}
 
 	@Test
 	public void testFromJsonReferenceJatsType() throws FileNotFoundException, ScriptException
 	{
 		engine.eval("var MyType=(function(){function MyType() { }; MyType.prototype.getId = function () { return this.id; }; return MyType })()");
 		engine.eval("var obj=JATS.fromJson({id:1, jatsType:MyType, me:{$ref:'1'}})");
 		Assert.assertTrue((Boolean) engine.eval("obj.me===obj"));
 		Assert.assertTrue((Boolean) engine.eval("obj.getId()===1"));
 	}
 
 }
