 /*
  * Author: Dabo Ross
  * Website: www.daboross.net
  * Email: daboross@daboross.net
  */
 package net.daboross.mavenbukkitcreator;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.util.regex.Matcher;
 
 /**
  *
  * @author daboross
  */
 public class ProjectCreator {
 
     private final String name, desc;
     private File projectDir, javaDir, resourceDir, targetMainFile, targetPluginYaml, targetPomXml, targetGitIgnore;
    private final String gitOriginName;
 
    public ProjectCreator(String name, String desc, String gitOriginName) {
         this.name = name;
         this.desc = desc;
        this.gitOriginName = gitOriginName;
     }
 
    public void create() throws IOException, InterruptedException {
         getDirs();
         copyFile(getClass().getResourceAsStream("/template/Main.java.new"), targetMainFile);
         copyFile(getClass().getResourceAsStream("/template/plugin.yml.new"), targetPluginYaml);
         copyFile(getClass().getResourceAsStream("/template/pom.xml.new"), targetPomXml);
         copyFile(getClass().getResourceAsStream("/template/GitIgnore.new"), targetGitIgnore);
        new GitInit(projectDir, gitOriginName).run();
     }
 
     private void getDirs() {
         projectDir = new File(name);
         if (projectDir.exists()) {
             System.err.println("Project Directory Exists!");
             System.exit(1);
         }
         projectDir.mkdirs();
         javaDir = new File(new File(new File(new File(new File(new File(new File(projectDir, "src"), "main"), "java"), "net"), "daboross"), "bukkitdev"), name.toLowerCase());
         resourceDir = new File(new File(new File(projectDir, "src"), "main"), "resources");
         javaDir.mkdirs();
         resourceDir.mkdirs();
         targetMainFile = new File(javaDir, name + ".java");
         targetPluginYaml = new File(resourceDir, "plugin.yml");
         targetPomXml = new File(projectDir, "pom.xml");
         targetGitIgnore = new File(projectDir, ".gitignore");
     }
 
     private void copyFile(InputStream input, File output) throws IOException {
         InputStreamReader inputStreamReader = new InputStreamReader(input);
         BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
         FileWriter fileWriter = new FileWriter(output);
         BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
         PrintWriter printWriter = new PrintWriter(bufferedWriter);
         String line;
         while ((line = bufferedReader.readLine()) != null) {
             printWriter.println(line.replaceAll(Matcher.quoteReplacement("$$NAME"), name)
                     .replaceAll(Matcher.quoteReplacement("$$LOWER"), name.toLowerCase())
                     .replaceAll(Matcher.quoteReplacement("$$DESC"), desc));
         }
         bufferedReader.close();
         inputStreamReader.close();
         printWriter.close();
         bufferedWriter.close();
         fileWriter.close();
     }
 }
