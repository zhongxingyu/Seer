 package org.tsinghua.omedia.form;
 
 import java.lang.reflect.Field;
 
 import org.tsinghua.omedia.annotation.form.AlphaOrNumber;
 import org.tsinghua.omedia.annotation.form.Email;
 import org.tsinghua.omedia.annotation.form.NotEmpty;
 import org.tsinghua.omedia.annotation.form.SameTo;
 import org.tsinghua.omedia.annotation.form.Size;
 import org.tsinghua.omedia.tool.Logger;
 import org.tsinghua.omedia.tool.StringUtils;
 
/**
 * 
 * @author xuhongfeng
 *
 */
 public class FormValidator {
     private static final Logger logger = Logger.getLogger(FormValidator.class);
     
     @SuppressWarnings("unchecked")
     public static <F extends AbstractForm> String  validate(F form) {
         Throwable exception;
         try {
             Class<? extends AbstractForm> clazz = form.getClass();
             while(true) {
                 Field[] fields = clazz.getDeclaredFields();
                 for(Field e:fields) {
                     e.setAccessible(true);
                     if(e.isAnnotationPresent(NotEmpty.class)) {
                         String value = (String) e.get(form);
                         if(value==null || value.length()==0) {
                             return e.getAnnotation(NotEmpty.class).msg();
                         }
                     }
                     if(e.isAnnotationPresent(Size.class)) {
                         Size sizeAnno = e.getAnnotation(Size.class);
                         String value = (String) e.get(form);
                         if(value==null || value.length()<sizeAnno.min()) {
                             return sizeAnno.minMsg();
                         } else if(value.length()>sizeAnno.max()) {
                             return sizeAnno.maxMsg();
                         }
                     }
                     if(e.isAnnotationPresent(AlphaOrNumber.class)) {
                         String value = (String) e.get(form);
                         AlphaOrNumber anno = e.getAnnotation(AlphaOrNumber.class);
                         if(value==null || !StringUtils.isAlphaOrNum(value)) {
                             return anno.msg();
                         }
                     }
                     if(e.isAnnotationPresent(Email.class)) {
                         String value = (String) e.get(form);
                         Email anno = e.getAnnotation(Email.class);
                         if(value==null || !StringUtils.isEmail(value)) {
                             return anno.msg();
                         }
                     }
                     if(e.isAnnotationPresent(SameTo.class)) {
                         Object value = e.get(form);
                         SameTo anno = e.getAnnotation(SameTo.class);
                         String name = anno.name();
                         Field referenceField = clazz.getDeclaredField(name);
                         Object referenceObject = referenceField.get(form);
                         if(!value.equals(referenceObject)) {
                             return anno.msg();
                         }
                     }
                 }
                 if(clazz.getSuperclass() == AbstractForm.class) {
                     break;
                 }
                 clazz = (Class<? extends AbstractForm>) clazz.getSuperclass();
             }
             return null;
         } catch (Throwable e1) {
             exception = e1;
         }
         logger.error("", exception);
         return exception.getMessage();
     }
 }
