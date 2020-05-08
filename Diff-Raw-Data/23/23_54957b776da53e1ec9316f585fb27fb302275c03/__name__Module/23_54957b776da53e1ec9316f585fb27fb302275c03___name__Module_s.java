package com.arcbees.ide.plugin.template.presenter{package};
 
 import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;
 
 public class ${name}Module extends AbstractPresenterModule {
     @Override
     protected void configure() {
         bindPresenter(${name}Presenter.class, ${name}Presenter.MyView.class, ${name}View.class, 
         		${name}Presenter.MyProxy.class);
     }
 }
