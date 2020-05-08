 /**
  * <copyright>
  *
  * Copyright (c) 2012 Springsite BV (The Netherlands) and others
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   Martin Taal - Initial API and implementation
  *
  * </copyright>
  *
  * $Id: ModelEMFConverter.java,v 1.27 2011/09/14 15:35:53 mtaal Exp $
  */
 
 package org.eclipse.emf.texo.json;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import org.eclipse.emf.ecore.EDataType;
 import org.eclipse.emf.ecore.EEnum;
 import org.eclipse.emf.ecore.EEnumLiteral;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.xml.type.XMLTypeFactory;
 import org.eclipse.emf.ecore.xml.type.XMLTypePackage;
 import org.eclipse.emf.texo.component.TexoComponent;
 import org.eclipse.emf.texo.model.ModelPackage;
 import org.eclipse.emf.texo.model.ModelResolver;
 import org.eclipse.emf.texo.utils.ModelUtils;
 
 /**
  * Is used to convert model primitive values to JSON primitive values.
  * 
  * @author <a href="mtaal@elver.org">Martin Taal</a>
  */
 public class JSONValueConverter implements TexoComponent {
 
   private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
   private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ssZZZZZ");
   private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ");
 
   /**
    * Converts a primitive type value to a format acceptable by JSON. Only handles these ones:
    * <p>
    * EEnum: For EEnum: uses the name
    * </p>
    * <p>
    * Date: converts to a xml schema format
    * </p>
    * 
    * @param value
    *          the value to convert
    * @param eDataType
    *          its EDataType
    * @return the converted value
    */
   protected Object toJSON(Object target, final Object value, final EDataType eDataType) {
     if (target instanceof EObject && ModelUtils.isEEnum(eDataType)) {
       return eDataType.getEPackage().getEFactoryInstance().convertToString(eDataType, value);
     }
 
     if (value instanceof Enum<?>) {
       return ((Enum<?>) value).name();
     }
 
     if (usePlainDate()) {
       if (value instanceof Date && eDataType == XMLTypePackage.eINSTANCE.getDate()) {
         return convertDateToJSON(value);
       }
 
       if (value instanceof Date && eDataType == XMLTypePackage.eINSTANCE.getDateTime()) {
         return convertDateTimeToJSON(value);
       }
 
       if (value instanceof Date && eDataType == XMLTypePackage.eINSTANCE.getTime()) {
         return convertTimeToJSON(value);
       }
     }
 
     if (value instanceof Date) {
       return convertDateTimeToJSON(value);
     }
 
     if (eDataType.getEPackage() == XMLTypePackage.eINSTANCE) {
       return XMLTypeFactory.eINSTANCE.convertToString(eDataType, value);
     }
 
     return value;
   }
 
   /**
    * Return true it the {@link Date} type should be used for DateTime values.
    */
   protected boolean usePlainDate() {
     return true;
   }
 
   public String convertDateToJSON(Object value) {
     return dateFormat.format((Date) value);
   }
 
   public String convertDateTimeToJSON(Object value) {
     return dateTimeFormat.format((Date) value);
   }
 
   public String convertTimeToJSON(Object value) {
     return timeFormat.format((Date) value);
   }
 
   /**
    * Converts a primitive type value, this implementation only converts an EEnum to an Enum value.
    * 
    * @param value
    *          the value to convert
    * @param eDataType
    *          its EDataType
    * @return the converted value
    */
   @SuppressWarnings({ "unchecked", "rawtypes" })
   protected Object fromJSON(Object target, final Object value, final EDataType eDataType) {
     if (ModelUtils.isEEnum(eDataType)) {
       final EDataType enumDataType = getDataTypeOrBaseType(eDataType);
      final EEnum eeNum = (EEnum) eDataType;
 
       if (!(value instanceof String)) {
         // hopefully already the correct value...
         return value;
       }
 
       if (target instanceof EObject) {
         for (EEnumLiteral eeNumLiteral : eeNum.getELiterals()) {
           if (eeNumLiteral.getName().compareToIgnoreCase((String) value) == 0
               || eeNumLiteral.getLiteral().compareToIgnoreCase((String) value) == 0) {
             return eDataType.getEPackage().getEFactoryInstance().createFromString(eDataType, eeNumLiteral.getLiteral());
           }
         }
         return eDataType.getEPackage().getEFactoryInstance().createFromString(eDataType, (String) value);
       }
 
       // modelobject
       final ModelPackage modelPackage = ModelResolver.getInstance().getModelPackage(
           enumDataType.getEPackage().getNsURI());
       if (modelPackage == null) {
         // dynamic model
         EEnumLiteral literal = eeNum.getEEnumLiteral((String) value);
         if (literal == null) {
           literal = eeNum.getEEnumLiteralByLiteral((String) value);
         }
         return literal;
       }
       final Class<? extends Enum> enumClass = (Class<? extends Enum>) modelPackage.getEClassifierClass(enumDataType);
       return Enum.valueOf(enumClass, ((String) value).toUpperCase());
     }
 
     if (usePlainDate()) {
       if (eDataType == XMLTypePackage.eINSTANCE.getDate()) {
         return createDateFromJSON(value);
       }
 
       if (eDataType == XMLTypePackage.eINSTANCE.getDateTime()) {
         return createDateTimeFromJSON(value);
       }
 
       if (eDataType == XMLTypePackage.eINSTANCE.getTime()) {
         return createTimeFromJSON(value);
       }
     }
 
 
     if (eDataType.getInstanceClass() != null && Date.class.isAssignableFrom(eDataType.getInstanceClass())) {
       return createDateTimeFromJSON(value);
     }
 
     if (value instanceof Integer) {
       // cast to the correct number type
       if (eDataType.getInstanceClass() == long.class || eDataType.getInstanceClass() == Long.class) {
         return ((Integer) value).longValue();
       }
       if (eDataType.getInstanceClass() == byte.class || eDataType.getInstanceClass() == Byte.class) {
         return ((Integer) value).byteValue();
       }
       if (eDataType.getInstanceClass() == short.class || eDataType.getInstanceClass() == Short.class) {
         return ((Integer) value).shortValue();
       }
       if (eDataType.getInstanceClass() == double.class || eDataType.getInstanceClass() == Double.class) {
         return ((Integer) value).doubleValue();
       }
       if (eDataType.getInstanceClass() == float.class || eDataType.getInstanceClass() == Float.class) {
         return ((Integer) value).floatValue();
       }
     }
 
     if (value instanceof String && eDataType.getEPackage() == XMLTypePackage.eINSTANCE) {
       return XMLTypeFactory.eINSTANCE.createFromString(eDataType, (String) value);
     }
 
     return value;
   }
 
   public Date createDateTimeFromJSON(Object value) {
     try {
       return dateTimeFormat.parse((String) value);
     } catch (ParseException e) {
       throw new RuntimeException(e);
     }
   }
 
   public Date createDateFromJSON(Object value) {
     try {
       return dateFormat.parse((String) value);
     } catch (ParseException e) {
       throw new RuntimeException(e);
     }
   }
 
   public Date createTimeFromJSON(Object value) {
     try {
       return timeFormat.parse((String) value);
     } catch (ParseException e) {
       throw new RuntimeException(e);
     }
   }
 
   /**
    * See the javadoc in the {@link ModelUtils#getEnumBaseDataTypeIfObject(EDataType)} for details.
    * 
    * @param eDataType
    * @return the passed EDataType or its base type if the base type is an EEnum
    */
   private EDataType getDataTypeOrBaseType(EDataType eDataType) {
     final EDataType baseType = ModelUtils.getEnumBaseDataTypeIfObject(eDataType);
     if (baseType != null) {
       return baseType;
     }
     return eDataType;
   }
 
 }
