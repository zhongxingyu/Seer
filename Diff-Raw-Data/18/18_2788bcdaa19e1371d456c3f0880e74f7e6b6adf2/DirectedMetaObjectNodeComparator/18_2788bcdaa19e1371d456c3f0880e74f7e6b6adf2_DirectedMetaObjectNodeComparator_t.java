 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.navigator.utils;
 
 import Sirius.navigator.connection.SessionManager;
 import Sirius.navigator.exception.ConnectionException;
 
 import Sirius.server.middleware.types.MetaClassNode;
 import Sirius.server.middleware.types.MetaNode;
 import Sirius.server.middleware.types.MetaObject;
 import Sirius.server.middleware.types.MetaObjectNode;
 import Sirius.server.middleware.types.Node;
 
 import org.apache.log4j.Logger;
 
 import java.util.Comparator;
 
 /**
  * Works like the MetaObjectNodeComparator. Additionally sort order can be set to be ascending or descending.
  *
  * @author   jweintraut
  * @version  $Revision$, $Date$
  */
 public class DirectedMetaObjectNodeComparator implements Comparator<Node> {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final Logger LOG = Logger.getLogger(DirectedMetaObjectNodeComparator.class);
 
     //~ Instance fields --------------------------------------------------------
 
     private boolean ascending;
     private boolean cancelled = false;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new DirectedMetaObjectNodeComparator object.
      *
      * @param  ascending  Sort ascending?
      */
     public DirectedMetaObjectNodeComparator(final boolean ascending) {
         this.ascending = ascending;
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * Since compare() connects to the server, the comparison is time consuming. Once called, Arrays.sort() invokes
      * compare() for every array entry, even if the user cancelled the SwingWorker which called Arrays.sort(). To reduce
      * resource consumption this method sets a flag which tells the comparator to abort the comparison. This is done by
      * returning a default value for every unsorted entry.
      */
     public void cancel() {
         cancelled = true;
     }
 
     @Override
     public int compare(final Node o1, final Node o2) {
         if (cancelled) {
             return 0;
         }
 
         if (!isSupported(o1) || !isSupported(o2)) {
             LOG.warn("The DirectedMetaObjectNodeComparator should compare a node of an unknown type. Types: "
                         + o1.getClass().getName() + " and " + o2.getClass().getName());
             return 0;
         }
         if ((o1 == null) && (o2 == null)) {
             return 0;
         }
         if (o1 == null) {
             return ascending ? -1 : 1;
         }
         if (o2 == null) {
             return ascending ? 1 : -1;
         }
 
         String mos1 = o1.toString();
         String mos2 = o2.toString();
 
         final String class1 = o1.getClassId() + "@" + o1.getDomain();
         final String class2 = o2.getClassId() + "@" + o2.getDomain();
 
        if (((class1 != null) && (class2 != null)) || ((class1 == null) && (class2 == null))) {
            int comparison = 0;
 
            if (!((class1 == null) && (class2 == null))) {
                comparison = class1.compareTo(class2);
            }

            if ((comparison == 0)) {
                 if (((mos1 == null) || (mos1.trim().length() == 0)) && (o1 instanceof MetaObjectNode)) {
                     final MetaObjectNode mon1 = (MetaObjectNode)o1;
                     MetaObject mo1 = mon1.getObject();
                     try {
                         if (mo1 == null) {
                             mo1 = SessionManager.getProxy()
                                         .getMetaObject(
                                                 ((MetaObjectNode)o1).getObjectId(),
                                                 o1.getClassId(),
                                                 o1.getDomain());
                             mon1.setObject(mo1);
                             mon1.setName(mo1.toString());
                         }
                     } catch (final ConnectionException e) {
                         LOG.error("Connection problem: ", e);
                     }
                     mos1 = mon1.toString();
                 }
                 if (((mos2 == null) || (mos2.trim().length() == 0)) && (o2 instanceof MetaObjectNode)) {
                     try {
                         final MetaObjectNode mon2 = (MetaObjectNode)o2;
                         MetaObject mo2 = mon2.getObject();
 
                         if (mo2 == null) {
                             mo2 = SessionManager.getProxy()
                                         .getMetaObject(
                                                 ((MetaObjectNode)o2).getObjectId(),
                                                 o2.getClassId(),
                                                 o2.getDomain());
                             mon2.setObject(mo2);
                             mon2.setName(mo2.toString());
                         }
                         mos2 = mon2.toString();
                     } catch (final ConnectionException e) {
                         LOG.error("Connection problem: ", e);
                     }
                 }
 
                 if ((mos1 == null) && (mos2 == null)) {
                     comparison = 0;
                 } else if (mos1 == null) {
                     comparison = -1;
                 } else if (mos2 == null) {
                     comparison = 1;
                 } else {
                     comparison = mos1.compareTo(mos2);
                 }
            } else {
                if (class1 == null) {
                    comparison = -1;
                } else if (class2 == null) {
                    comparison = 1;
                }
             }
 
             return ascending ? comparison : (-1 * comparison);
         }
         return 0;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   n  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private boolean isSupported(final Node n) {
         return ((n instanceof MetaObjectNode) || (n instanceof MetaNode) || (n instanceof MetaClassNode));
     }
 }
