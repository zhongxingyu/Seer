 /**
  * 
  */
 package org.iplantc.de.client.views.panels;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.iplantc.core.jsonutil.JsonUtil;
 import org.iplantc.core.uicommons.client.ErrorHandler;
 import org.iplantc.de.client.I18N;
 import org.iplantc.de.client.models.Collaborator;
 import org.iplantc.de.client.models.JsCollaborators;
 import org.iplantc.de.client.services.UserSessionServiceFacade;
 import org.iplantc.de.client.utils.NotifyInfo;
 import org.iplantc.de.client.views.panels.ManageCollaboratorsPanel.MODE;
 
 import com.extjs.gxt.ui.client.Style.SortDir;
 import com.extjs.gxt.ui.client.dnd.GridDragSource;
 import com.extjs.gxt.ui.client.event.DNDEvent;
 import com.extjs.gxt.ui.client.event.IconButtonEvent;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.store.ListStore;
 import com.extjs.gxt.ui.client.widget.ContentPanel;
 import com.extjs.gxt.ui.client.widget.HorizontalPanel;
 import com.extjs.gxt.ui.client.widget.button.IconButton;
 import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
 import com.extjs.gxt.ui.client.widget.grid.ColumnData;
 import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
 import com.extjs.gxt.ui.client.widget.grid.Grid;
 import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
 import com.extjs.gxt.ui.client.widget.layout.FitLayout;
 import com.google.gwt.core.client.JsArray;
 import com.google.gwt.json.client.JSONArray;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.json.client.JSONParser;
 import com.google.gwt.json.client.JSONString;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Label;
 
 /**
  * @author sriram
  * 
  */
 public class CollaboratorsPanel extends ContentPanel {
 
     private Grid<Collaborator> grid;
     private ManageCollaboratorsPanel.MODE mode;
 
     // list that holds user's collaborators
     private List<Collaborator> my_collaborators;
 
     public CollaboratorsPanel(String title, ManageCollaboratorsPanel.MODE mode, int width, int height) {
         setHeading(title);
         setSize(width, height);
         setLayout(new FitLayout());
         setBodyBorder(false);
         setBorders(false);
         this.mode = mode;
         init();
     }
 
     private void init() {
 
         my_collaborators = new ArrayList<Collaborator>();
         ListStore<Collaborator> store = new ListStore<Collaborator>();
         grid = new Grid<Collaborator>(store, buildColumnModel());
         grid.setAutoExpandColumn(Collaborator.NAME);
         grid.setBorders(false);
         grid.getView().setEmptyText(I18N.DISPLAY.noCollaborators());
 
         add(grid);
         new GridDragSource(grid) {
             @Override
             protected void onDragStart(DNDEvent e) {
                 e.setData(grid.getSelectionModel().getSelectedItems());
             }
 
             @Override
             protected void onDragDrop(DNDEvent e) {
                 // do nothing intentionally
             }
         };
 
     }
 
     private ColumnModel buildColumnModel() {
         ColumnConfig name = new ColumnConfig(Collaborator.NAME, I18N.DISPLAY.name(), 150);
         name.setRenderer(new NameCellRenderer());
 
         ColumnConfig email = new ColumnConfig(Collaborator.EMAIL, I18N.DISPLAY.email(), 200);
         return new ColumnModel(Arrays.asList(name, email));
 
     }
 
     public void loadResults(List<Collaborator> collaborators) {
         // clear results before adding. Sort alphabetically
         ListStore<Collaborator> store = grid.getStore();
         store.removeAll();
         store.add(collaborators);
         store.sort(Collaborator.NAME, SortDir.ASC);
     }
 
     public List<Collaborator> parseResults(String result) {
         JSONObject obj = JSONParser.parseStrict(result).isObject();
         String json = obj.get("users").toString();
         JsArray<JsCollaborators> collabs = JsonUtil.asArrayOf(json);
         List<Collaborator> collaborators = new ArrayList<Collaborator>();
         for (int i = 0; i < collabs.length(); i++) {
             Collaborator c = new Collaborator(collabs.get(i));
             collaborators.add(c);
         }
         return collaborators;
     }
 
     public void clearStore() {
         grid.getStore().removeAll();
     }
 
     public void parseAndLoad(String result) {
         loadResults(parseResults(result));
     }
 
     public void setMode(ManageCollaboratorsPanel.MODE mode) {
         this.mode = mode;
     }
 
     /**
      * A custom renderer that renders with add / delete icon
      * 
      * @author sriram
      * 
      */
     private class NameCellRenderer implements GridCellRenderer<Collaborator> {
 
         private static final String REMOVE_BUTTON_STYLE = "remove_button";
         private static final String ADD_BUTTON_STYLE = "add_button";
         private static final String DELETE_BUTTON_STYLE = "delete_button";
         private static final String DONE_BUTTON_STYLE = "done_button";
 
         @Override
         public Object render(Collaborator model, String property, ColumnData config, int rowIndex,
                 int colIndex, ListStore<Collaborator> store, Grid<Collaborator> grid) {
 
             final HorizontalPanel hp = new HorizontalPanel();
 
             if (mode.equals(MODE.SEARCH)) {
                 hp.add(buildButton(ADD_BUTTON_STYLE, model));
             } else {
                 hp.add(buildButton(REMOVE_BUTTON_STYLE, model));
             }
             hp.add(new Label(model.getName()));
             hp.setSpacing(3);
             return hp;
         }
 
         private IconButton buildButton(final String style, final Collaborator model) {
             final IconButton btn = new IconButton(style, new SelectionListener<IconButtonEvent>() {
 
                 @Override
                 public void componentSelected(IconButtonEvent ce) {
                     IconButton src = (IconButton)ce.getSource();
                     String existing_style = src.getStyleName();
                     if (existing_style.contains(ADD_BUTTON_STYLE)) {
                         // TODO: check duplicates
 
                         addCollaborators(model);
                         if (mode.equals(MODE.SEARCH)) {
                             src.changeStyle(DONE_BUTTON_STYLE);
                         } else {
                             src.changeStyle(REMOVE_BUTTON_STYLE);
                         }
                         return;
                     }
 
                     if (existing_style.contains(REMOVE_BUTTON_STYLE)) {
                         src.changeStyle(DELETE_BUTTON_STYLE);
                         return;
                     }
 
                     if (existing_style.contains(DELETE_BUTTON_STYLE)) {
                         removeCollaborators(model);
                         return;
                     }
 
                 }
             });
             return btn;
         }
     }
 
     private void addCollaborators(final Collaborator model) {
         UserSessionServiceFacade facade = new UserSessionServiceFacade();
         JSONObject obj = buildJSONModel(model);
         facade.addCollaborators(obj, new AsyncCallback<String>() {
 
             @Override
             public void onSuccess(String result) {
                 my_collaborators.add(model);
                 NotifyInfo.display("Collaborator Added", model.getName()
                         + " is now collaborator with you.");
 
             }
 
             @Override
             public void onFailure(Throwable caught) {
                 ErrorHandler.post(caught);
             }
         });
 
     }
 
     private void removeCollaborators(final Collaborator model) {
         UserSessionServiceFacade facade = new UserSessionServiceFacade();
         JSONObject obj = buildJSONModel(model);
         facade.removeCollaborators(obj, new AsyncCallback<String>() {
 
             @Override
             public void onSuccess(String result) {
                 // TODO: call service to update
                 my_collaborators.remove(model);
                 grid.getStore().remove(model);
                 NotifyInfo.display("Collaborator Removed", model.getName()
                         + " removed from your collaborators list.");
 
             }
 
             @Override
             public void onFailure(Throwable caught) {
                 ErrorHandler.post(caught);
             }
         });
 
     }
 
     private JSONObject buildJSONModel(final Collaborator model) {
         JSONArray arr = new JSONArray();
        JSONObject user = new JSONObject();
        user.put("username", new JSONString(model.getUserName()));
        arr.set(0, user);
 
         JSONObject obj = new JSONObject();
         obj.put("users", arr);
         return obj;
     }
 
     public void setCurrentCollaborators(List<Collaborator> collaborators) {
         my_collaborators = collaborators;
     }
 
     public void showCurrentCollborators() {
         loadResults(my_collaborators);
     }
 
     public List<Collaborator> getCurrentCollaborators() {
         return my_collaborators;
     }
 
 }
