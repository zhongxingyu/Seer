 /**
  * <copyright>
  *
  * Copyright (c) 2012, 2013 E.D.Willink and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     E.D.Willink - initial API and implementation
  *
  * </copyright>
  */
 package org.eclipse.ocl.examples.pivot.manager;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.jdt.annotation.NonNull;
 import org.eclipse.jdt.annotation.Nullable;
 import org.eclipse.ocl.examples.domain.elements.DomainInheritance;
 import org.eclipse.ocl.examples.domain.elements.DomainOperation;
 import org.eclipse.ocl.examples.domain.ids.ParametersId;
 import org.eclipse.ocl.examples.domain.library.LibraryFeature;
 import org.eclipse.ocl.examples.pivot.Type;
 
 public class FinalAnalysis
 {
 	protected final @NonNull MetaModelManager metaModelManager;
 	private final @NonNull Map<DomainInheritance, Set<DomainInheritance>> type2subTypes = new HashMap<DomainInheritance, Set<DomainInheritance>>();
 	private final @NonNull Map<DomainOperation, Set<DomainOperation>> operation2overrides = new HashMap<DomainOperation, Set<DomainOperation>>();
 
 	public FinalAnalysis(@NonNull PackageManager packageManager) {
 		this.metaModelManager = packageManager.getMetaModelManager();
 		for (PackageServer packageServer :  packageManager.getAllPackages()) {
 			for (TypeServer typeServer :  packageServer.getMemberTypes()) {
 				Type subType = typeServer.getPivotType();
 				DomainInheritance subInheritance = subType.getInheritance(metaModelManager);
 				for (DomainInheritance superType : typeServer.getAllSuperClasses()) {
 					Set<DomainInheritance> subInheritances = type2subTypes.get(superType);
 					if (subInheritances == null) {
 						subInheritances = new HashSet<DomainInheritance>();
 						type2subTypes.put(superType, subInheritances);
 					}
 					subInheritances.add(subInheritance);
 				}
 			}
 		}
 		for (DomainInheritance domainInheritance : type2subTypes.keySet()) {
 			Set<DomainInheritance> subInheritances = type2subTypes.get(domainInheritance);
 			for (DomainOperation domainOperation : domainInheritance.getLocalOperations()) {
 				String opName = domainOperation.getName();
 				ParametersId parametersId = domainOperation.getParametersId();
 				LibraryFeature domainImplementation = domainOperation.getImplementation();
 				Set<DomainOperation> overrides = null;
 				for (DomainInheritance subInheritance : subInheritances) {
 					if (subInheritance != domainInheritance) {
 						for (DomainOperation subOperation : subInheritance.getLocalOperations()) {
 							if (opName.equals(subOperation.getName()) && parametersId.equals(subOperation.getParametersId())) {
 								LibraryFeature subImplementation = subOperation.getImplementation();
 								if (domainImplementation != subImplementation) {
 									if (overrides == null) {
 										overrides = new HashSet<DomainOperation>();
 										overrides.add(domainOperation);
 									}
 									overrides.add(subOperation);
 								}
 							}
 						}
 					}
 				}
 				operation2overrides.put(domainOperation, overrides);
 			}
 		}
 //		StringBuilder s = new StringBuilder();
 //		print(s);
 //		System.out.println(s);
 	}
 	
 	public boolean isFinal(@NonNull DomainInheritance domainInheritance) {
 		Set<DomainInheritance> subInheritances = type2subTypes.get(domainInheritance);
 		return subInheritances.size() <= 1;
 	}
 	
 	public boolean isFinal(@NonNull DomainOperation operation) {
 		Set<DomainOperation> overrides = operation2overrides.get(operation);
 		return overrides == null;
 	}
 	
 	public @Nullable DomainOperation isFinal(@NonNull DomainOperation operation, @NonNull DomainInheritance domainInheritance) {
 		Set<DomainOperation> overrides = operation2overrides.get(operation);
 		if (overrides == null) {
 			return operation;
 		}
 		DomainOperation candidate = null;
 		for (DomainOperation override : overrides) {
 			DomainInheritance overrideInheritance = override.getInheritance(metaModelManager);
			if ((overrideInheritance != null) && domainInheritance.conformsTo(metaModelManager, overrideInheritance)) {
 				if (candidate != null) {
 					return null;
 				}
 				candidate = override;
 			}
 		}
 		return candidate;
 	}
 	
 	public void print(@NonNull StringBuilder s) {
 		List<DomainInheritance> allInheritances = new ArrayList<DomainInheritance>(type2subTypes.keySet());
 		Collections.sort(allInheritances, new Comparator<DomainInheritance>()
 		{
 			public int compare(DomainInheritance o1, DomainInheritance o2) {
 				return o1.getName().compareTo(o2.getName());
 			}
 		});
 		s.append("Final types");
 		for (DomainInheritance anInheritance : allInheritances) {
 			assert anInheritance != null;
 			if (isFinal(anInheritance)) {
 				s.append("\n\t");
 				s.append(anInheritance.getName());
 			}
 		}
 		s.append("\nNon-final types");
 		for (DomainInheritance anInheritance : allInheritances) {
 			assert anInheritance != null;
 			if (!isFinal(anInheritance)) {
 				s.append("\n\t");
 				s.append(anInheritance.getName());
 			}
 		}	
 	}
 
 }
