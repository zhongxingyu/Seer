 package io.loader.jenkins.impl;
 
 import com.cloudbees.plugins.credentials.CredentialsDescriptor;
 import com.cloudbees.plugins.credentials.CredentialsProvider;
 import com.cloudbees.plugins.credentials.CredentialsScope;
 
 import hudson.Extension;
 import hudson.model.Item;
 import hudson.security.ACL;
 import hudson.util.FormValidation;
 import hudson.util.ListBoxModel;
 import hudson.util.Secret;
 import io.loader.jenkins.AbstractLoaderioCredential;
 import io.loader.jenkins.LoaderCredential;
 import io.loader.jenkins.api.LoaderAPI;
 import net.sf.json.JSONException;
 
 import org.apache.commons.lang.StringUtils;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.QueryParameter;
 import org.kohsuke.stapler.Stapler;
 
 import javax.mail.MessagingException;
 import javax.servlet.ServletException;
 import java.io.IOException;
 
 
 public class LoaderCredentialImpl extends AbstractLoaderioCredential {
 
     /**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private final Secret apiKey;
     private final String description;
 
     @DataBoundConstructor
     public LoaderCredentialImpl(String apiKey, String description) {
         this.apiKey = Secret.fromString(apiKey);
         this.description = description;
     }
 
     public String getDescription() {
         return description;
     }
 
     public Secret getApiKey() {
         return apiKey;
     }
 
     @Extension
     public static class DescriptorImpl extends CredentialsDescriptor {
 
         /**
          * {@inheritDoc}
          */
         @Override
         public String getDisplayName() {
             return Messages.LoaderCredential_DisplayName();
         }
 
         @Override
         public ListBoxModel doFillScopeItems() {
             ListBoxModel m = new ListBoxModel();
             m.add(CredentialsScope.GLOBAL.getDisplayName(), CredentialsScope.GLOBAL.toString());
             return m;
         }
 
 
 
         // Used by global.jelly to authenticate User key
         public FormValidation doTestConnection(@QueryParameter("apiKey") final String apiKey) throws MessagingException, IOException, JSONException, ServletException {
         	return checkLoaderKey(apiKey);
         }
         
     // Used by global.jelly to authenticate User key
         public FormValidation doTestExistedConnection(@QueryParameter("apiKey") final Secret apiKey) throws MessagingException, IOException, JSONException, ServletException {
             return checkLoaderKey(apiKey.getPlainText());
         }
         
         private FormValidation checkLoaderKey(final String apiKey) throws JSONException, IOException, ServletException {
         	LoaderAPI ldr = new LoaderAPI(apiKey);
             Boolean testValid = ldr.getTestApi();
             if (!testValid) {
                 return FormValidation.errorWithMarkup("API Key is Invalid");
             } else {
                 return FormValidation.ok("API Key is Valid.");
             }
         }
 
     }
     
 }
