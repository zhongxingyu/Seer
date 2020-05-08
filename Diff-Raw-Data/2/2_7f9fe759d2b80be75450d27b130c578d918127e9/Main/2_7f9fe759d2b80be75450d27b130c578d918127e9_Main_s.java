 package de.h7r.sine;
 
 import com.google.common.base.Joiner;
 import com.google.common.base.Preconditions;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 import com.google.common.io.Files;
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import org.eclipse.jetty.server.Connector;
 import org.eclipse.jetty.server.Request;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.server.handler.AbstractHandler;
 import org.eclipse.jetty.server.nio.SelectChannelConnector;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.File;
 import java.io.IOException;
 import java.nio.charset.Charset;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 public class Main {
 
     static {
         // if not given in the command line, set log file name to the default value.
         if (System.getProperty ("sine_log_file") == null) {
             System.setProperty ("sine_log_file", "sine.log");
         }
     }
 
     private static final Logger LOG = LoggerFactory.getLogger (Main.class);
 
     public static void main (String[] args)
             throws Exception {
 
         // read configuration
         LOG.info ("SINE configuration server starting ...");
 
         SINEConfiguration conf = SINEConfiguration.fromSystemProperties ();
 
         buildData (conf.getConfigPath ());
 
         // startup jetty with rest interface
         startServerAndListen (conf);
     }
 
     private static SINENode buildData (File configPath)
             throws IOException {
 
        LOG.info (String.format ("Reading properties data form store %s", configPath));
 
         File env = new File (configPath, SINEConstants.ENVS);
 
         return walk ("", SINEConstants.ENVS, env.listFiles ());
 
     }
 
     private static SINENode walk (String prefix,
                                   String currentName,
                                   File[] listFiles)
             throws IOException {
 
         Gson gson = new Gson ();
 
         SINENode n = new SINENode (prefix);
         n.setLocalName (currentName);
 
         for (int i = 0; i < listFiles.length; i++) {
             String prefix2 = prefix + "/" + listFiles[i].getName ();
 
             LOG.debug ("walking {} > {}", new Object [] {prefix, prefix2});
 
             if (listFiles[i].isDirectory ()) {
                 SINENode n1 = walk (prefix2, listFiles[i].getName (), listFiles[i].listFiles ());
                 n.addChild (n1);
                 LOG.debug ("pushed node on " + prefix2 + ", as " + n1);
 
             } else {
                 SINENode c = new SINENode (prefix2, listFiles[i].getName (), Files.readLines (listFiles[i], Charset.defaultCharset ()));
                 n.addChild (c);
                 c.close ();
             }
         }
 
         LOG.debug (String.format ("%s: %s", prefix, n));
         n.close ();
         return n;
     }
 
     private static void startServerAndListen (SINEConfiguration conf)
             throws Exception {
 
         LOG.info (String.format ("Starting embedded web server with configuration: %s", conf));
 
         Server server = new Server ();
         Connector connector = new SelectChannelConnector ();
         connector.setHost (conf.getBindAddress ());
         connector.setPort (conf.getPort ());
         server.addConnector (connector);
 
         server.setHandler (new AbstractHandler () {
 
             @Override
             public void handle (String target,
                                 Request baseRequest,
                                 HttpServletRequest req,
                                 HttpServletResponse resp)
                     throws IOException, ServletException {
 
                 resp.setContentType ("application/json");
 
                 try {
                     String p = req.getRequestURI ().substring (1);
 
                     LOG.debug (String.format ("Handling request for %s", p));
 
                     if (p.startsWith (SINEConstants.META)) {
                         throw new UnsupportedOperationException ();
                     } else if (p.startsWith (SINEConstants.ENVS)) {
 
                         String q = p.trim ().substring (SINEConstants.ENVS.length ());
 
                         if (q.endsWith ("/")) {
                             q = q.substring (0, Math.max (q.length () - 2, 0));                     
                         }
 
                         LOG.info ("query for " + q);
 
                         String s = coalesce (NodeRegistry.get (q), "null");
 
                         LOG.info (String.format ("%s -> %s", p, s));
 
                         resp.setStatus (HttpServletResponse.SC_OK);
                         resp.getWriter ().write (s);
 
                     } else {
                         throw new IllegalArgumentException (String.format ("Don't know how to handle request to [%s]", p));
                     }
 
                 } catch (Exception e) {
                     LOG.error ("Unhandled error serving request.", e);
                     resp.setStatus (HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                 } finally {
                     baseRequest.setHandled (true);
                 }
 
             }
 
             private String coalesce (String v,
                                      String def) {
 
                 return v == null ? def : v;
             }
 
         });
 
         // ready to rumble
         server.start ();
         server.join ();
     }
 }
 
 class SINEConfiguration {
 
     private String bindAddress;
     private int port;
     private File configPath;
 
     public String getBindAddress () {
 
         return bindAddress;
     }
 
     public static SINEConfiguration fromSystemProperties ()
             throws IOException {
 
         File storePath = new File (System.getProperty ("store", ".")).getCanonicalFile ();
 
         File env = new File (storePath, SINEConstants.ENVS);
         Preconditions.checkArgument (env.exists (), "Check your configuration/startup parameters: envs directory does not exist.");
 
         SINEConfiguration sc = new SINEConfiguration ();
 
         sc.setBindAddress (System.getProperty ("bindAddress", "0.0.0.0"));
         sc.setPort (Integer.parseInt (System.getProperty ("port", "7522")));
 
         Preconditions.checkArgument (storePath.exists (), String.format ("storePath [%s] does not exist", storePath));
         Preconditions.checkArgument (storePath.isDirectory (), String.format ("storePath [%s] does not point to a directory", storePath));
 
         sc.setConfigPath (storePath);
 
         return sc;
     }
 
     public void setBindAddress (String bindAddress) {
 
         this.bindAddress = bindAddress;
     }
 
     public int getPort () {
 
         return port;
     }
 
     public void setPort (int port) {
 
         this.port = port;
     }
 
     public File getConfigPath () {
 
         return configPath;
     }
 
     public void setConfigPath (File configPath) {
 
         this.configPath = configPath;
     }
 
     @Override
     public String toString () {
 
         return "SINEConfiguration [bindAddress=" + bindAddress + ", port=" + port + ", configPath=" + configPath + "]";
     }
 }
 
 class NodeRegistry {
 
     private static final Logger LOG = LoggerFactory.getLogger (NodeRegistry.class);
 
     private static Map<String, SINENode> nodes = Maps.newHashMap ();
 
     public static void register (SINENode sineNode) {
 
         nodes.put (sineNode.getPrefix (), sineNode);
         LOG.debug ("Registering creation of node " + sineNode.getPrefix ());
 
     }
 
     public static String get (String k) {
 
         if (!nodes.containsKey (k)) {
             return null;
         }
         return nodes.get (k).getRepr ();
     }
 
     public static ImmutableSet<String> allKeys () {
 
         return ImmutableSet.copyOf (nodes.keySet ());
 
     }
 
 }
 
 class SINENode {
 
     private static final Logger LOG = LoggerFactory.getLogger (SINENode.class);
 
     private Set<SINENode> children = Sets.newHashSet ();
     private String prefix;
     private String localName;
     private String content;
     private String repr;
     private boolean closed = false;
 
     public SINENode (String prefix2) {
 
         this.prefix = prefix2;
         NodeRegistry.register (this);
     }
 
     public void close () {
 
         Gson gson = new GsonBuilder ().create ();
         closed = true;
 
         LOG.info ("finished " + prefix);
         if (children.isEmpty ()) {
             repr = gson.toJson (content);
         } else {
             StringBuilder sb = new StringBuilder ();
             sb.append ("{");
             boolean more = false;
             for (SINENode n : children) {
                 LOG.info ("local name on building: " + n.getLocalName ());
 
                 if (more) {
                     sb.append (",");
                 } else {
                     more = true;
                 }
 
                 sb.append (gson.toJson (n.getLocalName ()));
                 sb.append (":");
                 sb.append (n.getRepr ());
 
             }
             sb.append ("}");
 
             repr = sb.toString ();
         }
     }
 
     public String getRepr () {
         // LOG.info ("repr " + localName);
         Preconditions.checkState (closed, "node not closed yet");
 
         return repr;
     }
 
     public void setContent (List<String> readLines) {
 
         this.content = Joiner.on ("\n").join (readLines);
 
     }
 
     public SINENode (String prefix2,
                      String localName,
                      List<String> readLines) {
 
         this (prefix2);
         this.localName = localName;
         setContent (readLines);
 
         LOG.info ("created node \t" + prefix2 + ", " + localName);
     }
 
     public void addChild (SINENode v) {
 
         children.add (v);
 
         LOG.info (String.format ("adding child to %s: %s", this.localName, v));
     }
 
     public String getPrefix () {
 
         return prefix;
     }
 
     @Override
     public String toString () {
 
         Map<String, String> r = Maps.newHashMap ();
         r.put (localName, content);
         if (!children.isEmpty ()) {
             for (SINENode c : children) {
                 r.put (c.getLocalName (), c.toString ());
             }
         }
         return r.toString ();
     }
 
     public String getLocalName () {
 
         return localName;
     }
 
     public void setLocalName (String localName) {
 
         this.localName = localName;
     }
 
 }
