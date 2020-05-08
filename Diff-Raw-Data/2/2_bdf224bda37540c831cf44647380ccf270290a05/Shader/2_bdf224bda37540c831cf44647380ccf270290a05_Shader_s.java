 package com.pixelthieves.core.graphics;
 
 import com.badlogic.gdx.Application.ApplicationType;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.files.FileHandle;
 import com.badlogic.gdx.graphics.glutils.ShaderProgram;
 
 import java.util.HashMap;
 
 /**
  * Class that is responsible for loading and baking shaders.
  */
 public class Shader {
     private static Shader instance;
     private final HashMap<String, ShaderStructure> map = new HashMap<String, ShaderStructure>();
 
     private Shader() {
         FileHandle dirHandle;
 
         // Eclipse side-effect of linking assets, assets are copied in bin folder
         if (Gdx.app.getType() == ApplicationType.Android) {
             dirHandle = Gdx.files.internal("data/shaders");
         } else {
             // ApplicationType.Desktop ..
             dirHandle = Gdx.files.internal("./bin/data/shaders");
             if (!dirHandle.exists()) {
                 dirHandle = Gdx.files.internal("data/shaders");
             }
         }
 
         FileHandle[] list = dirHandle.list();
 
         for (FileHandle file : list) {
 
             String ext = file.extension();
             String name = file.nameWithoutExtension();
 
             ShaderStructure ss;
             if (!map.containsKey(name)) {
                 ss = new ShaderStructure(name);
             } else {
                 ss = map.get(name);
             }
 
             switch (ShaderType.valueOf(ext)) {
                 case vsh:
                     ss.setVertex(file.readString());
                     break;
                 case fsh:
                     ss.setFragment(file.readString());
                     break;
             }
 
             if (!map.containsKey(name)) {
                 map.put(name, ss);
             }
 
         }
     }
 
     /**
      * Returns a shader with corresponding name.
      *
      * @param name of the shader
      * @return a baked shader
      */
     public static ShaderProgram getShader(String name) {
         if (instance == null) {
             instance = new Shader();
         }
         return instance.getStructure(name).getShader();
     }
 
     private ShaderStructure getStructure(String name) {
         ShaderStructure value = map.get(name);
         if (value == null) {
             throw new IllegalArgumentException("Specified Shader does not exists: " + name);
         }
         return value;
     }
 
     private enum ShaderType {
         vsh, fsh
     }
 
     private static class ShaderStructure {
         private final String name;
         private String vertex;
         private String fragment;
         private ShaderProgram program;
 
         public ShaderStructure(String name) {
             this.name = name;
         }
 
         public void setVertex(String vertex) {
             this.vertex = vertex;
             compile();
         }
 
         public void setFragment(String fragment) {
             this.fragment = fragment;
             compile();
         }
 
         private void compile() {
             if (vertex != null && fragment != null) {
                 System.out.println("Compiling shader [" + name + "]");
                 program = new ShaderProgram(vertex, fragment);
                 if (!program.isCompiled()) {
                    throw new ShaderException(name, program);
                 }
             }
         }
 
         public ShaderProgram getShader() {
             return program;
         }
 
     }
 
 }
