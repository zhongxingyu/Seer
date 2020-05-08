 package br.com.cybereagle.androidlibrary.dagger;
 
 import android.content.Context;
 import dagger.Module;
 import dagger.Provides;
 
 import javax.inject.Singleton;
 
 public interface AndroidModule {
 
     @ForApplication
     Context provideApplicationContext();
 }
