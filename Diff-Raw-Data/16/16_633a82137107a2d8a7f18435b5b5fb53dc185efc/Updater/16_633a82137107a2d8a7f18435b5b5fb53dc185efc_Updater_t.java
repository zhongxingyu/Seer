 package com.araeosia.ArcherGames.utils;
 
 import com.araeosia.ArcherGames.ArcherGames;
 import com.google.gson.Gson;
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.HashMap;
 
 public class Updater {
 
 	private static ArcherGames plugin;
 	private String address = "http://znc.araeosia.com/plugins/versions.php";
 	private String updatePath = "plugins" + File.separator + "ArcherGames.jar";
 
 	public Updater(ArcherGames plugin) {
 		this.plugin = plugin;
 	}
 
 	public String fetchDataFromURL(String URL) throws IOException {
 		URL url = new URL(URL);
 		BufferedReader in = new BufferedReader(
 				new InputStreamReader(url.openStream()));
 
 		String inputLine;
 		while ((inputLine = in.readLine()) != null) {
 			inputLine += inputLine;
 		}
 		in.close();
 		return inputLine;
 	}
 
 	public Boolean updateCheck() {
 		String JSONString = "";
 		try {
 			JSONString = fetchDataFromURL(address);
			Gson gson = new Gson();
			HashMap<String, HashMap<String, HashMap<String, String>>> data = gson.fromJson(JSONString, HashMap.class);
			int version = Integer.parseInt(data.get("plugins").get("ArcherGames").get("version"));
			if (version != plugin.getConfig().getInt("ArcherGames.technical.version")) {
				download(data.get("plugins").get("ArcherGames").get("download"));
				return true;
			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 	public void download(String locationAddress) {
 		OutputStream out = null;
 		URLConnection conn = null;
 		InputStream in = null;
 		try {
 			URL url = new URL(locationAddress);
 			out = new BufferedOutputStream(new FileOutputStream(updatePath));
 			conn = url.openConnection();
 			in = conn.getInputStream();
 			byte[] buffer = new byte[1024];
 			int numRead;
 			long numWritten = 0;
 			while ((numRead = in.read(buffer)) != -1) {
 				out.write(buffer, 0, numRead);
 				numWritten += numRead;
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				if (in != null) {
 					in.close();
 				}
 				if (out != null) {
 					out.close();
 				}
 			} catch (IOException ioe) {
 				ioe.printStackTrace();
 			}
 		}
 	}
 }
