 /*
  * Copyright 2012 Diamond Light Source Ltd.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package uk.ac.diamond.scisoft.icatexplorer.rcp.actions;
 
 import java.io.File;
 import java.util.Date;
 
 import org.dawb.common.ui.util.EclipseUtils;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.QualifiedName;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.IMemento;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.navigator.CommonActionProvider;
 import org.eclipse.ui.navigator.ICommonActionConstants;
 import org.eclipse.ui.navigator.ICommonActionExtensionSite;
 import org.eclipse.ui.navigator.ICommonMenuConstants;
 import org.eclipse.ui.navigator.ICommonViewerSite;
 import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.icatexplorer.rcp.datafiles.DatafileTreeData;
 import uk.ac.diamond.scisoft.icatexplorer.rcp.datasets.DatasetTreeData;
 import uk.ac.diamond.scisoft.icatexplorer.rcp.icatclient.ICATSessions;
 import uk.ac.diamond.scisoft.icatexplorer.rcp.sftpclient.SftpClient;
 import uk.ac.diamond.scisoft.icatexplorer.rcp.utils.DateTools;
 import uk.ac.diamond.scisoft.icatexplorer.rcp.utils.DatesConverter;
 import uk.ac.diamond.scisoft.icatexplorer.rcp.utils.FilenameUtils;
 import uk.ac.diamond.scisoft.icatexplorer.rcp.visits.VisitTreeData;
 
 public class OpenActionProvider extends CommonActionProvider {
 
	private static Logger logger = LoggerFactory
			.getLogger(OpenActionProvider.class);
 
 	private OpenChildAction openAction;
 	public IWorkbenchPage page;
 	private String downloadDir;
 	private String sftpServer;
 	//private String projectName;
 
	public OpenActionProvider() {
	}
 
 	@Override
 	public void init(ICommonActionExtensionSite site) {
 		ICommonViewerSite viewSite = site.getViewSite();
 		if (viewSite instanceof ICommonViewerWorkbenchSite) {
 			ICommonViewerWorkbenchSite workbenchSite = (ICommonViewerWorkbenchSite) viewSite;
 			openAction = new OpenChildAction(workbenchSite.getPage(),
 					workbenchSite.getSelectionProvider());
 
 		}
 	}
 
 	@Override
 	public void restoreState(IMemento memento) {
 		super.restoreState(memento);
 	}
 
 	@Override
 	public void saveState(IMemento memento) {
 		super.saveState(memento);
 	}
 
 	@Override
 	public void fillActionBars(IActionBars actionBars) {
 		if (openAction.isEnabled()) {
 			actionBars.setGlobalActionHandler(ICommonActionConstants.OPEN,
 					openAction);
 		}
 	}
 
 	@Override
 	public void fillContextMenu(IMenuManager menu) {
 		if (openAction.isEnabled()) {
 			menu.appendToGroup(ICommonMenuConstants.GROUP_OPEN, openAction);
 		}
 	}
 
 	/**
 	 * 
 	 * 
 	 * 
 	 */
 	class OpenChildAction extends Action {
 		private final ISelectionProvider provider;
 		private Object data;
 
		public OpenChildAction(IWorkbenchPage workbenchPage,
				ISelectionProvider selectionProvider) {
			// provider = selectionProvider;
			setText("Open");
 			page = workbenchPage;
 			provider = selectionProvider;
 
 		}
 
 		@Override
 		public void run() {

 			if (data != null) {
 				logger.debug("open called on " + data.getClass().getName());
 
 				// If we had a selection lets open the editor
 				if (data instanceof DatafileTreeData) {
 
 					IProject parentProject = ((DatafileTreeData) data).getParentProject();
 					QualifiedName qNameSessionId = new QualifiedName("SESSIONID", "String");
 					String sessionId = null;
 					boolean datafileDownloaded = false;
 					
 					try {
 						sessionId = parentProject.getPersistentProperty(qNameSessionId);
 					} catch (CoreException e1) {
 						logger.error("error getting the sessionId for project " + parentProject.getName(), e1);
 					}
 					
 					
 						downloadDir = ICATSessions.get(sessionId).getDownloadDir();
 
 						// check whether download directory actually exists
 						File file = new File(downloadDir);
 						if (!file.exists()) {
 							logger.error("download directory does not exist or has not been set: " + downloadDir);
 						}
 						
 						DatafileTreeData DatafileTreeData = (DatafileTreeData) data;
 						logger.debug("opening "
 								+ DatafileTreeData.getIcatDatafile().getLocation()
 								+ " with id: "
 								+ DatafileTreeData.getIcatDatafile().getId());
 						
 											
 						logger.info("downloading the file to local filesystem");
 
 						File file3 = new File(downloadDir);
 
 						// computing the local file path
 						FilenameUtils fileUtils = new FilenameUtils(DatafileTreeData.getIcatDatafile()
 								.getLocation(), '/', '.');
 						File file4 = new File(file3, fileUtils.filename());
 						File file5 = new File(file4.getPath());
 
 						String localFilePath = file5.getPath();
 
 						logger.debug("file exists? " + (Boolean.toString(file5.exists())).toUpperCase() + " - " + localFilePath);
 						
 						if (!(new File(localFilePath)).exists()) {
 							// download file to temp dir
 							logger.info("downloading DatafileTreeData id: "
 									+ DatafileTreeData.getIcatDatafile()
 									.getId());
 
 							String fedid = ICATSessions.get(sessionId).getFedId();
 							String password = ICATSessions.get(sessionId).getPassword();
 
 							sftpServer = ICATSessions.get(sessionId).getIcatCon().getSftpServer();
 							SftpClient sftpClient = new SftpClient();
 							
 							try {
 								localFilePath = sftpClient.downloadFile(fedid,
 										password, sftpServer, DatafileTreeData
 										.getIcatDatafile().getLocation(),
 										file3.getPath()/* downloadDir */);
 								
 								logger.info("file successfully downloaded to "
 										+ localFilePath);
 								
 								datafileDownloaded = true;					
 								
 							} catch (Exception e) {
 															
 								int NB_DAYS_DATAFILE_EXPIRED = 180;
 								
 								Date datafileDate = DatesConverter.asDate(DatafileTreeData.getIcatDatafile().getDatafileCreateTime());
 								Date nowDate = new Date();
 								String messageText = "a problem occured when trying to get the file from Diamond file system."+ " \nError: " + e.getMessage();
 								
 								if(DateTools.countDaysBetween(datafileDate, nowDate) > NB_DAYS_DATAFILE_EXPIRED){
 									messageText = "Permission denied! Datafile is older than " + NB_DAYS_DATAFILE_EXPIRED +" days. " +
 							        		"Please make a request to IT support for the access to be reinstated."+ " \nError: " + e.getMessage();
 								
 								logger.error("problem getting file from Diamond file system: " + e.getMessage());
 
 									// TODO download from archiving system
 								}								
 								
 								// display warning message
 								MessageBox messageBox = new MessageBox(PlatformUI.getWorkbench().
 						                getActiveWorkbenchWindow().getShell(), SWT.ICON_WARNING | SWT.OK );
 						        
 						        messageBox.setText("Warning");
 						        messageBox.setMessage(messageText);
 						        int buttonID = messageBox.open();
 
 							}
 							
 						} else {
 							
 							datafileDownloaded = true;
 							logger.debug("file: " + localFilePath + " already exist in local filesystem");
 						}
 						
 					if (datafileDownloaded) {
 						// open editor
 						try {
 							logger.debug("open file in editor:  "
 									+ localFilePath);
 							EclipseUtils.openExternalEditor(localFilePath);
 						} catch (PartInitException e) {
 							logger.error("Cannot open file " + DatafileTreeData.getIcatDatafile()
 											.getLocation(), e);
 						}
 					}// datafileDownloaded?
 										
 				}// end check whether datafiledata
 			}// end if data <> null
 			super.run();
 		}
 
 		// handle single click
 		@Override
 		public boolean isEnabled() {
 			ISelection selection = provider.getSelection();
 
 			if (!selection.isEmpty()) {
 				IStructuredSelection sSelection = (IStructuredSelection) selection;
 				if (sSelection.size() == 1
 						&& sSelection.getFirstElement() instanceof VisitTreeData) {
 					data = sSelection.getFirstElement();
 
 					return true;
 				} else if (sSelection.size() == 1
 						&& sSelection.getFirstElement() instanceof DatasetTreeData) {
 					data = sSelection.getFirstElement();
 					return true;
 				} else if (sSelection.getFirstElement() instanceof DatafileTreeData) {
 					data = sSelection.getFirstElement();
 
 					return true;
 				}
 			}
 			return false;
 		}
 
 	}
 
 }
