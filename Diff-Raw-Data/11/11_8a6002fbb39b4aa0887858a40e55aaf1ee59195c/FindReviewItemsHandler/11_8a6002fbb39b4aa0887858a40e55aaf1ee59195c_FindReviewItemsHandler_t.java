 // $codepro.audit.disable com.instantiations.assist.eclipse.analysis.audit.rule.effectivejava.alwaysOverridetoString.alwaysOverrideToString, com.instantiations.assist.eclipse.analysis.deserializeabilitySecurity, com.instantiations.assist.eclipse.analysis.enforceCloneableUsageSecurity
 /*******************************************************************************
  * Copyright (c) 2010 Ericsson Research Canada
  * 
  * All rights reserved. This program and the accompanying materials are
  * made available under the terms of the Eclipse Public License v1.0 which
  * accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Description:
  * 
  * This class implements the context-sensitive command to find review items
  * in the parent project to add to the review
  * 
  * Contributors:
  *   Sebastien Dubois - Created for Mylyn Review R4E project
  *   
  ******************************************************************************/
 
 package org.eclipse.mylyn.reviews.r4e.ui.internal.commands;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.eclipse.compare.rangedifferencer.RangeDifference;
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IPackageFragment;
 import org.eclipse.jdt.core.IPackageFragmentRoot;
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.jface.viewers.AbstractTreeViewer;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EContextType;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EFileVersion;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EFormalReview;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReview;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewComponent;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewPhase;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewType;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.impl.OutOfSyncException;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.impl.ResourceHandlingException;
 import org.eclipse.mylyn.reviews.r4e.core.rfs.spi.IRFSRegistry;
 import org.eclipse.mylyn.reviews.r4e.core.rfs.spi.RFSRegistryFactory;
 import org.eclipse.mylyn.reviews.r4e.core.rfs.spi.ReviewsFileStorageException;
 import org.eclipse.mylyn.reviews.r4e.ui.R4EUIPlugin;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.editors.R4ECompareEditorInput;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIPosition;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIDeltaContainer;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIFileContext;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIModelController;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIReviewBasic;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIReviewItem;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUITextPosition;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.preferences.PreferenceConstants;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.CommandUtils;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.Diff;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.DiffUtils;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.MailServicesProxy;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.R4EUIConstants;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.UIUtils;
 import org.eclipse.mylyn.versions.core.Change;
 import org.eclipse.mylyn.versions.core.ChangeSet;
 import org.eclipse.mylyn.versions.core.ScmArtifact;
 import org.eclipse.mylyn.versions.ui.ScmUi;
 import org.eclipse.mylyn.versions.ui.spi.ScmConnectorUi;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.team.core.history.IFileRevision;
 import org.eclipse.ui.handlers.HandlerUtil;
 
 /**
  * @author lmcdubo
  * @version $Revision: 1.0 $
  */
 public class FindReviewItemsHandler extends AbstractHandler {
 
 	// ------------------------------------------------------------------------
 	// Methods
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Method execute.
 	 * 
 	 * @param event
 	 *            ExecutionEvent
 	 * @return Object
 	 * @throws ExecutionException
 	 * @see org.eclipse.core.commands.IHandler#execute(ExecutionEvent)
 	 */
 	public Object execute(final ExecutionEvent event) {
 
 		// Get project to use (use adapters if needed)
 		final ISelection selection = HandlerUtil.getCurrentSelection(event);
 		if (selection instanceof IStructuredSelection) {
 			final Object selectedElement = ((IStructuredSelection) selection).getFirstElement();
 			IProject project = null;
 
 			// NOTE: The valadity testes are done if the ProjectPropertyTester class
 			if (selectedElement instanceof IProject) {
 				project = (IProject) selectedElement;
 			} else if (R4EUIPlugin.isJDTAvailable() && selectedElement instanceof IJavaProject) {
 				project = ((IJavaProject) selectedElement).getProject();
 			} else if (R4EUIPlugin.isCDTAvailable() && selectedElement instanceof org.eclipse.cdt.core.model.ICProject) {
 				project = ((org.eclipse.cdt.core.model.ICProject) selectedElement).getProject();
 			} else if (selectedElement instanceof IPackageFragment || selectedElement instanceof IPackageFragmentRoot) {
 				project = ((IJavaElement) selectedElement).getJavaProject().getProject();
 			} else if (selectedElement instanceof IFolder) {
 				project = ((IFolder) selectedElement).getProject();
 			} else if (selectedElement instanceof IAdaptable) {
 				final IAdaptable adaptableProject = (IAdaptable) selectedElement;
 				project = (IProject) adaptableProject.getAdapter(IProject.class);
 			} else {
 				// Should never happen
 				R4EUIPlugin.Ftracer.traceError("No project defined for selection of class "
 						+ selectedElement.getClass());
 				R4EUIPlugin.getDefault().logError(
 						"No project defined for selection of class " + selectedElement.getClass(), null);
 				final ErrorDialog dialog = new ErrorDialog(null, R4EUIConstants.DIALOG_TITLE_ERROR,
 						"Find Review Item Error", new Status(IStatus.ERROR, R4EUIPlugin.PLUGIN_ID, 0,
 								"No project defined for selection", null), IStatus.ERROR);
 				dialog.open();
 				return null;
 			}
 
 			final ScmConnectorUi uiConnector = ScmUi.getUiConnector(project);
 			if (null != uiConnector) {
 				R4EUIPlugin.Ftracer.traceDebug("Resolved Scm Ui connector: " + uiConnector);
 				R4EUIModelController.setJobInProgress(true); //Disable operations on UI
 				final ChangeSet changeSet = uiConnector.getChangeSet(null, project);
 				createReviewItem(event, changeSet);
 				R4EUIModelController.setJobInProgress(false);
 			} else {
 				// We could not find any version control system, thus no items
 				final String strProject = ((null == project) ? "null" : project.getName());
 				R4EUIPlugin.Ftracer.traceDebug("No Scm Ui connector found for project: " + strProject);
 				final ErrorDialog dialog = new ErrorDialog(null, R4EUIConstants.DIALOG_TITLE_WARNING,
 						"Cannot find new Review Items", new Status(IStatus.WARNING, R4EUIPlugin.PLUGIN_ID, 0,
 								"No SCM Connector detected for Project " + strProject, null), IStatus.WARNING);
 				dialog.open();
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Create and serialize the changeset in a Review Item
 	 * 
 	 * @param changeSet
 	 * @param event
 	 *            ExecutionEvent
 	 */
 	private void createReviewItem(final ExecutionEvent event, final ChangeSet changeSet) {
 
 		if (null == changeSet) {
 			R4EUIPlugin.Ftracer.traceInfo("Received null ChangeSet");
 			return;
 		}
 
 		final int size = changeSet.getChanges().size();
 		R4EUIPlugin.Ftracer.traceInfo("Received ChangeSet with " + size + " elements");
 		if (0 == size) {
 			return; // nothing to add
 		}
 
 		//Check if Review Item already exists
 		final R4EUIReviewBasic uiReview = R4EUIModelController.getActiveReview();
 		for (R4EUIReviewItem uiItem : uiReview.getReviewItems()) {
 			if (changeSet.getId().equals(uiItem.getItem().getRepositoryRef())) {
 				//The commit item already exists so ignore command
 				R4EUIPlugin.Ftracer.traceWarning("Review Item already exists.  Ignoring");
 				final ErrorDialog dialog = new ErrorDialog(null, R4EUIConstants.DIALOG_TITLE_WARNING,
 						"Cannot add Review Item", new Status(IStatus.WARNING, R4EUIPlugin.PLUGIN_ID, 0,
 								"Review Item already exists", null), IStatus.WARNING);
 				dialog.open();
 				R4EUIModelController.setJobInProgress(false);
 				return;
 			}
 		}
 
 		final IRFSRegistry localRepository;
 		try {
 			// Get handle to local storage repository
 			localRepository = RFSRegistryFactory.getRegistry(R4EUIModelController.getActiveReview().getReview());
 
 			//Create Synchronized list that will temporarly hold the elements to be added
 			final List<TempFileContext> filesToAddlist = Collections.synchronizedList(new ArrayList());
 
 			final Job job = new Job("Importing Files and Calculating Changes...") {
 				@Override
 				public IStatus run(IProgressMonitor monitor) {
 
 					monitor.beginTask("Importing Files and Calculating Changes...", changeSet.getChanges().size());
 					//Since importing files and calculating delta can take a while, we run this in a parallel job.  When it completes
 					//We update the UI with the new elements
 					for (final Change change : changeSet.getChanges()) {
 						try {
 							//If the task is cancelled, we break here and set currently imported elements
 							if (monitor.isCanceled()) {
 								monitor.done();
 								return Status.CANCEL_STATUS;
 							}
							monitor.subTask("Getting base and target artifacts.");
 							final ScmArtifact baseArt = change.getBase();
 							final ScmArtifact targetArt = change.getTarget();
 							if (null == baseArt && null == targetArt) {
 								R4EUIPlugin.Ftracer.traceDebug("Received a Change with no base and target in ChangeSet: "
 										+ changeSet.getId() + ", Date: " + changeSet.getDate().toString());
 							}
 
 							// Copy remote files to the local repository
 							R4EFileVersion baseLocalVersion = null;
 							R4EFileVersion targetLocalVersion = null;
 							if (null != baseArt) {
								monitor.subTask("Fetching file " + baseArt.getPath() + " (base) from remote repository");
 								baseLocalVersion = CommandUtils.copyRemoteFileToLocalRepository(localRepository,
 										baseArt);
 							}
 							if (null != targetArt) {
								monitor.subTask("Fetching file " + targetArt.getPath()
										+ " (target) from remote repository");
 								targetLocalVersion = CommandUtils.copyRemoteFileToLocalRepository(localRepository,
 										targetArt);
 							}
 
 							// Add File Context to the list to be added
 							TempFileContext file = new TempFileContext(localRepository, baseLocalVersion,
 									targetLocalVersion, CommandUtils.adaptType(change.getChangeType()));
 
 							//If configured, get deltas for this file
 							if (R4EUIPlugin.getDefault()
 									.getPreferenceStore()
 									.getBoolean(PreferenceConstants.P_USE_DELTAS)) {
								monitor.subTask("Computing differences for file " + file.toString());
 								updateFilesWithDeltas(file);
 							}
 							filesToAddlist.add(file);
 							monitor.worked(1);
 						} catch (final ReviewsFileStorageException e) {
 							R4EUIPlugin.Ftracer.traceError("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 							R4EUIPlugin.getDefault().logError("Exception: " + e.toString(), e);
 						} catch (final CoreException e) {
 							R4EUIPlugin.Ftracer.traceError("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 							R4EUIPlugin.getDefault().logError("Exception: " + e.toString(), e);
 						}
 					}
					monitor.subTask("Adding Review Item to R4E model");
 					Display.getDefault().asyncExec(new Runnable() {
 						public void run() {
 							//Now add elements to the UI model if there are any elements to add
 							synchronized (filesToAddlist) {
 								try {
 									final R4EUIReviewItem uiReviewItem;
 									if (filesToAddlist.size() > 0) {
 										uiReviewItem = uiReview.createCommitReviewItem(changeSet, null);
 
 										//Set focus to newly created element
 										final TreeViewer viewer = R4EUIModelController.getNavigatorView()
 												.getTreeViewer();
 										viewer.expandToLevel(uiReviewItem, AbstractTreeViewer.ALL_LEVELS);
 										viewer.setSelection(new StructuredSelection(uiReviewItem), true);
 									} else {
 										return;
 									}
 
 									for (TempFileContext file : filesToAddlist) {
 										try {
 											final R4EUIFileContext uiFileContext = uiReviewItem.createFileContext(
 													file.getBase(), file.getTarget(), file.getType());
 
 											for (IR4EUIPosition position : file.getPositions()) {
 												//Lazily create the Delta container if not already done
 												R4EUIDeltaContainer deltaContainer = (R4EUIDeltaContainer) uiFileContext.getContentsContainerElement();
 												if (null == deltaContainer) {
 													deltaContainer = new R4EUIDeltaContainer(uiFileContext,
 															R4EUIConstants.DELTAS_LABEL);
 													uiFileContext.addChildren(deltaContainer);
 												}
 												deltaContainer.createDelta((R4EUITextPosition) position);
 											}
 										} catch (OutOfSyncException e) {
 											R4EUIPlugin.Ftracer.traceError("Exception: " + e.toString() + " ("
 													+ e.getMessage() + ")");
 											R4EUIPlugin.getDefault().logError("Exception: " + e.toString(), e);
 										} catch (ResourceHandlingException e) {
 											R4EUIPlugin.Ftracer.traceError("Exception: " + e.toString() + " ("
 													+ e.getMessage() + ")");
 											R4EUIPlugin.getDefault().logError("Exception: " + e.toString(), e);
 										}
 									}
 
 									//Notify users if need be
 									final List<R4EReviewComponent> addedItems = new ArrayList<R4EReviewComponent>();
 									addedItems.add(uiReviewItem.getItem());
 									final R4EReview review = uiReview.getReview();
 									if (review.getType().equals(R4EReviewType.R4E_REVIEW_TYPE_FORMAL)) {
 										if (((R4EFormalReview) review).getCurrent()
 												.getType()
 												.equals(R4EReviewPhase.R4E_REVIEW_PHASE_PREPARATION)) {
 											MailServicesProxy.sendItemsAddedNotification(addedItems);
 
 										}
 									}
 								} catch (CoreException e) {
 									UIUtils.displayCoreErrorDialog(e);
 								} catch (ResourceHandlingException e) {
 									UIUtils.displayResourceErrorDialog(e);
 								} catch (OutOfSyncException e) {
 									UIUtils.displaySyncErrorDialog(e);
 								} finally {
 									R4EUIModelController.setJobInProgress(false); //Enable UI operations now
 								}
 							}
 						}
 					});
 					monitor.done();
 					return Status.OK_STATUS;
 				}
 			};
 			job.setUser(true);
 			job.schedule();
 		} catch (ReviewsFileStorageException e) {
 			UIUtils.displayReviewsFileStorageErrorDialog(e);
 			R4EUIModelController.setJobInProgress(false);
 		}
 	}
 
 	/**
 	 * Method updateFilesWithDeltas.
 	 * 
 	 * @param aFile
 	 *            TempFileContext
 	 * @throws CoreException
 	 */
 	private void updateFilesWithDeltas(final TempFileContext aFile) throws CoreException {
 
 		//Find all differecences between Base and Target files
 		final R4ECompareEditorInput input = CommandUtils.createCompareEditorInput(aFile.getBase(), aFile.getTarget());
 		input.prepareCompareInputNoEditor();
 
 		final DiffUtils diffUtils = new DiffUtils();
 		final List<Diff> diffs;
 
 		diffs = diffUtils.doDiff(false, true, input);
 
 		//Add Deltas from the list of differences
 		for (Diff diff : diffs) {
 			IR4EUIPosition position = CommandUtils.getPosition(diff.getPosition(R4EUIConstants.LEFT_CONTRIBUTOR)
 					.getOffset(), diff.getPosition(R4EUIConstants.LEFT_CONTRIBUTOR).getLength(),
 					diff.getDocument(R4EUIConstants.LEFT_CONTRIBUTOR));
 
 			if (null == position || RangeDifference.NOCHANGE == diff.getKind()) {
 				continue; //Cannot resolve position for this delta or no change
 			}
 			aFile.getPositions().add(position);
 		}
 	}
 
 	/**
 	 * @author lmcdubo
 	 */
 	private static class TempFileContext {
 		/**
 		 * Field base.
 		 */
 		private final R4EFileVersion base;
 
 		/**
 		 * Field target.
 		 */
 		private final R4EFileVersion target;
 
 		/**
 		 * Field type.
 		 */
 		private final R4EContextType type;
 
 		/**
 		 * Field positions.
 		 */
 		private final List<IR4EUIPosition> positions;
 
 		/**
 		 * Constructor for TempFileContext.
 		 * 
 		 * @param aRepository
 		 *            IRFSRegistry
 		 * @param aBase
 		 *            R4EFileVersion
 		 * @param aTarget
 		 *            R4EFileVersion
 		 * @param aType
 		 *            R4EContextType
 		 */
 		TempFileContext(IRFSRegistry aRepository, R4EFileVersion aBase, R4EFileVersion aTarget, R4EContextType aType) {
 			base = aBase;
 			//Add IFileRevision info
 			if (null != base && null != aRepository) {
 				try {
 					final IFileRevision fileRev = aRepository.getIFileRevision(null, base);
 					base.setFileRevision(fileRev);
 				} catch (ReviewsFileStorageException e) {
 					R4EUIPlugin.Ftracer.traceInfo("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 				}
 			}
 
 			target = aTarget;
 			//Add IFileRevision info
 			if (null != target && null != aRepository) {
 				try {
 					final IFileRevision fileRev = aRepository.getIFileRevision(null, target);
 					target.setFileRevision(fileRev);
 				} catch (ReviewsFileStorageException e) {
 					R4EUIPlugin.Ftracer.traceInfo("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 				}
 			}
 
 			type = aType;
 			positions = new ArrayList<IR4EUIPosition>();
 		}
 
 		/**
 		 * Method getBase.
 		 * 
 		 * @return R4EFileVersion
 		 */
 		public R4EFileVersion getBase() {
 			return base;
 		}
 
 		/**
 		 * Method getTarget.
 		 * 
 		 * @return R4EFileVersion
 		 */
 		public R4EFileVersion getTarget() {
 			return target;
 		}
 
 		/**
 		 * Method getType.
 		 * 
 		 * @return R4EContextType
 		 */
 		public R4EContextType getType() {
 			return type;
 		}
 
 		/**
 		 * Method getPositions.
 		 * 
 		 * @return List<IR4EUIPosition>
 		 */
 		public List<IR4EUIPosition> getPositions() {
 			return positions;
 		}
 	}
 }
