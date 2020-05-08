 package com.adi3000.common.util.data;
 
 import java.util.Collection;
 
 import org.hibernate.Hibernate;
 
 import com.adi3000.common.database.hibernate.data.DataObject;
 
 public final class HibernateUtil {
 
 	public static void initialize(Collection<? extends DataObject> list){
		for(DataObject o : list){
			Hibernate.initialize(o);
 		}
 	}
 	public static void initialize(DataObject proxy){
 			Hibernate.initialize(proxy);
 	}
 }
