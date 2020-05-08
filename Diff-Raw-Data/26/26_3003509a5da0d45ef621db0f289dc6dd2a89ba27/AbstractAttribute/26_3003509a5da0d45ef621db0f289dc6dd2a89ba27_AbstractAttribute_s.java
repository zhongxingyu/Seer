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
     Contact info: chous@acm-sl.org
     Postal Address: c/Playa de Lagoa, 1
                     Urb. Valdecabanas
                     Boadilla del monte
                     28660 Madrid
                     Spain
 
  *****************************************************************************
  *
  * Filename: AbstractAttribute.java
  *
  * Author: Jose San Leandro Armendariz
  *
  * Description: Abstract logic-less implementation of Attribute interface.
  *
  */
 package org.acmsl.queryj.metadata.vo;
 
 /*
  * Importing some Apache Commons-Lang classes.
  */
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 
 /*
  * Importing some JetBrains annotations.
  */
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 
 /**
  * Abstract logic-less implementation of {@link Attribute} interface.
  * @author <a href="mailto:chous@acm-sl.org"
  *         >Jose San Leandro</a>
  */
 public abstract class AbstractAttribute<T>
     implements Attribute<T>
 {
     /**
      * The name.
      */
     private T m__strName;
 
     /**
      * The type.
      */
     private int m__iTypeId;
 
     /**
      * The type.
      */
     private T m__strType;
 
     /**
      * The table name.
      */
     private T m__strTableName;
 
     /**
      * The comment.
      */
     private T m__strComment;
 
     /**
      * The keyword user to retrieve the value, if any.
      */
     private T m__strKeyword;
 
     /**
      * The query user to retrieve the value, if any.
      */
     private T m__strRetrievalQuery;
 
     /**
      * Whether the attribute allows null values or not.
      */
     private boolean m__bNullable = false;
 
     /**
      * The optional value.
      */
     private T m__strValue;
 
     /**
      * Whether the attribute is marked as read-only.
      */
     private boolean m__bReadOnly = false;
 
     /**
      * Whether the attribute is marked as boolean.
      */
     private boolean m__bBoolean = false;
 
     /**
      * The symbol for <code>true</code> values in boolean attributes.
      */
     private T m__strBooleanTrue;
 
     /**
      * The symbol for <code>false</code> values in boolean attributes.
      */
     private T m__strBooleanFalse;
 
     /**
      * The symbol for <code>null</code> values in boolean attributes.
      */
     private T m__strBooleanNull;
 
     /**
      * The ordinal position.
      */
     private int m__iOrdinalPosition;
 
     /**
      * The column length.
      */
     private int m__iColumnLength;
 
     /**
      * The column precision.
      */
     private int m__iColumnPrecision;
 
     /**
      * The sequence associated to the column.
      */
     private T m__strSequence;
 
     /**
      * The stack trace to know who created me.
      */
     private final StackTraceElement[] m__aStackTrace =
         new RuntimeException("fake").getStackTrace();
 
     /**
      * Creates an <code>AbstractAttribute</code> with minimal information,
      * allowing lazy-loading mechanisms for non-essential information.
      * @param tableName the table name.
      * @param name the name.
      */
     protected AbstractAttribute(
         final T tableName, final T name)
     {
         immutableSetTableName(tableName);
         immutableSetName(name);
     }
 
     /**
      * Creates an <code>AbstractAttribute</code> with the following
      * information.
      * @param name the name.
      * @param typeId the type id.
      * @param type the type.
      * @param tableName the table name.
      * @param comment the comment.
      * @param ordinalPosition the ordinal position.
      * @param length the maximum data length that can be stored in this attribute.
      * @param precision the precision if the data is numeric.
      * @param allowsNull whether the attribute allows null values or not.
      * @param value the optional value.
      */
     protected AbstractAttribute(
         @Nullable final T name,
         final int typeId,
         @Nullable final T type,
         @Nullable final T tableName,
         @Nullable final T comment,
         final int ordinalPosition,
         final int length,
         final int precision,
         final boolean allowsNull,
         @Nullable final T value)
     {
         this(tableName, name);
         immutableSetTypeId(typeId);
         if (type != null)
         {
             immutableSetType(type);
         }
         immutableSetComment(comment);
         immutableSetOrdinalPosition(ordinalPosition);
         immutableSetLength(length);
         immutableSetPrecision(precision);
         immutableIsNullable(allowsNull);
         immutableSetValue(value);
     }
 
     /**
      * Creates an <code>AbstractAttribute</code> with the following
      * information.
      * @param name the name.
      * @param typeId the type id.
      * @param type the type.
      * @param tableName the table name.
      * @param comment the comment.
      * @param ordinalPosition the ordinal position.
      * @param length the maximum data length that can be stored in this attribute.
      * @param precision the precision if the data is numeric.
      * @param keyword the keyword used to retrieve the value, if any.
      * @param retrievalQuery the query used to retrieve the value, if any.
      * @param sequence the sequence name (for Oracle engines).
      * @param allowsNull whether the attribute allows null values or not.
      * @param value the optional value.
      * @param readOnly whether the attribute is marked as read-only.
      * @param isBool whether the attribute is marked as boolean.
      * @param booleanTrue the symbol for <code>true</code> values in boolean attributes.
      * @param booleanFalse the symbol for <code>false</code> values in boolean attributes.
      * @param booleanNull the symbol for <code>null</code> values in boolean attributes.
      */
     protected AbstractAttribute(
         @NotNull final T name,
         final int typeId,
         @NotNull final T type,
         @NotNull final T tableName,
         @Nullable final T comment,
         final int ordinalPosition,
         final int length,
         final int precision,
         @Nullable final T keyword,
         @Nullable final T retrievalQuery,
         @Nullable final T sequence,
         final boolean allowsNull,
         @Nullable final T value,
         final boolean readOnly,
         final boolean isBool,
         @Nullable final T booleanTrue,
         @Nullable final T booleanFalse,
         @Nullable final T booleanNull)
     {
         this(
             name,
             typeId,
             type,
             tableName,
             comment,
             ordinalPosition,
             length,
             precision,
             allowsNull,
             value);
 
         if (keyword != null)
         {
             immutableSetKeyword(keyword);
         }
         if (retrievalQuery != null)
         {
             immutableSetRetrievalQuery(retrievalQuery);
         }
         if (sequence != null)
         {
             immutableSetSequence(sequence);
         }
         immutableSetReadOnly(readOnly);
         immutableSetBoolean(isBool);
         immutableSetBooleanTrue(booleanTrue);
         immutableSetBooleanFalse(booleanFalse);
         immutableSetBooleanNull(booleanNull);
     }
 
     /**
      * Specifies the name.
      * @param name such name.
      */
     protected final void immutableSetName(@NotNull final T name)
     {
         m__strName = name;
     }
 
     /**
      * Specifies the name.
      * @param name such name.
      */
     protected void setName(@NotNull final T name)
     {
         immutableSetName(name);
     }
 
     /**
      * Retrieves the attribute name.
      * @return such name.
      */
     @NotNull
     @Override
     public T getName()
     {
         return m__strName;
     }
 
     /**
      * Specifies the type.
      * @param type such information.
      */
     protected final void immutableSetTypeId(final int type)
     {
         m__iTypeId = type;
     }
 
     /**
      * Specifies the type.
      * @param type such information.
      */
     @SuppressWarnings("unused")
     protected void setTypeId(final int type)
     {
         immutableSetTypeId(type);
     }
 
     /**
      * Retrieves the attribute type.
      * @return its type.
      */
     @Override
     public int getTypeId()
     {
         return m__iTypeId;
     }
 
     /**
      * Specifies the type.
      * @param type such information.
      */
     protected final void immutableSetType(@NotNull final T type)
     {
         m__strType = type;
     }
 
     /**
      * Specifies the native type.
      * @param type such information.
      */
     @SuppressWarnings("unused")
     protected void setType(@NotNull final T type)
     {
         immutableSetType(type);
     }
 
     /**
      * Retrieves the type.
      * @return such information.
      */
     @NotNull
     @Override
     public T getType()
     {
         return m__strType;
     }
 
     /**
      * Specifies the table name.
      * @param tableName such information.
      */
     protected final void immutableSetTableName(@NotNull final T tableName)
     {
         m__strTableName = tableName;
     }
 
     /**
      * Specifies the table name.
      * @param tableName such information.
      */
     protected void setTableName(@NotNull final T tableName)
     {
         immutableSetTableName(tableName);
     }
 
     /**
      * Retrieves the table name.
      * @return such information.
      */
     @NotNull
     @Override
     public T getTableName()
     {
         return m__strTableName;
     }
 
     /**
      * Specifies the comment.
      * @param comment such comment.
      */
     protected final void immutableSetComment(@Nullable final T comment)
     {
         m__strComment = comment;
     }
 
     /**
      * Specifies the comment.
      * @param comment such comment.
      */
     @SuppressWarnings("unused")
     protected void setComment(@Nullable final T comment)
     {
         immutableSetComment(comment);
     }
 
     /**
      * Retrieves the attribute comment.
      * @return such comment.
      */
     @Nullable
     @Override
     public T getComment()
     {
         return m__strComment;
     }
 
     /**
      * Specifies the keyword used to retrieve the value, if any.
      * @param keyword such information.
      */
     protected final void immutableSetKeyword(@NotNull final T keyword)
     {
         m__strKeyword = keyword;
     }
 
     /**
      * Specifies the keyword used to retrieve the value, if any.
      * @param keyword such information.
      */
     @SuppressWarnings("unused")
     protected void setKeyword(@NotNull final T keyword)
     {
         immutableSetKeyword(keyword);
     }
 
     /**
      * Retrieves the keyword used to retrieve the value, if any.
      * @return such information.
      */
     @Nullable
     @Override
     public T getKeyword()
     {
         return m__strKeyword;
     }
 
     /**
      * Specifies the query used to retrieve the value, if any.
      * @param query such information.
      */
     protected final void immutableSetRetrievalQuery(@NotNull final T query)
     {
         m__strRetrievalQuery = query;
     }
 
     /**
      * Specifies the query used to retrieve the value, if any.
      * @param query such information.
      */
     @SuppressWarnings("unused")
     protected void setRetrievalQuery(@NotNull final T query)
     {
         immutableSetRetrievalQuery(query);
     }
 
     /**
      * Retrieves the query used to retrieve the value, if any.
      * @return such information.
      */
     @Nullable
     @Override
     public T getRetrievalQuery()
     {
         return m__strRetrievalQuery;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean isExternallyManaged()
     {
         return isExternallyManaged(getRetrievalQuery(), getKeyword(), getSequence());
     }
 
     /**
      * Checks whether the attribute is externally-managed or not.
      * @param retrievalQuery the retrieval query.
      * @param keyword the keyword.
      * @param sequence the sequence.
      * @return {@code true} if any of the properties is {@code null} or empty.
      */
     protected boolean isExternallyManaged(
         @Nullable final T retrievalQuery,
         @Nullable final T keyword,
         @Nullable final T sequence)
     {
         return
             (   (!isNullOrEmpty(retrievalQuery))
              || (!isNullOrEmpty(keyword))
              || (!isNullOrEmpty(sequence)));
     }
 
     /**
      * Checks whether given value is null or empty.
      * @param value the value.
      * @return {@code true} in such case.
      */
    protected boolean isNullOrEmpty(@Nullable final T value)
     {
         final boolean result;
 
         if (value == null)
         {
             result = true;
         }
         else
         {
             result = "".equals(value.toString().trim());
         }
 
         return result;
     }
 
     /**
      * Specifies whether the attribute allows null values or not.
      * @param allowsNull such information.
      */
     protected final void immutableIsNullable(final boolean allowsNull)
     {
         m__bNullable = allowsNull;
     }
 
     /**
      * Specifies whether the attribute allows null values or not.
      * @param allowsNull such information.
      */
     @SuppressWarnings("unused")
     protected void setIsNullable(final boolean allowsNull)
     {
         immutableIsNullable(allowsNull);
     }
 
     /**
      * Retrieves whether it allows null values or not.
      * @return such information.
      */
     @Override
     public boolean isNullable()
     {
         return m__bNullable;
     }
 
     /**
      * Alias for {@link #isNullable()}
      * @return whether it allows null values or not.
      */
     @SuppressWarnings("unused")
     public boolean getAllowsNull()
     {
         return isNullable();
     }
 
     /**
      * Specifies the optional attribute's value, meaning
      * it doesn't just describe the metadata, but also
      * contains data.
      * @param value such information.
      */
     protected final void immutableSetValue(@Nullable final T value)
     {
         m__strValue = value;
     }
 
     /**
      * Specifies the optional attribute's value, meaning
      * it doesn't just describe the metadata, but also
      * contains data.
      * @param value such information.
      */
     @SuppressWarnings("unused")
     protected void setValue(@Nullable final T value)
     {
         immutableSetValue(value);
     }
 
     /**
      * Retrieves the optional attribute's value, meaning
      * it doesn't just describe the metadata, but also
      * contains data.
      * @return such information.
      */
     @Nullable
     @Override
     public T getValue()
     {
         return m__strValue;
     }
 
     /**
      * Specifies whether the attribute is marked as read-only.
      * @param flag such condition.
      */
     protected final void immutableSetReadOnly(final boolean flag)
     {
         m__bReadOnly = flag;
     }
 
     /**
      * Specifies whether the attribute is marked as read-only.
      * @param flag such condition.
      */
     @SuppressWarnings("unused")
     protected void setReadOnly(final boolean flag)
     {
         immutableSetReadOnly(flag);
     }
 
     /**
      * Retrieves whether the attribute is marked as read-only.
      * @return such condition.
      */
     @Override
     public boolean isReadOnly()
     {
         return m__bReadOnly;
     }
 
     /**
      * Specifies whether the attribute is marked as boolean.
      * @param flag such condition.
      */
     protected final void immutableSetBoolean(final boolean flag)
     {
         m__bBoolean = flag;
     }
 
     /**
      * Specifies whether the attribute is marked as boolean.
      * @param flag such condition.
      */
     @SuppressWarnings("unused")
     protected void setBoolean(final boolean flag)
     {
         immutableSetBoolean(flag);
     }
 
     /**
      * Retrieves whether the attribute is marked as boolean.
      * @return such condition.
      */
     @Override
     public boolean isBoolean()
     {
         return m__bBoolean;
     }
 
     /**
      * Specifies the symbol for <code>true</code> values.
      * @param value such information.
      */
     protected final void immutableSetBooleanTrue(@Nullable final T value)
     {
         m__strBooleanTrue = value;
     }
 
     /**
      * Specifies the symbol for <code>true</code> values.
      * @param value such information.
      */
     @SuppressWarnings("unused")
     protected void setBooleanTrue(@Nullable final T value)
     {
         immutableSetBooleanTrue(value);
     }
 
     /**
      * Retrieves the symbol for <code>true</code> values.
      * @return such information.
      */
     @Nullable
     @Override
     public T getBooleanTrue()
     {
         return m__strBooleanTrue;
     }
 
     /**
      * Specifies the symbol for <code>false</code> values.
      * @param value such information.
      */
     protected final void immutableSetBooleanFalse(@Nullable final T value)
     {
         m__strBooleanFalse = value;
     }
 
     /**
      * Specifies the symbol for <code>false</code> values.
      * @param value such information.
      */
     @SuppressWarnings("unused")
     protected void setBooleanFalse(@Nullable final T value)
     {
         immutableSetBooleanFalse(value);
     }
 
     /**
      * Retrieves the symbol for <code>false</code> values.
      * @return such information.
      */
     @Nullable
     @Override
     public T getBooleanFalse()
     {
         return m__strBooleanFalse;
     }
 
     /**
      * Specifies the symbol for <code>null</code> values.
      * @param value such information.
      */
     protected final void immutableSetBooleanNull(@Nullable final T value)
     {
         m__strBooleanNull = value;
     }
 
     /**
      * Specifies the symbol for <code>null</code> values.
      * @param value such information.
      */
     @SuppressWarnings("unused")
     protected void setBooleanNull(@Nullable final T value)
     {
         immutableSetBooleanNull(value);
     }
 
     /**
      * Retrieves the symbol for <code>null</code> values.
      * @return such information.
      */
     @Nullable
     @Override
     public T getBooleanNull()
     {
         return m__strBooleanNull;
     }
 
     /**
      * Specifies the ordinal position.
      * @param position the position.
      */
     protected final void immutableSetOrdinalPosition(final int position)
     {
         m__iOrdinalPosition = position;
     }
 
     /**
      * Specifies the ordinal position.
      * @param position the position.
      */
     @SuppressWarnings("unused")
     protected void setOrdinalPosition(final int position)
     {
         immutableSetOrdinalPosition(position);
     }
 
     /**
      * Retrieves the ordinal position.
      * @return the position.
      */
     @Override
     @SuppressWarnings("unused")
     public int getOrdinalPosition()
     {
         return m__iOrdinalPosition;
     }
 
     /**
      * Specifies the column length.
      * @param length the length.
      */
     protected final void immutableSetLength(final int length)
     {
         m__iColumnLength = length;
     }
 
     /**
      * Specifies the column length.
      * @param length the length.
      */
     @SuppressWarnings("unused")
     protected void setLength(final int length)
     {
         immutableSetLength(length);
     }
 
     /**
      * Retrieves the column length.
      * @return such information.
      */
     @Override
     public int getLength()
     {
         return m__iColumnLength;
 
     }
 
     /**
      * Specifies the column precision.
      * @param precision the precision.
      */
     protected final void immutableSetPrecision(final int precision)
     {
         m__iColumnPrecision = precision;
     }
 
     /**
      * Specifies the column precision.
      * @param precision the precision.
      */
     @SuppressWarnings("unused")
     protected void setPrecision(final int precision)
     {
         immutableSetPrecision(precision);
     }
 
     /**
      * Retrieves the column precision.
      * @return such information.
      */
     @Override
     public int getPrecision()
     {
         return m__iColumnPrecision;
     }
 
     /**
      * Specifies the sequence name (if any), for Oracle engines.
      * @param sequence such name.
      */
     protected final void immutableSetSequence(@NotNull final T sequence)
     {
         this.m__strSequence = sequence;
     }
 
     /**
      * Specifies the sequence name (if any), for Oracle engines.
      * @param sequence such name.
      */
     @SuppressWarnings("unused")
     protected void setSequence(@NotNull final T sequence)
     {
         immutableSetSequence(sequence);
     }
 
     /**
      * Retrieves the sequence name.
      * @return such name.
      */
     @Override
     @Nullable
     public T getSequence()
     {
         return this.m__strSequence;
     }
 
     /**
      * Retrieves the stack trace when the attribute was created.
      * @return such information.
      */
     @SuppressWarnings("unused")
     protected StackTraceElement[] getStackTrace()
     {
         return m__aStackTrace;
     }
 
     /**
      * Prints given stack trace in JSON format.
      * @param stackTrace the stack trace.
      * @return the JSON format.
      */
     @NotNull
     protected String toJSON(@NotNull final StackTraceElement[] stackTrace)
     {
         @NotNull final StringBuilder result = new StringBuilder("[ ");
 
         boolean firstTime = true;
         for (@NotNull final StackTraceElement entry : stackTrace)
         {
             if (firstTime)
             {
                 firstTime = false;
             }
             else
             {
                 result.append(", ");
             }
 
             result.append("\"");
             result.append(entry);
             result.append("\"");
         }
 
         return result.toString();
     }
 
     /**
      * Retrieves the attribute name.
      * @return such information.
      */
     @Override
     @NotNull
     public String toString()
     {
         return
               "{ \"class\": \"" + AbstractAttribute.class.getName() + '"'
 /*
             + ", \"stackTrace\": " + toJSON(m__aStackTrace)
 */
             + ", \"name\": \"" + m__strName + '"'
 /*
             + ", \"typeId\": " + m__iTypeId
             + ", \"type\": \"" + m__strType + '"'
             + ", \"tableName\": \"" + m__strTableName + '"'
             + ", \"comment\": \"" + m__strComment + '"'
 */
             + ((m__strKeyword != null) ? ", \"keyword\": \"" + m__strKeyword +  '"' : "")
             + ((m__strRetrievalQuery != null) ? ", \"retrievalQuery\": \"" + m__strRetrievalQuery + '"' : "")
 /*
             + ", \"nullable\": \"" + m__bNullable + '"'
             + ", \"value\": " + m__strValue
 */
             + ", \"readOnly\": " + m__bReadOnly
 /*
             + ", \"boolean\": " + m__bBoolean
             + ", \"booleanTrue\": \"" + m__strBooleanTrue + '"'
             + ", \"booleanFalse\": \"" + m__strBooleanFalse + '"'
             + ", \"booleanNull\":" + m__strBooleanNull + '"'
             + ", \"ordinalPosition\":" + m__iOrdinalPosition
             + ", \"columnLength\": " + m__iColumnLength
             + ", \"columnPrecision\": " + m__iColumnPrecision
 */
             + " }";
     }
 
     @Override
     public int hashCode()
     {
         return new HashCodeBuilder().append(this.m__strName).append(this.m__iTypeId).append(this.m__strType)
             .append(this.m__strTableName).append(this.m__strComment).append(this.m__strKeyword)
             .append(this.m__strRetrievalQuery).append(this.m__bNullable).append(this.m__strValue)
             .append(this.m__bReadOnly).append(this.m__bBoolean).append(this.m__strBooleanTrue)
             .append(this.m__strBooleanFalse).append(this.m__strBooleanNull).append(this.m__iOrdinalPosition)
             .append(this.m__iColumnLength).append(this.m__iColumnPrecision).append(this.m__aStackTrace).toHashCode();
     }
 
     @Override
     @SuppressWarnings("unchecked")
     public boolean equals(@Nullable final Object obj)
     {
         boolean result = false;
 
         if (obj instanceof Attribute)
         {
             result = areEqual(this, (Attribute<T>) obj);
         }
 
         return result;
     }
 
     /**
      * Checks whether given {@link Attribute attributes} are equal.
      * @param first the first attribute.
      * @param second the second attribute.
      * @return <code>true</code> in such case.
      */
     protected boolean areEqual(@NotNull final Attribute<T> first, @NotNull final Attribute<T> second)
     {
         return
             new EqualsBuilder()
                 .append(first.getName(), second.getName())
                 .append(first.getTypeId(), second.getTypeId())
                 .append(first.getTableName(), second.getTableName())
                 .isEquals();
     }
 
     /**
      * Compares given object with this instance.
      * @param object the object to compare to.
      * @return the result of such comparison.
      * @throws ClassCastException if the type of the specified
      * object prevents it from being compared to this Object.
      */
     @Override
     public int compareTo(@Nullable final Attribute<T> object)
         throws  ClassCastException
     {
         int result = 1;
 
         if (object != null)
         {
             result = compareThem(this, object);
         }
 
         return result;
     }
 
     /**
      * Compares both {@link Attribute} instances.
      * @param first the first.
      * @param second the second.
      * @return the outcome of comparing <code>first.getOrdinalPosition()</code> vs
      * <code>second.getOrdinalPosition()</code>
      */
     protected int compareThem(@NotNull final Attribute<T> first, @NotNull final Attribute<T> second)
     {
         return first.getOrdinalPosition() - second.getOrdinalPosition();
     }
 }
