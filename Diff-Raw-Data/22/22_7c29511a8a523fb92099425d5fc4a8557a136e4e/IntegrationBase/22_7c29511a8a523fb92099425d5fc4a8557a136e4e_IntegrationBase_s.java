 /**
  * Copyright 1&1 Internet AG, https://github.com/1and1/
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package net.oneandone.maven.plugins.prerelease.util;
 
 import net.oneandone.sushi.fs.World;
 import net.oneandone.sushi.fs.file.FileNode;
 import net.oneandone.sushi.io.MultiWriter;
 import net.oneandone.sushi.launcher.ExitCode;
 import net.oneandone.sushi.launcher.Failure;
 import net.oneandone.sushi.launcher.Launcher;
 import org.junit.BeforeClass;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.List;
 
 import static org.junit.Assert.fail;
 
 public class IntegrationBase {
     public static final World WORLD = new World();
 
     public static final FileNode PROJECT_HOME = WORLD.guessProjectHome(IntegrationBase.class);
     public static final FileNode TARGET = PROJECT_HOME.join("target");
 
     private static final String SVN = "svn";
 
     public static void svn(FileNode workingDirectory, List<String> parameters) throws Failure {
         svn(workingDirectory, SVN, parameters);
     }
 
     public static void svn(FileNode workingDirectory, String command, List<String> parameters) throws Failure {
         Writer out;
         Launcher launcher;
 
         out = MultiWriter.createNullWriter();
         launcher = new Launcher(workingDirectory, command);
         launcher.args(parameters);
         launcher.exec(out);
     }
 
     protected static void createRepository(FileNode currentWorkingDirectory, FileNode repository) throws IOException {
         ArrayList<String> parameters = new ArrayList<>();
         parameters.add("create");
         parameters.add(repository.getAbsolute());
         repository.deleteTreeOpt();
         svnAdmin(currentWorkingDirectory, parameters);
     }
 
     protected static void svnAdmin(FileNode currentWorkingDirectory, List<String> parameters) throws Failure {
         svn(currentWorkingDirectory, "svnadmin", parameters);
     }
 
     protected static void svnImport(URI repository, FileNode importFolder) throws Failure {
         svnImport(repository, importFolder.getAbsolute());
     }
 
     protected static void svnImport(URI repository, String importFolder) throws Failure {
         ArrayList<String> parameters = new ArrayList<>();
         parameters.add("import");
         parameters.add(importFolder);
         parameters.add(repository.toString());
         parameters.add("-m");
         parameters.add("[unit test] import.");
         svn(TARGET, parameters);
     }
 
     protected static void svnMkdir(URI repository) throws Failure {
         ArrayList<String> parameters = new ArrayList<>();
         parameters.add("mkdir");
         parameters.add(repository.toString());
         parameters.add("-m");
         parameters.add("[unit test] added");
         svn(TARGET, parameters);
     }
 
     protected static void svnRemove(URI repository) throws Failure {
         ArrayList<String> parameters = new ArrayList<>();
         parameters.add("remove");
         parameters.add(repository.toString());
         parameters.add("-m");
         parameters.add("[unit test] remove");
         svn(TARGET, parameters);
     }
 
     protected static void svnCommit(FileNode workingDirectory, String message) throws Failure {
         ArrayList<String> parameters = new ArrayList<>();
         parameters.add("commit");
         parameters.add("-m");
         parameters.add("'" + message + "'");
         svn(workingDirectory, parameters);
     }
 
     protected static void svnAdd(FileNode workingDirectory, FileNode fileToAdd) throws Failure {
         ArrayList<String> parameters = new ArrayList<>();
         parameters.add("add");
         parameters.add(fileToAdd.getAbsolute());
         svn(workingDirectory, parameters);
     }
 
     protected static void svnCheckout(URI repository, String targetFolder) throws Failure {
         ArrayList<String> parameters = new ArrayList<>();
         parameters.add("checkout");
         parameters.add(repository.toString());
         parameters.add(targetFolder);
         svn(TARGET, parameters);
     }
 
 
     protected static void append(FileNode fileToChange, String message) throws IOException {
         Writer dest;
 
         dest = fileToChange.createAppender();
         dest.write(message);
         dest.close();
     }
 
     //--
 
     protected static final FileNode SETTINGS = TARGET.join("settings.xml");
     protected static final FileNode MAVEN_LOCAL_REPOSITORY = TARGET.join("it/maven-local-repository");
     protected static final FileNode SVN_REPOSITORY = TARGET.join("it/svn-repository");
    protected static final String VERSION;

    static {
        try {
            VERSION = TARGET.join("classes/META-INF/wsd.properties").readProperties().getProperty("version");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        if (VERSION == null) {
            throw new IllegalStateException("unknown version");
        }
    }
 
     protected static final String REPOSITORY_URL = "file://" + SVN_REPOSITORY.getAbsolute();
 
     @BeforeClass
     public static void beforeSuite() throws IOException {
         MAVEN_LOCAL_REPOSITORY.mkdirsOpt();
         SETTINGS.writeString(PROJECT_HOME.join("src/it/settings.xml").readString().replace("@@TARGET@@", TARGET.getAbsolute()));
         createRepository(TARGET, SVN_REPOSITORY);
         for (FileNode project : PROJECT_HOME.join("src/it").list()) {
             if (project.isDirectory() && !".svn".equals(project.getName())) {
                 importProject(project.getName());
             }
         }
     }
 
     private static void importProject(String name) throws IOException {
         String str;
         FileNode tmp;
         FileNode pom;
 
         tmp = WORLD.getTemp().createTempDirectory();
         PROJECT_HOME.join("src/it", name).copyDirectory(tmp);
         pom = tmp.join("pom.xml");
         str = pom.readString();
         str = str.replace("@@TARGET@@", TARGET.getAbsolute());
        str = str.replace("@@VERSION@@", VERSION);
         str = str.replace("@@SVNURL@@", REPOSITORY_URL + "/" + name + "/trunk");
         pom.writeString(str);
         svnImport(URI.create(REPOSITORY_URL + "/" + name + "/trunk"), tmp);
         svnMkdir(URI.create(REPOSITORY_URL + "/" + name + "/tags"));
         svnMkdir(URI.create(REPOSITORY_URL + "/" + name + "/branches"));
     }
 
     protected static FileNode checkoutProject(String name) throws IOException {
         return checkoutProject(name, "");
     }
 
     protected static FileNode checkoutProject(String name, String directorySuffix) throws IOException {
         FileNode checkout;
 
         checkout = TARGET.join("it/" + name + directorySuffix);
         checkout.deleteTreeOpt();
         svnCheckout(URI.create(REPOSITORY_URL + "/" + name + "/trunk"), checkout.getAbsolute());
         return checkout;
     }
 
     protected static void mvn(FileNode working, String ... args) throws Exception {
         Launcher mvn;
 
         mvn = new Launcher(working, "mvn", "-Dprerelease.user=michael.hartmeier@1und1.de",
                 "-Dprerelease.lockTimeout=5", "-Dprerelease.checkoutLink=", "-e", "-s", SETTINGS.getAbsolute());
         mvn.arg(args);
         try {
             mvn.exec();
         } catch (ExitCode e) {
             fail(e.output);
         }
     }
 
     public void silenceCheckstyle() {
     }
 }
