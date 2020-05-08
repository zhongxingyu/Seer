 /*
  * Nebula2D is a cross-platform, 2D game engine for PC, Mac, & Linux
  * Copyright (c) 2014 Jon Bonazza
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.nebula2d.editor.framework;
 
 import com.badlogic.gdx.files.FileHandle;
 import com.badlogic.gdx.utils.XmlWriter;
 import com.nebula2d.assets.AssetManager;
 import com.nebula2d.editor.io.FullBufferedWriter;
 import com.nebula2d.editor.io.savers.SceneSaver;
 import com.nebula2d.editor.ui.BuildProgressUpdateListener;
 import com.nebula2d.editor.ui.MainFrame;
 import com.nebula2d.editor.ui.SceneGraph;
 import com.nebula2d.editor.util.PlatformUtil;
 import com.nebula2d.editor.util.builders.SceneBuilder;
 import com.nebula2d.scene.Scene;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.List;
 
 public class Project {
 
     private List<Scene> scenes;
     private Scene currentScene;
     private String projectDir;
     private String projectName;
 
     public Project(String dir, String name) {
         this.projectDir = dir;
         this.projectName = name;
         scenes = new ArrayList<>();
     }
 
     public Project(String path) {
         File file = new File(path);
         this.projectDir = file.getParent();
         this.projectName = file.getName();
         if (this.projectName.endsWith(".n2d"))
             this.projectName = this.projectName.substring(0, this.projectName.length() - 4);
 
         scenes = new ArrayList<>();
     }
 
     public List<Scene> getScenes() {
         return this.scenes;
     }
 
     public void addScene(Scene scene) {
         scenes.add(scene);
     }
 
     public Scene getScene(int idx) {
         return scenes.get(idx);
     }
 
     public Scene getCurrentScene() {
         return currentScene;
     }
 
     public int getCurrentSceneIndex() {
         return currentScene != null ? scenes.indexOf(currentScene) : -1;
     }
 
     public void setCurrentScene(String name) {
         for (int i = 0; i < scenes.size(); ++i) {
             if (scenes.get(i).getName().equals(name)) {
                 setCurrentScene(i);
                 break;
             }
         }
     }
 
     public void setCurrentScene(int idx) {
         if (scenes.size() > idx) {
            AssetManager.unloadAssets(getCurrentScene().getName());
             currentScene = getScene(idx);
             AssetManager.loadAssets(getCurrentScene().getName());
         }
     }
 
     public String getPath() {
         return projectDir + File.separator + projectName + ".n2d";
     }
 
     public String getProjectDir() {
         return projectDir;
     }
 
     public String getNameWithoutExt() {
         int dot = projectName.indexOf('.');
         if (dot == -1)
             return projectName;
 
         String ext = projectName.substring(dot);
 
         if (!ext.equals(".n2d"))
             return projectName;
 
         return projectName.substring(0, projectName.length() - ext.length());
     }
 
     public void loadProject() throws IOException {
         scenes.clear();
         // TODO: Implement
     }
 
     public void saveProject() throws IOException {
         try (FullBufferedWriter fw = new FullBufferedWriter(new FileWriter(getPath()))) {
             fw.writeLine(projectDir);
             fw.writeLine(projectName);
             fw.writeIntLine(scenes.size());
             for (Scene scene : scenes)
                 new SceneSaver(scene).save(fw);
 
             fw.writeIntLine(getCurrentSceneIndex());
         }
     }
 
     public void loadCurrentScene() {
         loadScene(getCurrentScene());
     }
 
     private void loadScene(Scene scene) {
         SceneGraph graph = MainFrame.getSceneGraph();
         scene.getLayers().forEach(graph::addLayer);
         graph.refresh();
     }
 
     public boolean containsSceneWithName(String name) {
         for (Scene scene : scenes) {
             if (scene.getName().equals(name))
                 return true;
         }
 
         return false;
     }
 
     public void build(String startScene, FileHandle sceneFileOut, FileHandle assetsFileOut,
                       BuildProgressUpdateListener listener) throws  IOException {
         StringWriter sceneStrWriter = new StringWriter();
         StringWriter assetsStrWriter = new StringWriter();
         XmlWriter sceneXmlWriter = new XmlWriter(sceneStrWriter);
         XmlWriter assetsXmlWriter = new XmlWriter(assetsStrWriter);
 
         sceneXmlWriter.element("project").
                 attribute("name", projectName).
                 attribute("startScene", startScene);
         assetsXmlWriter.element("assets");
 
         for (int i = 0; i < scenes.size(); ++i) {
             Scene scene = scenes.get(i);
             new SceneBuilder(scene).build(sceneXmlWriter, assetsXmlWriter);
             sceneXmlWriter.pop();
             listener.onBuildProgressUpdate(scene, i, scenes.size());
         }
         sceneXmlWriter.pop();
         assetsXmlWriter.pop();
 
         sceneFileOut.writeString(sceneStrWriter.toString(), false);
         assetsFileOut.writeString(assetsStrWriter.toString(), false);
 
         listener.onProjectCompiled();
     }
 
     public String getTempDir() {
         return PlatformUtil.pathJoin(projectDir, "tmp");
     }
 
     public boolean ensureTempDir() {
         File tmpDir = new File(getTempDir());
         return tmpDir.exists() || tmpDir.mkdir();
     }
 }
