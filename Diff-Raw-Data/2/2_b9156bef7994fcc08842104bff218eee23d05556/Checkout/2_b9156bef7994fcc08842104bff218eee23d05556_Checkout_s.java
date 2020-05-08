 /* Copyright (c) 2011 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the LGPL 2.1 license, available at the root
  * application directory.
  */
 
 package org.geogit.cli.porcelain;
 
 import static com.google.common.base.Preconditions.checkArgument;
 import static com.google.common.base.Preconditions.checkState;
 
 import java.util.List;
 
 import jline.console.ConsoleReader;
 
 import org.geogit.api.GeoGIT;
 import org.geogit.api.porcelain.CheckoutException;
 import org.geogit.api.porcelain.CheckoutOp;
 import org.geogit.api.porcelain.CheckoutResult;
 import org.geogit.cli.AbstractCommand;
 import org.geogit.cli.CLICommand;
 import org.geogit.cli.GeogitCLI;
 
 import com.beust.jcommander.Parameter;
 import com.beust.jcommander.Parameters;
 import com.google.common.collect.Lists;
 
 /**
  * This command checks out a branch into the working tree. Checkout also updates HEAD to set the
  * specified branch as the current branch. This command can also be used to discard local changes if
  * used with force option.
  * <p>
  * When used with the {@code -p} option and path names are given it will update those paths in the
  * working tree from the index tree if {@code <branchOrCommitName>} isn't given otherwise it will
  * update from that tree. Note that this doesn't switch what branch you are on.
  * <p>
  * CLI proxy for {@link CheckoutOp}
  * <p>
  * Usage:
  * <ul>
  * <li> {@code geogit checkout [-f] [<branchName>]}
  * <li> {@code geogit checkout [<branchOrCommitName>] [-p <paths>...]}
  * </ul>
  * 
  * @see CheckoutOp
  */
 @Parameters(commandNames = "checkout", commandDescription = "Checkout a branch or paths to the working tree")
 public class Checkout extends AbstractCommand implements CLICommand {
 
     @Parameter(arity = 1, description = "<branch|commit>")
     private List<String> branchOrStartPoint = Lists.newArrayList();
 
     @Parameter(names = { "--force", "-f" }, description = "When switching branches, proceed even if the index or the "
             + "working tree differs from HEAD. This is used to throw away local changes.")
     private boolean force = false;
 
     @Parameter(names = { "--path", "-p" }, description = "Don't switch branches just update the named paths in the "
             + "working tree from the index tree or a named treeish object.", variableArity = true)
     private List<String> paths = Lists.newArrayList();
 
     @Parameter(names = "--ours", description = "When checking out paths from the index, check out 'ours' version for unmerged paths")
     private boolean ours;
 
     @Parameter(names = "--theirs", description = "When checking out paths from the index, check out 'theirs' version for unmerged paths")
     private boolean theirs;
 
     @Override
     public void runInternal(GeogitCLI cli) throws Exception {
         final GeoGIT geogit = cli.getGeogit();
         checkState(geogit != null, "not in a geogit repository.");
         checkArgument(branchOrStartPoint.size() != 0 || !paths.isEmpty(),
                 "no branch or paths specified");
         checkArgument(branchOrStartPoint.size() < 2, "too many arguments");
 
         try {
             final ConsoleReader console = cli.getConsole();
             String branchOrCommit = (branchOrStartPoint.size() > 0 ? branchOrStartPoint.get(0)
                     : null);
 
             CheckoutResult result = geogit.command(CheckoutOp.class).setForce(force)
                     .setSource(branchOrCommit).addPaths(paths).setOurs(ours).setTheirs(theirs)
                     .call();
 
             switch (result.getResult()) {
             case CHECKOUT_LOCAL_BRANCH:
                 console.println("Switched to branch '" + result.getNewRef().localName() + "'");
                 break;
             case CHECKOUT_REMOTE_BRANCH:
                 console.println("Branch '" + result.getNewRef().localName()
                         + "' was set up to track remote branch '" + result.getNewRef().localName()
                         + "from " + result.getRemoteName() + ".");
                 console.println("Switched to a new branch '" + result.getNewRef().localName() + "'");
                 break;
             case UPDATE_OBJECTS:
                 console.println("Objects in the working tree were updated to the specifed version.");
                 break;
             case DETACHED_HEAD:
                 console.println("You are in 'detached HEAD' state. HEAD is now at "
                         + result.getOid().toString().substring(0, 7) + "...");
                 break;
             default:
                 break;
             }
         } catch (CheckoutException e) {
             switch (e.statusCode) {
             case LOCAL_CHANGES_NOT_COMMITTED:
                 cli.getConsole()
                         .println(
                                "Doing a checkout without a clean working tree and index is currently unsupported.");
                 break;
             case UNMERGED_PATHS:
                 cli.getConsole().println(e.getMessage());
             }
         }
     }
 
 }
