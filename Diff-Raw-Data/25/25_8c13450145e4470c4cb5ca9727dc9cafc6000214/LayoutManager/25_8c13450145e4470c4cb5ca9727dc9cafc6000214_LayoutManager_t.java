 package com.uxebu.swfparser.dump.layout;
 
 import com.uxebu.swfparser.dump.actions.ButtonCondActionFlag;
 import com.uxebu.swfparser.dump.actions.ClipActionFlag;
 
 import java.io.File;
 import java.io.FileOutputStream;
 
 public class LayoutManager
 {
     private String outputDirectory;
 
     public LayoutManager(String outputDirectory)
     {
         this.outputDirectory = outputDirectory;
 
         init();
     }
 
     private void init()
     {
         createDirectoryIfNotExists(outputDirectory);
 
         for (String directory : new String[]{"rootMovie", "button", "initClip", "sprite"})
         {
             createDirectoryIfNotExists(outputDirectory + "/" + directory);
         }
     }
 
     private void createDirectoryIfNotExists(String directory)
     {
         File fileOutputDirectory = new File(directory);
 
         if (!fileOutputDirectory.exists())
         {
             fileOutputDirectory.mkdirs();
         }
     }
 
     public void addButton(int characterId, ButtonCondActionFlag buttonCondActionFlag, String content)
     {
         dumpFile(outputDirectory + "/button/button-" + characterId + "-" + buttonCondActionFlag + ".js", content);
     }
 
     private void dumpFile(String fileName, String content)
     {
         try
         {
             FileOutputStream spriteFile = new FileOutputStream(fileName);
             spriteFile.write(content.getBytes());
             spriteFile.flush();
             spriteFile.close();
         }
         catch (Exception e)
         {
             e.printStackTrace();
             throw new RuntimeException("Error creating file " + fileName);
         }
     }
 
     public void addSprite(int characterId, int frameNumber, String content)
     {
         dumpFile(outputDirectory + "/sprite/sprite-" + characterId + "-" + frameNumber + ".js", content);
     }
 
     public void addInitClip(int spriteId, String content)
     {
         dumpFile(outputDirectory + "/initClip/initClip-" + spriteId + ".js", content);
     }
 
     public void addRootMovie(int frameNumber, String content)
     {
         dumpFile(outputDirectory + "/rootMovie/rootMovie-" + frameNumber + ".js", content);
     }
 
    public void addSpriteClipAction(int spriteCharacterId, int frame, int depth, ClipActionFlag flag, String content)
     {
        dumpFile(outputDirectory + "/sprite/sprite-" + spriteCharacterId + "-clipAction-" + frame + '-' + depth + "-" + flag + ".js", content);
     }
 
    public void addRootMovieClipAction(int frame, int depth, ClipActionFlag flag, String content)
     {
        dumpFile(outputDirectory + "/rootMovie/rootMovie-clipAction-" + frame + '-' + depth + "-" + flag + ".js", content);
     }
 }
