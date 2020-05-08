 package com.kreuz45.plugins;
 import hudson.Extension;
 import hudson.Launcher;
 import hudson.model.BuildListener;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.tasks.BuildStepDescriptor;
 import hudson.tasks.Builder;
 import hudson.util.FormValidation;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.List;
 
 import javax.servlet.ServletException;
 
 import net.sf.json.JSONObject;
 
 import org.apache.xmlrpc.XmlRpcException;
 import org.apache.xmlrpc.client.XmlRpcClient;
 import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.QueryParameter;
 import org.kohsuke.stapler.StaplerRequest;
 
 /**
  * Sample {@link Builder}.
  *
  * <p>
  * When the user configures the project and enables this builder,
  * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked
  * and a new {@link HelloWorldBuilder} is created. The created
  * instance is persisted to the project configuration XML by using
  * XStream, so this allows you to use instance fields (like {@link #url})
  * to remember the configuration.
  *
  * <p>
  * When a build is performed, the {@link #perform(AbstractBuild, Launcher, BuildListener)}
  * method will be invoked. 
  *
  * @author Kohsuke Kawaguchi
  */
 public class HelloWorldBuilder extends Builder {
 
     private final String url, user, password, title, body;
 
 	// Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
     @DataBoundConstructor
     public HelloWorldBuilder(String url, String user, String password, String title, String body) {
         this.url = url;
         this.user = user;
         this.password = password;
         this.title = title;
         this.body = body;
     }
 
     /**
      * We'll use this from the <tt>config.jelly</tt>.
      */
     public String getUrl() {
         return url;
     }
     public String getUser() {
 		return user;
 	}
 
 	public String getPassword() {
 		return password;
 	}
 
 	public String getTitle() {
 		return title;
 	}
 
 	public String getBody() {
 		return body;
 	}
 
     @Override
     public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
         // This is where you 'build' the project.
         // Since this is a dummy, we just say 'hello world' and call that a build.
 
         listener.getLogger().println("Hello, "+url+"!");
         try {
         	XmlRpcClient client = new XmlRpcClient();
             XmlRpcClientConfigImpl conf = new XmlRpcClientConfigImpl();
 			conf.setServerURL(new URL(url));
 			client.setConfig(conf);
 			
 			List params = new ArrayList();
 			params.add(1);
 			params.add(this.user);
 			params.add(this.password);
 			
 			Hashtable article = new Hashtable();
 			article.put("post_title", this.title);
 			article.put("post_content", this.body);
 			
 			params.add(article);
 			params.add(1);
 			
 			// s
			HashMap ret = (HashMap) client.execute("wp.newPost", params);
 			// T[õX|Xo
 			System.out.println("ret=" + ret);
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (XmlRpcException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
         
         
         return true;
     }
 
     // Overridden for better type safety.
     // If your plugin doesn't really define any property on Descriptor,
     // you don't have to do this.
     @Override
     public DescriptorImpl getDescriptor() {
         return (DescriptorImpl)super.getDescriptor();
     }
 
     /**
      * Descriptor for {@link HelloWorldBuilder}. Used as a singleton.
      * The class is marked as public so that it can be accessed from views.
      *
      * <p>
      * See <tt>src/main/resources/hudson/plugins/hello_world/HelloWorldBuilder/*.jelly</tt>
      * for the actual HTML fragment for the configuration screen.
      */
     @Extension // This indicates to Jenkins that this is an implementation of an extension point.
     public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
         /**
          * To persist global configuration information,
          * simply store it in a field and call save().
          *
          * <p>
          * If you don't want fields to be persisted, use <tt>transient</tt>.
          */
         private boolean useFrench;
 
         /**
          * Performs on-the-fly validation of the form field 'name'.
          *
          * @param value
          *      This parameter receives the value that the user has typed.
          * @return
          *      Indicates the outcome of the validation. This is sent to the browser.
          */
         public FormValidation doCheckUrl(@QueryParameter String value)
                 throws IOException, ServletException {
             if (value.length() == 0)
                 return FormValidation.error("Please set a name");
             if (value.length() < 4)
                 return FormValidation.warning("Isn't the name too short?");
             return FormValidation.ok();
         }
 
         public boolean isApplicable(Class<? extends AbstractProject> aClass) {
             // Indicates that this builder can be used with all kinds of project types 
             return true;
         }
 
         /**
          * This human readable name is used in the configuration screen.
          */
         public String getDisplayName() {
             return "Say hello world";
         }
 
         @Override
         public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
             // To persist global configuration information,
             // set that to properties and call save().
             useFrench = formData.getBoolean("useFrench");
             // ^Can also use req.bindJSON(this, formData);
             //  (easier when there are many fields; need set* methods for this, like setUseFrench)
             save();
             return super.configure(req,formData);
         }
 
         /**
          * This method returns true if the global configuration says we should speak French.
          *
          * The method name is bit awkward because global.jelly calls this method to determine
          * the initial state of the checkbox by the naming convention.
          */
         public boolean getUseFrench() {
             return useFrench;
         }
     }
 }
 
