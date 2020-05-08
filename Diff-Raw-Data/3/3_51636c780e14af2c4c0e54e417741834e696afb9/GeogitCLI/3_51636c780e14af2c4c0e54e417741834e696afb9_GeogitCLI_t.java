 /* Copyright (c) 2013 OpenPlans. All rights reserved.
  * This code is licensed under the BSD New License, available at the root
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
 import java.util.Map.Entry;
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
 import org.geogit.api.porcelain.ConfigException;
 import org.geogit.api.porcelain.ConfigGet;
 import org.geotools.util.DefaultProgressListener;
 import org.geotools.util.logging.Logging;
 import org.opengis.util.ProgressListener;
 
 import com.beust.jcommander.JCommander;
 import com.beust.jcommander.ParameterException;
 import com.google.common.base.Optional;
 import com.google.common.base.Predicate;
 import com.google.common.base.Splitter;
 import com.google.common.base.Strings;
 import com.google.common.base.Throwables;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
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
             geogitInjector = GlobalInjectorBuilder.builder.build();
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
                     consoleReader.println(Optional.fromNullable(e.getMessage()).or("Uknown error"));
                     consoleReader.flush();
                 } else if (e instanceof CommandFailedException) {
                     // do nothing here, this exception indicates a failure, but the corresponding
                     // command throwing it should have taken care of outputting an error message or
                     // providing user interaction
                     consoleReader.flush();
                } else {
                    consoleReader.println(e.getMessage());
                    consoleReader.flush();
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
             args = unalias(args);
             final String commandName = args[0];
             JCommander commandParser = mainCommander.getCommands().get(commandName);
 
             if (commandParser == null) {
                 consoleReader.println(args[0] + " is not a geogit command. See geogit --help.");
                 // check for similar commands
                 Map<String, JCommander> candidates = spellCheck(mainCommander.getCommands(),
                         commandName);
                 if (!candidates.isEmpty()) {
                     String msg = candidates.size() == 1 ? "Did you mean this?"
                             : "Did you mean one of these?";
                     consoleReader.println();
                     consoleReader.println(msg);
                     for (String name : candidates.keySet()) {
                         consoleReader.println("\t" + name);
                     }
                 }
                 consoleReader.flush();
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
             if (mainCommander.getObjects().size() == 0) {
                 mainCommander.usage();
             } else if (mainCommander.getObjects().get(0) instanceof CLICommandExtension) {
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
      * If the passed arguments contains an alias, it replaces it by the full command corresponding
      * to that alias and returns anew set of arguments
      * 
      * IF not, it returns the passed arguments
      * 
      * @param args
      * @return
      */
     private String[] unalias(String[] args) {
         String configParam = "alias." + args[0];
         if (geogit == null) { // in case the repo is not initialized yet
             return args;
         }
         try {
             Optional<String> unaliased = geogit.command(ConfigGet.class).setName(configParam)
                     .call();
             if (!unaliased.isPresent()) {
                 unaliased = geogit.command(ConfigGet.class).setGlobal(true).setName(configParam)
                         .call();
             }
             if (!unaliased.isPresent()) {
                 return args;
             }
             Iterable<String> tokens = Splitter.on(" ").split(unaliased.get());
             List<String> allArgs = Lists.newArrayList(tokens);
             allArgs.addAll(Lists.newArrayList(Arrays.copyOfRange(args, 1, args.length)));
             return allArgs.toArray(new String[0]);
         } catch (ConfigException e) {
             return args;
         }
     }
 
     /**
      * Return all commands with a command name at a levenshtein distance of less than 3, as
      * potential candidates for a mistyped command
      * 
      * @param commands the list of all available commands
      * @param commandName the command name
      * @return a map filtered according to distance between command names
      */
     private Map<String, JCommander> spellCheck(Map<String, JCommander> commands,
             final String commandName) {
         Map<String, JCommander> candidates = Maps.filterEntries(commands,
                 new Predicate<Map.Entry<String, JCommander>>() {
                     @Override
                     public boolean apply(@Nullable Entry<String, JCommander> entry) {
                         char[] s1 = entry.getKey().toCharArray();
                         char[] s2 = commandName.toCharArray();
                         int[] prev = new int[s2.length + 1];
                         for (int j = 0; j < s2.length + 1; j++) {
                             prev[j] = j;
                         }
                         for (int i = 1; i < s1.length + 1; i++) {
                             int[] curr = new int[s2.length + 1];
                             curr[0] = i;
                             for (int j = 1; j < s2.length + 1; j++) {
                                 int d1 = prev[j] + 1;
                                 int d2 = curr[j - 1] + 1;
                                 int d3 = prev[j - 1];
                                 if (s1[i - 1] != s2[j - 1]) {
                                     d3 += 1;
                                 }
                                 curr[j] = Math.min(Math.min(d1, d2), d3);
                             }
                             prev = curr;
                         }
                         return prev[s2.length] < 3;
                     }
                 });
         return candidates;
     }
 
     /**
      * This prints out only porcelain commands
      * 
      * @param mainCommander
      * 
      * @throws IOException
      */
     public void printShortCommandList(JCommander mainCommander) {
         TreeSet<String> commandNames = Sets.newTreeSet();
         int longestCommandLenght = 0;
         // do this to ignore aliases
         for (String name : mainCommander.getCommands().keySet()) {
             JCommander command = mainCommander.getCommands().get(name);
             Class<? extends Object> clazz = command.getObjects().get(0).getClass();
             String packageName = clazz.getPackage().getName();
             if (!packageName.startsWith("org.geogit.cli.plumbing")) {
                 commandNames.add(name);
                 longestCommandLenght = Math.max(longestCommandLenght, name.length());
             }
         }
         ConsoleReader console = getConsole();
         try {
             console.println("usage: geogit <command> [<args>]");
             console.println();
             console.println("The most commonly used geogit commands are:");
             for (String cmd : commandNames) {
                 console.print(Strings.padEnd(cmd, longestCommandLenght, ' '));
                 console.print("\t");
                 console.println(mainCommander.getCommandDescription(cmd));
             }
             console.flush();
         } catch (IOException e) {
             throw Throwables.propagate(e);
         }
     }
 
     /**
      * This prints out all commands, including plumbing ones, without description
      * 
      * @param mainCommander
      * @throws IOException
      */
     public void printCommandList(JCommander mainCommander) {
         TreeSet<String> commandNames = Sets.newTreeSet();
         int longestCommandLenght = 0;
         // do this to ignore aliases
         for (String name : mainCommander.getCommands().keySet()) {
             commandNames.add(name);
             longestCommandLenght = Math.max(longestCommandLenght, name.length());
         }
         ConsoleReader console = getConsole();
         try {
             console.println("usage: geogit <command> [<args>]");
             console.println();
             int i = 0;
             for (String cmd : commandNames) {
                 console.print(Strings.padEnd(cmd, longestCommandLenght, ' '));
                 i++;
                 if (i % 3 == 0) {
                     console.println();
                 } else {
                     console.print("\t");
                 }
             }
             console.flush();
         } catch (IOException e) {
             throw Throwables.propagate(e);
         }
     }
 
     /**
      * /**
      * 
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
                 public void started() {
                     super.started();
                     lastRun = -(delayMillis + 1);
                 }
 
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
                     if (percent > 99f || (currentTimeMillis - lastRun) > delayMillis) {
                         lastRun = currentTimeMillis;
                         log(percent);
                     }
                 }
 
                 private void log(float percent) {
                     CursorBuffer cursorBuffer = console.getCursorBuffer();
                     cursorBuffer.clear();
                     String description = getDescription();
                     if (description != null) {
                         cursorBuffer.write(description);
                     }
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
