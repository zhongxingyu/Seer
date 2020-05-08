 package org.rosenvold.spring.convention.beanclassresolvers;
 
 import org.rosenvold.spring.convention.NameToClassResolver;
 import org.rosenvold.spring.convention.InterfaceToImplementationMapper;
 
 /**
  * @author Kristian Rosenvold
  */
 public class GenericBeanClassResolver implements NameToClassResolver {
     private final InterfaceToImplementationMapper[] mappers;
 
     public GenericBeanClassResolver(InterfaceToImplementationMapper... mappers) {
         this.mappers = mappers;
     }
 
     public Class resolveBean(String name) {
         final Class aClass = resolveClass(name);
         if (aClass != null && aClass.isInterface()) {
             final int mapperCount = mappers.length;
             for (int i = 0; i < mapperCount; i++){
                 final Class beanClass = mappers[i].getBeanClass(aClass);
                 if (beanClass != null){
                     return beanClass;
                 }
             }
         }
         return aClass;
     }
 
     private Class resolveClass(final String beanName) {
         try {
             return Class.forName(beanName);
         } catch (ClassNotFoundException ex) {
             return null;
         }
     }
 
 
 }
