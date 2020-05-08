 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 /*
  * DownloadManagerDialog.java
  *
  * Created on 13.05.2011, 13:33:00
  */
 package de.cismet.tools.gui.downloadmanager;
 
 import org.apache.log4j.Logger;
 
 import java.awt.Dimension;
 import java.awt.Frame;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 
 import java.io.File;
 
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.swing.text.AbstractDocument;
 import javax.swing.text.AttributeSet;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.DocumentFilter;
 
 import de.cismet.tools.BrowserLauncher;
 
 import de.cismet.tools.gui.downloadmanager.Download.State;
 
 /**
  * This dialog contains a DownloadManagerPanel to visualise the list of current downloads. Additionally there are
  * controls to open the destination directory and to clear the download list.
  *
  * @author   jweintraut
  * @version  $Revision$, $Date$
  */
 public class DownloadManagerDialog extends javax.swing.JDialog implements WindowListener, DownloadListChangedListener {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static Logger LOG = Logger.getLogger(DownloadManagerDialog.class);
 
     private static DownloadManagerDialog instance;
    private static boolean askForJobname = true;
     private static boolean openAutomatically = true;
     private static boolean closeAutomatically = true;
     private static String jobname = "";
 
     //~ Instance fields --------------------------------------------------------
 
     private boolean isJobnameConfirmed = true;
 
     private Collection<Download> downloadsToOpen = new LinkedList<Download>();
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton btnCancel;
     private javax.swing.JButton btnClearList;
     private javax.swing.JButton btnOK;
     private javax.swing.JDialog dlgJobname;
     private javax.swing.Box.Filler filler1;
     private javax.swing.JLabel lblDestinationDirectory;
     private javax.swing.JLabel lblDownloadsTotalKey;
     private javax.swing.JLabel lblDownloadsTotalValue;
     private javax.swing.JLabel lblJobname;
     private javax.swing.JLabel lblUserDirectory;
     private javax.swing.JLabel lblUserDirectoryLabel;
     private javax.swing.JPanel pnlControls;
     private de.cismet.tools.gui.downloadmanager.DownloadManagerPanel pnlDownloadManagerPanel;
     private javax.swing.JPanel pnlJobnameControls;
     private javax.swing.JScrollPane scpDownloadManagerPanel;
     private javax.swing.JSeparator sepControls;
     private javax.swing.JSeparator sepJobnameControls;
     private javax.swing.JTextField txtJobname;
     private org.jdesktop.beansbinding.BindingGroup bindingGroup;
     // End of variables declaration//GEN-END:variables
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates new form DownloadManagerDialog.
      *
      * @param  parent  The parent frame.
      */
     private DownloadManagerDialog(final java.awt.Frame parent) {
         super(parent, false);
 
         initComponents();
 
         bindingGroup.unbind();
         if (txtJobname.getDocument() instanceof AbstractDocument) {
             ((AbstractDocument)txtJobname.getDocument()).setDocumentFilter(new UserDirectoryFilter());
         }
         bindingGroup.bind();
 
         DownloadManager.instance().addDownloadListChangedListener(pnlDownloadManagerPanel);
         final List<Download> reversedDownloads = new LinkedList<Download>();
         for (final Download download : DownloadManager.instance().getDownloads()) {
             reversedDownloads.add(0, download);
         }
         pnlDownloadManagerPanel.add(reversedDownloads);
 
         final int countDownloadsErroneous = DownloadManager.instance().getCountDownloadsErroneous();
         final int countDownloadsCompleted = DownloadManager.instance().getCountDownloadsCompleted();
         final int countDownloadsTotal = DownloadManager.instance().getCountDownloadsTotal();
 
         lblDownloadsTotalValue.setText(String.valueOf(countDownloadsTotal));
         btnClearList.setEnabled((countDownloadsCompleted + countDownloadsErroneous) > 0);
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * Creates or returns the singleton object.
      *
      * @param   parent  The parent frame of this dialog.
      *
      * @return  The singleton instance.
      */
     public static DownloadManagerDialog instance(final Frame parent) {
         if (instance == null) {
             instance = new DownloadManagerDialog(parent);
             DownloadManager.instance().addDownloadListChangedListener(instance);
 
             instance.addWindowListener(instance);
             instance.setPreferredSize(new Dimension(450, 500));
             instance.setLocationRelativeTo(parent);
         }
 
         return instance;
     }
 
     /**
      * If no download manager dialog exists, this method will instantiate and display a download manager dialog.
      *
      * @param  parent  The parent frame of this dialog.
      */
     public static void show(final Frame parent) {
         if (instance == null) {
             final DownloadManagerDialog dialog = instance(parent);
             dialog.setVisible(true);
             dialog.pack();
         }
     }
 
     /**
      * Displays the DownloadManagerDialog and asks - if user wants to - for a jobname.
      *
      * @param   parent  The parent frame.
      *
      * @return  The jobname specified by the user.
      */
     public static boolean showAskingForUserTitle(final Frame parent) {
         final boolean close = (instance == null);
         show(parent);
         boolean result = instance.isJobnameConfirmed;
 
         if (askForJobname) {
             instance.txtJobname.setText(jobname);
             instance.txtJobname.setCaretPosition(jobname.length());
             instance.dlgJobname.setPreferredSize(instance.dlgJobname.getMinimumSize());
             instance.dlgJobname.setLocationRelativeTo(parent);
             instance.dlgJobname.pack();
             instance.dlgJobname.setVisible(true);
 
             result = instance.isJobnameConfirmed;
 
             if (!instance.isJobnameConfirmed && close) {
                 close();
             }
         }
 
         return result;
     }
 
     /**
      * Closes the DownloadManagerDialog.
      */
     public static void close() {
         instance.dispatchEvent(new WindowEvent(instance, WindowEvent.WINDOW_CLOSING));
     }
 
     /**
      * Returns the jobname.
      *
      * @return  The jobname.
      */
     public static String getJobname() {
         return jobname;
     }
 
     /**
      * Sets the jobname. Should only be used by xome configuring component.
      *
      * @param  jobname  The jobname.
      */
     public static void setJobname(final String jobname) {
         DownloadManagerDialog.jobname = jobname;
     }
 
     /**
      * If the user wants to be asked for a jobname.
      *
      * @return  A flag indicating whether the user wants to be asked for a jobname.
      */
     public static boolean isAskForJobname() {
         return askForJobname;
     }
 
     /**
      * Sets the flag whether the user wants to be asked for a jobname.
      *
      * @param  askForJobname  The flag whether the user wants to be asked for a jobname.
      */
     public static void setAskForJobname(final boolean askForJobname) {
         DownloadManagerDialog.askForJobname = askForJobname;
     }
 
     /**
      * If the user wants the last download to be opened automatically.
      *
      * @return  A flag indicating whether the user wants the last download to be opened automatically.
      */
     public static boolean isOpenAutomatically() {
         return openAutomatically;
     }
 
     /**
      * Sets the flag whether the user wants the last download to be opened automatically.
      *
      * @param  openAutomatically  The flag whether the user wants the last download to be opened automatically.
      */
     public static void setOpenAutomatically(final boolean openAutomatically) {
         DownloadManagerDialog.openAutomatically = openAutomatically;
     }
 
     /**
      * If the user the DownloadManagerDialog to close automatically.
      *
      * @return  A flag indicating whether the user wants the DownloadManagerDialog to close automatically.
      */
     public static boolean isCloseAutomatically() {
         return closeAutomatically;
     }
 
     /**
      * Sets the flag whether the user wants the DownloadManagerDialog to close automatically.
      *
      * @param  closeAutomatically  The flag whether the user wants the DownloadManagerDialog to close automatically.
      */
     public static void setCloseAutomatically(final boolean closeAutomatically) {
         DownloadManagerDialog.closeAutomatically = closeAutomatically;
     }
 
     /**
      * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
      * content of this method is always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
         java.awt.GridBagConstraints gridBagConstraints;
         bindingGroup = new org.jdesktop.beansbinding.BindingGroup();
 
         dlgJobname = new javax.swing.JDialog();
         lblJobname = new javax.swing.JLabel();
         txtJobname = new javax.swing.JTextField();
         pnlJobnameControls = new javax.swing.JPanel();
         btnOK = new javax.swing.JButton();
         btnCancel = new javax.swing.JButton();
         sepJobnameControls = new javax.swing.JSeparator();
         lblDestinationDirectory = new javax.swing.JLabel();
         lblUserDirectory = new javax.swing.JLabel();
         lblUserDirectoryLabel = new javax.swing.JLabel();
         scpDownloadManagerPanel = new javax.swing.JScrollPane();
         pnlDownloadManagerPanel = new de.cismet.tools.gui.downloadmanager.DownloadManagerPanel();
         sepControls = new javax.swing.JSeparator();
         pnlControls = new javax.swing.JPanel();
         btnClearList = new javax.swing.JButton();
         lblDownloadsTotalKey = new javax.swing.JLabel();
         lblDownloadsTotalValue = new javax.swing.JLabel();
         filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0),
                 new java.awt.Dimension(0, 0),
                 new java.awt.Dimension(32767, 32767));
 
         dlgJobname.setTitle(org.openide.util.NbBundle.getMessage(
                 DownloadManagerDialog.class,
                 "DownloadManagerDialog.dlgJobname.title")); // NOI18N
         dlgJobname.setMinimumSize(new java.awt.Dimension(400, 180));
         dlgJobname.setModal(true);
         dlgJobname.getContentPane().setLayout(new java.awt.GridBagLayout());
 
         lblJobname.setText(org.openide.util.NbBundle.getMessage(
                 DownloadManagerDialog.class,
                 "DownloadManagerDialog.lblJobname.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
         dlgJobname.getContentPane().add(lblJobname, gridBagConstraints);
 
         txtJobname.setText(org.openide.util.NbBundle.getMessage(
                 DownloadManagerDialog.class,
                 "DownloadManagerDialog.txtJobname.text")); // NOI18N
         txtJobname.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     txtJobnameActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         dlgJobname.getContentPane().add(txtJobname, gridBagConstraints);
 
         pnlJobnameControls.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.TRAILING));
 
         btnOK.setText(org.openide.util.NbBundle.getMessage(
                 DownloadManagerDialog.class,
                 "DownloadManagerDialog.btnOK.text")); // NOI18N
         btnOK.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     btnOKActionPerformed(evt);
                 }
             });
         pnlJobnameControls.add(btnOK);
 
         btnCancel.setText(org.openide.util.NbBundle.getMessage(
                 DownloadManagerDialog.class,
                 "DownloadManagerDialog.btnCancel.text")); // NOI18N
         btnCancel.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     btnCancelActionPerformed(evt);
                 }
             });
         pnlJobnameControls.add(btnCancel);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 5;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_END;
         gridBagConstraints.weightx = 1.0;
         dlgJobname.getContentPane().add(pnlJobnameControls, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_END;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
         dlgJobname.getContentPane().add(sepJobnameControls, gridBagConstraints);
 
         lblDestinationDirectory.setText(DownloadManager.instance().getDestinationDirectory().getAbsolutePath()
                     + File.separator);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
         dlgJobname.getContentPane().add(lblDestinationDirectory, gridBagConstraints);
 
         final org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ,
                 txtJobname,
                 org.jdesktop.beansbinding.ELProperty.create("${text}"),
                 lblUserDirectory,
                 org.jdesktop.beansbinding.BeanProperty.create("text"));
         binding.setSourceNullValue("Nothing entered");
         binding.setSourceUnreadableValue("Unreadable");
         bindingGroup.addBinding(binding);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
         dlgJobname.getContentPane().add(lblUserDirectory, gridBagConstraints);
 
         lblUserDirectoryLabel.setText(org.openide.util.NbBundle.getMessage(
                 DownloadManagerDialog.class,
                 "DownloadManagerDialog.lblUserDirectoryLabel.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         dlgJobname.getContentPane().add(lblUserDirectoryLabel, gridBagConstraints);
 
         setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
         setTitle(org.openide.util.NbBundle.getMessage(DownloadManagerDialog.class, "DownloadManagerDialog.title")); // NOI18N
         setMinimumSize(new java.awt.Dimension(423, 300));
         getContentPane().setLayout(new java.awt.GridBagLayout());
 
         scpDownloadManagerPanel.setBorder(null);
         scpDownloadManagerPanel.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
         scpDownloadManagerPanel.setViewportView(pnlDownloadManagerPanel);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weighty = 1.0;
         getContentPane().add(scpDownloadManagerPanel, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 1.0;
         getContentPane().add(sepControls, gridBagConstraints);
 
         pnlControls.setLayout(new java.awt.GridBagLayout());
 
         btnClearList.setMnemonic(org.openide.util.NbBundle.getMessage(
                 DownloadManagerDialog.class,
                 "DownloadManagerDialog.btnClearList.mnemonic").charAt(0));
         btnClearList.setText(org.openide.util.NbBundle.getMessage(
                 DownloadManagerDialog.class,
                 "DownloadManagerDialog.btnClearList.text")); // NOI18N
         btnClearList.setEnabled(false);
         btnClearList.setFocusPainted(false);
         btnClearList.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     btnClearListActionPerformed(evt);
                 }
             });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         pnlControls.add(btnClearList, gridBagConstraints);
 
         lblDownloadsTotalKey.setText(org.openide.util.NbBundle.getMessage(
                 DownloadManagerDialog.class,
                 "DownloadManagerDialog.lblDownloadsTotalKey.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         pnlControls.add(lblDownloadsTotalKey, gridBagConstraints);
 
         lblDownloadsTotalValue.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
         lblDownloadsTotalValue.setText(org.openide.util.NbBundle.getMessage(
                 DownloadManagerDialog.class,
                 "DownloadManagerDialog.lblDownloadsTotalValue.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 3;
         gridBagConstraints.ipadx = 20;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         pnlControls.add(lblDownloadsTotalValue, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 1.0;
         pnlControls.add(filler1, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         getContentPane().add(pnlControls, gridBagConstraints);
 
         bindingGroup.bind();
 
         pack();
     } // </editor-fold>//GEN-END:initComponents
 
     /**
      * Notifies the DownloadManager singleton that all obsolete downloads should be removed.
      *
      * @param  evt  The event object.
      */
    private void btnClearListActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnClearListActionPerformed
         DownloadManager.instance().removeObsoleteDownloads();
    }                                                                                //GEN-LAST:event_btnClearListActionPerformed
 
     /**
      * An action listener.
      *
      * @param  evt  The event.
      */
     /**
      * An action listener.
      *
      * @param  evt  The event.
      */
    private void btnOKActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnOKActionPerformed
         jobname = txtJobname.getText();
         isJobnameConfirmed = true;
         dlgJobname.dispose();
    }                                                                         //GEN-LAST:event_btnOKActionPerformed
 
     /**
      * An action listener.
      *
      * @param  evt  The event.
      */
    private void txtJobnameActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_txtJobnameActionPerformed
         jobname = txtJobname.getText();
         isJobnameConfirmed = true;
         dlgJobname.dispose();
    }                                                                              //GEN-LAST:event_txtJobnameActionPerformed
 
     /**
      * An action listener.
      *
      * @param  evt  The event.
      */
    private void btnCancelActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnCancelActionPerformed
         isJobnameConfirmed = false;
         dlgJobname.dispose();
    }                                                                             //GEN-LAST:event_btnCancelActionPerformed
 
     /**
      * An action listener.
      *
      * @param  event  The event.
      */
     /**
      * Opens a file manager pointing to the destination directory for downloads.
      *
      * @param  event  The event object.
      */
     @Override
     public void downloadListChanged(final DownloadListChangedEvent event) {
         if (event == null) {
             return;
         }
 
         switch (event.getAction()) {
             case ADDED: {
                 final Collection<Download> downloads = event.getDownloads();
                 if (openAutomatically) {
                     downloadsToOpen.addAll(downloads);
                 }
             }
             case CHANGED_COUNTERS: {
                 final int countDownloadsErraneous = DownloadManager.instance().getCountDownloadsErroneous();
                 final int countDownloadsCompleted = DownloadManager.instance().getCountDownloadsCompleted();
                 final int countDownloadsTotal = DownloadManager.instance().getCountDownloadsTotal();
 
                 lblDownloadsTotalValue.setText(String.valueOf(countDownloadsTotal));
                 btnClearList.setEnabled((countDownloadsCompleted + countDownloadsErraneous) > 0);
 
                 if (openAutomatically) {
                     final Iterator<Download> downloadToOpenIter = downloadsToOpen.iterator();
                     while (downloadToOpenIter.hasNext()) {
                         final Download downloadToOpen = downloadToOpenIter.next();
 
                         if ((downloadToOpen != null)
                                     && (downloadToOpen.getFileToSaveTo() != null)
                                     && (downloadToOpen.getStatus() == State.COMPLETED)) {
                             BrowserLauncher.openURLorFile(downloadToOpen.getFileToSaveTo().getAbsolutePath());
                             downloadToOpenIter.remove();
                         }
                     }
                 }
 
                 // The second condition ensures that the dialog isn't closed after the list was cleared
                 if (closeAutomatically && (countDownloadsTotal > 0)
                             && (countDownloadsTotal == countDownloadsCompleted)) {
                     close();
                 }
             }
         }
     }
 
     @Override
     public void windowOpened(final WindowEvent e) {
     }
 
     @Override
     public void windowClosing(final WindowEvent e) {
         removeWindowListener(instance);
         DownloadManager.instance().removeDownloadListChangedListener(pnlDownloadManagerPanel);
         DownloadManager.instance().removeDownloadListChangedListener(instance);
         instance = null;
     }
 
     @Override
     public void windowClosed(final WindowEvent e) {
     }
 
     @Override
     public void windowIconified(final WindowEvent e) {
     }
 
     @Override
     public void windowDeiconified(final WindowEvent e) {
     }
 
     @Override
     public void windowActivated(final WindowEvent e) {
     }
 
     @Override
     public void windowDeactivated(final WindowEvent e) {
     }
 
     //J-
     private class UserDirectoryFilter extends DocumentFilter {
         /*
          * Most filesystems allow all characters (except NUL) for filenames.
          * NTFS doesn't allow NUL \ / : * ? " < > |
          */
         private static final String FILTER = "[^\u0000\\\\/:*?\"<>|]*";
 
         @Override
         public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
             if(string != null && string.length() > 0) {
                 if(!string.matches(FILTER)) {
                     return;
                 }
             }
 
             super.insertString(fb, offset, string, attr);
         }
 
         @Override
         public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
             if(text != null && text.length() > 0) {
                 if(!text.matches(FILTER)) {
                     return;
                 }
             }
 
             super.replace(fb, offset, length, text, attrs);
         }
 
     }
     //J+
 }
