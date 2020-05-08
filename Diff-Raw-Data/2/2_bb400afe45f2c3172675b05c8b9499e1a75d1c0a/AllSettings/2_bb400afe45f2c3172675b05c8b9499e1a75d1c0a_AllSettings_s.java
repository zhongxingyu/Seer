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
 
 package truelauncher;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Scanner;
 
 public class AllSettings {
 	
 	
 	public static void loadConfig() throws FileNotFoundException
 	{
 		final File configfile = new File(LauncherUtils.getDir() + File.separator + AllSettings.getLauncherConfigFolderPath()+File.separator+"clientsconfig");
 		if (configfile.exists())
 		{
 			Scanner in = new Scanner(configfile);
 			int clientsnumber = Integer.valueOf(in.nextLine().split("[=]")[1]);
 			in.nextLine();
 			clientfolders = new String[clientsnumber][5];
 			for (int i = 0; i < clientsnumber; i++)
 			{
 				String client = in.nextLine();
 				client = client.replace("\"", "");
 				clientfolders[i] = client.split("\\,");
 			}
 			in.nextLine();
 			tempfolder = in.nextLine();
 			tempfolder = tempfolder.replace("\"", "");
 			downloadclients = new String[clientsnumber][3];
 			for (int i = 0; i < clientsnumber; i++)
 			{
 				String client = in.nextLine();
 				client = client.replace("\"", "");
 				downloadclients[i] = client.split("\\,");
 			}
 			in.nextLine();
 			while (in.hasNextLine())
 			{
 				String lib = in.nextLine();
 				lib = lib.replace("\"", "");
 				clientlibs.add(lib);
 			}
 			in.close();
 		}
 		else
 		{
 			try {
 				configfile.getParentFile().mkdirs();
 				BufferedInputStream in = new BufferedInputStream(Launcher.class.getResourceAsStream("config/clientsconfig"));
 				BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(configfile));
 				byte[] buf = new byte[4096];
 				int len;
 				while ((len = in.read(buf)) > 0) {
 					out.write(buf, 0, len);
 				}
 				in.close();
 				out.close();
 				loadConfig();
 			}
    			catch (Exception e) {LauncherUtils.logError(e);}
 			//load predefined config from laucnher and reload settings
 			//loadConfig();
 		}
 		//update config
 		Thread update = new Thread(){
 			public void run()
 			{
 				try {
 					URL url = new URL(getLauncherWebUpdateURLFolder()+"clientsconfig");
 					URLConnection conn = url.openConnection();
 
 					if ((conn instanceof HttpURLConnection)) {
 						conn.setRequestProperty("Cache-Control", "no-cache");
 						conn.connect();
 					}
 					InputStream inputstream = conn.getInputStream();
 
 					FileOutputStream writer = new FileOutputStream(configfile);
 					byte[] buffer = new byte[153600];
 
 					int bufferSize = 0;
 					while ((bufferSize = inputstream.read(buffer)) > 0) {
 						writer.write(buffer, 0, bufferSize);
 						buffer = new byte[153600];
 					}
 
 					writer.close();
 					inputstream.close();
 				} catch (Exception e) {LauncherUtils.logError(e);}
 			}
 		};
 		update.start();
 	}
 
 	//For client launch
	//1 - name, 2- launchfolder, 3 - minecraft jar file, 4 - launch type (1 - 1.5.2 and older, 2 - 1.6 and newer), 5 - tweaks present(0 - no , 1 - forge , 2 - forge w/o liteloader, 3 - liteloader w/ forge , for newer launch versions) 
 	private static String[][] clientfolders;
 
 	//For client download
 	//folder in which clients .zip file will be downloaded
 	private static String tempfolder = ".true-games.org/packedclients";
 	//1 - name, 2 - downloadlink, 3 - folderto
 	private static String[][] downloadclients;
 
 	//just a paths to all the libs that minecraft may need (add every lib here that minecraft may need)
 	private static ArrayList<String> clientlibs = new ArrayList<String>();
 
 	//launcher version
 	private static int lversion = 17;
 	//laucnher folder update URL;
 	//folder structure 
 	//{folder}/Laucnher.jar - launcher location
 	//{folder}/version - launcher version
 	private static String lupdateurlfolder = "http://download.true-games.org/minecraft/launcher/";
 	
 	//folder in which configuration will be stored
 	private static String configfolder = ".true-games.org/configdata";
 
 	//main frame size
 	public static int w = 740;
 	public static int h = 340;
 	//images
 	public static String lname = "True-games.org|MinecraftLauncher";
 	public static String icon = "images/icon.png";
 	public static String bgimage = "images/bgimage.png";
 	public static String labelimage = "images/labelbar.png";
 	public static String textimage = "images/textfield.png";
 	public static String explainimage = "images/expbar.png";
 	public static String mclaunchimage = "images/mclaunch.png";
 	public static String close = "images/close.png";
 	public static String hide = "images/hide.png";
 	
 	
 	//folder for error logging
 	public static String errFolder = ".true-games.org/errLog";
 	
 
 	//code to get those values
 	
 	//gui block 2 vars begin
 	public static List<String> getClientsList()
 	{
 
 		List<String> servnames =new ArrayList<String>();
 		for (int i=0; i<clientfolders.length;i++)
 		{
 			servnames.add(clientfolders[i][0]);
 		}
 		return servnames;
 	}
 	
 	public static String getClientFolderByName(String name)
 	{
 		String folder = "minecraft";
 		for (int i=0; i<clientfolders.length;i++)
 		{
 			if (clientfolders[i][0].equals(name))
 			{
 				folder = clientfolders[i][1];
 			}
 		}
 		return folder;
 	}
 	
 	public static String getClientJarByName(String name)
 	{
 		String folder = "fail";
 		for (int i=0; i<clientfolders.length;i++)
 		{
 			if (clientfolders[i][0].equals(name))
 			{
 				folder = clientfolders[i][2];
 			}
 		}
 		return folder;
 	}
 		
 	public static int getClientLaunchVersionByName(String name)
 	{
 		int version = 1;
 		for (int i=0; i<clientfolders.length;i++)
 		{
 			if (clientfolders[i][0].equals(name))
 			{
 				version = Integer.valueOf(clientfolders[i][3]);
 			}
 		}
 		return version;
 	}	
 	
 	public static ArrayList<String> getClientLibs() {
 		return clientlibs;
 	}
 	
 	public static int getClientTweaksType(String name) {
 		int present = 0;
 		for (int i=0; i<clientfolders.length;i++)
 		{
 			if (clientfolders[i][0].equals(name))
 			{
 				present = Integer.valueOf(clientfolders[i][4]);
 			}
 		}
 		// TODO Auto-generated method stub
 		return present;
 	}
 	//gui block 2 vars end
 	
 	
 	//gui block 3 vars begin
 	public static List<String> getClientListDownloads()
 	{
 
 		List<String> servlinks =new ArrayList<String>();
 		for (int i=0; i<downloadclients.length;i++)
 		{
 			servlinks.add(downloadclients[i][0]);
 		}
 		return servlinks;
 	}
 	
 	public static String getClientDownloadLinkByName(String name)
 	{
 		String link = "";
 		for (int i=0; i<downloadclients.length;i++)
 		{
 			if (downloadclients[i][0].equals(name))
 			{
 				link = downloadclients[i][1];
 			}
 		}
 		return link;
 	}
 	
 	public static String getClientUnpackToFolderByName(String name)
 	{
 		String fldto = "";
 		for (int i=0; i<downloadclients.length;i++)
 		{
 			if (downloadclients[i][0].equals(name))
 			{
 				fldto = downloadclients[i][2];
 			}
 		}
 		return fldto;
 	}
 	//gui block 3 vars end
 	
 	//folder for packed clients begin
 	public static String getCientTempFolderPath()
 	{
 		return tempfolder;
 	}
 	//folder for packed clients end
 	
 	
 	//Lacunher vars begin
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
 	//Lacunher vars end
 
 
 }
