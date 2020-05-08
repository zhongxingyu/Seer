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
 package org.jboss.reflect.plugins.introspection;
 
 import java.lang.reflect.ParameterizedType;
 
 import org.jboss.reflect.plugins.ClassInfoImpl;
 import org.jboss.reflect.spi.ClassInfo;
 import org.jboss.reflect.spi.DelegateClassInfo;
 import org.jboss.reflect.spi.TypeInfo;
 import org.jboss.reflect.spi.TypeInfoFactory;
 import org.jboss.util.JBossStringBuilder;
 
 /**
  * ParameterizedClassInfo.
  * 
  * @author <a href="adrian@jboss.com">Adrian Brock</a>
  * @version $Revision: 1.1 $
  */
 public class ParameterizedClassInfo extends DelegateClassInfo
 {
    /** The serialVersionUID */
    private static final long serialVersionUID = 2;
    
    /** The factory */
    protected transient IntrospectionTypeInfoFactoryImpl factory;
    
    /** The parameterized type */
    transient ParameterizedType parameterizedType;
    
    /** The owner type */
    private TypeInfo ownerType = ClassInfoImpl.UNKNOWN_TYPE;
    
    /** The type arguments */
    private TypeInfo[] typeArguments = ClassInfoImpl.UNKNOWN_TYPES;
    
    /** The component type */
    private transient TypeInfo componentType = ClassInfoImpl.UNKNOWN_TYPE;
    
    /** The key type */
    private transient TypeInfo keyType = ClassInfoImpl.UNKNOWN_TYPE;
    
    /** The key type */
    private transient TypeInfo valueType = ClassInfoImpl.UNKNOWN_TYPE;
    
    /**
     * Create a new ParameterizedClassInfo.
     *
     * @param factory the factory
     * @param delegate the raw array info
     * @param parameterizedType the parameterized  type
     */
    public ParameterizedClassInfo(IntrospectionTypeInfoFactoryImpl factory, ClassInfo delegate, ParameterizedType parameterizedType)
    {
       super(delegate);
       this.factory = factory;
       this.delegate = delegate;
       this.parameterizedType = parameterizedType;
    }
 
    public TypeInfoFactory getTypeInfoFactory()
    {
       return factory;
    }
 
    @Override
    public TypeInfo[] getActualTypeArguments()
    {
       if (typeArguments == ClassInfoImpl.UNKNOWN_TYPES)
          typeArguments = factory.getActualTypeArguments(this);
       return typeArguments;
    }
 
    @Override
    public TypeInfo getOwnerType()
    {
       if (ownerType == ClassInfoImpl.UNKNOWN_TYPE)
          ownerType = factory.getOwnerType(this);
       return ownerType;
    }
 
    @Override
    public ClassInfo getRawType()
    {
       return delegate;
    }
 
    @Override
    public TypeInfo getComponentType()
    {
       if (componentType == ClassInfoImpl.UNKNOWN_TYPE)
          componentType = factory.getComponentType(this);
       return componentType;
    }
 
    @Override
    public TypeInfo getKeyType()
    {
      if (keyType == ClassInfoImpl.UNKNOWN_TYPE);
          keyType = factory.getKeyType(this);
       return keyType;
    }
 
    @Override
    public TypeInfo getValueType()
    {
       if (valueType == ClassInfoImpl.UNKNOWN_TYPE)
          valueType = factory.getValueType(this);
       return valueType;
    }
 
    @Override
    public void toShortString(JBossStringBuilder buffer)
    {
       buffer.append(parameterizedType);
    }
 
    @Override
    protected void toString(JBossStringBuilder buffer)
    {
       buffer.append(parameterizedType);
    }
 }
