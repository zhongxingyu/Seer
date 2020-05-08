 package org.jenkinsci.plugins.recipe.mechanisms;
 
 import hudson.Extension;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.HttpResponse;
 import org.kohsuke.stapler.HttpResponses;
 import org.kohsuke.stapler.StaplerRequest;
 
 import java.io.IOException;
 import java.io.StringWriter;
 
 /**
  * @author Kohsuke Kawaguchi
  */
 public class CommunityMechanism extends ExportMechanism {
     @DataBoundConstructor
     public CommunityMechanism() {
     }
 
     public String getRecipeAsString() throws IOException {
         StringWriter sw = new StringWriter();
         getRecipe().writeTo(sw);
         return sw.toString();
     }
 
     @Override
     public HttpResponse doExport(StaplerRequest req) {
         return HttpResponses.forwardToView(this, "trampoline");
     }
 
     @Extension
     public static class DescriptorImpl extends ExportMechanismDescriptor {
         @Override
         public String getDisplayName() {
            return "Download to your computer";
         }
     }
 }
