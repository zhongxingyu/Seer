 package com.atlassian.plugin.web.model;
 
 import com.atlassian.plugin.web.ContextProvider;
 import com.atlassian.plugin.web.WebFragmentHelper;
 import com.atlassian.plugin.web.descriptors.AbstractWebFragmentModuleDescriptor;
 import com.atlassian.plugin.loaders.LoaderUtils;
 
 import java.util.Map;
 import java.util.HashMap;
import java.util.TreeMap;
import java.util.SortedMap;
 
 import org.dom4j.Element;
 
 /**
 * Represents a sorted map of parameters. Individual value of the param can be rendered using velocity.
  */
 public class WebParam extends AbstractWebItem
 {
    /**
     * parameters are sorted in order for the i18n arguments to be in order
     */
    protected SortedMap params;
 
     public WebParam(Element element, WebFragmentHelper webFragmentHelper, ContextProvider contextProvider, AbstractWebFragmentModuleDescriptor descriptor)
     {
         super(webFragmentHelper, contextProvider, descriptor);
        this.params =  new TreeMap(LoaderUtils.getParams(element));
     }
 
     public WebParam(Map params, WebFragmentHelper webFragmentHelper, ContextProvider contextProvider, AbstractWebFragmentModuleDescriptor descriptor)
     {
         super(webFragmentHelper, contextProvider, descriptor);
        this.params = new TreeMap(params);
     }
 
    public SortedMap getParams()
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
