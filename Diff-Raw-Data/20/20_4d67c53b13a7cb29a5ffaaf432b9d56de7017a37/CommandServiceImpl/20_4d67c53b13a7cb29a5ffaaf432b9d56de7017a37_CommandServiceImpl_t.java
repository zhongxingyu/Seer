 package com.cloudbees.sdk;
 
 import com.cloudbees.sdk.cli.CLICommand;
 import com.cloudbees.sdk.cli.CLIModule;
 import com.cloudbees.sdk.cli.CommandDescription;
 import com.cloudbees.sdk.cli.CommandGroup;
 import com.cloudbees.sdk.cli.CommandResolver;
 import com.cloudbees.sdk.cli.DirectoryStructure;
 import com.cloudbees.sdk.cli.Experimental;
 import com.cloudbees.sdk.cli.ICommand;
 import com.cloudbees.sdk.cli.CommandService;
 import com.cloudbees.sdk.cli.Verbose;
 import com.cloudbees.sdk.extensibility.AnnotationLiteral;
 import com.cloudbees.sdk.extensibility.ExtensionFinder;
 import com.cloudbees.sdk.extensibility.ExtensionPointList;
 import com.google.inject.Binding;
 import com.google.inject.ConfigurationException;
 import com.google.inject.Injector;
 import com.google.inject.Key;
 import com.google.inject.Module;
 import com.google.inject.Provider;
 import org.sonatype.aether.RepositoryException;
 
 import javax.inject.Inject;
 import javax.inject.Singleton;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 
 /**
  * Looks up a {@link ICommand} object from the command name.
  * 
  * @author Fabiane Donze
  * @author Kohsuke Kawaguchi
  */
 @Singleton
 public class CommandServiceImpl implements CommandService {
     static final String NL = System.getProperty("line.separator");
 
     DirectoryStructure structure;
 
     @Inject
     private Injector injector;
 
     @Inject
     Provider<ArtifactClassLoaderFactory> artifactClassLoaderFactoryProvider;
     
     @Inject
     private InstalledPluginList installedPluginList;
     
     @Inject
     private Verbose verbose;
 
     @Inject @ExtensionClassLoader
     private ClassLoader extClassLoader;
     
     @Inject
     private ExtensionPointList<CommandResolver> resolvers;
     
     @Inject
     public CommandServiceImpl(DirectoryStructure structure) {
         this.structure = structure;
     }
     
 
     /**
      * Look up a command from Guice, if necessary by downloading it.
      */
     public ICommand getCommand(String name) throws IOException {
         try {
             Injector injector = this.injector;
 
             // CommandResolvers take precedence over our default
             for (CommandResolver cr : resolvers.list(injector)) {
                 ICommand cmd = cr.resolve(name);
                 if (cmd!=null)
                     return cmd;
             }
 
             String[] tokens = name.split(":");
             if (tokens.length>1) {
                 ArtifactClassLoaderFactory f = artifactClassLoaderFactoryProvider.get();
 
                 // commands that are not built-in
                 GAV gav = installedPluginList.get(tokens[0]);
                 if (gav != null) {
                     f.add(gav);
                 } else {
                     for (GAV candidate : mapCommandToArtifacts(tokens[0])) {
                         try {
                             f.add(candidate);
                             gav = candidate;
                             break;  // found it!
                         } catch (RepositoryException e) {
                             if (verbose.isVerbose())
                                 e.printStackTrace();
                             // keep on trying the next candidate
                         }
                     }
                     if (gav==null)
                         return null;    // couldn't find it
 
                     installedPluginList.put(tokens[0], gav);
                 }
                 ClassLoader cl = f.createClassLoader(extClassLoader);
                 injector = createChildModule(injector, cl);
             }
             Provider<ICommand> p;
             try {
                 p = injector.getProvider(Key.get(ICommand.class, AnnotationLiteral.of(CLICommand.class, name)));
             } catch (ConfigurationException e) {
                 return null; // failed to find the command
             }
             return p.get();
         } catch (RepositoryException e) {
             throw (IOException)new IOException("Failed to resolve command: "+name).initCause(e);
         } catch (IOException e) {
             throw (IOException)new IOException("Failed to resolve command: "+name).initCause(e);
         } catch (InstantiationException e) {
             throw (IOException)new IOException("Failed to resolve command: "+name).initCause(e);
         }
     }
 
     protected Injector createChildModule(Injector parent, final ClassLoader cl) throws InstantiationException, IOException {
         final List<Module> childModules = new ArrayList<Module>();
         childModules.add(new ExtensionFinder(cl) {
             @Override
             protected <T> void bind(Class<? extends T> impl, Class<T> extensionPoint) {
                 if (impl.getClassLoader()!=cl)  return; // only add newly discovered stuff
 
                 // install CLIModules
                 if (extensionPoint==CLIModule.class) {
                     try {
                         install((Module)impl.newInstance());
                     } catch (InstantiationException e) {
                         throw (Error)new InstantiationError().initCause(e);
                     } catch (IllegalAccessException e) {
                         throw (Error)new IllegalAccessError().initCause(e);
                     }
                     return;
                 }
                 super.bind(impl,extensionPoint);
             }
         });
 
         return parent.createChildInjector(childModules);
     }
 
     /**
      * Controls the mapping from the command set prefix to the artifact.
      *
      * @return
      *      Artifacts that we should try to locate, in the order of preference.
      */
     protected Collection<GAV> mapCommandToArtifacts(String prefix) {
         return Arrays.asList(
             new GAV("com.cloudbees.sdk.plugins", prefix + "-plugin", "LATEST"),
             new GAV("org.cloudbees.sdk.plugins", prefix + "-plugin", "LATEST")
         );
     }
     
     public String getHelp(URL helpTitleFile, String groupHelp, boolean all) {
         StringBuilder sb = new StringBuilder(getHelpTitle(helpTitleFile));
 
         Map<String,List<Binding<?>>> map = new HashMap<String, List<Binding<?>>>();
 
         findCommands(all, map, injector);
 
         for (GAV p : installedPluginList.values()) {
             try {
                 ArtifactClassLoaderFactory f = artifactClassLoaderFactoryProvider.get();
                 f.add(p);
                 ClassLoader cl = f.createClassLoader(extClassLoader);
                 findCommands(all, map, createChildModule(injector, cl));
             } catch (Throwable e) {
                 LOGGER.warning("Failed to load "+p);
             }
             // if we fail to resolve artifacts for a plugin, move on and try to show help for other plugins
         }
 
         for (String group: map.keySet()) {
             sb.append(NL).append(group).append(" ").append(groupHelp).append(NL);
             for (Binding<?> b : map.get(group)) {
                 Class<?> cmd = b.getProvider().get().getClass();
                 sb.append("    ").append(cmd.getAnnotation(CLICommand.class).value());
                 CommandDescription description = cmd.getAnnotation(CommandDescription.class);
                 if (description != null)
                     sb.append("      ").append(description.value()).append(NL);
                 else
                     sb.append(NL);
             }
         }
         return sb.toString();
     }
 
     /**
      * Finds all the commands in the given injector.
      */
     private void findCommands(boolean all, Map<String, List<Binding<?>>> map, Injector injector) {
         for (Binding<?> b : injector.getAllBindings().values()) {
             if (ICommand.class==b.getKey().getTypeLiteral().getRawType()) {
                 Object o = b.getProvider().get();
                 if (o==null)    continue;
 
                 Class<?> cmd = o.getClass();
                 if (!cmd.isAnnotationPresent(CLICommand.class))
                     continue;
                 CommandGroup group = cmd.getAnnotation(CommandGroup.class);
 
                 if (cmd.isAnnotationPresent(Experimental.class) && !all)
                     continue;
 
                 String key = group == null ? "" : group.value();
                 List<Binding<?>> list = map.get(key);
                 if (list == null) {
                     list = new ArrayList<Binding<?>>();
                     map.put(key, list);
                 }
                if (!hasBinding(list, b)) list.add(b);
             }
         }
     }
 
    private boolean hasBinding(List<Binding<?>> list, Binding binding) {
        for (Binding b: list) {
            if (b.getKey().getAnnotation().equals(binding.getKey().getAnnotation()))
                return true;
        }
        return false;
    }

     private StringBuffer getHelpTitle(URL helpTitleFile) {
         StringBuffer sb = new StringBuffer();
         BufferedReader reader = null;
         try {
             reader =  new BufferedReader(new InputStreamReader(helpTitleFile.openStream()));
             String line;
             while (( line = reader.readLine()) != null){
                 sb.append(line).append(NL);
             }
         }
         catch (IOException ex){
             System.err.println("ERROR: Cannot find help file: " + helpTitleFile);
         }
         finally {
             if (reader != null) try {
                 reader.close();
             } catch (IOException ignored) {}
         }
         return sb;
     }
 
     private static final Logger LOGGER = Logger.getLogger(CommandServiceImpl.class.getName());
 }
