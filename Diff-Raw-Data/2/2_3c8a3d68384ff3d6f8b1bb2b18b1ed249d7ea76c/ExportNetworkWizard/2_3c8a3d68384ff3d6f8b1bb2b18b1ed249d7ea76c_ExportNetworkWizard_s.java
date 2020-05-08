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
 
 package org.amanzi.awe.views.network.view;
 
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.amanzi.awe.views.network.NetworkTreePlugin;
 import org.amanzi.neo.loader.core.LoaderUtils;
 import org.amanzi.neo.loader.core.preferences.DataLoadPreferences;
 import org.amanzi.neo.loader.ui.NeoLoaderPlugin;
 import org.amanzi.neo.services.DatasetService;
 import org.amanzi.neo.services.INeoConstants;
 import org.amanzi.neo.services.NeoServiceFactory;
 import org.amanzi.neo.services.enums.DatasetRelationshipTypes;
 import org.amanzi.neo.services.enums.GeoNeoRelationshipTypes;
 import org.amanzi.neo.services.enums.INodeType;
 import org.amanzi.neo.services.enums.NodeTypes;
 import org.amanzi.neo.services.network.FrequencyPlanModel;
 import org.amanzi.neo.services.network.NetworkModel;
 import org.amanzi.neo.services.node2node.NodeToNodeRelationModel;
 import org.amanzi.neo.services.node2node.NodeToNodeTypes;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.wizard.Wizard;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IExportWizard;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.PlatformUI;
 import org.neo4j.graphdb.Direction;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.NotFoundException;
 import org.neo4j.graphdb.Path;
 import org.neo4j.graphdb.Relationship;
 import org.neo4j.graphdb.traversal.Evaluation;
 import org.neo4j.graphdb.traversal.Evaluator;
 import org.neo4j.graphdb.traversal.TraversalDescription;
 import org.neo4j.graphdb.traversal.Traverser;
 import org.neo4j.helpers.Predicate;
 import org.neo4j.kernel.Traversal;
 import org.neo4j.kernel.Uniqueness;
 import org.neo4j.neoclipse.property.RelationshipTypes;
 
 import au.com.bytecode.opencsv.CSVWriter;
 
 /**
  * <p>
  * Wizard for export network to csv file
  * </p>
  * .
  * 
  * @author NiCK
  * @since 1.0.0
  */
 public class ExportNetworkWizard extends Wizard implements IExportWizard {
 
     /** The selection page. */
     ExportNetworkWizardSelectionPage selectionPage = null;
 
     /** The column config page. */
     ExportNetworkWizardColumnsConfigPage columnConfigPage = null;
 
     /** The file property page. */
     ExportNetworkWizardFilePropertyPage filePropertyPage = null;
 
     /** The saving data selection page. */
     private static ExportNetworkWizardSavingDataSelectionPage savingDataSelectionPage = null;
 
     private IStructuredSelection selection;
 
     private static ArrayList<ExportNetworkWizardColumnsConfigPage> availablePages = new ArrayList<ExportNetworkWizardColumnsConfigPage>();
     private static int currentIndex;
     public static final String PROPERTY_CSV = "propertyCSV";
     private static final HashMap<String, Map<String, Map<String, String>>> pagesWithProperties = new HashMap<String, Map<String,Map<String,String>>>();
     private static ArrayList<String> frequencyPlanModelNames = new ArrayList<String>();
     private static String[] selectionFrequencyPlanModelNames = null;
     private static boolean stateOfNetworkDataCheckbox = true;
     
     @Override
     public boolean performFinish() {
         final String fileSelected = selectionPage.getFileName();
         final String separator = getSeparator();
         final String quoteChar = getQuoteChar();
         final String charSet = filePropertyPage.getCharsetValue();
         final Node rootNode = selectionPage.getSelectedNode();
         //final Map<String, Map<String, String>> propertyMap = columnConfigPage.getPropertyMap();
         final ArrayList<Boolean> checkBoxStates = savingDataSelectionPage.getCheckBoxesState();
         final String fileWithPrefix = fileSelected;
         final HashMap<String, Map<String, Map<String, String>>> pagesWithProperties = selectionPage.getPagesWithProperties();
         Job exportJob = new Job("Network export") {
 
             @Override
             protected IStatus run(IProgressMonitor monitor) {
                 try {
                     runExport(fileWithPrefix, pagesWithProperties, rootNode, checkBoxStates, separator, quoteChar, charSet);
                     return Status.OK_STATUS;
                 } catch (IOException e) {
                     return new Status(Status.ERROR, NetworkTreePlugin.PLUGIN_ID, e.getLocalizedMessage(), e);
                 } finally {
                     // ActionUtil.getInstance().runTask(new Runnable() {
                     //
                     // @Override
                     // public void run() {
                     // // button.setEnabled(true);
                     // // tableControl.setEnabled(true);
                     // }
                     // }, true);
                 }
             }
         };
         exportJob.schedule();
         return true;
     }
 
     /**
      * Gets the quote char.
      * 
      * @return the quote char
      */
     private String getQuoteChar() {
         return filePropertyPage.getTextDelValue();
     }
 
     /**
      * Gets the separator.
      * 
      * @return the separator
      */
     private String getSeparator() {
         return filePropertyPage.getFieldDelValue();
     }
 
     @Override
     public void addPages() {
         availablePages.clear();
         
         super.addPages();
         if (selectionPage == null) {
             selectionPage = new ExportNetworkWizardSelectionPage("mainPage", selection);
         }
 
         if (savingDataSelectionPage == null) {
             savingDataSelectionPage = new ExportNetworkWizardSavingDataSelectionPage("savingDataSelectionPage");
         }
         if (columnConfigPage == null) {
             columnConfigPage = new ExportNetworkWizardColumnsConfigPage(ColumnsConfigPageTypes.NETWORK_SECTOR_DATA.getName(),
                     "Export network");
         }
         if (filePropertyPage == null) {
             // NeoLoaderPlugin.getDefault().getPreferenceStore().getString(DataLoadPreferences.DEFAULT_CHARSET);
             filePropertyPage = new ExportNetworkWizardFilePropertyPage(PROPERTY_CSV, "windows-1251", "\t", "\"");
         }
 
         ArrayList<Boolean> checkBoxStates = savingDataSelectionPage.getDefaultCheckBoxesState();
         ArrayList<String> nameOfPages = savingDataSelectionPage.getNameOfPages();
         Iterator<String> iterator = nameOfPages.iterator();
         // list.add(columnConfigPage);
         for (Boolean checkbox : checkBoxStates) {
             String nameOfPage = iterator.next();
             if (checkbox == true) {
                 availablePages.add(new ExportNetworkWizardColumnsConfigPage(nameOfPage, nameOfPage));
             }
         }
         addPage(selectionPage);
         addPage(savingDataSelectionPage);
         addPage(columnConfigPage);
         for (ExportNetworkWizardColumnsConfigPage page : availablePages) {
             addPage(page);
         }
         addPage(filePropertyPage);
     }
 
     @Override
     public void init(IWorkbench workbench, IStructuredSelection selection) {
         setNeedsProgressMonitor(false);
         this.selection = selection;
         setWindowTitle("Export Network");
     }
 
     public static ExportNetworkWizardSavingDataSelectionPage getSavingDataPage() {
         return savingDataSelectionPage;
     }
 
     public static ArrayList<ExportNetworkWizardColumnsConfigPage> getAvailablePages() {
         return availablePages;
     }
 
     public static void removeFromAvailablePages(String nameOfPage) {
         int index = 0;
         for (ExportNetworkWizardColumnsConfigPage page : availablePages) {
             if (page.getName().equals(nameOfPage)) {
                 availablePages.remove(index);
                 currentIndex--;
                 break;
             }
 
             index++;
         }
     }
 
     public static int getCurrentIndex() {
         return currentIndex;
     }
 
     public static void setCurrentIndex(int curIndex) {
         currentIndex = curIndex;
     }
     
     public static void setStateOfNetworkDataCheckbox(boolean state) {
         stateOfNetworkDataCheckbox = state;
     }
     
     public static boolean getStateOfNetworkDataCheckbox() {
         return stateOfNetworkDataCheckbox;
     }
     
     /**
      * @param selectionFrequencyPlanModelNames The selectionFrequencyPlanModelNames to set.
      */
     public static void setSelectionFrequencyPlanModelNames(String[] selectionFrequencyPlanModelNames) {
         ExportNetworkWizard.selectionFrequencyPlanModelNames = selectionFrequencyPlanModelNames;
     }
 
     /**
      * @return Returns the selectionFrequencyPlanModelNames.
      */
     public static String[] getSelectionFrequencyPlanModelNames() {
         return selectionFrequencyPlanModelNames;
     }
 
     /**
      * @param frequencyPlanModelNames The frequencyPlanModelNames to set.
      */
     public static void setFrequencyPlanModelNames(ArrayList<String> frequencyPlanModelNames) {
         ExportNetworkWizard.frequencyPlanModelNames = frequencyPlanModelNames;
     }
 
     /**
      * @return Returns the frequencyPlanModelNames.
      */
     public static ArrayList<String> getFrequencyPlanModelNames() {
         return frequencyPlanModelNames;
     }
 
     /**
      * Displays error message instead of throwing an exception.
      * 
      * @param e exception thrown
      */
     private void displayErrorMessage(final Exception e) {
         final Display display = PlatformUI.getWorkbench().getDisplay();
         display.asyncExec(new Runnable() {
 
             @Override
             public void run() {
                 MessageDialog.openError(display.getActiveShell(), "Export problem", e.getMessage());
             }
 
         });
     }
 
     /**
      * Run export.
      * 
      * @param fileSelected the file selected
      * @param propertyMapToNetwork the property map
      * @param rootNode the root node
      * @param separator the separator
      * @param quoteChar the quote char
      * @param charSet the char set
      * @throws IOException
      */
     @SuppressWarnings("deprecation")
     private void runExport(final String fileSelected, final HashMap<String, Map<String, Map<String, String>>> propertyMap, Node rootNode,
             ArrayList<Boolean> checkBoxStates, String separator, String quoteChar, String charSet)
             throws IOException {
         final Map<String, Map<String, String>> propertyMapToNetwork = propertyMap.get(ColumnsConfigPageTypes.NETWORK_SECTOR_DATA.getName());
         
         DatasetService datasetService = NeoServiceFactory.getInstance().getDatasetService();
 
         String[] strtypes = datasetService.getSructureTypesId(rootNode);
         List<String> headers = new ArrayList<String>();
         List<String> usingHeadersFromStructure = new ArrayList<String>();
 
         for (int i = 1; i < strtypes.length; i++) {
             headers.add(strtypes[i]);
         }
 
         TraversalDescription descr = Traversal.description().depthFirst().uniqueness(Uniqueness.NONE)
                 .relationships(GeoNeoRelationshipTypes.CHILD, Direction.OUTGOING).filter(Traversal.returnAllButStartNode());
         descr = descr.filter(new Predicate<Path>() {
 
             @Override
             public boolean accept(Path paramT) {
                 return !paramT.endNode().hasRelationship(GeoNeoRelationshipTypes.CHILD, Direction.OUTGOING);
             }
         });
         Iterator<Path> iter = descr.traverse(rootNode).iterator();
         List<String> fields = new ArrayList<String>();
         if (fileSelected != null) {
 //
 //            String ext = "";
 //
 //            String extention = LoaderUtils.getFileExtension(fileSelected);
 //            if (extention.equals("")) {
 //                ext = ".csv";
 //            } else if (extention.equals(".")) {
 //                ext = "csv";
 //            }
 
             char separatorChar = separator.charAt(0);
             char quote = quoteChar.isEmpty() ? CSVWriter.NO_QUOTE_CHARACTER : quoteChar.charAt(0);
             CSVWriter writer = new CSVWriter(new OutputStreamWriter(new FileOutputStream(fileSelected + ColumnsConfigPageTypes.NETWORK_SECTOR_DATA.getFileName()), charSet),
                     separatorChar, quote);
 
             try {
                 for (String headerType : headers) {
                     Map<String, String> propertyCol = propertyMapToNetwork.get(headerType);
                     if (propertyCol == null) {
                         continue;
                     } else {
                         // save using headers from newtwork structure
                         usingHeadersFromStructure.add(headerType);
                     }
                     for (String col : propertyCol.values()) {
                         fields.add(col);
                     }
                 }
 
                 // choose which types we write
                 int k = 1;
                 strtypes = new String[usingHeadersFromStructure.size() + 1];
                 strtypes[0] = "network";
                 for (String headerType : usingHeadersFromStructure) {
                     strtypes[k] = headerType;
                     k++;
                 }
 
                 writer.writeNext(fields.toArray(new String[0]));
                 
                 // export all data
                 while (iter.hasNext()) {
                     fields.clear();
                     Path path = iter.next();
                     int currentIndex = 1;
                     for (Node node : path.nodes()) {
                         INodeType nodeType = datasetService.getNodeType(node);
                         if (nodeType == NodeTypes.NETWORK) {
                             continue;
                         }
                         Map<String, String> propertyCol = propertyMapToNetwork.get(nodeType.getId());
                         // if property exist, but not need write
                         if (propertyCol == null) {
                             continue;
                         }
                         int i = 0, index = 0;
                         while (!nodeType.getId().equals(strtypes[i])) {
                             if (propertyMapToNetwork.get(strtypes[i]) == null)
                                 index++;
                             else
                                 index += propertyMapToNetwork.get(strtypes[i]).keySet().size();
                             i++;
                         }
 
                         if (currentIndex != index && propertyCol != null) {
                             for (int j = 0; j < index - currentIndex; j++) {
                                 fields.add("");
                             }
                             currentIndex += (index - currentIndex);
                         } else if (propertyCol == null) {
                             if (index == currentIndex) {
                                 currentIndex++;
                             } else {
                                 currentIndex += (index - currentIndex);
                             }
                         }
                         if (propertyCol != null) {
                             for (String propertyName : propertyCol.keySet()) {
                                 fields.add(String.valueOf(node.getProperty(propertyName, "")));
                                 currentIndex++;
                             }
                         }
                     }
                     if (fields.size() != 0)
                         writer.writeNext(fields.toArray(new String[0]));
                 }
             } finally {
                 writer.close();
             }
 
         }
         
         //export all additional data
         int indexOfPage = 0;
         ArrayList<ColumnsConfigPageTypes> exportingTypes = new ArrayList<ColumnsConfigPageTypes>();
         for (Boolean checkBoxState : checkBoxStates) {
             if (checkBoxState) {
                 ColumnsConfigPageTypes pageType = ColumnsConfigPageTypes.findColumnsConfigPageTypeByIndex(indexOfPage);
                 exportingTypes.add(pageType);
             }
             indexOfPage++;
         }
         
         runExportAdditionalData(rootNode, exportingTypes, propertyMap, fileSelected, separator, quoteChar, charSet);
     }
     
     /**
      * Create writer to import data to file
      *
      * @param dataName name of file to some data
      * @param fileWithPrefix prefix of saving file
      * @param separator the separator
      * @param quoteChar the quote char
      * @param charSet the char set
      * @return CSVWriter
      * @throws IOException
      */
     private CSVWriter createWriterToSomeData(String fileWithPrefix, String separator, String quoteChar, String charSet) throws IOException  {
         String ext = "";
 
         String extention = LoaderUtils.getFileExtension(fileWithPrefix);
         if (extention.equals("")) {
             ext = ".csv";
         } else if (extention.equals(".")) {
             ext = "csv";
         }
 
         char separatorChar = separator.charAt(0);
         char quote = quoteChar.isEmpty() ? CSVWriter.NO_QUOTE_CHARACTER : quoteChar.charAt(0);
         CSVWriter writer = new CSVWriter(new OutputStreamWriter(new FileOutputStream(fileWithPrefix + ext), charSet),
                 separatorChar, quote);   
         
         return writer;
     }
     
     @SuppressWarnings("deprecation")
     private void runExportAdditionalData(Node rootNode, ArrayList<ColumnsConfigPageTypes> pageTypes, 
             HashMap<String, Map<String, Map<String, String>>> propertyMap, String folderName, String separator, 
             String quoteChar, String charSet) throws IOException {
         
         NetworkModel networkModel = new NetworkModel(rootNode);
         
         for (ColumnsConfigPageTypes pageType : pageTypes) {
             CSVWriter writer = null;
             if (pageType != ColumnsConfigPageTypes.TRX_DATA)
                 writer = createWriterToSomeData(folderName + pageType.getFileName(), separator, quoteChar, charSet);
             
             DatasetService datasetService = NeoServiceFactory.getInstance().getDatasetService();
             TraversalDescription descr = Traversal.description().depthFirst().uniqueness(Uniqueness.NONE)
                                         .relationships(GeoNeoRelationshipTypes.CHILD, Direction.OUTGOING).filter(Traversal.returnAllButStartNode());
             descr = descr.filter(new Predicate<Path>() {
             
                 @Override
                 public boolean accept(Path paramT) {
                     return !paramT.endNode().hasRelationship(GeoNeoRelationshipTypes.CHILD, Direction.OUTGOING);
                 }
             });
             Iterator<Path> iter = descr.traverse(rootNode).iterator();
     
             ArrayList<String> fields = new ArrayList<String>();
             ArrayList<String> oldFields = new ArrayList<String>();
             oldFields.add("start string");
             
             String[] headersToArray = null;
             if (pagesWithProperties.get(pageType.getName()) != null) {
                 Collection<String> headers = pagesWithProperties.get(pageType.getName()).get("sector").values();
                 if (headers == null) {
                     headersToArray = pageType.getProperties();
                 }
                 else {
                     headersToArray = new String[headers.size()];
                 }
                 int i = 0;
                 for (String str : headers) {
                     headersToArray[i++] = str;
                 }
             }
             else {
                 headersToArray = pageType.getProperties();
             }
             
             int notEmptyProperties = 0;
             
             switch (pageType) {
             case NEIGBOURS_DATA:
                 writer.writeNext(headersToArray);
                 
                 Set<NodeToNodeRelationModel> neighbourModels = networkModel.findAllN2nModels(NodeToNodeTypes.NEIGHBOURS);
                 for (NodeToNodeRelationModel model : neighbourModels) {
                     String servingSector = "", targetSector = "", 
                         twoSectorTogether = "", attempts = "";
                     Integer index = 0;
                     HashMap<String, Integer> twoSectorTogetherList = new HashMap<String, Integer>();
                     
                     Traverser traverser = model.getNeighTraverser(new Evaluator() {
                         
                         @Override
                         public Evaluation evaluate(Path arg0) {
                             return Evaluation.INCLUDE_AND_CONTINUE;
                         }
                     });
                     for (Node node : traverser.nodes()) {
                         for (Relationship rel : model.getOutgoingRelations(node)) {
                             fields.clear();
                             servingSector = rel.getStartNode().getProperty(INeoConstants.PROPERTY_NAME_NAME).toString();
                             targetSector = rel.getEndNode().getProperty(INeoConstants.PROPERTY_NAME_NAME).toString();
                             twoSectorTogether = servingSector + targetSector;
                             if (twoSectorTogetherList.get(twoSectorTogether) == null) {
                                 twoSectorTogetherList.put(twoSectorTogether, index);
                                 index++;
                                 fields.add(servingSector);
                                 fields.add(attempts);
                                 fields.add(targetSector);
                                 if (!fields.containsAll(oldFields)) {
                                     writer.writeNext(fields.toArray(new String[0]));
                                     oldFields.clear();
                                     oldFields.addAll(fields);
                                 }
                             }
                         }
                     }
                 }
                 
                 writer.close();
                 break;
                
             case INTERFERENCE_MATRIX:
                 writer.writeNext(headersToArray);
                 
                 Set<NodeToNodeRelationModel> interferenceModels = networkModel.findAllN2nModels(NodeToNodeTypes.INTERFERENCE_MATRIX);
                 for (NodeToNodeRelationModel model : interferenceModels) {
                     String adj = "", co = "", source = "",
                     servingSector = "", interferingSector = "", twoSectorTogether = "";
                     Integer index = 0;
                     HashMap<String, Integer> twoSectorTogetherList = new HashMap<String, Integer>();
                     
                     Traverser traverser = model.getNeighTraverser(new Evaluator() {
                         
                         @Override
                         public Evaluation evaluate(Path arg0) {
                             return Evaluation.INCLUDE_AND_CONTINUE;
                         }
                     });
                     for (Node node : traverser.nodes()) {
                         for (Relationship rel : model.getOutgoingRelations(node)) {
                             fields.clear();
                             servingSector = rel.getStartNode().getProperty(INeoConstants.PROPERTY_NAME_NAME).toString();
                             interferingSector = rel.getEndNode().getProperty(INeoConstants.PROPERTY_NAME_NAME).toString();
                             twoSectorTogether = servingSector + interferingSector;
                             if (twoSectorTogetherList.get(twoSectorTogether) == null) {
                                 twoSectorTogetherList.put(twoSectorTogether, index);
                                 index++;
                                     
                                 adj = rel.getProperty("adj").toString();
                                 co = rel.getProperty("co").toString();
                                 fields.add(servingSector);
                                 fields.add(interferingSector);
                                 fields.add(source);
                                 fields.add(co);
                                 fields.add(adj);
                                 if (!fields.containsAll(oldFields)) {
                                     writer.writeNext(fields.toArray(new String[0]));
                                     oldFields.clear();
                                     oldFields.addAll(fields);
                                 }
                             }
                         }
                     }
                 }
                 
                 writer.close();
                 break;
             case FREQUENCY_CONSTRAINT_DATA:
                 writer.writeNext(headersToArray);
                 
                 NodeToNodeRelationModel n2nIllegalFrequency = networkModel.getIllegalFrequency();
                 String chanellType = "", trx_id = "", sector_name = "", 
                     frequency = "", type = "", penalty = "";
 
                 Traverser traverser = n2nIllegalFrequency.getServTraverser(new Evaluator() {
                     
                     @Override
                     public Evaluation evaluate(Path arg0) {
                         return Evaluation.INCLUDE_AND_CONTINUE;
                     }
                 });
                 for (Node servNode : traverser.nodes()) {
                     trx_id = (String)servNode.getProperty(INeoConstants.PROPERTY_NAME_NAME.toString());
                     Node carrier = n2nIllegalFrequency.findNodeFromProxy(servNode);
                     for (Relationship rel2 : carrier.getRelationships()) {
                         if (rel2.isType(RelationshipTypes.CHILD)) {
                             sector_name = rel2.getStartNode().getProperty(INeoConstants.PROPERTY_NAME_NAME).toString();
                         }
                         if (rel2.isType(DatasetRelationshipTypes.PLAN_ENTRY)) {
                             try {
                                 frequency = rel2.getEndNode().getProperty("arfcn").toString(); 
                             }
                             catch (Exception e) {
                                 frequency = "";
                             }
                         }
                     }
                     for (Relationship rel : n2nIllegalFrequency.getOutgoingRelations(servNode)) {
                         try {
                             chanellType = rel.getProperty("channel_type").toString();
                             type = rel.getProperty("type").toString();
                             penalty = rel.getProperty("penalty").toString();
                         }
                         catch (NotFoundException e) {
                             
                         }
                         chanellType = chanellType == null ? "" : chanellType;
                         type = type == null ? "" : type;
                         penalty = penalty == null ? "" : penalty;
                         
                         fields.add(sector_name);
                         fields.add(trx_id);
                         fields.add(chanellType);
                         fields.add(frequency);
                         fields.add(type);
                         fields.add(penalty);
                         if (!fields.contains(""))
                             writer.writeNext(fields.toArray(new String[0]));
                         fields.clear();
                     }
                 }
                 
                 writer.close();
                 break;
             case TRX_DATA:
                 boolean isNeedExportModel = false;
                 long timestamp = 0;
                 String latestModelName = null;
                 
                 Set<FrequencyPlanModel> frequencyModels = networkModel.findAllFrqModel();
                 Iterable<Node> nodes = networkModel.findAllNodeByType(NodeTypes.TRX);
                 
                 for (FrequencyPlanModel model : frequencyModels) {
                     for (String selection : getSelectionFrequencyPlanModelNames()) {
                         if (selection.equals(model.getName()))
                             isNeedExportModel = true;
                         
                         if (selection.equals("latest")) {
                             LABEL: for (Node carrierNode : nodes) {
                                 Node planNode = model.findPlanNode(carrierNode);
                                 if (planNode != null) {
                                     for (Relationship rel : planNode.getRelationships()) {
                                         if (rel.getType().toString().equals(DatasetRelationshipTypes.CHILD.toString())) {
                                             Object timestampObject = null;
                                             if (rel.getStartNode().hasProperty("time"))
                                                 timestampObject = rel.getStartNode().getProperty("time");
                                             if (timestampObject != null) {
                                                 long timestampTemp = Long.parseLong(timestampObject.toString());
                                                 if (timestampTemp > timestamp) {
                                                     timestamp = timestampTemp;
                                                     latestModelName = model.getName();
                                                 }
                                                 break LABEL;
                                             }
                                         }
                                     }
                                 }
                             }
                         }
                     }
                     if (isNeedExportModel) {
                         runExportFrequencyPlan(nodes, model, headersToArray, folderName, separator, quoteChar, charSet);
                     }
                     isNeedExportModel = false;
                 }
                 
                 if (timestamp != 0) {
                     FrequencyPlanModel model = FrequencyPlanModel.getModel(rootNode, latestModelName);
                     runExportFrequencyPlan(nodes, model, headersToArray, folderName, separator, quoteChar, charSet);
                 }
                 break;
             case TRAFFIC_DATA:
             case SEPARATION_CONSTRAINT_DATA:
                 try {
                     writer.writeNext(headersToArray);
                     String rightHeaders[] = null;
                     String value = null;
                     
                     // export all data
                     while (iter.hasNext()) {
                         fields.clear();
                         Path path = iter.next();
                         for (Node node : path.nodes()) {     
                             INodeType nodeType = datasetService.getNodeType(node);
                             if (nodeType == NodeTypes.SECTOR) {
                                 if (rightHeaders == null)
                                     rightHeaders = getRightHeaders(node, pageType);
                                 notEmptyProperties = 0;
                                 for (String propertyName : rightHeaders) {
                                     value = String.valueOf(node.getProperty(propertyName, ""));
                                     fields.add(value);
                                     if (!value.equals(""))
                                         notEmptyProperties++;
                                 }
                             }
                         }
                         
                         if (notEmptyProperties > 1 && !fields.containsAll(oldFields)) {
                             writer.writeNext(fields.toArray(new String[0]));
                             oldFields.clear();
                             oldFields.addAll(fields);
                         }
                     }
                 }
                 finally {
                     writer.close();
                 }
                 break;
             }
         }
     }
     
     
     private void runExportFrequencyPlan(Iterable<Node> nodes, FrequencyPlanModel model, String[] headersToArray,
             String fileWithPrefix, String separator, 
             String quoteChar, String charSet) throws IOException {
         ArrayList<String> fields = new ArrayList<String>();
         String[] rightHeadersFromPlan = null, 
         rightHeadersFromCarrier = null;
         String valueTRX = null;
         String sectorName = null;
         int notEmptyProperties = 0;
 
         CSVWriter trxWriter = createWriterToSomeData(fileWithPrefix + ColumnsConfigPageTypes.TRX_DATA.getFileName() + "_" + cleanHeader(model.getName()), separator, quoteChar, charSet);
         trxWriter.writeNext(headersToArray);
         for (Node carrierNode : nodes) {
             notEmptyProperties = 0;
             Node planNode = model.findPlanNode(carrierNode);
             for (Relationship relation : carrierNode.getRelationships()) {
                 if (relation.getType().toString().equals(DatasetRelationshipTypes.CHILD.toString())) {
                     sectorName = relation.getStartNode().getProperty(INeoConstants.PROPERTY_NAME_NAME).toString();
                 }
             }
             fields.add(sectorName);
             if (rightHeadersFromCarrier == null)
                 rightHeadersFromCarrier = getRightHeaders(carrierNode, ColumnsConfigPageTypes.TRX_DATA);
             if (planNode != null) {
                 if (rightHeadersFromPlan == null)
                     rightHeadersFromPlan = getRightHeaders(planNode, ColumnsConfigPageTypes.TRX_DATA);
             }
            for (int i = 0; i < rightHeadersFromCarrier.length; i++) {
                 valueTRX = String.valueOf(carrierNode.getProperty(rightHeadersFromCarrier[i], ""));
                 if (valueTRX == null || valueTRX.equals("") && planNode != null)
                     valueTRX = String.valueOf(planNode.getProperty(rightHeadersFromPlan[i], ""));
                 
                 fields.add(valueTRX);
                 if (!valueTRX.equals(""))
                     notEmptyProperties++;
             }
             
             if (notEmptyProperties > 1)
                 trxWriter.writeNext(fields.toArray(new String[0]));
             fields.clear();
         }
         
         trxWriter.close();
     }
     
     
     private String[] getRightHeaders(Node sectorNode, ColumnsConfigPageTypes pageType) {
         
         String rightHeaders[] = new String[pageType.getProperties().length];
         int index = 0;
         String[] propertyCol = pageType.getProperties();
         for (String propertyName : propertyCol) {
             propertyName = findNeedPropertyFromNode(propertyName, sectorNode);
             if (propertyName.equals("Sector")) {
                 LABEL: for (String header : getPossibleHeaders(DataLoadPreferences.NH_SECTOR)) {
                     for (String property : sectorNode.getPropertyKeys())
                     if (cleanHeader(header).equals(cleanHeader(property))) {
                         propertyName = cleanHeader(property);
                         break LABEL;
                     }
                 }
             }
             rightHeaders[index++] = propertyName;
         }
         return rightHeaders;
     }
     
     private String cleanHeader(String header) {
         return header.replaceAll("[\\s\\-\\[\\]\\(\\)\\/\\.\\\\\\:\\#]+", "_").replaceAll("[^\\w]+", "_").replaceAll("_+", "_").replaceAll("\\_$", "").toLowerCase();
     }
     
     /**
      * Check that node contains property
      *
      * @param possibleProperty possible property
      * @param node the node 
      * @return possible property or clean(possibleProperty)
      */
     private String findNeedPropertyFromNode(String possibleProperty, Node node) {
         for (String propertyName : node.getPropertyKeys()) {
             if (propertyName.equals(cleanHeader(possibleProperty)))
                 return propertyName;
         }
         return possibleProperty;
     }
     
 
     /**
      * Kasnitskij_V: Get synonyms to header
      * 
      * @param header -header of value from preference store
      * @return array of possible headers
      */
     protected static String[] getPossibleHeaders(String header) {
         if (header == null) {
             return new String[0];
         }
         String text = NeoLoaderPlugin.getDefault().getPreferenceStore().getString(header);
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
      * Kasnitskij_V:
      * 
      * @return return map in which key = propertyName and value = name of header
      */
     protected static HashMap<String, String> getMapPropertyNameHeader() {
         Map<String, String> map = new HashMap<String, String>();
         map.put("name_site", DataLoadPreferences.NH_SITE);
         map.put("name_sector", DataLoadPreferences.NH_SECTOR);
         map.put("lat", DataLoadPreferences.NH_LATITUDE);
         map.put("lon", DataLoadPreferences.NH_LONGITUDE);
         map.put("ci", DataLoadPreferences.NH_SECTOR_CI);
         map.put("lac", DataLoadPreferences.NH_SECTOR_LAC);
         map.put("beamwidth", DataLoadPreferences.NH_BEAMWIDTH);
         map.put("azimuth", DataLoadPreferences.NH_AZIMUTH);
         map.put("city", DataLoadPreferences.NH_CITY);
         map.put("msc", DataLoadPreferences.NH_MSC);
         map.put("bsc", DataLoadPreferences.NH_BSC);
 
         return (HashMap<String, String>)map;
     }
 
     public static void putIntoPropertyMap(String name, Map<String, Map<String, String>> propertyMap) {
         pagesWithProperties.put(name, propertyMap);
     }
 }
