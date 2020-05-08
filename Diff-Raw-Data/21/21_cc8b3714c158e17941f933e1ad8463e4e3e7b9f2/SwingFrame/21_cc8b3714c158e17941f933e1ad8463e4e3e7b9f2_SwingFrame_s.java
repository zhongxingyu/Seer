 /*
  * LICENSE
  *
  * "THE BEER-WARE LICENSE" (Revision 42):
  * "Sven Strittmatter" <weltraumschaf@googlemail.com> wrote this file.
  * As long as you retain this notice you can do whatever you want with
  * this stuff. If we meet some day, and you think this stuff is worth it,
  * you can buy me a beer in return.
  *
  */
 
 package de.weltraumschaf.commons.swing;
 
 import de.weltraumschaf.commons.system.DefaultExiter;
 import de.weltraumschaf.commons.system.Exitable;
 import java.awt.BorderLayout;
 import java.awt.HeadlessException;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 
 /**
  * Extension of {@link javax.swing.JFrame} with {@link java.awt.BorderLayout} for easier use.
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 public class SwingFrame extends JFrame {
 
     /**
      * Logging facility.
      */
     private static final Logger LOGGER = Logger.getLogger(SwingFrame.class.getName());
 
     /**
      * Centered panel for the main UI.
      */
     protected final JPanel panel = new JPanel();
 
     /**
      * Whether to exit the whole application on closing window.
      */
     protected boolean exitOnCloseWindow;
 
     /**
      * Abstraction for {@link System#exit(int)}.
      */
     private transient Exitable exiter = new DefaultExiter();
 
     /**
      * Initializes the content pane layout with {@link java.awt.BorderLayout} and add
      * {@link #panel} to the center.
      *
      * @param title the title for the frame
      * @exception HeadlessException if GraphicsEnvironment.isHeadless() returns true.
      */
     public SwingFrame(final String title) throws HeadlessException {
         super(title);
         getContentPane().setLayout(new BorderLayout());
         getContentPane().add(panel, BorderLayout.CENTER);
     }
 
     /**
      * Call this method to initializes the frame before displaying it.
      */
     public final void init() {
         initLookAndFeel();
 
         if (exitOnCloseWindow) {
             bindWindowClosing();
         }
 
         initMenu();
         initToolBar();
         pack();
     }
 
     /**
      * Whether to exit application on closing the window.
      *
      * By default {@link #exitOnCloseWindow} is false.
      *
      * @param exitOnCloseWindow True or false.
      */
     public void setExitOnCloseWindow(boolean exitOnCloseWindow) {
         this.exitOnCloseWindow = exitOnCloseWindow;
     }
 
     /**
      * Set an exiter to handle exit calls.
      *
      * @param exiter Object which handles exit calls.
      */
     public void setExiter(final Exitable exiter) {
         this.exiter = exiter;
     }
 
     /**
      * Returns true if the host OS is Mac OS X.
      *
      * @return true or false.
      */
    private boolean isMacOs() {
        // FIXME: Implement this method.
        return true;
     }
 
     /**
      * Initializes the look and feel.
      *
      * Uses the system look and feel. If the guest OS is Mac OS x the menu
      * bar is took off the frame.
      */
     protected void initLookAndFeel() {
         if (isMacOs()) {
             // take the menu bar off the jframe
             System.setProperty("apple.laf.useScreenMenuBar", "true");
         }
 
         try {
             UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         } catch (ClassNotFoundException ex) {
             LOGGER.log(Level.SEVERE, null, ex);
         } catch (InstantiationException ex) {
             LOGGER.log(Level.SEVERE, null, ex);
         } catch (IllegalAccessException ex) {
             LOGGER.log(Level.SEVERE, null, ex);
         } catch (UnsupportedLookAndFeelException ex) {
             LOGGER.log(Level.SEVERE, null, ex);
         }
     }
 
     /**
      * Binds on the window closing event and calls the exiter to exit application.
      */
     protected void bindWindowClosing() {
         addWindowListener(new WindowAdapter() {
 
             @Override
             public void windowClosing(WindowEvent e) {
                 exiter.exit(0);
             }
 
         });
     }
 
     /**
      * Template method for menu initialization.
      *
      * Override this method with your custom code, if you need a menu.
      */
     protected void initMenu() {
         // Empty template method.
     }
 
     /**
      * Template method for tool bar initialization.
      *
      * Override this method with your custom code, if you need a tool bar.
      */
     protected void initToolBar() {
         // Empty template method.
     }
 }
