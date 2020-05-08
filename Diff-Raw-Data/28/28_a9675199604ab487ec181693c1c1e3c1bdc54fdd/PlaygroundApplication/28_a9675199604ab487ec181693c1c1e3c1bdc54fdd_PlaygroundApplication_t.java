 package com.comsysto.playground.ui.application;
 
 
 import com.comsysto.playground.ui.page.home.HomePage;
import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.settings.IRequestCycleSettings;
import org.apache.wicket.settings.def.JavaScriptLibrarySettings;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.wicketstuff.annotation.scan.AnnotatedMountScanner;
 
 public class PlaygroundApplication extends WebApplication {
 
     @Override
     protected void init() {
         super.init();
 
         new AnnotatedMountScanner().scanPackage("com.comsysto.ui.page").mount(this);
 
         getMarkupSettings().setStripWicketTags(true);
         getRequestCycleSettings().setRenderStrategy(IRequestCycleSettings.RenderStrategy.REDIRECT_TO_RENDER);
         getComponentInstantiationListeners().add(new SpringComponentInjector(this));
         getDebugSettings().setAjaxDebugModeEnabled(true);
 
         setJavaScriptLibrarySettings(new JavaScriptLibrarySettings());
 
     }
 
 
     @Override
     public Class<? extends Page> getHomePage() {
         return HomePage.class;
     }
 }
