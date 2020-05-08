 /*
  * Copyright (c) 2013. Tomasz Szuba, Paulina Schab, Michał Tkaczyk. All rights reserved.
  */
 
 package com.miniinf.OSPManager.jasper;
 
 import org.springframework.beans.BeansException;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.config.BeanPostProcessor;
 import org.springframework.stereotype.Component;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 import java.lang.reflect.Method;
 
 import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;
 
 /**
  * Created by Tomasz Szuba
  * Date: 23.12.12
  */
 @Component
 public class ReportPathBeanPostProcessor implements BeanPostProcessor {
 
     public static final String[] EMPTY_STRING_ARRAY = new String[]{""};
 
    public static final String[] ASTERIX_STRING_ARRAY = new String[]{"*"};

     @Autowired(required = true)
     JasperAnnotationViewResolver resolver;
 
     private static String[] filterParams(String[] mappings) {
         String[] ret = new String[mappings.length];
         for (int i = 0; i < mappings.length; i++) {
             ret[i] = mappings[i].replaceAll("\\{.*}", "*");
         }
         return ret;
     }
 
     @Override
     public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
         Class<?> beanClass = bean.getClass();
         if (findAnnotation(beanClass, Controller.class) != null) {
             scanClass(beanClass);
         }
         return bean;
     }
 
     private void scanClass(Class<?> beanClass) {
         String[] classMappingValue = EMPTY_STRING_ARRAY;
         RequestMapping classMapping = findAnnotation(beanClass, RequestMapping.class);
         if (classMapping != null) {
             classMappingValue = filterParams(classMapping.value());
         }
         String classReportValue = "";
         ReportPath classReport = findAnnotation(beanClass, ReportPath.class);
         if (classReport != null) {
             classReportValue = classReport.value();
         }
         for (Method method : beanClass.getMethods()) {
             scanMethod(method, classMappingValue, classReportValue);
         }
     }
 
     private void scanMethod(Method method, String[] classMappings, String classReport) {
         RequestMapping methodMapping = findAnnotation(method, RequestMapping.class);
         if (methodMapping != null) {
             String[] methodMappingValue = methodMapping.value().length > 0 ?
                                                  filterParams(methodMapping.value()) : ASTERIX_STRING_ARRAY;
             ReportPath methodReport = findAnnotation(method, ReportPath.class);
             if (methodReport != null) {
                 addReportMapping(classMappings, methodMappingValue, classReport + methodReport.value());
             }
 
         }
     }
 
     private void addReportMapping(String[] classMappings, String[] methodMappings, String reportPath) {
         for (String classMapping : classMappings) {
             for (String methodMapping : methodMappings) {
                resolver.addReportPath(classMapping + methodMapping + "*", reportPath);
             }
         }
     }
 
     @Override
     public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
         return bean;
     }
 }
