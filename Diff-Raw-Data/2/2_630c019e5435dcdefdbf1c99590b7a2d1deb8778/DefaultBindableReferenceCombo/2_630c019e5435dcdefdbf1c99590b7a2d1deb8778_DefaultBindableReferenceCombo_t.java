 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.editors;
 
 import Sirius.navigator.tools.CacheException;
 import Sirius.navigator.tools.MetaObjectCache;
 import Sirius.navigator.tools.MetaObjectChangeEvent;
 import Sirius.navigator.tools.MetaObjectChangeListener;
 import Sirius.navigator.tools.MetaObjectChangeSupport;
 
 import Sirius.server.localserver.attribute.ClassAttribute;
 import Sirius.server.middleware.types.MetaClass;
 import Sirius.server.middleware.types.MetaClassStore;
 import Sirius.server.middleware.types.MetaObject;
 
 import org.apache.log4j.Logger;
 
 import org.jdesktop.beansbinding.Converter;
 import org.jdesktop.beansbinding.Validator;
 
 import org.openide.util.WeakListeners;
 
 import java.awt.Component;
 
 import java.io.Serializable;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.DefaultListCellRenderer;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.SwingWorker;
 
 import de.cismet.cids.dynamics.CidsBean;
 
 import de.cismet.tools.CismetThreadPool;
 
 /**
  * DOCUMENT ME!
  *
  * @author   thorsten
  * @version  $Revision$, $Date$
  */
 public class DefaultBindableReferenceCombo extends JComboBox implements Bindable, MetaClassStore, Serializable {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final Logger LOG = Logger.getLogger(DefaultBindableReferenceCombo.class);
 
     protected static final Comparator<CidsBean> BEAN_TOSTRING_COMPARATOR = new BeanToStringComparator();
 
     //~ Instance fields --------------------------------------------------------
 
     protected CidsBean cidsBean;
 
     private final transient MetaObjectChangeListener mocL;
     private MetaClass metaClass;
     private boolean fakeModel;
     private boolean nullable;
     private boolean onlyUsed;
     private Comparator<CidsBean> comparator;
     private String nullValueRepresentation;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new DefaultBindableReferenceCombo object.
      */
     public DefaultBindableReferenceCombo() {
         this(null, false, false, BEAN_TOSTRING_COMPARATOR);
     }
 
     /**
      * Creates a new DefaultBindableReferenceCombo object.
      *
      * @param  nullable  DOCUMENT ME!
      */
     public DefaultBindableReferenceCombo(final boolean nullable) {
         this(null, nullable, false, BEAN_TOSTRING_COMPARATOR);
     }
 
     /**
      * Creates a new DefaultBindableReferenceCombo object.
      *
      * @param  comparator  DOCUMENT ME!
      */
     public DefaultBindableReferenceCombo(final Comparator<CidsBean> comparator) {
         this(null, false, false, comparator);
     }
 
     /**
      * Creates a new DefaultBindableReferenceCombo object.
      *
      * @param  mc  DOCUMENT ME!
      */
     public DefaultBindableReferenceCombo(final MetaClass mc) {
         this(mc, false, false, BEAN_TOSTRING_COMPARATOR);
     }
 
     /**
      * Creates a new DefaultBindableReferenceCombo object.
      *
      * @param  mc        DOCUMENT ME!
      * @param  nullable  DOCUMENT ME!
      * @param  onlyUsed  DOCUMENT ME!
      */
     public DefaultBindableReferenceCombo(final MetaClass mc, final boolean nullable, final boolean onlyUsed) {
         this(mc, nullable, onlyUsed, BEAN_TOSTRING_COMPARATOR);
     }
 
     /**
      * Creates a new DefaultBindableReferenceCombo object.
      *
      * @param  mc          DOCUMENT ME!
      * @param  nullable    DOCUMENT ME!
      * @param  onlyUsed    DOCUMENT ME!
      * @param  comparator  DOCUMENT ME!
      */
     public DefaultBindableReferenceCombo(final MetaClass mc,
             final boolean nullable,
             final boolean onlyUsed,
             final Comparator<CidsBean> comparator) {
         final String[] s = new String[] { null };
         setModel(new DefaultComboBoxModel(s));
 
         this.nullable = nullable;
         this.onlyUsed = onlyUsed;
         this.comparator = comparator;
         this.nullValueRepresentation = " ";
         this.mocL = new MetaObjectChangeListenerImpl();
 
         this.setRenderer(new DefaultBindableReferenceComboRenderer());
 
         init(mc, false);
 
         final MetaObjectChangeSupport mocSupport = MetaObjectChangeSupport.getDefault();
         mocSupport.addMetaObjectChangeListener(WeakListeners.create(MetaObjectChangeListener.class, mocL, mocSupport));
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @param  mc           DOCUMENT ME!
      * @param  forceReload  DOCUMENT ME!
      */
     private void init(final MetaClass mc, final boolean forceReload) {
        if (!isFakeModel() && (mc != null)) {
             CismetThreadPool.execute(new SwingWorker<DefaultComboBoxModel, Void>() {
 
                     @Override
                     protected DefaultComboBoxModel doInBackground() throws Exception {
                         return getModelByMetaClass(mc, nullable, onlyUsed, comparator, forceReload);
                     }
 
                     @Override
                     protected void done() {
                         try {
                             setModel(get());
                             setSelectedItem(cidsBean);
                         } catch (InterruptedException interruptedException) {
                         } catch (ExecutionException executionException) {
                             LOG.error("Error while initializing the model of a referenceCombo", executionException); // NOI18N
                         }
                     }
                 });
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   forceReload  DOCUMENT ME!
      *
      * @throws  IllegalStateException  DOCUMENT ME!
      */
     public void reload(final boolean forceReload) {
         if (metaClass == null) {
             throw new IllegalStateException("the metaclass has not been set yet"); // NOI18N
         }
 
         init(metaClass, forceReload);
     }
 
     @Override
     public String getBindingProperty() {
         return "selectedItem"; // NOI18N
     }
 
     @Override
     public Validator getValidator() {
         return null;
     }
 
     @Override
     public Converter getConverter() {
         return null;
     }
 
     @Override
     public void setSelectedItem(final Object anObject) {
         if (isFakeModel()) {
             setModel(new DefaultComboBoxModel(new Object[] { anObject }));
         }
         super.setSelectedItem(anObject);
         cidsBean = (CidsBean)anObject;
     }
 
     @Override
     public MetaClass getMetaClass() {
         return metaClass;
     }
 
     @Override
     public void setMetaClass(final MetaClass metaClass) {
         this.metaClass = metaClass;
         init(metaClass, false);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public boolean isFakeModel() {
         return fakeModel;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  fakeModel  DOCUMENT ME!
      */
     public void setFakeModel(final boolean fakeModel) {
         this.fakeModel = fakeModel;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public String getNullValueRepresentation() {
         return nullValueRepresentation;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  nullValueRepresentation  DOCUMENT ME!
      */
     public void setNullValueRepresentation(final String nullValueRepresentation) {
         this.nullValueRepresentation = nullValueRepresentation;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public boolean isNullable() {
         return nullable;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  onlyUsed  DOCUMENT ME!
      */
     public void setOnlyUsed(final boolean onlyUsed) {
         this.onlyUsed = onlyUsed;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public boolean isOnlyUsed() {
         return onlyUsed;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  nullable  DOCUMENT ME!
      */
     public void setNullable(final boolean nullable) {
         this.nullable = nullable;
     }
 
     @Override
     public Object getNullSourceValue() {
         return null;
     }
 
     @Override
     public Object getErrorSourceValue() {
         return null;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   mc           DOCUMENT ME!
      * @param   nullable     DOCUMENT ME!
      * @param   onlyUsed     DOCUMENT ME!
      * @param   comparator   DOCUMENT ME!
      * @param   forceReload  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static DefaultComboBoxModel getModelByMetaClass(final MetaClass mc,
             final boolean nullable,
             final boolean onlyUsed,
             final Comparator<CidsBean> comparator,
             final boolean forceReload) {
         final ClassAttribute ca = mc.getClassAttribute("sortingColumn");                                 // NOI18N
         String orderBy = "";                                                                             // NOI18N
         if (ca != null) {
             final String value = ca.getValue().toString();
             orderBy = " order by " + value;                                                              // NOI18N
         }
         String query = "select " + mc.getID() + "," + mc.getPrimaryKey() + " from " + mc.getTableName(); // NOI18N
         if (onlyUsed) {
             query += " where used is true";                                                              // NOI18N
         }
         query += orderBy;
 
         MetaObject[] metaObjects;
         try {
             metaObjects = MetaObjectCache.getInstance().getMetaObjectsByQuery(query, forceReload);
         } catch (final CacheException ex) {
             LOG.warn("cache could not come up with appropriate objects", ex); // NOI18N
             metaObjects = new MetaObject[0];
         }
 
         final List<CidsBean> cbv = new ArrayList<CidsBean>(metaObjects.length);
         if (nullable) {
             cbv.add(null);
         }
         for (final MetaObject mo : metaObjects) {
             cbv.add(mo.getBean());
         }
         if (ca == null) {
             // Sorts the model using String comparison on the bean's toString()
             Collections.sort(cbv, comparator);
         }
 
         return new DefaultComboBoxModel(cbv.toArray());
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   mc          DOCUMENT ME!
      * @param   nullable    DOCUMENT ME!
      * @param   onlyUsed    DOCUMENT ME!
      * @param   comparator  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  Exception  DOCUMENT ME!
      */
     public static DefaultComboBoxModel getModelByMetaClass(final MetaClass mc,
             final boolean nullable,
             final boolean onlyUsed,
             final Comparator<CidsBean> comparator) throws Exception {
         return getModelByMetaClass(mc, nullable, onlyUsed, comparator, false);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   mc        DOCUMENT ME!
      * @param   nullable  DOCUMENT ME!
      * @param   onlyUsed  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  Exception  DOCUMENT ME!
      */
     public static DefaultComboBoxModel getModelByMetaClass(final MetaClass mc,
             final boolean nullable,
             final boolean onlyUsed) throws Exception {
         return getModelByMetaClass(mc, nullable, onlyUsed, BEAN_TOSTRING_COMPARATOR, false);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   mc        DOCUMENT ME!
      * @param   nullable  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  Exception  DOCUMENT ME!
      */
     public static DefaultComboBoxModel getModelByMetaClass(final MetaClass mc, final boolean nullable)
             throws Exception {
         return getModelByMetaClass(mc, nullable, false, BEAN_TOSTRING_COMPARATOR, false);
     }
 
     //~ Inner Classes ----------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     protected static final class BeanToStringComparator implements Comparator<CidsBean> {
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public int compare(final CidsBean o1, final CidsBean o2) {
             final String s1 = (o1 == null) ? "" : o1.toString(); // NOI18N
             final String s2 = (o2 == null) ? "" : o2.toString(); // NOI18N
 
             return (s1).compareToIgnoreCase(s2);
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private final class MetaObjectChangeListenerImpl implements MetaObjectChangeListener {
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public void metaObjectAdded(final MetaObjectChangeEvent moce) {
             // we're only registered to the DefaultMetaObjectChangeSupport that asserts of proper initialisation of
             // events
             if ((metaClass != null) && metaClass.equals(moce.getNewMetaObject().getMetaClass())) {
                 init(metaClass, true);
             }
         }
 
         @Override
         public void metaObjectChanged(final MetaObjectChangeEvent moce) {
             // we're only registered to the DefaultMetaObjectChangeSupport that asserts of proper initialisation of
             // events
             if ((metaClass != null) && metaClass.equals(moce.getNewMetaObject().getMetaClass())) {
                 init(metaClass, true);
             }
         }
 
         @Override
         public void metaObjectRemoved(final MetaObjectChangeEvent moce) {
             // we're only registered to the DefaultMetaObjectChangeSupport that asserts of proper initialisation of
             // events
             if ((metaClass != null) && metaClass.equals(moce.getOldMetaObject().getMetaClass())) {
                 init(metaClass, true);
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private final class DefaultBindableReferenceComboRenderer extends DefaultListCellRenderer {
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public Component getListCellRendererComponent(final JList list,
                 final Object value,
                 final int index,
                 final boolean isSelected,
                 final boolean cellHasFocus) {
             final Component ret = super.getListCellRendererComponent(
                     list,
                     value,
                     index,
                     isSelected,
                     cellHasFocus);
             if ((value == null) && (ret instanceof JLabel)) {
                 ((JLabel)ret).setText(getNullValueRepresentation());
             }
 
             return ret;
         }
     }
 }
