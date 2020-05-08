 /**
  * 
  */
 package edu.wustl.cab2b.client.ui.mainframe.stackbox;
 
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Vector;
 
 import edu.wustl.cab2b.client.cache.UserCache;
 import edu.wustl.cab2b.client.ui.controls.Cab2bHyperlink;
 import edu.wustl.cab2b.client.ui.controls.Cab2bLabel;
 import edu.wustl.cab2b.client.ui.controls.Cab2bPanel;
 import edu.wustl.cab2b.client.ui.controls.RiverLayout;
 import edu.wustl.cab2b.client.ui.experiment.ExperimentOpenPanel;
 import edu.wustl.cab2b.client.ui.mainframe.NewWelcomePanel;
 import edu.wustl.cab2b.client.ui.util.CommonUtils;
 import edu.wustl.cab2b.common.domain.Experiment;
 
 /**
  * MyExperiment panel containg links for latest five experiment and Show All links 
  * @author deepak_shingan
  *
  */
 public class MyExperimentLinkPanel extends Cab2bPanel {
     /**
      * Only single private static instance of MyExperimentLinkPanel class  
      */
     private static MyExperimentLinkPanel instance = new MyExperimentLinkPanel();
 
     /**
      * Constructor
      */
     private MyExperimentLinkPanel() {
         updateMyExperimentPanel();
     }
 
     /**
      * Returns singleton instance of MyExperimentLinkPanel
      * @return SavedQueryLinkPanel
      */
     public static MyExperimentLinkPanel getInstance() {
         return instance;
     }
 
     /**
      * Comparator class for experiments, returns experiments of descending order based on Experiment.ID
      * @author deepak_shingan
      *
      */
     class ExperimentComparator implements Comparator<Experiment> {
         public int compare(Experiment arg0, Experiment arg1) {
             int result = 0;
             long value = arg0.getId() - arg1.getId();
             if (value > 0) {
                 result = -1;
             } else if (value < 0) {
                 result = 1;
             }
             return result;
         }
     }
 
     /**
      * Method for updating MyExperimentPanel
      */
     public void updateMyExperimentPanel() {
         this.removeAll();
         Vector<Experiment> exps = CommonUtils.getExperiments(NewWelcomePanel.getMainFrame(),
                                                              UserCache.getInstance().getCurrentUser());
         Collections.sort(exps, new ExperimentComparator());
         getLatestExperimentsPanel(exps, new ExperimentHyperlinkActionListener());
         this.updateUI();
     }
 
     /**
      * This method returns panel with most recently saved five experiments from database.
      * TODO: Currently getting all experiments from database also have to update code for 
      * userbase fetaching of experiments 
      * @param data
      */
     private void getLatestExperimentsPanel(Vector<Experiment> data, ActionListener actionListener) {
 
         this.setLayout(new RiverLayout(5, 5));
         this.add(new Cab2bLabel());
         Cab2bHyperlink hyperlink = null;
         int expCounter = 0;
         for (Experiment experiment : data) {
             hyperlink = new Cab2bHyperlink(true);
             CommonUtils.setHyperlinkProperties(hyperlink, experiment, experiment.getName(),
                                                experiment.getDescription(), actionListener);
             this.add("br", hyperlink);
             if (++expCounter > 4)
                 break;
         }
 
         ActionListener showAllExpAction = new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 NewWelcomePanel.getMainFrame().getGlobalNavigationPanel().getGlobalNavigationGlassPane().setExperimentHomePanel();
             }
         };
 
        if (expCounter > 4) {
             Cab2bHyperlink showAllHyperlink = new Cab2bHyperlink(true);
            CommonUtils.setHyperlinkProperties(showAllHyperlink, null, MainFrameStackedBoxPanel.SHOW_ALL_LINK, "",
                                                showAllExpAction);
             this.add("br right", showAllHyperlink);
         } else if (expCounter == 0) {
             Cab2bLabel label = new Cab2bLabel("No saved experiments.");
             label.setBackground(Color.blue);
             this.add(label);
         }
     }
 
     /**
      * Homepage Experiment Link action listener class
      * @author deepak_shingan
      * 
      */
     class ExperimentHyperlinkActionListener implements ActionListener {
         public void actionPerformed(ActionEvent e) {
             Cab2bHyperlink hyperLink = (Cab2bHyperlink) e.getSource();
             Experiment exp = (Experiment) hyperLink.getUserObject();
             ExperimentOpenPanel expPanel = new ExperimentOpenPanel(exp);
             NewWelcomePanel.getMainFrame().openExperiment(expPanel);
             NewWelcomePanel.getMainFrame().getGlobalNavigationPanel().getGlobalNavigationGlassPane().setExperimentTabSelected();
             updateUI();
         }
     }
 }
