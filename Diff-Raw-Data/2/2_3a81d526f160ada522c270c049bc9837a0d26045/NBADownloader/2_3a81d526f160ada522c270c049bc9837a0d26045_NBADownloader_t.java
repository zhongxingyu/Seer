 package nbaDownloader;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 public class NBADownloader 
 {
 
 	private final static String urlStart = "http://stats.nba.com/";
 	private final static String pbpStart = "stats/playbyplay?GameID=";
 	private final static String pbpEnd = "&StartPeriod=0&EndPeriod=0";
 	private final static String boxStart = "stats/boxscore?GameID=";
 	private final static String boxEnd = "&RangeType=0&StartPeriod=0&EndPeriod=0"+
 											"&StartRange=0&EndRange=0";
 	private final static String shotStart = "stats/shotchartdetail?Season=";
 	private final static String shotMid = "&SeasonType=Regular+Season"+
 											"&TeamID=0&PlayerID=0&GameID=";
 	private final static String shotEnd = "&Outcome=&Location=&Month=0&"+
 											"SeasonSegment=&DateFrom=&DateTo=&"+
 											"OpponentTeamID=0&VsConference=&"+
 											"VsDivision=&Position=&RookieYear=&"+
 											"GameSegment=&Period=0&LastNGames=0&"+
 											"ContextFilter=&ContextMeasure=FG_PCT"+
 											"&display-mode=performance&zone-mode="+
 											"zone&viewShots=false";
 	private final static String playerStart = "stats/commonallplayers/?LeagueID="+
 												"00&Season=";
 	private final static String playerEnd = "&IsOnlyCurrentSeason=0&callback="+
 												"playerinfocallback";
 	private final static String teamURL = "stats/commonteamyears?LeagueID=00"+
 											"&callback=teaminfocallback";
 	private final static String customBoxStart = "stats/boxscore?GameID=";
 	private final static String customBoxMid = "&RangeType=2&StartPeriod=0&EndPeriod=0"+
 											"&StartRange=";
 	private final static String customBoxEnd = "&EndRange=";
 												
 	
 	public NBADownloader()
 	{
 		
 	}
 	
 	private static BufferedReader download(String urlString)
 	{
 		URL url;
 		InputStream is = null;
 		BufferedReader br = null;
 		
 		try 
 		{
 			url = new URL(urlString);
 			is = url.openStream();  // throws an IOException
 			br = new BufferedReader(new InputStreamReader(is));
 		} 
 		catch (MalformedURLException mue) 
 		{
 			mue.printStackTrace();
 		} 
 		catch (IOException ioe) 
 		{
 			ioe.printStackTrace();
 			try 
 			{
 				Thread.sleep(3000);
 			} 
 			catch (InterruptedException e) 
 			{
 				e.printStackTrace();
 			}
 			br = download(urlString);
 		}
 		
 		return br;
 	}
 	
 	private static void write(BufferedReader reader, String savePath,
 			String fileName)
 	{
 		String line;
 		
 		try 
 		{
 			File file = new File(savePath + fileName + ".json");
  
 			// if file doesn't exists, then create it
 			if (!file.exists()) 
 			{
 				file.createNewFile();
 			}
  
 			FileWriter fw = new FileWriter(file.getAbsoluteFile());
 			BufferedWriter bw = new BufferedWriter(fw);
 			
 			while ((line = reader.readLine()) != null)
 			{
 				bw.write(line);
 				bw.write("\n");
 			}
 			
 			bw.close();
  
 		} 
 		catch (IOException e) 
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	public static BufferedReader downloadPBP(String gameID)
 	{
 		return download(urlStart + pbpStart + gameID + pbpEnd);
 	}
 	
 	public static void savePBP(String gameID, String savePath, 
 			String fileName)
 	{
 		write(downloadPBP(gameID), savePath, fileName);
 	}
 	
 	public static void savePBP(String gameID, String savePath)
 	{
 		savePBP(gameID, savePath, gameID + "PBP");
 	}
 	
 	public static BufferedReader downloadBoxScore(String gameID)
 	{
 		return download(urlStart + boxStart + gameID + boxEnd);
 	}
 	
 	public static void saveBoxScore(String gameID, String savePath, 
 			String fileName)
 	{
 		write(downloadBoxScore(gameID), savePath, fileName);
 	}
 	
 	public static void saveBoxScore(String gameID, String savePath)
 	{
 		saveBoxScore(gameID, savePath, gameID + "Box");
 	}
 	
 	public static BufferedReader downloadShotData(String season, String gameID)
 	{
 		return download(urlStart + shotStart + season + shotMid + gameID 
 				+ shotEnd);
 	}
 	
 	public static void saveShotData(String season, String gameID,
 			String savePath, String fileName)
 	{
 		write(downloadShotData(season, gameID), savePath, fileName);
 	}
 	
 	public static void saveShotData(String season, String gameID, 
 			String savePath)
 	{
 		saveShotData(season, gameID, savePath, gameID + "Shots");
 	}
 	
 	public static BufferedReader downloadPlayerData(String season)
 	{
 		return download(urlStart + playerStart + season + playerEnd);
 	}
 	
 	public static void savePlayerData(String season, String savePath, 
 			String fileName)
 	{
 		write(downloadPlayerData(season), savePath, fileName);
 	}
 	
 	public static void savePlayerData(String season, String savePath)
 	{
 		savePlayerData(season, savePath, season + "Players");
 	}
 	
 	public static BufferedReader downloadTeamData()
 	{
 		return download(urlStart + teamURL);
 	}
 	
 	public static void saveTeamData(String savePath, String fileName)
 	{
 		write(downloadTeamData(), savePath, fileName);
 	}
 	
 	public static void saveTeamData(String savePath)
 	{
 		saveTeamData(savePath, "Teams");
 	}
 	
 	public static BufferedReader downloadCustomBox(String gameID, int startTime, int endTime)
 	{
		return download(urlStart + customBoxStart + gameID + customBoxMid + startTime +
 							customBoxEnd + endTime);
 	}
 }
