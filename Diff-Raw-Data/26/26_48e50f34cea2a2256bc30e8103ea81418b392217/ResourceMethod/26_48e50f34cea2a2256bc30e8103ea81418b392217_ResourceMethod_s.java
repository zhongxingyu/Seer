 /******************************************************************************* 
  * Copyright (c) 2008 Red Hat, Inc. 
  * Distributed under license by Red Hat, Inc. All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Xavier Coulon - Initial API and implementation 
  ******************************************************************************/
 
 package org.jboss.tools.ws.jaxrs.core.metamodel;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jdt.core.IMethod;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.dom.CompilationUnit;
 import org.eclipse.jdt.core.dom.IMethodBinding;
 import org.eclipse.jdt.core.dom.ITypeBinding;
 import org.jboss.tools.ws.jaxrs.core.utils.JdtUtils;
 
 /**
  * @author xcoulon
  * 
  */
 // FIXME : Use protected vs public methods to retrieve resolved/internal
 // mappings ?
 
 public class ResourceMethod extends BaseElement<IMethod> {
 
 	private final Resource parentResource;
 
 	private final ResourceMethodMapping resourceMethodMapping;
 
 	private EnumKind kind = null;
 
 	/**
 	 * return type of the java method. Null if this is not a subresource
 	 * locator.
 	 */
 	private IType returnType = null;
 
 	/**
 	 * Full constructor.
 	 * 
 	 * @throws CoreException
 	 * @throws InvalidModelElementException
 	 */
 	public ResourceMethod(final IMethod javaMethod, final Resource parentResource, final Metamodel metamodel,
 			final IProgressMonitor progressMonitor) throws InvalidModelElementException, CoreException {
 		super(javaMethod, metamodel);
 		this.parentResource = parentResource;
 		this.resourceMethodMapping = new ResourceMethodMapping(this);
 		merge(javaMethod, progressMonitor);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	// FIXME : always returning at least MAPPING ??
 	public final Set<EnumElementChange> merge(final IMethod javaMethod, final IProgressMonitor progressMonitor)
 			throws InvalidModelElementException, CoreException {
 		CompilationUnit compilationUnit = getCompilationUnit(progressMonitor);
 		Set<EnumElementChange> changes = new HashSet<EnumElementChange>();
 
 		if (this.resourceMethodMapping.merge(compilationUnit)) {
 			changes.add(EnumElementChange.MAPPING);
 		}
 		/*
 		 * Set<IProblem> problems = JdtUtils.resolveErrors(javaMethod,
 		 * compilationUnit); if (problems != null && problems.size() > 0) { //
 		 * metamodel.reportErrors(javaMethod, problems); return; }
 		 */
 		HTTPMethod httpMethod = resourceMethodMapping.getHTTPMethod();
 		String uriPathTemplateFragment = resourceMethodMapping.getUriPathTemplateFragment();
 		EnumKind nextKind = null;
 		if (uriPathTemplateFragment == null && httpMethod != null) {
 			nextKind = EnumKind.RESOURCE_METHOD;
 		} else if (uriPathTemplateFragment != null && httpMethod != null) {
 			nextKind = EnumKind.SUBRESOURCE_METHOD;
 		} else if (uriPathTemplateFragment != null && httpMethod == null) {
 			nextKind = EnumKind.SUBRESOURCE_LOCATOR;
 		} else {
 			throw new InvalidModelElementException(
 					"ResourceMethod has no valid @Path annotation and no HTTP ResourceMethod annotation");
 		}
 		if (this.kind != nextKind) {
 			if (this.kind == EnumKind.SUBRESOURCE_LOCATOR || nextKind == EnumKind.SUBRESOURCE_LOCATOR) {
 				changes.add(EnumElementChange.KIND);
 			} else {
 				changes.add(EnumElementChange.MAPPING);
 			}
 			this.kind = nextKind;
 		}
		IMethodBinding methodBinding = JdtUtils.resolveMethodBinding(javaMethod, compilationUnit);

		ITypeBinding javaReturnType = methodBinding.getReturnType();
		IType nextReturnType = javaReturnType != null ? (IType) javaReturnType.getJavaElement() : null;
		if ((nextReturnType != null && !nextReturnType.equals(this.returnType))
				|| (this.returnType != null && !this.returnType.equals(nextReturnType))) {
			changes.add(EnumElementChange.KIND);
			this.returnType = nextReturnType;
 		}
 
 		return changes;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @throws CoreException
 	 */
 	@Override
 	public void validate(IProgressMonitor progressMonitor) throws CoreException {
 		getMapping().validate();
 	}
 
 	/**
 	 * Sets a flag of whether the underlying java method has compilation errors
 	 * or not. If true, also marke the parent resource with errors flag.
 	 * 
 	 * @param h
 	 *            : true if the java element has errors, false otherwise
 	 */
 	@Override
 	public void hasErrors(final boolean h) {
 		super.hasErrors(h);
 		if (hasErrors()) {
 			parentResource.hasErrors(true);
 		}
 	}
 
 	@Override
 	public final BaseElement.EnumKind getKind() {
 		return kind;
 	}
 
 	/**
 	 * @return the parentResource
 	 */
 	public final Resource getParentResource() {
 		return parentResource;
 	}
 
 	/**
 	 * @return the resourceMethodMapping
 	 */
 	public final ResourceMethodMapping getMapping() {
 		return resourceMethodMapping;
 	}
 
 	/**
 	 * @return the returnType
 	 */
 	public final IType getReturnType() {
 		return returnType;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public final String toString() {
 		return "ResourceMethod [" + parentResource.getName() + "." + getJavaElement().getElementName() + "] -> "
 				+ resourceMethodMapping.toString() + ", kind=" + kind + "]";
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		return getJavaElement().hashCode();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if (obj instanceof ResourceMethod) {
 			return getJavaElement().equals(((ResourceMethod) obj).getJavaElement());
 		}
 		return false;
 	}
 
 }
