 package org.sonatype.nexus.plugin.rdf.internal.capabilities;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Map;
 
 import org.apache.maven.model.Repository;
 import org.codehaus.plexus.util.StringUtils;
 import org.sonatype.sisu.maven.bridge.MavenBuilder;
 import org.sonatype.sisu.rdf.StatementsProducerContext;
 
 public class RDFConfiguration
     implements StatementsProducerContext
 {
 
     private final String repositoryId;
 
     private final String[] remoteRepositoriesIds;
 
     private final Repository[] remoteRepositories;
 
     private final String projectOwner;
 
     private final String defaultLicense;
 
     public RDFConfiguration( final Map<String, String> properties )
     {
         repositoryId = repository( properties );
         remoteRepositoriesIds = remoteRepositoriesIds( properties );
         remoteRepositories = remoteRepositories( repositoryId, remoteRepositoriesIds );
         projectOwner = projectOwner( properties );
         defaultLicense = defaultLicense( properties );
     }
 
     public String repositoryId()
     {
         return repositoryId;
     }
 
     public String[] remoteRepositoriesIds()
     {
         return remoteRepositoriesIds;
     }
 
     public Repository[] remoteRepositories()
     {
         return remoteRepositories;
     }
 
     public String projectOwner()
     {
         return projectOwner;
     }
 
     public String defaultLicense()
     {
         return defaultLicense;
     }
 
     @Override
     public int hashCode()
     {
         final int prime = 31;
         int result = 1;
         result = prime * result + ( ( defaultLicense == null ) ? 0 : defaultLicense.hashCode() );
         result = prime * result + ( ( projectOwner == null ) ? 0 : projectOwner.hashCode() );
         result = prime * result + Arrays.hashCode( remoteRepositoriesIds );
         result = prime * result + ( ( repositoryId == null ) ? 0 : repositoryId.hashCode() );
         return result;
     }
 
     @Override
     public boolean equals( Object obj )
     {
         if ( this == obj )
             return true;
         if ( obj == null )
             return false;
         if ( getClass() != obj.getClass() )
             return false;
         RDFConfiguration other = (RDFConfiguration) obj;
         if ( defaultLicense == null )
         {
             if ( other.defaultLicense != null )
                 return false;
         }
         else if ( !defaultLicense.equals( other.defaultLicense ) )
             return false;
         if ( projectOwner == null )
         {
             if ( other.projectOwner != null )
                 return false;
         }
         else if ( !projectOwner.equals( other.projectOwner ) )
             return false;
         if ( !Arrays.equals( remoteRepositoriesIds, other.remoteRepositoriesIds ) )
             return false;
         if ( repositoryId == null )
         {
             if ( other.repositoryId != null )
                 return false;
         }
         else if ( !repositoryId.equals( other.repositoryId ) )
             return false;
         return true;
     }
 
     @Override
     public String toString()
     {
         StringBuilder builder = new StringBuilder();
         builder.append( "RDFConfiguration [repositoryId=" );
         builder.append( repositoryId );
         builder.append( ", remoteRepositoriesIds=" );
         builder.append( Arrays.toString( remoteRepositoriesIds ) );
         builder.append( ", projectOwner=" );
         builder.append( projectOwner );
         builder.append( ", defaultLicense=" );
         builder.append( defaultLicense );
         builder.append( "]" );
         return builder.toString();
     }
 
     private static String repository( final Map<String, String> properties )
     {
         String repositoryId = properties.get( RDFCapabilityDescriptor.REPO_OR_GROUP_ID );
         repositoryId = repositoryId.replaceFirst( "repo_", "" );
         repositoryId = repositoryId.replaceFirst( "group_", "" );
         return repositoryId;
     }
 
     private static String[] remoteRepositoriesIds( final Map<String, String> properties )
     {
         final String remotes = properties.get( RemoteRepositoriesFormField.ID );
         if ( StringUtils.isBlank( remotes ) )
         {
             return null;
         }
 
         final String[] remoteRepositories = remotes.split( "," );
         return remoteRepositories;
     }
 
     private Repository[] remoteRepositories( String repositoryId, String[] remoteRepositoriesIds )
     {
         Collection<Repository> repositories = new ArrayList<Repository>();
         repositories.add( MavenBuilder.repository( repositoryId, "nexus://" + repositoryId ) );
         if ( remoteRepositoriesIds != null )
         {
             for ( String repoId : remoteRepositoriesIds )
             {
                 repositories.add( MavenBuilder.repository( repoId, "nexus://" + repoId ) );
             }
         }
         return repositories.toArray( new Repository[repositories.size()] );
     }
 
     private static String projectOwner( final Map<String, String> properties )
     {
         String projectOwner = properties.get( ProjectOwnerFormField.ID );
         if ( StringUtils.isBlank( projectOwner ) )
         {
             return null;
         }
         return projectOwner;
     }
 
     private static String defaultLicense( final Map<String, String> properties )
     {
        String defaultLicense = properties.get( DefaultLicenseFormField.ID );
         if ( StringUtils.isBlank( defaultLicense ) )
         {
             return null;
         }
         return defaultLicense;
     }
 
 }
