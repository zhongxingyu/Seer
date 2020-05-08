 /*******************************************************************************
  * Copyright (c) 2008, 2010 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.eef.runtime.ui.viewers;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.common.util.Diagnostic;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.edit.domain.EditingDomain;
 import org.eclipse.emf.eef.runtime.api.component.IPropertiesEditionComponent;
 import org.eclipse.emf.eef.runtime.api.notify.IPropertiesEditionEvent;
 import org.eclipse.emf.eef.runtime.api.notify.IPropertiesEditionListener;
 import org.eclipse.emf.eef.runtime.api.parts.IPropertiesEditionPart;
 import org.eclipse.emf.eef.runtime.context.PropertiesEditingContext;
 import org.eclipse.emf.eef.runtime.context.impl.DomainPropertiesEditionContext;
 import org.eclipse.emf.eef.runtime.context.impl.EObjectPropertiesEditionContext;
 import org.eclipse.emf.eef.runtime.impl.services.PropertiesContextService;
 import org.eclipse.emf.eef.runtime.providers.PropertiesEditingProvider;
 import org.eclipse.emf.eef.runtime.ui.utils.EEFRuntimeUIMessages;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.Viewer;
 
 /**
  * @author <a href="mailto:goulwen.lefur@obeo.fr">Goulwen Le Fur</a>
  */
 public class PropertiesEditionContentProvider implements IStructuredContentProvider {
 
 	private AdapterFactory adapterFactory;
 
 	private IPropertiesEditionComponent propertiesEditionComponent;
 
 	private String mode;
 
 	private EditingDomain editingDomain;
 
 	private List<IPropertiesEditionListener> propertiesEditionListeners;
 
 	/**
 	 * @param adapterFactory
 	 */
 	public PropertiesEditionContentProvider(AdapterFactory adapterFactory, String mode) throws InstantiationException {
 		if (mode == IPropertiesEditionComponent.LIVE_MODE)
 			throw new InstantiationException(
 					EEFRuntimeUIMessages.PropertiesEditionContentProvider_editingDomain_not_defined);
 		this.adapterFactory = adapterFactory;
 		this.mode = mode;
 		this.propertiesEditionListeners = new ArrayList<IPropertiesEditionListener>();
 	}
 
 	/**
 	 * @param adapterFactory
 	 */
 	public PropertiesEditionContentProvider(AdapterFactory adapterFactory, String mode, EditingDomain editingDomain) {
 		this.adapterFactory = adapterFactory;
 		this.mode = mode;
 		this.editingDomain = editingDomain;
 		this.propertiesEditionListeners = new ArrayList<IPropertiesEditionListener>();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
 	 */
 	public void dispose() {
 		if (propertiesEditionComponent != null) {
 			PropertiesContextService.getInstance().pop();
 			propertiesEditionComponent.dispose();
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
 	 *      java.lang.Object, java.lang.Object)
 	 */
 	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
 		if (propertiesEditionComponent != null) {
 			PropertiesContextService.getInstance().pop();
 			propertiesEditionComponent.dispose();
			propertiesEditionComponent = null;
 		}
 		EObject eObject = null;
 		PropertiesEditingContext context = null;
 		if (newInput instanceof EObject) {
 			eObject = (EObject)newInput;
 			if (mode == IPropertiesEditionComponent.LIVE_MODE) {
 				context = new DomainPropertiesEditionContext(null, null, editingDomain, adapterFactory, eObject);
 			} else if (mode == IPropertiesEditionComponent.BATCH_MODE) {
 				context = new EObjectPropertiesEditionContext(null, null, eObject, adapterFactory);
 			}
 		} else if (newInput instanceof EObjectPropertiesEditionContext) {
 			context = (PropertiesEditingContext)newInput;
 			eObject = context.getEObject();
 		}
 		if (eObject != null) {
 			PropertiesEditingProvider propertiesEditionProvider = (PropertiesEditingProvider)adapterFactory.adapt(eObject, PropertiesEditingProvider.class);
 			if (propertiesEditionProvider != null) {
 				this.propertiesEditionComponent = propertiesEditionProvider.getPropertiesEditingComponent(context, mode);
 				if (mode == IPropertiesEditionComponent.LIVE_MODE) {
 					propertiesEditionComponent.setLiveEditingDomain(editingDomain);
 				}
 				updateListeners();
 			}
 		}
 		
 		// FIXME: find a better way to manage the context
 		PropertiesContextService.getInstance().push(eObject, propertiesEditionComponent);
 	}
 
 	/**
 	 * @param listener
 	 *            the properties listener to add
 	 */
 	public void addPropertiesListener(IPropertiesEditionListener listener) {
 		propertiesEditionListeners.add(listener);
 		if (propertiesEditionComponent != null)
 			propertiesEditionComponent.addListener(listener);
 	}
 
 	/**
 	 * Validate the model and return the resulting Diagnostic
 	 * 
 	 * @param event
 	 *            the event triggering the validation
 	 * @return the resulting value
 	 */
 	public Diagnostic validateValue(IPropertiesEditionEvent event) {
 		if (propertiesEditionComponent != null)
 			return propertiesEditionComponent.validateValue(event);
 		return null;
 	}
 
 	/**
 	 * Returns the Edition Parts list.
 	 * 
 	 * @return a String array with all parts keys
 	 */
 	public String[] partsList() {
 		if (propertiesEditionComponent != null)
 			return propertiesEditionComponent.partsList();
 		return new String[0];
 	}
 
 	/**
 	 * Return the Edition Part in the given context.
 	 * 
 	 * @param kind
 	 *            the kind of the expected Part
 	 * @param key
 	 *            the Part key
 	 * @return the Part created in the parent
 	 */
 	public IPropertiesEditionPart getPropertiesEditionPart(int kind, String key) {
 		if (propertiesEditionComponent != null)
 			return propertiesEditionComponent.getPropertiesEditionPart(kind, key);
 		return null;
 	}
 
 	/**
 	 * This method translate a Part name into his identifier
 	 * 
 	 * @param key
 	 *            the part name
 	 * @return the key identifying the part
 	 */
 	public Object translatePart(String key) {
 		if (propertiesEditionComponent != null)
 			return propertiesEditionComponent.translatePart(key);
 		return null;
 	}
 
 	/**
 	 * @param key
 	 *            the key identifying the ElementEditor
 	 * @param kind
 	 *            the kind of the part
 	 * @param element
 	 *            the element which initialize the part
 	 */
 	public void initPart(Object key, int kind, EObject element) {
 		if (propertiesEditionComponent != null)
 			propertiesEditionComponent.initPart(key, kind, element);
 	}
 
 	/**
 	 * @param key
 	 *            the key identifying the ElementEditor
 	 * @param kind
 	 *            the kind of the part
 	 * @param element
 	 *            the element which initialize the part
 	 * @param allResources
 	 *            the resource where the part has to process
 	 */
 	public void initPart(Object key, int kind, EObject element, ResourceSet allResources) {
 		if (propertiesEditionComponent != null)
 			propertiesEditionComponent.initPart(key, kind, element, allResources);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
 	 */
 	public Object[] getElements(Object inputElement) {
 		return new Object[] {inputElement};
 	}
 
 	/**
 	 * 
 	 */
 	protected void updateListeners() {
 		for (IPropertiesEditionListener listener : propertiesEditionListeners) {
 			propertiesEditionComponent.addListener(listener);
 		}
 	}
 
 }
