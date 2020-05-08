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
 import ca.tnt.ldaputils.annotations.DN;
 import ca.tnt.ldaputils.annotations.LdapAttribute;
 import ca.tnt.ldaputils.annotations.LdapEntity;
 import ca.tnt.ldaputils.annotations.Manager;
 import ca.tnt.ldaputils.annotations.TypeHandler;
 import ca.tnt.ldaputils.exception.LdapNamingException;
 import ca.tnt.ldaputils.exception.LpaAnnotationException;
 import org.apache.log4j.Logger;
 
 import javax.naming.InvalidNameException;
 import javax.naming.NamingException;
 import javax.naming.directory.Attribute;
 import javax.naming.directory.Attributes;
 import javax.naming.ldap.LdapName;
 import javax.naming.ldap.Rdn;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.util.List;
 
 /**
  * {@link IAnnotationHandler} abstract implementation that processes LPA
  * annotations for the purpose of providing a base Class for other {@link
  * LdapEntity} annotation processing handlers.
  * <p/>
  * This class was engineered in such a way; wait, back the truck up just a bit
  * (hehe). Let's face it, this class was HACKED together in such a way that
  * allows subclassing; DOC more about that later.
  * <p/>
  * Created :  03/01/11 1:38 AM MST
  *
  * @author Trenton D. Adams
  */
 public abstract class LdapEntityHandler implements IAnnotationHandler
 {
     private static final Logger logger = Logger.getLogger(
         LdapEntityHandler.class);
     /**
      * The {@link LdapEntity} annotated object instance.
      */
     protected Object entity;
 
     /**
      * Manager instance that is calling the handler
      */
     protected LdapManager manager;
 
     /**
      * Simply determines if this is a multi valued field.  We assume it is if it
      * is not either a String or a byte array.  These are the only supported
      * return types for attribute values from Sun's LDAP provider; therefore if
      * someone has declared something else, it must be a collection of some
      * sort, or their own type
      * <p/>
      * REQUIRED_FEATURE support other LDAP providers (issue-7) We'll have to
      * figure something else out if we want to work with other LDAP JNDI service
      * providers in the future.
      *
      * @param fieldType the field's type
      *
      * @return true if it is a multi valued field, false otherwise
      */
     protected static boolean isMultiValued(final Class fieldType)
     {
         return !String.class.equals(fieldType) &&
             !(byte.class.equals(fieldType) && fieldType.isArray());
     }
 
     /**
      * <span style="color:red;">WARNING! WARNING! WARNING!</span> It is not
      * recommended that you override this method.  It makes the basic calls to
      * all the {@link LdapEntity} related annotation methods, such as {@link
      * #processManager}, {@link #processDN}, and {@link #processLdapAttribute},
      *
      * @param annotation     the annotation
      * @param annotatedClass the field to process
      *
      * @return true if annotation processing was successful
      */
     @SuppressWarnings({"ChainedMethodCall", "MethodWithMultipleReturnPoints"})
     @Override
     public boolean processAnnotation(final Annotation annotation,
         final Class annotatedClass)
     {
         try
         {
             final String className = entity.getClass().getName();
 
             if (!preProcessAnnotation((LdapEntity) annotation, annotatedClass))
             {
                 return false;
             }
 
             for (final Field field : annotatedClass.getDeclaredFields())
             {   // BEGIN field iteration
                 if (field.isAnnotationPresent(Manager.class))
                 {
                     processManager(field);
                 }
 
                 if (field.isAnnotationPresent(DN.class))
                 {
                     logger.debug(String.format("%-20s annotation on ", "@DN") +
                         className + ':' + field.getName());
                     processDN(annotatedClass, field);
                 }
 
                 if (field.isAnnotationPresent(LdapAttribute.class))
                 {   // BEGIN LdapAttribute annotation processing
                     logger.debug(String.format("%-20s annotation on ",
                         "@LdapAttribute") + className + ':' + field.getName());
                     processLdapAttribute(annotatedClass, field);
                 }   // END LdapAttribute annotation processing
             }   // END field iteration
 
         }
         catch (NamingException e)
         {
             throw new LdapNamingException(e);
         }
         catch (IllegalAccessException e)
         {
             throw new IllegalArgumentException(e);
         }
         catch (NoSuchMethodException e)
         {
             throw new IllegalArgumentException(annotatedClass.getName() +
                 " is not a valid LdapEntity POJO; method incorrectly defined " +
                 "or does not exist", e);
         }
         catch (InstantiationException e)
         {
             throw new IllegalArgumentException(e);
         }
         catch (InvocationTargetException e)
         {
             throw new IllegalArgumentException(annotatedClass.getName() +
                 " is not a valid LdapEntity POJO; method incorrectly defined",
                 e);
         }
 
         return true;
     }
 
     /**
      * Does annotation pre-processing, in case the subclass wants to do
      * something special before anything is processed.  We implement the default
      * here, which is to return true.
      *
      * @param annotation     the annotation being processed
      * @param annotatedClass the annotated class with the annotation
      *
      * @return true if nothing went wrong during processing
      */
     protected boolean preProcessAnnotation(final LdapEntity annotation,
         final Class annotatedClass)
     {
         return true;
     }
 
     /**
      * Validates object classes for the entity.
      * <p/>
      * CRITICAL is this needed in the abstract class?  If so, I'm thinking it
      * should be abstract?
      * <p/>
      * Override for a subclassed annotation processor.  In some cases, just
      * return true for the subclass, if you have nothing to do.
      *
      * @param annotation the {@link LdapEntity} annotation instance for the
      *                   class
      * @param attributes the attributes to validate against
      *
      * @return whether the object classes are valid or not
      */
     @SuppressWarnings({"MethodMayBeStatic"})
     protected boolean validateObjectClasses(final LdapEntity annotation,
         final Attributes attributes)
     {
         final Attribute objectClass = attributes.get("objectClass");
         final String[] supportedClasses =
             ((LdapEntity) annotation).requiredObjectClasses();
         boolean hasSupportedClass = true;
         for (final String supportedClass : supportedClasses)
         {
             hasSupportedClass = hasSupportedClass && objectClass.contains(
                 supportedClass);
         }
         return hasSupportedClass;
     }
 
     /**
      * Handles injecting the manager.
      * <p/>
      * CRITICAL - okay, bad code, probably shouldn't be assuming manager is
      * available, when no constructors are called with it.
      *
      * @param field the field to inject the manager to
      *
      * @throws IllegalAccessException if java policies prevent access to fields
      *                                via reflection
      */
     protected void processManager(final Field field)
         throws IllegalAccessException
     {
         field.setAccessible(true);
         field.set(entity, manager);
         field.setAccessible(false);
     }
 
     /**
      * Processing for {@link LdapAttribute } annotation where name is '*'
      *
      * @param field the field the annotation is on
      *
      * @throws IllegalAccessException if java policies prevent access to fields
      *                                via reflection
      */
     protected abstract void processLdapAttributes(Field field)
         throws IllegalAccessException;
 
     /**
      * Processing for {@link DN } annotation
      *
      * @param annotatedClass class of the annotated object
      * @param field          the field the annotation is on
      *
      * @throws IllegalAccessException if java policies prevent access to fields
      *                                via reflection
      * @throws NoSuchMethodException  if the {@link LdapAttribute#referencedDNMethod()}
      *                                is the name of a method that does not
      *                                exist
      */
     protected abstract void processDN(Class annotatedClass, Field field)
         throws IllegalAccessException, NoSuchMethodException;
 
     /**
      * <span style="color:red;">WARNING! WARNING! WARNING!</span> It is not
      * recommended that you override this method.  In fact, we may make it so
      * that you can't in the future.  You've been warned!
      * <p/>
      * Validates that the class is annotated with {@link DN}, and that the
      * property getter is available.
      *
      * @param annotatedClass the {@link LdapEntity} annotated class
      * @param field          the field annotated with DN
      *
      * @return true if the DN validation was successful
      *
      * @throws NoSuchMethodException if the {@link LdapAttribute#referencedDNMethod()}
      *                               is the name of a method that does not
      *                               exist
      */
     @SuppressWarnings({"MethodMayBeStatic"})
     protected boolean validateDN(final Class annotatedClass, final Field field)
         throws NoSuchMethodException
     {
         final String methodName;    // final name of method
         final String fieldName;
         final String firstChar;     // first char of field name
         final String endOfField;    // field name minus first character
         final Method dnGetMethod;
 
         fieldName = field.getName();
         firstChar = fieldName.substring(0, 1);
         endOfField = fieldName.length() > 1 ? fieldName.substring(1) : "";
         methodName = "get" + firstChar.toUpperCase() + endOfField;
         dnGetMethod = annotatedClass.getMethod(methodName);
         if (!Modifier.isPublic(dnGetMethod.getModifiers()) ||
             !LdapName.class.equals(dnGetMethod.getReturnType()))
         {   // not defined to return an LdapName or not public
             throw new NoSuchMethodException(methodName +
                 " is not defined correctly.  It must be a public " +
                 "no args method, returning an LdapName");
         }
 
         return true;
     }
 
     /**
      * Only to be used if the field needs to be injected.  If you're doing
      * read-only processing for your subclass of LdapEntityLoader, then override
      * this, and have it do nothing.
      *
      * @param field      the field
      * @param fieldValue the value of the field to inject
      *
      * @throws IllegalAccessException if java policies prevent access to fields
      *                                via reflection
      */
     private void injectField(final Field field, final Object fieldValue)
         throws IllegalAccessException
     {
         if (fieldValue != null)
         {   // never set to null, as the contructor may have initialized
             // a default empty collection or something.
             field.set(entity, fieldValue);
         }
     }
 
     /**
      * <span style="color:red;">WARNING! WARNING! WARNING!</span> It is not
      * recommended that you override this method.  We do checks to determine
      * what is classified as a regular attribute field vs an aggregatized one
      * (I'm making a new word, leave me alone), and call either {@link
      * #processAttribute(Field, LdapAttribute)} or {@link
      * #processAggregate(Field, Class, Class, LdapAttribute)}. We also handle
      * the field injection.
      * <p/>
      * Processing for {@link LdapAttribute } annotation.
      * <p/>
      * DOC move this to the appropriate subclass if needed, when we're done.
      * <p/>
      * If the LdapEntity annotated Class is an instanceof {@link TypeHandler},
      * the {@link TypeHandler#processValues(List, Class)} will be called for all
      * aggregate fields, instead of the normal type processing that goes on,
      * described by {@link LdapAttribute}
      * <p/>
      *
      * @param annotatedClass the class of the annotated object
      * @param field          the field the annotation is on
      *
      * @throws IllegalAccessException    if java policies prevent access to
      *                                   fields via reflection
      * @throws InvocationTargetException an exception occured during the call to
      *                                   the method defined by {@link
      *                                   LdapAttribute#referencedDNMethod()}
      * @throws NoSuchMethodException     if the {@link LdapAttribute#referencedDNMethod()}
      *                                   is the name of a method that does not
      *                                   exist
      * @throws NamingException           general JNDI exception wrapper for any
      *                                   errors that occur in the directory
      * @throws InstantiationException    if an error occurs creating an
      *                                   aggregate instance
      * @throws ClassCastException        if you use a supported collection that
      *                                   is not parameterized, such as
      *                                   SortedSet, instead of SortedSet&lt;String&gt;,
      *                                   for example.
      */
     @SuppressWarnings({"MethodWithMultipleReturnPoints", "unchecked"})
     protected void processLdapAttribute(final Class annotatedClass,
         final Field field) throws IllegalAccessException, NamingException,
         InvocationTargetException, NoSuchMethodException, InstantiationException
     {   // BEGIN processLdapAttribute()
         field.setAccessible(true);
 
         Object fieldValue = null;
         final LdapAttribute attrAnnotation = field.getAnnotation(
             LdapAttribute.class);
         if ("*".equals(attrAnnotation.name()))
         {   // all attributes stored in field
             processLdapAttributes(field);
             return;
         }
         final Class<?> aggClass = attrAnnotation.aggregateClass();
 
         try
         {
             final boolean isAggregate = !Object.class.equals(aggClass);
             if (!isAggregate)
             {   // regular string or byte array attributes in a collection, or
                 // by themselves.
                 fieldValue = processAttribute(field, attrAnnotation);
             }
             else
             {   // an aggregate, attribut must be a string
                 fieldValue = processAggregate(field, annotatedClass,
                     aggClass, attrAnnotation);
             }
 
             injectField(field, fieldValue); // may do nothing
         }
         finally
         {   // reset java language checks
             field.setAccessible(false);
         }
     }   // END processLdapAttribute()
 
     /**
      * Simple attribute processing.  Called when we're doing with attributes
      * only, and not aggregates.
      * <p/>
      * Note, the object returned should be null if you are not attempting to
      * inject anything into this field.  That may be the case if you are
      * implementing a handler to "read" from the instance, rather than write to
      * it.
      *
      * @param field          the field being processed
      * @param attrAnnotation the {@link LdapAttribute} annotation instance being
      *                       processed
      *
      * @return the value for the field
      *
      * @throws NamingException        general JNDI exception wrapper for any
      *                                errors that occur in the directory
      * @throws IllegalAccessException if java policies prevent access to fields
      *                                via reflection
      */
     @SuppressWarnings({"MethodWithMultipleReturnPoints", "unchecked"})
     protected abstract Object processAttribute(Field field,
         LdapAttribute attrAnnotation)
         throws NamingException, IllegalAccessException;
 
     /**
      * <span style="color:red;">WARNING! WARNING! WARNING!</span> It is not
      * recommended that you override this method.  We do checks to determine if
      * we need to process a local or foreign aggregate, and validate the dn for
      * the foreign aggregates.
      * <p/>
      * Process aggregates, and call the {@link #processForeignAggregate(Field,
      * Class, String, LdapAttribute)} or the {@link #processLocalAggregate(Field,
      * Class, LdapAttribute)} as appropriate.
      *
      * @param field          the field being processed
      * @param annotatedClass the annotated {@link LdapEntity} class
      * @param aggClass       the aggregate class as defined by {@link
      *                       LdapAttribute#aggregateClass()}
      * @param attrAnnotation the {@link LdapAttribute} annotation instance being
      *                       processed
      *
      * @return the aggregate instance
      *
      * @throws IllegalAccessException    if java policies prevent access to
      *                                   fields via reflection
      * @throws InstantiationException    if an error occurs creating an
      *                                   aggregate instance
      * @throws NoSuchMethodException     if the {@link LdapAttribute#referencedDNMethod()}
      *                                   is the name of a method that does not
      *                                   exist
      * @throws InvocationTargetException an exception occured during the call to
      *                                   the method defined by {@link
      *                                   LdapAttribute#referencedDNMethod()}
      * @throws NamingException           general JNDI exception wrapper for any
      *                                   errors that occur in the directory
      */
     @SuppressWarnings({"MethodWithMultipleReturnPoints", "unchecked"})
     protected Object processAggregate(final Field field,
         final Class annotatedClass,
         final Class<?> aggClass,
         final LdapAttribute attrAnnotation)
         throws InstantiationException, IllegalAccessException,
         NoSuchMethodException, InvocationTargetException, NamingException
     {
         final Object fieldValue;
         // request to inject an LdapEntity from another LDAP entry
         // or use the existing ldap entry to grab Auxiliary attributes
 
         // local aggregates are loaded from the current ldap entry
         final String referenceDNMethod = attrAnnotation.referencedDNMethod();
         final String referenceDN = attrAnnotation.referencedDN();
         final boolean isLocalAggregate = "".equals(referenceDNMethod) &&
             "".equals(referenceDN);
         if (!"".equals(referenceDNMethod) && !"".equals(referenceDN))
         {
             throw new LpaAnnotationException(
                 "invalid @LdapAttribute item, cannot use referenceDNMethod " +
                     "and referenceDN at the same time");
         }
 
         if (isLocalAggregate)
         {   // use current ldap entry for population of aggregate
             fieldValue = processLocalAggregate(field, aggClass, attrAnnotation);
         }
         else
         {   // BEGIN foreign ldap entry processing for aggregate
             String dnReference;
 
             if (!"".equals(referenceDNMethod))
             {
                 final Method getDnReference = annotatedClass.getMethod(
                     referenceDNMethod);
                 dnReference = (String) getDnReference.invoke(entity);
             }
             else
             {
                 if (referenceDN.matches("^\\$\\{(.*)\\}$"))
                 {
                     dnReference = referenceDN.replaceAll("^\\$\\{(.*)\\}$", "$1");
                     // The reference is in ${property.name} syntax
                     dnReference = manager.getProperty(dnReference);
                 }
                 else
                 {
                     dnReference = referenceDN;
                 }
             }
 
             if (!dnReference.contains("?"))
             {
                 throw new LpaAnnotationException(dnReference +
                     " is an invalid dynamic reference to an LDAP entry, " +
                     "it does not contain a replaceable parameter marked " +
                     "with '?'");
             }
 
             fieldValue = processForeignAggregate(field, aggClass,
                 dnReference, attrAnnotation);
         }   // END foreign ldap entry processing for aggregate
         return fieldValue;
     }
 
     /**
      * Retrieves the referenced dn, given the dnReference (see {@link
      * LdapAttribute#referencedDNMethod()} for more information) and the
      * attributeValue, and creates a new LdapEntity object.
      *
      * @param entityClass    the java class of the LdapEntity
      * @param dnReference    the dn reference with a '?' bind parameter
      * @param attributeValue the value of the attribute to escape, and inject
      *                       into dnReference before conversion to an LdapName
      *
      * @return the LdapEntity object
      *
      * @throws InvalidNameException if an error occurs creating the LdapName.
      */
     protected Object getReferencedEntity(final Class entityClass,
         final String dnReference, final Object attributeValue)
         throws InvalidNameException
     {
         final String dnLocalReference = dnReference.replace("?",
             Rdn.escapeValue(attributeValue));
         final LdapName ldapName = new LdapName(dnLocalReference);
         return manager.find(entityClass, ldapName);
     }
 
     @Override
     public Class getAnnotatedClass()
     {
         return entity.getClass();
     }
 
     @Override
     public Class<? extends Annotation> getAnnotationClass()
     {
         return LdapEntity.class;
     }
 
     @Override
     public void noAnnotation(final Class annotatedClass)
     {
         // is this type of comparison technically correct?  I'm not sure
         // how the class loader works, and whether it is always the case that
         // a single Class instance is loaded or not.  If only one instance is
         // loaded, this is certainly the fastest way of comparing them.
         if (annotatedClass == entity.getClass())
         {   // top level class required to be annotated.
             throw new IllegalArgumentException(
                 annotatedClass.getName() +
                     " is not a valid LdapEntity POJO; @LdapEntity" +
                     " annotation REQUIRED");
         }
     }
 
     /**
      * Called to process a foreign aggregate.  It's up to the subclass to do
      * what it likes.  Returning null indicates the subclass does not want
      * anything stored in the {@link LdapEntity} annotated instance, but is
      * instead reading from the instance.
      * <p/>
      * Tip on implementing: aggregates are stored in one of two ways. <ol> <li>
      * directly into an aggregate object, if you know that it's a single valued
      * attribute, or your business practise makes it always single valued.</li>
      * <li>in a collection of aggregate objects, of some sort.  e.g.
      * <code>SortedSet&lt;MyAggregateClass&gt; myAggregates</code></li> </ol>
      * Therefore, the easiest way to determine if it's a single aggregate, is to
      * check the field type, against the {@link LdapAttribute#aggregateClass()}
      * by asking if they are equal.  See the {@link LdapEntityLoader} source for
      * an example.
      *
      * @param field          the field being processed
      * @param aggClass       the aggregate class as defined by {@link
      *                       LdapAttribute#aggregateClass()}
      * @param dnReference    the "properly" formatted dn, with bind parameter,
      *                       as returned by the {@link LdapAttribute#referencedDNMethod()}
      *                       method
      * @param attrAnnotation the {@link LdapAttribute} annotation instance being
      *                       processed
      *
      * @return the new aggregate instance, or collection of aggregate instances;
      *         subclasses may also return null
      *
      * @throws NamingException        general JNDI exception wrapper for any
      *                                errors that occur in the directory
      * @throws IllegalAccessException if java policies prevent access to fields
      *                                via reflection
      */
     @SuppressWarnings({"unchecked"})
     protected abstract Object processForeignAggregate(Field field,
         Class<?> aggClass, String dnReference, LdapAttribute attrAnnotation)
         throws NamingException, IllegalAccessException;
 
     /**
      * Do what you need to for the local aggregate. Returning null indicates the
      * subclass does not want anything stored in the {@link LdapEntity}
      * annotated instance, but is instead reading from the instance.
      * <p/>
      * A local aggregate is an aggregate object which will be injected into the
      * object field, which has requested it via {@link LdapAttribute#aggregateClass()},
      * and is also using the existing LDAP entry's attributes as a basis for the
      * object.  See the documentation on {@link LdapAttribute#aggregateClass()}
      * for more information.
      * <p/>
      *
      * @param field          the field being processed
      * @param aggClass       the aggregate class, if needed.
      * @param attrAnnotation the {@link LdapAttribute} annotation instance being
      *                       processed
      *
      * @return the new fieldValue if one is needed; subclasses may also return
      *         null
      *
      * @throws IllegalAccessException if java policies prevent access to fields
      *                                via reflection
      * @throws InstantiationException if an error occurs creating an aggregate
      *                                instance
      * @throws NamingException        general JNDI exception wrapper for any
      *                                errors that occur in the directory
      */
     protected abstract Object processLocalAggregate(Field field,
         Class<?> aggClass, LdapAttribute attrAnnotation)
         throws IllegalAccessException, InstantiationException, NamingException;
 }
