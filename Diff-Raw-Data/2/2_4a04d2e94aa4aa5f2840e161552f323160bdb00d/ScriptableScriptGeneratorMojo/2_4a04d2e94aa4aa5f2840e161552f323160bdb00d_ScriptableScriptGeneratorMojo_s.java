 /**
  * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
  * 
  * Please see distribution for license.
  */
 package com.opengamma.maven;
 
 import static org.twdata.maven.mojoexecutor.MojoExecutor.artifactId;
 import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
 import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
 import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
 import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;
 import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
 import static org.twdata.maven.mojoexecutor.MojoExecutor.groupId;
 import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;
 import static org.twdata.maven.mojoexecutor.MojoExecutor.version;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.maven.artifact.DependencyResolutionRequiredException;
 import org.apache.maven.execution.MavenSession;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.BuildPluginManager;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.project.MavenProject;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.opengamma.scripts.ScriptGenerator;
 import com.opengamma.scripts.ScriptUtils;
 import com.opengamma.scripts.Scriptable;
 
 import freemarker.template.Template;
 
 // CSOFF for Javadoc tags
 /**
  * Generates scripts from a template based on annotated classes.
  * 
  * @goal generate-scripts
  * @phase prepare-package
  * @requiresDependencyResolution compile
  * @threadSafe
  * @description 
  */
 // CSON
 public class ScriptableScriptGeneratorMojo extends AbstractMojo {
   // no underscore makes m2e auto-complete correct
 
   /**
    * Set to true to skip all processing, default false.
    * @parameter alias="skip" property="opengamma.generate.scripts.skip"
    */
   private boolean skip;  // CSIGNORE
   /**
    * Where the scripts should be generated.
    * @parameter alias="outputDir" default-value="${project.build.directory}/scripts"
    * @required
    */
   private File outputDir;  // CSIGNORE
   /**
    * The type to generate.
    * This is a shortcut, allowing all the files stored in the scripts project
    * to be accessed without needing to specify lots of config everywhere.
    * The only recognized value at present is 'tool'.
    * If this is set, then the unixTemplate and windowsTemplate fields will be
    * set, and a standard set of additional scripts added.
    * Use the 'unix' and 'windows' boolean flags to control which is output.
    * @parameter alias="type" property="opengamma.generate.scripts.type"
    */
   private String type;  // CSIGNORE
   /**
    * True to generate unix scripts, default true.
    * @parameter alias="unix" property="opengamma.generate.scripts.unix" default-value="true"
    */
   private boolean unix;  // CSIGNORE
   /**
    * The basic template file name on Unix.
    * This is used as the default template file name.
    * It effectively maps to 'java.lang.Object' in the unixTemplatesMap.
    * @parameter alias="unixTemplate"
    */
   private String unixTemplate;  // CSIGNORE
   /**
    * A map of class name to template file name on Unix.
    * @parameter alias="baseClassTemplateMap"
    */
   private Map<String, String> unixTemplatesMap;  // CSIGNORE
   /**
    * True to generate windows scripts, default true.
    * @parameter alias="windows" property="opengamma.generate.scripts.windows" default-value="true"
    */
   private boolean windows;  // CSIGNORE
   /**
    * The basic template file name on Windows.
    * This is used as the default template file name.
    * It effectively maps to 'java.lang.Object' in the windowsTemplatesMap.
    * @parameter alias="windowsTemplate"
    */
   private String windowsTemplate;  // CSIGNORE
   /**
    * A map of class name to template file name on Windows.
    * @parameter alias="baseClassTemplateMap"
    */
   private Map<String, String> windowsTemplatesMap;  // CSIGNORE
   /**
    * Additional scripts to copy unchanged.
    * @parameter alias="additionalScripts"
    */
   private String[] additionalScripts;  // CSIGNORE
   /**
    * Set to true to create an attached zip archive, default true.
    * @parameter alias="zip" property="opengamma.generate.scripts.zip" default-value="false"
    */
   private boolean zip;  // CSIGNORE
 
   /**
    * The project base directory.
    * @parameter default-value="${project.basedir}"
    * @required
    * @readonly
    */
   private File baseDir;  // CSIGNORE
   /**
    * @parameter default-value="${project}"
    * @required
    * @readonly
    */
   private MavenProject project;  // CSIGNORE
   /**
    * The current Maven session.
    *
    * @parameter default-value="${session}"
    * @required
    * @readonly
    */
   private MavenSession mavenSession;  // CSIGNORE
   /**
    * The Maven BuildPluginManager component.
    *
    * @component
    * @required
    */
   private BuildPluginManager mavenPluginManager;  // CSIGNORE
 
   //-------------------------------------------------------------------------
   /**
    * Maven plugin for generating scripts to run tools annotated with {@link Scriptable}.
    * <p>
    * Variables available in the Freemarker template are:
    * <ul>
    * <li> className - the fully-qualified Java class name
    * </ul>
    */
   @Override
   public void execute() throws MojoExecutionException, MojoFailureException {
     if (skip) {
       return;
     }
     
     if (type != null) {
       processType();
     }
     
     // make the output directory
     try {
       FileUtils.forceMkdir(outputDir);
     } catch (Exception ex) {
       throw new MojoExecutionException("Error creating output directory " + outputDir.getAbsolutePath(), ex);
     }
     
     // resolve the input
     Map<String, String> unixMap = buildInputMap(unixTemplate, unixTemplatesMap);
     Map<String, String> windowsMap = buildInputMap(windowsTemplate, windowsTemplatesMap);
     URL[] classpathUrls = buildProjectClasspath(project);
     ClassLoader classLoader = new URLClassLoader(classpathUrls, this.getClass().getClassLoader());
     
     // build the scripts
     if (unixMap.isEmpty() == false || windowsMap.isEmpty() == false) {
       List<Class<?>> scriptableClasses = buildScriptableClasses(classpathUrls, classLoader);
       getLog().info("Generating " + scriptableClasses.size() + " scripts");
       if (unix) {
         generateScripts(unixMap, scriptableClasses, classLoader, false);
       }
       if (windows) {
         generateScripts(windowsMap, scriptableClasses, classLoader, true);
       }
     } else {
       getLog().info("No scripts to generate");
     }
     copyAdditionalScripts(classLoader);
     if (zip) {
       buildZip();
     }
   }
 
   //-------------------------------------------------------------------------
   // processes the type, avoiding leaking config everywhere
   private void processType() throws MojoExecutionException {
     if (type.equals("tool") == false) {
       throw new MojoExecutionException("Invalid type, only 'tool' is valid: " + type);
     }
     if (StringUtils.isNotEmpty(unixTemplate) || StringUtils.isNotEmpty(windowsTemplate)) {
       throw new MojoExecutionException("When type is set, unixTemplate and windowsTemplate must not be set");
     }
     Set<String> additional = new HashSet<>();
     if (additionalScripts != null) {
       additional.addAll(Arrays.asList(additionalScripts));
     }
     if (unix) {
       unixTemplate = "com/opengamma/scripts/templates/tool-template-unix.ftl";
       additional.add("com/opengamma/scripts/run-tool.sh");
       additional.add("com/opengamma/scripts/java-utils.sh");
       additional.add("com/opengamma/scripts/componentserver-init-utils.sh");
       additional.add("com/opengamma/scripts/templates/project-utils.sh.ftl");
     }
     if (windows) {
       windowsTemplate = "com/opengamma/scripts/templates/tool-template-windows.ftl";
       additional.add("com/opengamma/scripts/run-tool.bat");
       additional.add("com/opengamma/scripts/run-tool-deprecated.bat");
     }
     additionalScripts = (String[]) additional.toArray(new String[additional.size()]);
   }
 
   //-------------------------------------------------------------------------
   // merges the input template and templateMap
   private static Map<String, String> buildInputMap(String template, Map<String, String> tempateMap) throws MojoExecutionException {
     Map<String, String> result = Maps.newHashMap();
     if (tempateMap != null) {
       result.putAll(tempateMap);
     }
     if (template != null) {
       if (result.containsKey("java.lang.Object")) {
         throw new MojoExecutionException("Cannot specify template if templateMap contains key 'java.lang.Object'");
       }
       result.put("java.lang.Object", template);
     }
     return result;
   }
 
   //-------------------------------------------------------------------------
   // extracts the project classpath from Maven
   @SuppressWarnings("unchecked")
   private static URL[] buildProjectClasspath(MavenProject project) throws MojoExecutionException {
     List<String> classpathElementList;
     try {
       classpathElementList = project.getCompileClasspathElements();
     } catch (DependencyResolutionRequiredException ex) {
       throw new MojoExecutionException("Error obtaining dependencies", ex);
     }
     return ScriptUtils.getClasspathURLs(classpathElementList);
   }
 
   //-------------------------------------------------------------------------
   // scans for the Scriptable annotation
   private static List<Class<?>> buildScriptableClasses(URL[] classpathUrls, ClassLoader classLoader) throws MojoExecutionException {
     Set<String> scriptableClassNames = ScriptUtils.findClassAnnotation(classpathUrls, Scriptable.class);
     List<Class<?>> result = Lists.newArrayList();
     for (String scriptable : scriptableClassNames) {
       result.add(resolveClass(scriptable, classLoader));
     }
     return result;
   }
 
   //-------------------------------------------------------------------------
   // generates the scripts
   private void generateScripts(Map<String, String> templates, List<Class<?>> scriptableClasses, ClassLoader classLoader, boolean windows) throws MojoExecutionException {
     if (templates.isEmpty()) {
       return;
     }
     Map<Class<?>, Template> templateMap = resolveTemplateMap(templates, classLoader);
     for (Class<?> scriptableClass : scriptableClasses) {
       Map<String, Object> templateData = new HashMap<String, Object>();
      templateData.put(ScriptGenerator.TEMPLATE_CLASS_NAME, scriptableClass);
       templateData.put(ScriptGenerator.TEMPLATE_PROJECT_NAME, project.getArtifactId());
       templateData.put(ScriptGenerator.TEMPLATE_PROJECT_JAR, project.getBuild().getFinalName() + ".jar");
       Template template = lookupTempate(scriptableClass, templateMap);
       ScriptGenerator.generate(scriptableClass.getName(), outputDir, template, templateData, windows);
     }
   }
 
   //-------------------------------------------------------------------------
   // resolves the template names to templates
   private Map<Class<?>, Template> resolveTemplateMap(Map<String, String> templates, ClassLoader classLoader) throws MojoExecutionException {
     Map<Class<?>, Template> templateMap = Maps.newHashMap();
     for (Map.Entry<String, String> unresolvedEntry : templates.entrySet()) {
       String className = unresolvedEntry.getKey();
       Class<?> clazz = resolveClass(className, classLoader);
       Template template = getTemplate(unresolvedEntry.getValue(), classLoader);
       templateMap.put(clazz, template);
     }
     return templateMap;
   }
 
   //-------------------------------------------------------------------------
   // load a template
   private Template getTemplate(String templateName, ClassLoader classLoader) throws MojoExecutionException {
     try {
       return ScriptGenerator.loadTemplate(baseDir, templateName, classLoader);
     } catch (Exception ex) {
       throw new MojoExecutionException("Error loading Freemarker template: " + templateName, ex);
     }
   }
 
   //-------------------------------------------------------------------------
   // lookup and find the best matching template
   private static Template lookupTempate(Class<?> scriptableClass, Map<Class<?>, Template> templateMap) throws MojoExecutionException {
     Class<?> clazz = scriptableClass;
     while (clazz != null) {
       Template template = templateMap.get(clazz);
       if (template != null) {
         return template;
       }
       clazz = clazz.getSuperclass();
     }
     throw new MojoExecutionException("No template found: " + scriptableClass);
   }
 
   //-------------------------------------------------------------------------
   // copies any additional scripts
   private void copyAdditionalScripts(ClassLoader classLoader) throws MojoExecutionException {
     if (additionalScripts == null || additionalScripts.length == 0) {
       getLog().info("No additional scripts to copy");
       return;
     }
     getLog().info("Copying " + additionalScripts.length + " additional script(s)");
     for (String script : additionalScripts) {
       File scriptFile = new File(baseDir, script);
       // process ftl if necessary
       if (script.endsWith(".ftl")) {
         generateAdditionalFile(classLoader, script, scriptFile);
       } else {
         // simple copy, either file or resource
         if (scriptFile.exists()) {
           copyAdditionalFile(classLoader, script, scriptFile);
         } else {
           copyAdditionalResource(classLoader, script, scriptFile);
         }
       }
     }
   }
 
   private void generateAdditionalFile(ClassLoader classLoader, String script, File scriptFile) throws MojoExecutionException {
     File destinationFile = new File(outputDir, scriptFile.getName().substring(0, scriptFile.getName().length() - 4));
     Map<String, Object> templateData = new HashMap<String, Object>();
     templateData.put(ScriptGenerator.TEMPLATE_PROJECT_NAME, project.getArtifactId());
     templateData.put(ScriptGenerator.TEMPLATE_PROJECT_JAR, project.getBuild().getFinalName() + ".jar");
     Template template = getTemplate(script, classLoader);
     ScriptGenerator.generate(destinationFile, template, templateData);
     destinationFile.setReadable(true, false);
     destinationFile.setExecutable(true, false);
   }
 
   private void copyAdditionalFile(ClassLoader classLoader, String script, File scriptFile) throws MojoExecutionException {
     if (scriptFile.isFile() == false) {
       throw new MojoExecutionException("Additional script is not a file, directories cannot be copied: " + scriptFile);
     }
     try {
       File destinationFile = new File(outputDir, scriptFile.getName());
       FileUtils.copyFileToDirectory(scriptFile, outputDir);
       destinationFile.setReadable(true, false);
       destinationFile.setExecutable(true, false);
     } catch (IOException ex) {
       throw new MojoExecutionException("Unable to copy additional script file: " + script, ex);
     }
   }
 
   private void copyAdditionalResource(ClassLoader classLoader, String script, File scriptFile) throws MojoExecutionException {
     try (InputStream resourceStream = classLoader.getResourceAsStream(script)) {
       if (resourceStream == null) {
         throw new MojoExecutionException("Additional script cannot be found: " + script);
       }
       File destinationFile = new File(outputDir, scriptFile.getName());
       FileUtils.writeByteArrayToFile(destinationFile, IOUtils.toByteArray(resourceStream));
       destinationFile.setReadable(true, false);
       destinationFile.setExecutable(true, false);
     } catch (IOException ex) {
       throw new MojoExecutionException("Unable to copy additional script resource: " + script, ex);
     }
   }
 
   //-------------------------------------------------------------------------
   // loads a class
   private static Class<?> resolveClass(String className, ClassLoader classLoader) throws MojoExecutionException {
     try {
       return classLoader.loadClass(className);
     } catch (ClassNotFoundException e) {
       throw new MojoExecutionException("Unable to resolve class " + className);
     }
   }
 
   //-------------------------------------------------------------------------
   // process the zipping
   private void buildZip() throws MojoExecutionException {
     // pick correct assembly xml
     String descriptorResource = "assembly-scripts.xml";
     if (unix && !windows) {
       descriptorResource = "assembly-unix.xml";
     } else if (windows && !unix) {
       descriptorResource = "assembly-windows.xml";
     }
     
     // copy it to a real file location
     File descriptorFile = new File(outputDir, new File(descriptorResource).getName());
     try (InputStream resourceStream = getClass().getResourceAsStream(descriptorResource)) {
       if (resourceStream == null) {
         throw new MojoExecutionException("Assembly descriptor cannot be found: " + descriptorResource);
       }
       FileUtils.writeByteArrayToFile(descriptorFile, IOUtils.toByteArray(resourceStream));
       descriptorFile.setReadable(true, false);
       descriptorFile.setExecutable(true, false);
     } catch (IOException ex) {
       throw new MojoExecutionException("Unable to copy additional script: " + descriptorFile, ex);
     }
     
     // run the assembly plugin
     executeMojo(
       plugin(
         groupId("org.apache.maven.plugins"),
         artifactId("maven-assembly-plugin"),
         version("2.4")
       ),
       goal("single"),
       configuration(
         element("descriptors", element("descriptor", descriptorFile.getAbsolutePath()))
       ),
       executionEnvironment(
         project,
         mavenSession,
         mavenPluginManager
       )
     );
     
     // delete the temp file
     descriptorFile.delete();
   }
 
 }
