 /**
  * EasyBeans
  * Copyright (C) 2011 Bull S.A.S.
  * Contact: easybeans@ow2.org
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
  * USA
  *
  * --------------------------------------------------------------------------
  * $Id: EnvEntryBean.java 5743 2011-02-28 16:02:42Z benoitf $
  * --------------------------------------------------------------------------
  */
 
 package org.ow2.easybeans.application.enventry;
 
 import javax.annotation.Resource;
 import javax.ejb.Stateless;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 
 import org.testng.Assert;
 
 
 /**
  * This beans inject some env-entry reference.
  *
  *  The following are the legal values of env-entry-type-valuesType:
  *      java.lang.Boolean
  *      java.lang.Byte
  *      java.lang.Character
  *      java.lang.String
  *      java.lang.Short
  *      java.lang.Integer
  *      java.lang.Long
  *      java.lang.Float
  *      java.lang.Double
  *      java.lang.Class
  *      any enumeration type (i.e. a subclass of java.lang.Enum)
  * @author Florent Benoit
  */
 @Stateless(mappedName="EnvEntryBean@Remote")
 public class EnvEntryBean implements IEnvEntry {
 
     @Resource(lookup="java:module/env/entry-boolean")
     private boolean simpleEntryBoolean;
     private boolean simpleEntryBooleanSetter;
 
     @Resource(lookup="java:app/env/entry-boolean")
     private Boolean entryBoolean;
     private Boolean entryBooleanSetter;
 
     @Resource(lookup="java:module/env/entry-byte")
     private byte simpleEntryByte;
     private byte simpleEntryByteSetter;
 
     @Resource(lookup="java:app/env/entry-byte")
     private Byte entryByte;
     private Byte entryByteSetter;
 
     @Resource(lookup="java:module/env/entry-character")
     private char simpleEntryCharacter;
     private char simpleEntryCharacterSetter;
 
     @Resource(lookup="java:app/env/entry-character")
     private Character entryCharacter;
     private Character entryCharacterSetter;
 
     @Resource(lookup="java:module/env/entry-string")
     private String entryString;
     private String entryStringSetter;
 
     @Resource(lookup="java:module/env/entry-short")
     private short simpleEntryShort;
     private short simpleEntryShortSetter;
 
     @Resource(lookup="java:app/env/entry-short")
     private Short entryShort;
     private Short entryShortSetter;
 
 
     @Resource(lookup="java:module/env/entry-int")
     private int simpleEntryInteger;
     private int simpleEntryIntegerSetter;
 
     @Resource(lookup="java:app/env/entry-integer")
     private Integer entryInteger;
     private Integer entryIntegerSetter;
 
     @Resource(lookup="java:module/env/entry-long")
     private long simpleEntryLong;
     private long simpleEntryLongSetter;
 
     @Resource(lookup="java:app/env/entry-long")
     private Long entryLong;
     private Long entryLongSetter;
 
     @Resource(lookup="java:module/env/entry-float")
     private float simpleEntryFloat;
     private float simpleEntryFloatSetter;
 
 
     @Resource(lookup="java:app/env/entry-float")
     private Float entryFloat;
     private Float entryFloatSetter;
 
     @Resource(lookup="java:module/env/entry-double")
     private double simpleEntryDouble;
     private double simpleEntryDoubleSetter;
 
     @Resource(lookup="java:app/env/entry-double")
     private Double entryDouble;
     private Double entryDoubleSetter;
 
     @Resource(lookup="java:module/env/entry-class")
     private Class<DummyEntryClass> entryClass;
 
     private Class<DummyEntryClass> entryClassSetter;
 
     @Resource(lookup="java:module/env/entry-enum")
     private MyEnum entryEnum;
 
 
     private MyEnum entryEnumSetter;
 
 
     public void checkInjectedFields() {
         Assert.assertTrue(this.simpleEntryBoolean);
         Assert.assertTrue(this.simpleEntryBooleanSetter);
         Assert.assertTrue(this.entryBoolean);
         Assert.assertTrue(this.entryBooleanSetter);
 
         Assert.assertEquals(this.simpleEntryByte, 1);
         Assert.assertEquals(this.simpleEntryByteSetter, 1);
         Assert.assertEquals(this.entryByte, Byte.valueOf("1"));
         Assert.assertEquals(this.entryByteSetter, Byte.valueOf("1"));
 
         Assert.assertEquals(this.simpleEntryCharacter, 'f');
         Assert.assertEquals(this.simpleEntryCharacterSetter, 'b');
         Assert.assertEquals(this.entryCharacter, Character.valueOf('b'));
         Assert.assertEquals(this.entryCharacterSetter, Character.valueOf('f'));
 
         Assert.assertEquals(this.entryString, "Florent");
         Assert.assertEquals(this.entryStringSetter, "Benoit");
 
         Assert.assertEquals(this.simpleEntryShort, 25);
         Assert.assertEquals(this.simpleEntryShortSetter, 25);
         Assert.assertEquals(this.entryShort, Short.valueOf("20"));
         Assert.assertEquals(this.entryShortSetter, Short.valueOf("20"));
 
         Assert.assertEquals(this.simpleEntryInteger, 2503);
         Assert.assertEquals(this.simpleEntryIntegerSetter, 2503);
         Assert.assertEquals(this.entryInteger, Integer.valueOf(2512));
         Assert.assertEquals(this.entryIntegerSetter, Integer.valueOf(2512));
 
         Assert.assertEquals(this.simpleEntryLong, 10);
         Assert.assertEquals(this.simpleEntryLongSetter, 10);
         Assert.assertEquals(this.entryLong, Long.valueOf(11));
         Assert.assertEquals(this.entryLongSetter, Long.valueOf(11));
 
         Assert.assertEquals(this.simpleEntryFloat, 25.03f);
         Assert.assertEquals(this.simpleEntryFloatSetter, 25.03f);
         Assert.assertEquals(this.entryFloat, Float.valueOf("19.79"));
         Assert.assertEquals(this.entryFloatSetter, Float.valueOf("19.79"));
 
         Assert.assertEquals(this.simpleEntryDouble, 2503d);
         Assert.assertEquals(this.simpleEntryDoubleSetter, 2503d);
         Assert.assertEquals(this.entryDouble, Double.valueOf("1979"));
         Assert.assertEquals(this.entryDoubleSetter, Double.valueOf("1979"));
 
         Assert.assertEquals(this.entryClass, DummyEntryClass.class);
         Assert.assertEquals(this.entryClassSetter, DummyEntryClass.class);
 
         Assert.assertEquals(this.entryEnum, MyEnum.FLORENT);
         Assert.assertEquals(this.entryEnumSetter, MyEnum.BENOIT);
     }
 
     /**
      * Ensures that lookup in java:module and java:comp is the same.
      */
    public void checkCompNotEqualsModule() throws NamingException {
         Integer lookupModuleValue = (Integer) new InitialContext().lookup("java:module/env/entry-int");
         Integer lookupCompValue = (Integer) new InitialContext().lookup("java:comp/env/entry-int");
 
        Assert.assertNotSame(lookupModuleValue, lookupCompValue);
     }
 
 
     @Resource(lookup="java:app/env/entry-boolean")
     private void setSimpleEntryBoolean(final boolean simpleEntryBooleanSetter) {
         this.simpleEntryBooleanSetter = simpleEntryBooleanSetter;
     }
 
     @Resource(lookup="java:module/env/entry-boolean")
     private void setEntryBoolean(final Boolean entryBooleanSetter) {
         this.entryBooleanSetter = entryBooleanSetter;
     }
 
 
 
     @Resource(lookup="java:app/env/entry-byte")
     private void setSimpleEntryByte(final byte simpleEntryByteSetter) {
         this.simpleEntryByteSetter = simpleEntryByteSetter;
     }
 
     @Resource(lookup="java:module/env/entry-byte")
     private void setEntryByte(final Byte entryByteSetter) {
         this.entryByteSetter = entryByteSetter;
     }
 
     @Resource(lookup="java:app/env/entry-character")
     private void setSimpleEntryCharacter(final char simpleEntryCharacterSetter) {
         this.simpleEntryCharacterSetter = simpleEntryCharacterSetter;
     }
 
     @Resource(lookup="java:module/env/entry-character")
     private void setEntryCharacter(final Character entryCharacterSetter) {
         this.entryCharacterSetter = entryCharacterSetter;
     }
 
 
     @Resource(lookup="java:app/env/entry-string")
     private void setEntryString(final String entryStringSetter) {
         this.entryStringSetter = entryStringSetter;
     }
 
     @Resource(lookup="java:module/env/entry-short")
     private void setSimpleEntryShort(final short simpleEntryShortSetter) {
         this.simpleEntryShortSetter = simpleEntryShortSetter;
     }
 
     @Resource(lookup="java:app/env/entry-short")
     private void setEntryShort(final Short entryShortSetter) {
         this.entryShortSetter = entryShortSetter;
     }
 
     @Resource(lookup="java:module/env/entry-int")
     private void setSimpleEntryInteger(final int simpleEntryIntegerSetter) {
         this.simpleEntryIntegerSetter = simpleEntryIntegerSetter;
     }
 
     @Resource(lookup="java:app/env/entry-integer")
     private void setEntryInteger(final Integer entryIntegerSetter) {
         this.entryIntegerSetter = entryIntegerSetter;
     }
 
 
     @Resource(lookup="java:module/env/entry-long")
     private void setSimpleEntryLong(final long simpleEntryLongSetter) {
         this.simpleEntryLongSetter = simpleEntryLongSetter;
     }
 
     @Resource(lookup="java:app/env/entry-long")
     private void setEntryLong(final Long entryLongSetter) {
         this.entryLongSetter = entryLongSetter;
     }
 
     @Resource(lookup="java:module/env/entry-float")
     private void setSimpleEntryFloat(final float simpleEntryFloatSetter) {
         this.simpleEntryFloatSetter = simpleEntryFloatSetter;
     }
 
     @Resource(lookup="java:app/env/entry-float")
     private void setEntryFloat(final Float entryFloatSetter) {
         this.entryFloatSetter = entryFloatSetter;
     }
 
 
     @Resource(lookup="java:module/env/entry-double")
     private void setSimpleEntryDouble(final double simpleEntryDoubleSetter) {
         this.simpleEntryDoubleSetter = simpleEntryDoubleSetter;
     }
 
     @Resource(lookup="java:app/env/entry-double")
     private void setEntryDouble(final double entryDoubleSetter) {
         this.entryDoubleSetter = entryDoubleSetter;
     }
 
 
     @Resource(lookup="java:app/env/entry-class")
     private void setEntryClassSetter(final Class<DummyEntryClass> entryClassSetter) {
         this.entryClassSetter = entryClassSetter;
     }
 
     @Resource(lookup="java:app/env/entry-enum")
     private void setEntryEnumSetter(final MyEnum entryEnumSetter) {
         this.entryEnumSetter = entryEnumSetter;
     }
 
 
 
 
 }
