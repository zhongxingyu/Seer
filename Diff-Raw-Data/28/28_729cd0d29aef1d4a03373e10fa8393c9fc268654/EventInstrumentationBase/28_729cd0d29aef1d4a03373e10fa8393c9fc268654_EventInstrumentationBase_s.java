 package org.languages.alng.instrumentation.events;
 
 import java.io.File;
 
 import junit.framework.Assert;
 
 import org.spoofax.interpreter.terms.IStrategoAppl;
 import org.spoofax.interpreter.terms.IStrategoConstructor;
 import org.spoofax.interpreter.terms.IStrategoTerm;
 import org.strategoxt.HybridInterpreter;
 
 import util.HybridInterpreterHelper;
 
 public abstract class EventInstrumentationBase {
 
 	
 	protected HybridInterpreter i = null;
 	
 	private IStrategoTerm originalDSLProgramSource = null;
 	
 	public IStrategoTerm getOriginalDSLProgramSource()
 	{
 		return this.originalDSLProgramSource;
 	}
 	
 	protected void initHI()
 	{
 		// init the HybridInterpreter
 		i = HybridInterpreterHelper.createHybridInterpreter();
 	}
 	
 	/**
 	 * Read the dlsdi file and call SET-SEL-DEFINITION
 	 */
 	protected void loadDSLDI(String dsldiLocation)
 	{
 		Assert.assertTrue(new File(dsldiLocation).exists());
 		// load the dlsdi
 		// read the SEL-definition and make sure it is available via GET-SEL-DEFINITION
 		i.setCurrent(i.getFactory().makeString(dsldiLocation));
 		boolean b = HybridInterpreterHelper.safeInvoke(i, "read-dsldi-file");
 		Assert.assertTrue(b);
 		// is SEL the current term?
 		Assert.assertEquals(IStrategoTerm.APPL, i.current().getTermType());
 		Assert.assertEquals("SEL", ((IStrategoAppl)i.current()).getName());
 		b = HybridInterpreterHelper.safeInvoke(i, "SET-SEL-DEFINITION");
 	}
 	
 	/**
 	 * Calls SET-DSL-PROGRAM-FILENAME and SET-DSL-PROGRAM-SOURCE.
 	 * The original DSL program is stored in this.originalDSLProgramSource.
 	 * @param dslProgramLocation
 	 */
 	protected void loadDSLProgram(String dslProgramLocation)
 	{
 		Assert.assertTrue(new File(dslProgramLocation).exists());
 		loadDSLProgramFilename(dslProgramLocation);
 		loadDSLProgramSource(dslProgramLocation);
 	}
 	
 	private void loadDSLProgramFilename(String dslProgramLocation)
 	{
 		
 		boolean b = false;
 		// SET-DSL-PROGRAM-FILENAME
 		i.setCurrent(i.getFactory().makeString(dslProgramLocation));
 		b = HybridInterpreterHelper.safeInvoke(i, "SET-DSL-PROGRAM-FILENAME");
 		Assert.assertTrue(b);
 	}
 	
 	private void loadDSLProgramSource(String dslProgramLocation)
 	{
 		boolean b = false;
 		i.setCurrent(i.getFactory().makeString(dslProgramLocation));
 		b = HybridInterpreterHelper.safeInvoke(i, "parse-actionlanguage-file");
 		Assert.assertTrue("Could not parse the ActionLanguage script", b);
 		
 		this.originalDSLProgramSource = i.current();
 		// SET-DSL-PROGRAM-SOURCE
 		b = HybridInterpreterHelper.safeInvoke(i, "SET-DSL-PROGRAM-SOURCE");
 		Assert.assertTrue(b);
 	}
 	
 	protected void registerExtractFunction(String extractTransformation)
 	{
 		boolean b = false;
 		Assert.assertNotNull("Strategy does not exist: " + extractTransformation, i.lookupUncifiedSVar(extractTransformation));
 		// call ext-register-extract-strategy(extract-strategy|strategy-name)
 		// the strategy ignores the current term
 		i.getContext().getVarScope().add("t0", i.getFactory().makeString(extractTransformation));
 		i.getContext().getVarScope().addSVar("s0", i.lookupUncifiedSVar(extractTransformation));
 		b = HybridInterpreterHelper.safeInvoke(i, "ext_register_extract_strategy_1_1");
 		Assert.assertTrue(b);
 		i.getContext().getVarScope().removeVar("t0");
 		i.getContext().getVarScope().removeSVar("s0");
 	}
 	
 	protected void registerGenerateFunction(String generateTransformation)
 	{
 		boolean b = false;
 		Assert.assertNotNull("Strategy does not exist: " + generateTransformation, i.lookupUncifiedSVar(generateTransformation));
 		// call ext-register-gen-strategy(gen-strategy|strategy-name)
 		// the strategy ignores the current term
 		i.getContext().getVarScope().add("t0", i.getFactory().makeString(generateTransformation));
 		i.getContext().getVarScope().addSVar("s0", i.lookupUncifiedSVar(generateTransformation));
 		b = HybridInterpreterHelper.safeInvoke(i, "ext_register_gen_strategy_1_1");
 		Assert.assertTrue(b);
 		i.getContext().getVarScope().removeVar("t0");
 		i.getContext().getVarScope().removeSVar("s0");
 	}
 	
 	protected void setOutputBasePath(String outputBasePath)
 	{
 		boolean b = false;
 		i.setCurrent(i.getFactory().makeString(outputBasePath));
 		b = HybridInterpreterHelper.safeInvoke(i, "SET-OUTPUT-BASEPATH");
 		Assert.assertTrue(b);
 	}
 	
 	protected void setLanguageID(String languageID)
 	{
 		boolean b = false;
 		i.setCurrent(i.getFactory().makeString(languageID));
 		b = HybridInterpreterHelper.safeInvoke(i, "SET-LANGUAGE-ID");
 		Assert.assertTrue(b);
 	}
 	
 	protected void setPostInstrumentation(String postInstrumentationStrategy)
 	{
 		boolean b = false;
 		i.setCurrent(i.getFactory().makeString(postInstrumentationStrategy));
 		b = HybridInterpreterHelper.safeInvoke(i, "SET-POST-INSTRUMENTATION-NAME");
 		Assert.assertTrue(b);
 	}
 	
 	protected void addWriteFile(String languageID, String writeStrategy)
 	{
 		boolean b = false;
 		IStrategoTerm writeFileDefinition = i.getFactory().makeTuple(i.getFactory().makeString(languageID), i.getFactory().makeString(writeStrategy));
 		i.setCurrent(writeFileDefinition);
 		b = HybridInterpreterHelper.safeInvoke(i, "add-dsl-specific-write-file");
 		Assert.assertTrue(b);
 	}
 	
 	/**
 	 * Get the (<GET-DSL-PROGRAM-FILENAME>, <GET-DSL-PROGRAM-SOURCE>)
 	 * @return
 	 */
 	protected IStrategoTerm getFilenameDSLProgramTuple()
 	{
 		boolean b = false;
 		b = HybridInterpreterHelper.safeInvoke(i, "GET-DSL-PROGRAM-FILENAME");
 		Assert.assertTrue(b);
 		IStrategoTerm filename = i.current();
 		
 		b = HybridInterpreterHelper.safeInvoke(i, "GET-DSL-PROGRAM-SOURCE");
 		Assert.assertTrue(b);
 		IStrategoTerm source = i.current();
 		
 		IStrategoTerm tuple = i.getFactory().makeTuple(filename, source);
 		return tuple;
 	}
 	
 	/**
 	 * MAkes a Succes(source-file, generated-file)
 	 * @param source
 	 * @param generated
 	 * @return
 	 */
 	protected IStrategoTerm makeSuccess(String source, String generated)
 	{
 		IStrategoConstructor successConstructor = i.getFactory().makeConstructor("Success", 2);
 		IStrategoTerm sourceFile = i.getFactory().makeString(source);
 		IStrategoTerm generatedFile = i.getFactory().makeString(generated);
 		IStrategoTerm succesReport = i.getFactory().makeAppl(successConstructor, sourceFile, generatedFile);
 		
 		return succesReport;
 	}
 	
 	protected void compareDslProgramSourceWithATermFile(String location)
 	{
 		Assert.assertTrue((new File(location)).exists());
 		
 		boolean b = false;
 		b = HybridInterpreterHelper.safeInvoke(i, "GET-DSL-PROGRAM-SOURCE");
 		Assert.assertTrue(b);
 		IStrategoTerm source = i.current();
 		
 		IStrategoTerm input = i.getFactory().makeString(location);
 		i.setCurrent(input);
 		HybridInterpreterHelper.safeInvoke(i, "ReadFromFile");
 		Assert.assertTrue(b);
 		Assert.assertEquals(i.current().toString(), source.toString());
 	}
 	
 	/**
 	 * GET-DSL-PROGRAM and compare it against the ActionLanguage file at alngProgramLocation.
 	 * @param alngProgramLocation
 	 */
 	protected void compareDslProgramSourceWithAlng(String alngProgramLocation)
 	{
 		Assert.assertTrue((new File(alngProgramLocation)).exists());
 		
 		boolean b = false;
 		b = HybridInterpreterHelper.safeInvoke(i, "GET-DSL-PROGRAM-SOURCE");
 		Assert.assertTrue(b);
 		IStrategoTerm source = i.current();
 		
 		IStrategoTerm input = i.getFactory().makeString(alngProgramLocation);
 		i.setCurrent(input);
 		HybridInterpreterHelper.safeInvoke(i, "parse-actionlanguage-file");
 		Assert.assertTrue(b);
 		Assert.assertEquals(i.current().toString(), source.toString());
 	}
 }
