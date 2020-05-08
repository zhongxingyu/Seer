 package hornetq_project_gen.hornetq;
 
 import hornetq_project_gen.MavenModelHelper;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.maven.model.Dependency;
 import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.JavaCore;
 
 public class MavenModelHelperV23x extends MavenModelHelper {
 
     public MavenModelHelperV23x(File sourceRoot, File pomFile, File repoBase) throws IOException, XmlPullParserException {
         super(sourceRoot, pomFile, repoBase);
         ignoredLinks.add("hornetq-ra-jar/target/generated-sources");
         ignoredLinks.add("hornetq-twitter-integration/target/generated-sources/annotations");
         ignoredLinks.add("hornetq-commons/target/generated-sources/annotations");
         ignoredLinks.add("integration/hornetq-jboss-as-integration/target/generated-sources/annotations");
         ignoredLinks.add("hornetq-core/target/generated-sources/javacc");
         ignoredLinks.add("build");
         ignoredLinks.add("byteman-tests");
         ignoredLinks.add("distribution");
         ignoredLinks.add("examples");
         ignoredLinks.add("hornetq-rest/src/test/java");
 
         addExcludedDep("org.jboss.logging", "jboss-logging-spi", "2.1.0.GA");
     }
     /**
      * Those need to copy manually:
      * ./hornetq-jms/target/classes/org/hornetq/jms/HornetQJMSLogger.class
      * ./hornetq-core/target/classes/org/hornetq/core/server/HornetQLogger.class
      * @throws CoreException 
      * @throws  
      */
     public void findMissingEntries(IProject project, Map<String, IClasspathEntry> sources) throws CoreException {
         IFolder folder = project.getFolder("generated-classes");
 
         if (!sources.containsKey(folder.getFullPath().toOSString())) {
            folder.create(false, true, null);
            IClasspathEntry newPath = JavaCore.newLibraryEntry(folder.getFullPath(), null, null);
            sources.put(folder.getFullPath().toOSString(), newPath);
         }
     }
 
     /**
      * srcRoot/hornetq-core-client/target/classes/hornetq-version.properties
      */
     public void copyResources(IProject project, Map<String, IClasspathEntry> sources) throws CoreException {
         IFolder folder = project.getFolder("all-resources");
         folder.create(false, true, null);
         
         IClasspathEntry newPath = JavaCore.newSourceEntry(folder.getFullPath());
         sources.put(folder.getFullPath().toOSString(), newPath);
 
         copySingleFileToFolder("hornetq-core-client/target/classes/hornetq-version.properties", folder);
         copySingleFileToFolder("tests/integration-tests/target/test-classes/test-jgroups-file_ping.xml", folder);
     }
 
     @Override
     protected void addExtraDependencies(List<Dependency> deps) {
        addDep(deps, "org.jboss.resteasy", "jaxrs-api", "2.3.4.Final");
        addDep(deps, "org.apache.httpcomponents", "httpcore", "4.1.2");
        addDep(deps, "org.apache.httpcomponents", "httpclient", "4.1.2");
        addDep(deps, "javax.servlet", "servlet-api", "2.5");
        addDep(deps, "org.hamcrest", "hamcrest-core", "1.3");
     }
 
 }
