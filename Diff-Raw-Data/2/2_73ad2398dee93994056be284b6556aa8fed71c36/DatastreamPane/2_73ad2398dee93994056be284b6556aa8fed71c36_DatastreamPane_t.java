 package fedora.client.objecteditor;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.io.*;
 import java.util.*;
 import javax.swing.*;
 import javax.swing.event.*;
 
 import fedora.client.Administrator;
 
 import fedora.server.types.gen.Datastream;
 import fedora.server.utilities.StreamUtility;
 
 /**
  * Displays a datastream's attributes, allowing the editing of its state,
  * and some of the most recent version's attributes.  Also provides buttons
  * for working with the content of the datastream, depending on its type.
  *
  * @author cwilper@cs.cornell.edu
  * @version $Id$
  */
 public class DatastreamPane
         extends EditingPane 
         implements ChangeListener {
 
     private String m_pid;
     private Datastream m_mostRecent;
 
     private Hashtable[] m_labelTables;
     private JComboBox m_stateComboBox;
     private JSlider m_versionSlider;
     private JPanel m_valuePane;
     private CardLayout m_versionCardLayout;
     private CurrentVersionPane m_currentVersionPane;
     private DatastreamsPane m_owner;
     private PurgeButtonListener m_purgeButtonListener;
     private boolean m_done;
     private Dimension m_labelDims;
     private JTextArea m_dtLabel;
     private JPanel m_dateLabelAndValue;
     private Datastream[] m_versions;
 
     /**
      * Build the pane.
      */
     public DatastreamPane(ObjectEditorFrame gramps, String pid, Datastream[] versions, DatastreamsPane owner)
             throws Exception {
         super(gramps, owner, versions[0].getID());
 		m_pid=pid;
         m_versions=versions;
         Datastream mostRecent=versions[0];
         m_mostRecent=mostRecent;
         m_owner=owner;
         m_labelDims=new JLabel("Control Group").getPreferredSize();
         new TextContentEditor();  // causes it to be registered if not already
         new ImageContentViewer();  // causes it to be registered if not already
         new SVGContentViewer();  // causes it to be registered if not already
 
         // mainPane(commonPane, versionPane)
 
             // NORTH: commonPane(state, controlGroup)
 
                     // LEFT: labels
                     JLabel stateLabel=new JLabel("State");
                     JLabel controlGroupLabel=new JLabel("Control Group");
                     JLabel[] leftCommonLabels=new JLabel[] { 
                             stateLabel, 
                             controlGroupLabel};
 
                     // RIGHT: values
                     String[] comboBoxStrings={"Active", "Inactive", "Deleted"};
                     m_stateComboBox=new JComboBox(comboBoxStrings);
                     Administrator.constrainHeight(m_stateComboBox);
                     if (mostRecent.getState().equals("A")) {
                         m_stateComboBox.setSelectedIndex(0);
                         m_stateComboBox.setBackground(Administrator.ACTIVE_COLOR);
                     } else if (mostRecent.getState().equals("I")) {
                         m_stateComboBox.setSelectedIndex(1);
                         m_stateComboBox.setBackground(Administrator.INACTIVE_COLOR);
                     } else {
                         m_stateComboBox.setSelectedIndex(2);
                         m_stateComboBox.setBackground(Administrator.DELETED_COLOR);
                     }
                     m_stateComboBox.addActionListener(dataChangeListener);
                     m_stateComboBox.addActionListener(new ActionListener() {
                         public void actionPerformed(ActionEvent evt) {
                             String curState;
                             if (m_stateComboBox.getSelectedIndex()==1) {
                                 curState="I";
                                 m_stateComboBox.setBackground(Administrator.INACTIVE_COLOR);
                             } else if (m_stateComboBox.getSelectedIndex()==2) {
                                 curState="D";
                                 m_stateComboBox.setBackground(Administrator.DELETED_COLOR);
                             } else {
                                 curState="A";
                                 m_stateComboBox.setBackground(Administrator.ACTIVE_COLOR);
                             }
                             m_owner.colorTabForState(m_mostRecent.getID(), curState);
                         }
                     });
 
                     JTextArea controlGroupValueLabel=new JTextArea(
                             getControlGroupString(
                                     mostRecent.getControlGroup().toString())
                             );
                     controlGroupValueLabel.setBackground(Administrator.BACKGROUND_COLOR);
                     controlGroupValueLabel.setEditable(false);
                     JComponent[] leftCommonValues = 
                             new JComponent[] { m_stateComboBox, 
                                                controlGroupValueLabel};
     
                 JPanel leftCommonPane=new JPanel();
                 GridBagLayout leftCommonGridBag=new GridBagLayout();
                 leftCommonPane.setLayout(leftCommonGridBag);
                 addLabelValueRows(leftCommonLabels, leftCommonValues, 
                         leftCommonGridBag, leftCommonPane);
 
                 JPanel commonPane=leftCommonPane;
 
             // CENTER: versionPane(m_versionSlider, m_valuePane)
 
                 // NORTH: m_versionSlider
 
                 // set up the shared button listener for purge
                 m_purgeButtonListener=new PurgeButtonListener(versions);
 
                 // do the slider if needed
                 if (versions.length>1) {
                     m_versionSlider=new JSlider(JSlider.HORIZONTAL,
                             0, versions.length-1, 0);
                     m_versionSlider.addChangeListener(this);
                     m_versionSlider.setMajorTickSpacing(1);
                     m_versionSlider.setSnapToTicks(true);
                     m_versionSlider.setPaintTicks(true);
                 }
 
 
                 // CENTER: m_valuePane(one card for each version)
 
                 m_valuePane=new JPanel();
                 m_versionCardLayout=new CardLayout();
                 m_valuePane.setLayout(m_versionCardLayout);
                 JPanel[] valuePanes=new JPanel[versions.length];
 
                     // CARD: valuePanes[0](versionValuePane, versionActionPane)
 
                     m_currentVersionPane=new CurrentVersionPane(mostRecent);
                     valuePanes[0]=m_currentVersionPane;
 
                 m_valuePane.add(valuePanes[0], "0");
 
                     // CARD: valuePanes[1 to i](versionValuePane, versionActionPane)
 
                     for (int i=1; i<versions.length; i++) {
                         valuePanes[i]=new PriorVersionPane(versions[i]);
     
                         m_valuePane.add(valuePanes[i], "" + i);
                     }
 
             JPanel versionPane=new JPanel();
             versionPane.setLayout(new BorderLayout());
             if (versions.length>1) {
                 // Add a panel to versionPane.NORTH
                 // FlowLayout(SwingConstants.LEFT)
                 // Created   Date   m_versionSlider
                 m_dateLabelAndValue=new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
                 JLabel createdLabel=new JLabel("Created");
                 createdLabel.setPreferredSize(m_labelDims);
                 m_dateLabelAndValue.add(createdLabel);
                 m_dateLabelAndValue.add(Box.createHorizontalStrut(0));
                 m_dtLabel=new JTextArea(versions[0].getCreateDate() + " ");
                 m_dtLabel.setBackground(Administrator.BACKGROUND_COLOR);
                 m_dtLabel.setEditable(false);
                 m_dateLabelAndValue.add(m_dtLabel);
 
                 JPanel stretch=new JPanel(new BorderLayout());
                 stretch.setBorder(BorderFactory.createEmptyBorder(0,0,4,0));
                 stretch.add(m_dateLabelAndValue, BorderLayout.WEST);
                 stretch.add(m_versionSlider, BorderLayout.CENTER);
                 versionPane.add(stretch, BorderLayout.NORTH);
             }
             versionPane.add(m_valuePane, BorderLayout.CENTER);
 
         mainPane.setLayout(new BorderLayout());
         mainPane.add(commonPane, BorderLayout.NORTH);
         mainPane.add(versionPane, BorderLayout.CENTER);
     }
 
     public void stateChanged(ChangeEvent e) {
        JSlider source=(JSlider)e.getSource();
        if (!source.getValueIsAdjusting()) {
            m_versionCardLayout.show(m_valuePane, "" + source.getValue());
            m_dtLabel.setText(m_versions[source.getValue()].getCreateDate());
        }
     }
 
     public boolean isDirty() {
         if (m_done) return false;
         int stateIndex=0;
         if (m_mostRecent.getState().equals("I")) {
             stateIndex=1;
         }
         if (m_mostRecent.getState().equals("D")) {
             stateIndex=2;
         }
         if (stateIndex!=m_stateComboBox.getSelectedIndex()) {
             return true;
         }
         if (m_currentVersionPane.isDirty()) {
             return true;
         }
         return false;
     }
 
     private String getControlGroupString(String abbrev) {
         if (abbrev.equals("M")) {
             return "Managed Content";
         } else if (abbrev.equals("X")) {
             return "Internal XML Metadata";
         } else if (abbrev.equals("R")) {
             return "Redirect";
         } else {
             return "External Reference";
         }
     }
 
     public void saveChanges(String logMessage) 
             throws Exception {
         String state=null;
         int i=m_stateComboBox.getSelectedIndex();
         if (i==0)
            state="A";
         if (i==1)
            state="I";
         if (i==2)
            state="D";
 		if (m_currentVersionPane.isDirty()) { 
 		    // defer to the currentVersionPane if anything else changed
             try {
      		    m_currentVersionPane.saveChanges(state, 
      		                                     logMessage, 
      		                                     false);
             } catch (Exception e) {
                 if (e.getMessage() == null 
                         || e.getMessage().indexOf(" would invalidate ") == -1) {
                     throw e;
                 }
                 // ask if they want to force it.
                 Object[] options = { "Yes", "No" };
                 int selected=JOptionPane.showOptionDialog(null, 
                         e.getMessage() + "\n\nForce it?",
                         "Warning", JOptionPane.DEFAULT_OPTION, 
                         JOptionPane.WARNING_MESSAGE, null, options, options[1]);
                 if (selected==0) {
      		        m_currentVersionPane.saveChanges(state,  
      		                                         logMessage, 
      		                                         true);
                 }
             }
 		} else {
 		    // since only state changed, we can take care of it here
 			Administrator.APIM.setDatastreamState(m_pid, m_mostRecent.getID(),
 			        state, logMessage);
 		}
     }
 
     public void changesSaved() {
         m_owner.refresh(m_mostRecent.getID());
         m_done=true; 
     }
 
     public void undoChanges() {
         if (m_mostRecent.getState().equals("A")) {
             m_stateComboBox.setSelectedIndex(0);
             m_stateComboBox.setBackground(Administrator.ACTIVE_COLOR);
         } else if (m_mostRecent.getState().equals("I")) {
             m_stateComboBox.setSelectedIndex(1);
             m_stateComboBox.setBackground(Administrator.INACTIVE_COLOR);
         } else if (m_mostRecent.getState().equals("D")) {
             m_stateComboBox.setSelectedIndex(2);
             m_stateComboBox.setBackground(Administrator.DELETED_COLOR);
         }
         m_owner.colorTabForState(m_mostRecent.getID(), m_mostRecent.getState());
         m_currentVersionPane.undoChanges();
     }
 
     protected String getFedoraURL(Datastream ds, boolean withDate) {
         StringBuffer buf=new StringBuffer();
        buf.append(Administrator.getProtocol()+"://");
         buf.append(Administrator.getHost());
         if (Administrator.getPort()!=80) {
             buf.append(':');
             buf.append(Administrator.getPort());
         }
         buf.append("/fedora/get/");
         buf.append(m_pid);
         buf.append('/');
         buf.append(ds.getID());
         if (withDate) {
             buf.append('/');
             buf.append(ds.getCreateDate());
         }
         return buf.toString();
     }
 
     public class CurrentVersionPane
             extends JPanel
             implements PotentiallyDirty {
 
         private Datastream m_ds;
         private JTextField m_locationTextField;
         private JTextField m_labelTextField;
 		private String m_origLabel;
 		private JTextField m_MIMETextField;
 		private String m_origMIME;
         private JTextField m_formatURITextField;
 		private String m_origFormatURI;
 		private JTextField m_altIDsTextField;
 		private String m_origAltIDs;
         private JButton m_editButton;
         private JButton m_viewButton;
         private JButton m_importButton;
         private JButton m_exportButton;
         private JButton m_separateViewButton;
 
         private ContentEditor m_editor;
         private ContentViewer m_viewer;
         private boolean m_canEdit;
         private boolean m_canView;
         private File m_importFile;
         private JLabel m_importLabel;
 
         private boolean X;
         private boolean M;
         private boolean E;
         private boolean R;
 
         public CurrentVersionPane(Datastream ds) {
             m_ds=ds;
             // clean up attribute values for presentation in text boxes...
 			// set a null ds label to ""
 			m_origLabel=m_ds.getLabel();
 			if (m_origLabel==null) m_origLabel="";
 			// set a null mime type to ""
 			m_origMIME=m_ds.getMIMEType();
 			if (m_origMIME==null) m_origMIME="";
             // set a null format_uri to ""
             m_origFormatURI=m_ds.getFormatURI();
             if (m_origFormatURI==null) m_origFormatURI="";
             // create a string from alt ids array 
 			m_origAltIDs = "";
 			String[] altIDs = m_ds.getAltIDs();
 			if (altIDs != null) {
 				for (int z = 0; z < altIDs.length; z++) {
 					if (z > 0) m_origAltIDs += " ";
 					m_origAltIDs += altIDs[z];
 				}
 			}
             
             if (ds.getControlGroup().toString().equals("X")) {
                 X=true;
             } else if (ds.getControlGroup().toString().equals("M")) {
                 M=true;
             } else if (ds.getControlGroup().toString().equals("E")) {
                 E=true;
             } else if (ds.getControlGroup().toString().equals("R")) {
                 R=true;
             }
             // editing is possible if it's XML or Managed content and 
             // not a special datastream and hasEditor(mimeType)
             // AND the initial state wasn't "D"
             boolean noEdits=ds.getState().equals("D");
             if ((X || M) && (!noEdits)
                     && ( !ds.getID().equals("METHODMAP") 
                             && !ds.getID().equals("DSINPUTSPEC") 
                             && !ds.getID().equals("WSDL") 
                        )
                ) {
                 m_canEdit=ContentHandlerFactory.hasEditor(ds.getMIMEType());
             }
             m_canView=ContentHandlerFactory.hasViewer(ds.getMIMEType());
             // whether they're used or not, create these here
             m_editButton=new JButton("Edit");
             Administrator.constrainHeight(m_editButton);
             m_viewButton=new JButton("View");
             Administrator.constrainHeight(m_viewButton);
             m_importButton=new JButton("Import...");
             Administrator.constrainHeight(m_importButton);
             m_exportButton=new JButton("Export...");
             Administrator.constrainHeight(m_exportButton);
             // How we set this JPanel up depends on:
             // what control group it is in and
             // whether it can be edited or viewed
             setLayout(new BorderLayout());
 
             // do the field panel (NORTH)
             JLabel labelLabel=new JLabel("Label");
             labelLabel.setPreferredSize(m_labelDims);
 			JLabel MIMELabel=new JLabel("MIME Type");
 			MIMELabel.setPreferredSize(m_labelDims);
 			JLabel formatURILabel=new JLabel("Format URI");
 			formatURILabel.setPreferredSize(m_labelDims);
 			JLabel altIDsLabel=new JLabel("Alternate IDs");
 			altIDsLabel.setPreferredSize(m_labelDims);
             JLabel urlLabel=new JLabel("Fedora URL");
             urlLabel.setPreferredSize(m_labelDims);
             JLabel[] labels;
             if (R || E) {
                 JLabel locationLabel=new JLabel("Location");
                 locationLabel.setPreferredSize(m_labelDims);
                 if (m_versionSlider!=null) {
                     labels=new JLabel[] {labelLabel, 
                     					 MIMELabel,
                     					 formatURILabel,
 										 altIDsLabel, 
                     					 locationLabel, 
                     					 urlLabel};
                 } else {
                     labels=new JLabel[] {new JLabel("Created"), 
                     					 labelLabel,
 										 MIMELabel, 
                     					 formatURILabel,
                     					 altIDsLabel, 
                     					 locationLabel, 
                     					 urlLabel};
                 }
             } else {
                 if (m_versionSlider!=null) {
                     labels=new JLabel[] {labelLabel, 
 										 MIMELabel,
 										 formatURILabel,
                     					 altIDsLabel, 
                     					 urlLabel};
                 } else {
                     labels=new JLabel[] {new JLabel("Created"), 
                     					 labelLabel,
                     					 MIMELabel, 
                     					 formatURILabel,
                     					 altIDsLabel, 
                     					 urlLabel};
                 }
             }
             // set up text fields for ds attributes at version level
 			JComponent[] values;
             // ds label text field
             m_labelTextField=new JTextField(m_origLabel);
             m_labelTextField.getDocument().addDocumentListener(
                     dataChangeListener);
 			// ds MIME text field
 			m_MIMETextField=new JTextField(m_origMIME);
 			m_MIMETextField.getDocument().addDocumentListener(
 					dataChangeListener);
             // ds format URI text field
 			m_formatURITextField=new JTextField(m_origFormatURI);
 			m_formatURITextField.getDocument().addDocumentListener(
 					dataChangeListener);
 			// ds alternate ids text field
 			m_altIDsTextField = new JTextField(m_origAltIDs);
 			m_altIDsTextField.getDocument().addDocumentListener(
 					dataChangeListener);
 			// disable text fields for special datastreams that cannot be edited
 			if (ds.getID().equals("METHODMAP")
 					|| ds.getID().equals("DSINPUTSPEC")
 					|| ds.getID().equals("WSDL") || noEdits) {
 				// disable formatURI changes for special datastreams
 				m_labelTextField.setEnabled(false);
 				m_MIMETextField.setEnabled(false);
 				m_formatURITextField.setEnabled(false);
 				m_altIDsTextField.setEnabled(false);
 			}
             // Fedora URL text field
             JTextField urlTextField=new JTextField(getFedoraURL(m_ds, false));
             urlTextField.setEditable(false);  // so they can copy, but not modify
 			// ds location URL text field (R and E datastreams only)
             if (R || E) {
                 m_locationTextField=new JTextField(m_ds.getLocation());
                 m_locationTextField.getDocument().addDocumentListener(
                     dataChangeListener);
                 if (noEdits) {
                     m_locationTextField.setEnabled(false);
                 }
                 if (m_versionSlider!=null) {
                     values=new JComponent[] {m_labelTextField,
                     						 m_MIMETextField,
                     						 m_formatURITextField,
                     						 m_altIDsTextField, 
                                              m_locationTextField, 
                                              urlTextField};
                                              
                 } else {
                     JTextArea cDateTextArea=new JTextArea(m_ds.getCreateDate());
                     cDateTextArea.setBackground(Administrator.BACKGROUND_COLOR);
                     cDateTextArea.setEditable(false);
                     values=new JComponent[] {cDateTextArea,
                                              m_labelTextField,
 											 m_MIMETextField,
                                              m_formatURITextField,
 											 m_altIDsTextField,  
                                              m_locationTextField, 
                                              urlTextField};
                 }
             } else {
                 if (m_versionSlider!=null) {
                     values=new JComponent[] {m_labelTextField,
 											 m_MIMETextField,
                     						 m_formatURITextField,
 											 m_altIDsTextField, 
                                              urlTextField};
                 } else {
                     JTextArea cDateTextArea=new JTextArea(m_ds.getCreateDate());
                     cDateTextArea.setBackground(Administrator.BACKGROUND_COLOR);
                     cDateTextArea.setEditable(false);
                     values=new JComponent[] {cDateTextArea,
                                              m_labelTextField,
 											 m_MIMETextField,
                                              m_formatURITextField,
 											 m_altIDsTextField,  
                                              urlTextField};
                 }
             }
 
             JPanel fieldPane=new JPanel();
             GridBagLayout grid=new GridBagLayout();
             fieldPane.setLayout(grid);
             addLabelValueRows(labels, values, 
                     grid, fieldPane);
             add(fieldPane, BorderLayout.NORTH);
 
             // Do the buttons!
             JPanel actionPane=new JPanel();
             actionPane.setLayout(new FlowLayout());
             if (m_canEdit) {
                 // we know it's editable... add a button
                 m_editButton.addActionListener(new ActionListener() {
                     public void actionPerformed(ActionEvent evt) {
                         // add the editor, and disable the button
                         try {
                         startEditor();
                         } catch (Exception e) {
                         	Administrator.showErrorDialog(Administrator.getDesktop(), "Content Edit Error", 
                         			e.getMessage(), e);
                         }
                     }
                 });
                 actionPane.add(m_editButton);
                 // if a *separate* viewer is also available, add a view button
                 if (!ContentHandlerFactory.viewerIsEditor(ds.getMIMEType())) {
                     m_separateViewButton=new JButton("View");
                     Administrator.constrainHeight(m_separateViewButton);
                     m_separateViewButton.addActionListener(new ActionListener() {
                         public void actionPerformed(ActionEvent evt) {
                             // open a separate viewing window, using the content
                             // from the *server* if the text is "View", and the
                             // content from the editor if the text is "Preview"
                             try {
                                 startSeparateViewer();
                             } catch (Exception e) {
                             	Administrator.showErrorDialog(Administrator.getDesktop(), "Content View Error", 
                             			e.getMessage(), e);
                             }
                         }
                     });
                     actionPane.add(m_separateViewButton);
                 }
             } else if (m_canView) {
                 // it's not editable, but it's VIEWable... add a button
                 m_viewButton.addActionListener(new ActionListener() {
                     public void actionPerformed(ActionEvent evt) {
                         // add the viewer, and disable the view button
                         try {
                         startViewer();
                         } catch (Exception e) {
                         	Administrator.showErrorDialog(Administrator.getDesktop(), "Content View Error", 
                         			e.getMessage(), e);
                         }
                     }
                 });
                 actionPane.add(m_viewButton);                 
             }
             // should we add the Import button?  If we can set content, yes.
             if ((X || M) && (!noEdits)
                     && ( !ds.getID().equals("METHODMAP") 
                         && !ds.getID().equals("DSINPUTSPEC") 
                         && !ds.getID().equals("WSDL") 
                    ) ) {
                 actionPane.add(m_importButton);
                 m_importButton.addActionListener(new ActionListener() {
                     public void actionPerformed(ActionEvent evt) {
                         // remember what we did so we can back out if needed
                         boolean startedEditor=false;
                         boolean startedViewer=false;
                         try {
                             // prompt for the file or URL:
                             ImportDialog imp=new ImportDialog();
 
                             if (imp.file!=null) {
                                 File file = imp.file;
                                 String url=imp.url;
                                 Administrator.setLastDir(file.getParentFile()); // remember the dir for next time
                                 if (m_canEdit) {
                                     if (m_editButton.isEnabled()) {
                                         startEditor();
                                         startedEditor=true;
                                     } 
                                     // set content of existing edit widget
                                     m_editor.setContent(new FileInputStream(file));
                                 } else if (m_canView) {
                                     if (m_viewButton.isEnabled()) {
                                         startViewer();
                                         startedViewer=true;
                                     }
                                     // set the content of the existing viewer widget
                                     m_viewer.setContent(new FileInputStream(file));
                                     // if that went ok, then remember the file
                                     m_importFile=file;
                                     // and send the signal
                                     dataChangeListener.dataChanged();
                                 } else {
                                     // can't view or edit, so put a label
                                     if (url!=null) {
                                         m_importLabel=new JLabel("Will import " + url);
                                     } else {
                                         m_importLabel=new JLabel("Will import " + file.getPath());
                                     }
                                     add(m_importLabel, BorderLayout.CENTER);
                                     validate();
                                     // if that went ok, then remember the file
                                     m_importFile=file;
                                     // and send the signal
                                     dataChangeListener.dataChanged();
                                 }
                             }
                         } catch (Exception e) {
                             if (startedEditor) {
                                 // restore the original ui state
                                 m_editButton.setEnabled(true);
                                 remove(m_editor.getComponent());
                                 m_editor=null;
                             }
                             if (startedViewer) {
                                 // restore the original ui state
                                 m_viewButton.setEnabled(true);
                                 remove(m_viewer.getComponent());
                                 m_viewer=null;
                             }
                         	Administrator.showErrorDialog(Administrator.getDesktop(), "Content Import Failure", 
                         			e.getMessage(), e);
                         }
                     }
                 });
             }
             // export is always possible!
             actionPane.add(m_exportButton);
             m_exportButton.addActionListener(new ExportActionListener(m_ds));
             // and purge is, too
             JButton purgeButton=new JButton("Purge...");
             Administrator.constrainHeight(purgeButton);
             purgeButton.addActionListener(m_purgeButtonListener);
             purgeButton.setActionCommand(m_ds.getCreateDate());
             actionPane.add(purgeButton);
             add(actionPane, BorderLayout.SOUTH);
         }
 
         public Datastream getDatastream() {
             return m_ds;
         }
 
         /**
          * Bring up the editing pane, initialized with this datastream's
          * content.
          */
         private void startEditor() throws Exception {
                             m_editor=ContentHandlerFactory.getEditor(
                                     m_ds.getMIMEType(), 
                                     Administrator.DOWNLOADER.getDatastreamContent(
                                             m_pid, m_ds.getID(), 
                                             m_ds.getCreateDate()));
                             m_editor.setContentChangeListener(dataChangeListener);
                             add(m_editor.getComponent(), BorderLayout.CENTER);
                             m_editButton.setEnabled(false);
                             validate();
         }
 
         public void startViewer() throws Exception {
                             m_viewer=ContentHandlerFactory.getViewer(
                                     m_ds.getMIMEType(), 
                                     Administrator.DOWNLOADER.getDatastreamContent(
                                             m_pid, m_ds.getID(), 
                                             m_ds.getCreateDate()));
                             add(m_viewer.getComponent(), BorderLayout.CENTER);
                             m_viewButton.setEnabled(false);
                             validate();
         }
 
         public void startSeparateViewer() throws Exception {
             InputStream contentStream;
             if (m_separateViewButton.getText().equals("Preview")) {
                 // the editor will provide the content
                 contentStream=m_editor.getContent();
             } else {
                 // the server will provide the content
                 contentStream=Administrator.DOWNLOADER.getDatastreamContent(
                         m_pid, m_ds.getID(), m_ds.getCreateDate());
             }
             ContentViewer separateViewer=ContentHandlerFactory.getViewer(
                     m_ds.getMIMEType(), contentStream);
             // now open up a new JInternalFrame and put the v.getComponent()
             // in it.
             JInternalFrame viewFrame=new JInternalFrame(
                     m_separateViewButton.getText() + "ing " + m_ds.getID() 
                     + " datastream from object " + m_pid, true, true, true, true);
             //viewFrame.setFrameIcon(new ImageIcon(this.getClass().getClassLoader().getResource("images/standard/general/Edit16.gif")));
             JPanel myPanel=new JPanel();
             myPanel.setLayout(new BorderLayout());
             myPanel.add(separateViewer.getComponent(), BorderLayout.CENTER);
             viewFrame.getContentPane().add(myPanel);
             viewFrame.setSize(720,520);
             Administrator.getDesktop().add(viewFrame);
             viewFrame.setVisible(true);
             viewFrame.toFront();
         }
 
 		public void saveChanges(String state, 
 		                        //String mimeType, 
 		                        //String formatURI,
 		                        //String[] altIDs,
 		                        String logMessage,
 		                        boolean force)
 	            throws Exception {
 	        String label=m_labelTextField.getText().trim();
 			String mimeType=m_MIMETextField.getText().trim();
 	        String formatURI=m_formatURITextField.getText().trim();
 			String[] altIDs = m_altIDsTextField.getText().trim().split(" ");
 			if (X) {
 			    byte[] content=new byte[0];
 			    if (m_editor!=null && m_editor.isDirty()) {
 				    InputStream in=m_editor.getContent();
 				    ByteArrayOutputStream out=new ByteArrayOutputStream();
 					StreamUtility.pipeStream(in, out, 4096);
 					content=out.toByteArray();
 				}
 			    Administrator.APIM.modifyDatastreamByValue(m_pid, 
 			                                               m_ds.getID(), 
 	                                                       altIDs,
 			                                               label, 
 	                                                       true, // DEFAULT_VERSIONABLE
 	                                                       mimeType,
 	                                                       formatURI,
 			                                               content, 
 			                                               state,
 			                                               logMessage, 
 	                                                       force);
 			} else if (M) {
 	            String loc=null; // if not set, server will not change content
 	            if (m_importFile!=null) {
 	                // upload the import file, getting a temporary ref
 	                loc=Administrator.UPLOADER.upload(m_importFile);
 	            } else if (m_editor!=null && m_editor.isDirty()) {
 	                // They've edited managed content that came up in an editor... 
 	                // use its content
 	                loc=Administrator.UPLOADER.upload(m_editor.getContent());
 	            }
 	            Administrator.APIM.modifyDatastreamByReference(m_pid, 
 	                                                           m_ds.getID(),
 	                                                           altIDs,
 	                                                           label, 
 	                                                           true, // DEFAULT_VERSIONABLE
 	                                                           mimeType,
 	                                                           formatURI,
 	                                                           loc, 
 	                                                           state,
 	                                                           logMessage, 
 	                                                           force);
 	        } else {
 			    // external ref or redirect
 	            Administrator.APIM.modifyDatastreamByReference(m_pid, 
 	                                                           m_ds.getID(),
 	                                                           altIDs,
 	                                                           label, 
 	                                                           true, // DEFAULT_VERSIONABLE
 	                                                           mimeType,
 	                                                           formatURI,
 	                                                           m_locationTextField.getText(), 
 	                                                           state,
 	                                                           logMessage, 
 	                                                           force);
 			}
 	    }
         public boolean isDirty() {
             if (m_editor!=null) {
                 if (m_editor.isDirty()) {
                     // ensure the button label for view is right, if it's there
                     if (m_separateViewButton!=null) {
                         if (m_separateViewButton.getText().equals("View")) {
                             m_separateViewButton.setText("Preview");
                         }
                     }
                     return true;
                 } else {
                     // ensure the button label for view is right, if it's there
                     if (m_separateViewButton!=null) {
                         if (m_separateViewButton.getText().equals("Preview")) {
                             m_separateViewButton.setText("View");
                         }
                     }
                 }
             }
             if (!m_ds.getLabel().equals(m_labelTextField.getText())) {
                  return true;
             }
 			if (!m_origMIME.equals(m_MIMETextField.getText())) {
 				return true;
 			}
 			if (!m_origFormatURI.equals(m_formatURITextField.getText())) {
 				return true;
 			}
 			if (!m_origAltIDs.equals(m_altIDsTextField.getText())) {
 				return true;
 			}
             if (m_locationTextField!=null 
                     && !m_locationTextField.getText().equals(m_ds.getLocation())) {
                  return true;
             }
             if (m_importFile!=null) return true;
             return false;
         }
 
         public void undoChanges() {
             m_labelTextField.setText(m_origLabel);
 			m_MIMETextField.setText(m_origMIME);
 			m_formatURITextField.setText(m_origFormatURI);
 			m_altIDsTextField.setText(m_origAltIDs);
             if (m_locationTextField!=null) m_locationTextField.setText(m_ds.getLocation());
             if (m_editor!=null) m_editor.undoChanges();
             if (m_importFile!=null) {
                 m_importFile=null;
                 // and remove the viewer if it's up, and re-enable the view
                 // button
                 if (m_canView) {
                     // must be viewing, so remove the viewer and re-enable the
                     // view button
                     m_viewButton.setEnabled(true);
                     remove(m_viewer.getComponent());
                     m_viewer=null;
                 } else {
                     // remove the JLabel
                     remove(m_importLabel);
                     m_importLabel=null;
                 }
             }
         }
     }
 
     public class PriorVersionPane
             extends JPanel {
 
         private boolean X;
         private boolean M;
         private boolean E;
         private boolean R;
 
         private ContentViewer v;
         private Datastream m_ds;
 		private String m_priorLabel;
 		private String m_priorMIME;
         private String m_priorFormatURI;
         private String m_priorAltIDs;
 
         public PriorVersionPane(Datastream ds) {
             m_ds=ds;
 			// clean up attribute values for presentation in text boxes...
 			// set a null ds label to ""
 			m_priorLabel=m_ds.getLabel();
 			if (m_priorLabel==null) m_priorLabel="";
 			// set a null MIME type to ""
 			m_priorMIME=m_ds.getMIMEType();
 			if (m_priorMIME==null) m_priorMIME="";
 			// set a null format_uri to ""
 			m_priorFormatURI=m_ds.getFormatURI();
 			if (m_priorFormatURI==null) m_priorFormatURI="";
 			// create a string from alt ids array 
 			m_priorAltIDs = "";
 			String[] altIDs = m_ds.getAltIDs();
 			if (altIDs != null) {
 				for (int z = 0; z < altIDs.length; z++) {
 					if (z > 0) m_priorAltIDs += " ";
 					m_priorAltIDs += altIDs[z];
 				}
 			}
             
             if (ds.getControlGroup().toString().equals("X")) {
                 X=true;
             } else if (ds.getControlGroup().toString().equals("M")) {
                 M=true;
             } else if (ds.getControlGroup().toString().equals("E")) {
                 E=true;
             } else if (ds.getControlGroup().toString().equals("R")) {
                 R=true;
             }
             setLayout(new BorderLayout());
             // NORTH: fieldPanel
             // disabled labels and values
             // ds label...
             JLabel labelLabel=new JLabel("Label");
             labelLabel.setMinimumSize(m_labelDims);
             JTextField labelValue=new JTextField();
             labelValue.setText(m_priorLabel);
             labelValue.setEditable(false);
 			// ds MIME type...
 			JLabel MIMELabel=new JLabel("MIME Type");
 			MIMELabel.setMinimumSize(m_labelDims);
 			JTextField MIMEValue=new JTextField();
 			MIMEValue.setText(m_priorMIME);
 			MIMEValue.setEditable(false);
             // ds format URI...
 			JLabel formatURILabel=new JLabel("Format URI");
 			formatURILabel.setMinimumSize(m_labelDims);
 			JTextField formatURIValue=new JTextField();
 			formatURIValue.setText(m_priorFormatURI);
 			formatURIValue.setEditable(false);
 			// ds alternate ids...
 			JLabel altIDsLabel=new JLabel("Alternate IDs");
 			altIDsLabel.setMinimumSize(m_labelDims);
 			JTextField altIDsValue=new JTextField();
 			altIDsValue.setText(m_priorAltIDs);
 			altIDsValue.setEditable(false);
 			// ds Fedora URL...
             JLabel urlLabel=new JLabel("Fedora URL");
             urlLabel.setPreferredSize(m_labelDims);
             JTextField urlTextField=new JTextField(getFedoraURL(m_ds, true));
             urlTextField.setEditable(false);  // so they can copy, but not modify
 
             JLabel[] labels;
             JComponent[] values;
             if (E || R) {
                 labels=new JLabel[] {labelLabel, MIMELabel, formatURILabel, altIDsLabel, new JLabel("Location"), urlLabel};
                 JTextField refValue=new JTextField();
                 refValue.setText(ds.getLocation());
                 refValue.setEditable(false);
                 values=new JComponent[] {labelValue, MIMEValue, formatURIValue, altIDsValue, refValue, urlTextField};
             } else {
                 labels=new JLabel[] {labelLabel, MIMELabel, formatURILabel, altIDsLabel, urlLabel};
                 values=new JComponent[] {labelValue, MIMEValue, formatURIValue, altIDsValue, urlTextField};
             }
 
             JPanel fieldPanel=new JPanel();
             GridBagLayout fieldGrid=new GridBagLayout();
             fieldPanel.setLayout(fieldGrid);
             addLabelValueRows(labels, values, 
                        fieldGrid, fieldPanel);
             add(fieldPanel, BorderLayout.NORTH);
 
             // SOUTH: buttonPanel
             JPanel buttonPanel=new JPanel();
             buttonPanel.setLayout(new FlowLayout());
             if (ContentHandlerFactory.hasViewer(ds.getMIMEType())) {
                 JButton viewButton=new JButton("View");
                 Administrator.constrainHeight(viewButton);
                 // CENTER: populated on view
                 viewButton.addActionListener(new ActionListener() {
                     public void actionPerformed(ActionEvent evt) {
                         JButton btn=(JButton) evt.getSource();
                         try {
                             ContentViewer v=ContentHandlerFactory.getViewer(
                                     m_ds.getMIMEType(), 
                                     Administrator.DOWNLOADER.getDatastreamContent(
                                             m_pid, m_ds.getID(), 
                                             m_ds.getCreateDate()
                                     ) );
                             add(v.getComponent(), BorderLayout.CENTER);
                             btn.setEnabled(false);
                             validate();
                         } catch (Exception e) {
                         	Administrator.showErrorDialog(Administrator.getDesktop(), "Content View Failure", 
                         			e.getMessage(), e);
                         }
                     }
                 });
                 buttonPanel.add(viewButton);
             }
             JButton exportButton=new JButton("Export...");
             Administrator.constrainHeight(exportButton);
             exportButton.addActionListener(new ExportActionListener(m_ds));
             buttonPanel.add(exportButton);
             JButton purgeButton=new JButton("Purge...");
             Administrator.constrainHeight(purgeButton);
             purgeButton.addActionListener(m_purgeButtonListener);
             purgeButton.setActionCommand(m_ds.getCreateDate());
             buttonPanel.add(purgeButton);
             add(buttonPanel, BorderLayout.SOUTH);
 
         }
 
         public Datastream getDatastream() {
             return m_ds;
         }
 
     }
 
     protected class PurgeButtonListener
             implements ActionListener {
 
         Datastream[] m_versions;
         Object[] m_dateStrings;
         HashMap m_dsIndex;
 
         public PurgeButtonListener(Datastream[] versions) {
             m_versions=versions;
             m_dateStrings=new Object[versions.length];
             m_dsIndex=new HashMap();
             for (int i=0; i<versions.length; i++) {
                 m_dateStrings[i]=versions[i].getCreateDate();
                 m_dsIndex.put(versions[i].getCreateDate(), new Integer(i));
             }
         }
 
         public void actionPerformed(ActionEvent evt) {
             int sIndex=0;
             boolean canceled=false;
             if (m_versions.length>1) {
                 String defaultValue=evt.getActionCommand(); // default date string
                 String selected=(String) JOptionPane.showInputDialog(
                         Administrator.getDesktop(),
                         "Choose the latest version to purge:",
                         "Purge version(s) from datastream " + m_versions[0].getID(),
                         JOptionPane.QUESTION_MESSAGE,
                         null,
                         m_dateStrings,
                         defaultValue);
                 if (selected==null) {
                     canceled=true;
                 } else {
                     sIndex=((Integer) m_dsIndex.get(selected)).intValue();
                 }
             }
             if (!canceled) {
                 // do warning
                 boolean removeAll=false;
                 String detail;
                 if (sIndex==0) {
                     detail="the entire datastream.";
                     removeAll=true;
                 } else if (sIndex==m_versions.length-1) {
                     detail="the oldest version of the datastream.";
                 } else {
                     int num=m_versions.length-sIndex;
                     detail="the oldest " + num + " versions of the datastream.";
                 }
                 int n = JOptionPane.showOptionDialog(Administrator.getDesktop(),
                         "This will permanently remove " + detail + "\n"
                         + "Are you sure you want to do this?",
                         "Confirmation",
                         JOptionPane.YES_NO_OPTION,
                         JOptionPane.WARNING_MESSAGE,
                         null,     //don't use a custom Icon
                         new Object[] {"Yes", "No"},  //the titles of buttons
                         "Yes"); //default button title
                 if (n==0) {
                     try {
                         Administrator.APIM.purgeDatastream(m_pid, 
                                 m_versions[sIndex].getID(),
                                 m_versions[sIndex].getCreateDate(),
                                 "DatastreamPane generated this logMessage.", // DEFAULT_LOGMESSAGE
                                 false); // DEFAULT_FORCE_PURGE
                         if (removeAll) {
                             m_owner.remove(m_versions[0].getID());
                             m_done=true;
                         } else {
                             m_owner.refresh(m_versions[0].getID());
                             m_done=true;
                         }
                     } catch (Exception e) {
                     	Administrator.showErrorDialog(Administrator.getDesktop(), "Purge error", 
                     			e.getMessage(), e);
                     }
                 }
             }
         }
     }
 
     public class ExportActionListener 
 	        implements ActionListener {
 
 		Datastream m_ds;
 
         public ExportActionListener(Datastream ds) {
 			m_ds=ds;
 		}
         public void actionPerformed(ActionEvent evt) {
             try {
         	    FileDialog dlg=new FileDialog(Administrator.INSTANCE, 
 	                                          "Export Datastream Content to...",
 		                                   	FileDialog.SAVE);
                 if (Administrator.getLastDir()!=null) {
             		dlg.setDirectory(Administrator.getLastDir().getPath());
                 }
                 dlg.setVisible(true);
                 if (dlg.getFile()!=null) {
                     File file = new File(new File(dlg.getDirectory()), dlg.getFile());
                     System.out.println("Exporting to " + file.getPath());
                     Administrator.setLastDir(file.getParentFile()); // remember the dir for next time
                     Administrator.DOWNLOADER.getDatastreamContent(m_pid, 
                             m_ds.getID(), m_ds.getCreateDate(),
                             new FileOutputStream(file));
                 }
             } catch (Exception e) {
             	Administrator.showErrorDialog(Administrator.getDesktop(), "Content Export Failure", 
             			e.getMessage(), e);
             }
         }
     }
 
 }
