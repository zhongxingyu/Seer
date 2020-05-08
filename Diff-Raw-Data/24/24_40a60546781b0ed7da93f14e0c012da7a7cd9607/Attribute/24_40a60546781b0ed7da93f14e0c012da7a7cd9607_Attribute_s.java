 /*
  * Owl Platform World Model Library for Java
  * Copyright (C) 2012 Robert Moore and the Owl Platform
  * 
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation; either version 2.1 of the License, or
  * (at your option) any later version.
  *  
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *  
  * You should have received a copy of the GNU Lesser General Public License along
  * with this program; if not, write to the Free Software Foundation, Inc.,
  * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
  */
 package com.owlplatform.worldmodel;
 
 import java.io.UnsupportedEncodingException;
 import java.util.Date;
 
 import com.owlplatform.common.util.NumericUtils;
 import com.owlplatform.worldmodel.types.DataConverter;
 
 /**
  * Represents an Attribute of an Identifier within the World Model. Attributes
  * represent characteristics of Identifiers, change over time, and can have
  * multiple origins (sources).
  * 
  * @author Robert Moore
  * 
  */
 public class Attribute {
 
   /**
    * 32 bit unsigned integer specifying an alias for this attribute's name.
    */
   private int attributeNameAlias;
 
   /**
    * The actual UTF-16 representation of the attribute name.
    */
   private String attributeName;
 
   /**
    * A signed 64 bit integer specifying an offset from the epoch, 1970, when
    * this attribute value was created.
    */
   private long creationDate;
 
   /**
    * A signed 64 bit integer specifying an offset from the epoch, 1970, when
    * this attribute expired. This is 0 if the attribute has not expired.
    */
   private long expirationDate;
 
   /**
    * 32 bit unsigned integer specifying an alias for the origin of this
    * attribute.
    */
   private int originNameAlias;
 
   /**
    * The actual UTF-16 representation of the data origin. This value is only
    * used internally and not transmitted over the network.
    */
   private String originName;
 
   /**
    * A buffer of the specified length that contains this attribute's data. The
    * content of this buffer is specified by the attribute's name.
    */
   private byte[] data = null;
 
   /**
    * Identifier that this attribute is associated with.
    */
   private String identifier;
 
   /**
    * Returns the length of this attribute, in bytes, as encoded according to the
    * Client-World Model protocol.
    * 
    * @return the length of the encoded form of this attribute object
    */
   public int getClientLength() {
     // Attribute name alias, creation date, expiration date, Origin name
     // alias, data length
     int length = 4 + 8 + 8 + 4 + 4;
     // Data
     if (this.data != null) {
       length += this.data.length;
     }
     return length;
   }
 
   /**
    * Returns the length of this attribute, in bytes, as encoded according to the
    * Solver-World Model protocol.
    * 
    * @return the length of the encoded form of this attribute object
    */
   public int getSolverLength() {
     // Attribute name alias, creation date, Id length,
     int length = 4 + 8 + 4;
 
     // Id,
     if (this.identifier != null) {
       try {
        length += this.identifier.getBytes("UTF16-BE").length;
       } catch (UnsupportedEncodingException e) {
         throw new RuntimeException("Unable to encode UTF16 strings.", e);
       }
     }
 
     // Data length
     length += 4;
     // Data
     if (this.data != null) {
       length += this.data.length;
     }
 
     return length;
   }
 
   /**
    * Returns the alias value for this attribute's name, or 0 if it has not been
    * set.
    * 
    * @return the alias value for this attribute's name, or 0 if it has not been
    *         set.
    */
   public int getAttributeNameAlias() {
     return this.attributeNameAlias;
   }
 
   /**
    * Sets the alias value for this attribute's name.
    * 
    * @param attributeNameAlias
    *          the new alias value.
    */
   public void setAttributeNameAlias(int attributeNameAlias) {
     this.attributeNameAlias = attributeNameAlias;
   }
 
   /**
    * Returns this attribute value's creation timestamp.
    * 
    * @return this attribute value's creation timestamp.
    */
   public long getCreationDate() {
     return this.creationDate;
   }
 
   /**
    * Sets the new creation timestamp for this attribute value.
    * 
    * @param creationDate
    *          the new creation timestamp value.
    */
   public void setCreationDate(long creationDate) {
     this.creationDate = creationDate;
   }
 
   /**
    * Returns this attribute value's expiration timestamp, or 0 if it is not set.
    * 
    * @return this attribute value's expriation timestamp.
    */
   public long getExpirationDate() {
     return this.expirationDate;
   }
 
   /**
    * Sets the new expiration timestamp for this attribute value.
    * 
    * @param expirationDate
    *          the new expiration timestamp value.
    */
   public void setExpirationDate(long expirationDate) {
     this.expirationDate = expirationDate;
   }
 
   /**
    * Returns this attribute value's origin name alias value.
    * 
    * @return this attribute vlaue's origin name alias.
    */
   public int getOriginNameAlias() {
     return this.originNameAlias;
   }
 
   /**
    * Sets this attribute value's origin name.
    * 
    * @param originNameAlias
    *          the new origin name value.
    */
   public void setOriginNameAlias(int originNameAlias) {
     this.originNameAlias = originNameAlias;
   }
 
   /**
    * Returns the data for this attribute value, if any.
    * 
    * @return the data for this attribute value, or {@code null} if there is
    *         none.
    */
   public byte[] getData() {
     return this.data;
   }
 
   /**
    * Sets the data for this attribute value.
    * 
    * @param data
    *          the new data for this attribute value.
    */
   public void setData(byte[] data) {
     this.data = data;
   }
 
   /**
    * Returns the name of this attribute.
    * 
    * @return the name of this attribute.
    */
   public String getAttributeName() {
     return this.attributeName;
   }
 
   /**
    * Sets the name of this attribute.
    * 
    * @param attributeName
    *          the new name of this attribute.
    */
   public void setAttributeName(String attributeName) {
     this.attributeName = attributeName;
   }
 
   /**
    * Returns the name of this attribute.
    * 
    * @return the name of this attribute, or {@code null} if it has not been set.
    */
   public String getOriginName() {
     return this.originName;
   }
 
   /**
    * Sets the origin for this attribute value.
    * 
    * @param originName
    *          the new origin for this attribute value.
    */
   public void setOriginName(String originName) {
     this.originName = originName;
   }
 
   /**
    * Gets the Identifier that this attribute is associated with.
    * 
    * @return the Identifier.
    */
   public String getId() {
     return this.identifier;
   }
 
   /**
    * Sets the Identifier that this attribute is associated with.
    * 
    * @param identifier
    *          the new Identifier.
    */
   public void setId(String identifier) {
     this.identifier = identifier;
   }
 
   @Override
   public String toString() {
     StringBuffer sb = new StringBuffer();
     if (this.attributeName == null) {
       sb.append('(').append(this.attributeNameAlias).append(") ");
     } else {
       sb.append(this.attributeName).append(' ');
     }
     sb.append(new Date(this.creationDate)).append('-')
         .append(new Date(this.expirationDate)).append(" from ");
     if (this.originName == null) {
       sb.append('(').append(this.originNameAlias).append(")");
     } else {
       sb.append(this.originName);
     }
     sb.append(":");
     if (this.data != null) {
       if (this.attributeName != null
           && DataConverter.hasConverterForAttribute(this.attributeName)) {
         sb.append(DataConverter.asString(this.attributeName, this.data));
       } else {
         sb.append(NumericUtils.toHexString(this.data));
       }
     } else {
       sb.append("NULL");
     }
 
     return sb.toString();
   }
 }
