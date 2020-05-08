 /*
  * Copyright (C) 2013 Dabo Ross <http://www.daboross.net/>
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
 package net.daboross.tools.mavenbukkitcreator;
 
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
     private File projectDirectory, sourceDirectory, resourceDirectory,
             targetPluginFile, targetPluginYaml, targetPomXml, targetGitIgnore, targetTravisYaml, targetLicense;
     private final String gitProjectName;
     private final boolean isPluginRequest;
 
     public ProjectCreator(String name, String desc, String gitProjectName, boolean isPluginRequest) {
         this.name = name;
         this.desc = desc;
         this.gitProjectName = gitProjectName;
         this.isPluginRequest = isPluginRequest;
     }
 
     public void create() throws IOException, InterruptedException {
         getDirs();
         copyFile(getClass().getResourceAsStream("/template/Plugin.java.new"), targetPluginFile);
         copyFile(getClass().getResourceAsStream("/template/plugin.yml.new"), targetPluginYaml);
         copyFile(getClass().getResourceAsStream("/template/pom.xml.new"), targetPomXml);
         copyFile(getClass().getResourceAsStream("/template/.gitignore.new"), targetGitIgnore);
         copyFile(getClass().getResourceAsStream("/template/.travis.yml.new"), targetTravisYaml);
         copyFile(getClass().getResourceAsStream("/LICENSE"), targetLicense);
         new GitCreator(projectDirectory, gitProjectName, desc).run(false);
     }
 
     private void getDirs() {
         projectDirectory = new File((isPluginRequest ? "Request-" : "Private-") + name);
         if (projectDirectory.exists()) {
             System.err.println("Project Directory Exists!");
             System.exit(1);
         }
         projectDirectory.mkdirs();
         sourceDirectory = new File(new File(new File(new File(new File(new File(new File(projectDirectory, "src"), "main"), "java"), "net"), "daboross"), "bukkitdev"), name.toLowerCase());
         resourceDirectory = new File(new File(new File(projectDirectory, "src"), "main"), "resources");
         sourceDirectory.mkdirs();
         resourceDirectory.mkdirs();
         targetPluginFile = new File(sourceDirectory, name + "Plugin.java");
         targetPluginYaml = new File(resourceDirectory, "plugin.yml");
         targetLicense = new File(projectDirectory, "LICENSE");
         targetPomXml = new File(projectDirectory, "pom.xml");
         targetTravisYaml = new File(projectDirectory, ".travis.yml");
         targetGitIgnore = new File(projectDirectory, ".gitignore");
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
                    .replaceAll(Matcher.quoteReplacement("$$DESC"), desc)
                    .replaceAll(Matcher.quoteReplacement("$$GITHUBNAME"), gitProjectName));
         }
         bufferedReader.close();
         inputStreamReader.close();
         printWriter.close();
         bufferedWriter.close();
         fileWriter.close();
     }
 }
