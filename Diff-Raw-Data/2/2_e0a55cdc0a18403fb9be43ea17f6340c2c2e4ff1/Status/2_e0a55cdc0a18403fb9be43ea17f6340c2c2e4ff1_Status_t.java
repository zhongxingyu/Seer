 /*
  *    GeoTools - The Open Source Java GIS Toolkit
  *    http://geotools.org
  *
  *    (C) 2002-2011, Open Source Geospatial Foundation (OSGeo)
  *
  *    This library is free software; you can redistribute it and/or
  *    modify it under the terms of the GNU Lesser General Public
  *    License as published by the Free Software Foundation;
  *    version 2.1 of the License.
  *
  *    This library is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  *    Lesser General Public License for more details.
  */
 package org.geogit.cli.porcelain;
 
 import static com.google.common.base.Preconditions.checkArgument;
 import static com.google.common.base.Preconditions.checkState;
 
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.List;
 
 import jline.console.ConsoleReader;
 
 import org.fusesource.jansi.Ansi;
 import org.fusesource.jansi.Ansi.Color;
 import org.geogit.api.DiffEntry;
 import org.geogit.api.DiffEntry.ChangeType;
 import org.geogit.api.GeoGIT;
 import org.geogit.cli.AnsiDecorator;
 import org.geogit.cli.CLICommand;
 import org.geogit.cli.GeogitCLI;
 import org.geogit.storage.StagingDatabase;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Service;
 
 import com.beust.jcommander.Parameter;
 import com.beust.jcommander.Parameters;
 
 @Service
 @Scope(value = "prototype")
 @Parameters(commandNames = "status", commandDescription = "Show the working tree status")
 public class Status implements CLICommand {
 
     @Parameter(names = "--color", description = "Whether to apply colored output. Possible values are auto|never|always.", converter = ColorArg.Converter.class)
     private ColorArg coloredOutput = ColorArg.auto;
 
     @Parameter(names = "--limit", description = "Limit number of displayed changes. Must be >= 0.")
     private Integer limit = 50;
 
     @Parameter(names = "--all", description = "Force listing all changes (overrides limit).")
     private boolean all = false;
 
     public void setColor(ColorArg strategy) {
         this.coloredOutput = strategy;
     }
 
     public void setAll(boolean all) {
         this.all = all;
     }
 
     public void setLimit(Integer limit) {
         checkArgument(limit == null || limit.intValue() >= 0);
         this.limit = limit;
     }
 
     /**
      * @param cli
      * @see org.geogit.cli.CLICommand#run(org.geogit.cli.GeogitCLI)
      */
     @Override
     public void run(GeogitCLI cli) throws Exception {
         checkState(cli.getGeogit() != null, "Not a geogit repository: " + cli.getPlatform().pwd());
 
         ConsoleReader console = cli.getConsole();
         GeoGIT geogit = cli.getGeogit();
 
         final StagingDatabase indexDb = geogit.getRepository().getIndex().getDatabase();
 
         List<String> pathFilter = null;
         final int countStaged = indexDb.countStaged(pathFilter);
         final int countUnstaged = indexDb.countUnstaged(pathFilter);
 
         console.println("# On branch <can't know yet>");
 
         if (countStaged == 0 && countUnstaged == 0) {
             console.println("nothing to commit (working directory clean)");
             return;
         }
 
         if (countStaged > 0) {
             Iterator<DiffEntry> staged = indexDb.getStaged(pathFilter);
 
             console.println("# Changes to be committed:");
             console.println("#   (use \"geogit reset HEAD <path/to/fid>...\" to unstage)");
 
             console.println("#");
 
             print(console, staged, Color.GREEN, countStaged);
 
             console.println("#");
         }
 
         if (countUnstaged > 0) {
             Iterator<DiffEntry> unstaged = indexDb.getUnstaged(pathFilter);
             console.println("# Changes not staged for commit:");
             console.println("#   (use \"geogit add <path/to/fid>...\" to update what will be committed");
             console.println("#   (use \"geogit checkout -- <path/to/fid>...\" to discard changes in working directory");
             console.println("#");
            print(console, unstaged, Color.RED, countUnstaged);
         }
     }
 
     private void print(final ConsoleReader console, final Iterator<DiffEntry> changes,
             final Color color, final int total) throws IOException {
 
         final int limit = all || this.limit == null ? Integer.MAX_VALUE : this.limit.intValue();
 
         StringBuilder sb = new StringBuilder();
 
         boolean useColor;
         switch (this.coloredOutput) {
         case never:
             useColor = false;
             break;
         case always:
             useColor = true;
             break;
         default:
             useColor = console.getTerminal().isAnsiSupported();
         }
 
         Ansi ansi = AnsiDecorator.newAnsi(useColor, sb);
 
         DiffEntry entry;
         ChangeType type;
         List<String> path;
         int cnt = 0;
         while (changes.hasNext() && cnt < limit) {
             ++cnt;
 
             entry = changes.next();
             type = entry.getType();
             path = entry.getPath();
 
             sb.setLength(0);
             ansi.a("#      ").fg(color).a(type.toString().charAt(0)).a("  ").a(path).reset();
             console.println(ansi.toString());
         }
 
         sb.setLength(0);
         ansi.a("# ").a(total).reset().a(" total.");
         console.println(ansi.toString());
     }
 
 }
