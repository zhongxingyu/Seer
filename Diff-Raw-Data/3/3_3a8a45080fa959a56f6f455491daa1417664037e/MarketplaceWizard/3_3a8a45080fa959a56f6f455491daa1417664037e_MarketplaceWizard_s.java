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
 
 import java.io.UnsupportedEncodingException;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.epp.internal.mpc.ui.MarketplaceClientUi;
 import org.eclipse.epp.internal.mpc.ui.MarketplaceClientUiPlugin;
 import org.eclipse.epp.internal.mpc.ui.catalog.MarketplaceCatalog;
 import org.eclipse.epp.internal.mpc.ui.catalog.MarketplaceDiscoveryStrategy;
 import org.eclipse.epp.internal.mpc.ui.operations.FeatureDescriptor;
 import org.eclipse.epp.internal.mpc.ui.operations.ProfileChangeOperationComputer;
 import org.eclipse.epp.internal.mpc.ui.operations.ProfileChangeOperationComputer.OperationType;
 import org.eclipse.epp.internal.mpc.ui.wizards.SelectionModel.CatalogItemEntry;
 import org.eclipse.epp.internal.mpc.ui.wizards.SelectionModel.FeatureEntry;
 import org.eclipse.epp.mpc.ui.CatalogDescriptor;
 import org.eclipse.equinox.internal.p2.discovery.AbstractDiscoveryStrategy;
 import org.eclipse.equinox.internal.p2.discovery.model.CatalogItem;
 import org.eclipse.equinox.internal.p2.ui.discovery.util.WorkbenchUtil;
 import org.eclipse.equinox.internal.p2.ui.discovery.wizards.DiscoveryWizard;
 import org.eclipse.equinox.p2.metadata.IInstallableUnit;
 import org.eclipse.equinox.p2.operations.ProfileChangeOperation;
 import org.eclipse.equinox.p2.operations.ProvisioningJob;
 import org.eclipse.equinox.p2.operations.UninstallOperation;
 import org.eclipse.equinox.p2.ui.AcceptLicensesWizardPage;
 import org.eclipse.equinox.p2.ui.ProvisioningUI;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.wizard.IWizardPage;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.swt.browser.Browser;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.browser.IWebBrowser;
 import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
 import org.eclipse.ui.internal.browser.WorkbenchBrowserSupport;
 import org.eclipse.ui.statushandlers.StatusManager;
 
 /**
  * @author David Green
  */
 public class MarketplaceWizard extends DiscoveryWizard implements InstallProfile, IMarketplaceWebBrowser {
 
 	private static final String PREF_DEFAULT_CATALOG = CatalogDescriptor.class.getSimpleName();
 
 	private CatalogSelectionPage catalogSelectionPage;
 
 	private Set<String> installedFeatures;
 
 	private final SelectionModel selectionModel;
 
 	private MarketplaceBrowserIntegration browserListener;
 
 	private ProfileChangeOperation profileChangeOperation;
 
 	private FeatureSelectionWizardPage featureSelectionWizardPage;
 
 	private AcceptLicensesWizardPage acceptLicensesPage;
 
 	private IInstallableUnit[] operationIUs;
 
 	private Set<CatalogItem> operationNewInstallItems;
 
 	private boolean initialSelectionInitialized;
 
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
 
 	void initializeInitialSelection() throws CoreException {
 		if (!wantInitializeInitialSelection()) {
 			throw new IllegalStateException();
 		}
 		initialSelectionInitialized = true;
 		initializeCatalog();
 		try {
 			getContainer().run(true, true, new IRunnableWithProgress() {
 				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
 					new SelectionModelStateSerializer(getCatalog(), getSelectionModel()).deserialize(monitor,
 							getConfiguration().getInitialState(), getConfiguration().getInitialOperationByNodeId());
 				}
 			});
 		} catch (InvocationTargetException e) {
 			throw new CoreException(
 					MarketplaceClientUi.computeStatus(e, Messages.MarketplaceViewer_unexpectedException));
 		} catch (InterruptedException e) {
 			// user canceled
 			throw new CoreException(Status.CANCEL_STATUS);
 		}
 		for (Entry<CatalogItem, Operation> entry : getSelectionModel().getItemToOperation().entrySet()) {
 			if (entry.getValue() != Operation.NONE) {
 				entry.getKey().setSelected(true);
 			}
 		}
 	}
 
 	boolean wantInitializeInitialSelection() {
 		return !initialSelectionInitialized
 				&& (getConfiguration().getInitialState() != null || getConfiguration().getInitialOperationByNodeId() != null);
 	}
 
 	@Override
 	public boolean canFinish() {
 		if (computeMustCheckLicenseAcceptance()) {
 			if (acceptLicensesPage == null || !acceptLicensesPage.isPageComplete()) {
 				return false;
 			}
 		}
 		if (profileChangeOperation != null) {
 			IStatus resolutionResult = profileChangeOperation.getResolutionResult();
 			switch (resolutionResult.getSeverity()) {
 			case IStatus.OK:
 			case IStatus.WARNING:
 			case IStatus.INFO:
 				return true;
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public IWizardPage getNextPage(IWizardPage page) {
 		return getNextPage(page, true);
 	}
 
 	IWizardPage getNextPage(IWizardPage page, boolean computeChanges) {
 		if (page == featureSelectionWizardPage) {
 			if (profileChangeOperation == null) {
 				if (computeChanges) {
 					updateProfileChangeOperation();
 					if (profileChangeOperation != null) {
 						getContainer().updateButtons();
 					}
 				}
 				if (profileChangeOperation == null || !profileChangeOperation.getResolutionResult().isOK()) {
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
 
 	FeatureSelectionWizardPage getFeatureSelectionWizardPage() {
 		return featureSelectionWizardPage;
 	}
 
 	@Override
 	public IWizardPage getStartingPage() {
 		if (getConfiguration().getCatalogDescriptor() != null) {
 			if (wantInitializeInitialSelection()) {
 				return getFeatureSelectionWizardPage();
 			}
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
 			if (computeMustCheckLicenseAcceptance()) {
 				if (acceptLicensesPage != null && acceptLicensesPage.isPageComplete()) {
 					acceptLicensesPage.performFinish();
 				}
 			}
 			ProvisioningJob provisioningJob = profileChangeOperation.getProvisioningJob(null);
 			if (!operationNewInstallItems.isEmpty()) {
 				provisioningJob.addJobChangeListener(new ProvisioningJobListener(operationNewInstallItems));
 			}
 			ProvisioningUI.getDefaultUI().schedule(provisioningJob, StatusManager.SHOW | StatusManager.LOG);
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public MarketplacePage getCatalogPage() {
 		return (MarketplacePage) super.getCatalogPage();
 	}
 
 	public synchronized Set<String> getInstalledFeatures() {
 		if (installedFeatures == null) {
 			try {
 				if (Display.getCurrent() != null) {
 					getContainer().run(true, false, new IRunnableWithProgress() {
 						public void run(IProgressMonitor monitor) throws InvocationTargetException,
 								InterruptedException {
 							installedFeatures = MarketplaceClientUi.computeInstalledFeatures(monitor);
 						}
 					});
 				} else {
 					installedFeatures = MarketplaceClientUi.computeInstalledFeatures(new NullProgressMonitor());
 				}
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
 
 	public void openUrl(String url) {
 		CatalogDescriptor catalogDescriptor = getConfiguration().getCatalogDescriptor();
 		URL catalogUrl = catalogDescriptor.getUrl();
 		URI catalogUri;
 		try {
 			catalogUri = catalogUrl.toURI();
 		} catch (URISyntaxException e) {
 			// should never happen
 			throw new IllegalStateException(e);
 		}
 		if (WorkbenchBrowserSupport.getInstance().isInternalWebBrowserAvailable()
 				&& url.toLowerCase().startsWith(catalogUri.toString().toLowerCase())) {
 			int style = IWorkbenchBrowserSupport.AS_EDITOR | IWorkbenchBrowserSupport.LOCATION_BAR
 					| IWorkbenchBrowserSupport.NAVIGATION_BAR;
 			String browserId = "MPC-" + catalogUri.toString().replaceAll("[^a-zA-Z0-9_-]", "_"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 			try {
 				IWebBrowser browser = WorkbenchBrowserSupport.getInstance().createBrowser(style, browserId,
 						catalogDescriptor.getLabel(), catalogDescriptor.getDescription());
 				final String originalUrl = url;
 				if (url.indexOf('?') == -1) {
 					url += '?';
 				} else {
 					url += '&';
 				}
 				String state = new SelectionModelStateSerializer(getCatalog(), getSelectionModel()).serialize();
 				url += "mpc=true&mpc_state=" + URLEncoder.encode(state, "UTF-8"); //$NON-NLS-1$//$NON-NLS-2$
 				browser.openURL(new URL(url)); // ORDER DEPENDENCY
 				getContainer().getShell().close();
 				if (!hookLocationListener(browser)) { // ORDER DEPENDENCY
 					browser.openURL(new URL(originalUrl));
 				}
 			} catch (PartInitException e) {
 				StatusManager.getManager().handle(e.getStatus(),
 						StatusManager.SHOW | StatusManager.BLOCK | StatusManager.LOG);
 			} catch (MalformedURLException e) {
 				IStatus status = new Status(IStatus.ERROR, MarketplaceClientUi.BUNDLE_ID, NLS.bind(
 						Messages.MarketplaceWizard_cannotOpenUrl, new Object[] { url, e.getMessage() }), e);
 				StatusManager.getManager().handle(status, StatusManager.SHOW | StatusManager.BLOCK | StatusManager.LOG);
 			} catch (UnsupportedEncodingException e) {
 				throw new IllegalStateException(e); // should never happen
 			}
 		} else {
 			WorkbenchUtil.openUrl(url, IWorkbenchBrowserSupport.AS_EXTERNAL);
 		}
 	}
 
 	private boolean hookLocationListener(IWebBrowser webBrowser) {
 		try {
 			Field partField = findField(webBrowser.getClass(), "part", IWorkbenchPart.class); //$NON-NLS-1$
 			if (partField != null) {
 				partField.setAccessible(true);
 				IWorkbenchPart part = (IWorkbenchPart) partField.get(webBrowser);
 				if (part != null) {
 					Field browserViewerField = findField(part.getClass(), "webBrowser", null); //$NON-NLS-1$
 					if (browserViewerField != null) {
 						browserViewerField.setAccessible(true);
 						Object browserViewer = browserViewerField.get(part);
 						if (browserViewer != null) {
 							Field browserField = findField(browserViewer.getClass(), "browser", Browser.class); //$NON-NLS-1$
 							if (browserField != null) {
 								browserField.setAccessible(true);
 								Browser browser = (Browser) browserField.get(browserViewer);
 								if (browser != null) {
 									// only hook the listener once
 									if (browser.getData(MarketplaceBrowserIntegration.class.getName()) == null) {
 										if (browserListener == null) {
 											browserListener = new MarketplaceBrowserIntegration(
 													getConfiguration().getCatalogDescriptors(),
 													getConfiguration().getCatalogDescriptor());
 										}
 										browser.setData(MarketplaceBrowserIntegration.class.getName(), browserListener);
 										// hook in listeners
 										browser.addLocationListener(browserListener);
 										browser.addOpenWindowListener(browserListener);
 									}
 									return true;
 								}
 							}
 						}
 					}
 				}
 			}
 		} catch (Throwable t) {
 			// ignore
 		}
 		return false;
 	}
 
 	private Field findField(Class<?> clazz, String fieldName, Class<?> fieldClass) {
 		while (clazz != Object.class) {
 			for (Field field : clazz.getDeclaredFields()) {
 				if (field.getName().equals(fieldName)
 						&& (fieldClass == null || fieldClass.isAssignableFrom(field.getType()))) {
 					return field;
 				}
 			}
 			clazz = clazz.getSuperclass();
 		}
 		return null;
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
 				operationNewInstallItems = computeNewInstallCatalogItems();
 
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
 
 	private Set<CatalogItem> computeNewInstallCatalogItems() {
 		Set<CatalogItem> items = new HashSet<CatalogItem>();
 		Map<CatalogItem, Collection<String>> iusByCatalogItem = new HashMap<CatalogItem, Collection<String>>();
 		for (CatalogItemEntry entry : getSelectionModel().getCatalogItemEntries()) {
 			if (entry.getOperation() != Operation.INSTALL) {
 				continue;
 			}
 			List<FeatureEntry> features = entry.getChildren();
 			Collection<String> featureIds = new ArrayList<String>(features.size());
 			for (FeatureEntry feature : features) {
 				featureIds.add(feature.getFeatureDescriptor().getId());
 			}
 			iusByCatalogItem.put(entry.getItem(), featureIds);
 		}
 		for (IInstallableUnit unit : operationIUs) {
 			for (Entry<CatalogItem, Collection<String>> entry : iusByCatalogItem.entrySet()) {
 				if (entry.getValue().contains(unit.getId())) {
 					items.add(entry.getKey());
 				}
 			}
 		}
 		return items;
 	}
 
 	void initializeCatalog() {
 		for (AbstractDiscoveryStrategy strategy : getCatalog().getDiscoveryStrategies()) {
 			strategy.dispose();
 		}
 		getCatalog().getDiscoveryStrategies().clear();
 		if (getConfiguration().getCatalogDescriptor() != null) {
 			getCatalog().getDiscoveryStrategies().add(
 					new MarketplaceDiscoveryStrategy(getConfiguration().getCatalogDescriptor()));
 		}
 	}
 
 }
