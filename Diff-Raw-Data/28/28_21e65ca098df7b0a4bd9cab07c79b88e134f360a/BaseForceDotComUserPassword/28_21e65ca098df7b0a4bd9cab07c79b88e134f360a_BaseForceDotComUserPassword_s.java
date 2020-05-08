 package org.jenkinsci.plugins.jenkins_force;
 import org.jenkinsci.plugins.jenkins_force.ForceDotComUserPassword;
 import com.cloudbees.plugins.credentials.CredentialsDescriptor;
 import com.cloudbees.plugins.credentials.CredentialsScope;
 
 import hudson.Extension;
 import hudson.util.Secret;
 import org.kohsuke.stapler.DataBoundConstructor;
 
 public class BaseForceDotComUserPassword extends BaseForceDotComUser implements ForceDotComUserPassword
 {
     private static final long serialVersionUID = 1L;
 
     private final Secret password;
 
     @DataBoundConstructor
     public BaseForceDotComUserPassword( CredentialsScope scope, String id, String username, String password, String env, String description )
     {
         super( scope, id, username, env, description );
         this.password = Secret.fromString( password );
     }
 
     public Secret getPassword()
     {
         return password;
     }
 
     @Extension
     public static class DescriptorImpl extends CredentialsDescriptor
     {
         @Override
         public String getDisplayName()
         {
             return Messages.BaseForceDotComUserPassword_DisplayName();
         }
     }
 }
