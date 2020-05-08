 package org.jboss.maven.extension.dependency.modelbuildingmodifier.versionoverride;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.maven.model.building.ModelBuildingResult;
 import org.codehaus.plexus.logging.Logger;
 import org.jboss.maven.extension.dependency.modelbuildingmodifier.versionoverride.target.OverrideTarget;
 import org.jboss.maven.extension.dependency.util.SystemProperties;
 import org.jboss.maven.extension.dependency.util.log.Logging;
 
 /**
  * Abstract class that provides fields and methods common to classes that need to override versions by groupID and
  * artifactID
  */
 public abstract class VersionOverrider
 {
     private static final Logger logger = Logging.getLogger();
 
     /**
      * The String separates parts of a Property name
      */
     protected static final String PROPERTY_NAME_SEPERATOR = ":";
 
     /**
      * Get a Map representing the overrides present in the system properties that are flagged with propertyNamePrepend
      * 
      * @param propertyNamePrepend A String ending in PROPERTY_NAME_SEPERATOR
      * @param description A short name describing the type of override
      * @return The override data as a two layer nested Map, semantically: <groupID, <artifactID, VersionOverride>>
      */
     protected Map<String, Map<String, VersionOverrideInfo>> getOverrideMap( String propertyNamePrepend,
                                                                             String description )
     {
         Map<String, String> propertyMap = SystemProperties.getPropertiesByPrepend( propertyNamePrepend );
 
         HashMap<String, Map<String, VersionOverrideInfo>> groupOverrideMap =
             new HashMap<String, Map<String, VersionOverrideInfo>>();
         Map<String, VersionOverrideInfo> artifactOverrideMap;
 
         for ( String propertyName : propertyMap.keySet() )
         {
             // Split the name portion into parts (ex: junit:junit to {junit, junit})
             String[] propertyNameParts = propertyName.split( PROPERTY_NAME_SEPERATOR );
 
             if ( propertyNameParts.length == 2 )
             {
                 // Part 1 is the group name. ex: org.apache.maven.plugins
                 String groupID = propertyNameParts[0];
                 // Part 2 is the artifact ID. ex: maven-compiler-plugin
                 String artifactID = propertyNameParts[1];
 
                 // The value of the property is the desired version. ex: 3.0
                 String version = propertyMap.get( propertyName );
 
                 logger.info( "Detected " + description + " version override property. GroupID: " + groupID
                     + "  ArtifactID: " + artifactID + "  Target Version: " + version );
 
                 // Create VersionOverride object
                 VersionOverrideInfo versionOverride = new VersionOverrideInfo( groupID, artifactID, version );
 
                 // Insert the override into override map
                 if ( groupOverrideMap.containsKey( groupID ) )
                 {
                     artifactOverrideMap = groupOverrideMap.get( groupID );
                     artifactOverrideMap.put( artifactID, versionOverride );
                 }
                 else
                 {
                     artifactOverrideMap = new HashMap<String, VersionOverrideInfo>();
                     artifactOverrideMap.put( artifactID, versionOverride );
                     groupOverrideMap.put( groupID, artifactOverrideMap );
                 }
             }
             else
             {
                 logger.error( "Detected bad " + description + " version override property. Name: " + propertyName );
             }
         }
 
         if ( groupOverrideMap.size() == 0 )
         {
             logger.debug( "No " + description + " version overrides." );
         }
 
         return groupOverrideMap;
     }
 
     /**
      * Apply version override to the current target if applicable.
      * 
      * @param result The model to override versions in
      * @param overrideMap The Map of overrides to apply
      * @param target The OverrideTarget to use
      * @param desc A short name describing the type of override
      * @return The modified model
      * @see OverrideTarget
      */
     protected ModelBuildingResult applyVersionToTargetInModel( ModelBuildingResult result,
                                                                Map<String, Map<String, VersionOverrideInfo>> overrideMap,
                                                                OverrideTarget target, String desc )
     {
         String currGroupID = target.getGroupId();
         if ( overrideMap.containsKey( currGroupID ) )
         {
             Map<String, VersionOverrideInfo> artifactOverrideMap = overrideMap.get( currGroupID );
             String currArtifactID = target.getArtifactId();
             if ( artifactOverrideMap.containsKey( currArtifactID ) )
             {
                 String overrideVersion = artifactOverrideMap.get( currArtifactID ).getVersion();
                 String currVersion = target.getVersion();
                if ( currVersion == null || !currVersion.equals( overrideVersion ) )
                 {
                     target.setVersion( overrideVersion );
                     logger.debug( desc + " version of ArtifactID " + currArtifactID + " was overridden from "
                         + currVersion + " to " + target.getVersion() + " (" + overrideVersion + ")" );
                 }
                 else
                 {
                     logger.debug( desc + " version of ArtifactID " + currArtifactID
                         + " was the same as the override version (both are " + currVersion + ")" );
                 }
                 artifactOverrideMap.get( currArtifactID ).setOverriden( true );
             }
         }
 
         return result;
     }
 }
