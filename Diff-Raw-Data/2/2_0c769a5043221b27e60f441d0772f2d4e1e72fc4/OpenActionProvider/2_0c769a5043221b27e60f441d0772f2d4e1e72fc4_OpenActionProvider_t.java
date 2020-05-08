 package uk.ac.diamond.scisoft.icatexplorer.rcp.actions;
 
 import java.io.File;
 import java.net.InetAddress;
 import java.util.Properties;
 
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
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
 
 import uk.ac.diamond.scisoft.analysis.rcp.views.DatasetInspectorView;
 import uk.ac.diamond.scisoft.analysis.rcp.views.PlotView;
 import uk.ac.diamond.scisoft.analysis.rcp.views.SidePlotView;
 import uk.ac.diamond.scisoft.icatexplorer.rcp.datafiles.DatafileTreeData;
 import uk.ac.diamond.scisoft.icatexplorer.rcp.datasets.DatasetTreeData;
 import uk.ac.diamond.scisoft.icatexplorer.rcp.icatclient.ICATClient;
 import uk.ac.diamond.scisoft.icatexplorer.rcp.icatclient.ICATSessions;
 import uk.ac.diamond.scisoft.icatexplorer.rcp.sftpclient.SftpClient;
 import uk.ac.diamond.scisoft.icatexplorer.rcp.utils.FilenameUtils;
 import uk.ac.diamond.scisoft.icatexplorer.rcp.utils.NetworkUtils;
 import uk.ac.diamond.scisoft.icatexplorer.rcp.utils.OSDetector;
 import uk.ac.diamond.scisoft.icatexplorer.rcp.utils.PropertiesUtils;
 import uk.ac.diamond.scisoft.icatexplorer.rcp.visits.VisitTreeData;
 import uk.ac.diamond.sda.meta.views.MetadataPageView;
 import uk.ac.gda.common.rcp.util.EclipseUtils;
 
 public class OpenActionProvider extends CommonActionProvider {
 
 	private static Logger logger = LoggerFactory
 			.getLogger(OpenActionProvider.class);
 
 	private OpenChildAction openAction;
 	public IWorkbenchPage page;
 	private Properties properties;
 	private String downloadDir;
 	private String sftpServer;
 	//private String projectName;
 
 	public OpenActionProvider() {
 		logger.debug("reading properties file...");
 
 		try {
 			properties = PropertiesUtils.readConfigFile();
 
 			InetAddress addr = InetAddress.getLocalHost();
 
 			if (NetworkUtils.insideDLS(addr)) {
 				sftpServer = properties.getProperty("internal.sftp.server");
 			} else {
 				sftpServer = properties.getProperty("external.sftp.server");
 			}
 
 		} catch (Exception e) {
 			logger.error("cannot read properties file", e);
 		}
 
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
 		private ISelectionProvider provider;
 		private Object data;
 
 		public OpenChildAction(IWorkbenchPage workbenchPage,
 				ISelectionProvider selectionProvider) {
 			// super("Open item");
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
 					
 					String projectName = ((DatafileTreeData) data).getParentProject();
 					downloadDir = ((ICATClient)ICATSessions.get(projectName)).getDownloadDir();
 
 					// check whether download directory actually exists
 					File file = new File(downloadDir);
 					if (!file.exists()) {
 						logger.error("download directory does not exist or has not been set: "
 								+ downloadDir);
 					}
 
 					
 					
 					DatafileTreeData DatafileTreeData = (DatafileTreeData) data;
 					logger.debug("opening "
 							+ DatafileTreeData.getIcatDatafile().getLocation()
 							+ " with id: "
 							+ DatafileTreeData.getIcatDatafile().getId());
 
 					// check current operating system
 					if (OSDetector.isUnix()) {
 						try {
 
 							EclipseUtils.openExternalEditor(DatafileTreeData
 									.getIcatDatafile().getLocation());
 
 						} catch (PartInitException e) {
 							logger.error("Cannot open file "
 									+ DatafileTreeData.getIcatDatafile()
 									.getLocation(), e);
 						}
 					} else {// windows os detected
 
 						logger.info("non-unix system detected...proceed with downloading the file");
 
 						File file3 = new File(downloadDir);
 
 						// computing the local file path
 						FilenameUtils fileUtils = new FilenameUtils(
 								DatafileTreeData.getIcatDatafile()
 								.getLocation(), '/', '.');
 						File file4 = new File(file3, fileUtils.filename());
 						File file5 = new File(file4.getPath());
 
 						String localFilePath = file5.getPath();
 
 						logger.debug("file exists? "
 								+ (Boolean.toString(file5.exists()))
 								.toUpperCase() + " - " + localFilePath);
 						if (!(new File(localFilePath)).exists()) {
 							// download file to temp dir
 							logger.info("downloading DatafileTreeData id: "
 									+ DatafileTreeData.getIcatDatafile()
 									.getId());
 
 							String fedid = ICATSessions.get(projectName)
 									.getFedId();
 							String password = ICATSessions.get(projectName)
 									.getPassword();
 
 							SftpClient sftpClient = new SftpClient();
 							localFilePath = sftpClient.downloadFile(fedid,
 									password, sftpServer, DatafileTreeData
 									.getIcatDatafile().getLocation(),
 									file3.getPath()/* downloadDir */);
 
 							logger.info("file successfully downloaded to "
 									+ localFilePath);
 						} else {
 
 							logger.debug("file: " + localFilePath
 									+ " already exist in local filesystem");
 						}
 
 						// open editor
 						try {
 							logger.debug("open file in editor:  "
 									+ localFilePath);
 							EclipseUtils.openExternalEditor(localFilePath);
 						} catch (PartInitException e) {
 							logger.error("Cannot open file "
 									+ DatafileTreeData.getIcatDatafile()
 									.getLocation(), e);
 						}
 
 					}
 
 					// open remaining views
 					// place holders for remaining views
 					// String plot = PlotView.ID + "DP";
 					try {
 						String plot = PlotView.ID + "DP";
 						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(plot);
 						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(MetadataPageView.ID);
 						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(DatasetInspectorView.ID);
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(SidePlotView.ID + ":Dataset Plot");
 					} catch (PartInitException e) {
 						logger.error("Error opening view: ", e);
 					}
 
 				}
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
