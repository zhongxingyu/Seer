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
 
 import java.io.ObjectStreamException;
 import java.util.HashMap;
 
 import org.jboss.reflect.plugins.ClassInfoImpl;
 import org.jboss.reflect.plugins.ValueConvertor;
 import org.jboss.reflect.plugins.introspection.IntrospectionTypeInfoFactory;
 
 /**
  * Primitive info
  *
  * TODO JBMICROCONT-118 fix the introspection assumption
  * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
  * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
  */
 public class PrimitiveInfo extends AbstractTypeInfo
 {
    /** serialVersionUID */
    private static final long serialVersionUID = 3256718498443835449L;
 
    /** The boolean info */
    public static final PrimitiveInfo BOOLEAN = new PrimitiveInfo("boolean", 0, Boolean.TYPE);
 
    /** The byte info */
    public static final PrimitiveInfo BYTE = new PrimitiveInfo("byte", 1, Byte.TYPE);
 
    /** The char info */
    public static final PrimitiveInfo CHAR = new PrimitiveInfo("char", 2, Character.TYPE);
 
    /** The double info */
    public static final PrimitiveInfo DOUBLE = new PrimitiveInfo("double", 3, Double.TYPE);
 
    /** The float info */
    public static final PrimitiveInfo FLOAT = new PrimitiveInfo("float", 4, Float.TYPE);
 
    /** The int info */
    public static final PrimitiveInfo INT = new PrimitiveInfo("int", 5, Integer.TYPE);
 
    /** The long info */
    public static final PrimitiveInfo LONG = new PrimitiveInfo("long", 6, Long.TYPE);
 
    /** The short info */
    public static final PrimitiveInfo SHORT = new PrimitiveInfo("short", 7, Short.TYPE);
 
    /** The void info */
    public static final PrimitiveInfo VOID = new PrimitiveInfo("void", 8, Void.TYPE);
 
    /** The primitives */
    private static final PrimitiveInfo[] values = {BOOLEAN, BYTE, CHAR, DOUBLE, FLOAT, INT, LONG, SHORT, VOID};
 
    /** The type info factory */
    protected static final TypeInfoFactory typeInfoFactory = new IntrospectionTypeInfoFactory();
 
    /** The name */
    protected final transient String name;
 
    /** The ordinal */
    protected final int ordinal;
    
    /** The type */
    protected final transient Class<? extends Object> type;
 
    /** The primitives */
    private static final HashMap<String, PrimitiveInfo> map = new HashMap<String, PrimitiveInfo>();
 
    static
    {
       map.put("boolean", BOOLEAN);
       map.put("byte", BYTE);
       map.put("char", CHAR);
       map.put("double", DOUBLE);
       map.put("float", FLOAT);
       map.put("int", INT);
       map.put("long", LONG);
       map.put("short", SHORT);
       map.put("void", VOID);
    }
 
    /**
     * Get the primitive info for a type
     * 
     * @param name the name
     * @return the info
     */
    public static PrimitiveInfo valueOf(String name)
    {
       return map.get(name);
    }
 
    /**
     * Create a new primitive info
     * 
     * @param name the name
     * @param ordinal the oridinal
     * @param type the class
     */
    protected PrimitiveInfo(String name, int ordinal, Class<? extends Object> type)
    {
       this.name = name;
       this.ordinal = ordinal;
       this.type = type;
    }
 
    /**
     * Get the ordinal
     * 
     * @return the oridinal
     */
    public int ordinal()
    {
       return ordinal;
    }
 
    public String getName()
    {
       return name;
    }
 
    public String getSimpleName()
    {
      return type.getSimpleName();
    }
    
    @Deprecated
    public Class<?> getType()
    {
       return type;
    }
    
    public Object convertValue(Object value) throws Throwable
    {
       Object progressResult = ValueConvertor.progressValue(type, value);
       if (progressResult != null)
       {
          return progressResult;
       }
       return ValueConvertor.convertValue(type, value);
    }
 
    public Object convertValue(Object value, boolean replaceProperties) throws Throwable
    {
       Object progressResult = ValueConvertor.progressValue(type, value);
       if (progressResult != null)
       {
          return progressResult;
       }
       return ValueConvertor.convertValue(type, value, replaceProperties);
    }
 
    @Override
    public boolean isPrimitive()
    {
       return true;
    }
 
    public TypeInfo getArrayType(int depth)
    {
       Class arrayClass = ClassInfoImpl.getArrayClass(getType(), depth);
       return typeInfoFactory.getTypeInfo(arrayClass);
    }
 
    public Object[] newArrayInstance(int size) throws Throwable
    {
       throw new UnsupportedOperationException("Not an array " + name);
    }
 
    public boolean isAssignableFrom(TypeInfo info)
    {
       return (info == this);
    }
 
    public TypeInfoFactory getTypeInfoFactory()
    {
       // FIXME - fix this once you fix the top TODO
       return typeInfoFactory;
    }
 
    @Override
    public String toString()
    {
       return name;
    }
 
    @Override
    public String toShortString()
    {
       return name;
    }
 
    @Override
    public boolean equals(Object obj)
    {
       if (obj == this)
          return true;
       if (obj == null)
          return false;
       if (!(obj instanceof PrimitiveInfo))
          return false;
       if (!obj.getClass().equals(this.getClass()))
          return false;
       PrimitiveInfo other = (PrimitiveInfo) obj;
       return other.ordinal == this.ordinal;
    }
 
    @Override
    public int hashCode()
    {
       return name.hashCode();
    }
 
    Object readResolve() throws ObjectStreamException
    {
       return values[ordinal];
    }
 }
