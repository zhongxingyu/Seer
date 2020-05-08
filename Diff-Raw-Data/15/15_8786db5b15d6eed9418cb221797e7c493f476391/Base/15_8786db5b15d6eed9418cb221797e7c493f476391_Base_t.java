 /**
  * Copyright 1&1 Internet AG, https://github.com/1and1/
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
 package net.oneandone.lavender.cli;
 
 
 import net.oneandone.lavender.config.Net;
 import net.oneandone.lavender.config.Pool;
 import net.oneandone.lavender.config.Settings;
 import net.oneandone.sushi.cli.Command;
 import net.oneandone.sushi.cli.Console;
 import net.oneandone.sushi.cli.Option;
 
 public abstract class Base implements Command {
     protected final Console console;
     protected final Settings settings;
     protected final Net net;
 
     @Option("user")
     protected String user = "unknown@all";
 
     @Option("await")
    protected int await = 60 * 60;  // 1 hour
 
     @Option("no-lock")
     protected boolean noLock = false;
 
     protected Base(Console console, Settings settings, Net net) {
         this.console = console;
         this.settings = settings;
         this.net = net;
     }
 
     @Override
     public abstract void invoke() throws Exception;
 
     protected Pool pool() {
         return new Pool(console.world, noLock ? null : user, await);
     }
 }
