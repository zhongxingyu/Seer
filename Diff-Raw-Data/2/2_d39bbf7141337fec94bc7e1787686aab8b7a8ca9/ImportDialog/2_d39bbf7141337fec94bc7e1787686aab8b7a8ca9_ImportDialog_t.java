 package fedora.client.objecteditor;
 
 import java.awt.BorderLayout;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.GridBagLayout;
 import java.awt.GridBagConstraints;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Properties;
 
 import javax.swing.AbstractAction;
 import javax.swing.BorderFactory;
 import javax.swing.ButtonGroup;
 import javax.swing.JRadioButton;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JDialog;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 
 import fedora.client.Administrator;
 
 /**
  * Launch a dialog for getting a local file or a URL in order to import
  * content.  If a URL is chosen, download the content ensure that the public
  * file member points to it.  The URL must be resolvable and return a response
  * code of 200 in order for this to work.  In the case where the file's content
  * comes from a URL, it will request deleteOnExit().
  *
  * When this dialog closes, if file is null, it was canceled.
  * If it was not canceled, file will point to the content.
  * If url is not null, it means the import occurred from a url and that
  * is the source.
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
  * <p>The entire file consists of original code.  Copyright &copy; 2002-2004 by The
  * Rector and Visitors of the University of Virginia and Cornell University.
  * All rights reserved.</p>
  *
  * -----------------------------------------------------------------------------
  *
  * @author cwilper@cs.cornell.edu
  * @version $Id$
  */
 
 public class ImportDialog
         extends JDialog {
 
     public File file=null;
     public String url=null;
 
     private JTextField m_fileField;
     private JTextField m_urlField;
     private JRadioButton m_fileButton;
     private JRadioButton m_urlButton;
 
 
     private ImportDialog m_importDialog;
 
     public ImportDialog() {
         super(JOptionPane.getFrameForComponent(Administrator.getDesktop()), "Import Content", true);
 
         m_importDialog=this;
         ImportAction importAction=new ImportAction();
         JButton importButton=new JButton(importAction);
         Administrator.constrainHeight(importButton);
 
         m_fileButton=new JRadioButton("From file");
         m_urlButton=new JRadioButton("From URL");
         ButtonGroup group=new ButtonGroup();
         group.add(m_fileButton);
         group.add(m_urlButton);
 		m_fileButton.setSelected(true);
 
         m_fileField=new JTextField(20);
         JButton browseButton=new JButton("Browse...");
         Administrator.constrainHeight(browseButton);
 		browseButton.addActionListener(new ActionListener() {
 		    public void actionPerformed(ActionEvent evt) {
                 JFileChooser browse;
                 if (Administrator.getLastDir()==null) {
                     browse=new JFileChooser();
                 } else {
                     browse=new JFileChooser(Administrator.getLastDir());
                 }
                 browse.setApproveButtonText("Import");
                 browse.setApproveButtonMnemonic('I');
                 browse.setApproveButtonToolTipText("Imports the selected file.");
                 browse.setDialogTitle("Import New Datastream Content...");
                 int returnVal = browse.showOpenDialog(Administrator.getDesktop());
                 if (returnVal == JFileChooser.APPROVE_OPTION) {
                     m_fileField.setText(browse.getSelectedFile().getPath());
 				}
 			}
 		});
         JPanel fileValuePanel=new JPanel();
         fileValuePanel.setLayout(new FlowLayout());
         fileValuePanel.add(m_fileField);
         fileValuePanel.add(browseButton);
 
         m_urlField=new JTextField(20);
 
         JPanel inputPane=new JPanel();
         inputPane.setBorder(BorderFactory.createCompoundBorder(
                 BorderFactory.createCompoundBorder(
                     BorderFactory.createEmptyBorder(4, 4, 4, 4),
                     BorderFactory.createEtchedBorder()
                 ),
                 BorderFactory.createEmptyBorder(4,4,4,4)
                 ));
         GridBagLayout gridBag=new GridBagLayout();
         inputPane.setLayout(gridBag);
         addRows(new JComponent[] {m_fileButton, m_urlButton}, 
                 new JComponent[] {fileValuePanel, m_urlField}, 
                 gridBag, inputPane);
 
         JButton cancelButton=new JButton(new AbstractAction() {
             public void actionPerformed(ActionEvent evt) {
                 dispose();
             }
         });
        cancelButton.setText("Cancel");
         Administrator.constrainHeight(cancelButton);
 
         JPanel buttonPane=new JPanel();
         buttonPane.add(importButton);
         buttonPane.add(cancelButton);
         Container contentPane=getContentPane();
         contentPane.setLayout(new BorderLayout());
         contentPane.add(inputPane, BorderLayout.CENTER);
         contentPane.add(buttonPane, BorderLayout.SOUTH);
         pack();
         setLocation(Administrator.INSTANCE.getCenteredPos(getWidth(), getHeight()));
         setVisible(true);
     }
 
     public void addRows(JComponent[] left, JComponent[] right,
             GridBagLayout gridBag, Container container) {
         GridBagConstraints c=new GridBagConstraints();
         c.insets=new Insets(0, 4, 4, 4);
         for (int i=0; i<left.length; i++) {
             c.anchor=GridBagConstraints.WEST;
             c.gridwidth=GridBagConstraints.RELATIVE; //next-to-last
             c.fill=GridBagConstraints.NONE;      //reset to default
             c.weightx=0.0;                       //reset to default
             gridBag.setConstraints(left[i], c);
             container.add(left[i]);
 
             c.gridwidth=GridBagConstraints.REMAINDER;     //end row
                 c.anchor=GridBagConstraints.WEST;
             c.weightx=1.0;
             gridBag.setConstraints(right[i], c);
             container.add(right[i]);
         }
 
     }
 
     public class ImportAction
             extends AbstractAction {
 
         public ImportAction() {
             super("Import");
         }
 
         public void actionPerformed(ActionEvent evt) {
             try {
 			    if (m_fileButton.isSelected()) {
 				    if (m_fileField.getText().equals("")) {
 					    throw new IOException("No filename entered.");
 					}
 					File f=new File(m_fileField.getText());
 					if (!f.exists()) {
 					    throw new IOException("File does not exist.");
 					}
                     file=f;
 				} else {
 				    if (m_urlField.getText().equals("")) {
 					    throw new IOException("No URL entered.");
 					}
 					File f=File.createTempFile("fedora-ingest-", null);
 					f.deleteOnExit();
 					try {
                     Administrator.DOWNLOADER.get(m_urlField.getText(), new FileOutputStream(f));
 					} catch (Exception e) {
 					    throw new IOException("Download failed: " + m_urlField.getText());
 					}
 					url=m_urlField.getText();
 					file=f;
 				}
                 dispose();
             } catch (Exception e) {
                 JOptionPane.showMessageDialog(m_importDialog,
                     e.getMessage(),
                     "Import Error",
                     JOptionPane.ERROR_MESSAGE);
             }
         }
     }
 
 }
