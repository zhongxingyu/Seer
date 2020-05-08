 package com.atlassian.plugin.webresource;
 
 import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
 
 /**
  * A way of linking to web 'resources', such as javascript or css.  This allows us to include resources once
  * on any given page, as well as ensuring that plugins can declare resources, even if they are included
  * at the bottom of a page.
  */
public class WebResourceModuleDescriptor extends AbstractModuleDescriptor<Object>
 {
     /**
      * As this descriptor just handles resources, you should never call this
      */
     public Object getModule()
     {
         throw new UnsupportedOperationException("There is no module for Web Resources");
     }
 }
