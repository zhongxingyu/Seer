 package org.motechproject.carereporting.domain.listeners;
 
 import org.hibernate.event.spi.PreInsertEvent;
 import org.hibernate.event.spi.PreInsertEventListener;
import org.hibernate.event.spi.PreUpdateEvent;
import org.hibernate.event.spi.PreUpdateEventListener;
 import org.motechproject.carereporting.domain.AbstractEntity;
 
 import java.util.Date;
 
 
public class AbstractEntityEventListener implements PreInsertEventListener, PreUpdateEventListener {
 
     @Override
     public boolean onPreInsert(PreInsertEvent preInsertEvent) {
         if (preInsertEvent.getEntity() instanceof AbstractEntity) {
             AbstractEntity entity = (AbstractEntity) preInsertEvent.getEntity();
             entity.setCreationDate(new Date());
            entity.setModificationDate(new Date());
         }
         return false;
     }
 
     @Override
    public boolean onPreUpdate(PreUpdateEvent preUpdateEvent) {
        if (preUpdateEvent.getEntity() instanceof AbstractEntity) {
            AbstractEntity entity = (AbstractEntity) preUpdateEvent.getEntity();
             entity.setModificationDate(new Date());
         }
        return false;
     }
 }
