 package de.iweinzierl.passsafe.gui.util;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Frame;
 import java.awt.Toolkit;
 import java.awt.Window;
 
 import javax.swing.JComponent;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 import javax.swing.border.LineBorder;
 
 import de.iweinzierl.passsafe.gui.resources.Messages;
 
 public class UiUtils {
 
     public static final int ERRORDIALOG_WIDTH = 350;
     public static final int ERRORDIALOG_HEIGHT = 125;
 
     private static final String ERROR_TEMPLATE = "<html><body style='width: %spx'>%s</body></html>";
 
     public static void center(final Component component) {
         Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
         int x = (int) ((dimension.getWidth() - component.getWidth()) / 2);
         int y = (int) ((dimension.getHeight() - component.getHeight()) / 2);
         component.setLocation(x, y);
     }
 
     public static Frame getOwner(final Component component) {
         Container parent = component.getParent();
         if (parent instanceof Frame) {
             return (Frame) parent;
         } else {
             return getOwner(parent);
         }
     }
 
     public static void displayError(final Window owner, final String text) {
         JLabel label = new JLabel(String.format(ERROR_TEMPLATE, 250, text));
         label.setPreferredSize(new Dimension(250, 100));
 
         JPanel panel = new JPanel();
         panel.setLayout(new BorderLayout());
         panel.add(label);
         panel.setBorder(new EmptyBorder(10, 10, 10, 10));
 
         JDialog dialog = new JDialog(owner);
         dialog.setContentPane(panel);
         dialog.setPreferredSize(new Dimension(ERRORDIALOG_WIDTH, ERRORDIALOG_HEIGHT));
         dialog.setMinimumSize(new Dimension(ERRORDIALOG_WIDTH, ERRORDIALOG_HEIGHT));
         dialog.setTitle(Messages.getMessage(Messages.ERROR_DIALOG_TITLE));
        dialog.setModal(true);
 
         center(dialog);
         dialog.show();
     }
 
     public static void markFieldAsInvalid(final JComponent component) {
         component.setBorder(new LineBorder(Color.RED));
     }
 }
