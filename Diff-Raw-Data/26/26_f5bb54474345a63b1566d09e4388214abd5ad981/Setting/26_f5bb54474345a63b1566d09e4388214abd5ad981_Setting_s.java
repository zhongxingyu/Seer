 /*
  * $Source$
  * $Revision$
  *
  * Copyright (C) 2000 William Chesters
  *
  * Part of Melati (http://melati.org), a framework for the rapid
  * development of clean, maintainable web applications.
  *
  * Melati is free software; Permission is granted to copy, distribute
  * and/or modify this software under the terms either:
  *
  * a) the GNU General Public License as published by the Free Software
  *    Foundation; either version 2 of the License, or (at your option)
  *    any later version,
  *
  *    or
  *
  * b) any version of the Melati Software License, as published
  *    at http://melati.org
  *
  * You should have received a copy of the GNU General Public License and
  * the Melati Software License along with this program;
  * if not, write to the Free Software Foundation, Inc.,
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA to obtain the
  * GNU General Public License and visit http://melati.org to obtain the
  * Melati Software License.
  *
  * Feel free to contact the Developers of Melati (http://melati.org),
  * if you would like to work out a different arrangement than the options
  * outlined here.  It is our intention to allow Melati to be used by as
  * wide an audience as possible.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * Contact details for copyright holder:
  *
  *     William Chesters <williamc At paneris.org>
  *     http://paneris.org/~williamc
  *     Obrechtstraat 114, 2517VX Den Haag, The Netherlands
  */
 
 package org.melati.poem;
 
 import org.melati.poem.generated.SettingBase;
 
 /**
  * A setting, analageous to a Property.
  * <p>
  * NOTE While the underlying value is held as a String 
  * that is converted to any type.
  * 
  * <p>
  * 
  * Melati POEM generated, modified definition of  
  * a <code>Persistent</code> <code>Setting</code> object.
  * 
  * <p> 
  * Description: 
  *   A configurable setting for the application. 
  * </p>
  * 
  * <table> 
  * <tr><th colspan='3'>
  * Field summary for SQL table <code>Setting</code>
  * </th></tr>
  * <tr><th>Name</th><th>Type</th><th>Description</th></tr>
  * <tr><td> id </td><td> Integer </td><td> The Table Row Object ID </td></tr> 
  * <tr><td> name </td><td> String </td><td> A code name for this setting 
  * </td></tr> 
  * <tr><td> value </td><td> String </td><td> The value of this setting 
  * </td></tr> 
  * </table> 
  * 
  * @generator org.melati.poem.prepro.TableDef#generateMainJava 
  * @author WilliamC@paneris.org
  */
 
 public class Setting extends SettingBase {
  /**
   * Constructor 
   * for a <code>Persistent</code> <code>Setting</code> object.
   * <p>
   * Description: 
   *   A configurable setting for the application. 
   * </p>
   * 
   * @generator org.melati.poem.prepro.TableDef#generateMainJava 
   */
   public Setting() { }
 
   // programmer's domain-specific code here
 
   private FieldAttributes valueAttributes = null;
   private Object raw = null;
   private Object cooked = null;
 
   /**
    * Constructor with reasonable defaults for a String setting
    * Use SettingTable.create to persist this.
    * @param typefactory
    * @param name
    * @param value
    * @param displayname
    * @param description
    */
   public Setting(Integer typefactory, String name, String value,
                  String displayname, String description) {
     setTypefactory_unsafe(typefactory);
     setName_unsafe(name);
     setValue_unsafe(value);
     setDisplayname_unsafe(displayname);
     setDescription_unsafe(description);
     setUsereditable_unsafe(Boolean.TRUE);
     setNullable_unsafe(Boolean.TRUE);
     setSize_unsafe(new Integer(-1));
     setWidth_unsafe(new Integer(20));
     setHeight_unsafe(new Integer(1));
     setPrecision_unsafe(new Integer(22));
     setScale_unsafe(new Integer(2));
   }
 
  /**
   * Thrown when a {@link Setting} value fails validation.
   */
   public static class SettingValidationException extends PoemException {
     private static final long serialVersionUID = 1L;
 
     /** The name of the requested setting. */
     public String name;
 
     /** Constructor. */
     public SettingValidationException(String name, Exception problem) {
       super(problem);
       this.name = name;
     }
 
     /** @return The detail message. */
     public String getMessage() {
       return "A problem arose updating the value of the `" + name +
       "' setting:\n" + subException.getMessage();
     }
   }
 
   /**
    * Check that value is of correct type before setting. 
    * {@inheritDoc}
    * @see org.melati.poem.generated.SettingBase#setValue(java.lang.String)
    */
   public void setValue(String value) {
     Object rawLocal;
     try {
       rawLocal = getType().rawOfString(value);
     } catch (Exception e) {
       throw new SettingValidationException(getName_unsafe(), e);
     }
 
     super.setValue(value);
     this.raw = rawLocal;
     cooked = null;
   }
 
   /**
    * Set from a raw value; checking that the value is of the correct type first.
    * @param raw the raw to set
    */
   public void setRaw(Object raw) {
     String string;
     Object newRaw;
     try {
       string = getType().stringOfRaw(raw);
     } catch (Exception e) {
       throw new SettingValidationException(getName_unsafe(), e);
     }
     try {
       newRaw = getType().rawOfString(string);
     } catch (Exception e) {
       throw new SettingValidationException(getName_unsafe(), e);
     }
     super.setValue(newRaw == null ? null : string);
     this.raw = newRaw;
     cooked = null;
   }
 
   /**
    * @return the raw value
    */
   public Object getRaw() {
     if (raw == null)
       try {
         raw = getType().rawOfString(getValue());
       } catch (Exception e) {
         throw new SettingValidationException(getName_unsafe(), e);
       }
 
     return raw;
   }
 
   /**
    * @return the cooked, ie typed, Object
    */
   public Object getCooked() {
     if (cooked == null)
       cooked = getType().cookedOfRaw(getRaw());
     return cooked;
   }
 
  /**
   * Thrown when a {@link Setting}'s type does not match the type required. 
   */
   public static class 
       SettingTypeMismatchException extends AppBugPoemException {
     private static final long serialVersionUID = 1L;
 
     /** Name of the setting. */
     public String name;
     /** The factory used. */
     public PoemTypeFactory type;
     /** Required type. */
     public String reqType;
 
     /** Constructor. */
     public SettingTypeMismatchException(String name, PoemTypeFactory type,
                                         String reqType) {
       this.name = name;
       this.type = type;
       this.reqType = reqType;
     }
 
     /** @return The detail message. */
     public String getMessage() {
       return "The setting `" + name + "' has type `" + type + "' but " +
       "the application asked for a value of type " + reqType;
     }
   }
 
   /**
    * @return value as an Integer
    */
   public Integer getIntegerCooked() {
     Object cookedLocal = getCooked();
     if (cookedLocal == null && getNullable().booleanValue())
       return null;
     else if (cookedLocal instanceof Integer)
       return (Integer)cookedLocal;
     else
       throw new SettingTypeMismatchException(getName_unsafe(),
                                              getTypefactory(), "Integer");
   }
 
   /**
    * @return value as a String
    */
   public String getStringCooked() {
     Object cookedLocal = getCooked();
     if (cookedLocal == null && getNullable().booleanValue())
       return null;
     else if (cookedLocal instanceof String)
       return (String)cookedLocal;
     else
       throw new SettingTypeMismatchException(getName_unsafe(),
                                              getTypefactory(), "String");
   }
 
   /**
    * @return value as a Boolean
    */
   public Boolean getBooleanCooked() {
     Object cookedLocal = getCooked();
     if (cookedLocal == null && getNullable().booleanValue())
       return null;
     else if (cookedLocal instanceof Boolean)
       return (Boolean)cookedLocal;
     else
       throw new SettingTypeMismatchException(getName_unsafe(),
                                              getTypefactory(), "Boolean");
   }
 
  private FieldAttributes valueAttributes() {
     if (valueAttributes == null)
       valueAttributes =
           fieldAttributesRenamedAs(getSettingTable().getValueColumn());
 
     return valueAttributes;
   }
 
   /**
   * {@inheritDoc}
    * @see org.melati.poem.generated.SettingBase#getValueField()
    */
   public Field getValueField() {
     try {
      return new Field(getRaw(), valueAttributes());
     } catch (AccessPoemException accessException) {
      return new Field(accessException, valueAttributes());
     }
   }
 }
