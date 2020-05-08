 /**
  * Copyright 1&1 Internet AG, http://www.1and1.org
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation; either version 2 of the License,
  * or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package net.sf.beezle.jasmin.main;
 
 import net.sf.beezle.jasmin.model.Engine;
 import net.sf.beezle.jasmin.model.Module;
 import net.sf.beezle.jasmin.model.Repository;
 import net.sf.beezle.jasmin.model.Resolver;
 import net.sf.beezle.sushi.fs.Node;
 import net.sf.beezle.sushi.fs.World;
 import net.sf.beezle.sushi.fs.file.FileNode;
 import org.apache.log4j.Logger;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Random;
 
 public class Runner {
     public static final Random RANDOM = new Random();
     public static final Logger LOG = Logger.getLogger(Runner.class);
 
     public static Runner create(String name, World world) throws IOException {
         FileNode base;
         Application application;
         FileNode localhost;
         Engine engine;
 
         base = world.guessProjectHome(Runner.class);
        application = new Application(null, new Resolver(world, true), base.getName(), world.memoryNode("<repository/>"), null);
         localhost = world.getTemp().createTempDirectory();
         engine = application.createEngineSimple(base.getParent(), localhost);
         return new Runner(name, engine);
     }
 
     //--
 
     private final String name;
     private final Engine engine;
     private final List<String> paths;
 
     public Runner(String name, Engine engine) {
         this.name = name;
         this.engine = engine;
         this.paths = new ArrayList<String>();
     }
 
     public int pathCount() {
         return paths.size();
     }
 
     public Runner add(Node log, String application, int max) throws IOException {
         for (String line : log.readLines()) {
             line = line.trim();
             line = line.substring(line.indexOf('|') + 1);
             if (line.startsWith(application)) {
                 line = line.substring(application.length());
                 line = line.substring(0, line.indexOf(' '));
                 paths.add(line);
                 if (paths.size() == max) {
                     break;
                 }
             }
         }
         return this;
     }
 
     public Runner addAll() {
         Repository repository;
         List<String> variants;
 
         repository = engine.repository;
         variants = repository.getVariants();
         for (Module module : repository.modules()) {
             addTypes(module, "head");
             for (String variant : variants) {
                 addTypes(module, variant);
             }
         }
         return this;
     }
 
     public void addTypes(Module module, String variant) {
         add(module.getName() + "/js/" + variant);
         add(module.getName() + "/js-min/" + variant);
         add(module.getName() + "/css/" + variant);
         add(module.getName() + "/css-min/" + variant);
     }
 
     public Runner add(String... pathList) {
         this.paths.addAll(Arrays.asList(pathList));
         return this;
     }
 
 
     public Engine getEngine() {
         return engine;
     }
 
     //--
 
     public void invoke() throws Exception {
         invoke(1, paths.size(), false, false, true);
     }
 
     public void invoke(int clientCount, int requestCount) throws Exception {
         invoke(clientCount, requestCount, true, true, true);
     }
 
     public void invoke(int clientCount, int requestCount, boolean random, boolean lastModified, boolean stats)
             throws Exception {
         long started;
         Client[] clients;
 
         if (stats) {
             System.out.println("[" + name + "]\t" + clientCount + " client(s) a " + requestCount + " requests (random="
                     + random + ", lastModified=" + lastModified + ")");
         }
         started = System.currentTimeMillis();
         clients = new Client[clientCount];
         for (int i = 0; i < clients.length; i++) {
             clients[i] = new Client(engine, paths, requestCount, random, lastModified);
             clients[i].start();
         }
         for (Client client : clients) {
             client.finish();
         }
         if (stats) {
             stats(started);
         }
     }
 
     //--
 
     private void stats(long started) {
         long used;
 
         System.out.println();
         usedMemory();
         runGC();
         // CAUTION: I need a println before the next "used" call to get accurate numbers!?
         System.out.println("  ms:          " + (System.currentTimeMillis() - started));
         used = usedMemory();
         engine.free();
         runGC();
         System.out.println("  mem:         " + (used - usedMemory()));
         System.out.println();
         System.out.println();
     }
 
     private static void runGC() {
         // It helps to call Runtime.gc()
         // using several method calls:
         for (int r = 0; r < 4; ++r) {
             doRunGC();
         }
     }
 
     private static void doRunGC() {
         long used;
         long prev;
 
         used = usedMemory();
         prev = Long.MAX_VALUE;
         for (int i = 0; (used < prev) && (i < 500); i++) {
             Runtime.getRuntime().runFinalization();
             Runtime.getRuntime().gc();
             try {
                 Thread.sleep(100);
             } catch (InterruptedException e) {
                 throw new RuntimeException(e);
             }
             prev = used;
             used = usedMemory();
         }
     }
 
     private static long usedMemory() {
         return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
     }
 }
