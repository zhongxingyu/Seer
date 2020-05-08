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
 import net.oneandone.sushi.fs.Node;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.text.MessageFormat;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * RewriteEngine that is able to load and push resources during rewrite.
  */
 public class RewriteEngine {
     private static final Logger LOG = LoggerFactory.getLogger(RewriteEngine.class);
 
     //--
 
     public static RewriteEngine load(Index index, Node nodesFiles) throws IOException {
         try (InputStream src = nodesFiles.createInputStream()) {
             return load(index, src);
         }
     }
 
     public static RewriteEngine load(Index index, URL url) throws IOException {
         try (InputStream src = url.openStream()) {
             return load(index, src);
         }
     }
 
     public static RewriteEngine load(Index index, InputStream raw) throws IOException {
         RewriteEngine result;
         BufferedReader in;
         String line;
 
         result = new RewriteEngine(index);
         in = new BufferedReader(new InputStreamReader(raw, Index.ENCODING));
         while (true) {
             line = in.readLine();
             if (line == null) {
                 break;
             }
             line = line.trim();
             if (!line.isEmpty()) {
                 if (!line.endsWith("/")) {
                     line = line + "/";
                 }
                 result.add(URI.create(line));
             }
         }
         in.close();
         return result;
     }
 
     //--
 
     protected final Index index;
 
     /** The nodes used for HTTP */
     protected final Map<String, URI> httpNodes;
 
     /** The nodes used for HTTPS */
     protected final Map<String, URI> httpsNodes;
 
     /** The consistent hash function. */
     protected final ConsistentHash consistentHash;
 
     public RewriteEngine(Index index) {
         this.index = index;
         this.consistentHash = new ConsistentHash(200);
         this.httpNodes = new HashMap<>();
         this.httpsNodes = new HashMap<>();
     }
 
     public void add(URI uri) {
         if (!uri.getPath().endsWith("/")) {
             throw new IllegalArgumentException(uri.toString());
         }
         switch (uri.getScheme()) {
             case "http":
                 httpNodes.put(uri.getHost(), uri);
                 consistentHash.addNode(uri.getHost());
                 break;
             case "https":
                 httpsNodes.put(uri.getHost(), uri);
                 break;
             default:
                 throw new IllegalArgumentException(uri + " has unsupported scheme, only http and https are supported.");
         }
     }
 
     public String rewrite(String uri, URI baseURI, String contextPath) {
         URI reference;
         String result;
         int len;
 
         len = uri.length();
         if (len > 2) {
             if ((uri.startsWith("\"") && uri.endsWith("\"")) || (uri.startsWith("'") && uri.endsWith("'"))) {
                 uri = uri.substring(1, len - 1);
             }
         }
         try {
             reference = new URI(uri);
         } catch (URISyntaxException e) {
            LOG.warn("cannot rewrite invalid URI '" + uri + "'");
             return uri;
         }
         result = rewrite(reference, baseURI, contextPath).toASCIIString();
         if (LOG.isDebugEnabled()) {
             LOG.debug("rewrite ok: '" + uri + "' -> '" + result + "'");
         }
         return result;
     }
 
 
     public URI rewrite(URI reference, URI baseURI, String contextPath) {
         Label label;
 
         label = lookup(reference, baseURI, contextPath);
         if (label == null) {
             if (LOG.isDebugEnabled()) {
                 String message = "No resource found in index for reference={0}, baseURI={1}, contextPath={2}";
                 String formatted = MessageFormat.format(message, reference, baseURI, contextPath);
                 LOG.debug(formatted);
             }
             return reference;
         }
 
         return calculateURL(label, baseURI);
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
 
     Label lookup(URI reference, URI baseURI, String contextPath) {
         String resolved;
         Label label;
 
         resolved = resolve(reference, baseURI, contextPath);
         label = resolved == null ? null : index.lookup(resolved);
         if (LOG.isDebugEnabled()) {
             LOG.debug("Lookup index for reference " + reference + "(resolved=" + resolved + "): " + label);
         }
         return label;
     }
 
     /** @return path, without contextPath */
     String resolve(URI reference, URI baseURI, String contextPath) {
         URI uri = baseURI.resolve(reference);
         String resolved = uri.getPath();
         if (resolved == null) {
             return null;
         }
         if (resolved.startsWith(contextPath)) {
             resolved = resolved.substring(contextPath.length());
         }
         return resolved;
     }
 }
