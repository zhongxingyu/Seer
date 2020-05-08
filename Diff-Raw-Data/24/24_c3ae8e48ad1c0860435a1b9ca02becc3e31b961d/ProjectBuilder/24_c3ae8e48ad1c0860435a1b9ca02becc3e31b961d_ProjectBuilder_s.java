 package com.nebula2d.editor.util;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.files.FileHandle;
 import com.nebula2d.editor.framework.Project;
 import com.nebula2d.editor.ui.BuildProgressDialog;
 import net.lingala.zip4j.core.ZipFile;
 import net.lingala.zip4j.exception.ZipException;
 
 import javax.swing.JOptionPane;
 import javax.swing.SwingUtilities;
 import java.io.*;
 
 /**
  * ProjectBuilder is a helper class for building a Nebula2D Project.
  *
  * Created by bonazza on 8/7/14.
  */
 public class ProjectBuilder {
 
     public static enum ProjectType {
         PC("desktop.zip"),
         MAC("desktop.zip"),
         LINUX("desktop.zip"),
         ANDROID("android.zip");
 
         private String sourcePath;
 
         private ProjectType(String sourcePath) {
             this.sourcePath = sourcePath;
         }
 
         public String getSourcePath() {
             return sourcePath;
         }
     }
     private Project project;
 
     public ProjectBuilder(Project project) {
         this.project = project;
     }
 
    public void build(int startScene, ProjectType type) throws IOException, ZipException, InterruptedException {
         switch (type) {
             case PC:
                 buildPC(startScene);
                 break;
         }
     }
 
    private void buildPC(int startScene) throws IOException, ZipException, InterruptedException {
         ProjectType type = ProjectType.PC;
         FileHandle source = extractSource(type);
         FileHandle resourcesDir = source.child("src/main/resources");
         resourcesDir.mkdirs();
         FileHandle sceneFileHandle = resourcesDir.child("scenes.xml");
         FileHandle assetsFileHandle = resourcesDir.child("assets.xml");
         createConfigFile(resourcesDir);
 
         BuildProgressDialog buildDialog = new BuildProgressDialog(project.getNameWithoutExt());
         buildDialog.setVisible(true);
         new Thread(() -> {
             try {
                 project.build(startScene, sceneFileHandle, assetsFileHandle, buildDialog);
                 GradleExecutor.build(source.path());
                 buildDialog.onBuildComplete();
             } catch (Exception e) {
                 SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null,
                         String.format("Failed to build project %s", project.getNameWithoutExt())));
             }
         }).start();
     }
 
     private void createConfigFile(FileHandle dir) throws IOException {
         dir.child("meta.cfg").file().createNewFile();
     }
 
     public FileHandle extractSource(ProjectType type) throws IOException, ZipException {
         String sourcePath = type.getSourcePath();
         project.ensureTempDir();
         String tmpDir = project.getTempDir();
         FileHandle dest = Gdx.files.absolute(tmpDir);
         FileHandle sourceHandle = Gdx.files.classpath("client").child(sourcePath);
         sourceHandle.copyTo(dest);
         FileHandle zipFile = dest.child(sourcePath);
         new ZipFile(zipFile.path()).extractAll(dest.path());
         zipFile.delete();
         return dest;
     }
 }
