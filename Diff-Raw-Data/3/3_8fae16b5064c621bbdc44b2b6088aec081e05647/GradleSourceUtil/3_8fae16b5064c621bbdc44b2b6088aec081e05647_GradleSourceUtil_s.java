 /*
  * Copyright 2013 Red Hat, Inc. and/or its affiliates.
  *
  * Licensed under the Eclipse Public License version 1.0, available at
  * http://www.eclipse.org/legal/epl-v10.html
  */
 package org.jboss.forge.addon.gradle.parser;
 
 import java.util.List;
 import java.util.Map;
 
 import org.gradle.jarjar.com.google.common.base.Joiner;
 import org.gradle.jarjar.com.google.common.collect.Lists;
 import org.gradle.jarjar.com.google.common.collect.Maps;
 import org.jboss.forge.addon.gradle.projects.exceptions.UnremovableElementException;
 import org.jboss.forge.addon.gradle.projects.model.GradleDependencyBuilder;
 import org.jboss.forge.addon.gradle.projects.model.GradleDependencyConfiguration;
 import org.jboss.forge.furnace.util.Strings;
 
 /**
  * Set of pure functions manipulating Gradle build scripts.
  * 
  * @author Adam Wyłuda
  */
 public class GradleSourceUtil
 {
    public static final String FORGE_LIBRARY = "forge.gradle";
    public static final String FORGE_LIBRARY_RESOURCE = "/forge.gradle";
    public static final String FORGE_OUTPUT_TASK = "forgeOutput";
    public static final String FORGE_OUTPUT_XML = "forge-output.xml";
    public static final String PROFILE_SUFFIX = "-profile.gradle";
 
    public static final String INCLUDE_FORGE_LIBRARY = "apply from: 'forge.gradle'\n";
    public static final String MANAGED_CONFIG = "managed";
    public static final String DIRECT_CONFIG = GradleDependencyConfiguration.DIRECT.getName();
 
    public static final String ARCHIVE_NAME_METHOD = "archiveName";
 
    public static final String PROJECT_PROPERTY_PREFIX = "ext.";
 
    /**
     * Sets the project name in Gradle project settings script.
     * 
     * @param source Contents of settings.gradle from project root directory.
     * @param projectPath Project path in format like :rootProject:subproject
     * @param newName New name for the project.
     * @returns Content of new settings.gradle which changes project name.
     */
    public static String setProjectName(String source, String projectPath, String newName)
    {
       source = SourceUtil.addNewLineAtEnd(source);
       source += String.format("project('%s').name = '%s'\n", projectPath, newName);
 
       return source;
    }
 
    public static String setArchiveName(String source, String archiveName)
    {
       SimpleGroovyParser parser = SimpleGroovyParser.fromSource(source);
       String archiveNameInvocationString = archiveNameString(archiveName);
 
       for (InvocationWithString invocation : parser.getInvocationsWithString())
       {
          if (invocation.getMethodName().equals(ARCHIVE_NAME_METHOD))
          {
             source = SourceUtil.removeSourceFragment(source, invocation);
             source = SourceUtil.insertString(source, archiveNameInvocationString,
                      invocation.getLineNumber(), invocation.getColumnNumber());
             return source;
          }
       }
 
       source = SourceUtil.addNewLineAtEnd(source) + archiveNameInvocationString + "\n";
 
       return source;
    }
 
    private static String archiveNameString(String archiveName)
    {
       return String.format("%s '%s'", ARCHIVE_NAME_METHOD, archiveName);
    }
 
    public static String insertDependency(String source, String group, String name, String version, String configuration)
    {
       String depString = String.format("%s '%s:%s:%s'", configuration, group, name, version);
       source = SourceUtil.insertIntoInvocationAtPath(source, depString, "dependencies");
       return source;
    }
 
    public static String removeDependency(String source, String group, String name, String version, String configuration)
             throws UnremovableElementException
    {
       String depString = String.format("%s:%s:%s", group, name, version);
       Map<String, String> depMap = Maps.newHashMap();
       depMap.put("group", group);
       depMap.put("name", name);
       depMap.put("version", version);
 
       SimpleGroovyParser parser = SimpleGroovyParser.fromSource(source);
       for (InvocationWithClosure deps : parser.allInvocationsAtPath("dependencies"))
       {
          // Search in string invocations
          for (InvocationWithString invocation : deps.getInvocationsWithString())
          {
             if (invocation.getMethodName().equals(configuration) && invocation.getString().equals(depString))
             {
                return SourceUtil.removeSourceFragmentWithLine(source, invocation);
             }
          }
 
          // Search in map invocations
          for (InvocationWithMap invocation : deps.getInvocationsWithMap())
          {
             if (invocation.getMethodName().equals(configuration) && invocation.getParameters().equals(depMap))
             {
                return SourceUtil.removeSourceFragmentWithLine(source, invocation);
             }
          }
       }
 
       throw new UnremovableElementException();
    }
 
    /**
     * Returns a list of dependencies which are declared in given source.
     */
    public static List<GradleDependencyBuilder> getDependencies(String source)
    {
       List<GradleDependencyBuilder> deps = Lists.newArrayList();
 
       SimpleGroovyParser parser = SimpleGroovyParser.fromSource(source);
       for (InvocationWithClosure invocation : parser.allInvocationsAtPath("dependencies"))
       {
          for (InvocationWithString stringInvocation : invocation.getInvocationsWithString())
          {
             if (isGradleString(stringInvocation.getString()))
             {
                GradleDependencyBuilder dep =
                         GradleDependencyBuilder.fromGradleString(stringInvocation.getMethodName(),
                                  stringInvocation.getString());
                deps.add(dep);
             }
          }
 
          for (InvocationWithMap mapInvocation : invocation.getInvocationsWithMap())
          {
             Map<String, String> params = mapInvocation.getParameters();
             if (params.containsKey("group") && params.containsKey("name") && params.containsKey("version"))
             {
                GradleDependencyBuilder dep = GradleDependencyBuilder.create()
                         .setConfiguration(mapInvocation.getMethodName())
                         .setGroup(params.get("group"))
                         .setName(params.get("name"))
                         .setVersion(params.get("version"));
                deps.add(dep);
             }
          }
       }
 
       return deps;
    }
 
    public static String insertDirectDependency(String source, String group, String name)
    {
       String depString = String.format("%s handler: it, group: '%s', name: '%s'", DIRECT_CONFIG, group, name);
       source = SourceUtil.insertIntoInvocationAtPath(source, depString, "dependencies");
       return source;
    }
 
    public static String removeDirectDependency(String source, String group, String name)
             throws UnremovableElementException
    {
       Map<String, String> depMap = Maps.newHashMap();
       depMap.put("group", group);
       depMap.put("name", name);
 
       SimpleGroovyParser parser = SimpleGroovyParser.fromSource(source);
       for (InvocationWithClosure deps : parser.allInvocationsAtPath("dependencies"))
       {
          for (InvocationWithMap invocation : deps.getInvocationsWithMap())
          {
             if (invocation.getMethodName().equals(DIRECT_CONFIG) &&
                      invocation.getParameters().equals(depMap))
             {
                return SourceUtil.removeSourceFragmentWithLine(source, invocation);
             }
          }
       }
 
       throw new UnremovableElementException();
    }
 
    /**
     * Returns a list of direct (defined using <i>direct</i> closure) dependencies declared in given source.
     */
    public static List<GradleDependencyBuilder> getDirectDependencies(String source)
    {
       List<GradleDependencyBuilder> deps = Lists.newArrayList();
 
       SimpleGroovyParser parser = SimpleGroovyParser.fromSource(source);
       for (InvocationWithClosure invocation : parser.allInvocationsAtPath("dependencies"))
       {
          for (InvocationWithMap mapInvocation : invocation.getInvocationsWithMap())
          {
             if (mapInvocation.getMethodName().equals(DIRECT_CONFIG))
             {
                Map<String, String> params = mapInvocation.getParameters();
                GradleDependencyBuilder dep = GradleDependencyBuilder.create()
                         .setGroup(params.get("group"))
                         .setName(params.get("name"));
                deps.add(dep);
             }
          }
       }
 
       return deps;
    }
 
    public static String insertManagedDependency(String source, String group, String name, String version,
             String configuration)
    {
       String depString = String.format("managed config: '%s', group: '%s', name: '%s', version: '%s'",
                configuration, group, name, version);
       source = SourceUtil.insertIntoInvocationAtPath(source, depString, "allprojects", "dependencies");
       return source;
    }
 
    public static String removeManagedDependency(String source, String group, String name, String version,
             String configuration) throws UnremovableElementException
    {
       SimpleGroovyParser parser = SimpleGroovyParser.fromSource(source);
       for (InvocationWithClosure deps : parser.allInvocationsAtPath("allprojects", "dependencies"))
       {
          for (InvocationWithMap invocation : deps.getInvocationsWithMap())
          {
             Map<String, String> params = invocation.getParameters();
             if (invocation.getMethodName().equals(MANAGED_CONFIG) &&
                      params.get("group").equals(group) &&
                      params.get("name").equals(name) &&
                     params.get("version").equals(version) &&
                     params.get("config").equals(configuration))
             {
                return SourceUtil.removeSourceFragmentWithLine(source, invocation);
             }
          }
       }
 
       throw new UnremovableElementException();
    }
 
    /**
     * Returns a list of managed dependencies (declared using <i>managed</i> closure) in given source.
     */
    public static List<GradleDependencyBuilder> getManagedDependencies(String source)
    {
       List<GradleDependencyBuilder> deps = Lists.newArrayList();
 
       SimpleGroovyParser parser = SimpleGroovyParser.fromSource(source);
       for (InvocationWithClosure invocation : parser.allInvocationsAtPath("allprojects", "dependencies"))
       {
          for (InvocationWithMap mapInvocation : invocation.getInvocationsWithMap())
          {
             if (mapInvocation.getMethodName().equals(MANAGED_CONFIG))
             {
                Map<String, String> params = mapInvocation.getParameters();
                GradleDependencyBuilder dep = GradleDependencyBuilder.create()
                         .setConfiguration(params.get("config"))
                         .setGroup(params.get("group"))
                         .setName(params.get("name"))
                         .setVersion(params.get("version"));
                deps.add(dep);
             }
          }
       }
 
       return deps;
    }
 
    /**
     * Adds {@code apply plugin: 'clazz'} after the last plugin application, if there are no other applied plugins then
     * it adds it at the end of source.
     */
    public static String insertPlugin(String source, String clazz)
    {
       String pluginString = String.format("\napply plugin: '%s'", clazz);
 
       SimpleGroovyParser parser = SimpleGroovyParser.fromSource(source);
       if (parser.getInvocationsWithMap().size() > 0)
       {
          InvocationWithMap lastInvocation = parser.getInvocationsWithMap().get(
                   parser.getInvocationsWithMap().size() - 1);
          source = SourceUtil.insertString(source, pluginString, lastInvocation.getLastLineNumber(),
                   lastInvocation.getLastColumnNumber());
       }
       else
       {
          source += pluginString;
       }
       return source;
    }
 
    public static String removePlugin(String source, String clazz)
             throws UnremovableElementException
    {
       SimpleGroovyParser parser = SimpleGroovyParser.fromSource(source);
       for (InvocationWithMap invocation : parser.getInvocationsWithMap())
       {
          if ((invocation.getMethodName().equals("apply") || invocation.getMethodName().equals("project.apply")) &&
                   invocation.getParameters().size() == 1 &&
                   clazz.equals(invocation.getParameters().get("plugin")))
          {
             return SourceUtil.removeSourceFragmentWithLine(source, invocation);
          }
       }
 
       throw new UnremovableElementException();
    }
 
    public static String insertRepository(String source, String name, String url)
    {
       // TODO Repository name?
       String repoString = String.format("url '%s'", url);
       source = SourceUtil.insertIntoInvocationAtPath(source, repoString, "repositories", "maven");
       return source;
    }
 
    public static String removeRepository(String source, String name, String url)
             throws UnremovableElementException
    {
       SimpleGroovyParser parser = SimpleGroovyParser.fromSource(source);
       for (InvocationWithClosure repos : parser.allInvocationsAtPath("repositories", "maven"))
       {
          for (InvocationWithString invocation : repos.getInvocationsWithString())
          {
             if (invocation.getMethodName().equals("url") &&
                      invocation.getString().equals(url))
             {
                return SourceUtil.removeSourceFragmentWithLine(source, invocation);
             }
          }
       }
 
       throw new UnremovableElementException();
    }
 
    /**
     * Sets <b>dynamic</b> project property in the build script.
     * <p/>
     * For example setProperty for key X and value Y will append X = Y to the build script.
     * <p/>
     * Be aware that dynamic properties are scheduled to be removed in Gradle 2.0 so always use project's <b>ext</b>
     * namespace for non-standard properties, like setProperty(source, "ext.myProp", "value").
     */
    public static String setProperty(String source, String key, String value)
    {
       SimpleGroovyParser parser = SimpleGroovyParser.fromSource(source);
       String assignmentString = variableAssignmentString(key, value);
       for (VariableAssignment assignment : parser.getVariableAssignments())
       {
          // If it's already defined somewhere
          if (assignment.getVariable().equals(key))
          {
             source = SourceUtil.removeSourceFragment(source, assignment);
             source = SourceUtil.insertString(source, assignmentString,
                      assignment.getLineNumber(), assignment.getColumnNumber());
             return source;
          }
       }
 
       // If it was not defined anywhere
       source = SourceUtil.addNewLineAtEnd(source) + assignmentString + "\n";
 
       return source;
    }
 
    private static String variableAssignmentString(String variable, String value)
    {
       return String.format("%s = '%s'", variable, value);
    }
 
    public static String removeProperty(String source, String key) throws UnremovableElementException
    {
       SimpleGroovyParser parser = SimpleGroovyParser.fromSource(source);
       for (VariableAssignment assignment : parser.getVariableAssignments())
       {
          if (assignment.getVariable().equals(key))
          {
             return SourceUtil.removeSourceFragmentWithLine(source, assignment);
          }
       }
 
       throw new UnremovableElementException();
    }
 
    // There is no way to remove a task because tasks are composed of many actions
    // so we can only insert new tasks
    public static String insertTask(String source, String name, List<String> dependsOn, String type, String code)
    {
       String taskDeclarationString = taskDeclarationString(name, dependsOn, type);
       String taskString = String.format("\ntask %s << {\n%s\n}\n", taskDeclarationString, code);
       source += taskString;
       return source;
    }
 
    private static String taskDeclarationString(String name, List<String> dependsOn, String type)
    {
       if (dependsOn.isEmpty() && Strings.isNullOrEmpty(type))
       {
          return name;
       }
       else
       {
          String dependsOnString = dependsOnString(dependsOn);
          String typeString = Strings.isNullOrEmpty(type) ? "" : ", type: " + type;
          return String.format("('%s'%s%s)", name, dependsOnString, typeString);
       }
    }
 
    private static String dependsOnString(List<String> dependsOn)
    {
       if (dependsOn.size() == 0)
       {
          return "";
       }
       else if (dependsOn.size() == 1)
       {
          return ", dependsOn: '" + dependsOn.get(0) + "'";
       }
       else
       {
          List<String> dependsOnInQuotes = Lists.newArrayList();
          for (String dep : dependsOn)
          {
             dependsOnInQuotes.add("'" + dep + "'");
          }
          return ", dependsOn: [" + Joiner.on(", ").join(dependsOnInQuotes) + "]";
       }
    }
 
    /**
     * Checks if source includes forge library and if not then adds it.
     */
    public static String checkForIncludeForgeLibraryAndInsert(String source)
    {
       SimpleGroovyParser parser = SimpleGroovyParser.fromSource(source);
       for (InvocationWithMap invocation : parser.getInvocationsWithMap())
       {
          if (invocation.getMethodName().equals("apply"))
          {
             Map<String, String> map = invocation.getParameters();
             String from = map.get("from");
 
             // If it is already included in source then we just return the source
             if ("forge.gradle".equals(from))
             {
                return source;
             }
          }
       }
 
       // If statement including forge library was not found then we add it
       return INCLUDE_FORGE_LIBRARY + source;
    }
 
    /**
     * Returns a map of properties which are clearly declared in a given build script (i.e. they can be
     * modified/removed). Also, they must be project properties, declared in project.ext namespace.
     */
    public static Map<String, String> getDirectProperties(String source)
    {
       SimpleGroovyParser parser = SimpleGroovyParser.fromSource(source);
       Map<String, String> properties = Maps.newHashMap();
       for (VariableAssignment assignment : parser.getVariableAssignments())
       {
          if (assignment.getVariable().startsWith(PROJECT_PROPERTY_PREFIX))
          {
             properties.put(assignment.getVariable().substring(PROJECT_PROPERTY_PREFIX.length()),
                      assignment.getValue());
          }
       }
       return properties;
    }
 
    private static boolean isGradleString(String string)
    {
       return string.split(":").length == 3;
    }
 }
