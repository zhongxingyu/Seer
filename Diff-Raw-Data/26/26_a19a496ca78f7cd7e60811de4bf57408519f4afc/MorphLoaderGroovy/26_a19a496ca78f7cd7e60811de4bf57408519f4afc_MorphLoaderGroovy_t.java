 /*
 Mjdj MIDI Morph - an extensible MIDI processor and translator.
 Copyright (C) 2010 Confusionists, LLC (www.confusionists.com)
 
 This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 
 See the GNU General Public License for more details. You should have received a copy of the GNU General Public License along with this program. If not, see <http://www.gnu.org/licenses/>. 
 
 You may contact the author at mjdj_midi_morph [at] confusionists.com
 */
 package com.confusionists.mjdj.fileIO;
 
 import groovy.lang.GroovyClassLoader;
 import groovy.lang.GroovyObject;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.confusionists.mjdj.ui.Logger;
 import com.confusionists.mjdjApi.morph.Morph;
 import com.confusionists.util.RecursiveFileList;
 
 /**
  * This could be used for dynamic loading, but in practice, the developer has an IDE or build process. Groovy classes are loaded as classes.
  */
 public class MorphLoaderGroovy {
 
     public void load(List<Morph> morphs) throws Exception {
         assert (MorphLoaderJava.classLoader != null);
         // get all .class files in the directory
 
        File[] files = RecursiveFileList.getList(ExternalDirectories.instance.morphsDirGroovy, "groovy");
         for (File file : files) {
         	
             GroovyClassLoader loader = new GroovyClassLoader(MorphLoaderJava.classLoader);
 
             try {
                 Class<?> groovyClass = loader.parseClass(file);
                 GroovyObject groovyObject = (GroovyObject) groovyClass.newInstance();
                 System.out.println("Loaded groovy ok " + groovyClass.getSimpleName());
                 morphs.add((Morph) groovyObject);
             } catch (InstantiationException ie) {
                 System.out.println("Could not instantiate " + file.getName() + ", could be abstract.");
             } catch (Exception e) {
                Logger.log("Could not load Groovy translator " + file.getAbsolutePath().substring(ExternalDirectories.instance.morphsDirGroovy.getAbsolutePath().length()), e);
             }
 
         }
     }
 
     /* testing main */
     public static void main(String[] args) throws Exception {
         MorphLoaderJava.main(args);
         List<Morph> morphs = new ArrayList<Morph>();
         System.out.println("Groovy loader " + morphs.size());
 
     }
 }
