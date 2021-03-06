/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2008-2012 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
 package org.sonar.batch.bootstrap;
 
 import org.junit.Test;
 import org.mockito.invocation.InvocationOnMock;
 import org.mockito.stubbing.Answer;
 import org.picocontainer.Startable;
 import org.sonar.api.batch.InstantiationStrategy;
 import org.sonar.api.batch.bootstrap.ProjectDefinition;
 import org.sonar.api.batch.bootstrap.ProjectReactor;
 import org.sonar.api.config.Settings;
 import org.sonar.api.platform.ComponentContainer;
 
 import static org.fest.assertions.Assertions.assertThat;
 import static org.mockito.Matchers.any;
 import static org.mockito.Matchers.eq;
 import static org.mockito.Mockito.doAnswer;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.verify;
 
 public class BootstrapExtensionExecutorTest {
   private ProjectReactor reactor = new ProjectReactor(ProjectDefinition.create().setKey("foo"));
 
   @Test
   public void start() {
     ComponentContainer container = new ComponentContainer();
     // dependencies required for ProjectExclusions
     container.addSingleton(reactor);
     container.addSingleton(new Settings());
 
     // declare a bootstrap component
     final Startable bootstrapComponent = mock(Startable.class);
     ExtensionInstaller installer = mock(ExtensionInstaller.class);
     doAnswer(new Answer() {
       public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
         ComponentContainer childContainer = (ComponentContainer) invocationOnMock.getArguments()[0];
         childContainer.addSingleton(bootstrapComponent);
         return null;
       }
     }).when(installer).install(any(ComponentContainer.class), eq(InstantiationStrategy.BOOTSTRAP));
 
     BootstrapExtensionExecutor executor = new BootstrapExtensionExecutor(container, installer);
     executor.start();
 
     // should install bootstrap components into a ephemeral container
     verify(installer).install(any(ComponentContainer.class), eq(InstantiationStrategy.BOOTSTRAP));
     verify(bootstrapComponent).start();
     verify(bootstrapComponent).stop();
 
     // the ephemeral container is destroyed
     assertThat(container.getComponentByType(ProjectExclusions.class)).isNull();
     assertThat(container.getChild()).isNull();
   }
 
 
 }
