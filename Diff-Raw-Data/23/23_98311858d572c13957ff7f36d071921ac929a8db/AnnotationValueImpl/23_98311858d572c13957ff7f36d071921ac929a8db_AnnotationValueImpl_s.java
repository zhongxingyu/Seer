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
 import java.util.HashMap;
 
 import org.jboss.reflect.spi.AnnotationInfo;
 import org.jboss.reflect.spi.AnnotationValue;
 import org.jboss.reflect.spi.TypeInfo;
 import org.jboss.reflect.spi.Value;
 import org.jboss.util.JBossObject;
 
 /**
  * An annotation value
  *
  * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
  * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
  */
 public class AnnotationValueImpl extends JBossObject implements AnnotationValue, Serializable
 {
    /** serialVersionUID */
    private static final long serialVersionUID = 3257290210164289843L;
 
    /** The annotation type */
    protected AnnotationInfo annotationType;
    
    /** The attribute values */
    protected HashMap attributeValues;
    
    /** The hash code */
    protected int hash = -1;
 
    /**
     * Create a new Annotation value
     */
    public AnnotationValueImpl()
    {
    }
 
    /**
     * Create a new Annotation value
     * 
     * @param annotationType the annotation info
     * @param attributeValues the attribute values
     */
    public AnnotationValueImpl(AnnotationInfo annotationType, HashMap attributeValues)
    {
       this.annotationType = annotationType;
       this.attributeValues = attributeValues;
       calculateHash();
    }
 
    public AnnotationInfo getAnnotationType()
    {
       return annotationType;
    }
    
    public Value getValue(String attributeName)
    {
       return (Value) attributeValues.get(attributeName);
    }
 
    public TypeInfo getType()
    {
       return annotationType;
    }
 
    public boolean equals(Object o)
    {
       if (this == o) return true;
       if (!(o instanceof AnnotationValueImpl)) return false;
 
       final AnnotationValueImpl annotationValue = (AnnotationValueImpl) o;
 
       if (!annotationType.equals(annotationValue.annotationType)) return false;
       if (!attributeValues.equals(annotationValue.attributeValues)) return false;
 
       return true;
    }
 
    public int hashCode()
    {
       return hash;
    }
 
    /**
     * Calculate the hashcode
     */
    protected void calculateHash()
    {
       int result;
      result = annotationType.hashCode();
       result = 29 * result + attributeValues.hashCode();
       hash = result;
    }
 }
