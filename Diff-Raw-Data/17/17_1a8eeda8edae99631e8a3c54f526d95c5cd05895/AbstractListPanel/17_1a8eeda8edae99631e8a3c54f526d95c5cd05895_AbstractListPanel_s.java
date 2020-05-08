 package org.mule.galaxy.web.client.ui.panel;
 
 import com.extjs.gxt.ui.client.data.BeanModel;
 import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
 import com.extjs.gxt.ui.client.event.SelectionChangedListener;
 import com.extjs.gxt.ui.client.store.StoreEvent;
 import com.extjs.gxt.ui.client.store.StoreListener;
 import com.extjs.gxt.ui.client.widget.ContentPanel;
 import com.extjs.gxt.ui.client.widget.form.StoreFilterField;
 import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
 import com.extjs.gxt.ui.client.widget.grid.Grid;
 import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
 import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.Panel;
 
 /**
  *
  * Base class for panel presenting information using a {@link Grid}.
  * <br />
 * An optional toolbar composed of a {@link StoreFilterField} and a {@link ControlToolbarButtonBar} can be set. 
  *
  * @param <M>
  */
 public abstract class AbstractListPanel<M extends BeanModel> extends AbstractRefreshableComponent {
 
     /**
      *
      * {@link ToolbarButtonBar} aware of underlying {@link Grid} selection.
      *
      */
    public abstract static class ControlToolbarButtonBar extends ToolbarButtonBar {
 
         private final Grid<BeanModel> grid;
         
        public ControlToolbarButtonBar(final Grid<BeanModel> grid) {
             this.grid = grid;
 
             grid.getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<BeanModel>() {
                 public void selectionChanged(final SelectionChangedEvent<BeanModel> event) {
                     refreshSelection();
                 }
             });
             grid.getStore().addStoreListener(new StoreListener<BeanModel>() {
                 @Override
                 public void handleEvent(final StoreEvent<BeanModel> e) {
                     refreshSelection();
                 }
             });
         }
 
         public Grid<BeanModel> getGrid() {
             return this.grid;
         }
 
         protected abstract void refreshSelection();
 
     }
 
     private final FlowPanel panel = new FlowPanel();
    private ControlToolbarButtonBar toolbarButtonBar;
 
     public AbstractListPanel() {
         initWidget(this.panel);
     }
 
     protected ContentPanel createContentPanel() {
         return new FullContentPanel();
     }
 
     protected abstract Grid<M> createGrid();
 
     protected StoreFilterField<M> createFilter() {
         return null;
     }
 
    protected ControlToolbarButtonBar createToolbarButtonBar(final Grid<M> grid) {
         return null;
     }
 
     @Override
     public void doShowPage() {
         super.doShowPage();
 
         final Grid<M> grid = createGrid();
 
         final ToolBar buttonBar = new ToolBar();
         final StoreFilterField<M> filter = createFilter();
         if (filter != null) {
             // Bind the filter field to your grid store (grid.getStore())
             filter.bind(grid.getStore());
 
             buttonBar.add(filter);
         }
         // Fills the toolbar width, pushing any newly added items to the right.
         buttonBar.add(new FillToolItem());
 
         final CheckBoxSelectionModel<M> selectionModel = new CheckBoxSelectionModel<M>();
         grid.setSelectionModel(selectionModel);
         this.toolbarButtonBar = createToolbarButtonBar(grid);
         if (this.toolbarButtonBar != null) {
             selectionModel.bind(grid.getStore());
             grid.addPlugin(selectionModel);
             grid.getColumnModel().getColumns().add(0, selectionModel.getColumn());
 
             buttonBar.add(this.toolbarButtonBar);
         }
 
         final ContentPanel contentPanel = createContentPanel();
         final String title = getTitle();
         if (title != null) {
             contentPanel.setHeading(title);
         }
         contentPanel.add(buttonBar);
         contentPanel.add(grid);
 
         getPanel().clear();
         getPanel().add(contentPanel);
 
         refresh();
     }
 
     protected Panel getPanel() {
         return this.panel;
     }
 
     public String getTitle() {
         return null;
     }
 
    public ControlToolbarButtonBar getToolbarButtonBar() {
         return this.toolbarButtonBar;
     }
 
 }
