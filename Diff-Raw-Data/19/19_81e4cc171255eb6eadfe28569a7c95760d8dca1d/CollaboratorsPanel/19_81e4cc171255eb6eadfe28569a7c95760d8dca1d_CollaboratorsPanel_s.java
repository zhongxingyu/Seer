 /**
  * 
  */
 package org.iplantc.de.client.views.panels;
 
 import java.util.Arrays;
 import java.util.List;
 
 import org.iplantc.de.client.I18N;
 import org.iplantc.de.client.models.Collaborator;
 import org.iplantc.de.client.utils.CollaboratorsUtil;
 import org.iplantc.de.client.views.panels.ManageCollaboratorsPanel.MODE;
 
 import com.extjs.gxt.ui.client.Style.SortDir;
 import com.extjs.gxt.ui.client.data.ModelData;
 import com.extjs.gxt.ui.client.dnd.GridDragSource;
 import com.extjs.gxt.ui.client.event.BaseEvent;
 import com.extjs.gxt.ui.client.event.DNDEvent;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.IconButtonEvent;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.store.ListStore;
 import com.extjs.gxt.ui.client.widget.ContentPanel;
 import com.extjs.gxt.ui.client.widget.HorizontalPanel;
 import com.extjs.gxt.ui.client.widget.MessageBox;
 import com.extjs.gxt.ui.client.widget.button.IconButton;
 import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
 import com.extjs.gxt.ui.client.widget.grid.ColumnData;
 import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
 import com.extjs.gxt.ui.client.widget.grid.Grid;
 import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
 import com.extjs.gxt.ui.client.widget.layout.FitLayout;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Label;
 
 /**
  * @author sriram
  * 
  */
 public class CollaboratorsPanel extends ContentPanel {
 
     private Grid<Collaborator> grid;
     private ManageCollaboratorsPanel.MODE mode;
 
     public CollaboratorsPanel(String title, ManageCollaboratorsPanel.MODE mode, int width, int height) {
         setHeading(title);
         setSize(width, height);
         setLayout(new FitLayout());
         setBodyBorder(false);
         setBorders(true);
         this.mode = mode;
         init();
     }
 
     private void init() {
         ListStore<Collaborator> store = new ListStore<Collaborator>();
         grid = new Grid<Collaborator>(store, buildColumnModel());
         grid.setAutoExpandColumn(Collaborator.NAME);
         grid.setBorders(false);
        grid.getView().setEmptyText(I18N.DISPLAY.noCollaborators());
 
         add(grid);
         new GridDragSource(grid) {
             @Override
             protected void onDragStart(DNDEvent e) {
                 List<ModelData> list = grid.getSelectionModel().getSelectedItems();
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
 
     public void clearStore() {
         grid.getStore().removeAll();
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
         public Object render(final Collaborator model, String property, ColumnData config, int rowIndex,
                 int colIndex, ListStore<Collaborator> store, final Grid<Collaborator> grid) {
 
             final HorizontalPanel hp = new HorizontalPanel();
 
             if (mode.equals(MODE.SEARCH)) {
                 if (!CollaboratorsUtil.isCurrentCollaborator(model)) {
                     IconButton ib = buildButton(ADD_BUTTON_STYLE, model);
                     hp.add(ib);
                     ib.setToolTip("Add");
                 } else {
                     IconButton ib = buildButton(DONE_BUTTON_STYLE, model);
                     hp.add(ib);
                     ib.setToolTip(I18N.DISPLAY.collaborators());
                 }
             } else {
                 IconButton ib = buildButton(REMOVE_BUTTON_STYLE, model);
                 hp.add(ib);
                 ib.setToolTip("Remove");
             }
             hp.sinkEvents(Events.OnMouseDown.getEventCode());
             hp.addListener(Events.OnMouseDown, new Listener<BaseEvent>() {
 
                 @Override
                 public void handleEvent(BaseEvent be) {
                     grid.getSelectionModel().select(false, model);
                 }
             });
 
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
                         if (!CollaboratorsUtil.checkCurrentUser(model)) {
                             addCollaborators(Arrays.asList(model));
                             if (mode.equals(MODE.SEARCH)) {
                                 src.changeStyle(DONE_BUTTON_STYLE);
                                 src.setToolTip("Added");
                             } else if (mode.equals(MODE.MANAGE)) {
                                 src.changeStyle(REMOVE_BUTTON_STYLE);
                                 src.setToolTip("Remove");
                             }
                             return;
                         } else {
                             MessageBox.alert(I18N.DISPLAY.error(), I18N.ERROR.selfCollabAddError(), null);
                         }
                     }
 
                     if (existing_style.contains(REMOVE_BUTTON_STYLE)) {
                         src.changeStyle(DELETE_BUTTON_STYLE);
                         src.setToolTip(I18N.DISPLAY.confirmRemove());
                         return;
                     }
 
                     if (existing_style.contains(DELETE_BUTTON_STYLE)) {
                         removeCollaborators(Arrays.asList(model));
                         return;
                     }
 
                 }
             });
             return btn;
         }
     }
 
     public void addCollaborators(final List<Collaborator> models) {
         CollaboratorsUtil.addCollaborators(models, new AsyncCallback<Void>() {
 
             @Override
             public void onSuccess(Void result) {
                 // do nothing
             }
 
             @Override
             public void onFailure(Throwable caught) {
                 // do nothing
             }
         });
 
     }
 
     private void removeCollaborators(final List<Collaborator> models) {
         CollaboratorsUtil.removeCollaborators(models, new AsyncCallback<Void>() {
 
             @Override
             public void onSuccess(Void result) {
                 for (Collaborator c : models) {
                     grid.getStore().remove(c);
                 }
 
             }
 
             @Override
             public void onFailure(Throwable caught) {
                 // do nothing
             }
         });
 
     }
 
     public void showCurrentCollborators() {
         loadResults(CollaboratorsUtil.getCurrentCollaborators());
     }
 
     public Collaborator getSelectedCollaborator() {
         return grid.getSelectionModel().getSelectedItem();
     }
 
 }
