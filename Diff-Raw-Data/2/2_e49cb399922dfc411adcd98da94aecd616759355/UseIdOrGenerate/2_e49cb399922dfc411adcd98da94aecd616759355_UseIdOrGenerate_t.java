 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.wazari.dao.jpa.entity.idGenerator;
 
 import java.io.Serializable;
 import net.wazari.dao.entity.facades.EntityWithId;
 import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
 import org.hibernate.id.IdentityGenerator;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author kevinpouget
  */
 public class UseIdOrGenerate extends IdentityGenerator {
     private static final Logger log = LoggerFactory.getLogger(UseIdOrGenerate.class.getName());
 
     @Override
     public Serializable generate(SessionImplementor session, Object obj) throws HibernateException {
         if (obj == null) throw new HibernateException(new NullPointerException()) ;
 
         if ((((EntityWithId) obj).getId()) == null) {
             Serializable id = super.generate(session, obj) ;
             return id;
         } else {
             return ((EntityWithId) obj).getId();
 
         }
     }
 }
