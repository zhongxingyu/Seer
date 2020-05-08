 package fedora.client.objecteditor;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.io.*;
 import java.util.*;
 import javax.swing.*;
 
 import fedora.client.Administrator;
 import fedora.client.objecteditor.types.DatastreamInputSpec;
 import fedora.server.types.gen.Datastream;
 import fedora.server.types.gen.DatastreamBinding;
 
 public class MechanismInputPanel
         extends JPanel {
 
     private Map m_loadedPanels;
     private JComponent m_containerToValidate;
     private Dimension m_preferredLabelSize;
     private Datastream[] m_currentDatastreamVersions;
     private ObjectEditorFrame m_gramps;
     private ValidityListener m_owner;
     private String m_bDefPID;
     private String m_bMechPID;
     private String m_bMechLabel;
 
     private Map m_bindingPanelMap;
 
     /**
      * Initialize with information given the indicated behavior definition.
      *
      * If bDefPID is given as null, don't display anything yet.
      * If containerToValidate is not given as null, that container
      * will be validate()ed each time this panel changes its structure.
      * If preferredLabelSize is not given as null, left labels' preferred
      * size will be set accordingly.
      */
     public MechanismInputPanel(String bDefPID, 
                                JComponent containerToValidate,
                                Dimension preferredLabelSize,
                                Datastream[] currentDatastreamVersions,
                                ObjectEditorFrame gramps,
                                ValidityListener owner) 
            throws IOException {
 
         m_containerToValidate=containerToValidate;
         m_preferredLabelSize=preferredLabelSize;
         m_currentDatastreamVersions=currentDatastreamVersions;
         m_gramps=gramps;
         m_owner=owner; 
 
         m_bindingPanelMap=new HashMap();
         setLayout(new BorderLayout());
         m_loadedPanels=new HashMap();
         if (bDefPID!=null) {
             setBDef(bDefPID);
         }
     }
 
     /**
      * Switch what is displayed (if anything) with information given the 
      * indicated behavior definition.
      * 
      * If null is given, clear what is currently displayed.
      */
     public void setBDef(String bDefPID) 
             throws IOException {
         removeAll();
         if (bDefPID!=null) {
             JPanel lp=(JPanel) m_loadedPanels.get(bDefPID);
             if (lp==null) {
                 lp=makePanel(bDefPID);
                 m_loadedPanels.put(bDefPID, lp);
             }
             add(lp, BorderLayout.CENTER);
         }
         m_bDefPID=bDefPID;
         m_owner.setValid(hasValidBindings());
         if (m_containerToValidate!=null) {
             m_containerToValidate.revalidate();
             m_containerToValidate.repaint(new Rectangle(m_containerToValidate.getSize()));
         }
     }
 
     public boolean hasValidBindings() {
         if (m_bDefPID==null) return false;
         BindingPanel bp=(BindingPanel) m_bindingPanelMap.get(m_bDefPID);
         if (bp==null) return false;
         DatastreamBindingPane dsbp=bp.getDatastreamBindingPane();
         if (dsbp==null) return false;
         return dsbp.doValidityCheck();
     }
 
     public String getBDefPID() {
         return m_bDefPID;
     }
 
     public String getBMechPID() {
         return m_bMechPID;
     }
 
     public String getBMechLabel() {
         return m_bMechLabel;
     }
 
     public DatastreamBinding[] getBindings() {
         if (m_bDefPID==null) return null;
         BindingPanel bp=(BindingPanel) m_bindingPanelMap.get(m_bDefPID);
         if (bp==null) return null;
         DatastreamBindingPane dsbp=bp.getDatastreamBindingPane();
         if (dsbp==null) return null;
         return dsbp.getBindings();
     }
 
     /**
      * Create and return a new panel based on the given bDefID
      *
      * Mechanism        "[Select..]" || bMechPID - Label  [OPEN]
      * [BINDINGPANE]
      */
     private JPanel makePanel(String bDefPID) 
             throws IOException {
 
         // CENTER: bindingPanel
         final BindingPanel bindingPanel=new BindingPanel(null);
         m_bindingPanelMap.put(bDefPID, bindingPanel);
 
         // NORTH: mechChoicePanel
 
             // NORTH WEST: "Mechanism"
             JLabel label=new JLabel("Mechanism");
             if (m_preferredLabelSize!=null) {
                 label.setMinimumSize(m_preferredLabelSize);
                 label.setPreferredSize(m_preferredLabelSize);
             }
             JPanel labelPanel=new JPanel(new BorderLayout());
             labelPanel.setBorder(BorderFactory.createEmptyBorder(0,4,0,4));
             labelPanel.add(label, BorderLayout.NORTH);
 
             // CENTER: dropdown
             Map bMechLabels=Util.getBMechLabelMap(bDefPID);
             String[] bMechSelections=new String[bMechLabels.keySet().size()+1];
             if (bMechSelections.length==1) {
                 bMechSelections[0]="No matching behavior mechanisms in repository!";
             } else {
                 Iterator iter=bMechLabels.keySet().iterator();
                 int i=0;
                 bMechSelections[i++]="[Select a Behavior Mechanism]";
                 while (iter.hasNext()) {
                     String pid=(String) iter.next();
                     String mechLabel=(String) bMechLabels.get(pid);
                     bMechSelections[i++]=pid + " - " + mechLabel;
                 }
             }
             final JComboBox bMechComboBox=new JComboBox(bMechSelections); 
             Administrator.constrainHeight(bMechComboBox);
             bMechComboBox.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent evt) {
                     try {
                         String[] parts=((String) bMechComboBox.getSelectedItem()).split(" - ");
                         String pid;
                         if (parts.length==1) {
                             pid=null;
                             m_bMechLabel=null;
                         } else {
                             pid=parts[0];
                             m_bMechLabel=parts[1];
                         }
                         bindingPanel.setBMech(pid);
                     } catch (Exception e) {
                         e.printStackTrace();
                         JOptionPane.showMessageDialog(
                                 Administrator.getDesktop(),
                                 e.getMessage(), 
                                 "Error getting bmech info",
                                 JOptionPane.ERROR_MESSAGE);
                     }
                 }
             });
 
            // EAST: openButton
            JButton openButton=new JButton("Open");
            Administrator.constrainHeight(openButton);
 
         JPanel mechChoicePanel=new JPanel(new BorderLayout(4, 0));
         mechChoicePanel.setBorder(BorderFactory.createEmptyBorder(0,0,0,4));
         mechChoicePanel.add(labelPanel, BorderLayout.WEST);
         mechChoicePanel.add(bMechComboBox, BorderLayout.CENTER);
        mechChoicePanel.add(openButton, BorderLayout.EAST);
 
 
         JPanel panel=new JPanel(new BorderLayout());
         panel.add(mechChoicePanel, BorderLayout.NORTH);
         panel.add(bindingPanel, BorderLayout.CENTER);
         return panel;
     }
 
     class BindingPanel
             extends JPanel {
 
         private Map m_loadedPanels;
         private Map m_datastreamBindingPanes;
 
         public BindingPanel(String bMechPID) 
                 throws IOException {
             m_loadedPanels=new HashMap();
             m_datastreamBindingPanes=new HashMap();
             setLayout(new BorderLayout());
             if (bMechPID!=null) {
                 setBMech(bMechPID);
             }
         }
 
         public void setBMech(String bMechPID) 
                 throws IOException {
             removeAll();
             if (bMechPID!=null) {
                 JPanel lp=(JPanel) m_loadedPanels.get(bMechPID);
                 if (lp==null) {
                     lp=makePanel(bMechPID);
                     m_loadedPanels.put(bMechPID, lp);
                 }
                 add(lp, BorderLayout.CENTER);
             }
             m_bMechPID=bMechPID;
             m_owner.setValid(hasValidBindings());
             if (m_containerToValidate!=null) {
                 m_containerToValidate.revalidate();
                 m_containerToValidate.repaint(new Rectangle(m_containerToValidate.getSize()));
             }
         }
 
         public DatastreamBindingPane getDatastreamBindingPane() {
             if (m_bMechPID==null) return null;
             return (DatastreamBindingPane) m_datastreamBindingPanes.get(m_bMechPID);
         }
 
         public JPanel makePanel(String bMechPID) 
                 throws IOException {
             // NORTH WEST: "Bindings"
             JLabel label=new JLabel("Bindings");
             if (m_preferredLabelSize!=null) {
                 label.setMinimumSize(m_preferredLabelSize);
                 label.setPreferredSize(m_preferredLabelSize);
             }
             JPanel labelPanel=new JPanel(new BorderLayout());
             labelPanel.setBorder(BorderFactory.createEmptyBorder(0,4,0,4));
             labelPanel.add(label, BorderLayout.NORTH);
 
             // CENTER: DatastreamBindingPane
             DatastreamInputSpec spec=Util.getInputSpec(bMechPID);
             DatastreamBindingPane bPane=
                     new DatastreamBindingPane(
                                  m_currentDatastreamVersions,
                                  new DatastreamBinding[0],
                                  bMechPID,
                                  spec,
                                  m_owner,
                                  null);
             m_gramps.addDatastreamListener(bPane);           
             m_datastreamBindingPanes.put(bMechPID, bPane);
 
             // put it together and return it
             JPanel panel=new JPanel(new BorderLayout());
             panel.add(labelPanel, BorderLayout.WEST);
             panel.add(bPane, BorderLayout.CENTER);
             return panel;
         }
 
     }
 
 }
