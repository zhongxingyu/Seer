 /*
  * This file is part of SearchIds.
  *
  * Copyright © 2012-2013 Visual Illusions Entertainment
  *
  * SearchIds is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License,
  * or (at your option) any later version.
  *
  * SearchIds is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
  * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along with SearchIds.
  * If not, see http://www.gnu.org/licenses/gpl.html.
  */
 package net.visualillusionsent.spout.server.plugin.searchids;
 
 import net.visualillusionsent.searchids.SearchIdsInformation;
 import net.visualillusionsent.searchids.SearchIdsProperties;
 import net.visualillusionsent.utils.VersionChecker;
 import org.spout.api.command.CommandArguments;
 import org.spout.api.command.CommandSource;
 import org.spout.api.command.annotated.Command;
 import org.spout.api.command.annotated.Permissible;
 import org.spout.api.entity.Player;
 import org.spout.api.exception.CommandException;
 
 /**
  * Command Executor for Spout
  * 
  * @author Jason (darkdiplomat)
  */
 public class SpoutSearchCommandExecutor {
 
     private final SpoutSearchIds searchids;
     private final SearchIdsInformation sii;
 
     SpoutSearchCommandExecutor(SpoutSearchIds searchids) {
         this.searchids = searchids;
         this.sii = new SearchIdsInformation(searchids);
     }
 
     @Command(aliases = { "search" }, usage = "<query>", desc = "Searches.")
     @Permissible("searchids.search")
     public void search(CommandSource source, CommandArguments args) throws CommandException {
        if (args.length() > 0) {
            String query = args.getJoinedString(0).trim();
             if (source instanceof Player) {
                 searchids.printSearchResults((Player) source, SpoutSearchIds.parser.search(query, SearchIdsProperties.base), query);
             }
             else {
                 searchids.printConsoleSearchResults(SpoutSearchIds.parser.search(query, SearchIdsProperties.base), query);
             }
         }
         else {
             source.sendMessage("§cCorrect usage is: /search [item to search for]");
         }
     }
 
     @Command(aliases = { "searchids" }, desc = "SearchIds information command.")
     public void information(CommandSource source, CommandArguments args) throws CommandException {
         for (String msg : sii.getAbout()) {
             if (msg.equals("$VERSION_CHECK$")) {
                 VersionChecker vc = searchids.getVersionChecker();
                 Boolean islatest = vc.isLatest();
                 if (islatest == null) {
                     source.sendMessage(sii.center("\u00A77VersionCheckerError: " + vc.getErrorMessage()));
                 }
                 else if (!vc.isLatest()) {
                     source.sendMessage(sii.center("\u00A77" + vc.getUpdateAvailibleMessage()));
                 }
                 else {
                     source.sendMessage(sii.center("\u00A72Latest Version Installed"));
                 }
             }
             else {
                 source.sendMessage(msg);
             }
         }
     }
 }
