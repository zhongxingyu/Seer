 /* Copyright (c) 2011 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the LGPL 2.1 license, available at the root
  * application directory.
  */
 
 package org.geogit.cli.plumbing;
 
 import java.io.File;
 import java.net.URL;
 
 import jline.console.ConsoleReader;
 
 import org.geogit.api.GeoGIT;
 import org.geogit.api.plumbing.ResolveGeogitDir;
import org.geogit.api.porcelain.AddOp;
 import org.geogit.cli.AbstractCommand;
 import org.geogit.cli.GeogitCLI;
 
 import com.beust.jcommander.Parameter;
 import com.beust.jcommander.Parameters;
 
 /**
 * Many geogit porcelainish commands take mixture of flags (i.e. parameters that begin with a dash
 * -) and parameters meant for the underlying git rev-list command they use internally and flags and
 * parameters for the other commands they use downstream of git rev-list. This command is used to
 * distinguish between them.
 * <p>
 * CLI proxy for {@link AddOp}
  * <p>
  * Usage:
  * <ul>
 * <li> {@code geogit add [-n] [<pattern>...]}
  * </ul>
 * 
 * @see AddOp
  */
 @Parameters(commandNames = "rev-parse", commandDescription = "Resolve parameters according to the arguments")
 public class RevParse extends AbstractCommand {
 
     @Parameter(names = "--resolve-geogit-dir", description = "Check if the current directory is inside a geogit repository and print out the repository location")
     private boolean resolve_geogit_dir;
 
     @Parameter(names = "--is-inside-work-tree", description = "Check if the current directory is inside a geogit repository and print out the repository location")
     private boolean is_inside_work_tree;
 
     @Override
     protected void runInternal(GeogitCLI cli) throws Exception {
         GeoGIT geogit = cli.getGeogit();
         if (null == geogit) {
             geogit = cli.newGeoGIT();
         }
         if (resolve_geogit_dir) {
             resolveGeogitDir(cli.getConsole(), geogit);
         } else if (is_inside_work_tree) {
             isInsideWorkTree(cli.getConsole(), geogit);
         }
 
     }
 
     private void isInsideWorkTree(ConsoleReader console, GeoGIT geogit) throws Exception {
         URL repoUrl = geogit.command(ResolveGeogitDir.class).call();
 
         File pwd = geogit.getPlatform().pwd();
 
         if (null == repoUrl) {
             console.println("Error: not a geogit repository (or any parent) '"
                     + pwd.getAbsolutePath() + "'");
         } else {
             boolean insideWorkTree = !pwd.getAbsolutePath().contains(".geogit");
             console.println(String.valueOf(insideWorkTree));
         }
     }
 
     private void resolveGeogitDir(ConsoleReader console, GeoGIT geogit) throws Exception {
 
         URL repoUrl = geogit.command(ResolveGeogitDir.class).call();
         if (null == repoUrl) {
             File currDir = geogit.getPlatform().pwd();
             console.println("Error: not a geogit dir '"
                     + currDir.getCanonicalFile().getAbsolutePath() + "'");
         } else if ("file:".equals(repoUrl.getProtocol())) {
             console.println(new File(repoUrl.toURI()).getCanonicalFile().getAbsolutePath());
         } else {
             console.println(repoUrl.toExternalForm());
         }
     }
 
 }
