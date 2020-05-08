 package com.github.vspiewak.mowitnow.mower.app;
 
 import java.io.File;
 
 import com.github.vspiewak.mowitnow.mower.api.Engine;
 import com.github.vspiewak.mowitnow.mower.api.Scene;
 import com.github.vspiewak.mowitnow.mower.api.Vehicule;
 import com.github.vspiewak.mowitnow.mower.base.Orientation;
 import com.github.vspiewak.mowitnow.mower.base.Position;
 import com.github.vspiewak.mowitnow.mower.config.ConfigBuilder;
 import com.github.vspiewak.mowitnow.mower.config.ConfigExecutor;
 import com.github.vspiewak.mowitnow.mower.config.ConfigFileBuilder;
 import com.github.vspiewak.mowitnow.mower.domain.Lawn;
 import com.github.vspiewak.mowitnow.mower.domain.Mower;
 import com.github.vspiewak.mowitnow.mower.domain.XEngine;
 
 /**
  * Application Factory providing facilities for building api components
  * 
  * @author Vincent Spiewak
  * @since 1.0
  */
 public final class AppFactory {
 
    /* hide factory class constructor */
    private AppFactory() { }
    
    /* domain factories */
    public static Position newPosition(int x, int y) {
       return new Position(x, y);
    }
 
    public static Vehicule newMower(Position p, Orientation o) {
       return new Mower(p, o);
    }
 
    public static Scene newLawn(Position p) {
       return new Lawn(p);
    }
 
    /* init engine and lawn */
    public static void init(Scene scene) {
       Engine engine = XEngine.get();
       engine.clear();
       engine.init(scene);
    }
 
    /* init engine, lawn and vehicule(s) */
    public static void init(int x, int y, Vehicule... vehicules) {
 
       /* init engine & lawn */
       init(x, y);
 
       /* add all vehicules */
       for (Vehicule vehicule : vehicules) {
          XEngine.get().attach(vehicule);
       }
    }
    
    public static ConfigBuilder newConfigFileBuilder(File file) {
       return new ConfigFileBuilder(file);
    }
    
    public static ConfigExecutor newConfigExecutor() {
       return new ConfigExecutor();
    }
    
    /* fattened versions */
    public static Vehicule newMower(int x, int y, Orientation o) {
       return new Mower(newPosition(x, y), o);
    }
 
    public static Scene newLawn(int x, int y) {
       return new Lawn(newPosition(x, y));
    }

    public static void init(int x, int y) {
       init(newLawn(x, y));
    }
 
 
 }
