 /**
  * <copyright>
  *
  * Copyright (c) 2011 E.D.Willink and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     E.D.Willink - initial API and implementation
  *
  * </copyright>
  *
  * $Id$
  */
 package org.eclipse.ocl.examples.codegen.common;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class EmitQueries
 {
 	/**
 	 * The known classes that templatres may use in unqualified form. The list is here
 	 * in a Java form to reduce the impact of refactoring on Acceleo templates.
 	 */
 	private static final Class<?>[] knownClasses = {
 		org.eclipse.ocl.examples.domain.elements.DomainClassifierType.class,
 		org.eclipse.ocl.examples.domain.elements.DomainCollectionType.class,
 		org.eclipse.ocl.examples.domain.elements.DomainProperty.class,
 		org.eclipse.ocl.examples.domain.elements.DomainStandardLibrary.class,
 		org.eclipse.ocl.examples.domain.elements.DomainTupleType.class,
 		org.eclipse.ocl.examples.domain.elements.DomainType.class,
 		org.eclipse.ocl.examples.domain.elements.DomainTypedElement.class,
 		org.eclipse.ocl.examples.domain.evaluation.DomainEvaluator.class,
 		org.eclipse.ocl.examples.domain.evaluation.InvalidValueException.class,
 		org.eclipse.ocl.examples.domain.library.AbstractBinaryOperation.class,
 		org.eclipse.ocl.examples.domain.library.AbstractProperty.class,
		org.eclipse.ocl.examples.domain.library.AbstractTernaryOperation.class,
 		org.eclipse.ocl.examples.domain.library.AbstractUnaryOperation.class,
 		org.eclipse.ocl.examples.domain.library.LibraryBinaryOperation.class,
 		org.eclipse.ocl.examples.domain.library.LibraryIteration.class,
 		org.eclipse.ocl.examples.domain.library.LibraryProperty.class,
		org.eclipse.ocl.examples.domain.library.LibraryTernaryOperation.class,
 		org.eclipse.ocl.examples.domain.library.LibraryUnaryOperation.class,
 		org.eclipse.ocl.examples.domain.messages.EvaluatorMessages.class,
 		org.eclipse.ocl.examples.domain.values.BagValue.class,
 		org.eclipse.ocl.examples.domain.values.BooleanValue.class,
 		org.eclipse.ocl.examples.domain.values.CollectionValue.class,
 		org.eclipse.ocl.examples.domain.values.ElementValue.class,
 		org.eclipse.ocl.examples.domain.values.IntegerRange.class,
 		org.eclipse.ocl.examples.domain.values.IntegerValue.class,
 		org.eclipse.ocl.examples.domain.values.InvalidValue.class,
 		org.eclipse.ocl.examples.domain.values.NullValue.class,
 		org.eclipse.ocl.examples.domain.values.ObjectValue.class,
 		org.eclipse.ocl.examples.domain.values.OrderedSetValue.class,
 		org.eclipse.ocl.examples.domain.values.RealValue.class,
 		org.eclipse.ocl.examples.domain.values.SequenceValue.class,
 		org.eclipse.ocl.examples.domain.values.SetValue.class,
 		org.eclipse.ocl.examples.domain.values.StringValue.class,
 		org.eclipse.ocl.examples.domain.values.TupleValue.class,
 		org.eclipse.ocl.examples.domain.values.UnlimitedValue.class,
 		org.eclipse.ocl.examples.domain.values.Value.class,
 		org.eclipse.ocl.examples.domain.values.ValueFactory.class,
 		org.eclipse.ocl.examples.library.ecore.EcoreExecutorEnumeration.class,
 		org.eclipse.ocl.examples.library.ecore.EcoreExecutorEnumerationLiteral.class,
 		org.eclipse.ocl.examples.library.ecore.EcoreExecutorManager.class,
 		org.eclipse.ocl.examples.library.ecore.EcoreExecutorPackage.class,
 		org.eclipse.ocl.examples.library.ecore.EcoreExecutorType.class,
 		org.eclipse.ocl.examples.library.executor.ExecutorDoubleIterationManager.class,
 		org.eclipse.ocl.examples.library.executor.ExecutorFragment.class,
 		org.eclipse.ocl.examples.library.executor.ExecutorLambdaType.class,
 		org.eclipse.ocl.examples.library.executor.ExecutorOperation.class,
 		org.eclipse.ocl.examples.library.executor.ExecutorProperty.class,
 		org.eclipse.ocl.examples.library.executor.ExecutorSingleIterationManager.class,
 		org.eclipse.ocl.examples.library.executor.ExecutorSpecializedType.class,
 		org.eclipse.ocl.examples.library.executor.ExecutorStandardLibrary.class,
 		org.eclipse.ocl.examples.library.executor.ExecutorType.class,
 		org.eclipse.ocl.examples.library.executor.ExecutorTypeParameter.class
 	};
 
 	protected Map<String, String> computeKnown2ExternalMap(String knownImports) {
 		Map<String, String> known2external = new HashMap<String, String>();
 		for (String knownClass : knownImports.split("\\n")) {
 			String trimmed = knownClass.trim();
 			if (trimmed.length() > 0) {
 				String lastSegment = trimmed.substring(trimmed.lastIndexOf(".")+1);
 				known2external.put(lastSegment, trimmed);
 				known2external.put(trimmed, trimmed);
 			}
 		}
 		return known2external;
 	}	
 
 	public String debug(Object element) {
 		return null;
 	}	
 	
 	/**
 	 * Replace all embedded <%xxx%> embedded import paths using unqualified names
 	 * for knownImports by fully qualified names so that the return value may be
 	 * correctly processed by the GenModel ImportManager.
 	 * prefix the return with correspondinbg Java import declarations.
 	 */
 	public String expandKnownImports(String knownImports, String markedUpDocument) {
 		Map<String, String> known2external = computeKnown2ExternalMap(knownImports);
 		String[] splits = markedUpDocument.split("(\\<%)|(%\\>)");	
 		StringBuilder s = new StringBuilder();
 		for (int i = 0; i < splits.length; i += 2) {
 			s.append(splits[i]);
 			if (i+1 < splits.length) {
 				String candidate = splits[i+1].trim();
 				String knownImport = known2external.get(candidate);
 				s.append("<%");
 				s.append(knownImport != null ? knownImport : candidate);
 				s.append("%>");
 			}
 		}		
 		return s.toString();
 	}
 	
 	public String knownImports() {
 		StringBuilder s = new StringBuilder();
 		for (Class<?> knownClass : knownClasses) {
 			s.append(knownClass.getName());
 			s.append("\n");
 		}
 		return s.toString();
 	}
 	
 	/**
 	 * Replace all embedded <%xxx%> embedded import paths by shorter names and
 	 * prefix the return with correspondinbg Java import declarations.
 	 */
 	public String prefixImports(String knownImports, String markedUpDocument) {
 		/*
 		 * Map of known short internal name to external name.
 		 */
 		Map<String, String> known2external = computeKnown2ExternalMap(knownImports);
 		String[] splits = markedUpDocument.split("(\\<%)|(%\\>)");	
 		/*
 		 * Map of full external name to short internal name. The short internal name is the full name if
 		 * there is any ambiguity.
 		 */
 		Map<String, String> external2internal = new HashMap<String, String>();
 		/*
 		 * Map of short internal name to full external name or null if there is an ambiguity.
 		 */
 		Map<String, String> internal2external = new HashMap<String, String>();
 		
 		for (int i = 1; i < splits.length; i += 2) {
 			external2internal.put(splits[i].trim(), null);
 		}
 		
 		ArrayList<String> candidates = new ArrayList<String>(external2internal.keySet());
 		for (String candidate : candidates) {
 			String lastSegment = candidate.substring(candidate.lastIndexOf(".")+1);
 			String knownClass = known2external.get(candidate);
 			if (knownClass != null) {
 				if (knownClass.equals(candidate) || lastSegment.equals(candidate)) {
 					internal2external.put(lastSegment, knownClass);
 					external2internal.put(knownClass, lastSegment);
 					external2internal.put(lastSegment, lastSegment);
 				}
 				else {
 					external2internal.put(candidate, candidate);
 				}
 			}
 			else {
 				if (!internal2external.containsKey(lastSegment)) {
 					internal2external.put(lastSegment, candidate);
 					external2internal.put(candidate, lastSegment);
 				}
 				else {
 					String oldExternal = internal2external.get(lastSegment);
 					if (oldExternal != null) {
 						external2internal.put(oldExternal, oldExternal);
 						internal2external.put(lastSegment, null);
 					}
 					external2internal.put(candidate, candidate);
 				}
 			}
 		}
 		
 		List<String> allValues = new ArrayList<String>(internal2external.values());
 		allValues.remove(null);
 		Collections.sort(allValues);
 		StringBuilder s = new StringBuilder();
 		for (String externalPath : allValues) {
 			s.append("import ");
 			s.append(externalPath);
 			s.append(";\n");
 		}
 		s.append("\n");
 		for (int i = 0; i < splits.length; i += 2) {
 			s.append(splits[i]);
 			if (i+1 < splits.length) {
 				String candidate = splits[i+1].trim();
 				s.append(external2internal.get(candidate));
 			}
 		}		
 		return s.toString();
 	}
 }
