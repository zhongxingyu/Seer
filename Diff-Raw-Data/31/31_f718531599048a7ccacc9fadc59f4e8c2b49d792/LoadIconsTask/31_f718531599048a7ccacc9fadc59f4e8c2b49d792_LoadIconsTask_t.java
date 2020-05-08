 package com.intelix.digihdmi.app.tasks;
 
 import com.intelix.digihdmi.app.DigiHdmiApp;
 import com.intelix.digihdmi.app.views.IconContainerPanel;
 import com.intelix.digihdmi.app.views.InputIconListView;
 import com.intelix.digihdmi.app.views.OutputIconListView;
 import com.intelix.digihdmi.model.Connector;
 import com.intelix.digihdmi.util.Indexed;
 import java.util.List;
 import java.util.logging.Logger;
 import javax.swing.AbstractButton;
 import javax.swing.Action;
 import javax.swing.JComponent;
 import org.jdesktop.application.Application;
 import org.jdesktop.application.Task;
 
 /**
  *
  * @author mcaron
  */
 public abstract class LoadIconsTask extends Task {
 
     Connector selected;
     List<String> iconList;
     DigiHdmiApp app;
     private final IconContainerPanel panel;
 
     public LoadIconsTask(Application app, IconContainerPanel p)
     {
         super(app);
         panel = p;
         this.app = (DigiHdmiApp)app;
     }
 
     public LoadIconsTask(Application app, Connector ctr) {
         super(app);
         selected = ctr;
         this.app = (DigiHdmiApp) app;
 
         JComponent c = ((DigiHdmiApp) app).getCurrentView();
         if (c instanceof InputIconListView) {
             panel = (IconContainerPanel)((InputIconListView) c).getButtonsPanel();
         } else if (c instanceof OutputIconListView) {
             panel = (IconContainerPanel)((OutputIconListView) c).getButtonsPanel();
         } else {
             panel = null;
         }
     }
 
     @Override
     protected void process(List values) {
         if (values.get(0) instanceof AbstractButton)
         {
             for (AbstractButton b: (List<AbstractButton>)values)
             {
                 if (selected != null) {
                     // this is our second trip through the icon list, we're
                     // just adding actions to existing buttons
                     b.setAction(getButtonAction());
                 } else {
                     // we're initializing the icons here
                     panel.addButton(b);
                 }
                //((DigiHdmiApp) getApplication()).getCurrentView().validate();
             }
         }
     }
 
     protected abstract String getIconResourceString(int index);
 
     @Override
     protected Object doInBackground() throws Exception {
         Logger.getLogger(this.getClass().getCanonicalName()).fine("Running load icons task");
 
         int numIcons = panel.getNumIcons();
         if (selected == null)
         {
             // actually LOAD the icons from their resources
             for (int i=1; i<=numIcons; i++)
             {
                 AbstractButton b = panel.createButton("Button_" + i,getIconResourceString(i));
                 ((Indexed)b).setIndex(i);
                 publish(b);
             }
         } else {
             // assign new actions for each button
             for (int i=0; i<numIcons; i++)
             {
                 publish(panel.getButton(i));
             }
         }
 
         Logger.getLogger(this.getClass().getCanonicalName()).fine("Finished load icons task");
         return null;
     }
 
     protected abstract Action getButtonAction();
 }
