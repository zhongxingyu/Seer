 package org.kevoree.library.javase.webserver.collaborationToolsBasics.client;
 
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.core.client.JavaScriptObject;
 import com.google.gwt.core.client.Scheduler;
 import com.google.gwt.core.client.Scheduler.RepeatingCommand;
 import com.google.gwt.core.client.Scheduler.ScheduledCommand;
 import com.google.gwt.dom.client.NativeEvent;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.KeyUpEvent;
 import com.google.gwt.event.dom.client.KeyUpHandler;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.user.client.Event;
 import com.google.gwt.user.client.Event.NativePreviewEvent;
 import com.google.gwt.user.client.Event.NativePreviewHandler;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.*;
 import com.smartgwt.client.widgets.menu.Menu;
 import com.smartgwt.client.widgets.tree.Tree;
 import com.smartgwt.client.widgets.tree.TreeGrid;
 import com.smartgwt.client.widgets.tree.TreeNode;
 import org.kevoree.library.javase.webserver.collaborationToolsBasics.shared.AbstractItem;
 import org.kevoree.library.javase.webserver.collaborationToolsBasics.shared.FileItem;
 import org.kevoree.library.javase.webserver.collaborationToolsBasics.shared.FolderItem;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.smartgwt.client.widgets.grid.events.CellContextClickEvent;
 import com.smartgwt.client.widgets.grid.events.CellContextClickHandler;
 import com.smartgwt.client.widgets.grid.events.CellDoubleClickEvent;
 import com.smartgwt.client.widgets.grid.events.CellDoubleClickHandler;
 import com.smartgwt.client.widgets.menu.MenuItem;
 import com.smartgwt.client.widgets.menu.events.MenuItemClickEvent;
 
 
 
 /**
  * Entry point classes define <code>onModuleLoad()</code>.
  */
 public class IHMcodeMirror implements EntryPoint,MirrorEditorCallback {
     private TextBox textBoxLoginNew, textBoxPasswordNew, textBoxNameRepositoryNew,
             textBoxURLRepositoryImport, textBoxLoginImport, textBoxPasswordImport;
     private HTML textAreaCodeShow;
     private TextArea codeMirror;
     private String login, password, nomRepository, urlRepository;
     private PopupPanel popupFormNew, popupFormOpen, popupUploadFile;
     private Button btnOpen, btnSave, btnCreateFile;
     private NativeEvent ne;
 
     private RootPanel buttonBar,editor,systemFileRoot;
     private ScrollPanel systemFile ;
     private Tree tree;
     private TreeGrid treeGrid;
     private TreeNode currentSelectedNode;
 
     private final StructureServiceAsync structureService = GWT
             .create(StructureService.class);
 
     private final RepositoryToolsServicesAsync repositoryToolsServices = GWT
             .create(RepositoryToolsServices.class);
 
 
     /**
      * This is the entry point method.
      */
     public void onModuleLoad() {
         // Listener CodeMirror
         //TODO
         // CodeMirrorEditorWrapper.addOnChangeHandler(this);
 
         // get Divs
         buttonBar = RootPanel.get("buttonBar");
         editor = RootPanel.get("editor");
         systemFileRoot = RootPanel.get("fileSystem");
 
        /* systemFile = new ScrollPanel();
         systemFile.setAlwaysShowScrollBars(true);
         systemFile.setStyleName("systemFileScrollPanel");*/
        treeGrid = new TreeGrid();
         systemFileRoot.add(treeGrid);
 
         // add editor's content
         Grid gridEditor = new Grid(2,2);
 
 
         // textArea codeMirror
         //TODO
         //codeMirror = new HTML("<form><textarea id=\"code\" name=\"code\"> </textarea></form>");
 
         // textArea Display codeMirror's stuff
         textAreaCodeShow = new HTML();
         textAreaCodeShow.setStyleName("textAreaCodeShow");
         codeMirror = new TextArea();
         codeMirror.setHeight("800px");
         codeMirror.setWidth("300px");
 
         gridEditor.setWidget(0,0,codeMirror);
         gridEditor.setWidget(0,1,textAreaCodeShow);
 
         codeMirror.addKeyUpHandler(new KeyUpHandler() {
             @Override
             public void onKeyUp(KeyUpEvent event) {
                 repositoryToolsServices.updateContentFileAndCommit(codeMirror.getText().getBytes(), login, new AsyncCallback<Boolean>() {
                     @Override
                     public void onFailure(Throwable throwable) {
                         //To change body of implemented methods use File | Settings | File Templates.
                     }
 
                     @Override
                     public void onSuccess(Boolean aBoolean) {
                         textAreaCodeShow.setHTML(codeMirror.getText());
                     }
 
                 });
             }
 
         });
 
         //TODO
 
 
         editor.add(gridEditor);
 
         // add buttonBar's Content
 
         // general Layout
         Grid grid = new Grid(2, 3);
         buttonBar.add(grid);
         buttonBar.setStyleName("buttonBarGrid");
 
         // Form open existing project
         popupFormOpen = new PopupPanel(true);
         popupFormOpen.setStyleName("popup");
         Grid gridFieldsOpen = new Grid(4, 2);
         Label lblURLRepository = new Label("HTTPS du repository :");
 
         Button btnNouveau = new Button("New");
         btnNouveau.addClickHandler(new ClickHandler() {
             public void onClick(ClickEvent event) {
                 popupFormNew.center();
             }
         });
 
         Button btnUploadFile = new Button("Import a file into repository");
         btnUploadFile.setWidth("125px");
         btnUploadFile.addClickHandler(new ClickHandler() {
             @Override
             public void onClick(ClickEvent clickEvent) {
                 popupUploadFile.center();
             }
         });
         grid.setWidget(0, 2, btnUploadFile);
 
         grid.setWidget(0, 0, btnNouveau);
         btnNouveau.setWidth("123px");
 
         btnSave = new Button("Save");
         btnSave.addClickHandler(new ClickHandler() {
             @Override
             public void onClick(ClickEvent clickEvent) {
                 pushContentEditorToRepo();
             }
         });
 
         btnOpen = new Button("Open");
         grid.setWidget(0, 1, btnOpen);
         btnOpen.setWidth("125px");
 
         btnOpen.addClickHandler(new ClickHandler() {
             public void onClick(ClickEvent event) {
                 popupFormOpen.center();
             }
         });
 
         btnCreateFile = new Button("Create new file");
         grid.setWidget(1,1,btnCreateFile);
         btnCreateFile.setWidth("125px");
 
 
         btnCreateFile.addClickHandler(new ClickHandler() {
             @Override
             public void onClick(ClickEvent clickEvent) {
                 repositoryToolsServices.createFileIntoLocalRepository(new FileItem("test" + System.currentTimeMillis() + ".txt"), new AsyncCallback<AbstractItem>() {
                     @Override
                     public void onFailure(Throwable throwable) {
                         //To change body of implemented methods use File | Settings | File Templates.
                     }
 
                     @Override
                     public void onSuccess(AbstractItem item) {
                         loadFileSystem(item);
                     }
                 });
             }
         });
 
         Button btnImport = new Button("Import");
         btnImport.addClickHandler(new ClickHandler() {
             public void onClick(ClickEvent event) {
                 popupFormOpen.hide();
                 btnSave.setEnabled(true);
                 login = textBoxLoginImport.getText();
                 password = textBoxPasswordImport.getText();
                 urlRepository = textBoxURLRepositoryImport.getText();
                 if(!login.isEmpty() && !password.isEmpty() && !urlRepository.isEmpty())
                     repositoryToolsServices.importRepository(login, password, urlRepository, new AsyncCallback<AbstractItem>() {
                         @Override
                         public void onFailure(Throwable throwable) {
                             RootPanel.get().add(new HTML(" FAIL at import repo " ));
                         }
 
                         @Override
                         public void onSuccess(AbstractItem abstractItem) {
                             textAreaCodeShow.setHTML("");
                             codeMirror.setText("");
                             loadFileSystem(abstractItem);
                             popupUploadFile = new UploadFileForm(abstractItem);
                         }
                     });
             }
         });
 
         grid.setWidget(1, 0, btnSave);
         btnSave.setWidth("125px");
         btnSave.setEnabled(false);
 
         popupFormOpen.setWidget(gridFieldsOpen);
 
         // Form New project
         popupFormNew = new PopupPanel(true);
         popupFormNew.setStyleName("popup");
         Grid gridFieldsNew = new Grid(4, 2);
 
         // component for form new
         Label lblLogin = new Label("Login :");
         gridFieldsNew.setWidget(0, 0, lblLogin);
         lblLogin.setHeight("25px");
 
         textBoxLoginNew = new TextBox();
         gridFieldsNew.setWidget(0, 1, textBoxLoginNew);
 
         Label lblPassword = new Label("Password :");
         gridFieldsNew.setWidget(1, 0, lblPassword);
 
         textBoxPasswordNew = new TextBox();
         gridFieldsNew.setWidget(1, 1, textBoxPasswordNew);
 
         Label lblNomDuRepository = new Label("Nom du repository :");
         gridFieldsNew.setWidget(2, 0, lblNomDuRepository);
 
         textBoxNameRepositoryNew = new TextBox();
         gridFieldsNew.setWidget(2, 1, textBoxNameRepositoryNew);
 
         // component for form Open
         Label lblLogin2 = new Label("Login :");
         lblLogin2.setHeight("25px");
         textBoxLoginImport = new TextBox();
         Label lblPassword2 = new Label("Password :");
         textBoxPasswordImport = new TextBox();
 
         textBoxURLRepositoryImport = new TextBox();
         gridFieldsOpen.setWidget(0, 0, lblLogin2);
         gridFieldsOpen.setWidget(0, 1, textBoxLoginImport);
         gridFieldsOpen.setWidget(1, 0, lblPassword2);
         gridFieldsOpen.setWidget(1, 1, textBoxPasswordImport);
         gridFieldsOpen.setWidget(2, 0, lblURLRepository);
         gridFieldsOpen.setWidget(2, 1, textBoxURLRepositoryImport);
         gridFieldsOpen.setWidget(3, 0, btnImport);
 
         Button btnCreateRepo = new Button("Create repository");
         btnCreateRepo.addClickHandler(new ClickHandler() {
             public void onClick(ClickEvent event) {
                 login = textBoxLoginNew.getText();
                 password = textBoxPasswordNew.getText();
                 nomRepository = textBoxNameRepositoryNew.getText();
                 if(!login.isEmpty() && !password.isEmpty() && !nomRepository.isEmpty())
                     repositoryToolsServices.initRepository(login,password,nomRepository,new AsyncCallback<AbstractItem>() {
                         @Override
                         public void onFailure(Throwable throwable) { }
 
 
 
                         @Override
                         public void onSuccess(AbstractItem abstractItem) {
                             textAreaCodeShow.setHTML("");
                             codeMirror.setText("");
                             loadFileSystem(abstractItem);
                             popupFormNew.hide();
                             btnSave.setEnabled(true);
                             popupUploadFile = new UploadFileForm(abstractItem);
                         }
                     });
             }
         });
 
 
         gridFieldsNew.setWidget(3, 0, btnCreateRepo);
         popupFormNew.setWidget(gridFieldsNew);
         popupFormOpen.setWidget(gridFieldsOpen);
 
         HandlerRegistration logHandler = Event.addNativePreviewHandler(new NativePreviewHandler() {
 
             @Override
             public void onPreviewNativeEvent(NativePreviewEvent event) {
                 ne = event.getNativeEvent();
 
                 if (ne.getType().equals("keydown")
                         && ne.getCtrlKey()
                         && (ne.getKeyCode() == 'n' || ne.getKeyCode() == 'N')) {
                     ne.preventDefault();
                     Scheduler.get().scheduleDeferred( new ScheduledCommand() {
                         @Override
                         public void execute() {
                             if (popupFormNew.isShowing()) {
                                 popupFormNew.hide();
                             } else {
                                 popupFormNew.center();
                             }
                         }
                     });
                 } else if (ne.getType().equals("keydown")
                         && ne.getCtrlKey()
                         && (ne.getKeyCode() == 'o' || ne.getKeyCode() == 'O')) {
                     ne.preventDefault();
                     Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                         @Override
                         public void execute() {
                             if (popupFormOpen.isShowing()) {
                                 popupFormOpen.hide();
                             } else {
                                 popupFormOpen.center();
                             }
                         }
                     });
                 } else if (ne.getType().equals("keydown")
                         && ne.getCtrlKey()
                         && (ne.getKeyCode() == 's' || ne.getKeyCode() == 'S')) {
                     ne.preventDefault();
 
                     Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                         @Override
                         public void execute() {
                             if (btnSave.isEnabled()) {
                                 // fonction to call for saving
                                 pushContentEditorToRepo();
                             }
                         }
                     });
                 }
             }
         });
 
         // Auto-Save
         int savePeriod = 5000000;
         Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {
             @Override
             public boolean execute() {
                 if (btnSave.isEnabled()) {
                     // fonction to call for saving
                     pushContentEditorToRepo();
                 }
                 return true;
             }
 
         }, savePeriod);
     }
 
     @Override
     public void invokeMirrorCallback(JavaScriptObject obj) {
         //TODO
         //textAreaCodeShow.setHTML(CodeMirrorEditorWrapper.getText());
         textAreaCodeShow.setHTML(codeMirror.getText());
         writeEditorContentToFile();
     }
 
     public void writeEditorContentToFile(){
         //TODO
         //  String contentEditor =  CodeMirrorEditorWrapper.getText();
         String contentEditor =  codeMirror.getText();
 
         repositoryToolsServices.updateContentFileAndCommit(contentEditor.getBytes(), login, new AsyncCallback<Boolean>() {
             @Override
             public void onFailure(Throwable throwable) {
                 //To change body of implemented methods use File | Settings | File Templates.
             }

             @Override
             public void onSuccess(Boolean aBoolean) {
             }
 
         });
     }
 
     public void pushContentEditorToRepo() {
         repositoryToolsServices.pushRepository(login, password,new
                 AsyncCallback<Boolean>(){
 
                     @Override public void onFailure(Throwable throwable) { }
 
                     @Override public void onSuccess(Boolean aBoolean) {  }
 
                 });
     }
 
     public void loadFileSystem(AbstractItem abstractItem) {
         // add FileSystem's content
         structureService.getArborescence(abstractItem, new AsyncCallback<AbstractItem>() {
 
             @Override
             public void onFailure(Throwable caught) {
                 RootPanel.get().add(new HTML("FAIL" + caught.getStackTrace().toString() + " message " + caught.getMessage()));
             }
 
             @Override
             public void onSuccess(AbstractItem result) {
                 RootPanel.get().add(new HTML("SUCCESS"));
                 tree = new Tree();
                 TreeNode root = new TreeNode(result.getName());
                 tree.setData(new TreeNode[] { root });
                 createGwtTree(result, root);
                 treeGrid.setData(tree);
                 treeGrid.setContextMenu(initContextMenu());
                 treeGrid.draw();
                 //don't allow right clicks on tree grid
                 treeGrid.addCellContextClickHandler(new CellContextClickHandler(){
                     public void onCellContextClick(CellContextClickEvent event) {
                         event.cancel();
                         currentSelectedNode = (TreeNode) event.getRecord();
                         treeGrid.getContextMenu().showContextMenu();
                     }
                 });
                 treeGrid.addCellDoubleClickHandler(new CellDoubleClickHandler() {
 
                     @Override
                     public void onCellDoubleClick(CellDoubleClickEvent event) {
                         event.cancel();
                         if(!event.getRecord().getAttributeAsBoolean("isFolder")){
                             AbstractItem item = (AbstractItem) event.getRecord().getAttributeAsObject("abstractItem");
                             Window.alert(item.getPath());
                         }
 
                     }
                 });
                /* tree = new Tree();
                 root = new TreeItem(result.getName());
 
                 tree.addSelectionHandler(new SelectionHandler<TreeItem>() {
 
                     @Override
                     public void onSelection(SelectionEvent<TreeItem> event) {
                         TreeItem item = event.getSelectedItem();
                         repositoryToolsServices.getFileContent(getItemPath(item), new AsyncCallback<String>() {
                             @Override
                             public void onFailure(Throwable throwable) {
                                 //To change body of implemented methods use File | Settings | File Templates.
                             }
 
                             @Override
                             public void onSuccess(String s) {
                                 // CodeMirrorEditorWrapper.setText(s);
                                 //TODO
                                 codeMirror.setText(s);
                                 textAreaCodeShow.setHTML(s);
                             }
                         });
                     }
                 });
                 createGwtTree(result, root);
                 tree.addItem(root.getChild(0));
                 systemFile.clear();
                 systemFile.add(tree);   */
 
             }
         });
     }
 
 
     public Menu initContextMenu(){
         Menu mainMenu = new Menu();
         MenuItem createFileMenu = new MenuItem("Create file");
         createFileMenu.addClickHandler(new com.smartgwt.client.widgets.menu.events.ClickHandler() {
 
             @Override
             public void onClick(MenuItemClickEvent event) {
                 AbstractItem item = (AbstractItem) currentSelectedNode.getAttributeAsObject("abstractItem");
                 if(currentSelectedNode.getAttributeAsBoolean("isFolder")){
                     //TODO
                     Window.alert("Le parent est : "+item.getPath());
                 }else{
                     //TODO
                     Window.alert("Le parent est : "+item.getParent().getPath());
                 }
             }
         });
 
         MenuItem createFolderMenu = new MenuItem("Create folder");
         createFolderMenu.addClickHandler(new com.smartgwt.client.widgets.menu.events.ClickHandler() {
 
             @Override
             public void onClick(MenuItemClickEvent event) {
                 AbstractItem item = (AbstractItem) currentSelectedNode.getAttributeAsObject("abstractItem");
                 if(currentSelectedNode.getAttributeAsBoolean("isFolder")){
                     //TODO
                     Window.alert("Le parent est : "+item.getPath());
                 }else{
                     //TODO
                     Window.alert("Le parent est : "+item.getParent().getPath());
                 }
             }
         });
 
         MenuItem refactorMenu = new MenuItem("Refactor");
         refactorMenu.addClickHandler(new com.smartgwt.client.widgets.menu.events.ClickHandler() {
 
             @Override
             public void onClick(MenuItemClickEvent event) {
                 AbstractItem item = (AbstractItem) currentSelectedNode.getAttributeAsObject("abstractItem");
                 //TODO
                 Window.alert("Le parent est : "+item.getPath());
             }
         });
 
         mainMenu.setItems(createFileMenu,createFolderMenu,refactorMenu);
         return mainMenu;
     }
 
 
     public void createGwtTree(AbstractItem item, TreeNode root) {
         for (int i = 0; i < ((FolderItem) item).getChilds().size(); i++) {
             if (((FolderItem) item).getChilds().get(i).getClass() == FolderItem.class) {
                 FolderItem itemFolder = (FolderItem) ((FolderItem) item).getChilds().get(i);
                 TreeNode folder = new TreeNode(itemFolder.getName());
                 folder.setAttribute("abstractItem", itemFolder);
                 folder.setAttribute("isFolder",true);
                 tree.add(folder, root);
                 if (itemFolder.getChilds().size() == 0){
                     folder.setIsFolder(true);
                 }
                 createGwtTree(itemFolder, folder);
             } else {
                 TreeNode file = new TreeNode(((FolderItem) item).getChilds()
                         .get(i).getName());
                 file.setAttribute("isFolder",false);
                 file.setAttribute("abstractItem", ((FolderItem) item).getChilds().get(i));
                 tree.add(file, root);
             }
         }
     }
 }
 
