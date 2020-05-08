 package com.example.atlassian.stash.plugins.jenkins;
 
 import com.atlassian.stash.hook.repository.*;
 import com.atlassian.stash.repository.*;
import com.atlassian.stash.server.ApplicationPropertiesService;
 import com.atlassian.stash.setting.*;
 import java.net.URL;
 import java.util.Collection;
 
 public class JenkinsPostReceiveRepositoryHook implements AsyncPostReceiveRepositoryHook, RepositorySettingsValidator
 {
    private final ApplicationPropertiesService applicationProperties;

    public JenkinsPostReceiveRepositoryHook(ApplicationPropertiesService applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

     /**
      * Connects to a configured URL to notify of all changes.
      */
     @Override
     public void postReceive(RepositoryHookContext context, Collection<RefChange> refChanges)
     {
        String projectKey = context.getRepository().getProject().getKey();
        String repositoryName = context.getRepository().getName();

        String url = context.getSettings().getString("url") + "/git/notifyCommit?url=http://" +
                applicationProperties.getBaseUrl().getHost() + "/" + projectKey + "/" + repositoryName + ".git";

         if (url != null)
         {
             try
             {
                 new URL(url).openConnection().getInputStream().close();
             }
             catch (Exception e)
             {
                 e.printStackTrace();
             }
         }
     }
 
     @Override
     public void validate(Settings settings, SettingsValidationErrors errors, Repository repository)
     {
         if (settings.getString("url", "").isEmpty())
         {
             errors.addFieldError("url", "Url field is blank, please supply one");
         }
     }
 }
