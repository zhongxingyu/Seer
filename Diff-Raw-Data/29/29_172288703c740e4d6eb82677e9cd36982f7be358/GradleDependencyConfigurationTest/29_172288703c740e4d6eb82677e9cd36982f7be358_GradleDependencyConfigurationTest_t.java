 /*
  * Copyright 2013 Red Hat, Inc. and/or its affiliates.
  *
  * Licensed under the Eclipse Public License version 1.0, available at
  * http://www.eclipse.org/legal/epl-v10.html
  */
 package org.jboss.forge.addon.gradle.projects.model;
 
 import static org.jboss.forge.addon.gradle.projects.model.GradleDependencyConfiguration.COMPILE;
 import static org.jboss.forge.addon.gradle.projects.model.GradleDependencyConfiguration.DIRECT;
 import static org.jboss.forge.addon.gradle.projects.model.GradleDependencyConfiguration.OTHER;
 import static org.jboss.forge.addon.gradle.projects.model.GradleDependencyConfiguration.RUNTIME;
 import static org.jboss.forge.addon.gradle.projects.model.GradleDependencyConfiguration.TEST_COMPILE;
 import static org.jboss.forge.addon.gradle.projects.model.GradleDependencyConfiguration.TEST_RUNTIME;
 import static org.junit.Assert.fail;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.junit.Test;
 
 /**
  * Overrides tests are based on <a
  * href="http://www.gradle.org/docs/current/userguide/userguide_single.html#tab:configurations">Java plugin
  * configurations</a>.
  * 
  * @author Adam Wyłuda
  */
 public class GradleDependencyConfigurationTest
 {
    @Test
    public void testCompileOverrides()
    {
      assertOverridesOnly(COMPILE, COMPILE, RUNTIME, TEST_COMPILE, TEST_RUNTIME, OTHER);
    }
    
    @Test
    public void testRuntimeOverrides()
    {
      assertOverridesOnly(RUNTIME, RUNTIME, TEST_RUNTIME, OTHER);
    }
 
    @Test
    public void testTestCompileOverrides()
    {
      assertOverridesOnly(TEST_COMPILE, TEST_COMPILE, TEST_RUNTIME, OTHER);
    }
    
    @Test
    public void testTestRuntimeOverrides()
    {
      assertOverridesOnly(TEST_RUNTIME, TEST_RUNTIME, OTHER);
    }
    
    @Test
    public void testDirectOverrides()
    {
       assertOverridesOnly(DIRECT, DIRECT);
    }
    
    @Test
    public void testOtherOverrides()
    {
       assertOverridesOnly(OTHER, OTHER);
    }
 
    private void assertOverridesOnly(GradleDependencyConfiguration config, GradleDependencyConfiguration... all)
    {
       List<GradleDependencyConfiguration> shouldOverride = new ArrayList<GradleDependencyConfiguration>();
       Collections.addAll(shouldOverride, all);
 
       List<GradleDependencyConfiguration> shouldNotOverride = new ArrayList<GradleDependencyConfiguration>();
       Collections.addAll(shouldNotOverride, GradleDependencyConfiguration.values());
       shouldNotOverride.removeAll(shouldOverride);
       
       for (GradleDependencyConfiguration oneOfAll : shouldOverride)
       {
          if (!config.overrides(oneOfAll))
          {
             fail(config.toString() + " is not overriding " + oneOfAll);
          }
       }
       
       for (GradleDependencyConfiguration oneOfAll : shouldNotOverride)
       {
          if (config.overrides(oneOfAll))
          {
             fail(config.toString() + " overrides " + oneOfAll);
          }
       }
    }
 }
