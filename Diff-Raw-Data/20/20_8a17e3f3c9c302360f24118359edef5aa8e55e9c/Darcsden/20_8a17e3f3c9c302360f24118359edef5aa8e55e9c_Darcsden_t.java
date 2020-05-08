 /*
  * LICENSE
  *
  * "THE BEER-WARE LICENSE" (Revision 42):
  * "Sven Strittmatter" <ich@weltraumschaf.de> wrote this file.
  * As long as you retain this notice you can do whatever you want with
  * this stuff. If we meet some day, and you think this stuff is worth it,
  * you can buy me a beer in return.
  */
 
 package org.jenkinsci.plugins.darcs.browsers;
 
 import org.jenkinsci.plugins.darcs.DarcsChangeSet;
 
 import java.io.IOException;
 import java.net.URL;
 import java.net.MalformedURLException;
 import javax.servlet.ServletException;
 
 import hudson.Extension;
import hudson.Util;
 import hudson.model.Descriptor;
 import hudson.scm.RepositoryBrowser;
 import hudson.util.FormValidation;
 
 import net.sf.json.JSONObject;
 
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.QueryParameter;
 import org.kohsuke.stapler.StaplerRequest;
 
 /**
  *
  * @author Sven Strittmatter <ich@weltraumschaf.de>
  */
 public final class Darcsden extends DarcsRepositoryBrowser {
     @Extension
     public static class DescriptorImpl extends Descriptor<RepositoryBrowser<?>> {
         public String getDisplayName() {
             return "Darcsden";
         }
 
         /**
          * Validates the URL given in the config formular.
          *
          * @todo implement check.
          * 
          * @param value
          * @return
          * @throws IOException
          * @throws ServletException
          */
         public FormValidation doCheck(@QueryParameter final String value) throws IOException, ServletException {
             return new FormValidation.URLCheck() {
                 @Override
                 protected FormValidation check() throws IOException, ServletException {
 
                     return FormValidation.ok();
                 }
             }.check();
         }
 
         @Override
         public DarcsWeb newInstance(StaplerRequest req, JSONObject formData) throws FormException {
             return req.bindParameters(DarcsWeb.class, "darcsden.darcs.");
         }
     }
 
 
     private static final long serialVersionUID = 1L;
 
     public final URL url;
     
     @DataBoundConstructor
     public Darcsden(URL url) throws MalformedURLException {
        this.url = new URL(Util.removeTrailingSlash(url.toString()));
     }
 
     public URL getChangeSetLink(DarcsChangeSet changeSet) throws IOException {
         String hash = changeSet.getHash();
         String shortHash = hash.substring(0, hash.lastIndexOf('-'));
         DarcsQueryBuilder query = new DarcsQueryBuilder(DarcsQueryBuilder.SeparatorType.SLASHES);
         query.add("patch")
              .add(shortHash);
 
         return new URL(url + query.toString());
     }
 
     public URL getFileDiffLink(DarcsChangeSet changeSet, String file) throws IOException {
         return null;
     }
 }
