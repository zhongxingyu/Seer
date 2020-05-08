 package edu.northwestern.bioinformatics.bering.tools;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 
 /**
  * @author Rhett Sutphin
  */
 public class UriTools {
     private static final String JAR_SCHEME = "jar";
 
     /**
      * Wraps/extends {@link java.net.URI#resolve} so that it works with <code>jar:file:</code>
      * "nested" URLs.
      */
     public static URI resolve(URI base, String path) {
         if (JAR_SCHEME.equals(base.getScheme()) && base.isOpaque()) {
            URI nested = URI.create(base.getSchemeSpecificPart());
             URI nestedResolved = nested.resolve(path);
             try {
                return new URI(JAR_SCHEME, nestedResolved.toString(), null);
             } catch (URISyntaxException e) {
                 throw new IllegalArgumentException(
                     "Unable to resolve " + path + " against " + base.toString(), e);
             }
         } else {
             return base.resolve(path);
         }
     }
 

     private UriTools() { }
 }
