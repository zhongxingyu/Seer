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
 
 import net.oneandone.lavender.config.Cluster;
 import net.oneandone.lavender.config.Docroot;
 import net.oneandone.lavender.config.Filter;
 import net.oneandone.lavender.config.Net;
 import net.oneandone.lavender.config.Settings;
 import net.oneandone.lavender.config.Target;
 import net.oneandone.lavender.index.Distributor;
 import net.oneandone.lavender.index.Index;
 import net.oneandone.lavender.modules.SvnModule;
 import net.oneandone.lavender.modules.SvnModuleConfig;
 import net.oneandone.sushi.cli.ArgumentException;
 import net.oneandone.sushi.cli.Console;
 import net.oneandone.sushi.cli.Value;
 
 import java.io.IOException;
 
 public class Svn extends Base {
     @Value(name = "directory", position = 1)
     private String directory;
 
     @Value(name = "cluster", position = 2)
     private String clusterName;
 
     private final String svn;
 
     public Svn(Console console, Settings settings, String svn, Net net) {
         super(console, settings, net);
         this.svn = svn;
     }
 
     @Override
     public void invoke() throws IOException {
         Cluster cluster;
         Docroot docroot;
         Target target;
         Filter filter;
         SvnModuleConfig moduleConfig;
         SvnModule module;
         Distributor distributor;
         long changed;
         Index index;
 
         if (directory.isEmpty() || directory.contains("/")) {
             throw new ArgumentException("invalid directory: " + directory);
         }
         cluster = net.get(clusterName);
         docroot = cluster.docroot(Docroot.SVN);
         target = new Target(cluster, docroot, docroot.aliases().get(0));
         filter = new Filter();
         filter.setIncludes("*");
         filter.setExcludes();
         moduleConfig = new SvnModuleConfig("svn", filter);
        moduleConfig.pathPrefix = directory + "/";
         moduleConfig.svnurl = svn + "/data/" + directory;
         moduleConfig.lavendelize = false;
         module = moduleConfig.create(console.world, settings.svnUsername, settings.svnPassword);
         distributor = target.open(console.world, directory + ".idx");
         changed = module.publish(distributor);
         index = distributor.close();
         module.saveCaches();
         console.info.println("done: " + changed + "/" + index.size() + " files changed");
     }
 }
