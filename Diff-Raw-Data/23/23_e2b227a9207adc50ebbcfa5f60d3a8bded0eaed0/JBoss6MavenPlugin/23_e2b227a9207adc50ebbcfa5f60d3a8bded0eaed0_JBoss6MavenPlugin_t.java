 package org.jboss.forge.jrebel.container;
 
 import javax.inject.Inject;
 
 import org.jboss.forge.project.Project;
 import org.jboss.forge.shell.Shell;
 
@Container(ContainerType.JBOSS_AS6)
 public class JBoss6MavenPlugin extends CargoMavenPlugin {
     
     @Inject 
     private Shell shell;
     
     @Inject
     private Project project;
     
     @Override
     protected String getName() {
         return "JBoss 6";
     }
 
     @Override
     protected String getContainerId() {
         return "jboss6x";
     }
 
     @Override
     protected String getContainerHome() {
         return "${env.JBOSS_HOME}";
     }
 
     @Override
     protected Shell getShell() {
         return shell;
     }
 
     @Override
     protected Project getProject() {
         return project;
     }
 
 }
