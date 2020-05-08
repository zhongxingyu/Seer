 /*
  * Copyright 2011 Ian D. Bollinger
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.celeria.minecraft.backup;
 
 import static org.junit.Assert.*;
 import static org.mockito.Mockito.*;
 import com.google.inject.Inject;
 import org.bukkit.command.*;
 import org.bukkit.plugin.Plugin;
 import org.celeria.minecraft.guice.TaskScheduler;
 import org.joda.time.Duration;
 import org.joda.time.Period;
 import org.jukito.*;
 import org.junit.*;
 import org.junit.runner.RunWith;
 
 @RunWith(JukitoRunner.class)
 public class ArchivistTest {
     public static class Module extends JukitoModule {
         @Override
         protected void configureTest() {
             bindMock(Plugin.class);
            bind(Duration.class).toInstance(new Duration(20));
             try {
                 bind(PluginCommand.class).toConstructor(
                         PluginCommand.class.getDeclaredConstructor(
                                 String.class, Plugin.class));
             } catch (final NoSuchMethodException e) {
                 addError(e);
             }
         }
     }
 
     @Inject private Archivist mineBackUp;
     @Inject private TaskScheduler scheduler;
     @Inject private PluginCommand pluginCommand;
     @Inject private CommandExecutor manualBackUpExecutor;
     @Inject private DeleteOldBackupsTask deleteOldBackupsTask;
     @Inject private BackUpWorldsTask backUpTask;
     @Inject private Period backUpPeriod;
 
     @Before
     public void setUp() {
         // Do nothing for now.
     }
 
     @Test
     public void testRun() {
         mineBackUp.run();
         verify(scheduler).cancelTasks();
         verify(scheduler).repeatAsynchronousTask(deleteOldBackupsTask,
                 2 * backUpPeriod.getMillis());
         verify(scheduler).repeatSynchronousTask(backUpTask,
                 backUpPeriod.getMillis(), backUpPeriod.getMillis());
     }
 
     public void shouldSetManualBackUpExecutor() {
         assertSame(manualBackUpExecutor, pluginCommand.getExecutor());
     }
 
     @Test
     public void testStop() {
         mineBackUp.stop();
         verify(scheduler).cancelTasks();
     }
 }
