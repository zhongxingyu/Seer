 /*******************************************************************************
  * Copyright (c) 2012 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.eef.cdo.runtime.policies;
 
 import java.util.List;
 
 import org.eclipse.emf.cdo.CDOObject;
 import org.eclipse.emf.cdo.util.CDOUtil;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.eef.cdo.runtime.EEFCDORuntimePlugin;
 import org.eclipse.emf.eef.cdo.runtime.provider.ICDOLockStrategyProvider;
 import org.eclipse.emf.eef.runtime.api.component.IPropertiesEditionComponent;
 import org.eclipse.emf.eef.runtime.policies.ILockPolicy;
 import org.eclipse.emf.eef.runtime.ui.widgets.settings.EEFEditorSettings;
 import org.eclipse.emf.eef.runtime.ui.widgets.settings.EEFEditorSettingsBuilder.EEFEditorSettingsImpl;
 
 /**
  * Lock policy for EEF wizard : lock the semantic elements and its EEFEditorSettings for SmartModelNavigation.
  * 
  * @author <a href="mailto:nathalie.lepine@obeo.fr">Nathalie Lepine</a>
  * 
  */
 public class EEFWizardLockPolicy implements ILockPolicy {
 
 	/**
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.emf.eef.runtime.policies.ILockPolicy#lock(org.eclipse.emf.eef.runtime.api.component.IPropertiesEditionComponent)
 	 */
 	public void lock(IPropertiesEditionComponent propertiesEditingComponent) {
 		lock(propertiesEditingComponent.getEditingContext().getEObject());
		lock(propertiesEditingComponent.getEditingContext().getAllSettings());
 	}
 
 	private void lock(List<EEFEditorSettings> allSettings) {
 		for (EEFEditorSettings eefEditorSettings : allSettings) {
 			if (eefEditorSettings instanceof EEFEditorSettingsImpl) {
 				lock(((EEFEditorSettingsImpl) eefEditorSettings)
 						.getOrCreateSignificantObject());
 			}
 		}
 	}
 
 	private void lock(EObject eObject) {
 		if (eObject != null) {
 			CDOObject cdoObject = CDOUtil.getCDOObject(eObject);
 			if (cdoObject != null) {
 				if (!EEFCDORuntimePlugin.getDefault().getLockStrategyProvider()
 						.isEmpty()) {
 					for (ICDOLockStrategyProvider provider : EEFCDORuntimePlugin
 							.getDefault().getLockStrategyProvider()) {
 						provider.lock(cdoObject);
 					}
 				} else if (cdoObject.cdoWriteLock() != null) {
 					cdoObject.cdoWriteLock().tryLock();
 				}
 			}
 		}
 	}
 
 	private void unlock(List<EEFEditorSettings> allSettings) {
 		for (EEFEditorSettings eefEditorSettings : allSettings) {
 			if (eefEditorSettings instanceof EEFEditorSettingsImpl) {
 				unlock(((EEFEditorSettingsImpl) eefEditorSettings)
 						.getOrCreateSignificantObject());
 			}
 		}
 	}
 
 	private void unlock(EObject eObject) {
 		if (eObject != null) {
 			CDOObject cdoObject = CDOUtil.getCDOObject(eObject);
 			if (cdoObject != null) {
 				if (!EEFCDORuntimePlugin.getDefault().getLockStrategyProvider()
 						.isEmpty()) {
 					for (ICDOLockStrategyProvider provider : EEFCDORuntimePlugin
 							.getDefault().getLockStrategyProvider()) {
 						provider.release(cdoObject);
 					}
 				} else if (cdoObject.cdoWriteLock() != null) {
 					cdoObject.cdoWriteLock().unlock();
 				}
 			}
 		}
 	}
 
 	/**
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.emf.eef.runtime.policies.ILockPolicy#release(org.eclipse.emf.eef.runtime.api.component.IPropertiesEditionComponent)
 	 */
 	public void release(IPropertiesEditionComponent propertiesEditingComponent) {
 		unlock(propertiesEditingComponent.getEditingContext().getEObject());
 		unlock(propertiesEditingComponent.getEditingContext().getAllSettings());
 	}
 
 }
