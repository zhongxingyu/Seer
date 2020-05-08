 package org.dawnsci.persistence.test;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.dawb.common.services.IPersistenceService;
 import org.dawb.common.services.IPersistentFile;
 import org.dawnsci.persistence.PersistenceServiceCreator;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.fasterxml.jackson.core.JsonGenerationException;
 import com.fasterxml.jackson.core.JsonParseException;
 import com.fasterxml.jackson.core.type.TypeReference;
 import com.fasterxml.jackson.databind.JsonMappingException;
 import com.fasterxml.jackson.databind.ObjectMapper;
 
 import uk.ac.diamond.scisoft.analysis.fitting.functions.CompositeFunction;
 import uk.ac.diamond.scisoft.analysis.fitting.functions.Fermi;
 import uk.ac.diamond.scisoft.analysis.fitting.functions.Gaussian;
 import uk.ac.diamond.scisoft.analysis.fitting.functions.IFunction;
 
 public class ReadWriteFunctionTest {
 
 	private File tmp;
 	private IPersistenceService persist;
 	private IPersistentFile file;
 
 	@Before
 	public void before() throws Exception {
 		tmp = File.createTempFile("TestFunction", ".txt");
 		tmp.createNewFile();
 
 		// create the PersistentService
 		persist = PersistenceServiceCreator.createPersistenceService();
 		file = persist.createPersistentFile(tmp.getAbsolutePath());
 	}
 
 	@After
 	public void after(){
 		if (tmp != null)
 			tmp.deleteOnExit();
 
 		if(file != null)
 			file.close();
 	}
 
 
 	@Test
 	public void testWriteFunction() throws Exception {
 		
 		Gaussian gaussian = new Gaussian();
 		IFunction resultFunction = readWriteFunction(gaussian);
 		assertEquals(gaussian, resultFunction);
 	}
 
 	@Test
 	public void testWriteFunctionWithParam() throws Exception {
 		
 		Gaussian gaussian = new Gaussian(1,2,3);
 		IFunction resultFunction = readWriteFunction(gaussian);
 		assertEquals(gaussian, resultFunction);
 	}
 
 	@Test
 	public void testWriteFermiFunctionWithParam() throws Exception {
 		
 		Fermi fermi = new Fermi(1,2,3,4);
 		IFunction resultFunction = readWriteFunction(fermi);
 		assertEquals(fermi, resultFunction);
 	}
 	
 	@Test
 	public void testWriteCompositeFunction() throws Exception {
 		CompositeFunction compoundFunction = new CompositeFunction();
		Gaussian gaussian = new Gaussian();
 		Fermi fermi = new Fermi(1,2,3,4);
 		compoundFunction.addFunction(gaussian);
 		compoundFunction.addFunction(fermi);
		IFunction resultFunction = readWriteFunction(compoundFunction);
		assertEquals(compoundFunction, resultFunction);
 	}
 
 	@Test
 	public void testJackson() throws JsonGenerationException, JsonMappingException, IOException {
 		CompositeFunction compoundFunction = new CompositeFunction();
 		Gaussian gaussian = new Gaussian();
 		Fermi fermi = new Fermi(1,2,3,4);
 		compoundFunction.addFunction(gaussian);
 		compoundFunction.addFunction(fermi);
		setFunction(compoundFunction);
		
		CompositeFunction resultFunction = (CompositeFunction) getFunction(CompositeFunction.class);
		System.out.println();
		assertEquals(compoundFunction, resultFunction);
 	}
 
 	public void setFunction(IFunction function) throws JsonGenerationException, JsonMappingException, IOException {
 		ObjectMapper mapper = new ObjectMapper();
		mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
 		mapper.writeValue(tmp, function);
 	}
 
 	public IFunction getFunction(Class<?> myClass) throws JsonParseException, JsonMappingException, IOException {
 		ObjectMapper mapper = new ObjectMapper();
 		IFunction function =  mapper.readValue(tmp,  new TypeReference<CompositeFunction>() {});
 		System.out.println(mapper.writeValueAsString(function));
 		return function;
 	}
 
 
 	private IFunction readWriteFunction(IFunction function) throws Exception {
 		Map<String, IFunction> functions = new HashMap<String, IFunction>();
 		functions.put("MyFunction", function);
 		file.setFunctions(functions);
 		// TODO replace the current serializer by one that is generic enough to return 
 		// polymorphic objects
 //		setFunction(function);
 
 		Map<String, IFunction> functionsRead = null;
 		//read the persistent file and retrieve the functions
 		functionsRead = file.getFunctions(null);
 	
 		//test that the rois are the same
 		assertEquals(functions.containsKey("MyFunction"), functionsRead.containsKey("MyFunction"));
 
 		//test the unmarshalling of the JSON String
 		IFunction resultFunction = functionsRead.get("MyFunction");
 		return resultFunction;
 	}
  
 }
