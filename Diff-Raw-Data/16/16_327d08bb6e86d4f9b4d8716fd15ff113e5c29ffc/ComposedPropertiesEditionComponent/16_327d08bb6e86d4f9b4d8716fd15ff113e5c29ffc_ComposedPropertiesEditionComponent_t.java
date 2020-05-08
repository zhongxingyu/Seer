 /*******************************************************************************
 * Copyright (c) 2008, 2012 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.eef.runtime.impl.components;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.runtime.Status;
 import org.eclipse.emf.common.util.BasicDiagnostic;
 import org.eclipse.emf.common.util.Diagnostic;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.edit.domain.EditingDomain;
 import org.eclipse.emf.eef.runtime.api.component.IPropertiesEditionComponent;
 import org.eclipse.emf.eef.runtime.api.notify.IPropertiesEditionEvent;
 import org.eclipse.emf.eef.runtime.api.notify.IPropertiesEditionListener;
 import org.eclipse.emf.eef.runtime.api.parts.IPropertiesEditionPart;
 import org.eclipse.emf.eef.runtime.context.PropertiesEditingContext;
 import org.eclipse.emf.eef.runtime.impl.parts.NullCompositePropertiesEditionPart;
 import org.eclipse.emf.eef.runtime.impl.utils.EEFRuntimeMessages;
 import org.eclipse.emf.eef.runtime.impl.utils.StringTools;
 
 /**
  * @author <a href="mailto:goulwen.lefur@obeo.fr">Goulwen Le Fur</a>
  * @author <a href="mailto:mikael.barbero@obeo.fr">Mikaël Barbero</a>
  */
 public class ComposedPropertiesEditionComponent implements IPropertiesEditionComponent {
 
 	/**
 	 * List of managed components
 	 */
 	protected List<IPropertiesEditionComponent> subComponents;
 
 	/**
 	 * List of this component listeners
 	 */
 	private List<IPropertiesEditionListener> listeners;
 
 	/**
 	 * The current editingContext
 	 */
 	private PropertiesEditingContext editingContext;
 
 	/**
 	 * Default constructor
 	 */
 	public ComposedPropertiesEditionComponent(PropertiesEditingContext editingContext, String mode) {
 		this.editingContext = editingContext;
 		this.subComponents = new ArrayList<IPropertiesEditionComponent>();
 		this.listeners = new ArrayList<IPropertiesEditionListener>();
 	}
 
 	/**
 	 * Parameterized constructor
 	 * 
 	 * @param subComponents
 	 *            list of the managed components
 	 */
 	public ComposedPropertiesEditionComponent(List<IPropertiesEditionComponent> subComponents) {
 		this.subComponents = subComponents;
 		this.listeners = new ArrayList<IPropertiesEditionListener>();
 		for (IPropertiesEditionComponent component : subComponents) {
 			listeners.add(component);
 			component.addListener(this);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.runtime.api.component.IPropertiesEditionComponent#setLiveEditingDomain(org.eclipse.emf.edit.domain.EditingDomain)
 	 */
 	public void setLiveEditingDomain(EditingDomain editingDomain) {
 		for (IPropertiesEditionComponent component : subComponents) {
 			component.setLiveEditingDomain(editingDomain);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.runtime.api.component.IPropertiesEditionComponent#partsList()
 	 */
 	public String[] partsList() {
 		List<String> partsList = new ArrayList<String>();
 		for (IPropertiesEditionComponent component : subComponents) {
 			String[] partsList2 = component.partsList();
 			for (int i = 0; i < partsList2.length; i++) {
 				String string = partsList2[i];
 				if (!partsList.contains(string))
 					partsList.add(string);
 			}
 		}
 		String[] result = new String[partsList.size()];
 		int i = 0;
 		for (String nextPart : partsList) {
 			result[i++] = nextPart;
 		}
 		return result;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.runtime.api.component.IPropertiesEditionComponent#initPart(java.lang.Object,
 	 *      int, org.eclipse.emf.ecore.EObject, org.eclipse.emf.ecore.resource.ResourceSet)
 	 */
 	public void initPart(Object key, int kind, EObject element, ResourceSet allResource) {
 		for (IPropertiesEditionComponent component : subComponents) {
 			component.initPart(key, kind, element, allResource);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.runtime.api.component.IPropertiesEditionComponent#initPart(java.lang.Object,
 	 *      int, org.eclipse.emf.ecore.EObject)
 	 */
 	public void initPart(Object key, int kind, EObject element) {
 		this.initPart(key, kind, element, element.eResource().getResourceSet());
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.runtime.api.component.IPropertiesEditionComponent#getPropertiesEditionPart(java.lang.String,
 	 *      java.lang.String)
 	 */
 	public IPropertiesEditionPart getPropertiesEditionPart(int kind, String key) {
 		for (IPropertiesEditionComponent component : subComponents) {
 			IPropertiesEditionPart propertiesEditionPart = component.getPropertiesEditionPart(kind, key);
 			if (propertiesEditionPart != null
 					&& !(propertiesEditionPart instanceof NullCompositePropertiesEditionPart))
 				return propertiesEditionPart;
 		}
 		return new NullCompositePropertiesEditionPart(this);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.runtime.api.component.IPropertiesEditionComponent#getEditingContext()
 	 */
 	public PropertiesEditingContext getEditingContext() {
 		return editingContext;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.runtime.api.notify.IPropertiesEditionListener#firePropertiesChanged(org.eclipse.emf.eef.runtime.api.notify.IPropertiesEditionEvent)
 	 */
 	public void firePropertiesChanged(IPropertiesEditionEvent event) {
 		event.addHolder(this);
 		for (IPropertiesEditionListener listener : listeners) {
 			if (!event.hold(listener))
 				listener.firePropertiesChanged(event);
 		}
 	}
 
 	public void delayedFirePropertiesChanged(IPropertiesEditionEvent event) {
 		for (IPropertiesEditionComponent propertiesEditionComponent : subComponents) {
 			if (!event.hold(propertiesEditionComponent))
 				propertiesEditionComponent.delayedFirePropertiesChanged(event);
 		}
 	}
 
 	public void activate() {
 		for (IPropertiesEditionComponent propertiesEditionComponent : subComponents) {
 			propertiesEditionComponent.activate();
 		}
 	}
 
 	public void deactivate() {
 		for (IPropertiesEditionComponent propertiesEditionComponent : subComponents) {
 			propertiesEditionComponent.deactivate();
 		}
 	}
 
 	/**
 	 * Add a new component of the managed components
 	 * 
 	 * @param component
 	 *            the component to add
 	 */
 	public void addSubComponent(IPropertiesEditionComponent component) {
 		if (subComponents != null && listeners != null) {
 			subComponents.add(component);
 			listeners.add(component);
 			component.addListener(this);
 		}
 	}
 
 	/**
 	 * Remove a component from the managed components
 	 * 
 	 * @param component
 	 *            the component to removed
 	 */
 	public void removeSubComponent(IPropertiesEditionComponent component) {
 		if (subComponents != null && listeners != null) {
 			component.removeListener(this);
 			subComponents.remove(component);
 			listeners.remove(component);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.runtime.api.component.IPropertiesEditionComponent#addListener(org.eclipse.emf.eef.runtime.api.notify.IPropertiesEditionListener)
 	 */
 	public void addListener(IPropertiesEditionListener listener) {
 		if (listeners == null)
 			listeners = new ArrayList<IPropertiesEditionListener>();
 		listeners.add(listener);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.runtime.api.component.IPropertiesEditionComponent#removeListener(org.eclipse.emf.eef.runtime.api.notify.IPropertiesEditionListener)
 	 */
 	public void removeListener(IPropertiesEditionListener listener) {
 		if (listeners != null)
 			listeners.remove(listener);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.runtime.api.component.IPropertiesEditionComponent#validateValue(org.eclipse.emf.eef.runtime.api.notify.IPropertiesEditionEvent)
 	 */
 	public Diagnostic validateValue(IPropertiesEditionEvent event) {
 		BasicDiagnostic diagnostic = new BasicDiagnostic(Diagnostic.OK,
 				EEFRuntimeMessages.ComposedPropertiesEditionComponent_eef_validation_diagnostic, 0,
 				EEFRuntimeMessages.ComposedPropertiesEditionComponent_no_problem, null);
 		for (IPropertiesEditionComponent component : subComponents) {
 			Diagnostic validateValue = component.validateValue(event);
 			if (validateValue != null)
 				diagnostic.add(validateValue);
 		}
 		diagnostic.recomputeSeverity();
 		return diagnostic;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.runtime.api.component.IPropertiesEditionComponent#validate()
 	 */
 	public Diagnostic validate() {
 		BasicDiagnostic diagnostic = new BasicDiagnostic(Diagnostic.OK,
 				EEFRuntimeMessages.ComposedPropertiesEditionComponent_eef_validation_diagnostic, Status.OK,
 				EEFRuntimeMessages.ComposedPropertiesEditionComponent_no_problem, null);
 		for (IPropertiesEditionComponent component : subComponents) {
 			diagnostic.getChildren().add(component.validate());
 		}
 		return diagnostic;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.runtime.api.component.IPropertiesEditionComponent#dispose()
 	 */
 	public void dispose() {
 		for (IPropertiesEditionComponent component : subComponents) {
 			component.dispose();
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.runtime.api.component.IPropertiesEditionComponent#mustBeComposed(java.lang.Object,
 	 *      int) Very strange case ... shouldn't be invoke ...
 	 */
 	public boolean mustBeComposed(Object key, int kind) {
 		for (IPropertiesEditionComponent component : subComponents) {
 			if (!component.mustBeComposed(key, kind))
 				return false;
 		}
 		return true;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.runtime.api.component.IPropertiesEditionComponent#isRequired(java.lang.Object,
 	 *      int)
 	 */
 	public boolean isRequired(Object key, int kind) {
 		for (IPropertiesEditionComponent component : subComponents) {
 			if (component.isRequired(key, kind))
 				return true;
 		}
 		return false;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.runtime.api.component.IPropertiesEditionComponent#getHelpContent(java.lang.String,
 	 *      int)
 	 */
 	public String getHelpContent(Object key, int kind) {
 		for (IPropertiesEditionComponent component : subComponents) {
 			if (component.getHelpContent(key, kind) != StringTools.EMPTY_STRING)
 				return component.getHelpContent(key, kind);
 		}
 		return null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.runtime.api.component.IPropertiesEditionComponent#translatePart(java.lang.String)
 	 */
 	public Object translatePart(String key) {
 		for (IPropertiesEditionComponent component : subComponents) {
 			if (component.translatePart(key) != null)
 				return component.translatePart(key);
 		}
 		return null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.runtime.api.component.IPropertiesEditionComponent#setPropertiesEditionPart(java.lang.Object,
 	 *      int, org.eclipse.emf.eef.runtime.api.parts.IPropertiesEditionPart)
 	 */
 	public void setPropertiesEditionPart(Object key, int kind, IPropertiesEditionPart propertiesEditionPart) {
 		for (IPropertiesEditionComponent component : subComponents) {
 			component.setPropertiesEditionPart(key, kind, propertiesEditionPart);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.runtime.api.component.IPropertiesEditionComponent#getTabText(java.lang.Object)
 	 */
 	public String getTabText(String key) {
 		for (IPropertiesEditionComponent component : subComponents) {
 			IPropertiesEditionPart part = component.getPropertiesEditionPart(0, key);
 			if (part != null)
 				return part.getTitle();
 		}
 		return key;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.runtime.api.component.IPropertiesEditionComponent#associatedFeature(java.lang.Object)
 	 */
 	public EStructuralFeature associatedFeature(Object editorKey) {
 		for (IPropertiesEditionComponent component : subComponents) {
 			EStructuralFeature associatedFeature = component.associatedFeature(editorKey);
 			if (associatedFeature != null)
 				return associatedFeature;
 		}
 		return null;
 	}
 
 }
