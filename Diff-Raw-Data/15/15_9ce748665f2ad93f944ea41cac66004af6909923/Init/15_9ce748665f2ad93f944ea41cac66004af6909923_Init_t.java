 /* Copyright (c) 2013 OpenPlans. All rights reserved.
  * This code is licensed under the BSD New License, available at the root
  * application directory.
  */
 
 package org.geogit.cli.porcelain;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.List;
 
 import org.geogit.api.GeoGIT;
 import org.geogit.api.plumbing.ResolveGeogitDir;
 import org.geogit.api.porcelain.ConfigException;
 import org.geogit.api.porcelain.InitOp;
 import org.geogit.cli.AbstractCommand;
 import org.geogit.cli.CLICommand;
 import org.geogit.cli.CommandFailedException;
 import org.geogit.cli.GeogitCLI;
 import org.geogit.cli.InvalidParameterException;
 import org.geogit.cli.RequiresRepository;
 import org.geogit.repository.Repository;
 
 import com.beust.jcommander.Parameter;
 import com.beust.jcommander.Parameters;
 import com.google.common.collect.ImmutableList;
 
 /**
  * This command creates an empty geogit repository - basically a .geogit directory with
  * subdirectories for the object, refs, index, and config databases. An initial HEAD that references
  * the HEAD of the master branch is also created.
  * <p>
  * CLI proxy for {@link InitOp}
  * <p>
  * Usage:
  * <ul>
  * <li> {@code geogit init [<directory>]}
  * </ul>
  * 
  * @see InitOp
  */
 @RequiresRepository(false)
 @Parameters(commandNames = "init", commandDescription = "Create an empty geogit repository or reinitialize an existing one")
 public class Init extends AbstractCommand implements CLICommand {
 
     @Parameter(description = "Repository location (directory).", required = false, arity = 1)
     private List<String> location;
 
     @Parameter(names = "--config", description = "Initial configuration values for new repository", required = false, variableArity = true)
     private List<String> config;
 
     private final static List<String> defaultConfiguration =
         ImmutableList.of(
                 "storage.graph", "tinkergraph",
                 "tinkergraph.version", "0.1",
                 "storage.objects", "bdbje",
                 "storage.staging", "bdbje",
                 "bdbje.version", "0.1",
                 "storage.refs", "file",
                 "file.version", "1.0");
 
     /**
      * Executes the init command.
      */
     @Override
     public void runInternal(GeogitCLI cli) throws IOException {
        if (configuration != null && configuration.size() % 2 != 0) {
             throw new InvalidParameterException("Configuration options must all have names and values");
         }
 
         final File repoDir;
         {
             File currDir = cli.getPlatform().pwd();
             if (location != null && location.size() == 1) {
                 String target = location.get(0);
                 File f = new File(target);
                 if (!f.isAbsolute()) {
                     f = new File(currDir, target).getCanonicalFile();
                 }
                 repoDir = f;
                 if (!repoDir.exists() && !repoDir.mkdirs()) {
                     throw new CommandFailedException("Can't create directory "
                             + repoDir.getAbsolutePath());
                 }
             } else {
                 repoDir = currDir;
             }
         }
 
         GeoGIT geogit = null;
         if (cli.getGeogit() == null) {
             geogit = new GeoGIT(cli.getGeogitInjector(), repoDir);
         } else {
             geogit = cli.getGeogit();
         }
 
         ImmutableList.Builder<String> builder = ImmutableList.builder();
         builder.addAll(defaultConfiguration);
         if (config != null) builder.addAll(config);
         List<String> effectiveConfiguration = builder.build();
 
         Repository repository = geogit.command(InitOp.class).setConfig(effectiveConfiguration).call();
         final boolean repoExisted = repository == null;
         geogit.setRepository(repository);
         cli.setGeogit(geogit);
 
         final URL envHome = geogit.command(ResolveGeogitDir.class).call();
 
         File repoDirectory;
         try {
             repoDirectory = new File(envHome.toURI());
         } catch (URISyntaxException e) {
             throw new IllegalStateException("Environment home can't be resolved to a directory", e);
         }
         String message;
         if (repoExisted) {
             message = "Reinitialized existing Geogit repository in "
                     + repoDirectory.getAbsolutePath();
         } else {
             message = "Initialized empty Geogit repository in " + repoDirectory.getAbsolutePath();
         }
         cli.getConsole().println(message);
     }
 }
