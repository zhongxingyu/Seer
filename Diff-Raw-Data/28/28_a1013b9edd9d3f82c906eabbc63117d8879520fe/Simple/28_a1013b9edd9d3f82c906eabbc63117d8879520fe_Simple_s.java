 /*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
 package org.jboss.test.managed.factory.support;
 
 import java.io.Serializable;
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.util.Date;
 
 /**
  * Simple.
  * 
  * @author <a href="adrian@jboss.com">Adrian Brock</a>
  * @version $Revision: 1.1 $
  */
 public class Simple implements Serializable
 {
    /** The serialVersionUID */
    private static final long serialVersionUID = -1;
    
    private BigDecimal bigDecimalValue;
    
    private BigInteger bigIntegerValue;
    
    private boolean booleanvalue;
    
    private Boolean booleanValue;
    
    private byte bytevalue;
    
    private Byte byteValue;
    
    private char charactervalue;
    
    private Character characterValue; 
    
    private Date dateValue;
    
    private double doublevalue;
    
    private Double doubleValue;
    
    private float floatvalue;
    
    private Float floatValue;
    
    private int integervalue;
    
    private Integer integerValue;
    
    private long longvalue;
    
    private Long longValue;
    
    private short shortvalue;
    
    private Short shortValue;
    
    private String stringValue;
 
    /**
     * Get the bigDecimalValue.
     * 
     * @return the bigDecimalValue.
     */
    public BigDecimal getBigDecimalValue()
    {
       return bigDecimalValue;
    }
 
    /**
     * Set the bigDecimalValue.
     * 
     * @param bigDecimalValue the bigDecimalValue.
     */
    public void setBigDecimalValue(BigDecimal bigDecimalValue)
    {
       this.bigDecimalValue = bigDecimalValue;
    }
 
    /**
     * Get the bigIntegerValue.
     * 
     * @return the bigIntegerValue.
     */
    public BigInteger getBigIntegerValue()
    {
       return bigIntegerValue;
    }
 
    /**
     * Set the bigIntegerValue.
     * 
     * @param bigIntegerValue the bigIntegerValue.
     */
    public void setBigIntegerValue(BigInteger bigIntegerValue)
    {
       this.bigIntegerValue = bigIntegerValue;
    }
 
    /**
     * Get the booleanvalue.
     * 
     * @return the booleanvalue.
     */
    public boolean isBooleanvalue()
    {
       return booleanvalue;
    }
 
    /**
     * Set the booleanvalue.
     * 
     * @param booleanvalue the booleanvalue.
     */
    public void setBooleanvalue(boolean booleanvalue)
    {
       this.booleanvalue = booleanvalue;
    }
 
    /**
     * Get the booleanValue.
     * 
     * @return the booleanValue.
     */
    public Boolean getBooleanValue()
    {
       return booleanValue;
    }
 
    /**
     * Set the booleanValue.
     * 
     * @param booleanValue the booleanValue.
     */
    public void setBooleanValue(Boolean booleanValue)
    {
       this.booleanValue = booleanValue;
    }
 
    /**
     * Get the bytevalue.
     * 
     * @return the bytevalue.
     */
    public byte getBytevalue()
    {
       return bytevalue;
    }
 
    /**
     * Set the bytevalue.
     * 
     * @param bytevalue the bytevalue.
     */
    public void setBytevalue(byte bytevalue)
    {
       this.bytevalue = bytevalue;
    }
 
    /**
     * Get the byteValue.
     * 
     * @return the byteValue.
     */
    public Byte getByteValue()
    {
       return byteValue;
    }
 
    /**
     * Set the byteValue.
     * 
     * @param byteValue the byteValue.
     */
    public void setByteValue(Byte byteValue)
    {
       this.byteValue = byteValue;
    }
 
    /**
     * Get the charactervalue.
     * 
     * @return the charactervalue.
     */
    public char getCharactervalue()
    {
       return charactervalue;
    }
 
    /**
     * Set the charactervalue.
     * 
     * @param charactervalue the charactervalue.
     */
    public void setCharactervalue(char charactervalue)
    {
       this.charactervalue = charactervalue;
    }
 
    /**
     * Get the characterValue.
     * 
     * @return the characterValue.
     */
    public Character getCharacterValue()
    {
       return characterValue;
    }
 
    /**
     * Set the characterValue.
     * 
     * @param characterValue the characterValue.
     */
    public void setCharacterValue(Character characterValue)
    {
       this.characterValue = characterValue;
    }
 
    /**
     * Get the dateValue.
     * 
     * @return the dateValue.
     */
    public Date getDateValue()
    {
       return dateValue;
    }
 
    /**
     * Set the dateValue.
     * 
     * @param dateValue the dateValue.
     */
    public void setDateValue(Date dateValue)
    {
       this.dateValue = dateValue;
    }
 
    /**
     * Get the doublevalue.
     * 
     * @return the doublevalue.
     */
    public double getDoublevalue()
    {
       return doublevalue;
    }
 
    /**
     * Set the doublevalue.
     * 
     * @param doublevalue the doublevalue.
     */
    public void setDoublevalue(double doublevalue)
    {
       this.doublevalue = doublevalue;
    }
 
    /**
     * Get the doubleValue.
     * 
     * @return the doubleValue.
     */
    public Double getDoubleValue()
    {
       return doubleValue;
    }
 
    /**
     * Set the doubleValue.
     * 
     * @param doubleValue the doubleValue.
     */
    public void setDoubleValue(Double doubleValue)
    {
       this.doubleValue = doubleValue;
    }
 
    /**
     * Get the floatvalue.
     * 
     * @return the floatvalue.
     */
    public float getFloatvalue()
    {
       return floatvalue;
    }
 
    /**
     * Set the floatvalue.
     * 
     * @param floatvalue the floatvalue.
     */
    public void setFloatvalue(float floatvalue)
    {
       this.floatvalue = floatvalue;
    }
 
    /**
     * Get the floatValue.
     * 
     * @return the floatValue.
     */
    public Float getFloatValue()
    {
       return floatValue;
    }
 
    /**
     * Set the floatValue.
     * 
     * @param floatValue the floatValue.
     */
    public void setFloatValue(Float floatValue)
    {
       this.floatValue = floatValue;
    }
 
    /**
     * Get the integervalue.
     * 
     * @return the integervalue.
     */
    public int getIntegervalue()
    {
       return integervalue;
    }
 
    /**
     * Set the integervalue.
     * 
     * @param integervalue the integervalue.
     */
    public void setIntegervalue(int integervalue)
    {
       this.integervalue = integervalue;
    }
 
    /**
     * Get the integerValue.
     * 
     * @return the integerValue.
     */
    public Integer getIntegerValue()
    {
       return integerValue;
    }
 
    /**
     * Set the integerValue.
     * 
     * @param integerValue the integerValue.
     */
    public void setIntegerValue(Integer integerValue)
    {
       this.integerValue = integerValue;
    }
 
    /**
     * Get the longvalue.
     * 
     * @return the longvalue.
     */
    public long getLongvalue()
    {
       return longvalue;
    }
 
    /**
     * Set the longvalue.
     * 
     * @param longvalue the longvalue.
     */
    public void setLongvalue(long longvalue)
    {
       this.longvalue = longvalue;
    }
 
    /**
     * Get the longValue.
     * 
     * @return the longValue.
     */
    public Long getLongValue()
    {
       return longValue;
    }
 
    /**
     * Set the longValue.
     * 
     * @param longValue the longValue.
     */
    public void setLongValue(Long longValue)
    {
       this.longValue = longValue;
    }
 
    /**
     * Get the shortvalue.
     * 
     * @return the shortvalue.
     */
    public short getShortvalue()
    {
       return shortvalue;
    }
 
    /**
     * Set the shortvalue.
     * 
     * @param shortvalue the shortvalue.
     */
    public void setShortvalue(short shortvalue)
    {
       this.shortvalue = shortvalue;
    }
 
    /**
     * Get the shortValue.
     * 
     * @return the shortValue.
     */
    public Short getShortValue()
    {
       return shortValue;
    }
 
    /**
     * Set the shortValue.
     * 
     * @param shortValue the shortValue.
     */
    public void setShortValue(Short shortValue)
    {
       this.shortValue = shortValue;
    }
 
    /**
     * Get the stringValue.
     * 
     * @return the stringValue.
     */
    public String getStringValue()
    {
       return stringValue;
    }
 
    /**
     * Set the stringValue.
     * 
     * @param stringValue the stringValue.
     */
    public void setStringValue(String stringValue)
    {
       this.stringValue = stringValue;
    }
 }
