 /*
  * Copyright 2012 INRIA
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.mymed.controller.core.manager;
 
 import java.io.UnsupportedEncodingException;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import ch.qos.logback.classic.Logger;
 
 import com.mymed.controller.core.exception.InternalBackEndException;
 import com.mymed.controller.core.manager.storage.IStorageManager;
 import com.mymed.model.data.AbstractMBean;
 import com.mymed.properties.IProperties;
 import com.mymed.properties.PropType;
 import com.mymed.properties.PropertiesManager;
 import com.mymed.utils.ClassType;
 import com.mymed.utils.MLogger;
 
 /**
  * Abstract manager the all the managers should extend.
  * <p>
  * This manager provides the basic operation to recreate a bean object.
  * 
  * @author lvanni
  * @author Milo Casagrande
  * 
  */
 public abstract class AbstractManager {
 
   protected IStorageManager storageManager;
   protected static final Logger LOGGER = MLogger.getLogger();
 
   private static final PropertiesManager PROPERTIES = PropertiesManager.getInstance();
   protected static final IProperties GENERAL = PROPERTIES.getManager(PropType.GENERAL);
   protected static final IProperties COLUMNS = PROPERTIES.getManager(PropType.COLUMNS);
   protected static final IProperties ERRORS = PROPERTIES.getManager(PropType.ERRORS);
   protected static final IProperties FIELDS = PROPERTIES.getManager(PropType.FIELDS);
 
   protected static final String ENCODING = GENERAL.get("general.string.encoding");
  protected static final String ERROR_ENCODING = ERRORS.get("error.encodig");
 
   private static final int PRIV = Modifier.PRIVATE;
 
   public AbstractManager(final IStorageManager storageManager) {
     this.storageManager = storageManager;
   }
 
   /**
    * Introspection
    * 
    * @param mbean
    * @param args
    * @return
    * @throws InternalBackEndException
    */
   public AbstractMBean introspection(final AbstractMBean mbean, final Map<byte[], byte[]> args)
       throws InternalBackEndException {
     for (final Entry<byte[], byte[]> arg : args.entrySet()) {
       try {
        final Field field = mbean.getClass().getDeclaredField(new String(arg.getKey(), ENCODING));
 
         /*
          * We check the value of the modifiers of the field: if the field is
          * private and final, or private static and final, we skip it.
          */
         final int modifiers = field.getModifiers();
         if (modifiers != PRIV) {
           continue;
         }
 
         final ClassType classType = ClassType.inferType(field.getGenericType());
         final String setterName = createSetterName(field, classType);
         final Method method = mbean.getClass().getMethod(setterName, classType.getPrimitiveType());
         final Object argument = ClassType.objectFromClassType(classType, arg.getValue());
 
         method.invoke(mbean, argument);
       } catch (final NoSuchFieldException e) {
        try {
          LOGGER.info("WARNING: {} is not a bean field", new String(arg.getKey(), ENCODING));
        } catch (final UnsupportedEncodingException ex) {
          // If we ever get here, there is something seriously wrong.
          // This should never happen.
          LOGGER.info(ERROR_ENCODING, ENCODING);
          LOGGER.debug(ERROR_ENCODING, ENCODING, ex);
        }
       } catch (final SecurityException ex) {
         throw new InternalBackEndException(ex);
       } catch (final NoSuchMethodException ex) {
         throw new InternalBackEndException(ex);
       } catch (final IllegalArgumentException ex) {
         throw new InternalBackEndException(ex);
       } catch (final IllegalAccessException ex) {
         throw new InternalBackEndException(ex);
       } catch (final InvocationTargetException ex) {
         throw new InternalBackEndException(ex);
       } catch (final UnsupportedEncodingException ex) {
         // If we ever get here, there is something seriously wrong.
         // This should never happen.
         LOGGER.info(ERROR_ENCODING, ENCODING);
         LOGGER.debug(ERROR_ENCODING, ENCODING, ex);
       }
     }
 
     return mbean;
   }
   /**
    * Create the name of the setter method based on the field name and its class.
    * <p>
    * This is particularly useful due to the fact that boolean fields does not
    * have a normal setter name.
    * 
    * @param field
    *          the filed we want the setter method of
    * @param classType
    *          the class type of the field
    * @return the name of the setter method
    */
   private String createSetterName(final Field field, final ClassType classType) {
     final StringBuilder setterName = new StringBuilder(20);
     final String fieldName = field.getName();
 
     setterName.append("set");
     String subName = fieldName;
 
     /*
      * Check that the boolean field we are on does start with 'is'. This should
      * be the default prefix for boolean fields. In this case the setter method
      * will be based on the field name, but without the 'is' prefix.
      */
     if (classType.equals(ClassType.BOOL) && fieldName.startsWith("is")) {
       subName = fieldName.substring(2, fieldName.length());
     }
 
     setterName.append(subName.substring(0, 1).toUpperCase(Locale.US));
     setterName.append(subName.substring(1));
     setterName.trimToSize();
 
     return setterName.toString();
   }
 }
