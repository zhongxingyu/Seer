 /**
  * Copyright 2010 CosmoCode GmbH
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
 
 package de.cosmocode.palava.core;
 
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 import java.util.Properties;
 
 import org.apache.commons.io.IOUtils;
 import org.kohsuke.args4j.CmdLineException;
 import org.kohsuke.args4j.CmdLineParser;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import de.cosmocode.collections.MultiProperties;
 import de.cosmocode.commons.State;
 
 /**
  * Application entry point.
  *
  * @since 2.0
  * @author Willi Schoenborn
  * @author Tobias Sarnowski
  */
 public final class Main {
 
     private static final Logger LOG = LoggerFactory.getLogger(Main.class);
 
     private final Options options = new Options();
 
     private final Framework framework;
 
     private Main(String[] args) {
         final CmdLineParser parser = new CmdLineParser(options);
 
         try {
             parser.parseArgument(args);
         } catch (CmdLineException e) {
             System.err.println("Usage:  java [options] configuration-files...");
             parser.printUsage(System.err);
             throw new IllegalArgumentException(e);
         }
 
         LOG.info("Options: {}", options);
         LOG.info("Environment: {}", System.getenv());
         LOG.info("System: {}", System.getProperties());
 
         final Properties properties;
         
         try {
             properties = MultiProperties.load(options.getConfigs());
         } catch (IOException e) {
             throw new IllegalArgumentException(e);
         }
         
         try {
             framework = Palava.newFramework(properties);
         } finally {
             persistState();
         }
     }
 
     private void start() {
         try {
             framework.start();
             persistState();
         /* CHECKSTYLE:OFF */
         } catch (RuntimeException e) {
         /* CHECKSTYLE:ON */
             LOG.error("startup failed", e);
             e.printStackTrace();
             stop();
             System.exit(1);
         }
     }
 
     private void stop() {
         try {
             framework.stop();
         } finally {
             persistState();
         }
     }
 
     private void persistState() {
         if (options.getStateFile() == null) return;
 
         try {
             final State state = framework == null ? State.FAILED : framework.currentState();
             final Writer writer = new FileWriter(options.getStateFile());
             try {
                 IOUtils.write(state.name() + "\n", writer);
             } finally {
                 writer.close();
             }
         } catch (IOException e) {
             throw new IllegalArgumentException("cannot persist state to file", e);
         }
     }
 
     private void waitIfNecessary() {
         if (options.isNoAutoShutdown()) {
             LOG.debug("Automatic shutdown disabled; running until someone else triggers the shutdown");
 
            synchronized (this) {
                 try {
                    wait();
                 } catch (InterruptedException e) {
                     // it's ok, shut down
                     LOG.debug("main thread interrupted", e);
                 }
             }
         }
     }
     
     /**
      * Application entry point.
      *
      * @param args command line arguments
      * @throws CmdLineException if command line parsing failed
      */
     public static void main(String[] args) throws CmdLineException {
         AsciiArt.print();
 
         final Main main;
         
         try {
             main = new Main(args);
         /* CHECKSTYLE:OFF */
         } catch (RuntimeException e) {
         /* CHECKSTYLE:ON */
             LOG.error("configuration error", e);
             e.printStackTrace();
             System.exit(1);
             throw e;
         }
 
         main.start();
 
         LOG.debug("Adding shutdown hook");
         Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
 
             @Override
             public void run() {
                 main.stop();
                synchronized (main) {
                    main.notify();
                }
             }
 
         }));
 
         main.waitIfNecessary();
        System.exit(0);
     }
 
 }
