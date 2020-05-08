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
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.lang.reflect.Field;
 import java.lang.reflect.Modifier;
 import java.lang.reflect.ReflectPermission;
 import java.security.AccessController;
 import java.security.Permission;
 import java.security.PrivilegedAction;
 
 import org.jboss.reflect.plugins.FieldInfoImpl;
 import org.jboss.reflect.spi.AnnotationValue;
 import org.jboss.reflect.spi.ClassInfo;
 import org.jboss.reflect.spi.TypeInfo;
 
 /**
  * A field info
  *
  * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
  * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
  * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
  */
 public class ReflectFieldInfoImpl extends FieldInfoImpl
 {
    /** The serialVersionUID */
    private static final long serialVersionUID = 2;
 
    /** The permission */
    private static Permission accessCheck = new ReflectPermission("suppressAccessChecks");
 
    /** The field */
    protected transient Field field;
 
    /**
     * Create a new field info
     */
    public ReflectFieldInfoImpl()
    {
    }
 
    /**
     * Create a new FieldInfo.
     *
     * @param annotations the annotations
     * @param name the name
     * @param type the field type
     * @param modifiers the field modifiers
     * @param declaring the declaring class
     */
    public ReflectFieldInfoImpl(AnnotationValue[] annotations, String name, TypeInfo type, int modifiers, ClassInfo declaring)
    {
       super(annotations, name, type, modifiers, declaring);
    }
 
    /**
     * Set the field
     *
     * @param field the field
     */
    public void setField(Field field)
    {
      if (field != null)
         accessCheck(Modifier.isPublic(field.getModifiers()));

       this.field = field;

       if (isPublic() == false && field != null)
          setAccessible();
    }
 
    /**
     * Get the field
     *
     * @return the field
     */
    public Field getField()
    {
       accessCheck();
       return field;
    }
 
    /**
     * Check access permission.
     */
    protected final void accessCheck() // final because we don't want subclasses to disable it
    {
       accessCheck(isPublic());
    }
 
    /**
     * Check access permission.
     * 
     * @param isPublic whether the field is public
     */
    protected final void accessCheck(final boolean isPublic) // final because we don't want subclasses to disable it
    {
       if (isPublic == false)
       {
          SecurityManager sm = System.getSecurityManager();
          if (sm != null)
             sm.checkPermission(accessCheck);
       }
    }
 
    public Object get(Object target) throws Throwable
    {
       accessCheck();
       return ReflectionUtils.getField(field, target);
    }
 
    public Object set(Object target, Object value) throws Throwable
    {
       accessCheck();
       return ReflectionUtils.setField(field, target, value);
    }
 
    /**
     * Read the object, handling field read.
     *
     * @param oistream the stream
     * @throws IOException io error
     * @throws ClassNotFoundException cnf error
     * @throws NoSuchFieldException no such field error
     */
    @SuppressWarnings("deprecation")
    private void readObject(ObjectInputStream oistream)
          throws IOException, ClassNotFoundException, NoSuchFieldException
    {
       oistream.defaultReadObject();
       setField(ReflectionUtils.findExactField(getDeclaringClass().getType(), name));
    }
 
    /**
     * Set field accessible to true
     */
    private void setAccessible()
    {
       SecurityManager sm = System.getSecurityManager();
       if (sm == null)
          field.setAccessible(true);
       else
          AccessController.doPrivileged(new SetAccessible());
    }
 
    /**
     * Set accessible privileged block
     */
    private class SetAccessible implements PrivilegedAction<Object>
    {
       public Object run()
       {
          field.setAccessible(true);
          return null;
       }
    }
 }
