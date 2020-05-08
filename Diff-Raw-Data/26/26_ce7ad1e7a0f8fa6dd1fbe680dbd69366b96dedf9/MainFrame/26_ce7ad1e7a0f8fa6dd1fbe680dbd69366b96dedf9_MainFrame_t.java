 package andoridmanifest_analyzer;
 
 import javax.swing.table.DefaultTableModel;
 import manifest.ManifestReader;
 import permissions.*;
 import de.javasoft.plaf.synthetica.SyntheticaBlackMoonLookAndFeel; 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileWriter;
 import java.io.InputStream;
 import java.util.ArrayList;
 import javax.swing.ImageIcon;
 import javax.swing.ListSelectionModel;
 import javax.swing.UIManager;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 import javax.swing.tree.DefaultMutableTreeNode;
 import manifest.Feature;
 import manifest.Instrumentation;
 import manifest.application.Activity;
 import manifest.application.ActivityAlias;
 import manifest.application.Application;
 import manifest.application.ApplicationComponent;
 import manifest.application.Provider;
 import manifest.application.Receiver;
 import manifest.application.Service;
 import manifest.application.intent.Action;
 import manifest.application.intent.Category;
 import manifest.application.intent.Data;
 import manifest.application.intent.Intent;
 import manifest.application.metaData.MetaData;
 import manifest.application.permission.GrantUriPermission;
 import manifest.application.permission.PathPermission;
 
 /**
  *
  * @author Administrator
  */
 public class MainFrame extends javax.swing.JFrame {
 
     private Object[] columnNamesLibraries = {"No.", "Name", "Required"};
     private Object[] columnNamesPermissions = {"No.", "Name", "Description", "Icon", "Label", "Permission Group", "Protection Level"};
     private Object[] columnNamesPermissionTree = {"No.", "Name", "Icon", "Label"};
     private Object[] columnNamesPermissionGroup = {"No.", "Name", "Description", "Icon", "Label"};
     private Object[] columnNamesUsesPermissions = {"No.", "Permission Name", "Type", "Description", "Score", "Remarks"};
     private Object[] columnNamesApplications = {"No.", "Application Name", "Type", "Enabled", "Intents", "Action", "Category", "Data", "Meta Data"};
     private PermissionLoader pload = new PermissionLoader();
     private ManifestReader reader;
 
     /**
      * Creates new form MainFrame
      */
     public MainFrame() {
         try {
             UIManager.setLookAndFeel(new SyntheticaBlackMoonLookAndFeel());
         } catch (Exception e) {
            e.printStackTrace();
             MainFrame.txtLogs.setText(MainFrame.txtLogs.getText() + e.toString()+"\n");
         }
        initComponents();
        MainFrame.txtLogs.setText("Starting....\n");
         this.setIconImage(new ImageIcon("./res/icon.png").getImage());
         try {
             pload.loadPermission("groupPermission.txt", 1);
             pload.loadPermission("permission.txt", 2);
         } catch (Exception ex) {
             MainFrame.txtLogs.setText(MainFrame.txtLogs.getText() + ex.toString()+"\n");
             //ex.printStackTrace();
         }
         tblUsesPermissions.setModel(new CustomTableModel(null, columnNamesUsesPermissions));
         tblApps.setModel(new CustomTableModel(null, columnNamesApplications));
         tblLibraries.setModel(new CustomTableModel(null, columnNamesLibraries));
         tblApps.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         tblPermissions.setModel(new CustomTableModel(null, columnNamesPermissions));
         tblPermissionGroup.setModel(new CustomTableModel(null, columnNamesPermissionGroup));
         tblPermissionTree.setModel(new CustomTableModel(null, columnNamesPermissionTree));
         
         treeComponents.addTreeSelectionListener(new TreeSelectionListener(){
             public void valueChanged(TreeSelectionEvent e){
                 DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();
                 System.out.println("You selected " + node);
                 if(tblApps.getRowCount() > 0 && tblApps.getSelectedRow() >= 0 && tblApps.getValueAt(tblApps.getSelectedRow(), 1).toString() != null ){
                     String text = "<b>You selected: </b> Index " + tblApps.getValueAt(tblApps.getSelectedRow(), 0).toString() +" - "+ node+"<br><hr>";
                     int selectedIndex = Integer.parseInt(tblApps.getValueAt(tblApps.getSelectedRow(), 0).toString())-1;
                     
                     Application app = reader.getManifest().getApplication();
                     ApplicationComponent appComp = app.getApplicationComponents().get(selectedIndex);
                     ArrayList<Intent> intents = null;
                     ArrayList<MetaData> metaData = null;
                     ArrayList<Action> actions = null;
                     ArrayList<Category> categories = null;
                     ArrayList<Data> data = null;
                     ArrayList<GrantUriPermission> grants = null;                    
                     ArrayList<PathPermission> paths = null;                        
                     
                     if (node.isRoot()) {
                         text += "<b>AllowTaskReparenting: </b>" + app.isAllowTaskReparenting() + "<br>";
                         text += "<b>AllowBackup: </b>" + app.isAllowBackup()+ "<br>";
                         text += "<b>BackupAgent: </b>" + app.getBackupAgent()+ "<br>";
                         text += "<b>Debuggable: </b>" + app.isDebuggable() + "<br>";
                         text += "<b>Description: </b>" + app.getDescription()+ "<br>";
                         text += "<b>Enabled: </b>" + app.isEnabled() + "<br>";
                         text += "<b>HasCode: </b>" + app.isHasCode() + "<br>";
                         text += "<b>HardwareAccelerated: </b>" + app.isHardwareAccelerated()+ "<br>";
                         text += "<b>Icon: </b>" + app.getIcon() + "<br>";
                         text += "<b>KillAfterRestore: </b>" + app.isKillAfterRestore()+ "<br>";
                         text += "<b>LargeHeap: </b>" + app.isLargeHeap()+ "<br>";
                         text += "<b>Label: </b>" + app.getLabel()+ "<br>";
                         text += "<b>Logo: </b>" + app.getLogo()+ "<br>";
                         text += "<b>ManageSpaceActivity: </b>" + app.getManageSpaceActivity()+ "<br>";
                         text += "<b>Name: </b>" + app.getName()+ "<br>";
                         text += "<b>Permission: </b>" + app.getPermission()+ "<br>";
                         text += "<b>Persistent: </b>" + app.isPersistent()+ "<br>";
                         text += "<b>Process: </b>" + app.getProcess()+ "<br>";
                         text += "<b>RestoreAnyVersion: </b>" + app.isRestoreAnyVersion()+ "<br>";
                         text += "<b>RequiredAccountType: </b>" + app.getRequiredAccountType()+ "<br>";
                         text += "<b>RestrictedAccountType: </b>" + app.getRestrictedAccountType()+ "<br>";
                         text += "<b>SupportsRtl: </b>" + app.isSupportsRtl()+ "<br>";
                         text += "<b>TaskAffinity: </b>" + app.getTaskAffinity()+ "<br>";
                         text += "<b>TestOnly: </b>" + app.isTestOnly()+ "<br>";
                         text += "<b>Theme: </b>" + app.getTheme()+ "<br>";
                         text += "<b>UIOptions: </b>" + app.getUiOptions()+ "<br>";
                         text += "<b>VmSafeMode: </b>" + app.isVmSafeMode()+ "<br>";
                     }else{        
                         DefaultMutableTreeNode node2 = (DefaultMutableTreeNode)node.getParent();
                         
                         if (appComp instanceof Activity) {
                             Activity activity = (Activity) appComp;
                             intents = activity.getIntents();
                             metaData = activity.getMetaDatas(); 
                         }else if (appComp instanceof ActivityAlias) {
                                 ActivityAlias activityAlias = (ActivityAlias) appComp;
                                 intents = activityAlias.getIntents();
                                 metaData = activityAlias.getMetaDatas();
                         } else if (appComp instanceof Service) {
                                 Service service = (Service) appComp;
                                 metaData = service.getMetaDatas();
                         }else if (appComp instanceof Receiver) {
                                 Receiver receiver = (Receiver) appComp;
                                 intents = receiver.getIntents();
                                 metaData = receiver.getMetaDatas();
                         }else if (appComp instanceof Provider) {
                                 Provider provider = (Provider) appComp;
                                 grants = provider.getGrantUriPermissions();
                                 metaData = provider.getMetaDatas();
                                 paths = provider.getPathPermissions();
                         }
                         
                         if (node2.isRoot()) {
                             if (appComp instanceof Activity) {
                                 Activity activity = (Activity) appComp;
                                 text += "<b>Component: </b>Activity<br>";
                                 text += "<b>AllowTaskReparenting: </b>" + activity.isAllowTaskReparenting() + "<br>";
                                 text += "<b>alwaysRetainTaskState: </b>" + activity.isAlwaysRetainTaskState()+ "<br>";
                                 text += "<b>ClearTaskOnLaunch: </b>" + activity.isClearTaskOnLaunch()+ "<br>";
                                 text += "<b>ConfigChanges: </b>" + activity.getConfigChanges()+ "<br>";
                                 text += "<b>Enabled: </b>" + activity.isEnabled() + "<br>";
                                 text += "<b>ExcludeFromRecents: </b>" + activity.isExcludeFromRecents()+ "<br>";
                                 text += "<b>Exported: </b>" + activity.isExported() + "<br>";
                                 text += "<b>FinishOnTaskLaunch: </b>" + activity.isFinishOnTaskLaunch()+ "<br>";
                                 text += "<b>HardwareAccelerated: </b>" + activity.isHardwareAccerlerated()+ "<br>";
                                 text += "<b>Icon: </b>" + activity.getIcon() + "<br>";
                                 text += "<b>Label: </b>" + activity.getLabel() + "<br>";
                                 text += "<b>LaunchMode: </b>" + activity.getLaunchmode()+ "<br>";
                                 text += "<b>Multiprocess: </b>" + activity.isMultiprocess()+ "<br>";
                                 text += "<b>Name: </b>" + activity.getName() + "<br>";
                                 text += "<b>NoHistory: </b>" + activity.isNoHistory() + "<br>";
                                 text += "<b>ParentActivityName: </b>" + activity.getParentActivityName()+ "<br>";
                                 text += "<b>Permisssion: </b>" + activity.getPermission() + "<br>";
                                 text += "<b>Process: </b>" + activity.getProcess() + "<br>";
                                 text += "<b>ScreenOrientation: </b>" + activity.getScreenOrientation() + "<br>";
                                 text += "<b>StateNotNeeded: </b>" + activity.isStateNotNeeded()+ "<br>";
                                 text += "<b>TaskAffinity: </b>" + activity.getTaskAffinity() + "<br>";
                                 text += "<b>Theme: </b>" + activity.getTheme() + "<br>";
                                 text += "<b>UIOptions: </b>" + activity.getUiOptions()+ "<br>";
                                 text += "<b>WindowSoftInputMode: </b>" + activity.getWindowSoftInputMode()+ "<br>";
                             } else if (appComp instanceof ActivityAlias) {
                                 ActivityAlias activityAlias = (ActivityAlias) appComp;
                                 text += "<b>Component: </b>ActivityAlias<br>";
                                 text += "<b>Enabled: </b>" + activityAlias.isEnabled() + "<br>";
                                 text += "<b>Exported: </b>" + activityAlias.isExported()+ "<br>";
                                 text += "<b>Icon: </b>" + activityAlias.getIcon() + "<br>";
                                 text += "<b>Label: </b>" + activityAlias.getLabel() + "<br>";
                                 text += "<b>Name: </b>" + activityAlias.getName() + "<br>";
                                 text += "<b>Permission: </b>" + activityAlias.getPermission() + "<br>";
                                 text += "<b>TargetActivity: </b>" + activityAlias.getTargetActivity()+ "<br>";
                             } else if (appComp instanceof Service) {
                                 Service service = (Service) appComp;
                                 text += "<b>Component: </b>Service<br>";
                                 text += "<b>Enabled: </b>" + service.isEnabled() + "<br>";
                                 text += "<b>Exported: </b>" + service.isExported()+ "<br>";
                                 text += "<b>Icon: </b>" + service.getIcon() + "<br>";
                                 text += "<b>IsolatedProcess: </b>" + service.isIsolatedProcess()+ "<br>";
                                 text += "<b>Label: </b>" + service.getLabel() + "<br>";
                                 text += "<b>Name: </b>" + service.getName() + "<br>";
                                 text += "<b>Permission: </b>" + service.getPermission() + "<br>";
                                 text += "<b>Process: </b>" + service.getProcess() + "<br>";
                             } else if (appComp instanceof Receiver) {
                                 Receiver receiver = (Receiver) appComp;
                                 text += "<b>Component: </b>Receiver<br>";
                                 text += "<b>Enabled: </b>" + receiver.isEnabled() + "<br>";
                                 text += "<b>Exported: </b>" + receiver.isExported()+ "<br>";
                                 text += "<b>Icon: </b>" + receiver.getIcon() + "<br>";
                                 text += "<b>Label: </b>" + receiver.getLabel() + "<br>";
                                 text += "<b>Name: </b>" + receiver.getName() + "<br>";
                                 text += "<b>Permission: </b>" + receiver.getPermission() + "<br>";
                                 text += "<b>Process: </b>" + receiver.getProcess() + "<br>";
                             } else if (appComp instanceof Provider) {
                                 Provider provider = (Provider) appComp;
                                 text += "<b>Component: </b>Provider<br>";
                                 text += "<b>Authorities: </b>" + provider.getAuthorities()+ "<br>";
                                 text += "<b>Enabled: </b>" + provider.isEnabled() + "<br>";
                                 text += "<b>Exported: </b>" + provider.isExported()+ "<br>";
                                 text += "<b>GrantUriPermission: </b>" + provider.isGrantUriPermission()+ "<br>";
                                 text += "<b>Icon: </b>" + provider.getIcon() + "<br>";
                                 text += "<b>InitOrder: </b>" + provider.getInitOrder()+ "<br>";
                                 text += "<b>Label: </b>" + provider.getLabel() + "<br>";
                                 text += "<b>MultiProcess: </b>" + provider.isMultiprocess()+ "<br>";
                                 text += "<b>Name: </b>" + provider.getName() + "<br>";
                                 text += "<b>Permission: </b>" + provider.getPermission() + "<br>";
                                 text += "<b>Process: </b>" + provider.getProcess() + "<br>";
                                 text += "<b>ReadPermission: </b>" + provider.getReadPermission()+ "<br>";
                                 text += "<b>Syncable: </b>" + provider.isSyncable()+ "<br>";
                                 text += "<b>WritePermission: </b>" + provider.getWritePermission() + "<br>";
                             }
                         }else{
                             if(node.toString().equalsIgnoreCase("Intent-Filter")){
                                 text += "<b>Icon: </b>" + intents.get( node2.getIndex(node)).getIcon() + "<br>";
                                 text += "<b>Label: </b>" + intents.get( node2.getIndex(node)).getLabel() + "<br>";
                                 text += "<b>Priority: </b>" + intents.get( node2.getIndex(node)).getPriority() + "<br>";                                
                             }else if(node.toString().equalsIgnoreCase("grant-uri-permission")){
                                 text += "<b>Path: </b>" + grants.get( node2.getIndex(node)).getPath()+ "<br>";
                                 text += "<b>Path Pattern: </b>" + grants.get( node2.getIndex(node)).getPathPattern() + "<br>";
                                 text += "<b>Path Prefix: </b>" + grants.get( node2.getIndex(node)).getPathPrefix()+ "<br>";                                
                             }else if(node.toString().equalsIgnoreCase("meta-data")){
                                 int intentCount = 0;
                                 for(int k = 0; k < node.getSiblingCount(); k++){
                                     if(node2.getChildAt(k).toString().equalsIgnoreCase("Intent-Filter") ||node2.getChildAt(k).toString().equalsIgnoreCase("grant-uri-permission") ){
                                         intentCount++;
                                     }
                                 }
                                 text += "<b>Name: </b>" + metaData.get( node2.getIndex(node)-intentCount).getName() + "<br>";
                                 text += "<b>Resource: </b>" + metaData.get( node2.getIndex(node)-intentCount).getResource() + "<br>";
                                 text += "<b>Value: </b>" + metaData.get( node2.getIndex(node)-intentCount).getValue()+ "<br>";                                
                             }else if(node.toString().equalsIgnoreCase("path-permission")){
                                 int pathCount = 0;
                                 for(int k = 0; k < node.getSiblingCount(); k++){
                                     if(node2.getChildAt(k).toString().equalsIgnoreCase("meta-data") ||node2.getChildAt(k).toString().equalsIgnoreCase("grant-uri-permission") ){
                                         pathCount++;
                                     }
                                 }
                                 text += "<b>Path: </b>" + paths.get( node2.getIndex(node)-pathCount).getPath()+ "<br>";
                                 text += "<b>Path Pattern: </b>" + paths.get( node2.getIndex(node)-pathCount).getPathPattern() + "<br>";
                                 text += "<b>Path Prefix: </b>" + paths.get( node2.getIndex(node)-pathCount).getPathPrefix()+ "<br>";      
                                 text += "<b>Permission: </b>" + paths.get( node2.getIndex(node)-pathCount).getPermission()+ "<br>";
                                 text += "<b>Read Permission: </b>" + paths.get( node2.getIndex(node)-pathCount).getReadPermission()+ "<br>";
                                 text += "<b>Write Permission: </b>" + paths.get( node2.getIndex(node)-pathCount).getWritePermission()+ "<br>";                                      
                             }else if(node2.toString().equalsIgnoreCase("Intent-filter")){
                                 if(node.toString().equalsIgnoreCase("action")){
                                     actions = intents.get(node2.getParent().getIndex(node2)).getActions();
                                     text += "<b>Name: </b>" + actions.get(node2.getIndex(node)).getName()  + "<br>";                          
                                 }else if(node.toString().equalsIgnoreCase("category")){                                    
                                     int actionCount = 0;
                                     for(int k = 0; k < node.getSiblingCount(); k++){
                                         if(node2.getChildAt(k).toString().equalsIgnoreCase("action")){
                                             actionCount++;
                                         }
                                     }
                                     categories = intents.get(node2.getParent().getIndex(node2)).getCategories();
                                     text += "<b>Name: </b>" + categories.get(node2.getIndex(node)-actionCount).getName()+ "<br>";                                        
                                 }else if(node.toString().equalsIgnoreCase("data")){
                                     int acCount = 0;
                                     for(int k = 0; k < node.getSiblingCount(); k++){
                                         if(node2.getChildAt(k).toString().equalsIgnoreCase("action") || node2.getChildAt(k).toString().equalsIgnoreCase("category")){
                                             acCount++;
                                         }
                                     }
                                     data = intents.get(node2.getParent().getIndex(node2)).getData();
                                     text += "<b>Host: </b>" + data.get(node2.getIndex(node)-acCount).getHost()+ "<br>";        
                                     text += "<b>Mime Type: </b>" + data.get(node2.getIndex(node)-acCount).getMimeType()+ "<br>";     
                                     text += "<b>Path: </b>" + data.get(node2.getIndex(node)-acCount).getPath()+ "<br>";     
                                     text += "<b>Path Pattern: </b>" + data.get(node2.getIndex(node)-acCount).getPathPattern()+ "<br>";     
                                     text += "<b>Path Prefix: </b>" + data.get(node2.getIndex(node)-acCount).getPathPrefix()+ "<br>";     
                                     text += "<b>Port: </b>" + data.get(node2.getIndex(node)-acCount).getPort()+ "<br>";               
                                     text += "<b>Scheme: </b>" + data.get(node2.getIndex(node)-acCount).getScheme()+ "<br>";                                     
                                 }                              
                             }
                         }
                     }
                     txtComponent.setText(text);  
                     txtComponent.setCaretPosition(0);
                 }
             }            
         });
         
         tblApps.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
             public void valueChanged(ListSelectionEvent event) {
                 System.out.println(tblApps.getValueAt(tblApps.getSelectedRow(), 2).toString());
                 txtComponent.setText("");
                 //treeComponents.setModel(new TreeModel());
                 if(!tblApps.getValueAt(tblApps.getSelectedRow(), 2).toString().equalsIgnoreCase("provider")){
                     int selectedIndex = Integer.parseInt(tblApps.getValueAt(tblApps.getSelectedRow(), 0).toString()) - 1;
                     ApplicationComponent appComp = reader.getManifest().getApplication().getApplicationComponents().get(selectedIndex);
                     DefaultMutableTreeNode treeNodeRoot = new DefaultMutableTreeNode("Application");
                     DefaultMutableTreeNode treeNodeAppComponent = new DefaultMutableTreeNode(tblApps.getValueAt(tblApps.getSelectedRow(), 1).toString());
                                        
                     if (appComp instanceof Activity) {
                         Activity activity = (Activity) appComp;
                         ArrayList<Intent> intents = activity.getIntents();
                         ArrayList<MetaData> metaData = activity.getMetaDatas();
                         loadData(treeNodeAppComponent, intents, metaData);
                     } else if (appComp instanceof ActivityAlias) {
                         ActivityAlias activityAlias = (ActivityAlias) appComp;
                         ArrayList<Intent> intents = activityAlias.getIntents();
                         ArrayList<MetaData> metaData = activityAlias.getMetaDatas();
                         loadData(treeNodeAppComponent, intents, metaData);
                     } else if (appComp instanceof Service) {
                         Service service = (Service) appComp;
                         ArrayList<Intent> intents = service.getIntents();
                         ArrayList<MetaData> metaData = service.getMetaDatas();
                         loadData(treeNodeAppComponent, intents, metaData);
                     } else if (appComp instanceof Receiver) {
                         Receiver receiver = (Receiver) appComp;
                         ArrayList<Intent> intents = receiver.getIntents();
                         ArrayList<MetaData> metaData = receiver.getMetaDatas();
                         loadData(treeNodeAppComponent, intents, metaData);
                     }
                     treeNodeRoot.add(treeNodeAppComponent);
                     treeComponents.setModel(new javax.swing.tree.DefaultTreeModel(treeNodeRoot));
 
                 }else{
                     int selectedIndex = Integer.parseInt(tblApps.getValueAt(tblApps.getSelectedRow(), 0).toString()) - 1;
                     ApplicationComponent appComp = reader.getManifest().getApplication().getApplicationComponents().get(selectedIndex);
                     
                     DefaultMutableTreeNode treeNodeRoot = new DefaultMutableTreeNode("Application");
                     DefaultMutableTreeNode treeNodeAppComponent = new DefaultMutableTreeNode(tblApps.getValueAt(tblApps.getSelectedRow(), 1).toString());
                     
                     Provider provider = (Provider) appComp;
                     
                     ArrayList<GrantUriPermission> grantUris = provider.getGrantUriPermissions();
                     ArrayList<MetaData> metaDatas = provider.getMetaDatas();
                     ArrayList<PathPermission> pathPermissions = provider.getPathPermissions();
                     for (int i = 0; i < grantUris.size(); i++) {
                         DefaultMutableTreeNode treeUri = new javax.swing.tree.DefaultMutableTreeNode("Grant-Uri-Permission");
                         treeNodeAppComponent.add(treeUri);
                     }
                     for (int i = 0; i < metaDatas.size(); i++) {
                         DefaultMutableTreeNode treeMeta = new javax.swing.tree.DefaultMutableTreeNode("Meta-data");
                         treeNodeAppComponent.add(treeMeta);
                     }
                     for (int i = 0; i < pathPermissions.size(); i++) {
                         DefaultMutableTreeNode treePath = new javax.swing.tree.DefaultMutableTreeNode("Path-Permission");
                         treeNodeAppComponent.add(treePath);
                     }
                     treeNodeRoot.add(treeNodeAppComponent);
                     treeComponents.setModel(new javax.swing.tree.DefaultTreeModel(treeNodeRoot));
                 }
             }
         });
 
         new DropFile(System.out, this.getContentPane(), /*dragBorder,*/ new DropFile.Listener() {
             public void filesDropped(java.io.File[] files) {
                 reader = new ManifestReader();
                 for (int i = 0; i < files.length; i++) {
                     Object[][] data = null;
                     Object[][] dataApp = null;
                     Object[][] dataLib = null;
                     
                     Object[][] dataPermission = null;
                     Object[][] dataPermissionGroup = null;
                     Object[][] dataPermissionTree = null;
                     try {
                         MainFrame.txtLogs.setText("File Dropped: "+ files[i].getCanonicalPath() +"\n");
                        
                         if (files[i].getCanonicalPath().endsWith("apk")) {
                             ExtractAPK unZip = new ExtractAPK();
                             unZip.unZipIt(MainFrame.txtLogs, files[i].getCanonicalPath(), ".\\" + files[i].getCanonicalPath().substring(files[i].getCanonicalPath().lastIndexOf("\\")));
                             try {
                                 InputStream is = new FileInputStream(".\\" + files[i].getCanonicalPath().substring(files[i].getCanonicalPath().lastIndexOf("\\")) + "\\AndroidManifest.xml");
                                 byte[] xml = new byte[is.available()];
                                 int br = is.read(xml);
                                 //Tree tr = TrunkFactory.newTree();
                                 String xmlText = unZip.decompressXML(xml);
                                 File f = new File(".\\" + files[i].getCanonicalPath().substring(files[i].getCanonicalPath().lastIndexOf("\\")) + "\\AndroidManifest.xml");
                                 FileWriter fw = new FileWriter(f.getAbsoluteFile());
                                 BufferedWriter bw = new BufferedWriter(fw);
                                 bw.write(xmlText);
                                 bw.close();
                                 //prt("XML\n"+tr.list());
                             } catch (Exception ex) {
                                 ex.printStackTrace();
                             }
 
                             //System.out.println("Reading: .\\"+ files[i].getCanonicalPath().substring(files[i].getCanonicalPath().lastIndexOf("\\")) + "\\AndroidManifest.xml");
                             reader.readManifest(".\\" + files[i].getCanonicalPath().substring(files[i].getCanonicalPath().lastIndexOf("\\")) + "\\AndroidManifest.xml");
                             MainFrame.txtLogs.setText(MainFrame.txtLogs.getText() + "Reading: .\\"+ files[i].getCanonicalPath().substring(files[i].getCanonicalPath().lastIndexOf("\\")) + "\\AndroidManifest.xml"+"\n");
                         } else {
                             reader.readManifest(files[i].getCanonicalPath());
                             MainFrame.txtLogs.setText(MainFrame.txtLogs.getText() + "Reading: .\\"+ files[i].getCanonicalPath()+"\n");
                         }
                         reader.calculateScore(PermissionLoader.arrPermissions);
                         System.out.println("Score: " + reader.getManifest().getScore());
                         MainFrame.txtLogs.setText(MainFrame.txtLogs.getText() + "Score: " + reader.getManifest().getScore()+"\n");
                         lblPackageName2.setText(reader.getManifest().getPackageName());
                         lblSDK2.setText("Min: "+reader.getManifest().getSdkMinVersion()
                                 + " Max: "+reader.getManifest().getSdkMaxVersion()
                                 + " Target: "+reader.getManifest().getSdkTargetVersion());
                         lblAppLabel2.setText(reader.getManifest().getApplication().getLabel());
                         lblNumPermissions2.setText("" + reader.getManifest().getPermissions().size());
                         lblNumActivities2.setText("" + reader.getManifest().getApplication().getApplicationComponents().size());
                         if (reader.getManifest().getScore() / 150 < 100) {
                             progressPermissionsScoreMatrix.setValue((int) ((reader.getManifest().getScore() / 150.0) * 100));
                         } else {
                             progressPermissionsScoreMatrix.setValue(100);
                         }
                         
                         dataLib = new Object[reader.getManifest().getPermissions().size()][columnNamesLibraries.length];
                         for (int k = 0; k < reader.getManifest().getApplication().getLibraries().size(); k++) {
                             dataLib[k][0] = k + 1;
                             dataLib[k][1] = reader.getManifest().getApplication().getLibraries().get(k).getName();
                             dataLib[k][2] = reader.getManifest().getApplication().getLibraries().get(k).isRequired();
                         }
                         data = new Object[reader.getManifest().getPermissions().size()][columnNamesUsesPermissions.length];
                         for (int k = 0; k < reader.getManifest().getPermissions().size(); k++) {
                             data[k][0] = k + 1;
                             data[k][1] = reader.getManifest().getPermissions().get(k);
                             data[k][2] = "Permission Used";
                             data[k][3] = getDescription(pload, reader.getManifest().getPermissions().get(k));
                             data[k][4] = String.format("%02d", getScore(pload, reader.getManifest().getPermissions().get(k)));
                             data[k][5] = getRemarks(pload, reader.getManifest().getPermissions().get(k));
                         }
                         /*************************/
                         dataPermission = new Object[reader.getManifest().getPermission().size()][columnNamesPermissions.length];
                         
                         for (int k = 0; k < reader.getManifest().getPermission().size(); k++) {
                             dataPermission[k][0] = k + 1;
                             dataPermission[k][1] = reader.getManifest().getPermission().get(k).getName();
                             dataPermission[k][2] = reader.getManifest().getPermission().get(k).getDescription();
                             dataPermission[k][3] = reader.getManifest().getPermission().get(k).getIcon();
                             dataPermission[k][4] = reader.getManifest().getPermission().get(k).getLabel();
                             dataPermission[k][5] = reader.getManifest().getPermission().get(k).getPermissionGroup();
                             dataPermission[k][6] = reader.getManifest().getPermission().get(k).getProtectionLevel();
                         }
                         dataPermissionGroup = new Object[reader.getManifest().getPermissionGroup().size()][columnNamesPermissionGroup.length];
                         // private Object[] columnNamesPermissionGroup = {"No.", "Name", "Description", "Icon", "Label"};
                         
                         for (int k = 0; k < reader.getManifest().getPermissionGroup().size(); k++) {
                             dataPermissionGroup[k][0] = k + 1;
                             dataPermissionGroup[k][1] = reader.getManifest().getPermissionGroup().get(k).getName();
                             dataPermissionGroup[k][2] = reader.getManifest().getPermissionGroup().get(k).getDescription();
                             dataPermissionGroup[k][3] = reader.getManifest().getPermissionGroup().get(k).getIcon();
                             dataPermissionGroup[k][4] = reader.getManifest().getPermissionGroup().get(k).getLabel();
                         }
                         // private Object[] columnNamesPermissionTree = {"No.", "Name", "Icon", "Label"};
                         
                         dataPermissionTree = new Object[reader.getManifest().getPermissionTree().size()][columnNamesPermissionTree.length];
                         
                         for (int k = 0; k < reader.getManifest().getPermissionTree().size(); k++) {
                             dataPermissionTree[k][0] = k + 1;
                             dataPermissionTree[k][1] = reader.getManifest().getPermissionTree().get(k).getName();
                             dataPermissionTree[k][2] = reader.getManifest().getPermissionTree().get(k).getIcon();
                             dataPermissionTree[k][3] = reader.getManifest().getPermissionTree().get(k).getLabel();
                         }
                         /*************************/
                         dataApp = new Object[reader.getManifest().getApplication().getApplicationComponents().size()][columnNamesApplications.length];
                         for (int k = 0; k < reader.getManifest().getApplication().getApplicationComponents().size(); k++) {
                            //{"No.", "Application Name", "Type", "Enabled", "Priority", "Action", "Category", "Data", "Meta Data"};
                             
                             dataApp[k][0] = k + 1;
                             dataApp[k][1] = reader.getManifest().getApplication().getApplicationComponents().get(k).getName();
                             
                             dataApp[k][3] = reader.getManifest().getApplication().getApplicationComponents().get(k).isEnabled();
                             dataApp[k][4] = "0";
                             dataApp[k][5] = "0";
                             dataApp[k][6] = "0";
                             dataApp[k][7] = "0";
                             dataApp[k][8] = "0";
                                     
                             ApplicationComponent appComp = reader.getManifest().getApplication().getApplicationComponents().get(k);
                             if(reader.getManifest().getApplication().getApplicationComponents().get(k) instanceof Activity){
                                dataApp[k][2] = "Activity";
                                
                                 dataApp[k][4] = ((Activity)appComp).getIntents().size();// requires reading of intent filter
                                 if(((Activity)appComp).getIntents().size() > 0){
                                     dataApp[k][5] = ((Activity)appComp).getIntents().get(0).getActions().size();//reader.getManifest().getActivities().get(k).getAction();
                                     dataApp[k][6] = ((Activity)appComp).getIntents().get(0).getCategories().size();//reader.getManifest().getActivities().get(k).getCategory();
                                     dataApp[k][7] = ((Activity)appComp).getIntents().get(0).getData().size();//reader.getManifest().getActivities().get(k).getData();
                                 }
                                 dataApp[k][8] = ((Activity)appComp).getMetaDatas().size();//reader.getManifest().getActivities().get(k).getMetaData();
                             }
                             else if(reader.getManifest().getApplication().getApplicationComponents().get(k) instanceof ActivityAlias){
                                dataApp[k][2] = "Activity-Alias";
                                dataApp[k][4] = ((ActivityAlias)appComp).getIntents().size();// requires reading of intent filter
                                 if(((ActivityAlias)appComp).getIntents().size() > 0){
                                     dataApp[k][5] = ((ActivityAlias)appComp).getIntents().get(0).getActions().size();//reader.getManifest().getActivities().get(k).getAction();
                                     dataApp[k][6] = ((ActivityAlias)appComp).getIntents().get(0).getCategories().size();//reader.getManifest().getActivities().get(k).getCategory();
                                     dataApp[k][7] = ((ActivityAlias)appComp).getIntents().get(0).getData().size();//reader.getManifest().getActivities().get(k).getData();
                                 }
                                 dataApp[k][8] = ((ActivityAlias)appComp).getMetaDatas().size();//reader.getManifest().getActivities().get(k).getMetaData();
                             }
                             else if(reader.getManifest().getApplication().getApplicationComponents().get(k) instanceof Service){
                                dataApp[k][2] = "Service";
                                dataApp[k][4] = ((Service)appComp).getIntents().size();// requires reading of intent filter
                                if(((Service)appComp).getIntents().size() > 0){
                                     dataApp[k][5] = ((Service)appComp).getIntents().get(0).getActions().size();//reader.getManifest().getActivities().get(k).getAction();
                                     dataApp[k][6] = ((Service)appComp).getIntents().get(0).getCategories().size();//reader.getManifest().getActivities().get(k).getCategory();
                                     dataApp[k][7] = ((Service)appComp).getIntents().get(0).getData().size();//reader.getManifest().getActivities().get(k).getData();
                                }
                                dataApp[k][8] = ((Service)appComp).getMetaDatas().size();//reader.getManifest().getActivities().get(k).getMetaData();
                             }
                             else if(reader.getManifest().getApplication().getApplicationComponents().get(k) instanceof Receiver){
                                dataApp[k][2] = "Receiver";
                                dataApp[k][4] = ((Receiver)appComp).getIntents().size();// requires reading of intent filter
                                if(((Receiver)appComp).getIntents().size() > 0){
                                     dataApp[k][5] = ((Receiver)appComp).getIntents().get(0).getActions().size();//reader.getManifest().getActivities().get(k).getAction();
                                     dataApp[k][6] = ((Receiver)appComp).getIntents().get(0).getCategories().size();//reader.getManifest().getActivities().get(k).getCategory();
                                     dataApp[k][7] = ((Receiver)appComp).getIntents().get(0).getData().size();//reader.getManifest().getActivities().get(k).getData();
                                }
                                dataApp[k][8] = ((Receiver)appComp).getMetaDatas().size();//reader.getManifest().getActivities().get(k).getMetaData();
                             }
                             else if(reader.getManifest().getApplication().getApplicationComponents().get(k) instanceof Provider){
                                dataApp[k][2] = "Provider";
                                dataApp[k][4] = "0";
                                dataApp[k][5] = "0";
                                dataApp[k][6] = "0";
                                dataApp[k][7] = "0";
                               
                                dataApp[k][8] = ((Provider)appComp).getMetaDatas().size();//reader.getManifest().getActivities().get(k).getMetaData();
                             }
                         }
                         
                         loadOthers(reader);
                     } 
                     catch (java.io.IOException e) {
                         //e.printStackTrace();
                         MainFrame.txtLogs.setText(MainFrame.txtLogs.getText() + e.toString()+"\n");
                     } finally {
                         tblUsesPermissions.setModel(new CustomTableModel(data, columnNamesUsesPermissions));
                         tblApps.setModel(new CustomTableModel(dataApp, columnNamesApplications));
                         tblLibraries.setModel(new CustomTableModel(dataLib, columnNamesLibraries));
 
                         tblPermissions.setModel(new CustomTableModel(dataPermission, columnNamesPermissions));
                         tblPermissionGroup.setModel(new CustomTableModel(dataPermissionGroup, columnNamesPermissionGroup));
                         tblPermissionTree.setModel(new CustomTableModel(dataPermissionTree, columnNamesPermissionTree));
                     }
                 }
             }  
         }); 
     }
     
     private void loadOthers(ManifestReader reader)
     {
         String text = "<b>Instrumentation</b><br><hr>";
         ArrayList<Instrumentation> instrumentations = reader.getManifest().getInstrumentation();
         for(int  i = 0; i < instrumentations.size(); i++){
             text += "<b>Name: </b>" + instrumentations.get(i).getName()+ "<br>";
             text += "<b>Functional Test: </b>" + instrumentations.get(i).isFunctionalTest() + "<br>";
             text += "<b>Handle Profiling: </b>" + instrumentations.get(i).isHandleProfiling()+ "<br>";
             text += "<b>Icon: </b>" + instrumentations.get(i).getIcon()+ "<br>";
             text += "<b>Label: </b>" + instrumentations.get(i).getLabel()+ "<br>";
             text += "<b>Target Package: </b>" + instrumentations.get(i).getTargetPackage()+ "<br><hr>";
         }
         
         text += "<b>Uses Features</b><br><hr>";
         ArrayList<Feature> features = reader.getManifest().getFeature();
         for(int  i = 0; i < features.size(); i++){
             text += "<b>Name: </b>" + features.get(i).getName()+ "<br>";
             text += "<b>Required: </b>" + features.get(i).isRequired()+ "<br>";
             text += "<b>glEsVersion: </b>" + features.get(i).getGlEsVersion()+ "<br>";
         }
         txtOthers.setText(text);
     }
     
     private void loadData(DefaultMutableTreeNode treeNodeRoot, ArrayList<Intent> intents, ArrayList<MetaData> metaData)
     {
         DefaultMutableTreeNode treeNodeIntent;
         DefaultMutableTreeNode treeNodeAction;
         DefaultMutableTreeNode treeNodeCategory;
         DefaultMutableTreeNode treeNodedata;
         DefaultMutableTreeNode treeNodeMeta;
         
         for (int i = 0; i < intents.size(); i++) {
             treeNodeIntent = new DefaultMutableTreeNode("Intent-filter");
             treeNodeRoot.add(treeNodeIntent);
 
             ArrayList<Action> actionList = intents.get(i).getActions();
             for (int j = 0; j < actionList.size(); j++) {
                 treeNodeAction = new DefaultMutableTreeNode("action");
                 treeNodeIntent.add(treeNodeAction);
             }
 
             ArrayList<Category> catList = intents.get(i).getCategories();
             for (int j = 0; j < catList.size(); j++) {
                 treeNodeAction = new DefaultMutableTreeNode("category");
                 treeNodeIntent.add(treeNodeAction);
             }
 
             ArrayList<Data> dataList = intents.get(i).getData();
             for (int j = 0; j < dataList.size(); j++) {
                 treeNodeAction = new DefaultMutableTreeNode("data");
                 treeNodeIntent.add(treeNodeAction);
             }
         }
         for (int i = 0; i < metaData.size(); i++) {
             treeNodeMeta = new DefaultMutableTreeNode("Meta-data");
             treeNodeRoot.add(treeNodeMeta);
         }
     }
 
     private int getScore(PermissionLoader pload, String name) {
         int score = 0;
         for (int i = 0; i < pload.getArrPermissions().size(); i++) {
             if (name.toUpperCase().trim().endsWith(pload.getArrPermissions().get(i).getName().toUpperCase().trim())) {
                 score = pload.getArrPermissions().get(i).getScore();
             }
         }
         return score;
     }
 
     private String getRemarks(PermissionLoader pload, String name) {
         String remarks = null;
         for (int i = 0; i < pload.getArrPermissions().size(); i++) {
             if (name.toUpperCase().trim().endsWith(pload.getArrPermissions().get(i).getName().toUpperCase().trim())) {
                 remarks = pload.getArrPermissions().get(i).getRemarks();
             }
         }
         return remarks;
     }
 
     private String getDescription(PermissionLoader pload, String name) {
         String description = null;
         for (int i = 0; i < pload.getArrPermissions().size(); i++) {
             if (name.toUpperCase().trim().endsWith(pload.getArrPermissions().get(i).getName().toUpperCase().trim())) {
                 description = pload.getArrPermissions().get(i).getDescription();
             }
         }
         return description;
     }
 
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         pnlAPK = new javax.swing.JPanel();
         lblPackageName = new javax.swing.JLabel();
         lblPackageName2 = new javax.swing.JLabel();
         lblSDK = new javax.swing.JLabel();
         lblSDK2 = new javax.swing.JLabel();
         lblAppLabel = new javax.swing.JLabel();
         lblAppLabel2 = new javax.swing.JLabel();
         lblNumPermissions = new javax.swing.JLabel();
         lblNumPermissions2 = new javax.swing.JLabel();
         lblNumActivities = new javax.swing.JLabel();
         lblNumActivities2 = new javax.swing.JLabel();
         tabDetails = new javax.swing.JTabbedPane();
         pnlPermissions = new javax.swing.JPanel();
         lblScore = new javax.swing.JLabel();
         progressPermissionsScoreMatrix = new javax.swing.JProgressBar();
         jScrollPane1 = new javax.swing.JScrollPane();
         tblUsesPermissions = new javax.swing.JTable();
         pnlPermission = new javax.swing.JPanel();
         jScrollPane6 = new javax.swing.JScrollPane();
         tblPermissions = new javax.swing.JTable();
         pnlPermissionTree = new javax.swing.JPanel();
         jScrollPane7 = new javax.swing.JScrollPane();
         tblPermissionTree = new javax.swing.JTable();
         pnlPermissionGroup = new javax.swing.JPanel();
         jScrollPane8 = new javax.swing.JScrollPane();
         tblPermissionGroup = new javax.swing.JTable();
         pnlApplications = new javax.swing.JPanel();
         split1 = new javax.swing.JSplitPane();
         tblApplications = new javax.swing.JScrollPane();
         tblApps = new javax.swing.JTable();
         split2 = new javax.swing.JSplitPane();
         jScrollPane2 = new javax.swing.JScrollPane();
         treeComponents = new javax.swing.JTree();
         jScrollPane3 = new javax.swing.JScrollPane();
         txtComponent = new javax.swing.JTextPane();
         pnlLibrary = new javax.swing.JPanel();
         jScrollPane4 = new javax.swing.JScrollPane();
         tblLibraries = new javax.swing.JTable();
         pnlOthers = new javax.swing.JPanel();
         jScrollPane9 = new javax.swing.JScrollPane();
         txtOthers = new javax.swing.JTextPane();
         pnlError = new javax.swing.JPanel();
         jScrollPane5 = new javax.swing.JScrollPane();
         txtLogs = new javax.swing.JTextPane();
         btnClear = new javax.swing.JButton();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
         setTitle("Andorid Manifest Analyzer");
 
         pnlAPK.setBorder(javax.swing.BorderFactory.createTitledBorder("APK Details"));
 
         lblPackageName.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
         lblPackageName.setText("Package Name:");
 
         lblPackageName2.setText("-");
 
         lblSDK.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
         lblSDK.setText("SDK Version:");
 
         lblSDK2.setText("-");
 
         lblAppLabel.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
         lblAppLabel.setText("App Label:");
 
         lblAppLabel2.setText("-");
 
         lblNumPermissions.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
         lblNumPermissions.setText("No. of Permisssions:");
 
         lblNumPermissions2.setText("-");
 
         lblNumActivities.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
         lblNumActivities.setText("No. of Activities:");
 
         lblNumActivities2.setText("-");
 
         javax.swing.GroupLayout pnlAPKLayout = new javax.swing.GroupLayout(pnlAPK);
         pnlAPK.setLayout(pnlAPKLayout);
         pnlAPKLayout.setHorizontalGroup(
             pnlAPKLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlAPKLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(pnlAPKLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(pnlAPKLayout.createSequentialGroup()
                         .addGroup(pnlAPKLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(lblPackageName)
                             .addComponent(lblSDK)
                             .addComponent(lblAppLabel))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addGroup(pnlAPKLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                             .addComponent(lblSDK2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 657, Short.MAX_VALUE)
                             .addComponent(lblAppLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .addComponent(lblPackageName2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlAPKLayout.createSequentialGroup()
                         .addGroup(pnlAPKLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(lblNumActivities)
                             .addComponent(lblNumPermissions))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addGroup(pnlAPKLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                             .addComponent(lblNumActivities2, javax.swing.GroupLayout.DEFAULT_SIZE, 657, Short.MAX_VALUE)
                             .addComponent(lblNumPermissions2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                 .addContainerGap())
         );
         pnlAPKLayout.setVerticalGroup(
             pnlAPKLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlAPKLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(pnlAPKLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(lblPackageName)
                     .addComponent(lblPackageName2, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(pnlAPKLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                     .addComponent(lblSDK, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(lblSDK2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(pnlAPKLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(lblAppLabel)
                     .addComponent(lblAppLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(pnlAPKLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(lblNumPermissions)
                     .addComponent(lblNumPermissions2, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(pnlAPKLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                     .addComponent(lblNumActivities, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(lblNumActivities2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addGap(24, 24, 24))
         );
 
         lblScore.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
         lblScore.setText("Score Matrix:");
 
         progressPermissionsScoreMatrix.setValue(50);
 
         tblUsesPermissions.setAutoCreateRowSorter(true);
         tblUsesPermissions.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null}
             },
             new String [] {
                 "Title 1", "Title 2", "Title 3", "Title 4"
             }
         ));
         jScrollPane1.setViewportView(tblUsesPermissions);
 
         javax.swing.GroupLayout pnlPermissionsLayout = new javax.swing.GroupLayout(pnlPermissions);
         pnlPermissions.setLayout(pnlPermissionsLayout);
         pnlPermissionsLayout.setHorizontalGroup(
             pnlPermissionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlPermissionsLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(pnlPermissionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 818, Short.MAX_VALUE)
                     .addGroup(pnlPermissionsLayout.createSequentialGroup()
                         .addComponent(lblScore)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(progressPermissionsScoreMatrix, javax.swing.GroupLayout.PREFERRED_SIZE, 303, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(0, 0, Short.MAX_VALUE)))
                 .addContainerGap())
         );
         pnlPermissionsLayout.setVerticalGroup(
             pnlPermissionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlPermissionsLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(pnlPermissionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(progressPermissionsScoreMatrix, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(lblScore))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 473, Short.MAX_VALUE)
                 .addContainerGap())
         );
 
         tabDetails.addTab("Uses Permissions", pnlPermissions);
 
         tblPermissions.setAutoCreateRowSorter(true);
         tblPermissions.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null}
             },
             new String [] {
                 "Title 1", "Title 2", "Title 3", "Title 4"
             }
         ));
         jScrollPane6.setViewportView(tblPermissions);
 
         javax.swing.GroupLayout pnlPermissionLayout = new javax.swing.GroupLayout(pnlPermission);
         pnlPermission.setLayout(pnlPermissionLayout);
         pnlPermissionLayout.setHorizontalGroup(
             pnlPermissionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlPermissionLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 818, Short.MAX_VALUE)
                 .addContainerGap())
         );
         pnlPermissionLayout.setVerticalGroup(
             pnlPermissionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlPermissionLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 493, Short.MAX_VALUE)
                 .addContainerGap())
         );
 
         tabDetails.addTab("Permission", pnlPermission);
 
         tblPermissionTree.setAutoCreateRowSorter(true);
         tblPermissionTree.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null}
             },
             new String [] {
                 "Title 1", "Title 2", "Title 3", "Title 4"
             }
         ));
         jScrollPane7.setViewportView(tblPermissionTree);
 
         javax.swing.GroupLayout pnlPermissionTreeLayout = new javax.swing.GroupLayout(pnlPermissionTree);
         pnlPermissionTree.setLayout(pnlPermissionTreeLayout);
         pnlPermissionTreeLayout.setHorizontalGroup(
             pnlPermissionTreeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlPermissionTreeLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 818, Short.MAX_VALUE)
                 .addContainerGap())
         );
         pnlPermissionTreeLayout.setVerticalGroup(
             pnlPermissionTreeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlPermissionTreeLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 493, Short.MAX_VALUE)
                 .addContainerGap())
         );
 
         tabDetails.addTab("Permission Tree", pnlPermissionTree);
 
         tblPermissionGroup.setAutoCreateRowSorter(true);
         tblPermissionGroup.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null}
             },
             new String [] {
                 "Title 1", "Title 2", "Title 3", "Title 4"
             }
         ));
         jScrollPane8.setViewportView(tblPermissionGroup);
 
         javax.swing.GroupLayout pnlPermissionGroupLayout = new javax.swing.GroupLayout(pnlPermissionGroup);
         pnlPermissionGroup.setLayout(pnlPermissionGroupLayout);
         pnlPermissionGroupLayout.setHorizontalGroup(
             pnlPermissionGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlPermissionGroupLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jScrollPane8, javax.swing.GroupLayout.DEFAULT_SIZE, 818, Short.MAX_VALUE)
                 .addContainerGap())
         );
         pnlPermissionGroupLayout.setVerticalGroup(
             pnlPermissionGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlPermissionGroupLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jScrollPane8, javax.swing.GroupLayout.DEFAULT_SIZE, 493, Short.MAX_VALUE)
                 .addContainerGap())
         );
 
         tabDetails.addTab("Permission Group", pnlPermissionGroup);
 
         split1.setDividerLocation(150);
         split1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
         split1.setPreferredSize(new java.awt.Dimension(454, 733));
 
         tblApps.setAutoCreateRowSorter(true);
         tblApps.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null}
             },
             new String [] {
                 "Title 1", "Title 2", "Title 3", "Title 4"
             }
         ));
         tblApplications.setViewportView(tblApps);
 
         split1.setTopComponent(tblApplications);
 
         split2.setDividerLocation(250);
 
         javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("Application");
         javax.swing.tree.DefaultMutableTreeNode treeNode2 = new javax.swing.tree.DefaultMutableTreeNode("Application Component");
         javax.swing.tree.DefaultMutableTreeNode treeNode3 = new javax.swing.tree.DefaultMutableTreeNode("Intent-filter");
         javax.swing.tree.DefaultMutableTreeNode treeNode4 = new javax.swing.tree.DefaultMutableTreeNode("action");
         treeNode3.add(treeNode4);
         treeNode4 = new javax.swing.tree.DefaultMutableTreeNode("category");
         treeNode3.add(treeNode4);
         treeNode4 = new javax.swing.tree.DefaultMutableTreeNode("data");
         treeNode3.add(treeNode4);
         treeNode2.add(treeNode3);
         treeNode1.add(treeNode2);
         treeNode2 = new javax.swing.tree.DefaultMutableTreeNode("Meta-data");
         treeNode1.add(treeNode2);
         treeComponents.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
         jScrollPane2.setViewportView(treeComponents);
 
         split2.setLeftComponent(jScrollPane2);
 
         txtComponent.setEditable(false);
         txtComponent.setContentType("text/html"); // NOI18N
         jScrollPane3.setViewportView(txtComponent);
 
         split2.setRightComponent(jScrollPane3);
 
         split1.setRightComponent(split2);
 
         javax.swing.GroupLayout pnlApplicationsLayout = new javax.swing.GroupLayout(pnlApplications);
         pnlApplications.setLayout(pnlApplicationsLayout);
         pnlApplicationsLayout.setHorizontalGroup(
             pnlApplicationsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlApplicationsLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(split1, javax.swing.GroupLayout.DEFAULT_SIZE, 818, Short.MAX_VALUE)
                 .addContainerGap())
         );
         pnlApplicationsLayout.setVerticalGroup(
             pnlApplicationsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlApplicationsLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(split1, javax.swing.GroupLayout.DEFAULT_SIZE, 493, Short.MAX_VALUE)
                 .addContainerGap())
         );
 
         tabDetails.addTab("Applications", pnlApplications);
 
         tblLibraries.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null}
             },
             new String [] {
                 "Title 1", "Title 2", "Title 3", "Title 4"
             }
         ));
         jScrollPane4.setViewportView(tblLibraries);
 
         javax.swing.GroupLayout pnlLibraryLayout = new javax.swing.GroupLayout(pnlLibrary);
         pnlLibrary.setLayout(pnlLibraryLayout);
         pnlLibraryLayout.setHorizontalGroup(
             pnlLibraryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlLibraryLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 818, Short.MAX_VALUE)
                 .addContainerGap())
         );
         pnlLibraryLayout.setVerticalGroup(
             pnlLibraryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlLibraryLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 493, Short.MAX_VALUE)
                 .addContainerGap())
         );
 
         tabDetails.addTab("Libraries", pnlLibrary);
 
         txtOthers.setEditable(false);
         txtOthers.setContentType("text/html"); // NOI18N
         jScrollPane9.setViewportView(txtOthers);
 
         javax.swing.GroupLayout pnlOthersLayout = new javax.swing.GroupLayout(pnlOthers);
         pnlOthers.setLayout(pnlOthersLayout);
         pnlOthersLayout.setHorizontalGroup(
             pnlOthersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlOthersLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jScrollPane9, javax.swing.GroupLayout.DEFAULT_SIZE, 818, Short.MAX_VALUE)
                 .addContainerGap())
         );
         pnlOthersLayout.setVerticalGroup(
             pnlOthersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlOthersLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jScrollPane9, javax.swing.GroupLayout.DEFAULT_SIZE, 493, Short.MAX_VALUE)
                 .addContainerGap())
         );
 
         tabDetails.addTab("Others", pnlOthers);
 
         jScrollPane5.setViewportView(txtLogs);
 
         btnClear.setLabel("Clear Logs");
         btnClear.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnClearActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout pnlErrorLayout = new javax.swing.GroupLayout(pnlError);
         pnlError.setLayout(pnlErrorLayout);
         pnlErrorLayout.setHorizontalGroup(
             pnlErrorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlErrorLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(pnlErrorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jScrollPane5)
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlErrorLayout.createSequentialGroup()
                         .addGap(0, 735, Short.MAX_VALUE)
                         .addComponent(btnClear)))
                 .addContainerGap())
         );
         pnlErrorLayout.setVerticalGroup(
             pnlErrorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlErrorLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 443, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(btnClear)
                 .addContainerGap())
         );
 
         tabDetails.addTab("Logs", pnlError);
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(tabDetails)
                     .addComponent(pnlAPK, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(pnlAPK, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(tabDetails)
                 .addContainerGap())
         );
 
         pack();
         setLocationRelativeTo(null);
     }// </editor-fold>//GEN-END:initComponents
 
     private void btnClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearActionPerformed
         // TODO add your handling code here:
         MainFrame.txtLogs.setText("");
     }//GEN-LAST:event_btnClearActionPerformed
 
     /**
      * @param args the command line arguments
      */
     public static void main(String args[]) {
         /* Set the Nimbus look and feel */
         //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
         /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
          * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
          */
         
         //</editor-fold>
 
         /* Create and display the form */
         java.awt.EventQueue.invokeLater(new Runnable() {
             public void run() {
                 new MainFrame().setVisible(true);
             }
         });
     }
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton btnClear;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JScrollPane jScrollPane2;
     private javax.swing.JScrollPane jScrollPane3;
     private javax.swing.JScrollPane jScrollPane4;
     private javax.swing.JScrollPane jScrollPane5;
     private javax.swing.JScrollPane jScrollPane6;
     private javax.swing.JScrollPane jScrollPane7;
     private javax.swing.JScrollPane jScrollPane8;
     private javax.swing.JScrollPane jScrollPane9;
     private javax.swing.JLabel lblAppLabel;
     private javax.swing.JLabel lblAppLabel2;
     private javax.swing.JLabel lblNumActivities;
     private javax.swing.JLabel lblNumActivities2;
     private javax.swing.JLabel lblNumPermissions;
     private javax.swing.JLabel lblNumPermissions2;
     private javax.swing.JLabel lblPackageName;
     private javax.swing.JLabel lblPackageName2;
     private javax.swing.JLabel lblSDK;
     private javax.swing.JLabel lblSDK2;
     private javax.swing.JLabel lblScore;
     private javax.swing.JPanel pnlAPK;
     private javax.swing.JPanel pnlApplications;
     private javax.swing.JPanel pnlError;
     private javax.swing.JPanel pnlLibrary;
     private javax.swing.JPanel pnlOthers;
     private javax.swing.JPanel pnlPermission;
     private javax.swing.JPanel pnlPermissionGroup;
     private javax.swing.JPanel pnlPermissionTree;
     private javax.swing.JPanel pnlPermissions;
     private javax.swing.JProgressBar progressPermissionsScoreMatrix;
     private javax.swing.JSplitPane split1;
     private javax.swing.JSplitPane split2;
     private javax.swing.JTabbedPane tabDetails;
     private javax.swing.JScrollPane tblApplications;
     private javax.swing.JTable tblApps;
     private javax.swing.JTable tblLibraries;
     private javax.swing.JTable tblPermissionGroup;
     private javax.swing.JTable tblPermissionTree;
     private javax.swing.JTable tblPermissions;
     private javax.swing.JTable tblUsesPermissions;
     private javax.swing.JTree treeComponents;
     private javax.swing.JTextPane txtComponent;
     public static javax.swing.JTextPane txtLogs;
     private javax.swing.JTextPane txtOthers;
     // End of variables declaration//GEN-END:variables
 }
 
 class CustomTableModel extends DefaultTableModel {
 
     Object rowData[][];
 
     public CustomTableModel(Object rowData[][], Object columnNames[]) {
         super(rowData, columnNames);
         this.rowData = rowData;
     }
 
     public boolean isCellEditable(int row, int col) {
         return false;
     }
 
     public Object getValueAt(int row, int col) {
         return rowData[row][col];
     }
 }
