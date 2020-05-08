 /*
  * $Id$
  * --------------------------------------------------------------------------------------
  * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  */
 package org.mule.ibeans.module.xml;
 
 import org.mule.api.MuleContext;
 import org.mule.api.context.MuleContextAware;
 import org.mule.api.lifecycle.Disposable;
 import org.mule.api.registry.ResolverException;
 import org.mule.api.registry.TransformCriteria;
 import org.mule.api.registry.TransformerResolver;
 import org.mule.api.transformer.Transformer;
 import org.mule.config.i18n.CoreMessages;
 import org.mule.ibeans.module.xml.transformers.JAXBMarshallerTransformer;
 import org.mule.ibeans.module.xml.transformers.JAXBUnmarshallerTransformer;
 import org.mule.utils.AnnotationUtils;
 
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.WeakHashMap;
 
 import javax.xml.bind.JAXBContext;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  * TODO
  */
 public class JAXBTransformerResolver implements TransformerResolver, MuleContextAware, Disposable
 {
     /**
      * logger used by this class
      */
     protected transient final Log logger = LogFactory.getLog(JAXBTransformerResolver.class);
     private MuleContext muleContext;
 
     //We cache the the transformers, this will get cleared when the server shuts down
     private Map<String, Transformer> transformerCache = new WeakHashMap<String, Transformer>();
 
     //We cache the JAXB classes so we don't scan them each time a transformer is needed
     private Set<Class> jaxbClasses = new HashSet<Class>();
 
     public void setMuleContext(MuleContext context)
     {
         muleContext = context;
     }
 
     public Transformer resolve(TransformCriteria criteria) throws ResolverException
     {
         boolean marshal = false;
         Class annotatedType = null;
 
 
         String cacheKey = null;
         if (jaxbClasses.contains(criteria.getOutputType()))
         {
             cacheKey = criteria.getOutputType().getName() + "-unmarshal";
             annotatedType = criteria.getOutputType();
             marshal = false;
         }
         else
         {
             for (int i = 0; i < criteria.getInputTypes().length; i++)
             {
                 if (jaxbClasses.contains(criteria.getInputTypes()[i]))
                 {
                    cacheKey = criteria.getInputTypes()[i].getName() + "-marshal";
                     annotatedType = criteria.getInputTypes()[i];
                     marshal = true;
                     break;
                 }
 
             }
         }
         Transformer t = transformerCache.get(cacheKey);
         if (t != null)
         {
             return t;
         }
 
         JAXBContext jax;
 
         try
         {
             if (annotatedType == null)
             {
                 annotatedType = criteria.getOutputType();
                 boolean isJAXB = AnnotationUtils.hasAnnotationWithPackage("javax.xml.bind.annotation", annotatedType);
                 if (!isJAXB)
                 {
                     marshal = true;
                     for (int j = 0; j < criteria.getInputTypes().length; j++)
                     {
                         annotatedType = criteria.getInputTypes()[j];
                         isJAXB = AnnotationUtils.hasAnnotationWithPackage("javax.xml.bind.annotation", annotatedType);
                         if (isJAXB)
                         {
                             break;
                         }
                     }
                 }
 
                 if (!isJAXB)
                 {
                     return null;
                 }
                 jaxbClasses.add(annotatedType);
             }
 
             jax = muleContext.getRegistry().lookupObject(JAXBContext.class);
             if (jax == null)
             {
                 logger.info("No common JAXB context configured, creating a local one for: " + annotatedType);
                 jax = JAXBContext.newInstance(annotatedType);
             }
 
             if (marshal)
             {
                 t = new JAXBMarshallerTransformer(jax, criteria.getOutputType());
             }
             else
             {
                 t = new JAXBUnmarshallerTransformer(jax, criteria.getOutputType());
             }
 
             transformerCache.put(annotatedType.getName() + (marshal ? "-marshal" : "-unmarshal"), t);
             return t;
 
         }
         catch (Exception e)
         {
             //TODO
             throw new ResolverException(CoreMessages.createStaticMessage("Failed to unmarshal"), e);
         }
 
 
     }
 
     public void transformerChange(Transformer transformer, RegistryAction registryAction)
     {
         //nothing to do
     }
 
     public void dispose()
     {
         transformerCache.clear();
         jaxbClasses.clear();
     }
 }
