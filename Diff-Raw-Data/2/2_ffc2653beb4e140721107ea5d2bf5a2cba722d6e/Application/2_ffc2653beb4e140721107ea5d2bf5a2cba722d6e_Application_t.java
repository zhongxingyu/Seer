 package com.github.signed.maven.sanitizer;
 
 import com.github.signed.maven.sanitizer.path.ExecutionProbe;
 import com.github.signed.maven.sanitizer.path.PathsInPluginConfiguration;
 import com.github.signed.maven.sanitizer.path.ResourceRoots;
 import com.github.signed.maven.sanitizer.path.SourceRoots;
 import com.github.signed.maven.sanitizer.pom.CleanRoom;
 import com.github.signed.maven.sanitizer.pom.CopyPom;
 import org.apache.maven.cli.MavenFacade;
 import org.apache.maven.project.MavenProject;
 
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.util.Collections;
 import java.util.List;
 
 import static com.github.signed.maven.sanitizer.path.BasePath.baseDirectoryOf;
 
 public class Application {
 
     private final CopyPom copyPom;
     private final CleanRoom cleanRoom;
 
     public static void main(String [] args){
         Path source = Paths.get("source");
        Path destination = Paths.get("destination");
         Application application = new Application(source, destination);
         application.configure();
         application.sanitize();
     }
 
     private final Path source;
     private final CopyProjectFiles copyProjectFiles;
 
     public Application(Path source, Path destination) {
         this.source = source;
         final SourceToDestinationTreeMapper mapper = new SourceToDestinationTreeMapper(source, destination);
         cleanRoom = new CleanRoom(new FileSystem(), mapper);
         copyPom = new CopyPom(cleanRoom);
         copyProjectFiles = new CopyProjectFiles(cleanRoom);
     }
 
     public void configure(){
         copyProjectFiles.addPathsToCopy(new SourceRoots());
         copyProjectFiles.addPathsToCopy(new ResourceRoots());
         copyProjectFiles.addPathsToCopy(new PathsInPluginConfiguration(new ExecutionProbe("org.apache.maven.plugins", "maven-war-plugin", Collections.singletonList(Paths.get("src/main/webapp")), "warSourceDirectory")));
         copyProjectFiles.addPathsToCopy(new PathsInPluginConfiguration(new ExecutionProbe("org.apache.maven.plugins", "maven-assembly-plugin", Collections.<Path>emptyList(), "descriptors")));
     }
 
     public void sanitize() {
         List<MavenProject> mavenProjects = new MavenFacade().getMavenProjects(source);
         for (MavenProject mavenProject : mavenProjects) {
             cleanRoom.createDirectoryAssociatedTo(baseDirectoryOf(mavenProject));
             copyPom.from(mavenProject);
             copyProjectFiles.copy(mavenProject);
         }
     }
 
 }
