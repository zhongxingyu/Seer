 package org.jenkinsci.plugins.recipe.mechanisms;
 
 import hudson.Extension;
 import jenkins.model.Jenkins;
 import net.sf.json.JSONObject;
 import org.jenkinsci.plugins.recipe.Recipe;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.HttpResponse;
 import org.kohsuke.stapler.HttpResponses;
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.StaplerResponse;
 
 import javax.servlet.ServletException;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.net.HttpURLConnection;
 import java.net.URL;
 
 /**
  * Creates a Gist.
  *
  * @author Kohsuke Kawaguchi
  */
 public class GistMechanism extends ExportMechanism {
     @DataBoundConstructor
     public GistMechanism() {
     }
 
     @Override
     public HttpResponse doExport(StaplerRequest req) throws IOException {
         Recipe r = getRecipe();
         StringWriter sw = new StringWriter();
         r.writeTo(sw);
 
         JSONObject payload = new JSONObject()
                 .accumulate("description",r.getDisplayName())
                 .accumulate("public",true)
                 .accumulate("files", new JSONObject()
                         .accumulate(r.getFileName(), new JSONObject()
                                 .accumulate("content",sw.toString())));
 
         URL url = new URL("https://api.github.com/gists");
         HttpURLConnection con = (HttpURLConnection) Jenkins.getInstance().proxy.open(url);
         con.setRequestProperty("Content-type","application/json;charset=UTF-8");
         con.setRequestMethod("POST");
         con.setDoOutput(true);
 
         Writer out = new OutputStreamWriter(con.getOutputStream(),"UTF-8");
         out.write(payload.toString(2));
         out.close();
 
         if (con.getResponseCode()!=201) {
             return HttpResponses.error(500,"Failed to create Gist: "+con.getResponseCode()+":"+con.getResponseMessage());
         }
 
         return HttpResponses.redirectTo(
                 con.getHeaderField("Location").replace("api.github.com/gists","gist.github.com"));
     }
 
     public void doDynamic(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
         getRecipe().generateResponse(req, rsp, null);
     }
 
     @Extension
     public static class DescriptorImpl extends ExportMechanismDescriptor {
         @Override
         public String getDisplayName() {
            return "Create a Gist";
         }
     }
 }
 
