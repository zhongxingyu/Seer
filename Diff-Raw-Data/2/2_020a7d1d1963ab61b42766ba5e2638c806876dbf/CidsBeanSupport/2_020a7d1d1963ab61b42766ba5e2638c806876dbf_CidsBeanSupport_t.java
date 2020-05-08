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
 package de.cismet.cids.custom.objectrenderer.utils;
 
 import Sirius.navigator.connection.SessionManager;
 
 import Sirius.server.middleware.types.MetaClass;
 import Sirius.server.newuser.User;
 
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import de.cismet.cids.dynamics.CidsBean;
 
 import de.cismet.cids.navigator.utils.ClassCacheMultiple;
 
 /**
  * DOCUMENT ME!
  *
  * @author   srichter
  * @version  $Revision$, $Date$
  */
 public final class CidsBeanSupport {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CidsBeanSupport.class);
     public static final String DOMAIN_NAME = "WUNDA_BLAU";
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new CidsBeanSupport object.
      *
      * @throws  AssertionError  DOCUMENT ME!
      */
     private CidsBeanSupport() {
         throw new AssertionError();
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @param   tableName          DOCUMENT ME!
      * @param   initialProperties  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  Exception  DOCUMENT ME!
      */
     public static CidsBean createNewCidsBeanFromTableName(final String tableName,
             final Map<String, Object> initialProperties) throws Exception {
         final CidsBean newBean = createNewCidsBeanFromTableName(tableName);
         for (final Entry<String, Object> property : initialProperties.entrySet()) {
             newBean.setProperty(property.getKey(), property.getValue());
         }
         return newBean;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   tableName  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  Exception  DOCUMENT ME!
      */
     public static CidsBean createNewCidsBeanFromTableName(final String tableName) throws Exception {
         if (tableName != null) {
             final MetaClass metaClass = ClassCacheMultiple.getMetaClass(DOMAIN_NAME, tableName);
             if (metaClass != null) {
                 return metaClass.getEmptyInstance().getBean();
             }
         }
         throw new Exception("Could not find MetaClass for table " + tableName);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   bean                DOCUMENT ME!
      * @param   collectionProperty  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static List<CidsBean> getBeanCollectionFromProperty(final CidsBean bean, final String collectionProperty) {
         if ((bean != null) && (collectionProperty != null)) {
             final Object colObj = bean.getProperty(collectionProperty);
             if (colObj instanceof Collection) {
                 return (List<CidsBean>)colObj;
             }
         }
         return null;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   bean  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static boolean checkWritePermission(final CidsBean bean) {
         final User user = SessionManager.getSession().getUser();
        return bean.getHasWritePermission(user)&&bean.hasObjectWritePermission(user);
     }
 }
