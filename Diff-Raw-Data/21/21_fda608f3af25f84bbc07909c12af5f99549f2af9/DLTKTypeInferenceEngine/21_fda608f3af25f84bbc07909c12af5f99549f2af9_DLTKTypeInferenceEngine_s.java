 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.ti;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExtension;
 import org.eclipse.core.runtime.IExtensionPoint;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.dltk.evaluation.types.UnknownType;
 import org.eclipse.dltk.ti.goals.AbstractTypeGoal;
 import org.eclipse.dltk.ti.types.IEvaluatedType;
 
 public class DLTKTypeInferenceEngine implements ITypeInferencer {
 	
 	private static final String TYPE_EVALUATORS = "org.eclipse.dltk.core.typeEvaluators";
 	private final static Map evaluatorsByNatures = new HashMap();
 	
 	static {		
 		IExtensionPoint extensionPoint = Platform.getExtensionRegistry()
 				.getExtensionPoint(TYPE_EVALUATORS);
 		IExtension[] ext = extensionPoint.getExtensions();
		ArrayList resolvers = new ArrayList();
 		for (int a = 0; a < ext.length; a++) {
 			IConfigurationElement[] elements = ext[a]
 					.getConfigurationElements();
 			IConfigurationElement myElement = elements[0];
 			try {
 				String nature = myElement.getAttribute("nature");
 				List list = (List) evaluatorsByNatures.get(nature);
 				if (list == null) {
 					list = new ArrayList ();
 					evaluatorsByNatures.put(nature, list);
 				}
 				ITypeInferencer resolver = (ITypeInferencer) myElement
 						.createExecutableExtension("evaluator");
				resolvers.add(resolver);
				list.add(resolver);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	public DLTKTypeInferenceEngine() {		
 	}
 
 	public IEvaluatedType evaluateType(AbstractTypeGoal goal, int time) {
 		String nature = goal.getContext().getLangNature();
 		List list = (List) evaluatorsByNatures.get(nature);
 		if (list != null) {
 			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				ITypeInferencer ti = (ITypeInferencer) iterator.next();
 				IEvaluatedType type = ti.evaluateType(goal, time);
 				if (type != null && !(type instanceof UnknownType)) //TODO
 					return type;
 			}
 		}
 		return null;
 	}
 
 }
