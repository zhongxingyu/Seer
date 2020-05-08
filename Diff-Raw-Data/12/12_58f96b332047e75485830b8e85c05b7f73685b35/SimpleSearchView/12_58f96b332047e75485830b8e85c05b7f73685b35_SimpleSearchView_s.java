 /*
  * The MIT License
  *
  * Copyright 2013 Oleg Nenashev <nenashev@synopsys.com>, Synopsys Inc.
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package com.synopsys.arc.jenkinsci.plugins.dynamic_search.views;
 
 import hudson.Extension;
 import hudson.Util;
 import hudson.model.Descriptor;
 import hudson.model.Hudson;
 import hudson.model.ListView;
 import hudson.model.TopLevelItem;
 import hudson.model.View;
 import hudson.model.ViewDescriptor;
 import hudson.search.Search;
 import hudson.util.FormValidation;
 import hudson.views.ViewJobFilter;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.regex.Pattern;
 import java.util.regex.PatternSyntaxException;
 import javax.servlet.ServletException;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.QueryParameter;
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.StaplerResponse;
 
 /**
  * List View with dynamic filters.
  * Class uses internal storage to pass parameters between pages.
  * @todo Add support of URLs
  * @todo Add "Save as view" button
  * @fixme Add garbage collector
  * @author Oleg Nenashev <nenashev@synopsys.com>, Synopsys Inc.
  */
 public class SimpleSearchView extends ListView {
 
     transient Map<String, JobsFilter> contextMap;
     
     @DataBoundConstructor
     public SimpleSearchView(String name) {
         super(name);
     } 
 
     public static String getSessionId() {
         return Hudson.SESSION_HASH;
     }
     
     @Override
     public Search getSearch() {
         return super.getSearch(); 
     }  
 
     public boolean hasConfiguredFilters() {
         return contextMap!=null && contextMap.containsKey(getSessionId());
     }
     
     public JobsFilter getFilters() {
         return hasConfiguredFilters() ? contextMap.get(getSessionId()) : new JobsFilter(this);
     }
     
     /**
      * Cleans internal cache of JSON Objects.
      * @todo Cleanup approach, replace for URL-based parameterization
      * @return sessionId
      */
     public String cleanCache() {
         String sessionId = getSessionId();
         if (contextMap.containsKey(sessionId)) {
             contextMap.remove(sessionId);
         }
         
         //TODO: garbage collector 
         
         return sessionId;
     }
     
     @Override
     public List<TopLevelItem> getItems() {
         // Handle filters from config
         List<TopLevelItem> res = super.getItems(); 
         
         // Handle user-specified filter
         if (hasConfiguredFilters()) {
             JobsFilter filters = contextMap.get(getSessionId());
             res = filters.doFilter(res, this);
         }
         return res;
     }
        
     public void doSearchSubmit(StaplerRequest req, StaplerResponse rsp) throws IOException, UnsupportedEncodingException, ServletException, Descriptor.FormException {
         Hudson.getInstance().checkPermission(View.READ);
          
         // Get filters
         JobsFilter filter = new JobsFilter(req, this);
         
         // Put Context to the map
         if (contextMap==null) {
             synchronized(this) {
                 contextMap = new ConcurrentHashMap<String, JobsFilter>();
             }
         }
         contextMap.put(getSessionId(), filter);
         
         // Redirect to the current page in order to reload list with filters
         rsp.sendRedirect(".");
       }
     
     @Extension
     public static final class DescriptorImpl extends ViewDescriptor {
         @Override
         public String getDisplayName() {
            return "SimpleSearchView";
        }

         /**
          * Checks if the include regular expression is valid.
          */
         public FormValidation doCheckIncludeRegex( @QueryParameter String value ) throws IOException, ServletException, InterruptedException  {
             String v = Util.fixEmpty(value);
             if (v != null) {
                 try {
                     Pattern.compile(v);
                 } catch (PatternSyntaxException pse) {
                     return FormValidation.error(pse.getMessage());
                 }
             }
             return FormValidation.ok();
         }
     }
   
     /**
     public Iterable<ViewJobFilter> getUserJobFilters() {
         return jobFilters;
     } */
 
     public boolean hasUserJobFilterExtensions() {
         return !ViewJobFilter.all().isEmpty();
     }
 }
