 /*
  * Copyright (c) 2012 Jim Svensson <jimpa@tla.se>
  *
  * Permission to use, copy, modify, and distribute this software for any
  * purpose with or without fee is hereby granted, provided that the above
  * copyright notice and this permission notice appear in all copies.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
  * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
  * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
  * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
  * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
  * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
  * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
  */
 
 package se.tla.mavenversionbumper;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.TreeSet;
 
 import joptsimple.OptionException;
 import joptsimple.OptionParser;
 import joptsimple.OptionSet;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.input.AutoCloseInputStream;
 import org.jdom.JDOMException;
 
 import se.tla.mavenversionbumper.vcs.AbstractVersionControl;
 import se.tla.mavenversionbumper.vcs.Clearcase;
 import se.tla.mavenversionbumper.vcs.Git;
 import se.tla.mavenversionbumper.vcs.NoopVersionControl;
 import se.tla.mavenversionbumper.vcs.VersionControl;
 import bsh.EvalError;
 import bsh.Interpreter;
 
 /**
  * Command line interface for the version bumper.
  */
 public class Main {
 
     private static final List<Module> modulesLoadedForUpdate = new LinkedList<Module>();
 
     private static String baseDirName;
     private static VersionControl versionControl = new NoopVersionControl();
     private static final Map<String, Class<? extends VersionControl>> versionControllers;
 
     static {
         versionControllers = new HashMap<String, Class<? extends VersionControl>>();
         // Only lower case names.
         versionControllers.put("git", Git.class);
         versionControllers.put("clearcase", Clearcase.class);
     }
 
     enum Option {
         DRYRUN("Dry run. Don't modify anything, only validate configuration.", "d", "dry-run"),
         REVERT("Revert any uncommited changes.", "r", "revert"),
         PREPARETEST("Prepare module(s) for a test build.", "p", "prepare-test-build"),
         WARNOFSNAPSHOTS("Searches for any SNAPSHOT dependencies and warns about them. Works great with --dry-run.", "w", "warn-snapshots"),
         REVERSEENGINEER("Build a basic scenario file from a set of modules.", "reverse-engineer"),
         HELP("Show help.", "h", "?", "help");
 
         private final String helpText;
         private final String[] aliases;
 
         Option(String helpText, String... aliases) {
             this.helpText = helpText;
             this.aliases = aliases;
         }
 
         public List<String> getAliases() {
             return Arrays.asList(aliases);
         }
 
         public String getHelpText() {
             return helpText;
         }
 
         public boolean presentIn(OptionSet o) {
             return o.has(aliases[0]);
         }
     }
 
     enum TYPE {
         NORMAL, DRYRUN, REVERT, PREPARETEST
     }
 
     public static void main(String args[]) {
 
         OptionParser parser = new OptionParser() {
             {
                 acceptsAll(Option.DRYRUN.getAliases(), Option.DRYRUN.getHelpText());
                 acceptsAll(Option.PREPARETEST.getAliases(), Option.PREPARETEST.getHelpText());
                 acceptsAll(Option.REVERT.getAliases(), Option.REVERT.getHelpText());
                 acceptsAll(Option.WARNOFSNAPSHOTS.getAliases(), Option.WARNOFSNAPSHOTS.getHelpText());
                 acceptsAll(Option.REVERSEENGINEER.getAliases(), Option.REVERSEENGINEER.getHelpText());
                 acceptsAll(Option.HELP.getAliases(), Option.HELP.getHelpText());
             }
         };
 
         OptionSet options = null;
         try {
             options = parser.parse(args);
         } catch (OptionException e) {
             System.err.println(e.getMessage());
             System.exit(1);
         }
 
         if (Option.HELP.presentIn(options)) {
             try {
                 parser.printHelpOn(System.out);
             } catch (IOException e) {
                 System.err.println("Error printing help text: " + e.getMessage());
                 System.exit(1);
             }
             System.exit(0);
         }
 
         if (countTrues(Option.DRYRUN.presentIn(options),
                 Option.PREPARETEST.presentIn(options),
                 Option.REVERT.presentIn(options),
                 Option.REVERSEENGINEER.presentIn(options)) > 1) {
             System.err.println("Only one of --dry-run/-d, --prepare-test-build/-p, --revert/-r and --reverse-engineer");
             System.exit(1);
         }
 
         List<String> arguments = options.nonOptionArguments();
 
         if (arguments.size() < 2 || arguments.size() > 3) {
             System.err.println("Usage: [-d | --dry-run] [-p | --prepare-test-build] [-r | --revert] [--reverse-engineer] [-w | --warn-snapshot] [-h | --help] <base directory> <scenarioFile> [<VC properties file>]");
             System.exit(1);
         }
 
         baseDirName = arguments.get(0);
         String scenarioFileName = arguments.get(1);
         Properties versionControlProperties;
 
         if (arguments.size() == 3) {
             try {
                 String versionControlParameter = arguments.get(2);
                 versionControlProperties = new Properties();
                 versionControlProperties.load(new AutoCloseInputStream(new FileInputStream(versionControlParameter)));
 
                 String versionControlName = versionControlProperties.getProperty(AbstractVersionControl.VERSIONCONTROL, "").toLowerCase();
                 Class<? extends VersionControl> versionControlClass = versionControllers.get(versionControlName);
                 if (versionControlClass == null) {
                     System.err.println("No such version control: " + versionControlName);
                     System.exit(1);
                 }
 
                 versionControl = versionControlClass.getConstructor(Properties.class).newInstance(versionControlProperties);
             } catch (Exception e) {
                 System.err.println("Error starting up the version control");
                 e.printStackTrace();
                 System.exit(1);
             }
         }
         File scenarioFile = new File(scenarioFileName);
 
         if (Option.REVERSEENGINEER.presentIn(options)) {
             if (scenarioFile.exists()) {
                 System.err.println("Scenario file " + scenarioFileName + " mustn't exist in reverse engineering mode.");
                 System.exit(1);
             }
 
             File baseDir = new File(baseDirName);
             if (!baseDir.isDirectory()) {
                 System.err.println("Base directory " + baseDirName + " isn't a directory.");
                 System.exit(1);
             }
 
             try {
                 List<ReverseEngineeringModule> modules = findModulesForReverseEngineering(baseDir, "");
 
                 reverseEngineerModules(modules, scenarioFile);
             } catch (JDOMException e) {
                 e.printStackTrace();
             } catch (IOException e) {
                 e.printStackTrace();
             }
             return;
         }
 
         if (!(scenarioFile.isFile() && scenarioFile.canRead())) {
             System.err.println("Scenario file " + scenarioFileName + " isn't a readable file.");
             System.exit(1);
         }
 
        if (Option.REVERT.presentIn(options) && versionControl == null) {
             System.err.println("Version control has to be defined while reverting.");
             System.exit(1);
         }
 
         TYPE type = TYPE.NORMAL;
         if (Option.DRYRUN.presentIn(options)) {
             type = TYPE.DRYRUN;
         }
         if (Option.REVERT.presentIn(options)) {
             type = TYPE.REVERT;
         }
         if (Option.PREPARETEST.presentIn(options)) {
             type = TYPE.PREPARETEST;
         }
 
         try {
             String scenario = FileUtils.readFileToString(scenarioFile);
             Interpreter i = new Interpreter();
             i.eval("importCommands(\"se.tla.mavenversionbumper.commands\")");
             i.eval("import se.tla.mavenversionbumper.Main");
             i.eval("import se.tla.mavenversionbumper.Module");
             i.eval("import se.tla.mavenversionbumper.ReadonlyModule");
             i.eval("baseDir = \"" + baseDirName + "\"");
             i.eval("load(String moduleName) { return Main.load(moduleName, null, null); }");
             i.eval("load(String moduleName, String newVersion) { return Main.load(moduleName, newVersion, null); }");
             i.eval("load(String moduleName, String newVersion, String label) { return Main.load(moduleName, newVersion, label); }");
             i.eval("loadReadOnly(String groupId, String artifactId, String version) { return new ReadonlyModule(groupId, artifactId, version); }");
             i.eval(scenario);
 
             if (Option.WARNOFSNAPSHOTS.presentIn(options)) {
                 for (Module module : modulesLoadedForUpdate) {
                     List<String> result = module.findSnapshots();
                     if (result.size() > 0) {
                         System.out.println("SNAPSHOTS found in module " + module.gav());
                         for (String s : result) {
                             System.out.println("  " + s);
                         }
                     }
                 }
             }
 
             if (type.equals(TYPE.NORMAL) || type.equals(TYPE.PREPARETEST)) {
                 versionControl.before(modulesLoadedForUpdate);
 
                 // Save
                 for (Module module : modulesLoadedForUpdate) {
                     module.save();
                 }
             }
 
             if (type.equals(TYPE.NORMAL)) {
                 versionControl.commit(modulesLoadedForUpdate);
                 versionControl.label(modulesLoadedForUpdate);
                 versionControl.after(modulesLoadedForUpdate);
             }
 
             if (type.equals(TYPE.REVERT)) {
                 versionControl.restore(modulesLoadedForUpdate);
             }
         } catch (EvalError e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     private static int countTrues(boolean... bs) {
         int result = 0;
         for (boolean b : bs) {
             if (b) {
                 result++;
             }
         }
         return result;
     }
 
     public static List<ReverseEngineeringModule> findModulesForReverseEngineering(File baseDir, String modulePath) throws JDOMException, IOException {
         List<ReverseEngineeringModule> result = new LinkedList<ReverseEngineeringModule>();
 
         ReverseEngineeringModule m = new ReverseEngineeringModule(baseDir, modulePath);
         result.add(m);
 
         List<String> subModules = m.subModules();
         for (String subModule : subModules) {
             String subModulePath = (modulePath.isEmpty() ? subModule : modulePath + "/" + subModule);
             result.addAll(findModulesForReverseEngineering(baseDir, subModulePath));
         }
 
         return result;
     }
 
     public static void reverseEngineerModules(List<ReverseEngineeringModule> modules, File scenarioFile) throws IOException {
         StringBuilder builder = new StringBuilder();
         for (ReverseEngineeringModule module : modules) {
             module.consider(modules);
         }
 
         for (ReverseEngineeringModule module : new TreeSet<ReverseEngineeringModule>(modules)) {
             builder.append(module.toString());
         }
 
         FileUtils.write(scenarioFile, builder.toString(), "ISO-8859-1");
     }
 
     /**
      * Create a Module located by this filename that is a directory relative to the baseDir.
      *
      * @param moduleDirectoryName Name of base directory for the module.
      * @param newVersion          New version to set directly, of null if no version should be set.
      * @param label               New label to set directly, or null if no labeling should be performed.
      * @return Newly created Module.
      * @throws JDOMException If the modules pom.xml couldn't be parsed.
      * @throws IOException   if the modules pom.xml couldn't be read.
      */
     public static Module load(String moduleDirectoryName, String newVersion, String label) throws JDOMException, IOException {
         Module m = new Module(baseDirName, moduleDirectoryName);
 
         if (newVersion != null) {
             System.out.println("ORG: " + m.gav());
             m.version(newVersion);
         }
         if (label != null) {
             m.label(label);
         }
 
         if (newVersion != null) {
             System.out.println("NEW: " + m.gav() + (label != null ? " (" + label + ")" : ""));
         }
 
         modulesLoadedForUpdate.add(m);
 
         return m;
     }
 }
