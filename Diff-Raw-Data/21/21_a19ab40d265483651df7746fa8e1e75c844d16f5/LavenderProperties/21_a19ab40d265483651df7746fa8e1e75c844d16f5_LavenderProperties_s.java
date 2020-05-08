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
 package net.oneandone.lavender.modules;
 
 import net.oneandone.lavender.config.Docroot;
 import net.oneandone.sushi.fs.FileNotFoundException;
 import net.oneandone.sushi.fs.Node;
 import net.oneandone.sushi.fs.World;
 import net.oneandone.sushi.fs.filter.Filter;
 import net.oneandone.sushi.util.Separator;
 import net.oneandone.sushi.util.Strings;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.IOException;
 import java.net.NetworkInterface;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.Properties;
 
 public class LavenderProperties {
     private static final Logger LOG = LoggerFactory.getLogger(LavenderProperties.class);
 
     public static final String MODULE_PROPERTIES = "PUSTEFIX-INF/lavender.properties";
     private static final String APP_PROPERTIES = "WEB-INF/lavender.properties";
     public static final List<String> DEFAULT_INCLUDES = new ArrayList<>(Arrays.asList(
             "**/*.gif", "**/*.png", "**/*.jpg", "**/*.jpeg", "**/*.ico", "**/*.swf", "**/*.css", "**/*.js"));
 
     public static LavenderProperties loadModuleOpt(Node root) throws IOException {
         Node src;
         Properties properties;
 
         if (root == null) {
             return null;
         }
         src = root.join(LavenderProperties.MODULE_PROPERTIES);
         if (!src.exists()) {
             return null;
         }
         properties = src.readProperties();
         return LavenderProperties.parse(properties, pominfoOpt(root));
     }
 
     public static LavenderProperties loadApp(Node webapp) throws IOException {
         Node src;
         Properties pominfo;
 
         src = webapp.join(LavenderProperties.APP_PROPERTIES);
         if (!src.exists()) {
             // TODO: dump this compatibility check as soon as I have ITs with new wars
             src = webapp.join("WEB-INF/lavendel.properties");
             if (!src.exists()) {
                 throw new IOException("lavender.properties not found");
             }
         }
         pominfo = pominfoOpt(webapp.join("WEB-INF/classes"));
         if (pominfo == null) {
             // TODO: try target/classes - hack because pws deletes WEB-INF/classes to get virtual classpath working
             pominfo = pominfoOpt(webapp.join("../classes"));
         }
         if (pominfo == null) {
             throw new IOException("pominfo.properties for application not found");
         }
         return parse(src.readProperties(), pominfo);
     }
 
     private static Properties pominfoOpt(Node root) throws IOException {
         try {
             return root.join("META-INF/pominfo.properties").readProperties();
         } catch (FileNotFoundException e) {
             return null;
         }
     }
 
     // public only for testing
     public static LavenderProperties parse(Properties properties, Properties pominfo) throws IOException {
         return parse(properties, pominfo, DEFAULT_INCLUDES);
     }
 
     private static LavenderProperties parse(Properties properties, Properties pominfo, List<String> defaultIncludes) throws IOException {
         LavenderProperties result;
         String relative;
         String source;
 
         relative = eat(properties, "pustefix.relative");
         // TODO: enforce pominfo != null when enough modules have switched
         if (pominfo != null && thisMachine(pominfo.getProperty("ethernet"))) {
            source = pominfo.getProperty("basedir");
            source = Strings.removeRightOpt(source, "/");
            source = source + "/" + relative;
         } else {
             source = null;
         }
         result = new LavenderProperties(eatFilter(properties, "pustefix", defaultIncludes), source);
         for (String prefix : svnPrefixes(properties)) {
             result.configs.add(
                     new SvnProperties(
                             prefix.substring(SvnProperties.SVN_PREFIX.length()),
                             eatFilter(properties, prefix, defaultIncludes),
                             Strings.removeLeftOpt((String) properties.remove(prefix), "scm:svn:"),
                             eatType(properties, prefix),
                             eatBoolean(properties, prefix + ".lavendelize", true),
                             eatOpt(properties, prefix + ".resourcePathPrefix", ""),
                             eatTargetPathPrefix(properties, prefix),
                             eatSvnSource(properties, prefix, source)));
         }
         if (properties.size() > 0) {
             throw new IllegalArgumentException("unknown properties: " + properties);
         }
         return result;
     }
 
     private static String eatSvnSource(Properties properties, String prefix, String source) {
         String relative;
 
         relative = eatOpt(properties, prefix + ".relative", null);
        return source == null && relative != null ? null : source + relative;
     }
 
     private static String eatType(Properties properties, String prefix) {
         String type;
         String value;
 
         type = eatOpt(properties, prefix + ".type", Docroot.WEB);
         if (type == null) {
             value = eatOpt(properties, prefix + ".storage", null);
             if (value.startsWith("flash-")) {
                 // TODO: dump
                 LOG.warn("CAUTION: out-dated storage configured - use type instead");
                 type = Docroot.FLASH;
             } else {
                 throw new IllegalArgumentException("storage no longer supported: " + value);
             }
         }
         return type;
     }
 
     private static String eatTargetPathPrefix(Properties properties, String prefix) {
         String targetPathPrefix;
 
         targetPathPrefix = eatOpt(properties, prefix + ".targetPathPrefix", "");
         if (targetPathPrefix == null) {
             LOG.warn("CAUTION: out-dated pathPrefix - use targetPathPrefix instead");
             targetPathPrefix = eatOpt(properties, prefix + ".pathPrefix", null);
         }
         return targetPathPrefix;
     }
 
     private static Filter eatFilter(Properties properties, String prefix, List<String> defaultIncludes) {
         Filter result;
 
         result = new Filter();
         result.include(eatListOpt(properties, prefix + ".includes", defaultIncludes));
         result.exclude(eatListOpt(properties, prefix + ".excludes", Collections.EMPTY_LIST));
         return result;
     }
 
     private static List<String> eatListOpt(Properties p, String key, List<String> dflt) {
         String result;
 
         result = eatOpt(p, key, null);
         return result == null ? dflt : Separator.SPACE.split(result);
     }
 
     private static boolean eatBoolean(Properties p, String key, boolean dflt) {
         String result;
 
         result = eatOpt(p, key, null);
         if (result == null) {
             return dflt;
         }
         switch (result) {
             case "true": return true;
             case "false": return false;
             default: throw new IllegalArgumentException("true or false expected, got " + result);
         }
     }
 
     private static String eat(Properties p, String key) {
         String result;
 
         result = eatOpt(p, key, null);
         if (result == null) {
             throw new IllegalArgumentException("key not found: " + key);
         }
         return result;
     }
 
     private static String eatOpt(Properties p, String key, String dflt) {
         String result;
 
         result = (String) p.remove(key);
         return result == null ? dflt : result;
     }
 
     private static List<String> svnPrefixes(Properties properties) {
         List<String> result;
 
         result = new ArrayList<>();
         for (String name : properties.stringPropertyNames()) {
             if (name.startsWith(SvnProperties.SVN_PREFIX)) {
                 if (Strings.count(name, ".") == 1) {
                     result.add(name);
                 }
             }
         }
         return result;
     }
 
     //--
 
     public final Filter filter;
     public final String source;
     public final Collection<SvnProperties> configs;
 
     public LavenderProperties(Filter filter, String source) {
         if (filter == null) {
             throw new IllegalArgumentException();
         }
         this.filter = filter;
         this.source = source;
         this.configs = new ArrayList<>();
     }
 
     public void addModules(boolean prod, World world, String svnUsername, String svnPassword, List<Module> result) throws IOException {
         for (SvnProperties config : configs) {
             result.add(config.create(prod, world, svnUsername, svnPassword));
         }
     }
 
     public Node live(Node root) throws IOException {
         return source != null ? root.getWorld().file(source) : root;
     }
 
 
     //--
 
     private static boolean thisMachine(String ethernet) throws IOException {
         List<String> thisEthernet;
         List<String> otherEthernet;
 
         thisEthernet = ethernet();
         otherEthernet = Separator.COMMA.split(ethernet);
         for (String address : thisEthernet) {
             if (otherEthernet.contains(address)) {
                 return true;
             }
         }
         return false;
     }
 
     public static List<String> ethernet() throws IOException {
         Enumeration ifcs;
         List<String> result;
         NetworkInterface ifc;
         byte[] address;
 
         ifcs = NetworkInterface.getNetworkInterfaces();
         result = new ArrayList<>();
         while (ifcs.hasMoreElements()) {
             ifc = (NetworkInterface) ifcs.nextElement();
             address = ifc.getHardwareAddress();
             if (address != null) {
                 result.add(toHex(address));
             } else {
                 // ignore -- not available (i.e. loopback device) or not accessible
             }
         }
         return result;
     }
 
     public static String toHex(byte ... bytes) {
         StringBuilder builder;
 
         builder = new StringBuilder();
         for (byte b : bytes) {
             if (builder.length() > 0) {
                 builder.append(':');
             }
             builder.append(toHexChar(b >> 4 & 0xf));
             builder.append(toHexChar(b & 0xf));
         }
         return builder.toString();
     }
 
     private static char toHexChar(int b) {
         if (b >= 0 && b <= 9) {
             return (char) ('0' + b);
         } else if (b >= 10 && b <= 15) {
             return (char) ('a' + b - 10);
         } else {
             throw new IllegalArgumentException("" + b);
         }
     }
 }
