 package com.comsysto.playground.ui.application;
 
 
 import com.comsysto.playground.ui.page.home.HomePage;
 
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
