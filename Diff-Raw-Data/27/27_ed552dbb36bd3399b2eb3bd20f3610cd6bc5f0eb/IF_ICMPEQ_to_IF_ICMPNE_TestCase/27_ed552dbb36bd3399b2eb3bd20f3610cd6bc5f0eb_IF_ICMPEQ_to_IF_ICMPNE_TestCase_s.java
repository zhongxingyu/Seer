 package com.mutation.transform.bcel.ROR;
 
 import java.lang.reflect.Method;
 import java.util.List;
 
 import junit.framework.TestCase;
 
 import org.apache.bcel.Repository;
 
 import com.mutation.EMutationOperator;
 import com.mutation.Mutant;
 import com.mutation.transform.bcel.MutantGenerator;
 import com.mutation.transform.util.EventListenerMock;
 
 public class IF_ICMPEQ_to_IF_ICMPNE_TestCase extends TestCase {
 
 	// FileWriter writer = new FileWriter("mod.class");
 	// writer.write(new String(byteCode[0]));
 	// writer.flush();
 	// writer.close();
 
	private static final String TEMPLATE_CLASS_NAME = "mut.mutantgen.bcel.ROR.IF_ICMPEQ_to_IF_ICMPNE_TemplateClass";
 
 	private class TestClassLoader extends ClassLoader {
 
 		private void override(String binaryName, byte[] byteCode) {
 			super.defineClass(binaryName, byteCode, 0, byteCode.length);
 		}
 	}
 
 	public void test_IF_CMPNE_to_IF_CMPEQ_CheckNumberOfMutants() throws Exception {
 
 		EventListenerMock listenerMock = new EventListenerMock();
 
 		MutantGenerator bcel = new MutantGenerator();
 
 		List<Mutant> mutants = bcel.generateMutants(TEMPLATE_CLASS_NAME, Repository.lookupClass(TEMPLATE_CLASS_NAME)
 				.getBytes(), EMutationOperator.ROR, listenerMock);
 
 		assertEquals("Number of first level mutants incorrect", 2, mutants.size());
 	}
 
 	public void test_OriginalClassConditionalCases() {
 		assertEquals("Unmodified expectation not met", 1, new IF_ICMPEQ_to_IF_ICMPNE_TemplateClass().branch(5, 5));
 		assertEquals("Unmodified expectation not met", 3, new IF_ICMPEQ_to_IF_ICMPNE_TemplateClass().branch(1, 5));
 		assertEquals("Unmodified expectation not met", 3, new IF_ICMPEQ_to_IF_ICMPNE_TemplateClass().branch(1, 2));
 		assertEquals("Unmodified expectation not met", 2, new IF_ICMPEQ_to_IF_ICMPNE_TemplateClass().branch(5, 2));
 	}
 
 	public void test_IF_CMPNE_to_IF_CMPEQ_FirstBranchModifiedOnlyMutant_Case1() throws Exception {
 
 		EventListenerMock listenerMock = new EventListenerMock();
 
 		MutantGenerator bcel = new MutantGenerator();
 
 		List<Mutant> mutants = bcel.generateMutants(TEMPLATE_CLASS_NAME, Repository.lookupClass(TEMPLATE_CLASS_NAME)
 				.getBytes(), EMutationOperator.ROR, listenerMock);
 
 		TestClassLoader loader = new TestClassLoader();
 
 		loader.override(TEMPLATE_CLASS_NAME, mutants.get(0).getByteCode());
 
 		Class modifiedInputClass = loader.loadClass(TEMPLATE_CLASS_NAME);
 
 		Object modifiedInputClassInstance = modifiedInputClass.newInstance();
 
 		Method branchMethod = modifiedInputClass.getMethod("branch", new Class[] { int.class, int.class });
 
 		Object resultObject = branchMethod.invoke(modifiedInputClassInstance, new Object[] { 5, 5 });
 
 		assertNotNull(resultObject);
 		assertTrue(resultObject.getClass().isInstance(new Integer(2)));
 		assertTrue(resultObject.getClass().isAssignableFrom(Integer.class));
 		assertEquals(3, resultObject);
 	}
 
 	public void test_IF_CMPNE_to_IF_CMPEQ_FirstBranchModifiedOnlyMutant_Case2() throws Exception {
 
 		EventListenerMock listenerMock = new EventListenerMock();
 
 		MutantGenerator bcel = new MutantGenerator();
 
 		List<Mutant> mutants = bcel.generateMutants(TEMPLATE_CLASS_NAME, Repository.lookupClass(TEMPLATE_CLASS_NAME)
 				.getBytes(), EMutationOperator.ROR, listenerMock);
 
 		TestClassLoader loader = new TestClassLoader();
 
 		loader.override(TEMPLATE_CLASS_NAME, mutants.get(0).getByteCode());
 
 		Class modifiedInputClass = loader.loadClass(TEMPLATE_CLASS_NAME);
 
 		Object modifiedInputClassInstance = modifiedInputClass.newInstance();
 
 		Method branchMethod = modifiedInputClass.getMethod("branch", new Class[] { int.class, int.class });
 
 		Object resultObject = branchMethod.invoke(modifiedInputClassInstance, new Object[] { 1, 5 });
 
 		assertNotNull(resultObject);
 		assertTrue(resultObject.getClass().isInstance(new Integer(2)));
 		assertTrue(resultObject.getClass().isAssignableFrom(Integer.class));
 		assertEquals(1, resultObject);
 	}
 
 	public void test_IF_CMPNE_to_IF_CMPEQ_FirstBranchModifiedOnlyMutant_Case3() throws Exception {
 
 		EventListenerMock listenerMock = new EventListenerMock();
 
 		MutantGenerator bcel = new MutantGenerator();
 
 		List<Mutant> mutants = bcel.generateMutants(TEMPLATE_CLASS_NAME, Repository.lookupClass(TEMPLATE_CLASS_NAME)
 				.getBytes(), EMutationOperator.ROR, listenerMock);
 
 		TestClassLoader loader = new TestClassLoader();
 
 		loader.override(TEMPLATE_CLASS_NAME, mutants.get(0).getByteCode());
 
 		Class modifiedInputClass = loader.loadClass(TEMPLATE_CLASS_NAME);
 
 		Object modifiedInputClassInstance = modifiedInputClass.newInstance();
 
 		Method branchMethod = modifiedInputClass.getMethod("branch", new Class[] { int.class, int.class });
 
 		Object resultObject = branchMethod.invoke(modifiedInputClassInstance, new Object[] { 1, 2 });
 
 		assertNotNull(resultObject);
 		assertTrue(resultObject.getClass().isInstance(new Integer(2)));
 		assertTrue(resultObject.getClass().isAssignableFrom(Integer.class));
 		assertEquals(2, resultObject);
 	}
 
 	public void test_IF_CMPNE_to_IF_CMPEQ_FirstBranchModifiedOnlyMutant_Case4() throws Exception {
 
 		EventListenerMock listenerMock = new EventListenerMock();
 
 		MutantGenerator bcel = new MutantGenerator();
 
 		List<Mutant> mutants = bcel.generateMutants(TEMPLATE_CLASS_NAME, Repository.lookupClass(TEMPLATE_CLASS_NAME)
 				.getBytes(), EMutationOperator.ROR, listenerMock);
 
 		TestClassLoader loader = new TestClassLoader();
 
 		loader.override(TEMPLATE_CLASS_NAME, mutants.get(0).getByteCode());
 
 		Class modifiedInputClass = loader.loadClass(TEMPLATE_CLASS_NAME);
 
 		Object modifiedInputClassInstance = modifiedInputClass.newInstance();
 
 		Method branchMethod = modifiedInputClass.getMethod("branch", new Class[] { int.class, int.class });
 
 		Object resultObject = branchMethod.invoke(modifiedInputClassInstance, new Object[] { 5, 2 });
 
 		assertNotNull(resultObject);
 		assertTrue(resultObject.getClass().isInstance(new Integer(2)));
 		assertTrue(resultObject.getClass().isAssignableFrom(Integer.class));
 		assertEquals(3, resultObject);
 	}
 
 	public void test_IF_CMPNE_to_IF_CMPEQ_SecondBranchModifiedOnlyMutant_Case1() throws Exception {
 
 		EventListenerMock listenerMock = new EventListenerMock();
 
 		MutantGenerator bcel = new MutantGenerator();
 
 		List<Mutant> mutants = bcel.generateMutants(TEMPLATE_CLASS_NAME, Repository.lookupClass(TEMPLATE_CLASS_NAME)
 				.getBytes(), EMutationOperator.ROR, listenerMock);
 
 		TestClassLoader loader = new TestClassLoader();
 
 		loader.override(TEMPLATE_CLASS_NAME, mutants.get(1).getByteCode());
 
 		Class modifiedInputClass = loader.loadClass(TEMPLATE_CLASS_NAME);
 
 		Object modifiedInputClassInstance = modifiedInputClass.newInstance();
 
 		Method branchMethod = modifiedInputClass.getMethod("branch", new Class[] { int.class, int.class });
 
 		Object resultObject = branchMethod.invoke(modifiedInputClassInstance, new Object[] { 5, 5 });
 
 		assertNotNull(resultObject);
 		assertTrue(resultObject.getClass().isInstance(new Integer(2)));
 		assertTrue(resultObject.getClass().isAssignableFrom(Integer.class));
 		assertEquals(2, resultObject);
 	}
 
 	public void test_IF_CMPNE_to_IF_CMPEQ_SecondBranchModifiedOnlyMutant_Case2() throws Exception {
 
 		EventListenerMock listenerMock = new EventListenerMock();
 
 		MutantGenerator bcel = new MutantGenerator();
 
 		List<Mutant> mutants = bcel.generateMutants(TEMPLATE_CLASS_NAME, Repository.lookupClass(TEMPLATE_CLASS_NAME)
 				.getBytes(), EMutationOperator.ROR, listenerMock);
 
 		TestClassLoader loader = new TestClassLoader();
 
 		loader.override(TEMPLATE_CLASS_NAME, mutants.get(1).getByteCode());
 
 		Class modifiedInputClass = loader.loadClass(TEMPLATE_CLASS_NAME);
 
 		Object modifiedInputClassInstance = modifiedInputClass.newInstance();
 
 		Method branchMethod = modifiedInputClass.getMethod("branch", new Class[] { int.class, int.class });
 
 		Object resultObject = branchMethod.invoke(modifiedInputClassInstance, new Object[] { 1, 5 });
 
 		assertNotNull(resultObject);
 		assertTrue(resultObject.getClass().isInstance(new Integer(2)));
 		assertTrue(resultObject.getClass().isAssignableFrom(Integer.class));
 		assertEquals(3, resultObject);
 	}
 
 	public void test_IF_CMPNE_to_IF_CMPEQ_SecondBranchModifiedOnlyMutant_Case3() throws Exception {
 
 		EventListenerMock listenerMock = new EventListenerMock();
 
 		MutantGenerator bcel = new MutantGenerator();
 
 		List<Mutant> mutants = bcel.generateMutants(TEMPLATE_CLASS_NAME, Repository.lookupClass(TEMPLATE_CLASS_NAME)
 				.getBytes(), EMutationOperator.ROR, listenerMock);
 
 		TestClassLoader loader = new TestClassLoader();
 
 		loader.override(TEMPLATE_CLASS_NAME, mutants.get(1).getByteCode());
 
 		Class modifiedInputClass = loader.loadClass(TEMPLATE_CLASS_NAME);
 
 		Object modifiedInputClassInstance = modifiedInputClass.newInstance();
 
 		Method branchMethod = modifiedInputClass.getMethod("branch", new Class[] { int.class, int.class });
 
 		Object resultObject = branchMethod.invoke(modifiedInputClassInstance, new Object[] { 1, 2 });
 
 		assertNotNull(resultObject);
 		assertTrue(resultObject.getClass().isInstance(new Integer(2)));
 		assertTrue(resultObject.getClass().isAssignableFrom(Integer.class));
 		assertEquals(3, resultObject);
 	}
 
 	public void test_IF_CMPNE_to_IF_CMPEQ_SecondBranchModifiedOnlyMutant_Case4() throws Exception {
 
 		EventListenerMock listenerMock = new EventListenerMock();
 
 		MutantGenerator bcel = new MutantGenerator();
 
 		List<Mutant> mutants = bcel.generateMutants(TEMPLATE_CLASS_NAME, Repository.lookupClass(TEMPLATE_CLASS_NAME)
 				.getBytes(), EMutationOperator.ROR, listenerMock);
 
 		TestClassLoader loader = new TestClassLoader();
 
 		loader.override(TEMPLATE_CLASS_NAME, mutants.get(1).getByteCode());
 
 		Class modifiedInputClass = loader.loadClass(TEMPLATE_CLASS_NAME);
 
 		Object modifiedInputClassInstance = modifiedInputClass.newInstance();
 
 		Method branchMethod = modifiedInputClass.getMethod("branch", new Class[] { int.class, int.class });
 
 		Object resultObject = branchMethod.invoke(modifiedInputClassInstance, new Object[] { 5, 2 });
 
 		assertNotNull(resultObject);
 		assertTrue(resultObject.getClass().isInstance(new Integer(2)));
 		assertTrue(resultObject.getClass().isAssignableFrom(Integer.class));
 		assertEquals(1, resultObject);
 	}
 
 	public void test_SourceMappings() throws Exception {
 
 		EventListenerMock listenerMock = new EventListenerMock();
 
 		MutantGenerator bcel = new MutantGenerator();
 
 		List<Mutant> mutants = bcel.generateMutants(TEMPLATE_CLASS_NAME, Repository.lookupClass(TEMPLATE_CLASS_NAME)
 				.getBytes(), EMutationOperator.ROR, listenerMock);
 
 		assertEquals("Incorrect line number", 6, mutants.get(0).getSourceMapping().getLineNo());
 		assertEquals("Incorrect source reference class name", TEMPLATE_CLASS_NAME, mutants.get(0).getSourceMapping()
 				.getClassName());
 
 		assertEquals("Incorrect line number", 7, mutants.get(1).getSourceMapping().getLineNo());
 		assertEquals("Incorrect source reference class name", TEMPLATE_CLASS_NAME, mutants.get(1).getSourceMapping()
 				.getClassName());
 
 	}
 
 }
