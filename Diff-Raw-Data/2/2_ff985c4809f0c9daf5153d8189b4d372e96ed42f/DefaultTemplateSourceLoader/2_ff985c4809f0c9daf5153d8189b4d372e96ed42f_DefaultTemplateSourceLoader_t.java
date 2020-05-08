 package com.page5of4.mustache;
 
 import java.util.Locale;
 
 import org.apache.commons.io.IOUtils;
 import org.springframework.beans.BeansException;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 import org.springframework.core.io.Resource;
 import org.springframework.core.io.support.LocalizedResourceHelper;
 
 public class DefaultTemplateSourceLoader implements TemplateSourceLoader, ApplicationContextAware {
    private ApplicationContext applicationContext;
    private String basePath = "/WEB-INF/views/";
 
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
       this.applicationContext = applicationContext;
    }
 
    public String getBasePath() {
       return basePath;
    }
 
    public void setBasePath(String basePath) {
       this.basePath = basePath;
    }
 
    @Override
    public boolean containsSource(String path) {
      return getResource(getViewURI(path)).exists();
    }
 
    @Override
    public String getSource(String path) {
       String url = getViewURI(path);
       try {
          return IOUtils.toString(getResource(url).getInputStream());
       }
       catch(Exception e) {
          throw new RuntimeException(String.format("Error resolving: %s (%s)", url, path), e);
       }
    }
 
    private Resource getResource(String url) {
       try {
          LocalizedResourceHelper helper = new LocalizedResourceHelper(applicationContext);
          // Going to need to cache this, we could be called from another thread and won't have access to that Request.
          // Locale userLocale = RequestContextUtils.getLocale(servletRequest);
          Locale userLocale = Locale.US;
          return helper.findLocalizedResource(url, ".html", userLocale);
       }
       catch(Exception e) {
          throw new RuntimeException(String.format("Error resolving: %s", url), e);
       }
    }
 
    private String getViewURI(String view) {
       if(view.startsWith("/") || view.startsWith("\\")) return view;
       return basePath + view;
    }
 }
