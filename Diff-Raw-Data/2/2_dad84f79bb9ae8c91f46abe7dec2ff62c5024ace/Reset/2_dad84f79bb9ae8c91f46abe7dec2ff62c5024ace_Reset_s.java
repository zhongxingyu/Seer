 /* Copyright (c) 2011 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the LGPL 2.1 license, available at the root
  * application directory.
  */
 
 package org.geogit.cli.porcelain;
 
 import static com.google.common.base.Preconditions.checkState;
 
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.List;
 
 import org.geogit.api.GeoGIT;
 import org.geogit.api.ObjectId;
 import org.geogit.api.plumbing.DiffWorkTree;
 import org.geogit.api.plumbing.RevParse;
 import org.geogit.api.plumbing.diff.DiffEntry;
 import org.geogit.api.plumbing.diff.DiffEntry.ChangeType;
 import org.geogit.api.porcelain.ResetOp;
 import org.geogit.api.porcelain.ResetOp.ResetMode;
 import org.geogit.cli.AbstractCommand;
 import org.geogit.cli.CLICommand;
 import org.geogit.cli.GeogitCLI;
 
 import com.beust.jcommander.Parameter;
 import com.beust.jcommander.Parameters;
 import com.google.common.base.Optional;
 import com.google.common.base.Suppliers;
 
 /**
  * There are two forms of geogit reset. In the first form, copy entries from {@code <commit>} to the
  * index. In the second form, set the current branch head (HEAD) to {@code <commit>}, optionally
  * modifying index and working tree to match. The {@code <commit>} defaults to HEAD in both forms.
  * <p>
  * {@code geogit reset [<commit>] --path <path>...}
  * <p>
  * This form resets the index entries for all {@code <path>} to their state at {@code <commit>}. (It
  * does not affect the working tree, nor the current branch.)
  * <p>
  * {@code geogit reset --(hard|soft|mixed) [<commit>]}
  * <p>
  * This form resets the current branch head to {@code <commit>} and possibly updates the index
  * (resetting it to the tree of {@code <commit>}) and the working tree depending on {@code <mode>},
  * which must be one of the following:
  * <p>
  * {@code --soft} Does not touch the index file nor the working tree at all (but resets the head to
  * {@code <commit>}, just like all modes do). This leaves all your changed files
  * "Changes to be committed", as {@code geogit status} would put it.
  * <p>
  * {@code --mixed} Resets the index but not the working tree (i.e., the changed files are preserved
  * but not marked for commit) and reports what has not been updated. This is the default action.
  * <p>
  * {@code --hard} Resets the index and working tree. Any changes to tracked files in the working
  * tree since {@code <commit>} are discarded.
  * <p>
  * CLI proxy for {@link ResetOp}
  * <p>
  * Usage:
  * <ul>
  * <li> {@code geogit reset [<commit>] --path <path>...}
  * <li> {@code geogit reset --(hard|soft|mixed) [<commit>]}
  * </ul>
  * 
  * @author jgarrett
  * @see ResetOp
  */
@Parameters(commandNames = { "reset" }, commandDescription = "Forward-port local commits to the updated upstream head")
 public class Reset extends AbstractCommand implements CLICommand {
 
     @Parameter(names = { "--hard" }, description = "Resets the index and the working tree.")
     private boolean hard;
 
     @Parameter(names = { "--mixed" }, description = "Resets the index, but not the working tree.")
     private boolean mixed;
 
     @Parameter(names = { "--soft" }, description = "Does not affect index or working tree.")
     private boolean soft;
 
     @Parameter(description = "[<commit>]", arity = 1)
     private List<String> commit;
 
     @Parameter(names = { "-p", "--path" }, description = "<path>...", variableArity = true)
     private List<String> args;
 
     /**
      * Executes the reset command using the provided options.
      * 
      * @param cli
      * @see org.geogit.cli.AbstractCommand#runInternal(org.geogit.cli.GeogitCLI)
      */
     @Override
     public void runInternal(GeogitCLI cli) {
         final GeoGIT geogit = cli.getGeogit();
         checkState(geogit != null, "Not in a geogit repository.");
 
         ResetMode mode = resolveResetMode();
 
         ResetOp reset = cli.getGeogit().command(ResetOp.class);
 
         for (int i = 0; args != null && i < args.size(); i++) {
             reset.addPattern(args.get(i));
         }
 
         if (commit != null && commit.size() > 0) {
             Optional<ObjectId> commitId = geogit.command(RevParse.class).setRefSpec(commit.get(0))
                     .call();
             checkState(commitId.isPresent(), "Commit could not be resolved.");
             reset.setCommit(Suppliers.ofInstance(commitId.get()));
         }
 
         reset.setMode(mode);
 
         reset.call();
 
         final long countUnstaged = geogit.getRepository().getWorkingTree().countUnstaged(null);
         if (countUnstaged > 0) {
             try {
                 Iterator<DiffEntry> unstaged = geogit.command(DiffWorkTree.class).setFilter(null)
                         .call();
                 cli.getConsole().println("Unstaged changes after reset:");
                 while (unstaged.hasNext()) {
                     DiffEntry entry = unstaged.next();
                     ChangeType type = entry.changeType();
                     switch (type) {
                     case ADDED:
                         cli.getConsole().println("A\t" + entry.newPath());
                         break;
                     case MODIFIED:
                         cli.getConsole().println("M\t" + entry.newPath());
                         break;
                     case REMOVED:
                         cli.getConsole().println("D\t" + entry.oldPath());
                         break;
                     }
                 }
             } catch (IOException e) {
 
             }
         }
 
     }
 
     private ResetMode resolveResetMode() {
         ResetMode mode = ResetMode.NONE;
         if (hard) {
             mode = ResetMode.HARD;
         }
         if (mixed) {
             if (mode != ResetMode.NONE) {
                 throw new IllegalArgumentException("You may only specify one mode.");
             }
             mode = ResetMode.MIXED;
         }
         if (soft) {
             if (mode != ResetMode.NONE) {
                 throw new IllegalArgumentException("You may only specify one mode.");
             }
             mode = ResetMode.SOFT;
         }
         return mode;
     }
 
 }
