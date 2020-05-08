 /**
  * This file is part of the LDAP Persistence API (LPA).
  *
  * Copyright Trenton D. Adams <lpa at trentonadams daught ca>
  *
  * LPA is free software: you can redistribute it and/or modify it under
  * the terms of the GNU Lesser General Public License as published by the
  * Free Software Foundation, either version 3 of the License, or (at your
  * option) any later version.
  *
  * LPA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public
  * License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public 
  * License along with LPA.  If not, see <http://www.gnu.org/licenses/>.
  *
  * See the COPYING file for more information.
  */
 package ca.tnt.ldaputils.annotations.processing;
 
 import ca.tnt.ldaputils.LdapManager;
 import ca.tnt.ldaputils.annotations.*;
 import ca.tnt.ldaputils.exception.LpaAnnotationException;
 import org.apache.log4j.Logger;
 
 import javax.naming.NamingEnumeration;
 import javax.naming.NamingException;
 import javax.naming.directory.Attribute;
 import javax.naming.directory.Attributes;
 import javax.naming.ldap.LdapName;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.*;
 import java.util.*;
 
 /**
  * {@link IAnnotationHandler} implementation that processes LPA annotations for
  * the purpose of loading the {@link LdapEntity} annotated instance from the
  * ldap query results.
  * <p/>
  * Created :  22-Aug-2010 12:44:56 AM MST
  *
  * @author Trenton D. Adams
  */
 @SuppressWarnings({"ProtectedField"})
 public class LdapEntityLoader extends LdapEntityHandler
 {
     private static final Logger logger = Logger.getLogger(
         LdapEntityLoader.class);
     private boolean isDnSet;
     private Attributes attributes;
     private LdapName dn;
 
     /**
      * does nothing
      */
     public LdapEntityLoader()
     {
     }
 
     /**
      * creates a new LdapEntityHandler with given parameters.
      *
      * @param newObject  the new object to process
      * @param attributes the attributes from ldap
      * @param dn         the ldap distinguished name
      */
     public LdapEntityLoader(final Object newObject,
         final Attributes attributes, final LdapName dn)
     {
         entity = newObject;
         this.attributes = attributes;
         this.dn = dn;
     }
 
     @Override
     public void setManager(final LdapManager managerInstance)
     {
         manager = managerInstance;
     }
 
     @SuppressWarnings({"RefusedBequest"})
     @Override
     protected boolean preProcessAnnotation(final LdapEntity annotation,
         final Class annotatedClass)
     {
         return validateObjectClasses((LdapEntity) annotation, attributes);
     }
 
     @Override
     protected void processLdapAttributes(final Field field)
         throws IllegalAccessException
     {
         field.setAccessible(true);
         field.set(entity, attributes);
         field.setAccessible(false);
     }
 
     @Override
     protected void processDN(final Class annotatedClass, final Field field)
         throws IllegalAccessException, NoSuchMethodException
     {
         field.setAccessible(true);
         field.set(entity, dn.clone());
         field.setAccessible(false);
         isDnSet = validateDN(annotatedClass, field);
     }
 
     /**
      * @throws ClassCastException if we attempt to store an attribute value in
      *                            an unsupported field Class type.  Your
      *                            problem, not ours, until we support {@link
      *                            TypeHandler} for non aggregates
      */
     @SuppressWarnings({"MethodWithMultipleReturnPoints", "unchecked"})
     protected Object processAttribute(final Field field,
         final LdapAttribute attrAnnotation)
         throws NamingException, IllegalAccessException
     {   // BEGIN processAttribute() - simple attribute processing
         // CRITICAL verify what happens if say an image were the
         // attribute value.  It seems like this probably wouldn't work.
         // Apparently sun's implementation only has String or byte array
         // attribute values.  However, other service providers potentially
         // provide other types.  For now, we need to check for "byte[]" as a
         // special case.  Later we can think about what we want to do with other
         // cases
         // http://download.oracle.com/javase/jndi/tutorial/ldap/misc/attrs.html
         //
         // CRITICAL ldap types (issue-16)
         final Class fieldType = field.getType();
         final String attrName = attrAnnotation.name();
         final Attribute attr = attributes.get(attrName);
         final NamingEnumeration attrValues =
             attr != null ? attr.getAll() : null;
         Object fieldValue = field.get(entity);
         if (attr != null)
         {
             if (isMultiValued(fieldType))
             {   // assumed to accept list of ALL attribute values
                 if (fieldValue instanceof Collection)
                 {
                     ((Collection) fieldValue).addAll(Collections.list(
                         attrValues));
                 }
                 else if (fieldValue == null)
                 {
                     throw new NullPointerException(
                         "field \"" + field.getName() +
                             "\" is null; LPA REQUIRES Collection fields of " +
                             "LdapEntity objects to be initialized in their " +
                             "no args constructor.");
                 }
                 else
                 {
                     throw new LpaAnnotationException(
                         "unsupported Class type; we plan on supporting " +
                             "TypeHandler for non aggregates in the future");
                 }
             }
             else
             {   // accepts single valued String attributes attribute
                 fieldValue = attr.get();
             }
         }
         return fieldValue;
     }   // END processAttribute() - simple attribute processing
 
     @Override
     @SuppressWarnings(
         {"unchecked", "MethodWithMultipleReturnPoints", "ReturnOfNull"})
     protected Object processForeignAggregate(final Field field,
         final Class<?> aggClass, final String dnReference,
         final LdapAttribute attrAnnotation)
         throws NamingException, IllegalAccessException
     {
         final String attrName = attrAnnotation.name();
         final Attribute attr = attributes.get(attrName);
         final NamingEnumeration attrValues =
             attr != null ? attr.getAll() : null;
         if (attr == null)
         {   // FEATURE silent failure option (issue-18)
             return null;
         }
 
         final Class fieldType = field.getType();
         Object fieldValue;
         if (fieldType.equals(aggClass))
         {   // field not a collection of any kind, but is a
             // single object type of the aggClass.
             fieldValue = getReferencedEntity(aggClass, dnReference,
                 attr.get());
         }
         else
         {   // BEGIN handling collection of aggregates.
 
             fieldValue = field.get(entity);
             final List ldapEntities = loadAggregates(aggClass,
                 attrValues, dnReference);
 
             if (fieldType.isArray())
             {   // convert to the array type used in the field
                 final Object refArray = Array.newInstance(
                     fieldType.getComponentType(),
                     ldapEntities.size());
                 fieldValue = ldapEntities.toArray(
                     (Object[]) refArray);
             }
             else if (fieldValue instanceof Collection)
             {
                 ((Collection) fieldValue).addAll(ldapEntities);
             }
             else if (fieldValue == null)
             {
                 throw new NullPointerException(
                     "field \"" + field.getName() +
                         "\" is null; LPA REQUIRES Collection fields of " +
                         "LdapEntity annotated objects to be initialized in " +
                         "their no args constructor.");
             }
             else if (entity instanceof TypeHandler)
             {
                 fieldValue = ((TypeHandler) entity).processValues(
                     ldapEntities, fieldType);
             }
             else
             {
                 throw new LpaAnnotationException(
                     "unhandled field type: " + fieldType);
             }
         }   // END handling collection of aggregates.
         return fieldValue;
     }
 
     /**
      * Loads all of the aggregates for the specific attribute values.  This is
      * done by injecting an Rdn escaped value from attrValues into the
      * dnReference.
      *
      * @param aggClass    aggregate class
      * @param attrValues  values of the attribute that is used to reference the
      *                    aggregate
      * @param dnReference the dn reference where the attrValues get injected
      *                    into, to replace the '?'
      *
      * @return a List of all the aggregates retrieved from other LDAP entries.
      *
      * @throws NamingException general JNDI exception wrapper for any errors
      *                         that occur in the directory
      */
     @SuppressWarnings({"unchecked"})
     private List loadAggregates(final Class<?> aggClass,
         final NamingEnumeration attrValues, final String dnReference)
         throws NamingException
     {
         final List ldapEntities = new ArrayList();
         while (attrValues.hasMore())
         {   // iterate through all ldap attributes
             // construct new LdapEntity objects along the way
             final Object valueObject = attrValues.next();
             ldapEntities.add(getReferencedEntity(aggClass,
                 dnReference, valueObject));
         }
         return ldapEntities;
     }
 
     @SuppressWarnings({"ChainedMethodCall"})
     @Override
     public void validateProcessing()
     {
         if (!isDnSet())
         {   // an object in the tree must store the DN
             throw new IllegalArgumentException(
                 entity.getClass().getName() +
                     " is not a valid LdapEntity POJO; @DN annotation " +
                     "REQUIRED");
         }
     }
 
     /**
      * Checks if the distinguished name was set
      *
      * @return true if the distinguished name was processed and set, false
      *         otherwise.
      */
     protected boolean isDnSet()
     {
         return isDnSet;
     }
 
     @Override
     protected Object processLocalAggregate(final Field field,
         final Class<?> aggClass, final LdapAttribute attrAnnotation)
         throws IllegalAccessException, InstantiationException, NamingException
     {
         final Object fieldValue;
         fieldValue = aggClass.newInstance();
         final AnnotationProcessor annotationProcessor =
             new AnnotationProcessor();
         final LdapEntityLoader entityLoader;
         entityLoader = new LdapEntityLoader(fieldValue, attributes,
             dn);
         entityLoader.setManager(manager);
         annotationProcessor.addHandler(entityLoader);
         annotationProcessor.processAnnotations();
 
         return fieldValue;
     }
 }
