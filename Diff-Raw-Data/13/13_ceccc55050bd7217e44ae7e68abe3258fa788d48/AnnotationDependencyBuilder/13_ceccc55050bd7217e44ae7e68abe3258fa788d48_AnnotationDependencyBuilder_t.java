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
 package org.jboss.beans.metadata.plugins.annotations;
 
 import java.lang.annotation.Annotation;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jboss.beans.metadata.spi.annotations.DependencyFactory;
 import org.jboss.beans.metadata.spi.annotations.DependencyFactoryLookup;
 import org.jboss.classadapter.spi.ClassAdapter;
 import org.jboss.classadapter.spi.DependencyBuilder;
 import org.jboss.classadapter.spi.DependencyBuilderListItem;
 import org.jboss.metadata.spi.MetaData;
 import org.jboss.reflect.spi.ClassInfo;
 import org.jboss.reflect.spi.MethodInfo;
 
 /**
  * Create dependencies from annotations.
  *
  * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
  */
 public class AnnotationDependencyBuilder implements DependencyBuilder
 {
    protected static Class[] expectedAnnotations =
          {
             Callback.class,
             Install.class,
             Uninstall.class
          };
 
    @SuppressWarnings("unchecked")
    protected Class<? extends Annotation>[] getExpectedAnnotations()
    {
       return expectedAnnotations;
    }
 
    public List<DependencyBuilderListItem> getDependencies(ClassAdapter classAdapter, MetaData metaData)
    {
       ClassInfo classInfo = classAdapter.getClassInfo();
       if (classInfo != null)
       {
          List<DependencyBuilderListItem> list = new ArrayList<DependencyBuilderListItem>();
          MethodInfo[] methods = classInfo.getDeclaredMethods();
          if (methods != null)
          {
             for(MethodInfo mi : methods)
             {
                for(Class<? extends Annotation> annotation : getExpectedAnnotations())
                {
                   checkAnnotation(mi, annotation, list);
                }
             }
          }
          return list;
       }
       return null;
    }
 
    @SuppressWarnings("unchecked")
    protected void checkAnnotation(MethodInfo mi, Class<? extends Annotation> annotation, List<DependencyBuilderListItem> list)
    {
      Annotation expected = mi.getUnderlyingAnnotation(annotation);
      if (expected != null)
       {
         DependencyFactoryLookup dfl = expected.annotationType().getAnnotation(DependencyFactoryLookup.class);
          if (dfl == null)
            throw new IllegalArgumentException("Illegal annotation type - no DependencyFactoryLookup meta data: " + expected);
          DependencyFactory df = createDependencyFactory(dfl);
         list.add(df.createDependency(expected, mi));
       }
    }
 
    protected DependencyFactory createDependencyFactory(DependencyFactoryLookup dfl)
    {
       try
       {
          return dfl.value().newInstance();
       }
       catch (Exception e)
       {
          throw new Error("Cannot create DependencyFactory: " + e);
       }
    }
 }
