 /**
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 */
 
 package truelauncher.config;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.List;
 
 import truelauncher.utils.LauncherUtils;
 
 public class AllSettings {
 	
 	public static void load() throws Exception
 	{
 		File configfile = new File(LauncherUtils.getDir() + File.separator + AllSettings.getLauncherConfigFolderPath()+File.separator+"jsonclients");
 		ConfigLoader.loadConfig(configfile);
 		ConfigUpdater.startConfigUpdater(configfile);
 	}
 	
 	//For clients, this all is loaded from config.
 	//folder in which clients .zip file will be downloaded
 	protected static String tempfolder = ".true-games.org/packedclients";
 	//path to the folder where all libs are stored (all libs should end with .jar) (this is relative to client launchfolder)
 	protected static String libsfolder = "libraries";
 	//1 - name, 2 - launchfolder, 3 - minecraft jar file, 4 - mainclass , 5 - cmdargs
 	protected static LinkedHashMap<String, ClientData> clientsdata = new LinkedHashMap<String, ClientData>();
 	//allowed addresses for launcher to auth with
 	protected static HashSet<String> allowedaddresses = new HashSet<String>();
 	//config version
 	protected static int clientsconfigversion = 0;
 
 	//for launcher updater
 	//launcher version
 	private static int lversion = 21;
	//launcher folder update URL
 	//folder structure
 	//{folder}/Laucnher.jar - launcher location
 	//{folder}/version - launcher version
 	//{folder}/jsonclients - client config file
 	//{folder}/clientsversion - clients config file version
 	private static String lupdateurlfolder = "http://download.true-games.org/minecraft/launcher/";
 	//folder in which configurations will be stored
 	private static String configfolder = ".true-games.org/configdata";
 	//folder for error logging
 	public static String errFolder = ".true-games.org/errLog";
	
	
	
 	//config version
 	public static int getClientConfigVersion()
 	{
 		return clientsconfigversion;
 	}
 	//libs and temppath
 	public static String getClientsLibsFolder() 
 	{
 		return libsfolder;
 	}
 	public static String getClientsTempFolderPath()
 	{
 		return tempfolder;
 	}
 	//launch and download
 	public static List<String> getClientsList()
 	{
 		return  new ArrayList<String>(clientsdata.keySet());
 	}
 	public static String getClientFolderByName(String name)
 	{
 		return clientsdata.get(name).getLaunchFolder();
 	}
 	public static String getClientJarByName(String name)
 	{
 		return getClientFolderByName(name) + File.separator + clientsdata.get(name).getJarFile();
 	}
 	public static String getClientMainClassByName(String name)
 	{
 		return clientsdata.get(name).getMainClass();
 	}
 	public static String getClientCmdArgsByName(String name)
 	{
 		return clientsdata.get(name).getCmdArgs();
 	}
 	public static String getClientDownloadLinkByName(String name)
 	{
 		return clientsdata.get(name).getDownloadLink();
 	}
	public static int getClientVersionByName(String name)
 	{
 		return clientsdata.get(name).getVersion();
 	}
 	//auth
 	public static HashSet<String> getAllowedAuthAddresses()
 	{
 		return allowedaddresses;
 	}
 	
 
 	//laucnher vars
 	public static String getLauncherConfigFolderPath()
 	{
 		return configfolder;
 	}
 	
 	public static String getLauncherWebUpdateURLFolder()
 	{
 		return lupdateurlfolder;
 	}
 	public static int getLauncherVerison()
 	{
 		return lversion;
 	}
 
 
 }
