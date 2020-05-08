 /* Copyright (c) 2011 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the LGPL 2.1 license, available at the root
  * application directory.
  */
 package org.geogit.cli;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 import java.io.IOException;
 import java.text.NumberFormat;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 import java.util.ServiceLoader;
 import java.util.TreeSet;
 
 import javax.annotation.Nullable;
 
 import jline.console.ConsoleReader;
 import jline.console.CursorBuffer;
 
 import org.geogit.api.DefaultPlatform;
 import org.geogit.api.GeoGIT;
 import org.geogit.api.GlobalInjectorBuilder;
 import org.geogit.api.Platform;
 import org.geogit.api.plumbing.ResolveGeogitDir;
 import org.geotools.util.DefaultProgressListener;
 import org.geotools.util.logging.Logging;
 import org.opengis.util.ProgressListener;
 
 import com.beust.jcommander.JCommander;
 import com.beust.jcommander.ParameterException;
 import com.google.common.base.Optional;
 import com.google.common.base.Strings;
 import com.google.common.base.Throwables;
 import com.google.common.collect.Sets;
 import com.google.inject.Binding;
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.google.inject.Key;
 import com.google.inject.Module;
 
 /**
  * Command Line Interface for geogit.
  * <p>
  * Looks up and executes {@link CLICommand} implementations provided by any {@link Guice}
  * {@link Module} that implements {@link CLIModule} declared in any classpath's
  * {@code META-INF/services/com.google.inject.Module} file.
  */
 public class GeogitCLI {
 
     private Injector commandsInjector;
 
     private Injector geogitInjector;
 
     private Platform platform;
 
     private GeoGIT geogit;
 
     private ConsoleReader consoleReader;
 
     private DefaultProgressListener progressListener;
 
     /**
      * Construct a GeogitCLI with the given console reader.
      * 
      * @param consoleReader
      */
     public GeogitCLI(final ConsoleReader consoleReader) {
         this.consoleReader = consoleReader;
         this.platform = new DefaultPlatform();
         GlobalInjectorBuilder.builder = new CLIInjectorBuilder();
 
         Iterable<CLIModule> plugins = ServiceLoader.load(CLIModule.class);
         commandsInjector = Guice.createInjector(plugins);
     }
 
     /**
      * @return the platform being used by the geogit command line interface.
      * @see Platform
      */
     public Platform getPlatform() {
         return platform;
     }
 
     /**
      * Sets the platform for the command line interface to use.
      * 
      * @param platform the platform to use
      * @see Platform
      */
     public void setPlatform(Platform platform) {
         checkNotNull(platform);
         this.platform = platform;
     }
 
     /**
      * Provides a GeoGIT facade configured for the current repository if inside a repository,
      * {@code null} otherwise.
      * <p>
      * Note the repository is lazily loaded and cached afterwards to simplify the execution of
      * commands or command options that do not need a live repository.
      * 
      * @return the GeoGIT facade
      */
     public synchronized GeoGIT getGeogit() {
         if (geogit == null) {
             GeoGIT geogit = loadRepository();
             setGeogit(geogit);
         }
         return geogit;
     }
 
     /**
      * Gives the command line interface a GeoGIT facade to use.
      * 
      * @param geogit
      */
     public void setGeogit(@Nullable GeoGIT geogit) {
         this.geogit = geogit;
     }
 
     /**
      * Loads the repository _if_ inside a geogit repository and returns a configured {@link GeoGIT}
      * facade.
      * 
      * @return a geogit for the current repository or {@code null} if not inside a geogit repository
      *         directory.
      */
     private GeoGIT loadRepository() {
         GeoGIT geogit = newGeoGIT();
 
         if (null != geogit.command(ResolveGeogitDir.class).call()) {
             geogit.getRepository();
             return geogit;
         }
 
         return null;
     }
 
     /**
      * Constructs a new geogit facade.
      * 
      * @return the constructed GeoGIT.
      */
     public GeoGIT newGeoGIT() {
         Injector inj = getGeogitInjector();
         return new GeoGIT(inj, platform.pwd());
     }
 
     /**
      * @return the Guice injector being used by the command line interface. If one hasn't been made,
      *         it will be created.
      */
     public Injector getGeogitInjector() {
         if (geogitInjector == null) {
             geogitInjector = GlobalInjectorBuilder.builder.get();
         }
         return geogitInjector;
     }
 
     /**
      * Sets the Guice injector for the command line interface to use.
      * 
      * @param injector the Guice injector to use
      */
     public void setGeogitInjector(@Nullable Injector injector) {
         geogitInjector = injector;
     }
 
     /**
      * @return the console reader being used by the command line interface.
      */
     public ConsoleReader getConsole() {
         return consoleReader;
     }
 
     /**
      * Closes the GeoGIT facade if it exists.
      */
     public void close() {
         if (geogit != null) {
             geogit.close();
             geogit = null;
         }
     }
 
     /**
      * Entry point for the command line interface.
      * 
      * @param args
      */
     public static void main(String[] args) {
         Logging.ALL.forceMonolineConsoleOutput();
         // TODO: revisit in case we need to grafefully shutdown upon CTRL+C
         // Runtime.getRuntime().addShutdownHook(new Thread() {
         // @Override
         // public void run() {
         // System.err.println("Shutting down...");
         // System.err.flush();
         // }
         // });
 
         ConsoleReader consoleReader;
         try {
             consoleReader = new ConsoleReader(System.in, System.out);
             // needed for CTRL+C not to let the console broken
             consoleReader.getTerminal().setEchoEnabled(true);
         } catch (Exception e) {
             throw Throwables.propagate(e);
         }
 
         int exitCode = 0;
         GeogitCLI cli = new GeogitCLI(consoleReader);
         cli.processCommand(args);
 
         try {
             cli.close();
         } finally {
             try {
                 consoleReader.getTerminal().restore();
             } catch (Exception e) {
                 e.printStackTrace();
                 exitCode = -1;
             }
         }
 
         System.exit(exitCode);
     }
 
     /**
      * Finds all commands that are bound do the command injector.
      * 
      * @return a collection of keys, one for each command
      */
     private Collection<Key<?>> findCommands() {
         Map<Key<?>, Binding<?>> commands = commandsInjector.getBindings();
         return commands.keySet();
     }
 
     public JCommander newCommandParser() {
         JCommander jc = new JCommander(this);
         jc.setProgramName("geogit");
         for (Key<?> cmd : findCommands()) {
             Object obj = commandsInjector.getInstance(cmd);
             if (obj instanceof CLICommand || obj instanceof CLICommandExtension) {
                 jc.addCommand(obj);
             }
         }
         return jc;
     }
 
     /**
      * Processes a command, catching any exceptions and printing their messages to the console.
      * 
      * @param args
      * @return 0 for normal exit, -1 if there was an exception.
      */
     public int processCommand(String... args) {
         int exitCode = 0;
         try {
             execute(args);
         } catch (Exception e) {
             exitCode = -1;
             try {
                 if (e instanceof ParameterException) {
                     consoleReader.println(e.getMessage() + ". See geogit --help.");
                     consoleReader.flush();
                 } else if (e instanceof IllegalArgumentException
                         || e instanceof IllegalStateException) {
                    // e.printStackTrace();
                     consoleReader.println(Optional.fromNullable(e.getMessage()).or("Uknown error"));
                     consoleReader.flush();
                 } else {
                     e.printStackTrace();
                 }
             } catch (IOException ioe) {
                 ioe.printStackTrace();
             }
         }
         return exitCode;
     }
 
     /**
      * Executes a command.
      * 
      * @param args
      * @throws exceptions thrown by the executed commands.
      */
     public void execute(String... args) throws Exception {
         JCommander mainCommander = newCommandParser();
         if (null == args || args.length == 0) {
             printShortCommandList(mainCommander);
             return;
         }
         {
             final String commandName = args[0];
             JCommander commandParser = mainCommander.getCommands().get(commandName);
             if (commandParser == null) {
                 mainCommander.parse(args);// make it fail with a reasonable error message
                 return;
             }
 
             Object object = commandParser.getObjects().get(0);
             if (object instanceof CLICommandExtension) {
                 args = Arrays.asList(args).subList(1, args.length)
                         .toArray(new String[args.length - 1]);
                 mainCommander = ((CLICommandExtension) object).getCommandParser();
             }
         }
 
         mainCommander.parse(args);
         final String parsedCommand = mainCommander.getParsedCommand();
         if (null == parsedCommand) {
             if (mainCommander.getObjects().get(0) instanceof CLICommandExtension) {
                 CLICommandExtension extension = (CLICommandExtension) mainCommander.getObjects()
                         .get(0);
                 extension.getCommandParser().usage();
             } else {
                 mainCommander.usage();
             }
         } else {
             JCommander jCommander = mainCommander.getCommands().get(parsedCommand);
             List<Object> objects = jCommander.getObjects();
             CLICommand cliCommand = (CLICommand) objects.get(0);
             cliCommand.run(this);
             getConsole().flush();
         }
     }
 
     /**
      * @param mainCommander
      * @throws IOException
      */
     public void printShortCommandList(JCommander mainCommander) {
         TreeSet<String> commandNames = Sets.newTreeSet();
         int longesCommandLenght = 0;
         // do this to ignore aliases
         for (String name : mainCommander.getCommands().keySet()) {
             commandNames.add(name);
             longesCommandLenght = Math.max(longesCommandLenght, name.length());
         }
         ConsoleReader console = getConsole();
         try {
             console.println("usage: geogit <command> [<args>]");
             console.println();
             console.println("The most commonly used geogit commands are:");
             for (String cmd : commandNames) {
                 console.print(Strings.padEnd(cmd, longesCommandLenght, ' '));
                 console.print("\t");
                 console.println(mainCommander.getCommandDescription(cmd));
             }
             console.flush();
         } catch (IOException e) {
             throw Throwables.propagate(e);
         }
     }
 
     /**
      * @return the ProgressListener for the command line interface. If it doesn't exist, a new one
      *         will be constructed.
      * @see ProgressListener
      */
     public synchronized ProgressListener getProgressListener() {
         if (this.progressListener == null) {
 
             this.progressListener = new DefaultProgressListener() {
 
                 private final Platform platform = getPlatform();
 
                 private final ConsoleReader console = getConsole();
 
                 private final NumberFormat fmt = NumberFormat.getPercentInstance();
 
                 private final long delayMillis = 300;
 
                 // Don't skip the first update
                 private volatile long lastRun = -(delayMillis + 1);
 
                 @Override
                 public void complete() {
                     // avoid double logging if caller missbehaves
                     if (super.isCompleted()) {
                         return;
                     }
                     super.complete();
                     super.dispose();
                     try {
                         log(100f);
                         console.println();
                         console.flush();
                     } catch (IOException e) {
                         Throwables.propagate(e);
                     }
                 }
 
                 @Override
                 public void progress(float percent) {
                     super.progress(percent);
                     long currentTimeMillis = platform.currentTimeMillis();
                     if ((currentTimeMillis - lastRun) > delayMillis) {
                         lastRun = currentTimeMillis;
                         log(percent);
                     }
                 }
 
                 private void log(float percent) {
                     CursorBuffer cursorBuffer = console.getCursorBuffer();
                     cursorBuffer.clear();
                     cursorBuffer.write(fmt.format(percent / 100f));
                     try {
                         console.redrawLine();
                         console.flush();
                     } catch (IOException e) {
                         Throwables.propagate(e);
                     }
                 }
             };
 
         }
         return this.progressListener;
     }
 
 }
