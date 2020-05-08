 /*******************************************************************************
  * Copyright (c) 2010 The Eclipse Foundation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * 	The Eclipse Foundation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.epp.internal.mpc.ui.wizards;
 
 import java.lang.reflect.InvocationTargetException;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.epp.internal.mpc.ui.MarketplaceClientUi;
 import org.eclipse.epp.internal.mpc.ui.MarketplaceClientUiPlugin;
 import org.eclipse.epp.internal.mpc.ui.catalog.MarketplaceCatalog;
import org.eclipse.epp.internal.mpc.ui.operations.FeatureDescriptor;
 import org.eclipse.epp.internal.mpc.ui.operations.ProfileChangeOperationComputer;
 import org.eclipse.epp.internal.mpc.ui.operations.ProfileChangeOperationComputer.OperationType;
 import org.eclipse.epp.mpc.ui.CatalogDescriptor;
 import org.eclipse.equinox.internal.p2.discovery.model.CatalogItem;
 import org.eclipse.equinox.internal.p2.ui.discovery.wizards.DiscoveryWizard;
 import org.eclipse.equinox.p2.metadata.IInstallableUnit;
 import org.eclipse.equinox.p2.operations.ProfileChangeOperation;
 import org.eclipse.equinox.p2.operations.UninstallOperation;
 import org.eclipse.equinox.p2.ui.AcceptLicensesWizardPage;
 import org.eclipse.equinox.p2.ui.ProvisioningUI;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.wizard.IWizardPage;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.ui.statushandlers.StatusManager;
 
 /**
  * @author David Green
  */
 public class MarketplaceWizard extends DiscoveryWizard {
 
 	private static final String PREF_DEFAULT_CATALOG = CatalogDescriptor.class.getSimpleName();
 
 	private CatalogSelectionPage catalogSelectionPage;
 
 	private Set<String> installedFeatures;
 
 	private final SelectionModel selectionModel;
 
 	private ProfileChangeOperation profileChangeOperation;
 
 	private FeatureSelectionWizardPage featureSelectionWizardPage;
 
 	private AcceptLicensesWizardPage acceptLicensesPage;
 
 	private IInstallableUnit[] operationIUs;
 
 	public MarketplaceWizard(MarketplaceCatalog catalog, MarketplaceCatalogConfiguration configuration) {
 		super(catalog, configuration);
 		setWindowTitle(Messages.MarketplaceWizard_eclipseSolutionCatalogs);
 		selectionModel = new SelectionModel(this) {
 			@Override
 			public void selectionChanged() {
 				super.selectionChanged();
 				profileChangeOperation = null;
 			}
 		};
 	}
 
 	@Override
 	public MarketplaceCatalogConfiguration getConfiguration() {
 		return (MarketplaceCatalogConfiguration) super.getConfiguration();
 	}
 
 	@Override
 	public MarketplaceCatalog getCatalog() {
 		return (MarketplaceCatalog) super.getCatalog();
 	}
 
 	@Override
 	protected MarketplacePage doCreateCatalogPage() {
 		return new MarketplacePage(getCatalog(), getConfiguration());
 	}
 
 	public ProfileChangeOperation getProfileChangeOperation() {
 		return profileChangeOperation;
 	}
 
 	public void setProfileChangeOperation(ProfileChangeOperation profileChangeOperation) {
 		this.profileChangeOperation = profileChangeOperation;
 	}
 
 	@Override
 	public boolean canFinish() {
 		if (getContainer().getCurrentPage() == featureSelectionWizardPage) {
 			if (profileChangeOperation == null) {
 				updateProfileChangeOperation();
 			}
 		}
 		if (profileChangeOperation == null
 				|| profileChangeOperation.getResolutionResult().getSeverity() == IStatus.ERROR) {
 			return false;
 		}
 		if (computeMustCheckLicenseAcceptance()) {
 			if (acceptLicensesPage != null && acceptLicensesPage.isPageComplete()) {
 				return true;
 			}
 			return false;
 		} else {
 			return true;
 		}
 	}
 
 	@Override
 	public IWizardPage getNextPage(IWizardPage page) {
 		if (page == featureSelectionWizardPage) {
 			if (profileChangeOperation == null) {
 				updateProfileChangeOperation();
 				if (profileChangeOperation == null
 						|| profileChangeOperation.getResolutionResult().getSeverity() == IStatus.ERROR) {
 					// can't compute a change operation, so there must be some kind of error
 					// we show these on the the feature selection wizard page
 					return featureSelectionWizardPage;
 				} else if (profileChangeOperation instanceof UninstallOperation) {
 					// next button was used to resolve errors on an uninstall.
 					// by returning the same page the finish button will be enabled, allowing the user to finish.
 					return featureSelectionWizardPage;
 				}
 			}
 			if (computeMustCheckLicenseAcceptance()) {
 				if (acceptLicensesPage == null) {
 					acceptLicensesPage = new AcceptLicensesWizardPage(
 							ProvisioningUI.getDefaultUI().getLicenseManager(), operationIUs, profileChangeOperation);
 					addPage(acceptLicensesPage);
 				} else {
 					acceptLicensesPage.update(operationIUs, profileChangeOperation);
 				}
 				if (acceptLicensesPage.hasLicensesToAccept()) {
 					return acceptLicensesPage;
 				}
 			}
 			return null;
 		}
 		return super.getNextPage(page);
 	}
 
 	public boolean computeMustCheckLicenseAcceptance() {
 		return profileChangeOperation != null && !(profileChangeOperation instanceof UninstallOperation);
 	}
 
 	@Override
 	public void addPages() {
 		doDefaultCatalogSelection();
 		if (getConfiguration().getCatalogDescriptors().size() > 1) {
 			addPage(getCatalogSelectionPage());
 		}
 		super.addPages();
 		featureSelectionWizardPage = new FeatureSelectionWizardPage();
 		addPage(featureSelectionWizardPage);
 	}
 
 	public CatalogSelectionPage getCatalogSelectionPage() {
 		if (catalogSelectionPage == null) {
 			catalogSelectionPage = new CatalogSelectionPage(getConfiguration());
 		}
 		return catalogSelectionPage;
 	}
 
 	@Override
 	public IWizardPage getStartingPage() {
 		if (getConfiguration().getCatalogDescriptor() != null) {
 			return getCatalogPage();
 		}
 		return super.getStartingPage();
 	}
 
 	private void doDefaultCatalogSelection() {
 		if (getConfiguration().getCatalogDescriptor() == null) {
 			String defaultCatalogUrl = MarketplaceClientUiPlugin.getInstance().getPreferenceStore().getString(
 					PREF_DEFAULT_CATALOG);
 			// if a preferences was set, we default to that catalog descriptor
 			if (defaultCatalogUrl != null && defaultCatalogUrl.length() > 0) {
 				for (CatalogDescriptor descriptor : getConfiguration().getCatalogDescriptors()) {
 					URL url = descriptor.getUrl();
 					try {
 						if (url.toURI().toString().equals(defaultCatalogUrl)) {
 							getConfiguration().setCatalogDescriptor(descriptor);
 							break;
 						}
 					} catch (URISyntaxException e) {
 						// ignore
 					}
 				}
 			}
 			// if no preference was set or we could not find the descriptor then we default to eclipse.org
 			if (getConfiguration().getCatalogDescriptor() == null) {
 				for (CatalogDescriptor descriptor : getConfiguration().getCatalogDescriptors()) {
 					URL url = descriptor.getUrl();
 
 					String host = url.getHost();
 					if (host.endsWith(".eclipse.org")) { //$NON-NLS-1$
 						getConfiguration().setCatalogDescriptor(descriptor);
 						break;
 					}
 				}
 			}
 		}
 	}
 
 	@Override
 	public void dispose() {
 		if (getConfiguration().getCatalogDescriptor() != null) {
 			// remember the catalog for next time.
 			try {
 				MarketplaceClientUiPlugin.getInstance().getPreferenceStore().setValue(PREF_DEFAULT_CATALOG,
 						getConfiguration().getCatalogDescriptor().getUrl().toURI().toString());
 			} catch (URISyntaxException e) {
 				// ignore
 			}
 		}
 		if (getCatalog() != null) {
 			getCatalog().dispose();
 		}
 		super.dispose();
 	}
 
 	@Override
 	public boolean performFinish() {
 		if (profileChangeOperation != null
 				&& profileChangeOperation.getResolutionResult().getSeverity() != IStatus.ERROR) {
 			ProvisioningUI.getDefaultUI().schedule(profileChangeOperation.getProvisioningJob(null),
 					StatusManager.SHOW | StatusManager.LOG);
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public MarketplacePage getCatalogPage() {
 		return (MarketplacePage) super.getCatalogPage();
 	}
 
 	protected synchronized Set<String> getInstalledFeatures() {
 		if (installedFeatures == null) {
 			try {
 				getContainer().run(true, false, new IRunnableWithProgress() {
 					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
 						installedFeatures = MarketplaceClientUi.computeInstalledFeatures(monitor);
 					}
 				});
 			} catch (InvocationTargetException e) {
 				MarketplaceClientUi.error(e.getCause());
 				installedFeatures = Collections.emptySet();
 			} catch (InterruptedException e) {
 				// should never happen (not cancelable)
 				throw new IllegalStateException(e);
 			}
 		}
 		return installedFeatures;
 	}
 
 	public SelectionModel getSelectionModel() {
 		return selectionModel;
 	}
 
 	public void updateProfileChangeOperation() {
 		profileChangeOperation = null;
 		operationIUs = null;
 		if (getSelectionModel().computeProvisioningOperationViable()) {
 			try {
 				Map<CatalogItem, Operation> itemToOperation = getSelectionModel().getItemToOperation();
 				OperationType operationType = null;
 				List<CatalogItem> items = new ArrayList<CatalogItem>();
 				for (Map.Entry<CatalogItem, Operation> entry : itemToOperation.entrySet()) {
 					OperationType entryOperationType = entry.getValue().getOperationType();
 					if (entryOperationType != null) {
 						if (operationType == null || operationType == OperationType.UPDATE) {
 							operationType = entryOperationType;
							if (operationType == OperationType.UPDATE) {
								for (FeatureDescriptor descriptor : getSelectionModel().getSelectedFeatureDescriptors()) {
									if (!getInstalledFeatures().contains(descriptor.getId())) {
										operationType = OperationType.INSTALL;
									}
								}
							}
 						}
 						items.add(entry.getKey());
 					}
 				}
 				ProfileChangeOperationComputer provisioningOperation = new ProfileChangeOperationComputer(
 						operationType, itemToOperation.keySet(), getSelectionModel().getSelectedFeatureDescriptors());
 				getContainer().run(true, true, provisioningOperation);
 
 				profileChangeOperation = provisioningOperation.getOperation();
 				operationIUs = provisioningOperation.getIus();
 			} catch (InvocationTargetException e) {
 				Throwable cause = e.getCause();
 				IStatus status;
 				if (cause instanceof CoreException) {
 					status = ((CoreException) cause).getStatus();
 				} else {
 					status = new Status(IStatus.ERROR, MarketplaceClientUi.BUNDLE_ID, NLS.bind(
 							Messages.MarketplaceWizard_problemsPerformingProvisioningOperation,
 							new Object[] { cause.getMessage() }), cause);
 				}
 				StatusManager.getManager().handle(status, StatusManager.SHOW | StatusManager.BLOCK | StatusManager.LOG);
 			} catch (InterruptedException e) {
 				// canceled
 			}
 		}
 		if (getContainer().getCurrentPage() == featureSelectionWizardPage) {
 			featureSelectionWizardPage.updateMessage();
 		}
 	}
 }
