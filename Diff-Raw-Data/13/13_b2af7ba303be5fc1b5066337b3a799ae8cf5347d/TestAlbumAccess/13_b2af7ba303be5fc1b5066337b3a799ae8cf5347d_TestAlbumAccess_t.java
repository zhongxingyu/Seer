 package ch.bergturbenthal.image.server;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
import org.eclipse.jgit.api.Git;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.core.io.ClassPathResource;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration
 public class TestAlbumAccess {
   @Configuration
   static class Config {
     private static ClassPathResource resource = new ClassPathResource("testalbum");
     static {
       try {
         final File dir = resource.getFile();
        Git.init().setDirectory(dir).call();
       } catch (final IOException e) {
         throw new RuntimeException("Cannot initialize Test", e);
       }
     }
 
     @Bean
     public AlbumAccess albumAccess() {
       final FileAlbumAccess fileAlbumAccess = new FileAlbumAccess();
       fileAlbumAccess.setBaseDir(resource);
       return fileAlbumAccess;
     }
 
     @Bean
     public ExecutorService executorService() {
       return Executors.newFixedThreadPool(4);
     }
   }
 
   @Autowired
   private AlbumAccess albumAccess;
   @Autowired
   private ExecutorService executorService;
 
   @Test
   public void testFindAlbums() throws InterruptedException {
     final Collection<Callable<Void>> runnables = new ArrayList<Callable<Void>>();
     for (final Album album : albumAccess.listAlbums().values()) {
       for (final AlbumImage image : album.listImages().values()) {
         runnables.add(new Callable<Void>() {
 
           @Override
           public Void call() throws Exception {
             final File thumbnail = image.getThumbnail(400, 400, false, false);
             System.out.println(image + ":" + thumbnail.length());
             return null;
           }
         });
       }
       System.out.println("- " + album + ":" + album.totalSize());
     }
     executorService.invokeAll(runnables);
   }
 }
