 /*******************************************************************************
  * Copyright (c) 2008-2009 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.eef.runtime.ui.properties.sections;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.edit.domain.EditingDomain;
 import org.eclipse.emf.edit.domain.IEditingDomainProvider;
 import org.eclipse.emf.eef.runtime.EEFRuntimePlugin;
 import org.eclipse.emf.eef.runtime.api.adapters.SemanticAdapter;
 import org.eclipse.emf.eef.runtime.api.component.IPropertiesEditionComponent;
 import org.eclipse.emf.eef.runtime.api.providers.IPropertiesEditionProvider;
 import org.eclipse.emf.eef.runtime.impl.providers.RegistryPropertiesEditionProvider;
 import org.eclipse.emf.eef.runtime.ui.viewers.PropertiesEditionContentProvider;
 import org.eclipse.emf.eef.runtime.ui.viewers.PropertiesEditionViewer;
 import org.eclipse.emf.eef.runtime.ui.viewers.filters.PropertiesEditionPartFilter;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.ViewerFilter;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.views.properties.tabbed.AbstractPropertySection;
 import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;
 import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
 
 /**
  * @author <a href="mailto:goulwen.lefur@obeo.fr">Goulwen Le Fur</a>
  */
public class PropertiesEditionSection extends AbstractPropertySection {
 
 	/**
 	 * the property sheet page for this section.
 	 */
 	private TabbedPropertySheetPage propertySheetPage;
 
 	/**
 	 * the section's parent
 	 */
 	protected Composite parent;
 	
 	/**
 	 * The section's viewer
 	 */
 	protected PropertiesEditionViewer viewer;
 
 	
 	/**
 	 * The editingDomain where the viewer must perform editing commands.
 	 */
 	private EditingDomain editingDomain;
 
 	/**
 	 * The current selected object or the first object in the selection when multiple objects are selected.
 	 */
 	protected EObject eObject;
 
 	/**
 	 * The list of current selected objects.
 	 */
 	protected List eObjectList;
 
 	/**
 	 * Filters list
 	 */
 	private ViewerFilter[] filters = new ViewerFilter[1];
 	
 	/**
 	 * Global Properties Edition Provider
 	 */
 	private IPropertiesEditionProvider propertiesEditionProvider = new RegistryPropertiesEditionProvider();
 
 	private PropertiesEditionContentProvider contentProvider;
 	
 	/**
 	 * @see org.eclipse.ui.views.properties.tabbed.ISection#createControls(org.eclipse.swt.widgets.Composite,
 	 *      org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage)
 	 */
 	public void createControls(Composite parent, TabbedPropertySheetPage aTabbedPropertySheetPage) {
 		super.createControls(parent, aTabbedPropertySheetPage);
 		this.propertySheetPage = aTabbedPropertySheetPage;
 		this.parent = parent;
 		this.viewer = new PropertiesEditionViewer(parent, null,SWT.NONE, 1);
 		viewer.setToolkit(getWidgetFactory());
 	}
 
 	/**
 	 * @see org.eclipse.ui.views.properties.tabbed.ISection#setInput(org.eclipse.ui.IWorkbenchPart,
 	 *      org.eclipse.jface.viewers.ISelection)
 	 */
 	public void setInput(IWorkbenchPart part, ISelection selection) {
 		super.setInput(part, selection);
 		initializeEditingDomain(part);
 		if (!(selection instanceof IStructuredSelection)) {
 			return;
 		}
 		if (resolveSemanticObject(((IStructuredSelection)selection).getFirstElement()) != null) {
 			EObject newEObject = resolveSemanticObject(((IStructuredSelection)selection).getFirstElement());
 			if (newEObject != eObject) {
 				eObject = newEObject;
 				if (eObject != null) {
 					if (contentProvider != null)
 						contentProvider.dispose();
 					contentProvider = new PropertiesEditionContentProvider(propertiesEditionProvider, IPropertiesEditionComponent.LIVE_MODE, editingDomain);
 					viewer.setContentProvider(contentProvider);
 					filters[0] = new PropertiesEditionPartFilter(getDescriptor());
 					viewer.setFilters(filters);
 					viewer.setInput(eObject);
 				}
 			}
 		}
 		eObjectList = ((IStructuredSelection)selection).toList();
 	}
 
 	
 	private void initializeEditingDomain(IWorkbenchPart part) {
 		if (part instanceof ITabbedPropertySheetPageContributor) {
 			ITabbedPropertySheetPageContributor editor = (ITabbedPropertySheetPageContributor)part;
 			if (editor != null && editor instanceof IEditorPart) {
 				if (editor instanceof IEditingDomainProvider)
 					editingDomain = ((IEditingDomainProvider)editor).getEditingDomain();
 				else if ((((IEditorPart)editor).getAdapter(IEditingDomainProvider.class)) != null)
 					editingDomain = ((IEditingDomainProvider)((IEditorPart)editor).getAdapter(IEditingDomainProvider.class)).getEditingDomain();
 				else if ((((IEditorPart)editor).getAdapter(EditingDomain.class)) != null)
 					editingDomain = (EditingDomain) ((IEditorPart)editor).getAdapter(EditingDomain.class);
 			}
 		}
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.ui.views.properties.tabbed.AbstractPropertySection#dispose()
 	 */
 	public void dispose() {
 		super.dispose();
 		if (this.viewer != null) {
 			this.viewer.getContentProvider().dispose();
 			this.viewer = null;
 		}
 		
 	}
 
 	/**
 	 * Magic method For eclipse 3.2 & 3.3 & 3.4 & 3.5
 	 * 
 	 * @return
 	 */
 	protected String getDescriptor() {
 		Map descriptor = getPageDescriptor(propertySheetPage);
 		for (Iterator iterator = descriptor.keySet().iterator(); iterator.hasNext();) {
 			Object key = iterator.next();
 			Object tab = descriptor.get(key);
 			Method getSectionAtIndex = getMethod(tab, "getSectionAtIndex", int.class);
 			if (getSectionAtIndex != null) {
 				Object result = callMethod(tab, getSectionAtIndex, 0);
 				if (result == this) {
 					Method getId = getMethod(key, "getId");
 					if (getId != null) {
 						String id = (String)callMethod(key, getId);
 						return id;
 					}
 				}
 			}
 		}
 		return "";
 	}
 	
 	private Map getPageDescriptor(TabbedPropertySheetPage propertySheetPage) {
 		Field descriptorToTabField = null;
 		boolean oldAccessible = false;
 		try {
 			Class<?> cls = propertySheetPage.getClass();
 			while (!cls.equals(TabbedPropertySheetPage.class)) {
 				cls = cls.getSuperclass();
 			}
 			descriptorToTabField = cls.getDeclaredField("descriptorToTab");
 			oldAccessible = descriptorToTabField.isAccessible();
 			descriptorToTabField.setAccessible(true);
 			return (Map)descriptorToTabField.get(propertySheetPage);
 
 		} catch (SecurityException e) {
 
 			EEFRuntimePlugin.getDefault().logError("Error while getting descriptorToTab", e);
 		} catch (NoSuchFieldException e) {
 
 			EEFRuntimePlugin.getDefault().logError("Error while getting descriptorToTab", e);
 		} catch (IllegalArgumentException e) {
 
 			EEFRuntimePlugin.getDefault().logError("Error while getting descriptorToTab", e);
 		} catch (IllegalAccessException e) {
 
 			EEFRuntimePlugin.getDefault().logError("Error while getting descriptorToTab", e);
 		} finally {
 			if (descriptorToTabField != null) {
 				descriptorToTabField.setAccessible(oldAccessible);
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * @param object
 	 * @return
 	 */
 	private EObject resolveSemanticObject(Object object) {		
 		if (object instanceof EObject){
 			return (EObject) object;
 		}else if (object instanceof IAdaptable){
 			IAdaptable adaptable = (IAdaptable)object;
 			if(adaptable.getAdapter(SemanticAdapter.class)!=null){
 				SemanticAdapter semanticAdapter = (SemanticAdapter)adaptable.getAdapter(SemanticAdapter.class);
 				return semanticAdapter.getEObject();
 			}else if(adaptable.getAdapter(EObject.class)!=null){
 				return (EObject) adaptable.getAdapter(EObject.class);
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * @param source
 	 *            the source object
 	 * @param name
 	 *            the method to get
 	 * @param argsType
 	 *            the method arguments type
 	 * @return the given method
 	 */
 	private Method getMethod(Object source, String name, Class... argsType) {
 		try {
 			return source.getClass().getDeclaredMethod(name, argsType);
 		} catch (Exception e) {
 			EEFRuntimePlugin.getDefault().logError("Cannot found method " + name, e);
 		}
 		return null;
 	}
 
 	/**
 	 * @param source
 	 *            the source object
 	 * @param name
 	 *            the method to get
 	 * @param argsType
 	 *            the method arguments type
 	 * @return the result of the given method
 	 */
 	private Object callMethod(Object source, Method method, Object... args) {
 		try {
 			return method.invoke(source, args);
 		} catch (Exception e) {
 			EEFRuntimePlugin.getDefault().logError("An error occured on " + method.getName() + " call.",
 					e);
 		}
 		return null;
 	}
 }
