 /*
  * Copyright (c) 2013. AgileApes (http://www.agileapes.scom/), and
  * associated organization.
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy of this
  * software and associated documentation files (the "Software"), to deal in the Software
  * without restriction, including without limitation the rights to use, copy, modify,
  * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
  * permit persons to whom the Software is furnished to do so, subject to the following
  * conditions:
  *
  * The above copyright notice and this permission notice shall be included in all copies
  * or substantial portions of the Software.
  */
 
 package com.agileapes.couteau.enhancer.impl;
 
 import com.agileapes.couteau.enhancer.api.ClassEnhancer;
 import com.agileapes.couteau.enhancer.api.NamingPolicy;
 import com.agileapes.couteau.enhancer.error.EnhancementError;
 import com.agileapes.couteau.enhancer.model.ClassEnhancementModel;
 import com.agileapes.couteau.freemarker.utils.FreemarkerUtils;
 import com.agileapes.couteau.lang.compiler.DynamicClassCompiler;
 import com.agileapes.couteau.lang.compiler.impl.DefaultDynamicClassCompiler;
 import com.agileapes.couteau.lang.error.CompileException;
 import freemarker.template.Configuration;
 import freemarker.template.Template;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.io.StringWriter;
 
 /**
  * This uses a freemarker template to generate code that marks an enhancement.
  *
  * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
  * @since 1.0 (2013/9/10, 15:15)
  */
 public class GeneratingClassEnhancer<E> implements ClassEnhancer<E> {
 
    private DynamicClassCompiler compiler;
     private NamingPolicy namingPolicy;
     private Class[] interfaces;
     private Class<? extends E> superClass;
    private final ClassLoader classLoader;
 
     public GeneratingClassEnhancer() {
         this(null);
     }
 
     public GeneratingClassEnhancer(ClassLoader classLoader) {
        this.classLoader = classLoader;
     }
 
     @Override
     public void setSuperClass(Class<? extends E> superClass) {
         this.superClass = superClass;
     }
 
     @Override
     public void setInterfaces(Class... interfaces) {
         this.interfaces = interfaces;
     }
 
     @Override
     public void setNamingPolicy(NamingPolicy namingPolicy) {
         this.namingPolicy = namingPolicy;
     }
 
     @Override
     public Class<? extends E> enhance() {
        this.compiler = new DefaultDynamicClassCompiler(classLoader != null ? classLoader : superClass.getClassLoader());
         final Configuration configuration = FreemarkerUtils.getConfiguration(getClass(), "/");
         final Template template;
         try {
             template = configuration.getTemplate("enhancedClass.ftl");
         } catch (IOException e) {
             throw new EnhancementError("There was an error looking up the template", e);
         }
         final ClassEnhancementModel model = new ClassEnhancementModel();
         model.setSuperClass(superClass);
         model.setInterfaces(interfaces);
         model.setEnhancedName(namingPolicy.getClassName(superClass, this));
         final StringWriter out = new StringWriter();
         try {
             template.process(model, out);
         } catch (Exception e) {
             throw new EnhancementError("Failed to process template", e);
         }
         final Class<?> compiled;
         try {
             compiled = compiler.compile(model.getEnhancedName(), new StringReader(out.toString()));
         } catch (CompileException e) {
             throw new EnhancementError("Compilation error", e);
         }
         //noinspection unchecked
         return (Class<? extends E>) compiled;
     }
 
     public DynamicClassCompiler getCompiler() {
         return compiler;
     }
 
 }
