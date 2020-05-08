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
 
 import java.util.List;
 
 import org.eclipse.bpmn2.CatchEvent;
 import org.eclipse.bpmn2.Event;
 import org.eclipse.bpmn2.EventDefinition;
 import org.eclipse.bpmn2.ThrowEvent;
 import org.eclipse.bpmn2.modeler.core.merrimac.clad.AbstractBpmn2PropertySection;
 import org.eclipse.bpmn2.modeler.core.merrimac.clad.AbstractDetailComposite;
 import org.eclipse.bpmn2.modeler.core.merrimac.clad.AbstractListComposite;
 import org.eclipse.bpmn2.modeler.ui.property.events.CommonEventDetailComposite;
 import org.eclipse.bpmn2.modeler.ui.property.events.EventDefinitionsListComposite;
 import org.eclipse.bpmn2.modeler.ui.property.events.EventDefinitionsListComposite.EventDefinitionsDetailComposite;
 import org.eclipse.bpmn2.modeler.ui.property.tasks.DataAssociationDetailComposite.MapType;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.swt.widgets.Composite;
 
 /**
  * @author Bob Brodt
  *
  */
 public class JbpmCommonEventDetailComposite extends CommonEventDetailComposite {
 
 	/**
 	 * @param parent
 	 * @param style
 	 */
 	public JbpmCommonEventDetailComposite(Composite parent, int style) {
 		super(parent, style);
 	}
 
 	/**
 	 * @param section
 	 */
 	public JbpmCommonEventDetailComposite(AbstractBpmn2PropertySection section) {
 		super(section);
 	}
 
 	@Override
 	protected AbstractListComposite bindList(final EObject object, EStructuralFeature feature, EClass listItemClass) {
 		if (isModelObjectEnabled(object.eClass(), feature)) {
 			if ("eventDefinitions".equals(feature.getName())) { //$NON-NLS-1$
 				eventsTable = new EventDefinitionsListComposite(this, (Event)object) {
 
					public AbstractDetailComposite createDetailComposite(Composite parent, Class eClass) {
 						EventDefinitionsDetailComposite details = new EventDefinitionsDetailComposite(parent, (Event)getBusinessObject()) {
 							@Override
 							public void createBindings(EObject be) {
 								super.createBindings(be);
 								if (object instanceof CatchEvent)
 									getDataAssociationComposite().setAllowedMapTypes(MapType.Property.getValue());
 								else
 									getDataAssociationComposite().setAllowedMapTypes(MapType.Property.getValue() | MapType.SingleAssignment.getValue());
 							}
 						};
 						return details;
 					}
 					
 					@Override
 					protected EObject addListItem(EObject object, EStructuralFeature feature) {
 						List<EventDefinition> eventDefinitions = null;
 						if (event instanceof ThrowEvent)
 							eventDefinitions = ((ThrowEvent)event).getEventDefinitions();
 						else if  (event instanceof CatchEvent)
 							eventDefinitions = ((CatchEvent)event).getEventDefinitions();
 							
 						if (eventDefinitions.size()>0) {
 							MessageDialog.openError(getShell(), Messages.JbpmCommonEventDetailComposite_Error_Title,
 								Messages.JbpmCommonEventDetailComposite_Error_Message
 							);
 							return null;
 						}
 						return super.addListItem(object, feature);
 					}
 				};
 				eventsTable.bindList(object, feature);
 				eventsTable.setTitle(Messages.JbpmCommonEventDetailComposite_Title);
 				return eventsTable;
 			}
 			return super.bindList(object, feature, listItemClass);
 		}
 		return null;
 	}
 }
