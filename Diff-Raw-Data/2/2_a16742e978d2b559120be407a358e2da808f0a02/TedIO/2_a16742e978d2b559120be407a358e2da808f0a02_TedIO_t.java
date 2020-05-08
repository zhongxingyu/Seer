 package ted;
 
 /****************************************************
  * IMPORTS
  ****************************************************/
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 import java.util.StringTokenizer;
 import java.util.Vector;
 
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 
 import org.w3c.dom.Element;
 
 import ted.ui.editshowdialog.FeedPopupItem;
 
 /**
  * TED: Torrent Episode Downloader (2005 - 2006)
  * 
  * This class does all the file reading / writing for ted
  * 
  * @author Roel
  * @author Joost
  *
  * ted License:
  * This file is part of ted. ted and all of it's parts are licensed
  * under GNU General Public License (GPL) version 2.0
  * 
  * for more details see: http://en.wikipedia.org/wiki/GNU_General_Public_License
  *
  */
 public class TedIO 
 {
 	private String XMLurl = "http://ted.sourceforge.net/shows_clean.xml"; //$NON-NLS-1$
 	private String versionUrl = "http://ted.sourceforge.net/version.txt";
 	
 	private static String CONFIG_FILE = TedSystemInfo.getUserDirectory()+"config.ted"; //$NON-NLS-1$
 	private static String SHOWS_FILE = TedSystemInfo.getUserDirectory()+"shows.ted"; //$NON-NLS-1$
 	public static String XML_SHOWS_FILE = TedSystemInfo.getUserDirectory()+"shows_clean.xml"; //$NON-NLS-1$
 	
 	/****************************************************
 	 * CONSTRUCTORS
 	 ****************************************************/
 	
 	/****************************************************
 	 * PUBLIC METHODS
 	 ****************************************************/
 	/**
 	 * Saves the current shows of ted to the harddrive
 	 * @param series Current vector of shows in ted
 	 */
 	public void SaveShows(Vector series)
 	{
 		try
 		{
 			// Write to disk with FileOutputStream
 			FileOutputStream f_out = new FileOutputStream(SHOWS_FILE);
 	
 			// Write object with ObjectOutputStream
 			ObjectOutputStream obj_out = new ObjectOutputStream (f_out);
 	
 			// Write object out to disk
 			obj_out.writeObject ( series );
 			
 			f_out.close();
 			obj_out.close();
 		}
 		catch (Exception e)
 		{
 			TedLog.error(e, "Shows File writing error"); //$NON-NLS-1$
 		}
 	}
 	
 	/**
 	 * Reads the shows from the harddisk
 	 * @return Shows saved on the harddisk
 	 */
 	public Vector GetShows ()
 	{
 		Vector vec = new Vector();
 		try 
 		{
 			// check application folder for all shows file
 			
 			File userdir_shows_file = new File(SHOWS_FILE);
 			// if config does not exist, copy from current dir & delete original
 			if (!userdir_shows_file.exists())
 			{
 				// if config does not exist in current dir either: new ted
 				File shows_file = new File ("shows.ted"); //$NON-NLS-1$
 				
 				if (shows_file.exists())
 				{
 					// copy config_file and delete afterwards
 					try
 					{
 						copyFile(shows_file, userdir_shows_file);
 						if (!shows_file.delete())
 						{
 							TedLog.error("Error deleting shows file from original ted dir"); //$NON-NLS-1$
 						}
 					}
 					catch (Exception e)
 					{
 						TedLog.error(e, "Error copying shows file to user directory"); //$NON-NLS-1$
 					}		
 				}
 			}
 			// Read from disk using FileInputStream
 			FileInputStream f_in;
 			f_in = new FileInputStream(SHOWS_FILE);
 				
 			// Read object using ObjectInputStream
 			ObjectInputStream obj_in = new ObjectInputStream (f_in);
 			
 			// Read an object
 			Object obj = obj_in.readObject();
 	
 			if (obj instanceof Vector)
 			{
 				// Cast object to a Vector
 				vec = (Vector) obj;
 				return vec;
 			}
 			
 			f_in.close();
 			obj_in.close();
 		} 
 		catch (FileNotFoundException e) 
 		{
 			// do nothing, just return the empty vector
 			return vec;
 		}
 		catch (IOException e) 
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} 
 		catch (ClassNotFoundException e) 
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return vec;
 	}
 	
 	/**
 	 * Checks and saves a found torrent to the harddisk
 	 * @param name Name of the torrent
 	 * @param minSize Minimum size for the contents of the torrent
 	 * @param maxSize Maximum size for the contents of the torrent
 	 * @param location Folder where ted has to store the torrent
 	 * @param config Current TedConfig
 	 * @throws Exception
 	 */
 		
 	/**
 	 * Saves the config to the harddrive
 	 * @param tc TedConfig we have to save
 	 */
 	public void SaveConfig()
 	{
 		try
 		{
 			File file = new File(CONFIG_FILE);
 			FileWriter fw = new FileWriter(file);
 			
 			TedLogDialog t = TedLogDialog.getInstance();
 						
 			// write all the settings in new lines to config.ted
 			fw.append("refresh=" + TedConfig.getRefreshTime() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
 			fw.append("directory=" + TedConfig.getDirectory()  + "\n");	 //$NON-NLS-1$ //$NON-NLS-2$
 			fw.append("opentorrent=" + TedConfig.isOpenTorrent() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
 			fw.append("checkversion=" + TedConfig.isCheckVersion()  + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
 			fw.append("showerrors=" + TedConfig.isShowErrors()  + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
 			fw.append("showhurray=" + TedConfig.isShowHurray()  + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
 			fw.append("windowwidth=" + TedConfig.getWidth()  + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
 			fw.append("windowheight=" + TedConfig.getHeight()  + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
 			fw.append("windowx=" + TedConfig.getX()  + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
 			fw.append("windowy=" + TedConfig.getY()  + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
 			fw.append("startminimized=" + TedConfig.isStartMinimized() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
 			fw.append("timeoutsecs=" + TedConfig.getTimeOutInSecs() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
 			fw.append("seedersetting=" + TedConfig.getSeederSetting() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
 			fw.append("rssversion=" + TedConfig.getRSSVersion() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
 			fw.append("rssupdate=" + TedConfig.getAutoUpdateFeedList() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
 			fw.append("rssadjust=" + TedConfig.getAutoAdjustFeeds() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
 			fw.append("locale_language=" + TedConfig.getLanguage() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
 			fw.append("locale_country=" + TedConfig.getCountry() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
 			fw.append("parse_at_start=" + TedConfig.isParseAtStart() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
 			fw.append("add_tray=" + TedConfig.isAddSysTray() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
 			fw.append("downloadcompressed=" + TedConfig.getDoNotDownloadCompressed() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
 			fw.append("filterextensions=" + TedConfig.getFilterExtensions() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
 			fw.append("loglines=" + t.getLines() + "\n"); //$NON-NLS-1$
 			fw.append("allowlogging=" + TedConfig.isAllowLogging() + "\n"); //$NON-NLS-1$
 			fw.append("logtofile=" + TedConfig.isLogToFile()); //$NON-NLS-1$
 			
 			fw.close();
 		}
 		catch (Exception e)
 		{
             TedLog.error(e, "Config File writing error"); //$NON-NLS-1$
 		}		
 	}
 	
 	/**
 	 * Reads a TedConfig from the harddrive
 	 * @return TedConfig read from the harddrive
 	 * @throws FileNotFoundException
 	 */
 	public void GetConfig () throws FileNotFoundException
 	{
 		try
 		{
 			// check application folder for all config / shows files
 			
 			File userdir_config_file = new File(CONFIG_FILE);
 			// if config does not exist, copy from current dir & delete original
 			if (!userdir_config_file.exists())
 			{
 				// if config does not exist in current dir either: new ted
 				File config_file = new File ("config.ted"); //$NON-NLS-1$
 				
 				if (!config_file.exists())
 				{
 					// new ted: show config dialog at startup
 					FileNotFoundException e = new FileNotFoundException();
 					throw e; 
 				}
 				else
 				{
 					// copy config_file and delete afterwards
 					try
 					{
 						copyFile(config_file, userdir_config_file);
 						if (!config_file.delete())
 						{
 							TedLog.error("Error deleting config file from original ted dir"); //$NON-NLS-1$
 						}
 					}
 					catch (Exception e)
 					{
 						TedLog.error(e, "Error copying config file to user directory"); //$NON-NLS-1$
 					}
 					
 				}
 			}
 
             TedLog.debug("Open config file " + userdir_config_file.getAbsolutePath()); //$NON-NLS-1$
 			FileReader fr = new FileReader(userdir_config_file);
 			BufferedReader br = new BufferedReader(fr);
 			
 			String tempLanguage = ""; //$NON-NLS-1$
 			String tempCountry = ""; //$NON-NLS-1$
 			
 			String line = null;
 			StringTokenizer tokenizer;
 			String token;
 			while((line=br.readLine()) != null)
 			{
 				tokenizer = new StringTokenizer(line, "="); //$NON-NLS-1$
 				token = tokenizer.nextToken();
 
 				if(token.equals("refresh")) //$NON-NLS-1$
 				{
 					TedConfig.setRefreshTime(Integer.parseInt(tokenizer.nextToken()));
 				}
 				else if(token.equals("directory")) //$NON-NLS-1$
 				{
 					String s = tokenizer.nextToken();
 					File f = new File(s);
 					
 					if(f.isDirectory())
 						TedConfig.setDirectory(s);
 					else
 						JOptionPane.showMessageDialog(null, Lang.getString("TedConfigDialog.DialogSelectDirectory")); //$NON-NLS-1$
 				}
 				else if(token.equals("opentorrent")) //$NON-NLS-1$
 				{
 					TedConfig.setOpenTorrent(Boolean.parseBoolean(tokenizer.nextToken()));
 				}
 				else if(token.equals("checkversion")) //$NON-NLS-1$
 				{
 					TedConfig.setCheckVersion(Boolean.parseBoolean(tokenizer.nextToken()));
 				}
 				else if(token.equals("showerrors")) //$NON-NLS-1$
 				{
 					TedConfig.setShowErrors(Boolean.parseBoolean(tokenizer.nextToken()));
 				}
 				else if(token.equals("showhurray")) //$NON-NLS-1$
 				{
 					TedConfig.setShowHurray(Boolean.parseBoolean(tokenizer.nextToken()));
 				}
 				else if(token.equals("windowwidth")) //$NON-NLS-1$
 				{
 					TedConfig.setWidth(Integer.parseInt(tokenizer.nextToken()));
 				}
 				else if(token.equals("windowheight")) //$NON-NLS-1$
 				{
 					TedConfig.setHeight(Integer.parseInt(tokenizer.nextToken()));
 				}
 				else if(token.equals("windowx")) //$NON-NLS-1$
 				{
 					TedConfig.setX(Integer.parseInt(tokenizer.nextToken()));
 				}
 				else if(token.equals("windowy")) //$NON-NLS-1$
 				{
 					TedConfig.setY(Integer.parseInt(tokenizer.nextToken()));
 				}
 				else if(token.equals("startminimized")) //$NON-NLS-1$
 				{
 					TedConfig.setStartMinimized(Boolean.parseBoolean(tokenizer.nextToken()));
 				}
 				else if(token.equals("rssversion")) //$NON-NLS-1$
 				{
 					TedConfig.setRSSVersion(Integer.parseInt(tokenizer.nextToken()));
 				}
 				else if(token.equals("rssupdate")) //$NON-NLS-1$
 				{
 					TedConfig.setAutoUpdateFeedList(Integer.parseInt(tokenizer.nextToken()));
 				}
 				else if(token.equals("rssadjust")) //$NON-NLS-1$
 				{
 					TedConfig.setAutoAdjustFeeds(Integer.parseInt(tokenizer.nextToken()));
 				}
 				else if(token.equals("timeoutsecs")) //$NON-NLS-1$
 				{
 					TedConfig.setTimeOutInSecs(Integer.parseInt(tokenizer.nextToken()));
 				}
 				else if(token.equals("seedersetting")) //$NON-NLS-1$
 				{
 					TedConfig.setSeederSetting(Integer.parseInt(tokenizer.nextToken()));
 				}
 				else if(token.equals("locale_language")) //$NON-NLS-1$
 				{
 					tempLanguage = tokenizer.nextToken();
 				}
 				else if(token.equals("locale_country")) //$NON-NLS-1$
 				{
 					tempCountry = tokenizer.nextToken();
 				}
 				else if(token.equals("parse_at_start")) //$NON-NLS-1$
 				{
 					TedConfig.setParseAtStart(Boolean.parseBoolean(tokenizer.nextToken()));
 				}
 				else if(token.equals("add_tray")) //$NON-NLS-1$
 				{
 					TedConfig.setAddSysTray(Boolean.parseBoolean(tokenizer.nextToken()));
 				}
 				else if(token.equals("downloadcompressed")) //$NON-NLS-1$
 				{
 					TedConfig.setDoNotDownloadCompressed(Boolean.parseBoolean(tokenizer.nextToken()));
 				}
 				else if(token.equals("filterextensions")) //$NON-NLS-1$
 				{
 					TedConfig.setFilterExtensions(tokenizer.nextToken());
 				}	
 				else if(token.equals("loglines")) //$NON-NLS-1$
 				{
 					TedLogDialog t = TedLogDialog.getInstance();
 					t.setMaxLines(Integer.parseInt(tokenizer.nextToken()));
 				}
 				else if(token.equals("allowlogging")) //$NON-NLS-1$
 				{
 					TedConfig.setAllowLogging(Boolean.parseBoolean(tokenizer.nextToken()));
 				}
 				else if(token.equals("logtofile")) //$NON-NLS-1$
 				{
 					TedConfig.setLogToFile(Boolean.parseBoolean(tokenizer.nextToken()));
 				}
 			}
 			
 			TedConfig.setLocale(tempCountry, tempLanguage);
 		
 			br.close();
 			fr.close();
 			
 		}
 		catch (FileNotFoundException e)
 		{
 			// Config file not available
 			TedLog.error(e, "Configfile config.ted not available"); //$NON-NLS-1$
 			throw e;
 		}
 		catch (Exception e)
 		{
             TedLog.error(e, "Error reading configfile config.ted"); //$NON-NLS-1$
 		}
 	}
 
 	/**
 	 * Reads version.txt from the ted website and returns the current version of ted
 	 * @param d Running version of ted
 	 * @return Current version of ted
 	 */
 	public double checkNewTed(double d) 
 	{
 		try
 		{
 			TedLog.debug("Checking for new version of ted..."); //$NON-NLS-1$
 			URL url = new URL(versionUrl); //$NON-NLS-1$
 		    String line;
 		    StringTokenizer tokenizer;
 		    String token;
 
 		    BufferedReader data = this.makeBufferedReader(url, TedConfig.getTimeOutInSecs());
 		      	
 		    while ((line = data.readLine()) != null) 
 			{
 		    	tokenizer = new StringTokenizer(line, "="); //$NON-NLS-1$
 		    	token = tokenizer.nextToken();
 		    	if (token.equals("version")) //$NON-NLS-1$
 				{
 		    		return Double.parseDouble(tokenizer.nextToken());
 				}
 			}
 		    
 		    data.close();
 		}
 		catch (Exception e)
 		{
 			TedLog.error(e, "Error checking the version of ted"); //$NON-NLS-1$
 		}
 		return d;
 	}
 	
 	/**
 	 * @return The current XML version as published on ted's website
 	 */
 	private int getXMLVersion()
 	{
 		try
 		{
 			TedLog.debug("Checking for new version of show definitions XML"); //$NON-NLS-1$
 			URL url = new URL(versionUrl); //$NON-NLS-1$
 		    String line;
 		    StringTokenizer tokenizer;
 		    String token;
 
 		    BufferedReader data = this.makeBufferedReader(url, TedConfig.getTimeOutInSecs());
 		      	
 		    while ((line = data.readLine()) != null) 
 			{
 		    	tokenizer = new StringTokenizer(line, "="); //$NON-NLS-1$
 		    	token = tokenizer.nextToken();
 		    	if (token.equals("show_xml_version")) //$NON-NLS-1$
 				{
 		    		return Integer.parseInt(tokenizer.nextToken());
 				}
 			}
 		    
 		    data.close();
 		}
 		catch (Exception e)
 		{
 			TedLog.error(e, "Error checking the version of show xml"); //$NON-NLS-1$
 		}
 		return -1;
 	}
 
 	/**
 	 * Check whether there is a new version of shows.xml
 	 * Downloads it and updates shows
 	 * @param main
 	 * @param showresult Whether the user wants to see the result of the check
 	 */
 	public void checkNewXMLFile(TedMainDialog main, boolean showresult, TedTable mainTable)
 	{		
 		// check the website if there is a new version available
 		int version = TedConfig.getRSSVersion();
 		int onlineversion = this.getXMLVersion();
 		
 		// if there is a new version
 		if(onlineversion > version)
 		{
 			int answer = -1;
 			
 			// ask user for confirmation if we have to
 			if (TedConfig.askAutoUpdateFeedList())
 			{
 				answer = JOptionPane.showOptionDialog(null,
 		                Lang.getString("TedIO.DialogNewPredefinedShows1")+ " " + onlineversion + Lang.getString("TedIO.DialogNewPredefinedShows2"), //$NON-NLS-1$ //$NON-NLS-2$
 		                Lang.getString("TedIO.DialogNewPredefinedShowsHeader"), //$NON-NLS-1$
 		                JOptionPane.YES_NO_OPTION,
 		                JOptionPane.QUESTION_MESSAGE, null, Lang.getYesNoLocale(), Lang.getYesNoLocale()[0]);
 			}
 			
 			if (answer == JOptionPane.YES_OPTION || TedConfig.isAutoUpdateFeedList())
 			{
 				// download the XML file
 				downloadXML(main, TedConfig.getTimeOutInSecs(), onlineversion);
 				
 				int rows = mainTable.getRowCount();
 				
 				// check if the user wants us to update the shows
 				answer = -1;
 				if(rows != 0)
 				{
 					if(TedConfig.askAutoAdjustFeeds())
 					{
 						answer = JOptionPane.showConfirmDialog(null,
 				                Lang.getString("TedIO.DialogUpdateShows1")+ "\n" +//$NON-NLS-1$
 				                Lang.getString("TedIO.DialogUpdateShows2") + "\n" + //$NON-NLS-1$
 				                Lang.getString("TedIO.DialogUpdateShows3"), //$NON-NLS-1$
 				                Lang.getString("TedIO.DialogUpdateShowsHeader"), //$NON-NLS-1$
 				                JOptionPane.YES_NO_OPTION,
 				                JOptionPane.QUESTION_MESSAGE);
 					}
 					if(TedConfig.isAutoAdjustFeeds() || answer == JOptionPane.YES_OPTION)
 					{
 						// adjust the feeds
 						this.UpdateShow(main, true, mainTable);
 					}	
 				}
 			}
 		}
 		else if(showresult)
 		{
 			JOptionPane.showMessageDialog(null,
 	                Lang.getString("TedIO.DialogMostRecentShows1")+ " " + version + Lang.getString("TedIO.DialogMostRecentShows2"), //$NON-NLS-1$ //$NON-NLS-2$
 	                Lang.getString("TedIO.DialogMostRecentShowsHeader"), JOptionPane.INFORMATION_MESSAGE); //$NON-NLS-1$
 		}
 
 	}
 	
 	/**
 	 * Update the feeds with the correct urls
 	 * User defined feeds will not be adjusted
 	 * @param main
 	 */
 	public void UpdateShow(TedMainDialog main, boolean AutoUpdate, TedTable mainTable)
 	{
 		String s;
 		String location;
 		int returnVal;
 		
 		if(!AutoUpdate)
 		{
 			location = XML_SHOWS_FILE;
 			File standardFile = new File(location);
 		    JFileChooser chooser = new JFileChooser();
 		    TedExampleFileFilter filter = new TedExampleFileFilter();
 		    filter.addExtension("xml"); //$NON-NLS-1$
 		    filter.setDescription("XML-file"); //$NON-NLS-1$
 		    chooser.setFileFilter(filter);
 		    chooser.setSelectedFile(standardFile);
 		    chooser.setCurrentDirectory(standardFile);
 		    chooser.setDialogTitle("Choose file to synchronize with..."); //$NON-NLS-1$
 		    
 		    returnVal = chooser.showOpenDialog(chooser);
 		    
 		    location = chooser.getSelectedFile().getName();
 		    
 	    	if(!location.endsWith(".xml")) //$NON-NLS-1$
 	    		location += ".xml"; //$NON-NLS-1$
 		}
 		else
 		{
			location = TedIO.XML_SHOWS_FILE;
 			returnVal = JFileChooser.APPROVE_OPTION;
 		}
 		
 	    if(returnVal == JFileChooser.APPROVE_OPTION) 
 	    {
 	    	int rows = mainTable.getRowCount();
 	
 			TedXMLParser parser = new TedXMLParser();
 			Element el = parser.readXMLFromURL(location);
 			
 			
 			for(int i=0; i<rows; i++)
 			{		
 				TedSerie serie = mainTable.getSerieAt(i);
 						
 				if(serie!=null)
 				{
 					TedSerie XMLserie = parser.getSerie(el, serie.getName());
 					serie.AutoFillInPresets(XMLserie);
 					
 					// add auto-generated search based feeds to the show
 					// do this after AutoFillInPresets, 'cause that will reset the feeds
 					// of the serie
 					Vector<FeedPopupItem> items = new Vector<FeedPopupItem>();
 					items = parser.getAutoFeedLocations(el);
 					serie.generateFeedLocations(items);
 				}
 			}
 			if(!AutoUpdate)
 			{
 				s = Lang.getString("TedIO.ShowsSynced"); //$NON-NLS-1$
 			}
 			else
 			{
 				s = Lang.getString("TedIO.ShowsSyncedWithNew"); //$NON-NLS-1$
 			}
 			TedLog.debug(s);
 			
 			if(!AutoUpdate)
 			{
 				JOptionPane.showMessageDialog(null, s, null, 1);
 			}
 	    }
 	}
 	
 
 	/**
 	 * Translate url from feed into usable torrent download url
 	 * @param uri URL to translate
 	 * @param title Torrent title
 	 * @return
 	 */
 	public String translateUrl(String uri, String title, int timeOut) 
 	{
 		try
 		{
 			TedLog.debug("Translating feed torrent URL: " + uri); //$NON-NLS-1$
 			URL url = new URL("http://ted.sourceforge.net/urltranslator.php?url=" + uri + "&sTitle=" + URLEncoder.encode(title, "UTF-8")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 		    String line;
 		      
 		    BufferedReader data = this.makeBufferedReader(url, timeOut);
 		      	
 		    while ((line = data.readLine()) != null) 
 			{		    	
 	    		String torrentUrl = line;
 	    		if (torrentUrl.equals("null")) //$NON-NLS-1$
 	    		{
 	    			TedLog.debug("Unable to translate url, no known torrent url format"); //$NON-NLS-1$
 	    			return null;
 	    		}
 	    		else
 	    		{
 	    			TedLog.debug("Url translated. Torrent URL: " + torrentUrl); //$NON-NLS-1$
 	    			return torrentUrl;
 		    	}
 			}
 			
 		    data.close();
 		}
 		catch (Exception e)
 		{
             TedLog.error(e, "Error translating url"); //$NON-NLS-1$
 			return null;
 		}
 		return null;
 	}
 	
 	/**
 	 * Download the XML file from internet with the show definitions
 	 * @param main
 	 */
 	public void downloadXML(TedMainDialog main, int timeOut, int onlineVersion)
 	{		
 		try 
 		{
 			TedLog.debug("Downloading new show definitions XML"); //$NON-NLS-1$
 			// open connection to the XML file
 			URL url = new URL(this.XMLurl);
 			BufferedReader br = this.makeBufferedReader(url, timeOut);
 			
 			// write the xml file
 			FileWriter fw  = new FileWriter(XML_SHOWS_FILE);
 			
 			String line;	
 			while((line = br.readLine()) != null)
 			{
 				fw.write(line + "\n"); //$NON-NLS-1$
 			}
 			fw.close();
 			br.close();
 			
 			if (onlineVersion < 0)
 			{
 				// get the version of the XML file
 				TedXMLParser parser = new TedXMLParser();
 				Element el = parser.readXMLFromURL(XML_SHOWS_FILE);
 				onlineVersion = parser.getVersion(el);
 			}
 			
 			// set the version and save the config
 			TedConfig.setRSSVersion(onlineVersion);
 			this.SaveConfig();
 		}
 		catch (MalformedURLException e) 
 		{
 			TedLog.debug("Showdefinitions XML file cannot be found: MalformedURL"); //$NON-NLS-1$
 		} catch (IOException e) 
 		{
 			TedLog.debug("Showdefinitions XML file cannot be read"); //$NON-NLS-1$
 		}
 	}
 
 	/**
 	 * Downloads a torrent to the user selected location and launches
 	 * torrentclient if user wants to
 	 * @param url URL of torrent to download
 	 * @param name FileName to save torrent to (without directory)
 	 * @param config TedConfig containing usersettings
 	 * @throws Exception
 	 */
 	public void downloadTorrent(URL url, String name) throws Exception
 	{
 		try
 		{
 			// remove strange tokens from name string so torrent can be opened by client
     		TedLog.debug("Downloading best torrent. URL: " + url + " Name: " + name); //$NON-NLS-1$ //$NON-NLS-2$
     		// remove weird characters and spaces that can cause problems while
 			// opening the torrent
 			name = name.replaceAll("[/:&*?|\"\\\\]", "");
 			name = name.replaceAll(" ()", ".");
          
             //create output torrent file
             String loc = TedConfig.getDirectory() + File.separator  + name + ".torrent"; //$NON-NLS-1$
 			File outputFile = new File(loc); 
 		
     		//file already exists
     		int i = 1;
     		while(outputFile.exists())
     		{
     			loc  = TedConfig.getDirectory() + File.separator + name + "-" + i + ".torrent"; //$NON-NLS-1$ //$NON-NLS-2$
     			outputFile = new File(loc);
     			i++;
     		}
     		    		
     		URLConnection urlc2 = null;
 
     		urlc2 = url.openConnection();
     		urlc2.setConnectTimeout(1000*TedConfig.getTimeOutInSecs());
 			int length = urlc2.getContentLength();
 			InputStream in = urlc2.getInputStream();
 			
 			// incredible ugly hack to retrieve torrents from isohunt
 			if(length==-1)
 			{
 				length = 250000;
 			}
 			
             BufferedInputStream bis = new BufferedInputStream(in);
             FileOutputStream    bos = new FileOutputStream(outputFile);   
 			
             byte[] buff = new byte[length];
             int bytesRead;
 
             while(-1 != (bytesRead = bis.read(buff, 0, buff.length))) 
             {
             	bos.write(buff, 0, bytesRead);
             }
             
             in.close();
             bis.close();
             bos.close();            	
             
            // open the torrent by default program
             if (TedConfig.isOpenTorrent())
 			{
         		TedLog.debug("Opening torrent in default torrentclient.."); //$NON-NLS-1$
         		this.openFile(loc);
     		}
 		}
 		catch(Exception e)
 		{
 			throw e;
 		}
 		
 	}
 
 	public void openFile(String loc)
 	{
 		try
 		{
 			if (TedSystemInfo.osIsWindows())
 	    	{
 	        	String [] open = { "cmd" , "/C", loc};   //$NON-NLS-1$ //$NON-NLS-2$
 	        	System.out.println(loc);
 	        
 	        	Runtime.getRuntime().exec(open);
 			
 			}
 			else if (TedSystemInfo.osIsMac()) 
 			{
 	    		Class fileMgr = Class.forName("com.apple.eio.FileManager"); //$NON-NLS-1$
 	    		Method openURL = fileMgr.getDeclaredMethod("openURL", new Class[]{String.class});  //$NON-NLS-1$
 	        	openURL.invoke(null, new Object[] {"file://" + loc}); //$NON-NLS-1$
 			}
 			else if (TedSystemInfo.osIsLinux())
 			{
 				Runtime.getRuntime().exec( new String [] {"gnome-open", loc} ); //$NON-NLS-1$
 			}
 		}
 		catch (IOException e) 
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) 
 		{
 			
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (SecurityException e) 
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (NoSuchMethodException e) 
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalArgumentException e) 
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalAccessException e) 
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InvocationTargetException e) 
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	/****************************************************
 	 * PRIVATE METHODS
 	 ****************************************************/
 	
 	private static void copyFile(File in, File out) throws Exception 
 	{
 	    FileInputStream fis  = new FileInputStream(in);
 	    FileOutputStream fos = new FileOutputStream(out);
 	    byte[] buf = new byte[1024];
 	    int i = 0;
 	    while((i=fis.read(buf))!=-1) 
 	    {
 	    	fos.write(buf, 0, i);
 	    }
 	    fis.close();
 	    fos.close();
 	}
 	
 	private BufferedReader makeBufferedReader(URL url, int timeOut) throws IOException
 	{
 		URLConnection conn = url.openConnection();
 	    
 	    conn.setConnectTimeout(1000 * timeOut);
 	      
 	    BufferedReader br = new BufferedReader(new InputStreamReader(
 	    		  		conn.getInputStream()));
 	    return br;
 		
 	}
 
 }
