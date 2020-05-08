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
 
 package de.weltraumschaf.jebnf.gui;
 
 import de.weltraumschaf.commons.swing.MenuBarBuilder;
 import de.weltraumschaf.commons.swing.SwingFrame;
 import de.weltraumschaf.commons.swing.ToolBarBuilder;
 import java.awt.BorderLayout;
import java.awt.HeadlessException;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.JMenuBar;
 import javax.swing.JToolBar;
 
 /**
  * The applications main window.
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 public class MainWindow extends SwingFrame {
 
     /**
      * Logging facility.
      */
     private static final Logger LOG = Logger.getLogger(MainWindow.class.getName());
 
     /**
      * Id for serialization.
      */
     private static final long serialVersionUID = 1L;
 
    private RailroadDiagramPanel diagram;
 
     /**
      * Initializes the main window with an title.
      *
     * @param title
     * @throws HeadlessException
      */
    public MainWindow(final String title, final RailroadDiagramPanel diagram) throws HeadlessException {
         super(title);
         this.diagram = diagram;
     }
 
     @Override
     protected void initToolBar() {
         final JToolBar toolbar = ToolBarBuilder.builder()
             .button("/de/weltraumschaf/jebnf/gui/open_16x16.gif")
                 .toolTipText("Open an existing document.")
                 .addListener(new Listener())
             .end()
             .button("/de/weltraumschaf/jebnf/gui/save_16x16.gif")
                 .toolTipText("Save current document.")
                 .addListener(new Listener())
             .end()
             .button("/de/weltraumschaf/jebnf/gui/new_16x16.gif")
                 .toolTipText("Create a new document.")
                 .addListener(new Listener())
             .end()
             .create();
 
         toolbar.setFloatable(false);
         getContentPane().add(toolbar, BorderLayout.NORTH);
     }
 
     @Override
     protected void initMenu() {
         final JMenuBar menubar = MenuBarBuilder.builder()
             .menu("File")
                 .item("Open")
                     .addListener(new Listener())
                 .end()
                 .separator()
                 .item("Save")
                     .addListener(new Listener())
                 .end()
             .end()
             .menu("Edit")
                 .item("foo")
                 .end()
             .end()
             .menu("View")
                 .item("bar")
                 .end()
             .end()
             .menu("Window")
                 .item("baz")
                 .end()
             .end()
             .create();
 
         setJMenuBar(menubar);
     }
 
     @Override
     protected void initPanel() {
         panel.add(diagram);
     }
 
     /**
      * Default listener for playing around.
      *
      * @deprecated Will be removed soon!
      */
     @Deprecated private class Listener implements ActionListener {
 
         @Override
         public void actionPerformed(final ActionEvent e) {
             final Object source = e.getSource();
             LOG.log(Level.INFO, String.format("Received event from %s.", source.toString()));
         }
 
     }
 }
