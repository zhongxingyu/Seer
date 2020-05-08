 /*
  * SafeOnline project.
  *
  * Copyright 2006-2007 Lin.k N.V. All rights reserved.
  * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
  */
 
 package net.link.util.ws;
 
 import com.google.common.collect.MutableClassToInstanceMap;
import com.sun.xml.internal.ws.api.message.Packet;
import com.sun.xml.internal.ws.server.AbstractMultiInstanceResolver;
 import java.lang.reflect.Field;
 import javax.ejb.EJB;
 import javax.ejb.EJBException;
 import net.link.util.j2ee.EJBUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 
 /**
  * Implementation class for injection JAX-WS RI instance resolver. This JAX-WS RI instance resolver injects JNDI components. Simply use the
  * EJB annotation with mappedName attribute on the injection fields of your JAX-WS endpoints. Use only to inject stateless session beans.
  * This cannot be used for injection of stateful session beans.
  *
  * @author fcorneli
  */
 public class InjectionInstanceResolver<T> extends AbstractMultiInstanceResolver<T> {
 
     private static final Log LOG = LogFactory.getLog( InjectionInstanceResolver.class );
 
     private final MutableClassToInstanceMap<T> instances = MutableClassToInstanceMap.create();
 
     public InjectionInstanceResolver(Class<T> clazz) {
 
         super( clazz );
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public T resolve(@SuppressWarnings("unused") Packet request) {
 
         T instance = instances.getInstance( clazz );
         if (null == instance) {
             LOG.debug( "creating new instance for: " + clazz );
             instances.putInstance( clazz, instance = create() );
             ejbInjection( instance );
         }
 
         return instance;
     }
 
     private void ejbInjection(T instance) {
 
         Field[] fields = clazz.getDeclaredFields();
         for (Field field : fields) {
             EJB ejb = field.getAnnotation( EJB.class );
             if (null == ejb)
                 continue;
             String mappedName = ejb.mappedName();
             if (null == mappedName)
                 throw new EJBException( "@EJB mappedName attribute required" );
             LOG.debug( "injecting: " + mappedName );
             Class<?> type = field.getType();
             if (!type.isInterface())
                 throw new EJBException( "field is not an interface type" );
             Object ejbRef = EJBUtils.getEJB( mappedName, type );
             field.setAccessible( true );
             try {
                 field.set( instance, ejbRef );
             }
             catch (IllegalArgumentException e) {
                 throw new EJBException( "illegal argument", e );
             }
             catch (IllegalAccessException e) {
                 throw new EJBException( "illegal access", e );
             }
         }
     }
 
     @Override
     public void dispose() {
 
         instances.clear();
 
         super.dispose();
     }
 }
