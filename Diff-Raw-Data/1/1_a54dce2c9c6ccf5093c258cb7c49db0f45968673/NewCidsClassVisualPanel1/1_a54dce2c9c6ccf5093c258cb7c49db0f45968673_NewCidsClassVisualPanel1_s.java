 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.abf.domainserver.project.cidsclass;
 
 import org.apache.log4j.Logger;
 
 import org.jdesktop.swingx.JXTable;
 
 import org.openide.util.ImageUtilities;
 import org.openide.util.NbBundle;
 import org.openide.util.WeakListeners;
 
 import java.awt.Component;
 import java.awt.Image;
 import java.awt.datatransfer.Transferable;
 import java.awt.dnd.DnDConstants;
 import java.awt.dnd.DragGestureEvent;
 import java.awt.dnd.DragGestureListener;
 import java.awt.dnd.DragSource;
 import java.awt.dnd.DragSourceDragEvent;
 import java.awt.dnd.DragSourceDropEvent;
 import java.awt.dnd.DragSourceEvent;
 import java.awt.dnd.DragSourceListener;
 import java.awt.dnd.DropTarget;
 import java.awt.dnd.DropTargetDragEvent;
 import java.awt.dnd.DropTargetDropEvent;
 import java.awt.dnd.DropTargetEvent;
 import java.awt.dnd.DropTargetListener;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.DefaultCellEditor;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.DefaultListCellRenderer;
 import javax.swing.DefaultListModel;
 import javax.swing.ImageIcon;
 import javax.swing.JCheckBox;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.SortOrder;
 import javax.swing.event.CellEditorListener;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.TableCellEditor;
 
 import de.cismet.cids.abf.domainserver.project.DomainserverProject;
 import de.cismet.cids.abf.domainserver.project.utils.PermissionResolver;
 import de.cismet.cids.abf.domainserver.project.utils.ProjectUtils;
 import de.cismet.cids.abf.domainserver.project.utils.Renderers;
 import de.cismet.cids.abf.utilities.CidsTypeTransferable;
 import de.cismet.cids.abf.utilities.Comparators;
 
 import de.cismet.cids.jpa.backend.service.Backend;
 import de.cismet.cids.jpa.entity.cidsclass.Attribute;
 import de.cismet.cids.jpa.entity.cidsclass.CidsClass;
 import de.cismet.cids.jpa.entity.cidsclass.Icon;
 import de.cismet.cids.jpa.entity.cidsclass.Type;
 import de.cismet.cids.jpa.entity.permission.Policy;
 
 import de.cismet.tools.Equals;
 
 /**
  * DOCUMENT ME!
  *
  * @author   thorsten.hell@cismet.de
  * @author   martin.scholl@cismet.de
  * @version  $Revision$, $Date$
  */
 public final class NewCidsClassVisualPanel1 extends JPanel {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final transient Logger LOG = Logger.getLogger(
             NewCidsClassVisualPanel1.class);
 
     private static final int DOWN = 1;
     private static final int UP = -1;
 
     //~ Instance fields --------------------------------------------------------
 
     private final transient ImageIcon dbType;
     private transient ClassTableModel classTableModel;
     private transient CidsClass cidsClass;
     private final transient SyncClassDocListener syncClassDocL;
     private final transient SyncTableDocListener syncTableDocL;
     private final transient ItemListener attrPolicyL;
     private final transient NewCidsClassWizardPanel1 model;
 
     private final transient ActionListener oneToManyL;
 
     private final transient ListSelectionListener tableRowSelectionL;
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JToggleButton btnOneToMany;
     private final transient javax.swing.JComboBox cboAttrPolicy = new javax.swing.JComboBox();
     private final transient javax.swing.JComboBox cboClassIcons = new javax.swing.JComboBox();
     private final transient javax.swing.JComboBox cboObjectIcons = new javax.swing.JComboBox();
     private final transient javax.swing.JCheckBox chkIndexed = new javax.swing.JCheckBox();
     private final transient javax.swing.JCheckBox chkSync = new javax.swing.JCheckBox();
     private final transient javax.swing.JCheckBox chkType = new javax.swing.JCheckBox();
     private final transient javax.swing.JButton cmdDown = new javax.swing.JButton();
     private final transient javax.swing.JButton cmdRemove = new javax.swing.JButton();
     private final transient javax.swing.JButton cmdUp = new javax.swing.JButton();
     private final transient javax.swing.ButtonGroup grpTypeSort = new javax.swing.ButtonGroup();
     private final transient javax.swing.JPanel jPanel1 = new javax.swing.JPanel();
     private final transient javax.swing.JToolBar jToolBar1 = new javax.swing.JToolBar();
     private final transient javax.swing.JToolBar jToolBar2 = new javax.swing.JToolBar();
     private javax.swing.JToggleButton jtbAttrSync;
     private final transient javax.swing.JLabel lblAttrPolicy = new javax.swing.JLabel();
     private final transient javax.swing.JLabel lblClassIcon = new javax.swing.JLabel();
     private final transient javax.swing.JLabel lblClassName = new javax.swing.JLabel();
     private final transient javax.swing.JLabel lblDesc = new javax.swing.JLabel();
     private final transient javax.swing.JLabel lblObjectIcon = new javax.swing.JLabel();
     private final transient javax.swing.JLabel lblPrimKey = new javax.swing.JLabel();
     private final transient javax.swing.JLabel lblSortAndFilter = new javax.swing.JLabel();
     private final transient javax.swing.JLabel lblSpace1 = new javax.swing.JLabel();
     private final transient javax.swing.JLabel lblTableName = new javax.swing.JLabel();
     private final transient javax.swing.JList lstTypes = new DragJList();
     private final transient javax.swing.JPanel panAttr = new javax.swing.JPanel();
     private final transient javax.swing.JPanel panCenter = new javax.swing.JPanel();
     private final transient javax.swing.JPanel panClass1 = new javax.swing.JPanel();
     private final transient javax.swing.JPanel panClass2 = new javax.swing.JPanel();
     private final transient javax.swing.JPanel panClass3 = new javax.swing.JPanel();
     private final transient javax.swing.JPanel panTypes = new javax.swing.JPanel();
     private final transient javax.swing.JPanel panhead = new javax.swing.JPanel();
     private final transient javax.swing.JScrollPane scpTableAttr = new javax.swing.JScrollPane();
     private final transient javax.swing.JScrollPane scpTypes = new javax.swing.JScrollPane();
     private final transient javax.swing.JTable tblAttr = new DropAwareJXTable();
     private final transient javax.swing.JToggleButton togArrayTables = new javax.swing.JToggleButton();
     private final transient javax.swing.JToggleButton togOnlyDbTypes = new javax.swing.JToggleButton();
     private final transient javax.swing.JToggleButton togOnlyUserTypes = new javax.swing.JToggleButton();
     private final transient javax.swing.JToggleButton togSortedAlpha = new javax.swing.JToggleButton();
     private final transient javax.swing.JToggleButton togSortedLinks = new javax.swing.JToggleButton();
     private final transient javax.swing.JTextField txtClassname = new javax.swing.JTextField();
     private final transient javax.swing.JTextField txtDescription = new javax.swing.JTextField();
     private final transient javax.swing.JTextField txtPrimaryKeyfield = new javax.swing.JTextField();
     private final transient javax.swing.JTextField txtTablename = new javax.swing.JTextField();
     // End of variables declaration//GEN-END:variables
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new NewCidsClassVisualPanel1 object.
      *
      * @param  model  DOCUMENT ME!
      */
     public NewCidsClassVisualPanel1(final NewCidsClassWizardPanel1 model) {
         this.model = model;
         this.tableRowSelectionL = new TableRowSelectionListener();
         this.oneToManyL = new OneToManyActionListener();
 
         initComponents();
 
         dbType = new ImageIcon(ImageUtilities.loadImage(DomainserverProject.IMAGE_FOLDER + "db_types.png")); // NOI18N
         syncClassDocL = new SyncClassDocListener();
         syncTableDocL = new SyncTableDocListener();
         attrPolicyL = new ItemListener() {
 
                 @Override
                 public void itemStateChanged(final ItemEvent e) {
                     if (e.getStateChange() == ItemEvent.SELECTED) {
                         final Policy policy = (Policy)e.getItem();
                         model.getCidsClass().setAttributePolicy((policy.getId() == null) ? null : policy);
                     }
                 }
             };
         ((JXTable)tblAttr).setAutoStartEditOnKeyStroke(true);
         ((JXTable)tblAttr).setTerminateEditOnFocusLost(true);
         tblAttr.setDefaultEditor(String.class, new DefaultCellEditor(new JTextField()));
         tblAttr.getDefaultEditor(String.class).addCellEditorListener(new CellEditorListener() {
 
                 @Override
                 public void editingCanceled(final ChangeEvent e) {
                     model.fireChangeEvent();
                 }
 
                 @Override
                 public void editingStopped(final ChangeEvent e) {
                     model.fireChangeEvent();
                 }
             });
 
         final JCheckBox chkEditorBox = new JCheckBox();
         chkEditorBox.setHorizontalAlignment(JCheckBox.CENTER);
         tblAttr.setDefaultEditor(Boolean.class, new DefaultCellEditor(chkEditorBox));
         lstTypes.setCellRenderer(new ListCellRendererImpl());
         cboClassIcons.addItemListener(new ItemListener() {
 
                 @Override
                 public void itemStateChanged(final ItemEvent e) {
                     final CidsClass c = classTableModel.getCidsClass();
                     c.setClassIcon((Icon)cboClassIcons.getSelectedItem());
                 }
             });
         cboObjectIcons.addItemListener(new ItemListener() {
 
                 @Override
                 public void itemStateChanged(final ItemEvent e) {
                     final CidsClass c = classTableModel.getCidsClass();
                     c.setObjectIcon((Icon)cboObjectIcons.getSelectedItem());
                 }
             });
 
         tblAttr.getSelectionModel()
                 .addListSelectionListener(WeakListeners.create(
                         ListSelectionListener.class,
                         tableRowSelectionL,
                         tblAttr.getSelectionModel()));
         btnOneToMany.addActionListener(WeakListeners.create(ActionListener.class, oneToManyL, btnOneToMany));
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      */
     void init() {
         txtClassname.getDocument().removeDocumentListener(syncTableDocL);
         txtTablename.getDocument().removeDocumentListener(syncClassDocL);
         txtDescription.getDocument().removeDocumentListener(syncClassDocL);
         txtPrimaryKeyfield.getDocument().removeDocumentListener(syncClassDocL);
         cidsClass = model.getCidsClass();
         initCboAttrPolicy();
         final DomainserverProject project = model.getProject();
         classTableModel = new ClassTableModel(project, cidsClass);
         tblAttr.setModel(classTableModel);
         final JXTable attrTable = (JXTable)tblAttr;
         attrTable.setSortOrder(0, SortOrder.ASCENDING);
         attrTable.setSortable(true);
         for (int i = 1; i < attrTable.getColumnCount(); ++i) {
             attrTable.getColumnExt(i).setSortable(false);
         }
         attrTable.getColumnExt(0).setVisible(false);
         retrieveAndSortTypes();
         final List<Icon> allIcons = new ArrayList<Icon>(project.getCidsDataObjectBackend().getAllEntities(Icon.class));
         Collections.sort(allIcons, new Comparators.Icons());
         cboClassIcons.setModel(new DefaultComboBoxModel(allIcons.toArray()));
         cboObjectIcons.setModel(new DefaultComboBoxModel(allIcons.toArray()));
         cboClassIcons.setRenderer(new Renderers.IconCellRenderer(project));
         cboObjectIcons.setRenderer(new Renderers.IconCellRenderer(project));
         if (cidsClass == null) {
             txtClassname.setText("");                           // NOI18N
             txtTablename.setText("");                           // NOI18N
             chkSync.setSelected(true);
             cboClassIcons.setSelectedIndex(0);
             cboObjectIcons.setSelectedIndex(0);
             txtPrimaryKeyfield.setText("ID");                   // NOI18N
             chkType.setEnabled(false);
             chkType.setSelected(true);
             chkIndexed.setSelected(false);
             txtDescription.setText("");                         // NOI18N
         } else {
             cboClassIcons.setSelectedItem(cidsClass.getClassIcon());
             cboObjectIcons.setSelectedItem(cidsClass.getObjectIcon());
             txtClassname.setText(cidsClass.getName());
             txtTablename.setText(cidsClass.getTableName());
             chkSync.setSelected(cidsClass.getName().equalsIgnoreCase(cidsClass.getTableName()));
             txtPrimaryKeyfield.setText(cidsClass.getPrimaryKeyField());
             cboObjectIcons.getSelectedItem();
             cboObjectIcons.getModel();
             chkType.setEnabled(false);
             chkType.setSelected(true);
             chkIndexed.setSelected((cidsClass.isIndexed() != null) && cidsClass.isIndexed());
             final String desc = cidsClass.getDescription();
             txtDescription.setText((desc == null) ? "" : desc); // NOI18N
         }
         this.cidsClass = classTableModel.getCidsClass();
         txtClassname.getDocument().addDocumentListener(syncTableDocL);
         txtTablename.getDocument().addDocumentListener(syncClassDocL);
         txtDescription.getDocument().addDocumentListener(syncClassDocL);
         txtPrimaryKeyfield.getDocument().addDocumentListener(syncClassDocL);
         syncTablename();
         classTableModel.setAttrSync(jtbAttrSync.isSelected());
         model.fireChangeEvent();
         tblAttr.getSelectionModel().clearSelection();
     }
 
     /**
      * DOCUMENT ME!
      */
     private void initCboAttrPolicy() {
         cboAttrPolicy.removeItemListener(attrPolicyL);
         ((DefaultComboBoxModel)cboAttrPolicy.getModel()).removeAllElements();
         cboAttrPolicy.addItem(Policy.NO_POLICY);
         final List<Policy> policies = model.getProject().getCidsDataObjectBackend().getAllEntities(Policy.class);
         Collections.sort(policies, new Comparators.Policies());
         for (final Policy p : policies) {
             cboAttrPolicy.addItem(p);
         }
         if ((model.getCidsClass() == null)
                     || (model.getCidsClass().getAttributePolicy() == null)) {
             cboAttrPolicy.setSelectedIndex(0);
         } else {
             cboAttrPolicy.setSelectedItem(
                 model.getCidsClass().getAttributePolicy());
         }
         cboAttrPolicy.setRenderer(new DefaultListCellRenderer() {
 
                 @Override
                 public Component getListCellRendererComponent(
                         final JList list,
                         final Object value,
                         final int index,
                         final boolean isSelected,
                         final boolean cellHasFocus) {
                     final JLabel label = (JLabel)super.getListCellRendererComponent(
                             list,
                             value,
                             index,
                             isSelected,
                             cellHasFocus);
                     final Policy policy = (Policy)value;
                     final String s;
                     final CidsClass clazz = model.getCidsClass();
                     if ((policy != null)
                                 && Policy.NO_POLICY.equals(policy)
                                 && (clazz != null)
                                 && !clazz.getAttributes().isEmpty()) {
                         final Policy p = clazz.getAttributePolicy();
                         clazz.setAttributePolicy(null);
                         try {
                             s = "<"
                                         + PermissionResolver.getInstance(model.getProject())
                                         .getPermString(clazz.getAttributes().iterator().next(), null)
                                         .getInheritanceString() + ">"; // NOI18N
                         } finally {
                             clazz.setAttributePolicy(p);
                         }
                     } else {
                         s = policy.getName();
                     }
                     label.setText(s);
                     label.setIcon(null);
                     return label;
                 }
             });
         cboAttrPolicy.addItemListener(attrPolicyL);
     }
 
     /**
      * DOCUMENT ME!
      */
     private void syncClass() {
         final CidsClass c = classTableModel.getCidsClass();
         c.setName(txtClassname.getText());
         c.setTableName(txtTablename.getText());
         c.setDescription(txtDescription.getText());
         c.setObjectIcon((Icon)cboObjectIcons.getSelectedItem());
         c.setClassIcon((Icon)cboClassIcons.getSelectedItem());
         c.setPrimaryKeyField(txtPrimaryKeyfield.getText());
         c.setIndexed(chkIndexed.isSelected());
        c.setArrayLink(false);
     }
 
     /**
      * DOCUMENT ME!
      */
     private void syncTablename() {
         if (chkSync.isSelected()) {
             txtTablename.setText(ProjectUtils.toDBCompatibleString(txtClassname.getText()).toUpperCase());
         }
         syncClass();
     }
 
     @Override
     public String getName() {
         return org.openide.util.NbBundle.getMessage(
                 NewCidsClassVisualPanel1.class,
                 "NewCidsClassVisualPanel1.getName().returnvalue"); // NOI18N
     }
 
     /**
      * DOCUMENT ME!
      */
     private void retrieveAndSortTypes() {
         final Backend backend = model.getProject().getCidsDataObjectBackend();
         final List<Type> types = backend.getAllEntities(Type.class);
         final List countedTypesList = backend.getSortedTypes();
         final Map<Integer, Long> countedTypes = new HashMap<Integer, Long>();
         for (final Object o : countedTypesList) {
             final Object[] oa = (Object[])o;
             final Long counter = ((Long)oa[0]).longValue();
             final Integer countedTypeId = ((Integer)oa[1]);
             countedTypes.put(countedTypeId, counter);
         }
 
         if (togSortedLinks.isSelected()) {
             // we sort after type usage: types used more often come before types used less often or not at all
             // if types occur equally often they're sorted complex types first, normal types thereafter, whereas both
             // groups are sorted by their natural alphabetical order
             Collections.sort(types, new Comparator<Type>() {
 
                     @Override
                     public int compare(final Type t1, final Type t2) {
                         final int id1 = t1.getId();
                         final int id2 = t2.getId();
 
                         if (countedTypes.containsKey(id1) && countedTypes.containsKey(id2)) {
                             final Long l1 = countedTypes.get(id1);
                             final Long l2 = countedTypes.get(id2);
 
                             final int comp = (l1.compareTo(l2)) * (-1);
 
                             if (comp == 0) {
                                 return compareTypes(t1, t2);
                             } else {
                                 return comp;
                             }
                         } else if (!countedTypes.containsKey(id1) && countedTypes.containsKey(id2)) {
                             return 1;
                         } else if (countedTypes.containsKey(id1) && !countedTypes.containsKey(id2)) {
                             return -1;
                         } else {
                             return compareTypes(t1, t2);
                         }
                     }
 
                     private int compareTypes(final Type t1, final Type t2) {
                         if (!t1.isComplexType() && t2.isComplexType()) {
                             return 1;
                         } else if (t1.isComplexType() && !t2.isComplexType()) {
                             return -1;
                         } else {
                             return t1.getName().compareTo(t2.getName());
                         }
                     }
                 });
         } else {
             // sort alphabetical
             Collections.sort(types, new Comparator<Type>() {
 
                     @Override
                     public int compare(final Type t1, final Type t2) {
                         final String s1 = t1.getName();
                         final String s2 = t2.getName();
 
                         if (Equals.allNull(s1, s2)) {
                             return 0;
                         } else if (s1 == null) {
                             return -1;
                         } else if (s2 == null) {
                             return 1;
                         } else {
                             return s1.toLowerCase().compareTo(s2.toLowerCase());
                         }
                     }
                 });
         }
 
         final DefaultListModel dlModel = new DefaultListModel();
         for (final Type t : types) {
             if (!t.getName().startsWith("_")) // NOI18N
             {
                 if (togOnlyDbTypes.isSelected() && !t.isComplexType()) {
                     dlModel.addElement(t);
                 }
                 if (togOnlyUserTypes.isSelected() && t.isComplexType()) {
                     if (togArrayTables.isSelected()) {
                         dlModel.addElement(t);
                     } else {
                         final CidsClass c = t.getCidsClass();
                         t.getName();
                         t.getId();
                         if ((c != null) && !c.isArrayLink()) {
                             dlModel.addElement(t);
                         }
                     }
                 }
             }
         }
         lstTypes.setModel(dlModel);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public CidsClass getCidsClass() {
         syncClass();
         return classTableModel.getCidsClass();
     }
 
     /**
      * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
      * content of this method is always regenerated by the Form Editor.
      */
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
         java.awt.GridBagConstraints gridBagConstraints;
 
         btnOneToMany = new javax.swing.JToggleButton();
         jtbAttrSync = new javax.swing.JToggleButton();
 
         setLayout(new java.awt.BorderLayout());
 
         panCenter.setBorder(javax.swing.BorderFactory.createTitledBorder(
                 org.openide.util.NbBundle.getMessage(
                     NewCidsClassVisualPanel1.class,
                     "NewCidsClassVisualPanel1.panCenter.border.title"))); // NOI18N
         panCenter.setLayout(new java.awt.BorderLayout());
 
         panAttr.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
         panAttr.setLayout(new java.awt.BorderLayout());
 
         scpTableAttr.setPreferredSize(new java.awt.Dimension(300, 200));
         scpTableAttr.setViewportView(tblAttr);
 
         panAttr.add(scpTableAttr, java.awt.BorderLayout.CENTER);
 
         jToolBar1.setFloatable(false);
 
         cmdRemove.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/abf/domainserver/images/remove_row.png"))); // NOI18N
         cmdRemove.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     cmdRemoveActionPerformed(evt);
                 }
             });
         jToolBar1.add(cmdRemove);
 
         cmdUp.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/abf/domainserver/images/up.png"))); // NOI18N
         cmdUp.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     cmdUpActionPerformed(evt);
                 }
             });
         jToolBar1.add(cmdUp);
 
         cmdDown.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/abf/domainserver/images/down.png"))); // NOI18N
         cmdDown.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     cmdDownActionPerformed(evt);
                 }
             });
         jToolBar1.add(cmdDown);
 
         org.openide.awt.Mnemonics.setLocalizedText(btnOneToMany, "1:N");
         btnOneToMany.setFocusable(false);
         btnOneToMany.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         btnOneToMany.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         jToolBar1.add(btnOneToMany);
 
         jtbAttrSync.setSelected(true);
         org.openide.awt.Mnemonics.setLocalizedText(
             jtbAttrSync,
             NbBundle.getMessage(NewCidsClassVisualPanel1.class, "NewCidsClassVisualPanel1.jtbAttrSync.text")); // NOI18N
         jtbAttrSync.setFocusable(false);
         jtbAttrSync.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         jtbAttrSync.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         jtbAttrSync.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     jtbAttrSyncActionPerformed(evt);
                 }
             });
         jToolBar1.add(jtbAttrSync);
 
         org.openide.awt.Mnemonics.setLocalizedText(lblSpace1, "          ");
         jToolBar1.add(lblSpace1);
 
         org.openide.awt.Mnemonics.setLocalizedText(
             lblAttrPolicy,
             org.openide.util.NbBundle.getMessage(
                 NewCidsClassVisualPanel1.class,
                 "NewCidsClassVisualPanel1.lblAttrPolicy.text")); // NOI18N
         jToolBar1.add(lblAttrPolicy);
         jToolBar1.add(cboAttrPolicy);
 
         panAttr.add(jToolBar1, java.awt.BorderLayout.PAGE_START);
 
         panCenter.add(panAttr, java.awt.BorderLayout.CENTER);
 
         panTypes.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
         panTypes.setLayout(new java.awt.BorderLayout());
 
         scpTypes.setBorder(javax.swing.BorderFactory.createTitledBorder(
                 org.openide.util.NbBundle.getMessage(
                     NewCidsClassVisualPanel1.class,
                     "NewCidsClassVisualPanel1.scpTypes.border.title"))); // NOI18N
 
         scpTypes.setViewportView(lstTypes);
 
         panTypes.add(scpTypes, java.awt.BorderLayout.CENTER);
 
         jToolBar2.setFloatable(false);
 
         org.openide.awt.Mnemonics.setLocalizedText(
             lblSortAndFilter,
             org.openide.util.NbBundle.getMessage(
                 NewCidsClassVisualPanel1.class,
                 "NewCidsClassVisualPanel1.lblSortAndFilter.text")); // NOI18N
         jToolBar2.add(lblSortAndFilter);
 
         togOnlyUserTypes.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/abf/domainserver/images/datatype.png"))); // NOI18N
         togOnlyUserTypes.setSelected(true);
         togOnlyUserTypes.setToolTipText(org.openide.util.NbBundle.getMessage(
                 NewCidsClassVisualPanel1.class,
                 "NewCidsClassVisualPanel1.togOnlyUserTypes.tooltip"));                            // NOI18N
         togOnlyUserTypes.setFocusPainted(false);
         togOnlyUserTypes.setPreferredSize(new java.awt.Dimension(50, 25));
         togOnlyUserTypes.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     togOnlyUserTypesActionPerformed(evt);
                 }
             });
         jToolBar2.add(togOnlyUserTypes);
 
         togOnlyDbTypes.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/abf/domainserver/images/db_types.png"))); // NOI18N
         togOnlyDbTypes.setSelected(true);
         togOnlyDbTypes.setToolTipText(org.openide.util.NbBundle.getMessage(
                 NewCidsClassVisualPanel1.class,
                 "NewCidsClassVisualPanel1.togOnlyDbTypes.tooltip"));                              // NOI18N
         togOnlyDbTypes.setFocusPainted(false);
         togOnlyDbTypes.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     togOnlyDbTypesActionPerformed(evt);
                 }
             });
         jToolBar2.add(togOnlyDbTypes);
 
         grpTypeSort.add(togSortedAlpha);
         togSortedAlpha.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/abf/domainserver/images/sort_alph.png"))); // NOI18N
         togSortedAlpha.setToolTipText(org.openide.util.NbBundle.getMessage(
                 NewCidsClassVisualPanel1.class,
                 "NewCidsClassVisualPanel1.togSortedAlpha.tooltip"));                               // NOI18N
         togSortedAlpha.setFocusPainted(false);
         togSortedAlpha.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     togSortedAlphaActionPerformed(evt);
                 }
             });
         jToolBar2.add(togSortedAlpha);
 
         grpTypeSort.add(togSortedLinks);
         togSortedLinks.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/abf/domainserver/images/sort_links.png"))); // NOI18N
         togSortedLinks.setSelected(true);
         togSortedLinks.setToolTipText(org.openide.util.NbBundle.getMessage(
                 NewCidsClassVisualPanel1.class,
                 "NewCidsClassVisualPanel1.togSortedLinks.tooltip"));                                // NOI18N
         togSortedLinks.setFocusPainted(false);
         togSortedLinks.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     togSortedLinksActionPerformed(evt);
                 }
             });
         jToolBar2.add(togSortedLinks);
 
         togArrayTables.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cids/abf/domainserver/images/array.png"))); // NOI18N
         togArrayTables.setToolTipText(org.openide.util.NbBundle.getMessage(
                 NewCidsClassVisualPanel1.class,
                 "NewCidsClassVisualPanel1.togArrayTables.tooltip"));                           // NOI18N
         togArrayTables.setFocusPainted(false);
         togArrayTables.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     togArrayTablesActionPerformed(evt);
                 }
             });
         jToolBar2.add(togArrayTables);
 
         panTypes.add(jToolBar2, java.awt.BorderLayout.NORTH);
 
         panCenter.add(panTypes, java.awt.BorderLayout.EAST);
 
         add(panCenter, java.awt.BorderLayout.CENTER);
 
         panhead.setLayout(new java.awt.GridBagLayout());
 
         panClass1.setLayout(new java.awt.GridBagLayout());
 
         org.openide.awt.Mnemonics.setLocalizedText(
             lblClassName,
             org.openide.util.NbBundle.getMessage(
                 NewCidsClassVisualPanel1.class,
                 "NewCidsClassVisualPanel1.lblClassName.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panClass1.add(lblClassName, gridBagConstraints);
 
         org.openide.awt.Mnemonics.setLocalizedText(
             lblTableName,
             org.openide.util.NbBundle.getMessage(
                 NewCidsClassVisualPanel1.class,
                 "NewCidsClassVisualPanel1.lblTableName.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panClass1.add(lblTableName, gridBagConstraints);
 
         org.openide.awt.Mnemonics.setLocalizedText(
             lblDesc,
             org.openide.util.NbBundle.getMessage(
                 NewCidsClassVisualPanel1.class,
                 "NewCidsClassVisualPanel1.lblDesc.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panClass1.add(lblDesc, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panClass1.add(txtDescription, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panClass1.add(txtTablename, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panClass1.add(txtClassname, gridBagConstraints);
 
         chkSync.setSelected(true);
         org.openide.awt.Mnemonics.setLocalizedText(
             chkSync,
             org.openide.util.NbBundle.getMessage(
                 NewCidsClassVisualPanel1.class,
                 "NewCidsClassVisualPanel1.chkSync.text")); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panClass1.add(chkSync, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.weightx = 1.0;
         panhead.add(panClass1, gridBagConstraints);
 
         org.openide.awt.Mnemonics.setLocalizedText(
             lblClassIcon,
             org.openide.util.NbBundle.getMessage(
                 NewCidsClassVisualPanel1.class,
                 "NewCidsClassVisualPanel1.lblClassIcon.text")); // NOI18N
 
         org.openide.awt.Mnemonics.setLocalizedText(
             lblObjectIcon,
             org.openide.util.NbBundle.getMessage(
                 NewCidsClassVisualPanel1.class,
                 "NewCidsClassVisualPanel1.lblObjectIcon.text")); // NOI18N
 
         org.openide.awt.Mnemonics.setLocalizedText(
             lblPrimKey,
             org.openide.util.NbBundle.getMessage(
                 NewCidsClassVisualPanel1.class,
                 "NewCidsClassVisualPanel1.lblPrimKey.text")); // NOI18N
 
         final org.jdesktop.layout.GroupLayout panClass2Layout = new org.jdesktop.layout.GroupLayout(panClass2);
         panClass2.setLayout(panClass2Layout);
         panClass2Layout.setHorizontalGroup(
             panClass2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                 panClass2Layout.createSequentialGroup().addContainerGap().add(
                     panClass2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(lblPrimKey).add(
                         lblClassIcon).add(lblObjectIcon)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(
                     panClass2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false).add(
                         cboObjectIcons,
                         0,
                         org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                         Short.MAX_VALUE).add(
                         org.jdesktop.layout.GroupLayout.TRAILING,
                         cboClassIcons,
                         0,
                         org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                         Short.MAX_VALUE).add(
                         org.jdesktop.layout.GroupLayout.TRAILING,
                         txtPrimaryKeyfield,
                         org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                         105,
                         Short.MAX_VALUE)).addContainerGap(
                     org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                     Short.MAX_VALUE)));
         panClass2Layout.setVerticalGroup(
             panClass2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                 panClass2Layout.createSequentialGroup().addContainerGap().add(
                     panClass2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(lblPrimKey).add(
                         txtPrimaryKeyfield,
                         org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                         org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                         org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addPreferredGap(
                     org.jdesktop.layout.LayoutStyle.RELATED).add(
                     panClass2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(
                         cboClassIcons,
                         org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                         org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                         org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(lblClassIcon)).addPreferredGap(
                     org.jdesktop.layout.LayoutStyle.RELATED).add(
                     panClass2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(
                         cboObjectIcons,
                         org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                         org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                         org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(lblObjectIcon)).addContainerGap()));
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
         panhead.add(panClass2, gridBagConstraints);
 
         final org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
         jPanel1.setLayout(jPanel1Layout);
         jPanel1Layout.setHorizontalGroup(
             jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(0, 0, Short.MAX_VALUE));
         jPanel1Layout.setVerticalGroup(
             jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(0, 0, Short.MAX_VALUE));
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 1.0;
         panhead.add(jPanel1, gridBagConstraints);
 
         add(panhead, java.awt.BorderLayout.PAGE_START);
 
         panClass3.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
 
         org.openide.awt.Mnemonics.setLocalizedText(
             chkType,
             org.openide.util.NbBundle.getMessage(
                 NewCidsClassVisualPanel1.class,
                 "NewCidsClassVisualPanel1.chkType.text")); // NOI18N
         panClass3.add(chkType);
 
         org.openide.awt.Mnemonics.setLocalizedText(
             chkIndexed,
             org.openide.util.NbBundle.getMessage(
                 NewCidsClassVisualPanel1.class,
                 "NewCidsClassVisualPanel1.chkIndexed.text")); // NOI18N
         panClass3.add(chkIndexed);
 
         add(panClass3, java.awt.BorderLayout.PAGE_END);
     } // </editor-fold>//GEN-END:initComponents
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void cmdDownActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cmdDownActionPerformed
         move(DOWN);
     }                                                                           //GEN-LAST:event_cmdDownActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void cmdRemoveActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cmdRemoveActionPerformed
         final int selectedRow = tblAttr.getSelectedRow();
         if (selectedRow >= 0) {
             final int modelRow = ((JXTable)tblAttr).convertRowIndexToModel(selectedRow);
             classTableModel.getCidsClass().getAttributes().remove(
                 classTableModel.getAttributeAt(modelRow));
             classTableModel.fireTableDataChanged();
             final int rc = tblAttr.getRowCount();
             if (rc > 0) {
                 if ((rc - 1) >= selectedRow) {
                     ((JXTable)tblAttr).getSelectionModel().setSelectionInterval(
                         selectedRow,
                         selectedRow);
                 } else {
                     ((JXTable)tblAttr).getSelectionModel().setSelectionInterval(
                         rc
                                 - 1,
                         rc
                                 - 1);
                 }
                 model.fireChangeEvent();
             }
         }
     }                                                                             //GEN-LAST:event_cmdRemoveActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  direction  DOCUMENT ME!
      */
     private void move(final int direction) {
         final int selectedRow = tblAttr.getSelectedRow();
         if (((direction == DOWN) && (selectedRow >= 0)
                         && (selectedRow < (tblAttr.getRowCount() - 1)))
                     || ((direction == UP) && (selectedRow > 0)
                         && (selectedRow < tblAttr.getRowCount()))) {
             final int modelRow = ((JXTable)tblAttr).convertRowIndexToModel(selectedRow);
             final int switcherModelRow = ((JXTable)tblAttr).convertRowIndexToModel(selectedRow + direction);
             final Attribute selected = classTableModel.getAttributeAt(modelRow);
             final Attribute switcher = classTableModel.getAttributeAt(
                     switcherModelRow);
             final Integer sel = selected.getPosition();
             selected.setPosition(switcher.getPosition());
             switcher.setPosition(sel);
             classTableModel.fireTableDataChanged();
             ((JXTable)tblAttr).getSelectionModel()
                     .setSelectionInterval(
                         selectedRow
                         + direction,
                         selectedRow
                         + direction);
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void cmdUpActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cmdUpActionPerformed
         move(UP);
     }                                                                         //GEN-LAST:event_cmdUpActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void togArrayTablesActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_togArrayTablesActionPerformed
         retrieveAndSortTypes();
     }                                                                                  //GEN-LAST:event_togArrayTablesActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void togOnlyDbTypesActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_togOnlyDbTypesActionPerformed
         retrieveAndSortTypes();
     }                                                                                  //GEN-LAST:event_togOnlyDbTypesActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void togOnlyUserTypesActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_togOnlyUserTypesActionPerformed
         retrieveAndSortTypes();
     }                                                                                    //GEN-LAST:event_togOnlyUserTypesActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void togSortedLinksActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_togSortedLinksActionPerformed
         retrieveAndSortTypes();
     }                                                                                  //GEN-LAST:event_togSortedLinksActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void togSortedAlphaActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_togSortedAlphaActionPerformed
         retrieveAndSortTypes();
     }                                                                                  //GEN-LAST:event_togSortedAlphaActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void jtbAttrSyncActionPerformed(final java.awt.event.ActionEvent evt) //GEN-FIRST:event_jtbAttrSyncActionPerformed
     {                                                                             //GEN-HEADEREND:event_jtbAttrSyncActionPerformed
         classTableModel.setAttrSync(jtbAttrSync.isSelected());
     }                                                                             //GEN-LAST:event_jtbAttrSyncActionPerformed
 
     //~ Inner Classes ----------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private final class OneToManyActionListener implements ActionListener {
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public void actionPerformed(final ActionEvent e) {
             final int index = tblAttr.getSelectedRow();
 
             assert index > 0 : "no row selected, check row selection listener impl"; // NOI18N
 
             final int mIndex = tblAttr.convertRowIndexToModel(index);
             final Attribute a = classTableModel.getAttributeAt(mIndex);
 
             // only to prevent errors due to buggy code or external changes
             final Integer fkClass = a.getForeignKeyClass();
             if (btnOneToMany.isSelected()) {
                 assert (fkClass != null) && (fkClass > 0) : "attr already has 1:N foreign key setting"; // NOI18N
             } else {
                 assert (fkClass != null) && (fkClass < 0) : "attr already has 1:1 foreign key setting"; // NOI18N
             }
 
             a.setForeignKeyClass(-1 * fkClass);
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private final class TableRowSelectionListener implements ListSelectionListener {
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public void valueChanged(final ListSelectionEvent e) {
             if (e.getValueIsAdjusting()) {
                 return;
             }
 
             if (tblAttr.getSelectedRowCount() == 1) {
                 final int mIndex = tblAttr.convertRowIndexToModel(tblAttr.getSelectedRow());
                 final Attribute a = classTableModel.getAttributeAt(mIndex);
 
                 btnOneToMany.setEnabled(a.getType().isComplexType());
                 btnOneToMany.setSelected((a.getForeignKeyClass() != null) && (a.getForeignKeyClass() < 0));
             } else {
                 btnOneToMany.setEnabled(false);
                 btnOneToMany.setSelected(false);
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private final class ListCellRendererImpl extends DefaultListCellRenderer {
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public Component getListCellRendererComponent(final JList list,
                 final Object value,
                 final int index,
                 final boolean isSelected,
                 final boolean cellHasFocus) {
             final JLabel lbl = (JLabel)super.getListCellRendererComponent(
                     list,
                     value,
                     index,
                     isSelected,
                     cellHasFocus);
             if (!(value instanceof Type)) {
                 return lbl;
             }
             final Type t = (Type)value;
             lbl.setText(t.getName());
             if (!t.isComplexType()) {
                 lbl.setIcon(dbType);
                 return lbl;
             }
             final CidsClass c = t.getCidsClass();
             if (c == null) {
                 return lbl;
             }
             Icon icon = c.getClassIcon();
             if (icon == null) {
                 icon = c.getObjectIcon();
             }
             if (icon == null) {
                 lbl.setIcon(null);
             } else {
                 final Image image = ProjectUtils.getImageForIconAndProject(
                         icon,
                         model.getProject());
                 if (image == null) {
                     LOG.warn("the icon could not be set: " // NOI18N
                                 + icon.getFileName());
                 } else {
                     lbl.setIcon(new ImageIcon(image));
                 }
             }
             return lbl;
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private final class SyncClassDocListener implements DocumentListener {
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public void changedUpdate(final DocumentEvent e) {
             syncClass();
             model.fireChangeEvent();
         }
 
         @Override
         public void insertUpdate(final DocumentEvent e) {
             syncClass();
             model.fireChangeEvent();
         }
 
         @Override
         public void removeUpdate(final DocumentEvent e) {
             syncClass();
             model.fireChangeEvent();
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private final class SyncTableDocListener implements DocumentListener {
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public void changedUpdate(final DocumentEvent e) {
             syncTablename();
             model.fireChangeEvent();
         }
 
         @Override
         public void insertUpdate(final DocumentEvent e) {
             syncTablename();
             model.fireChangeEvent();
         }
 
         @Override
         public void removeUpdate(final DocumentEvent e) {
             syncTablename();
             model.fireChangeEvent();
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     final class DragJList extends JList implements DragGestureListener, DragSourceListener {
 
         //~ Methods ------------------------------------------------------------
 
         // <editor-fold defaultstate="collapsed" desc=" Not needed ListenerImpls ">
         @Override
         public void dragDropEnd(final DragSourceDropEvent e) {
             // not needed
         }
 
         @Override
         public void dragEnter(final DragSourceDragEvent e) {
             // not needed
         }
 
         @Override
         public void dragExit(final DragSourceEvent e) {
             // not needed
         }
 
         @Override
         public void dragOver(final DragSourceDragEvent e) {
             // not needed
         }
 
         @Override
         public void dropActionChanged(final DragSourceDragEvent e) {
             // not needed
         }
         // </editor-fold>
 
         //~ Instance fields ----------------------------------------------------
 
         private final transient DragSource dragSource;
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new DragJList object.
          */
         public DragJList() {
             dragSource = DragSource.getDefaultDragSource();
             dragSource.createDefaultDragGestureRecognizer(
                 this,                             // component where drag originates
                 DnDConstants.ACTION_COPY_OR_MOVE, // actions
                 this);                            // drag gesture recognizer
         }
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public void dragGestureRecognized(final DragGestureEvent e) {
             final Object o = this.getSelectedValue();
             final Transferable trans = new CidsTypeTransferable(o);
             dragSource.startDrag(e, DragSource.DefaultCopyDrop, trans, this);
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     final class DropAwareJXTable extends JXTable implements DropTargetListener {
 
         //~ Static fields/initializers -----------------------------------------
 
         public static final String EXTENSION_TYPE = "Extension type"; // NOI18N
 
         // <editor-fold defaultstate="collapsed" desc=" Not needed ListenerImpls ">
 
         // NOI18N
 
         // <editor-fold defaultstate="collapsed" desc=" Not needed ListenerImpls ">
         @Override
         public void dragExit(final DropTargetEvent dte) {
             // not needed
         }
 
         @Override
         public void dropActionChanged(final DropTargetDragEvent dtde) {
             // not needed
         }
 
         @Override
         public void dragOver(final DropTargetDragEvent dtde) {
             // not needed
         }
 
         @Override
         public void dragEnter(final DropTargetDragEvent dtde) {
             // not needed
         }
         // </editor-fold>
 
         //~ Instance fields ----------------------------------------------------
 
         private final transient int acceptableActions;
         private final transient DropTarget dt;
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new DropAwareJXTable object.
          */
         public DropAwareJXTable() {
             acceptableActions = DnDConstants.ACTION_COPY_OR_MOVE;
             dt = new DropTarget(this, acceptableActions, this);
         }
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public void drop(final DropTargetDropEvent dtde) {
             try {
                 final Object o = dtde.getTransferable().getTransferData(CidsTypeTransferable.CIDS_TYPE_FLAVOR);
                 if (o instanceof Type) {
                     final Attribute attr = new Attribute();
                     attr.setCidsClass(cidsClass);
                     final Type t = (Type)o;
                     if (t.isComplexType()) {
                         final CidsClass c = t.getCidsClass();
                         attr.setEditor(c.getEditor());
                         attr.setForeignKey(true);
                         attr.setForeignKeyClass(c.getId());
                         attr.setToString(c.getToString());
                         if (c.isArrayLink()) {
                             attr.setArray(true);
                             attr.setArrayKey(cidsClass.getName() + "_reference");                            // NOI18N
                         }
                     }
                     attr.setName("");                                                                        // NOI18N
                     attr.setType(t);
                     if (!t.isComplexType()
                                 && (t.getName().equalsIgnoreCase("varchar")                                  // NOI18N
                                     || t.getName().equalsIgnoreCase("char")                                  // NOI18N
                                     || t.getName().equalsIgnoreCase("varbit")))                              // NOI18N
                     {
                         final String answer = JOptionPane.showInputDialog(
                                 this,
                                 org.openide.util.NbBundle.getMessage(
                                     NewCidsClassVisualPanel1.class,
                                     "NewCidsClassVisualPanel1.DropAwareJXTable.drop().JOptionPane.message"), // NOI18N
                                 t.getName()
                                         + "(???)",                                                           // NOI18N
                                 JOptionPane.QUESTION_MESSAGE);
                         try {
                             attr.setPrecision(Integer.valueOf(answer));
                         } catch (final Exception skip) {
                             LOG.warn("exception skipped", skip);                                             // NOI18N
                         }
                     }
                     final int newRowIndex = getRowCount();
                     final int lastObjIndex = convertRowIndexToModel(newRowIndex - 1);
                     final Attribute last = classTableModel.getAttributeAt(lastObjIndex);
 
                     attr.setPosition(last.getPosition() + 1);
                     attr.setVisible(Boolean.TRUE);
                     attr.setExtensionAttr(EXTENSION_TYPE.equals(t.getName()));
                     classTableModel.addAttribute(attr);
 
                     // the new attribute is the last onne
                     scrollRowToVisible(newRowIndex);
                     final TableCellEditor tce = getCellEditor(newRowIndex, 0);
                     tce.addCellEditorListener(new CellEditorListener() {
 
                             @Override
                             public void editingStopped(final ChangeEvent e) {
                                 getSelectionModel().setSelectionInterval(newRowIndex, newRowIndex);
                                 tce.removeCellEditorListener(this);
                             }
 
                             @Override
                             public void editingCanceled(final ChangeEvent e) {
                                 editingStopped(e);
                             }
                         });
 
                     editCellAt(newRowIndex, 0);
                     requestFocusInWindow();
                 }
             } catch (final Exception e) {
                 LOG.warn("exception occured during drop action", e); // NOI18N
             } finally {
                 dtde.dropComplete(true);
             }
         }
     }
 }
