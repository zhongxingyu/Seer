 /*
  * JBoss, by Red Hat.
  * Copyright 2010, Red Hat, Inc., and individual contributors
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package org.jboss.forge.eclipse;
 
 import java.util.List;
 
 import javax.enterprise.event.Event;
 import javax.inject.Inject;
 
 import org.apache.maven.model.Model;
 import org.apache.maven.model.Repository;
 import org.jboss.forge.maven.MavenCoreFacet;
 import org.jboss.forge.maven.MavenPluginFacet;
 import org.jboss.forge.maven.plugins.MavenPlugin;
 import org.jboss.forge.maven.plugins.MavenPluginBuilder;
 import org.jboss.forge.parser.JavaParser;
 import org.jboss.forge.parser.java.Field;
 import org.jboss.forge.parser.java.JavaClass;
 import org.jboss.forge.project.Project;
 import org.jboss.forge.project.dependencies.Dependency;
 import org.jboss.forge.project.dependencies.DependencyBuilder;
 import org.jboss.forge.project.facets.DependencyFacet;
 import org.jboss.forge.project.facets.JavaSourceFacet;
 import org.jboss.forge.project.facets.events.InstallFacets;
 import org.jboss.forge.resources.DirectoryResource;
 import org.jboss.forge.resources.java.JavaResource;
 import org.jboss.forge.shell.PromptType;
 import org.jboss.forge.shell.Shell;
 import org.jboss.forge.shell.ShellMessages;
 import org.jboss.forge.shell.ShellPrompt;
 import org.jboss.forge.shell.plugins.Alias;
 import org.jboss.forge.shell.plugins.Command;
 import org.jboss.forge.shell.plugins.DefaultCommand;
 import org.jboss.forge.shell.plugins.Option;
 import org.jboss.forge.shell.plugins.PipeOut;
 import org.jboss.forge.shell.plugins.Plugin;
 import org.jboss.forge.shell.util.ResourceUtil;
 
 /**
  * @author Jérmie Lagarde
  * 
  */
 @Alias("eclipse-plugins")
 // @RequiresProject
 // @RequiresFacet({ DependencyFacet.class, MavenPluginFacet.class })
 public class EclipsePlugin implements Plugin
 {
 
    private static final String ECLIPSE_HELIOS_REPOSITORY = "http://download.eclipse.org/releases/helios";
    private static final String ECLIPSE_INDIGO_REPOSITORY = "http://download.eclipse.org/releases/indigo";
    private static final String ECLIPSE_JUNO_REPOSITORY = "http://download.eclipse.org/releases/juno";
 
    @Inject
    private Project project;
 
    @Inject
    private ShellPrompt prompt;
 
    @Inject
    private Shell shell;
 
    @Inject
    private Event<InstallFacets> event;
 
    @DefaultCommand
    public void status(final PipeOut out)
    {
       if (project.hasFacet(EclipsePluginFacet.class))
       {
          ShellMessages.success(out, "EclipsePlugin is installed.");
       }
       else
       {
          ShellMessages.warn(out, "EclipsePlugin is NOT installed.");
       }
    }
 
    @Command("setup")
    public void setup(
             @Option(name = "tychoVersion") String tychoVersion,
             @Option(name = "type", required = false, completer = EclipsePackagingTypeCompleter.class, defaultValue = "eclipse-plugin") final EclipsePackagingType type,
             @Option(name = "sourceDirectory", required = false, defaultValue = "src") String sourceDirectory,
             PipeOut out)
    {
       installTychoMavenPlugin(out, tychoVersion);
       installPackaging(type, out);
 
       if (EclipsePackagingType.PLUGIN.equals(type))
       {
          if (!project.hasFacet(EclipsePluginFacet.class))
          {
             event.fire(new InstallFacets(EclipsePluginFacet.class));
          }
       }
    }
 
    private void installTychoMavenPlugin(PipeOut out, String tychoVersion)
    {
       DependencyFacet dependencyFacet = project.getFacet(DependencyFacet.class);
       MavenPluginFacet pluginFacet = project.getFacet(MavenPluginFacet.class);
 
       DependencyBuilder tychoPlugin = DependencyBuilder.create().setGroupId("org.eclipse.tycho")
                .setArtifactId("tycho-maven-plugin");
       if (!pluginFacet.hasPluginRepository(ECLIPSE_JUNO_REPOSITORY)
                || !pluginFacet.hasPluginRepository(ECLIPSE_INDIGO_REPOSITORY)
                || !pluginFacet.hasPluginRepository(ECLIPSE_HELIOS_REPOSITORY))
       {
          // pluginFacet.addPluginRepository("eclipse-juno", ECLIPSE_INDIGO_REPOSITORY);
          MavenCoreFacet maven = project.getFacet(MavenCoreFacet.class);
          Model pom = maven.getPOM();
          Repository repo = new Repository();
          repo.setId("eclipse-juno");
          repo.setUrl(ECLIPSE_JUNO_REPOSITORY);
          repo.setLayout("p2");
          pom.getPluginRepositories().add(repo);
          maven.setPOM(pom);
       }
 
       List<Dependency> versions = dependencyFacet.resolveAvailableVersions(tychoPlugin);
 
       if (null == versions || versions.size() == 0)
       {
          ShellMessages.error(out, "Cannot find any versions for dependency " + tychoPlugin.toString()
                   + ", check maven settings.");
          return;
       }
 
       Dependency choosenVersion = null;
       if (tychoVersion != null)
       {
          for (Dependency dependency : versions)
          {
             if (dependency.getVersion().equals(tychoVersion))
             {
                choosenVersion = dependency;
             }
          }
          if (choosenVersion == null)
          {
             ShellMessages.warn(out, "Cannot find " + tychoVersion + " version for dependency " + tychoPlugin.toString()
                      + ".");
          }
       }
 
       if (choosenVersion == null)
          choosenVersion = prompt.promptChoiceTyped("Which version of Eclipse Tycho do you want to install?", versions,
                   versions.get(versions.size() - 1));
 
       dependencyFacet.setProperty("tycho-version", choosenVersion.getVersion());
       tychoPlugin.setVersion("${tycho-version}");
 
       MavenPlugin plugin;
       if (!pluginFacet.hasPlugin(tychoPlugin))
       {
          plugin = MavenPluginBuilder.create().setDependency(tychoPlugin).setExtensions(true);
          pluginFacet.addPlugin(plugin);
       }
       else
       {
          plugin = pluginFacet.getPlugin(tychoPlugin);
       }
    }
 
    private void installPackaging(EclipsePackagingType type, PipeOut out)
    {
       MavenCoreFacet mavenFacet = project.getFacet(MavenCoreFacet.class);
       Model pom = mavenFacet.getPOM();
       pom.setPackaging(type.getType());
       mavenFacet.setPOM(pom);
    }
 
    @Command(value = "create-activator", help = "Create a Activator class")
    public void createActivator(
             @Option(required = true,
                      name = "named",
                      description = "The Activator class name") final String activatorName,
             @Option(required = false,
                      name = "package",
                      type = PromptType.JAVA_PACKAGE,
                      description = "The package name") final String packageName)
             throws Throwable
    {
       final JavaSourceFacet java = project.getFacet(JavaSourceFacet.class);
 
       String activatorPackage;
 
       if ((packageName != null) && !"".equals(packageName))
       {
          activatorPackage = packageName;
       }
       else if (getPackagePortionOfCurrentDirectory() != null)
       {
          activatorPackage = getPackagePortionOfCurrentDirectory();
       }
       else
       {
          activatorPackage = shell.promptCommon(
                   "In which package you'd like to create this Activator class, or enter for default",
                   PromptType.JAVA_PACKAGE, java.getBasePackage());
       }
 
       JavaClass javaClass = JavaParser.create(JavaClass.class)
                .setPackage(activatorPackage)
                .setName(activatorName)
                .setPublic()
               .addInterface("org.osgi.framework.BundleActivator");
 
       Field<JavaClass> context = javaClass.addField("private static BundleContext context;");
       javaClass.addMethod().setReturnType(context.getTypeInspector().toString()).setName("getContext")
                .setPublic().setStatic(true).setBody("return context;");
 
       javaClass.addImport("org.osgi.framework.BundleContext");
       javaClass.addMethod().setReturnTypeVoid().setName("start").setPublic()
                .setParameters("BundleContext bundleContext").addThrows("Exception")
                .setBody("Activator.context = bundleContext;");
 
       javaClass.addMethod().setReturnTypeVoid().setName("stop").setPublic()
                .setParameters("BundleContext bundleContext").addThrows("Exception")
                .setBody("Activator.context = null;");
 
       JavaResource javaFileLocation = java.saveJavaSource(javaClass);
 
       shell.println("Created eclipse plugin activator [" + javaClass.getQualifiedName() + "]");
 
       /**
        * Pick up the generated resource.
        */
       shell.execute("pick-up " + javaFileLocation.getFullyQualifiedName().replace(" ", "\\ "));
    }
 
    /**
     * Retrieves the package portion of the current directory if it is a package, null otherwise.
     * 
     * @return String representation of the current package, or null
     */
    private String getPackagePortionOfCurrentDirectory()
    {
       for (org.jboss.forge.resources.DirectoryResource r : project.getFacet(JavaSourceFacet.class).getSourceFolders())
       {
          final DirectoryResource currentDirectory = shell.getCurrentDirectory();
          if (ResourceUtil.isChildOf(r, currentDirectory))
          {
             // Have to remember to include the last slash so it's not part of the package
             return currentDirectory.getFullyQualifiedName().replace(r.getFullyQualifiedName() + "/", "")
                      .replaceAll("/", ".");
          }
       }
       return null;
    }
 
 }
