 package org.synyx.minos.skillz.domain.convert;
 
 import java.util.Collection;
 
 import org.springframework.beans.BeansException;
 import org.springframework.beans.factory.BeanFactoryUtils;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 import org.springframework.core.convert.converter.Converter;
 import org.springframework.core.convert.converter.ConverterFactory;
 import org.synyx.hades.dao.GenericDao;
 import org.synyx.hades.domain.Persistable;
 import org.synyx.hades.util.ClassUtils;
 
 
 /**
  * Converts a {@link String} to the matching {@link Persistable}.
  * 
  * @author Markus Knittig - knittig@synyx.de
  */
@SuppressWarnings("unchecked")
 public class StringToPersistableConverterFactory implements
         ConverterFactory<String, Persistable<Long>>, ApplicationContextAware {
 
     private Collection<GenericDao> daos;
 
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * org.springframework.core.convert.converter.ConverterFactory#getConverter
      * (java.lang.Class)
      */
     @Override
     public <T extends Persistable<Long>> Converter<String, T> getConverter(
             Class<T> targetType) {
 
         return new StringToPersistableConverter(targetType);
     }
 
     private class StringToPersistableConverter<T extends Persistable<Long>>
             implements Converter<String, T> {
 
         private final Class<T> persistableType;
 
 
         public StringToPersistableConverter(Class<T> persistableType) {
 
             this.persistableType = persistableType;
         }
 
 
         /*
          * (non-Javadoc)
          * 
          * @see
          * org.springframework.core.convert.converter.Converter#convert(java
          * .lang.Object)
          */
         @Override
         public T convert(String source) {
 
             GenericDao dao = getDaoForType(persistableType);
             return (T) dao.readByPrimaryKey(Long.parseLong(source));
         }
 
     }
 
 
     private GenericDao<?, ? extends Persistable<Long>> getDaoForType(
             Class<? extends Persistable<Long>> type) {
 
         for (GenericDao<?, ? extends Persistable<Long>> dao : daos) {
             if (ClassUtils.getDomainClass(dao.getClass()).equals(type)) {
                 return dao;
             }
         }
         throw new RuntimeException("No DAO for type " + type + " found!");
     }
 
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * org.springframework.context.ApplicationContextAware#setApplicationContext
      * (org.springframework.context.ApplicationContext)
      */
     @Override
     public void setApplicationContext(ApplicationContext applicationContext)
             throws BeansException {
 
         daos =
                 BeanFactoryUtils.beansOfTypeIncludingAncestors(
                         applicationContext, GenericDao.class).values();
     }
 
 }
