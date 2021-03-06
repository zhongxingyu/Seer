 /*
  * @(#) MacIntegration.java     10-05-2008  18:59
  */
 package esmska.integration;
 
 import java.awt.PopupMenu;
 import java.awt.event.ActionEvent;
 
 import com.apple.eawt.Application;
 import com.apple.eawt.ApplicationEvent;
 import com.apple.eawt.ApplicationListener;
 
 import esmska.gui.MainFrame;
 import esmska.gui.NotificationIcon;
 
 /**
  * Integration for Mac OS X.
  * 
  * @author Marian Boucek
  * @version 1.0
  */
 public class MacIntegration extends IntegrationAdapter implements ApplicationListener {
     
     private Application app;
     
     /**
     * @see esmska.integration.IntegrationAdapter#initialize()
      */
     @Override
     protected void initialize() {
         app = new Application();
         
         app.setEnabledAboutMenu(true);
         app.setEnabledPreferencesMenu(true);
         
         app.addApplicationListener(this);
     }
     
     /**
      * @see esmska.integration.IntegrationAdapter#setActionBean(esmska.integration.ActionBean)
      */
     @Override
     public void setActionBean(ActionBean bean) {
         super.setActionBean(bean);
         
         NotificationIcon icon = NotificationIcon.getInstance();
         if (icon != null) {
             PopupMenu menu = icon.getPopup();
             app.setDockMenu(menu);
         }
     }
     
     // public interface -------------------------------------------------------
     /**
      * @see IntegrationAdapter#setSMSCount(Integer)
      */
     @Override
     public void setSMSCount(Integer count) {
         if (count == null || count <= 0) {
             app.setDockIconBadge(null);
         } else {
             app.setDockIconBadge(count.toString());
         }
     }
     
     // implementation of app interface ----------------------------------------
     /**
      * @see com.apple.eawt.ApplicationListener#handleAbout(com.apple.eawt.ApplicationEvent)
      */
     @Override
     public void handleAbout(ApplicationEvent e) {
         e.setHandled(true);
         bean.getAboutAction().actionPerformed(new ActionEvent(e.getSource(), 
                 ActionEvent.ACTION_PERFORMED, "aboutSelected"));
     }
     
     /**
      * @see com.apple.eawt.ApplicationListener#handleOpenApplication(com.apple.eawt.ApplicationEvent)
      */
     @Override
     public void handleOpenApplication(ApplicationEvent e) {
         e.setHandled(false);
     }
     
     /**
      * @see com.apple.eawt.ApplicationListener#handleOpenFile(com.apple.eawt.ApplicationEvent)
      */
     @Override
     public void handleOpenFile(ApplicationEvent e) {
         e.setHandled(false);
     }
     
     /**
      * @see com.apple.eawt.ApplicationListener#handlePreferences(com.apple.eawt.ApplicationEvent)
      */
     @Override
     public void handlePreferences(ApplicationEvent e) {
         e.setHandled(true);
         bean.getConfigAction().actionPerformed(new ActionEvent(e.getSource(),
                 ActionEvent.ACTION_PERFORMED, "configSelected"));
     }
     
     /**
      * @see com.apple.eawt.ApplicationListener#handlePrintFile(com.apple.eawt.ApplicationEvent)
      */
     @Override
     public void handlePrintFile(ApplicationEvent e) {
         e.setHandled(false);
     }
     
     /**
      * @see com.apple.eawt.ApplicationListener#handleQuit(com.apple.eawt.ApplicationEvent)
      */
     @Override
     public void handleQuit(ApplicationEvent e) {
         e.setHandled(true);
         bean.getQuitAction().actionPerformed(new ActionEvent(e.getSource(), 
                 ActionEvent.ACTION_PERFORMED, "quitSelected"));
     }
     
     /**
      * @see com.apple.eawt.ApplicationListener#handleReOpenApplication(com.apple.eawt.ApplicationEvent)
      */
     @Override
     public void handleReOpenApplication(ApplicationEvent e) {
         e.setHandled(true);
         MainFrame.getInstance().setVisible(true);
     }
 }
