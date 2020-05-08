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
 package org.jboss.reflect.spi;
 
 import java.io.Serializable;
 
 import org.jboss.util.JBossObject;
 
 /**
  * A primitive value
  *
  * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
  * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
  */
 public class PrimitiveValue extends JBossObject implements Serializable, Value
 {
    /** serialVersionUID */
    private static final long serialVersionUID = 3907214866304741945L;
 
    /** The value */
    protected String value;
    
    /** The type */
    protected PrimitiveInfo type;
 
    /**
     * Create a primitive value
     */
    public PrimitiveValue()
    {
    }
 
    /**
     * Create a primitive value
     * 
     * @param value the value
     * @param type the type
     */
    public PrimitiveValue(String value, PrimitiveInfo type)
    {
       this.value = value;
       this.type = type;
    }
 
    /**
     * Get the value
     * 
     * @return the value
     */
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
       if (!(o instanceof PrimitiveValue)) return false;
 
       final PrimitiveValue primitiveValue = (PrimitiveValue) o;
 
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
