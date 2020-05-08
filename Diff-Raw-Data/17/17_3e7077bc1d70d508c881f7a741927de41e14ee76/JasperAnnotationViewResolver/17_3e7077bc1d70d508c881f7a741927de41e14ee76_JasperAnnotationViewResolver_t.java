 /*
  * Copyright (c) 2013. Tomasz Szuba, Paulina Schab, Michał Tkaczyk. All rights reserved.
  */
 
 package com.miniinf.OSPManager.jasper;
 
 import com.miniinf.OSPManager.util.ReverseComparator;
 import org.springframework.util.AntPathMatcher;
 import org.springframework.web.servlet.View;
 import org.springframework.web.servlet.view.AbstractUrlBasedView;
 import org.springframework.web.servlet.view.AbstractView;
 import org.springframework.web.servlet.view.jasperreports.AbstractJasperReportsSingleFormatView;
 import org.springframework.web.servlet.view.jasperreports.JasperReportsViewResolver;
 
import java.util.*;
 
 /**
  * Implementation of ViewResolver resolving view based on url and internal data mapping
  * ant-style pattern to JasperReport path
  * <p/>
  * Created by Tomasz Szuba
  * Date: 23.12.12
  */
 public class JasperAnnotationViewResolver extends JasperReportsViewResolver {
 
    private Map<String, String> urlToReportPathMap = new HashMap<>();
 
     private Map<String, Class<? extends AbstractJasperReportsSingleFormatView>> formatMappings;
 
     private AntPathMatcher matcher = new AntPathMatcher();
 
     /**
      * Add new ant-pattern to path match
      *
      * @param pattern    pattern being match to url
      * @param reportPath path to JasperReport report template
      */
     public void addReportPath(String pattern, String reportPath) {
         urlToReportPathMap.put(pattern, reportPath);
     }
 
     /**
      * Default constructor. Creates ViewResolver instance
      */
     public JasperAnnotationViewResolver() {
         this.formatMappings = new HashMap<>();
         this.formatMappings.put(".pdf", JRPdfWithPageSupport.class);
         this.formatMappings.put(".xls", JRXlsWithPageSupport.class);
     }
 
     /**
      * {@inheritDoc}
      */
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
 
     /**
      * Build view based on url and internal map
      *
      * @param viewName name of view to build
      * @return built view
      * @throws Exception problems during loading view
      */
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
        String[] sorted = urlToReportPathMap.keySet().toArray(new String[]{});
        Arrays.sort(sorted, matcher.getPatternComparator(viewName));
        for (String ant : sorted) {
            if (matcher.match(ant, viewName)) {
                AbstractUrlBasedView ret = super.buildView(urlToReportPathMap.get(ant) + ext);
                 setViewClass(oldClass);
                 return ret;
             }
         }
         return null;
     }
 }
