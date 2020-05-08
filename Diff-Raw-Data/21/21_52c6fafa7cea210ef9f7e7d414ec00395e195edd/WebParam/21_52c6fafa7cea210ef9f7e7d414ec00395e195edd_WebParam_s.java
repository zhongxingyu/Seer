 package com.atlassian.plugin.web.model;
 
 import com.atlassian.plugin.web.ContextProvider;
 import com.atlassian.plugin.web.WebFragmentHelper;
 import com.atlassian.plugin.web.descriptors.AbstractWebFragmentModuleDescriptor;
 import com.atlassian.plugin.loaders.LoaderUtils;
 
 import java.util.Map;
 import java.util.HashMap;
 
 import org.dom4j.Element;
 
 /**
 * Represents a map of params. Individual value of the param
 * can be rendered
  */
 public class WebParam extends AbstractWebItem
 {
    protected Map params;
 
     public WebParam(Element element, WebFragmentHelper webFragmentHelper, ContextProvider contextProvider, AbstractWebFragmentModuleDescriptor descriptor)
     {
         super(webFragmentHelper, contextProvider, descriptor);
        this.params = LoaderUtils.getParams(element);
     }
 
     public WebParam(Map params, WebFragmentHelper webFragmentHelper, ContextProvider contextProvider, AbstractWebFragmentModuleDescriptor descriptor)
     {
         super(webFragmentHelper, contextProvider, descriptor);
        this.params = params;
     }
 
    public Map getParams()
     {
         return params;
     }
 
     public Object get(String key)
     {
         return params.get(key);
     }
 
     public String getRenderedParam(String paramKey)
     {
         return getRenderedParam(paramKey, new HashMap());
     }
 
     public String getRenderedParam(String paramKey, Map context)
     {
         context.putAll(getContextMap());
         return getWebFragmentHelper().renderVelocityFragment((String) params.get(paramKey), context);
     }
 }
