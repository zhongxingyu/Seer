 /*******************************************************************************
  * Copyright (c) 2009, 2010 Tasktop Technologies and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Tasktop Technologies - initial API and implementation
  *******************************************************************************/
 package org.eclipse.epp.internal.mpc.ui.operations;
 
 import java.lang.reflect.InvocationTargetException;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.SubMonitor;
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.eclipse.epp.internal.mpc.ui.MarketplaceClientUi;
 import org.eclipse.equinox.internal.p2.discovery.model.CatalogItem;
 import org.eclipse.equinox.internal.p2.ui.discovery.util.WorkbenchUtil;
 import org.eclipse.equinox.p2.metadata.IInstallableUnit;
 import org.eclipse.equinox.p2.metadata.Version;
 import org.eclipse.equinox.p2.operations.InstallOperation;
 import org.eclipse.equinox.p2.operations.ProfileChangeOperation;
 import org.eclipse.equinox.p2.operations.UninstallOperation;
 import org.eclipse.equinox.p2.operations.UpdateOperation;
 import org.eclipse.equinox.p2.repository.metadata.IMetadataRepository;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.swt.widgets.Display;
 
 /**
  * A job that configures a p2 provisioning operation for installing/updating/removing one or more {@link CatalogItem
  * connectors}. The bulk of the installation work is done by p2; this class just sets up the p2 repository meta-data and
  * selects the appropriate features to install.
  * 
  * @author David Green
  * @author Steffen Pingel
  */
 public class ProvisioningOperation extends AbstractProvisioningOperation {
 
 	public enum OperationType {
 		/**
 		 * Install and update features
 		 */
 		INSTALL,
 		/**
 		 * update only, use {@link #INSTALL} if features are to be installed and updated in a single operation
 		 */
 		UPDATE,
 		/**
 		 * uninstall features
 		 */
 		UNINSTALL
 	}
 
 	private final OperationType operationType;
 
 	private final List<FeatureDescriptor> featureDescriptors;
 
 	/**
 	 * @param operationType
 	 *            the type of operation to perform
 	 * @param items
 	 *            the items for which features are being installed
 	 * @param featureDescriptors
 	 *            the features to install/update/uninstall, which must correspond to features provided by the given
 	 *            items
 	 */
 	public ProvisioningOperation(OperationType operationType, Collection<CatalogItem> items,
 			Set<FeatureDescriptor> featureDescriptors) {
 		super(items);
 		if (featureDescriptors == null || featureDescriptors.isEmpty()) {
 			throw new IllegalArgumentException();
 		}
 		this.featureDescriptors = new ArrayList<FeatureDescriptor>(featureDescriptors);
 		if (operationType == null) {
 			throw new IllegalArgumentException();
 		}
 		this.operationType = operationType;
 	}
 
 	public void run(IProgressMonitor progressMonitor) throws InvocationTargetException, InterruptedException {
 		try {
			SubMonitor monitor = SubMonitor.convert(progressMonitor,
					Messages.ProvisioningOperation_configuringProvisioningOperation, 100);
 			try {
 				final IInstallableUnit[] ius = computeInstallableUnits(monitor.newChild(50));
 
 				checkCancelled(monitor);
 
 				switch (operationType) {
 				case INSTALL: {
 					final InstallOperation operation = resolveInstall(monitor.newChild(50), ius,
 							repositoryLocations.toArray(new URI[0]));
 					checkCancelled(monitor);
 					Display.getDefault().asyncExec(new Runnable() {
 						public void run() {
 							provisioningUI.openInstallWizard(Arrays.asList(ius), operation, null);
 						}
 					});
 
 				}
 					break;
 				case UNINSTALL: {
 					final UninstallOperation operation = resolveUninstall(monitor.newChild(50), ius,
 							repositoryLocations.toArray(new URI[0]));
 					checkCancelled(monitor);
 					Display.getDefault().asyncExec(new Runnable() {
 						public void run() {
 							provisioningUI.openUninstallWizard(Arrays.asList(ius), operation, null);
 						}
 					});
 
 				}
 					break;
 				case UPDATE: {
 					final UpdateOperation operation = resolveUpdate(monitor.newChild(50), ius,
 							repositoryLocations.toArray(new URI[0]));
 					checkCancelled(monitor);
 					Display.getDefault().asyncExec(new Runnable() {
 						public void run() {
 							provisioningUI.openUpdateWizard(true, operation, null);
 						}
 					});
 				}
 					break;
 				default:
 					throw new UnsupportedOperationException(operationType.name());
 				}
 			} finally {
 				monitor.done();
 			}
 		} catch (OperationCanceledException e) {
 			throw new InterruptedException();
 		} catch (Exception e) {
 			throw new InvocationTargetException(e);
 		}
 	}
 
 	private InstallOperation resolveInstall(IProgressMonitor monitor, final IInstallableUnit[] ius, URI[] repositories)
 			throws CoreException {
 		InstallOperation operation = provisioningUI.getInstallOperation(Arrays.asList(ius), repositories);
 		resolveModal(monitor, operation);
 		return operation;
 	}
 
 	private UninstallOperation resolveUninstall(IProgressMonitor monitor, final IInstallableUnit[] ius,
 			URI[] repositories) throws CoreException {
 		UninstallOperation operation = provisioningUI.getUninstallOperation(Arrays.asList(ius), repositories);
 		resolveModal(monitor, operation);
 		return operation;
 	}
 
 	private UpdateOperation resolveUpdate(IProgressMonitor monitor, final IInstallableUnit[] ius, URI[] repositories)
 			throws CoreException {
 		UpdateOperation operation = provisioningUI.getUpdateOperation(Arrays.asList(ius), repositories);
 		resolveModal(monitor, operation);
 		return operation;
 	}
 
 	public void resolveModal(IProgressMonitor monitor, ProfileChangeOperation operation) throws CoreException {
		operation.resolveModal(new SubProgressMonitor(monitor, items.size()));
 	}
 
 	public IInstallableUnit[] computeInstallableUnits(SubMonitor monitor) throws CoreException {
 		try {
 			monitor.setWorkRemaining(100);
 			// add repository urls and load meta data
 			List<IMetadataRepository> repositories = addRepositories(monitor.newChild(50));
 			final List<IInstallableUnit> installableUnits = queryInstallableUnits(monitor.newChild(50), repositories);
 			removeOldVersions(installableUnits);
 			checkForUnavailable(installableUnits);
 			pruneUnselected(installableUnits);
 			return installableUnits.toArray(new IInstallableUnit[installableUnits.size()]);
 
 		} catch (URISyntaxException e) {
 			// should never happen, since we already validated URLs.
 			throw new CoreException(new Status(IStatus.ERROR, MarketplaceClientUi.BUNDLE_ID,
 					Messages.ProvisioningOperation_unexpectedErrorUrl, e));
 		} catch (MalformedURLException e) {
 			// should never happen, since we already validated URLs.
 			throw new CoreException(new Status(IStatus.ERROR, MarketplaceClientUi.BUNDLE_ID,
 					Messages.ProvisioningOperation_unexpectedErrorUrl, e));
 		} finally {
 			monitor.done();
 		}
 	}
 
 	private void pruneUnselected(List<IInstallableUnit> installableUnits) {
 		Set<String> installableFeatureIds = new HashSet<String>();
 		for (FeatureDescriptor descriptor : featureDescriptors) {
 			installableFeatureIds.add(descriptor.getId());
 		}
 		Iterator<IInstallableUnit> it = installableUnits.iterator();
 		while (it.hasNext()) {
 			IInstallableUnit iu = it.next();
 			if (!installableFeatureIds.contains(iu.getId())) {
 				it.remove();
 			}
 		}
 	}
 
 	/**
 	 * Verifies that we found what we were looking for: it's possible that we have connector descriptors that are no
 	 * longer available on their respective sites. In that case we must inform the user. Unfortunately this is the
 	 * earliest point at which we can know.
 	 */
 	private void checkForUnavailable(final List<IInstallableUnit> installableUnits) throws CoreException {
 		// at least one selected connector could not be found in a repository
 		Set<String> foundIds = new HashSet<String>();
 		for (IInstallableUnit unit : installableUnits) {
 			foundIds.add(unit.getId());
 		}
 
 		String message = ""; //$NON-NLS-1$
 		String detailedMessage = ""; //$NON-NLS-1$
 		for (CatalogItem descriptor : items) {
 			StringBuilder unavailableIds = null;
 			for (String id : getFeatureIds(descriptor)) {
 				if (!foundIds.contains(id)) {
 					if (unavailableIds == null) {
 						unavailableIds = new StringBuilder();
 					} else {
 						unavailableIds.append(Messages.ProvisioningOperation_commaSeparator);
 					}
 					unavailableIds.append(id);
 				}
 			}
 			if (unavailableIds != null) {
 				if (message.length() > 0) {
 					message += Messages.ProvisioningOperation_commaSeparator;
 				}
 				message += descriptor.getName();
 
 				if (detailedMessage.length() > 0) {
 					detailedMessage += Messages.ProvisioningOperation_commaSeparator;
 				}
				detailedMessage += NLS.bind(Messages.ProvisioningOperation_unavailableFeatures, new Object[] {
						descriptor.getName(), unavailableIds.toString(), descriptor.getSiteUrl() });
 			}
 		}
 
 		if (message.length() > 0) {
 			// instead of aborting here we ask the user if they wish to proceed anyways
 			final boolean[] okayToProceed = new boolean[1];
 			final String finalMessage = message;
 			Display.getDefault().syncExec(new Runnable() {
 				public void run() {
					okayToProceed[0] = MessageDialog.openQuestion(WorkbenchUtil.getShell(),
							Messages.ProvisioningOperation_proceedQuestion, NLS.bind(
 									Messages.ProvisioningOperation_unavailableConnectors_proceedQuestion,
 									new Object[] { finalMessage }));
 				}
 			});
 			if (!okayToProceed[0]) {
 				throw new CoreException(new Status(IStatus.ERROR, MarketplaceClientUi.BUNDLE_ID, NLS.bind(
 						Messages.ProvisioningOperation_unavailableConnectors, detailedMessage), null));
 			}
 		}
 	}
 
 	/**
 	 * Filters those installable units that have a duplicate in the list with a higher version number. it's possible
 	 * that some repositories will host multiple versions of a particular feature. we assume that the user wants the
 	 * highest version.
 	 */
 	private void removeOldVersions(final List<IInstallableUnit> installableUnits) {
 		Map<String, Version> symbolicNameToVersion = new HashMap<String, Version>();
 		for (IInstallableUnit unit : installableUnits) {
 			Version version = symbolicNameToVersion.get(unit.getId());
 			if (version == null || version.compareTo(unit.getVersion()) == -1) {
 				symbolicNameToVersion.put(unit.getId(), unit.getVersion());
 			}
 		}
 		if (symbolicNameToVersion.size() != installableUnits.size()) {
 			for (IInstallableUnit unit : new ArrayList<IInstallableUnit>(installableUnits)) {
 				Version version = symbolicNameToVersion.get(unit.getId());
 				if (!version.equals(unit.getVersion())) {
 					installableUnits.remove(unit);
 				}
 			}
 		}
 	}
 
 }
