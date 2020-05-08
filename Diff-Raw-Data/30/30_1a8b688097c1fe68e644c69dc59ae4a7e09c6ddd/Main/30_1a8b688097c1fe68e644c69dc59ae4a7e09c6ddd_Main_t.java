
 package com.pakoito.wartricks;
 
 import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
 import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.wartricks.lifecycle.WartricksGame;
 
 public class Main {
    public static void main(final String[] args) {
        final LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
        cfg.title = "wartricks";
        cfg.useGL20 = false;
        cfg.width = 1080;
        cfg.height = 576;
        new LwjglApplication(new WartricksGame(), cfg);
    }
 }
