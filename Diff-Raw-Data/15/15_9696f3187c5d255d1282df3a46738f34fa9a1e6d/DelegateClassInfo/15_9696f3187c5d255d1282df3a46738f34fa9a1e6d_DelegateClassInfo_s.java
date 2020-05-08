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
 package org.jboss.reflect.spi;
 
 import java.lang.annotation.Annotation;
 
 import org.jboss.util.JBossStringBuilder;
 
 /**
  * Delegate ClassInfo
  * 
  * @author <a href="adrian@jboss.com">Adrian Brock</a>
  * @version $Revision: 1.1 $
  */
 public class DelegateClassInfo extends AbstractTypeInfo implements ClassInfo, InterfaceInfo
 {
    /** The serialVersionUID */
    private static final long serialVersionUID = 6830276668550581673L;
    
    /** The delegate */
    protected ClassInfo delegate;
    
    /**
     * Create delegate class info
     *
     * @param delegate the raw array info
     * @throws IllegalArgumentException for a null delegate
     */
    public DelegateClassInfo(ClassInfo delegate)
    {
       this(delegate, false);
    }
    
    /**
     * Create delegate class info
     *
     * @param delegate the raw array info
     * @param allowNull whether to allow a null delegate
     */
    public DelegateClassInfo(ClassInfo delegate, boolean allowNull)
    {
       if (delegate == null && allowNull == false)
          throw new IllegalArgumentException("Null delegate");
       this.delegate = delegate;
    }
 
    /**
     * Whether the delegate is initialized
     * 
     * @return true when there is a delegate
     */
    public boolean isInitialized()
    {
       return (delegate != null);
    }
 
    /**
     * Set the delegate
     * 
     * @param delegate the delegate
     * @throws IllegalArgumentException for a null delegate or it is not a ClassInfo
     */
    public void setDelegate(TypeInfo delegate)
    {
       if (delegate == null)
          throw new IllegalArgumentException("Null delegate");
       if (delegate instanceof ClassInfo == false)
          throw new IllegalArgumentException("Delegate is not a ClassInfo " + delegate.getClass().getName());
       this.delegate = (ClassInfo) delegate;
    }
 
    /**
     * Set the delegate
     * 
     * @param delegate the delegate
     * @throws IllegalArgumentException for a null delegate
     */
    public void setDelegate(ClassInfo delegate)
    {
       if (delegate == null)
          throw new IllegalArgumentException("Null delegate");
       this.delegate = delegate;
    }
 
    public TypeInfoFactory getTypeInfoFactory()
    {
       return delegate.getTypeInfoFactory();
    }
 
    public ConstructorInfo getDeclaredConstructor(TypeInfo[] parameters)
    {
       return delegate.getDeclaredConstructor(parameters);
    }
 
    public ConstructorInfo[] getDeclaredConstructors()
    {
       return delegate.getDeclaredConstructors();
    }
 
    public FieldInfo getDeclaredField(String name)
    {
       return delegate.getDeclaredField(name);
    }
 
    public FieldInfo[] getDeclaredFields()
    {
       return delegate.getDeclaredFields();
    }
 
    public MethodInfo getDeclaredMethod(String name, TypeInfo[] parameters)
    {
       return delegate.getDeclaredMethod(name, parameters);
    }
 
    public MethodInfo[] getDeclaredMethods()
    {
       return delegate.getDeclaredMethods();
    }
 
    public InterfaceInfo[] getGenericInterfaces()
    {
       return delegate.getGenericInterfaces();
    }
 
    public ClassInfo getGenericSuperclass()
    {
       return delegate.getGenericSuperclass();
    }
 
    public InterfaceInfo[] getInterfaces()
    {
       return delegate.getInterfaces();
    }
 
    public String getName()
    {
       return delegate.getName();
    }
 
    public String getSimpleName()
    {
       return delegate.getSimpleName();
    }
 
    public ClassInfo getSuperclass()
    {
       return delegate.getSuperclass();
    }
 
    public boolean isInterface()
    {
       return delegate.isInterface();
    }
 
    public AnnotationValue getAnnotation(String name)
    {
       return delegate.getAnnotation(name);
    }
 
    public AnnotationValue[] getAnnotations()
    {
       return delegate.getAnnotations();
    }
 
    public boolean isAnnotationPresent(String name)
    {
       return delegate.isAnnotationPresent(name);
    }
 
    public <T extends Annotation> T getUnderlyingAnnotation(Class<T> annotationType)
    {
       return delegate.getUnderlyingAnnotation(annotationType);
    }
 
    public Annotation[] getUnderlyingAnnotations()
    {
       return delegate.getUnderlyingAnnotations();
    }
 
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationType)
    {
       return delegate.isAnnotationPresent(annotationType);
    }
 
    public int getModifiers()
    {
       return delegate.getModifiers();
    }
 
    public boolean isPublic()
    {
       return delegate.isPublic();
    }
 
    public boolean isStatic()
    {
       return delegate.isStatic();
    }
 
    public Object convertValue(Object value) throws Throwable
    {
       return delegate.convertValue(value);
    }
 
    public Object convertValue(Object value, boolean replaceProperties) throws Throwable
    {
       return delegate.convertValue(value, replaceProperties);
    }
 
    public TypeInfo getArrayType(int depth)
    {
       return delegate.getArrayType(depth);
    }
    
    @Deprecated
    public Class getType()
    {
       return delegate.getType();
    }
 
    @Override
    public boolean isArray()
    {
       return delegate.isArray();
    }
 
    @Override
    public boolean isEnum()
    {
       return delegate.isEnum();
    }
 
    @Override
    public boolean isPrimitive()
    {
       return delegate.isPrimitive();
    }
 
    public Object[] newArrayInstance(int size) throws Throwable
    {
       return delegate.newArrayInstance(size);
    }
 
    public boolean isAssignableFrom(TypeInfo info)
    {
       return delegate.isAssignableFrom(info);
    }
 
    public TypeInfo[] getActualTypeArguments()
    {
       return delegate.getActualTypeArguments();
    }
 
    public TypeInfo getOwnerType()
    {
       return delegate.getOwnerType();
    }
 
    public ClassInfo getRawType()
    {
       return delegate.getRawType();
    }
 
    public PackageInfo getPackage()
    {
       return delegate.getPackage();
    }
 
    @Override
    protected int getHashCode()
    {
       return delegate.hashCode();
    }
 
    @Override
    public boolean equals(Object obj)
    {
       if (obj == this)
          return true;
       
       if (obj == null || obj instanceof ClassInfo == false)
          return false;
       
       ClassInfo other = (ClassInfo) obj;
       ClassInfo otherDelegate = other;
       if (other instanceof DelegateClassInfo)
          otherDelegate = ((DelegateClassInfo) other).delegate;
       
       if (delegate.equals(otherDelegate) == false)
          return false;
       
       // We are equal to the raw type (seems hacky?)
       if (other instanceof DelegateClassInfo == false)
          return true;
       
       TypeInfo[] typeArguments = getActualTypeArguments();
       TypeInfo[] otherTypeArguments = other.getActualTypeArguments();
       if (typeArguments.length != otherTypeArguments.length)
          return false;
       
       for (int i = 0; i < typeArguments.length; ++i)
       {
          if (typeArguments[i].equals(otherTypeArguments[i]) == false)
             return false;
       }
       return true;
    }
 
    @Override
    public void toShortString(JBossStringBuilder buffer)
    {
       delegate.toShortString(buffer);
    }
 
    @Override
    protected void toString(JBossStringBuilder buffer)
    {
       delegate.toShortString(buffer);
    }
 }
