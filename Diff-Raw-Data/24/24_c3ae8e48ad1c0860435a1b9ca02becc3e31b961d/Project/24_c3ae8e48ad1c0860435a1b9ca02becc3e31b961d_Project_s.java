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
 import com.nebula2d.editor.common.ISerializable;
 import com.nebula2d.editor.framework.assets.AssetManager;
 import com.nebula2d.editor.ui.BuildProgressUpdateListener;
 import com.nebula2d.editor.ui.MainFrame;
 import com.nebula2d.editor.ui.SceneGraph;
 import com.nebula2d.editor.util.FullBufferedReader;
 import com.nebula2d.editor.util.FullBufferedWriter;
 import com.nebula2d.editor.util.PlatformUtil;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.List;
 
 public class Project implements ISerializable {
 
     private List<Scene> scenes;
     private int currentSceneIdx;
     private String projectDir;
     private String projectName;
 
     public Project(String dir, String name) {
         this.projectDir = dir;
         this.projectName = name;
         this.currentSceneIdx = 0;
         scenes = new ArrayList<>();
     }
 
     public Project(String path) {
         File file = new File(path);
         this.projectDir = file.getParent();
         this.projectName = file.getName();
         if (this.projectName.endsWith(".n2d"))
             this.projectName = this.projectName.substring(0, this.projectName.length() - 4);
 
         this.currentSceneIdx = 0;
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
 
         if (scenes.isEmpty())
             return null;
 
         return scenes.get(currentSceneIdx);
     }
 
     public int getCurrentSceneIdx() {
         return currentSceneIdx;
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
         AssetManager.getInstance().changeScene(currentSceneIdx);
         currentSceneIdx = idx;
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
         FullBufferedReader fr = new FullBufferedReader(new FileReader(getPath()));
 
         load(fr);
     }
 
     public void saveProject() throws IOException {
         FullBufferedWriter fw = new FullBufferedWriter(new FileWriter(getPath()));
         save(fw);
         fw.close();
     }
 
     public void loadCurrentScene() {
         loadScene(currentSceneIdx);
     }
 
     public void loadScene(int idx) {
         Scene scene = getScene(idx);
 
         if (scene == null)
             return;
 
         loadScene(scene);
     }
 
     private void loadScene(Scene scene) {
         SceneGraph graph = MainFrame.getSceneGraph();
         Enumeration layers = scene.children();
         while (layers.hasMoreElements()) {
             graph.addLayer((Layer) layers.nextElement());
         }
         graph.refresh();
     }
 
     @Override
     public void load(FullBufferedReader fr) throws IOException {
         //Need to preset this so that it is available during loading.
         currentSceneIdx = fr.readIntLine();
         int size = fr.readIntLine();
         for (int i = 0; i < size; ++i) {
             String name = fr.readLine();
             Scene s = new Scene(name);
             s.load(fr);
             addScene(s);
         }
 
         AssetManager.getInstance().changeScene(currentSceneIdx);
     }
 
     @Override
     public void save(FullBufferedWriter fw) throws IOException {
         fw.writeIntLine(currentSceneIdx);
         fw.writeIntLine(scenes.size());
         for (Scene scene : scenes) {
             scene.save(fw);
         }
     }
 
     public boolean containsSceneWithName(String name) {
         for (Scene scene : scenes) {
             if (scene.getName().equals(name))
                 return true;
         }
 
         return false;
     }
 
    public void build(int startScene, FileHandle sceneFileOut, FileHandle assetsFileOut,
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
             scene.build(sceneXmlWriter, assetsXmlWriter, scene.getName());
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
