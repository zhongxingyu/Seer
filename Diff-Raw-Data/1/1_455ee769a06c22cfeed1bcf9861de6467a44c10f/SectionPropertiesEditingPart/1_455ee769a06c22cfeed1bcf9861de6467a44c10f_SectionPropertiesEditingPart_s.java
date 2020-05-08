 /*******************************************************************************
  * Copyright (c) 2011 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.eef.runtime.part.impl;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.emf.common.util.Diagnostic;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.edit.domain.EditingDomain;
 import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
 import org.eclipse.emf.eef.runtime.EEFRuntimePlugin;
 import org.eclipse.emf.eef.runtime.api.adapters.SemanticAdapter;
 import org.eclipse.emf.eef.runtime.api.component.IPropertiesEditionComponent;
 import org.eclipse.emf.eef.runtime.api.notify.IPropertiesEditionEvent;
 import org.eclipse.emf.eef.runtime.api.notify.IPropertiesEditionListener;
 import org.eclipse.emf.eef.runtime.api.parts.IFormPropertiesEditionPart;
 import org.eclipse.emf.eef.runtime.context.PropertiesEditingContext;
 import org.eclipse.emf.eef.runtime.context.impl.DomainPropertiesEditionContext;
 import org.eclipse.emf.eef.runtime.impl.notify.PropertiesValidationEditionEvent;
 import org.eclipse.emf.eef.runtime.impl.parts.CompositePropertiesEditionPart;
 import org.eclipse.emf.eef.runtime.part.impl.util.ValidationMessageInjector;
 import org.eclipse.emf.eef.runtime.ui.parts.impl.BindingViewHelper;
 import org.eclipse.emf.eef.runtime.ui.utils.EEFRuntimeUIMessages;
 import org.eclipse.emf.eef.runtime.ui.utils.EditingUtils;
 import org.eclipse.emf.eef.runtime.ui.viewers.PropertiesEditionMessageManager;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.views.properties.tabbed.ISection;
 import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
 
 /**
  * @author <a href="mailto:goulwen.lefur@obeo.fr">Goulwen Le Fur</a>
  */
 public abstract class SectionPropertiesEditingPart extends CompositePropertiesEditionPart implements IFormPropertiesEditionPart, ISection {
 
 	/**
 	 * The tabbed property sheet page
 	 */
 	private TabbedPropertySheetPage tabbedPropertySheetPage;
 
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
 	protected List<?> eObjectList;
 
 	protected Composite container;
 
 	private boolean usedAsPropertySection;
 
 	/**
 	 * Manager for error message
 	 */
 	private PropertiesEditionMessageManager messageManager;
 
 	private Composite editingComposite;
 
 	private ValidationMessageInjector injector;
 
 	/**
 	 * 
 	 */
 	protected SectionPropertiesEditingPart() {
 		super();
 		adapterFactory = new ComposedAdapterFactory(ComposedAdapterFactory.Descriptor.Registry.INSTANCE);
 	}
 
 	public SectionPropertiesEditingPart(IPropertiesEditionComponent editionComponent) {
 		super(editionComponent);
 		this.usedAsPropertySection = false;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.ui.views.properties.tabbed.ISection#createControls(org.eclipse.swt.widgets.Composite,
 	 *      org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage)
 	 */
 	public void createControls(Composite parent, TabbedPropertySheetPage tabbedPropertySheetPage) {
 		this.tabbedPropertySheetPage = tabbedPropertySheetPage;
 		this.container = tabbedPropertySheetPage.getWidgetFactory().createComposite(parent);
 		container.setLayout(new GridLayout(3, false));
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.ui.views.properties.tabbed.ISection#setInput(org.eclipse.ui.IWorkbenchPart,
 	 *      org.eclipse.jface.viewers.ISelection)
 	 */
 	public void setInput(IWorkbenchPart part, ISelection selection) {
 		initializeEditingDomain(part);
 		if (!(selection instanceof IStructuredSelection)) {
 			return;
 		}
 		if (resolveSemanticObject(((IStructuredSelection)selection).getFirstElement()) != null) {
 			EObject newEObject = resolveSemanticObject(((IStructuredSelection)selection).getFirstElement());
 			if (newEObject != eObject) {
 				eObject = newEObject;
 				if (eObject != null) {
 					injector = new ValidationMessageInjector(tabbedPropertySheetPage);
 					messageManager = new PropertiesEditionMessageManager() {
 
 						@Override
 						protected void updateStatus(String message) {
 							if (injector != null) {
 								injector.setMessage(message, IStatus.OK);
 							}
 						}
 
 						@Override
 						protected void updateError(String message) {
 							if (injector != null) {
 								injector.setMessage(message, IStatus.ERROR);
 							}
 						}
 
 						@Override
 						protected void updateWarning(String message) {
 							if (injector != null) {
 								injector.setMessage(message, IStatus.WARNING);
 							}
 						}
 
 					};
 					disposeComponent();
 					refreshComponent();
 				}
 			}
 		}
 		eObjectList = ((IStructuredSelection)selection).toList();
 		this.usedAsPropertySection = true;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.runtime.impl.parts.CompositePropertiesEditionPart#refresh()
 	 */
 	public void refresh() {
 		if (usedAsPropertySection) {
 			initSemanticContents();
 		} else {
 			super.refresh();
 		}
 	}
 
 	private void initializeEditingDomain(IWorkbenchPart part) {
 		editingDomain = EditingUtils.getResourceSetFromEditor(part);
 	}
 
 	/**
	/**
 	 * This method analyze an input to exact the EObject to edit.
 	 * First we try to adapt this object in {@link SemanticAdapter}. If this can't be done, 
 	 * we check if this object is an {@link EObject}. Finally, if this object isn't an
 	 * {@link EObject}, we try to adapt it in EObject.
 	 * @param object element to test
 	 * @return the EObject to edit with EEF.
 	 */
 	protected EObject resolveSemanticObject(Object object) {
 		IAdaptable adaptable = null;
 		if (object instanceof IAdaptable) {
 			adaptable = (IAdaptable)object;
 		}
 		if (adaptable != null) {
 			if (adaptable.getAdapter(SemanticAdapter.class) != null) {
 				SemanticAdapter semanticAdapter = (SemanticAdapter)adaptable
 						.getAdapter(SemanticAdapter.class);
 				return semanticAdapter.getEObject();
 			} 
 		}
 		if (object instanceof EObject) {
 			return (EObject)object;
 		} 
 		if (adaptable != null) {
 			if (adaptable.getAdapter(EObject.class) != null) {
 				return (EObject)adaptable.getAdapter(EObject.class);
 			}
 		}
 		return null;
 	}
 
 	private void refreshComponent() {
 		DomainPropertiesEditionContext propertiesEditingContext = new DomainPropertiesEditionContext(null,
 				null, editingDomain, adapterFactory, eObject);
 		propertiesEditionComponent = propertiesEditingContext.createPropertiesEditingComponent(
 				IPropertiesEditionComponent.LIVE_MODE, getDescriptor());
 		if (propertiesEditionComponent != null) {
 			this.adapterFactory = propertiesEditionComponent.getEditingContext().getAdapterFactory();
 			propertiesEditingContext.setHelper(new BindingViewHelper(propertiesEditingContext,
 					tabbedPropertySheetPage.getWidgetFactory()));
 			propertiesEditionComponent.setPropertiesEditionPart(
 					propertiesEditionComponent.translatePart(getDescriptor()), 0, this);
 			propertiesEditionComponent.setLiveEditingDomain(editingDomain);
 			if (editingComposite != null) {
 				editingComposite.dispose();
 			}
 			editingComposite = this.createFigure(container, tabbedPropertySheetPage.getWidgetFactory());
 			if (editingComposite != null) {
 				editingComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
 				container.layout();
 			}
 			if (messageManager != null) {
 				messageManager.processMessage(new PropertiesValidationEditionEvent(null,
 						Diagnostic.OK_INSTANCE));
 				propertiesEditionComponent.addListener(new IPropertiesEditionListener() {
 
 					public void firePropertiesChanged(IPropertiesEditionEvent event) {
 						messageManager.processMessage(event);
 
 					}
 				});
 			}
 		}
 	}
 
 	/**
 	 * @param descriptor
 	 */
 	protected void initSemanticContents() {
 		propertiesEditionComponent.initPart(propertiesEditionComponent.translatePart(getDescriptor()), 1,
 				eObject);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.ui.views.properties.tabbed.ISection#aboutToBeShown()
 	 */
 	public void aboutToBeShown() {
 		/* empty default implementation */
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.ui.views.properties.tabbed.ISection#aboutToBeHidden()
 	 */
 	public void aboutToBeHidden() {
 		if (injector != null) {
 			injector.dispose();
 			injector = null;
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.ui.views.properties.tabbed.ISection#dispose()
 	 */
 	public void dispose() {
 		disposeComponent();
 	}
 
 	private void disposeComponent() {
 		if (propertiesEditionComponent != null) {
 			PropertiesEditingContext editingContext = propertiesEditionComponent.getEditingContext();
 			if (editingContext != null && editingContext.getParentContext() == null) {
 				editingContext.dispose();
 			}
 			propertiesEditionComponent.dispose();
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.ui.views.properties.tabbed.ISection#getMinimumHeight()
 	 */
 	public int getMinimumHeight() {
 		return SWT.DEFAULT;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.ui.views.properties.tabbed.ISection#shouldUseExtraSpace()
 	 */
 	public boolean shouldUseExtraSpace() {
 		return false;
 	}
 
 	/**
 	 * Magic method For eclipse 3.2 & 3.3 & 3.4 & 3.5
 	 * 
 	 * @return
 	 */
 	protected String getDescriptor() {
 		Map<?, ?> descriptor = getPageDescriptor(tabbedPropertySheetPage);
 		for (Iterator<?> iterator = descriptor.keySet().iterator(); iterator.hasNext();) {
 			Object key = iterator.next();
 			Object tab = descriptor.get(key);
 			Method getSectionAtIndex = getMethod(tab, "getSectionAtIndex", int.class); //$NON-NLS-1$
 			if (getSectionAtIndex != null) {
 				Object result = callMethod(tab, getSectionAtIndex, new Integer(0));
 				if (result == this) {
 					Method getId = getMethod(key, "getId"); //$NON-NLS-1$
 					if (getId != null) {
 						String id = (String)callMethod(key, getId);
 						return id;
 					}
 				}
 			}
 		}
 		return ""; //$NON-NLS-1$
 	}
 
 	private Map<?, ?> getPageDescriptor(TabbedPropertySheetPage propertySheetPage) {
 		Field descriptorToTabField = null;
 		boolean oldAccessible = false;
 		try {
 			Class<?> cls = propertySheetPage.getClass();
 			while (!cls.equals(TabbedPropertySheetPage.class)) {
 				cls = cls.getSuperclass();
 			}
 			descriptorToTabField = cls.getDeclaredField("descriptorToTab"); //$NON-NLS-1$
 			oldAccessible = descriptorToTabField.isAccessible();
 			descriptorToTabField.setAccessible(true);
 			return (Map<?, ?>)descriptorToTabField.get(propertySheetPage);
 
 		} catch (SecurityException e) {
 
 			EEFRuntimePlugin.getDefault().logError(
 					EEFRuntimeUIMessages.PropertiesEditionSection_descriptorToTab_not_found, e);
 		} catch (NoSuchFieldException e) {
 
 			EEFRuntimePlugin.getDefault().logError(
 					EEFRuntimeUIMessages.PropertiesEditionSection_descriptorToTab_not_found, e);
 		} catch (IllegalArgumentException e) {
 
 			EEFRuntimePlugin.getDefault().logError(
 					EEFRuntimeUIMessages.PropertiesEditionSection_descriptorToTab_not_found, e);
 		} catch (IllegalAccessException e) {
 
 			EEFRuntimePlugin.getDefault().logError(
 					EEFRuntimeUIMessages.PropertiesEditionSection_descriptorToTab_not_found, e);
 		} finally {
 			if (descriptorToTabField != null) {
 				descriptorToTabField.setAccessible(oldAccessible);
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
 	private Method getMethod(Object source, String name, Class<?>... argsType) {
 		try {
 			return source.getClass().getDeclaredMethod(name, argsType);
 		} catch (Exception e) {
 			EEFRuntimePlugin.getDefault().logError(
 					EEFRuntimeUIMessages.PropertiesEditionSection_method_not_found + name, e);
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
 			EEFRuntimePlugin.getDefault().logError(
 					EEFRuntimeUIMessages.PropertiesEditionSection_error_occured_on + method.getName()
 					+ EEFRuntimeUIMessages.PropertiesEditionSection_call, e);
 		}
 		return null;
 	}
 
 }
