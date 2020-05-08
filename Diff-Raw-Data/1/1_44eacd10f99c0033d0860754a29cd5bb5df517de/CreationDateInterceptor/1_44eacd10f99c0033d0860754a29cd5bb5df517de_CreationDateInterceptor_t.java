 package com.monits.commons.dao.interceptor;
 
 import java.io.Serializable;
 
 import org.hibernate.EmptyInterceptor;
 import org.hibernate.type.Type;
 import org.joda.time.DateTime;
 
 import com.monits.commons.model.CreationDateable;
 
 /**
  * Sets the creation date on a {@link CreationDateable} instance
  *
  */
 public class CreationDateInterceptor extends EmptyInterceptor {
 
 	private static final long serialVersionUID = 1L;
 
 	@Override
 	public boolean onSave(Object entity, Serializable id, Object[] state,
 			String[] propertyNames, Type[] types) {
 
 		if (entity instanceof CreationDateable) {
 			for (int i = 0; i < propertyNames.length; i++) {
 				if (CreationDateable.FIELD_NAME.equals(propertyNames[i])) {
 					state[i] = new DateTime();
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 }
