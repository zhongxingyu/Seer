 package com.atlassian.maven.plugins.amps.product;
 
 import com.atlassian.maven.plugins.amps.MavenGoals;
 import org.apache.maven.project.MavenProject;
 import org.apache.maven.plugin.logging.Log;
 
 import java.util.Arrays;
 import java.util.Collection;
 
 public class ProductHandlerFactory
 {
     public static final String REFAPP = "refapp";
     public static final String CONFLUENCE = "confluence";
     public static final String JIRA = "jira";
     public static final String BAMBOO = "bamboo";
     public static final String FECRU = "fecru";
     public static final String CROWD = "crowd";
 
     public static ProductHandler create(String id, MavenProject project, MavenGoals goals, Log log)
     {
         if (REFAPP.equals(id))
         {
             return new RefappProductHandler(project, goals);
         }
         else if (CONFLUENCE.equals(id))
         {
             return new ConfluenceProductHandler(project, goals);
         }
         else if (JIRA.equals(id))
         {
             return new JiraProductHandler(project, goals);
         }
         else if (BAMBOO.equals(id))
         {
             return new BambooProductHandler(project, goals);
         }
         else if (FECRU.equals(id))
         {
             return new FeCruProductHandler(project, goals, log);
         }
         else if (CROWD.equals(id))
         {
             return new CrowdProductHandler(project, goals);
         }
 
        throw new IllegalArgumentException("Unknown product id: '" + id + "' Valid values: "
            + Arrays.toString(getIds().toArray()));
     }
 
     public static Collection<String> getIds()
     {
         return Arrays.asList(REFAPP, CONFLUENCE, JIRA, BAMBOO, FECRU, CROWD);
     }
 }
