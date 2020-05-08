 package fedora.client.objecteditor;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.io.*;
 import java.util.*;
 import javax.swing.*;
 import javax.swing.event.*;
 import javax.swing.table.*;
 
 import fedora.server.types.gen.Datastream;
 import fedora.server.types.gen.DatastreamBinding;
 import fedora.server.types.gen.DatastreamBindingMap;
 import fedora.server.types.gen.Disseminator;
 
 import fedora.client.Administrator;
 import fedora.client.actions.ViewObject;
 import fedora.client.objecteditor.types.DatastreamInputSpec;
 import fedora.client.objecteditor.types.MethodDefinition;
 import fedora.client.objecteditor.types.ParameterDefinition;
 
 /**
  * Pane for disseminator.
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
  */
 public class DisseminatorPane
         extends EditingPane
         implements ChangeListener {
 
     private String m_pid;
     private Disseminator[] m_versions;
     private DisseminatorsPane m_owner;
     private Disseminator m_mostRecent;
     private JComboBox m_stateComboBox;
     private JComboBox m_methodComboBox;
     private boolean m_done;
     private JSlider m_versionSlider;
     private Hashtable[] m_labelTables;
     private CardLayout m_versionCardLayout;
     private CurrentVersionPane m_currentVersionPane;
     private PurgeButtonListener m_purgeButtonListener;
     private JPanel m_valuePane;
     private JPanel m_methodCard;
     private CardLayout m_methodCardLayout;
     private JPanel m_methodDescCard;
     private CardLayout m_methodDescCardLayout;
     private JTextField m_bDefLabelTextField;
     private Dimension m_labelDims;
     private ObjectEditorFrame m_gramps;
     private boolean m_didSlider;
     private JComboBox m_bMechComboBox;
     private DisseminatorPane m_editingPane; // me
     private JTextArea m_dtLabel;
     private JPanel m_dateLabelAndValue;
 
     public DisseminatorPane(ObjectEditorFrame gramps, String pid,
             Disseminator[] versions, DisseminatorsPane owner)
             throws Exception {
         super(gramps, owner, versions[0].getID());
         m_editingPane=this;
         m_gramps=gramps;
         m_pid=pid;
         m_owner=owner;
         m_versions=versions;
         m_mostRecent=versions[0];
 
         // set up the common pane
         // first get width and height we'll use for the labels on the left
         m_labelDims=new JLabel("Mechanism").getPreferredSize();
         JLabel label1=new JLabel("State");
         label1.setPreferredSize(m_labelDims);
         JLabel label2=new JLabel("Behavior");
         label2.setPreferredSize(m_labelDims);
         JLabel[] left=new JLabel[] {label1, label2};
         m_stateComboBox=new JComboBox(new String[] {"Active", "Inactive", "Deleted"});
         Administrator.constrainHeight(m_stateComboBox);
         if (m_mostRecent.getState().equals("A")) {
             m_stateComboBox.setSelectedIndex(0);
             m_stateComboBox.setBackground(Administrator.ACTIVE_COLOR);
         } else if (m_mostRecent.getState().equals("I")) {
             m_stateComboBox.setSelectedIndex(1);
             m_stateComboBox.setBackground(Administrator.INACTIVE_COLOR);
         } else {
             m_stateComboBox.setSelectedIndex(2);
             m_stateComboBox.setBackground(Administrator.DELETED_COLOR);
         }
         Administrator.constrainHeight(m_stateComboBox);
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
         JButton bDefButton=new JButton("Open");
         bDefButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 new ViewObject(m_mostRecent.getBDefPID()).launch();
             }
         });
 
         JPanel bDefInfo=new JPanel(new BorderLayout());
 
         m_bDefLabelTextField=new JTextField(m_mostRecent.getBDefLabel());
         m_bDefLabelTextField.getDocument().addDocumentListener(dataChangeListener);
         if (m_mostRecent.getState().equals("D")) {
             m_bDefLabelTextField.setEnabled(false);
         }
         Administrator.constrainHeight(m_bDefLabelTextField);
         JPanel bDefLabelPanel=new JPanel(new BorderLayout());
         bDefLabelPanel.setBorder(BorderFactory.createEmptyBorder(0,4,0,4));
         bDefLabelPanel.add(m_bDefLabelTextField, BorderLayout.CENTER);
         JTextArea definedBy=new JTextArea("is defined by " + m_mostRecent.getBDefPID());
         definedBy.setBackground(Administrator.BACKGROUND_COLOR);
         definedBy.setEditable(false);
         bDefInfo.add(definedBy, BorderLayout.WEST);
         bDefInfo.add(bDefLabelPanel, BorderLayout.CENTER);
         Administrator.constrainHeight(bDefButton);
         bDefInfo.add(bDefButton, BorderLayout.EAST);
         JPanel bDefAll=new JPanel(new BorderLayout());
         bDefAll.add(bDefInfo, BorderLayout.NORTH);
         bDefAll.add(new BehaviorDescriptionPanel(m_mostRecent.getBDefPID(), null), BorderLayout.SOUTH);
 
         // top common
         JComponent[] right=new JComponent[] {m_stateComboBox, bDefAll};
         GridBagLayout topCommonLayout=new GridBagLayout();
         JPanel topCommonPane=new JPanel(topCommonLayout);
         Util.addRows(left, right, topCommonLayout, topCommonPane, true, false);
 
         // ahh well
         JPanel commonPane=new JPanel(new BorderLayout());
         commonPane.add(topCommonPane, BorderLayout.NORTH);
 
         m_purgeButtonListener=new PurgeButtonListener(versions);
 
         // set up the version pane, with the slider if needed
         if (versions.length>1) {
             m_didSlider=true;
             m_versionSlider=new JSlider(JSlider.HORIZONTAL, 0, versions.length-1, 0);
             m_versionSlider.addChangeListener(this);
             m_versionSlider.setMajorTickSpacing(1);
             m_versionSlider.setSnapToTicks(true);
             m_versionSlider.setPaintTicks(true);
             m_versionSlider.setPaintLabels(false);
         }
 
 
         // CENTER: m_valuePane(one card for each version)
         m_valuePane=new JPanel();
         m_versionCardLayout=new CardLayout();
         m_valuePane.setLayout(m_versionCardLayout);
         JPanel[] valuePanes=new JPanel[versions.length];
         // CARD: valuePanes[0](versionValuePane, versionActionPane)
         m_currentVersionPane=new CurrentVersionPane(m_mostRecent);
         valuePanes[0]=m_currentVersionPane;
         m_valuePane.add(valuePanes[0], "0");
         // CARD: valuePanes[1 to i](versionValuePane, versionActionPane)
         for (int i=1; i<versions.length; i++) {
             valuePanes[i]=new PriorVersionPane(versions[i]);
             m_valuePane.add(valuePanes[i], "" + i);
         }
         JPanel versionPane=new JPanel();
         // add it all to the versionPane, putting slider at north if needed
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
 
         // finally, put the panes on the mainPane
         mainPane.setLayout(new BorderLayout());
         mainPane.add(commonPane, BorderLayout.NORTH);
         mainPane.add(versionPane, BorderLayout.CENTER);
     }
 
     public void stateChanged(ChangeEvent e) {
        JSlider source=(JSlider)e.getSource();
        if (!source.getValueIsAdjusting()) {
            // make sure the selected version's date is shown...
            m_dtLabel.setText(m_versions[source.getValue()].getCreateDate() + " ");
            // and that the selected version is shown
            m_versionCardLayout.show(m_valuePane, "" + source.getValue());
            // set the new text of m_bDefLabelTextField
            m_bDefLabelTextField.setText(m_versions[source.getValue()].getBDefLabel());
            // and disable or enable it as necessary
            if (source.getValue()==0) {
                m_bDefLabelTextField.setEditable(true);
            } else {
                m_bDefLabelTextField.setEditable(false);
            }
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
         if (m_currentVersionPane.isDirty()) return true;
         return false;
     }
 
     public void saveChanges(String logMessage) throws Exception {
         String state=null;
         int i=m_stateComboBox.getSelectedIndex();
         if (i==0)
            state="A";
         if (i==1)
            state="I";
         if (i==2)
            state="D";
         if (m_currentVersionPane.isDirty()) {
             m_currentVersionPane.saveChanges(state, logMessage);
         } else {
             Administrator.APIM.setDisseminatorState(m_pid, m_mostRecent.getID(),
                     state, logMessage);
         }
     }
 
     /**
      * Called when changes to the server succeeded.
      * This method can do anything, but it should at least ensure that the
      * model and view are in-sync with each other (accurately reflecting the
      * current state of the server).
      */
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
 
     public class CurrentVersionPane
             extends JPanel
             implements PotentiallyDirty {
 
         private CardLayout m_bindingsCard;
         private JPanel m_stackedBindingPane;
         private Disseminator m_diss;
         private JTextField m_labelTextField;
         private JTextField m_bMechLabelTextField;
         private Map m_bMechLabels;
         private Map m_inputSpecs;
         private Map m_bindingPanes;
         private String m_lastSelectedBMech;
 
         public CurrentVersionPane(Disseminator diss) throws IOException {
             m_diss=diss;
 
             m_bindingPanes=new HashMap();
 
             // prepare by getting bmech labels and binding specs
             m_bMechLabels=Util.getBMechLabelMap(m_diss.getBDefPID());
             m_bMechLabels.put(m_diss.getBMechPID(), m_diss.getBMechLabel());
             m_inputSpecs=Util.getInputSpecMap(m_bMechLabels.keySet());
 
             // top panel is for labels and such
             JLabel label1=new JLabel("Label");
             label1.setPreferredSize(m_labelDims);
             JLabel label2=new JLabel("Mechanism");
             label2.setPreferredSize(m_labelDims);
             JLabel[] left;
             if (m_didSlider) {
                 left=new JLabel[] {label1, label2};
             } else {
                 JLabel createdLabel=new JLabel("Created");
                 createdLabel.setPreferredSize(m_labelDims);
                 left=new JLabel[] {createdLabel, label1, label2};
             }
             m_labelTextField=new JTextField(m_diss.getLabel());
             m_labelTextField.getDocument().addDocumentListener(dataChangeListener);
             m_labelTextField.setEditable(true);
             m_labelTextField.setEnabled(!m_diss.getState().equals("D"));
 
             // make the list of bmech pids for the dropdown,
             // ensuring that the first one listed is the one that
             // the disseminator currently uses.
             String[] bMechPIDs=new String[m_bMechLabels.keySet().size()];
             Iterator bMechIter=m_bMechLabels.keySet().iterator();
             int bMechNum=0;
             bMechPIDs[bMechNum]=m_diss.getBMechPID();
             while (bMechIter.hasNext()) {
                 String mechPID=(String) bMechIter.next();
                 if (!mechPID.equals(m_diss.getBMechPID())) {
                     bMechNum++;
                     bMechPIDs[bMechNum]=mechPID;
                 }
             }
             m_bMechComboBox=new JComboBox(bMechPIDs);
             Administrator.constrainHeight(m_bMechComboBox);
             m_lastSelectedBMech=m_diss.getBMechPID();
             m_bMechComboBox.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent evt) {
                     // put the currently-entered label into m_bMechLabels,
                     m_bMechLabels.put(m_lastSelectedBMech, m_bMechLabelTextField.getText());
                     // then switch to the appropriate panel and set the
                     // label text
                     String bMechPID=(String) m_bMechComboBox.getSelectedItem();
                     m_bindingsCard.show(m_stackedBindingPane, bMechPID);
                     m_bMechLabelTextField.setText((String) m_bMechLabels.get(bMechPID));
                     // tell it to update the buttons appropriately
                     DatastreamBindingPane pane=(DatastreamBindingPane) m_bindingPanes.get(bMechPID);
                     pane.fireDataChanged();
                     // then remember the selected value for next time
                     m_lastSelectedBMech=bMechPID;
                 }
             });
 
             JButton bMechButton=new JButton("Open");
             Administrator.constrainHeight(bMechButton);
             bMechButton.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent evt) {
                     new ViewObject((String) m_bMechComboBox.getSelectedItem()).launch();
                 }
             });
 
             m_bMechLabelTextField=new JTextField(m_diss.getBMechLabel());
             m_bMechLabelTextField.getDocument().addDocumentListener(dataChangeListener);
             m_bMechLabelTextField.setEditable(true);
             m_bMechLabelTextField.setEnabled(!m_diss.getState().equals("D"));
 
             JPanel bMechInfo=new JPanel(new BorderLayout());
             bMechInfo.add(m_bMechComboBox, BorderLayout.WEST);
             JPanel bMechLabelPanel=new JPanel(new BorderLayout());
             bMechLabelPanel.setBorder(BorderFactory.createEmptyBorder(0,4,0,4));
             bMechLabelPanel.add(m_bMechLabelTextField, BorderLayout.CENTER);
             bMechInfo.add(bMechLabelPanel, BorderLayout.CENTER);
             bMechInfo.add(bMechButton, BorderLayout.EAST);
 
             JComponent[] right;
             if (m_didSlider) {
                 right=new JComponent[] {m_labelTextField, bMechInfo};
             } else {
                 JTextArea cDateArea=new JTextArea(m_diss.getCreateDate());
                 cDateArea.setBackground(Administrator.BACKGROUND_COLOR);
                 cDateArea.setEditable(false);
                 right=new JComponent[] {cDateArea,
                                         m_labelTextField,
                                         bMechInfo};
             }
             GridBagLayout topGridBag=new GridBagLayout();
             JPanel topPanel=new JPanel(topGridBag);
             addLabelValueRows(left, right, topGridBag, topPanel);
 
             // middle panel is for displaying the datastream binding
             JPanel middlePanel=new JPanel(new BorderLayout());
             middlePanel.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
 
             m_bindingsCard=new CardLayout();
             m_stackedBindingPane=new JPanel(m_bindingsCard);
 
             // add a binding pane for each possible bmech to the card,
             // then make sure the disseminator's initial bmech's binding pane is
             // the first selected one.
             Iterator specMapIter=m_inputSpecs.keySet().iterator();
             while (specMapIter.hasNext()) {
                 String bMechPID=(String) specMapIter.next();
                 DatastreamInputSpec spec=
                         (DatastreamInputSpec) m_inputSpecs.get(bMechPID);
                 DatastreamBinding[] bindings;
                 if (bMechPID.equals(m_diss.getBMechPID())) {
                     bindings=m_diss.getDsBindMap().getDsBindings();
                 } else {
                     bindings=new DatastreamBinding[0];
                 }
                 DatastreamBindingPane dsBindingPane=new DatastreamBindingPane(
                         m_gramps.getCurrentDatastreamVersions(),
                         bindings,
                         bMechPID, spec, null, m_editingPane);
                 m_gramps.addDatastreamListener(dsBindingPane);
                 m_bindingPanes.put(bMechPID, dsBindingPane);
                 m_stackedBindingPane.add(dsBindingPane, bMechPID);
             }
             m_bindingsCard.show(m_stackedBindingPane, m_diss.getBMechPID());
 
             middlePanel.add(m_stackedBindingPane, BorderLayout.CENTER);
 
             JLabel bindingsLabel=new JLabel("Bindings");
             bindingsLabel.setPreferredSize(m_labelDims);
             JPanel bindingsLabelPane=new JPanel(new BorderLayout());
             bindingsLabelPane.setBorder(BorderFactory.createEmptyBorder(0,0,0,8));
             bindingsLabelPane.add(bindingsLabel, BorderLayout.NORTH);
             middlePanel.add(bindingsLabelPane, BorderLayout.WEST);
            // bottom panel is for the purge button
             JPanel bottomPanel=new JPanel(new FlowLayout());
             JButton purgeButton=new JButton("Purge...");
             Administrator.constrainHeight(purgeButton);
             purgeButton.setActionCommand(m_diss.getCreateDate());
             purgeButton.addActionListener(m_purgeButtonListener);
             bottomPanel.add(purgeButton);
 
             setLayout(new BorderLayout());
             add(topPanel, BorderLayout.NORTH);
             add(middlePanel, BorderLayout.CENTER);
             add(bottomPanel, BorderLayout.SOUTH);
         }
 
         public boolean isDirty() {
            if ((m_versionSlider==null || m_versionSlider.getValue()==0) && !m_bDefLabelTextField.getText().equals(m_diss.getBDefLabel())) {
                 return true;
             }
             if (!m_labelTextField.getText().equals(m_diss.getLabel())) {
                 return true;
             }
            if (!m_bMechLabelTextField.getText().equals(m_diss.getBMechLabel())) {
                 return true;
             }
             // is the bmech the same?
             String currentBMech=(String) m_bMechComboBox.getSelectedItem();
             if (currentBMech.equals(m_diss.getBMechPID())) {
                 // if so, is the datastreambindingpane dirty?
                 DatastreamBindingPane pane=
                         (DatastreamBindingPane) m_bindingPanes.get(currentBMech);
                 if (pane.isDirty()) return true;
             } else {
                 // nope, so it's dirty
                 return true;
             }
             return false;
         }
 
         public void saveChanges(String state, String logMessage)
                 throws Exception {
             String bMechPID=(String) m_bMechComboBox.getSelectedItem();
             // create the binding map from the model...
             DatastreamBindingMap bindingMap=new DatastreamBindingMap();
             bindingMap.setDsBindMapID("hopefully this is set by the server!"); // unnecessary
             bindingMap.setDsBindMechanismPID(bMechPID);
             bindingMap.setDsBindMapLabel("Binding map for bMech object: "
                     + bMechPID);
             bindingMap.setState("A");  // unnecessary...
             bindingMap.setDsBindings( ((DatastreamBindingPane) m_bindingPanes
                                       .get(bMechPID)).getBindings() );
             // and send the request
             Administrator.APIM.modifyDisseminator(
                     m_pid,
                     m_diss.getID(),
                     bMechPID,
                     m_labelTextField.getText(),
                     m_bDefLabelTextField.getText(),
                     m_bMechLabelTextField.getText(),
                     bindingMap,
                     logMessage,
                     state);
         }
 
         public void undoChanges() {
             m_labelTextField.setText(m_diss.getLabel());
             m_bDefLabelTextField.setText(m_diss.getBDefLabel());
             m_bMechLabelTextField.setText(m_diss.getBMechLabel());
             // switch to the original bmech and reset its binding values
             m_bMechComboBox.setSelectedItem(m_diss.getBMechPID());
             m_bindingsCard.show(m_stackedBindingPane, m_diss.getBMechPID());
             // make sure we remember the selected value for next time
             m_lastSelectedBMech=m_diss.getBMechPID();
             // ok, now reset binding values
             DatastreamBindingPane pane=
                     (DatastreamBindingPane) m_bindingPanes.get(
                             m_diss.getBMechPID());
             pane.undoChanges();
         }
 
     }
 
 
     public class PriorVersionPane
             extends JPanel {
 
         private Disseminator m_diss;
 
         private JTextField m_labelTextField;
         private JTextField m_bMechLabelTextField;
 
         public PriorVersionPane(Disseminator diss) {
             m_diss=diss;
             // top panel is for labels and such
             JLabel label1=new JLabel("Label");
             label1.setPreferredSize(m_labelDims);
             JLabel label2=new JLabel("Mechanism");
             label2.setPreferredSize(m_labelDims);
             JLabel[] left=new JLabel[] {label1, label2};
             m_labelTextField=new JTextField(m_diss.getLabel());
             m_labelTextField.setEditable(false);
             // bmechButton, m_bMechLabelTextField
             JButton bMechButton=new JButton("Details...");
             Administrator.constrainHeight(bMechButton);
             bMechButton.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent evt) {
                     new ViewObject(m_diss.getBMechPID()).launch();
                 }
             });
             m_bMechLabelTextField=new JTextField(m_diss.getBMechLabel());
             m_bMechLabelTextField.setEditable(false);
             JPanel bMechInfo=new JPanel(new BorderLayout());
             bMechInfo.add(new JLabel(m_diss.getBMechPID()), BorderLayout.WEST);
             JPanel bMechLabelPanel=new JPanel(new BorderLayout());
             bMechLabelPanel.setBorder(BorderFactory.createEmptyBorder(0,4,0,4));
             bMechLabelPanel.add(m_bMechLabelTextField, BorderLayout.CENTER);
             bMechInfo.add(bMechLabelPanel, BorderLayout.CENTER);
             bMechInfo.add(bMechButton, BorderLayout.EAST);
             JComponent[] right=new JComponent[] {m_labelTextField, bMechInfo};
             GridBagLayout topGridBag=new GridBagLayout();
             JPanel topPanel=new JPanel(topGridBag);
             addLabelValueRows(left, right, topGridBag, topPanel);
             // middle panel is for displaying the datastream binding
             JPanel middlePanel=new JPanel(new BorderLayout());
             middlePanel.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
 
             SortedMap dsBindingMap=DatastreamBindingPane.getSortedBindingMap(
                     m_diss.getDsBindMap().getDsBindings());
             // one tab per binding key
             JTabbedPane bindingTabbedPane=new JTabbedPane();
             Iterator keys=dsBindingMap.keySet().iterator();
             int tabNum=-1;
             while (keys.hasNext()) {
                 tabNum++;
                 String key=(String) keys.next();
                 Set values=(Set) dsBindingMap.get(key);
                 Iterator valueIter=values.iterator();
                 Object[][] rowData=new Object[values.size()][2];
                 int i=-1;
                 while (valueIter.hasNext()) {
                     i++;
                     DatastreamBinding binding=(DatastreamBinding) valueIter.next();
                     rowData[i][0]=binding.getDatastreamID();
                     rowData[i][1]=binding.getBindLabel();
                 }
                 JTable table=new JTable(rowData,
                                         new String[] {"Datastream", "Binding Label"});
                 table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
                 table.getColumnModel().getColumn(0).setMinWidth(90);
                 table.getColumnModel().getColumn(0).setMaxWidth(90);
                 table.setEnabled(false);
                 JPanel bindingTab=new JPanel(new BorderLayout());
                 bindingTab.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
                 bindingTab.add(new JScrollPane(table), BorderLayout.CENTER);
                 bindingTabbedPane.add(key, bindingTab);
                 bindingTabbedPane.setBackgroundAt(tabNum, Administrator.DEFAULT_COLOR);
             }
             JLabel bindingsLabel=new JLabel("Bindings");
             bindingsLabel.setPreferredSize(m_labelDims);
             JPanel bindingsLabelPane=new JPanel(new BorderLayout());
             bindingsLabelPane.setBorder(BorderFactory.createEmptyBorder(0,0,0,8));
             bindingsLabelPane.add(bindingsLabel, BorderLayout.NORTH);
             middlePanel.add(bindingsLabelPane, BorderLayout.WEST);
             middlePanel.add(bindingTabbedPane, BorderLayout.CENTER);
             // bottom panel is for the purge button
             JPanel bottomPanel=new JPanel(new FlowLayout());
             JButton purgeButton=new JButton("Purge...");
             Administrator.constrainHeight(purgeButton);
             purgeButton.setActionCommand(m_diss.getCreateDate());
             purgeButton.addActionListener(m_purgeButtonListener);
             bottomPanel.add(purgeButton);
 
             setLayout(new BorderLayout());
             add(topPanel, BorderLayout.NORTH);
             add(middlePanel, BorderLayout.CENTER);
             add(bottomPanel, BorderLayout.SOUTH);
         }
 
     }
 
     protected class PurgeButtonListener
             implements ActionListener {
 
         Disseminator[] m_versions;
         Object[] m_dateStrings;
         HashMap m_dissIndex;
 
         public PurgeButtonListener(Disseminator[] versions) {
             m_versions=versions;
             m_dateStrings=new Object[versions.length];
             m_dissIndex=new HashMap();
             for (int i=0; i<versions.length; i++) {
                 String dateAsString=versions[i].getCreateDate();
                 m_dateStrings[i]=dateAsString;
                 m_dissIndex.put(dateAsString, new Integer(i));
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
                         "Purge version(s) from disseminator " + m_versions[0].getID(),
                         JOptionPane.QUESTION_MESSAGE,
                         null,
                         m_dateStrings,
                         defaultValue);
                 if (selected==null) {
                     canceled=true;
                 } else {
                     sIndex=((Integer) m_dissIndex.get(selected)).intValue();
                 }
             }
             if (!canceled) {
                 // do warning
                 boolean removeAll=false;
                 String detail;
                 if (sIndex==0) {
                     detail="the entire disseminator.";
                     removeAll=true;
                 } else if (sIndex==m_versions.length-1) {
                     detail="the oldest version of the disseminator.";
                 } else {
                     int num=m_versions.length-sIndex;
                     detail="the oldest " + num + " versions of the disseminator.";
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
                         Administrator.APIM.purgeDisseminator(m_pid,
                                 m_versions[sIndex].getID(),
                                 m_versions[sIndex].getCreateDate());
                         if (removeAll) {
                             m_owner.remove(m_versions[0].getID());
                             m_owner.doNew(false);
                             m_done=true;
                         } else {
                             m_owner.refresh(m_versions[0].getID());
                             m_owner.doNew(false);
                             m_done=true;
                         }
                     } catch (Exception e) {
                         JOptionPane.showMessageDialog(Administrator.getDesktop(),
                                 e.getMessage(), "Purge error",
                                 JOptionPane.ERROR_MESSAGE);
                     }
                 }
             }
         }
     }
 
     public void addRows(JComponent[] left, JComponent[] right,
             GridBagLayout gridBag, Container container) {
         GridBagConstraints c=new GridBagConstraints();
         c.insets=new Insets(0, 4, 4, 4);
         c.anchor=GridBagConstraints.WEST;
         for (int i=0; i<left.length; i++) {
             if (i==2) {
                 c.anchor=GridBagConstraints.NORTHWEST;
             }
             c.gridwidth=GridBagConstraints.RELATIVE; //next-to-last
             c.fill=GridBagConstraints.NONE;      //reset to default
             c.weightx=0.0;                       //reset to default
             gridBag.setConstraints(left[i], c);
             container.add(left[i]);
 
             c.gridwidth=GridBagConstraints.REMAINDER;     //end row
             if (!(right[i] instanceof JComboBox) && !(right[i] instanceof JButton)) {
                 c.fill=GridBagConstraints.HORIZONTAL;
             }
             c.weightx=1.0;
             gridBag.setConstraints(right[i], c);
             container.add(right[i]);
         }
 
     }
 
 }
