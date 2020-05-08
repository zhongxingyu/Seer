 package org.sonatype.nexus.plugin.rdf.internal;
 
 import static org.sonatype.sisu.rdf.Names.LOCAL_STORAGE_DIR;
 
 import java.io.File;
 
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.inject.Singleton;
 
 import org.openrdf.repository.Repository;
 import org.openrdf.repository.RepositoryException;
 import org.sonatype.sisu.rdf.RepositoryHub;
 import org.sonatype.sisu.rdf.RepositoryIdentity;
 import org.sonatype.sisu.rdf.sesame.jena.JenaTDBRepository;
 
@Named( "jena-tdb" )
 @Singleton
 public class JenaTDBRepositoryFactory
     implements RepositoryHub.RepositoryFactory
 {
 
     private final File storageDir;
 
     @Inject
     public JenaTDBRepositoryFactory( @Named( LOCAL_STORAGE_DIR ) File storageDir )
     {
         this.storageDir = storageDir;
     }
 
     public Repository create( RepositoryIdentity id )
         throws RepositoryException
     {
         final File repoDir = new File( storageDir, id.stringValue() );
         repoDir.mkdirs();
         Repository repository = new JenaTDBRepository( repoDir );
 
         return repository;
     }
 
 }
