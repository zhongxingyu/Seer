 /******************************************************************************* 
  * Copyright (c) 2007 Red Hat, Inc. 
  * Distributed under license by Red Hat, Inc. All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Red Hat, Inc. - initial API and implementation 
  ******************************************************************************/ 
 package org.jboss.tools.cdi.internal.core.impl;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.eclipse.jdt.core.IType;
 import org.jboss.tools.cdi.core.CDIConstants;
 import org.jboss.tools.cdi.core.CDIUtil;
 import org.jboss.tools.cdi.core.IInjectionPoint;
 import org.jboss.tools.cdi.core.IParameter;
 import org.jboss.tools.cdi.core.IProducerMethod;
 import org.jboss.tools.cdi.core.IScope;
 import org.jboss.tools.cdi.core.IScopeDeclaration;
 import org.jboss.tools.cdi.core.IStereotype;
 import org.jboss.tools.cdi.core.IStereotypeDeclaration;
 import org.jboss.tools.cdi.internal.core.impl.definition.MethodDefinition;
 import org.jboss.tools.cdi.internal.core.impl.definition.ParameterDefinition;
 import org.jboss.tools.common.java.IAnnotationDeclaration;
 import org.jboss.tools.common.java.IParametedType;
 import org.jboss.tools.common.java.ITypeDeclaration;
 import org.jboss.tools.common.java.ParametedType;
 import org.jboss.tools.common.text.ITextSourceReference;
 import org.jboss.tools.common.util.BeanUtil;
 
 /**
  * 
  * @author Viacheslav Kabanovich
  *
  */
 public class ProducerMethod extends BeanMethod implements IProducerMethod {
 	protected AnnotationDeclaration produces;
 
 	ProducerMethod specialized = null;
 	Set<ProducerMethod> specializingProducerMethods = new HashSet<ProducerMethod>();
 
 	public ProducerMethod() {}
 
 	@Override
 	public void setDefinition(MethodDefinition definition) {
 		super.setDefinition(definition);
 		produces = definition.getProducesAnnotation();
 	}
 
 	@Override
 	protected Parameter newParameter(ParameterDefinition p) {
 		return new InjectionPointParameter();
 	}
 
 	public Set<ITypeDeclaration> getAllTypeDeclarations() {
 		Set<ITypeDeclaration> result = new HashSet<ITypeDeclaration>();
 		if(typeDeclaration != null && typeDeclaration.getStartPosition() > 0) {
 			result.add(typeDeclaration);
 		}
 		return result;
 	}
 
 	public IAnnotationDeclaration getAlternativeDeclaration() {
 		return getDefinition().getAlternativeAnnotation();
 	}
 
 	public IType getBeanClass() {
 		return getClassBean().getBeanClass();
 	}
 
 	public Set<IInjectionPoint> getInjectionPoints() {
 		HashSet<IInjectionPoint> result = new HashSet<IInjectionPoint>();
 		for (IParameter p: parameters) {
 			if(p instanceof IInjectionPoint) {
 				result.add((IInjectionPoint)p);
 			}
 		}
 		return result;
 	}
 
 	public Set<IParametedType> getLegalTypes() {
 		Set<IParametedType> result = new HashSet<IParametedType>();
 		AnnotationDeclaration d = getDefinition().getTypedAnnotation();
 		Set<IParametedType> all = getAllTypes();
 		if(d != null) {
 			Set<ITypeDeclaration> ts = getRestrictedTypeDeclarations(all);
 			result.addAll(ts);
 			ParametedType object = getObjectType(getBeanClass());
 			if(object != null) {
 				result.add(object);
 			}
 			return result;
 		}
 		return all;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.jboss.tools.cdi.core.IBean#getAllTypes()
 	 */
 	public Set<IParametedType> getAllTypes() {
 		if(typeDeclaration != null) {
 			return typeDeclaration.getAllTypes();
 		}
 		return new HashSet<IParametedType>();
 	}
 
 	public Set<ITypeDeclaration> getRestrictedTypeDeclaratios() {
 		return getRestrictedTypeDeclarations(getAllTypes());
 	}
 
 	public String getName() {
 		ProducerMethod specialized = getSpecializedBean();
 		if(specialized != null) {
			return specialized.getName();
 		}
 
 		AnnotationDeclaration named = findNamedAnnotation();
 		if(named == null) return null;
 
 		Object value = named.getMemberValue(null);
 		if(value != null && value.toString().trim().length() > 0) {
 			return value.toString().trim();
 		}
 		String name = getMethod().getElementName();
 		if(BeanUtil.isGetter(getMethod())) {
 			return BeanUtil.getPropertyName(name);
 		}
 		return name;
 	}
 
 	public ITextSourceReference getNameLocation(boolean stereotypeLocation) {
 		return (stereotypeLocation) ? CDIUtil.getNamedDeclaration(this) : findNamedAnnotation();
 	}
 
 	public void setSpecializedBean(ProducerMethod other) {
 		specialized = other;
 		if(other != null) {
 			other.specializingProducerMethods.add(this);
 		}
 	}
 
 	public ProducerMethod getSpecializedBean() {
 		if(getDefinition().getSpecializesAnnotation() == null) {
 			return null;
 		}
 		return specialized;
 	}
 
 	public IAnnotationDeclaration getSpecializesAnnotationDeclaration() {
 		return getDefinition().getSpecializesAnnotation();
 	}
 
 	public boolean isDependent() {
 		IScope scope = getScope();
 		return scope != null && CDIConstants.DEPENDENT_ANNOTATION_TYPE_NAME.equals(scope.getSourceType().getFullyQualifiedName());
 	}
 
 	boolean hasEnabledSpecializingProducerMethod() {
 		for (ProducerMethod sb: specializingProducerMethods) {
 			if(sb.hasEnabledSpecializingProducerMethod() || sb.isEnabled()) return true;
 		}
 		return false;
 	}
 
 	public boolean isEnabled() {
 		if(classBean != null && !classBean.isEnabled()) {
 			return false;
 		}
 		if(hasEnabledSpecializingProducerMethod()) {
 			return false;
 		}
 		if(isAlternative()) {
 			if(classBean != null && !getCDIProject().getAlternatives(classBean.getBeanClass().getFullyQualifiedName()).isEmpty()) {
 				return true;
 			}
 			Set<IStereotypeDeclaration> ds = getStereotypeDeclarations();
 			for (IStereotypeDeclaration d: ds) {
 				IStereotype s = d.getStereotype();
 				if(s != null && s.isAlternative() && !getCDIProject().getAlternatives(s.getSourceType().getFullyQualifiedName()).isEmpty()) {
 					return true;
 				}
 			}
 			return false;
 		}
 		return true;
 	}
 
 	public boolean isSpecializing() {
 		return getDefinition().getSpecializesAnnotation() != null;
 	}
 
 	//same as ProducerField.getScope
 	public IScope getScope() {
 		Set<IScopeDeclaration> ds = getScopeDeclarations();
 		if(!ds.isEmpty()) {
 			return ds.iterator().next().getScope();
 		}
 		Set<IStereotypeDeclaration> ss = getStereotypeDeclarations();
 		Set<IScope> defaults = new HashSet<IScope>();
 		for (IStereotypeDeclaration d: ss) {
 			IStereotype s = d.getStereotype();
 			IScope sc = s.getScope();
 			if(sc != null) {
 				defaults.add(sc);
 			}
 		}
 		if(defaults.size() == 1) {
 			return defaults.iterator().next();
 		} else if(defaults.size() > 1) {
 			return null;
 		}
 		return getCDIProject().getScope(CDIConstants.DEPENDENT_ANNOTATION_TYPE_NAME);
 	}
 
 	public IAnnotationDeclaration getProducesAnnotation() {
 		return produces;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.jboss.tools.cdi.core.IBean#isSelectedAlternative()
 	 */
 	public boolean isSelectedAlternative() {
 		if(getCDIProject().isTypeAlternative(getBeanClass().getFullyQualifiedName())) {
 			return true;
 		}
 		Set<IStereotypeDeclaration> ds = getStereotypeDeclarations();
 		for (IStereotypeDeclaration d: ds) {
 			IStereotype s = d.getStereotype();
 			if(s != null && s.isAlternative() && 
 					getCDIProject().isStereotypeAlternative(s.getSourceType().getFullyQualifiedName())	) return true;
 		}
 // TODO how it can be selected in this case?
 //		if(getDefinition().getAlternativeAnnotation() == null) {
 //			return false;
 //		}
 		return false;
 	}
 }
