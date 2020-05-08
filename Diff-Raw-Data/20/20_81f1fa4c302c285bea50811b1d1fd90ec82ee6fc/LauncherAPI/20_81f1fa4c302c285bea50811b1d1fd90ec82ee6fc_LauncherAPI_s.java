 package com.kokakiwi.mclauncher.api;
 
 import java.awt.Image;
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.URL;
import java.nio.MappedByteBuffer;
 import java.nio.channels.FileChannel;
 import java.nio.charset.Charset;
 import java.security.PublicKey;
 import java.security.cert.Certificate;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.net.ssl.HttpsURLConnection;
 
 import com.kokakiwi.mclauncher.MCLauncher;
 import com.kokakiwi.mclauncher.api.ui.Theme;
 import com.kokakiwi.mclauncher.core.Launcher;
 import com.kokakiwi.mclauncher.core.Loginer;
 import com.kokakiwi.mclauncher.core.TimeLine;
 import com.kokakiwi.mclauncher.core.Updater;
 import com.kokakiwi.mclauncher.ui.LauncherFrame;
 import com.kokakiwi.mclauncher.ui.simple.SimpleTheme;
 import com.kokakiwi.mclauncher.utils.ConfigList;
 import com.kokakiwi.mclauncher.utils.Configuration;
 import com.kokakiwi.mclauncher.utils.MCLogger;
 import com.kokakiwi.mclauncher.utils.SystemUtils;
 
 public class LauncherAPI
 {
     private final MCLauncher main;
     
     public LauncherAPI(MCLauncher main)
     {
         this.main = main;
     }
     
     // Delegate methods
     
     public MCLauncher getMain()
     {
         return main;
     }
     
     public TimeLine getTimeLine()
     {
         return main.getTimeLine();
     }
     
     public Configuration getConfig()
     {
         return main.getConfig();
     }
     
     public Loginer getLoginer()
     {
         return main.getLoginer();
     }
     
     public Updater getUpdater()
     {
         return main.getUpdater();
     }
     
     public Launcher getLauncher()
     {
         return main.getLauncher();
     }
     
     public LauncherFrame getFrame()
     {
         return main.getFrame();
     }
     
     public Theme getTheme()
     {
         return main.getTheme();
     }
     
     public Image getBackground()
     {
         Image background = null;
         
         try
         {
             background = main.getTheme().getBackground();
         }
         catch (final Exception e)
         {
             e.printStackTrace();
         }
         
         return background;
     }
     
     // Utils
     
     public File getLauncherDirectory()
     {
         File dir = null;
         File root = new File(System.getProperty("user.home", ".") + "/");
         final String appName = "bdh2/";
         
         
         switch (SystemUtils.getSystemOS())
         {
             case linux:
             case solaris:
                 dir = new File(root, "." + appName + "/");
                 break;
             
             case macosx:
                 dir = new File(root, "Library/Application Support/" + appName);
                 break;
             
             case windows:
                 final String applicationData = System.getenv("APPDATA");
                 if (applicationData != null
                         && !main.getConfig().getBoolean(
                                 "game.folder.customFolder"))
                 {
                     root = new File(applicationData);
                 }
                 
                 dir = new File(root, '.' + appName + '/');
                 break;
             
             default:
                 dir = new File(root, appName + '/');
                 break;
         }
         
         if (dir != null && !dir.exists() && !new File(dir, "bin").mkdirs())
         {
             throw new RuntimeException(
                     "The working directory could not be created: " + dir);
         }
         
         return dir;
     }
     
     public boolean deleteGame(String name)
     {
     	Configuration c = this.getConfigList().getConfig(name);
     	if(c != null)
     	{
 	    	File a = this.getMinecraftDirectory(c);
 	    	return this.deleteDir(a);
     	} 
     	return false;
     }
     
     public boolean deleteDir(File dir) {
         if (dir.isDirectory()) {
             String[] children = dir.list();
             for (int i=0; i<children.length; i++) {
                 boolean success = deleteDir(new File(dir, children[i]));
                 if (!success) {
                     return false;
                 }
             }
         }
 
         // The directory is now empty so delete it
         return dir.delete();
     }
     
     public File getMinecraftDirectory()
     {
     	return this.getMinecraftDirectory(main.getConfig());
     }
     
     public File getMinecraftDirectory(Configuration c)
     {
         File root = this.getLauncherDirectory();
         if (c.getBoolean("game.folder.customFolder"))
         {
             String customFolder = c.getString("game.folder.gameDir");
             if (customFolder != null)
             {
                 customFolder = customFolder.replace("{ROOT}",
                         SystemUtils.getExecDirectoryPath());
                 root = new File(customFolder).getAbsoluteFile();
             }
         }
         
         File dir = new File(root,c.getString(
                 "game.folder.folderName"));
         
         if (dir != null && !dir.exists() && !new File(dir, "bin").mkdirs())
         {
             throw new RuntimeException(
                     "The working directory could not be created: " + dir);
         }
         
         return dir;
     }
     
     public String postUrl(String dest, Map<String, String> params)
     {
         return postUrl(dest, params, null);
     }
     
     public String postUrl(String dest, Map<String, String> params,
             String keyFile)
     {
         String result = null;
         HttpURLConnection connection = null;
         
         try
         {
             final URL url = new URL(dest);
             connection = (HttpURLConnection) url.openConnection();
             
             connection.setRequestMethod("POST");
             connection.setRequestProperty("Content-Type",
                     "application/x-www-form-urlencoded");
             
             final StringBuffer sb = new StringBuffer();
             final Iterator<Entry<String, String>> iterator = params.entrySet()
                     .iterator();
             while (iterator.hasNext())
             {
                 final Entry<String, String> param = iterator.next();
                 
                 sb.append(param.getKey());
                 sb.append('=');
                 sb.append(param.getValue());
                 
                 if (iterator.hasNext())
                 {
                     sb.append('&');
                 }
             }
             
             connection.setRequestProperty("Content-Length",
                     Integer.toString(sb.toString().getBytes().length));
             connection.setRequestProperty("Content-Language", "en-US");
             
             connection.setUseCaches(false);
             connection.setDoInput(true);
             connection.setDoOutput(true);
             
             connection.connect();
             
             if (url.getProtocol().contains("https") && keyFile != null)
             {
                 final Certificate[] certs = ((HttpsURLConnection) connection)
                         .getServerCertificates();
                 
                 final byte[] bytes = new byte[294];
                 final DataInputStream dis = new DataInputStream(
                         LauncherAPI.class
                                 .getResourceAsStream("keys/" + keyFile));
                 dis.readFully(bytes);
                 dis.close();
                 
                 final Certificate c = certs[0];
                 final PublicKey pk = c.getPublicKey();
                 final byte[] data = pk.getEncoded();
                 
                 for (int i = 0; i < data.length; i++)
                 {
                     if (data[i] == bytes[i])
                     {
                         continue;
                     }
                     throw new RuntimeException("Public key mismatch");
                 }
             }
             
             final DataOutputStream out = new DataOutputStream(
                     connection.getOutputStream());
             out.writeBytes(sb.toString());
             out.flush();
             out.close();
             
             final InputStream in = connection.getInputStream();
             final BufferedReader reader = new BufferedReader(
                     new InputStreamReader(in));
             
             final StringBuffer response = new StringBuffer();
             String line;
             while ((line = reader.readLine()) != null)
             {
                 response.append(line);
             }
             reader.close();
             
             result = response.toString();
         }
         catch (final Exception e)
         {
             e.printStackTrace();
         }
         
         if (connection != null)
         {
             connection.disconnect();
         }
         
         return result;
     }
     
     public String getUrl(String dest)
     {
         return getUrl(dest, null);
     }
     
     public String getUrl(String dest, String keyFile)
     {
         String result = null;
         HttpURLConnection connection = null;
         
         try
         {
             final URL url = new URL(dest);
             connection = (HttpURLConnection) url.openConnection();
             
             connection.setRequestMethod("GET");
             
             connection.setUseCaches(false);
             connection.setDoInput(true);
             connection.setDoOutput(true);
             
             connection.connect();
             
             if (url.getProtocol().contains("https") && keyFile != null)
             {
                 final Certificate[] certs = ((HttpsURLConnection) connection)
                         .getServerCertificates();
                 
                 final byte[] bytes = new byte[294];
                 final DataInputStream dis = new DataInputStream(
                         LauncherAPI.class
                                 .getResourceAsStream("keys/" + keyFile));
                 dis.readFully(bytes);
                 dis.close();
                 
                 final Certificate c = certs[0];
                 final PublicKey pk = c.getPublicKey();
                 final byte[] data = pk.getEncoded();
                 
                 for (int i = 0; i < data.length; i++)
                 {
                     if (data[i] == bytes[i])
                     {
                         continue;
                     }
                     throw new RuntimeException("Public key mismatch");
                 }
             }
             
             final InputStream in = connection.getInputStream();
             final BufferedReader reader = new BufferedReader(
                     new InputStreamReader(in));
             
             final StringBuffer response = new StringBuffer();
             String line;
             while ((line = reader.readLine()) != null)
             {
                 response.append(line);
             }
             reader.close();
             
             result = response.toString();
         }
         catch (final Exception e)
         {
             e.printStackTrace();
         }
         
         if (connection != null)
         {
             connection.disconnect();
         }
         
         return result;
     }
 
 	public ConfigList getConfigList() 
 	{
 		return main.cl;
 	}
 	
 	public String readFile(File file) throws Exception
     {
     	String str = "";
 		FileInputStream stream = new FileInputStream(file);
 		try {
 		  FileChannel fc = stream.getChannel();
		  MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
 		  str = Charset.defaultCharset().decode(bb).toString();
 		}
 		finally {
 		  stream.close();
 		}
 		
         return str;
     }
 	
     public void writeFile(File file, String text) throws Exception
     {
         if (!file.exists())
         {
             file.createNewFile();
         }
         final DataOutputStream dos = new DataOutputStream(new FileOutputStream(
                 file));
         dos.write(text.getBytes());
         dos.close();
     }
 	
 	public String executePost(String targetURL, String urlParameters,
             String keyFileName,boolean multiline)
     {
         final String protocol = targetURL.substring(4);
         HttpURLConnection connection = null;
         try
         {
             final URL url = new URL(targetURL);
             if (protocol.contains("https"))
             {
                 connection = (HttpsURLConnection) url.openConnection();
             }
             else
             {
                 connection = (HttpURLConnection) url.openConnection();
             }
             connection.setRequestMethod("POST");
             connection.setRequestProperty("Content-Type",
                     "application/x-www-form-urlencoded");
             
             connection.setRequestProperty("Content-Length",
                     Integer.toString(urlParameters.getBytes().length));
             connection.setRequestProperty("Content-Language", "en-US");
             
             connection.setUseCaches(false);
             connection.setDoInput(true);
             connection.setDoOutput(true);
             
             connection.connect();
             
             if (protocol.contains("https"))
             {
                 final Certificate[] certs = ((HttpsURLConnection) connection)
                         .getServerCertificates();
                 
                 final byte[] bytes = new byte[294];
                 final DataInputStream dis = new DataInputStream(
                         getResourceAsStream("keys/" + keyFileName));
                 dis.readFully(bytes);
                 dis.close();
                 
                 final Certificate c = certs[0];
                 final PublicKey pk = c.getPublicKey();
                 final byte[] data = pk.getEncoded();
                 
                 for (int i = 0; i < data.length; i++)
                 {
                     if (data[i] == bytes[i])
                     {
                         continue;
                     }
                     throw new RuntimeException("Public key mismatch");
                 }
             }
             
             final DataOutputStream wr = new DataOutputStream(
                     connection.getOutputStream());
             wr.writeBytes(urlParameters);
             wr.flush();
             wr.close();
             
             final InputStream is = connection.getInputStream();
             final BufferedReader rd = new BufferedReader(new InputStreamReader(
                     is));
             
             final StringBuffer response = new StringBuffer();
             String line;
             while ((line = rd.readLine()) != null)
             {
                 response.append(line);
                 if(multiline == true)
                 	response.append("\r\n");
                 	
             }
             rd.close();
             
             final String str1 = response.toString();
             return str1;
         }
         catch (final Exception e)
         {
             MCLogger.debug(e.getLocalizedMessage());
             return null;
         }
         finally
         {
             if (connection != null)
             {
                 connection.disconnect();
             }
         }
     }
 	
 	public static InputStream getResourceAsStream(String url)
     {
         return SimpleTheme.class.getResourceAsStream("/"+url);
     }
 }
