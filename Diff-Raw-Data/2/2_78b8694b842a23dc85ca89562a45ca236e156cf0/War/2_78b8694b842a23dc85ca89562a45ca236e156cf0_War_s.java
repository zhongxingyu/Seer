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
 
 import net.oneandone.lavender.config.Alias;
 import net.oneandone.lavender.config.Cluster;
 import net.oneandone.lavender.config.Docroot;
 import net.oneandone.lavender.config.Net;
 import net.oneandone.lavender.config.Pool;
 import net.oneandone.lavender.config.Settings;
 import net.oneandone.lavender.config.Target;
 import net.oneandone.lavender.index.Distributor;
 import net.oneandone.sushi.cli.ArgumentException;
 import net.oneandone.sushi.cli.Console;
 import net.oneandone.sushi.cli.Remaining;
 import net.oneandone.sushi.cli.Value;
 import net.oneandone.sushi.fs.file.FileNode;
 import net.oneandone.sushi.xml.XmlException;
 import org.xml.sax.SAXException;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 public class War extends Base {
     @Value(name = "inputWar", position = 1)
     private FileNode inputWar;
 
     @Value(name = "outputWar", position = 2)
     private FileNode outputWar;
 
     @Value(name = "idxName", position = 3)
     private String indexName;
 
     private final Map<String, Target> targets = new HashMap<>();
     private String nodes;
 
     @Remaining
     public void target(String keyvalue) {
         int idx;
         String type;
         String clusterName;
         String aliasName;
         Cluster cluster;
         Docroot docroot;
         Alias alias;
 
         idx = keyvalue.indexOf('=');
         if (idx == -1) {
             throw new ArgumentException("<type>=<cluster> expected, got " + keyvalue);
         }
         type = keyvalue.substring(0, idx);
         clusterName = keyvalue.substring(idx + 1);
         idx = clusterName.indexOf('/');
         if (idx == -1) {
             aliasName = null;
         } else {
             aliasName = clusterName.substring(idx + 1);
            clusterName = clusterName.substring(idx);
         }
         cluster = net.get(clusterName);
         docroot = cluster.docroot(type);
         alias = aliasName == null ? docroot.aliases().get(0) : docroot.alias(aliasName);
         if (Docroot.WEB.equals(type)) {
             nodes = alias.nodesFile();
         }
         targets.put(type, new Target(cluster, docroot, alias));
     }
 
     public War(Console console, Settings settings, Net net) {
         super(console, settings, net);
     }
 
     @Override
     public void invoke() throws IOException, SAXException, XmlException {
         FileNode tmp;
         FileNode outputNodesFile;
         WarEngine engine;
         Map<String, Distributor> distributors;
 
         if (targets.isEmpty()) {
             throw new ArgumentException("missing targets");
         }
         if (nodes == null) {
             throw new ArgumentException("missing web target");
         }
         inputWar.checkFile();
         outputWar.checkNotExists();
         tmp = inputWar.getWorld().getTemp();
         outputNodesFile = tmp.createTempFile();
         try (Pool pool = pool()) {
             distributors = distributors(pool);
             engine = new WarEngine(distributors, indexName, settings.svnUsername, settings.svnPassword,
                     inputWar, outputWar, outputNodesFile, nodes);
             engine.run();
         }
         outputNodesFile.deleteFile();
     }
 
     private Map<String, Distributor> distributors(Pool pool) throws IOException {
         Map<String, Distributor> result;
 
         result = new HashMap<>();
         for (Map.Entry<String, Target> entry : targets.entrySet()) {
             result.put(entry.getKey(), entry.getValue().open(pool, indexName));
         }
         return result;
     }
 }
