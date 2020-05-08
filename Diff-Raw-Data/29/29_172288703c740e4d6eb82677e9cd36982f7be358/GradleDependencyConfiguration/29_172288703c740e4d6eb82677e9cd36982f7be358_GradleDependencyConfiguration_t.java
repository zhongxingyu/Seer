 /*
  * Copyright 2013 Red Hat, Inc. and/or its affiliates.
  *
  * Licensed under the Eclipse Public License version 1.0, available at
  * http://www.eclipse.org/legal/epl-v10.html
  */
 package org.jboss.forge.addon.gradle.projects.model;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * This is equivalent to Maven scope.
  * 
  * @author Adam Wyłuda
  */
 public enum GradleDependencyConfiguration
 {
    COMPILE("compile", "compile"),
    RUNTIME("runtime", "runtime"),
    TEST_COMPILE("testCompile", "test"),
    TEST_RUNTIME("testRuntime", "runtime"),
 
    /**
     * Direct dependency configuration (which doesn't have defined version and config).
     */
    DIRECT("direct", null),
 
    /**
     * Dependency configuration not defined in {@link GradleDependencyConfiguration}.
     */
    OTHER("", null);
 
    private static class ConfigContainer
    {
       private static final Map<String, GradleDependencyConfiguration> BY_NAME_MAP =
                new HashMap<String, GradleDependencyConfiguration>();
       private static final Map<String, GradleDependencyConfiguration> BY_MAVEN_SCOPE_MAP =
                new HashMap<String, GradleDependencyConfiguration>();
    }
 
    static
    {
       fillMavenScopeMap();
       configureExtends();
    }
 
    private final String name;
    private final String mavenScope;
   private final List<GradleDependencyConfiguration> extendedBy = new ArrayList<GradleDependencyConfiguration>();
 
    private GradleDependencyConfiguration(String name, String mavenScope)
    {
       this.name = name;
       this.mavenScope = mavenScope;
       ConfigContainer.BY_NAME_MAP.put(name, this);
    }
 
    public String getName()
    {
       return name;
    }
 
    public String toMavenScope()
    {
       return mavenScope;
    }
 
    /**
     * Defines partial ordering (in a mathematical sense) relation for configurations.
     */
    public boolean overrides(GradleDependencyConfiguration config)
    {
       if (config == this)
       {
          return true;
       }
      for (GradleDependencyConfiguration extendsConfig : extendedBy)
       {
          if (extendsConfig.overrides(config))
          {
             return true;
          }
       }
       return false;
    }
 
    /**
     * Searches map for config with specified name.
     * 
     * @param name Name of the configuration.
     * @return Configuration with specified name, if it doesn't exist, returns {@link #OTHER}.
     */
    public static GradleDependencyConfiguration fromName(String name)
    {
       GradleDependencyConfiguration config = ConfigContainer.BY_NAME_MAP.get(name);
       return config != null ? config : OTHER;
    }
 
    /**
     * Returns Gradle config corresponding to given maven scope.
     */
    public static GradleDependencyConfiguration fromMavenScope(String mavenScope)
    {
       GradleDependencyConfiguration config = ConfigContainer.BY_MAVEN_SCOPE_MAP.get(mavenScope);
       return config != null ? config : OTHER;
    }
 
    private static void fillMavenScopeMap()
    {
       ConfigContainer.BY_MAVEN_SCOPE_MAP.put("compile", COMPILE);
       ConfigContainer.BY_MAVEN_SCOPE_MAP.put("provided", COMPILE);
       ConfigContainer.BY_MAVEN_SCOPE_MAP.put("runtime", RUNTIME);
       ConfigContainer.BY_MAVEN_SCOPE_MAP.put("test", TEST_COMPILE);
       ConfigContainer.BY_MAVEN_SCOPE_MAP.put("system", COMPILE);
       ConfigContainer.BY_MAVEN_SCOPE_MAP.put("import", COMPILE);
    }
 
    private static void configureExtends()
    {
       // http://www.gradle.org/docs/current/userguide/userguide_single.html#tab:configurations
      Collections.addAll(COMPILE.extendedBy, RUNTIME, TEST_COMPILE);
      Collections.addAll(RUNTIME.extendedBy, OTHER, TEST_RUNTIME);
      Collections.addAll(TEST_COMPILE.extendedBy, OTHER, TEST_RUNTIME);
      Collections.addAll(TEST_RUNTIME.extendedBy, OTHER);
    }
 }
