 package org.dawnsci.python.rpc;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 import junit.framework.Assert;
 
 import org.dawb.common.util.eclipse.BundleUtils;
 import org.dawnsci.python.rpc.IPythonRunScript;
 import org.eclipse.core.runtime.CoreException;
 import org.junit.Test;
 
 import uk.ac.diamond.scisoft.analysis.rpc.AnalysisRpcException;
 
 abstract public class AbstractPythonRunScriptServicePluginTest implements
 		IPythonRunScript {
 
 	private String getScript(String script) throws IOException {
 		final String scriptPath = BundleUtils.getBundleLocation(
 				"org.dawb.common.python").getAbsolutePath()
 				+ "/test/org/dawb/common/python/rpc/" + script;
 		return scriptPath;
 	}
 
 	/** Make a temporary file out of a script */
 	private String getTemp(String pycode) throws IOException {
 		File temp = File.createTempFile("PythonRunScriptServiceTest", ".py");
 		FileWriter writer = new FileWriter(temp);
 		try {
 			writer.write(pycode);
 		} finally {
 			writer.close();
 		}
 		// Comment out this line if you want to keep the temp python file
 		temp.deleteOnExit();
 		return temp.getAbsolutePath();
 	}
 
 	@Test
 	public void testEmptyResult() throws AnalysisRpcException, IOException,
 			CoreException {
 		Map<String, Object> result = runScript(
 				getTemp("def run(**kwargs): return {}"),
 				Collections.<String, Object> emptyMap());
 		Assert.assertEquals(Collections.emptyMap(), result);
 	}
 
 	@Test
 	public void testNullResult() throws AnalysisRpcException, IOException,
 			CoreException {
 		// Remember, no return in Python is the same as return None
 		Map<String, Object> result = runScript(
 				getTemp("def run(**kwargs): pass"),
 				Collections.<String, Object> emptyMap());
 		Assert.assertEquals(null, result);
 	}
 
 	@Test
 	public void testExceptionFromPython() throws IOException {
 		try {
			runScript(getTemp("def run(**kwargs): assert False"),
 					Collections.<String, Object> emptyMap());
 		} catch (AnalysisRpcException e) {
			Assert.assertTrue(e.getCause().toString()
 					.contains("AssertionError"));
			Assert.assertTrue(e.getCause().toString().contains("assert False"));
 		}
 	}
 
 	@Test
 	public void testBasicOutput() throws AnalysisRpcException, IOException,
 			CoreException {
 		Map<String, Object> result = runScript(
 				getTemp("def run(**kwargs): return {'var1': 23}"),
 				Collections.<String, Object> emptyMap());
 		Assert.assertTrue(result.size() == 1);
 		Assert.assertEquals(result.get("var1"), 23);
 	}
 
 	@Test
 	public void testMultipleOutput() throws AnalysisRpcException, IOException,
 			CoreException {
 		Map<String, Object> result = runScript(
 				getTemp("def run(**kwargs): return {'var1': 23, 'var2': 'abc'}"),
 				Collections.<String, Object> emptyMap());
 		Assert.assertTrue(result.size() == 2);
 		Assert.assertEquals(result.get("var1"), 23);
 		Assert.assertEquals(result.get("var2"), "abc");
 	}
 
 	@SuppressWarnings("unchecked")
 	@Test
 	public void testComplexOutput() throws AnalysisRpcException, IOException,
 			CoreException {
 		Map<String, Object> result = runScript(
 				getTemp("def run(**kwargs): return {'var1': {1 : 2, 'b' : 3}}"),
 				Collections.<String, Object> emptyMap());
 		Assert.assertTrue(result.size() == 1);
 		Map<Object, Object> var1 = (Map<Object, Object>) result.get("var1");
 		Assert.assertEquals(var1.get(1), 2);
 		Assert.assertEquals(var1.get("b"), 3);
 	}
 
 	@Test
 	public void testBasicInput() throws AnalysisRpcException, IOException {
 		Map<String, Object> input = new HashMap<String, Object>();
 		input.put("var1", 23);
 		runScript(getTemp("def run(var1, **kwargs): assert var1 == 23"), input);
 	}
 
 	@Test
 	public void testMultipleInput() throws AnalysisRpcException, IOException {
 		Map<String, Object> input = new HashMap<String, Object>();
 		input.put("var1", 23);
 		input.put("var2", "abc");
 		runScript(
 				getTemp("def run(var1, var2, **kwargs): assert var1 == 23; assert var2 == 'abc'"),
 				input);
 	}
 
 	@Test
 	public void testComplexInput() throws AnalysisRpcException, IOException {
 		Map<String, Object> input = new HashMap<String, Object>();
 		Map<Object, Object> var1 = new HashMap<Object, Object>();
 		var1.put(1, 2);
 		var1.put("b", 3);
 		input.put("var1", var1);
 		runScript(
 				getTemp("def run(var1, **kwargs): assert var1 == {1 : 2, 'b' : 3}"),
 				input);
 	}
 
 	/** Regression test (of sorts) for DAWNSCI-317 */
 	@Test
 	public void testBugDawnSci317() throws AnalysisRpcException, IOException {
 		runScript(getScript("runscript_bug_dawnsci317.py"),
 				Collections.<String, Object> emptyMap());
 	}
 
 	@Test
 	public void testImport() throws AnalysisRpcException, IOException {
 		runScript(getTemp("import math\ndef run(**kwargs): pass"),
 				Collections.<String, Object> emptyMap());
 	}
 
 	@Test
 	public void testAlternativeMethodName() throws AnalysisRpcException,
 			IOException {
 		Map<String, Object> result = runScript(
 				getTemp("def altrun(**kwargs): return {'var1': 23}"),
 				Collections.<String, Object> emptyMap(), "altrun");
 		Assert.assertTrue(result.size() == 1);
 		Assert.assertEquals(result.get("var1"), 23);
 	}
 
 	@Test
 	public void testVariableName() throws AnalysisRpcException, IOException {
 		Map<String, Object> result = runScript(
 				getTemp("run=lambda **kwargs: {'var1': 23}"),
 				Collections.<String, Object> emptyMap());
 		Assert.assertTrue(result.size() == 1);
 		Assert.assertEquals(result.get("var1"), 23);
 	}
 
 	@Test
 	public void testClassCall() throws AnalysisRpcException, IOException {
 		Map<String, Object> result = runScript(
 				getTemp("class x:\n\tdef __call__(self, **kwargs): return {'var1': 23}\nrun=x()"),
 				Collections.<String, Object> emptyMap());
 		Assert.assertTrue(result.size() == 1);
 		Assert.assertEquals(result.get("var1"), 23);
 	}
 
 }
