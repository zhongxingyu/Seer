 /*
                         QueryJ
 
     Copyright (C) 2002-today  Jose San Leandro Armendariz
                               chous@acm-sl.org
 
     This library is free software; you can redistribute it and/or
     modify it under the terms of the GNU General Public
     License as published by the Free Software Foundation; either
     version 2 of the License, or any later version.
 
     This library is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
     General Public License for more details.
 
     You should have received a copy of the GNU General Public
     License along with this library; if not, write to the Free Software
     Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 
     Thanks to ACM S.L. for distributing this library under the GPL license.
     Contact info: jose.sanleandro@acm-sl.com
 
  *****************************************************************************
  *
  * Filename: CachingAttributeDecorator.java
  *
  * Author: Jose San Leandro Armendariz
  *
  * Description: Adds a simple caching mechanism while decorating Attribute
  *              instances.
  *
  */
 package org.acmsl.queryj.tools.metadata;
 
 /*
  * Importing project classes.
  */
 import org.acmsl.queryj.tools.metadata.vo.Attribute;
 
 /*
  * Importing some JetBrains annotations.
  */
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 
 /**
  * Adds a simple caching mechanism while decorating <code>Attribute</code>
  * instances.
  * @author <a href="mailto:chous@acm-sl.org">Jose San Leandro Armendariz</a>
  */
 public class CachingAttributeDecorator
     extends  AbstractAttributeDecorator
 {
     /**
      * The cached name uppercased.
      */
     private String m__strCachedNameUppercased;
 
     /**
      * The cached capitalized name.
      */
     private String m__strCachedNameCapitalized;
 
     /**
      * The cached uncapitalized name.
      */
     private String m__strCachedNameUncapitalized;
 
     /**
      * The cached lowercased name.
      */
     private String m__strCachedNameLowercased;
 
     /**
      * The cached uncapitalized table name.
      */
     private String m__strCachedUncapitalizedTableName;
 
     /**
      * The cached VO name.
      */
     private String m__strCachedVoName;
 
     /**
      * The cached Java name.
      */
     private String m__strCachedJavaName;
 
     /**
      * The cached getter method.
      */
     private String m__strCachedGetterMethod;
 
     /**
      * The cached <code>isPrimitive</code> value.
      */
     private Boolean m__bCachedIsPrimitive;
 
     /**
      * The cached object type.
      */
     private String m__strCachedObjectType;
 
     /**
      * The cached <code>isClob</code> value.
      */
     private Boolean m__bCachedIsClob;
 
     /**
      * The cached <code>isString</code> value.
      */
     private Boolean m__bCachedIsString;
 
     /**
      * The cached <code>isDate</code> value.
      */
     private Boolean m__bCachedIsDate;
 
     /**
      * The cached query.
      */
     private String m__strCachedQuery;
 
     /**
      * The cached QueryJ field type.
      */
     private String m__strCachedQueryJFieldType;
 
     /**
      * The cached <code>StatementSetter</code> field type.
      */
     private String m__strCachedStatementSetterFieldType;
 
     /**
      * The cached uppercased table name.
      */
     private String m__strCachedTableNameUppercased;
 
     /**
      * The cached normalized and lower-cased table name.
      */
     private String m__strCachedTableNameNormalizedLowercased;
 
     /**
      * Whether the type refers to a number smaller than int.
      */
     private Boolean m__bCachedNumberSmallerThanInt;
 
     /**
      * The cached Java type.
      */
     private String m__strCachedType;
 
     /**
      * Creates a <code>CachingAttributeDecorator</code> with the
      * <code>Attribute</code> to decorate.
      * @param attribute the attribute.
      * @param metadataManager the metadata manager.
      */
     public CachingAttributeDecorator(
         @NotNull final Attribute attribute,
         @NotNull final MetadataManager metadataManager)
     {
         super(attribute, metadataManager);
     }
 
     /**
      * Specifies the name uppercased.
      * @param name such name.
      */
     protected final void immutableSetCachedNameUppercased(
         final String name)
     {
         m__strCachedNameUppercased = name;
     }
 
     /**
      * Specifies the name uppercased.
      * @param name such name.
      */
     protected void setCachedNameUppercased(
         final String name)
     {
         immutableSetCachedNameUppercased(name);
     }
 
     /**
      * Retrieves the name uppercased.
      * @return such value.
      */
     public String getCachedNameUppercased()
     {
         return m__strCachedNameUppercased;
     }
 
     /**
      * Retrieves the name, in upper case.
      * @return such value.
      */
     public String getNameUppercased()
     {
         String result = getCachedNameUppercased();
 
         if  (result == null)
         {
             result = super.getNameUppercased();
             setCachedNameUppercased(result);
         }
 
         return result;
     }
 
     /**
      * Specifies the cached capitalized name.
      * @param name such name.
      */
     protected final void immutableSetCachedNameCapitalized(
         final String name)
     {
         m__strCachedNameCapitalized = name;
     }
 
     /**
      * Specifies the cached capitalized name.
      * @param name such name.
      */
     protected void setCachedNameCapitalized(
         final String name)
     {
         immutableSetCachedNameCapitalized(name);
     }
 
     /**
      * Retrieves the cached capitalized name.
      * @return such name.
      */
     public String getCachedNameCapitalized()
     {
         return m__strCachedNameCapitalized;
     }
 
     /**
      * Retrieves the capitalized name.
      * @return such name.
      */
     public String getNameCapitalized()
     {
         String result = getCachedNameCapitalized();
 
         if  (result == null)
         {
             result = super.getNameCapitalized();
             setCachedNameCapitalized(result);
         }
 
         return result;
     }
 
     /**
      * Specifies the cached uncapitalized name.
      * @param name such name.
      */
     protected final void immutableSetCachedNameUncapitalized(
         final String name)
     {
         m__strCachedNameUncapitalized = name;
     }
 
     /**
      * Specifies the cached uncapitalized name.
      * @param name such name.
      */
     protected void setCachedNameUncapitalized(
         final String name)
     {
         immutableSetCachedNameUncapitalized(name);
     }
 
     /**
      * Retrieves the cached uncapitalized name.
      * @return such name.
      */
     public String getCachedNameUncapitalized()
     {
         return m__strCachedNameUncapitalized;
     }
 
     /**
      * Retrieves the uncapitalized name.
      * @return such name.
      */
     public String getNameUncapitalized()
     {
         String result = getCachedNameUncapitalized();
 
         if  (result == null)
         {
             result = super.getNameUncapitalized();
             setCachedNameUncapitalized(result);
         }
 
         return result;
     }
 
     /**
      * Specifies the cached lowercased name.
      * @param value the value to cache.
      */
     protected final void immutableSetCachedNameLowercased(
         final String value)
     {
         m__strCachedNameLowercased = value;
     }
 
     /**
      * Specifies the cached lowercased name.
      * @param value the value to cache.
      */
     protected void setCachedNameLowercased(final String value)
     {
         immutableSetCachedNameLowercased(value);
     }
 
     /**
      * Retrieves the cached lowercased name.
      * @return such value.
      */
     public String getCachedNameLowercased()
     {
         return m__strCachedNameLowercased;
     }
 
     /**
      * Retrieves the name, in lower case.
      * @return such value.
      */
     public String getNameLowercased()
     {
         String result = getCachedNameLowercased();
 
         if  (result == null)
         {
             result = super.getNameLowercased();
             setCachedNameLowercased(result);
         }
 
         return result;
     }
 
     /**
      * Specifies the cached uncapitalized table name.
      * @param value the value to cache.
      */
     protected final void immutableSetCachedUncapitalizedTableName(
         final String value)
     {
         m__strCachedUncapitalizedTableName = value;
     }
 
     /**
      * Specifies the cached uncapitalized table name.
      * @param value the value to cache.
      */
     protected void setCachedUncapitalizedTableName(final String value)
     {
         immutableSetCachedUncapitalizedTableName(value);
     }
 
     /**
      * Retrieves the cached uncapitalized table name.
      * @return such value.
      */
     public String getCachedUncapitalizedTableName()
     {
         return m__strCachedUncapitalizedTableName;
     }
 
     /**
      * Retrieves the table name, uncapitalized.
      * @return such value.
      */
     public String getUncapitalizedTableName()
     {
         String result = getCachedUncapitalizedTableName();
 
         if  (result == null)
         {
             result = super.getUncapitalizedTableName();
             setCachedUncapitalizedTableName(result);
         }
 
         return result;
     }
 
     /**
      * Specifies the cached VO name.
      * @param value the value to cache.
      */
     protected final void immutableSetCachedVoName(
         final String value)
     {
         m__strCachedVoName = value;
     }
 
     /**
      * Specifies the cached VO name.
      * @param value the value to cache.
      */
     protected void setCachedVoName(final String value)
     {
         immutableSetCachedVoName(value);
     }
 
     /**
      * Retrieves the cached VO name.
      * @return such value.
      */
     public String getCachedVoName()
     {
         return m__strCachedVoName;
     }
 
     /**
      * Retrieves the value-object name associated to the table name.
      * @return such name.
      */
     public String getVoName()
     {
         String result = getCachedVoName();
 
         if  (result == null)
         {
             result = super.getVoName();
             setCachedVoName(result);
         }
 
         return result;
     }
 
     /**
      * Specifies the cached Java name.
      * @param value the value to cache.
      */
     protected final void immutableSetCachedJavaName(
         final String value)
     {
         m__strCachedJavaName = value;
     }
 
     /**
      * Specifies the cached Java name.
      * @param value the value to cache.
      */
     protected void setCachedJavaName(final String value)
     {
         immutableSetCachedJavaName(value);
     }
 
     /**
      * Retrieves the cached Java name.
      * @return such value.
      */
     public String getCachedJavaName()
     {
         return m__strCachedJavaName;
     }
 
     /**
      * Retrieves the attribute's Java name.
      * @return such information.
      */
     public String getJavaName()
     {
         String result = getCachedJavaName();
 
         if  (result == null)
         {
             result = super.getJavaName();
             setCachedJavaName(result);
         }
 
         return result;
     }
 
     /**
      * Specifies the cached getter method.
      * @param value the value to cache.
      */
     protected final void immutableSetCachedGetterMethod(
         final String value)
     {
         m__strCachedGetterMethod = value;
     }
 
     /**
      * Specifies the cached getter method.
      * @param value the value to cache.
      */
     protected void setCachedGetterMethod(final String value)
     {
         immutableSetCachedGetterMethod(value);
     }
 
     /**
      * Retrieves the cached getter method.
      * @return such value.
      */
     public String getCachedGetterMethod()
     {
         return m__strCachedGetterMethod;
     }
 
     /**
      * Retrieves the attribute's associated getter method.
      * @return such information.
      */
     public String getGetterMethod()
     {
         String result = getCachedGetterMethod();
 
         if  (result == null)
         {
             result = super.getGetterMethod();
             setCachedGetterMethod(result);
         }
 
         return result;
     }
 
     /**
      * Specifies the cached <code>isPrimitive</code> value.
      * @param value the value to cache.
      */
     protected final void immutableSetCachedIsPrimitive(
         final Boolean value)
     {
         m__bCachedIsPrimitive = value;
     }
 
     /**
      * Specifies the cached <code>isPrimitive</code> value.
      * @param value the value to cache.
      */
     protected void setCachedIsPrimitive(final Boolean value)
     {
         immutableSetCachedIsPrimitive(value);
     }
 
     /**
      * Retrieves the cached <code>isPrimitive</code> value.
      * @return such value.
      */
     public Boolean getCachedIsPrimitive()
     {
         return m__bCachedIsPrimitive;
     }
 
     /**
      * Retrieves whether this attribute can be modelled as a primitive or not.
      * @return <code>false</code> if no primitive matches.
      */
     public Boolean isPrimitive()
     {
         Boolean result = getCachedIsPrimitive();
 
         if  (result == null)
         {
             result = super.isPrimitive();
             setCachedIsPrimitive(result);
         }
 
         return result;
     }
 
     /**
      * Specifies the cached object type.
      * @param value the value to cache.
      */
     protected final void immutableSetCachedObjectType(
         final String value)
     {
         m__strCachedObjectType = value;
     }
 
     /**
      * Specifies the cached object type.
      * @param value the value to cache.
      */
     protected void setCachedObjectType(final String value)
     {
         immutableSetCachedObjectType(value);
     }
 
     /**
      * Retrieves the cached object type.
      * @return such value.
      */
     public String getCachedObjectType()
     {
         return m__strCachedObjectType;
     }
 
     /**
      * Retrieves the object type.
      * @return such information.
      */
     @Nullable
     public String getObjectType()
     {
         String result = getCachedObjectType();
 
         if  (result == null)
         {
             result = super.getObjectType();
             setCachedObjectType(result);
         }
 
         return result;
     }
 
     /**
      * Specifies the cached <code>isClob</code> value.
      * @param value the value to cache.
      */
     protected final void immutableSetCachedIsClob(
         final Boolean value)
     {
         m__bCachedIsClob = value;
     }
 
     /**
      * Specifies the cached <code>isClob</code> value.
      * @param value the value to cache.
      */
     protected void setCachedIsClob(final Boolean value)
     {
         immutableSetCachedIsClob(value);
     }
 
     /**
      * Retrieves the cached <code>isClob</code> value.
      * @return such value.
      */
     public Boolean getCachedIsClob()
     {
         return m__bCachedIsClob;
     }
     /**
      * Retrieves whether the attribute is a clob or not.
      * return such information.
      */
     public boolean isClob()
     {
         Boolean result = getCachedIsClob();
 
         if  (result == null)
         {
             result = super.isClob() ? Boolean.TRUE : Boolean.FALSE;
             setCachedIsClob(result);
         }
 
         return result;
     }
 
     /**
      * Specifies the cached <code>isString</code> value.
      * @param value the value to cache.
      */
     protected final void immutableSetCachedIsString(
         final Boolean value)
     {
         m__bCachedIsString = value;
     }
 
     /**
      * Specifies the cached <code>isString</code> value.
      * @param value the value to cache.
      */
     protected void setCachedIsString(final Boolean value)
     {
         immutableSetCachedIsString(value);
     }
 
     /**
      * Retrieves the cached <code>isString</code> value.
      * @return such value.
      */
     public Boolean getCachedIsString()
     {
         return m__bCachedIsString;
     }
 
     /**
      * Retrieves whether the attribute is a string or not.
      * return such information.
      */
     public boolean isString()
     {
         Boolean result = getCachedIsString();
 
         if  (result == null)
         {
             result = super.isString() ? Boolean.TRUE : Boolean.FALSE;
             setCachedIsString(result);
         }
 
         return result;
     }
 
     /**
      * Specifies the cached <code>isDate</code> value.
      * @param value the value to cache.
      */
     protected final void immutableSetCachedIsDate(
         final Boolean value)
     {
         m__bCachedIsDate = value;
     }
 
     /**
      * Specifies the cached <code>isDate</code> value.
      * @param value the value to cache.
      */
     protected void setCachedIsDate(final Boolean value)
     {
         immutableSetCachedIsDate(value);
     }
 
     /**
      * Retrieves the cached <code>isDate</code> value.
      * @return such value.
      */
     public Boolean getCachedIsDate()
     {
         return m__bCachedIsDate;
     }
 
     /**
      * Retrieves whether the attribute is a date or not.
      * return such information.
      */
     public boolean isDate()
     {
         Boolean result = getCachedIsDate();
 
         if  (result == null)
         {
             result = super.isDate() ? Boolean.TRUE : Boolean.FALSE;
             setCachedIsDate(result);
         }
 
         return result;
     }
 
     /**
      * Specifies the cached query.
      * @param value the value to cache.
      */
     protected final void immutableSetCachedQuery(
         final String value)
     {
         m__strCachedQuery = value;
     }
 
     /**
      * Specifies the cached query.
      * @param value the value to cache.
      */
     protected void setCachedQuery(final String value)
     {
         immutableSetCachedQuery(value);
     }
 
     /**
      * Retrieves the cached query.
      * @return such value.
      */
     public String getCachedQuery()
     {
         return m__strCachedQuery;
     }
     /**
      * Retrieves the query to retrieve the externally-managed value.
      * @return such information.
      */
     public String getQuery()
     {
         String result = getCachedQuery();
 
         if  (result == null)
         {
             result = super.getQuery();
             setCachedQuery(result);
         }
 
         return result;
     }
 
     /**
      * Specifies the cached QueryJ field type.
      * @param value the value to cache.
      */
     protected final void immutableSetCachedQueryJFieldType(
         final String value)
     {
         m__strCachedQueryJFieldType = value;
     }
 
     /**
      * Specifies the cached QueryJ field type.
      * @param value the value to cache.
      */
     protected void setCachedQueryJFieldType(final String value)
     {
         immutableSetCachedQueryJFieldType(value);
     }
 
     /**
      * Retrieves the cached QueryJ field type.
      * @return such value.
      */
     public String getCachedQueryJFieldType()
     {
         return m__strCachedQueryJFieldType;
     }
 
     /**
      * Retrieves the QueryJ type.
      * @return the QueryJ type.
      */
     public String getQueryJFieldType()
     {
         String result = getCachedQueryJFieldType();
 
         if  (result == null)
         {
             result = super.getQueryJFieldType();
             setCachedQueryJFieldType(result);
         }
 
         return result;
     }
 
     /**
      * Specifies the cached <code>StatementSetter</code> field type.
      * @param value the value to cache.
      */
     protected final void immutableSetCachedStatementSetterFieldType(
         final String value)
     {
         m__strCachedStatementSetterFieldType = value;
     }
 
     /**
      * Specifies the cached <code>StatementSetter</code> field type.
      * @param value the value to cache.
      */
     protected void setCachedStatementSetterFieldType(final String value)
     {
         immutableSetCachedStatementSetterFieldType(value);
     }
 
     /**
      * Retrieves the cached <code>StatementSetter</code> field type.
      * @return such value.
      */
     public String getCachedStatementSetterFieldType()
     {
         return m__strCachedStatementSetterFieldType;
     }
     /**
      * Retrieves the QueryJ type for statement setters.
      * @return the QueryJ type.
      */
     public String getStatementSetterFieldType()
     {
         String result = getCachedStatementSetterFieldType();
 
         if  (result == null)
         {
             result = super.getStatementSetterFieldType();
             setCachedStatementSetterFieldType(result);
         }
 
         return result;
     }
 
     /**
      * Specifies the cached uppercased table name.
      * @param value the value to cache.
      */
     protected final void immutableSetCachedTableNameUppercased(
         final String value)
     {
         m__strCachedTableNameUppercased = value;
     }
 
     /**
      * Specifies the cached uppercased table name.
      * @param value the value to cache.
      */
     protected void setCachedTableNameUppercased(final String value)
     {
         immutableSetCachedTableNameUppercased(value);
     }
 
     /**
      * Retrieves the cached uppercased table name.
      * @return such value.
      */
     public String getCachedTableNameUppercased()
     {
         return m__strCachedTableNameUppercased;
     }
 
     /**
      * Retrieves the attribute's table in upper-case.
      * @return such information.
      */
     public String getTableNameUppercased()
     {
         String result = getCachedTableNameUppercased();
 
         if  (result == null)
         {
             result = super.getTableNameUppercased();
             setCachedTableNameUppercased(result);
         }
 
         return result;
     }
 
     /**
      * Specifies the cached normalized and lower-cased table name.
      * @param value the value to cache.
      */
     protected final void immutableSetCachedTableNameNormalizedLowercased(
         final String value)
     {
         m__strCachedTableNameNormalizedLowercased = value;
     }
 
     /**
      * Specifies the cached normalized and lower-cased table name.
      * @param value the value to cache.
      */
     protected void setCachedTableNameNormalizedLowercased(final String value)
     {
         immutableSetCachedTableNameNormalizedLowercased(value);
     }
 
     /**
      * Retrieves the cached normalized and lower-cased table name.
      * @return such value.
      */
     public String getCachedTableNameNormalizedLowercased()
     {
         return m__strCachedTableNameNormalizedLowercased;
     }
 
     /**
      * Retrieves the attribute's table in upper-case.
      * @return such information.
      */
     public String getTableNameNormalizedLowercased()
     {
         String result = getCachedTableNameNormalizedLowercased();
 
         if  (result == null)
         {
             result = super.getTableNameNormalizedLowercased();
             setCachedTableNameNormalizedLowercased(result);
         }
 
         return result;
     }
 
     /**
      * Specifies whether the type means the attribute is a
      * number smaller than an int.
      * @param flag such condition.
      */
     protected final void immutableSetCachedNumberSmallerThanInt(
         final Boolean flag)
     {
         m__bCachedNumberSmallerThanInt = flag;
     }
 
     /**
      * Specifies whether the type means the attribute is a
      * number smaller than an int.
      * @param flag such condition.
      */
     protected void setCachedNumberSmallerThanInt(
         final Boolean flag)
     {
         immutableSetCachedNumberSmallerThanInt(flag);
     }
 
     /**
      * Retrieves whether the type means the attribute is a
      * number smaller than an int.
      * @return such condition.
      */
     protected Boolean getCachedNumberSmallerThanInt()
     {
         return m__bCachedNumberSmallerThanInt;
     }
 
     /**
      * Retrieves whether the type means the attribute is a
      * number smaller than an int.
      * @return such condition.
      */
     @SuppressWarnings("unused")
     public boolean getNumberSmallerThanInt()
     {
         Boolean result = getCachedNumberSmallerThanInt();
 
         if  (result == null)
         {
             result =
                 (super.isNumberSmallerThanInt())
                 ?  Boolean.TRUE
                 :  Boolean.FALSE;
 
             setCachedNumberSmallerThanInt(result);
         }
 
         return result;
     }
 
     /**
      * Specifies the cached type.
      * @param type such type.
      */
     protected final void immutableSetCachedType(final String type)
     {
         m__strCachedType = type;
     }
 
     /**
      * Specifies the cached type.
      * @param type such type.
      */
     protected void setCachedType(final String type)
     {
         immutableSetCachedType(type);
     }
 
     /**
      * Retrieves the cached type.
      * @return such type.
      */
     public String getCachedType()
     {
         return m__strCachedType;
     }
 
     /**
      * Retrieves the Java type of the property.
      * @return such information.
      */
     @NotNull
     public String getType()
     {
         String result = getCachedType();
 
         if  (result == null)
         {
             result = super.getType();
             setCachedType(result);
         }
 
         return result;
     }
 
     /**
      * Compares this object with the specified object for order.  Returns a
      * negative integer, zero, or a positive integer as this object is less
      * than, equal to, or greater than the specified object.
      * <p/>
      * <p>The implementor must ensure <tt>sgn(x.compareTo(y)) ==
      * -sgn(y.compareTo(x))</tt> for all <tt>x</tt> and <tt>y</tt>.  (This
      * implies that <tt>x.compareTo(y)</tt> must throw an exception iff
      * <tt>y.compareTo(x)</tt> throws an exception.)
      * <p/>
      * <p>The implementor must also ensure that the relation is transitive:
      * <tt>(x.compareTo(y)&gt;0 &amp;&amp; y.compareTo(z)&gt;0)</tt> implies
      * <tt>x.compareTo(z)&gt;0</tt>.
      * <p/>
      * <p>Finally, the implementor must ensure that <tt>x.compareTo(y)==0</tt>
      * implies that <tt>sgn(x.compareTo(z)) == sgn(y.compareTo(z))</tt>, for
      * all <tt>z</tt>.
      * <p/>
      * <p>It is strongly recommended, but <i>not</i> strictly required that
      * <tt>(x.compareTo(y)==0) == (x.equals(y))</tt>.  Generally speaking, any
      * class that implements the <tt>Comparable</tt> interface and violates
      * this condition should clearly indicate this fact.  The recommended
      * language is "Note: this class has a natural ordering that is
      * inconsistent with equals."
      * <p/>
      * <p>In the foregoing description, the notation
      * <tt>sgn(</tt><i>expression</i><tt>)</tt> designates the mathematical
      * <i>signum</i> function, which is defined to return one of <tt>-1</tt>,
      * <tt>0</tt>, or <tt>1</tt> according to whether the value of
      * <i>expression</i> is negative, zero or positive.
      *
      * @param o the object to be compared.
      * @return a negative integer, zero, or a positive integer as this object
      *         is less than, equal to, or greater than the specified object.
      * @throws ClassCastException if the specified object's type prevents it
      *                            from being compared to this object.
      */
     public int compareTo(final Object o)
     {
         int result = 1;
 
         if (   (this == o)
             || (this.equals(o))
             || (getAttribute().equals(o)))
         {
             result = 0;
         }
 
         return result;
     }
 
     @Override
     public boolean equals(final Object o)
     {
         if (this == o)
         {
             return true;
         }
         if (o == null || getClass() != o.getClass())
         {
             return false;
         }
 
         @NotNull final CachingAttributeDecorator that = (CachingAttributeDecorator) o;
 
         if (m__bCachedIsClob != null ? !m__bCachedIsClob.equals(that.m__bCachedIsClob) : that.m__bCachedIsClob != null)
         {
             return false;
         }
         if (m__bCachedIsDate != null ? !m__bCachedIsDate.equals(that.m__bCachedIsDate) : that.m__bCachedIsDate != null)
         {
             return false;
         }
         if (m__bCachedIsPrimitive != null ? !m__bCachedIsPrimitive.equals(that.m__bCachedIsPrimitive)
                                           : that.m__bCachedIsPrimitive != null)
         {
             return false;
         }
         if (m__bCachedIsString != null ? !m__bCachedIsString.equals(that.m__bCachedIsString)
                                        : that.m__bCachedIsString != null)
         {
             return false;
         }
         if (m__bCachedNumberSmallerThanInt != null ? !m__bCachedNumberSmallerThanInt
             .equals(that.m__bCachedNumberSmallerThanInt) : that.m__bCachedNumberSmallerThanInt != null)
         {
             return false;
         }
         if (m__strCachedGetterMethod != null ? !m__strCachedGetterMethod.equals(that.m__strCachedGetterMethod)
                                              : that.m__strCachedGetterMethod != null)
         {
             return false;
         }
         if (m__strCachedJavaName != null ? !m__strCachedJavaName.equals(that.m__strCachedJavaName)
                                          : that.m__strCachedJavaName != null)
         {
             return false;
         }
         if (m__strCachedType != null ? !m__strCachedType.equals(that.m__strCachedType)
                                          : that.m__strCachedType != null)
         {
             return false;
         }
         if (m__strCachedNameCapitalized != null ? !m__strCachedNameCapitalized.equals(that.m__strCachedNameCapitalized)
                                                 : that.m__strCachedNameCapitalized != null)
         {
             return false;
         }
         if (m__strCachedNameLowercased != null ? !m__strCachedNameLowercased.equals(that.m__strCachedNameLowercased)
                                                : that.m__strCachedNameLowercased != null)
         {
             return false;
         }
         if (m__strCachedNameUncapitalized != null ? !m__strCachedNameUncapitalized
             .equals(that.m__strCachedNameUncapitalized) : that.m__strCachedNameUncapitalized != null)
         {
             return false;
         }
         if (m__strCachedNameUppercased != null ? !m__strCachedNameUppercased.equals(that.m__strCachedNameUppercased)
                                                : that.m__strCachedNameUppercased != null)
         {
             return false;
         }
         if (m__strCachedObjectType != null ? !m__strCachedObjectType.equals(that.m__strCachedObjectType)
                                            : that.m__strCachedObjectType != null)
         {
             return false;
         }
         if (m__strCachedQuery != null ? !m__strCachedQuery.equals(that.m__strCachedQuery)
                                       : that.m__strCachedQuery != null)
         {
             return false;
         }
         if (m__strCachedQueryJFieldType != null ? !m__strCachedQueryJFieldType.equals(that.m__strCachedQueryJFieldType)
                                                 : that.m__strCachedQueryJFieldType != null)
         {
             return false;
         }
         if (m__strCachedStatementSetterFieldType != null ? !m__strCachedStatementSetterFieldType
             .equals(that.m__strCachedStatementSetterFieldType) : that.m__strCachedStatementSetterFieldType != null)
         {
             return false;
         }
         if (m__strCachedTableNameNormalizedLowercased != null ? !m__strCachedTableNameNormalizedLowercased
             .equals(that.m__strCachedTableNameNormalizedLowercased)
                                                               : that.m__strCachedTableNameNormalizedLowercased != null)
         {
             return false;
         }
         if (m__strCachedTableNameUppercased != null ? !m__strCachedTableNameUppercased
             .equals(that.m__strCachedTableNameUppercased) : that.m__strCachedTableNameUppercased != null)
         {
             return false;
         }
         return !(m__strCachedUncapitalizedTableName != null ? !m__strCachedUncapitalizedTableName
             .equals(that.m__strCachedUncapitalizedTableName) : that.m__strCachedUncapitalizedTableName != null);
     }
 
     @Override
     public int hashCode()
     {
         int result = m__strCachedNameUppercased != null ? m__strCachedNameUppercased.hashCode() : 0;
         result = 31 * result + (m__strCachedNameCapitalized != null ? m__strCachedNameCapitalized.hashCode() : 0);
         result = 31 * result + (m__strCachedNameUncapitalized != null ? m__strCachedNameUncapitalized.hashCode() : 0);
         result = 31 * result + (m__strCachedNameLowercased != null ? m__strCachedNameLowercased.hashCode() : 0);
         result =
             31 * result + (m__strCachedUncapitalizedTableName != null ? m__strCachedUncapitalizedTableName.hashCode()
                                                                       : 0);
         result = 31 * result + (m__strCachedVoName != null ? m__strCachedVoName.hashCode() : 0);
         result = 31 * result + (m__strCachedJavaName != null ? m__strCachedJavaName.hashCode() : 0);
         result = 31 * result + (m__strCachedGetterMethod != null ? m__strCachedGetterMethod.hashCode() : 0);
         result = 31 * result + (m__bCachedIsPrimitive != null ? m__bCachedIsPrimitive.hashCode() : 0);
         result = 31 * result + (m__strCachedObjectType != null ? m__strCachedObjectType.hashCode() : 0);
         result = 31 * result + (m__bCachedIsClob != null ? m__bCachedIsClob.hashCode() : 0);
         result = 31 * result + (m__bCachedIsString != null ? m__bCachedIsString.hashCode() : 0);
         result = 31 * result + (m__bCachedIsDate != null ? m__bCachedIsDate.hashCode() : 0);
         result = 31 * result + (m__strCachedQuery != null ? m__strCachedQuery.hashCode() : 0);
         result = 31 * result + (m__strCachedQueryJFieldType != null ? m__strCachedQueryJFieldType.hashCode() : 0);
         result =
             31 * result + (m__strCachedStatementSetterFieldType != null ? m__strCachedStatementSetterFieldType
                 .hashCode()
                                                                         : 0);
         result =
             31 * result + (m__strCachedTableNameUppercased != null ? m__strCachedTableNameUppercased.hashCode() : 0);
         result =
             31 * result + (m__strCachedTableNameNormalizedLowercased != null ? m__strCachedTableNameNormalizedLowercased
                 .hashCode() : 0);
         result = 31 * result + (m__bCachedNumberSmallerThanInt != null ? m__bCachedNumberSmallerThanInt.hashCode() : 0);
         result = 31 * result + (m__strCachedType != null ? m__strCachedType.hashCode() : 0);
         return result;
     }
 }
