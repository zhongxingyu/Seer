 package com.praeses.androidstack.di;
 
 import com.praeses.androidstack.core.EnglishHelloModel;
 import com.praeses.androidstack.core.HelloModel;
 import com.squareup.otto.Bus;
 
 import dagger.Module;
 import dagger.Provides;
 
@Module
 public class HelloModule {
     @Provides
     public HelloModel provideHelloModel(Bus bus) {
         return new EnglishHelloModel(bus);
     }
 }
