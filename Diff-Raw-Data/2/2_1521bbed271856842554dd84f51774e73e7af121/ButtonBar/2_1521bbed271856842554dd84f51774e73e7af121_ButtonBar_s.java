 package de.iweinzierl.passsafe.gui.widget;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 
 import java.io.IOException;
 
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import de.iweinzierl.passsafe.gui.Application;
 import de.iweinzierl.passsafe.gui.ApplicationController;
 import de.iweinzierl.passsafe.gui.action.NewCategoryDialogAction;
 import de.iweinzierl.passsafe.gui.action.NewEntryDialogAction;
 import de.iweinzierl.passsafe.gui.resources.Errors;
 import de.iweinzierl.passsafe.gui.resources.Messages;
 import de.iweinzierl.passsafe.gui.util.UiUtils;
 
 public class ButtonBar extends JPanel {
 
    public static final int BUTTON_WIDTH = 100;
     public static final int BUTTON_HEIGHT = 25;
 
     private Logger LOGGER = LoggerFactory.getLogger(ButtonBar.class);
 
     private ApplicationController controller;
     private Application parent;
 
     public ButtonBar(final ApplicationController controller, final Application parent) {
         super();
         this.controller = controller;
         this.parent = parent;
 
         initialize();
     }
 
     private void initialize() {
         setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
         add(createAddEntryButton());
         add(createAddCategoryButton());
         add(createSyncButton());
         add(createChangePasswordButton());
         add(createSearchField());
     }
 
     private JButton createAddEntryButton() {
         return WidgetFactory.createButton(Messages.getMessage(Messages.BUTTONBAR_NEWENTRY), BUTTON_WIDTH, BUTTON_HEIGHT,
                 new NewEntryDialogAction(controller, parent));
     }
 
     private JButton createAddCategoryButton() {
         return WidgetFactory.createButton(Messages.getMessage(Messages.BUTTONBAR_NEWCATEGORY), BUTTON_WIDTH,
                 BUTTON_HEIGHT, new NewCategoryDialogAction(controller, parent));
     }
 
     private JButton createSyncButton() {
         return WidgetFactory.createButton(Messages.getMessage(Messages.BUTTONBAR_SYNC), BUTTON_WIDTH, BUTTON_HEIGHT,
                 new ActionListener() {
                     @Override
                     public void actionPerformed(final ActionEvent e) {
                         try {
                             controller.requestSync();
                         } catch (IOException ex) {
                             LOGGER.error("Unable to sync", ex);
                             UiUtils.displayError(null, Errors.getError(Errors.SYNC_FAILED));
                         }
                     }
                 });
     }
 
     private JButton createChangePasswordButton() {
         return WidgetFactory.createButton(Messages.getMessage(Messages.BUTTONBAR_CHANGEPASSWORD), BUTTON_WIDTH,
                 BUTTON_HEIGHT, new ActionListener() {
                     @Override
                     public void actionPerformed(final ActionEvent e) {
                         try {
                             controller.requestChangePassword();
                         } catch (IOException ex) {
                             LOGGER.error("Unable to sync", ex);
                             UiUtils.displayError(null, Errors.getError(Errors.SYNC_FAILED));
                         }
                     }
                 });
     }
 
     private JTextField createSearchField() {
         final JTextField search = new JTextField(30);
         search.addKeyListener(new KeyListener() {
                 @Override
                 public void keyTyped(final KeyEvent e) { }
 
                 @Override
                 public void keyPressed(final KeyEvent e) { }
 
                 @Override
                 public void keyReleased(final KeyEvent e) {
                     String searchText = search.getText();
                     LOGGER.debug("Search for: {}", searchText);
 
                     controller.requestEntrySearch(searchText);
                 }
             });
 
         return search;
     }
 }
