 /*
  * (c) Copyright 2010-2011 AgileBirds
  *
  * This file is part of OpenFlexo.
  *
  * OpenFlexo is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * OpenFlexo is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with OpenFlexo. If not, see <http://www.gnu.org/licenses/>.
  *
  */
 package org.openflexo.foundation.viewpoint;
 
 import java.lang.reflect.Type;
 import java.util.Vector;
 import java.util.logging.Logger;
 
 import org.openflexo.antar.binding.DataBinding;
 import org.openflexo.foundation.ontology.IFlexoOntologyClass;
 import org.openflexo.foundation.ontology.SubClassOfClass;
 import org.openflexo.foundation.technologyadapter.FlexoMetaModel;
 import org.openflexo.foundation.technologyadapter.FlexoModel;
 import org.openflexo.foundation.validation.FixProposal;
 import org.openflexo.foundation.validation.ValidationError;
 import org.openflexo.foundation.validation.ValidationIssue;
 import org.openflexo.foundation.validation.ValidationRule;
 import org.openflexo.foundation.view.action.EditionSchemeAction;
 import org.openflexo.foundation.viewpoint.VirtualModel.VirtualModelBuilder;
 import org.openflexo.toolbox.StringUtils;
 
 public abstract class AddClass<M extends FlexoModel<M, MM>, MM extends FlexoMetaModel<MM>, T extends IFlexoOntologyClass> extends
 		AddConcept<M, MM, T> {
 
 	private static final Logger logger = Logger.getLogger(AddClass.class.getPackage().getName());
 
 	private String ontologyClassURI = null;
 
 	private DataBinding<String> className;
 
 	public AddClass(VirtualModel.VirtualModelBuilder builder) {
 		super(builder);
 	}
 
 	@Override
 	public EditionActionType getEditionActionType() {
 		return EditionActionType.AddClass;
 	}
 
 	/*@Override
 	public List<ClassPatternRole> getAvailablePatternRoles() {
 		return getEditionPattern().getPatternRoles(ClassPatternRole.class);
 	}*/
 
 	@Override
 	public ClassPatternRole getPatternRole() {
 		PatternRole superPatternRole = super.getPatternRole();
 		if (superPatternRole instanceof ClassPatternRole) {
 			return (ClassPatternRole) superPatternRole;
 		} else if (superPatternRole != null) {
 			// logger.warning("Unexpected pattern role of type " + superPatternRole.getClass().getSimpleName());
 			return null;
 		}
 		return null;
 	}
 
 	@Override
 	public IFlexoOntologyClass getOntologyClass() {
 		if (StringUtils.isNotEmpty(ontologyClassURI)) {
 			return getVirtualModel().getOntologyClass(ontologyClassURI);
 		} else {
 			if (getPatternRole() instanceof ClassPatternRole) {
 				return getPatternRole().getOntologicType();
 			}
 		}
 		return null;
 	}
 
 	public abstract Class<T> getOntologyClassClass();
 
 	@Override
 	public void setOntologyClass(IFlexoOntologyClass ontologyClass) {
 		if (ontologyClass != null) {
 			if (getPatternRole() instanceof ClassPatternRole) {
 				if (getPatternRole().getOntologicType().isSuperConceptOf(ontologyClass)) {
 					ontologyClassURI = ontologyClass.getURI();
 				} else {
 					getPatternRole().setOntologicType(ontologyClass);
 				}
 			} else {
 				ontologyClassURI = ontologyClass.getURI();
 			}
 		} else {
 			ontologyClassURI = null;
 		}
 	}
 
	public String _getOntologyClassURI() {
 		if (getOntologyClass() != null) {
 			if (getPatternRole() instanceof ClassPatternRole && getPatternRole().getOntologicType() == getOntologyClass()) {
 				// No need to store an overriding type, just use default provided by pattern role
 				return null;
 			}
 			return getOntologyClass().getURI();
 		}
 		return ontologyClassURI;
 	}
 
 	public void _setOntologyClassURI(String ontologyClassURI) {
 		this.ontologyClassURI = ontologyClassURI;
 	}
 
 	public DataBinding<String> getClassName() {
 		if (className == null) {
 			className = new DataBinding<String>(this, String.class, DataBinding.BindingDefinitionType.GET);
 			className.setBindingName("className");
 		}
 		return className;
 	}
 
 	public void setClassName(DataBinding<String> className) {
 		if (className != null) {
 			className.setOwner(this);
 			className.setDeclaredType(String.class);
 			className.setBindingDefinitionType(DataBinding.BindingDefinitionType.GET);
 			className.setBindingName("className");
 		}
 		this.className = className;
 	}
 
 	@Override
 	public Type getAssignableType() {
 		if (getOntologyClass() == null) {
 			return IFlexoOntologyClass.class;
 		}
 		return SubClassOfClass.getSubClassOfClass(getOntologyClass());
 	}
 
 	public static class AddClassActionMustDefineAnOntologyClass extends ValidationRule<AddClassActionMustDefineAnOntologyClass, AddClass> {
 		public AddClassActionMustDefineAnOntologyClass() {
 			super(AddClass.class, "add_individual_action_must_define_an_ontology_class");
 		}
 
 		@Override
 		public ValidationIssue<AddClassActionMustDefineAnOntologyClass, AddClass> applyValidation(AddClass action) {
 			if (action.getOntologyClass() == null) {
 				Vector<FixProposal<AddClassActionMustDefineAnOntologyClass, AddClass>> v = new Vector<FixProposal<AddClassActionMustDefineAnOntologyClass, AddClass>>();
 				for (ClassPatternRole pr : action.getEditionPattern().getClassPatternRoles()) {
 					v.add(new SetsPatternRole(pr));
 				}
 				return new ValidationError<AddClassActionMustDefineAnOntologyClass, AddClass>(this, action,
 						"add_individual_action_does_not_define_any_ontology_class", v);
 			}
 			return null;
 		}
 
 		protected static class SetsPatternRole extends FixProposal<AddClassActionMustDefineAnOntologyClass, AddClass> {
 
 			private ClassPatternRole patternRole;
 
 			public SetsPatternRole(ClassPatternRole patternRole) {
 				super("assign_action_to_pattern_role_($patternRole.patternRoleName)");
 				this.patternRole = patternRole;
 			}
 
 			public ClassPatternRole getPatternRole() {
 				return patternRole;
 			}
 
 			@Override
 			protected void fixAction() {
 				AddClass<?, ?, ?> action = getObject();
 				action.setAssignation(new DataBinding<Object>(patternRole.getPatternRoleName()));
 			}
 
 		}
 	}
 
 	public static class URIBindingIsRequiredAndMustBeValid extends BindingIsRequiredAndMustBeValid<AddClass> {
 		public URIBindingIsRequiredAndMustBeValid() {
 			super("'uri'_binding_is_required_and_must_be_valid", AddClass.class);
 		}
 
 		@Override
 		public DataBinding<String> getBinding(AddClass object) {
 			return object.getClassName();
 		}
 
 	}
 
 }
