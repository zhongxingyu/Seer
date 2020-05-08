 package main;
 
 import java.awt.Color;
 import java.util.ArrayList;
 
 import cleaner.*;
 import menusystem2.*;
 
 //very static class to hold the current progress of the scan
 public class Updater
 {
 	public static ArrayList<Match> cacheMatches = new ArrayList<Match>();
 	public static ArrayList<Match> historyMatches = new ArrayList<Match>();
 	
 	public static int totalFiles = 0;
 	public static int scannedFiles = 0;
 	
 	public static void startUp()
 	{
 		Lists.loadLists();
 	}
 	
 	public static void setTotalFiles(int total)
 	{
 		totalFiles = total;
 		MakeMenu.loadingbar.setMax(total);
 	}
 	
 	public static void incrementScannedFiles()
 	{
 		scannedFiles++;
 		MakeMenu.loadingbar.setCurrent(scannedFiles);
 	}
 	
 	public static void clean()
 	{
		System.out.println("Cleaning...");
//		Lists.deleteMatchedFiles = true;
		runScan();
 	}
 	
 	public static void scan()
 	{
 		System.out.print("Scanning...");
 		runScan();
 		System.out.println("Scan complete.");
 	}
 	
 	private static void runScan()
 	{
 //		Lists.loadLists();
 		historyMatches = Lists.getMatches(Lists.historyFolders, true);
 		cacheMatches = Lists.getMatches(Lists.cacheFolders, true);
 		
 		Updater.finishScan();
 		
 //		for(Match m : historyMatches)
 //		{
 //			System.out.println("~"+m.matchedTerm);
 //			m.toMenu().printMenus();
 //		}
 	}
 	
 	public static void finishScan()
 	{
 		ArrayList<Menu> matchMenus = new ArrayList<Menu>();
 		
 		Button historyMenu = new Button(10, 1, true, "History Matches");
 		historyMenu.useInvertedText = false;
 		historyMenu.changeColorWhenClicked = false;
 		historyMenu.setColor(new Color(0, 75, 136));
 		matchMenus.add(historyMenu);
 		
 		for(int i = 0; i < historyMatches.size(); i++)
 			matchMenus.add(historyMatches.get(i).toMenu());
 		
 		Button cacheMenu = new Button(10, 1, true, "Cache Matches");
 		cacheMenu.useInvertedText = false;
 		cacheMenu.changeColorWhenClicked = false;
 		cacheMenu.setColor(new Color(0, 75, 136));
 		matchMenus.add(cacheMenu);
 		
 		for(int i = 0; i < cacheMatches.size(); i++)
 			matchMenus.add(cacheMatches.get(i).toMenu());
 		
 		MakeMenu.matchScroller.setPos(0);
 		MakeMenu.matchScroller.addMenus(matchMenus);
 		MakeMenu.matchScroller.sizeSubMenus();
 		
 		MakeMenu.loadingbar.reset();
 	}
 	
 	public static void removeLastNMinutes(int n)
 	{
 //		SaveNLoad.getLastAccessTime(file)
 	}
 
 	public static void addLists()
 	{
 		ArrayList<Menu> allListMenus = new ArrayList<Menu>();
 		
 		getMenusFromList(Lists.blacklist, allListMenus);
 		getMenusFromList(Lists.whitelist, allListMenus);
 		getMenusFromList(Lists.extensions, allListMenus);
 		getMenusFromList(Lists.whitelistFolders, allListMenus);
 		getMenusFromList(Lists.blacklistFolders, allListMenus);
 		
 		MakeMenu.listsScroller.addMenus(allListMenus);
 	}
 	
 	//add menus from list to allListMenus, then return allListMenus
 	public static ArrayList<Menu> getMenusFromList(MatchList list, ArrayList<Menu> allListMenus)
 	{
 		ArrayList<Menu> matchListMenus = list.getMenus();
 		for(int j = 0; j < matchListMenus.size(); j++)
 			allListMenus.add(matchListMenus.get(j));
 		return allListMenus;
 	}
 	
 	public static void addRecent()
 	{
 		ArrayList<Menu> allRecentMenus = new ArrayList<Menu>();
 		
 		for(Menu m : Lists.historyFolders.getMenus())
 			allRecentMenus.add(m);
 		
 		for(Menu m : Lists.cacheFolders.getMenus())
 			allRecentMenus.add(m);
 		
 		MakeMenu.folderScroller.addMenus(allRecentMenus);
 	}
 	
 	public static ListFile getListFromID(long ID)
 	{
 		for(int i = 0; i < Lists.allLists.size(); i++)
 			if(Lists.allLists.get(i).ID == ID)
 				return Lists.allLists.get(i);
 		
 		return new ListFile();
 	}
 	
 	public static Match getMatchFromID(long ID)
 	{
 		for(int i = 0; i < historyMatches.size(); i++)
 			if(historyMatches.get(i).ID == ID)
 				return historyMatches.get(i);
 		
 		for(int i = 0; i < cacheMatches.size(); i++)
 			if(cacheMatches.get(i).ID == ID)
 				return cacheMatches.get(i);
 		
 		return new Match();
 	}
 }
