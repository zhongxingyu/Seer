 package de.iweinzierl.passsafe.gui.widget;
 
 import java.awt.Dimension;
import java.awt.FlowLayout;
 import java.awt.datatransfer.Clipboard;
 import java.awt.datatransfer.StringSelection;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import java.beans.PropertyChangeListener;
 
 import javax.swing.Action;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.text.JTextComponent;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import de.iweinzierl.passsafe.gui.ApplicationController;
 import de.iweinzierl.passsafe.gui.event.Event;
 import de.iweinzierl.passsafe.gui.event.EventBus;
 import de.iweinzierl.passsafe.gui.event.EventType;
 import de.iweinzierl.passsafe.gui.exception.PassSafeSecurityException;
 import de.iweinzierl.passsafe.gui.resources.Images;
 import de.iweinzierl.passsafe.gui.resources.Messages;
 import de.iweinzierl.passsafe.gui.util.UiUtils;
 import de.iweinzierl.passsafe.gui.widget.secret.SwitchablePasswordField;
 import de.iweinzierl.passsafe.shared.domain.Entry;
 
 // TODO make text fields smaller
 public class EntryView extends JPanel {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(EntryView.class);
 
     private class ClipboardMenu extends JMenuItem {
 
         private ClipboardMenu(final JComponent component) {
             super(new ClipboardMenuAction(component));
         }
     }
 
     private class ClipboardMenuAction implements Action {
 
         private JComponent component;
 
         private ClipboardMenuAction(final JComponent component) {
             this.component = component;
         }
 
         @Override
         public Object getValue(final String key) {
             switch (key) {
 
                 case "Name" :
                     return Messages.getMessage(Messages.ENTRYVIEW_MENU_COPYTOCLIPBOARD);
             }
 
             return null;
         }
 
         @Override
         public void putValue(final String key, final Object value) {
             // nothing to do here
         }
 
         @Override
         public void setEnabled(final boolean b) {
             // nothing to do here
         }
 
         @Override
         public boolean isEnabled() {
             return true;
         }
 
         @Override
         public void addPropertyChangeListener(final PropertyChangeListener listener) {
             // nothing to do here
         }
 
         @Override
         public void removePropertyChangeListener(final PropertyChangeListener listener) {
             // nothing to do here
         }
 
         @Override
         public void actionPerformed(final ActionEvent e) {
             Clipboard clipboard = getToolkit().getSystemClipboard();
             String text = null;
             if (component instanceof JTextField) {
                 text = ((JTextField) component).getText();
             } else if (component instanceof SwitchablePasswordField) {
                 text = ((SwitchablePasswordField) component).getPassword();
             }
 
             StringSelection stringSelection = new StringSelection(text);
             clipboard.setContents(stringSelection, null);
         }
     }
 
     private interface ValueProvider {
         String getValue();
 
         void setValue(Entry entry, String value);
     }
 
     private final ApplicationController controller;
 
     private final JTextField titleField;
     private final JTextField urlField;
     private final JTextArea commentsField;
 
     private final SwitchablePasswordField usernameField;
     private final SwitchablePasswordField passwordField;
 
     private final JButton userInvisible;
     private final JButton userVisible;
     private final JButton passInvisible;
     private final JButton passVisible;
 
     private Entry entry;
 
     public EntryView(final ApplicationController controller) {
         super();
         this.controller = controller;
 
         titleField = new JTextField();
         urlField = new JTextField();
         commentsField = new JTextArea();
         usernameField = new SwitchablePasswordField();
         passwordField = new SwitchablePasswordField();
 
         userVisible = WidgetFactory.createImageButton(Images.ENTRYVIEW_BUTTON_VISIBLE);
         userInvisible = WidgetFactory.createImageButton(Images.ENTRYVIEW_BUTTON_INVISIBLE);
         passVisible = WidgetFactory.createImageButton(Images.ENTRYVIEW_BUTTON_VISIBLE);
         passInvisible = WidgetFactory.createImageButton(Images.ENTRYVIEW_BUTTON_INVISIBLE);
 
         initialize();
     }
 
     private void initialize() {
        setLayout(new FlowLayout(FlowLayout.CENTER));
 
         titleField.setEditable(false);
         usernameField.setEditable(false);
         passwordField.setEditable(false);
         urlField.setEditable(false);
         commentsField.setEditable(false);
 
         add(createTitleRow());
         add(createUrlRow());
         add(createUsernameRow());
         add(createPasswordRow());
         add(createCommentRow());
 
         setMinimumSize(new Dimension(450, 225));
         setPreferredSize(new Dimension(450, 225));
         setMaximumSize(new Dimension(700, 400));
     }
 
     private JPanel createRow(final JLabel label, final JComponent inputField, final JPanel buttons) {
         JPanel row = new JPanel();
        row.setLayout(new FlowLayout(FlowLayout.LEFT));
 
         label.setPreferredSize(new Dimension(100, 25));
        inputField.setPreferredSize(new Dimension(150, 25));
         buttons.setPreferredSize((new Dimension(200, 25)));
 
        row.add(label);
        row.add(inputField);
        row.add(buttons);
 
         return row;
     }
 
     private JPanel createTitleRow() {
 
         initializeClipboardFunctions(titleField);
 
         return createRow(new JLabel(Messages.getMessage(Messages.ENTRYVIEW_LABEL_TITLE)), titleField,
                 createStandardButtons(titleField, new ValueProvider() {
                         @Override
                         public String getValue() {
                             return entry.getTitle();
                         }
 
                         @Override
                         public void setValue(final Entry entry, final String value) {
                             entry.setTitle(value);
                         }
                     }));
     }
 
     private JPanel createUrlRow() {
 
         return createRow(new JLabel(Messages.getMessage(Messages.ENTRYVIEW_LABEL_URL)), urlField,
                 createStandardButtons(urlField, new ValueProvider() {
                         @Override
                         public String getValue() {
                             return entry.getUrl();
                         }
 
                         @Override
                         public void setValue(final Entry entry, final String value) {
                             entry.setUrl(value);
                         }
                     }));
     }
 
     private JPanel createCommentRow() {
 
         return createRow(new JLabel(Messages.getMessage(Messages.ENTRYVIEW_LABEL_COMMENT)), commentsField,
                 createStandardButtons(commentsField, new ValueProvider() {
                         @Override
                         public String getValue() {
                             return entry.getComment();
                         }
 
                         @Override
                         public void setValue(final Entry entry, final String value) {
                             entry.setComment(value);
                         }
                     }));
     }
 
     private JPanel createUsernameRow() {
 
         initializeClipboardFunctions(usernameField);
 
         return createRow(new JLabel(Messages.getMessage(Messages.ENTRYVIEW_LABEL_USERNAME)), usernameField,
                 createSecretButtons(usernameField, userInvisible, userVisible));
     }
 
     private JPanel createPasswordRow() {
 
         initializeClipboardFunctions(passwordField);
 
         return createRow(new JLabel(Messages.getMessage(Messages.ENTRYVIEW_LABEL_PASSWORD)), passwordField,
                 createSecretButtons(passwordField, passInvisible, passVisible));
     }
 
     private void initializeClipboardFunctions(final JComponent textField) {
         JPopupMenu componentPopupMenu = textField.getComponentPopupMenu();
         if (componentPopupMenu == null) {
             componentPopupMenu = new JPopupMenu(Messages.getMessage(Messages.ENTRYVIEW_MENU_TITLE));
             textField.setComponentPopupMenu(componentPopupMenu);
         }
 
         componentPopupMenu.add(new ClipboardMenu(textField));
     }
 
     public void apply(final Entry entry) {
         if (entry != null) {
             reset();
             this.entry = entry;
             titleField.setText(entry.getTitle());
             urlField.setText(entry.getUrl());
             commentsField.setText(entry.getComment());
 
             try {
                 usernameField.setPassword(controller.getPasswordHandler().decrypt(entry.getUsername()));
                 passwordField.setPassword(controller.getPasswordHandler().decrypt(entry.getPassword()));
             } catch (PassSafeSecurityException e) {
                 LOGGER.error("Unable to set password to password field", e);
                 UiUtils.displayError(null, "TODO");
             }
         }
     }
 
     public void reset() {
         titleField.setText("");
         urlField.setText("");
         commentsField.setText("");
 
         usernameField.setPassword(null);
         passwordField.setPassword(null);
 
         usernameField.hidePassword();
         passwordField.hidePassword();
 
         passInvisible.setVisible(false);
         passVisible.setVisible(true);
         userInvisible.setVisible(false);
         userVisible.setVisible(true);
     }
 
     private JPanel createStandardButtons(final JTextComponent textComponent, final ValueProvider valueProvider) {
         final JPanel panel = new JPanel();
         panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
 
         final JButton edit = WidgetFactory.createImageButton(Images.ENTRYVIEW_BUTTON_EDIT);
         final JButton save = WidgetFactory.createImageButton(Images.ENTRYVIEW_BUTTON_SAVE);
         final JButton cancel = WidgetFactory.createImageButton(Images.ENTRYVIEW_BUTTON_CANCEL);
 
         edit.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(final ActionEvent e) {
                     textComponent.setEditable(true);
                     edit.setEnabled(false);
                     save.setEnabled(true);
                     cancel.setEnabled(true);
                 }
             });
 
         save.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(final ActionEvent e) {
                     valueProvider.setValue(entry, textComponent.getText());
 
                     EventBus.getInstance().fire(new Event() {
                             @Override
                             public EventType getType() {
                                 return EventType.MODIFY_ENTRY;
                             }
 
                             @Override
                             public Object getData() {
                                 return entry;
                             }
                         });
 
                     textComponent.setEditable(false);
                     save.setEnabled(false);
                     cancel.setEnabled(false);
                     edit.setEnabled(true);
                 }
             });
 
         cancel.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(final ActionEvent e) {
                     textComponent.setEditable(false);
                     save.setEnabled(false);
                     cancel.setEnabled(false);
                     edit.setEnabled(true);
 
                     if (entry != null) {
                         textComponent.setText(valueProvider.getValue());
                     }
                 }
             });
 
         save.setEnabled(false);
         cancel.setEnabled(false);
 
         panel.add(edit);
         panel.add(save);
         panel.add(cancel);
 
         return panel;
     }
 
     private JPanel createSecretButtons(final SwitchablePasswordField secretField, final JButton hide,
             final JButton show) {
 
         final JPanel panel = new JPanel();
         panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
 
         final JButton edit = WidgetFactory.createImageButton(Images.ENTRYVIEW_BUTTON_EDIT);
         final JButton save = WidgetFactory.createImageButton(Images.ENTRYVIEW_BUTTON_SAVE);
         final JButton cancel = WidgetFactory.createImageButton(Images.ENTRYVIEW_BUTTON_CANCEL);
 
         edit.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(final ActionEvent e) {
                     secretField.setEditable(true);
                     edit.setEnabled(false);
                     save.setEnabled(true);
                     cancel.setEnabled(true);
                 }
             });
 
         save.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(final ActionEvent e) {
 
                     EventBus.getInstance().fire(new Event() {
                             @Override
                             public EventType getType() {
                                 return EventType.MODIFY_ENTRY;
                             }
 
                             @Override
                             public Object getData() {
                                 return entry;
                             }
                         });
 
                     secretField.setEditable(false);
                     save.setEnabled(false);
                     cancel.setEnabled(false);
                     edit.setEnabled(true);
                 }
             });
 
         cancel.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(final ActionEvent e) {
                     secretField.setEditable(false);
                     save.setEnabled(false);
                     cancel.setEnabled(false);
                     edit.setEnabled(true);
                 }
             });
 
         show.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(final ActionEvent e) {
                     secretField.showPassword();
                     hide.setVisible(true);
                     show.setVisible(false);
                 }
             });
 
         hide.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(final ActionEvent e) {
                     secretField.hidePassword();
                     hide.setVisible(false);
                     show.setVisible(true);
                 }
             });
 
         save.setEnabled(false);
         cancel.setEnabled(false);
         hide.setVisible(false);
 
         panel.add(show);
         panel.add(hide);
         panel.add(edit);
         panel.add(save);
         panel.add(cancel);
 
         return panel;
     }
 }
