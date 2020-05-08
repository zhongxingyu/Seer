 /*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
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
 package org.jboss.test.javabean.test;
 
 import java.beans.BeanInfo;
 import java.beans.PropertyDescriptor;
 import java.beans.Introspector;
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 
 import org.jboss.test.javabean.support.SimpleBean;
 import org.jboss.util.NestedRuntimeException;
 
 import junit.framework.Test;
 
 /**
  * PropertyTestCase.
 * 
  * @author <a href="adrian@jboss.com">Adrian Brock</a>
  * @version $Revision$
  */
 public class PropertyTestCase extends AbstractJavaBeanTest
 {
 //   private static DateFormat dateFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy");
   // even example from API doesn't work for me (alesj) - with EEE
    private static DateFormat dateFormat = new SimpleDateFormat("MMM d HH:mm:ss z yyyy");
 
    String stringValue =  "StringValue";
    Byte byteValue = new Byte("12");
    Boolean booleanValue = Boolean.TRUE;
    // TODO character
   // Character characterValue = new Character('a'); 
    Short shortValue = new Short("123");
    Integer integerValue = new Integer("1234");
    Long longValue = new Long("12345");
    Float floatValue = new Float("3.14");
    Double doubleValue = new Double("3.14e12");
 //   Date dateValue = createDate("Mon Jan 01 00:00:00 CET 2001");
    Date dateValue = createDate("Jan 01 00:00:00 CET 2001");
    BigDecimal bigDecimalValue = new BigDecimal("12e4");
    BigInteger bigIntegerValue = new BigInteger("123456");
 
    public void testConfigure() throws Exception
    {
       // tmp format
       System.setProperty("org.jboss.util.propertyeditor.DateEditor.format", "MMM d HH:mm:ss z yyyy");
       // check bean
       SimpleBean bean = unmarshal("TestConfigure.xml", SimpleBean.class);
       assertEquals("()", bean.getConstructorUsed());
      
       assertEquals(stringValue, bean.getAString());
       assertEquals(byteValue, bean.getAByte());
       assertEquals(booleanValue, bean.getABoolean());
      // TODO character 
       // assertEquals(characterValue, bean.getACharacter());
       assertEquals(shortValue, bean.getAShort());
       assertEquals(integerValue, bean.getAnInt());
       assertEquals(longValue, bean.getALong());
       assertEquals(floatValue, bean.getAFloat());
       assertEquals(doubleValue, bean.getADouble());
       assertEquals(dateValue, bean.getADate());
       assertEquals(bigDecimalValue, bean.getABigDecimal());
       assertEquals(bigIntegerValue, bean.getABigInteger());
       assertEquals(byteValue.byteValue(), bean.getAbyte());
       assertEquals(booleanValue.booleanValue(), bean.isAboolean());
       // TODO character
      // assertEquals(characterValue.charValue(), bean.getAchar()); 
       assertEquals(shortValue.shortValue(), bean.getAshort());
       assertEquals(integerValue.intValue(), bean.getAnint());
       assertEquals(longValue.longValue(), bean.getAlong());
       assertEquals(floatValue.floatValue(), bean.getAfloat());
       assertEquals(doubleValue.doubleValue(), bean.getAdouble());
       Number number = bean.getANumber();
       assertEquals(Long.class, number.getClass());
       assertEquals(longValue, number);
       assertEquals(stringValue, bean.getOverloadedProperty());
       // An all uppercase property
       assertEquals("XYZ", bean.getXYZ());
       assertEquals("abc", bean.getAbc());
    }
 
    /**
     * Validate the JavaBean property name introspection
     * @throws Exception
     */
    public void testJavaBeanMatching() throws Exception
    {
       BeanInfo info = Introspector.getBeanInfo(SimpleBean.class);
       PropertyDescriptor[] props = info.getPropertyDescriptors();
       HashMap<String, PropertyDescriptor> propMap = new HashMap<String, PropertyDescriptor>();
       for(PropertyDescriptor pd : props)
       {
          propMap.put(pd.getName(), pd);
       }
       assertNotNull("Has XYZ", propMap.get("XYZ"));
       assertNull("Does not have xYZ", propMap.get("xYZ"));
    }
 
    protected Date createDate(String date)
    {
       try
       {
          return dateFormat.parse(date);
       }
       catch (Exception e)
       {
          throw new NestedRuntimeException(e);
       }
    }
 
    /**
     * Setup the test
     * 
     * @return the test
     */
    public static Test suite()
    {
       return suite(PropertyTestCase.class);
    }
 
    /**
     * Create a new InstantiateTestCase.
     * 
     * @param name the test name
     */
    public PropertyTestCase(String name)
    {
       super(name);
    }
 
 }
