 /*******************************************************************************
  * Copyright (c) 2009, Cloudsmith Inc.
  * The code, documentation and other materials contained herein have been
  * licensed under the Eclipse Public License - v 1.0 by the copyright holder
  * listed above, as the Initial Contributor under such license. The text of
  * such license is available at www.eclipse.org.
  ******************************************************************************/
 
 package org.eclipse.b3.beelang.junit;
 
 import java.io.IOException;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Type;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.eclipse.b3.BeeLangRuntimeModule;
 import org.eclipse.b3.backend.core.B3Engine;
 import org.eclipse.b3.backend.core.B3EngineException;
 import org.eclipse.b3.backend.evaluator.b3backend.B3JavaImport;
 import org.eclipse.b3.backend.evaluator.b3backend.BExecutionContext;
 import org.eclipse.b3.backend.evaluator.b3backend.BFunction;
 import org.eclipse.b3.backend.evaluator.typesystem.TypeUtils;
 import org.eclipse.b3.beeLang.BeeModel;
 import org.eclipse.b3.beelang.tests.Activator;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.resource.ContentHandler;
 import org.eclipse.xtext.resource.XtextResource;
 import org.eclipse.xtext.resource.XtextResourceSet;
 import org.junit.runner.Description;
 import org.junit.runner.Runner;
 import org.junit.runner.notification.Failure;
 import org.junit.runner.notification.RunNotifier;
 import org.junit.runners.ParentRunner;
 import org.junit.runners.model.InitializationError;
 
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 
 /**
  * <p>
  * A factory class for building JUnit 4 runners capable of executing B3 language functions as JUnit tests. A separate
  * runner is built for each of the <code>{@link B3Files}</code> defined for the class passed to the factory's
  * constructor. A runner built by this factory executes every B3 function which name start with the prefix of "test"
  * from the B3 file it was built for as a separate JUnit test.
  * </p>
  * 
  * @author michal.ruzicka@cloudsmith.com
  * 
  * @see B3Files
  * @see JUnitB3TestRunner
  */
 class JUnitB3FileRunnerFactory {
 
 	public static final String TEST_FUNCTION_PREFIX = "test";
 
 	protected static final Object[] EMPTY_PARAMETER_ARRAY = new Object[] {};
 
 	protected static final Type[] EMPTY_TYPE_ARRAY = new Type[] {};
 
 	protected class JUnitB3FileRunner extends ParentRunner<JUnitB3FileRunner.TestFunctionDescriptor> {
 
 		protected class TestFunctionDescriptor {
 
 			protected BFunction testFunction;
 
 			protected Description testFunctionDescription;
 
 			public TestFunctionDescriptor(BFunction function) {
 				testFunction = function;
 				// We call Description.createSuiteDescription despite this is really no test suite. This is because the
 				// other Description.create*Description methods take a Class<?> argument which we can't provide since
				// the tests are actually B3 function not wrapped by any Java class.
 				testFunctionDescription = Description.createSuiteDescription(String.format("%s(%s)",
 						function.getName(), b3FilePath));
 			}
 
 			public BFunction getFunction() {
 				return testFunction;
 			}
 
 			public Description getDescription() {
 				return testFunctionDescription;
 			}
 
 		}
 
 		protected String b3FilePath;
 
 		protected ArrayList<TestFunctionDescriptor> testFunctionDescriptors;
 
 		protected B3Engine b3Engine;
 
 		public JUnitB3FileRunner(String b3File) throws Exception {
 			super(definitionClass);
 
 			if(b3File.charAt(0) != '/')
 				b3File = '/' + b3File;
 
 			URI b3FileURI = URI.createPlatformPluginURI(Activator.PLUGIN_ID + b3File, true);
 			XtextResource resource = (XtextResource) beeLangResourceSet.createResource(b3FileURI,
 					ContentHandler.UNSPECIFIED_CONTENT_TYPE);
 
 			try {
 				resource.load(null);
 			} catch(IOException e) {
 				throw new Exception("Failed to load B3 file: " + b3File, e);
 			}
 			// TODO: consult resource.getErrors() and report possible errors
 
 			b3FilePath = b3File;
 
 			BeeModel beeModel = (BeeModel) resource.getParseResult().getRootASTElement();
 			BExecutionContext b3Context = (b3Engine = new B3Engine()).getContext();
 
 			testFunctionDescriptors = new ArrayList<TestFunctionDescriptor>();
 
 			try {
 				// Define all imports as constants
 				for(Type type : beeModel.getImports()) {
 					if(type instanceof B3JavaImport) {
 						Class<?> klass = TypeUtils.getRaw(type);
 						b3Context.defineValue(((B3JavaImport) type).getName(), klass, klass);
 					}
 				}
 
 				// Define all functions and create descriptors of test functions
 				for(BFunction function : beeModel.getFunctions()) {
 					b3Context.defineFunction(function);
 
 					String functionName = function.getName();
 
 					if(functionName.length() > TEST_FUNCTION_PREFIX.length()
 							&& functionName.startsWith(TEST_FUNCTION_PREFIX)
 							&& function.getParameterTypes().length == 0)
 						testFunctionDescriptors.add(new TestFunctionDescriptor(function));
 				}
 			} catch(B3EngineException e) {
 				throw new Exception("Failed to initialize B3Engine in preparation for testing of: " + b3File, e);
 			}
 		}
 
 		@Override
 		protected String getName() {
 			return b3FilePath;
 		}
 
 		@Override
 		protected Description describeChild(TestFunctionDescriptor child) {
 			return child.getDescription();
 		}
 
 		@Override
 		protected void runChild(TestFunctionDescriptor child, RunNotifier notifier) {
 			Description childDescription = child.getDescription();
 			String childFunctionName = child.getFunction().getName();
 
 			notifier.fireTestStarted(childDescription);
 			try {
 				b3Engine.getContext().callFunction(childFunctionName, EMPTY_PARAMETER_ARRAY, EMPTY_TYPE_ARRAY);
 			} catch(Throwable e) {
 				notifier.fireTestFailure(new Failure(childDescription, e));
 			} finally {
 				notifier.fireTestFinished(childDescription);
 			}
 		}
 
 		@Override
 		protected List<TestFunctionDescriptor> getChildren() {
 			return testFunctionDescriptors;
 		}
 
 	}
 
 	protected XtextResourceSet beeLangResourceSet;
 
 	protected final Class<?> definitionClass;
 
 	protected List<Runner> b3FileRunners;
 
 	{
 		Injector beeLangInjector = Guice.createInjector(new BeeLangRuntimeModule());
 
 		beeLangResourceSet = beeLangInjector.getProvider(XtextResourceSet.class).get();
 	}
 
 	public JUnitB3FileRunnerFactory(Class<?> klass) throws InitializationError {
 		definitionClass = klass;
 
 		Annotation[] testClassAnnotations = klass.getAnnotations();
 
 		for(Annotation annotation : testClassAnnotations) {
 			if(annotation instanceof B3Files) {
 				createB3FileRunners(((B3Files) annotation).value());
 				return;
 			}
 		}
 
 		throw new InitializationError("No @B3Files annotation specified for class " + klass.getName());
 	}
 
 	protected void createB3FileRunners(String[] b3Files) throws InitializationError {
 		ArrayList<Runner> runners = new ArrayList<Runner>(b3Files.length);
 		LinkedList<Throwable> errors = new LinkedList<Throwable>();
 
 		for(String b3File : b3Files) {
 			try {
 				runners.add(new JUnitB3FileRunner(b3File));
 			} catch(Throwable t) {
 				errors.add(t);
 			}
 		}
 
 		if(!errors.isEmpty())
 			throw new InitializationError(errors);
 
 		b3FileRunners = runners;
 	}
 
 	public List<Runner> getB3FileRunners() {
 		return b3FileRunners;
 	}
 
 }
