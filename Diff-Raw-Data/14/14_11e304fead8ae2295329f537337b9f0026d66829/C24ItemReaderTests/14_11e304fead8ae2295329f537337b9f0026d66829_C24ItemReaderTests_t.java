 /*
  * Copyright 2012 C24 Technologies
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *			http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package biz.c24.io.spring.batch.reader;
 
 import java.io.IOException;
 import java.util.Collection;
 import java.util.LinkedList;
 
 import org.junit.Test;
 import org.springframework.batch.core.JobParameters;
 import org.springframework.batch.core.StepExecution;
 import org.springframework.batch.item.NonTransientResourceException;
 import org.springframework.batch.item.ParseException;
 import org.springframework.batch.item.UnexpectedInputException;
 import org.springframework.core.io.ClassPathResource;
 
 import biz.c24.io.api.ParserException;
 import biz.c24.io.api.data.ComplexDataObject;
 import biz.c24.io.api.data.ValidationException;
 import biz.c24.io.examples.models.basic.EmployeeElement;
 import biz.c24.io.spring.batch.reader.source.BufferedReaderSource;
 import biz.c24.io.spring.batch.reader.source.FileSource;
 import biz.c24.io.spring.batch.reader.source.ZipFileSource;
 import biz.c24.io.spring.core.C24Model;
 import static org.mockito.Mockito.*;
 import static org.hamcrest.CoreMatchers.*;
 import static org.junit.Assert.*;
 
 /*
  * Validate the C24ItemReader
  * 
  * @author Andrew Elmore
  */
 public class C24ItemReaderTests {
 	
 	private C24Model employeeModel = new C24Model(EmployeeElement.getInstance());
 
 	@Test
 	public void testValidCsvRead() throws UnexpectedInputException, ParseException, NonTransientResourceException, IOException, ValidationException {
 		
 		FileSource source = new FileSource();
 		String filename = "employees-3-valid.csv";
 		
 		// No validation, no splitting
 		Collection<ComplexDataObject> objs = readFile(employeeModel, null, false, source, filename);
 		assertThat(objs.size(), is(3));
 		
 		// Validation but no splitting
 		objs = readFile(employeeModel, null, true, source, filename);
 		assertThat(objs.size(), is(3));
 		
 		// Validation & splitting
 		objs = readFile(employeeModel, ".*", true, source, filename);
 		assertThat(objs.size(), is(3));
 	}
 	
 	
 	@Test
 	public void testSemanticallyInvalidCsvRead() throws UnexpectedInputException, ParseException, NonTransientResourceException, IOException, ValidationException {
 		FileSource source = new FileSource();
 		String filename = "employees-3-semanticallyinvalid.csv";
 		
 		// No validation, no splitting
 		Collection<ComplexDataObject> objs = readFile(employeeModel, null, false, source, filename);
 		assertThat(objs.size(), is(3));
 
 		// Validation but no splitting
 		try {
 			readFile(employeeModel, null, true, source, filename);
 			fail("Structurally invalid file did not generate a ValidationException");
		} catch(ParseException pEx) {
 			// Expected behaviour
 		}
 		
 		// Validation & splitting
 		// Validation but no splitting
 		try {
 			readFile(employeeModel, ".*", true, source, filename);
 			fail("Structurally invalid file did not generate a ValidationException");
		} catch(ParseException pEx) {
 			// Expected behaviour
 		}
 	}
 	
 	
 	@Test
 	public void testStructurallyInvalidCsvRead() throws UnexpectedInputException, ParseException, NonTransientResourceException, IOException, ValidationException {
 		FileSource source = new FileSource();
 		String filename = "employees-3-structurallyinvalid.csv";
 		
 		// No validation, no splitting
 		try {
 			readFile(employeeModel, null, false, source, filename);
 			fail("Structurally invalid file did not generate a ParserException");
		} catch(UnexpectedInputException uiEx) {
 			// Expected behaviour
 		}
 		
 		// Validation but no splitting
 		try {
 			readFile(employeeModel, null, true, source, filename);
 			fail("Structurally invalid file did not generate a ParserException");
		} catch(UnexpectedInputException uiEx) {
 			// Expected behaviour
 		}
 		
 		// Validation & splitting
 		try {
 			readFile(employeeModel, ".*", true, source, filename);
 			fail("Structurally invalid file did not generate a ParserException");
		} catch(UnexpectedInputException uiEx) {
 			// Expected behaviour
 		}
 	}
 	
 	@Test
 	public void testValidZipRead() throws UnexpectedInputException, ParseException, NonTransientResourceException, IOException, ValidationException {
 		
 		BufferedReaderSource source = new ZipFileSource();
 		String filename = "employees-5-valid.zip";
 		
 		// No validation, no splitting
 		Collection<ComplexDataObject> objs = readFile(employeeModel, null, false, source, filename);
 		assertThat(objs.size(), is(5));
 		
 		// Validation but no splitting
 		objs = readFile(employeeModel, null, true, source, filename);
 		assertThat(objs.size(), is(5));
 		
 		// Validation & splitting
 		objs = readFile(employeeModel, ".*", true, source, filename);
 		assertThat(objs.size(), is(5));
 	}
 	
 	
 	private Collection<ComplexDataObject> readFile(C24Model model, String optionalElementStartRegEx, boolean validate, BufferedReaderSource source, String filename) throws IOException, UnexpectedInputException, ParseException, NonTransientResourceException, ValidationException {
 		C24ItemReader reader = new C24ItemReader();
 		reader.setModel(model);
 		if(optionalElementStartRegEx != null) {
 			reader.setElementStartPattern(optionalElementStartRegEx);
 		}
 		
 		reader.setSource(source);
 		reader.setValidate(validate);
 		
 		StepExecution stepExecution = getStepExecution(filename);
 		
 		reader.setup(stepExecution);
 		
 		ComplexDataObject obj = null;
 		Collection<ComplexDataObject> objs = new LinkedList<ComplexDataObject>();
 		
 		while((obj = reader.read()) != null) {
 			assertThat(obj.getDefiningElementDecl(), is(EmployeeElement.getInstance()));
 			objs.add(obj);
 		}
 		
 		reader.cleanup();
 		
 		return objs;
 	}
 		
 	private StepExecution getStepExecution(String classPathResourceName) throws IOException {
 		
 		ClassPathResource sourceFile = new ClassPathResource(classPathResourceName);
 		
 		JobParameters jobParams = mock(JobParameters.class);
 		when(jobParams.getString("input.file")).thenReturn(sourceFile.getFile().getAbsolutePath());
 
 		StepExecution stepExecution = mock(StepExecution.class);
 		when(stepExecution.getJobParameters()).thenReturn(jobParams);
 		
 		return stepExecution;
 		
 	}
 }
