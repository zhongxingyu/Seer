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
  *
  * @author thorsten
  * 
  * 
  */
 @org.openide.util.lookup.ServiceProvider(service = CidsBeanPersistService.class)
 public class NavigatorCidsBeanPersistService implements CidsBeanPersistService {
 
     public CidsBean persistCidsBean(CidsBean cidsBean) throws Exception {
        MetaObject MetaObject=cidsBean.getMetaObject();
        String domain = MetaObject.getDomain();
         User user = SessionManager.getSession().getUser();
        if (MetaObject.getStatus() == MetaObject.MODIFIED) {
            SessionManager.getConnection().updateMetaObject(user, MetaObject, domain);
            return SessionManager.getConnection().getMetaObject(user, MetaObject.getID(), MetaObject.getClassID(), domain).getBean();
        } else if (MetaObject.getStatus() == MetaObject.TO_DELETE) {
            SessionManager.getConnection().deleteMetaObject(user, MetaObject, domain);
             return null;
        } else if (MetaObject.getStatus() == MetaObject.NEW) {
            MetaObject mo = SessionManager.getConnection().insertMetaObject(user, MetaObject, domain);
             if (mo != null) {
                 return mo.getBean();
             }
         }
         return null;
     }
 }
