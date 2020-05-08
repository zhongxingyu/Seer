 package org.motechproject.carereporting.domain.listeners;
 
import org.hibernate.HibernateException;
 import org.hibernate.event.spi.PreInsertEvent;
 import org.hibernate.event.spi.PreInsertEventListener;
import org.hibernate.event.spi.SaveOrUpdateEvent;
import org.hibernate.event.spi.SaveOrUpdateEventListener;
 import org.motechproject.carereporting.domain.AbstractEntity;
 
 import java.util.Date;
 
 
public class AbstractEntityEventListener implements PreInsertEventListener, SaveOrUpdateEventListener {
 
     @Override
     public boolean onPreInsert(PreInsertEvent preInsertEvent) {
         if (preInsertEvent.getEntity() instanceof AbstractEntity) {
             AbstractEntity entity = (AbstractEntity) preInsertEvent.getEntity();
             entity.setCreationDate(new Date());
         }
         return false;
     }
 
     @Override
    public void onSaveOrUpdate(SaveOrUpdateEvent event) throws HibernateException {
        if (event.getEntity() instanceof AbstractEntity) {
            AbstractEntity entity = (AbstractEntity) event.getEntity();
             entity.setModificationDate(new Date());
         }
     }

 }
