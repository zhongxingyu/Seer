 /*******************************************************************************
  * Copyright (c) 2009, Cloudsmith Inc.
  * The code, documentation and other materials contained herein have been
  * licensed under the Eclipse Public License - v 1.0 by the copyright holder
  * listed above, as the Initial Contributor under such license. The text of
  * such license is available at www.eclipse.org.
  ******************************************************************************/
 
 package org.eclipse.b3.beelang.junit;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Type;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.b3.BeeLangStandaloneSetup;
 import org.eclipse.b3.backend.core.B3Engine;
 import org.eclipse.b3.backend.evaluator.b3backend.B3JavaImport;
 import org.eclipse.b3.backend.evaluator.b3backend.BExecutionContext;
 import org.eclipse.b3.backend.evaluator.b3backend.BFunction;
 import org.eclipse.b3.backend.evaluator.typesystem.TypeUtils;
 import org.eclipse.b3.beeLang.BeeModel;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.resource.ContentHandler;
 import org.eclipse.emf.ecore.resource.Resource.Diagnostic;
 import org.eclipse.xtext.diagnostics.AbstractDiagnostic;
 import org.eclipse.xtext.resource.XtextResource;
 import org.eclipse.xtext.resource.XtextResourceSet;
 import org.junit.runner.Description;
 import org.junit.runner.Runner;
 import org.junit.runner.notification.Failure;
 import org.junit.runner.notification.RunNotifier;
 import org.junit.runners.ParentRunner;
 import org.junit.runners.model.InitializationError;
 import org.osgi.framework.BundleReference;
 
 import com.google.inject.Injector;
 
 /**
  * A factory class for building JUnit 4 runners capable of executing B3 language functions as JUnit tests. A separate
  * runner is built for each of the <code>{@link B3TestFiles}</code> defined for the class passed to the factory's
  * constructor. Each runner built by this factory executes all B3 functions from the B3 file it was built for which
  * names start with the prefix of "test" as separate JUnit tests.
  * 
  * @author michal.ruzicka@cloudsmith.com
  * 
  * @see B3TestFiles
  * @see JUnitB3TestRunner
  */
 class JUnitB3FileRunnerFactory {
 
 	public static final String TEST_FUNCTION_PREFIX = "test";
 
 	protected static final Object[] EMPTY_PARAMETER_ARRAY = new Object[] {};
 
 	protected static final Type[] EMPTY_TYPE_ARRAY = new Type[] {};
 
 	protected class JUnitB3FileRunner extends ParentRunner<JUnitB3FileRunner.TestFunctionDescriptor> {
 
 		protected class TestFunctionDescriptor {
 
 			protected String testFunctionName;
 
 			protected Description testDescription;
 
 			public TestFunctionDescriptor(String functionName) {
 				testFunctionName = functionName;
 				// We call Description.createSuiteDescription despite this is really no test suite. This is because the
 				// other Description.create*Description methods take a Class<?> argument which we can't provide since
 				// the tests are actually B3 functions not wrapped by any Java class.
 				testDescription = Description.createSuiteDescription(String.format("%s(%s)", functionName, b3FilePath));
 			}
 
 			public String getFunctionName() {
 				return testFunctionName;
 			}
 
 			public Description getDescription() {
 				return testDescription;
 			}
 
 		}
 
 		protected String b3FilePath;
 
 		protected ArrayList<TestFunctionDescriptor> testFunctionDescriptors;
 
 		protected B3Engine b3Engine;
 
 		public JUnitB3FileRunner(String b3File) throws Exception {
 			super(definitionClass);
 
 			b3FilePath = (b3File.charAt(0) != '/')
 					? '/' + b3File
 					: b3File;
 
 			initializeFunctionTests();
 		}
 
 		protected void initializeFunctionTests() throws Exception {
 			URI b3FileURI = URI.createPlatformPluginURI(containingBundleName + b3FilePath, true);
 			XtextResource resource = (XtextResource) beeLangResourceSet.createResource(b3FileURI,
 					ContentHandler.UNSPECIFIED_CONTENT_TYPE);
 
 			resource.load(null);
 
 			EList<Diagnostic> errors = resource.getErrors();
 			if(errors.size() > 0) {
 				ArrayList<Throwable> problems = new ArrayList<Throwable>(errors.size());
 
 				for(Diagnostic error : errors) {
 					try {
 						if(error instanceof AbstractDiagnostic)
 							throw new Exception("Error at line: " + error.getLine() + ": " + error.getMessage());
 						throw new Exception("Error at unspecified location: " + error.getMessage());
 					} catch(Throwable t) {
 						problems.add(t);
 					}
 				}
 
				throw new MultiProblemException("There were parse errors in the file", problems);
 			}
 
 			BeeModel beeModel = (BeeModel) resource.getParseResult().getRootASTElement();
 			BExecutionContext b3Context = (b3Engine = new B3Engine()).getContext();
 
 			// Define all imports as constants
 			for(Type type : beeModel.getImports()) {
 				if(type instanceof B3JavaImport) {
 					Class<?> klass = TypeUtils.getRaw(type);
 					b3Context.defineValue(((B3JavaImport) type).getName(), klass, klass);
 				}
 			}
 
 			testFunctionDescriptors = new ArrayList<TestFunctionDescriptor>();
 
 			// Define all functions and create descriptors of test functions
 			for(BFunction function : beeModel.getFunctions()) {
 				b3Context.defineFunction(function);
 
 				String functionName = function.getName();
 
 				if(functionName.length() > TEST_FUNCTION_PREFIX.length()
 						&& functionName.startsWith(TEST_FUNCTION_PREFIX) && function.getParameterTypes().length == 0)
 					testFunctionDescriptors.add(new TestFunctionDescriptor(function.getName()));
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
 			Description testDescription = child.getDescription();
 
 			notifier.fireTestStarted(testDescription);
 			try {
 				b3Engine.getContext().callFunction(child.getFunctionName(), EMPTY_PARAMETER_ARRAY, EMPTY_TYPE_ARRAY);
 			} catch(Throwable t) {
 				notifier.fireTestFailure(new Failure(testDescription, t));
 			} finally {
 				notifier.fireTestFinished(testDescription);
 			}
 		}
 
 		@Override
 		protected List<TestFunctionDescriptor> getChildren() {
 			return testFunctionDescriptors;
 		}
 
 	}
 
 	protected static class ErrorReportingRunner extends Runner {
 
 		private final Description testDescription;
 
 		private final Throwable error;
 
 		private String errorMessage;
 
 		public ErrorReportingRunner(Description description, Throwable t, String message) {
 			testDescription = description;
 			error = t;
 			errorMessage = message;
 		}
 
 		@Override
 		public Description getDescription() {
 			return testDescription;
 		}
 
 		@Override
 		public void run(RunNotifier notifier) {
 			Description description = getDescription();
 
 			notifier.fireTestStarted(description);
 			try {
 				throw new Exception(errorMessage, error);
 			} catch(Throwable t) {
 				notifier.fireTestFailure(new Failure(description, t));
 				Throwable cause = t.getCause();
 				if(cause != null && cause instanceof MultiProblemException) {
 					for(Throwable problem : ((MultiProblemException) cause).getProblems())
 						notifier.fireTestFailure(new Failure(description, problem));
 				}
 			} finally {
 				notifier.fireTestFinished(description);
 			}
 		}
 
 	}
 
 	protected XtextResourceSet beeLangResourceSet;
 
 	protected final Class<?> definitionClass;
 
 	protected final String containingBundleName;
 
 	protected List<Runner> b3FileRunners;
 
 	public JUnitB3FileRunnerFactory(Class<?> klass) throws InitializationError {
 		ClassLoader classLoader = klass.getClassLoader();
 
 		if(!(classLoader instanceof BundleReference))
 			throw new InitializationError("Failed to find out bundle containing class: " + klass.getName());
 
 		definitionClass = klass;
 		containingBundleName = ((BundleReference) classLoader).getBundle().getSymbolicName();
 
 		Annotation[] testClassAnnotations = klass.getAnnotations();
 
 		for(Annotation annotation : testClassAnnotations) {
 			if(annotation instanceof B3TestFiles) {
 				createB3FileRunners(((B3TestFiles) annotation).value());
 				return;
 			}
 		}
 
 		throw new InitializationError("No @" + B3TestFiles.class.getSimpleName() + " annotation specified for class: "
 				+ klass.getName());
 	}
 
 	protected void createResourceSet() {
 		Injector beeLangInjector = new BeeLangStandaloneSetup().createInjectorAndDoEMFRegistration();
 
 		beeLangResourceSet = beeLangInjector.getProvider(XtextResourceSet.class).get();
 	}
 
 	protected Runner createB3FileRunner(String b3File) throws Exception {
 		return new JUnitB3FileRunner(b3File);
 	}
 
 	protected Runner createErrorReportingRunner(String b3File, Throwable t) {
 		return new ErrorReportingRunner(Description.createSuiteDescription(b3File), t,
 				"Test initialization failed for: " + b3File);
 	}
 
 	protected void createB3FileRunners(String[] b3Files) {
 		createResourceSet();
 
 		ArrayList<Runner> runners = new ArrayList<Runner>(b3Files.length);
 
 		for(String b3File : b3Files) {
 			try {
 				runners.add(createB3FileRunner(b3File));
 			} catch(Throwable t) {
 				runners.add(createErrorReportingRunner(b3File, t));
 			}
 		}
 
 		b3FileRunners = runners;
 	}
 
 	public List<Runner> getB3FileRunners() {
 		return b3FileRunners;
 	}
 
 }
