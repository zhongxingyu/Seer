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
 package org.eclipse.emf.eef.runtime.ui.widgets.settings;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.ecore.EClassifier;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.eef.runtime.impl.utils.EEFUtils;
 
 
 
 /**
  * @author glefur
  * @since 1.0
  *
  */
 public class EEFEditorSettingsBuilder  {
 
 	private EObject source;
 	private EStructuralFeature feature;
 	private List<NavigationStep> steps;
 	
 	/**
 	 * @param source
 	 * @param feature 
 	 * @return a new instance of {@link EEFEditorSettingsBuilder}.
 	 */
 	public static EEFEditorSettingsBuilder create(EObject source, EStructuralFeature feature) {
 		return new EEFEditorSettingsBuilder(source, feature);
 	}
 	
 	/**
 	 * @param source
 	 * @param feature 
 	 */
 	private EEFEditorSettingsBuilder(EObject source, EStructuralFeature feature) { 
 		this.source = source;
 		this.feature = feature;
 		steps = new ArrayList<NavigationStep>();
 	}
 	
 	/**
 	 * @param step
 	 * @return
 	 */
 	public EEFEditorSettingsBuilder nextStep(NavigationStep step) {
 		steps.add(step);
 		return this;
 	}
 
 	/**
 	 * @return
 	 */
 	public EEFEditorSettings build() {
 		for (NavigationStep step : steps) {
 			if (step.getReference().isMany() && step.getIndex() == NavigationStep.NOT_INITIALIZED) {
 				throw new IllegalStateException("Navigation step misconfigured : Reference " + step.getReference().getName() + " is mulivalued. You must define an index.");
 			}
 		}
 		return new EEFEditorSettingsImpl(source, feature, (List<NavigationStep>) Collections.unmodifiableList(steps));
 	}
 	
 	/**
 	 * @author glefur
 	 *
 	 */
 	public class EEFEditorSettingsImpl implements EEFEditorSettings {
 
 		private EObject source;
 		private EStructuralFeature feature; 
 		private List<NavigationStep> steps;
 		private EObject significantObject;
 		
 		private EEFEditorSettingsImpl(EObject source, EStructuralFeature feature, List<NavigationStep> steps) {
 			this.source = source;
 			this.feature = feature;
 			this.steps = steps;
 		}
 		
 		/**
 		 * {@inheritDoc}
 		 * @see org.eclipse.emf.eef.runtime.ui.widgets.settings.EEFEditorSettings#getValue()
 		 */
 		public Object getValue() {
 			EObject significantObject = getSignificantObject();
 			if (significantObject != null && significantObject.eResource() != null) {
 				return significantObject.eGet(feature);
 			} else {
 				return null;
 			}
 		}
 
 		/**
 		 * Compute and cache the object to edit following the NavigationStep.
 		 * @return object to edit.
 		 */
 		public EObject getSignificantObject() {
 			if (significantObject == null) {
 				EObject current = source;
 				for (NavigationStep step : EEFEditorSettingsImpl.this.steps) {
 					// reference *
 					if (step.getReference().isMany()) {
 						@SuppressWarnings("unchecked")
 						List<EObject> result = (List<EObject>)current.eGet(step.getReference());
 						List<EObject> result2 = new ArrayList<EObject>();
 						
 						if (!result.isEmpty() && (!step.getFilters().isEmpty() || step.getDiscriminator() != null)) {
 							// add filters
 							if (!step.getFilters().isEmpty()) {
 								for (EEFFilter eefFilter : step.getFilters()) {
 									for (EObject eObject : result) {
 										if (eefFilter.select(eObject)) {
 											result2.add(eObject);
 										}
 									}
 									result = result2;
 									result2 = new ArrayList<EObject>();
 								}
 							}
 							// add discriminator
 							if (step.getDiscriminator() != null) {
 								for (EObject eObject : result) {
 									if (step.getDiscriminator().isInstance(eObject)) {
 										result2.add(eObject);
 									}
 								}
 							}
 							
 						} 
 						
 						// Use init if result.isEmpty()
 						if (result.isEmpty()) {
 							return null;
 						}
 						
 						if (step.getIndex() != NavigationStep.NOT_INITIALIZED && step.getIndex() < result.size()) {
 							current = result.get(step.getIndex());
 							// Use init if current == null
 							if (current == null) {
 								return null;
 							} 
 						} else {
 							throw new IllegalStateException();
 						}
 						
 					} else {
 						// reference 0 or 1
 						EObject current2 = current;
 						current = (EObject) current2.eGet(step.getReference());
 						if (current == null) {
 							return null;
 						}
 					}
 				}
 				significantObject = current;
 			}
 			return significantObject;
 		}
 		
 		/**
 		 * Compute and cache the object to edit following the NavigationStep.
 		 * @return object to edit.
 		 */
 		public EObject getOrCreateSignificantObject() {
			if (significantObject == null || (significantObject != null && significantObject.eResource() == null)) {
 				EObject current = source;
 				for (NavigationStep step : EEFEditorSettingsImpl.this.steps) {
 					// reference *
 					if (step.getReference().isMany()) {
 						@SuppressWarnings("unchecked")
 						List<EObject> result = (List<EObject>)current.eGet(step.getReference());
 						List<EObject> result2 = new ArrayList<EObject>();
 						
 						if (!result.isEmpty() && (!step.getFilters().isEmpty() || step.getDiscriminator() != null)) {
 							// add filters
 							if (!step.getFilters().isEmpty()) {
 								for (EEFFilter eefFilter : step.getFilters()) {
 									for (EObject eObject : result) {
 										if (eefFilter.select(eObject)) {
 											result2.add(eObject);
 										}
 									}
 									result = result2;
 									result2 = new ArrayList<EObject>();
 								}
 							}
 							// add discriminator
 							if (step.getDiscriminator() != null) {
 								for (EObject eObject : result) {
 									if (step.getDiscriminator().isInstance(eObject)) {
 										result2.add(eObject);
 									}
 								}
 								result = result2;
 								result2 = new ArrayList<EObject>();
 							}
 							
 							// no filter and no discriminator -> get step.reference
 //						} else {
 //								result2 = result;
 						}
 						
 						// Use init if result.isEmpty()
 						if (result.isEmpty() && step.getInit() != null) {
 							result.add(step.getInit().init(current));
 						}
 						
 						if (step.getIndex() != NavigationStep.NOT_INITIALIZED && step.getIndex() < result.size()) {
 							current = result.get(step.getIndex());
 							// Use init if current == null
 							if (current == null && step.getInit() != null) {
 								EObject current2 = current;
 								current = step.getInit().init(current2);
 							} 
 						} else {
 							throw new IllegalStateException();
 						}
 						
 					} else {
 						// reference 0 or 1
 						EObject current2 = current;
 						current = (EObject) current2.eGet(step.getReference());
 						if (current == null) {
 							if (step.getInit() != null) {
 								current = step.getInit().init(current2);
 							}
 						}
 					}
 				}
 				significantObject = current;
 			}
 			return significantObject;
 		}
 		
 		public void setValue(Object newValue) {
 			getOrCreateSignificantObject().eSet(feature, newValue);
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * @see org.eclipse.emf.eef.runtime.ui.widgets.settings.EEFEditorSettings#choiceOfValues(org.eclipse.emf.common.notify.AdapterFactory)
 		 */
 		public Object choiceOfValues(AdapterFactory adapterFactory) {
 			return feature instanceof EReference ? EEFUtils.choiceOfValues(source, feature) : null;
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * @see org.eclipse.emf.eef.runtime.ui.widgets.settings.EEFEditorSettings#isAffectingFeature(org.eclipse.emf.ecore.EStructuralFeature)
 		 */
 		public boolean isAffectingFeature(EStructuralFeature feature) {
 			for (NavigationStep step : EEFEditorSettingsImpl.this.steps) {
 				if (step.getReference().equals(feature)) {
 					return true;
 				}
 			}
 			return false;
 		}
 		
 		/**
 		 * {@inheritDoc}
 		 * @see org.eclipse.emf.eef.runtime.ui.widgets.settings.EEFEditorSettings#isAffectingEvent(org.eclipse.emf.common.notify.Notification)
 		 */
 		public boolean isAffectingEvent(Notification notification) {
 			if (
 					(notification.getFeature() instanceof EStructuralFeature && isAffectingFeature((EStructuralFeature) notification.getFeature())) 
 					|| (getSignificantObject()!= null && getSignificantObject().equals(notification.getNotifier()))) {
 				return true;
 			}
 			return false;
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * @see org.eclipse.emf.eef.runtime.ui.widgets.settings.EEFEditorSettings#getSource()
 		 */
 		public EObject getSource() {
 			return source;
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * @see org.eclipse.emf.eef.runtime.ui.widgets.settings.EEFEditorSettings#getEType()
 		 */
 		public EClassifier getEType() {
 			return feature.getEType();
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * @see org.eclipse.emf.eef.runtime.ui.widgets.settings.EEFEditorSettings#getLastReference()
 		 */
 		public EReference getLastReference() {
 			if (feature instanceof EReference) {
 				return (EReference) feature;
 			} else if (EEFEditorSettingsImpl.this.steps.size() > 0) {
 				return EEFEditorSettingsImpl.this.steps.get(EEFEditorSettingsImpl.this.steps.size() - 1).getReference();
 			}
 			return null;
 		}
 	}
 	
 }
