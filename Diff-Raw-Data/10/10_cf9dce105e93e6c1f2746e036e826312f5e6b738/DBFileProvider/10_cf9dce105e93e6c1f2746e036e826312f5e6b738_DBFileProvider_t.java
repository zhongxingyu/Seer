 package org.jboss.pressgang.ccms.provider;
 
 import javax.persistence.EntityManager;
 import java.util.List;
 
 import org.jboss.pressgang.ccms.model.File;
 import org.jboss.pressgang.ccms.model.LanguageFile;
 import org.jboss.pressgang.ccms.provider.listener.ProviderListener;
 import org.jboss.pressgang.ccms.wrapper.DBFileWrapper;
 import org.jboss.pressgang.ccms.wrapper.DBWrapperFactory;
 import org.jboss.pressgang.ccms.wrapper.FileWrapper;
import org.jboss.pressgang.ccms.wrapper.LanguageFileWrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.CollectionWrapper;
 
 public class DBFileProvider extends DBDataProvider implements FileProvider {
     protected DBFileProvider(EntityManager entityManager, DBWrapperFactory wrapperFactory, List<ProviderListener> listeners) {
         super(entityManager, wrapperFactory, listeners);
     }
 
     @Override
     public FileWrapper getFile(int id) {
         return getWrapperFactory().create(getEntity(File.class, id), false);
     }
 
     @Override
     public FileWrapper getFile(int id, Integer revision) {
         if (revision == null) {
             return getFile(id);
         } else {
            return getWrapperFactory().create(getRevisionEntity(File.class, id, revision), true);
         }
     }
 
    public CollectionWrapper<LanguageFileWrapper> getFileLanguageFiles(int id, Integer revision) {
         final DBFileWrapper file = (DBFileWrapper) getFile(id, revision);
         if (file == null) {
             return null;
         } else {
             return getWrapperFactory().createCollection(file.unwrap().getLanguageFiles(), LanguageFile.class, revision != null);
         }
     }
 
     @Override
     public CollectionWrapper<FileWrapper> getFileRevisions(int id, Integer revision) {
         final List<File> revisions = getRevisionList(File.class, id);
         return getWrapperFactory().createCollection(revisions, File.class, revision != null);
     }
 }
