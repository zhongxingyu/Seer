 //
 // $Id$
 // 
 // jupload - A file upload applet.
 // Copyright 2007 The JUpload Team
 // 
 // Created: ?
 // Creator: William JinHua Kwong
 // Last modified: $Date$
 //
 // This program is free software; you can redistribute it and/or modify it under
 // the terms of the GNU General Public License as published by the Free Software
 // Foundation; either version 2 of the License, or (at your option) any later
 // version. This program is distributed in the hope that it will be useful, but
 // WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 // FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 // details. You should have received a copy of the GNU General Public License
 // along with this program; if not, write to the Free Software Foundation, Inc.,
 // 675 Mass Ave, Cambridge, MA 02139, USA.
 
 package wjhk.jupload2.gui;
 
 import java.awt.Container;
 import java.awt.Frame;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.InputEvent;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.io.File;
 
 import javax.swing.BoxLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JProgressBar;
 import javax.swing.JScrollPane;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.SwingConstants;
 import javax.swing.Timer;
 
 import wjhk.jupload2.policies.UploadPolicy;
 import wjhk.jupload2.policies.UploadPolicyFactory;
 import wjhk.jupload2.upload.FileUploadThread;
 import wjhk.jupload2.upload.FileUploadThreadFTP;
 import wjhk.jupload2.upload.FileUploadThreadHTTP;
 
 /**
  * Overwriting the default Append class such that it scrolls to the bottom of
  * the text. The JFC doesn't always remember to do that. <BR>
  */
 
 final class JUploadPopupMenu extends JPopupMenu implements ActionListener,
         ItemListener {
 
     /** A generated serialVersionUID */
     private static final long serialVersionUID = -5473337111643079720L;
 
     /**
      * Identifies the menu item that will set debug mode on or off (on means:
      * debugLevel=100)
      */
     JCheckBoxMenuItem cbMenuItemDebugOnOff = null;
 
     /**
      * The current upload policy.
      */
     private UploadPolicy uploadPolicy;
 
     JUploadPopupMenu(UploadPolicy uploadPolicy) {
         this.uploadPolicy = uploadPolicy;
         // Creation of the menu items
         this.cbMenuItemDebugOnOff = new JCheckBoxMenuItem("Debug on");
         this.cbMenuItemDebugOnOff
                 .setState(this.uploadPolicy.getDebugLevel() == 100);
         add(this.cbMenuItemDebugOnOff);
         this.cbMenuItemDebugOnOff.addItemListener(this);
     }
 
     /**
      * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
      */
     public void actionPerformed(@SuppressWarnings("unused")
     ActionEvent action) {
         // Nothing to do.
     }
 
     /**
      * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
      */
     public void itemStateChanged(ItemEvent e) {
         if (this.cbMenuItemDebugOnOff == e.getItem()) {
             this.uploadPolicy.setDebugLevel((this.cbMenuItemDebugOnOff
                     .isSelected() ? 100 : 0));
         }
     }
 }
 
 /**
  * Main code for the applet (or frame) creation. It contains all creation for
  * necessary elements, or calls to {@link wjhk.jupload2.policies.UploadPolicy}
  * to allow easy personalization.
  * 
  * @author William JinHua Kwong
  * @version $Revision$
  */
 public class JUploadPanel extends JPanel implements ActionListener,
         MouseListener {
 
     /**
      * 
      */
     private static final long serialVersionUID = -1212601012568225757L;
 
     private static final double gB = 1024L * 1024L * 1024L;
 
     private static final double mB = 1024L * 1024L;
 
     private static final double kB = 1024L;
 
     // TODO: translation
     private String speedunit_gb_per_second = "Gb/s";
 
     // TODO: translation
     private String speedunit_mb_per_second = "Mb/s";
 
     // TODO: translation
     private String speedunit_kb_per_second = "Kb/s";
 
     // TODO: translation
     private String speedunit_b_per_second = "b/s";
 
     // TODO: translation
     private String timefmt_hms = "%1$dh, %2$d min. and %3$d sec.";
 
     // TODO: translation
     private String timefmt_ms = "%1$d min. and %2$d sec.";
 
     // TODO: translation
     private String timefmt_s = "%1$d seconds";
 
     // TODO: translation
     private String status_msg = "JUpload %1$d%% done, Transfer rate: %2$,3.2f %3$s, ETA: %4$s";
 
     /** The popup menu of the applet */
     private JUploadPopupMenu jUploadPopupMenu;
 
     // Timeout at DEFAULT_TIMEOUT milliseconds
     private final static int DEFAULT_TIMEOUT = 100;
 
     /**
      * The upload status (progressbar) gets updated every (DEFAULT_TIMEOUT *
      * PROGRESS_INTERVAL) ms.
      */
     private final static int PROGRESS_INTERVAL = 10;
 
     /**
      * The counter for updating the upload status. The upload status
      * (progressbar) gets updated every (DEFAULT_TIMEOUT * PROGRESS_INTERVAL)
      * ms.
      */
     private int update_counter = 0;
 
     // ------------- VARIABLES ----------------------------------------------
     private JPanel topPanel;
 
     private JButton browseButton, removeButton, removeAllButton;
 
     private JUploadFileChooser fileChooser = null;
 
     private FilePanel filePanel = null;
 
     private JPanel progressPanel;
 
     private JButton uploadButton, stopButton;
 
     private JProgressBar progressBar = null;
 
     private JScrollPane jLogWindowPane = null;
 
     private JLabel statusLabel = null;
 
     private JUploadTextArea logWindow = null;
 
     private boolean isLogWindowVisible = false;
 
     private Timer timer = null;
 
     private UploadPolicy uploadPolicy = null;
 
     protected FileUploadThread fileUploadThread = null;
 
     // ------------- CONSTRUCTOR --------------------------------------------
 
     /**
      * Constructor to call, when running from outside of an applet : this is
      * taken from the version 1 of Jupload. It can be used for test (like
      * originally), but this is now useless as eclipse allow direct execution of
      * applet, for tests. It can also be used to use this code from within a
      * java application.
      * 
      * @param containerParam The container, where all GUI elements are to be
      *            created.
      * @param logWindow The log window that should already have been created.
      *            This allows putting text into it, before the effective
      *            creation of the layout.
      * @param uploadPolicyParam The current UploadPolicy. Null if a new one must
      *            be created.
      * @see UploadPolicyFactory#getUploadPolicy(wjhk.jupload2.JUploadApplet)
      */
     public JUploadPanel(@SuppressWarnings("unused")
     Container containerParam, JUploadTextArea logWindow,
             UploadPolicy uploadPolicyParam) throws Exception {
         this.logWindow = logWindow;
         this.uploadPolicy = uploadPolicyParam;
         this.jUploadPopupMenu = new JUploadPopupMenu(this.uploadPolicy);
 
         this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
         logWindow.addMouseListener(this);
 
         // Setup Top Panel
         setupTopPanel();
 
         // Setup File Panel.
         this.filePanel = (null == this.filePanel) ? new FilePanelTableImp(this,
                 this.uploadPolicy) : this.filePanel;
         this.add((Container) this.filePanel);
 
         // Setup Progress Panel.
         setupProgressPanel(this.uploadButton, this.progressBar, this.stopButton);
 
         // Setup status bar
         setupStatusBar();
 
         // Setup logging window.
         setupLogWindow();
 
         // Setup File Chooser.
         try {
             this.fileChooser = new JUploadFileChooser(uploadPolicyParam);
         } catch (Exception e) {
             this.uploadPolicy.displayErr(e);
         }
     }
 
     // ----------------------------------------------------------------------
 
     private void setupTopPanel() {
 
         // -------- JButton browse --------
         this.browseButton = new JButton(this.uploadPolicy
                 .getString("buttonBrowse"));
         this.browseButton.setIcon(new ImageIcon(getClass().getResource(
                 "/images/explorer.gif")));
         this.browseButton.addActionListener(this);
 
         // -------- JButton remove --------
         this.removeButton = new JButton(this.uploadPolicy
                 .getString("buttonRemoveSelected"));
         this.removeButton.setIcon(new ImageIcon(getClass().getResource(
                 "/images/recycle.gif")));
         this.removeButton.setEnabled(false);
         this.removeButton.addActionListener(this);
 
         // -------- JButton removeAll --------
         this.removeAllButton = new JButton(this.uploadPolicy
                 .getString("buttonRemoveAll"));
         this.removeAllButton.setIcon(new ImageIcon(getClass().getResource(
                 "/images/cross.gif")));
         this.removeAllButton.setEnabled(false);
         this.removeAllButton.addActionListener(this);
 
         // ------- Then ask the current upload policy to create the top panel
         // --------//
         this.topPanel = this.uploadPolicy.createTopPanel(this.browseButton,
                 this.removeButton, this.removeAllButton, this);
         this.add(this.topPanel);
     }
 
     private void setupProgressPanel(JButton jbUpload, JProgressBar jpbProgress,
             JButton jbStop) {
         this.progressPanel = new JPanel();
 
         // -------- JButton upload --------
         if (null == jbUpload) {
             this.uploadButton = new JButton(this.uploadPolicy
                     .getString("buttonUpload"));
             this.uploadButton.setIcon(new ImageIcon(getClass().getResource(
                     "/images/up.gif")));
         } else {
             this.uploadButton = jbUpload;
         }
         this.uploadButton.setEnabled(false);
         this.uploadButton.addActionListener(this);
 
         // -------- JProgressBar progress --------
         if (null == jpbProgress) {
             this.progressBar = new JProgressBar(SwingConstants.HORIZONTAL);
             this.progressBar.setStringPainted(true);
         } else {
             this.progressBar = jpbProgress;
         }
 
         // -------- JButton stop --------
         if (null == jbStop) {
             this.stopButton = new JButton(this.uploadPolicy
                     .getString("buttonStop"));
             this.stopButton.setIcon(new ImageIcon(getClass().getResource(
                     "/images/cross.gif")));
         } else {
             this.stopButton = jbStop;
         }
         this.stopButton.setEnabled(false);
         this.stopButton.addActionListener(this);
 
         this.progressPanel = this.uploadPolicy.createProgressPanel(
                 this.progressBar, this.uploadButton, this.stopButton, this);
         this.add(this.progressPanel);
     }
 
     private void setupLogWindow() {
         this.jLogWindowPane = new JScrollPane();
         this.jLogWindowPane
                 .setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
         this.jLogWindowPane
                 .setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 
         this.jLogWindowPane.getViewport().add(this.logWindow);
 
         showOrHideLogWindow();
         // this.add(jLogWindowPane);
     }
 
     private void setupStatusBar() {
        this.statusLabel = new JLabel(" ");
         JPanel p = this.uploadPolicy.createStatusBar(this.statusLabel, this);
         if (null != p)
             this.add(p);
     }
 
     // ----------------------------------------------------------------------
     protected void addFiles(File[] f, File root) {
         this.filePanel.addFiles(f, root);
         if (0 < this.filePanel.getFilesLength()) {
             this.removeButton.setEnabled(true);
             this.removeAllButton.setEnabled(true);
             this.uploadButton.setEnabled(true);
         }
     }
 
     /**
      * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
      */
     public void actionPerformed(ActionEvent e) {
         if (e.getSource() instanceof Timer) {
             // timer is expired
             if ((this.update_counter++ > PROGRESS_INTERVAL)
                     || (!this.fileUploadThread.isAlive())) {
                 // Time for an update now.
                 this.update_counter = 0;
                 if (null != this.progressBar
                         && (this.fileUploadThread.getStartTime() != 0)) {
                     long duration = (System.currentTimeMillis() - this.fileUploadThread
                             .getStartTime()) / 1000;
                     double done = this.fileUploadThread.getUploadedLength();
                     double total = this.fileUploadThread.getTotalLength();
                     double percent;
                     double cps;
                     long remaining;
                     String eta;
                     try {
                         percent = 100.0 * done / total;
                     } catch (ArithmeticException e1) {
                         percent = 100;
                     }
                     try {
                         cps = done / duration;
                     } catch (ArithmeticException e1) {
                         cps = done;
                     }
                     try {
                         remaining = (long) ((total - done) / cps);
                         if (remaining > 3600) {
                             eta = String.format(this.timefmt_hms, new Long(
                                     remaining / 3600), new Long(
                                     (remaining / 60) % 60), new Long(
                                     remaining % 60));
                         } else if (remaining > 60) {
                             eta = String.format(this.timefmt_ms, new Long(
                                     remaining / 60), new Long(remaining % 60));
                         } else
                             eta = String.format(this.timefmt_s, new Long(
                                     remaining));
                     } catch (ArithmeticException e1) {
                         eta = "unknown";
                     }
                     this.progressBar.setValue((int) percent);
                     String unit = this.speedunit_b_per_second;
                     if (cps >= gB) {
                         cps /= gB;
                         unit = this.speedunit_gb_per_second;
                     } else if (cps >= mB) {
                         cps /= mB;
                         unit = this.speedunit_mb_per_second;
                     } else if (cps >= kB) {
                         cps /= kB;
                         unit = this.speedunit_kb_per_second;
                     }
                     String status = String.format(this.status_msg, new Integer(
                             (int) percent), new Double(cps), unit, eta);
                     this.statusLabel.setText(status);
                     this.uploadPolicy.getApplet().getAppletContext()
                             .showStatus(status);
                 }
             }
             if (!this.fileUploadThread.isAlive()) {
                 this.uploadPolicy.displayDebug(
                         "JUploadPanel: after !fileUploadThread.isAlive()", 60);
                 this.timer.stop();
                 String svrRet = this.fileUploadThread.getResponseMsg();
                 Exception ex = this.fileUploadThread.getException();
 
                 // Restore enable state, as the upload is finished.
                 this.stopButton.setEnabled(false);
                 this.browseButton.setEnabled(true);
 
                 // Free resources of the upload thread.
                 this.fileUploadThread.close();
                 this.fileUploadThread = null;
 
                 this.uploadPolicy.afterUpload(ex, svrRet);
                 // Do something (eg Redirect to another page for
                 // processing).
                 // EGR if((null != aus) && isSuccess)
                 // aus.executeThis(svrRet);
 
                 boolean haveFiles = (0 < this.filePanel.getFilesLength());
                 this.uploadButton.setEnabled(haveFiles);
                 this.removeButton.setEnabled(haveFiles);
                 this.removeAllButton.setEnabled(haveFiles);
 
                 this.uploadPolicy.getApplet().getAppletContext().showStatus("");
                 this.statusLabel.setText(" ");
 
             }
             return;
         }
         this.uploadPolicy.displayDebug("Action : " + e.getActionCommand(), 1);
         if (e.getActionCommand() == this.browseButton.getActionCommand()) {
             // Browse clicked
             if (null != this.fileChooser) {
                 try {
                     int ret = this.fileChooser.showOpenDialog(new Frame());
                     if (JFileChooser.APPROVE_OPTION == ret)
                         addFiles(this.fileChooser.getSelectedFiles(),
                                 this.fileChooser.getCurrentDirectory());
                     // We stop any running task for the JUploadFileView
                     this.fileChooser.shutdownNow();
                 } catch (Exception ex) {
                     this.uploadPolicy.displayErr(ex);
                 }
             }
         } else if (e.getActionCommand() == this.removeButton.getActionCommand()) {
             // Remove clicked
             this.filePanel.removeSelected();
             if (0 >= this.filePanel.getFilesLength()) {
                 this.removeButton.setEnabled(false);
                 this.removeAllButton.setEnabled(false);
                 this.uploadButton.setEnabled(false);
             }
         } else if (e.getActionCommand() == this.removeAllButton
                 .getActionCommand()) {
             // Remove All clicked
             this.filePanel.removeAll();
             this.removeButton.setEnabled(false);
             this.removeAllButton.setEnabled(false);
             this.uploadButton.setEnabled(false);
         } else if (e.getActionCommand() == this.uploadButton.getActionCommand()) {
             // Upload clicked
 
             // Check that the upload is ready (we ask the uploadPolicy. Then,
             // we'll call beforeUpload for each
             // FileData instance, that exists in allFiles[].
 
             // ///////////////////////////////////////////////////////////////////////////////////////////////
             // IMPORTANT: It's up to the UploadPolicy to explain to the user
             // that the upload is not ready!
             // ///////////////////////////////////////////////////////////////////////////////////////////////
             if (this.uploadPolicy.isUploadReady()) {
                 this.uploadPolicy.beforeUpload();
 
                 this.browseButton.setEnabled(false);
                 this.removeButton.setEnabled(false);
                 this.removeAllButton.setEnabled(false);
                 this.uploadButton.setEnabled(false);
                 this.stopButton.setEnabled(true);
 
                 // The FileUploadThread instance depends on the protocol.
                 if (this.uploadPolicy.getPostURL().substring(0, 4).equals(
                         "ftp:")) {
                     // fileUploadThread = new
                     // FileUploadThreadFTP(filePanel.getFiles(), uploadPolicy,
                     // progress);
                     this.fileUploadThread = new FileUploadThreadFTP(
                             this.filePanel.getFiles(), this.uploadPolicy,
                             this.progressBar);
                 } else {
                     // fileUploadThread = new
                     // FileUploadThreadV4(filePanel.getFiles(), uploadPolicy,
                     // progress);
                     this.fileUploadThread = new FileUploadThreadHTTP(
                             this.filePanel.getFiles(), this.uploadPolicy,
                             this.progressBar);
                 }
                 this.fileUploadThread.start();
 
                 // Create a timer.
                 this.timer = new Timer(DEFAULT_TIMEOUT, this);
                 this.timer.start();
                 this.uploadPolicy.displayDebug("Timer started", 60);
 
             } // if isIploadReady()
         } else if (e.getActionCommand() == this.stopButton.getActionCommand()) {
             // We request the thread to stop its job.
             this.fileUploadThread.stopUpload();
         }
         // focus the table. This is necessary in order to enable mouse events
         // for triggering tooltips.
         this.filePanel.focusTable();
     }
 
     /**
      * @return the uploadPolicy
      */
     public UploadPolicy getUploadPolicy() {
         return this.uploadPolicy;
     }
 
     /**
      * @return the filePanel
      */
     public FilePanel getFilePanel() {
         return this.filePanel;
     }
 
     /**
      * This methods show or hides the logWindow, depending on the following
      * applet parameters. The following conditions must be met, to hide the log
      * window: <DIR>
      * <LI>showLogWindow (must be False)
      * <LI>debugLevel (must be 0 or less) </DIR>
      */
     public void showOrHideLogWindow() {
         if (this.uploadPolicy.getShowLogWindow()
                 || this.uploadPolicy.getDebugLevel() > 0) {
             // The log window should be visible. Is it visible already?
             if (!this.isLogWindowVisible) {
                 add(this.jLogWindowPane, -1);
                 this.isLogWindowVisible = true;
                 // Let's recalculate the component display
                 validate();
             }
         } else {
             // It should be hidden.
             if (this.isLogWindowVisible) {
                 remove(this.jLogWindowPane);
                 this.isLogWindowVisible = false;
                 // Let's recalculate the component display
                 validate();
             }
         }
     }
 
     /**
      * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
      */
     public void mouseClicked(MouseEvent mouseEvent) {
         maybeOpenPopupMenu(mouseEvent);
     }
 
     /**
      * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
      */
     public void mouseEntered(MouseEvent mouseEvent) {
         maybeOpenPopupMenu(mouseEvent);
     }
 
     /**
      * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
      */
     public void mouseExited(MouseEvent mouseEvent) {
         maybeOpenPopupMenu(mouseEvent);
     }
 
     /**
      * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
      */
     public void mousePressed(MouseEvent mouseEvent) {
         maybeOpenPopupMenu(mouseEvent);
     }
 
     /**
      * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
      */
     public void mouseReleased(MouseEvent mouseEvent) {
         maybeOpenPopupMenu(mouseEvent);
     }
 
     /**
      * This method opens the popup menu, if the mouseEvent is relevant. In this
      * case it returns true. Otherwise, it does nothing and returns false.
      * 
      * @param mouseEvent The triggered mouse event.
      * @return true if the popup menu was opened, false otherwise.
      */
     boolean maybeOpenPopupMenu(MouseEvent mouseEvent) {
         if (mouseEvent.isPopupTrigger()
                 && ((mouseEvent.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) == InputEvent.CTRL_DOWN_MASK)) {
             if (this.jUploadPopupMenu != null) {
                 this.jUploadPopupMenu.show(mouseEvent.getComponent(),
                         mouseEvent.getX(), mouseEvent.getY());
                 return true;
             }
         }
         return false;
     }
 
 }
