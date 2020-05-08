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
 package net.oneandone.lavender.filter.processor;
 
 import net.oneandone.lavender.index.Index;
 import net.oneandone.lavender.index.Label;
 import net.oneandone.lavender.filter.processor.ConsistentHash;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Uses a consistent hash function to calculate the host name for a particular resource.
  */
 public class UrlCalculator {
     /** The nodes used for HTTP */
    protected final Map<String, URI> httpNodes = new HashMap<String, URI>();
 
     /** The nodes used for HTTPS */
    protected final Map<String, URI> httpsNodes = new HashMap<String, URI>();
 
     /** The consistent hash function. */
     protected final ConsistentHash consistentHash;
 
     public UrlCalculator(URL nodesUrl) throws IOException {
         InputStream in;
 
         in = nodesUrl.openStream();
         if (in == null) {
             throw new IllegalStateException("nodes file not found: " + nodesUrl);
         }
         readNodes(in);
         in.close();
         this.consistentHash = new ConsistentHash(200, httpNodes.keySet().toArray(new String[0]));
     }
 
     private void readNodes(InputStream raw) throws IOException {
         BufferedReader in;
         String line;
 
         in = new BufferedReader(new InputStreamReader(raw, Index.ENCODING));
         while (true) {
             line = in.readLine();
             if (line == null) {
                 return;
             }
             line = line.trim();
             if (!line.isEmpty()) {
                 if (!line.endsWith("/")) {
                     line = line + "/";
                 }
                 URI uri = URI.create(line);
                 if ("http".equals(uri.getScheme())) {
                     httpNodes.put(uri.getHost(), uri);
                 } else if ("https".equals(uri.getScheme())) {
                     httpsNodes.put(uri.getHost(), uri);
                 } else {
                     throw new IllegalArgumentException("Node " + line + " has unsupported schema, only http and https are supported.");
                 }
             }
         }
     }
 
     public URI calculateURL(Label label, URI baseURI) {
         if (label.getLavendelizedPath() == null) {
             throw new IllegalStateException();
         }
         byte[] md5 = label.md5();
         String node = consistentHash.getNodeForHash(md5);
         String lavendelizedPath = label.getLavendelizedPath();
         URI nodeURI = baseURI.getScheme().equals("https") ? httpsNodes.get(node) : httpNodes.get(node);
         String path = nodeURI.getPath() + lavendelizedPath;
         int port = nodeURI.getPort();
         try {
             return new URI(nodeURI.getScheme(), null, node, port, path, null, null);
         } catch (URISyntaxException e) {
             throw new IllegalArgumentException(e);
         }
     }
 }
