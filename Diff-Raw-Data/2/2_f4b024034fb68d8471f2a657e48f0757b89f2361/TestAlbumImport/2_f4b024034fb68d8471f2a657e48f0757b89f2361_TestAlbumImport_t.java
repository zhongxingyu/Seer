 package ch.bergturbenthal.image.server;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.UUID;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import org.eclipse.jgit.api.Git;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.core.io.ClassPathResource;
 import org.springframework.core.io.FileSystemResource;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration
 public class TestAlbumImport {
   @Configuration
   static class Config {
    private static ClassPathResource resource = new ClassPathResource("photos/testalbum");
     private static File albumBaseDir = new File("target/testalbum", UUID.randomUUID().toString());
     static {
       try {
         // final File dir = resource.getFile();
         final File albumDir = new File(albumBaseDir, "album");
         Git.init().setDirectory(albumDir).call();
         final PrintWriter writer = new PrintWriter(new File(albumDir, ".autoadd"), "utf-8");
         writer.println("2001-01-01");
         writer.close();
 
       } catch (final IOException e) {
         throw new RuntimeException("Cannot initialize Test", e);
       }
     }
 
     @Bean
     public AlbumAccess albumAccess() {
       final FileAlbumAccess fileAlbumAccess = new FileAlbumAccess();
       fileAlbumAccess.setBaseDir(new FileSystemResource(albumBaseDir));
       fileAlbumAccess.setImportBaseDir(resource);
       return fileAlbumAccess;
     }
 
     @Bean
     public ExecutorService executorService() {
       return Executors.newFixedThreadPool(4);
     }
 
     @Bean
     public File importBaseDir() throws IOException {
       return resource.getFile();
     }
   }
 
   @Autowired
   private AlbumAccess albumAccess;
   @Autowired
   private ExecutorService executorService;
   @Autowired
   private File importBaseDir;
 
   @Test
   public void testImport() {
     albumAccess.importFiles(importBaseDir);
   }
 }
