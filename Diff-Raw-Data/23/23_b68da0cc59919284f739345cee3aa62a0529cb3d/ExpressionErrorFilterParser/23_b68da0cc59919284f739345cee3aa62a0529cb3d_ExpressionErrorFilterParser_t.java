 /*
  * $Id$
  * --------------------------------------------------------------------------------------
  * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  */
 package org.mule.ibeans.internal.parsers;
 
 import org.mule.api.DefaultMuleException;
 import org.mule.api.MuleException;
 import org.mule.ibeans.api.client.filters.AtomErrorFilter;
 import org.mule.ibeans.api.client.filters.ExpressionErrorFilter;
 import org.mule.ibeans.api.client.filters.JsonErrorFilter;
 import org.mule.ibeans.api.client.filters.RssErrorFilter;
 import org.mule.ibeans.api.client.filters.XmlErrorFilter;
 import org.mule.ibeans.internal.client.ErrorExpressionFilter;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Field;
 
 /**
  * TODO
  */
 public class ExpressionErrorFilterParser implements ErrorFilterParser
 {
     public boolean isSupported(Annotation annotation)
     {
         return (annotation instanceof ExpressionErrorFilter ||
                 annotation instanceof XmlErrorFilter ||
                 annotation instanceof AtomErrorFilter ||
                 annotation instanceof RssErrorFilter ||
                 annotation instanceof JsonErrorFilter);
     }
 
     public ErrorFilterHolder parse(Annotation anno) throws MuleException
     {
         ErrorExpressionFilter errorFilter;
         try
         {
             String expr = (String) anno.annotationType().getMethod("expr").invoke(anno);
             String errorCode = (String) anno.annotationType().getMethod("errorCode").invoke(anno);
             String mimeType = (String) anno.annotationType().getMethod("mimeType").invoke(anno);

            Field f;
             String evaluator;

            try
             {
                f = anno.annotationType().getDeclaredField("eval");
                evaluator = (String) f.get(anno);
             }
            catch (NoSuchFieldException nsfe)
             {
                //This is a custom ExpressionErrorFilter
                evaluator = (String) anno.annotationType().getMethod("eval").invoke(anno);
             }
 
            //special handling for xpath queries
             if (evaluator.equals("xpath2"))
             {
                 expr = "[boolean]" + expr;
             }
             errorFilter = new ErrorExpressionFilter(evaluator, expr);
             errorFilter.setErrorCodeExpr(errorCode);
             return new ErrorFilterHolder(mimeType, errorFilter);
         }
         catch (Exception e)
         {
            throw new DefaultMuleException("Failed to parse error filter from annotation: " + anno, e);
         }
     }
 }
