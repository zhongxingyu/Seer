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
 package ca.tnt.ldaputils.annotations;
 
 import java.util.List;
 import java.util.SortedSet;
 
 /**
  * Handler interface for handling Class types that the default annotation
  * processor for LPA does not support
  * <p/>
 * MINOR TypeHandler simplification (issue-12)
  * Created :  13-Dec-2010 12:26:00 AM MST
  *
  * @author Trenton D. Adams
  */
 public interface TypeHandler
 {
     /**
      * Processes LdapEntity objects for the given classType.  For example, if we
      * want our data stored in a Map, we would access each object in the list,
      * grab what we think the key should be, use that as the key, and the object
      * itself as the value, and put it into the map.  The field of course would
      * be defined something like <code>Map<String,MyEntity>
      * aggregateField</code>
      * <p/>
      * The type handler MUST handle ALL collection types that {@link LdapEntity}
      * aggregates are stored in, except {@link List}, {@link SortedSet}, or
      * native java arrays.  If it does not, the data simply won't be stored in
      * the object.  The data will then either be whatever your default
      * constructor sets it to, or null. YOUR PROBLEM, not ours. ;)
      *
      * @param list    the data that we need to put into a Collection of some
      *                sort.
      * @param refType the type of the field
      *
      * @return the new collection defined by classType
      */
     Object processValues(List list, Class refType);
 
 
     /**
      * Processes LdapEntity objects for the given classType, and return them in
      * a list, for binding or updating.  For example, if you have a
      * processValues() that stores attribute values in a Map, you need to be
      * able to convert it back to a list, for binding, or updating.
      * <p/>
      * If you are using the aggregate functionality, you MUST convert the
      * attribute data back to it's original type, such as String.
      * <p/>
      * The implementation MUST return a non-null List, whether empty or not;
      * otherwise the annotation processing will throw a NullPointerException
      *
      * @param classType the Class of the field that we're trying to handle.
      * @param refType   the type of the field
      * @param instance  the instance of the field, might be a collection, or
      *                  some other type
      *
      * @return the List of attribute values.
      */
     List getValues(Class classType, Class refType, Object instance);
 }
