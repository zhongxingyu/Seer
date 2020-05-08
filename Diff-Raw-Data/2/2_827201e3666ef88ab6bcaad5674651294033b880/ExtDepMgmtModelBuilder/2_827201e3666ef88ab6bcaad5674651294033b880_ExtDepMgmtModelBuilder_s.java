 package org.jboss.maven.extension.dependency;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.maven.model.Dependency;
 import org.apache.maven.model.building.DefaultModelBuilder;
 import org.apache.maven.model.building.ModelBuilder;
 import org.apache.maven.model.building.ModelBuildingException;
 import org.apache.maven.model.building.ModelBuildingRequest;
 import org.apache.maven.model.building.ModelBuildingResult;
 import org.codehaus.plexus.component.annotations.Component;
 import org.jboss.maven.extension.dependency.util.StdoutLogger;
 import org.jboss.maven.extension.dependency.util.SystemProperties;
 import org.codehaus.plexus.logging.Logger;
 
 @Component( role = ModelBuilder.class )
 public class ExtDepMgmtModelBuilder
     extends DefaultModelBuilder
     implements ModelBuilder
 {
 
     /**
      * The String separates parts of a Property name
      */
     private static final String PROPERTY_NAME_SEPERATOR = ":";
 
     /**
      * The String that needs to be prepended a system property to make it a version override. <br />
      * ex: -Dversion:junit:junit=4.10
      */
     private static final String VERSION_PROPERTY_NAME = "version" + PROPERTY_NAME_SEPERATOR;
 
     private static final Logger logger = new StdoutLogger();
 
     /**
      * Key: String of artifactID <br />
      * Value: Map of overrides for a groupID Inner Map Key: String of groupID Inner Map Value: String of desired
      * override version number
      */
     private final Map<String, Map<String, String>> groupOverrideMap;
 
     /**
      * Load overrides list when the object is instantiated
      */
     public ExtDepMgmtModelBuilder()
     {
         Map<String, String> propertyMap = SystemProperties.getPropertiesByPrepend( VERSION_PROPERTY_NAME );
 
         HashMap<String, Map<String, String>> groupOverrideMap = new HashMap<String, Map<String, String>>();
         Map<String, String> artifactOverrideMap;
 
         for ( String propertyName : propertyMap.keySet() )
         {
             // Split the name portion into parts (ex: junit:junit to {junit, junit})
             String[] propertyNameParts = propertyName.split( PROPERTY_NAME_SEPERATOR );
 
             if ( propertyNameParts.length == 2 )
             {
                 // Part 1 is the group name. ex: org.apache.maven.plugins
                 String groupID = propertyNameParts[0];
                 // Part 2 is the artifact ID. ex: junit
                 String artifactID = propertyNameParts[1];
 
                 // The value of the property is the desired version. ex: 3.0
                 String version = propertyMap.get( propertyName );
 
                 logger.debug( "Detected version override property. Group: " + groupID + "  ArtifactID: " + artifactID
                     + "  Target Version: " + version );
 
                 // Insert the override into override map
                 if ( groupOverrideMap.containsKey( groupID ) )
                 {
                     artifactOverrideMap = groupOverrideMap.get( groupID );
                     artifactOverrideMap.put( artifactID, version );
                 }
                 else
                 {
                     artifactOverrideMap = new HashMap<String, String>();
                     artifactOverrideMap.put( artifactID, version );
                     groupOverrideMap.put( groupID, artifactOverrideMap );
                 }
             }
             else
             {
                 logger.error( "Detected bad version override property. Name: " + propertyName );
             }
         }
 
         if ( groupOverrideMap.size() == 0 )
         {
             logger.debug( "No version overrides." );
         }
 
         this.groupOverrideMap = groupOverrideMap;
     }
 
     @Override
     public ModelBuildingResult build( ModelBuildingRequest request )
         throws ModelBuildingException
     {
         // logger.debug( "build(ModelBuildingRequest) called." );
         // System.out.println(">>>> build(ModelBuildingRequest) called.");
         ModelBuildingResult buildResult = super.build( request );
         return overrideVersions( request, buildResult );
     }
 
     @Override
     public ModelBuildingResult build( ModelBuildingRequest request, ModelBuildingResult result )
         throws ModelBuildingException
     {
         // logger.debug( "build(ModelBuildingRequest, ModelBuildingResult) called." );
         // System.out.println(">>>> build(ModelBuildingRequest, ModelBuildingResult) called.");
         ModelBuildingResult buildResult = super.build( request, result );
         return overrideVersions( request, buildResult );
     }
 
     /**
      * Override the versions of any matching artifacts in the given model
      * 
      * @param request
      * @param result
      * @return
      */
     private ModelBuildingResult overrideVersions( ModelBuildingRequest request, ModelBuildingResult result )
     {
         for ( Dependency dependency : result.getEffectiveModel().getDependencies() )
         {
             String currGroupID = dependency.getGroupId();
             if ( groupOverrideMap.containsKey( currGroupID ) )
             {
                 Map<String, String> artifactOverrideMap = groupOverrideMap.get( currGroupID );
                 String currArtifactID = dependency.getArtifactId();
                if ( artifactOverrideMap.containsKey( currGroupID ) )
                 {
                     String overrideVersion = artifactOverrideMap.get( currArtifactID );
                     String currVersion = dependency.getVersion();
                     if ( !currVersion.equals( overrideVersion ) )
                     {
                         dependency.setVersion( overrideVersion );
                         logger.debug( "Version of ArtifactID " + currArtifactID + " was overridden from " + currVersion
                             + " to " + dependency.getVersion() + " (" + overrideVersion + ")" );
                     }
                     else
                     {
                         logger.debug( "Version of ArtifactID " + currArtifactID
                             + " was the same as the override version (both are " + currVersion + ")" );
                     }
                 }
             }
         }
         return result;
     }
 }
