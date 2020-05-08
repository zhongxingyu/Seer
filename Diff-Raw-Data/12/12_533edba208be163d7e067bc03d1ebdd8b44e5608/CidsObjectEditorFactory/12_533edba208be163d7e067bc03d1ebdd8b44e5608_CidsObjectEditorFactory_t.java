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
 import de.cismet.cids.editors.converters.GeometryToStringConverter;
 import de.cismet.cids.editors.converters.SqlDateToStringConverter;
 import de.cismet.cids.editors.converters.BooleanToStringConverter;
 import Sirius.server.localserver.attribute.MemberAttributeInfo;
 import Sirius.server.localserver.attribute.ObjectAttribute;
 import Sirius.server.middleware.types.MetaClass;
 import Sirius.server.middleware.types.MetaClassStore;
 import Sirius.server.middleware.types.MetaObject;
 import Sirius.server.newuser.User;
 import de.cismet.cids.dynamics.CidsBean;
 import de.cismet.cids.dynamics.CidsBeanStore;
 import de.cismet.cids.navigator.utils.ClassCacheMultiple;
 import de.cismet.cids.navigator.utils.FinalReference;
 import de.cismet.tools.BlacklistClassloading;
 import de.cismet.tools.gui.ComponentWrapper;
 import de.cismet.tools.gui.DoNotWrap;
 import de.cismet.tools.gui.WrappedComponent;
 import de.cismet.tools.gui.log4jquickconfig.Log4JQuickConfig;
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
 
 
 /**
  *
  * @author thorsten
  */
 public class CidsObjectEditorFactory {
 
     private static CidsObjectEditorFactory editorFactory;
     private final static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CidsObjectEditorFactory.class);
     public static final String NO_VALUE = org.openide.util.NbBundle.getMessage(CidsObjectEditorFactory.class, "CidsObjectEditorFactory.NO_VALUE");//NOI18N
     public static final String PARENT_CIDS_EDITOR = "parentCidsEditor";//NOI18N
     private static final String CMD_ADD_OBJECT = "cmdAddObject";//NOI18N
     private static final String CMD_REMOVE_OBJECT = "cmdRemoveObject";//NOI18N
     public static final String CIDS_BEAN = "cidsBean";//NOI18N
     public static final String SOURCE_LIST = "sourceList";//NOI18N
     private static Converter nullToBackgroundColorConverter = new IsNullToColorConverter();
     private boolean lazyClassFetching = true;
     private static final String EDITOR_PREFIX = "de.cismet.cids.custom.objecteditors.";//NOI18N
     private static final String EDITOR_SUFFIX = "Editor";//NOI18N
     private static final String ATTRIBUTE_EDITOR_SUFFIX = "AttributeEditor";//NOI18N
     private HashMap<String, Converter> defaultConverter = new HashMap<String, Converter>();
     private User user;
     private ComponentWrapper componentWrapper = null;
 
     private CidsObjectEditorFactory() {
         //Die Klassennamen werden über class.getName() erzeugt. So checkt der Compiler ob sie korrekt referenziert wurden
         defaultConverter.put(com.vividsolutions.jts.geom.Geometry.class.getName(), new GeometryToStringConverter());
         defaultConverter.put(java.sql.Date.class.getName(), new SqlDateToStringConverter());
         defaultConverter.put(java.lang.Boolean.class.getName(), new BooleanToStringConverter());
 
 
         try {
             final Class<?> wrapperClass = BlacklistClassloading.forName("de.cismet.cids.custom.objecteditors.EditorWrapper");//NOI18N
             componentWrapper = (ComponentWrapper) wrapperClass.newInstance();
         } catch (Exception skip) {
             log.debug("Error while loading the EditorWrapper", skip);//NOI18N
         }
     }
 
     public static CidsObjectEditorFactory getInstance() {
         if (editorFactory == null) {
             editorFactory = new CidsObjectEditorFactory();
             editorFactory.user = SessionManager.getSession().getUser();
         }
         return editorFactory;
     }
 
     public ComponentWrapper getComponentWrapper() {
         return componentWrapper;
     }
 
     private GridBagConstraints getCommonConstraints() {
         GridBagConstraints gbc = new GridBagConstraints();
         gbc.fill = GridBagConstraints.HORIZONTAL;
         gbc.anchor = GridBagConstraints.NORTHWEST;
         return gbc;
     }
 
     private void modifyForLabel(GridBagConstraints gbc) {
         // gbc.weightx = 0.3;
         gbc.insets = new java.awt.Insets(4, 5, 3, 0);
         gbc.gridx = 0;
 
 
 
     }
 
     private void modifyForEditor(GridBagConstraints gbc) {
         gbc.weightx = 0.7;
         gbc.insets = new java.awt.Insets(0, 5, 3, 0);
         gbc.gridx = 2;
         gbc.gridwidth = 2;
     }
 
     public static MetaClass getMetaClass(String domain, int classid) {
         return ClassCacheMultiple.getMetaClass(domain, classid);
     }
 
     public JComponent getEditor(MetaObject MetaObject) {
         //Hier kann man noch mit Caching arbeiten
         JComponent editorComponent = getObjectEditor(MetaObject.getMetaClass());
         if (editorComponent == null) {
             editorComponent = (JComponent) getDefaultEditor(MetaObject.getMetaClass());
         }
         final JComponent finalEditorComponent = editorComponent;
         if (editorComponent instanceof CidsBeanStore) {
             final CidsBean bean = MetaObject.getBean();
 //            final Runnable setCidsBeanRunnable = new Runnable() {
 //
 //                @Override
 //                public void run() {
                     ((CidsBeanStore) finalEditorComponent).setCidsBean(bean);
                     if (finalEditorComponent instanceof AutoBindableCidsEditor) {
                         bindCidsEditor((AutoBindableCidsEditor) finalEditorComponent);
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
             if (componentWrapper != null && !(editorComponent instanceof DoNotWrap)) {
                 return (JComponent) componentWrapper.wrapComponent((JComponent) editorComponent);
             } else {
                 return editorComponent;
             }
         } else {
             //log
             return null;
         }
 
     }
 
     private JComponent getSimpleAttributeEditor(MetaClass metaClass, MemberAttributeInfo mai) {
         JComponent ret = null;
 
 
         if (ret == null) {
             String attributeClassname = mai.getJavaclassname();
 
             if (attributeClassname.equals(java.lang.String.class.getName())) {
                 ret = new DefaultBindableJTextField();
                 ((DefaultBindableJTextField) ret).setConverter(defaultConverter.get(attributeClassname));
             } else if (attributeClassname.equals(java.sql.Date.class.getName())) {
                 ret = new DefaultBindableDateChooser();
             } else if (attributeClassname.equals(java.sql.Timestamp.class.getName())) {
                 ret = new DefaultBindableTimestampChooser();
             } else if (attributeClassname.equals(java.lang.Boolean.class.getName())) {
                 ret = new DefaultBindableJCheckBox();
             } else if (mai.isForeignKey() && mai.isSubstitute()) {
                 MetaClass foreignClass = getMetaClass(metaClass.getDomain(), mai.getForeignKeyClassId());
                 if (foreignClass.getClassAttribute("reasonable_few") != null) {//NOI18N
                     ret = new DefaultBindableReferenceCombo(foreignClass);
                 }
             } else {
                 log.debug("no DefaultEditor for " + attributeClassname + " found. set to textbox ");//NOI18N
                 ret = new DefaultBindableJTextField();
                 ((DefaultBindableJTextField) ret).setConverter(defaultConverter.get(attributeClassname));
 
             }
         }
 
 //        if (ret!=null){
 //            ret.setOpaque(false);
 //        }
 
         return ret;
 
     }
 
     private final String getObjectEditorClassnameByConvention(MetaClass metaClass) {
         return getClassnameByConvention(metaClass, EDITOR_PREFIX, EDITOR_SUFFIX);
     }
 
     private final String getClassnameByConvention(MetaClass metaClass, String prefix, String suffix) {
         final String domain = metaClass.getDomain().toLowerCase();
         String className = metaClass.getTableName().toLowerCase();
         className = className.substring(0, 1).toUpperCase() + className.substring(1);
         className = prefix + domain + "." + className + suffix;//NOI18N
         return className;
     }
 
     private final JComponent getObjectEditor(final MetaClass metaClass) {
         final String overrideObjectEditorClassName = System.getProperty(metaClass.getDomain() + "." + metaClass.getTableName().toLowerCase() + ".objecteditor");//NOI18N
         final String editorClassName = overrideObjectEditorClassName == null ? getObjectEditorClassnameByConvention(metaClass) : overrideObjectEditorClassName;
         try {
             final Class<?> editorClass = BlacklistClassloading.forName(editorClassName);
             if (editorClass != null) {
                 final FinalReference<JComponent> result = new FinalReference<JComponent>();
 //                final Runnable createObjectEditorRunnable = new Runnable() {
 //
 //                    @Override
 //                    public void run() {
                         try {
                             final JComponent ed = (JComponent) editorClass.newInstance();
                             if (ed instanceof MetaClassStore) {
                                 ((MetaClassStore) ed).setMetaClass(metaClass);
                             }
                             result.setObject(ed);
                         } catch (Throwable t) {
                             throw new RuntimeException(t);
                         }
 //                    }
 //                };
 //                if (EventQueue.isDispatchThread()) {
 //                    createObjectEditorRunnable.run();
 //                } else {
 //                    EventQueue.invokeAndWait(createObjectEditorRunnable);
 //                }
                 return result.getObject();
             }
         } catch (Exception e) {
             log.error("Error while creating the editor class " + editorClassName, e);//NOI18N
         }
 //        if (ret != null) {
 //            ret.setOpaque(false);
 //        }
         return null;
     }
 
     private AutoBindableCidsEditor getDefaultEditor(final MetaClass metaClass) {
         final Vector<MemberAttributeInfo> mais = new Vector<MemberAttributeInfo>(metaClass.getMemberAttributeInfos().values());
         final FinalReference<AutoBindableCidsEditor> result = new FinalReference<AutoBindableCidsEditor>();
 //        final Runnable createDefaultEditorRunnable = new Runnable() {
 //
 //            @Override
 //            public void run() {
 
                 DefaultCidsEditor cidsEditor = new DefaultCidsEditor();
                 result.setObject(cidsEditor);
                 GridBagLayout gbl = new GridBagLayout();
                 GridBagConstraints gbc = null;
                 cidsEditor.setLayout(gbl);
                 int row = 0;
 
                 for (MemberAttributeInfo mai : mais) {
                     if (mai.isVisible()) {
                         //Description
                         JLabel lblDescription = new JLabel();
                         lblDescription.setText(mai.getName());
                         lblDescription.setHorizontalAlignment(JLabel.RIGHT);
                         gbc = getCommonConstraints();
                         modifyForLabel(gbc);
                         gbc.gridy = row;
                         cidsEditor.add(lblDescription, gbc);
 
 
 
                         //Editor
                         JComponent cmpEditor = null;
 
                         if (mai.isForeignKey()) {
                             int foreignKey = mai.getForeignKeyClassId();
                             String domain = metaClass.getDomain();
                             MetaClass foreignClass = getMetaClass(domain, foreignKey);
 
                             if (mai.isArray()) {
                                 //--------------------------------------------------
                                 //Arrays
                                 //--------------------------------------------------
                                 MetaClass detailClass = null;
 
                                 //Detaileditorcomponent
                                 Vector<MemberAttributeInfo> arrayAttrs = new Vector<MemberAttributeInfo>(foreignClass.getMemberAttributeInfos().values());
                                 for (MemberAttributeInfo arrayMai : arrayAttrs) {
                                     if (arrayMai.isForeignKey()) {
                                         int detailKey = arrayMai.getForeignKeyClassId();
                                         detailClass = getMetaClass(domain, detailKey);
                                         cmpEditor = (JComponent) getObjectEditor(detailClass);
                                         if (cmpEditor == null) {
                                             cmpEditor = (JComponent) getDefaultEditor(detailClass);
                                         }
 
                                         if (cmpEditor instanceof BindingInformationProvider) {
                                             BindingInformationProvider ed = (BindingInformationProvider) cmpEditor;
                                             Set<String> fields = ed.getAllControls().keySet();
                                             for (String key : fields) {
                                                 String newKey = mai.getFieldName().toLowerCase() + "[]." + key;//NOI18N
                                                 cidsEditor.addControlInformation(newKey, ed.getAllControls().get(key));
                                             }
 
                                         } else if (cmpEditor instanceof Bindable) {
                                             //TODO
                                             throw new UnsupportedOperationException();
                                         }
 
                                         break;
                                     }
 
                                 }
 
                                 //Masterliste
                                 cidsEditor.remove(lblDescription);
                                 gbc = getCommonConstraints();
                                 modifyForLabel(gbc);
                                 gbc.insets = new java.awt.Insets(4, 25, 3, 0);
                                 gbc.gridy = row++;
                                 gbc.fill = java.awt.GridBagConstraints.BOTH;
                                 String field = mai.getFieldName().toLowerCase();
 
                                 BindableJList lstArrayMaster = new BindableJList();
 
                                 // <editor-fold defaultstate="collapsed" desc="CellRenderer">
                                 final DefaultListCellRenderer dlcr = new DefaultListCellRenderer();
                                 lstArrayMaster.setCellRenderer(new ListCellRenderer() {
 
                                     public Component getListCellRendererComponent(
                                             JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                                         JLabel l = (JLabel) dlcr.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                                         if (l.getText() == null || l.getText().trim().equals("") || l.getText().equals("null")) { //TODO Der check auf den String "null" muss wieder raus//NOI18N
                                             CidsBean cb = (CidsBean) value;
                                             if (cb.getMetaObject().getStatus() == MetaObject.NEW) {
                                                 l.setText(org.openide.util.NbBundle.getMessage(CidsObjectEditorFactory.class, "CidsObjectEditorFactory.getDefaultEditor().getListCellRendererComponent.newElementText"));//NOI18N
                                                 if (isSelected) {
                                                     l.setBackground(Color.GREEN);
                                                 }
 
                                             } else {
                                                 l.setText(cb.getMetaObject().getMetaClass().toString() + " " + cb.getProperty(cb.getMetaObject().getMetaClass().getPrimaryKey().toLowerCase()));//NOI18N
                                             }
 
                                         }
                                         return l;
                                     }
                                 });
 // </editor-fold>
 
                                 ArrayTitleAndControls arrayTitleAndControls = new ArrayTitleAndControls(lblDescription.getText(), detailClass, field, lstArrayMaster);
                                 cidsEditor.add(arrayTitleAndControls, gbc);
 
                                 gbc = getCommonConstraints();
                                 modifyForLabel(gbc);
                                 gbc.insets = new java.awt.Insets(4, 25, 0, 0);
                                 gbc.gridy = row;
                                 gbc.fill = java.awt.GridBagConstraints.BOTH;
                                 cidsEditor.addControlInformation(field + "[]", lstArrayMaster);//NOI18N
                                 cidsEditor.add(lstArrayMaster, gbc);
 
                                 gbc = getCommonConstraints();
                                 modifyForEditor(gbc);
                                 gbc.gridy = row;
                                 cmpEditor.putClientProperty(PARENT_CIDS_EDITOR, cidsEditor);
                                 cidsEditor.add(cmpEditor, gbc);
 
 
                             } else if (mai.isForeignKey()) {
                                 //--------------------------------------------------
                                 //Normale Unterobjekte
                                 //--------------------------------------------------
 
                                 //Entfernen Button
                                 gbc = getCommonConstraints();
                                 modifyForLabel(gbc);
                                 gbc.fill = GridBagConstraints.NONE;
                                 gbc.insets = new java.awt.Insets(0, 0, 0, 3);
                                 gbc.gridx = 3;
                                 gbc.gridy = row;
                                 JButton cmdRemove = new JButton();
                                 cmdRemove.setBorderPainted(false);
                                 cmdRemove.setMinimumSize(new Dimension(12, 12));
                                 cmdRemove.setPreferredSize(new Dimension(12, 12));
 
                                 cmdRemove.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cids/editors/edit_remove_mini.png")));//NOI18N
                                 cmdRemove.setVisible(false);
                                 cidsEditor.add(cmdRemove, gbc);
 
 
                                 //Erstellen Button
                                 gbc = getCommonConstraints();
                                 modifyForLabel(gbc);
                                 gbc.insets = new java.awt.Insets(0, 0, 0, 3);
                                 gbc.fill = GridBagConstraints.NONE;
                                 gbc.gridx = 3;
                                 gbc.gridy = row;
 
                                 JButton cmdAdd = new JButton();
                                 cmdAdd.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cids/editors/edit_add_mini.png")));//NOI18N
                                 cmdAdd.setBorderPainted(false);
                                 cmdAdd.setMinimumSize(new Dimension(12, 12));
                                 cmdAdd.setPreferredSize(new Dimension(12, 12));
                                 cmdAdd.setVisible(false);
                                 cidsEditor.add(cmdAdd, gbc);
 
                                 //Editor
 
 
                                 cmpEditor = getCustomAttributeEditor(metaClass, mai);
 
                                 if (cmpEditor == null) {
                                     cmpEditor = (JComponent) getObjectEditor(foreignClass);
                                 }
 
                                 if (cmpEditor == null && mai.isSubstitute()) {
                                     cmpEditor = getSimpleAttributeEditor(metaClass, mai);
                                 }
 
                                 //Sicherheithalber ....
                                 if (cmpEditor == null) {
                                     cmpEditor = (JComponent) getDefaultEditor(foreignClass);
                                 }
 
 
                                 //bindable geht vor
                                 if (cmpEditor instanceof Bindable) {
                                     cidsEditor.addControlInformation(mai.getFieldName().toLowerCase(), (Bindable) cmpEditor);
 
                                 } else if (cmpEditor instanceof BindingInformationProvider) {
                                     BindingInformationProvider ed = (BindingInformationProvider) cmpEditor;
                                     Set<String> fields = ed.getAllControls().keySet();
                                     for (String key : fields) {
                                         String newKey = mai.getFieldName().toLowerCase() + "." + key;//NOI18N
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
                                     log.warn("Editor was null. " + metaClass.getTableName() + "." + mai.getFieldName());//NOI18N
                                 }
 
                             }
 
                         } else {
                             // Die Editorkomponente über die Metainformations checken
 
                             //--------------------------------------------------
                             //Einfache Attribute
                             //--------------------------------------------------
 
                             cmpEditor = getCustomAttributeEditor(metaClass, mai);
 
                             if (cmpEditor == null) {
                                 cmpEditor = getSimpleAttributeEditor(metaClass, mai);
                             }
 
                             log.debug("ATTRIBUTE_CLASS_NAME:" + mai.getJavaclassname() + " --> " + cmpEditor.getClass().toString() );//NOI18N
                             cidsEditor.addControlInformation(mai.getFieldName().toLowerCase(), (Bindable) cmpEditor);
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
         return result.getObject();
     }
 
     private final String getAttributeEditorClassnameByConvention(MetaClass metaClass, MemberAttributeInfo mai) {
         final String domain = metaClass.getDomain().toLowerCase();
         String fieldname = mai.getFieldName().toLowerCase();
         String className = metaClass.getTableName().toLowerCase();
         fieldname = fieldname.substring(0, 1).toUpperCase() + fieldname.substring(1);
         className = EDITOR_PREFIX + domain + "." + className + "." + fieldname + ATTRIBUTE_EDITOR_SUFFIX;//NOI18N
         String overrideAttributeEditorClassName = System.getProperty(domain + "." + metaClass.getTableName().toLowerCase() + ".attributeeditor");//NOI18N
         if (overrideAttributeEditorClassName != null) {
             className = overrideAttributeEditorClassName;
         }
         return className;
     }
 
     private JComponent getCustomAttributeEditor(MetaClass metaClass, MemberAttributeInfo mai) {
         // TODO
         //Hier müssen auch noch die Einstellungen inder DB (ComplexEditor, Editor) berücksichtigt werden
 
         //MetaClass contains the MemberAttributeInfo
         final String className = getAttributeEditorClassnameByConvention(metaClass, mai);
         
         final FinalReference<JComponent> result = new FinalReference<JComponent>();
         try {
             Class<?> attrEditorClass = BlacklistClassloading.forName(className);
             
             if (attrEditorClass==null&& mai.getEditor()!=null) {
                 attrEditorClass=BlacklistClassloading.forName(mai.getEditor());
             }
 
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
                             final Bindable editor = (Bindable) attrEditorClass.newInstance();
                             if (foreignClass != null) {
                                 ((MetaClassStore) editor).setMetaClass(foreignClass);
                             }
                             result.setObject((JComponent) editor);
                         } catch (Throwable t) {
                             log.error("getCustomAttributeEditor of "+metaClass.getTableName()+"."+mai.getFieldName()+" returns an error",t);//NOI18N
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
             log.error("Error when creating a SimpleAttributeEditor", e);//NOI18N
         }
         JComponent ret= result.getObject();
         return ret;
     }
 
     private final void bindCidsEditor(final AutoBindableCidsEditor ed) {
         BindingGroup bg = ed.getBindingGroup();
         MetaObject MetaObject = ed.getCidsBean().getMetaObject();
         ObjectAttribute[] allAttrs = MetaObject.getAttribs();
         Binding binding = null;
         Set<String> keys = ed.getAllControls().keySet();
         HashMap<String, JList> arraylists = new HashMap<String, JList>();
 
 
         HashSet complexEditors = new HashSet();
 
         //Prefetching all the JLists
         for (final String key : keys) {
             if (key.endsWith("[]")) {//NOI18N
                 final JList lstList = (JList) ed.getControlByName(key);
                 arraylists.put(key, lstList);
             }
         }
 
 
 
 
 
         for (final String key : keys) {
             if (key.endsWith("[]")) {//NOI18N
                 //--------------------------------------------------
                 //Array
                 //--------------------------------------------------
 
 
                 //--------------------------------------------------
                 //Zuerst die Master JLists
                 //--------------------------------------------------
                 String keyWithoutBrackets = key.substring(0, key.length() - 2);
                 Object bindingSource = null;
                 ELProperty elProperty = null;
 
                 final JList lstList = arraylists.get(key);
 
                 if (keyWithoutBrackets.contains("[]")) {//NOI18N
                     //[] mehr als einmal vorhanden
 
                     String parentListIdentifier = keyWithoutBrackets.substring(0, keyWithoutBrackets.lastIndexOf("[]") + 2);//NOI18N
                     bindingSource = arraylists.get(parentListIdentifier);
                     lstList.putClientProperty(SOURCE_LIST, bindingSource);
                     String subKeyWithoutBrackets = key.substring(key.indexOf(parentListIdentifier) + ".".length() + parentListIdentifier.length(), key.length() - 2);//NOI18N
                     elProperty = ELProperty.create("${selectedElement." + subKeyWithoutBrackets + "}");//NOI18N
                 } else {
                     bindingSource = ed;
                     lstList.putClientProperty(CIDS_BEAN, ed.getCidsBean());
                     elProperty = ELProperty.create("${cidsBean." + keyWithoutBrackets + "}");//NOI18N
                 }
 
 
                 org.jdesktop.swingbinding.JListBinding jListBinding = org.jdesktop.swingbinding.SwingBindings.createJListBinding(AutoBinding.UpdateStrategy.READ_WRITE, bindingSource, elProperty, lstList);
 
                 // <editor-fold defaultstate="collapsed" desc="füge PC Listener hinzu um Änderungen an den variablen direkt in der Liste anzuzeigen">
                 try {
                     ObservableList observableList = (ObservableList) ed.getCidsBean().getProperty(keyWithoutBrackets);
                     for (Object o : observableList) {
                         ((CidsBean) o).addPropertyChangeListener(new PropertyChangeListener() {
 
                             public void propertyChange(PropertyChangeEvent evt) {
                                 lstList.repaint();
                             }
                         });
                     }
 
                     //und jetzt noch für die zukuenftigen
                     observableList.addObservableListListener(new ObservableListListenerAdapter() {
 
                         @Override
                         public void listElementsAdded(ObservableList list, int index, int length) {
                             for (int i = index; i < index + length; ++i) {
                                 ((CidsBean) list.get(i)).addPropertyChangeListener(new PropertyChangeListener() {
 
                                     public void propertyChange(PropertyChangeEvent evt) {
                                         lstList.repaint();
                                     }
                                 });
                             }
 
                         }
                     });
 
                     // </editor-fold>
                 } catch (Exception e) {
                     log.debug("No observableList update for Array in Array in ...", e);//NOI18N
                 }
 
                 bg.addBinding(jListBinding);
 
             } else {
                 //--------------------------------------------------
                 //keine Arrays
                 //--------------------------------------------------
                 JComponent jc = (JComponent) ed.getControlByName(key); //in jc steckt die Editorkomponente
                 Bindable bjc = (Bindable) jc;
 
                 BindingInformationProvider parentCidsEditor = (BindingInformationProvider) jc.getClientProperty(PARENT_CIDS_EDITOR);
 
                 binding = Bindings.createAutoBinding(AutoBinding.UpdateStrategy.READ_WRITE, jc, ELProperty.create("${text==null}"), jc, BeanProperty.create("background"));//NOI18N
                 binding.setConverter(nullToBackgroundColorConverter);
                 bg.addBinding(binding);
 
 
                 if (key.contains("[]")) {//NOI18N
                     //--------------------------------------------------
                     //Detailattribute
                     //--------------------------------------------------
                     int whereSubKeyStarts = key.lastIndexOf("[]") + 3;//NOI18N
                     String arrayFieldWithBrackets = key.substring(0, whereSubKeyStarts - 1);
                     final JList list = (JList) ed.getControlByName(arrayFieldWithBrackets);
                     String subkey = key.substring(whereSubKeyStarts);
                     String exp = "selectedElement." + subkey;//NOI18N
                     binding = Bindings.createAutoBinding(AutoBinding.UpdateStrategy.READ_WRITE, list, ELProperty.create("${" + exp + "}"), jc, BeanProperty.create(bjc.getBindingProperty()));//NOI18N
                     binding.setSourceUnreadableValue(null);
                     Converter c = bjc.getConverter();
                     if (c != null) {
                         binding.setConverter(c);
                     }
 
                     bg.addBinding(binding);
 
                     //Direktes Detailattribut (oder ein Subobjekt)
                     if (subkey.contains(".")) {//NOI18N
                         String[] sa = subkey.split("\\.");//NOI18N
                         final String object = subkey.substring(0, subkey.lastIndexOf("."));//NOI18N
                         //Check ob das Teilobjekt nicht auf null gesetzt ist
                         String expression = "selectedElement." + object;//NOI18N
                         addDisablingAndNullCheckerBindings(bg, expression, list, jc);
                         addAddRemoveControlVisibilityBinding(bg, (JComponent) parentCidsEditor, list, object, true);
                     }
 
                 } else {
                     //--------------------------------------------------
                     //nicht Teil eines Arrays
                     //--------------------------------------------------
 
                     binding = Bindings.createAutoBinding(AutoBinding.UpdateStrategy.READ_WRITE, ed, ELProperty.create("${cidsBean." + key + "}"), jc, BeanProperty.create(bjc.getBindingProperty()));//NOI18N
 
                     Converter c = bjc.getConverter();
                     if (c != null) {
                         binding.setConverter(c);
                     }
 
                     bg.addBinding(binding);
 
                     if (key.contains(".")) {//NOI18N
                         // Subobjekt
                         String[] sa = key.split("\\.");//NOI18N
                         String attribute = sa[sa.length - 1];
                         final String object = key.substring(0, key.lastIndexOf("."));//NOI18N
 
                         //Check ob das Teilobjekt nicht auf null gesetzt ist
                         String expression = "cidsBean." + object;//NOI18N
                         addDisablingAndNullCheckerBindings(bg, expression, ed, jc);
                         addAddRemoveControlVisibilityBinding(bg, (JComponent) parentCidsEditor, (JComponent) ed, object, false);
 
                     } else if (bjc instanceof CidsBeanStore) {
                         //Subobjekt das nur durch ein Bindable editiert wird
                         String expression = "cidsBean." + key;//NOI18N
                         addDisablingAndNullCheckerBindings(bg, expression, ed, jc);
                         addAddRemoveControlVisibilityBinding(bg, jc, (JComponent) ed, key, false);
                     }
                 }
                 //hier wird sichergestellt dass nur einmal für jeden komplexen editor die +/- buttons hinzugefuegt werden
                 if (parentCidsEditor != null) {
                     complexEditors.add(parentCidsEditor);
                 }
 
             }
         }
         bg.bind();
     }
 
     private final void addDisablingAndNullCheckerBindings(BindingGroup bg, String expression, Object sourceObject, JComponent component) {
         Binding binding = Bindings.createAutoBinding(AutoBinding.UpdateStrategy.READ_WRITE, sourceObject, ELProperty.create("${" + expression + "!=null}"), component, BeanProperty.create("enabled"));//NOI18N
         bg.addBinding(binding);
         binding = Bindings.createAutoBinding(AutoBinding.UpdateStrategy.READ_WRITE, sourceObject, ELProperty.create("${" + expression + "==null}"), component, BeanProperty.create("background"));//NOI18N
         binding.setConverter(nullToBackgroundColorConverter);
         //binding.setSourceNullValue(NO_VALUE);//Geht nicht weil ed.cidsBean nicht null ist
         bg.addBinding(binding);
     }
 
     private void addAddRemoveControlVisibilityBinding(final BindingGroup bg,
             final JComponent buttonContainer,
             final JComponent bindingSourceObject,
             final String attributeName,
             final boolean detailObjectOfAnArray) {
         Binding binding = null;
         JButton cmdAdd = (JButton) buttonContainer.getClientProperty("cmdAddObject");//NOI18N
         JButton cmdRemove = (JButton) buttonContainer.getClientProperty("cmdRemoveObject");//NOI18N
         String objectExpression;
         if (detailObjectOfAnArray) {
             objectExpression = "selectedElement." + attributeName;//NOI18N
         } else {
             objectExpression = "cidsBean." + attributeName;//NOI18N
         }
         if (cmdAdd != null) {
             binding = Bindings.createAutoBinding(AutoBinding.UpdateStrategy.READ_WRITE, bindingSourceObject, ELProperty.create("${" + objectExpression + "==null}"), cmdAdd, BeanProperty.create("visible"));//NOI18N
             bg.addBinding(binding);
             cmdAdd.addActionListener(new ActionListener() {
 
                 public void actionPerformed(ActionEvent e) {
                     CidsBean actionBean;
                     if (detailObjectOfAnArray) {
                         actionBean = (CidsBean) ((JList) bindingSourceObject).getSelectedValue();
                     } else {
                         actionBean = ((CidsBeanStore) bindingSourceObject).getCidsBean();
                     }
                     ObjectAttribute oa = actionBean.getMetaObject().getAttributeByFieldName(attributeName);
                     MetaClass mc = getMetaClass(actionBean.getMetaObject().getDomain(), oa.getMai().getForeignKeyClassId());
                     CidsBean newOne = mc.getEmptyInstance().getBean();
                     try {
                         actionBean.setProperty(attributeName, newOne);
                     } catch (Exception ex) {
                         ex.printStackTrace();
                     }
 
                 }
             });
 
         }
         if (cmdRemove != null) {
             binding = Bindings.createAutoBinding(AutoBinding.UpdateStrategy.READ_WRITE, bindingSourceObject, ELProperty.create("${" + objectExpression + "!=null}"), cmdRemove, BeanProperty.create("visible"));//NOI18N
             bg.addBinding(binding);
             cmdRemove.addActionListener(new ActionListener() {
 
                 public void actionPerformed(ActionEvent e) {
                     CidsBean actionBean;
                     if (detailObjectOfAnArray) {
                         actionBean = (CidsBean) ((JList) bindingSourceObject).getSelectedValue();
                     } else {
                         actionBean = ((CidsBeanStore) bindingSourceObject).getCidsBean();
                     }
 
                     try {
                         ((CidsBean) actionBean.getProperty(attributeName)).delete();
                         // anderer option nur null setzen
                         //actionBean.setProperty(attributeName, null);
                     } catch (Exception ex) {
                         ex.printStackTrace();
                     }
 
                 }
             });
         }
 
 
     }
 
     public static void main(String[] args) throws Exception {
         EventQueue.invokeLater(new Runnable() {
 
             public void run() {
                 try {
                     final JPanel panCommand = new JPanel();
 
                     final String domain = "WUNDA_DEMO";//NOI18N
                     final int CLASSID = 374;
                     final int OBJECTID = 1;
 //                    final int CLASSID = 47; //Bauvisualisierung
 //                    final int OBJECTID = 1;
 
 //                    final int CLASSID = 45; //POI
 //                    final int OBJECTID = 7; //botanischer Garten
 
 
                     Log4JQuickConfig.configure4LumbermillOnLocalhost();
                     ConnectionSession session = null;
                     ConnectionProxy proxy = null;
                     ConnectionInfo connectionInfo = new ConnectionInfo();
                     connectionInfo.setCallserverURL("rmi://localhost/callServer");//NOI18N
                     connectionInfo.setPassword("demo");//NOI18N
                     connectionInfo.setUsergroup("Demo");//NOI18N
                     connectionInfo.setUserDomain("WUNDA_DEMO");//NOI18N
                     connectionInfo.setUsergroupDomain("WUNDA_DEMO");//NOI18N
                     connectionInfo.setUsername("demo");//NOI18N
 
                     Connection connection = ConnectionFactory.getFactory().createConnection("Sirius.navigator.connection.RMIConnection", connectionInfo.getCallserverURL());//NOI18N
 
                     //connection.g
 
                     session = ConnectionFactory.getFactory().createSession(connection, connectionInfo, true);
                     proxy = ConnectionFactory.getFactory().createProxy("Sirius.navigator.connection.proxy.DefaultConnectionProxyHandler", session);//NOI18N
                     SessionManager.init(proxy);
 
                     MetaObject MetaObject = SessionManager.getConnection().getMetaObject(SessionManager.getSession().getUser(), OBJECTID, CLASSID, domain);//meta.getMetaObject(u, 1, AAPERSON_CLASSID, domain);
 
                     log.fatal(MetaObject.getDebugString());
 
                     JFrame tester = new JFrame(org.openide.util.NbBundle.getMessage(CidsObjectEditorFactory.class, "CidsObjectEditorFactory.tester.title"));//NOI18N
                     Container cp = tester.getContentPane();
                     cp.setLayout(new BorderLayout());
                     final JComponent ed = CidsObjectEditorFactory.getInstance().getEditor(MetaObject);
                     cp.add(ed);
                     tester.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
                     JButton cmdPersist = new JButton(org.openide.util.NbBundle.getMessage(CidsObjectEditorFactory.class, "CidsObjectEditorFactory.cmdPersist.text"));//NOI18N
                     cmdPersist.addActionListener(new ActionListener() {
 
                         public void actionPerformed(ActionEvent e) {
                             try {
                                 if (ed instanceof WrappedComponent) {
 
                                     ((CidsBeanStore) ((WrappedComponent) ed).getOriginalComponent()).getCidsBean().persist();
                                 } else {
                                     ((CidsBeanStore) ed).getCidsBean().persist();
                                 }
 
                             } catch (Exception ex) {
                                 ex.printStackTrace();
                             }
 
                         }
                     });
                    JButton cmdLog = new JButton(org.openide.util.NbBundle.getMessage(CidsObjectEditorFactory.class, "CidsObjectEditorFactory.cmdLog.text"));//NOI18N
                     cmdLog.addActionListener(new ActionListener() {
 
                         public void actionPerformed(ActionEvent e) {
                             CidsBean cb = null;
                             if (ed instanceof WrappedComponent) {
                                 cb = ((CidsBeanStore) ((WrappedComponent) ed).getOriginalComponent()).getCidsBean();
                             } else {
                                 cb = ((CidsBeanStore) ed).getCidsBean();
                             }
                             if (cb != null) {
                                 log.fatal(cb.getMOString());
                             }
                         }
                     });
                    JButton cmdReload = new JButton(org.openide.util.NbBundle.getMessage(CidsObjectEditorFactory.class, "CidsObjectEditorFactory.cmdReload.text"));//NOI18N
                     cmdReload.addActionListener(new ActionListener() {
 
                         public void actionPerformed(ActionEvent e) {
                             try {
                                 AutoBindableCidsEditor abce = null;
                                 if (ed instanceof WrappedComponent) {
                                     abce = ((AutoBindableCidsEditor) ((WrappedComponent) ed).getOriginalComponent());
                                 } else {
                                     abce = ((AutoBindableCidsEditor) ed);
                                 }
                                 if (abce != null) {
                                     abce.setCidsBean(SessionManager.getConnection().getMetaObject(SessionManager.getSession().getUser(), OBJECTID, CLASSID, domain).getBean());
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
 class ObservableListListenerAdapter implements ObservableListListener {
 
     public void listElementPropertyChanged(ObservableList list, int index) {
     }
 
     public void listElementReplaced(ObservableList list, int index, Object oldElement) {
     }
 
     public void listElementsAdded(ObservableList list, int index, int length) {
     }
 
     public void listElementsRemoved(ObservableList list, int index, List oldElements) {
     }
 }
 
 class BindableJList extends JList implements Bindable {
 
     public BindableJList() {
     }
 
     public BindableJList(Vector<?> listData) {
         super(listData);
     }
 
     public BindableJList(Object[] listData) {
         super(listData);
     }
 
     public BindableJList(ListModel dataModel) {
         super(dataModel);
     }
 
     public String getBindingProperty() {
         return null;
     }
 
     public Converter getConverter() {
         return null;
     }
 
     public Validator getValidator() {
         return null;
     }
 }
 
 
 
