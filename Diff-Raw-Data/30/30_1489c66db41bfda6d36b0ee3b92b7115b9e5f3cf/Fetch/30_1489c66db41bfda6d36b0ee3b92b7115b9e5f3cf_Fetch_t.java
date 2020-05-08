 /* Copyright (c) 2011 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the LGPL 2.1 license, available at the root
  * application directory.
  */
 
 package org.geogit.cli.porcelain;
 
 import static com.google.common.base.Preconditions.checkState;
 
 import java.util.List;
 import java.util.Map.Entry;
 
 import jline.console.ConsoleReader;
 
 import org.geogit.api.porcelain.FetchOp;
 import org.geogit.api.porcelain.FetchResult;
 import org.geogit.api.porcelain.FetchResult.ChangedRef;
 import org.geogit.api.porcelain.FetchResult.ChangedRef.ChangeTypes;
 import org.geogit.cli.AbstractCommand;
 import org.geogit.cli.CLICommand;
 import org.geogit.cli.GeogitCLI;
 
 import com.beust.jcommander.Parameter;
 import com.beust.jcommander.Parameters;
 
 /**
  * Fetches named heads or tags from one or more other repositories, along with the objects necessary
  * to complete them.
  * <p>
  * {@code geogit fetch} can fetch from either a single named repository, or from several
  * repositories at once.
  * <p>
  * CLI proxy for {@link FetchOp}
  * <p>
  * Usage:
  * <ul>
  * <li> {@code geogit fetch [<options>] [<repository>...]}
  * </ul>
  * 
  * @see FetchOp
  */
 @Parameters(commandNames = "fetch", commandDescription = "Download objects and refs from another repository")
 public class Fetch extends AbstractCommand implements CLICommand {
 
     @Parameter(names = "--all", description = "Fetch from all remotes.")
     private boolean all = false;
 
     @Parameter(names = { "-p", "--prune" }, description = "After fetching, remove any remote-tracking branches which no longer exist on the remote.")
     private boolean prune = false;
 
     @Parameter(description = "[<repository>...]")
     private List<String> args;
 
     /**
      * Executes the fetch command using the provided options.
      * 
      * @param cli
      * @see org.geogit.cli.AbstractCommand#runInternal(org.geogit.cli.GeogitCLI)
      */
     @Override
     public void runInternal(GeogitCLI cli) throws Exception {
         checkState(cli.getGeogit() != null, "Not a geogit repository: " + cli.getPlatform().pwd());
 
         FetchOp fetch = cli.getGeogit().command(FetchOp.class);
         fetch.setProgressListener(cli.getProgressListener());
         fetch.setAll(all).setPrune(prune);
 
         if (args != null) {
             for (String repo : args) {
                 fetch.addRemote(repo);
             }
         }
 
         FetchResult result = fetch.call();
         ConsoleReader console = cli.getConsole();
         if (result.getChangedRefs().entrySet().size() > 0) {
             for (Entry<String, List<ChangedRef>> entry : result.getChangedRefs().entrySet()) {
                 console.println("From " + entry.getKey());
 
                 for (ChangedRef ref : entry.getValue()) {
                     String line;
                     if (ref.getType() == ChangeTypes.CHANGED_REF) {
                         line = "   " + ref.getOldRef().getObjectId().toString().substring(0, 7)
                                 + ".." + ref.getNewRef().getObjectId().toString().substring(0, 7)
                                 + "     " + ref.getOldRef().localName() + " -> "
                                 + ref.getOldRef().getName();
                     } else if (ref.getType() == ChangeTypes.ADDED_REF) {
                        line = " * [new branch]     " + ref.getNewRef().localName() + " -> "
                                + ref.getNewRef().getName();
                     } else {
                         line = " x [deleted]        (none) -> " + ref.getOldRef().getName();
                     }
                     console.println(line);
                 }
             }
         } else {
             console.println("Already up to date.");
         }
     }
 }
