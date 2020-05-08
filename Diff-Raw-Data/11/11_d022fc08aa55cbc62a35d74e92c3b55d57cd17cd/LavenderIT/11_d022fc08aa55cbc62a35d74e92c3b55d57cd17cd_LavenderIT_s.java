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
 package net.oneandone.lavender.publisher;
 
 import net.oneandone.lavender.publisher.cli.Main;
 import net.oneandone.lavender.publisher.config.Cluster;
 import net.oneandone.lavender.publisher.config.Net;
 import net.oneandone.sushi.fs.Node;
 import net.oneandone.sushi.fs.World;
 import net.oneandone.sushi.fs.file.FileNode;
 import net.oneandone.sushi.fs.filter.Filter;
 import net.oneandone.sushi.fs.filter.Predicate;
 import org.junit.Test;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import static org.junit.Assert.assertEquals;
 
 public class LavenderIT {
     private static final World WORLD = new World();
 
     @Test
     public void warOnly() throws Exception {
         check("war", "67f812dc689928f9d8ed4dc271143c62", "com/oneandone/sales/dslcancel-de/1.4.7/dslcancel-de-1.4.7.war");
     }
 
     @Test
     public void warWithDownloads() throws Exception {
        check("downloads", "40b307593cc2970a7f4e766ab648ebb9", "de/ui/webdev/pfixui/1.1.46/pfixui-1.1.46.war");
     }
 
     @Test
     public void warWithDownloads2() throws Exception {
        check("downloads2", "2595c2fbf2f96f619398b50a4e9d6623", "de/ui/webdev/pfixui/1.1.45/pfixui-1.1.45.war");
     }
 
     @Test
     public void warWithFlashEu() throws Exception {
        check("flash-eu", "25aa23795e61a430880944ceebef7fc7",
                 "com/oneandone/sales/diy/de/diy-business-de/2.1.147/diy-business-de-2.1.147.war");
     }
 
     @Test
     public void warWithFlashUs() throws Exception {
        check("flash-us", "e59d0b4ad52596751e47f833c8f1b2f7",
                 "com/oneandone/sales/diy/us/diy-business-us/2.2.51/diy-business-us-2.2.51.war");
     }
 
     private static final String INDEX_NAME = "indexfilefortests.idx";
 
     public void check(String name, String expected, String warUrl) throws Exception {
         Node src;
         FileNode target;
         FileNode testhosts;
         FileNode war;
         FileNode warModified;
         long started;
         Net net;
 
         System.out.println(name + " started: ");
         src = WORLD.node("http://mavenrepo.united.domain:8081/nexus/content/repositories/1und1-stable/" + warUrl);
         war = WORLD.getTemp().createTempFile();
         warModified = WORLD.getTemp().createTempFile();
         warModified.deleteFile();
         src.copyFile(war);
         target = WORLD.guessProjectHome(getClass()).join("target");
         testhosts = target.join("it", name);
         testhosts.getParent().mkdirsOpt();
         started = System.currentTimeMillis();
         net = net(testhosts);
         assertEquals(0, Main.doMain(false, net, target.join("itlogs").getAbsolute(),
                 "-e", "war", "test", war.getAbsolute(), warModified.getAbsolute(), INDEX_NAME));
         System.out.println(name + " done: " + (System.currentTimeMillis() - started) + " ms");
 
         // tmp space on pearls is very restricted
         war.deleteFile();
         warModified.deleteFile();
 
         assertEquals(expected, md5(testhosts));
     }
 
     private Net net(FileNode testhosts) throws IOException {
         Net net;
 
         testhosts.deleteTreeOpt();
         testhosts.mkdir();
         net = new Net();
         net.add("test", new Cluster()
                 .addLocalhost(testhosts.join("cdn1"), "indexes")
                 .addLocalhost(testhosts.join("cdn2"), "indexes")
                 .addVhost("fix", "/fix", "fix1.uicdn.net", "fix2.uicdn.net", "fix3.uicdn.net", "fix4.uicdn.net"));
         net.add("flash-eu", new Cluster()
                 .addLocalhost(testhosts.join("flash-eu1"), "htdocs/.lavender")
                 .addLocalhost(testhosts.join("flash-eu2"), "htdocs/.lavender")
                 .addVhost("foo", "", "foo"));
         net.add("flash-us", new Cluster()
                 .addLocalhost(testhosts.join("flash-us"), "htdocs/.lavender")
                 .addVhost("foo", "", "foo"));
         return net;
     }
 
     private String md5(Node directory) throws IOException {
         World world;
         Filter filter;
         List<String> entries;
         Node tmp;
         String md5;
 
         world = directory.getWorld();
         filter = world.filter();
         filter.include("**/*");
         filter.predicate(Predicate.FILE);
         entries = new ArrayList<>();
         for (Node file : directory.find(filter)) {
             if (file.getName().startsWith(INDEX_NAME)) {
                 // because different machine sort entries differently. And there's a timestamp comment in the beginning
                 md5 = Integer.toHexString(file.readProperties().hashCode());
             } else {
                 md5 = file.md5();
             }
             entries.add(file.getRelative(directory) + ':' + md5 + '\n');
         }
         Collections.sort(entries); // because it runs on different filesystems
         tmp = world.memoryNode();
         return tmp.writeLines(entries).md5();
     }
 }
