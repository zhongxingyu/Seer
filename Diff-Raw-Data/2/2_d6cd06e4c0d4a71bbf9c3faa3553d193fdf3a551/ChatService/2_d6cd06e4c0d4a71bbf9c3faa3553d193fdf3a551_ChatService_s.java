 package de.derflash.plugins.cnwarn.services;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.Iterator;
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 import com.avaje.ebean.SqlRow;
 
 import de.derflash.plugins.cnwarn.model.Warn;
 import de.derflash.plugins.cnwarn.model.Watch;
 
 public class ChatService {
     public void showPlayerNoWarning(Player player) {
         player.sendMessage(ChatColor.GREEN + "Du bist nicht verwarnt! Weiter so!");
     }
 
     public void showPlayerAlreadyAcceptedWarning(Player player) {
         player.sendMessage(ChatColor.GREEN + "Du hast deine Verwarnungen bereits aktzeptiert!");
         player.sendMessage(ChatColor.GREEN + "Benutze " + ChatColor.RED + "/warn info" + ChatColor.GREEN + ", um dir dein Verwarnungen anzuschauen.");
     }
 
     public void showPlayerAcceptedWarning(Player player, String playerName, List<Warn> warnList) {
         showWarnList(player, player.getName(), warnList);
         player.sendMessage(ChatColor.GREEN + "Du hast deine Verwarnung akzeptiert!");
         player.sendMessage(ChatColor.DARK_RED + "Halte dich in Zukunft an unsere Server-Regeln!!!");
     }
 
     public void showWarnList(Player player, String playerName, List<Warn> warnList) {
         player.sendMessage(ChatColor.DARK_RED + "|------------- " + playerName + " Verwarnungen -------------|");
         SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yy 'um' hh:mm 'Uhr'");
         int ratingSum = 0;
         for (Warn warn : warnList) {
             Date createDate = warn.getCreated();
             String created = formatter.format(createDate);
             ratingSum += warn.getRating();
 
             player.sendMessage(ChatColor.YELLOW + "[" + warn.getId() + "] Grund: " + ChatColor.WHITE + warn.getMessage() + " [Stufe " + warn.getRating() + "]");
             player.sendMessage("     " + ChatColor.WHITE + created + " von " + ChatColor.YELLOW + warn.getStaffname() + ChatColor.WHITE + " akzeptiert: "
                     + (warn.getAccepted() == null ? "Nein" : "Ja"));
             if (warn.getAccepted() != null) {
                 GregorianCalendar acceptedDate = new GregorianCalendar();
                 acceptedDate.setTime(warn.getAccepted());
                 acceptedDate.add(Calendar.DAY_OF_MONTH, 30);
                String accepted = formatter.format(acceptedDate);
 
                 player.sendMessage("     " + ChatColor.WHITE + "Verfällt am: " + accepted);
             }
         }
 
         player.sendMessage(ChatColor.RED + "Gesamtpunktzahl: " + ChatColor.WHITE + ratingSum);
     }
 
     public void showStaffAddWarnCorrectDesc(Player player) {
         player.sendMessage(ChatColor.RED + "Der Verwarnungsgrund muss mindestens 5 Zeichen lang sein.");
         player.sendMessage(ChatColor.RED + "/warn [Spielername] [Grund] [Bewertung]");
     }
 
     public void showStaffAddWarnCorrectRating(Player player) {
         player.sendMessage(ChatColor.RED + "Die Bewertung muss eine Zahl zwischen 1 und 6 sein.");
         player.sendMessage(ChatColor.RED + "/warn [Spielername] [Grund] [Bewertung]");
     }
 
     public void showStaffAddWarnCorrectRatingNum(Player player) {
         player.sendMessage(ChatColor.RED + "Die Bewertung muss eine Zahl sein.");
         player.sendMessage(ChatColor.RED + "/warn [Spielername] [Grund] [Bewertung]");
     }
 
     public void showPlayerNewWarning(Player player) {
         player.sendMessage(ChatColor.DARK_RED + "!!! ACHTUNG !!! " + ChatColor.YELLOW + player.getName() + ChatColor.DARK_RED + " DU WURDEST VERWARNT !!!");
         player.sendMessage(ChatColor.DARK_RED + "Du kannst dich jetzt nicht mehr bewegen,");
         player.sendMessage(ChatColor.DARK_RED + "bis du die Verwarnung aktzeptiert hast.");
         player.sendMessage(ChatColor.DARK_RED + "Mit " + ChatColor.GREEN + "/warn info" + ChatColor.DARK_RED + " kannst du dir den Grund ansehen.");
         player.sendMessage(ChatColor.DARK_RED + "Mit " + ChatColor.GREEN + "/warn accept" + ChatColor.DARK_RED + " aktzeptierst du die Verwarnung.");
     }
 
     public void showStaffDelWarnCorrect(Player player) {
         player.sendMessage(ChatColor.RED + "Du musst eine Zahl als Id angeben.");
         player.sendMessage(ChatColor.RED + "/warn del [Id]");
     }
 
     public void showStaffPlayerHasNoWarning(Player player, String playerName) {
         player.sendMessage(ChatColor.RED + playerName + " hat keine Verwarnungen.");
     }
 
     public void showStaffNoWarningForPlayer(Player player, String playerName) {
         player.sendMessage(ChatColor.YELLOW + playerName + ChatColor.GREEN + " wurde bisher noch nicht verwarnt.");
         player.sendMessage(ChatColor.GREEN + "/warn search [Spieler] " + ChatColor.WHITE + ", um nach einem Spielernamen zu suchen.");
     }
 
     public void showStaffSearchWarnCorrect(Player player) {
         player.sendMessage(ChatColor.DARK_RED + "Der Suchbegriff muss mindestens 3 Zeichen enthalten.");
     }
 
     public void showStaffAddWatch(Player player, String playerName) {
         player.sendMessage(ChatColor.AQUA + playerName + " erfolgreich zur Watchlist hinzugefügt!");
     }
 
     public void showStaffAlreadyAddedWatch(Player player, String playerName) {
         player.sendMessage(ChatColor.DARK_RED + "Dieser Spieler steht bereits unter Beobachtung! Siehe: /watch info " + playerName);
     }
 
     public void showStaffPlayerNotWatched(Player player) {
         player.sendMessage(ChatColor.GREEN + "Dieser Spieler wird nicht beobachtet");
     }
 
     public void showStaffDelWatchedPlayer(Player player, String playerName) {
         player.sendMessage(ChatColor.AQUA + playerName + " erfolgreich von der Watchlist entfernt!");
     }
 
     public void showStaffWatchInfo(Player player, Watch watch) {
         player.sendMessage(ChatColor.DARK_RED + "Beobachteter Spieler: " + watch.getPlayername());
         player.sendMessage(ChatColor.AQUA + "Erstellt von " + watch.getStaffname() + " am " + watch.getCreated());
         player.sendMessage(ChatColor.AQUA + "Beschreibung: " + watch.getMessage());
     }
 
     public void showStaffAllWatchedPlayers(Player player, List<Watch> watchs) {
         player.sendMessage(ChatColor.DARK_RED + "Beobachtete Spieler:");
         String playerList = null;
         Iterator<Watch> i = watchs.iterator();
         while (i.hasNext()) {
             Watch watch = i.next();
             if (playerList == null) {
                 playerList = "" + watch.getId() + ":" + watch.getPlayername();
             } else {
                 playerList += ", " + watch.getId() + ":" + watch.getPlayername();
             }
         }
         player.sendMessage(ChatColor.AQUA + playerList);
     }
 
     public void showStaffNoWatchedPlayers(Player player) {
         player.sendMessage(ChatColor.GREEN + "Es werden keine Spieler beobachtet");
     }
 
     public void showStaffJoinWatchedPlayer(Player player, Watch watch) {
         player.sendMessage(ChatColor.DARK_RED + "[CNWarn] " + ChatColor.AQUA + watch.getPlayername() + " steht auf der Watchlist!");
         player.sendMessage(ChatColor.AQUA + "Erstellt: " + watch.getCreated());
         player.sendMessage(ChatColor.AQUA + "Beschreibung: " + watch.getMessage());
     }
 
     public void showStaffNewWarn(Player player, String warnedPlayer, String message, Integer rating, Boolean wasWarned, Integer warnCount, Integer ratingSum) {
         player.sendMessage(ChatColor.GREEN + "Verwarnter Spieler: " + ChatColor.YELLOW + warnedPlayer);
         player.sendMessage(ChatColor.GREEN + "Grund: " + ChatColor.WHITE + message + ChatColor.GREEN + " Stufe: " + ChatColor.WHITE + rating.toString());
 
         if (wasWarned) {
             player.sendMessage(ChatColor.YELLOW + warnedPlayer + ChatColor.RED + " wurde bereits zuvor verwarnt!");
             player.sendMessage(ChatColor.RED + "Verwarnungen: " + ChatColor.WHITE + warnCount.toString() + ChatColor.RED + " | Bewertung: " + ChatColor.WHITE
                     + ratingSum.toString());
         }
     }
 
     public void showStaffOfflinePlayer(Player player, String playerName) {
         player.sendMessage(ChatColor.YELLOW + playerName + ChatColor.RED + " ist offline.");
         player.sendMessage(ChatColor.WHITE + "Bist du sicher, dass du den Namen richtig geschrieben hast?");
         player.sendMessage(ChatColor.YELLOW + "/warn confirm" + ChatColor.GREEN + ", um die Verwarnung zu bestätigen.");
     }
 
     public void showStaffNoConfirmWarning(Player player) {
         player.sendMessage("Es existiert keine offline Verwarnung, die bestätigt werden kann.");
     }
 
     public void showStaffDelWarning(Player player, Integer id) {
         player.sendMessage("Verwarnung mit der Id " + id + " wurde gelöscht.");
     }
 
     public void showStaffDelAllWarning(Player player, String playerName) {
         player.sendMessage("Alle Verwarnungen von " + playerName + " wurden gelöscht.");
     }
 
     public void showStaffSearch(Player player, String playerName) {
         player.sendMessage(ChatColor.DARK_RED + "|--------- Suche nach verwarnten Spielern: " + playerName + " ---------|");
     }
 
     public void showStaffSearchResult(Player player, List<SqlRow> found) {
         if (found.isEmpty()) {
             player.sendMessage(ChatColor.DARK_RED + "Keine Ergebnisse gefunden.");
         } else {
             String out = "";
             for (SqlRow row : found) {
                 String _name = row.getString("playername");
                 if (out.length() == 0) {
                     out = _name;
                 } else {
                     out = out + ", " + _name;
                 }
             }
             player.sendMessage(ChatColor.YELLOW + out);
         }
     }
 
     public void showStaffPlayerNeverPlayedBefore(Player player, String playerName) {
         player.sendMessage(ChatColor.DARK_RED + "Verwarnung nicht angenommen! Der Spieler " + playerName + " war noch nie auf diesem Server.");
     }
 }
