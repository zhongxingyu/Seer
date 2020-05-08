 /**
  *
  */
 package org.iplantc.de.client.views.dialogs;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.iplantc.core.client.widgets.Hyperlink;
 import org.iplantc.core.jsonutil.JsonUtil;
 import org.iplantc.core.uicommons.client.ErrorHandler;
 import org.iplantc.core.uidiskresource.client.models.DiskResource;
 import org.iplantc.core.uidiskresource.client.models.Folder;
 import org.iplantc.core.uidiskresource.client.models.Permissions;
 import org.iplantc.de.client.I18N;
 import org.iplantc.de.client.models.Collaborator;
 import org.iplantc.de.client.models.DataSharing;
 import org.iplantc.de.client.models.Sharing;
 import org.iplantc.de.client.services.DiskResourceServiceFacade;
 import org.iplantc.de.client.utils.CollaboratorsUtil;
 import org.iplantc.de.client.views.panels.SharePanel;
 
 import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
 import com.extjs.gxt.ui.client.Style.LayoutRegion;
 import com.extjs.gxt.ui.client.Style.SelectionMode;
 import com.extjs.gxt.ui.client.core.FastMap;
 import com.extjs.gxt.ui.client.data.ModelData;
 import com.extjs.gxt.ui.client.dnd.GridDragSource;
 import com.extjs.gxt.ui.client.dnd.GridDropTarget;
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.DNDEvent;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.store.ListStore;
 import com.extjs.gxt.ui.client.widget.ContentPanel;
 import com.extjs.gxt.ui.client.widget.Dialog;
 import com.extjs.gxt.ui.client.widget.MessageBox;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.button.ToolButton;
 import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
 import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
 import com.extjs.gxt.ui.client.widget.grid.ColumnData;
 import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
 import com.extjs.gxt.ui.client.widget.grid.Grid;
 import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
 import com.extjs.gxt.ui.client.widget.grid.GridView;
 import com.extjs.gxt.ui.client.widget.grid.GridViewConfig;
 import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
 import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
 import com.extjs.gxt.ui.client.widget.layout.FitLayout;
 import com.extjs.gxt.ui.client.widget.tips.ToolTipConfig;
 import com.google.gwt.json.client.JSONArray;
 import com.google.gwt.json.client.JSONBoolean;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.json.client.JSONString;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.dom.client.Element;
 
 /**
  * @author sriram
  * 
  */
 public class DataSharingDialog extends Dialog {
 
     private final List<DiskResource> resources;
 
     private BorderLayout layout;
 
     private Grid<DiskResource> diskResourceGrid;
 
     private Grid<Collaborator> collaboratorsGrid;
 
     private FastMap<Sharing> sharingList;
     private FastMap<List<Sharing>> dataSharingMap;
 
     private SharePanel sharePanel;
 
     public DataSharingDialog(List<DiskResource> resources) {
         this.resources = resources;
         setSize(800, 400);
         setHeading(I18N.DISPLAY.manageSharing());
         setButtons();
     }
 
     private void setButtons() {
         setButtons(Dialog.OK);
         setButtonAlign(HorizontalAlignment.RIGHT);
         Button okButton = getOkButton();
         okButton.setText(org.iplantc.de.client.I18N.DISPLAY.done());
         okButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
 
             @Override
             public void componentSelected(ButtonEvent ce) {
                 JSONObject requestBody = buildSharingJson();
                 if (requestBody != null) {
                     callSharingService(requestBody);
                 }
                 JSONObject unshareRequestBody = buildUnSharingJson();
                 if (unshareRequestBody != null) {
                     callUnshareService(unshareRequestBody);
                 }
                 if (requestBody != null || unshareRequestBody != null) {
                     MessageBox.info(I18N.DISPLAY.share() + "/ " + I18N.DISPLAY.unshare(),
                             I18N.DISPLAY.sharingCompleteMsg(), null);
                 }
 
             }
         });
     }
 
     public void initView() {
         initLayout();
         setHideOnButtonClick(true);
         setModal(true);
         setResizable(false);
         layout();
     }
 
     private void initLayout() {
         layout = new BorderLayout();
         setLayout(layout);
         buildWest();
         buildCenter();
     }
 
     private Button getOkButton() {
         return getButtonById(Dialog.OK);
     }
 
     private void buildCenter() {
         ContentPanel center = new ContentPanel();
         center.setLayout(new FitLayout());
         center.setHeading("2. " + I18N.DISPLAY.selectFilesFolders());
         ToolButton helpBtn = new ToolButton("x-tool-help");
         helpBtn.setToolTip(buildHelpToolTip(I18N.HELP.shareDiskResourceHelp()));
         center.getHeader().addTool(helpBtn);
         center.add(buildDiskResourceGrid());
         BorderLayoutData data = new BorderLayoutData(LayoutRegion.CENTER);
         data.setSplit(true);
         data.setCollapsible(true);
         add(center, data);
     }
 
     private void buildEast() {
         BorderLayoutData data = new BorderLayoutData(LayoutRegion.EAST, 320, 200, 350);
         sharePanel = new SharePanel(resources);
         sharePanel.setHeading("3. " + I18N.DISPLAY.changePermissions());
         ToolButton helpBtn = new ToolButton("x-tool-help");
         helpBtn.setToolTip(buildHelpToolTip(I18N.HELP.sharingPermissionsHelp()));
         sharePanel.getHeader().addTool(helpBtn);
         getUserPermissionsInfo();
         data.setSplit(true);
         add(sharePanel, data);
         layout();
     }
 
     private void buildWest() {
         ContentPanel west = new ContentPanel();
         west.setLayout(new FitLayout());
         west.setHeading("1. " + I18N.DISPLAY.selectCollabs());
         ToolButton helpBtn = new ToolButton("x-tool-help");
         helpBtn.setToolTip(buildHelpToolTip(I18N.HELP.shareCollaboratorsHelp()));
         west.getHeader().addTool(helpBtn);
         west.add(buildCollaboratorsGrid());
         BorderLayoutData data = new BorderLayoutData(LayoutRegion.WEST, 200, 250, 350);
         data.setSplit(true);
         data.setCollapsible(true);
         add(west, data);
 
     }
 
     private ToolTipConfig buildHelpToolTip(String helpText) {
         ToolTipConfig ttc = getToolTipConfig();
         ttc.setTitle(I18N.DISPLAY.help());
         ttc.setText(helpText);
         return ttc;
     }
 
     private ToolTipConfig getToolTipConfig() {
         ToolTipConfig config = new ToolTipConfig();
         config.setMouseOffset(new int[] {0, 0});
         config.setAnchor("left");
         config.setCloseable(true);
         return config;
     }
 
     private Widget buildCollaboratorsGrid() {
         CheckBoxSelectionModel<Collaborator> sm = new CheckBoxSelectionModel<Collaborator>();
         collaboratorsGrid = new Grid<Collaborator>(new ListStore<Collaborator>(),
                 buildCollaboratorColumnModel(sm));
         sm.setSelectionMode(SelectionMode.MULTI);
         collaboratorsGrid.setSelectionModel(sm);
 
         collaboratorsGrid.addPlugin(sm);
         new GridDragSource(collaboratorsGrid) {
             @Override
             protected void onDragStart(DNDEvent e) {
                 List<Collaborator> list = collaboratorsGrid.getSelectionModel().getSelectedItems();
                 if (list == null || list.size() == 0) {
                     e.setCancelled(true);
                 } else {
                     e.setData(list);
                     e.setCancelled(false);
                 }
             }
 
             @Override
             protected void onDragDrop(DNDEvent e) {
                 // do nothing intentionally
             }
         };
         GridView view = collaboratorsGrid.getView();
         view.setViewConfig(buildGridViewConfig());
         view.setForceFit(true);
         loadCollaborators();
         return collaboratorsGrid;
     }
 
     private GridViewConfig buildGridViewConfig() {
         GridViewConfig config = new GridViewConfig() {
 
             @Override
             public String getRowStyle(ModelData model, int rowIndex, ListStore<ModelData> ds) {
                 return "iplantc-select-grid";
             }
 
         };
 
         return config;
     }
 
     private void loadCollaborators() {
         collaboratorsGrid.mask(I18N.DISPLAY.loadingMask());
         CollaboratorsUtil.getCollaborators(new AsyncCallback<Void>() {
 
             @Override
             public void onFailure(Throwable caught) {
                 collaboratorsGrid.unmask();
             }
 
             @Override
             public void onSuccess(Void result) {
                 collaboratorsGrid.unmask();
                 collaboratorsGrid.getStore().add(CollaboratorsUtil.getCurrentCollaborators());
                 // share panel is built after collaborators are ready
                 buildEast();
             }
 
         });
     }
 
     private ColumnModel buildCollaboratorColumnModel(CheckBoxSelectionModel<Collaborator> sm) {
         List<ColumnConfig> columns = new ArrayList<ColumnConfig>();
 
         ColumnConfig name = new ColumnConfig(Collaborator.NAME, I18N.DISPLAY.name(), 150);
         columns.addAll(Arrays.asList(sm.getColumn(), name));
 
         return new ColumnModel(columns);
     }
 
     private void getUserPermissionsInfo() {
         DiskResourceServiceFacade facade = new DiskResourceServiceFacade();
         sharePanel.mask(I18N.DISPLAY.loadingMask());
         facade.getPermissions(buildPermissionsRequestBody(), new LoadPermissionsCallback());
     }
 
     private void loadPermissions(String path, JSONArray user_arr) {
         for (int i = 0; i < user_arr.size(); i++) {
             JSONObject obj = user_arr.get(i).isObject();
             JSONObject perm = JsonUtil.getObject(obj, "permissions");
             Collaborator collaborator = CollaboratorsUtil.findCollaboratorByUserName(JsonUtil.getString(
                     obj, "user"));
 
             String userName = collaborator.getUserName();
             Sharing s = sharingList.get(userName);
             Sharing dataSharing = new DataSharing(collaborator, new Permissions(perm), path);
             if (s == null) {
                 s = new Sharing(collaborator);
                 sharingList.put(userName, s);
             }
             List<Sharing> list = dataSharingMap.get(userName);
             if (list == null) {
                 list = new ArrayList<Sharing>();
                 dataSharingMap.put(userName, list);
             }
             list.add(dataSharing);
 
         }
 
     }
 
     private JSONObject buildPermissionsRequestBody() {
         JSONObject obj = new JSONObject();
         JSONArray ids = new JSONArray();
         for (int i = 0; i < resources.size(); i++) {
             ids.set(i, new JSONString(resources.get(i).getId()));
         }
         obj.put("paths", ids);
         return obj;
     }
 
     private void callSharingService(JSONObject obj) {
         DiskResourceServiceFacade facade = new DiskResourceServiceFacade();
         facade.shareDiskResource(obj, new AsyncCallback<String>() {
 
             @Override
             public void onSuccess(String result) {
                 // do nothing intentionally
             }
 
             @Override
             public void onFailure(Throwable caught) {
                 ErrorHandler.post(caught);
 
             }
         });
     }
 
     private void callUnshareService(JSONObject obj) {
         DiskResourceServiceFacade facade = new DiskResourceServiceFacade();
         facade.unshareDiskResource(obj, new AsyncCallback<String>() {
 
             @Override
             public void onSuccess(String result) {
                 // do nothing
             }
 
             @Override
             public void onFailure(Throwable caught) {
                 ErrorHandler.post(caught);
 
             }
         });
     }
 
     private Grid<DiskResource> buildDiskResourceGrid() {
         CheckBoxSelectionModel<DiskResource> sm = new CheckBoxSelectionModel<DiskResource>();
         diskResourceGrid = new Grid<DiskResource>(new ListStore<DiskResource>(),
                 buildDiskResourceColumnModel(sm));
         diskResourceGrid.setSelectionModel(sm);
         diskResourceGrid.addPlugin(sm);
         diskResourceGrid.getStore().add(resources);
         GridView view = diskResourceGrid.getView();
         view.setViewConfig(buildGridViewConfig());
         view.setForceFit(true);
         new GridDragSource(diskResourceGrid);
         new DiskResourceDropTarget(diskResourceGrid);
         return diskResourceGrid;
     }
 
     private void setDNDFeedback(DNDEvent e, boolean feedback) {
         e.getStatus().setStatus(feedback);
         e.setCancelled(!feedback);
     }
 
     private boolean setDiskResourcDropFeedback(DNDEvent e, Element data) {
         if (data == null) {
             setDNDFeedback(e, false);
             return false;
         }
 
         int row = diskResourceGrid.getView().findRowIndex(data);
         if (row < 0) {
             setDNDFeedback(e, false);
             return false;
         }
 
         return true;
     }
 
     private ColumnModel buildDiskResourceColumnModel(CheckBoxSelectionModel<DiskResource> sm) {
         // build column configs and add them to a list for the ColumnModel.
         List<ColumnConfig> columns = new ArrayList<ColumnConfig>();
 
         ColumnConfig name = new ColumnConfig(DiskResource.NAME, I18N.DISPLAY.name(), 235);
         name.setRenderer(new DiskResourceNameCellRenderer());
         columns.addAll(Arrays.asList(sm.getColumn(), name));
 
         return new ColumnModel(columns);
     }
 
     private final class DiskResourceDropTarget extends GridDropTarget {
         private DiskResourceDropTarget(@SuppressWarnings("rawtypes") Grid grid) {
             super(grid);
         }
 
         @Override
         public void onDragMove(DNDEvent e) {
             super.onDragMove(e);
             Element data = diskResourceGrid.getView().findRow(e.getTarget());
             setDiskResourcDropFeedback(e, data);
         }
 
         @Override
         public void onDragEnter(DNDEvent e) {
             Element data = diskResourceGrid.getView().findRow(e.getTarget());
             setDiskResourcDropFeedback(e, data);
         }
 
         @Override
         public void onDragDrop(DNDEvent e) {
             Element data = diskResourceGrid.getView().findRow(e.getTarget());
             if (setDiskResourcDropFeedback(e, data)) {
                 int row = diskResourceGrid.getView().findRowIndex(data);
                 DiskResource dr = diskResourceGrid.getStore().getAt(row);
                 List<Collaborator> selectedCollabs = e.getData();
                 FastMap<DataSharing> smap = new FastMap<DataSharing>();
                 for (Collaborator c : selectedCollabs) {
                     DataSharing ds = new DataSharing(c, new Permissions(true, false, false), dr.getId());
                     smap.put(c.getUserName(), ds);
                 }
 
                 sharePanel.addDataSharing(smap);
             }
         }
     }
 
     private final class LoadPermissionsCallback implements AsyncCallback<String> {
         @Override
         public void onFailure(Throwable caught) {
             ErrorHandler.post(caught);
             sharePanel.unmask();
         }
 
         @Override
         public void onSuccess(String result) {
             JSONObject obj = JsonUtil.getObject(result);
             JSONArray permissionsArray = JsonUtil.getArray(obj, "paths");
             sharingList = new FastMap<Sharing>();
             dataSharingMap = new FastMap<List<Sharing>>();
             if (permissionsArray != null) {
                 for (int i = 0; i < permissionsArray.size(); i++) {
                     JSONObject user_perm_obj = permissionsArray.get(i).isObject();
                     String path = JsonUtil.getString(user_perm_obj, "path");
                     JSONArray user_arr = JsonUtil.getArray(user_perm_obj, "user-permissions");
                     loadPermissions(path, user_arr);
                 }
             }
             ArrayList<Sharing> list = new ArrayList<Sharing>(sharingList.values());
             sharePanel.loadSharingData(list, dataSharingMap);
             sharePanel.unmask();
 
         }
     }
 
     /**
      * A custom renderer that renders folder / file names as hyperlink
      * 
      * @author sriram
      * 
      */
     class DiskResourceNameCellRenderer implements GridCellRenderer<DiskResource> {
         @Override
         public Object render(final DiskResource model, String property, ColumnData config, int rowIndex,
                 int colIndex, ListStore<DiskResource> store, final Grid<DiskResource> grid) {
             Hyperlink link = null;
 
             if (model instanceof Folder) {
                 link = new Hyperlink("<img src='./gxt/images/default/tree/folder.gif'/>&nbsp;" //$NON-NLS-1$
                         + model.getName(), "mydata_name"); //$NON-NLS-1$
             } else {
                 link = new Hyperlink(
                         "<img src='./images/file.gif'/>&nbsp;" + model.getName(), "mydata_name"); //$NON-NLS-1$ //$NON-NLS-2$
             }
             link.setToolTip(model.getId());
             link.setWidth(model.getName().length());
             link.setOnMouseOverStyle("text-decoration", "none");
             return link;
         }
     }
 
     private JSONObject buildSharingJson() {
         JSONObject sharingObj = new JSONObject();
         FastMap<List<Sharing>> sharingMap = sharePanel.getSharingMap();
 
         if (sharingMap != null && sharingMap.size() > 0) {
             JSONArray sharingArr = new JSONArray();
             int index = 0;
             for (String userName : sharingMap.keySet()) {
                 List<Sharing> shareList = sharingMap.get(userName);
                 JSONObject userObj = new JSONObject();
                 userObj.put("user", new JSONString(userName));
                 userObj.put("paths", buildPathArrWithPermissions(shareList));
                 sharingArr.set(index++, userObj);
             }
 
             sharingObj.put("sharing", sharingArr);
             return sharingObj;
         } else {
             return null;
         }
     }
 
     private JSONArray buildPathArrWithPermissions(List<Sharing> list) {
         JSONArray pathArr = new JSONArray();
         int index = 0;
         JSONObject obj;
         for (Sharing s : list) {
             DataSharing ds = (DataSharing)s;
             obj = new JSONObject();
             obj.put("path", new JSONString(ds.getPath()));
             obj.put("permissions", buildSharingPermissions(ds));
             pathArr.set(index++, obj);
         }
 
         return pathArr;
     }
 
     private JSONArray buildPathArr(List<Sharing> list) {
         JSONArray pathArr = new JSONArray();
         int index = 0;
         for (Sharing s : list) {
             DataSharing ds = (DataSharing)s;
             pathArr.set(index++, new JSONString(ds.getPath()));
         }
         return pathArr;
     }
 
     private JSONObject buildSharingPermissions(DataSharing sh) {
         JSONObject permission = new JSONObject();
         permission.put("read", JSONBoolean.getInstance(sh.isReadable()));
         permission.put("write", JSONBoolean.getInstance(sh.isWritable()));
         permission.put("own", JSONBoolean.getInstance(sh.isOwner()));
         return permission;
     }
 
     private JSONObject buildUnSharingJson() {
         JSONObject unsharingObj = new JSONObject();
         FastMap<List<Sharing>> unSharingMap = sharePanel.getUnshareList();
 
         if (unSharingMap != null && unSharingMap.size() > 0) {
             JSONArray unsharingArr = new JSONArray();
             int index = 0;
             for (String userName : unSharingMap.keySet()) {
                 List<Sharing> shareList = unSharingMap.get(userName);
                 JSONObject userObj = new JSONObject();
                 userObj.put("user", new JSONString(userName));
                 userObj.put("paths", buildPathArr(shareList));
                 unsharingArr.set(index++, userObj);
             }
             unsharingObj.put("unshare", unsharingArr);
             return unsharingObj;
         } else {
             return null;
         }
 
     }
 }
