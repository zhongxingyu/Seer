 /*
  * The MIT License
  * 
  * Copyright (c) 2012, Jesse Farinacci
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 
 package org.jenkins.ci.plugins.keyboard_shortcuts;
 
 import hudson.model.Node;
 
 import java.util.List;
 import java.util.TreeMap;
 
 import jenkins.model.Jenkins;
 import net.sf.json.JSONArray;
 import net.sf.json.JSONObject;
 
import org.apache.commons.lang.StringUtils;
 
 /**
  * Common utilities for {@link hudson.model.Node}s.
  * 
  * @author <a href="mailto:jieryn@gmail.com">Jesse Farinacci</a>
  */
 public final class NodeUtils {
     public static List<Node> getAllNodes() {
         return Jenkins.getInstance().getNodes();
     }
 
     public static JSONArray getAllNodesAsJsonArray() {
         final JSONArray nodes = new JSONArray();
 
         for (final Node node : getAllNodes()) {
             if (node == null) {
                 continue;
             }
 
             if (StringUtils.isEmpty(node.getNodeName())) {
                 continue;
             }
 
             if (StringUtils.isEmpty(node.getDisplayName())) {
                 continue;
             }
 
             final TreeMap<String, String> map = new TreeMap<String, String>();
             map.put("name", node.getNodeName());
             map.put("displayName", node.getDisplayName());
             nodes.add(JSONObject.fromObject(map));
         }
 
         // why isn't master a node? i don't get it
         final TreeMap<String, String> map = new TreeMap<String, String>();
         map.put("name", "(master)");
         map.put("displayName", "master");
         nodes.add(JSONObject.fromObject(map));
 
         return nodes;
     }
 
     /**
      * Static-only access.
      */
     private NodeUtils() {
         // static-only access
     }
 }
