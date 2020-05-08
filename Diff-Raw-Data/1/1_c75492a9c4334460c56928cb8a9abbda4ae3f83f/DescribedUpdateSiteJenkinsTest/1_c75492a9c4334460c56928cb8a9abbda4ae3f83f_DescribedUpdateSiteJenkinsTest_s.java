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
 
 
 import hudson.model.UpdateSite;
 import jenkins.model.Jenkins;
 
 import org.jvnet.hudson.test.HudsonTestCase;
 import org.jvnet.hudson.test.TestExtension;
import org.jvnet.hudson.test.HudsonTestCase.WebClient;
 import org.jvnet.hudson.test.recipes.LocalData;
 import org.kohsuke.stapler.DataBoundConstructor;
 
 import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
 import com.gargoylesoftware.htmlunit.html.HtmlForm;
 import com.gargoylesoftware.htmlunit.html.HtmlPage;
 
 /**
  * Tests for DescribedUpdateSite, concerned with Jenkins.
  */
 public class DescribedUpdateSiteJenkinsTest extends HudsonTestCase
 {
     public static class DescribedUpdateSiteForConfigureTest extends DescribedUpdateSite
     {
         private static final long serialVersionUID = -5159897288105253315L;
         
         private boolean editable = true;
         
         @Override
         public boolean isEditable()
         {
             return editable;
         }
         
         public void setEditable(boolean editable)
         {
             this.editable = editable;
         }
         
         private String testValue;
         
         public String getTestValue()
         {
             return testValue;
         }
         
         @DataBoundConstructor
         public DescribedUpdateSiteForConfigureTest(String id, String url, String testValue)
         {
             super(id, url);
             this.testValue = testValue;
         }
         
         @TestExtension("testDoConfigure")
         static public class DescriptorImpl extends Descriptor
         {
             @Override
             public String getDescription()
             {
                 return "DescribedUpdateSiteForConfigureTest";
             }
             
             @Override
             public String getDisplayName()
             {
                 return "DescribedUpdateSiteForConfigureTest";
             }
         }
     }
     public void testDoConfigure() throws Exception
     {
         String existingId = "test1";
         UpdateSite site1 = new UpdateSite(
             existingId,
             "http://example.com/test/update-center.json"
         );
         DescribedUpdateSiteForConfigureTest target = new DescribedUpdateSiteForConfigureTest(
             "test2",
             "http://example.com/test2/update-center.json",
             "Some value"
         );
         
         // Multiple update site.
         Jenkins.getInstance().getUpdateCenter().getSites().clear();
         Jenkins.getInstance().getUpdateCenter().getSites().add(site1);
         Jenkins.getInstance().getUpdateCenter().getSites().add(target);
         
         // can configure for editable
         {
             String originalId = target.getId();
             
             WebClient wc = new WebClient();
             
             HtmlPage editSitePage = wc.goTo(String.format("%s/%s", UpdateSitesManager.URL, target.getPageUrl()));
             
             HtmlForm editSiteForm = editSitePage.getFormByName("editSiteForm");
             assertNotNull("There must be editSiteForm", editSiteForm);
             
             String newId = "test3";
             String newUrl = "http://localhost/update-center.json";
             String newTestValue = "Some New Value";
             editSiteForm.getInputByName("_.id").setValueAttribute(newId);
             editSiteForm.getInputByName("_.url").setValueAttribute(newUrl);
             editSiteForm.getInputByName("_.testValue").setValueAttribute(newTestValue);
             submit(editSiteForm);
             
             DescribedUpdateSiteForConfigureTest site = null;
             for(UpdateSite s: Jenkins.getInstance().getUpdateCenter().getSites())
             {
                 if(newId.equals(s.getId()))
                 {
                     site = (DescribedUpdateSiteForConfigureTest)s;
                 }
                 assertFalse("id must be updated(old one must not remain)", originalId.equals(s.getId()));
             }
             assertNotNull("id must be updated", site);
             assertEquals("url must be updated", newUrl, site.getUrl());
             assertEquals("testValue must be updated", newTestValue, site.getTestValue());
             target = site;
         }
         
         // nothing changed
         {
             String id = target.getId();
             String url = target.getUrl();
             String testValue = target.getTestValue();
             
             WebClient wc = new WebClient();
             
             HtmlPage editSitePage = wc.goTo(String.format("%s/%s", UpdateSitesManager.URL, target.getPageUrl()));
             
             HtmlForm editSiteForm = editSitePage.getFormByName("editSiteForm");
             assertNotNull("There must be editSiteForm", editSiteForm);
             
             submit(editSiteForm);
             
             DescribedUpdateSiteForConfigureTest site = null;
             for(UpdateSite s: Jenkins.getInstance().getUpdateCenter().getSites())
             {
                 if(id.equals(s.getId()))
                 {
                     site = (DescribedUpdateSiteForConfigureTest)s;
                 }
             }
             assertNotNull("id must not be updated", site);
             assertEquals("url must not be updated", url, site.getUrl());
             assertEquals("testValue must not be updated", testValue, site.getTestValue());
             target = site;
         }
         
         // duplicate id
         {
             WebClient wc = new WebClient();
             wc.setPrintContentOnFailingStatusCode(false);
             
             HtmlPage editSitePage = wc.goTo(String.format("%s/%s", UpdateSitesManager.URL, target.getPageUrl()));
             
             HtmlForm editSiteForm = editSitePage.getFormByName("editSiteForm");
             assertNotNull("There must be editSiteForm", editSiteForm);
             
             editSiteForm.getInputByName("_.id").setValueAttribute(existingId);
             try
             {
                 submit(editSiteForm);
                 fail("This request must be rejected");
             }
             catch(FailingHttpStatusCodeException e)
             {
                 assertEquals("This request must be rejected with 400 Bad Request", 400, e.getStatusCode());
             }
         }
         
         // cannot configure for editable
         {
             WebClient wc = new WebClient();
             wc.setPrintContentOnFailingStatusCode(false);
             
             HtmlPage editSitePage = wc.goTo(String.format("%s/%s", UpdateSitesManager.URL, target.getPageUrl()));
             
             HtmlForm editSiteForm = editSitePage.getFormByName("editSiteForm");
             assertNotNull("There must be editSiteForm", editSiteForm);
             
             target.setEditable(false);
             
             try
             {
                 submit(editSiteForm);
                 fail("This request must be rejected");
             }
             catch(FailingHttpStatusCodeException e)
             {
                 assertEquals("This request must be rejected with 400 Bad Request", 400, e.getStatusCode());
             }
             
             editSitePage = wc.goTo(String.format("%s/%s", UpdateSitesManager.URL, target.getPageUrl()));
             editSiteForm = editSitePage.getFormByName("editSiteForm");
             
             assertEquals("no button must exists", 0, editSiteForm.getHtmlElementsByTagName("button").size());
             assertEquals("no button must exists", 0, editSiteForm.getSubmitButtons().size());
             
             target.setEditable(true);
         }
     }
     
     public static class DescribedUpdateSiteForDeleteTest extends DescribedUpdateSite
     {
         private static final long serialVersionUID = 2950050222218020632L;
         
         private boolean editable;
         
         @Override
         public boolean isEditable()
         {
             return editable;
         }
         
         public void setEditable(boolean editable)
         {
             this.editable = editable;
         }
 
         public DescribedUpdateSiteForDeleteTest(String id, String url)
         {
             super(id, url);
             this.editable = true;
         }
         
         @TestExtension("testDoDelete")
         public static class DescriptorImpl extends Descriptor
         {
             @Override
             public String getDescription()
             {
                 return "DescribedUpdateSiteForDeleteTest";
             }
             
             @Override
             public String getDisplayName()
             {
                 return "DescribedUpdateSiteForDeleteTest";
             }
         }
     }
     
     public void testDoDelete() throws Exception
     {
         // Can delete editable
         {
             UpdateSite site1 = new UpdateSite(
                 "test1",
                 "http://example.com/test/update-center.json"
             );
             DescribedUpdateSiteForDeleteTest target = new DescribedUpdateSiteForDeleteTest(
                 "test2",
                 "http://example.com/test2/update-center.json"
             );
             
             // Multiple update site.
             Jenkins.getInstance().getUpdateCenter().getSites().clear();
             Jenkins.getInstance().getUpdateCenter().getSites().add(site1);
             Jenkins.getInstance().getUpdateCenter().getSites().add(target);
             
             int initialSize = Jenkins.getInstance().getUpdateCenter().getSites().size();
             
             WebClient wc = new WebClient();
             
             HtmlPage deleteSitePage = wc.goTo(String.format("%s/%s/delete", UpdateSitesManager.URL, target.getPageUrl()));
             assertEquals("UpdateSite must not be deleted yet.", initialSize, Jenkins.getInstance().getUpdateCenter().getSites().size());
             
             HtmlForm deleteSiteForm = deleteSitePage.getFormByName("deleteSiteForm");
             assertNotNull("There must be deleteSiteForm", deleteSiteForm);
             
             submit(deleteSiteForm);
             assertEquals("UpdateSite must be deleted.", initialSize - 1, Jenkins.getInstance().getUpdateCenter().getSites().size());
         }
         
         // Cannot delete not editable
         {
             UpdateSite site1 = new UpdateSite(
                 "test1",
                 "http://example.com/test/update-center.json"
             );
             DescribedUpdateSiteForDeleteTest target = new DescribedUpdateSiteForDeleteTest(
                 "test2",
                 "http://example.com/test2/update-center.json"
             );
             
             // Multiple update site.
             Jenkins.getInstance().getUpdateCenter().getSites().clear();
             Jenkins.getInstance().getUpdateCenter().getSites().add(site1);
             Jenkins.getInstance().getUpdateCenter().getSites().add(target);
             
             int initialSize = Jenkins.getInstance().getUpdateCenter().getSites().size();
             
             WebClient wc = new WebClient();
             wc.setPrintContentOnFailingStatusCode(false);
             
             HtmlPage deleteSitePage = wc.goTo(String.format("%s/%s/delete", UpdateSitesManager.URL, target.getPageUrl()));
             assertEquals("UpdateSite must not be deleted yet.", initialSize, Jenkins.getInstance().getUpdateCenter().getSites().size());
             
             HtmlForm deleteSiteForm = deleteSitePage.getFormByName("deleteSiteForm");
             assertNotNull("There must be deleteSiteForm", deleteSiteForm);
             
             target.setEditable(false);
             
             try
             {
                 System.out.println(deleteSiteForm.getActionAttribute());
                 submit(deleteSiteForm);
                 fail("This request must be rejected");
             }
             catch(FailingHttpStatusCodeException e)
             {
                 assertEquals("This request must be rejected with 400 Bad Request", 400, e.getStatusCode());
             }
             assertEquals("UpdateSite must not be deleted.", initialSize, Jenkins.getInstance().getUpdateCenter().getSites().size());
             
             try
             {
                 wc.goTo(String.format("%s/%s/delete", UpdateSitesManager.URL, target.getPageUrl()));
                 fail("This request must be rejected");
             }
             catch(FailingHttpStatusCodeException e)
             {
                 assertEquals("This request must be rejected with 400 Bad Request", 400, e.getStatusCode());
             }
             assertEquals("UpdateSite must not be deleted.", initialSize, Jenkins.getInstance().getUpdateCenter().getSites().size());
         }
     }
     
     @LocalData
     public void testPrivilege() throws Exception
     {
         UpdateSite site = new UpdateSite(
             "test1",
             "http://example.com/test/update-center.json"
         );
         Jenkins.getInstance().getUpdateCenter().getSites().add(site);
         
         WebClient wcAdmin = new WebClient();
         wcAdmin.login("admin", "admin");
         
         WebClient wcUser = new WebClient();
         wcUser.setPrintContentOnFailingStatusCode(false);
         wcUser.login("user", "user");
         
         // configure
         {
             HtmlPage editSitePage = wcAdmin.goTo(String.format("%s/%s", UpdateSitesManager.URL, site.getId()));
             HtmlForm editSiteForm = editSitePage.getFormByName("editSiteForm");
             submit(editSiteForm);
         }
         {
             try
             {
                 wcUser.goTo(String.format("%s/%s", UpdateSitesManager.URL, site.getId()));
                 fail("Access without privilege must rejected");
             }
             catch(FailingHttpStatusCodeException e)
             {
                 // Rejecting with view causes 500 error...
             }
             /*
             HtmlPage editSitePage = wcUser.goTo(String.format("%s/%s", UpdateSitesManager.URL, site.getId()));
             HtmlForm editSiteForm = editSitePage.getFormByName("editSiteForm");
             try
             {
                 submit(editSiteForm);
                 fail("Access without privilege must rejected");
             }
             catch(FailingHttpStatusCodeException e)
             {
                 assertEquals("Access without privilege must rejected with 403 Forbidden", 403, e.getStatusCode());
             }
             */
         }
         
         // delete
         wcAdmin.goTo(String.format("%s/%s/delete", UpdateSitesManager.URL, site.getId()));
         try
         {
             wcUser.goTo(String.format("%s/%s/delete", UpdateSitesManager.URL, site.getId()));
             fail("Access without privilege must rejected");
         }
         catch(FailingHttpStatusCodeException e)
         {
             assertEquals("Access without privilege must rejected with 403 Forbidden", 403, e.getStatusCode());
         }
     }
 }
