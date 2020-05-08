 package com.asascience.edc.sos.ui;
 
 import com.asascience.edc.gui.FileBrowser;
 import com.asascience.edc.sos.SosServer;
 import com.asascience.edc.utils.FileSaveUtils;
 import java.awt.Cursor;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.io.File;
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.SwingWorker;
 import net.miginfocom.swing.MigLayout;
 import org.apache.log4j.Logger;
 
 /** 
  * SosGetObsProgressMonitor.java
  * 
  * @author Kyle Wilcox <kwilcox@asascience.com>
  */
 public class SosGetObsProgressMonitor extends JPanel implements ActionListener, PropertyChangeListener {
 
   private JButton startButton;
   private JProgressBar progressBar;
   private JTextArea taskOutput;
   private Task task;
   private SosServer sosServer;
   private ListenForObsProgress listener;
   private PropertyChangeSupport pcs;
   private File savePath;
   private static Logger guiLogger = Logger.getLogger("com.asascience.log." + SosGetObsProgressMonitor.class.getName());
 
   public void propertyChange(PropertyChangeEvent evt) {
     listener.propertyChange(evt);
   }
 
   class Task extends SwingWorker<Void, Void> {
     @Override
     public Void doInBackground() {
       setProgress(0);
       try {
         sosServer.addPropertyChangeListener(listener);
         sosServer.getObservations(savePath);
       } catch (Exception e) {
         taskOutput.append(String.format("%1$s\n", e.toString()));
       }
       sosServer.removePropertyChangeListener(listener);
       return null;
     }
 
     public void setTaskProgress(int progress) {
       setProgress(progress);
     }
     
     @Override
     public void done() {
       startButton.setEnabled(true);
       setCursor(null);
       taskOutput.append("Done!\n");
       sosServer.removePropertyChangeListener(listener);
     }
   }
 
   public SosGetObsProgressMonitor(SosServer data) {
     super(new MigLayout("fill"));
 
     pcs = new PropertyChangeSupport(this);
     listener = new ListenForObsProgress();
 
     sosServer = data;
    
     startButton = new JButton("Start");
     startButton.setActionCommand("start");
     startButton.addActionListener(this);
 
     progressBar = new JProgressBar(0, 100);
     progressBar.setValue(0);
     progressBar.setStringPainted(true);
     
     // NEED TO HAVE THE SAVE PATH SET AT THIS POINT
     File tempPath = new File(FileSaveUtils.chooseFilename(new File(sosServer.getHomeDir() + File.separator + FileSaveUtils.getNameAndDateFromUrl(sosServer.getBaseUrl()) + File.separator), "Output Directory (many files may be saved here)"));
     FileBrowser fileBrowser = new FileBrowser(tempPath);
     fileBrowser.addPropertyChangeListener("fileChanged", new PropertyChangeListener() {
 
       public void propertyChange(PropertyChangeEvent evt) {
         savePath = ((File)evt.getNewValue());
         // Immediatly kick off the processing
         startButton.doClick();
       }
     });
     fileBrowser.setSelectDirectory(true, false);
 
     taskOutput = new JTextArea(5, 20);
     taskOutput.setMargin(new Insets(5,5,5,5));
     taskOutput.setEditable(false);
 
     JPanel panel = new JPanel(new MigLayout("gapx 10, fillx"));
     panel.add(progressBar, "growx");
     panel.add(startButton, "wrap");
     panel.add(fileBrowser, "growx, spanx");
 
     add(panel, "wrap, align center");
     add(new JScrollPane(taskOutput), "grow");
     setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
   }
 
   public void actionPerformed(ActionEvent e) {
     setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
     task = new Task();
     task.addPropertyChangeListener(listener);
     task.execute();
     startButton.setEnabled(false);
   }
 
   @Override
   public void addPropertyChangeListener(PropertyChangeListener l) {
     pcs.addPropertyChangeListener(l);
   }
   @Override
   public void removePropertyChangeListener(PropertyChangeListener l) {
     pcs.removePropertyChangeListener(l);
   }
 
   class ListenForObsProgress implements PropertyChangeListener {
     public void propertyChange(PropertyChangeEvent evt) {
       if ("progress".equals(evt.getPropertyName())) {
         int progress = (Integer)evt.getNewValue();
         progressBar.setValue(progress);
         task.setTaskProgress(progress);
       } else if ("message".equals(evt.getPropertyName())) {
         taskOutput.append(String.format("%1$s\n", evt.getNewValue()));
         guiLogger.info((String)evt.getNewValue());
       } else if ("done".equals(evt.getPropertyName())) {
         guiLogger.info("Processing of SOS Data complete");
         pcs.firePropertyChange(evt);
       }
     }
   }
 }
