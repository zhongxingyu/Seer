 package net.premereur.mvp.example.vaadin.app;
 
 import net.premereur.mvp.core.EventBus;
 import net.premereur.mvp.core.View;
 
 import com.google.inject.Inject;
 import com.vaadin.ui.ComponentContainer;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.MenuBar;
 import com.vaadin.ui.VerticalLayout;
 import com.vaadin.ui.Window;
 import com.vaadin.ui.MenuBar.Command;
 import com.vaadin.ui.MenuBar.MenuItem;
 
 /**
  * The main window of the application. Other windows are attached here to provide content the user can actually interact with. This window does provide a menu.
  * 
  * @author gpremer
  * 
  */
 @SuppressWarnings("serial")
 public class ApplicationWindow extends Window implements View {
 
     private final ApplicationBus bus;
 
     private ComponentContainer workPane;
 
     /**
      * Constructs the application window.
      * 
      * @param bus
      */
     @Inject
     public ApplicationWindow(final EventBus bus) {
         super("Sample Vaadin MVP Application");
         this.bus = (ApplicationBus) bus;
        ((VerticalLayout) getContent()).setMargin(false);
         setSizeFull();
         addMenu();
         addWorkPane();
     }
 
     private ApplicationBus getEventBus() {
         return bus;
     }
 
     private void addMenu() {
         final HorizontalLayout menuView = new HorizontalLayout();
         final MenuBar menuBar = new MenuBar();
         menuView.setSizeUndefined();
         addComponent(menuView);
         menuView.addComponent(menuBar);
         final MenuItem appItem = menuBar.addItem("Application", null);
         appItem.addItem("Categories", new Command() {
 
             @Override
             public void menuSelected(final MenuItem selectedItem) {
                 // We may want to refactor this so that we forward the event to
                 // the presenter iso to the bus directly
                 getEventBus().selectCategoryMgt();
             }
         });
         appItem.addItem("Products", new Command() {
 
             @Override
             public void menuSelected(final MenuItem selectedItem) {
                 // TODO send to event bus
             }
         });
     }
 
     private void addWorkPane() {
         workPane = new VerticalLayout(); // An empty pane really
         addComponent(workPane);
     }
 
     /**
      * Sets the actual visible screen below the menu.
      * 
      * @param container
      */
     void setWorkPane(final ComponentContainer container) {
         removeComponent(workPane);
         this.workPane = container;
         workPane.setSizeFull();
         addComponent(workPane);
     }
 
 }
