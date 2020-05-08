 package com.cloudbees.sdk.commands;
 
 import com.cloudbees.sdk.ArtifactInstallFactory;
 import com.cloudbees.sdk.GAV;
 import com.cloudbees.sdk.cli.BeesCommand;
 import com.cloudbees.sdk.cli.CLICommand;
 import com.cloudbees.sdk.utils.Helper;
 import com.google.inject.Provider;
 
 import javax.inject.Inject;
 import java.io.File;
 import java.io.IOException;
 
 /**
  * @author Fabian Donze
  */
 @CLICommand("sdk:plugin:install")
 @BeesCommand(group="SDK", description = "Installs a CLI plugin")
 public class PluginInstallCommand extends Command {
 
     String artifact;
     String localrepo;
     File jar;
     File pom;
 
     @Inject
     Provider<ArtifactInstallFactory> artifactInstallFactoryProvider;
 
     public PluginInstallCommand() {
     }
 
     public void setArtifact(String artifact) {
         this.artifact = artifact;
     }
 
     public String getArtifact() throws IOException {
         if (artifact == null) artifact = Helper.promptFor("Artifact: ", true);
         return artifact;
     }
 
     public void setJar(String jar) {
         this.jar = new File(jar);
     }
 
     public void setPom(String pom) {
         this.pom = new File(pom);
     }
 
     public void setLocalrepo(String localrepo) {
         this.localrepo = localrepo;
     }
 
     private GAV parseGav(String artifact) {
         String[] tokens = artifact.split(":");
         if (tokens.length == 3)
             return new GAV(artifact);
        else
             return new GAV(tokens[0], tokens[1], "LATEST");
     }
 
     @Override
     protected String getUsageMessage() {
         return "ARTIFACT (groupId:artifact[:version] of the plugin to install)";
     }
 
     @Override
     protected boolean preParseCommandLine() {
         // add the Options
         addOption("j", "jar", true, "the plugin jar file to install");
         addOption("p", "pom", true, "the plugin pom.xml file to install");
         addOption("l", "localrepo", true, "the maven local repo", true);
 
         return true;
     }
 
     @Override
     protected boolean postParseCheck() {
         if (super.postParseCheck()) {
             setArtifact(getParameters().get(0));
             return true;
         }
         return false;
     }
 
     @Override
     protected boolean execute() throws Exception {
         // install the artifact
         try {
             ArtifactInstallFactory installFactory = artifactInstallFactoryProvider.get();
             if (localrepo != null)
                 installFactory.setLocalRepository(localrepo);
             GAV gav = parseGav(artifact);
             if (pom != null && jar != null) {
                 gav = installFactory.install(gav, jar, pom);
             } else
                 gav = installFactory.install(gav);
             System.out.println("Plugin installed: " + gav);
         } catch (Exception e) {
             throw (IOException) new IOException("Failed to install " + getArtifact()).initCause(e);
         }
         return true;
     }
 
 }
 
