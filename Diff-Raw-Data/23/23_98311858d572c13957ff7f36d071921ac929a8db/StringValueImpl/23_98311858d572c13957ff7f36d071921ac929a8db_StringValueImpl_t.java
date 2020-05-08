 /*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
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
 package org.jboss.reflect.plugins;
 
 import java.io.Serializable;
 
 import org.jboss.reflect.spi.StringValue;
 import org.jboss.reflect.spi.TypeInfo;
 import org.jboss.util.JBossObject;
 
 /**
  * A string value
  *
  * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
  * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
  */
 public class StringValueImpl extends JBossObject implements StringValue, Serializable
 {
    /** serialVersionUID */
    private static final long serialVersionUID = 3977862864859836468L;
 
    /** The value */
    protected String value;
    
    /** The type */
    protected TypeInfo type;
 
    /**
     * Create a new string value
     */
    public StringValueImpl()
    {
    }
    
    /**
     * Create a new string value
     * 
     * @param value the value
     * @param type the type
     */
    public StringValueImpl(String value, TypeInfo type)
    {
       this.value = value;
       this.type = type;
    }
 
    public String getValue()
    {
       return value;
    }
 
    public TypeInfo getType()
    {
       return type;
    }
 
    public boolean equals(Object o)
    {
       if (this == o) return true;
       if (!(o instanceof StringValueImpl)) return false;
 
       final StringValueImpl primitiveValue = (StringValueImpl) o;
 
       if (!type.equals(primitiveValue.type)) return false;
       if (!value.equals(primitiveValue.value)) return false;
 
       return true;
    }
 
    public int hashCode()
    {
       int result;
      result = (value != null) ? value.hashCode() : 0;
       result = 29 * result + type.hashCode();
       return result;
    }
 }
