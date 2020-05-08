 /**
  * Copyright (C) 2012 Jacob Scott <jascottytechie@gmail.com>
  * Description: Provides methods for getting information about clans or tournament teams
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package me.jascotty2.clanstats;
 
 import java.awt.Desktop;
 import java.io.File;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.JOptionPane;
 import me.jascotty2.lib.io.CheckInput;
 import me.jascotty2.lib.util.ArrayManip;
 import me.jascotty2.lib.util.Str;
 
 public class Main implements GetClan.ScanCallback {
 
 	static Main m = new Main();
 	static long st;
 	static String clan = null;
 	static GetClan c = null;
 	static boolean printDone = false, deamonMode = false;
 	static String saveDir = "saves";
 	static String tournSaveDir = "";
 	static String fileName = "%c[%a].html";
 	static Character[] safeChars = new Character[]{
 		'#', '$', '%', '&', '\'', '(', ')', '*', '+', ',', '-', '.',
 		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ';', '<', '>', '@',
 		'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
 		'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
 		'[', '}', ']', '^', '_', '`',
 		'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
 		'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
 		'{', '|', '}', '~'};
 
 	public static void main(String[] args) {
 		
 		st = System.currentTimeMillis();
 
 		if (args.length > 0) {
 			// command: java -jar ClanStats.jar [options] <clan>
 			// options: -f <output file>	save to file. options: %c -> clan name, %a clan tag
 			//			-d <directory>		change save directory (if -f not defined)
 			//			-u <tournament url>	if is a tournament, what the url for it is
 			//			-t <#>				max number of lookup threads
 			//			--open-on-close		open the file once finished
 			deamonMode = true;
 			saveDir = ".";
 
 			String url = "";
 			boolean fnDef = false;
 			String error = "";
 			int maxThreads = 0;
 
 			for (int i = 0; i < args.length && error.isEmpty(); ++i) {
 				if (args[i].equalsIgnoreCase("-f") || args[i].equalsIgnoreCase("--file")) {
					if (++i >= args.length - 1) {
 						error = "No Filename Defined";
 					} else {
 						fileName = args[i];
 						fnDef = true;
 					}
 				} else if (args[i].equalsIgnoreCase("-d")) {
					if (++i >= args.length - 1) {
 						error = "No Directory Defined";
 					} else {
 						tournSaveDir = saveDir = args[i];
 					}
 				} else if (args[i].equalsIgnoreCase("-u")) {
					if (++i >= args.length - 1) {
 						error = "No URL Defined";
 					} else {
 						url = args[i];
 					}
 				} else if (args[i].equalsIgnoreCase("-c") || args[i].equalsIgnoreCase("--clan")) {
					if (++i >= args.length - 1) {
 						error = "No Clan Search Term Defined";
 					} else {
 						clan = args[i];
 					}
 				} else if (args[i].equalsIgnoreCase("-t") || args[i].equalsIgnoreCase("--max-threads")) {
					if (++i >= args.length - 1) {
 						error = "Missing Threads Argument";
 					} else if (CheckInput.GetInt(args[i], -1) <= 0) {
 						error = "Threads Must be a Positive Number";
 					} else {
 						maxThreads = CheckInput.GetInt(args[i], 0);
 					}
 				} else if (args[i].equalsIgnoreCase("--open-on-close")) {
 					deamonMode = false;
 				} else if (args[i].equalsIgnoreCase("--help") || args[i].equalsIgnoreCase("-h")) {
 					error = "ClanStats Command Help:";
 				} else {
 					if (i == args.length - 1 && clan == null) {
 						clan = args[i];
 					} else {
 						error = "Invalid Option: " + args[i];
 					}
 				}
 			}
 
 			if (error.isEmpty() && clan == null) {
 				error = "No Clan Defined";
 			}
 
 			if (!error.isEmpty()) {
 				System.out.println(error);
 				System.out.println("Command: java -jar ClanStats.jar [options] <clan>");
 				System.out.println("Options: ");
 				System.out.println("         -f <output file> save to file");
 				System.out.println("               formatting: %c -> clan name");
 				System.out.println("                           %a -> clan tag ");
 				System.out.println("         -d <directory>");
 				System.out.println("               change default save directory");
 				System.out.println("         -u <tournament url> ");
 				System.out.println("               if is a tournament, what the url for it is");
 				System.out.println("         -t <#>");
 				System.out.println("               set the max. number of lookup threads (default 20)");
 				System.out.println("          --open-on-close  open the file once finished");
 				return;
 			}
 
 			if (fileName.contains(File.separator)) {
 				// extract dir from filename & append to dir
 				String d = fileName.substring(0, fileName.lastIndexOf(File.separator));
 				if (saveDir.equals(".")) {
 					tournSaveDir = saveDir = d;
 				} else {
 					saveDir += (saveDir.endsWith(File.separator) ? "" : File.separator) + d;
 					tournSaveDir = saveDir;
 				}
 				fileName = fileName.substring(fileName.lastIndexOf(File.separator) + 1);
 			}
 			if (!url.isEmpty() && !fnDef) {
 				fileName = "%c.html";
 			}
 
 			st = System.currentTimeMillis();
 
 			if (!url.isEmpty()) {
 				c = new GetTournamentTeam(url, clan);
 			} else {
 				c = new GetClan(clan);
 			}
 
 			if (maxThreads > 0) {
 				c.max_threads = maxThreads;
 			}
 
 			c.run();
 			if (!c.isFound) {
 				System.out.println("Error: the "
 						+ (c instanceof GetTournamentTeam ? "team" : "clan ")
 						+ "'" + clan + "' was not found");
 				return;
 			}
 		} else {
 			do {
 				clan = JOptionPane.showInputDialog(null, "Clan to Search, or Tournament URL",
 						"Clan Stats by Jacob Scott", JOptionPane.QUESTION_MESSAGE);
 				if (clan == null || clan.length() == 0) {
 					return;
 				}
 				st = System.currentTimeMillis();
 				c = !clan.toLowerCase().startsWith("http://") ? new GetClan(clan)
 						: new GetTournamentTeam(clan, "");//"MAYHM"); //"SNS");//
 				if (c instanceof GetTournamentTeam) {
 					if (((GetTournamentTeam) c).eventID.isEmpty()) {
 						clan = JOptionPane.showInputDialog(null,
 								"<html><span style='font-weight:bold;color:red;'>Tournament '"
 								+ "<span style='font-weight:bold;color:blue;'>" + clan + "</span>'"
 								+ " was not found (was that the correct link?)</span><br />"
 								+ "Clan to Search, or Tournament URL:</html>", "Clan Stats by Jacob Scott", JOptionPane.QUESTION_MESSAGE);
 						if (clan == null || clan.length() == 0) {
 							return;
 						} else {
 							continue;
 						}
 					} else {
 						clan = JOptionPane.showInputDialog(null, "Tournament Team to Search:",
 								"Clan Stats by Jacob Scott", JOptionPane.QUESTION_MESSAGE);
 						if (clan == null || clan.length() == 0) {
 							continue;
 						}
 						st = System.currentTimeMillis();
 						c.searchTag = clan;
 					}
 				}
 				c.run();
 				while (!c.isFound) {
 					clan = JOptionPane.showInputDialog(null,
 							"<html><span style='font-weight:bold;color:red;'>"
 							+ (c instanceof GetTournamentTeam ? "Team" : "Clan")
 							+ " '<span style='font-weight:bold;color:blue;'>" + clan
 							+ "</span>' was not found</span><br />"
 							+ (c instanceof GetTournamentTeam ? "Tournament Team" : "Clan")
 							+ " to Search?</html>", "Clan Stats by Jacob Scott", JOptionPane.QUESTION_MESSAGE);
 					if (clan == null || clan.length() == 0) {
 						break;
 					}
 					st = System.currentTimeMillis();
 					c.searchTag = clan;
 					c.run();
 				}
 				if(!c.isFound && !(c instanceof GetTournamentTeam)) {
 					break;
 				}
 			} while (!c.isFound);
 			if (c instanceof GetTournamentTeam) {
 				fileName = "%c.html";
 			}
 		}
 		c.callback = m;
 		c.lookupPlayers();
 	}
 
 	@Override
 	public void ScanDone() {
 		try {
 			if (c instanceof GetTournamentTeam) {
 				((GetTournamentTeam) c).applyTierLimits();
 			}
 			if (!deamonMode) {
 				long end = System.currentTimeMillis();
 
 				System.out.println("results: (scan completed in " + ((end - st) / 1000) + " seconds)");
 				System.out.println("");
 				System.out.println("name: " + c.clanName);
 				System.out.println("tag: " + c.clanTag);
 				System.out.println("created: " + c.created);
 				System.out.println("members: " + c.numPlayers);
 				System.out.println("owner: " + c.ownerID + "  (" + c.ownerName + ")");
 				System.out.println("clanNum: " + c.clanID);
 				System.out.println("emblem: " + c.emblemURL);
 
 				System.out.println("provinces: " + c.provinces.size());
 				System.out.println("income: " + c.clanIncome);
 
 				System.out.println("total by tier: ");
 				for (int i = 0; i < 10; ++i) {
 					System.out.println((i + 1) + " - " + c.totalTiers[i]);
 				}
 				System.out.println("max by tier: ");
 				for (int i = 0; i < 10; ++i) {
 					System.out.println((i + 1) + " - " + c.maxTiers[i]);
 				}
 
 				System.out.println("potential lineup: "); // (no tier limits)
 				int num = 1;
 				c.sortPlayersByTankAndRating();
 				//for (int tier = 10; tier > 0; --tier) {
 				for (PlayerInfo p : c.players) {
 					StringBuilder line = new StringBuilder(Str.padRight(String.valueOf(num++) + ":", 3)
 							+ Str.padRight(p.playername, 25));
 					line.append("# ").append(Str.padRight(String.valueOf(p.playerRating), 7));
 					line.append("* ").append(Str.padRight(String.valueOf(p.totals.battles), 6)).append(" - ");
 					int n = 10;
 					for (Tank t : p.getSortedTanks()) {
 						line.append(Str.padRight(t.toString(), 25)).append(Str.padLeft(t.effectiveTier() + "(" + p.tankBattles.get(t).battles + ")", 6));
 						if (--n > 0) {
 							line.append(" | ");
 						} else {
 							break;
 						}
 					}
 					System.out.println(line.toString());
 				}
 			}
 
 			/*
 			// output for php test page
 			System.out.println("$players = array(");
 			for (PlayerInfo p : c.players) {
 			StringBuilder line = (new StringBuilder("\t\"")).append(p.playername).append("\" => array (\n");
 			line.append("\t\t\"rating\" => ").append(String.valueOf(p.playerRating)).append(",\n");
 			line.append("\t\t\"battles\" => ").append(String.valueOf(p.totals.battles)).append(",\n");
 			line.append("\t\t\"tanks\" => array(\n\t\t\t");
 			int n  = p.tankBattles.size();
 			for (Tank t : p.getSortedTanks()) {
 			line.append("\"").append(t.name).append("\" => ").append(t.tier);
 			if (--n > 0) {
 			line.append(", ");
 			} else {
 			line.append(")),\n");
 			}
 			}
 			System.out.println(line.toString());
 			}
 			System.out.println("\t);");
 			 */
 			File dir;
 			File f;
 			String safeName = "";
 			if (c.clanName != null) {
 				for (char ch : c.clanName.toCharArray()) {
 					if (ArrayManip.indexOf(safeChars, (Character) ch) != -1) {
 						safeName += ch;
 					}
 				}
 			}
 			String fn = fileName.
 					replace("%c", safeName).
 					replace("%a", c.clanTag == null ? "" : c.clanTag);
 			if (c instanceof GetTournamentTeam) {
 				if (tournSaveDir.isEmpty()) {
 					dir = new File(saveDir + File.separator + "teams"
 							+ File.separator + ((GetTournamentTeam) c).tournamentID
 							+ (((GetTournamentTeam) c).eventName.isEmpty() ? ""
 							: "-" + ((GetTournamentTeam) c).eventName.replace(" ", "_")));
 				} else {
 					dir = new File(tournSaveDir.replace("%i", ((GetTournamentTeam) c).tournamentID).
 							replace("%n", ((GetTournamentTeam) c).eventName.replace(" ", "_")));
 				}
 				dir.mkdirs();
 				f = new File(dir, fn);
 			} else {
 				dir = new File(saveDir);
 				dir.mkdirs();
 				f = new File(dir, fn);
 			}
 
 			if (!deamonMode) {
 				System.out.println("writing to file: " + f.getPath());
 			}
 			if (fn.toLowerCase().endsWith(".htm") || fn.toLowerCase().endsWith(".html")) {
 				OutputHTML.writeFile(c, f, !deamonMode);
 				if (!deamonMode) {
 					System.out.println("opening in default browser..");
 					Desktop.getDesktop().browse(f.toURI());
 				}
 			} else {
 				OutputDat.writeFile(c, f);
 			}
 		} catch (Exception e) {
 			Logger.getAnonymousLogger().log(Level.SEVERE, e.getMessage(), e);
 			JOptionPane.showMessageDialog(null, "Unexpected Error: " + e.getMessage(),
 					"Error", JOptionPane.ERROR_MESSAGE);
 		}
 		printDone = true;
 	}
 }
