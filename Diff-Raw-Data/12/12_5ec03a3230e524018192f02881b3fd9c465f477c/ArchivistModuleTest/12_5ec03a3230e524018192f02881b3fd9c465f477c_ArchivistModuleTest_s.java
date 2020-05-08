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
 import com.google.inject.Inject;
 import org.bukkit.Server;
 import org.bukkit.command.PluginCommand;
 import org.joda.time.Instant;
 import org.joda.time.format.DateTimeFormatter;
 import org.jukito.JukitoRunner;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 @RunWith(JukitoRunner.class)
 public class ArchivistModuleTest {
     @Inject private ArchivistModule module;
 
     @Test
     public void testProvidePluginCommand(final Server server) {
         final PluginCommand pluginCommand = module.providePluginCommand(server);
         // assertEquals("backup", pluginCommand.getName());
         // TODO: verify somrthing.
     }
 
     @Test
     public void testProvideDateTimeFormatter() {
         @SuppressWarnings("unused")
        final DateTimeFormatter dateTimeFormatter = module.provideDateTimeFormatter();
         // TODO: verify something.
     }
 
     @Test
     public void testProvideCurrentTime() {
         final Instant before = Instant.now();
         final Instant currentTime = module.provideCurrentTime();
         final Instant after = Instant.now();
         assertTrue(currentTime.equals(before) || currentTime.isAfter(before));
         assertTrue(currentTime.equals(after) || currentTime.isBefore(after));
     }
 }
