 /*
  * The MIT License
  * 
  * Copyright (c) 2013 IKEDA Yasuyuki
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
 package jp.ikedam.jenkins.plugins.updatesitesmanager;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.ServletException;
 
 import net.sf.json.JSONObject;
 
 import org.apache.commons.lang.StringUtils;
 import org.kohsuke.stapler.ForwardToView;
 import org.kohsuke.stapler.HttpRedirect;
 import org.kohsuke.stapler.HttpResponse;
 import org.kohsuke.stapler.QueryParameter;
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.StaplerResponse;
 import org.kohsuke.stapler.interceptor.RequirePOST;
 
 import jenkins.model.Jenkins;
 import jenkins.model.ModelObjectWithContextMenu;
 import hudson.DescriptorExtensionList;
 import hudson.ExtensionPoint;
 import hudson.Util;
 import hudson.model.Describable;
 import hudson.model.UpdateSite;
 import hudson.model.Descriptor.FormException;
 import hudson.util.FormValidation;
 
 /**
  * Base for UpdateSite that have Descriptor.
  */
 @SuppressWarnings("serial")
 abstract public class DescribedUpdateSite extends UpdateSite implements Describable<DescribedUpdateSite>, ExtensionPoint, Serializable, ModelObjectWithContextMenu
 {
     /**
      * Constructor
      * 
      * @param id
      * @param url
      */
     public DescribedUpdateSite(String id, String url)
     {
         super(StringUtils.trim(id), StringUtils.trim(url));
     }
     
     /**
      * Returns UpdateSite to register to Jenkins.
      * 
      * @return UpdateSite to register to Jenkins
      */
     public UpdateSite getUpdateSite()
     {
         return this;
     }
     
     /**
      * Returns the name of this site.
      * 
      * @return the name of this site
      * @see hudson.model.ModelObject#getDisplayName()
      */
     @Override
     public String getDisplayName()
     {
         return getId();
     }
     
     /**
      * Returns the relative URL to manage this UpdateSite.
      * 
      * This method is usually named getUrl.
      * But getUrl is already used in UpdateSite, use this method instead.
      * 
      * @return the relative URL to manage this UpdateSite
      */
     public String getPageUrl()
     {
         return Util.rawEncode(getId());
     }
     
     /**
      * Returns whether this UpdateSite is editable in UpdateSitesManager.
      * 
      * Return false if the UpdateSite should be managed in another place
      * (e.g. plugin managing its UpdateSite).
      * 
      * @return whether this UpdateSite is editable
      */
     public boolean isEditable()
     {
         return true;
     }
     
     /**
      * Returns whether this UpdateSite is disabled.
      * 
      * Returning true makes Jenkins ignore the plugins in this UpdateSite.
      * 
      * @return whether this UpdateSite is disabled.
      */
     public boolean isDisabled()
     {
         return false;
     }
     
     /**
      * Returns note
      * 
      * Provided for users to note about this UpdateSite.
      * Used only for displaying purpose.
      * 
      * @return note
      */
     public String getNote()
     {
         return "";
     }
     
     /**
      * Returns a list of plugins that should be shown in the "available" tab.
      * 
      * Returns nothing when disabled.
      * 
      * @return
      * @see hudson.model.UpdateSite#getAvailables()
      */
     @Override
     public List<Plugin> getAvailables()
     {
         if(isDisabled())
         {
             return new ArrayList<Plugin>(0);
         }
         return super.getAvailables();
     }
     
     /**
      * Returns the list of plugins that are updates to currently installed ones.
      * 
      * Returns nothing when disabled.
      * 
      * @return
      * @see hudson.model.UpdateSite#getUpdates()
      */
     @Override
     public List<Plugin> getUpdates()
     {
         if(isDisabled())
         {
             return new ArrayList<Plugin>(0);
         }
         return super.getUpdates();
     }
     
     /**
      * Returns true if it's time for us to check for new version.
      * 
      * Always returns false when disabled.
      * 
      * @return
      * @see hudson.model.UpdateSite#isDue()
      */
     @Override
     public boolean isDue()
     {
         if(isDisabled())
         {
             return false;
         }
         return super.isDue();
     }
     
     /**
      * Does any of the plugin has updates? 
      * 
      * Always returns false when disabled.
      * 
      * @return any of the plugin has updates?
      * @see hudson.model.UpdateSite#hasUpdates()
      */
     @Override
     public boolean hasUpdates()
     {
         if(isDisabled())
         {
             return false;
         }
         return super.hasUpdates();
     }
     
     /**
      * Process to update request.
      * 
      * @param req
      * @param rsp
      * @return
      * @throws FormException
      * @throws ServletException
      * @throws IOException
      */
     @RequirePOST
     public HttpResponse doConfigure(StaplerRequest req, StaplerResponse rsp) throws FormException, ServletException, IOException
     {
         Jenkins.getInstance().checkPermission(Jenkins.ADMINISTER);
         
         if(!isEditable())
         {
             throw new FormException("this site cannot be edited", "id");
         }
         
         JSONObject json = req.getSubmittedForm();
         DescribedUpdateSite newSite = req.bindJSON(getClass(), json);
         
         if(!newSite.getId().equals(getId()))
         {
             for(UpdateSite site: Jenkins.getInstance().getUpdateCenter().getSites())
             {
                 if(site.getId().equals(newSite.getId()))
                 {
                     // ID is duplicated.
                     throw new FormException("id is duplicated", "id");
                 }
             }
         }
         
         Jenkins.getInstance().getUpdateCenter().getSites().replace(
                 getUpdateSite(), 
                 newSite.getUpdateSite()
         );
         Jenkins.getInstance().getUpdateCenter().save();
         
         return new HttpRedirect(".."); // ${rootURL}/updatesites/
     }
     
     /**
      * Process delete request.
      * 
      * @param req
      * @param rsp
      * @return
      * @throws FormException
      * @throws IOException
      */
     public HttpResponse doDelete(StaplerRequest req, StaplerResponse rsp) throws FormException, IOException
     {
         Jenkins.getInstance().checkPermission(Jenkins.ADMINISTER);
         
         if(!isEditable())
         {
             throw new FormException("this site cannot be edited", "id");
         }
         
         if(!"POST".equals(req.getMethod()))
         {
             // Only POST method is acceptable.
             // If not POST method, show page to allow to POST request.
             return new ForwardToView(this, "delete.jelly");
         }
         
        Jenkins.getInstance().getUpdateCenter().getSites().remove(getUpdateSite());
         Jenkins.getInstance().getUpdateCenter().save();
         
         return new HttpRedirect(".."); // ${rootURL}/updatesites/
     }
     
     /**
      * Returns ContextMenu shown in UpdateSite list page.
      * 
      * @param request
      * @param response
      * @return
      * @throws Exception
      * @see jenkins.model.ModelObjectWithContextMenu#doContextMenu(org.kohsuke.stapler.StaplerRequest, org.kohsuke.stapler.StaplerResponse)
      */
     @Override
     public ContextMenu doContextMenu(StaplerRequest request, StaplerResponse response) throws Exception {
         return new ContextMenu().from(this,request,response);
     }
     
     /**
      * Returns all DescribedUpdateSite classes registered to Jenkins.
      * 
      * @return the list of Descriptor of DescribedUpdateSite subclasses.
      */
     static public DescriptorExtensionList<DescribedUpdateSite,DescribedUpdateSite.Descriptor> all()
     {
         return Jenkins.getInstance().<DescribedUpdateSite,DescribedUpdateSite.Descriptor>getDescriptorList(DescribedUpdateSite.class);
     }
     
     
     /**
      * Returns the descriptor for this class.
      * 
      * @return the descriptor for this class.
      * @see hudson.model.Describable#getDescriptor()
      */
     @Override
     public Descriptor getDescriptor()
     {
         return (Descriptor)Jenkins.getInstance().getDescriptorOrDie(getClass());
     }
     
     /**
      * Base class for Descriptor of subclass of DescribedUpdateSite
      */
     static public abstract class Descriptor extends hudson.model.Descriptor<DescribedUpdateSite>
     {
         /**
          * Return the description of this DescribedUpdateSite.
          * 
          * Shown when selecting DescribedUpdateSite to create.
          * 
          * @return the description of this DescribedUpdateSite
          */
         abstract public String getDescription();
         
         /**
          * Returns whether this DescribedUpdateSite can be used to create a new UpdateSite.
          * 
          * Return false for classes that is used for managing existing entry, but not for add new entry.
          * 
          * @return whether this DescribedUpdateSite can be used to create a new UpdateSite.
          */
         public boolean canCreateNewSite()
         {
             return true;
         }
         
         /**
          * Validate id
          * 
          * @param id
          * @return
          */
         public FormValidation doCheckId(@QueryParameter String id)
         {
             if(StringUtils.isBlank(id))
             {
                 return FormValidation.error(Messages.DescribedupdateSite_id_required());
             }
             return FormValidation.ok();
         }
         
         /**
          * Validate url
          * 
          * @param url
          * @return
          */
         public FormValidation doCheckUrl(@QueryParameter String url)
         {
             if(StringUtils.isBlank(url))
             {
                 return FormValidation.error(Messages.DescribedupdateSite_url_required());
             }
             
             URI uri;
             try
             {
                 uri = new URI(url);
             }
             catch(URISyntaxException e)
             {
                 return FormValidation.error(Messages.DescribedupdateSite_url_invalid(e.getLocalizedMessage()));
             }
             
             if(
                 StringUtils.isBlank(uri.getScheme())
                 || StringUtils.isBlank(uri.getHost())
             )
             {
                 return FormValidation.error(Messages.DescribedupdateSite_url_invalid("incomplete URI"));
             }
             return FormValidation.ok();
         }
     }
 }
