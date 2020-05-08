 package org.jrydberg.ui.client;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.resources.client.ClientBundle;
 import com.google.gwt.resources.client.ImageResource;
 import com.google.gwt.user.client.Command;
 import com.google.gwt.user.client.ui.RootLayoutPanel;
 
 /**
  * Entry point classes define <code>onModuleLoad()</code>.
  */
 public class JrydbergUi implements EntryPoint {
   
   public interface Resources extends ClientBundle {
     Resources INSTANCE = GWT.create(Resources.class);
     
     @Source("resources/plus.png")
     ImageResource plusIcon();
     
   }
   
   /**
    * This is the entry point method.
    */
   public void onModuleLoad() {
   
     LayoutPanel layoutPanel = new LayoutPanel(400, 200);
     
     layoutPanel.add(new TextBox()).at(10, 10).size(100, 24);
     layoutPanel.add(new Button("test")).at(120, 10).size(100, 24);
 
     layoutPanel.add(ButtonBar.create()
           .add(new Button(Resources.INSTANCE.plusIcon()), 30)
           .add(new Button("b"), 30)
           .add(new Button("+"), 30))
         .at(10, 40).size(100, 24);
    
     final Command testCommand = new Command() {
       @Override
       public void execute() {
         // TODO Auto-generated method stub
       }
     };
     
     PopupMenu menu = new PopupMenu();
     menu.addItem("test", testCommand);
     menu.addItem("repository-1", testCommand);
     menu.addItem("repository-1", testCommand);
     menu.addItem("Add service...", testCommand);
     
     layoutPanel.add(ButtonBar.create()
           .add(new MenuButton(Resources.INSTANCE.plusIcon(), menu), 40)
           .add(new Button("-"), 30))
         .at(10, 80).size(100, 24);
     
 //    layoutPanel.add(new MenuButton(Resources.INSTANCE.plusIcon(), null))
 //        .at(10, 80).size(100, 24);
     
     RootLayoutPanel.get().add(layoutPanel);
   }
 }
