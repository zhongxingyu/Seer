 /*
  * Copyright 2013 Red Hat, Inc. and/or its affiliates.
  *
  * Licensed under the Eclipse Public License version 1.0, available at
  * http://www.eclipse.org/legal/epl-v10.html
  */
 package org.jboss.forge.addon.shell;
 
 import java.util.concurrent.TimeUnit;
 
 import javax.inject.Inject;
 
 import org.jboss.arquillian.container.test.api.Deployment;
 import org.jboss.arquillian.junit.Arquillian;
 import org.jboss.forge.addon.shell.test.ShellTest;
 import org.jboss.forge.addon.ui.result.Failed;
 import org.jboss.forge.addon.ui.result.Result;
 import org.jboss.forge.arquillian.AddonDependency;
 import org.jboss.forge.arquillian.Dependencies;
 import org.jboss.forge.arquillian.archive.ForgeArchive;
 import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
 import org.jboss.shrinkwrap.api.ShrinkWrap;
 import org.junit.Assert;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 /**
  * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
  */
 @RunWith(Arquillian.class)
 public class ShellTestHarnessTest
 {
    @Deployment
    @Dependencies({
             @AddonDependency(name = "org.jboss.forge.addon:ui"),
             @AddonDependency(name = "org.jboss.forge.addon:shell-test-harness"),
             @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static ForgeArchive getDeployment()
    {
       ForgeArchive archive = ShrinkWrap
                .create(ForgeArchive.class)
                .addClasses(MockCommandExecutionListener.class)
                .addBeansXML()
                .addAsAddonDependencies(
                         AddonDependencyEntry.create("org.jboss.forge.addon:ui"),
                         AddonDependencyEntry.create("org.jboss.forge.addon:shell-test-harness"),
                         AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
                );
 
       return archive;
    }
 
    @Inject
    private ShellTest test;
 
    @Test
    public void testShellTestUtility() throws Exception
    {
       Assert.assertNotNull(test);
 
      Result result = test.execute("command-list", 15, TimeUnit.SECONDS);
       Assert.assertNotNull(result);
       Assert.assertFalse(result instanceof Failed);
    }
 
 }
