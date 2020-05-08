 /*******************************************************************************
  * Copyright (c) 2011, 2012 Red Hat, Inc.
  *  All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Red Hat, Inc. - initial API and implementation
  *
  * @author Bob Brodt
  ******************************************************************************/
 
 package org.eclipse.bpmn2.modeler.runtime.jboss.jbpm5.property;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.bpmn2.Assignment;
 import org.eclipse.bpmn2.DataAssociation;
 import org.eclipse.bpmn2.DataInput;
 import org.eclipse.bpmn2.DataInputAssociation;
 import org.eclipse.bpmn2.Expression;
 import org.eclipse.bpmn2.FormalExpression;
 import org.eclipse.bpmn2.InputOutputSpecification;
import org.eclipse.bpmn2.InputSet;
 import org.eclipse.bpmn2.ItemAwareElement;
 import org.eclipse.bpmn2.PotentialOwner;
 import org.eclipse.bpmn2.ResourceAssignmentExpression;
 import org.eclipse.bpmn2.ResourceRole;
 import org.eclipse.bpmn2.Task;
 import org.eclipse.bpmn2.modeler.core.adapters.InsertionAdapter;
 import org.eclipse.bpmn2.modeler.core.merrimac.clad.AbstractBpmn2PropertySection;
 import org.eclipse.bpmn2.modeler.core.merrimac.clad.AbstractListComposite;
 import org.eclipse.bpmn2.modeler.core.merrimac.clad.DefaultListComposite;
 import org.eclipse.bpmn2.modeler.core.merrimac.clad.ListCompositeColumnProvider;
 import org.eclipse.bpmn2.modeler.core.merrimac.clad.ListCompositeContentProvider;
 import org.eclipse.bpmn2.modeler.core.merrimac.clad.TableColumn;
 import org.eclipse.bpmn2.modeler.core.merrimac.dialogs.BooleanObjectEditor;
 import org.eclipse.bpmn2.modeler.core.merrimac.dialogs.IntObjectEditor;
 import org.eclipse.bpmn2.modeler.core.merrimac.dialogs.NCNameObjectEditor;
 import org.eclipse.bpmn2.modeler.core.merrimac.dialogs.ObjectEditor;
 import org.eclipse.bpmn2.modeler.core.merrimac.dialogs.TextObjectEditor;
 import org.eclipse.bpmn2.modeler.core.runtime.ModelExtensionDescriptor;
 import org.eclipse.bpmn2.modeler.core.runtime.ModelExtensionDescriptor.ModelExtensionAdapter;
 import org.eclipse.bpmn2.modeler.core.runtime.ModelExtensionDescriptor.Property;
 import org.eclipse.bpmn2.modeler.core.utils.ModelUtil;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Composite;
 
 /**
  * @author Bob Brodt
  *
  */
 public class JbpmTaskDetailComposite extends JbpmActivityDetailComposite {
 
 	/**
 	 * @param section
 	 */
 	public JbpmTaskDetailComposite(AbstractBpmn2PropertySection section) {
 		super(section);
 	}
 
 	public JbpmTaskDetailComposite(Composite parent, int style) {
 		super(parent, style);
 	}
 
 	@Override
 	public void cleanBindings() {
 		super.cleanBindings();
 	}
 
 	@Override
 	protected boolean isModelObjectEnabled(String className, String featureName) {
 		if (className.equals("DataInput"))
 			return true;
 		return super.isModelObjectEnabled(className, featureName);
 	}
 
 	@Override
 	public void createBindings(EObject be) {
 		createInputParameterBindings((Task)be);
 		super.createBindings(be);
 	}
 	
 	/**
 	 * Create Object Editors for each of the Task's input parameters (DataInputs) that are
 	 * defined in the extension plugin.xml
 	 * 
 	 * The mappings for these parameters are simply text expressions and will be rendered
 	 * as individual editable fields on the Task property tab instead of being included in
 	 * the I/O Parameters list.
 	 * 
 	 * @param task
 	 */
 	protected void createInputParameterBindings(Task task) {
 		
 		ModelExtensionAdapter adapter = ModelExtensionDescriptor.getModelExtensionAdapter(task);
 		if (adapter != null) {
 			Resource resource = task.eResource();
 			
 			// This Task object has <modelExtension> properties defined in the plugin.xml
 			// check if any of the <property> elements extend the DataInputs or DataOutputs
 			// (i.e. the I/O Parameter mappings) and create Object Editors for them.
 			// If the Task does not define these parameter mappings, create temporary objects
 			// for the editors (these will go away if they are not touched by the user)
 			List<Property> props = adapter.getProperties("ioSpecification/dataInputs/name");
 			InputOutputSpecification ioSpec = task.getIoSpecification();
			if (ioSpec==null) {
				ioSpec = FACTORY.createInputOutputSpecification();
				InsertionAdapter.add(task,
						PACKAGE.getActivity_IoSpecification(),
						ioSpec);
			}
 			for (Property property : props) {
 				
 				// this will become the label for the Object Editor
 				String name = property.getFirstStringValue();
 				// the input parameter
 				DataInput parameter = null;
 				// the DataInputAssociation
 				DataAssociation association = null;
 				for (DataInput di : ioSpec.getDataInputs()) {
 					if (name.equals(di.getName())) {
 						// this is the one!
 						parameter = di;
 						for (DataAssociation da : task.getDataInputAssociations()) {
 							if (da.getTargetRef() == di) {
 								association = da;
 								break;
 							}
 						}
 						break;
 					}
 				}
 				
 				// create the DataInput element (the parameter) if needed
 				if (parameter==null) {
 					parameter = FACTORY.createDataInput();
 					ModelUtil.setID(parameter, resource);
 					parameter.setName(name);
					InsertionAdapter.add(ioSpec,
 							PACKAGE.getInputOutputSpecification_DataInputs(),
 							parameter);
					
					// create the InputSet if needed
					InputSet inputSet = null;
					if (ioSpec.getInputSets().size()==0) {
						inputSet = FACTORY.createInputSet();
						InsertionAdapter.add(ioSpec,
								PACKAGE.getInputOutputSpecification_InputSets(),
								inputSet);
					}
					else
						inputSet = ioSpec.getInputSets().get(0);
					// add the parameter to the InputSet also
					InsertionAdapter.add(inputSet,
							PACKAGE.getInputSet_DataInputRefs(),
							parameter);
 				}
 				
 				// create the DataInputAssociation if needed
 				if (association == null) {
 					association = FACTORY.createDataInputAssociation();
 					association.setTargetRef(parameter);
 					InsertionAdapter.add(task,
 							PACKAGE.getActivity_DataInputAssociations(),
 							association);
 				}
 				
 				// create an Assignment and FormalExpression if needed
 				// the "To" expression is the input parameter,
 				// the "From" expression body is the target of the Object Editor
 				FormalExpression fromExpression = null;
 				Assignment assignment = null;
 				if (association.getAssignment().size() == 1) {
 					assignment = (Assignment) association.getAssignment().get(0);
 					fromExpression = (FormalExpression) assignment.getFrom();
 				}
 				if (assignment==null) {
 					assignment = FACTORY.createAssignment();
 					FormalExpression toExpression = FACTORY.createFormalExpression();
 					toExpression.setBody(parameter.getId());
 					assignment.setTo(toExpression);
 					InsertionAdapter.add(association, PACKAGE.getDataAssociation_Assignment(), assignment);
 				}
 				if (fromExpression==null) {
 					fromExpression = FACTORY.createFormalExpression();
 					InsertionAdapter.add(assignment, PACKAGE.getAssignment_From(), fromExpression);
 				}
 				
 				// create the Object Editor for the "From" expression body:
 				// the data type is obtained from the DataInput <property> element from plugin.xml
 				EAttribute attribute = PACKAGE.getFormalExpression_Body();
 				String dataType = property.type;
 				ObjectEditor editor = null;
 				if ("EInt".equals(dataType)) {
 					editor = new IntObjectEditor(this,fromExpression,attribute);
 				}
 				else if ("EBoolean".equals(dataType)) {
 					editor = new BooleanObjectEditor(this,fromExpression,attribute);
 				}
 				else if ("ID".equals(dataType)) {
 					editor = new NCNameObjectEditor(this,fromExpression,attribute);
 				}
 				else {
 					editor = new TextObjectEditor(this,fromExpression,attribute);
 					((TextObjectEditor)editor).setMultiLine(false);
 				}
 				editor.createControl(getAttributesParent(),ModelUtil.toDisplayName(name));
 			}
 		}
 	}
 
 	@Override
 	protected AbstractListComposite bindList(EObject object, EStructuralFeature feature, EClass listItemClass) {
 		if (feature.getName().equals("resources")) {
 			if (isModelObjectEnabled(object.eClass(), feature)) {
 				ActorsListComposite actors = new ActorsListComposite(this);
 				actors.bindList(object, feature);
 				actors.setTitle("Actors");
 				return actors;
 			}
 			return null;
 		}
 		else
 			return super.bindList(object, feature, listItemClass);
 	}
 	
 	public class ActorsNameTableColumn extends TableColumn {
 		public ActorsNameTableColumn(EObject object) {
 			super(object, PACKAGE.getFormalExpression_Body());
 			setHeaderText("Name");
 			setEditable(true);
 		}
 	}
 	
 	public class ActorsListComposite extends DefaultListComposite {
 
 		public ActorsListComposite(Composite parent) {
 			super(parent, AbstractListComposite.COMPACT_STYLE);
 		}
 		
 		public EClass getListItemClass(EObject object, EStructuralFeature feature) {
 			return PACKAGE.getFormalExpression();
 		}
 		
 		@Override
 		public ListCompositeColumnProvider getColumnProvider(EObject object, EStructuralFeature feature) {
 			if (columnProvider==null) {
 				columnProvider = new ListCompositeColumnProvider(this);
 				columnProvider.add( new ActorsNameTableColumn(object) );
 			}
 			return columnProvider;
 		}
 		
 		public ListCompositeContentProvider getContentProvider(EObject object, EStructuralFeature feature, EList<EObject>list) {
 			if (contentProvider==null) {
 				contentProvider = new ListCompositeContentProvider(this, object, feature, list) {
 					@Override
 					public Object[] getElements(Object inputElement) {
 						List<Object> elements = new ArrayList<Object>();
 						Task task = (Task)object;
 						for (ResourceRole owner : task.getResources()) {
 							ResourceAssignmentExpression resourceAssignment = owner.getResourceAssignmentExpression();
 							if (resourceAssignment!=null) {
 								Expression expression = resourceAssignment.getExpression();
 								if (expression instanceof FormalExpression) {
 									elements.add(expression);
 								}
 							}
 						}
 						return elements.toArray(); 
 					}
 				};
 			}
 			return contentProvider;
 		}
 
 		protected EObject addListItem(EObject object, EStructuralFeature feature) {
 			Task task = (Task)object;
 			FormalExpression expression = FACTORY.createFormalExpression();
 			ResourceAssignmentExpression resourceAssignment = FACTORY.createResourceAssignmentExpression();
 			resourceAssignment.setExpression(expression);
 			PotentialOwner owner = FACTORY.createPotentialOwner();
 			owner.setResourceAssignmentExpression(resourceAssignment);
 			task.getResources().add(owner);
 
 			ModelUtil.setID(expression);
 			expression.setBody("New Actor");
 
 			ModelUtil.setID(resourceAssignment);
 			ModelUtil.setID(owner);
 			
 			
 			return expression;
 		}
 		
 		protected Object removeListItem(EObject object, EStructuralFeature feature, int index) {
 			Task task = (Task)object;
 			int size = task.getResources().size();
 			if (index>=0 && index<size) {
 				task.getResources().remove(index);
 			}
 			return null;
 		}
 
 		@Override
 		protected Object moveListItemUp(EObject object, EStructuralFeature feature, int index) {
 			Task task = (Task)object;
 			int size = task.getResources().size();
 			if (index>0 && index<size) {
 				ResourceRole owner = task.getResources().remove(index);
 				task.getResources().add(index-1, owner);
 				return owner;
 			}
 			return null;
 		}
 
 		@Override
 		protected Object moveListItemDown(EObject object, EStructuralFeature feature, int index) {
 			Task task = (Task)object;
 			int size = task.getResources().size();
 			if (index>=0 && index<size-1) {
 				ResourceRole owner = task.getResources().remove(index);
 				task.getResources().add(index, owner);
 				return owner;
 			}
 			return null;
 		}
 	}
 }
