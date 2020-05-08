 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.editors;
 
 import Sirius.navigator.tools.MetaObjectCache;
 
 import Sirius.server.localserver.attribute.MemberAttributeInfo;
 import Sirius.server.middleware.types.MetaClass;
 import Sirius.server.middleware.types.MetaClassStore;
 import Sirius.server.middleware.types.MetaObject;
 
 import org.jdesktop.beansbinding.Converter;
 import org.jdesktop.beansbinding.Validator;
 
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 
import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.JCheckBox;
 import javax.swing.JPanel;
 import javax.swing.SwingWorker;
 
 import de.cismet.cids.navigator.utils.ClassCacheMultiple;
 
 import de.cismet.tools.CismetThreadPool;
import org.apache.log4j.Logger;
 
 /**
  * DOCUMENT ME!
  *
  * @author   therter
  * @version  $Revision$, $Date$
  */
 public class DefaultBindableCheckboxField extends JPanel implements Bindable, MetaClassStore, ActionListener {
 
     //~ Static fields/initializers ---------------------------------------------
 
    private static final Logger LOG = Logger.getLogger(DefaultBindableCheckboxField.class);
 
     //~ Instance fields --------------------------------------------------------
 
     private PropertyChangeSupport propertyChangeSupport;
     private List selectedElements = null;
     private MetaClass mc = null;
     private Map<JCheckBox, MetaObject> boxToObjectMapping = new HashMap<JCheckBox, MetaObject>();
     private volatile boolean threadRunning = false;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new CustomReferencedCheckboxField object.
      */
     public DefaultBindableCheckboxField() {
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public PropertyChangeSupport getPropertyChangeSupport() {
         if (propertyChangeSupport == null) {
             propertyChangeSupport = new PropertyChangeSupport(this);
         }
         return propertyChangeSupport;
     }
 
     /**
      * Add PropertyChangeListener.
      *
      * @param  listener  DOCUMENT ME!
      */
     @Override
     public void addPropertyChangeListener(final PropertyChangeListener listener) {
         if (listener != null) {
             getPropertyChangeSupport().addPropertyChangeListener(listener);
         }
     }
 
     /**
      * Remove PropertyChangeListener.
      *
      * @param  listener  DOCUMENT ME!
      */
     @Override
     public void removePropertyChangeListener(final PropertyChangeListener listener) {
         getPropertyChangeSupport().removePropertyChangeListener(listener);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  elements  DOCUMENT ME!
      */
     public void setSelectedElements(final Object elements) {
         if (elements instanceof List) {
             this.selectedElements = (List)elements;
         }
         activateSelectedObjects();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public Object getSelectedElements() {
         return selectedElements;
     }
 
     @Override
     public String getBindingProperty() {
         return "selectedElements";
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
     public Object getNullSourceValue() {
         return null;
     }
 
     @Override
     public Object getErrorSourceValue() {
         return null;
     }
 
     @Override
     public MetaClass getMetaClass() {
         return this.mc;
     }
 
     @Override
     public void setMetaClass(final MetaClass metaClass) {
         if (LOG.isDebugEnabled()) {
             LOG.debug("set meta class " + ((metaClass != null) ? metaClass.getName() : "null"));
         }
         CismetThreadPool.execute(new SwingWorker<MetaObject[], Void>() {
 
                 @Override
                 protected MetaObject[] doInBackground() throws Exception {
                     while (!setThreadRunning()) {
                         try {
                             Thread.sleep(20);
                         } catch (InterruptedException ex) {
                         }
                     }
 
                     mc = metaClass;
                     boxToObjectMapping = new HashMap<JCheckBox, MetaObject>();
 
                     if (mc != null) {
                         final MetaClass foreignClass = getReferencedClass(mc);
                         final String query = "select " + foreignClass.getID() + ", " + foreignClass.getPrimaryKey()
                                     + " from "
                                     + foreignClass.getTableName();
 
                         return MetaObjectCache.getInstance().getMetaObjectByQuery(query);
                     } else {
                         LOG.error("The meta class was not set.", new Throwable());
                     }
 
                     return null;
                 }
 
                 @Override
                 protected void done() {
                     try {
                         final MetaObject[] metaObjects = get();
                         DefaultBindableCheckboxField.this.removeAll();
 
                         if (metaObjects != null) {
                             JCheckBox box = null;
                             DefaultBindableCheckboxField.this.setLayout(new GridLayout(metaObjects.length, 1));
                             if (LOG.isDebugEnabled()) {
                                 LOG.debug(metaObjects.length + "objects found.");
                             }
 
                             for (final MetaObject tmpMc : metaObjects) {
                                 box = new JCheckBox(tmpMc.getBean().toString());
                                 box.addActionListener(DefaultBindableCheckboxField.this);
                                 box.setOpaque(false);
                                 box.setContentAreaFilled(false);
                                 add(box);
                                 boxToObjectMapping.put(box, tmpMc);
                             }
                             activateSelectedObjects();
                         }
                     } catch (Exception e) {
                         LOG.error("Error while filling a checkbox field.", e); // NOI18N
                     } finally {
                         threadRunning = false;
                     }
                 }
             });
     }
 
     /**
      * DOCUMENT ME!
      */
     private void activateSelectedObjects() {
         if (selectedElements != null) {
             final Iterator<JCheckBox> it = boxToObjectMapping.keySet().iterator();
 
             while (it.hasNext()) {
                 final JCheckBox tmp = it.next();
                 final MetaObject mo = boxToObjectMapping.get(tmp);
                 if ((mo != null) && selectedElements.contains(mo.getBean())) {
                     tmp.setSelected(true);
                 } else {
                     tmp.setSelected(false);
                 }
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   mclass  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private MetaClass getReferencedClass(final MetaClass mclass) {
         MetaClass result = mclass;
         if (mclass.isArrayElementLink()) {
             final HashMap hm = mc.getMemberAttributeInfos();
             final Iterator it = hm.values().iterator();
             while (it.hasNext()) {
                 final Object tmp = it.next();
                 if (tmp instanceof MemberAttributeInfo) {
                     if (((MemberAttributeInfo)tmp).isForeignKey()) {
                         final int classId = ((MemberAttributeInfo)tmp).getForeignKeyClassId();
                         result = ClassCacheMultiple.getMetaClass(mclass.getDomain(), classId);
                     }
                 }
             }
         } else {
             LOG.error("The given class " + mclass.getName() + " is no array element link.");
         }
 
         return result;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  decider                 DOCUMENT ME!
      * @param  removeSelectedElements  DOCUMENT ME!
      */
     public void refreshCheckboxState(final FieldStateDecider decider, final boolean removeSelectedElements) {
         CismetThreadPool.execute(new SwingWorker<Void, Void>() {
 
                 @Override
                 protected Void doInBackground() throws Exception {
                     while (!setThreadRunning()) {
                         try {
                             Thread.sleep(20);
                         } catch (InterruptedException ex) {
                         }
                     }
 
                     return null;
                 }
 
                 @Override
                 protected void done() {
                     try {
                         final Iterator<JCheckBox> it = boxToObjectMapping.keySet().iterator();
                         if (removeSelectedElements) {
                             selectedElements.clear();
                         }
 
                         while (it.hasNext()) {
                             final JCheckBox box = it.next();
                             box.setEnabled(decider.isCheckboxForClassActive(boxToObjectMapping.get(box)));
                             box.setSelected(false);
                             if (Thread.currentThread().isInterrupted()) {
                                 return;
                             }
                         }
                         activateSelectedObjects();
                     } finally {
                         threadRunning = false;
                     }
                 }
             });
     }
 
     /**
      * DOCUMENT ME!
      */
     public void dispose() {
     }
 
     @Override
     public void actionPerformed(final ActionEvent ae) {
         final MetaObject mo = boxToObjectMapping.get((JCheckBox)ae.getSource());
        final List old = new ArrayList(selectedElements);
         if (selectedElements.contains(mo.getBean())) {
             selectedElements.remove(mo.getBean());
         } else {
             selectedElements.add(mo.getBean());
         }
        propertyChangeSupport.firePropertyChange("selectedElements", old, selectedElements);
        propertyChangeSupport.firePropertyChange("selectedElements", null, mo.getBean());
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private synchronized boolean setThreadRunning() {
         if (threadRunning) {
             return false;
         } else {
             threadRunning = true;
             return true;
         }
     }
 }
