 package com.zenika.dorm.core.repository.impl;
 
 import com.google.inject.Inject;
 import com.zenika.dorm.core.exception.RepositoryException;
 import com.zenika.dorm.core.graph.Dependency;
 import com.zenika.dorm.core.model.DormFile;
 import com.zenika.dorm.core.model.DormMetadata;
 import com.zenika.dorm.core.model.impl.DefaultDormFile;
 import com.zenika.dorm.core.repository.DormRepositoryManager;
 import org.apache.ivy.plugins.repository.file.FileRepository;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.File;
 import java.io.IOException;
 
 /**
  * todo: need to refactor dorm file class, it's not correct. Need to use more ivy classes like FileResource
  * Nasty ivy repository manager, need refactoring
  *
  * @author Lukasz Piliszczuk <lukasz.piliszczuk AT zenika.com>
  */
 public class IvyRepositoryManager implements DormRepositoryManager {
 
     public static final File BASEDIR = new File("tmp/dorm-repo/");
 
     private static final Logger LOG = LoggerFactory.getLogger(IvyRepositoryManager.class);
 
     private FileRepository repository;
 
    @Inject
     public IvyRepositoryManager() {
        try {
            BASEDIR.mkdirs();
            repository = new FileRepository(BASEDIR.getAbsoluteFile());
        } catch (Exception e) {
            LOG.info("Cannot initiate the file repository", e);
            throw new RepositoryException("Cannot initiate the file repository", e);
        }
     }
 
     @Override
     public boolean put(Dependency dependency) {
 
         LOG.debug("Put dependency = " + dependency);
 
         DormFile file = dependency.getFile();
 
         if (file == null) {
             throw new RepositoryException("Dorm file is required");
         }
 
         String destination = dependency.getMetadata().getFullQualifier() + "/" +
                 dependency.getMetadata().getExtension().getQualifier();
 
         try {
             repository.put(file.getFile(), destination, true);
         } catch (IOException e) {
             throw new RepositoryException("Cannot put file to the repository", e);
         }
 
         LOG.debug("File is stored to the repository : " + BASEDIR + "/" + destination);
 
         return true;
     }
 
     @Override
     public DormFile get(DormMetadata metadata) {
 
         LOG.debug("Get dorm file by metadata : " + metadata);
 
         String location = getPathFromMetadata(metadata);
         LOG.debug("Get file at location : " + location);
 
         // todo: fix this with creating a repository resource
         File file = new File(BASEDIR + "/" + location);
 
         try {
             repository.get(location, file);
         } catch (IOException e) {
             throw new RepositoryException("Cannot get the following file from the repository : "
                     + location, e);
         }
 
         LOG.debug("File is stored at location : " + file.getPath());
 
         return DefaultDormFile.create(file);
     }
 
     private String getPathFromMetadata(DormMetadata metadata) {
         return metadata.getFullQualifier() + "/" + metadata.getExtension().getQualifier();
     }
 }
