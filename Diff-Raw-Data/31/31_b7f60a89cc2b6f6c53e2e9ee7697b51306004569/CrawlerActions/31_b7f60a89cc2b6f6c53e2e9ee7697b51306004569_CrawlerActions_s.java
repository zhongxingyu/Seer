 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package webspider.actions;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JTextField;
 
 import webspider.Settings;
 import webspider.actions.SpiderActions;
 import webspider.core.crawler.Spider;
 
 /**
  *
  * @author esh
  */
public class CrawlerActions implements ActionListener, Observer{
     private JButton controlButton;
     private JButton stopButton;
     private JTextField baseurlField;
 
     private JLabel stats_status;
     private JLabel stats_good;
     private JLabel stats_bad;
     private JLabel stats_internal;
     private JLabel stats_external;
     private JLabel stats_disallowed;
     
     private SpiderActions actions;
 
     protected Thread backgroundThread;
     protected Spider spider;
 
     CrawlerActions(SpiderActions actions) {
         this.actions = actions;
         spider = new Spider(actions);
     }
 
     public void actionPerformed(ActionEvent e) {
         if(e.getActionCommand().equals("start")){
             controlButton.setActionCommand("pause");
             controlButton.setText("Pause");
             stopButton.setEnabled(true);
             if(Settings.BACK_BUTTON)actions.getBacker().setEnabled(false);
             spider.startIWSpider(baseurlField.getText());
         }else if(e.getActionCommand().equals("pause")){
             spider.stopIWSpider();
             controlButton.setActionCommand("resume");
             controlButton.setText("Resume");
         }else if(e.getActionCommand().equals("resume")){
             spider.resumeIWSpider();
             controlButton.setActionCommand("pause");
             controlButton.setText("Pause");
         }else if(e.getActionCommand().equals("stop")){
             spider.killIWSpider();
             controlButton.setActionCommand("start");
             controlButton.setText("Start");
             stopButton.setEnabled(false);
             if(Settings.BACK_BUTTON)actions.getBacker().setEnabled(true);
         }
     }
 
     public void finished(){
         controlButton.setActionCommand("start");
         controlButton.setText("Start");
         stopButton.setEnabled(false);
         actions.getBacker().setEnabled(true);
     }
 
     // Statistics elements
     public void updateStats(){
         stats_status.setText("Status : " + spider.getStatus());
         stats_good.setText("Local Links : " + spider.getLocalLinks());
         stats_bad.setText("Dead Links : " + spider.getDeadLinks());
         stats_internal.setText("Non-Parsable Links : " + spider.getNonParsableLinks());
         stats_external.setText("External Links : " + spider.getExternalLinks());
         stats_disallowed.setText("Disallowed Links : " + spider.getDisallowedLinks());
     }

     public void initStatus(JLabel stats_status){
         this.stats_status = stats_status;
     }
 
     public void initGood(JLabel stats_good){
         this.stats_good = stats_good;
     }
 
     public void initBad(JLabel stats_bad){
         this.stats_bad = stats_bad;
     }
 
     public void initInternal(JLabel stats_internal){
         this.stats_internal = stats_internal;
     }
 
     public void initExternal(JLabel stats_external){
         this.stats_external = stats_external;
     }
 
     public void initDisallowed(JLabel stats_disallowed){
         this.stats_disallowed = stats_disallowed;
     }
 
     //ELEMENTS
     public void initContoller(JButton controlButton){
         this.controlButton = controlButton;
     }
 
     public void initStopper(JButton stopButton){
         this.stopButton = stopButton;
     }
 
     public void initBaseText(JTextField baseurlField){
         this.baseurlField = baseurlField;
     }
 
	@Override
	public void update(Observable arg0, Object arg1) {
		
	}

 }
