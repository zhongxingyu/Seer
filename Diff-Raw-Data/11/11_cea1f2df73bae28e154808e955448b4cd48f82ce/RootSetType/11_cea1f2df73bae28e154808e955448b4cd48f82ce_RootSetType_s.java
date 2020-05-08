 /* ***** BEGIN LICENSE BLOCK *****
  * Version: MPL 1.1
  *
  * The contents of this file are subject to the Mozilla Public License Version
  * 1.1 (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  * http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
  * for the specific language governing rights and limitations under the
  * License.
  *
  * The Original Code is Gmodel.
  *
  * The Initial Developer of the Original Code is
  * Sofismo AG (Sofismo).
  * Portions created by the Initial Developer are
  * Copyright (C) 2009-2011 Sofismo AG.
  * All Rights Reserved.
  *
  * Contributor(s):
  * Andrew Shewring
  * ***** END LICENSE BLOCK ***** */
 
 package org.s23m.cell.eclipse.openarchitectureware.types.singleton;
 
 import java.util.Collections;
 
 import org.eclipse.internal.xtend.type.baseimpl.OperationImpl;
 import org.eclipse.xtend.expression.TypeSystem;
 import org.eclipse.xtend.typesystem.Feature;
 import org.eclipse.xtend.typesystem.Type;
 import org.s23m.cell.Identity;
 import org.s23m.cell.Set;
 import org.s23m.cell.eclipse.openarchitectureware.types.AbstractSetType;
 import org.s23m.cell.eclipse.openarchitectureware.types.OrderedSetDecorator;
 
 /**
  * Only used when creating a {@link Type} corresponding to the root {@link Set} type
  */
 public final class RootSetType extends AbstractSetType {
 
 	// Unfortunately the name of the type cannot end in "Set" due to a bug in XpandParser
 	private static final String FULLY_QUALIFIED_NAME = createFullyQualifiedName(Set.class) + "Type";
 
 	private final IdentityType identityType;
 
 	/**
 	 * Only used when creating a {@link Type} corresponding to
 	 * the root {@link Set} type
 	 */
 	public RootSetType(final TypeSystem typeSystem, final IdentityType identityType) {
 		super(typeSystem, FULLY_QUALIFIED_NAME, true, Collections.singleton(typeSystem.getObjectType()));
 		this.identityType = identityType;
 	}
 
 	@Override
 	public Feature[] getContributedFeatures() {
 		final Type booleanType = getTypeSystem().getBooleanType();
 		final Type integerType = getTypeSystem().getIntegerType();
 		return new Feature[] {
 			new OperationImpl(this, "identity", identityType) {
 				protected @Override Identity evaluateInternal(final Object target, final Object[] params) {
 					return ((Set) target).identity();
 				}
 			},
 
 			/* Zero-parameter operations */
 			new ZeroParameterSetOperation("flavor") {
 				public @Override Set evaluate(final Set target) {
 					return target.flavor();
 				}
 			},
 			new ZeroParameterSetOperation("variables") {
 				public @Override Set evaluate(final Set target) {
 					return target.variables();
 				}
 			},
 			new ZeroParameterSetOperation("values") {
 				public @Override Set evaluate(final Set target) {
 					return target.values();
 				}
 			},
 			new ZeroParameterSetOperation("container") {
 				public @Override Set evaluate(final Set target) {
 					return target.container();
 				}
 			},
 			new ZeroParameterSetOperation("category") {
 				public @Override Set evaluate(final Set target) {
 					return target.category();
 				}
 			},
 			new ZeroParameterSetOperation("filterLinks") {
 				public @Override Set evaluate(final Set target) {
 					return target.filterLinks();
 				}
 			},
 			new ZeroParameterSetOperation("filterInstances") {
 				public @Override Set evaluate(final Set target) {
 					return target.filterInstances();
 				}
 			},
 			//new ZeroParameterSetOperation("connectedInstances") {
 			//	public @Override Set evaluate(final Set target) {
 			//		return target.connectedInstances();
 			//	}
 			//},
			new ZeroParameterSetOperation("from") {
 				public @Override Set evaluate(final Set target) {
					return target.from();
 				}
 			},
			new ZeroParameterSetOperation("to") {
 				public @Override Set evaluate(final Set target) {
					return target.to();
 				}
 			},
 			new ZeroParameterSetOperation("from") {
 				public @Override Set evaluate(final Set target) {
 					return target.from();
 				}
 			},
 			new ZeroParameterSetOperation("to") {
 				public @Override Set evaluate(final Set target) {
 					return target.to();
 				}
 			},
 			new ZeroParameterSetOperation("isExternal") {
 				public @Override Set evaluate(final Set target) {
 					return target.isExternal();
 				}
 			},
 
 			/* One-parameter operations */
 			// TODO prefix with "to"?
 			new OneParameterSetOperation("filter") {
 				public @Override Set evaluate(final Set target, final Set parameter) {
 					return target.filter(parameter);
 				}
 			},
 			// TODO prefix with "to"?
 			new OneParameterSetOperation("filterFlavor") {
 				public @Override Set evaluate(final Set target, final Set parameter) {
 					return new OrderedSetDecorator(target.filterFlavor(parameter));
 				}
 			},
 			// TODO prefix with "to"?
 			new OneParameterSetOperation("localSuperSetOf") {
 				public @Override Set evaluate(final Set target, final Set parameter) {
 					return target.directSuperSetOf(parameter);
 				}
 			},
 			// TODO prefix with "to"?
 			new OneParameterSetOperation("localRootSuperSetOf") {
 				public @Override Set evaluate(final Set target, final Set parameter) {
 					return target.localRootSuperSetOf(parameter);
 				}
 			},
 			new OneParameterSetOperation("isLocalSuperSetOf") {
 				public @Override Set evaluate(final Set target, final Set parameter) {
 					return target.isLocalSuperSetOf(parameter);
 				}
 			},
 			new OneParameterSetOperation("containsEdgeFromOrTo") {
 				public @Override Set evaluate(final Set target, final Set parameter) {
 					return target.containsEdgeTo(parameter);
 				}
 			},
 			new OneParameterSetOperation("visibleArtifactsForSubGraph") {
 				public @Override Set evaluate(final Set target, final Set parameter) {
 					return target.visibleArtifactsForSubGraph(parameter);
 				}
 			},
 			new OneParameterSetOperation("hasVisibilityOf") {
 				public @Override Set evaluate(final Set target, final Set parameter) {
 					return target.hasVisibilityOf(parameter);
 				}
 			},
 			//new OneParameterSetOperation("containerCategory") {
 			//	public @Override Set evaluate(final Set target, final Set parameter) {
 			//		return target.metaVisibilityOf(parameter);
 			//	}
 			//},
 
 			/* InstanceList operations */
 			new OperationImpl(this, "contains", booleanType, this) {
 				protected @Override Boolean evaluateInternal(final Object target, final Object[] params) {
 					final Set set = (Set) target;
 					final Set parameter = (Set) params[0];
 					return set.containsSemanticMatch(parameter);
 				}
 			},
 			new OperationImpl(this, "containsAll", booleanType, this) {
 				protected @Override Boolean evaluateInternal(final Object target, final Object[] params) {
 					final Set set = (Set) target;
 					final Set parameter = (Set) params[0];
 					return set.containsSemanticMatchesForAll(parameter);
 				}
 			},
 			new OperationImpl(this, "isEmpty", booleanType) {
 				protected @Override Boolean evaluateInternal(final Object target, final Object[] params) {
 					final Set set = (Set) target;
 					return set.isEmpty();
 				}
 			},
 
 			new OperationImpl(this, "size", integerType) {
 				protected @Override Integer evaluateInternal(final Object target, final Object[] params) {
 					final Set set = (Set) target;
 					return set.size();
 				}
 			}
 		};
 	}
 
 }
