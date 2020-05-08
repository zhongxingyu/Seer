 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 
 package org.amanzi.neo.wizards;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.Map;
 import java.net.URLEncoder;
 
 import org.amanzi.awe.console.AweConsolePlugin;
 import org.amanzi.neo.core.INeoConstants;
 import org.amanzi.neo.core.NeoCorePlugin;
 import org.amanzi.neo.core.enums.GeoNeoRelationshipTypes;
 import org.amanzi.neo.core.enums.NodeTypes;
 import org.amanzi.neo.core.service.NeoServiceProvider;
 import org.amanzi.neo.core.utils.NeoUtils;
 import org.amanzi.neo.loader.DriveLoader;
 import org.amanzi.neo.loader.GPSRemoteUrlLoader;
 import org.amanzi.neo.loader.TemsRemoteUrlLoader;
 import org.amanzi.neo.loader.internal.NeoLoaderPlugin;
 import org.amanzi.neo.loader.internal.NeoLoaderPluginMessages;
 import org.amanzi.neo.loader.ui.utils.LoaderUiUtils;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.wizard.Wizard;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IImportWizard;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.PlatformUI;
 import org.neo4j.graphdb.*;
 import org.neo4j.graphdb.Traverser.Order;
 
 /**
  * <p>
  * Wizard for import dataset from url
  * </p>
  * 
  * @author NiCK
  * @since 1.0.0
  */
 public class DatasetImportUrlWizard extends Wizard implements IImportWizard {
 
     /** The Constant PAGE_TITLE. */
     private static final String PAGE_TITLE = NeoLoaderPluginMessages.TemsImportWizard_PAGE_TITLE;
 
     /** The Constant PAGE_DESCR. */
     private static final String PAGE_DESCR = NeoLoaderPluginMessages.TemsImportWizard_PAGE_DESCR;
 
     /** The main page. */
     private DatasetImportUrlWizardPage mainPage;
 
     /** The dataset name. */
     private String datasetName;
     private Node dataset = null;
 
     /** The url. */
     private URL url;
     
     private Node lastMNode;
     
     private Node lastMsNode;
 
     /**
      * Perform finish.
      * 
      * @return true, if successful
      */
     @Override
     public boolean performFinish() {
     	datasetName = mainPage.getDataset();
 
         runLoadingJob();
         return true;
     }
     
     private Node findLastNode (Node startNode){
     	
     	Traverser traverser = startNode.traverse(Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH, 
     							ReturnableEvaluator.ALL_BUT_START_NODE, GeoNeoRelationshipTypes.NEXT, Direction.OUTGOING);
     	for (Node node: traverser){
     		if (!node.hasRelationship(GeoNeoRelationshipTypes.NEXT, Direction.OUTGOING)){
     			return node;
     		}
     	}
     	
     	return startNode;
     }
 
     /**
      * Run loading job.
      */
     private void runLoadingJob() {
         LoadDriveJob job = new LoadDriveJob(mainPage.getControl().getDisplay());
         job.schedule(50);
     }
 
     @Override
     public void init(IWorkbench workbench, IStructuredSelection selection) {
         mainPage = new DatasetImportUrlWizardPage(PAGE_TITLE, PAGE_DESCR);
     }
 
     @Override
     public void addPages() {
         super.addPages();
         addPage(mainPage);
     }
 
     /**
      * The Class LoadDriveJob.
      */
     private class LoadDriveJob extends Job {
 
         /** The job display. */
         private final Display jobDisplay;
         
         /**
          * Instantiates a new load drive job.
          * 
          * @param jobDisplay the job display
          */
         public LoadDriveJob(Display jobDisplay) {
             super(NeoLoaderPluginMessages.DriveDialog_MonitorName);
             this.jobDisplay = jobDisplay;
         }
 
         /**
          * Run.
          * 
          * @param monitor the monitor
          * @return the status
          */
         @Override
         protected IStatus run(IProgressMonitor monitor) {
             try {
                 loadDriveData(jobDisplay, monitor);
                 return Status.OK_STATUS;
             } catch (Exception e) {
                 e.printStackTrace();
                 return Status.CANCEL_STATUS;
             }
         }
 
     }
     
     private void findDataset(String datasetName) {
     	Traverser allDatasetTraverser = NeoCorePlugin.getDefault().getProjectService().getAllDatasetTraverser(
                 NeoServiceProvider.getProvider().getService().getReferenceNode());
         
 
         for (Node node : allDatasetTraverser) {
         	if(((String)node.getProperty(INeoConstants.PROPERTY_NAME_NAME)).equals(datasetName)) {
         		dataset = node;
         		return;
         	}
         }
     }
 
     private String getNextUrl() {
     	lastMNode = null;
     	lastMsNode = null;
     	String urlString = mainPage.getUrl() + "&numEvents=10000";
     	
     	if(dataset == null) {
     		findDataset(this.datasetName);
     	}
     	if (dataset != null){
     		Traverser traverser = dataset.traverse(Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH, new ReturnableEvaluator(){
     			@Override
                 public boolean isReturnableNode(TraversalPosition currentPos) {
                     if (currentPos.currentNode().hasRelationship(GeoNeoRelationshipTypes.CHILD, Direction.OUTGOING))
                     	return false;
     				return true;
                 }
     		}, GeoNeoRelationshipTypes.CHILD, Direction.OUTGOING);
     		
     		for (Node node: traverser){
     			if (node.getProperty(INeoConstants.PROPERTY_TYPE_NAME).equals(NodeTypes.M.getId())){
     				lastMNode = findLastNode(node);
     			}
     			else if (node.getProperty(INeoConstants.PROPERTY_TYPE_NAME).equals(INeoConstants.HEADER_MS)){
     				lastMsNode  = findLastNode(node);
     			}
     		}
     		
     		if(lastMNode != null) {
 	    		String time = (String) lastMNode.getProperty(INeoConstants.PROPERTY_TIME_NAME);
 	    		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
 	    		Date date = new Date();
 	    		try {
 	    			date = format.parse(time);
 	    		} catch (ParseException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 	    		
 	    		String imei=null, imsi=null;
 	    		Object ob1 = lastMNode.getProperty("imei");
 	    		Object ob2 = lastMNode.getProperty("imsi");
 	
 	    		if(ob1 instanceof Float) {
 	    			imei = Long.toString(((Float)ob1).longValue());
 	    		} else if(ob1 instanceof Integer) {
 		    			imei = Long.toString(((Integer)ob1).longValue());
 	    		} else if(ob1 instanceof Long) {
 	    			imei = ((Long)ob1).toString();
 	    		} else {
 	    			imei = ((String)ob1);
 	    		}
 	    		if(ob2 instanceof Float) {
 	    			imsi = Long.toString(((Float)ob2).longValue());
 	    		} else if(ob2 instanceof Integer) {
 		    			imsi = Long.toString(((Integer)ob2).longValue());
 	    		} else if(ob2 instanceof Long) {
 	    			imsi = ((Long)ob2).toString();
 	    		} else {
 	    			imsi = ((String)ob2);
 	    		}
 	    		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
 	    		time = dateFormat.format(date);
 				
 	    		try {
 					urlString = urlString + "&start=" + time + "&imei=" + URLEncoder.encode(imei, "UTF-8") + "&imsi=" + URLEncoder.encode(imsi, "UTF-8");
 				} catch (UnsupportedEncodingException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
     		}
     	}
     	return urlString;
     }
     
     /**
      * Load drive data.
      * 
      * @param display the display
      * @param monitor the monitor
      */
     private void loadDriveData(Display display, IProgressMonitor monitor) {
         if (monitor == null) {
             monitor = new NullProgressMonitor();
         }
         DriveLoader driveLoader = null;
 
         Node prev_lastMNode = null;
         Node prev_lastMsNode = null;
         int eventsCnt = 0;
         while(true) {
 			String urlStr = getNextUrl();
 
 			try {
 				if (prev_lastMNode == lastMNode && lastMNode != null) {
 					// exit condition
 					break;
 				}
 				if (lastMNode == null && eventsCnt > 0) {
 					urlStr += "&skipCount=" + eventsCnt;
 				} else {
 					if (lastMNode != null && lastMNode == prev_lastMNode
 							&& eventsCnt > 0) {
 						urlStr += "&skipCount=" + eventsCnt;
 					} else {
 						eventsCnt = 0;
 					}
 				}
 				prev_lastMNode = lastMNode;
 				prev_lastMsNode = lastMsNode;
 				url = new URL(urlStr);
 
				driveLoader = new TemsRemoteUrlLoader(url, datasetName,display, datasetName,dataset, lastMNode, lastMsNode);
 
 				driveLoader.run(monitor);
 				driveLoader.printStats(false);
 				driveLoader.clearCaches();
 				
 				//lastEventData = ((TemsRemoteUrlLoader)driveLoader).getLastProcessedEvent();
 				if(((TemsRemoteUrlLoader)driveLoader).getEventProcessedCount() ==0 ) {
 					break;
 				}
 				eventsCnt += 1000;
 			} catch (MalformedURLException e) {
 				e.printStackTrace();
 				break;
 			} catch (IOException e) {
 				NeoLoaderPlugin.exception(e);
 				break;
 			}
 		}
         
         
         
         try {
         	handleSelect(monitor, new Node[] {dataset});
         } catch (Exception e) {
         	//MessageDialog.
     		//MessageDialog.openInformation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Error loading data", "Please try again");
         	AweConsolePlugin.error("Error loading data, Please try again");
     		driveLoader = null;
         }
         if (driveLoader != null) {
             try {
                 DriveLoader.finishUpGis();
             } catch (MalformedURLException e) {
                 NeoLoaderPlugin.error(e.getMessage());
             }
         }
 
         if (driveLoader != null) {
             driveLoader.addLayersToMap();
         }
 
         monitor.done();
     }
 
     /**
      * Handle select.
      * 
      * @param monitor the monitor
      * @param rootNodes the root nodes
      */
     protected void handleSelect(IProgressMonitor monitor, Node[] rootNodes) {
         if (monitor.isCanceled()) {
             return;
         }
         LinkedHashSet<Node> sets = LoaderUiUtils.getSelectedNodes(NeoServiceProvider.getProvider().getService());
         for (Node node : rootNodes) {
             sets.add(node);
         }
         LoaderUiUtils.storeSelectedNodes(sets);
     }
 }
