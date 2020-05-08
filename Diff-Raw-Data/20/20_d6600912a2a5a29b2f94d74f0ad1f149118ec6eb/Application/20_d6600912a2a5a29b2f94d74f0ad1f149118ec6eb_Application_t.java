 package controllers;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.TreeMap;
 
 import play.*;
 import play.libs.Comet;
 import play.mvc.*;
 import play.mvc.Http.*;
 import scala.actors.threadpool.Arrays;
 
 import views.html.*;
 
 import models.*;
 
 public class Application extends Controller {
   
 	public static final String CD_CONTENT_PATH = "." + File.separator + "resources" + File.separator + "CDContenu";
 	//public static final String CD_CONTENT_PATH = ".";
 	public static final String YEAR_FILE_PATH = "." + File.separator + "resources" + File.separator + "year.txt";
 	public static final String GAMES_PATH = CD_CONTENT_PATH + File.separator + "LesLogiciels" + File.separator;
 	public static final String GAMES_INFOS_PATH = CD_CONTENT_PATH + File.separator + "GamesInfos" + File.separator;
 	
   public static int getYear() {
 	  Integer year = 0;
 	  
 	  try{
           BufferedReader br = new BufferedReader(new FileReader(YEAR_FILE_PATH));
           String line;
           try{
               line = br.readLine();
 
               year = Integer.parseInt(line);
 
               br.close();
           }
           catch(IOException ioe){
           System.out.println("/!\\ Error while reading file \n");}
       }
       catch(FileNotFoundException fnfe){
           System.out.println("/!\\ File doesn't exists\n");
       }
       
       return year;
   }
   
   public static void copyInfosFiles() {
 	  boolean doCopy = true;
 	  
 	  File gamesInfosFolder = new File(GAMES_INFOS_PATH);
 	  
 	  if(gamesInfosFolder.exists() && gamesInfosFolder.isDirectory()) {
 		  if(gamesInfosFolder.listFiles().length > 0) {
 			  doCopy = false;
 		  }
 	  }
 	  
 	  if(doCopy) {
 		  // création de répertoire et copie des infos.xml
 		  
 		  
 		  // FileUtils.copy(new File("." + File.separator + "lib" + File.separator + "mac"), new File(this.getInstallationFolder() + File.separator + "lib" + File.separator));
 	  }
   }
 	
   public static Result index() {
 	  int year = getYear();
       
 	  copyInfosFiles();
 	  
       return ok(index.render("Bienvenue dans l'installation du CD DeViNT " + year + " !", year));
   }
   
   public static Result licence() {
 	 int year = getYear();
 	  
 	 return ok(licence.render("Les projets DeViNT sont sous licence !", year));
   }
   
   public static Result config(String uninstall) {
 	  int year = getYear();
 	  
 	  session("uninstall", uninstall.equals("uninstall")+"");
 	  
 	  String defaultInstallFolder = "";
 	  
 	  if(OSValidator.isWindows()) {
 			defaultInstallFolder = "C:" + File.separator + "Program Files" + File.separator + "DeViNT" + File.separator;
 			if(OSValidator.isWindowsSeven()) {
 				defaultInstallFolder = System.getProperty("user.home") + File.separator + "DeViNT" + File.separator;
 			}
 		} else if(OSValidator.isUnix()) {
 			defaultInstallFolder = File.separator + "usr" + File.separator + "DeViNT" + File.separator;
 		} else if(OSValidator.isMac()) {
 			defaultInstallFolder = File.separator + "Applications" + File.separator + "DeViNT" + File.separator;
 		}
 	  if (session("uninstall") != null && session("uninstall").equals("true"))
 		return ok(config.render("Configuration de la désinstallation", year, defaultInstallFolder));
 	  else
 		return ok(config.render("Configuration de l'installation", year, defaultInstallFolder));
   }
   
   public static Result gamechoices(String sortedMethod) {
 	  int year = getYear();
 	  
 	  RequestBody body = request().body();
 	  Map<String,String[]> args = body.asFormUrlEncoded();
 	  
 	  if(args != null && args.keySet().contains("installfolder")) {
 		  session("installfolder", args.get("installfolder")[0]);
 	  }
 	  
 	  ArrayList<Game> games = Game.getAll(GAMES_PATH);
 	  
 	  if(session("uninstall") != null && session("uninstall").equals("true"))
 		games = Game.getAll(session("installfolder"));
 	  
 	  if (session("uninstall") != null && session("uninstall").equals("true") && games == null)
 		return ok(end.render("Il n'y a rien à supprimer!", year, true));
 	  
 	  Collections.sort(games, new YearGameSortComparator());
 	  String htmlGameList = getGamesYearRepresentation(games);
 	  
 	  if(sortedMethod.equals("category")) {
 		  htmlGameList = getGamesCategoryRepresentation(getSortedbyCategory(games));
 	  }
 	  if (session("uninstall") != null && session("uninstall").equals("true"))
 		return ok(gamechoices.render("Choix des projets à déinstaller", year, sortedMethod, htmlGameList));
 	  else
 	    return ok(gamechoices.render("Choix des projets à installer", year, sortedMethod, htmlGameList));
   }
   
   public static TreeMap<GameCategory, ArrayList<Game>> getSortedbyCategory(ArrayList<Game> games) {
 	  
 	  TreeMap<GameCategory, ArrayList<Game>> catGames = new TreeMap<GameCategory, ArrayList<Game>>();
 	  
 	    for(Game g: games) {
 			for(GameCategory gameCat: g.getGameCategories()) {
 				if(!catGames.containsKey(gameCat)) {
 					catGames.put(gameCat, new ArrayList<Game>());
 				}
 				catGames.get(gameCat).add(g);
 			}
 		}
 		
 		return catGames;
   }
   
   public static String getGamesYearRepresentation(ArrayList<Game> games) {
 	  String html = "";
 	  
 	  
 	  int currentYear = 0;
 	  
 	  for(Game g : games) {
 		if(currentYear != g.getAnnee()) {
 			if(currentYear != 0) {
 				html += "</ul></li>";
 			}
 			html += "<li class=\"active\">";
 			html += "<a><label class=\"checkbox\"><input type=\"checkbox\" class=\"game\" id=\"game_" + g.getAnnee() + "_all\" onchange=\"javascript:selectAll('game_" + g.getAnnee() + "')\" /></label>&nbsp;&nbsp;" + g.getAnnee() + "</a>";
 			html += "<ul>";
 			currentYear = g.getAnnee();
 		}
 	  
 		html += "<li><label class=\"checkbox\">";
 		html += "<input type=\"checkbox\" class=\"game game_" + g.getAnnee() + "\" name=\"games\" value=\"" + g.getGameRep() + "\" /> " + g.getTitle();
 		html += "</label></li>";
 	  }
 	  html += "</ul></li>";
 	  
 	  return html;
   }
   
   public static String getGamesCategoryRepresentation(TreeMap<GameCategory, ArrayList<Game>> catGames) {
 	  String html = "";
 	  
 	  for(GameCategory gameCat: catGames.keySet()) {
 		  String clearCatName = gameCat.toString().toLowerCase().replaceAll(" ", "");
 			html += "<li class=\"active\">";
 			html += "<a><label class=\"checkbox\"><input type=\"checkbox\" class=\"game\" id=\"game_" + clearCatName + "_all\" onchange=\"javascript:selectAll('game_" + clearCatName + "')\" /></label>&nbsp;&nbsp;" + gameCat + "</a>";
 			html += "<ul>";
 			
 			for(Game g: catGames.get(gameCat)) {
 				String clearGameTitle = g.getTitle().toLowerCase().replaceAll(" ", "");
 				html += "<li><label class=\"checkbox\">";
 				html += "<input type=\"checkbox\" class=\"game game_" + clearCatName + " game_" + clearGameTitle + "\" onchange=\"javascript:selectGame(this, 'game_" + clearGameTitle + "')\" name=\"games\" value=\"" + g.getGameRep() + "\" /> " + g.getTitle();
 				html += "</label></li>";
 			}
 		
 			html += "</ul></li>";
 	  }
 	  
 	  
 	  return html;
   }
   
   public static Result installation() {
 	  int year = getYear();
 		  
 	  RequestBody body = request().body();
 	  Map<String,String[]> args = body.asFormUrlEncoded();
 	  
 	  String selectedGamesString = "";
 	  
 	  if(args != null && args.keySet().contains("games")) {  
 		  String[] selectedGames = args.get("games");
 		  for(String sg : selectedGames) {
 			  selectedGamesString += sg + "_SG_"; 
 		  }
 	  }
 	  
 	  session("selectedGames", selectedGamesString);
 	  if (session("uninstall") != null && session("uninstall").equals("true"))
 		return ok(installation.render("Désinstallation: effacement des fichiers", year));
 	  else
 		return ok(installation.render("Installation: copie des fichiers", year));
   }
   
   public static Result comet() {
 	  final String installationFolder = session("installfolder");
 	  final String[] selectedGames = session("selectedGames").split("_SG_");
 	  if (session("uninstall") != null && session("uninstall").equals("true")) {
 		  Comet comet = new Comet("parent.cometMessage") {
 			  public void onConnected() {	  
 					launchUninstall(this, installationFolder, selectedGames);
 				}
 		  };
 		  return ok(comet);
 	  } else {
 		  Comet comet = new Comet("parent.cometMessage") {
 			  public void onConnected() {	  
 					launchCopy(this, installationFolder, selectedGames);
 				}
 		  };
 		  return ok(comet);
 	  }
   }
   
   public static void launchCopy(Comet comet, String installationFolder, String[] selectedGames) {  
 	  	long startCopyTime = System.currentTimeMillis();
 		
 		// launchUninstall(); sendMessage("bar")
 		
 		ArrayList<Game> games = Game.getAll(GAMES_PATH);
 		
 		ArrayList<Game> toInstall = new ArrayList<Game>();
 		
 		for(Game g : games) {
 			for(String sg : selectedGames) {
 				if(sg.equals(g.getGameRep().toString())) {
 					toInstall.add(g);
 				}
 			}
 		}
 		
 		if (toInstall.size() != 0) {
 			File installDir =  new File(installationFolder);
 			if(!installDir.exists()) {
 				installDir.mkdirs();
 			}
 			
 			File idfile = new File(installationFolder+File.separator+"installationid");
 			if (!idfile.exists()) {
 				try {
 					FileWriter fw = new FileWriter(idfile);
 					fw.write(""+installationFolder.hashCode());
 					fw.flush();
 					fw.close();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 			
 			/* Copie du bon répertoire Jre */
 			if (!new File(installationFolder + File.separator + "jre" + File.separator).exists()) {
 				if(OSValidator.isWindows()) {
 					try {
 						comet.sendMessage("info: Copie du répertoire \"jre\" et de ses sous-répertoires");
 						FileUtils.copy(new File(CD_CONTENT_PATH + File.separator + "jre" + File.separator + "win"), new File(installationFolder + File.separator + "jre" + File.separator));
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				} else if(OSValidator.isUnix()) {
 					try {
 						comet.sendMessage("info: Copie du répertoire \"jre\" et de ses sous-répertoires");
 						FileUtils.copy(new File(CD_CONTENT_PATH + File.separator + "jre" + File.separator + "linux"), new File(installationFolder + File.separator + "jre" + File.separator));
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				} else if(OSValidator.isMac()) {
 					try {
 						comet.sendMessage("info: Copie du répertoire \"jre\" et de ses sous-répertoires");
 						FileUtils.copy(new File(CD_CONTENT_PATH + File.separator + "jre" + File.separator + "mac"), new File(installationFolder + File.separator + "jre" + File.separator));
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 			}
 			
 			/* Copie du bon répertoire Lib */
 			if (!new File(installationFolder + File.separator + "lib" + File.separator).exists()) {
 				if(OSValidator.isWindows()) {
 					try {
 						comet.sendMessage("info: Copie du répertoire \"lib\" et de ses sous-répertoires");
 						FileUtils.copy(new File(CD_CONTENT_PATH + File.separator + "lib" + File.separator + "win"), new File(installationFolder + File.separator + "lib" + File.separator));
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				} else if(OSValidator.isUnix()) {
 					try {
 						comet.sendMessage("info: Copie du répertoire \"lib\" et de ses sous-répertoires");
 						FileUtils.copy(new File(CD_CONTENT_PATH + File.separator + "lib" + File.separator + "linux"), new File(installationFolder + File.separator + "lib" + File.separator));
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				} else if(OSValidator.isMac()) {
 					try {
 						comet.sendMessage("info: Copie du répertoire \"lib\" et de ses sous-répertoires");
 						FileUtils.copy(new File(CD_CONTENT_PATH + File.separator + "lib" + File.separator + "mac"), new File(installationFolder + File.separator + "lib" + File.separator));
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 			}
 			
 			/* Copie de DListor */
 			if (!new File(installationFolder + File.separator + "DListor" + File.separator).exists()) {
 				try {
 					comet.sendMessage("info: Copie du répertoire \"DListor\" et de ses sous-répertoires");
 					FileUtils.copy(new File(CD_CONTENT_PATH + File.separator + "DListor" + File.separator), new File(installationFolder + File.separator + "DListor" + File.separator));
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 			
 			if (!new File(installationFolder + File.separator + "VocalyzeSIVOX" + File.separator).exists()) {
 				/* Copie de VocalyzeSIVOX */
 				try {
 					comet.sendMessage("info: Copie du répertoire \"VocalyzeSIVOX\" et de ses sous-répertoires");
 					//FileUtils.copy(new File(CD_CONTENT_PATH + File.separator + "LesLogiciels" + File.separator + "VocalyzeSIVOX" + File.separator), new File(installationFolder + File.separator + "VocalyzeSIVOX" + File.separator));
 					FileUtils.copy(new File(CD_CONTENT_PATH + File.separator + "VocalyzeSIVOX" + File.separator), new File(installationFolder + File.separator + "VocalyzeSIVOX" + File.separator));
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 			
 			/* Copie des jeux sélectionnés  */
 			for(Game g: toInstall) {
 				try {
 					comet.sendMessage("info: Copie du projet \"" + g.getTitle() + "\" (répertoire \"" + g.getGameRep().getName() + "\" et ses sous-répertoires)");
 					FileUtils.copy(g.getGameRep(), new File(installationFolder + File.separator + g.getGameRep().getName() + File.separator));
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				HelpUtils.GAMELIST += "<li><a href=\"jeux/" + g.getGameRep().getName() + ".html\" title=\"" + g.getTitle() + "\"><strong>" + g.getTitle() + "</strong></a></li>\n";
 				HelpUtils.GAMELIST_INGAMEFOLDER += "<li><a href=\"" + g.getGameRep().getName() + ".html\" title=\"" + g.getTitle() + "\"><strong>" + g.getTitle() + "</strong></a></li>\n";
 			}
 			
 			/* Création de l'aide */
 			comet.sendMessage("info: -- Génération de l'aide --");
 			try {
 				comet.sendMessage("info: Copie des fichiers de base de l'aide");
 				FileUtils.copy(new File(CD_CONTENT_PATH + File.separator + "DHelp" + File.separator), new File(installationFolder + File.separator + "Aide" + File.separator));
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
 			HelpUtils.GAMELIST = "<h1>Aide sur les projets installés</h1>\n<ul id=\"listjeux\">\n" + HelpUtils.GAMELIST + "</ul>\n";
 			HelpUtils.GAMELIST_INGAMEFOLDER = "<h1>Aide sur les projets installés</h1>\n<ul id=\"listjeux\">\n" + HelpUtils.GAMELIST_INGAMEFOLDER + "</ul>\n";
 			
 				// Création du fichier jeux.html
 			
 			
 			String jeuxContent = HelpUtils.HEADER + HelpUtils.GAMELIST;
 			jeuxContent += "<div id=\"aidejeu\">\n";
 			jeuxContent += "Cliquez sur le nom d'un jeu ci-contre pour voir l'aide associée :)\n";
 			jeuxContent += "</div>\n";
 			jeuxContent += HelpUtils.FOOTER;
 			FileUtils.write(installationFolder + File.separator + "Aide" + File.separator + "jeux.html"  , jeuxContent);
 			
 				
 				//Création de tous les fichiers aides des jeux installés
 			for(Game g: toInstall) {
 				comet.sendMessage("info: Génération de l'aide pour le jeu \"" + g.getTitle() + "\"");
 				String gameContent = HelpUtils.HEADER_INGAMEFOLDER + HelpUtils.GAMELIST_INGAMEFOLDER;
 				gameContent += "<div id=\"aidejeu\">\n";
 				
 				gameContent += "	<div class=\"jeu_title\">\n";
 				gameContent += "<h1>" + g.getTitle() + "</h1>\n";
 				gameContent += "	</div>\n";
 				
 				
 				
 				gameContent += "	<div class=\"jeu_authors boite\">\n";
 				gameContent += "		<div class=\"boite_title\">\n";
 				gameContent += "		Auteurs\n";
 				gameContent += "		</div>\n";
 				gameContent += "		<div class=\"boite_content\">\n";
 				for(String author: g.getAuthors()) {
 					gameContent += "		<div class=\"jeu_author\">\n";
 					gameContent += author + "\n";
 					gameContent += "		</div>\n";
 				}
 				gameContent += "	<div class=\"clear\">&nbsp;</div>\n";
 				gameContent += "		</div>\n";
 				gameContent += "	</div>\n";
 				
 				gameContent += "	<div class=\"jeu_year\">\n";
 				gameContent += "<u>Année</u>: " + g.getAnnee() + "\n";
 				gameContent += "	</div>\n";
 				
 				gameContent += "	<div class=\"jeu_public\">\n";
 				gameContent += "<u>Public</u>: " + g.getPublicStyle().toString() + "\n";
 				gameContent += "	</div>\n";
 				
 				gameContent += "	<div class=\"jeu_age\">\n";
 				gameContent += "<u>Age</u>: " + g.getAge() + "\n";
 				gameContent += "	</div>\n";
 				
 				gameContent += "	<div class=\"jeu_categories\">\n";
 				gameContent += "	<u>Catégories de jeux</u>:<br/>\n";
 				for(GameCategory gc: g.getGameCategories()) {
 					gameContent += "		<div class=\"jeu_category\">\n";
 					gameContent += gc.toString() + "\n";
 					gameContent += "		</div>\n";
 				}
 				gameContent += "	</div>\n";
 				
 				gameContent += "	<div class=\"clear\">&nbsp;</div>\n";
 				
 				gameContent += "	<div class=\"jeu_shortdesc boite\">\n";
 				gameContent += "		<div class=\"boite_title\">\n";
 				gameContent += "		Résumé\n";
 				gameContent += "		</div>\n";
 				gameContent += "		<div class=\"boite_content\">\n";
 				gameContent += g.getShortDescription() + "\n";
 				gameContent += "		</div>\n";
 				gameContent += "	</div>\n";
 				
 				gameContent += "	<div class=\"jeu_gamerules boite\">\n";
 				gameContent += "		<div class=\"boite_title\">\n";
 				gameContent += "		Règles du jeu\n";
 				gameContent += "		</div>\n";
 				gameContent += "		<div class=\"boite_content\">\n";
 				gameContent += g.getGameRules() + "\n";
 				gameContent += "		</div>\n";
 				gameContent += "	</div>\n";
 				
 				gameContent += "	<div class=\"jeu_gameplay boite\">\n";
 				gameContent += "		<div class=\"boite_title\">\n";
 				gameContent += "		Commandes du jeu\n";
 				gameContent += "		</div>\n";
 				gameContent += "		<div class=\"boite_content\">\n";
 				gameContent += g.getGamePlay() + "\n";
 				gameContent += "		</div>\n";
 				gameContent += "	</div>\n";
 				
 				gameContent += "	<div class=\"jeu_notes boite\">\n";
 				gameContent += "		<div class=\"boite_title\">\n";
 				gameContent += "		Notes sur le jeu\n";
 				gameContent += "		</div>\n";
 				gameContent += "		<div class=\"boite_content\">\n";
 				gameContent += "			<ul>\n";
 				for(String note: g.getNotes()) {
 					gameContent += "		<li class=\"jeu_note\">\n";
 					gameContent += note + "\n";
 					gameContent += "		</li>\n";
 				}
 				gameContent += "			</ul>\n";
 				gameContent += "		</div>\n";
 				gameContent += "	</div>\n";
 				
 				
 				
 				gameContent += "</div>\n";
 				gameContent += HelpUtils.FOOTER;
 				
 				String gameHelpFilePath = installationFolder + File.separator + "Aide" + File.separator + "jeux" + File.separator + g.getGameRep().getName() + ".html";
 				FileUtils.write(gameHelpFilePath , gameContent);
 				
 				File helpFolder = new File(g.getGameRep().getAbsolutePath() + File.separator + "doc" + File.separator + g.getGameRep().getName() + File.separator);
 				if(helpFolder.exists()) {
 					try {
 						FileUtils.copy(helpFolder, new File(installationFolder + File.separator + "Aide" + File.separator + "jeux" + File.separator + g.getGameRep().getName() + File.separator));
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 			}
 			
 			String gameShortcutPath = "";
 			String helpShortcutPath = "";
 			
 			if (OSValidator.isWindowsSeven()) {
 				gameShortcutPath = System.getProperty("user.home")+File.separator+"Desktop"+File.separator+"Jeux DeViNT.lnk";
 				helpShortcutPath = System.getProperty("user.home")+File.separator+"Desktop"+File.separator+"Aide DeViNT.lnk";
 			} else if (OSValidator.isWindowsXP()) {
 				if (Locale.getDefault().equals(Locale.FRANCE)) {
 					gameShortcutPath = System.getProperty("user.home")+File.separator+"Bureau"+File.separator+"Jeux DeViNT.lnk";
 					helpShortcutPath = System.getProperty("user.home")+File.separator+"Bureau"+File.separator+"Aide DeViNT.lnk";
 				}else if (Locale.getDefault().equals(Locale.ENGLISH)) {
 					gameShortcutPath = System.getProperty("user.home")+File.separator+"Desktop"+File.separator+"Jeux DeViNT.lnk";
 					helpShortcutPath = System.getProperty("user.home")+File.separator+"Desktop"+File.separator+"Aide DeViNT.lnk";
 				}
 			}
 			
 			/* Création des icones pour windows */
 			if(OSValidator.isWindows() && !gameShortcutPath.equals("") && !helpShortcutPath.equals("")) {
 				try {
 					Shortcut scutJeux = new Shortcut(new File(installationFolder+File.separator+"DListor"+File.separator+"bin"+File.separator+"execution.bat"));
 					Shortcut scutAide = new Shortcut(new File(installationFolder+File.separator+"Aide"+File.separator+"index.html"));
 					OutputStream osJeux = new FileOutputStream(gameShortcutPath);
 					OutputStream osAide = new FileOutputStream(helpShortcutPath);
 					osJeux.write(scutJeux.getBytes());
 					osJeux.flush();
 					osJeux.close();
 					osAide.write(scutAide.getBytes());
 					osAide.flush();
 					osAide.close();
 				} catch (UnsupportedEncodingException e) {
 					e.printStackTrace();
 				} catch (FileNotFoundException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		}	
 		
 		long copyTotalTime = System.currentTimeMillis() - startCopyTime;
 		
 		long copyTotalTimeSec = (copyTotalTime / 1000);
 		
 		long copyTotalTimeMin = (copyTotalTimeSec / 60);
 		
 		long copyTotalTimeSecRest = copyTotalTimeSec - (copyTotalTimeMin*60);
 		
 		comet.sendMessage("success: Installation terminée!");
 		comet.sendMessage("success: Temps total de l'installation: " + copyTotalTimeMin + " minutes et "  + copyTotalTimeSecRest + " secondes (Total: " + copyTotalTime + " millisecondes)");
 		
 		comet.sendMessage("<a href=\"/end\" class=\"btn btn-large btn-primary\" >Suivant</a>");
 		
 		comet.close();
 		
 	}
 	
 public static void launchUninstall(Comet comet, String installationFolder, String[] selectedGames) {
 	
 	long startCopyTime = System.currentTimeMillis();
 	
 	File idfile = new File(installationFolder+File.separator+"installationid");
 	if (idfile.exists() && idfile.isFile()) {
 		try {
 			FileReader fr = new FileReader(idfile);
 			char[] cbuf = new char[50]; 
 			int nbChar = fr.read(cbuf);
 			String fileNumber = "";
 			for (int i = 0; i<nbChar; i++) {
 				fileNumber+=cbuf[i];
 			}
			fr.close();
 			try {
 				if (Integer.parseInt(fileNumber) != installationFolder.hashCode()) {
 					comet.sendMessage("Il n'existe pas de projets déjà installés dans "+installationFolder);
					//comet.sendMessage("<a href=\"/end\" class=\"btn btn-large btn-primary\" >Suivant</a>");
 					return;
 				}
 			} catch (NumberFormatException e) {
 				comet.sendMessage("Il n'existe pas de projets déjà installés dans "+installationFolder);
				//comet.sendMessage("<a href=\"/end\" class=\"btn btn-large btn-primary\" >Suivant</a>");
 				return;
 			}
 		} catch (FileNotFoundException e) {
 			comet.sendMessage("Il n'existe pas de projets déjà installés dans "+installationFolder);
			//comet.sendMessage("<a href=\"/end\" class=\"btn btn-large btn-primary\" >Suivant</a>");
 			e.printStackTrace();
 			return;
 		} catch (IOException e) {
 			comet.sendMessage("Il n'existe pas de projets déjà installés dans "+installationFolder);
			//comet.sendMessage("<a href=\"/end\" class=\"btn btn-large btn-primary\" >Suivant</a>");
 			e.printStackTrace();
 			return;
 		}
 		
 	} else {
 		comet.sendMessage("Il n'existe pas de projets déjà installés dans "+installationFolder);
		//comet.sendMessage("<a href=\"/end\" class=\"btn btn-large btn-primary\" >Suivant</a>");
 		return;
 	}
 	
 	ArrayList<Game> installedGames = Game.getAll(installationFolder);
 	
 	if (installedGames == null) {
 		return;
 	}
 	
 	ArrayList<Game> toUninstall = new ArrayList<Game>();
 	
 	for(Game g : installedGames) {
 		for(String sg : selectedGames) {
 			if(sg.equals(g.getGameRep().toString())) {
 				toUninstall.add(g);
 			}
 		}
 	}
 	
 	boolean uninstallAll = (toUninstall.size() == installedGames.size());
 	
 	for (Game game : installedGames) {
 		if ((toUninstall.contains(game)) && game != null) {
 			comet.sendMessage("info: Désinstallation de "+game.getTitle());
 			FileUtils.rmDir(game.getGameRep());
 		}
 	}
 	
 	comet.sendMessage("info: Suppression des aides générées précédemment.");
 	FileUtils.rmDir(new File(installationFolder+File.separator+"Aide"));
 	
 	if (uninstallAll) {
 		comet.sendMessage("info: Destruction du répertoire \"lib\" et de ses sous répertoires.");
 		FileUtils.rmDir(new File(installationFolder+File.separator+"lib"));
 		comet.sendMessage("info: Destruction du répertoire \"jre\" et de ses sous répertoires.");
 		FileUtils.rmDir(new File(installationFolder+File.separator+"jre"));
 		comet.sendMessage("info: Destruction du répertoire \"VocalyzeSIVOX\" et de ses sous répertoires.");
 		FileUtils.rmDir(new File(installationFolder+File.separator+"VocalyzeSIVOX"));
 		comet.sendMessage("info: Destruction du répertoire \"Listor\" et de ses sous répertoires.");
 		FileUtils.rmDir(new File(installationFolder+File.separator+"Listor"));
 		comet.sendMessage("info: Destruction du répertoire \"DListor\" et de ses sous répertoires.");
 		FileUtils.rmDir(new File(installationFolder+File.separator+"DListor"));
 		comet.sendMessage("info: Destruction du répertoire \"Aide\" et de ses sous répertoires.");
 		FileUtils.rmDir(new File(installationFolder+File.separator+"Aide"));
		//That was too violent, it was deleting everything in the directory.
		//Might use new File(...).delete instead if feel necessary
 		//comet.sendMessage("info: Destruction du répertoire d'installation et de ses sous répertoires.");
 		//FileUtils.rmDir(new File(installationFolder+File.separator));
 		
		//delete the file identifying the directory as a DeViNT installation dir.
		idfile.delete();
		
 		String gameShortcutPath = "";
 		String helpShortcutPath = "";
 		
 		if (OSValidator.isWindowsSeven()) {
 			gameShortcutPath = System.getProperty("user.home")+File.separator+"Desktop"+File.separator+"Jeux DeViNT.lnk";
 			helpShortcutPath = System.getProperty("user.home")+File.separator+"Desktop"+File.separator+"Aide DeViNT.lnk";
 		} else if (OSValidator.isWindowsXP()) {
 			if (Locale.getDefault().equals(Locale.FRANCE)) {
 				gameShortcutPath = System.getProperty("user.home")+File.separator+"Bureau"+File.separator+"Jeux DeViNT.lnk";
 				helpShortcutPath = System.getProperty("user.home")+File.separator+"Bureau"+File.separator+"Aide DeViNT.lnk";
 			}else if (Locale.getDefault().equals(Locale.ENGLISH)) {
 				gameShortcutPath = System.getProperty("user.home")+File.separator+"Desktop"+File.separator+"Jeux DeViNT.lnk";
 				helpShortcutPath = System.getProperty("user.home")+File.separator+"Desktop"+File.separator+"Aide DeViNT.lnk";
 			}
 		}
 		
 		
 		if (OSValidator.isWindows() && !gameShortcutPath.equals("") && !helpShortcutPath.equals("")) {
 			comet.sendMessage("info: Suppression du raccourcis Jeux DeViNT.");
 			FileUtils.rmDir(new File(gameShortcutPath));
 			comet.sendMessage("info: Suppression du raccourcis Aide DeViNT.");
 			FileUtils.rmDir(new File(helpShortcutPath));
 		}
 	}
 	
 	long copyTotalTime = System.currentTimeMillis() - startCopyTime;
 	
 	long copyTotalTimeSec = (copyTotalTime / 1000);
 	
 	long copyTotalTimeMin = (copyTotalTimeSec / 60);
 	
 	long copyTotalTimeSecRest = copyTotalTimeSec - (copyTotalTimeMin*60);
 	
 	comet.sendMessage("success: Désinstallation terminée!");
 	comet.sendMessage("success: Temps total de l'opération: " + copyTotalTimeMin + " minutes et "  + copyTotalTimeSecRest + " secondes (Total: " + copyTotalTime + " millisecondes)");
 	
 	comet.sendMessage("<a href=\"/end\" class=\"btn btn-large btn-primary\" >Suivant</a>");
 	
 	comet.close();
 }
   
   public static Result end() {
 	  int year = getYear();
 	  if (session("uninstall") != null && session("uninstall").equals("true")) {
 		return ok(end.render("La désinstallation est terminée!", year, true));
 	  } else {
 		return ok(end.render("L'installation du CD DeViNT " + year + " est terminée!", year, false));
 	  }
   }
 }
