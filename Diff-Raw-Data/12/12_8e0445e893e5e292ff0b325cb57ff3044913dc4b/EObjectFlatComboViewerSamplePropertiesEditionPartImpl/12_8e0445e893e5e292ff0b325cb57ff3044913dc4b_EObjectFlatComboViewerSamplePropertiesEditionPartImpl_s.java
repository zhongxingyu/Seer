 /*******************************************************************************
  * Copyright (c) 2009 - 2010 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.eef.eefnr.parts.impl;
 
 // Start of user code for imports
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
 import org.eclipse.emf.eef.eefnr.parts.EObjectFlatComboViewerSamplePropertiesEditionPart;
 import org.eclipse.emf.eef.eefnr.parts.EefnrViewsRepository;
 import org.eclipse.emf.eef.eefnr.providers.EefnrMessages;
 import org.eclipse.emf.eef.runtime.api.component.IPropertiesEditionComponent;
 import org.eclipse.emf.eef.runtime.api.notify.IPropertiesEditionEvent;
 import org.eclipse.emf.eef.runtime.api.parts.ISWTPropertiesEditionPart;
 import org.eclipse.emf.eef.runtime.impl.notify.PropertiesEditionEvent;
 import org.eclipse.emf.eef.runtime.impl.parts.CompositePropertiesEditionPart;
 import org.eclipse.emf.eef.runtime.ui.widgets.ButtonsModeEnum;
 import org.eclipse.emf.eef.runtime.ui.widgets.EObjectFlatComboViewer;
 import org.eclipse.emf.eef.runtime.ui.widgets.SWTUtils;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.ViewerFilter;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Group;
 
 
 
 // End of user code	
 
 /**
  * @author <a href="mailto:nathalie.lepine@obeo.fr">Nathalie Lepine</a>
  * 
  */
 public class EObjectFlatComboViewerSamplePropertiesEditionPartImpl extends CompositePropertiesEditionPart implements ISWTPropertiesEditionPart, EObjectFlatComboViewerSamplePropertiesEditionPart {
 
 	protected EObjectFlatComboViewer eobjectflatcomboviewerRequiredPropery;
 	protected EObjectFlatComboViewer eobjectflatcomboviewerOptionalPropery;
 
 
 
 	/**
 	 * Default constructor
 	 * @param editionComponent the {@link IPropertiesEditionComponent} that manage this part
 	 * 
 	 */
 	public EObjectFlatComboViewerSamplePropertiesEditionPartImpl(IPropertiesEditionComponent editionComponent) {
 		super(editionComponent);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.runtime.api.parts.ISWTPropertiesEditionPart#
 	 * 			createFigure(org.eclipse.swt.widgets.Composite)
 	 * 
 	 */
 	public Composite createFigure(final Composite parent) {
 		view = new Composite(parent, SWT.NONE);
 		GridLayout layout = new GridLayout();
 		layout.numColumns = 3;
 		view.setLayout(layout);
 		createControls(view);
 		return view;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.runtime.api.parts.ISWTPropertiesEditionPart#
 	 * 			createControls(org.eclipse.swt.widgets.Composite)
 	 * 
 	 */
 	public void createControls(Composite view) { 
 		createPropertiesGroup(view);
 
 
 		// Start of user code for additional ui definition
 		
 		// End of user code
 	}
 
 	/**
 	 * 
 	 */
 	protected void createPropertiesGroup(Composite parent) {
 		Group propertiesGroup = new Group(parent, SWT.NONE);
 		propertiesGroup.setText(EefnrMessages.EObjectFlatComboViewerSamplePropertiesEditionPart_PropertiesGroupLabel);
 		GridData propertiesGroupData = new GridData(GridData.FILL_HORIZONTAL);
 		propertiesGroupData.horizontalSpan = 3;
 		propertiesGroup.setLayoutData(propertiesGroupData);
 		GridLayout propertiesGroupLayout = new GridLayout();
 		propertiesGroupLayout.numColumns = 3;
 		propertiesGroup.setLayout(propertiesGroupLayout);
 		createEobjectflatcomboviewerRequiredProperyFlatComboViewer(propertiesGroup);
 		createEobjectflatcomboviewerOptionalProperyFlatComboViewer(propertiesGroup);
 	}
 
 	/**
 	 * @param propertiesGroup
 	 * 
 	 */
 	protected void createEobjectflatcomboviewerRequiredProperyFlatComboViewer(Composite parent) {
 		SWTUtils.createPartLabel(parent, EefnrMessages.EObjectFlatComboViewerSamplePropertiesEditionPart_EobjectflatcomboviewerRequiredProperyLabel, propertiesEditionComponent.isRequired(EefnrViewsRepository.EObjectFlatComboViewerSample.eobjectflatcomboviewerRequiredPropery, EefnrViewsRepository.SWT_KIND));
 		eobjectflatcomboviewerRequiredPropery = new EObjectFlatComboViewer(parent, false);
 		eobjectflatcomboviewerRequiredPropery.setLabelProvider(new AdapterFactoryLabelProvider(adapterFactory));
 
 		eobjectflatcomboviewerRequiredPropery.addSelectionChangedListener(new ISelectionChangedListener() {
 
 			public void selectionChanged(SelectionChangedEvent event) {
 				propertiesEditionComponent.firePropertiesChanged(new PropertiesEditionEvent(EObjectFlatComboViewerSamplePropertiesEditionPartImpl.this, EefnrViewsRepository.EObjectFlatComboViewerSample.eobjectflatcomboviewerRequiredPropery, PropertiesEditionEvent.CHANGE, PropertiesEditionEvent.SET, null, getEobjectflatcomboviewerRequiredPropery()));
 			}
 
 		});
 		GridData eobjectflatcomboviewerRequiredProperyData = new GridData(GridData.FILL_HORIZONTAL);
 		eobjectflatcomboviewerRequiredPropery.setLayoutData(eobjectflatcomboviewerRequiredProperyData);
 		eobjectflatcomboviewerRequiredPropery.setID(EefnrViewsRepository.EObjectFlatComboViewerSample.eobjectflatcomboviewerRequiredPropery);
 		SWTUtils.createHelpButton(parent, propertiesEditionComponent.getHelpContent(EefnrViewsRepository.EObjectFlatComboViewerSample.eobjectflatcomboviewerRequiredPropery, EefnrViewsRepository.SWT_KIND), null); //$NON-NLS-1$
 	}
 
 	/**
 	 * @param propertiesGroup
 	 * 
 	 */
 	protected void createEobjectflatcomboviewerOptionalProperyFlatComboViewer(Composite parent) {
 		SWTUtils.createPartLabel(parent, EefnrMessages.EObjectFlatComboViewerSamplePropertiesEditionPart_EobjectflatcomboviewerOptionalProperyLabel, propertiesEditionComponent.isRequired(EefnrViewsRepository.EObjectFlatComboViewerSample.eobjectflatcomboviewerOptionalPropery, EefnrViewsRepository.SWT_KIND));
 		eobjectflatcomboviewerOptionalPropery = new EObjectFlatComboViewer(parent, true);
 		eobjectflatcomboviewerOptionalPropery.setLabelProvider(new AdapterFactoryLabelProvider(adapterFactory));
 
 		eobjectflatcomboviewerOptionalPropery.addSelectionChangedListener(new ISelectionChangedListener() {
 
 			public void selectionChanged(SelectionChangedEvent event) {
 				propertiesEditionComponent.firePropertiesChanged(new PropertiesEditionEvent(EObjectFlatComboViewerSamplePropertiesEditionPartImpl.this, EefnrViewsRepository.EObjectFlatComboViewerSample.eobjectflatcomboviewerOptionalPropery, PropertiesEditionEvent.CHANGE, PropertiesEditionEvent.SET, null, getEobjectflatcomboviewerOptionalPropery()));
 			}
 
 		});
 		GridData eobjectflatcomboviewerOptionalProperyData = new GridData(GridData.FILL_HORIZONTAL);
 		eobjectflatcomboviewerOptionalPropery.setLayoutData(eobjectflatcomboviewerOptionalProperyData);
 		eobjectflatcomboviewerOptionalPropery.setID(EefnrViewsRepository.EObjectFlatComboViewerSample.eobjectflatcomboviewerOptionalPropery);
 		SWTUtils.createHelpButton(parent, propertiesEditionComponent.getHelpContent(EefnrViewsRepository.EObjectFlatComboViewerSample.eobjectflatcomboviewerOptionalPropery, EefnrViewsRepository.SWT_KIND), null); //$NON-NLS-1$
 	}
 
 
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.runtime.api.notify.IPropertiesEditionListener#firePropertiesChanged(org.eclipse.emf.eef.runtime.api.notify.IPropertiesEditionEvent)
 	 * 
 	 */
 	public void firePropertiesChanged(IPropertiesEditionEvent event) {
 		// Start of user code for tab synchronization
 		
 		// End of user code
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.eefnr.parts.EObjectFlatComboViewerSamplePropertiesEditionPart#getEobjectflatcomboviewerRequiredPropery()
 	 * 
 	 */
 	public EObject getEobjectflatcomboviewerRequiredPropery() {
 		if (eobjectflatcomboviewerRequiredPropery.getSelection() instanceof StructuredSelection) {
 			Object firstElement = ((StructuredSelection) eobjectflatcomboviewerRequiredPropery.getSelection()).getFirstElement();
 			if (firstElement instanceof EObject)
 				return (EObject) firstElement;
 		}
 		return null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.eefnr.parts.EObjectFlatComboViewerSamplePropertiesEditionPart#initEobjectflatcomboviewerRequiredPropery(ResourceSet allResources, EObject current)
 	 */
	public void initEobjectflatcomboviewerRequiredPropery(ResourceSet allResources, EObject current) {
		eobjectflatcomboviewerRequiredPropery.setInput(allResources);
 		if (current != null) {
			eobjectflatcomboviewerRequiredPropery.setSelection(new StructuredSelection(current));
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.eefnr.parts.EObjectFlatComboViewerSamplePropertiesEditionPart#setEobjectflatcomboviewerRequiredPropery(EObject newValue)
 	 * 
 	 */
 	public void setEobjectflatcomboviewerRequiredPropery(EObject newValue) {
 		if (newValue != null) {
 			eobjectflatcomboviewerRequiredPropery.setSelection(new StructuredSelection(newValue));
 		} else {
 			eobjectflatcomboviewerRequiredPropery.setSelection(new StructuredSelection()); //$NON-NLS-1$
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.eefnr.parts.EObjectFlatComboViewerSamplePropertiesEditionPart#setEobjectflatcomboviewerRequiredProperyButtonMode(ButtonsModeEnum newValue)
 	 */
 	public void setEobjectflatcomboviewerRequiredProperyButtonMode(ButtonsModeEnum newValue) {
 		eobjectflatcomboviewerRequiredPropery.setButtonMode(newValue);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.eefnr.parts.EObjectFlatComboViewerSamplePropertiesEditionPart#addFilterEobjectflatcomboviewerRequiredPropery(ViewerFilter filter)
 	 * 
 	 */
 	public void addFilterToEobjectflatcomboviewerRequiredPropery(ViewerFilter filter) {
 		eobjectflatcomboviewerRequiredPropery.addFilter(filter);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.eefnr.parts.EObjectFlatComboViewerSamplePropertiesEditionPart#addBusinessFilterEobjectflatcomboviewerRequiredPropery(ViewerFilter filter)
 	 * 
 	 */
 	public void addBusinessFilterToEobjectflatcomboviewerRequiredPropery(ViewerFilter filter) {
 		eobjectflatcomboviewerRequiredPropery.addBusinessRuleFilter(filter);
 	}
 
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.eefnr.parts.EObjectFlatComboViewerSamplePropertiesEditionPart#getEobjectflatcomboviewerOptionalPropery()
 	 * 
 	 */
 	public EObject getEobjectflatcomboviewerOptionalPropery() {
 		if (eobjectflatcomboviewerOptionalPropery.getSelection() instanceof StructuredSelection) {
 			Object firstElement = ((StructuredSelection) eobjectflatcomboviewerOptionalPropery.getSelection()).getFirstElement();
 			if (firstElement instanceof EObject)
 				return (EObject) firstElement;
 		}
 		return null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.eefnr.parts.EObjectFlatComboViewerSamplePropertiesEditionPart#initEobjectflatcomboviewerOptionalPropery(ResourceSet allResources, EObject current)
 	 */
 	public void initEobjectflatcomboviewerOptionalPropery(ResourceSet allResources, EObject current) {
 		eobjectflatcomboviewerOptionalPropery.setInput(allResources);
 		if (current != null) {
 			eobjectflatcomboviewerOptionalPropery.setSelection(new StructuredSelection(current));
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.eefnr.parts.EObjectFlatComboViewerSamplePropertiesEditionPart#setEobjectflatcomboviewerOptionalPropery(EObject newValue)
 	 * 
 	 */
 	public void setEobjectflatcomboviewerOptionalPropery(EObject newValue) {
 		if (newValue != null) {
 			eobjectflatcomboviewerOptionalPropery.setSelection(new StructuredSelection(newValue));
 		} else {
 			eobjectflatcomboviewerOptionalPropery.setSelection(new StructuredSelection()); //$NON-NLS-1$
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.eefnr.parts.EObjectFlatComboViewerSamplePropertiesEditionPart#setEobjectflatcomboviewerOptionalProperyButtonMode(ButtonsModeEnum newValue)
 	 */
 	public void setEobjectflatcomboviewerOptionalProperyButtonMode(ButtonsModeEnum newValue) {
 		eobjectflatcomboviewerOptionalPropery.setButtonMode(newValue);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.eefnr.parts.EObjectFlatComboViewerSamplePropertiesEditionPart#addFilterEobjectflatcomboviewerOptionalPropery(ViewerFilter filter)
 	 * 
 	 */
 	public void addFilterToEobjectflatcomboviewerOptionalPropery(ViewerFilter filter) {
 		eobjectflatcomboviewerOptionalPropery.addFilter(filter);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.eefnr.parts.EObjectFlatComboViewerSamplePropertiesEditionPart#addBusinessFilterEobjectflatcomboviewerOptionalPropery(ViewerFilter filter)
 	 * 
 	 */
 	public void addBusinessFilterToEobjectflatcomboviewerOptionalPropery(ViewerFilter filter) {
 		eobjectflatcomboviewerOptionalPropery.addBusinessRuleFilter(filter);
 	}
 
 
 
 
 
 
 
 	/**
 	 * {@inheritDoc}
 	 *
 	 * @see org.eclipse.emf.eef.runtime.api.parts.IPropertiesEditionPart#getTitle()
 	 * 
 	 */
 	public String getTitle() {
 		return EefnrMessages.EObjectFlatComboViewerSample_Part_Title;
 	}
 
 	// Start of user code additional methods
 	
 	// End of user code
 
 
 }
