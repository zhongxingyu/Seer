 /* Copyright (c) 2011 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the LGPL 2.1 license, available at the root
  * application directory.
  */
 package org.geogit.pg.cli.test.functional;
 
 import static org.geogit.pg.cli.test.functional.PGGlobalState.currentDirectory;
 import static org.geogit.pg.cli.test.functional.PGGlobalState.stdIn;
 import static org.geogit.pg.cli.test.functional.PGGlobalState.stdOut;
 import static org.junit.Assert.assertNotNull;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.InputStreamReader;
 import java.nio.charset.Charset;
 import java.util.List;
 
 import jline.UnsupportedTerminal;
 import jline.console.ConsoleReader;
 
 import org.geogit.api.GeoGIT;
 import org.geogit.api.Platform;
 import org.geogit.cli.GeogitCLI;
 import org.geogit.di.GeogitModule;
 import org.geogit.storage.bdbje.JEStorageModule;
 import org.geotools.util.logging.Logging;
 
 import com.google.common.io.ByteStreams;
 import com.google.common.io.CharStreams;
 import com.google.common.io.InputSupplier;
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.google.inject.util.Modules;
 
 public abstract class AbstractPGFunctionalTest {
 
     static {
         Logging.ALL.forceMonolineConsoleOutput(java.util.logging.Level.SEVERE);
     }
 
     /**
     * Runs the given command with its arguments and returns the command output as a list of
     * strings, one per line.
      */
     protected List<String> runAndParseCommand(String... command) throws Exception {
 
         ByteArrayOutputStream out = runCommand(command);
         InputSupplier<InputStreamReader> readerSupplier = CharStreams.newReaderSupplier(
                 ByteStreams.newInputStreamSupplier(out.toByteArray()), Charset.forName("UTF-8"));
         List<String> lines = CharStreams.readLines(readerSupplier);
         return lines;
     }
 
     protected ByteArrayOutputStream runCommand(String... command) throws Exception {
         // System.err.println("Running command " + Arrays.toString(command));
         assertNotNull(currentDirectory);
 
         stdIn = new ByteArrayInputStream(new byte[0]);
         stdOut = new ByteArrayOutputStream();
 
         Platform platform = new PGTestPlatform(currentDirectory);
 
         ConsoleReader consoleReader = new ConsoleReader(stdIn, stdOut, new UnsupportedTerminal());
 
         Injector injector = Guice.createInjector(Modules.override(new GeogitModule()).with(
                 new JEStorageModule()));
         GeoGIT geogit = new GeoGIT(injector, currentDirectory);
         try {
             GeogitCLI cli = new GeogitCLI(consoleReader);
             if (geogit.getRepository() != null) {
                 cli.setGeogit(geogit);
             }
             cli.setPlatform(platform);
             cli.execute(command);
         } finally {
             geogit.close();
         }
         return stdOut;
     }
 
     protected String getDatabaseParameters() throws Exception {
         IniPGProperties properties = new IniPGProperties();
         StringBuilder sb = new StringBuilder();
         sb.append(" --host ");
         sb.append(properties.get("database.host", String.class).or("localhost"));
 
         sb.append(" --port ");
         sb.append(properties.get("database.port", String.class).or("5432"));
 
         sb.append(" --schema ");
         sb.append(properties.get("database.schema", String.class).or("public"));
 
         sb.append(" --database ");
         sb.append(properties.get("database.database", String.class).or("database"));
 
         sb.append(" --user ");
         sb.append(properties.get("database.user", String.class).or("postgres"));
 
         sb.append(" --password ");
         sb.append(properties.get("database.password", String.class).or("postgres"));
 
         return sb.toString();
     }
 }
