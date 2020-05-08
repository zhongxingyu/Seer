 package com.sigma.qsab.glitches;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.Properties;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 
 public abstract class GlitchLoader {
 
     public static Glitch[] getGlitches() throws IOException {
         Properties glitchMap = new Properties();
         glitchMap.load(GlitchLoader.class.getResourceAsStream("/glitches.properties"));
         ArrayList<Glitch> glitchList = new ArrayList<Glitch>();
         ClassLoader loader = new URLClassLoader(new URL[]{}, Glitch.class.getClassLoader());
         for (int i = 0; i < Integer.valueOf(glitchMap.getProperty("nrofglitches", "0")).intValue(); i++) {
             try {
                 glitchList.add((Glitch) (loader.loadClass("com.sigma.qsab.glitches.customglitches." + glitchMap.getProperty("" + i)).newInstance()));
             } catch (ClassNotFoundException ex) {
                 System.out.println("Class " + glitchMap.getProperty("" + i) + " could not be found.");
             } catch (InstantiationException ex) {
                 System.out.println("Class " + glitchMap.getProperty("" + i) + " could not be initiated.");
             } catch (IllegalAccessException ex) {
                 System.out.println("Class " + glitchMap.getProperty("" + i) + " could not be accessed.");
             }
         }
         Collections.sort(glitchList);
         return glitchList.toArray(new Glitch[glitchList.size()]);
     }
 
     public static Glitch[] getGlitchesDynamically() throws IOException {
         String path = System.getProperty("java.class.path");
         ArrayList<Glitch> glitchList = new ArrayList<Glitch>();
         if (path.endsWith(".jar")) {
             glitchList = loadGlitchesFromJar(path);
         } else {
             glitchList = loadGlitchesFromDirectory(path);
         }
         Collections.sort(glitchList);
         return glitchList.toArray(new Glitch[glitchList.size()]);
     }
 
     private static ArrayList<Glitch> loadGlitchesFromJar(String path) throws IOException {
         ArrayList<Glitch> glitchList = new ArrayList<Glitch>();
         ClassLoader loader = new URLClassLoader(new URL[]{}, Glitch.class.getClassLoader());
         JarFile jar = new JarFile(path);
        Enumeration<JarEntry> entries = jar.entries();        
         while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();            
             if (entry.isDirectory()) continue;
             if (!entry.getName().startsWith("com.sigma.qsab.glitches.customglitches.")) continue;
             try {
                    glitchList.add(
                            (Glitch) (loader.loadClass("com.sigma.qsab.glitches.customglitches."
                            + entry.getName()).newInstance()));
                } catch (ClassNotFoundException ex) {
                    System.out.println("Class " + entry.getName() + " could not be found.");
                } catch (InstantiationException ex) {
                    System.out.println("Class " + entry.getName() + " could not be initiated.");
                } catch (IllegalAccessException ex) {
                    System.out.println("Class " + entry.getName() + " could not be accessed.");
                }
         }
         return glitchList;
     }
 
     private static ArrayList<Glitch> loadGlitchesFromDirectory(String path) {
         ArrayList<Glitch> glitchList = new ArrayList<Glitch>();
         ClassLoader loader = new URLClassLoader(new URL[]{}, Glitch.class.getClassLoader());
         File[] files = (new File(path + "/com/sigma/qsab/glitches/customglitches")).listFiles();
         for (File f : files) {
             if (f.getName().endsWith(".class")) {
                 try {
                     glitchList.add(
                             (Glitch) (loader.loadClass("com.sigma.qsab.glitches.customglitches."
                             + f.getName().substring(0, f.getName().length() - 6)).newInstance()));
                 } catch (ClassNotFoundException ex) {
                     System.out.println("Class " + f.getName() + " could not be found.");
                 } catch (InstantiationException ex) {
                     System.out.println("Class " + f.getName() + " could not be initiated.");
                 } catch (IllegalAccessException ex) {
                     System.out.println("Class " + f.getName() + " could not be accessed.");
                 }
             }
         }
         return glitchList;
     }
 }
