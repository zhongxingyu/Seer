 package kkckkc.jsourcepad.installer;
 
 import kkckkc.jsourcepad.installer.bundle.BundleInstallerDialog;
 import kkckkc.jsourcepad.model.Window;
 import kkckkc.jsourcepad.util.action.ActionGroup;
 import kkckkc.jsourcepad.util.action.ActionManager;
 import org.springframework.beans.factory.BeanFactory;
 
 import javax.swing.*;
 import java.awt.event.ActionEvent;
 
 public class InstallerMenu {
 
     public static void init(final BeanFactory container, final Window window) {
         ActionManager am = container.getBean(ActionManager.class);
         ActionGroup ag = am.getActionGroup("bundles-menu");
 
 
         AbstractAction showInstallerAction = new AbstractAction() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 BundleInstallerDialog dialog = window.getPresenter(BundleInstallerDialog.class);
                 dialog.show();;
             }
         };
         showInstallerAction.putValue(AbstractAction.NAME, "Install Bundles...");
 
         ag.add(ag.size() - 1, showInstallerAction);
 
     }
 
 }
