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
 package org.eclipse.bpmn2.modeler.core.runtime;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.bpmn2.modeler.core.merrimac.clad.DefaultPropertySection;
 import org.eclipse.bpmn2.modeler.core.utils.BusinessObjectUtil;
 import org.eclipse.bpmn2.modeler.core.utils.ModelUtil;
import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.graphiti.mm.pictograms.ConnectionDecorator;
 import org.eclipse.graphiti.mm.pictograms.Diagram;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.services.Graphiti;
 import org.eclipse.graphiti.ui.editor.DiagramEditor;
 import org.eclipse.jface.viewers.IFilter;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.views.properties.tabbed.AbstractPropertySection;
 import org.eclipse.ui.views.properties.tabbed.AbstractSectionDescriptor;
 import org.eclipse.ui.views.properties.tabbed.ISection;
 
 public class Bpmn2SectionDescriptor extends AbstractSectionDescriptor {
 
 		protected String id;
 		protected String tab;
 		protected AbstractPropertySection sectionClass;
 		protected List<Class> appliesToClasses = new ArrayList<Class>();
 		protected String enablesFor;
 		protected String filterClassName;
 		protected PropertySectionFilter filter;
 		
 		public Bpmn2SectionDescriptor(Bpmn2TabDescriptor td, IConfigurationElement e) {
 			tab = td.getId();
 			id = tab + ".section"; //$NON-NLS-1$
 
 			try {
 				String className = e.getAttribute("class"); //$NON-NLS-1$
 				if ("default".equals(className)) { //$NON-NLS-1$
 					sectionClass = new DefaultPropertySection();
 					if (e.getAttribute("features")!=null) { //$NON-NLS-1$
 						String[] properties = e.getAttribute("features").split(" "); //$NON-NLS-1$ //$NON-NLS-2$
 						((DefaultPropertySection)sectionClass).setProperties(properties);
 					}
 				}
 				else {
 					sectionClass = (AbstractPropertySection) e.createExecutableExtension("class"); //$NON-NLS-1$
 				}
 				filterClassName = e.getAttribute("filter"); //$NON-NLS-1$
 				if (filterClassName==null || filterClassName.isEmpty())
 					filterClassName = "org.eclipse.bpmn2.modeler.core.runtime.PropertySectionFilter"; //$NON-NLS-1$
 				filter = (PropertySectionFilter) Class.forName(filterClassName).getConstructor(null).newInstance(null);
 				enablesFor = e.getAttribute("enablesFor"); //$NON-NLS-1$
 				String type = e.getAttribute("type"); //$NON-NLS-1$
 				if (type!=null && !type.isEmpty()) {
 					String types[] = type.split(" "); //$NON-NLS-1$
 					for (String t : types) {
						Class clazz = null;
						try {
							clazz = Class.forName(t);
						}
						catch (Exception cnf) {
							clazz = null;
						}
						if (clazz==null) {
							for (TargetRuntime rt : TargetRuntime.getAllRuntimes()) {
								try {
									clazz = rt.getRuntimeExtension().getClass().getClassLoader().loadClass(t);
									break;
								}
								catch (Exception cnf) {
								}
							}
						}
						addAppliesToClass(clazz);
 						if (sectionClass instanceof DefaultPropertySection) {
							((DefaultPropertySection)sectionClass).addAppliesToClass(clazz);
 						}
 					}
 				}
 			} catch (Exception e1) {
 				e1.printStackTrace();
 			}
 			
 			td.getSectionDescriptors().add(this);
 		}
 		
 		public Bpmn2SectionDescriptor(Bpmn2TabDescriptor td, Bpmn2SectionDescriptor that) {
 			tab = td.getId();
 			id = tab + ".section" + hashCode(); //$NON-NLS-1$
 			this.sectionClass = that.sectionClass;
 			this.appliesToClasses.addAll(that.appliesToClasses);
 			this.enablesFor = that.enablesFor;
 			this.filterClassName = that.filterClassName;
 			this.filter = that.filter;
 		}
 		
 		@Override
 		public String getId() {
 			return id;
 		}
 
 		@Override
 		public ISection getSectionClass() {
 			return sectionClass;
 		}
 
 		@Override
 		public String getTargetTab() {
 			return tab;
 		}
 
 		protected void addAppliesToClass(Class clazz) {
			Assert.isNotNull(clazz);
 			appliesToClasses.add(clazz);
 		}
 		
 		@Override
 		public boolean appliesTo(IWorkbenchPart part, ISelection selection) {
 
 			EObject businessObject = null;
 			PictogramElement pe = BusinessObjectUtil.getPictogramElementForSelection(selection);
 			if (pe != null) {
 				if (pe instanceof ConnectionDecorator) {
 					// this is a special hack to allow selection of connection decorator labels:
 					// the connection decorator does not have a business object linked to it,
 					// but its parent (the connection) does.
 					pe = (PictogramElement) pe.eContainer();
 				}
 				if (!filter.select(pe))
 					return false;
 				businessObject = BusinessObjectUtil.getBusinessObjectForPictogramElement(pe);
 			}
 			else {
 				businessObject = BusinessObjectUtil.getBusinessObjectForSelection(selection);
 			}
 				
 			DiagramEditor editor = ModelUtil.getDiagramEditor(businessObject);
 			if (editor!=null) {
 				TargetRuntime rt = (TargetRuntime) editor.getAdapter(TargetRuntime.class);
 				if (rt!=null) {
 					int selected = 0;
 					int count = 0;
 					for (CustomTaskDescriptor tc : rt.getCustomTasks()) {
 						for (String s : tc.getPropertyTabs()) {
 							if (tab.equals(s)) {
 								if (tc.getFeatureContainer().getId(businessObject)!=null)
 									++selected;
 								++count;
 							}
 						}
 					}
 					if (count>0 && selected==0)
 						return false;
 				}
 				
 				// check if the selected business object (a BPMN2 model element)
 				// is contained in this editor's Resource
 				Diagram diagram = editor.getDiagramTypeProvider().getDiagram();
 				EObject o = Graphiti.getLinkService().getBusinessObjectForLinkedPictogramElement(diagram);
 				if (o==null || o.eResource() != businessObject.eResource()) {
 					return false;
 				}
 			}
 
 			// should we delegate to the section to determine whether it should be included in this tab?
 			if (sectionClass instanceof IBpmn2PropertySection) {
 				if (businessObject==null)
 					return false;
 				return ((IBpmn2PropertySection)sectionClass).appliesTo(part, selection);
 			}
 			
 			// if an input description was specified, check if the selected business object is of this description. 
 			if (appliesToClasses.isEmpty()) {
 				return true;
 			}
 			
 			// check all linked BusinessObjects for a match
 			if (pe!=null) {
 				if (pe.getLink()!=null) {
 					for (EObject eObj : pe.getLink().getBusinessObjects()){
 						if (appliesTo(eObj)) {
 							return true;
 						}
 					}
 				}
 			}
 			if (businessObject!=null) {
 				if (appliesTo(businessObject)) {
 					return true;
 				}
 			}
 			return false;
 		}
 
 		public boolean appliesTo(EObject eObj) {
 			for (Class c : appliesToClasses) {
 				if (c.isInstance(eObj))
 					return true;
 			}
 			return false;
 		}
 
 		/* (non-Javadoc)
 		 * @see org.eclipse.ui.views.properties.tabbed.AbstractSectionDescriptor#getEnablesFor()
 		 * Returns the value of the "enablesFor" attribute of the configuration element in plugin.xml
 		 * This is an integer value representing the number of items that must be selected for this
 		 * Property Tab to be enabled.
 		 */
 		@Override
 		public int getEnablesFor() {
 			try {
 				return Integer.parseInt(enablesFor);
 			}
 			catch (Exception ex) {
 				
 			}
 			return super.getEnablesFor();
 		}
 
 		@Override
 		public IFilter getFilter() {
 			return new IFilter() {
 
 				@Override
 				public boolean select(Object toTest) {
 					return false;
 				}
 				
 			};
 		}
 
 		@Override
 		public List getInputTypes() {
 			return super.getInputTypes();
 		}
 
 		/**
 		 * @param replacedId
 		 * @param part
 		 * @param selection
 		 * @return
 		 */
 		public boolean doReplaceTab(String replacedId, IWorkbenchPart part, ISelection selection) {
 			if (sectionClass instanceof IBpmn2PropertySection) {
 				return ((IBpmn2PropertySection)sectionClass).doReplaceTab(replacedId, part, selection);
 			}
 			return appliesTo(part,selection);
 		}
 		
 	}
