 /*******************************************************************************
  * Copyright (c) 18 jan. 2013 NetXForge.
  * 
  * This program is free software: you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later
  * version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details. You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>
  * 
  * Contributors: Christophe Bouhier - initial API and implementation and/or
  * initial documentation
  *******************************************************************************/
 package com.netxforge.netxstudio.models.importer.ui;
 
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.IJobChangeEvent;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.core.runtime.jobs.JobChangeAdapter;
 import org.eclipse.emf.cdo.CDOObject;
 import org.eclipse.emf.cdo.CDOState;
 import org.eclipse.emf.cdo.transaction.CDOTransaction;
 import org.eclipse.emf.cdo.util.CDOUtil;
 import org.eclipse.emf.cdo.util.CommitException;
 import org.eclipse.emf.cdo.view.CDOObjectHandler;
 import org.eclipse.emf.cdo.view.CDOView;
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.TreeIterator;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
 import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.ITreeContentProvider;
 import org.eclipse.jface.window.Window;
 import org.eclipse.jface.wizard.Wizard;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IImportWizard;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.dialogs.CheckedTreeSelectionDialog;
 import org.eclipse.ui.progress.IProgressConstants;
 
 import com.google.common.collect.BiMap;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Lists;
 import com.google.inject.Inject;
 import com.netxforge.netxstudio.common.model.ModelUtils;
 import com.netxforge.netxstudio.data.IDataProvider;
 import com.netxforge.netxstudio.data.cdo.NonStatic;
 import com.netxforge.netxstudio.library.Component;
 import com.netxforge.netxstudio.library.LibraryPackage;
 import com.netxforge.netxstudio.library.NetXResource;
 import com.netxforge.netxstudio.models.importer.MasterDataImporterJob;
 import com.netxforge.netxstudio.models.importer.OldIDLookupService;
 import com.netxforge.netxstudio.models.importer.ui.internal.ImportUIActivator;
 import com.netxforge.netxstudio.operators.Operator;
 import com.netxforge.netxstudio.operators.OperatorsPackage;
 import com.netxforge.netxstudio.scheduling.SchedulingPackage;
 import com.netxforge.netxstudio.screens.editing.EMFEditingService;
 import com.netxforge.netxstudio.services.ServicesPackage;
 
 /**
  * 
  * @author Christophe Bouhier
  * 
  */
 public abstract class AbstractImportWizard extends Wizard implements
 		IImportWizard {
 
 	private IStructuredSelection selection;
 	private ImportResourceWizardPage dbImportResourcePage;
 
 	public AbstractImportWizard() {
 	}
 
 	@Inject
 	@NonStatic
 	private IDataProvider dataProvider;
 
 	@Inject
 	private IDataProvider uiDataProvider;
 
 	@Inject
 	private ModelUtils modelUtils;
 
 	private ITreeContentProvider importContentProvider;
 
 	abstract EPackage[] getEPackages();
 
 	abstract boolean useIndexed();
 
 	@Override
 	public boolean performFinish() {
 
 		final IPath inFilePath = dbImportResourcePage.getPath();
 		boolean indexed = dbImportResourcePage.isIndexed();
 		// Implement the import.
 
 		final MasterDataImporterJob job = new MasterDataImporterJob(
 				dataProvider, getEPackages());
 
 		job.addNotifier(new JobChangeAdapter() {
 			@Override
 			public void done(IJobChangeEvent event) {
 				super.done(event);
 				Display.getDefault().syncExec(new Runnable() {
 
 					public void run() {
 
 						// Consider saving this info locally as well.
 						final List<EObject> results = job.getResults();
 
 						Map<String, EObject> runIndex = job.getRunIndex();
 						// present the result to the user.
 
 						if (results != null && results.size() > 0) {
 							// Does our injector proxy work?
 							if (importContentProvider == null) {
 								importContentProvider = new ImportResultTreeContentProvider();
 							}
 
 							CheckedTreeSelectionDialog dialog = new CheckedTreeSelectionDialog(
 									Display.getDefault().getActiveShell(),
 									new ImportResultLabelProvider(
 											EMFEditingService
 													.getAdapterFactory()),
 									importContentProvider);
 							dialog.setTitle("Result of the import");
 							dialog.setMessage("Select which items to import");
 							dialog.setBlockOnOpen(true);
 							dialog.setInput(results);
 							dialog.setContainerMode(true);
 							int result = dialog.open();
 							if (result == Window.OK) {
 								final Object[] selections = dialog.getResult();
 
 								final StoreSelectionJob storeSelection = new StoreSelectionJob(
 										true);
 								storeSelection.storeJobParameters(results,
 										selections, runIndex);
 								storeSelection.schedule(100);
 
 							}
 						} else {
 							if (ImportUIActivator.DEBUG) {
 								ImportUIActivator.TRACE.trace(
 										ImportUIActivator.TRACE_IMPORT_OPTION,
 										"No result to process");
 							}
 						}
 					}
 				});
 			}
 		});
 		job.setIndexed(indexed);
 		job.setIPathToProcess(inFilePath);
 		job.go(); // Should spawn a job processing the import file.
 		return true;
 	}
 
 	class PrintObject {
 		ComposedAdapterFactory af;
 		AdapterFactoryLabelProvider lp;
 
 		PrintObject(ComposedAdapterFactory af) {
 			this.af = af;
 			lp = new AdapterFactoryLabelProvider(af);
 		}
 
 		String getPrint(Object object) {
 			return lp.getText(object);
 		}
 	}
 
 	/**
 	 * 
 	 * @param listOfObjectsToStore
 	 */
 	private void unsetExternal(List<EObject> listOfObjectsToStore) {
 		for (EObject object : listOfObjectsToStore) {
 			TreeIterator<EObject> eAllContents = object.eAllContents();
 			while (eAllContents.hasNext()) {
 				EObject next = eAllContents.next();
 				EList<EReference> eAllReferences = next.eClass()
 						.getEAllReferences();
 				for (EReference eRef : eAllReferences) {
 					if (!next.eIsSet(eRef)) {
 						// continue, only filled ERefs.
 						continue;
 					}
 					if (eRef.isMany()) {
 						Object eGet = next.eGet(eRef);
 
 						@SuppressWarnings("unchecked")
 						final List<? extends EObject> collection = (List<? extends EObject>) eGet;
 
 						final List<EObject> toRemove = Lists.newArrayList();
 						for (EObject eo : collection) {
 							if (isExternal(eo, eRef)) {
 								if (ImportUIActivator.DEBUG) {
 									ImportUIActivator.TRACE
 											.trace(ImportUIActivator.TRACE_IMPORT_OPTION,
 													"-- cleaning external ref from object"
 															+ eo
 															+ "with reference"
 															+ eRef);
 									;
 								}
 
 								int index = collection.indexOf(eo);
 								toRemove.add(collection.get(index));
 							}
 						}
 
 						for (EObject eo : toRemove) {
 							collection.remove(eo);
 						}
 
 					} else {
 						final EObject eo = (EObject) next.eGet(eRef);
 						if (isExternal(eo, eRef)) {
 							if (ImportUIActivator.DEBUG) {
 								ImportUIActivator.TRACE.trace(
 										ImportUIActivator.TRACE_IMPORT_OPTION,
 										"-- cleaning external ref from object"
 												+ eo + "with reference" + eRef);
 								;
 							}
 							CDOUtil.cleanStaleReference(next, eRef);
 							// this.unsetDangling(next, eRef);
 						}
 					}
 				}
 			}
 		}
 
 	}
 
 	private boolean isExternal(EObject eo, EReference eRef) {
 		if (eo instanceof CDOObject) {
 			CDOObject cdoObject = CDOUtil.getCDOObject(eo);
 			if (cdoObject != null && cdoObject.cdoID() != null) {
 				return cdoObject.cdoID().isExternal();
 			} else {
 				return true;
 			}
 
 		}
 		return false;
 	}
 
 	/**
 	 * Unset dangling references. DO NOT USE, THIS CODE IS NOT CORRECT, the
 	 * dangling check is based on a non-null eResource. In our case, it is
 	 * non-null, but external.
 	 * 
 	 * @param listOfObjectsToStore
 	 * @deprecated
 	 */
 	@SuppressWarnings("unchecked")
 	private void unsetDangling(List<EObject> listOfObjectsToStore) {
 		for (EObject object : listOfObjectsToStore) {
 			TreeIterator<EObject> eAllContents = object.eAllContents();
 			while (eAllContents.hasNext()) {
 				EObject next = eAllContents.next();
 				EList<EReference> eAllReferences = next.eClass()
 						.getEAllReferences();
 				for (EReference eRef : eAllReferences) {
 					if (!next.eIsSet(eRef)) {
 						// continue, only filled ERefs.
 						continue;
 					}
 					if (eRef.isMany()) {
 						Object eGet = next.eGet(eRef);
 						List<? extends EObject> collection = (List<? extends EObject>) eGet;
 						List<EObject> toRemove = Lists.newArrayList();
 						for (EObject eo : collection) {
 							if (isDangling(eo, eRef)) {
 								int index = collection.indexOf(eo);
 								toRemove.add(collection.get(index));
 							}
 						}
 
 						for (EObject eo : toRemove) {
 							collection.remove(eo);
 						}
 
 					} else {
 						Object eGet = next.eGet(eRef);
 						EObject eo = (EObject) eGet;
 						if (isDangling(eo, eRef)) {
 							this.unsetDangling(next, eRef);
 						}
 					}
 				}
 			}
 		}
 	}
 
 	public boolean isDangling(EObject eo, EReference eRef) {
 		if (eo.eResource() == null) {
 			System.out.println("Dangling object: " + eo.eClass()
 					+ " from ref: " + eRef);
 			return true;
 		}
 		return false;
 	}
 
 	public void unsetDangling(EObject eo, EReference eRef) {
 		if (eo.eIsSet(eRef)) {
 			eo.eUnset(eRef);
 		}
 	}
 
 	@SuppressWarnings("unused")
 	private boolean equalObject(EList<EObject> parentList, EObject targetObject) {
 
 		boolean already = false;
 		for (EObject object : parentList) {
 			if (EcoreUtil.equals(object, targetObject)) {
 				already = true;
 				break;
 			}
 		}
 		return already;
 	}
 
 	private List<?> getContainerList(EClass key) {
 
 		final Resource res = getResource(key);
 
 		// ResourceSet set = res.getResourceSet();
 		// if (set != null) {
 		// System.out.println("importing in set: " + set.toString());
 		//
 		// }
 
 		// RFSService objects are stored in an Operator.
 		if (key.equals(ServicesPackage.eINSTANCE.getRFSService())) {
 			if (res.getContents().size() > 0) {
 				// Store in the first one.
 				Operator op = (Operator) res.getContents().get(0);
 				return op.getServices();
 			}
 		}
 		return res.getContents();
 	}
 
 	private Resource getResource(EClass eClass) {
 
 		if (eClass.equals(ServicesPackage.Literals.RFS_SERVICE)) {
 
 			// return dataProvider.getTransaction().createResource("/" +
 			// OperatorsPackage.eINSTANCE
 			// .getOperator().getName());
 			return dataProvider.getResource(OperatorsPackage.eINSTANCE
 					.getOperator());
 		} else if (superExtendsEClass(eClass, SchedulingPackage.Literals.JOB)) {
 			return dataProvider.getResource(SchedulingPackage.Literals.JOB);
 		}
 
 		return dataProvider.getResource(eClass);
 	}
 
 	private boolean superExtendsEClass(EClass toCheck, EClass eClass) {
 		for (EClass superType : toCheck.getEAllSuperTypes()) {
 			if (superType.equals(eClass)) {
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	@SuppressWarnings("unused")
 	private boolean contains(Object targetObject, Object original) {
 		return EcoreUtil.isAncestor((EObject) original, (EObject) targetObject);
 	}
 
 	public void init(IWorkbench workbench, IStructuredSelection selection) {
 		this.selection = selection;
 		setWindowTitle(Messages.ImportWizard_4);
 		//		setDefaultPageImageDescriptor(Activator.getImageDescriptor("icons/wizban/ExportDBWizard.gif")); //$NON-NLS-1$
 		setNeedsProgressMonitor(true);
 	}
 
 	@Override
 	public void addPages() {
 
 		dbImportResourcePage = new ImportResourceWizardPage(
 				Messages.ImportWizard_5);
 		dbImportResourcePage.setTitle(Messages.ImportWizard_0);
 		dbImportResourcePage.setDescription(Messages.ImportWizard_6);
 		dbImportResourcePage.init(selection);
 		this.addPage(dbImportResourcePage);
 
 	}
 
 	public class ImportResultLabelProvider extends AdapterFactoryLabelProvider {
 		public ImportResultLabelProvider(AdapterFactory adapterFactory) {
 			super(adapterFactory);
 		}
 
 		@Override
 		public String getText(Object object) {
 			return super.getText(object);
 		}
 	}
 
 	/**
 	 * A {@link Job} for storing a collection of objects.
 	 * 
 	 * @author Christophe Bouhier
 	 */
 	class StoreSelectionJob extends Job {
 
 		/** The user selection after parsing */
 		private Object[] selection;
 
 		/** The import run index */
 		private Map<String, EObject> runIndex;
 
 		/** The collection of object which are target before user selection */
 		private List<EObject> results;
 
 		/** Our progress monitor */
 		private IProgressMonitor monitor;
 
 		public StoreSelectionJob(boolean userJob) {
 			super("Store data job");
 			setUser(userJob);
 		}
 
 		@Override
 		protected IStatus run(IProgressMonitor monitor) {
 			return processJob(monitor);
 		}
 
 		private IStatus processJob(IProgressMonitor monitor) {
 
 			this.monitor = monitor;
 			// Do some duration calculation:
 
 			// the actual execution
 			storeForSameEClass();
 
 			if (isModal(this)) {
 				// The progress dialog is still open so
 				// just open the message
 				showResults();
 			} else {
 				setProperty(IProgressConstants.KEEP_PROPERTY, Boolean.TRUE);
 				setProperty(IProgressConstants.ACTION_PROPERTY,
 						getImportCompletedAction());
 			}
 
 			return Status.OK_STATUS;
 		}
 
 		public void storeJobParameters(List<EObject> results,
 				Object[] selection, Map<String, EObject> runIndex) {
 
 			this.results = results;
 			this.selection = selection;
 			this.runIndex = runIndex;
 
 		}
 
 		/**
 		 * Store the selection of items. As each root object will be of type
 		 * EClass, we deflat the
 		 * 
 		 * @param results
 		 * 
 		 * 
 		 * @param selection
 		 * @param runIndex
 		 */
 		private void storeForSameEClass() {
 
 			String server;
 
 			if (uiDataProvider != null) {
 				server = uiDataProvider.getServer();
 			} else {
 				return;
 			}
 
 			dataProvider.setDoGetResourceFromOwnTransaction(false);
 			// FIXME THIS WILL OPEN WITH CURRENT CREDENTIALS,
 			// REGARDLESS ALL IS PERMITTED NOW.
 			dataProvider.openSession("admin", "admin", server);
 			final CDOTransaction transaction = dataProvider.getTransaction();
 
 			if (ImportUIActivator.DEBUG) {
 				ImportUIActivator.TRACE.trace(
 						ImportUIActivator.TRACE_IMPORT_OPTION,
 						"Storing object selected objects. ");
 			}
 
 			// Optionally apply the selection.
 			List<EObject> listOfObjectsToStore;
 			if (selection != null) {
 				listOfObjectsToStore = applyUserSelection();
 			} else {
 				listOfObjectsToStore = results;
 			}
 
 			// Stuff the objects in the resources
 			resultToResources(listOfObjectsToStore, transaction);
 
 			// This code checks for a non-null eResource in a referenced object.
 			// that will
 			// be non-dangling, but it will also mean it will try to proxy
 			// resolve
 			// as an external file reference. for the
 			// "temp" resource we created.
 			unsetDangling(listOfObjectsToStore);
 
 			// A selection of an integer object graph, will break the graph,
 			// and
 			// leave
 			// it with unresolved/dangling references. As such the references
 			// are
 			// not dangling and
 			// will be resolved as external.
 			// To avoid resolving them as external, we should break these
 			// references.
 			// The reference can be register to which old OID these existed.
 			// when the object with the old OID becomes available, we can
 			// restore
 			// it.
 
 			unsetExternal(listOfObjectsToStore);
 
 			// Cast to a bimap for inverse value lookup. Only works with 1:1
 			// relation key:value.
 			final BiMap<String, EObject> indexBiMap = (BiMap<String, EObject>) runIndex;
 
 			transaction.addObjectHandler(new CDOObjectHandler() {
 				public void objectStateChanged(CDOView view, CDOObject object,
 						CDOState oldState, CDOState newState) {
 					// Any object moving to the CLEAN state will have a usable
 					// OID
 					// for reference
 					if (oldState == CDOState.NEW && newState == CDOState.CLEAN) {
 						String oldID = indexBiMap.inverse().get(object);
 						if (ImportUIActivator.DEBUG) {
 							ImportUIActivator.TRACE
 									.trace(ImportUIActivator.TRACE_IMPORT_OPTION,
 											"Committed object detected in the import run index, adding to ID mapper. ");
 
 							ImportUIActivator.TRACE.trace(
 									ImportUIActivator.TRACE_IMPORT_OPTION,
 									"oldID " + oldID + " newID"
 											+ object.cdoID());
 
 							OldIDLookupService.getInstance().register(oldID,
 									object.cdoID());
 						}
 					}
 				}
 			});
 
 			transaction.setCommitComment("Manual import");
 			try {
 				transaction.commit(monitor);
 			} catch (CommitException e) {
 				e.printStackTrace();
 			} finally {
 
 				dataProvider.setDoGetResourceFromOwnTransaction(true);
 				dataProvider.closeSession();
 			}
 		}
 
 		/**
 		 * @param listOfObjectsToStore
 		 * @param transaction
 		 */
 		private void resultToResources(List<EObject> listOfObjectsToStore,
 				CDOTransaction transaction) {
 			for (EObject object : listOfObjectsToStore) {
 
 				// Special treatment for NetXResource.
 				// If there is a component reference, put it in it's own CDO
 				// Resource,
 				// otherwise in the NetXResource.class CDO Resource
 				if (object instanceof NetXResource) {
 
 					NetXResource netXResource = (NetXResource) object;
 					Component componentRef = netXResource.getComponentRef();
 					Resource resource = null;
 					if (componentRef != null) {
 
 						// Remove later:
 						// String cdoResourcePath;
 						// try {
 						// cdoResourcePath = modelUtils
 						// .cdoCalculateResourceName(componentRef);
 						// resource = dataProvider
 						// .getResource(cdoResourcePath);
 						//
 						// } catch (IllegalAccessException e) {
 						// if (ImportUIActivator.DEBUG) {
 						// ImportUIActivator.TRACE.trace(
 						// ImportUIActivator.TRACE_IMPORT_OPTION,
 						// "Attempt to deduce a name from an invalid object: "
 						// + componentRef);
 						// }
 						// }
 						resource = modelUtils.cdoResourceForNetXResource(
 								componentRef, transaction);
 
 					} else {
 						resource = dataProvider
 								.getResource(LibraryPackage.Literals.NET_XRESOURCE);
 
 					}
 					if (resource != null) {
 						resource.getContents().add(netXResource);
 					}
 					resource.getContents().add(object);
 					continue;
 
 				}
 				@SuppressWarnings("unchecked")
 				final EList<EObject> parentList = (EList<EObject>) getContainerList(object
 						.eClass());
 
 				parentList.add(object);
 			}
 		}
 
 		/**
 		 * @return
 		 */
 		private List<EObject> applyUserSelection() {
			List<Object> selectionList = ImmutableList.of(selection);
 			List<EObject> listOfObjectsToStore = Lists.newArrayList();
 
 			// Get the result object tree, matching the selection.
 			for (Object sel : selectionList) {
 				if (sel instanceof EClass) {
 					for (EObject rootObject : results) {
 						if (rootObject.eClass().getName()
 								.equalsIgnoreCase(((EClass) sel).getName())) {
 							listOfObjectsToStore.add(rootObject);
 						}
 					}
 				}
 			}
 			return listOfObjectsToStore;
 		}
 
 		public boolean isModal(Job job) {
 			Boolean isModal = (Boolean) job
 					.getProperty(IProgressConstants.PROPERTY_IN_DIALOG);
 			if (isModal == null)
 				return false;
 			return isModal.booleanValue();
 		}
 
 		protected void showResults() {
 			Display.getDefault().asyncExec(new Runnable() {
 				public void run() {
 					getImportCompletedAction().run();
 				}
 			});
 		}
 
 		protected Action getImportCompletedAction() {
 			return new Action("Import completed") {
 				public void run() {
 					MessageDialog.openInformation(Display.getDefault()
 							.getActiveShell(), "Export completed",
 							"The import of data completed.");
 				}
 			};
 		}
 
 	}
 
 }
