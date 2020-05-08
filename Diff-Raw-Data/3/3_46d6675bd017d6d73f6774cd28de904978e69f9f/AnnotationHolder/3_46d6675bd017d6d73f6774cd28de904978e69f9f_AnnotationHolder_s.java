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
 
 import org.jboss.reflect.spi.AnnotatedInfo;
 import org.jboss.reflect.spi.AnnotationValue;
 import org.jboss.reflect.spi.AnnotationInfo;
 import org.jboss.util.JBossObject;
 
 import java.io.Serializable;
 import java.util.HashMap;
 
 /**
  * An annotation holder
  *
  * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
  * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
  */
 public class AnnotationHolder extends JBossObject implements AnnotatedInfo, Serializable
 {
    /** serialVersionUID */
    private static final long serialVersionUID = 3546645408219542832L;
 
    /** The annotations */
    protected AnnotationValue[] annotationsArray;
 
    /** Annotations map Map<String, AnnotationValue> */
    protected HashMap<String, AnnotationValue> annotationMap;
 
    /**
     * Create a new annotation holder
     */
    public AnnotationHolder()
    {
    }
 
    /**
     * Create a new AnnotationHolder.
     * 
     * @param annotations the annotations
     */
    public AnnotationHolder(AnnotationValue[] annotations)
    {
       setupAnnotations(annotations);
    }
 
    public AnnotationValue[] getAnnotations()
    {
       return annotationsArray;
    }
 
    public AnnotationValue getAnnotation(String name)
    {
       return annotationMap.get(name);
    }
 
    public boolean isAnnotationPresent(String name)
    {
       return annotationMap.containsKey(name);
    }
 
    /**
     * Set up the annotations
     * 
     * @param annotations the annotations
     */
    protected void setupAnnotations(AnnotationValue[] annotations)
    {
       if (annotations != null && annotations.length > 0)
       {
          this.annotationsArray = annotations;
          annotationMap = new HashMap<String, AnnotationValue>();
          for (int i = 0; i < annotations.length; i++)
          {
             AnnotationInfo type = annotations[i].getAnnotationType();
             annotationMap.put(type.getName(), annotations[i]);
          }
       }
    }
 }
