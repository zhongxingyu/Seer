 /*
  * JBoss, Home of Professional Open Source
  * Copyright 2008, Red Hat Middleware LLC, and individual contributors
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * http://www.apache.org/licenses/LICENSE-2.0
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,  
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.jboss.webbeans.introspector.jlr;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Type;
 import java.util.Set;
 
 import javax.enterprise.inject.spi.AnnotatedCallable;
 
 import org.jboss.webbeans.introspector.AnnotationStore;
 import org.jboss.webbeans.introspector.WBCallable;
 import org.jboss.webbeans.introspector.WBParameter;
 import org.jboss.webbeans.resources.ClassTransformer;
 
 /**
  * Represents a parameter
  * 
  * This class is immutable and therefore threadsafe
  * 
  * @author Pete Muir
  * 
  * @param <T>
  */
 public class WBParameterImpl<T, X> extends AbstractWBAnnotated<T, Object> implements WBParameter<T, X>
 {
    
    private static final Annotation[] EMPTY_ANNOTATION_ARRAY = new Annotation[0];
    
    // The final state
    private final boolean _final = false;
    // The static state
    private final boolean _static = false;
    private final boolean _public = false;
    private final boolean _private = false;
    private final boolean _packagePrivate = false;
    private final Package _package;
    private final int position;
    private final WBCallable<?, X, ?> declaringMember;
    
    private final String toString;
    
    public static <T, X> WBParameter<T, X> of(Annotation[] annotations, Class<T> rawType, Type type, WBCallable<?, X, ?> declaringMember, int position, ClassTransformer classTransformer)
    {
       return new WBParameterImpl<T, X>(annotations, rawType, type, declaringMember, position, classTransformer);
    }
    
    public static <T, X> WBParameter<T, X> of(Set<Annotation> annotations, Class<T> rawType, Type type, WBCallable<?, X, ?> declaringMember, int position, ClassTransformer classTransformer)
    {
       return new WBParameterImpl<T, X>(annotations.toArray(EMPTY_ANNOTATION_ARRAY), rawType, type, declaringMember, position, classTransformer);
    }
 
    /**
     * Constructor
     * 
     * @param annotations The annotations array
     * @param type The type of the parameter
     */
    protected WBParameterImpl(Annotation[] annotations, Class<T> rawType, Type type, WBCallable<?, X, ?> declaringMember, int position, ClassTransformer classTransformer)
    {
       super(AnnotationStore.of(annotations, annotations, classTransformer.getTypeStore()), rawType, type);
       this.declaringMember = declaringMember;
       this._package = declaringMember.getPackage();
       this.position = position;
      this.toString = new StringBuilder().append("parameter ").append(position + 1).append(" of ").append(declaringMember.toString()).toString();
    }
 
    /**
     * Indicates if the parameter is final
     * 
     * @return True if final, false otherwise
     * 
     * @see org.jboss.webbeans.introspector.WBAnnotated#isFinal()
     */
    public boolean isFinal()
    {
       return _final;
    }
 
    /**
     * Indicates if the parameter is static
     * 
     * @return True if static, false otherwise
     * 
     * @see org.jboss.webbeans.introspector.WBAnnotated#isStatic()
     */
    public boolean isStatic()
    {
       return _static;
    }
    
    public boolean isPublic()
    {
       return _public;
    }
    
    public boolean isPrivate()
    {
       return _private;
    }
    
    public boolean isPackagePrivate()
    {
       return _packagePrivate;
    }
    
    public Package getPackage()
    {
       return _package;
    }
 
    /**
     * Gets the name of the parameter
     * 
     * @throws IllegalArgumentException (not supported)
     * 
     * @see org.jboss.webbeans.introspector.WBAnnotated#getName()
     */
    public String getName()
    {
       throw new IllegalArgumentException("Unable to determine name of parameter");
    }
 
    /**
     * Gets a string representation of the parameter
     * 
     * @return A string representation
     */
    @Override
    public String toString()
    {
       return toString;
    }
 
    public AnnotatedCallable<X> getDeclaringCallable()
    {
       return declaringMember;
    }
 
    public int getPosition()
    {
       return position;
    }
    
    @Override
    public Object getDelegate()
    {
       return null;
    }
    
 }
