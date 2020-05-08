 package org.rosenvold.spring.convention.beanclassresolvers;
 
import org.rosenvold.spring.convention.CandidateEvaluator;
 import org.rosenvold.spring.convention.InterfaceToImplementationMapper;
import org.rosenvold.spring.convention.NameToClassResolver;
 
 /**
  * @author Kristian Rosenvold
  */
 public class GenericBeanClassResolver implements NameToClassResolver {
    private final CandidateEvaluator candidateEvaluator;
     private final InterfaceToImplementationMapper[] mappers;
 
    public GenericBeanClassResolver(CandidateEvaluator candidateEvaluator, InterfaceToImplementationMapper... mappers) {
        this.candidateEvaluator = candidateEvaluator;
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
             return null;
         }
        return candidateEvaluator.isBean(aClass) ? aClass : null ;
     }
 
     private Class resolveClass(final String beanName) {
         try {
             return Class.forName(beanName);
         } catch (ClassNotFoundException ex) {
             return null;
         }
     }
 
 
 }
