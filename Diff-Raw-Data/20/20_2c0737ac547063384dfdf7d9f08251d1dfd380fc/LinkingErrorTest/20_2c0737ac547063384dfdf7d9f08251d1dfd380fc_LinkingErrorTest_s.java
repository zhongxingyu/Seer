 /*******************************************************************************
  * Copyright (c) 2011 itemis AG (http://www.itemis.eu) and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *******************************************************************************/
 package org.eclipse.xtext.xtend2.tests.linking;
 
 import java.util.List;
 
 import org.eclipse.emf.common.util.BasicDiagnostic;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.Resource.Diagnostic;
 import org.eclipse.xtext.common.types.JvmTypeReference;
 import org.eclipse.xtext.common.types.JvmVoid;
 import org.eclipse.xtext.diagnostics.ExceptionDiagnostic;
 import org.eclipse.xtext.diagnostics.Severity;
 import org.eclipse.xtext.linking.lazy.LazyLinkingResource;
 import org.eclipse.xtext.resource.XtextResource;
 import org.eclipse.xtext.resource.XtextResourceSet;
 import org.eclipse.xtext.util.CancelIndicator;
 import org.eclipse.xtext.util.IAcceptor;
 import org.eclipse.xtext.util.StringInputStream;
 import org.eclipse.xtext.validation.CheckMode;
 import org.eclipse.xtext.validation.IDiagnosticConverter;
 import org.eclipse.xtext.validation.Issue;
 import org.eclipse.xtext.validation.ResourceValidatorImpl;
 import org.eclipse.xtext.xbase.typing.ITypeProvider;
 import org.eclipse.xtext.xtend2.tests.AbstractXtend2TestCase;
 import org.eclipse.xtext.xtend2.xtend2.XtendClass;
 import org.eclipse.xtext.xtend2.xtend2.XtendFile;
 import org.eclipse.xtext.xtend2.xtend2.XtendFunction;
 
 import com.google.inject.Inject;
 
 /**
  * @author Sebastian Zarnekow - Initial contribution and API
  */
 public class LinkingErrorTest extends AbstractXtend2TestCase {
 
 	@Inject
 	private ITypeProvider typeProvider;
 	
 	public void testNoException_01() throws Exception {
 		XtendFunction function = function("def noException() {\n" + 
 				// exception case is Integeri
 				"	    val closure = [Integeri| return i]\n" + 
 				"	    for (x : 1..100) closure.apply(x)\n" + 
 				"	}");
 		JvmTypeReference type = typeProvider.getTypeForIdentifiable(function);
 		assertTrue(type.getType() instanceof JvmVoid);
 		assertFalse(type.getType().eIsProxy());
 		assertNoExceptions(function);
 	}
 	
 	public void testNoException_02() throws Exception {
 		XtendFunction function = function("def noException() {\n" + 
 				// exception case is i
 				"	    val closure = [ i| return i]\n" + 
 				"	    for (x : 1..100) closure.apply(x)\n" + 
 				"	}");
 		JvmTypeReference type = typeProvider.getTypeForIdentifiable(function);
 		assertTrue(type.getType() instanceof JvmVoid);
 		assertFalse(type.getType().eIsProxy());
 		assertNoExceptions(function);
 	}
 	
 	public void testNoException_03() throws Exception {
 		XtendFile file = file("package org.eclipse.xtext.xtend2.tests.linking\n" + 
 				// error condition is empty class name
 				"class  {\n" + 
 				"\n" + 
 				"	aOrB(String a, String b) {\n" + 
 				"		if (a.isNullOrEmpty()) \n" + 
 				"			b\n" + 
 				"		else\n" + 
 				"			a \n" + 
 				"	}\n" + 
 				"	\n" + 
 				"	returnInIf() {\n" + 
 				"		if ('x'!='x') return 'xx' else return 'yy'\n" + 
 				"	}\n" + 
 				"\n" + 
 				"}");
 		assertNoExceptions(file);
 	}
 	
 	public void testNoException_04() throws Exception {
 		XtendFile file = file("package org.eclipse.xtext.xtend2.tests.linking\n" + 
 				"import java.util.ArrayList\n" + 
 				"import static java.util.Arrays.*\n" + 
 				"import static extension java.util.Collections.*\n" + 
 				"class NoException {\n" + 
 				// error condition is tring
 				"	@Inject extension tring\n" + 
 				"	boolean something(int i) {\n" + 
 				"	  i.indexOf() == 0" +
 				"	}\n" + 
 				"}");
 		assertNoExceptions(file);
 	}
 	
 	public void testNoException_05() throws Exception {
 		XtendFile file = file("package org.eclipse.xtext.xtend2.tests.linking\n" + 
 				"import java.util.ArrayList\n" + 
 				"import static java.util.Arrays.*\n" + 
 				"import static extension java.util.Collections.*\n" + 
 				// error condition is empty class name
 				"class  {\n" + 
 				"	@Inject\n" + 
 				"	ArrayList as myList\n" + 
 				"	@Inject extension\n" + 
 				"	String\n" + 
 				"	boolean something(int i) {\n" + 
 				"	  if (i.indexOf() == 0) {\n" + 
 				"	    return myList.contains(i)\n" + 
 				"	  } \n" + 
 				"	  asList(i)\n" + 
 				"	  i.singletonList()\n" + 
 				"	  false\n" + 
 				"	}\n" + 
 				"}");
 		assertNoExceptions(file);
 	}
 	
 	public void testNoException_06() throws Exception {
 		XtendFile file = file("package org.eclipse.xtext.xtend2.tests.linking\n" + 
 				"class NoException {\n" + 
 				"	String foo(String a,) {\n" + 
 				"		if (isUpper(a)) {\n" + 
 				"			another(a,b+'holla')\n" + 
 				"		}\n" + 
 				"	}\n" + 
 				"}");
 		assertNoExceptions(file);
 	}
 	
 	public void testNoException_07() throws Exception {
 		XtendFile file = file("package org.eclipse.xtext.xtend2.tests.linking\n" + 
 				"import\n" +
 				"import static java.util.Arrays.*\n" + 
 				"import static extension java.util.Collections.*\n" + 
 				"class NoException {\n" + 
 				"	@Inject\n" + 
 				"	ArrayList as myList\n" + 
 				"	@Inject extension String\n" + 
 				"	boolean something(int i) {\n" + 
 				"	  if (i.indexOf() == 0) {\n" + 
 				"	    return myList.contains(i)\n" + 
 				"	  } \n" + 
 				"	  asList(i)\n" + 
 				"	  i.singletonList()\n" + 
 				"	  false\n" + 
 				"	}\n" + 
 				"}");
 		assertNoExceptions(file);
 	}
 	
 	public void testNoException_08() throws Exception {
 		XtendFile file = file("package org.eclipse.xtext.xtend2.tests.linking\n" + 
 				"import java.util.ArrayList\n" + 
 				"import static.*\n" + 
 				"import static extension java.util.Collections.*\n" + 
 				"class NoException {\n" + 
 				"	@Inject\n" + 
 				"	ArrayList as myList\n" + 
 				"	@Inject extension String\n" + 
 				"	boolean something(int i) {\n" + 
 				"	  if (i.indexOf() == 0) {\n" + 
 				"	    return myList.contains(i)\n" + 
 				"	  } \n" + 
 				"	  asList(i)\n" + 
 				"	  i.singletonList()\n" + 
 				"	  false\n" + 
 				"	}\n" + 
 				"}");
 		assertNoExceptions(file);
 	}
 	
 	public void testNoException_09() throws Exception {
 		XtendFile file = file("package org.eclipse.xtext.xtend2.tests.linking\n" + 
 				"class NoException {\n" + 
 				"	String foo(String a, String b) {\n" + 
 				"		if (isUpper(a)) {\n" + 
 				"			another(a,b+'holla')\n" + 
 				"		} else {\n" + 
 				"			v");
 		assertNoExceptions(file);
 	}
 	
 	public void testBug343585() throws Exception {
 		XtendFile file = file("class Test extends Test {}");
 		assertNoExceptions(file);
 	}
 	
 	protected void assertNoExceptions(EObject object) {
 		Resource resource = object.eResource();
 		if (resource instanceof LazyLinkingResource)
 			((LazyLinkingResource) resource).resolveLazyCrossReferences(CancelIndicator.NullImpl);
 		List<Diagnostic> errors = object.eResource().getErrors();
 		for(Diagnostic error: errors) {
 			if (error instanceof ExceptionDiagnostic) {
 				((ExceptionDiagnostic) error).getException().printStackTrace();
 			}
 			assertFalse(error.toString(), error instanceof ExceptionDiagnostic);
 		}
 		validateWithoutException((XtextResource) resource);
 	}
 	
 	public void testNoExceptionInValidator_01() throws Exception {
 		XtendClass clazz = clazz("package pack class Case_2 {\n" + 
 				"\n" + 
 				"	aOrB(String a, String b) {\n" + 
 				"		if (a.isNullOrEmpty()) \n" + 
 				"			b\n" + 
 				"		else\n" + 
 				"			a \n" + 
 				"	}\n" + 
 				"	\n" + 
 				"	() {\n" + 
 				"		if ('x'!='x') return 'xx' else return 'yy'\n" + 
 				"	}\n" + 
 				"\n" + 
 				"}");
 		assertNoExceptions(clazz);
 		XtextResource resource = (XtextResource) clazz.eResource();
 		validateWithoutException(resource);
 	}
 	
 	public void testNoExceptionInValidator_02() throws Exception {
 		XtendClass clazz = clazz("package pack class Case_4 {\n" + 
 				"	richStrings_01() {\n" + 
 				"		'''foobar'''\n" + 
 				"	}\n" + 
 				"	richStrings_02() {\n" + 
 				"		''''start'\n" + 
 				"		  first line\n" + 
 				"");
 		assertNoExceptions(clazz);
 		XtextResource resource = (XtextResource) clazz.eResource();
 		validateWithoutException(resource);
 	}
 	
 	public void testNoExceptionInValidator_03() throws Exception {
 		XtendClass clazz = clazz("package pack class Case_4 {\n" + 
 				"	richStrings_01() {\n" + 
 				"		'''foobar'''\n" + 
 				"	}\n" + 
 				"	richStrings_02() {\n" + 
 				"		''''start'\n" + 
 				"		  first line\n'''" + 
 				"");
 		assertNoExceptions(clazz);
 		XtextResource resource = (XtextResource) clazz.eResource();
 		validateWithoutException(resource);
 	}
 
 	protected void validateWithoutException(XtextResource resource) {
 		ResourceValidatorImpl validator = new ResourceValidatorImpl();
 		assertNotSame(validator, resource.getResourceServiceProvider().getResourceValidator());
 		getInjector().injectMembers(validator);
 		validator.setDiagnosticConverter(new IDiagnosticConverter() {
 			public void convertValidatorDiagnostic(org.eclipse.emf.common.util.Diagnostic diagnostic, IAcceptor<Issue> acceptor) {
 				if (diagnostic instanceof BasicDiagnostic) {
 					List<?> data = diagnostic.getData();
 					if (!data.isEmpty() && data.get(0) instanceof Throwable) {
 						Throwable t = (Throwable) data.get(0);
 						// the framework catches runtime exception
 						// and AssertionError does not take a throwable as argument
 						throw new Error(t);
 					}
 				}
 			}
 			
 			public void convertResourceDiagnostic(Diagnostic diagnostic, Severity severity, IAcceptor<Issue> acceptor) {
 				if (diagnostic instanceof ExceptionDiagnostic) {
 					Exception e = ((ExceptionDiagnostic) diagnostic).getException();
 					// the framework catches runtime exception
 					// and AssertionError does not take a throwable as argument
 					throw new Error(new RuntimeException(e));
 				}
 			}
 		});
 		validator.validate(resource, CheckMode.ALL, CancelIndicator.NullImpl);
 	}
 	
 	@Override
 	protected XtendFile file(String string, boolean validate) throws Exception {
 		if (validate)
 			return super.file(string, validate);
 		XtextResourceSet set = get(XtextResourceSet.class);
 		String fileName = getFileName(string);
 		Resource resource = set.createResource(URI.createURI(fileName + ".xtend"));
 		resource.load(new StringInputStream(string), null);
 		XtendFile file = (XtendFile) resource.getContents().get(0);
 		return file;
 	}
 	
 }
