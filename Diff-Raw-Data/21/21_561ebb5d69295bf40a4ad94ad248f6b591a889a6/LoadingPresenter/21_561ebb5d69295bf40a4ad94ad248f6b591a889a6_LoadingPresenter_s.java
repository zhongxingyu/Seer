 /* Copyright 2011 Anthony Cassidy
  * Usage of the works is permitted provided that this instrument is retained with the works, so that any entity that uses the works is notified of this instrument.
  *  DISCLAIMER: THE WORKS ARE WITHOUT WARRANTY.
  */
 package com.github.a2g.core;
 
 
 import com.github.a2g.core.authoredroom.IAmTheMainAPI;
 import com.google.web.bindery.event.shared.EventBus;
 import com.google.gwt.user.client.ui.AcceptsOneWidget;
 
 
 public class LoadingPresenter {
     private EventBus bus;
     private IAmTheMainAPI api;
     private LoadingPanel view;
     int current;
     int total;
     	
     public LoadingPresenter(final AcceptsOneWidget panel, EventBus bus, IAmTheMainAPI api) {
         this.bus = bus;
         this.api = api;
         this.view = new LoadingPanel();
         panel.setWidget(view);
         this.api = api;
         this.current = 0;
         this.total = 0;
     }
     
    void setCurrent(int total)
     {
     	this.current = current;
     	view.update(current,total);
     }
 
     void setTotal(int total)
     {
     	this.total = total;
     	view.update(current,total);
     }
 }
