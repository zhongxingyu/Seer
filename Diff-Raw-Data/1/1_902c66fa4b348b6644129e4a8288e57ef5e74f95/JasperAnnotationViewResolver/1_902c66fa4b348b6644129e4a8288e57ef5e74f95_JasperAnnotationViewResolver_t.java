 /*
  * Copyright (c) 2013. Tomasz Szuba, Paulina Schab, Michał Tkaczyk. All rights reserved.
  */
 
 package com.miniinf.OSPManager.jasper;
 
 import org.springframework.util.AntPathMatcher;
 import org.springframework.web.servlet.View;
 import org.springframework.web.servlet.view.AbstractUrlBasedView;
 import org.springframework.web.servlet.view.AbstractView;
 import org.springframework.web.servlet.view.jasperreports.AbstractJasperReportsSingleFormatView;
 import org.springframework.web.servlet.view.jasperreports.JasperReportsViewResolver;
 
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.Map;
 
 /**
  * Created by Tomasz Szuba
  * Date: 23.12.12
  */
 public class JasperAnnotationViewResolver extends JasperReportsViewResolver {
 
     private Map<String, String> urlToReportPathMap = new HashMap<String, String>();
 
     private Map<String, Class<? extends AbstractJasperReportsSingleFormatView>> formatMappings;
 
     private AntPathMatcher matcher = new AntPathMatcher();
 
     public void addReportPath(String pattern, String reportPath) {
         urlToReportPathMap.put(pattern, reportPath);
     }
 
     public JasperAnnotationViewResolver() {
         this.formatMappings = new HashMap<>();
         this.formatMappings.put(".pdf", JRPdfWithPageSupport.class);
         this.formatMappings.put(".xls", JRXlsWithPageSupport.class);
     }
 
     @Override
     protected View loadView(String viewName, Locale locale) throws Exception {
         AbstractUrlBasedView view = buildView(viewName);
         if (view == null) {
             return null;
         }
         View result;
         try {
             result = applyLifecycleMethods(viewName, view);
         } catch (Exception e) {
            e.printStackTrace();
             return null;
         }
         return (view.checkResource(locale) ? result : null);
     }
 
     private View applyLifecycleMethods(String viewName, AbstractView view) {
         return (View) getApplicationContext().getAutowireCapableBeanFactory().initializeBean(view, viewName);
     }
 
     @Override
     protected AbstractUrlBasedView buildView(String viewName) throws Exception {
         String ext = viewName.substring(viewName.length() - 4);
         if (!ext.startsWith(".")) {
             ext = "";
         }
         Class<?> oldClass = getViewClass();
         Class<?> reportClass = formatMappings.get(ext);
         if (reportClass != null) {
             setViewClass(reportClass);
         }
         for (Map.Entry<String, String> entry : urlToReportPathMap.entrySet()) {
             if (matcher.match(entry.getKey(), viewName)) {
                 AbstractUrlBasedView ret = super.buildView(entry.getValue() + ext);
                 setViewClass(oldClass);
                 return ret;
             }
         }
         return null;
     }
 }
