 package com.habitualcoder.springpetclinic.selenium.utils.yatspec;
 
 import com.googlecode.yatspec.junit.SpecResultListener;
 import com.googlecode.yatspec.rendering.html.HtmlResultRenderer;
 import com.googlecode.yatspec.state.Result;
 
 import java.io.File;
 
 public class HtmlWithScreenShotResultListener implements SpecResultListener{
     private final ScreenShotRenderer screenShotRenderer;
     private HtmlResultRenderer delegate;
 

    public HtmlWithScreenShotResultListener() {
         delegate = new HtmlResultRenderer();
        screenShotRenderer = new ScreenShotRenderer();
         delegate.withCustomRenderer(ScreenShotHolder.class, screenShotRenderer);
     }
     @Override
     public void complete(File yatspecOutputDir, Result result) throws Exception {
         screenShotRenderer.setYatspecOutputDir(yatspecOutputDir);
         delegate.complete(yatspecOutputDir, result);
     }
 }
