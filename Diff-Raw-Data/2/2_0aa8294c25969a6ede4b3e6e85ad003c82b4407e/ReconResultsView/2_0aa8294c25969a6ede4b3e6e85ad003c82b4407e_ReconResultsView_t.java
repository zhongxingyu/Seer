 /*-
  * Copyright © 2013 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package uk.ac.diamond.tomography.reconstruction.views;
 
 import java.lang.reflect.InvocationTargetException;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.util.EContentAdapter;
 import org.eclipse.jface.layout.TableColumnLayout;
 import org.eclipse.jface.viewers.CellLabelProvider;
 import org.eclipse.jface.viewers.ColumnLayoutData;
 import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
 import org.eclipse.jface.viewers.ColumnWeightData;
 import org.eclipse.jface.viewers.DoubleClickEvent;
 import org.eclipse.jface.viewers.IDoubleClickListener;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.ITableLabelProvider;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TableViewerColumn;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerCell;
 import org.eclipse.jface.window.ToolTip;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.WorkbenchException;
 import org.eclipse.ui.actions.WorkspaceModifyOperation;
 import org.eclipse.ui.part.FileEditorInput;
 import org.eclipse.ui.part.ViewPart;
 import org.eclipse.ui.progress.UIJob;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.tomography.reconstruction.Activator;
 import uk.ac.diamond.tomography.reconstruction.jobs.ReconSchedulingRule;
 import uk.ac.diamond.tomography.reconstruction.results.reconresults.ReconstructionDetail;
 
 public class ReconResultsView extends ViewPart {
 
 	private static final String DATA_EXPLORE_PERSPECTIVE_ID = "uk.ac.diamond.scisoft.dataexplorationperspective";
 
 	private static final String COL_TIME_STARTED = "Time started";
 
 	private static final String COL_RECONSTRUCTION_OUTPUT = "Reconstruction Output";
 
 	private static final String COL_FILE_NAME = "FileName";
 
 	public static final String ID = "uk.ac.diamond.tomography.reconstruction.results";
 
 	private static final Logger logger = LoggerFactory.getLogger(ReconResultsView.class);
 
 	public ReconResultsView() {
 	}
 
 	public static class ResultsTableContentProvider implements IStructuredContentProvider {
 
 		private Viewer viewer;
 
 		private Resource currentResource;
 
 		private EContentAdapter notificationListener = new EContentAdapter() {
 			@Override
 			public void notifyChanged(org.eclipse.emf.common.notify.Notification notification) {
 				int eventType = notification.getEventType();
 
 				if (Notification.SET == eventType || Notification.ADD == eventType) {
 					if (notification.getFeature() != null && !notification.getFeature().equals("null")) {
 						viewer.refresh();
 					}
 				}
 			}
 		};
 
 		@Override
 		public void dispose() {
 			if (currentResource != null) {
 				currentResource.eAdapters().remove(notificationListener);
 			}
 		}
 
 		@Override
 		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
 			this.viewer = viewer;
 			if (currentResource != null) {
 				currentResource.eAdapters().remove(notificationListener);
 			}
 			if (newInput instanceof Resource) {
 				Resource res = (Resource) newInput;
 				currentResource = res;
 				res.eAdapters().add(notificationListener);
 			}
 
 		}
 
 		@Override
 		public Object[] getElements(Object inputElement) {
 			if (inputElement instanceof Resource) {
 				EObject rootObject = ((Resource) inputElement).getContents().get(0);
 				if (rootObject instanceof uk.ac.diamond.tomography.reconstruction.results.reconresults.ReconResults) {
 					uk.ac.diamond.tomography.reconstruction.results.reconresults.ReconResults reconResults = (uk.ac.diamond.tomography.reconstruction.results.reconresults.ReconResults) rootObject;
 					return reconResults.getReconresult().toArray();
 				}
 			}
 			return null;
 		}
 
 	}
 
 	public static class ResultsTableLabelProvider extends LabelProvider implements ITableLabelProvider {
 
 		@Override
 		public Image getColumnImage(Object element, int columnIndex) {
 			return null;
 		}
 
 		@Override
 		public String getColumnText(Object element, int columnIndex) {
 			if (element instanceof ReconstructionDetail) {
 				ReconstructionDetail detail = (ReconstructionDetail) element;
 
 				if (columnIndex == 0) {
 					return detail.getNexusFileName();
 				} else if (columnIndex == 1) {
 					return detail.getReconstructedLocation();
 				} else if (columnIndex == 2) {
 					return detail.getTimeReconStarted();
 				}
 			}
 
 			return null;
 		}
 
 	}
 
 	public void createColumns(TableViewer tableViewer, TableColumnLayout layout) {
 		for (int i = 0; i < columnHeaders.length; i++) {
 			TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, SWT.None);
 			tableViewerColumn.getColumn().setResizable(columnLayouts[i].resizable);
 			tableViewerColumn.getColumn().setText(columnHeaders[i]);
 			tableViewerColumn.getColumn().setToolTipText(columnHeaders[i]);
 			layout.setColumnData(tableViewerColumn.getColumn(), columnLayouts[i]);
 			tableViewerColumn.setLabelProvider(new CellLabelProvider() {
 
 				@Override
 				public void update(ViewerCell cell) {
 					//
 					int columnIndex = cell.getColumnIndex();
 					Object element = cell.getElement();
 
 					if (element instanceof ReconstructionDetail) {
 						ReconstructionDetail detail = (ReconstructionDetail) element;
 						switch (columnIndex) {
 						case 0:
 							cell.setText(detail.getNexusFileName());
 							break;
 						case 1:
 							cell.setText(detail.getReconstructedLocation());
 							break;
 						case 2:
 							cell.setText(detail.getTimeReconStarted());
 							break;
 						}
 
 					}
 				}
 
 				@Override
 				public String getToolTipText(Object element) {
 					if (element instanceof ReconstructionDetail) {
 						ReconstructionDetail detail = (ReconstructionDetail) element;
 						return String.format("Full Reconstruction location\n%s", detail.getReconstructedLocation());
 					}
 					return super.getToolTipText(element);
 				}
 
 				@Override
 				public int getToolTipDisplayDelayTime(Object object) {
 					return 2000;
 				}
 
 				@Override
 				public int getToolTipTimeDisplayed(Object object) {
 					return 5000;
 				}
 
 				@Override
 				public Point getToolTipShift(Object object) {
 					return new Point(-20, -20);
 				}
 			});
 		}
 	}
 
 	private final String columnHeaders[] = { COL_FILE_NAME, COL_RECONSTRUCTION_OUTPUT, COL_TIME_STARTED };
 	private ColumnLayoutData columnLayouts[] = { new ColumnWeightData(30, true), new ColumnWeightData(50, true),
 			new ColumnWeightData(20, true) };
 
 	private TableViewer resultsTableViewer;
 
 	@Override
 	public void createPartControl(Composite parent) {
 		Composite root = new Composite(parent, SWT.None);
 
 		resultsTableViewer = new TableViewer(root);
 		resultsTableViewer.getTable().setHeaderVisible(true);
 		resultsTableViewer.getTable().setLinesVisible(true);
 
 		TableColumnLayout tableLayout = new TableColumnLayout();
 		root.setLayout(tableLayout);
 
 		createColumns(resultsTableViewer, tableLayout);
 
 		resultsTableViewer.setContentProvider(new ResultsTableContentProvider());
 
 		resultsTableViewer.setInput(Activator.getDefault().getReconResultsResource());
 
 		ColumnViewerToolTipSupport.enableFor(resultsTableViewer, ToolTip.NO_RECREATE);
 		resultsTableViewer.addDoubleClickListener(new IDoubleClickListener() {
 
 			@Override
 			public void doubleClick(DoubleClickEvent event) {
 				logger.debug("event:{}", event);
 				ISelection selection = event.getSelection();
 				if (selection instanceof IStructuredSelection) {
 					IStructuredSelection structuredSel = (IStructuredSelection) selection;
 					Object firstElement = structuredSel.getFirstElement();
 
 					if (firstElement instanceof ReconstructionDetail) {
 						final ReconstructionDetail detail = (ReconstructionDetail) firstElement;
 						final String nexusFileLocation = detail.getNexusFileLocation();
 						final ReconSchedulingRule reconScheduleRule = new ReconSchedulingRule(nexusFileLocation);
 						UIJob openReconstructedImageFile = new UIJob(getViewSite().getShell().getDisplay(), String
 								.format("Showing reconstructed files (%s)", detail.getNexusFileName())) {
 
 							@Override
 							public IStatus runInUIThread(IProgressMonitor monitor) {
 								try {
 									PlatformUI.getWorkbench().showPerspective(DATA_EXPLORE_PERSPECTIVE_ID,
 											PlatformUI.getWorkbench().getActiveWorkbenchWindow());
 									IProject tomoFilesProject = Activator.getDefault().getTomoFilesProject();
 									final IFolder reconNxsFileFolder = tomoFilesProject.getFolder(String.format(
 											"Recon-%s", detail.getNexusFileName()));
 
 									if (!reconNxsFileFolder.exists()) {
 										try {
 											new WorkspaceModifyOperation(reconScheduleRule) {
 												@Override
 												protected void execute(IProgressMonitor monitor) throws CoreException,
 														InvocationTargetException, InterruptedException {
 													reconNxsFileFolder.createLink(
 															new Path(detail.getReconstructedLocation()),
 															IResource.REPLACE, monitor);
 												}
 											}.run(new NullProgressMonitor());
 										} catch (InvocationTargetException e) {
 											logger.error("Unable to create Tomo recon folder", e);
 										} catch (InterruptedException e) {
 											logger.error("Unable to create Tomo recon folder", e);
 										}
 									} else {
 										try {
 											reconNxsFileFolder.refreshLocal(IResource.DEPTH_INFINITE,
 													new NullProgressMonitor());
 										} catch (CoreException e) {
 											logger.error("Unable to refresh folder", e);
 										}
 									}
 
 									try {
 										IResource[] members = reconNxsFileFolder.members();
 										if (members.length > 0) {
 											IResource res = members[0];
 											if (res instanceof IFile && res.getFileExtension().equals("tif")) {
 												IFile tifFile = (IFile) res;
 												PlatformUI
 														.getWorkbench()
 														.getActiveWorkbenchWindow()
 														.getActivePage()
 														.openEditor(new FileEditorInput(tifFile),
 																"uk.ac.diamond.scisoft.analysis.rcp.editors.ImageEditor");
 											}
 										} else {
 											logger.debug("No reconstruction files in the folder");
 										}
 									} catch (CoreException e) {
										logger.error("Error returning list of reconstruction files and folders", e);
 									}
 
 								} catch (WorkbenchException e) {
 									logger.error("unable to open recon files", e);
 								}
 
 								return Status.OK_STATUS;
 							}
 						};
 						openReconstructedImageFile.setRule(reconScheduleRule);
 						openReconstructedImageFile.schedule();
 					}
 				}
 			}
 		});
 	}
 
 	@Override
 	public void setFocus() {
 		resultsTableViewer.getTable().setFocus();
 	}
 
 }
