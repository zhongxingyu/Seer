 /*
  * This file is covered by the terms of the Common Public License v1.0.
  *
  * Copyright (c) SZEDER Gábor
  *
  * Parts of this software were developed within the JEOPARD research
  * project, which received funding from the European Union's Seventh
  * Framework Programme under grant agreement No. 216682.
  */
 
 package de.fzi.cjunit.jpf.outside;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.*;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.Test;
 
 import gov.nasa.jpf.Error;
 import gov.nasa.jpf.GenericProperty;
 import gov.nasa.jpf.JPF;
 import gov.nasa.jpf.Property;
 import gov.nasa.jpf.PropertyListenerAdapter;
 import gov.nasa.jpf.search.Search;
 import gov.nasa.jpf.jvm.JVM;
 
 import de.fzi.cjunit.JPFPropertyViolated;
 import de.fzi.cjunit.testexceptions.TestException;
 
 public class JPFInvokerTest {
 
 	TestFailedProperty createTestFailedProperty() {
 		return new TestFailedProperty() {
 			@Override
 			public boolean getTestResult() {
 				return true;
 			}
 		};
 	}
 
 	TestFailedProperty createViolatedTestFailedProperty() {
 		return new TestFailedProperty() {
 			@Override
 			public boolean getTestResult() {
 				return false;
 			}
 			@Override
 			public Throwable getException() {
 				return new TestException();
 			}
 		};
 	}
 
 	GenericProperty createViolatedGenericProeprty() {
 		return new GenericProperty() {
 			@Override
 			public boolean check(Search search, JVM jvm) {
 				return false;
 			}
 			@Override
 			public String getErrorMessage() {
 				return "something went wrong";
 			}
 		};
 	}
 
 	@Test
 	public void createJPFArgs() throws Throwable {
 		String[] args = new JPFInvoker().createJPFArgs(new String(),
 				String.class.getMethod("toString"),
 				RuntimeException.class);
 		assertThat(args, hasItemInArray(
 				"de.fzi.cjunit.jpf.inside.TestWrapper"));
 		assertThat(args, hasItemInArray(
 				"--testclass=java.lang.String"));
 		assertThat(args, hasItemInArray("--testmethod=toString"));
 		assertThat(args, hasItemInArray("--expectedexception=java.lang.RuntimeException"));
 	}
 
 	@Test
 	public void getJPFSearchErrorsReturnsUnalteredErrorList() {
 		final List<Error> errors = new ArrayList<Error>();
 		List<Error> errorsBackup = new ArrayList<Error>(errors);
 
 		JPFInvoker jpfInvoker = new JPFInvoker() {
 			@Override
 			protected void initJPF(String[] args) {
 				conf = JPF.createConfig(args);
 				jpf = new JPF(conf) {
 					@Override
 					public List<Error> getSearchErrors() {
 						return errors;
 					}
 				};
 			}
 		};
 		// JPF requires at least an application name during
 		// initialization, otherwise it errors out
 		jpfInvoker.initJPF(new String[] { "dummyapp" });
 
 		assertThat("same instance", jpfInvoker.getJPFSearchErrors(),
 				sameInstance(errors));
 		assertThat("same content", jpfInvoker.getJPFSearchErrors(),
 				equalTo(errorsBackup));
 	}
 
 	@Test
 	public void getTestResultOfSucceededTest() {
 		JPFInvoker jpfInvoker = new JPFInvoker() {
 			@Override
 			protected List<Error> getJPFSearchErrors() {
 				return new ArrayList<Error>();
 			}
 		};
 
 		assertThat("test result", jpfInvoker.getTestResult(),
 				equalTo(true));
 	}
 
 	@Test
 	public void getTestResultOfFailedTest() {
 		JPFInvoker jpfInvoker = new JPFInvoker() {
 			@Override
 			protected List<Error> getJPFSearchErrors() {
 				List<Error> list = new ArrayList<Error>();
 				// null reference is not enough, we need a
 				// PropertyListenerAdapter instance here,
 				// because Error's ctor calls
 				// property.getErrorMessage()
 				list.add(new Error(0,
 						new PropertyListenerAdapter(),
 						null, null));
 				return list;
 			}
 		};
 
 		assertThat("test result", jpfInvoker.getTestResult(),
 				equalTo(false));
 	}
 
 	@Test
 	public void getExceptionFromPropertyWithProperty() {
 		JPFInvoker jpfInvoker = new JPFInvoker();
 		Property property = createViolatedGenericProeprty();
 
 		assertThat(jpfInvoker.getExceptionFromProperty(property),
 				instanceOf(JPFPropertyViolated.class));
 	}
 
 	@Test
 	public void getExceptionFromPropertyWithTestProperty() {
 		JPFInvoker jpfInvoker = new JPFInvoker();
 		Property property = createViolatedTestFailedProperty();
 
 		assertThat(jpfInvoker.getExceptionFromProperty(property),
 				instanceOf(TestException.class));
 	}
 
 	@Test
 	public void checkResultOfSucceededTest() throws Throwable {
 		JPFInvoker jpfInvoker = new JPFInvoker() {
 			@Override
 			public void createTestProperties() {
 				testFailedProperty = createTestFailedProperty();
 			}
 		};
 		jpfInvoker.createTestProperties();
 
 		jpfInvoker.checkProperties();
 	}
 
 	@Test(expected=TestException.class)
 	public void checkResultOfFailedTest() throws Throwable {
 		JPFInvoker jpfInvoker = new JPFInvoker() {
 			public void createTestProperties() {
 				testFailedProperty
 					= createViolatedTestFailedProperty();
 			}
 		};
 		jpfInvoker.createTestProperties();
 
 		jpfInvoker.checkProperties();
 	}
 }
