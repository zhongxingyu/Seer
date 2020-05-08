 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.abf.utilities.windows;
 
 import org.apache.log4j.Logger;
 
 import java.awt.Cursor;
 import java.awt.Desktop;
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.awt.datatransfer.StringSelection;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 import java.lang.reflect.Method;
 
 import java.net.URI;
 
 import javax.swing.ImageIcon;
 import javax.swing.JMenuItem;
 import javax.swing.JPopupMenu;
 
 /**
  * DOCUMENT ME!
  *
  * @author   mscholl
  * @version  $Revision$, $Date$
  */
 public class ErrorPanel extends javax.swing.JPanel {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final transient Logger LOG = Logger.getLogger(ErrorPanel.class);
 
     //~ Instance fields --------------------------------------------------------
 
     private final transient Throwable error;
     private final transient String message;
     private final transient Image errorImage;
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private final transient javax.swing.JScrollPane jScrollPane1 = new javax.swing.JScrollPane();
     private final transient javax.swing.JScrollPane jScrollPane2 = new javax.swing.JScrollPane();
     private final transient javax.swing.JLabel lblError = new javax.swing.JLabel();
     private final transient javax.swing.JLabel lblImage = new javax.swing.JLabel();
     private final transient javax.swing.JLabel lblLink = new javax.swing.JLabel();
     private final transient javax.swing.JPanel pnlError = new javax.swing.JPanel();
     private final transient javax.swing.JPanel pnlMessage = new javax.swing.JPanel();
     private final transient javax.swing.JTabbedPane tbpErrorMessage = new javax.swing.JTabbedPane();
     private final transient javax.swing.JTextArea txaMessage = new javax.swing.JTextArea();
     // End of variables declaration//GEN-END:variables
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new ErrorPanel object.
      *
      * @param  message     DOCUMENT ME!
      * @param  error       DOCUMENT ME!
      * @param  errorImage  DOCUMENT ME!
      */
     public ErrorPanel(final String message, final Throwable error, final Image errorImage) {
         this.message = message;
         this.error = error;
         this.errorImage = errorImage;
         initComponents();
         init();
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      */
     private void init() {
         tbpErrorMessage.setTitleAt(
             0,
             org.openide.util.NbBundle.getMessage(ErrorPanel.class, "ErrorPanel.tbpErrorMessage.titleAt0")); // NOI18N
         tbpErrorMessage.setTitleAt(
             1,
             org.openide.util.NbBundle.getMessage(ErrorPanel.class, "ErrorPanel.tbpErrorMessage.titleAt1")); // NOI18N
         txaMessage.setEditable(false);
         txaMessage.setLineWrap(true);
         txaMessage.setText(message);
         lblLink.setText(org.openide.util.NbBundle.getMessage(ErrorPanel.class, "ErrorPanel.lblLink.text")); // NOI18N
         lblLink.addMouseListener(new LinkAdapter());
         final StringBuffer stacktrace = new StringBuffer();
         for (final StackTraceElement ste : error.getStackTrace()) {
             stacktrace.append(ste.toString()).append("<br>");                                               // NOI18N
         }
         lblError.setText(
             "<html><font color=#FF0000>"                                                                    // NOI18N
                     + error.toString()
                     + "</font><br><br><b>STACKTRACE:</b><br>"                                               // NOI18N
                     + stacktrace.toString()
                     + "</html>");                                                                           // NOI18N
         lblError.addMouseListener(
             new MouseAdapter() {
 
                 @Override
                 public void mousePressed(final MouseEvent e) {
                     maybePopup(e);
                 }
 
                 @Override
                 public void mouseReleased(final MouseEvent e) {
                     maybePopup(e);
                 }
 
                 private void maybePopup(final MouseEvent e) {
                     if (e.isPopupTrigger()) {
                         final JPopupMenu popup = new JPopupMenu();
                         final JMenuItem item = new JMenuItem(
                                 org.openide.util.NbBundle.getMessage(
                                     ErrorPanel.class,
                                     "ErrorPanel.lblError.MouseAdapter.maybePopup(MouseEvent).item.text")); // NOI18N
                         item.addActionListener(
                             new ActionListener() {
 
                                 @Override
                                 public void actionPerformed(final ActionEvent e) {
                                     transferErrorMessage();
                                 }
                             });
                         popup.add(item);
                         popup.show(e.getComponent(), e.getX(), e.getY());
                     }
                 }
             });
         lblImage.setIcon(new ImageIcon(errorImage));
     }
 
     /**
      * DOCUMENT ME!
      */
     private void transferErrorMessage() {
         String errorMessage = lblError.getText();
         errorMessage = errorMessage.replaceAll("\\<br>", System.getProperty("line.separator")); // NOI18N
         errorMessage = errorMessage.replaceAll("\\<.*?>", "");                                  // NOI18N
         final StringSelection sel = new StringSelection(errorMessage);
         Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, sel);
     }
 
     /**
      * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
      * content of this method is always regenerated by the Form Editor.
      */
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
         setMaximumSize(new java.awt.Dimension(300, 300));
 
         pnlMessage.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));
 
         txaMessage.setColumns(20);
         txaMessage.setRows(5);
         jScrollPane2.setViewportView(txaMessage);
 
         lblLink.setText("jLabel1");
 
         final org.jdesktop.layout.GroupLayout pnlMessageLayout = new org.jdesktop.layout.GroupLayout(pnlMessage);
         pnlMessage.setLayout(pnlMessageLayout);
         pnlMessageLayout.setHorizontalGroup(
             pnlMessageLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                 pnlMessageLayout.createSequentialGroup().add(lblLink).addContainerGap()).add(
                 jScrollPane2,
                 org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                 429,
                 Short.MAX_VALUE));
         pnlMessageLayout.setVerticalGroup(
             pnlMessageLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                 org.jdesktop.layout.GroupLayout.TRAILING,
                 pnlMessageLayout.createSequentialGroup().add(
                     jScrollPane2,
                     org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                     148,
                     Short.MAX_VALUE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(lblLink)));
 
         tbpErrorMessage.addTab("tab1", pnlMessage);
 
         lblError.setText("jLabel1");
         lblError.setVerticalAlignment(javax.swing.SwingConstants.TOP);
         jScrollPane1.setViewportView(lblError);
 
         final org.jdesktop.layout.GroupLayout pnlErrorLayout = new org.jdesktop.layout.GroupLayout(pnlError);
         pnlError.setLayout(pnlErrorLayout);
         pnlErrorLayout.setHorizontalGroup(
             pnlErrorLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                 jScrollPane1,
                 org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                 429,
                 Short.MAX_VALUE));
         pnlErrorLayout.setVerticalGroup(
             pnlErrorLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                 jScrollPane1,
                 org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                 172,
                 Short.MAX_VALUE));
 
         tbpErrorMessage.addTab("tab2", pnlError);
 
         final org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                 layout.createSequentialGroup().addContainerGap().add(
                     lblImage,
                     org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                     79,
                     org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(
                     org.jdesktop.layout.LayoutStyle.RELATED).add(
                     tbpErrorMessage,
                     org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                     450,
                     Short.MAX_VALUE).addContainerGap()));
         layout.setVerticalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                 layout.createSequentialGroup().addContainerGap().add(
                     layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                         tbpErrorMessage,
                         org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                         218,
                         Short.MAX_VALUE).add(
                         layout.createSequentialGroup().add(47, 47, 47).add(
                             lblImage,
                             org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                             126,
                             org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(45, 45, 45)))));
     } // </editor-fold>//GEN-END:initComponents
 
     //~ Inner Classes ----------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private final class LinkAdapter extends MouseAdapter {
 
         //~ Methods ------------------------------------------------------------
 
         // TODO: use Desktop API when switching to Java 1.6
         @Override
         public void mouseClicked(final MouseEvent me) {
             final String url = org.openide.util.NbBundle.getMessage(
                     ErrorPanel.class,
                    "ErrorPanel.LinkAdapter.mouseClicked(MouseEvent).url");                                    // NOI18N
            final String osName = System.getProperty("os.name");                                               // NOI18N
             try {
                 if (Desktop.isDesktopSupported()) {
                     Desktop.getDesktop().browse(new URI(url));
                } else if (osName.startsWith("Mac OS"))                                                        // NOI18N
                 {
                    final Class fileMgr = Class.forName("com.apple.eio.FileManager");                          // NOI18N
                    final Method openURL = fileMgr.getDeclaredMethod("openURL", new Class[] { String.class }); // NOI18N
                     openURL.invoke(null, new Object[] { url });
                } else if (osName.startsWith("Windows"))                                                       // NOI18N
                 {
                    Runtime.getRuntime().exec("rundll32 url.dll, FileProtocolHandler " + url);                 // NOI18N
                 } else {
                     // assume Unix or Linux
                     final String[] browsers = {
                             "firefox",                                             // NOI18N
                             "opera",                                               // NOI18N
                             "konqueror",                                           // NOI18N
                             "epiphany",                                            // NOI18N
                             "mozilla",                                             // NOI18N
                             "netscape"                                             // NOI18N
                         };
                     String browser = null;
                     final String[] command = new String[] { "which", "" };         // NOI18N
                     for (int count = 0; (count < browsers.length) && (browser == null); ++count) {
                         command[1] = browsers[count];
                         if (Runtime.getRuntime().exec(command).waitFor() == 0) {
                             browser = browsers[count];
                             break;
                         }
                     }
                     if (browser == null) {
                         throw new IllegalStateException("could not find browser"); // NOI18N
                     } else {
                         Runtime.getRuntime().exec(new String[] { browser, url });
                     }
                 }
             } catch (final Exception e) {
                 LOG.error("could not open url: " + url, e);                        // NOI18N
             }
         }
 
         @Override
         public void mouseExited(final MouseEvent e) {
             setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
         }
 
         @Override
         public void mouseEntered(final MouseEvent e) {
             setCursor(new Cursor(Cursor.HAND_CURSOR));
         }
     }
 }
