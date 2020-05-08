 package org.motechproject.carereporting.domain.listeners.configurer;
 
 import org.hibernate.SessionFactory;
 import org.hibernate.event.service.spi.EventListenerRegistry;
 import org.hibernate.event.spi.EventType;
 import org.hibernate.internal.SessionFactoryImpl;
 import org.motechproject.carereporting.domain.listeners.AbstractEntityEventListener;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import javax.annotation.PostConstruct;
 
 @Component
 public class HibernateListenersConfigurer {
 
     @Autowired
     private SessionFactory sessionFactory;
 
     private AbstractEntityEventListener listener;
 
     @PostConstruct
     public void registerListeners() {
         listener = new AbstractEntityEventListener();
 
         EventListenerRegistry registry = ((SessionFactoryImpl) sessionFactory).getServiceRegistry().getService(EventListenerRegistry.class);
         registry.getEventListenerGroup(EventType.PRE_INSERT).appendListener(listener);
        registry.getEventListenerGroup(EventType.UPDATE).appendListener(listener);
        registry.getEventListenerGroup(EventType.SAVE).appendListener(listener);
     }
 }
