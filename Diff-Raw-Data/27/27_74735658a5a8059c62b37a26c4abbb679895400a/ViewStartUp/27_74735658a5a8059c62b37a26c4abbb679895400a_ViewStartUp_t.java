 package at.ac.tuwien.sepm.ui.startUp;
 
import at.ac.tuwien.sepm.service.FacebookService;
 import at.ac.tuwien.sepm.service.LvaFetcherService;
 import at.ac.tuwien.sepm.service.ModuleService;
 import at.ac.tuwien.sepm.service.PropertyService;
 import at.ac.tuwien.sepm.service.impl.ZidAuthService;
 import at.ac.tuwien.sepm.ui.SmallInfoPanel;
 import at.ac.tuwien.sepm.ui.StandardSimpleInsidePanel;
 import at.ac.tuwien.sepm.ui.UI;
 import at.ac.tuwien.sepm.ui.template.PanelTube;
 import org.apache.log4j.LogManager;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import javax.swing.*;
 import java.awt.*;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Author: Georg Plaz
  */
 @UI
 public class ViewStartUp extends StandardSimpleInsidePanel {
     private static Logger logger = LogManager.getLogger(ViewStartUp.class);
     private int currentIndex=0;
     //private StartRowPanel current;
     private List<SimpleDisplayPanel> allPanels;
 
     protected LvaFetcherService lvaFetcherService;
     protected ModuleService moduleService;
     protected PropertyService propertyService;
     protected ZidAuthService authService;
    protected FacebookService facebookService;
 
     @Autowired
    public ViewStartUp(LvaFetcherService lvaFetcherService,ModuleService moduleService,PropertyService propertyService,ZidAuthService authService, FacebookService facebookService) {
         this.lvaFetcherService = lvaFetcherService;
         this.moduleService=moduleService;
         this.propertyService=propertyService;
         this.authService=authService;
        this.facebookService = facebookService;
         addImage();
         init();
 
         setLayout(null);
         addTitle("Startup Wizard");
         //addReturnButton();
         addContent();
 
         this.repaint();
         this.revalidate();
     }
 
     private void addContent() {
         setBackground(Color.GREEN);
         allPanels = new ArrayList<SimpleDisplayPanel>();
         //JPanel temp = new JPanel();
         //temp.add(new JLabel("blabla"));
         //allPanels.add(temp);
         allPanels.add(new First(simpleWhiteSpace.getWidth(),simpleWhiteSpace.getHeight(),this));
         allPanels.add(new Second(simpleWhiteSpace.getWidth(),simpleWhiteSpace.getHeight(),this));
         allPanels.add(new Third(simpleWhiteSpace.getWidth(),simpleWhiteSpace.getHeight(),this));
 
         for(JPanel p: allPanels){
             p.setVisible(false);
             add(p);
             p.setBounds(simpleWhiteSpace);
             p.setBackground(Color.BLUE);
         }
 
 
 
     }
     public void jump(int index){
         logger.info("jumping from "+currentIndex+", to "+index);
         allPanels.get(currentIndex).setVisible(false);
         currentIndex = index;
         allPanels.get(currentIndex).setVisible(true);
         allPanels.get(currentIndex).refresh();
 
     }
     protected void next(){
         if(currentIndex+1 == allPanels.size()){
             propertyService.setProperty(PropertyService.FIRST_RUN,"false");
             PanelTube.backgroundPanel.showLastComponent();
             PanelTube.backgroundPanel.setControlsEnabled(true);
             PanelTube.backgroundPanel.viewSmallInfoText("Der Startup-Wizard wurde erfolgreich beendet.", SmallInfoPanel.Success);
         }else{
             jump(currentIndex+1);
         }
 
     }
     protected void back(){
         jump(currentIndex-1);
     }
 
 
 }
