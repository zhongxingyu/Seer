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
 package org.amanzi.neo.loader;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileInputStream;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.LinkedHashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 import java.util.StringTokenizer;
 
 import net.refractions.udig.catalog.CatalogPlugin;
 import net.refractions.udig.catalog.IGeoResource;
 import net.refractions.udig.catalog.IService;
 import net.refractions.udig.project.ILayer;
 import net.refractions.udig.project.IMap;
 import net.refractions.udig.project.ui.ApplicationGIS;
 import net.refractions.udig.project.ui.internal.actions.ZoomToLayer;
 
 import org.amanzi.neo.core.INeoConstants;
 import org.amanzi.neo.core.NeoCorePlugin;
 import org.amanzi.neo.core.enums.GeoNeoRelationshipTypes;
 import org.amanzi.neo.core.enums.NetworkFileType;
 import org.amanzi.neo.core.enums.NodeTypes;
 import org.amanzi.neo.core.enums.OssType;
 import org.amanzi.neo.core.service.NeoServiceProvider;
 import org.amanzi.neo.core.utils.ActionUtil;
 import org.amanzi.neo.core.utils.CSVParser;
 import org.amanzi.neo.core.utils.NeoUtils;
 import org.amanzi.neo.core.utils.Pair;
 import org.amanzi.neo.core.utils.ActionUtil.RunnableWithResult;
 import org.amanzi.neo.loader.internal.NeoLoaderPlugin;
 import org.amanzi.neo.loader.internal.NeoLoaderPluginMessages;
 import org.amanzi.neo.preferences.DataLoadPreferences;
 import org.apache.log4j.Logger;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.dialogs.MessageDialogWithToggle;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.ui.PlatformUI;
 import org.hsqldb.lib.StringUtil;
 import org.neo4j.graphdb.Direction;
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Relationship;
 import org.neo4j.graphdb.Transaction;
 
 public class LoaderUtils {
     /**
      * return AWE project name of active map
      * 
      * @return
      */
     public static String getAweProjectName() {
         IMap map = ApplicationGIS.getActiveMap();
         return map == ApplicationGIS.NO_MAP ? ApplicationGIS.getActiveProject().getName() : map.getProject().getName();
     }
 
     /**
      * Convert dBm values to milliwatts
      * 
      * @param dbm
      * @return milliwatts
      */
     public static final double dbm2mw(int dbm) {
         return Math.pow(10.0, ((dbm) / 10.0));
     }
 
     /**
      * Convert milliwatss values to dBm
      * 
      * @param milliwatts
      * @return dBm
      */
     public static final float mw2dbm(double mw) {
         return (float)(10.0 * Math.log10(mw));
     }
 
     /**
      * get type of network files
      * 
      * @param fileName file name
      * @return Pair<NetworkFiles, Exception> : <NetworkFiles if file was correctly parsed, else
      *         null,Exception if exception appears else null>
      */
     public static Pair<NetworkFileType, Exception> getFileType(String fileName) {
         try {
             BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
             String line;
             if (getFileExtension(fileName).equalsIgnoreCase(".xml")){
                 int c=0;
                 while ((line = reader.readLine()) != null && c<5) {
                     if (line.contains("configData")){
                         reader.close();
                         return new Pair<NetworkFileType, Exception>(NetworkFileType.UTRAN, null);
                     }
                 };
                 reader.close();
                 return new Pair<NetworkFileType, Exception>(NetworkFileType.NOKIA_TOPOLOGY, null);
             }
             while ((line = reader.readLine()) != null && line.length() < 2) {
                 // find header
             };
             reader.close();
             if (line == null) {
                 return new Pair<NetworkFileType, Exception>(null, null);
             }
             int maxMatch = 0;
             String[] possibleFieldSepRegexes = new String[] {"\t", ",", ";"};
             String fieldSepRegex = "\t";
             for (String regex : possibleFieldSepRegexes) {
                 String[] fields = line.split(regex);
                 if (fields.length > maxMatch) {
                     maxMatch = fields.length;
                     fieldSepRegex = regex;
                 }
             }
             CSVParser parser = new CSVParser(fieldSepRegex.charAt(0));
             List<String> headers = parser.parse(line);
             for (String header : getPossibleHeaders(DataLoadPreferences.PR_NAME)) {
                 if (headers.contains(header)) {
                     return new Pair<NetworkFileType, Exception>(NetworkFileType.PROBE, null);
                 }
             }
             for (String header : getPossibleHeaders(DataLoadPreferences.NE_ADJ_BTS)) {
                 if (headers.contains(header)) {
                     return new Pair<NetworkFileType, Exception>(NetworkFileType.NEIGHBOUR, null);
                 }
             }
             for (String header : getPossibleHeaders(DataLoadPreferences.NH_SECTOR)) {
                 if (headers.contains(header)) {
                     return new Pair<NetworkFileType, Exception>(NetworkFileType.RADIO_SECTOR, null);
                 }
             }
             for (String header : getPossibleHeaders(DataLoadPreferences.NH_SITE)) {
                 if (headers.contains(header)) {
                     return new Pair<NetworkFileType, Exception>(NetworkFileType.RADIO_SITE, null);
                 }
             }
             List<String>possibleHeaders=new LinkedList<String>();
             possibleHeaders.addAll(Arrays.asList(getPossibleHeaders(DataLoadPreferences.TR_SITE_ID_SERV)));
             possibleHeaders.addAll(Arrays.asList(getPossibleHeaders(DataLoadPreferences.TR_SITE_NO_SERV)));
             possibleHeaders.addAll(Arrays.asList(getPossibleHeaders(DataLoadPreferences.TR_ITEM_NAME_SERV)));
             possibleHeaders.addAll(Arrays.asList(getPossibleHeaders(DataLoadPreferences.TR_SITE_ID_NEIB)));
             possibleHeaders.addAll(Arrays.asList(getPossibleHeaders(DataLoadPreferences.TR_SITE_NO_NEIB)));
             possibleHeaders.addAll(Arrays.asList(getPossibleHeaders(DataLoadPreferences.TR_ITEM_NAME_NEIB)));
             for (String header : possibleHeaders) {
                 if (headers.contains(header)) {
                     return new Pair<NetworkFileType, Exception>(NetworkFileType.TRANSMISSION, null);
                 }
             }
             return new Pair<NetworkFileType, Exception>(null, null);
         } catch (Exception e) {
             return new Pair<NetworkFileType, Exception>(null, e);
         }
     }
 
     /**
      * @param key -key of value from preference store
      * @return array of possible headers
      */
     public static String[] getPossibleHeaders(String key) {
         String text = NeoLoaderPlugin.getDefault().getPreferenceStore().getString(key);
         if (text == null) {
             return new String[0];
         }
         String[] array = text.split(",");
         List<String> result = new ArrayList<String>();
         for (String string : array) {
             String value = string.trim();
             if (!value.isEmpty()) {
                 result.add(value);
             }
         }
         return result.toArray(new String[0]);
     }
 
     /**
      * Confirm load network on map
      * 
      * @param map map
      * @param fileName name of loaded file
      * @return true or false
      */
     public static boolean confirmAddToMap(final IMap map, final String fileName) {
     
         final IPreferenceStore preferenceStore = NeoLoaderPlugin.getDefault().getPreferenceStore();
         return (Integer)ActionUtil.getInstance().runTaskWithResult(new RunnableWithResult<Integer>() {
             int result;
     
             @Override
             public void run() {
                 boolean boolean1 = preferenceStore.getBoolean(DataLoadPreferences.ZOOM_TO_LAYER);
                 String message = String.format(NeoLoaderPluginMessages.ADD_LAYER_MESSAGE, fileName, map.getName());
                 if (map == ApplicationGIS.NO_MAP) {
                     message = String.format(NeoLoaderPluginMessages.ADD_NEW_MAP_MESSAGE, fileName);
                 }
                 // MessageBox msg = new
                 // MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                 // SWT.YES | SWT.NO);
                 // msg.setText(NeoLoaderPluginMessages.ADD_LAYER_TITLE);
                 // msg.setMessage(message);
                 MessageDialogWithToggle dialog = MessageDialogWithToggle.openYesNoQuestion(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                         NeoLoaderPluginMessages.ADD_LAYER_TITLE, message, NeoLoaderPluginMessages.TOGLE_MESSAGE, boolean1, preferenceStore,
                         DataLoadPreferences.ZOOM_TO_LAYER);
                 result = dialog.getReturnCode();
                 if (result == IDialogConstants.YES_ID) {
                     preferenceStore.putValue(DataLoadPreferences.ZOOM_TO_LAYER, String.valueOf(dialog.getToggleState()));
                 }
             }
     
             @Override
             public Integer getValue() {
                 return result;
             }
         }) == IDialogConstants.YES_ID;
     }
 
     /**
      * @param firstDataset
      */
     public static void addGisNodeToMap(String dataName, Node... gisNodes) {
         try {
             String databaseLocation = NeoServiceProvider.getProvider().getDefaultDatabaseLocation();
             URL url = new URL("file://" + databaseLocation);
             IService curService = CatalogPlugin.getDefault().getLocalCatalog().getById(IService.class, url, null);
             IMap map = ApplicationGIS.getActiveMap();
             if (confirmAddToMap(map, dataName)) {
                 List<ILayer> layerList = new ArrayList<ILayer>();
                 List<IGeoResource> listGeoRes = new ArrayList<IGeoResource>();
                 for (Node gis : gisNodes) {
                 map = ApplicationGIS.getActiveMap();
                 if (curService != null && NetworkLoader.findLayerByNode(map, gis) == null) {
                     for (IGeoResource iGeoResource : curService.resources(null)) {
                         if (iGeoResource.canResolve(Node.class)) {
                             if (iGeoResource.resolve(Node.class, null).equals(gis)) {
                                 listGeoRes.add(iGeoResource);
                                 break;
                             }
                         }
                     };
                 }
                 }
                 layerList.addAll(ApplicationGIS.addLayersToMap(map, listGeoRes, 0));
     
                 IPreferenceStore preferenceStore = NeoLoaderPlugin.getDefault().getPreferenceStore();
                 if (preferenceStore.getBoolean(DataLoadPreferences.ZOOM_TO_LAYER)) {
                     LoaderUtils.zoomToLayer(layerList);
                 }
             }
         } catch (Exception e) {
             NeoCorePlugin.error(null, e);
             throw (RuntimeException)new RuntimeException().initCause(e);
         }
     
     }
 
     /**
      * Zoom To 1st layers in list
      * 
      * @param layers list of layers
      */
     public static void zoomToLayer(final List< ? extends ILayer> layers) {
         ActionUtil.getInstance().runTask(new Runnable() {
             @Override
             public void run() {
                 ZoomToLayer zoomCommand = new ZoomToLayer();
                 zoomCommand.selectionChanged(null, new StructuredSelection(layers));
                 zoomCommand.runWithEvent(null, null);
             }
         }, true);
     }
     /**
      * Calculates list of files 
      *
      * @param directoryName directory to import
      * @param filter - filter (if filter teturn true for directory this directory will be handled also  )
      * @return list of files to import
      */
     public static List<File> getAllFiles(String directoryName, FileFilter filter) {
         File directory = new File(directoryName);
         LinkedList<File> result = new LinkedList<File>();
         for (File childFile : directory.listFiles(filter)) {
             if (childFile.isDirectory()) {
                 result.addAll(getAllFiles(childFile.getAbsolutePath(),filter));
             }
             else  {
                 result.add(childFile);
             }
         }
         return result;
     }
     /**
      *find or create OSS node
      * 
      * @return
      */
     public static Node findOrCreateOSSNode(OssType ossType,String ossName,GraphDatabaseService neo) {
         Node oss;
         Transaction tx = neo.beginTx();
         try {
             oss = NeoUtils.findRootNodeByName(ossName, neo);
             if (oss == null) {
                 oss = neo.createNode();
                 oss.setProperty(INeoConstants.PROPERTY_TYPE_NAME, NodeTypes.OSS.getId());
                 oss.setProperty(INeoConstants.PROPERTY_NAME_NAME, ossName);
                 ossType.setOssType(oss, neo);
                //TODO remove this relation!
                 String aweProjectName = LoaderUtils.getAweProjectName();
                 NeoCorePlugin.getDefault().getProjectService().addDataNodeToProject(aweProjectName, oss);
                 neo.getReferenceNode().createRelationshipTo(oss, GeoNeoRelationshipTypes.CHILD);
             }
             assert NodeTypes.OSS.checkNode(oss);
             tx.success();
         } finally {
             tx.finish();
         }
         return oss;
     }
 
     /**
      *find or create Cell root node
      * @param ossRoot oss_gpeh root node
      * @param neo neoservice
      * @return Pair<cell_root node, last child or null if no child found>
      */
     public static Pair<Node, Node> findOrCreateGPEHCellRootNode(Node ossRoot, GraphDatabaseService neo) {
         Transaction tx = neo.beginTx();
         try {
             Relationship relation = ossRoot.getSingleRelationship(GeoNeoRelationshipTypes.CELLS, Direction.OUTGOING);
             Node cellNode;
             if (relation!=null){
                 cellNode=relation.getOtherNode(ossRoot); 
             }else{
                 cellNode=neo.createNode();
                 //TODO check name
                 cellNode.setProperty(INeoConstants.PROPERTY_NAME_NAME, "CELL ROOT");
                 NodeTypes.GPEH_CELL_ROOT.setNodeType(cellNode, neo);
                 ossRoot.createRelationshipTo(cellNode,GeoNeoRelationshipTypes.CELLS);
             }
             Node child=NeoUtils.findLastChild(cellNode, neo);
             tx.success();
             return new Pair<Node,Node>(cellNode,child);
         } finally {
             tx.finish();
         }
     }
     /**
      * get file extension
      *
      * @param fileName - file name
      * @return file extension
      */
     public static String getFileExtension(String fileName) {
         int idx = fileName.lastIndexOf(".");
         return idx < 1 ? "" : fileName.substring(idx);
     }
 
 
 
     public static File getFirstFile(String dirName) {
         File file = new File(dirName);
         if (file.isFile()){
             return file;
         }
         File[] list = file.listFiles();
         if (list.length>0){
             return list[0];
         }else{
             //TODO optimize
           List<File> all = getAllFiles(dirName, new FileFilter() {
                 
                 @Override
                 public boolean accept(File pathname) {
                     return true;
                 }
             });
           if (all.isEmpty()){
               return null;
           }else{
               return all.iterator().next();
           }
         }
     }
     
     /**
      * Gets the selected nodes.
      *
      * @param service the service
      * @return the selected nodes
      */
     public static LinkedHashSet<Node>getSelectedNodes(GraphDatabaseService service){
         LinkedHashSet<Node> selectedNode = new LinkedHashSet<Node>();
         String storedId = NeoLoaderPlugin.getDefault().getPreferenceStore().getString(DataLoadPreferences.SELECTED_DATA);
         if (!StringUtil.isEmpty(storedId)) {
             Transaction tx = service.beginTx();
             try {
                 StringTokenizer st = new StringTokenizer(storedId, DataLoadPreferences.CRS_DELIMETERS);
                 while (st.hasMoreTokens()) {
                     String nodeId = st.nextToken();
                     try {
                         Node node = service.getNodeById(Long.parseLong(nodeId));
                         if (NeoUtils.isRoootNode(node)) {
                             selectedNode.add(node);
                         }
                     } catch (Exception e) {
                         Logger.getLogger(LoaderUtils.class).error("not loaded id " + nodeId, e);
                     }
                     
                 }
             } finally {
                 tx.finish();
             }
         }
         return selectedNode;
     }
     
     /**
      * Store selected nodes.
      *
      * @param selectedNodes the selected nodes
      */
     public static void storeSelectedNodes(Set<Node>selectedNodes){
         StringBuilder st = new StringBuilder();
         for (Node selNode : selectedNodes) {
             st.append(DataLoadPreferences.CRS_DELIMETERS).append(selNode.getId());
         }
         String value = st.length() < 1 ? "" : st.substring(DataLoadPreferences.CRS_DELIMETERS.length());
         NeoLoaderPlugin.getDefault().getPreferenceStore().setValue(DataLoadPreferences.SELECTED_DATA, value);
     }
     
 }
