 /*******************************************************************************
  * Copyright (c) 2006-2010
  * Software Technology Group, Dresden University of Technology
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * Software Technology Group - TU Dresden, Germany
  *      - initial API and implementation
  ******************************************************************************/
 package org.reuseware.coconut.fracol.resource.fracol.analysis.helper;
 
 import java.util.Map;
 
 import org.eclipse.emf.ecore.EReference;
 import org.reuseware.coconut.fracol.CompositionAssociation;
 import org.reuseware.coconut.fracol.FragmentRole;
 import org.reuseware.coconut.fracol.PortType;
 import org.reuseware.coconut.fracol.resource.fracol.IFracolReferenceResolveResult;
 import org.reuseware.coconut.fracol.resource.fracol.IFracolReferenceResolver;
 
 /**
  * Reference resolver that looks for a port type a
  * fragment association is referring to. The lookup is 
  * done inside the current fracol definition using the 
  * name of the port type.
  */
 public abstract class CompositionAssociationEndReferenceResolver implements IFracolReferenceResolver<CompositionAssociation, PortType> {
 	
 	/**
 	 * @param element the port type
 	 * @param container the composition association
 	 * @param reference reference The reference that points at the port type
 	 * 
 	 * @return name of the port type
 	 */
 	public String deResolve(PortType element, CompositionAssociation container, EReference reference) {
 		return element.getName();
 	}
 
 	/**
 	 * Attempts to find the port type with the given name.
 	 * 
 	 * @param identifier The name for the port type.
 	 * @param container The composition association that contains the reference.
 	 * @param reference The reference that points to the target of the reference.
 	 * @param position The index of the reference (if it has an upper bound greater
 	 * than 1).
 	 * @param resolveFuzzy return objects that do not match exactly
 	 * @param result an object that can be sued to store the result of the resolve
 	 * operation.
 	 */
 	public void resolve(String identifier, CompositionAssociation container, EReference reference, int position, boolean resolveFuzzy, IFracolReferenceResolveResult<PortType> result) {
 		result.setErrorMessage("Port type '" + identifier + "' not declared");
 		
 		FragmentRole role = getRole(container);
 		if (role == null || role.eIsProxy()) {
 			return;
 		}
 		
 		for (PortType portType : role.getPortTypes()) {
			if (resolveFuzzy) {
				if (portType.getName().startsWith(identifier)) {
					result.addMapping(portType.getName(), portType);
				}
			} else {
				if (portType.getName().equals(identifier)) {
					result.addMapping(identifier, portType);
 				}
 			}
 		}
 	}
 	
 	protected abstract FragmentRole getRole(CompositionAssociation container);
 	
 	/**
 	 * @param options not supported
 	 */
 	public void setOptions(Map<?, ?> options) { }
 }
