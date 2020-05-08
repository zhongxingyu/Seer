 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package de.cismet.cids.editors;
 
 import Sirius.navigator.connection.Connection;
 import Sirius.navigator.connection.ConnectionFactory;
 import Sirius.navigator.connection.ConnectionInfo;
 import Sirius.navigator.connection.ConnectionSession;
 import Sirius.navigator.connection.SessionManager;
 import Sirius.navigator.connection.proxy.ConnectionProxy;
 
 import Sirius.server.localserver.attribute.MemberAttributeInfo;
 import Sirius.server.localserver.attribute.ObjectAttribute;
 import Sirius.server.middleware.types.MetaClass;
 import Sirius.server.middleware.types.MetaClassStore;
 import Sirius.server.middleware.types.MetaObject;
 import Sirius.server.newuser.User;
 
 import org.jdesktop.beansbinding.AutoBinding;
 import org.jdesktop.beansbinding.BeanProperty;
 import org.jdesktop.beansbinding.Binding;
 import org.jdesktop.beansbinding.BindingGroup;
 import org.jdesktop.beansbinding.Bindings;
 import org.jdesktop.beansbinding.Converter;
 import org.jdesktop.beansbinding.ELProperty;
 import org.jdesktop.beansbinding.Validator;
 import org.jdesktop.observablecollections.ObservableList;
 import org.jdesktop.observablecollections.ObservableListListener;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 import java.awt.FlowLayout;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.Vector;
 
 import javax.swing.DefaultListCellRenderer;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.ListCellRenderer;
 import javax.swing.ListModel;
 
 import de.cismet.cids.dynamics.CidsBean;
 import de.cismet.cids.dynamics.DisposableCidsBeanStore;
 
 import de.cismet.cids.editors.converters.BooleanToStringConverter;
 import de.cismet.cids.editors.converters.GeometryToStringConverter;
 import de.cismet.cids.editors.converters.SqlDateToStringConverter;
 
 import de.cismet.cids.navigator.utils.ClassCacheMultiple;
 
 import de.cismet.cids.utils.ClassloadingHelper;
 
 import de.cismet.tools.BlacklistClassloading;
 
 import de.cismet.tools.gui.ComponentWrapper;
 import de.cismet.tools.gui.DoNotWrap;
 import de.cismet.tools.gui.WrappedComponent;
 import de.cismet.tools.gui.log4jquickconfig.Log4JQuickConfig;
 
 /**
  * DOCUMENT ME!
  *
  * @author   thorsten
  * @version  $Revision$, $Date$
  */
 public class CidsObjectEditorFactory {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static CidsObjectEditorFactory editorFactory;
     private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CidsObjectEditorFactory.class);
     public static final String NO_VALUE = org.openide.util.NbBundle.getMessage(
             CidsObjectEditorFactory.class,
             "CidsObjectEditorFactory.NO_VALUE");                                        // NOI18N
     public static final String PARENT_CIDS_EDITOR = "parentCidsEditor";                 // NOI18N
     private static final String CMD_ADD_OBJECT = "cmdAddObject";                        // NOI18N
     private static final String CMD_REMOVE_OBJECT = "cmdRemoveObject";                  // NOI18N
     public static final String CIDS_BEAN = "cidsBean";                                  // NOI18N
     public static final String SOURCE_LIST = "sourceList";                              // NOI18N
     private static Converter nullToBackgroundColorConverter = new IsNullToColorConverter();
 //    private boolean lazyClassFetching = true;
     private static final String EDITOR_PREFIX = "de.cismet.cids.custom.objecteditors."; // NOI18N
     private static final String EDITOR_SUFFIX = "Editor";                               // NOI18N
     private static final String ATTRIBUTE_EDITOR_SUFFIX = "AttributeEditor";            // NOI18N
 
     //~ Instance fields --------------------------------------------------------
 
     private HashMap<String, Converter> defaultConverter = new HashMap<String, Converter>();
     private User user;
     private ComponentWrapper componentWrapper = null;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new CidsObjectEditorFactory object.
      */
     private CidsObjectEditorFactory() {
         // Die Klassennamen werden über class.getName() erzeugt. So checkt der Compiler ob sie korrekt referenziert
         // wurden
         defaultConverter.put(com.vividsolutions.jts.geom.Geometry.class.getName(), new GeometryToStringConverter());
         defaultConverter.put(java.sql.Date.class.getName(), new SqlDateToStringConverter());
         defaultConverter.put(java.lang.Boolean.class.getName(), new BooleanToStringConverter());
 
         try {
             final Class<?> wrapperClass = BlacklistClassloading.forName(
                     "de.cismet.cids.custom.objecteditors.EditorWrapper"); // NOI18N
             componentWrapper = (ComponentWrapper)wrapperClass.newInstance();
         } catch (Exception skip) {
             if (log.isDebugEnabled()) {
                 log.debug("Error while loading the EditorWrapper", skip); // NOI18N
             }
         }
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static CidsObjectEditorFactory getInstance() {
         if (editorFactory == null) {
             editorFactory = new CidsObjectEditorFactory();
             editorFactory.user = SessionManager.getSession().getUser();
         }
         return editorFactory;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public ComponentWrapper getComponentWrapper() {
         return componentWrapper;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private GridBagConstraints getCommonConstraints() {
         final GridBagConstraints gbc = new GridBagConstraints();
         gbc.fill = GridBagConstraints.HORIZONTAL;
         gbc.anchor = GridBagConstraints.NORTHWEST;
         return gbc;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  gbc  DOCUMENT ME!
      */
     private void modifyForLabel(final GridBagConstraints gbc) {
         // gbc.weightx = 0.3;
         gbc.insets = new java.awt.Insets(4, 5, 3, 0);
         gbc.gridx = 0;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  gbc  DOCUMENT ME!
      */
     private void modifyForEditor(final GridBagConstraints gbc) {
         gbc.weightx = 0.7;
         gbc.insets = new java.awt.Insets(0, 5, 3, 0);
         gbc.gridx = 2;
         gbc.gridwidth = 2;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   domain   DOCUMENT ME!
      * @param   classid  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static MetaClass getMetaClass(final String domain, final int classid) {
         return ClassCacheMultiple.getMetaClass(domain, classid);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   MetaObject  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public JComponent getEditor(final MetaObject MetaObject) {
         // Hier kann man noch mit Caching arbeiten
         JComponent editorComponent = getObjectEditor(MetaObject.getMetaClass());
         if (editorComponent == null) {
             editorComponent = (JComponent)getDefaultEditor(MetaObject.getMetaClass());
         }
         final JComponent finalEditorComponent = editorComponent;
         if (editorComponent instanceof DisposableCidsBeanStore) {
             final CidsBean bean = MetaObject.getBean();
 //            final Runnable setCidsBeanRunnable = new Runnable() {
 //
 //                @Override
 //                public void run() {
             ((DisposableCidsBeanStore)finalEditorComponent).setCidsBean(bean);
             if (finalEditorComponent instanceof AutoBindableCidsEditor) {
                 bindCidsEditor((AutoBindableCidsEditor)finalEditorComponent);
             }
 //                }
 //            };
 //            if (EventQueue.isDispatchThread()) {
 //                setCidsBeanRunnable.run();
 //            } else {
 //                try {
 //                    EventQueue.invokeAndWait(setCidsBeanRunnable);
 //                } catch (Throwable t) {
 //                    log.error(t, t);
 //                }
 //            }
         }
 
 //
 //    }
 //    else
 //
 //
 //    {
 //        ed.setCidsBean(MetaObject.getBean());
 //    }
         if (editorComponent != null) {
             if ((componentWrapper != null) && !(editorComponent instanceof DoNotWrap)) {
                 return (JComponent)componentWrapper.wrapComponent((JComponent)editorComponent);
             } else {
                 return editorComponent;
             }
         } else {
             // log
             return null;
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   metaClass  DOCUMENT ME!
      * @param   mai        DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private JComponent getSimpleAttributeEditor(final MetaClass metaClass, final MemberAttributeInfo mai) {
         JComponent ret = null;
 
         if (ret == null) {
             final String attributeClassname = mai.getJavaclassname();
 
             if (attributeClassname.equals(java.lang.String.class.getName())) {
                 ret = new DefaultBindableJTextField();
                 ((DefaultBindableJTextField)ret).setConverter(defaultConverter.get(attributeClassname));
             } else if (attributeClassname.equals(java.sql.Date.class.getName())) {
                 ret = new DefaultBindableDateChooser();
             } else if (attributeClassname.equals(java.sql.Timestamp.class.getName())) {
                 ret = new DefaultBindableTimestampChooser();
             } else if (attributeClassname.equals(java.lang.Boolean.class.getName())) {
                 ret = new DefaultBindableJCheckBox();
             } else if (mai.isForeignKey() && mai.isSubstitute()) {
                 final MetaClass foreignClass = getMetaClass(metaClass.getDomain(), mai.getForeignKeyClassId());
                 if (foreignClass.getClassAttribute("reasonable_few") != null) {                          // NOI18N
                     ret = new DefaultBindableReferenceCombo(foreignClass);
                 }
             } else {
                 if (log.isDebugEnabled()) {
                     log.debug("no DefaultEditor for " + attributeClassname + " found. set to textbox "); // NOI18N
                 }
                 ret = new DefaultBindableJTextField();
                 ((DefaultBindableJTextField)ret).setConverter(defaultConverter.get(attributeClassname));
             }
         }
 
 //        if (ret!=null){
 //            ret.setOpaque(false);
 //        }
 
         return ret;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   metaClass  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private JComponent getObjectEditor(final MetaClass metaClass) {
         final Class<?> editorClass = ClassloadingHelper.getDynamicClass(
                 metaClass,
                 ClassloadingHelper.CLASS_TYPE.EDITOR);
         if (editorClass != null) {
             try {
                 final JComponent ed = (JComponent)editorClass.newInstance();
                 if (ed instanceof MetaClassStore) {
                     ((MetaClassStore)ed).setMetaClass(metaClass);
                 }
                 return ed;
             } catch (Throwable e) {
                 log.error("Error beim erzeugen der Editorklasse " + editorClass, e);
             }
         }
         return null;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   metaClass  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  UnsupportedOperationException  DOCUMENT ME!
      */
     private AutoBindableCidsEditor getDefaultEditor(final MetaClass metaClass) {
         final Vector<MemberAttributeInfo> mais = new Vector<MemberAttributeInfo>(metaClass.getMemberAttributeInfos()
                         .values());
 //        final FinalReference<AutoBindableCidsEditor> result = new FinalReference<AutoBindableCidsEditor>();
 //        final Runnable createDefaultEditorRunnable = new Runnable() {
 //
 //            @Override
 //            public void run() {
 
         final DefaultCidsEditor cidsEditor = new DefaultCidsEditor();
 //                result.setObject(cidsEditor);
         final GridBagLayout gbl = new GridBagLayout();
         GridBagConstraints gbc = null;
         cidsEditor.setLayout(gbl);
         int row = 0;
 
         for (final MemberAttributeInfo mai : mais) {
             if (mai.isVisible()) {
                 // Description
                 final JLabel lblDescription = new JLabel();
                 lblDescription.setText(mai.getName());
                 lblDescription.setHorizontalAlignment(JLabel.RIGHT);
                 gbc = getCommonConstraints();
                 modifyForLabel(gbc);
                 gbc.gridy = row;
                 cidsEditor.add(lblDescription, gbc);
 
                 // Editor
                 JComponent cmpEditor = null;
 
                 if (mai.isForeignKey()) {
                     final int foreignKey = mai.getForeignKeyClassId();
                     final String domain = metaClass.getDomain();
                     final MetaClass foreignClass = getMetaClass(domain, foreignKey);
 
                     if (mai.isArray()) {
                         // --------------------------------------------------
                         // Arrays
                         // --------------------------------------------------
                         MetaClass detailClass = null;
 
                         // Detaileditorcomponent
                         final Vector<MemberAttributeInfo> arrayAttrs = new Vector<MemberAttributeInfo>(
                                 foreignClass.getMemberAttributeInfos().values());
                         for (final MemberAttributeInfo arrayMai : arrayAttrs) {
                             if (arrayMai.isForeignKey()) {
                                 final int detailKey = arrayMai.getForeignKeyClassId();
                                 detailClass = getMetaClass(domain, detailKey);
                                 cmpEditor = (JComponent)getObjectEditor(detailClass);
                                 if (cmpEditor == null) {
                                     cmpEditor = (JComponent)getDefaultEditor(detailClass);
                                 }
 
                                 if (cmpEditor instanceof BindingInformationProvider) {
                                     final BindingInformationProvider ed = (BindingInformationProvider)cmpEditor;
                                     final Set<String> fields = ed.getAllControls().keySet();
                                     for (final String key : fields) {
                                         final String newKey = mai.getFieldName().toLowerCase() + "[]." + key; // NOI18N
                                         cidsEditor.addControlInformation(newKey, ed.getAllControls().get(key));
                                     }
                                 } else if (cmpEditor instanceof Bindable) {
                                     // TODO
                                     throw new UnsupportedOperationException();
                                 }
 
                                 break;
                             }
                         }
 
                         // Masterliste
                         cidsEditor.remove(lblDescription);
                         gbc = getCommonConstraints();
                         modifyForLabel(gbc);
                         gbc.insets = new java.awt.Insets(4, 25, 3, 0);
                         gbc.gridy = row++;
                         gbc.fill = java.awt.GridBagConstraints.BOTH;
                         final String field = mai.getFieldName().toLowerCase();
 
                         final BindableJList lstArrayMaster = new BindableJList();
 
                        // <editor-fold defaultstate="collapsed" desc="CellRenderer">
                         final DefaultListCellRenderer dlcr = new DefaultListCellRenderer();
                         lstArrayMaster.setCellRenderer(new ListCellRenderer() {
 
                                 @Override
                                 public Component getListCellRendererComponent(final JList list,
                                         final Object value,
                                         final int index,
                                         final boolean isSelected,
                                         final boolean cellHasFocus) {
                                     final JLabel l = (JLabel)dlcr.getListCellRendererComponent(
                                             list,
                                             value,
                                             index,
                                             isSelected,
                                             cellHasFocus);
                                     if ((l.getText() == null) || l.getText().trim().equals("")
                                                 || l.getText().equals("null")) {  // TODO Der check auf den String
                                                                                   // "null" muss wieder raus //NOI18N
                                         final CidsBean cb = (CidsBean)value;
                                         if (cb.getMetaObject().getStatus() == MetaObject.NEW) {
                                             l.setText("neues Element");           // NOI18N
                                             if (isSelected) {
                                                 l.setBackground(Color.GREEN);
                                             }
                                         } else {
                                             l.setText(
                                                 cb.getMetaObject().getMetaClass().toString()
                                                         + " "
                                                         + cb.getProperty(
                                                             cb.getMetaObject().getMetaClass().getPrimaryKey()
                                                                 .toLowerCase())); // NOI18N
                                         }
                                     }
                                     return l;
                                 }
                             });
// </editor-fold>
 
                         final ArrayTitleAndControls arrayTitleAndControls = new ArrayTitleAndControls(
                                 lblDescription.getText(),
                                 detailClass,
                                 field,
                                 lstArrayMaster);
                         cidsEditor.add(arrayTitleAndControls, gbc);
 
                         gbc = getCommonConstraints();
                         modifyForLabel(gbc);
                         gbc.insets = new java.awt.Insets(4, 25, 0, 0);
                         gbc.gridy = row;
                         gbc.fill = java.awt.GridBagConstraints.BOTH;
                         cidsEditor.addControlInformation(field + "[]", lstArrayMaster); // NOI18N
                         cidsEditor.add(lstArrayMaster, gbc);
 
                         gbc = getCommonConstraints();
                         modifyForEditor(gbc);
                         gbc.gridy = row;
                         cmpEditor.putClientProperty(PARENT_CIDS_EDITOR, cidsEditor);
                         cidsEditor.add(cmpEditor, gbc);
                     } else if (mai.isForeignKey()) {
                         // --------------------------------------------------
                         // Normale Unterobjekte
                         // --------------------------------------------------
 
                         // Entfernen Button
                         gbc = getCommonConstraints();
                         modifyForLabel(gbc);
                         gbc.fill = GridBagConstraints.NONE;
                         gbc.insets = new java.awt.Insets(0, 0, 0, 3);
                         gbc.gridx = 3;
                         gbc.gridy = row;
                         final JButton cmdRemove = new JButton();
                         cmdRemove.setBorderPainted(false);
                         cmdRemove.setMinimumSize(new Dimension(12, 12));
                         cmdRemove.setPreferredSize(new Dimension(12, 12));
 
                         cmdRemove.setIcon(new javax.swing.ImageIcon(
                                 getClass().getResource("/de/cismet/cids/editors/edit_remove_mini.png"))); // NOI18N
                         cmdRemove.setVisible(false);
                         cidsEditor.add(cmdRemove, gbc);
 
                         // Erstellen Button
                         gbc = getCommonConstraints();
                         modifyForLabel(gbc);
                         gbc.insets = new java.awt.Insets(0, 0, 0, 3);
                         gbc.fill = GridBagConstraints.NONE;
                         gbc.gridx = 3;
                         gbc.gridy = row;
 
                         final JButton cmdAdd = new JButton();
                         cmdAdd.setIcon(new javax.swing.ImageIcon(
                                 getClass().getResource("/de/cismet/cids/editors/edit_add_mini.png"))); // NOI18N
                         cmdAdd.setBorderPainted(false);
                         cmdAdd.setMinimumSize(new Dimension(12, 12));
                         cmdAdd.setPreferredSize(new Dimension(12, 12));
                         cmdAdd.setVisible(false);
                         cidsEditor.add(cmdAdd, gbc);
 
                         // Editor
 
                         cmpEditor = getCustomAttributeEditor(metaClass, mai);
 
                         if (cmpEditor == null) {
                             cmpEditor = (JComponent)getObjectEditor(foreignClass);
                         }
 
                         if ((cmpEditor == null) && mai.isSubstitute()) {
                             cmpEditor = getSimpleAttributeEditor(metaClass, mai);
                         }
 
                         // Sicherheithalber ....
                         if (cmpEditor == null) {
                             cmpEditor = (JComponent)getDefaultEditor(foreignClass);
                         }
 
                         // bindable geht vor
                         if (cmpEditor instanceof Bindable) {
                             cidsEditor.addControlInformation(mai.getFieldName().toLowerCase(), (Bindable)cmpEditor);
                         } else if (cmpEditor instanceof BindingInformationProvider) {
                             final BindingInformationProvider ed = (BindingInformationProvider)cmpEditor;
                             final Set<String> fields = ed.getAllControls().keySet();
                             for (final String key : fields) {
                                 final String newKey = mai.getFieldName().toLowerCase() + "." + key; // NOI18N
                                 cidsEditor.addControlInformation(newKey, ed.getAllControls().get(key));
                             }
                         }
 
                         gbc = getCommonConstraints();
                         modifyForEditor(gbc);
                         gbc.gridwidth = 1;
                         gbc.gridy = row;
                         if (cmpEditor != null) {
                             cmpEditor.putClientProperty(PARENT_CIDS_EDITOR, cidsEditor);
                             cidsEditor.add(cmpEditor, gbc);
                             cmpEditor.putClientProperty(CMD_ADD_OBJECT, cmdAdd);
                             cmpEditor.putClientProperty(CMD_REMOVE_OBJECT, cmdRemove);
                         } else {
                             log.warn("Editor was null. " + metaClass.getTableName() + "." + mai.getFieldName()); // NOI18N
                         }
                     }
                 } else {
                     // Die Editorkomponente über die Metainformations checken
 
                     // --------------------------------------------------
                     // Einfache Attribute
                     // --------------------------------------------------
 
                     cmpEditor = getCustomAttributeEditor(metaClass, mai);
 
                     if (cmpEditor == null) {
                         cmpEditor = getSimpleAttributeEditor(metaClass, mai);
                     }
                     if (log.isDebugEnabled()) {
                         log.debug("ATTRIBUTE_CLASS_NAME:" + mai.getJavaclassname() + " --> "
                                     + cmpEditor.getClass().toString()); // NOI18N
                     }
                     cidsEditor.addControlInformation(mai.getFieldName().toLowerCase(), (Bindable)cmpEditor);
                     gbc = getCommonConstraints();
                     modifyForEditor(gbc);
                     gbc.gridy = row;
                     cmpEditor.putClientProperty(PARENT_CIDS_EDITOR, cidsEditor);
                     cidsEditor.add(cmpEditor, gbc);
                 }
             }
 
             row++;
         }
 //            }
 //        };
 //        if (EventQueue.isDispatchThread()) {
 //            createDefaultEditorRunnable.run();
 //        } else {
 //            try {
 //                EventQueue.invokeAndWait(createDefaultEditorRunnable);
 //            } catch (Throwable t) {
 //                log.error(t, t);
 //                return null;
 //            }
 //        }
 //        return result.getObject();
         return cidsEditor;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   metaClass  DOCUMENT ME!
      * @param   mai        DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private JComponent getCustomAttributeEditor(final MetaClass metaClass, final MemberAttributeInfo mai) {
         // TODO
         // Hier müssen auch noch die Einstellungen inder DB (ComplexEditor, Editor) berücksichtigt werden
 
         // MetaClass contains the MemberAttributeInfo
         try {
             final Class<?> attrEditorClass = ClassloadingHelper.getDynamicClass(
                     metaClass,
                     mai,
                     ClassloadingHelper.CLASS_TYPE.ATTRIBUTE_EDITOR);
 
             if (attrEditorClass != null) {
                 final MetaClass foreignClass;
                 if (MetaClassStore.class.isAssignableFrom(attrEditorClass) && mai.isForeignKey()) {
                     foreignClass = getMetaClass(metaClass.getDomain(), mai.getForeignKeyClassId());
                 } else {
                     foreignClass = null;
                 }
 //                final Runnable createAttributeEditorRunnable = new Runnable() {
 //
 //                    @Override
 //                    public void run() {
                 try {
                     final Bindable editor = (Bindable)attrEditorClass.newInstance();
                     if (foreignClass != null) {
                         ((MetaClassStore)editor).setMetaClass(foreignClass);
                     }
                     return (JComponent)editor;
                 } catch (Throwable t) {
                     log.error("getCustomAttributeEditor von " + metaClass.getTableName() + "." + mai.getFieldName()
                                 + " liefert einen Fehler",
                         t);
                     throw new RuntimeException(t);
                 }
 //                    }
 //                };
 //                if (EventQueue.isDispatchThread()) {
 //                    createAttributeEditorRunnable.run();
 //                } else {
 //                    EventQueue.invokeAndWait(createAttributeEditorRunnable);
 //                }
             }
         } catch (Exception e) {
             log.error("Error when creating a SimpleAttributeEditor", e); // NOI18N
         }
         return null;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  ed  DOCUMENT ME!
      */
     private void bindCidsEditor(final AutoBindableCidsEditor ed) {
         final BindingGroup bg = ed.getBindingGroup();
 //        MetaObject MetaObject = ed.getCidsBean().getMetaObject();
 //        ObjectAttribute[] allAttrs = MetaObject.getAttribs();
         Binding binding = null;
         final Set<String> keys = ed.getAllControls().keySet();
         final HashMap<String, JList> arraylists = new HashMap<String, JList>();
 
         final HashSet complexEditors = new HashSet();
 
         // Prefetching all the JLists
         for (final String key : keys) {
             if (key.endsWith("[]")) { // NOI18N
                 final JList lstList = (JList)ed.getControlByName(key);
                 arraylists.put(key, lstList);
             }
         }
 
         for (final String key : keys) {
             if (key.endsWith("[]")) { // NOI18N
                 // --------------------------------------------------
                 // Array
                 // --------------------------------------------------
 
                 // --------------------------------------------------
                 // Zuerst die Master JLists
                 // --------------------------------------------------
                 final String keyWithoutBrackets = key.substring(0, key.length() - 2);
                 Object bindingSource = null;
                 ELProperty elProperty = null;
 
                 final JList lstList = arraylists.get(key);
 
                 if (keyWithoutBrackets.contains("[]")) { // NOI18N
                     // [] mehr als einmal vorhanden
 
                     final String parentListIdentifier = keyWithoutBrackets.substring(
                             0,
                             keyWithoutBrackets.lastIndexOf("[]")
                                     + 2);                                                               // NOI18N
                     bindingSource = arraylists.get(parentListIdentifier);
                     lstList.putClientProperty(SOURCE_LIST, bindingSource);
                     final String subKeyWithoutBrackets = key.substring(key.indexOf(parentListIdentifier) + ".".length()
                                     + parentListIdentifier.length(),
                             key.length()
                                     - 2);                                                               // NOI18N
                     elProperty = ELProperty.create("${selectedElement." + subKeyWithoutBrackets + "}"); // NOI18N
                 } else {
                     bindingSource = ed;
                     lstList.putClientProperty(CIDS_BEAN, ed.getCidsBean());
                     elProperty = ELProperty.create("${cidsBean." + keyWithoutBrackets + "}");           // NOI18N
                 }
 
                 final org.jdesktop.swingbinding.JListBinding jListBinding = org.jdesktop.swingbinding.SwingBindings
                             .createJListBinding(
                                 AutoBinding.UpdateStrategy.READ_WRITE,
                                 bindingSource,
                                 elProperty,
                                 lstList);
 
                // <editor-fold defaultstate="collapsed" desc="füge PC Listener hinzu um Änderungen an den variablen direkt in der Liste anzuzeigen">
                 try {
                     final ObservableList observableList = (ObservableList)ed.getCidsBean()
                                 .getProperty(keyWithoutBrackets);
                     for (final Object o : observableList) {
                         ((CidsBean)o).addPropertyChangeListener(new PropertyChangeListener() {
 
                                 @Override
                                 public void propertyChange(final PropertyChangeEvent evt) {
                                     lstList.repaint();
                                 }
                             });
                     }
 
                     // und jetzt noch für die zukuenftigen
                     observableList.addObservableListListener(new ObservableListListenerAdapter() {
 
                             @Override
                             public void listElementsAdded(final ObservableList list,
                                     final int index,
                                     final int length) {
                                 for (int i = index; i < (index + length); ++i) {
                                     ((CidsBean)list.get(i)).addPropertyChangeListener(new PropertyChangeListener() {
 
                                             @Override
                                             public void propertyChange(final PropertyChangeEvent evt) {
                                                 lstList.repaint();
                                             }
                                         });
                                 }
                             }
                         });

                    // </editor-fold>
                 } catch (Exception e) {
                     if (log.isDebugEnabled()) {
                         log.debug("No observableList update for Array in Array in ...", e); // NOI18N
                     }
                 }
 
                 bg.addBinding(jListBinding);
             } else {
                 // --------------------------------------------------
                 // keine Arrays
                 // --------------------------------------------------
                 final JComponent jc = (JComponent)ed.getControlByName(key); // in jc steckt die Editorkomponente
                 final Bindable bjc = (Bindable)jc;
 
                 final BindingInformationProvider parentCidsEditor = (BindingInformationProvider)jc.getClientProperty(
                         PARENT_CIDS_EDITOR);
 
                 binding = Bindings.createAutoBinding(
                         AutoBinding.UpdateStrategy.READ_WRITE,
                         jc,
                         ELProperty.create("${text==null}"),
                         jc,
                         BeanProperty.create("background")); // NOI18N
                 binding.setConverter(nullToBackgroundColorConverter);
 
                 bg.addBinding(binding);
 
                 if (key.contains("[]")) { // NOI18N
                     // --------------------------------------------------
                     // Detailattribute
                     // --------------------------------------------------
                     final int whereSubKeyStarts = key.lastIndexOf("[]") + 3; // NOI18N
                     final String arrayFieldWithBrackets = key.substring(0, whereSubKeyStarts - 1);
                     final JList list = (JList)ed.getControlByName(arrayFieldWithBrackets);
                     final String subkey = key.substring(whereSubKeyStarts);
                     final String exp = "selectedElement." + subkey;          // NOI18N
                     binding = Bindings.createAutoBinding(
                             AutoBinding.UpdateStrategy.READ_WRITE,
                             list,
                             ELProperty.create("${" + exp + "}"),
                             jc,
                             BeanProperty.create(bjc.getBindingProperty()));  // NOI18N
                     binding.setSourceUnreadableValue(null);
                     final Converter c = bjc.getConverter();
                     if (c != null) {
                         binding.setConverter(c);
                     }
                     final Object nullValue = bjc.getNullSourceValue();
                     final Object errorValue = bjc.getErrorSourceValue();
                     if (nullValue != null) {
                         binding.setSourceNullValue(nullValue);
                     }
                     if (errorValue != null) {
                         binding.setSourceUnreadableValue(errorValue);
                     }
 
                     bg.addBinding(binding);
 
                     // Direktes Detailattribut (oder ein Subobjekt)
                     if (subkey.contains(".")) {                                             // NOI18N
                         final String[] sa = subkey.split("\\.");                            // NOI18N
                         final String object = subkey.substring(0, subkey.lastIndexOf(".")); // NOI18N
                         // Check ob das Teilobjekt nicht auf null gesetzt ist
                         final String expression = "selectedElement." + object; // NOI18N
                         addDisablingAndNullCheckerBindings(bg, expression, list, jc);
                         addAddRemoveControlVisibilityBinding(bg, (JComponent)parentCidsEditor, list, object, true);
                     }
                 } else {
                     // --------------------------------------------------
                     // nicht Teil eines Arrays
                     // --------------------------------------------------
 
                     binding = Bindings.createAutoBinding(
                             AutoBinding.UpdateStrategy.READ_WRITE,
                             ed,
                             ELProperty.create("${cidsBean." + key + "}"),
                             jc,
                             BeanProperty.create(bjc.getBindingProperty())); // NOI18N
                     final Converter c = bjc.getConverter();
                     if (c != null) {
                         binding.setConverter(c);
                     }
                     final Object nullValue = bjc.getNullSourceValue();
                     final Object errorValue = bjc.getErrorSourceValue();
                     if (nullValue != null) {
                         binding.setSourceNullValue(nullValue);
                     }
                     if (errorValue != null) {
                         binding.setSourceUnreadableValue(errorValue);
                     }
 
                     bg.addBinding(binding);
 
                     if (key.contains(".")) { // NOI18N
                         // Subobjekt
                         final String[] sa = key.split("\\.");                         // NOI18N
                         final String attribute = sa[sa.length - 1];
                         final String object = key.substring(0, key.lastIndexOf(".")); // NOI18N
 
                         // Check ob das Teilobjekt nicht auf null gesetzt ist
                         final String expression = "cidsBean." + object; // NOI18N
                         addDisablingAndNullCheckerBindings(bg, expression, ed, jc);
                         addAddRemoveControlVisibilityBinding(
                             bg,
                             (JComponent)parentCidsEditor,
                             (JComponent)ed,
                             object,
                             false);
                     } else if (bjc instanceof DisposableCidsBeanStore) {
                         // Subobjekt das nur durch ein Bindable editiert wird
                         final String expression = "cidsBean." + key; // NOI18N
                         addDisablingAndNullCheckerBindings(bg, expression, ed, jc);
                         addAddRemoveControlVisibilityBinding(bg, jc, (JComponent)ed, key, false);
                     }
                 }
                 // hier wird sichergestellt dass nur einmal für jeden komplexen editor die +/- buttons hinzugefuegt
                 // werden
                 if (parentCidsEditor != null) {
                     complexEditors.add(parentCidsEditor);
                 }
             }
         }
         bg.bind();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  bg            DOCUMENT ME!
      * @param  expression    DOCUMENT ME!
      * @param  sourceObject  DOCUMENT ME!
      * @param  component     DOCUMENT ME!
      */
     private void addDisablingAndNullCheckerBindings(final BindingGroup bg,
             final String expression,
             final Object sourceObject,
             final JComponent component) {
         Binding binding = Bindings.createAutoBinding(
                 AutoBinding.UpdateStrategy.READ_WRITE,
                 sourceObject,
                 ELProperty.create("${" + expression + "!=null}"),
                 component,
                 BeanProperty.create("enabled"));    // NOI18N
         bg.addBinding(binding);
         binding = Bindings.createAutoBinding(
                 AutoBinding.UpdateStrategy.READ_WRITE,
                 sourceObject,
                 ELProperty.create("${" + expression + "==null}"),
                 component,
                 BeanProperty.create("background")); // NOI18N
         binding.setConverter(nullToBackgroundColorConverter);
         // binding.setSourceNullValue(NO_VALUE);//Geht nicht weil ed.cidsBean nicht null ist
         bg.addBinding(binding);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  bg                     DOCUMENT ME!
      * @param  buttonContainer        DOCUMENT ME!
      * @param  bindingSourceObject    DOCUMENT ME!
      * @param  attributeName          DOCUMENT ME!
      * @param  detailObjectOfAnArray  DOCUMENT ME!
      */
     private void addAddRemoveControlVisibilityBinding(final BindingGroup bg,
             final JComponent buttonContainer,
             final JComponent bindingSourceObject,
             final String attributeName,
             final boolean detailObjectOfAnArray) {
         Binding binding = null;
         final JButton cmdAdd = (JButton)buttonContainer.getClientProperty("cmdAddObject");       // NOI18N
         final JButton cmdRemove = (JButton)buttonContainer.getClientProperty("cmdRemoveObject"); // NOI18N
         String objectExpression;
         if (detailObjectOfAnArray) {
             objectExpression = "selectedElement." + attributeName;                               // NOI18N
         } else {
             objectExpression = "cidsBean." + attributeName;                                      // NOI18N
         }
         if (cmdAdd != null) {
             binding = Bindings.createAutoBinding(
                     AutoBinding.UpdateStrategy.READ_WRITE,
                     bindingSourceObject,
                     ELProperty.create("${" + objectExpression + "==null}"),
                     cmdAdd,
                     BeanProperty.create("visible"));                                             // NOI18N
             bg.addBinding(binding);
             cmdAdd.addActionListener(new ActionListener() {
 
                     @Override
                     public void actionPerformed(final ActionEvent e) {
                         CidsBean actionBean;
                         if (detailObjectOfAnArray) {
                             actionBean = (CidsBean)((JList)bindingSourceObject).getSelectedValue();
                         } else {
                             actionBean = ((DisposableCidsBeanStore)bindingSourceObject).getCidsBean();
                         }
                         final ObjectAttribute oa = actionBean.getMetaObject().getAttributeByFieldName(attributeName);
                         final MetaClass mc = getMetaClass(
                                 actionBean.getMetaObject().getDomain(),
                                 oa.getMai().getForeignKeyClassId());
                         final CidsBean newOne = mc.getEmptyInstance().getBean();
                         try {
                             actionBean.setProperty(attributeName, newOne);
                         } catch (Exception ex) {
                             ex.printStackTrace();
                         }
                     }
                 });
         }
         if (cmdRemove != null) {
             binding = Bindings.createAutoBinding(
                     AutoBinding.UpdateStrategy.READ_WRITE,
                     bindingSourceObject,
                     ELProperty.create("${" + objectExpression + "!=null}"),
                     cmdRemove,
                     BeanProperty.create("visible")); // NOI18N
             bg.addBinding(binding);
             cmdRemove.addActionListener(new ActionListener() {
 
                     @Override
                     public void actionPerformed(final ActionEvent e) {
                         CidsBean actionBean;
                         if (detailObjectOfAnArray) {
                             actionBean = (CidsBean)((JList)bindingSourceObject).getSelectedValue();
                         } else {
                             actionBean = ((DisposableCidsBeanStore)bindingSourceObject).getCidsBean();
                         }
 
                         try {
                             ((CidsBean)actionBean.getProperty(attributeName)).delete();
                             // anderer option nur null setzen actionBean.setProperty(attributeName, null);
                         } catch (Exception ex) {
                             ex.printStackTrace();
                         }
                     }
                 });
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   args  DOCUMENT ME!
      *
      * @throws  Exception  DOCUMENT ME!
      */
     public static void main(final String[] args) throws Exception {
         EventQueue.invokeLater(new Runnable() {
 
                 @Override
                 public void run() {
                     try {
                         final JPanel panCommand = new JPanel();
 
                         final String domain = "WUNDA_DEMO"; // NOI18N
                         final int CLASSID = 374;
                         final int OBJECTID = 1;
 //                    final int CLASSID = 47; //Bauvisualisierung
 //                    final int OBJECTID = 1;
 
 //                    final int CLASSID = 45; //POI
 //                    final int OBJECTID = 7; //botanischer Garten
 
                         Log4JQuickConfig.configure4LumbermillOnLocalhost();
                         ConnectionSession session = null;
                         ConnectionProxy proxy = null;
                         final ConnectionInfo connectionInfo = new ConnectionInfo();
                         connectionInfo.setCallserverURL("rmi://localhost/callServer"); // NOI18N
                         connectionInfo.setPassword("demo");                            // NOI18N
                         connectionInfo.setUsergroup("Demo");                           // NOI18N
                         connectionInfo.setUserDomain("WUNDA_DEMO");                    // NOI18N
                         connectionInfo.setUsergroupDomain("WUNDA_DEMO");               // NOI18N
                         connectionInfo.setUsername("demo");                            // NOI18N
 
                         final Connection connection = ConnectionFactory.getFactory()
                                     .createConnection(
                                         "Sirius.navigator.connection.RMIConnection",
                                         connectionInfo.getCallserverURL()); // NOI18N
 
                         // connection.g
 
                         session = ConnectionFactory.getFactory().createSession(connection, connectionInfo, true);
                         proxy = ConnectionFactory.getFactory()
                                     .createProxy(
                                             "Sirius.navigator.connection.proxy.DefaultConnectionProxyHandler",
                                             session); // NOI18N
                         SessionManager.init(proxy);
 
                         final MetaObject MetaObject = SessionManager.getConnection()
                                     .getMetaObject(SessionManager.getSession().getUser(), OBJECTID, CLASSID, domain); // meta.getMetaObject(u, 1, AAPERSON_CLASSID, domain);
 
                         log.fatal(MetaObject.getDebugString());
 
                         final JFrame tester = new JFrame(
                                 org.openide.util.NbBundle.getMessage(
                                     CidsObjectEditorFactory.class,
                                     "CidsObjectEditorFactory.tester.title")); // NOI18N
                         final Container cp = tester.getContentPane();
                         cp.setLayout(new BorderLayout());
                         final JComponent ed = CidsObjectEditorFactory.getInstance().getEditor(MetaObject);
                         cp.add(ed);
                         tester.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
                         final JButton cmdPersist = new JButton(
                                 org.openide.util.NbBundle.getMessage(
                                     CidsObjectEditorFactory.class,
                                     "CidsObjectEditorFactory.cmdPersist.text")); // NOI18N
                         cmdPersist.addActionListener(new ActionListener() {
 
                                 @Override
                                 public void actionPerformed(final ActionEvent e) {
                                     try {
                                         if (ed instanceof WrappedComponent) {
                                             ((DisposableCidsBeanStore)((WrappedComponent)ed).getOriginalComponent())
                                                     .getCidsBean().persist();
                                         } else {
                                             ((DisposableCidsBeanStore)ed).getCidsBean().persist();
                                         }
                                     } catch (Exception ex) {
                                         ex.printStackTrace();
                                     }
                                 }
                             });
 
                         final JButton cmdLog = new JButton(
                                 org.openide.util.NbBundle.getMessage(
                                     CidsObjectEditorFactory.class,
                                     "CidsObjectEditorFactory.cmdLog.text")); // NOI18N
                         cmdLog.addActionListener(new ActionListener() {
 
                                 @Override
                                 public void actionPerformed(final ActionEvent e) {
                                     CidsBean cb = null;
                                     if (ed instanceof WrappedComponent) {
                                         cb = ((DisposableCidsBeanStore)((WrappedComponent)ed).getOriginalComponent())
                                                     .getCidsBean();
                                     } else {
                                         cb = ((DisposableCidsBeanStore)ed).getCidsBean();
                                     }
                                     if (cb != null) {
                                         log.fatal(cb.getMOString());
                                     }
                                 }
                             });
 
                         final JButton cmdReload = new JButton(
                                 org.openide.util.NbBundle.getMessage(
                                     CidsObjectEditorFactory.class,
                                     "CidsObjectEditorFactory.cmdReload.text")); // NOI18N
                         cmdReload.addActionListener(new ActionListener() {
 
                                 @Override
                                 public void actionPerformed(final ActionEvent e) {
                                     try {
                                         AutoBindableCidsEditor abce = null;
                                         if (ed instanceof WrappedComponent) {
                                             abce = ((AutoBindableCidsEditor)((WrappedComponent)ed)
                                                             .getOriginalComponent());
                                         } else {
                                             abce = ((AutoBindableCidsEditor)ed);
                                         }
                                         if (abce != null) {
                                             abce.setCidsBean(
                                                 SessionManager.getConnection().getMetaObject(
                                                     SessionManager.getSession().getUser(),
                                                     OBJECTID,
                                                     CLASSID,
                                                     domain).getBean());
                                             abce.getBindingGroup().unbind();
                                             abce.getBindingGroup().bind();
                                         }
                                     } catch (Exception ex) {
                                         ex.printStackTrace();
                                     }
                                 }
                             });
 
                         panCommand.setLayout(new FlowLayout(FlowLayout.CENTER));
                         panCommand.add(cmdPersist);
                         panCommand.add(cmdLog);
                         panCommand.add(cmdReload);
                         tester.getContentPane().add(panCommand, BorderLayout.SOUTH);
                         tester.setSize(new Dimension(800, 500));
                         tester.setVisible(true);
                     } catch (Exception e) {
                         e.printStackTrace();
                     }
                 }
             });
     }
 }
 /**
  * DOCUMENT ME!
  *
  * @version  $Revision$, $Date$
  */
 class ObservableListListenerAdapter implements ObservableListListener {
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public void listElementPropertyChanged(final ObservableList list, final int index) {
     }
 
     @Override
     public void listElementReplaced(final ObservableList list, final int index, final Object oldElement) {
     }
 
     @Override
     public void listElementsAdded(final ObservableList list, final int index, final int length) {
     }
 
     @Override
     public void listElementsRemoved(final ObservableList list, final int index, final List oldElements) {
     }
 }
 
 /**
  * DOCUMENT ME!
  *
  * @version  $Revision$, $Date$
  */
 class BindableJList extends JList implements Bindable {
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new BindableJList object.
      */
     public BindableJList() {
     }
 
     /**
      * Creates a new BindableJList object.
      *
      * @param  listData  DOCUMENT ME!
      */
     public BindableJList(final Vector<?> listData) {
         super(listData);
     }
 
     /**
      * Creates a new BindableJList object.
      *
      * @param  listData  DOCUMENT ME!
      */
     public BindableJList(final Object[] listData) {
         super(listData);
     }
 
     /**
      * Creates a new BindableJList object.
      *
      * @param  dataModel  DOCUMENT ME!
      */
     public BindableJList(final ListModel dataModel) {
         super(dataModel);
     }
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public String getBindingProperty() {
         return null;
     }
 
     @Override
     public Converter getConverter() {
         return null;
     }
 
     @Override
     public Validator getValidator() {
         return null;
     }
 
     @Override
     public Object getNullSourceValue() {
         return null;
     }
 
     @Override
     public Object getErrorSourceValue() {
         return null;
     }
 }
