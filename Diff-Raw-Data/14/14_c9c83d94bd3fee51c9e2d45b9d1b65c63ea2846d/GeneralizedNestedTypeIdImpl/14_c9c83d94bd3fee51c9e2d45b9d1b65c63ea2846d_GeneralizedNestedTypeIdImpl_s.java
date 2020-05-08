 /**
  * <copyright>
  *
  * Copyright (c) 2012 E.D.Willink and others.
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
 package org.eclipse.ocl.examples.domain.ids.impl;
 
 import org.eclipse.jdt.annotation.NonNull;
 import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.ocl.examples.domain.ids.CollectionTypeId;
 import org.eclipse.ocl.examples.domain.ids.EnumerationLiteralId;
 import org.eclipse.ocl.examples.domain.ids.IdVisitor;
 import org.eclipse.ocl.examples.domain.ids.NestedTypeId;
 import org.eclipse.ocl.examples.domain.ids.NsURIPackageId;
 import org.eclipse.ocl.examples.domain.ids.PackageId;
 import org.eclipse.ocl.examples.domain.ids.TemplateBindings;
 import org.eclipse.ocl.examples.domain.ids.TemplateParameterId;
 import org.eclipse.ocl.examples.domain.ids.TemplateableTypeId;
 import org.eclipse.ocl.examples.domain.ids.TypeId;
 
 public class GeneralizedNestedTypeIdImpl extends GeneralizedTypeIdImpl<TemplateableTypeId> implements NestedTypeId,TemplateableTypeId
 {
 	/**
 	 * Map from a nested type name to the corresponding EnumerationLiteralId. 
 	 */
 	private @Nullable WeakHashMapOfWeakReference<String, EnumerationLiteralId> memberEnumerationLiterals = null;
 
 	protected final @NonNull PackageId parent;
 
 	GeneralizedNestedTypeIdImpl(@NonNull PackageId parent, @NonNull TemplateParameterId[] templateParameters, @NonNull String name) {
 		super(97 * parent.hashCode() + name.hashCode(), templateParameters, name);
 		this.parent = parent;
		if (name.equals("OclElement")) {
			System.out.println("got it");
		}
 	}
 
 	public @Nullable <R> R accept(@NonNull IdVisitor<R> visitor) {
 		return visitor.visitNestedTypeId(this);
 	}
 
 	@Override
 	protected @NonNull TemplateableTypeId createSpecializedId(@NonNull TemplateBindings templateBindings) {
 		return new SpecializedTypeIdImpl(this, templateBindings);
 	}
 
 	public @NonNull String getDisplayName() {
 		if (parent instanceof NsURIPackageId) {
 			return name;
 		}
 		else {
 			return parent + "::" + name;
 		}
 	}
 
 	@Override
 	public @NonNull EnumerationLiteralId getEnumerationLiteralId(@NonNull String name) {
     	WeakHashMapOfWeakReference<String, EnumerationLiteralId> memberEnumerationLiterals2 = memberEnumerationLiterals;
 		if (memberEnumerationLiterals2 == null) {
     		synchronized (this) {
     			memberEnumerationLiterals2 = memberEnumerationLiterals;
     	    	if (memberEnumerationLiterals2 == null) {
     	    		memberEnumerationLiterals = memberEnumerationLiterals2 = new WeakHashMapOfWeakReference<String, EnumerationLiteralId>()
     				{
 						@Override
 						protected @NonNull EnumerationLiteralId newTypeId(@NonNull String name) {
 							return new EnumerationLiteralIdImpl(GeneralizedNestedTypeIdImpl.this, name);
 						}
 					};
 	    	   }
     		}
     	}
 		return memberEnumerationLiterals2.getElementId(name);
 	}
 
 	public @NonNull TemplateableTypeId getGeneralizedId() {
 		return this;
 	}
 
 	public @NonNull String getMetaTypeName() {
 		return TypeId.CLASS_NAME;
 	}
 	
 	public @NonNull PackageId getParent() {
 		return parent;
 	}
 
     public @NonNull TemplateableTypeId specialize(@NonNull TemplateBindings templateBindings) {
     	return createSpecializedId(templateBindings);
 	}
 	
 	@Override
 	public String toString() {
 		return parent + "::" + name;
 	}
 
 }
