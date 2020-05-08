 package com.ngdb.web.services.infrastructure;
 
 import com.ngdb.entities.ArticleFactory;
 import com.ngdb.entities.article.Article;
 import com.ngdb.entities.article.Game;
 import com.ngdb.entities.article.element.File;
 import com.ngdb.services.Cacher;
 import org.apache.commons.io.FileUtils;
 import org.apache.tapestry5.ioc.annotations.Inject;
 import org.apache.tapestry5.upload.services.UploadedFile;
 import org.hibernate.Session;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Collection;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 
 import static com.google.common.io.ByteStreams.toByteArray;
 import static com.google.common.io.Files.createParentDirs;
 import static com.google.common.io.Files.write;
 
 public class FileService {
 
 	private static final String FILE_ROOT = "/ngdb/files/";
 
     @Inject
     private Session session;
 
     @Inject
     private CurrentUser currentUser;
 
     @Inject
     private ArticleFactory articleFactory;
 
     @Inject
     private Cacher cacher;
 
     public File store(UploadedFile uploadedFile, Article article, String name, String type) {
 		try {
 			File file = createFileFromUploadedFile(uploadedFile, article);
             return addFile(article, name, type, file);
 		} catch (IOException e) {
 			throw new IllegalStateException("Cannot create file with name '" + uploadedFile.getFileName() + "' for article " + article.getId(), e);
 		}
 	}
 
     public File store(String url, Article article, String name, String type) {
         File file = new File(name, url, type);
         return addFile(article, name, type, file);
     }
 
     private File addFile(Article article, String name, String type, File file) {
         file.setName(name);
         file.setType(type);
         file.setArticle(article);
         article.addFile(file);
        file = (File) session.merge(file);
         currentUser.addFile(article);
         cacher.invalidateFilesOf(article);
         return file;
     }
 
     private File createFileFromUploadedFile(UploadedFile uploadedFile, Article article) throws IOException {
 		InputStream fromStream = uploadedFile.getStream();
 		String fileName = uploadedFile.getFileName();
         String parentFolder = FILE_ROOT + article.getId() + "/";
         java.io.File to = new java.io.File(new java.io.File(parentFolder), fileName);
         createParentDirs(to);
         byte[] from = toByteArray(fromStream);
         write(from, to);
         return new File(parentFolder + fileName);
     }
 
 	public void delete(File file, Article article) {
         try {
             cacher.invalidateFilesOf(article);
             java.io.File ioFile = new java.io.File(file.getUrl());
             FileUtils.forceDelete(ioFile);
             java.io.File parent = ioFile.getParentFile();
             if(parent.listFiles().length == 0) {
                 FileUtils.forceDelete(parent);
             }
         } catch (IOException e) {
             throw new IllegalStateException(e);
         }
 	}
 
     public Collection<File> getFilesOf(Article article) {
         if(cacher.hasFilesOf(article)) {
             return cacher.getFilesOf(article);
         }
         Set<File> files;
         if (article.isGame()) {
             Game game = (Game) article;
             files = new TreeSet<File>(game.getFiles().all());
             List<Game> relatedGames = articleFactory.findAllGamesByNgh(game.getNgh());
             for (Game relatedGame : relatedGames) {
                 files.addAll(relatedGame.getFiles().all());
             }
         } else {
             files = article.getFiles().all();
         }
         cacher.setFilesOf(article, files);
         return files;
     }
 }
