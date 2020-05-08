 package com.soupcan.the_love_of_rice.java;
 
 import com.soupcan.the_love_of_rice.core.TheLoveOfRice;
 
 import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
 import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
 
 public class TheLoveOfRiceDesktop
 {
    public static void main(String[] args)
     {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.useGL20 = true;
         config.width = 1280 - 96;
         config.height = 768;
         config.title = "The Love of Rice";
        new LwjglApplication(TheLoveOfRice.instance, config);
    }
 }
