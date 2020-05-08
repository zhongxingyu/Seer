 package com.habitualcoder.springpetclinic.selenium.utils.yatspec;
 
 import com.googlecode.yatspec.rendering.Renderer;
 
 import java.io.File;
 import java.io.FileOutputStream;
 
 public class ScreenShotRenderer implements Renderer<ScreenShotHolder> {
     private File yatspecOutputDir;
 
     @Override
     public String render(ScreenShotHolder screenShotHolder) throws Exception {
         if (yatspecOutputDir == null) {
             throw new IllegalStateException("You must use HtmlWithScreenShotResultListener in your test to use ScreenShotRenderer");
         } else {
             String imageFilename = yatspecOutputDir + File.separator + getImageName();
             try (FileOutputStream fos = new FileOutputStream(imageFilename)) {
                 fos.write(screenShotHolder.getPngImageData());
             }
             System.out.println("Rendered screenshot to " + imageFilename);
             return String.format("<div class='nohighlight'><img src=\"%s\" alt=\"%s\"></img></div>", imageFilename, imageFilename);
         }
     }
 
     public void setYatspecOutputDir(File yatspecOutputDir) {
         this.yatspecOutputDir = yatspecOutputDir;
     }
 
     protected String getImageName() {
        return String.format("test-%s%s", this.getClass().getSimpleName(), ".png");
     }
 }
