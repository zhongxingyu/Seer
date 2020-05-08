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
 package de.cismet.cids.navigator.utils;
 
 import Sirius.navigator.connection.SessionManager;
 
 import Sirius.server.middleware.types.MetaObject;
 import Sirius.server.newuser.User;
 
 import de.cismet.cids.dynamics.CidsBean;
 
 import de.cismet.cids.utils.CidsBeanPersistService;
 
 /**
  * DOCUMENT ME!
  *
  * @author   thorsten
  * @version  $Revision$, $Date$
  */
 @org.openide.util.lookup.ServiceProvider(service = CidsBeanPersistService.class)
 public class NavigatorCidsBeanPersistService implements CidsBeanPersistService {
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public CidsBean persistCidsBean(final CidsBean cidsBean) throws Exception {
         final MetaObject metaObject = cidsBean.getMetaObject();
         final String domain = metaObject.getDomain();
         final User user = SessionManager.getSession().getUser();
        if (metaObject.getStatus() == metaObject.MODIFIED) {
             SessionManager.getConnection().updateMetaObject(user, metaObject, domain);
             return SessionManager.getConnection()
                         .getMetaObject(user, metaObject.getID(), metaObject.getClassID(), domain)
                         .getBean();
        } else if (metaObject.getStatus() == metaObject.TO_DELETE) {
             SessionManager.getConnection().deleteMetaObject(user, metaObject, domain);
             return null;
        } else if (metaObject.getStatus() == metaObject.NEW) {
             final MetaObject mo = SessionManager.getConnection().insertMetaObject(user, metaObject, domain);
            if (mo != null) {
                return mo.getBean();
            }
         }
        return null;
     }
 }
