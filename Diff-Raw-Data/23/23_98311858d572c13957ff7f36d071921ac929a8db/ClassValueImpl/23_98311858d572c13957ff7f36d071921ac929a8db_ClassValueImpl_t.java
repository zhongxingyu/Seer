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
 
 import org.jboss.reflect.spi.ClassValue;
 import org.jboss.reflect.spi.TypeInfo;
 import org.jboss.util.JBossObject;
 
 /**
  * Class value
  *
  * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
  * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
  */
 public class ClassValueImpl extends JBossObject implements ClassValue, Serializable
 {
    /** serialVersionUID */
    private static final long serialVersionUID = 3256721801307566649L;
 
    /** The value */
    protected String value;
    
    /** The type */
    protected TypeInfo type;
    
    /** The hash code */
    protected int hash = -1;
 
    /**
     * Create a new class value
     */
    public ClassValueImpl()
    {
    }
 
    /**
     * Create a new ClassValue.
     * 
     * @param value the value
     * @param type the type
     */
    public ClassValueImpl(String value, TypeInfo type)
    {
       this.value = value;
       this.type = type;
       calculateHash();
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
       if (!(o instanceof ClassValueImpl)) return false;
 
       final ClassValueImpl classValue = (ClassValueImpl) o;
 
       if (!type.equals(classValue.type)) return false;
       if (!value.equals(classValue.value)) return false;
 
       return true;
    }
 
    public int hashCode() { return hash; }
 
    /**
     * Calculate the hash code
     */
    protected void calculateHash()
    {
       int result;
      result = (value != null) ? value.hashCode() : 0;
       result = 29 * result + type.hashCode();
       hash = result;
    }
 }
