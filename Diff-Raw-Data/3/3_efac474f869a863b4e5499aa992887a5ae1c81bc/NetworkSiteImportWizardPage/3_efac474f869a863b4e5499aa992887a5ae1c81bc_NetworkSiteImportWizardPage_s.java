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
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.amanzi.neo.core.INeoConstants;
 import org.amanzi.neo.core.enums.GisTypes;
 import org.amanzi.neo.core.enums.NetworkFileType;
 import org.amanzi.neo.core.enums.NetworkTypes;
 import org.amanzi.neo.core.enums.NodeTypes;
 import org.amanzi.neo.core.service.NeoServiceProvider;
 import org.amanzi.neo.core.utils.NeoUtils;
 import org.amanzi.neo.core.utils.Pair;
 import org.amanzi.neo.loader.LoadNetwork;
 import org.amanzi.neo.loader.LoaderUtils;
 import org.amanzi.neo.loader.NeighbourLoader;
 import org.amanzi.neo.loader.internal.NeoLoaderPluginMessages;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.neo4j.api.core.Direction;
 import org.neo4j.api.core.NeoService;
 import org.neo4j.api.core.Node;
 import org.neo4j.api.core.Relationship;
 import org.neo4j.api.core.Transaction;
 
 /**
  * <p>
  * Network Loader
  * </p>
  * 
  * @author TsAr
  * @since 1.0.0
  */
 public class NetworkSiteImportWizardPage extends WizardPage {
 
     /**
      * Constructor
      * 
      * @param pageName page name
      * @param description page description
      */
     public NetworkSiteImportWizardPage(String pageName, String description) {
         super(pageName);
         setTitle(pageName);
         setDescription(description);
         setPageComplete(isValidPage());
         networkNode = null;
     }
 
     private String fileName;
     private Composite main;
     protected Combo network;
     private FileFieldEditorExt editor;
     private HashMap<String, Node> members;
     private final Set<String> restrictedNames=new HashSet<String>();
     protected Node networkNode;
     private Label labNetworkDescr;
     private Pair<NetworkFileType, Exception> netwFile = new Pair<NetworkFileType, Exception>(null, null);
     private Combo networkType;
     protected String networkName;
 
     /**
      *check page
      * 
      * @return true if page valid
      */
     protected boolean isValidPage() {
         //TODO must be refactoring after change loaders
         final NetworkFileType type = netwFile.getLeft();
         if (fileName == null || type == null || networkName == null || networkName.isEmpty()||restrictedNames.contains(networkName)) {
             return false;
         }
         if (networkNode==null){
             if (type == NetworkFileType.NEIGHBOUR) {
                 return false;
             }
             return networkName != null && !networkName.isEmpty();
         }
         NetworkTypes netType = NetworkTypes.getNodeType(networkNode, NeoServiceProvider.getProvider().getService());
         if (netType!=null&&!netType.isCorrectFileType(type)){
             return false;
         }
         return true;
     }
 
     @Override
     public void createControl(Composite parent) {
         main = new Group(parent, SWT.NULL);
         main.setLayout(new GridLayout(3, false));
         Label label = new Label(main, SWT.LEFT);
         label.setText(NeoLoaderPluginMessages.NetworkSiteImportWizard_NETWORK);
         label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
         network = new Combo(main, SWT.DROP_DOWN);
         network.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
         network.setItems(getGisItems());
         network.addModifyListener(new ModifyListener() {
 
             @Override
             public void modifyText(ModifyEvent e) {
                 networkName = network.getText();
                 setPageComplete(isValidPage());
             }
         });
         network.addSelectionListener(new SelectionListener() {
 
             @Override
             public void widgetSelected(SelectionEvent e) {
                 networkName = network.getText();
                 networkNode = members.get(networkName);
                 updateLabelNetwDescr();
                 setPageComplete(isValidPage());
             }
 
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
                 widgetSelected(e);
             }
         });
         editor = new FileFieldEditorExt("fileSelectNeighb", NeoLoaderPluginMessages.NetworkSiteImportWizard_FILE, main); // NON-NLS-1
         editor.setDefaulDirectory(NeighbourLoader.getDirectory());
 
         editor.getTextControl(main).addModifyListener(new ModifyListener() {
             public void modifyText(ModifyEvent e) {
                 setFileName(editor.getStringValue());
                 updateLabelFileDescr();
                 if (networkName==null||networkName.trim().isEmpty()){
                     networkName=new java.io.File(getFileName()).getName(); 
                     network.setText(networkName);
                     networkNode = members.get(networkName);
                     setPageComplete(isValidPage());
                 }
             }
         });
         editor.setFileExtensions(LoadNetwork.NETWORK_FILE_EXTENSIONS);
 
         label = new Label(main, SWT.LEFT);
         label.setText(NeoLoaderPluginMessages.NetworkSiteImportWizard_DATA_TYPE);
         label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
         networkType = new Combo(main, SWT.DROP_DOWN | SWT.READ_ONLY);
         networkType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
         networkType.setItems(getNetworkFileType());
         networkType.addSelectionListener(new SelectionListener() {
 
             @Override
             public void widgetSelected(SelectionEvent e) {
                 netwFile = new Pair<NetworkFileType, Exception>(NetworkFileType.getEnumById(networkType.getText()), null);
                 setPageComplete(isValidPage());
             }
 
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
                 widgetSelected(e);
             }
         });
         labNetworkDescr = new Label(main, SWT.LEFT);
         GridData layoutData = new GridData(GridData.FILL_HORIZONTAL, SWT.CENTER, true, false, 3, 1);
         layoutData.minimumWidth = 150;
         setControl(main);
     }
 
     /**
      * get array of file types
      * 
      * @return
      */
     private String[] getNetworkFileType() {
         NetworkFileType[] types = NetworkFileType.values();
         String[] result = new String[types.length];
         for (NetworkFileType type : types) {
             result[type.ordinal()] = type.getId();
         }
         return result;
     }
 
     /**
      *update file description
      */
     protected void updateLabelFileDescr() {
         netwFile = LoaderUtils.getFileType(getFileName());
         NetworkFileType fileType = netwFile.getLeft();
         int ind = fileType == null ? -1 : fileType.ordinal();
         networkType.select(ind);
         setPageComplete(isValidPage());
     }
 
     /**
      *
      */
     protected void updateLabelNetwDescr() {
         String text = "";
         if (networkNode != null) {
             NetworkTypes type = NetworkTypes.getNodeType(networkNode, NeoServiceProvider.getProvider().getService());
             if (type != null) {
                 text = "Network type: " + type.getId();
             }
         }
         labNetworkDescr.setText(text);
     }
 
     /**
      * Sets file name
      * 
      * @param fileName file name
      */
     protected void setFileName(String fileName) {
         this.fileName = fileName;
         setPageComplete(isValidPage());
         // editor.store();
         NeighbourLoader.setDirectory(editor.getDefaulDirectory());
     }
 
     /**
      * Forms list of GIS nodes
      * 
      * @return array of GIS nodes
      */
     private String[] getGisItems() {
         Transaction tx = NeoUtils.beginTransaction();
         try {
             NeoService service = NeoServiceProvider.getProvider().getService();
             Node refNode = service.getReferenceNode();
             restrictedNames.clear();
             members = new HashMap<String, Node>();
             String header = GisTypes.NETWORK.getHeader();
             for (Relationship relationship : refNode.getRelationships(Direction.OUTGOING)) {
                 Node node = relationship.getEndNode();
                 if (node.hasProperty(INeoConstants.PROPERTY_TYPE_NAME) && node.hasProperty(INeoConstants.PROPERTY_NAME_NAME)
                         && node.getProperty(INeoConstants.PROPERTY_TYPE_NAME).toString().equalsIgnoreCase(NodeTypes.GIS.getId())                        ) {
                     String id = node.getProperty(INeoConstants.PROPERTY_NAME_NAME).toString();
                     if (header.equals(node.getProperty(INeoConstants.PROPERTY_GIS_TYPE_NAME, ""))){
                         members.put(id, node);
                     }else{
                         restrictedNames.add(id);
                     }
                 }
             }
 
             return members.keySet().toArray(new String[] {});
         } finally {
             tx.finish();
         }
     }
 
     /**
      * @return Returns the fileName.
      */
     public String getFileName() {
         return fileName;
     }
 
     /**
      * @return Returns the networkNode.
      */
     public Node getNetworkNode() {
         return networkNode;
     }
 
     /**
      * @return selected NetworkFileType
      */
     public NetworkFileType getFileType() {
         return netwFile.getLeft();
     }
 
     /**
      * @return Returns the networkName.
      */
     public String getNetworkName() {
         return networkName;
     }
     
 }
