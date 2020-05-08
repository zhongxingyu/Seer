 /**
  *  Copyright 2008 ThimbleWare Inc.
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 package com.thimbleware.jmemcached;
 
 import org.apache.commons.cli.*;
 
 import java.net.InetSocketAddress;
 
 import com.thimbleware.jmemcached.util.Bytes;
 import com.thimbleware.jmemcached.storage.hash.ConcurrentLinkedHashMap;
 import com.thimbleware.jmemcached.storage.hash.SizedItem;
 import com.thimbleware.jmemcached.storage.ConcurrentSizedMap;
 import com.thimbleware.jmemcached.storage.ConcurrentSizedBlockStorageMap;
 import com.thimbleware.jmemcached.storage.mmap.MemoryMappedBlockStore;
 
 
 /**
  * Command line interface to the Java memcache daemon.
  *
  * Arguments in general parallel those of the C implementation.
  */
 public class Main {
 
     public static void main(String[] args) throws Exception {
         // look for external log4j.properties
 
         // setup command line options
         Options options = new Options();
         options.addOption("h", "help", false, "print this help screen");
         options.addOption("f", "mapped-file", true, "use external (from JVM) heap through a memory mapped file");
         options.addOption("bs", "block-size", true, "block size (in bytes) for external memory mapped file allocator.  default is 8 bytes");
         options.addOption("i", "idle", true, "disconnect after idle <x> seconds");
         options.addOption("p", "port", true, "port to listen on");
         options.addOption("m", "memory", true, "max memory to use; in bytes, specify K, kb, M, GB for larger units");
         options.addOption("c", "ceiling", true, "ceiling memory to use; in bytes, specify K, kb, M, GB for larger units");
         options.addOption("l", "listen", true, "Address to listen on");
         options.addOption("s", "size", true, "max items");
         options.addOption("b", "binary", false, "binary protocol mode");
         options.addOption("V", false, "Show version number");
         options.addOption("v", false, "verbose (show commands)");
 
         // read command line options
         CommandLineParser parser = new PosixParser();
         CommandLine cmdline = parser.parse(options, args);
 
         if (cmdline.hasOption("help") || cmdline.hasOption("h")) {
             System.out.println("Memcached Version " + MemCacheDaemon.memcachedVersion);
             System.out.println("http://thimbleware.com/projects/memcached\n");
 
             HelpFormatter formatter = new HelpFormatter();
             formatter.printHelp("java -jar memcached.jar", options);
             return;
         }
 
         if (cmdline.hasOption("V")) {
             System.out.println("Memcached Version " + MemCacheDaemon.memcachedVersion);
             return;
         }
 
         int port = 11211;
         if (cmdline.hasOption("p")) {
             port = Integer.parseInt(cmdline.getOptionValue("p"));
         } else if (cmdline.hasOption("port")) {
             port = Integer.parseInt(cmdline.getOptionValue("port"));
         }
 
         InetSocketAddress addr = new InetSocketAddress(port);
         if (cmdline.hasOption("l")) {
             addr = new InetSocketAddress(cmdline.getOptionValue("l"), port);
         } else if (cmdline.hasOption("listen")) {
             addr = new InetSocketAddress(cmdline.getOptionValue("listen"), port);
         }
 
         int max_size = 1000000;
         if (cmdline.hasOption("s"))
             max_size = (int)Bytes.valueOf(cmdline.getOptionValue("s")).bytes();
         else if (cmdline.hasOption("size"))
             max_size = (int)Bytes.valueOf(cmdline.getOptionValue("size")).bytes();
 
         System.out.println("Setting max cache elements to " + String.valueOf(max_size));
 
         int idle = -1;
         if (cmdline.hasOption("i")) {
             idle = Integer.parseInt(cmdline.getOptionValue("i"));
         } else if (cmdline.hasOption("idle")) {
             idle = Integer.parseInt(cmdline.getOptionValue("idle"));
         }
 
         boolean memoryMapped = false;
         String mmapFile = "";
         if (cmdline.hasOption("f")) {
             memoryMapped = true;
             mmapFile = cmdline.getOptionValue("f");
         } else if (cmdline.hasOption("mapped-file")) {
             memoryMapped = true;
             mmapFile = cmdline.getOptionValue("f");
         }
 
         boolean verbose = false;
         if (cmdline.hasOption("v")) {
             verbose = true;
         }
 
         long ceiling;
         if (cmdline.hasOption("c")) {
             ceiling = Bytes.valueOf(cmdline.getOptionValue("c")).bytes();
             System.out.println("Setting ceiling memory size to " + Bytes.bytes(ceiling).megabytes() + "M");
         } else if (cmdline.hasOption("ceiling")) {
             ceiling = Bytes.valueOf(cmdline.getOptionValue("ceiling")).bytes();
             System.out.println("Setting ceiling memory size to " + Bytes.bytes(ceiling).megabytes() + "M");
         } else if (!memoryMapped ){
             ceiling = 1024000;
             System.out.println("Setting ceiling memory size to default limit of " + Bytes.bytes(ceiling).megabytes() + "M");
         } else {
             System.out.println("ERROR : ceiling memory size mandatory when external memory mapped file is specified");
 
             return;
         }
 
         boolean binary = false;
         if (cmdline.hasOption("b")) {
             binary = true;
         }
 
         int blockSize = 8;
         if (!memoryMapped && (cmdline.hasOption("bs") || cmdline.hasOption("block-size"))) {
             System.out.println("WARN : block size option is only valid for memory mapped external heap storage; ignoring");
         } else if (cmdline.hasOption("bs")) {
             blockSize = Integer.parseInt(cmdline.getOptionValue("bs"));
         } else if (cmdline.hasOption("block-size")) {
             blockSize = Integer.parseInt(cmdline.getOptionValue("block-size"));
         }
 
         long maxBytes;
         if (cmdline.hasOption("m")) {
             maxBytes = Bytes.valueOf(cmdline.getOptionValue("m")).bytes();
             System.out.println("Setting max memory size to " + Bytes.bytes(maxBytes).gigabytes() + "GB");
         } else if (cmdline.hasOption("memory")) {
             maxBytes = Bytes.valueOf(cmdline.getOptionValue("memory")).bytes();
             System.out.println("Setting max memory size to " + Bytes.bytes(maxBytes).gigabytes() + "GB");
         } else if (!memoryMapped) {
             maxBytes = Runtime.getRuntime().maxMemory();
             System.out.println("Setting max memory size to JVM limit of " + Bytes.bytes(maxBytes).gigabytes() + "GB");
         } else {
             System.out.println("ERROR : max memory size mandatory when external memory mapped file is specified");
             return;
         }
 
         if (!memoryMapped && maxBytes > Runtime.getRuntime().maxMemory()) {
             System.out.println("ERROR : JVM heap size is not big enough. use '-Xmx" + String.valueOf(maxBytes / 1024000) + "m' java argument before the '-jar' option.");
             return;
         } else if (memoryMapped && maxBytes > Integer.MAX_VALUE) {
             System.out.println("ERROR : when external memory mapped, memory size may not exceed the size of Integer.MAX_VALUE (" + Bytes.bytes(Integer.MAX_VALUE).gigabytes() + "GB");
             return;
         }
 
         // create daemon and start it
         final MemCacheDaemon daemon = new MemCacheDaemon();
 
         ConcurrentSizedMap<String, MCElement> storage;
        if (memoryMapped)
             storage = ConcurrentLinkedHashMap.create(ConcurrentLinkedHashMap.EvictionPolicy.FIFO, max_size, maxBytes);
         else  {
             MemoryMappedBlockStore mappedBlockStore = new MemoryMappedBlockStore((int)maxBytes, mmapFile, blockSize);
 
             storage = new ConcurrentSizedBlockStorageMap(mappedBlockStore, (int)ceiling, max_size);
         }
 
         daemon.setCache(new CacheImpl(storage));
         daemon.setBinary(binary);
         daemon.setAddr(addr);
         daemon.setIdleTime(idle);
         daemon.setVerbose(verbose);
         daemon.start();
         
         Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
             public void run() {
                 if (daemon != null && daemon.isRunning()) daemon.stop();
             }
         }));
     }
 
 
 }
