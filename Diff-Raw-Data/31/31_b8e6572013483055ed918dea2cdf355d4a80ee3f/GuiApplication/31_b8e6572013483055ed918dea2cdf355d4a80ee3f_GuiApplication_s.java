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
 
 package de.weltraumschaf.jebnf.cli;
 
 import de.weltraumschaf.commons.IOStreams;
 import de.weltraumschaf.jebnf.ast.visitor.RailroadDiagramVisitor;
 import de.weltraumschaf.jebnf.ast.visitor.Visitor;
 import de.weltraumschaf.jebnf.gfx.RailroadDiagram;
 import de.weltraumschaf.jebnf.gui.RailroadDiagramPanel;
import java.awt.Dimension;
 import javax.swing.JFrame;
 import javax.swing.SwingUtilities;
 
 /**
  * Runs the GUI IDE.
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 public class GuiApplication extends ApplicationAdapter implements Runnable {
 
     /**
      * Main desktop window.
      */
     private final JFrame frame = new JFrame();
 
     /**
      * Initializes app with options and IO streams.
      *
      * @param options Command line options
      * @param ioStreams IO streams.
      * @param invoker Invoked the invokable.
      */
     public GuiApplication(final CliOptions options, final IOStreams ioStreams, final Main invoker) {
         super(options, ioStreams, invoker);
     }
 
     /**
      * {@link SwingUtilities#invokeLater(java.lang.Runnable) "Invokes itself later"}.
      */
     @Override
     public void execute() {
         SwingUtilities.invokeLater(this);
     }
 
     @Override
     public void run() {
         frame.setTitle("Railroad");
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.setLocationRelativeTo(null);
         frame.setVisible(true);
 
         final Visitor<RailroadDiagram> visitor = new RailroadDiagramVisitor();
         syntax.accept(visitor);
 
         final RailroadDiagram diagram = visitor.getResult();
         diagram.setDebug(options.isDebug());
         final RailroadDiagramPanel railroadDiagramPanel = new RailroadDiagramPanel(diagram);
         frame.add(railroadDiagramPanel);
         frame.validate();
         frame.pack();
     }
 
 }
