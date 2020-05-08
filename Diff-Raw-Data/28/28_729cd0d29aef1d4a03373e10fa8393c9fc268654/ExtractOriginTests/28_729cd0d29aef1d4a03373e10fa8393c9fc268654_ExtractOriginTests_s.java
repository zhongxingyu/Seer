 package org.languages.alng.instrumentation.extract;
 
 import java.io.File;
 
 import junit.framework.Assert;
 
 import org.junit.After;
 import org.junit.Test;
 import org.spoofax.interpreter.terms.IStrategoTerm;
 import org.strategoxt.HybridInterpreter;
 
 import util.ALTestConstants;
 import util.HybridInterpreterHelper;
 import util.alng_scripts.JustPrint;
 
 public class ExtractOriginTests {
 
 	private HybridInterpreter i;
 	
 	@Test
 	public void extractOriginFromJustPrint()
 	{
 		i = HybridInterpreterHelper.createHybridInterpreter();
 		
 		String alng_program_location = ALTestConstants.ALNG_SCRIPTS_DIR + "/justprint.alng";
 		File alngFile = new File(alng_program_location);
 		Assert.assertTrue(alngFile.exists());
 		
 		i.setCurrent(i.getFactory().makeString(alng_program_location));
 		boolean b = HybridInterpreterHelper.safeInvoke(i, "parse-actionlanguage-file");
 		Assert.assertTrue(b);
 		IStrategoTerm dslProgramAterm = i.current();
 		JustPrint justPrint = new JustPrint();
 		justPrint.init(dslProgramAterm);
 		
 		// origin-line
 		i.setCurrent(dslProgramAterm);
 		b = HybridInterpreterHelper.safeInvoke(i, "origin-line");
 		Assert.assertTrue(b);
 		Assert.assertNotNull(i.current());
 		Assert.assertEquals("0", i.current().toString()); // FIXME: should be 0
 		
 		// needs-location-correction
 		i.setCurrent(justPrint.getFunctionDeclVoid());
 		b = HybridInterpreterHelper.safeInvoke(i, "needs-location-correction");
 		Assert.assertFalse(b);
 		
 		// needs-location-correction
 		i.setCurrent(dslProgramAterm);
 		b = HybridInterpreterHelper.safeInvoke(i, "needs-location-correction");
 		Assert.assertTrue(b);
 		
 		i.setCurrent(dslProgramAterm);
 		b = HybridInterpreterHelper.safeInvoke(i, "origin-location");
 		Assert.assertTrue(b);
 		Assert.assertNotNull(i.current());
 		Assert.assertEquals("(0,0,7,0)", i.current().toString());
 		
 		i.setCurrent(dslProgramAterm);
 		b = HybridInterpreterHelper.safeInvoke(i, "get-a-b-c-d");
 		Assert.assertTrue(b);
 		Assert.assertEquals("(1,0,8,0)", i.current().toString());
 		
 		i.setCurrent(justPrint.getFunctionDeclVoid());
 		b = HybridInterpreterHelper.safeInvoke(i, "origin-location");
 		Assert.assertTrue(b);
 		Assert.assertNotNull(i.current());
 		Assert.assertEquals("(5,0,8,0)", i.current().toString());
 		
 		i.setCurrent(justPrint.getFunctionBody());
 		b = HybridInterpreterHelper.safeInvoke(i, "origin-location");
 		Assert.assertTrue(b);
 		Assert.assertNotNull(i.current());
 		Assert.assertEquals("(6,4,6,42)", i.current().toString());
 		
 		i.setCurrent(justPrint.getStatement());
 		b = HybridInterpreterHelper.safeInvoke(i, "origin-location");
 		Assert.assertTrue(b);
 		Assert.assertNotNull(i.current());
 		Assert.assertEquals("(6,4,6,42)", i.current().toString());
 	}
 	
 	@Test
 	public void extractOriginFromTermLoadedByOtherHybridInterpreter()
 	{
 		JustPrint justPrint = new JustPrint();
 		justPrint.init();
 		
 		i = HybridInterpreterHelper.createHybridInterpreter();
 		i.setCurrent(justPrint.getModule());
 		boolean b = HybridInterpreterHelper.safeInvoke(i, "origin-location");
 		Assert.assertTrue(b);
 		Assert.assertNotNull(i.current());
		Assert.assertEquals("(0,0,7,0)", i.current().toString());
 		
 		i.setCurrent(justPrint.getFunctionDeclVoid());
 		b = HybridInterpreterHelper.safeInvoke(i, "origin-location");
 		Assert.assertTrue(b);
 		Assert.assertNotNull(i.current());
 		Assert.assertEquals("(5,0,8,0)", i.current().toString());
 		
 		i.setCurrent(justPrint.getFunctionBody());
 		b = HybridInterpreterHelper.safeInvoke(i, "origin-location");
 		Assert.assertTrue(b);
 		Assert.assertNotNull(i.current());
 		Assert.assertEquals("(6,4,6,42)", i.current().toString());
 		
 		i.setCurrent(justPrint.getStatement());
 		b = HybridInterpreterHelper.safeInvoke(i, "origin-location");
 		Assert.assertTrue(b);
 		Assert.assertNotNull(i.current());
 		Assert.assertEquals("(6,4,6,42)", i.current().toString());
 	}
 	
 	@Test
 	public void walkJustPrint()
 	{
 		i = HybridInterpreterHelper.createHybridInterpreter();
 		
 		String alng_program_location = ALTestConstants.ALNG_SCRIPTS_DIR + "/justprint.alng";
 		File alngFile = new File(alng_program_location);
 		Assert.assertTrue(alngFile.exists());
 		
 		i.setCurrent(i.getFactory().makeString(alng_program_location));
 		boolean b = HybridInterpreterHelper.safeInvoke(i, "parse-actionlanguage-file");
 		Assert.assertTrue(b);
 		
 		b = HybridInterpreterHelper.safeInvoke(i, "walk-terms");
 		Assert.assertTrue(b);
 	}
 	@After
 	public void tearDownInterpreter() {
 		i.uninit();
 		i.shutdown();
 	}
 }
