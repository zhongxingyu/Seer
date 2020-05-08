 /**
  * Copyright (C) 2004 - 2009 Shopzilla, Inc. 
  * All rights reserved. Unauthorized disclosure or distribution is prohibited.
  */
 
 import com.google.common.collect.ImmutableList;
 import com.pettermahlen.bygg.configuration.ArtifactVersion;
 import com.pettermahlen.bygg.configuration.Plugins;
 
 import java.util.List;
 
 /**
  * TODO: document this class!
  *
  * @author Petter Måhlén
  * @since 04/03/2011
  */
 public class PluginConfiguration {
     public static Plugins plugins() {
        final List<ArtifactVersion> artifactVersions = ImmutableList.of(
                new ArtifactVersion(ProjectArtifacts.BYGG_TEST_PLUGIN, "1.0-SNAPSHOT"),
                new ArtifactVersion(ProjectArtifacts.GUICE, "2.0")
        );
 
         return new Plugins() {
            public List<ArtifactVersion> plugins() {
                 return artifactVersions;
             }
         };
     }
 }
