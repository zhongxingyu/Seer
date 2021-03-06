 package org.apache.maven.mercury.artifact;
 
 import org.apache.maven.mercury.repository.api.RepositoryReader;
 
 /**
  * this is the most primitive metadata there is, usually used to query repository for "real" metadata.
  * It holds everything a project.dependencies.dependency element can have
  *
  *
  * @author Oleg Gusakov
  * @version $Id$
  *
  */
 public class ArtifactBasicMetadata
 {
   public static final String DEFAULT_ARTIFACT_TYPE = "jar";
   
   /** 
    * standard glorified artifact coordinates
    */
   protected String groupId;
 
   protected String artifactId;
 
   protected String version;
 
   // This is Maven specific. jvz/
   protected String classifier;
 
   protected String type = DEFAULT_ARTIFACT_TYPE;
   
   protected ArtifactScopeEnum artifactScope;
 
   protected String scope;
 
   protected boolean optional;
 
   /** metadata URI */
   protected String uri;
   
   // oleg: resolution convenience transient data
   
   /** which reader found it */
   transient RepositoryReader _reader;
   
   //------------------------------------------------------------------
   /**
    * create basic out of <b>group:artifact:version:classifier:type</b> string, use 
    * empty string to specify missing component - for instance query for common-1.3.zip
    * can be specified as ":common:1.3::zip" - note missing groupId and classifier. 
    */
   public static ArtifactBasicMetadata create( String query )
   {
     ArtifactBasicMetadata mdq = new ArtifactBasicMetadata();
     
     if( query == null )
       return null;
     
     String [] tokens = query.split(":");
     
     if( tokens == null || tokens.length < 1 )
       return mdq;
 
     int count = tokens.length;
     
     mdq.groupId = nullify( tokens[0] );
   
     if( count > 1 )
       mdq.artifactId = nullify( tokens[1] );
     
     if( count > 2 )
       mdq.version = nullify( tokens[2] );
     
     if( count > 3 )
       mdq.classifier = nullify( tokens[3] );
     
     if( count > 4 )
       mdq.type = nullify( tokens[4] );
     
     return mdq;
   }
   //---------------------------------------------------------------------------
   private static final String nullify( String s )
   {
     if( s == null || s.length() < 1 )
       return null;
     return s;
   }
   //---------------------------------------------------------------------
   public boolean sameGAV( ArtifactMetadata md )
   {
     if( md == null )
       return false;
     
     return 
         sameGA( md )
         && version != null
         && version.equals( md.getVersion() )
     ;
   }
   //---------------------------------------------------------------------
   public boolean sameGA( ArtifactMetadata md )
   {
     if( md == null )
       return false;
     
     return
         groupId != null
         && artifactId != null
         && groupId.equals( md.getGroupId() )
         && artifactId.equals( md.getArtifactId() )
     ;
   }
 
   public String getGA()
   {
     return toDomainString();
   }
 
   public String getGAV()
   {
     return toString();
   }
   
   private static final String nvl( String val, String dflt )
   {
     return val == null ? dflt : val;
   }
   private static final String nvl( String val )
   {
     return nvl(val,"");
   }
   
   @Override
   public String toString()
   {
       return nvl(groupId) + ":" + nvl(artifactId) + ":" + nvl(version) + ":" + nvl(classifier) + ":" + nvl(type);
   }
 
   public String toDomainString()
   {
       return groupId + ":" + artifactId;
   }
   
   public String getBaseName()
   {
     return artifactId + "-" + version + (classifier == null ? "" :"-"+classifier);
   }
   
   public String getBaseName( String classifier )
   {
    return artifactId + "-" + version + ((classifier == null||classifier.length()<1) ? "" :"-"+classifier);
   }
 
   public String getCheckedType()
   {
       return type == null ? "jar" : type;
   }
   //---------------------------------------------------------------------------
   public String getGroupId()
   {
     return groupId;
   }
   public void setGroupId(
       String groupId )
   {
     this.groupId = groupId;
   }
   public String getArtifactId()
   {
     return artifactId;
   }
   public void setArtifactId(
       String artifactId )
   {
     this.artifactId = artifactId;
   }
   public String getVersion()
   {
     return version;
   }
   public void setVersion(
       String version )
   {
     this.version = version;
   }
   public String getClassifier()
   {
     return classifier;
   }
   public void setClassifier(
       String classifier )
   {
     this.classifier = classifier;
   }
   public String getType()
   {
     return type;
   }
   public void setType(
       String type )
   {
     this.type = type;
   }
 
   public String getScope()
   {
       return getArtifactScope().getScope();
   }
 
   public ArtifactScopeEnum getScopeAsEnum()
   {
       return artifactScope == null ? ArtifactScopeEnum.DEFAULT_SCOPE : artifactScope;
   }
   
   public ArtifactScopeEnum getArtifactScope()
   {
       return artifactScope == null ? ArtifactScopeEnum.DEFAULT_SCOPE : artifactScope;
   }
 
   public void setArtifactScope( ArtifactScopeEnum artifactScope )
   {
       this.artifactScope = artifactScope;
   }
 
   public void setScope( String scope )
   {
       this.artifactScope = scope == null ? ArtifactScopeEnum.DEFAULT_SCOPE : ArtifactScopeEnum.valueOf( scope );
   }
   public boolean isOptional()
   {
     return optional;
   }
   public void setOptional(boolean optional)
   {
     this.optional = optional;
   }
   public void setOptional(String optional)
   {
     this.optional = "true".equals(optional) ? true : false;
   }
 
   public String getUri()
   {
       return uri;
   }
 
   public void setUri( String uri )
   {
       this.uri = uri;
   }
   
   public boolean hasClassifier()
   {
     return classifier == null;
   }
   
   
   public RepositoryReader getReader()
   {
     return _reader;
   }
   public void setReader( RepositoryReader reader )
   {
     this._reader = reader;
   }
   
   
   @Override
   public boolean equals( Object obj )
   {
     if( obj == null || !( obj instanceof ArtifactBasicMetadata ) )
       return false;
     
     return toString().equals( obj.toString() );
   }
   @Override
   public int hashCode()
   {
     return toString().hashCode();
   }
   
   
   //---------------------------------------------------------------------------
   //---------------------------------------------------------------------------
 }
