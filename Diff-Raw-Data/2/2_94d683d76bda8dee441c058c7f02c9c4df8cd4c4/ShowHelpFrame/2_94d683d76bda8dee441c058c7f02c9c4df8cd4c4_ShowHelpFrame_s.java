 package de.ronnyfriedland.time.ui.dialog;
 
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.swing.JEditorPane;
 import javax.swing.JScrollPane;
 import javax.swing.event.HyperlinkEvent;
 import javax.swing.event.HyperlinkListener;
 
 import de.ronnyfriedland.time.config.Messages;
 import de.ronnyfriedland.time.ui.adapter.TimeTableKeyAdapter;
 
 /**
  * @author Ronny Friedland
  */
 public class ShowHelpFrame extends AbstractFrame implements HyperlinkListener {
 
     private static final Logger LOG = Logger.getLogger(ShowHelpFrame.class.getName());
     private static final String README_FILE = "timetable_readme.html";
     private static final long serialVersionUID = -3667564200048966812L;
     private final JEditorPane editorPane = new JEditorPane();
 
     public ShowHelpFrame() {
        super(Messages.HELP.getMessage(), 655, 450);
         createUI();
     }
 
     @Override
     protected void createUI() {
         editorPane.setContentType("text/html");
         try {
             editorPane.setPage(Thread.currentThread().getContextClassLoader().getResource(README_FILE));
         } catch (IOException e) {
             LOG.log(Level.WARNING, "Error loading help file", e);
         }
         editorPane.setEditable(false);
         editorPane.addHyperlinkListener(this);
         editorPane.addKeyListener(new TimeTableKeyAdapter());
 
         JScrollPane scrollPane = new JScrollPane(editorPane);
         scrollPane.setBounds(0, 0, 655, 435);
         getContentPane().add(scrollPane);
     }
 
     @Override
     public void hyperlinkUpdate(final HyperlinkEvent event) {
         if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
             try {
                 editorPane.setPage(event.getURL());
             } catch (IOException ioe) {
                 ioe.printStackTrace();
             }
         }
     }
 
     public static void main(final String[] args) {
         new ShowHelpFrame().setVisible(true);
     }
 
 }
