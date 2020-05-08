 /*
  * Copyright 2013 StuxCrystal
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software distributed
  * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
  * CONDITIONS OF ANY KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations under the License.
  */
 
 package net.stuxcrystal.configuration.generators.yaml;
 
 import net.stuxcrystal.configuration.ConfigurationLoader;
 import net.stuxcrystal.configuration.NodeTreeGenerator;
 import net.stuxcrystal.configuration.node.DataNode;
 import net.stuxcrystal.configuration.node.MapNode;
 import net.stuxcrystal.configuration.node.Node;
 import org.yaml.snakeyaml.DumperOptions;
 import org.yaml.snakeyaml.Yaml;
 import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;
 import org.yaml.snakeyaml.representer.Representer;
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 public class YamlGenerator implements NodeTreeGenerator {
 
     private Yaml parser;
 
     public YamlGenerator() {
         DumperOptions options = new DumperOptions();
         options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
         options.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);
         parser = new Yaml(new Constructor(), new Representer(), options, new StringOnlyResolver());
     }
 
     public String getName() {
         return "yaml";
     }
 
     @Override
     public boolean isValidFile(File file) throws IOException {
         return file.getName().endsWith(".yml") || file.getName().endsWith(".yaml");
     }
 
     @Override
     public Node<?> parseFile(File file, ConfigurationLoader cparser) throws IOException {
        try {
            return parseNode(parser.load(new FileInputStream(file)));
        } catch (YAMLException e) {
            throw new IOException(e);
        }
     }
 
     @Override
     public Node<?> parse(InputStream stream, ConfigurationLoader cparser) throws IOException {
        try {
            return parseNode(parser.load(stream));
        } catch (YAMLException e) {
            throw new IOException(e);
        }
     }
 
     @SuppressWarnings("unchecked")
     private Node<?> parseNode(Object value) {
         Node<?> n;
         if (value instanceof Map) {
             n = parseMap((Map<String, Object>) value);
         } else if (value instanceof List) {
             n = parseList((List<Object>) value);
         } else {
             n = new DataNode((String) value);
         }
         return n;
     }
 
     private Node<?> parseMap(Map<String, Object> data) {
         MapNode node = new MapNode();
         ArrayList<Node<?>> nodes = new ArrayList<>();
         for (Entry<String, Object> entry : data.entrySet()) {
             Node<?> n = parseNode(entry.getValue());
             n.setName(entry.getKey());
             n.setParent(node);
             nodes.add(n);
         }
 
         node.setData(nodes.toArray(new Node<?>[nodes.size()]));
         return node;
     }
 
     private Node<?> parseList(List<Object> data) {
         MapNode node = new MapNode();
         ArrayList<Node<?>> nodes = new ArrayList<>();
         for (Object o : data) {
             Node<?> n = parseNode(o);
             n.setParent(node);
             nodes.add(n);
         }
         node.setData(nodes.toArray(new Node<?>[nodes.size()]));
         return node;
     }
 
     @Override
     public void dumpFile(File file, Node<?> node, ConfigurationLoader parser) throws IOException {
         dump(new FileWriter(file), node, parser);
     }
 
     @Override
     public void dump(OutputStream stream, Node<?> node, ConfigurationLoader parser) throws IOException {
         dump(new OutputStreamWriter(stream), node, parser);
 
     }
 
     private void dump(Writer writer, Node<?> node, ConfigurationLoader parser) throws IOException {
         CommentAwareDumper dumper = new CommentAwareDumper(parser);
         dumper.dump(writer, node);
         writer.close();
     }
 }
