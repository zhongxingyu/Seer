 package fedora.client.actions;
 
 import java.awt.event.ActionEvent;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 import javax.swing.AbstractAction;
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 
 import fedora.client.Administrator;
 import fedora.client.export.AutoExporter;
 
 /**
  *
  * <p><b>Title:</b> ExportObject.java</p>
  * <p><b>Description:</b> </p>
  *
  * -----------------------------------------------------------------------------
  *
  * <p><b>License and Copyright: </b>The contents of this file are subject to the
  * Mozilla Public License Version 1.1 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a copy of the License
  * at <a href="http://www.mozilla.org/MPL">http://www.mozilla.org/MPL/.</a></p>
  *
  * <p>Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
  * the specific language governing rights and limitations under the License.</p>
  *
 * <p>The entire file consists of original code.  Copyright &copy; 2002, 2003 by The
  * Rector and Visitors of the University of Virginia and Cornell University.
  * All rights reserved.</p>
  *
  * -----------------------------------------------------------------------------
  *
  * @author cwilper@cs.cornell.edu
  * @version 1.0
  */
 public class ExportObject
         extends AbstractAction {
 
     private Set m_pids;
     private boolean m_prompt;
 
     public ExportObject() {
         super("Export Object...");
         m_prompt=true;
     }
 
     public ExportObject(String pid) {
         super("Export Object...");
         m_pids=new HashSet();
         m_pids.add(pid);
     }
 
     public ExportObject(Set pids) {
         super("Export Objects...");
         m_pids=pids;
     }
 
     public void actionPerformed(ActionEvent ae) {
         AutoExporter exporter=null;
         try {
             exporter=new AutoExporter(Administrator.getHost(), Administrator.getPort(), Administrator.getUser(), Administrator.getPass());
         } catch (Exception e) {
             JOptionPane.showMessageDialog(Administrator.getDesktop(),
                     e.getClass().getName() + ": " + e.getMessage(),
                     "Export Failure",
                     JOptionPane.ERROR_MESSAGE);
         }
         if (exporter!=null) {
             if (m_prompt) {
                 String pid=JOptionPane.showInputDialog("Enter the PID.");
                 if (pid==null) {
                    return;
                 }
                 m_pids=new HashSet();
                 m_pids.add(pid);
             }
             Iterator pidIter=m_pids.iterator();
             if (m_pids.size()==1) {
                 // If there's only one pid, get export filename
                 String pid=(String) pidIter.next();
                 try {
                     JFileChooser browse;
                     if (Administrator.getLastDir()==null) {
                         browse=new JFileChooser();
                     } else {
                         browse=new JFileChooser(Administrator.getLastDir());
                     }
                     browse.setApproveButtonText("Export");
                     browse.setApproveButtonMnemonic('E');
                     browse.setApproveButtonToolTipText("Exports to the selected file.");
                     browse.setDialogTitle("Export to...");
                     int returnVal = browse.showOpenDialog(Administrator.getDesktop());
                     if (returnVal == JFileChooser.APPROVE_OPTION) {
                         File file = browse.getSelectedFile();
                         Administrator.setLastDir(file.getParentFile()); // remember the dir for next time
                         exporter.export(pid, new FileOutputStream(file));
                         JOptionPane.showMessageDialog(Administrator.getDesktop(),
                                 "Exported " + pid);
                     }
                 } catch (Exception e) {
                     JOptionPane.showMessageDialog(Administrator.getDesktop(),
                             e.getClass().getName() + ": " + e.getMessage(),
                             "Export Failure",
                             JOptionPane.ERROR_MESSAGE);
                 }
             } else {
                 // If there are multiple pids, select a directory first.
                 try {
                     JFileChooser browse;
                     if (Administrator.getLastDir()==null) {
                         browse=new JFileChooser();
                     } else {
                         browse=new JFileChooser(Administrator.getLastDir());
                     }
                     browse.setApproveButtonText("Export");
                     browse.setApproveButtonMnemonic('E');
                     browse.setApproveButtonToolTipText("Exports to the selected directory.");
                     browse.setDialogTitle("Export to...");
                     browse.setDialogTitle("Choose export directory...");
                     browse.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                     int returnVal = browse.showOpenDialog(Administrator.getDesktop());
                     if (returnVal == JFileChooser.APPROVE_OPTION) {
                         Administrator.setLastDir(browse.getSelectedFile()); // remember the dir for next time
                         while (pidIter.hasNext()) {
                             String pid=(String) pidIter.next();
                             StringBuffer buf=new StringBuffer();
                             for (int i=0; i<pid.length(); i++) {
                                 char c=pid.charAt(i);
                                 if (c==':') {
                                     buf.append('_');
                                 } else {
                                     buf.append(c);
                                 }
                             }
                             File outFile=new File(browse.getSelectedFile(), buf.toString());
                             exporter.export(pid, new FileOutputStream(outFile));
                         }
                         JOptionPane.showMessageDialog(Administrator.getDesktop(),
                                 "Exported " + m_pids.size() + " objects.");
                     }
                 } catch (Exception e) {
                     JOptionPane.showMessageDialog(Administrator.getDesktop(),
                             e.getClass().getName() + ": " + e.getMessage(),
                             "Export Failure",
                             JOptionPane.ERROR_MESSAGE);
                 }
             }
         }
     }
 
 }
