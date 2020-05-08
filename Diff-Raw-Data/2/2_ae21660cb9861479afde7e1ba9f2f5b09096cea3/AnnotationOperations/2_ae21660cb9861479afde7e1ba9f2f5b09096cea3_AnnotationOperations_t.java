 /**
  * Copyright (c) 2011 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
  * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  */
 
 package org.sourcepit.modeling.common.impl;
 
 import org.sourcepit.modeling.common.Annotation;
 
 public final class AnnotationOperations
 {
    private AnnotationOperations()
    {
       super();
    }
 
    public static String getData(Annotation annotation, String key, String defaultValue)
    {
       final String value = annotation.getData().get(key);
       if (value != null)
       {
          return value;
       }
       return defaultValue;
    }
 
    public static void setData(Annotation annotation, String key, String value)
    {
       annotation.getData().put(key, value);
    }
 
    public static boolean getData(Annotation annotation, String key, boolean defaultValue)
    {
       final String value = annotation.getData().get(key);
       if (value != null)
       {
         return Boolean.valueOf(value);
       }
       return defaultValue;
    }
 
    public static void setData(Annotation annotation, String key, boolean value)
    {
       annotation.getData().put(key, Boolean.toString(value));
    }
 }
