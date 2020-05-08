 package com.soupcan.aquapulse.java;
 
 import com.soupcan.aquapulse.core.Aquapulse;
 
 import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
 import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
 
 public class AquapulseDesktop
 {
     public static void main(String[] args)
     {
         LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
         config.useGL20 = true;
         new LwjglApplication(new Aquapulse(), config);
     }
 }
