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
 package org.eclipse.emf.eef.navigation.components;
 
 // Start of user code for imports
 import java.util.List;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.impl.AdapterImpl;
 import org.eclipse.emf.common.util.BasicDiagnostic;
 import org.eclipse.emf.common.util.Diagnostic;
 import org.eclipse.emf.common.util.WrappedException;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EcorePackage;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.util.Diagnostician;
 import org.eclipse.emf.ecore.util.EContentAdapter;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.eef.eefnr.EefnrPackage;
 import org.eclipse.emf.eef.eefnr.navigation.DeferedFlatReferenceTableEditorSample;
 import org.eclipse.emf.eef.eefnr.navigation.NavigationPackage;
 import org.eclipse.emf.eef.eefnr.navigation.parts.DeferedFlatReferencesTableSamplePropertiesEditionPart;
 import org.eclipse.emf.eef.eefnr.navigation.parts.NavigationViewsRepository;
 import org.eclipse.emf.eef.runtime.EEFRuntimePlugin;
 import org.eclipse.emf.eef.runtime.api.component.IPropertiesEditionComponent;
 import org.eclipse.emf.eef.runtime.api.notify.IPropertiesEditionEvent;
 import org.eclipse.emf.eef.runtime.api.notify.IPropertiesEditionListener;
 import org.eclipse.emf.eef.runtime.api.parts.IPropertiesEditionPart;
 import org.eclipse.emf.eef.runtime.api.providers.IPropertiesEditionPartProvider;
 import org.eclipse.emf.eef.runtime.impl.command.StandardEditingCommand;
 import org.eclipse.emf.eef.runtime.impl.components.StandardPropertiesEditionComponent;
 import org.eclipse.emf.eef.runtime.impl.filters.EObjectStrictFilter;
 import org.eclipse.emf.eef.runtime.impl.notify.PropertiesEditionEvent;
 import org.eclipse.emf.eef.runtime.impl.notify.PropertiesValidationEditionEvent;
 import org.eclipse.emf.eef.runtime.impl.services.PropertiesEditionPartProviderService;
 import org.eclipse.emf.eef.runtime.impl.utils.EEFConverterUtil;
 import org.eclipse.emf.eef.runtime.ui.widgets.referencestable.ReferencesTableSettings;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerFilter;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.PlatformUI;
 	
 
 // End of user code
 
 /**
  * @author <a href="mailto:nathalie.lepine@obeo.fr">Nathalie Lepine</a>
  * 
  */
 public class DeferedFlatReferencesTableSampleEditorPropertiesEditionComponent extends StandardPropertiesEditionComponent {
 
 	
 	public static String DEFEREDFLATREFERENCESTABLESAMPLE_PART = "DeferedFlatReferencesTableSample"; //$NON-NLS-1$
 
 	
 	private String[] parts = {DEFEREDFLATREFERENCESTABLESAMPLE_PART};
 
 	/**
 	 * The EObject to edit
 	 * 
 	 */
 	private DeferedFlatReferenceTableEditorSample deferedFlatReferenceTableEditorSample;
 
 	/**
 	 * The DeferedFlatReferencesTableSample part
 	 * 
 	 */
 	protected DeferedFlatReferencesTableSamplePropertiesEditionPart deferedFlatReferencesTableSamplePart;
 	
 	/**
 	 * Settings for flatReferencesTableSampleEditor ReferencesTable
 	 */
 	private	ReferencesTableSettings flatreferenceEditorSettings;
 	
 	/**
 	 * Default constructor
 	 * 
 	 */
 	public DeferedFlatReferencesTableSampleEditorPropertiesEditionComponent(EObject deferedFlatReferenceTableEditorSample, String editing_mode) {
 		if (deferedFlatReferenceTableEditorSample instanceof DeferedFlatReferenceTableEditorSample) {
 			this.deferedFlatReferenceTableEditorSample = (DeferedFlatReferenceTableEditorSample)deferedFlatReferenceTableEditorSample;
 			if (IPropertiesEditionComponent.LIVE_MODE.equals(editing_mode)) {
 				semanticAdapter = initializeSemanticAdapter();
 				this.deferedFlatReferenceTableEditorSample.eAdapters().add(semanticAdapter);
 			}
 		}
 		this.editing_mode = editing_mode;
 	}
 
 	/**
 	 * Initialize the semantic model listener for live editing mode
 	 * 
 	 * @return the semantic model listener
 	 * 
 	 */
 	private AdapterImpl initializeSemanticAdapter() {
 		return new EContentAdapter() {
 
 			/**
 			 * {@inheritDoc}
 			 * 
 			 * @see org.eclipse.emf.common.notify.impl.AdapterImpl#notifyChanged(org.eclipse.emf.common.notify.Notification)
 			 * 
 			 */
 			public void notifyChanged(final Notification msg) {
 				if (deferedFlatReferencesTableSamplePart == null)
 					DeferedFlatReferencesTableSampleEditorPropertiesEditionComponent.this.dispose();
 				else {
 					Runnable updateRunnable = new Runnable() {
 						public void run() {
 							runUpdateRunnable(msg);
 						}
 					};
 					if (null == Display.getCurrent()) {
 						PlatformUI.getWorkbench().getDisplay().syncExec(updateRunnable);
 					} else {
 						updateRunnable.run();
 					}
 				}
 			}
 
 		};
 	}
 
 	/**
 	 * Used to update the views
 	 * 
 	 */
 	protected void runUpdateRunnable(final Notification msg) {
 		if (EefnrPackage.eINSTANCE.getAbstractSample_Name().equals(msg.getFeature()) && deferedFlatReferencesTableSamplePart != null){
 			if (msg.getNewValue() != null) {
 				deferedFlatReferencesTableSamplePart.setName(EcoreUtil.convertToString(EcorePackage.eINSTANCE.getEString(), msg.getNewValue()));
 			} else {
 				deferedFlatReferencesTableSamplePart.setName("");
 			}
 		}
if (NavigationPackage.eINSTANCE.getDeferedReference_FlatreferenceEditor().equals(msg.getFeature()))
 	deferedFlatReferencesTableSamplePart.updateFlatReferencesTableSampleEditor(deferedFlatReferenceTableEditorSample);
 
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.runtime.impl.components.StandardPropertiesEditionComponent#translatePart(java.lang.String)
 	 * 
 	 */
 	public java.lang.Class translatePart(String key) {
 		if (DEFEREDFLATREFERENCESTABLESAMPLE_PART.equals(key))
 			return NavigationViewsRepository.DeferedFlatReferencesTableSample.class;
 		return super.translatePart(key);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.runtime.api.component.IPropertiesEditionComponent#partsList()
 	 * 
 	 */
 	public String[] partsList() {
 		return parts;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.runtime.api.component.IPropertiesEditionComponent#getPropertiesEditionPart
 	 *  (java.lang.String, java.lang.String)
 	 * 
 	 */
 	public IPropertiesEditionPart getPropertiesEditionPart(int kind, String key) {
 		if (deferedFlatReferenceTableEditorSample != null && DEFEREDFLATREFERENCESTABLESAMPLE_PART.equals(key)) {
 			if (deferedFlatReferencesTableSamplePart == null) {
 				IPropertiesEditionPartProvider provider = PropertiesEditionPartProviderService.getInstance().getProvider(NavigationViewsRepository.class);
 				if (provider != null) {
 					deferedFlatReferencesTableSamplePart = (DeferedFlatReferencesTableSamplePropertiesEditionPart)provider.getPropertiesEditionPart(NavigationViewsRepository.DeferedFlatReferencesTableSample.class, kind, this);
 					addListener((IPropertiesEditionListener)deferedFlatReferencesTableSamplePart);
 				}
 			}
 			return (IPropertiesEditionPart)deferedFlatReferencesTableSamplePart;
 		}
 		return null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.runtime.impl.components.StandardPropertiesEditionComponent#
 	 *      setPropertiesEditionPart(java.lang.Class, int, org.eclipse.emf.eef.runtime.api.parts.IPropertiesEditionPart)
 	 * 
 	 */
 	public void setPropertiesEditionPart(java.lang.Class key, int kind, IPropertiesEditionPart propertiesEditionPart) {
 		if (key == NavigationViewsRepository.DeferedFlatReferencesTableSample.class)
 			this.deferedFlatReferencesTableSamplePart = (DeferedFlatReferencesTableSamplePropertiesEditionPart) propertiesEditionPart;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.runtime.api.component.IPropertiesEditionComponent#initPart(java.lang.Class, int, org.eclipse.emf.ecore.EObject, 
 	 *      org.eclipse.emf.ecore.resource.ResourceSet)
 	 * 
 	 */
 	public void initPart(java.lang.Class key, int kind, EObject elt, ResourceSet allResource) {
 		setInitializing(true);
 		if (deferedFlatReferencesTableSamplePart != null && key == NavigationViewsRepository.DeferedFlatReferencesTableSample.class) {
 			((IPropertiesEditionPart)deferedFlatReferencesTableSamplePart).setContext(elt, allResource);
 			final DeferedFlatReferenceTableEditorSample deferedFlatReferenceTableEditorSample = (DeferedFlatReferenceTableEditorSample)elt;
 			// init values
 			if (deferedFlatReferenceTableEditorSample.getName() != null)
 				deferedFlatReferencesTableSamplePart.setName(EEFConverterUtil.convertToString(EcorePackage.eINSTANCE.getEString(), deferedFlatReferenceTableEditorSample.getName()));
 
 			flatreferenceEditorSettings = new ReferencesTableSettings(deferedFlatReferenceTableEditorSample, NavigationPackage.eINSTANCE.getDeferedFlatReferenceTableEditorSample_References(), NavigationPackage.eINSTANCE.getDeferedReference_FlatreferenceEditor());
 			deferedFlatReferencesTableSamplePart.initFlatReferencesTableSampleEditor(flatreferenceEditorSettings);
 			// init filters
 
 			deferedFlatReferencesTableSamplePart.addFilterToFlatReferencesTableSampleEditor(new ViewerFilter() {
 
 				/**
 				 * {@inheritDoc}
 				 * 
 				 * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
 				 */
 				public boolean select(Viewer viewer, Object parentElement, Object element) {
 					if (element instanceof EObject)
 						return (!deferedFlatReferencesTableSamplePart.isContainedInFlatReferencesTableSampleEditorTable((EObject)element));
 					return false;
 				}
 
 			});
 			deferedFlatReferencesTableSamplePart.addFilterToFlatReferencesTableSampleEditor(new EObjectStrictFilter(EefnrPackage.eINSTANCE.getTotalSample()));
 			// Start of user code for additional businessfilters for flatReferencesTableSampleEditor
 			
 			// End of user code
 
 		}
 		// init values for referenced views
 
 		// init filters for referenced views
 
 		setInitializing(false);
 	}
 
 
 
 
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.runtime.api.notify.IPropertiesEditionListener#firePropertiesChanged(org.eclipse.emf.eef.runtime.api.notify.IPropertiesEditionEvent)
 	 * 
 	 */
 	public void firePropertiesChanged(final IPropertiesEditionEvent event) {
 		if (!isInitializing()) {
 			Diagnostic valueDiagnostic = validateValue(event);
 			if (IPropertiesEditionComponent.BATCH_MODE.equals(editing_mode)) {			
 				if (NavigationViewsRepository.DeferedFlatReferencesTableSample.name == event.getAffectedEditor()) {
 					updateName((java.lang.String)EEFConverterUtil.createFromString(EcorePackage.eINSTANCE.getEString(), (String)event.getNewValue()));
 				}
 				if (NavigationViewsRepository.DeferedFlatReferencesTableSample.flatReferencesTableSampleEditor == event.getAffectedEditor()) {
 					updateFlatReferencesTableSampleEditor(event);
 				}
 			}
 			else if (IPropertiesEditionComponent.LIVE_MODE.equals(editing_mode)) {
 				liveEditingDomain.getCommandStack().execute(new StandardEditingCommand() {
 					
 					public void execute() {
 						if (NavigationViewsRepository.DeferedFlatReferencesTableSample.name == event.getAffectedEditor()) {
 							updateName((java.lang.String)EEFConverterUtil.createFromString(EcorePackage.eINSTANCE.getEString(), (String)event.getNewValue()));
 						}
 						if (NavigationViewsRepository.DeferedFlatReferencesTableSample.flatReferencesTableSampleEditor == event.getAffectedEditor()) {
 							updateFlatReferencesTableSampleEditor(event);
 						}
 					}
 				});			
 			}
 			if (valueDiagnostic.getSeverity() != Diagnostic.OK && valueDiagnostic instanceof BasicDiagnostic)
 				super.firePropertiesChanged(new PropertiesValidationEditionEvent(event, valueDiagnostic));
 			else {
 				Diagnostic validate = validate();
 				super.firePropertiesChanged(new PropertiesValidationEditionEvent(event, validate));
 			}
 			super.firePropertiesChanged(event);
 		}
 	}
 
 	private void updateName(java.lang.String newValue) {
 		deferedFlatReferenceTableEditorSample.setName(newValue);	
 	}
 
 	private void updateFlatReferencesTableSampleEditor(final IPropertiesEditionEvent event) {
 		if (event.getKind() == PropertiesEditionEvent.SET)
 			flatreferenceEditorSettings.setToReference((List<EObject>) event.getNewValue());
 	}
 
 
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.runtime.impl.components.StandardPropertiesEditionComponent#isRequired(java.lang.String, int)
 	 * 
 	 */
 	public boolean isRequired(String key, int kind) {
 		return key == NavigationViewsRepository.DeferedFlatReferencesTableSample.flatReferencesTableSampleEditor;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.runtime.api.component.IPropertiesEditionComponent#validateValue(org.eclipse.emf.eef.runtime.api.notify.IPropertiesEditionEvent)
 	 * 
 	 */
 	public Diagnostic validateValue(IPropertiesEditionEvent event) {
 		Diagnostic ret = Diagnostic.OK_INSTANCE;
 		if (event.getNewValue() != null) {
 			String newStringValue = event.getNewValue().toString();
 			try {
 				if (NavigationViewsRepository.DeferedFlatReferencesTableSample.name == event.getAffectedEditor()) {
 					Object newValue = EcoreUtil.createFromString(EefnrPackage.eINSTANCE.getAbstractSample_Name().getEAttributeType(), newStringValue);
 					ret = Diagnostician.INSTANCE.validate(EefnrPackage.eINSTANCE.getAbstractSample_Name().getEAttributeType(), newValue);
 				}
 			} catch (IllegalArgumentException iae) {
 				ret = BasicDiagnostic.toDiagnostic(iae);
 			} catch (WrappedException we) {
 				ret = BasicDiagnostic.toDiagnostic(we);
 			}
 		}
 		return ret;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.runtime.api.component.IPropertiesEditionComponent#validate()
 	 * 
 	 */
 	public Diagnostic validate() {
 		Diagnostic validate = Diagnostic.OK_INSTANCE;
 		validate = EEFRuntimePlugin.getEEFValidator().validate(deferedFlatReferenceTableEditorSample);
 		// Start of user code for custom validation check
 		
 		// End of user code
 		return validate;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.runtime.api.component.IPropertiesEditionComponent#dispose()
 	 * 
 	 */
 	public void dispose() {
 		if (semanticAdapter != null)
 			deferedFlatReferenceTableEditorSample.eAdapters().remove(semanticAdapter);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 *
 	 * @see org.eclipse.emf.eef.runtime.api.component.IPropertiesEditionComponent#getTabText(java.lang.String)
 	 * 
 	 */
 	public String getTabText(String p_key) {
 		return deferedFlatReferencesTableSamplePart.getTitle();
 	}
 }
