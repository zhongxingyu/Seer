 package org.mule.galaxy.impl.jcr.onm;
 
 import java.beans.BeanInfo;
 import java.beans.Introspector;
 import java.beans.PropertyDescriptor;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.jcr.Node;
 import javax.jcr.Session;
 
 /**
  * A simple way to persist/build objects to and from JCR nodes. Only works for a 
  * limited set of use cases, but is good enough for most of our usages.
  */
 public class ClassPersister {
     
     private Class type;
     private Map<String, FieldDescriptor> propertyToFD = new HashMap<String, FieldDescriptor>();
     private List<String> parents = new ArrayList<String>();
     
     private PersisterManager persisterManager;
     private String path;
     
     public ClassPersister(Class type, String path, PersisterManager persisterManager) throws Exception {
         this.type = type;
         this.persisterManager = persisterManager;
         this.path = path;
         
         BeanInfo info = Introspector.getBeanInfo(type);
         for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
             Method read = pd.getReadMethod();
             Method write = pd.getWriteMethod();
             
             if (read == null || write == null) {
                 continue;
             }
             
             if (read.getDeclaringClass().equals(Object.class) 
                 || write.getDeclaringClass().equals(Object.class)) {
                 continue;
             }
             
             if (pd.getName().equals("id")) {
                 continue;
             }
             
             FieldDescriptor fd = new FieldDescriptor();
             fd.setName(pd.getName());
             fd.setReadMethod(read);
             fd.setWriteMethod(write);
             fd.setType(pd.getPropertyType());
             
             OneToMany otm = read.getAnnotation(OneToMany.class);
             if (otm != null) {
                 fd.setOneToMany(otm);
                 
                 parents.add(otm.mappedBy());
             }
             fd.setClassPersister(this);
             
             propertyToFD.put(pd.getName(), fd);
         }
     }
     
     public void persist(Object o, Node n, Session session) throws Exception {
         for (FieldDescriptor fd : propertyToFD.values()) {
             Object result = fd.getReadMethod().invoke(o, new Object[0]);
             FieldPersister persister = persisterManager.getPersister(fd.getType());
             
             persister.persist(result, n, fd, session);
         }
     }
     
     public Object build(Node n, Session session) throws Exception {
         Object o = type.newInstance();
         
         for (FieldDescriptor fd : propertyToFD.values()) {
             if (parents.contains(fd.getName())) {
                 continue;
             }
             
             FieldPersister persister = persisterManager.getPersister(fd.getType());
             Object value = persister.build(n, fd, session);
             
             if (value != null) {
                fd.getWriteMethod().invoke(o, value);
             }
         }
         
         return o;
     }
 
     public String getPath() {
         return path;
     }
 
     public Class getType() {
         return type;
     }
 }
