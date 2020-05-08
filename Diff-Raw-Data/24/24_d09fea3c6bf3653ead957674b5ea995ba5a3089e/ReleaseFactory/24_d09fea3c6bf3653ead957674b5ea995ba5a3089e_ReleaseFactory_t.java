 package edu.northwestern.bioinformatics.bering.runtime.classpath;
 
 import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;
 import org.apache.commons.io.FilenameUtils;
 import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
 import edu.northwestern.bioinformatics.bering.runtime.Release;
 import edu.northwestern.bioinformatics.bering.runtime.MigrationLoadingException;
 import edu.northwestern.bioinformatics.bering.runtime.Script;
 
 import java.io.IOException;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.ArrayList;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 
 /**
  * @author Rhett Sutphin
  */
 public class ReleaseFactory {
    private static final Log log = LogFactory.getLog(ReleaseFactory.class);

     private ScriptResource[] scriptResources;
     private Map<String,Release> releasesByName;
 
     public ReleaseFactory(Resource[] resources) {
         this.scriptResources = new ScriptResource[resources.length];
         for (int i = 0; i < resources.length; i++) {
             scriptResources[i] = new ScriptResource(resources[i]);
         }
         releasesByName = new HashMap<String, Release>();
     }
 
     public Release[] createReleases() {
         for (ScriptResource scriptResource : scriptResources) {
             Release release = getRelease(scriptResource);
             release.addScript(
                 createScript(scriptResource.getScriptName(), scriptResource.getResource(), release));
         }
         return new ArrayList<Release>(releasesByName.values()).toArray(new Release[releasesByName.size()]);
     }
 
     private Release getRelease(ScriptResource scriptResource) {
         if (!releasesByName.containsKey(scriptResource.getReleaseName())) {
             releasesByName.put(scriptResource.getReleaseName(), new Release(scriptResource.getReleaseName()));
         }
         return releasesByName.get(scriptResource.getReleaseName());
     }
 
     private Script createScript(String scriptName, Resource resource, Release release) {
         URL url = null;
         try {
            url = getUrl(resource);
             URI uri = url.toURI();
             return new Script(scriptName, uri, release);
         } catch (IOException e) {
             throw new MigrationLoadingException("Could not read contents of resource " + resource, e);
         } catch (URISyntaxException e) {
             throw new MigrationLoadingException("Could not convert resource URL (" + url + ") to URI", e);
         }
     }
 
    // Workaround for http://jira.springframework.org/browse/SPR-3899 .
    // Only affects Windows.  The bug is fixed in Spring 2.5
    private static URL getUrl(Resource resource) throws IOException {
        if (resource instanceof FileSystemResource) {
            return resource.getFile().toURI().toURL();
        } else {
            return resource.getURL();
        }
    }

     private static class ScriptResource {
         private Resource resource;
         private String releaseName;
         private String scriptName;
 
         public ScriptResource(Resource resource) {
             this.resource = resource;
             String[] urlComponents = getURL().split("/");
             releaseName = urlComponents[urlComponents.length - 2];
             scriptName = FilenameUtils.getBaseName(urlComponents[urlComponents.length - 1]);
         }
 
         private String getURL() {
             try {
                return getUrl(resource).toString();
             } catch (IOException e) {
                 throw new MigrationLoadingException("Could not get URL for " + resource, e);
             }
         }
 
         public Resource getResource() {
             return resource;
         }
 
         public String getReleaseName() {
             return releaseName;
         }
 
         public String getScriptName() {
             return scriptName;
         }
     }
 }
