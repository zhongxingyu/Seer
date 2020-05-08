 /*******************************************************************************
  * Copyright (c) 2012 rmateus.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v2.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
  ******************************************************************************/
 package cm.aptoide.pt.services;
 
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Binder;
 import android.os.Environment;
 import android.os.IBinder;
 import cm.aptoide.pt.*;
 import cm.aptoide.pt.Server.State;
 import cm.aptoide.pt.exceptions.AptoideException;
 import cm.aptoide.pt.util.NetworkUtils;
 import cm.aptoide.pt.util.RepoUtils;
 import org.json.JSONObject;
 
 import java.io.*;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 
 public class MainService extends Service {
 //	Database db;
 	private static boolean isParsing = false;
 	String defaultPath = Environment.getExternalStorageDirectory().getAbsolutePath();
 	String defaultXmlPath = defaultPath+"/.aptoide/info.xml";
 	String defaultTopXmlPath = defaultPath+"/.aptoide/top.xml";
 	String defaultLatestXmlPath = defaultPath+"/.aptoide/latest.xml";
 	String defaultExtrasXmlPath = defaultPath+"/.aptoide/extras.xml";
 	String defaultBootConfigXmlPath = defaultPath+"/.aptoide/boot_config.xml";
 
 	static ArrayList<String> serversParsing = new ArrayList<String>();
 	@Override
 	public IBinder onBind(Intent intent) {
 		registerReceiver(receiver , new IntentFilter("complete"));
 		return new LocalBinder();
 	}
 	private BroadcastReceiver receiver = new BroadcastReceiver() {
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			try{
 				serversParsing.remove(intent.getStringExtra("server"));
 			}catch (Exception e){
 				e.printStackTrace();
 			}
 
 		}
 	};
 
 
 	public class LocalBinder extends Binder{
 		public MainService getService(){
 			return MainService.this;
 		}
 	}
 
 //	public void parse(final Database db){
 //		new Thread() {
 //
 //
 //			public void run() {
 //
 //				ArrayList<Server> servers = ;
 //
 //				try {
 //					for(Server server : servers){
 //						if(serversParsing.get((int)server.id)==null){
 //
 //						}
 //
 //					}
 //					sendBroadcast(new Intent("starting"));
 //				} catch (Exception e) {
 //					e.printStackTrace();
 //				}
 //			};
 //		}.start();
 //	}
 
 	public String get(Server server,String xmlpath,String what, boolean delta) throws MalformedURLException, IOException{
 		getApplicationContext().sendBroadcast(new Intent("connecting"));
 		String hash = "";
 
 		if (delta&&server.hash.length() > 0) {
 			hash = "?hash=" + server.hash;
 		}
 		NetworkUtils utils = new NetworkUtils();
 		if(delta&&utils.checkServerConnection(server.url, server.getLogin().getUsername(),server.getLogin().getPassword())==401){
 			throw new AptoideException("401", new IOException());
 		}
 		String url = server.url + what + hash;
 
 
 		System.out.println(server);
 		System.out.println(server.getClass().getCanonicalName());
 		File f = new File(xmlpath);
 		InputStream in = utils.getInputStream(
 				url,
 				server.getLogin().getUsername(),
 				server.getLogin().getPassword(),
 				getApplicationContext());
 
 		int i = 0;
 		while (f.exists()) {
 			f = new File(xmlpath + i++);
 		}
 		FileOutputStream out = new FileOutputStream(f);
 
 		byte[] buffer = new byte[1024];
 		int len;
 		getApplicationContext().sendBroadcast(new Intent("downloading"));
 		while ((len = in.read(buffer)) != -1) {
 			out.write(buffer, 0, len);
 		}
 		out.close();
 
 		return f.getAbsolutePath();
 	}
 
 //
 //	public String getTop(Server server,String xmlpath) throws MalformedURLException, IOException{
 //		File f = new File(xmlpath);
 //			getApplicationContext().sendBroadcast(new Intent("connecting"));
 //			String url = server.url + "top.xml";
 //			System.out.println(url);
 //	    	InputStream in = getInputStream(new URL(url),server.username,server.password);
 //
 //
 //	    	int i = 0;
 //	    	while(f.exists()){
 //	    		f = new File(xmlpath+i++);
 //	    	}
 //			FileOutputStream out = new FileOutputStream(f);
 //
 //			byte[] buffer = new byte[1024];
 //			int len;
 //			getApplicationContext().sendBroadcast(new Intent("downloading"));
 //			while ((len = in.read(buffer)) != -1) {
 //				out.write(buffer, 0, len);
 //			}
 //			out.close();
 //
 //		return f.getAbsolutePath();
 //    }
 //
 //	public String getLatest(Server server,String xmlpath) throws MalformedURLException, IOException{
 //		File f = new File(xmlpath);
 //			getApplicationContext().sendBroadcast(new Intent("connecting"));
 //			String url = server.url + "latest.xml";
 //			System.out.println(url);
 //	    	InputStream in = getInputStream(new URL(url),server.username,server.password);
 //
 //
 //	    	int i = 0;
 //	    	while(f.exists()){
 //	    		f = new File(xmlpath+i++);
 //	    	}
 //			FileOutputStream out = new FileOutputStream(f);
 //
 //			byte[] buffer = new byte[1024];
 //			int len;
 //			getApplicationContext().sendBroadcast(new Intent("downloading"));
 //			while ((len = in.read(buffer)) != -1) {
 //				out.write(buffer, 0, len);
 //			}
 //			out.close();
 //
 //		return f.getAbsolutePath();
 //    }
 
 	public boolean isParsing() {
 		return isParsing ;
 	}
 
 	@Override
 	public void onDestroy() {
 		unregisterReceiver(receiver);
 		System.out.println("MainService OnDestroy");
 		try{
 			File[] files = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/.aptoide").listFiles();
 
 			for(File file : files){
 				if(file.getName().endsWith(".xml")&&!file.getName().contains("servers.xml")){
 					file.delete();
 				}
 			}
 		}catch (Exception e){
 			e.printStackTrace();
 		}
 		super.onDestroy();
 	}
 
 	public void addStore(Database db, String uri_str, String username, String password) {
 		Server server = null;
 		try{
 			if(db.getServer(uri_str)!=null){
 				return;
 			}
 			db.addStore(uri_str,username,password);
 			server = db.getServer(uri_str);
 
 			if(ApplicationAptoide.DEFAULTSTORE != null && uri_str.equals("http://" + ApplicationAptoide.DEFAULTSTORE + ".store.aptoide.com/")){
 				server.oem = true;
 				db.addStoreInfo(ApplicationAptoide.STOREAVATAR, ApplicationAptoide.STORENAME, "0", ApplicationAptoide.STORETHEME, ApplicationAptoide.STOREDESCRIPTION, ApplicationAptoide.STOREVIEW, ApplicationAptoide.STOREITEMS, db.getServer("http://" + ApplicationAptoide.DEFAULTSTORE + ".store.aptoide.com/").id);
 			}else{
 				db.addStoreInfo("",RepoUtils.split(server.url),"0","","","","",server.id);
 			}
 
 			parseServer(db, server);
 		} catch (Exception e){
 			e.printStackTrace();
 		}
 	}
 
 	public void parseServer(final Database db, final Server server) throws MalformedURLException, IOException {
 
 
 		if(!serversParsing.contains(server.url)){
             if(server.hash.equals("firstHash")){
                 server.state=State.QUEUED;
                 db.updateStatus(server);
             }
 			new Thread(new Runnable() {
 
 				@Override
 				public void run() {
 						addStoreInfo(db, server);
 //						parseBootConfig(db, server);
 						parseTop(db, server);
 						parseLatest(db, server);
 					try{
 						parseInfoXml(db, server);
 					} catch (AptoideException e){
 						Intent i = new Intent("401");
 						i.putExtra("url", server.url);
 						getApplicationContext().sendBroadcast(i);
 						serversParsing.remove(server.url);
 					}catch (IOException e){
 						server.state=State.FAILED;
 						db.updateStatus(server);
 						serversParsing.remove(server.url);
 						e.printStackTrace();
 					}
 				}
 			}).start();
 
 			serversParsing.add(server.url);
 		}
 	}
 
 //	private void parseBootConfig(final Database db, final Server server) {
 //		new Thread(new Runnable() {
 //			public void run() {
 //				String path;
 //				try {
 //					//			serversParsing.put((int)server.id, server);
 //					path = get(server, defaultBootConfigXmlPath, "boot_config.xml", false);
 //					SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
 //
 //					parser.parse(new File(path), new DefaultHandler(){
 //						StringBuilder sb = new StringBuilder();
 //						String avatar;
 //						String theme;
 //						String description;
 //						String view;
 //						String items;
 //
 //						public void startElement(String uri, String localName, String qName, org.xml.sax.Attributes attributes) throws SAXException {
 //							sb.setLength(0);
 //						};
 //
 //						public void characters(char[] ch, int start, int length) throws SAXException {
 //							sb.append(ch,start,length);
 //						};
 //
 //						public void endElement(String uri, String localName, String qName) throws SAXException {
 //
 //							ApplicationAptoide.StoreElements element;
 //							try{
 //								element = StoreElements.valueOf(localName);
 //							}catch (Exception e) {
 //								element = StoreElements.none;
 //							}
 //
 //							switch (element) {
 //							case avatar:
 //								this.avatar = sb.toString();
 //								break;
 //							case description:
 //								this.description = sb.toString();
 //								Log.d("MainService-bootconfig-parser: description", description);
 //								break;
 //							case items:
 //								this.items = sb.toString();
 //								break;
 //							case theme:
 //								this.theme = sb.toString();
 //								Log.d("MainService-bootconfig-parser: theme", theme);
 //								break;
 //							case view:
 //								this.view = sb.toString();
 //								break;
 //							case storeconf:
 //								db.updateStoreInfo(avatar, theme, description, view, items, server.id);
 //								break;
 //							default:
 //								break;
 //							}
 //
 //
 //						};
 //
 //
 //
 //
 //					});
 //
 //				} catch (MalformedURLException e) {
 //					e.printStackTrace();
 //				} catch (IOException e) {
 //					e.printStackTrace();
 //				} catch (ParserConfigurationException e) {
 //					// TODO Auto-generated catch block
 //					e.printStackTrace();
 //				} catch (SAXException e) {
 //					// TODO Auto-generated catch block
 //					e.printStackTrace();
 //				}
 //			}
 //		}).start();
 //
 //
 //
 //	}
 
 
 
 	public boolean deleteStore(Database db, long id){
 		if(!serversParsing.contains(db.getServer(id, false).url)){
 			db.deleteServer(id,true);
 			return true;
 		}
 		return false;
 	}
 
 	public void parseTop(final Database db, final Server server) {
 		new Thread(new Runnable() {
 			public void run() {
 				String path;
 				try {
 					//			serversParsing.put((int)server.id, server);
 
                     NetworkUtils utils = new NetworkUtils();
 
                     long lastModified = utils.getLastModified(new URL(server.url + "top.xml"));
 
                     if(Long.parseLong(db.getRepoHash(server.id, Category.TOP)) < lastModified){
 
                         path = get(server, defaultTopXmlPath, "top.xml", false);
 
                         Server serverTop = new ServerTop(server);
 
                         serverTop.hash = lastModified + "";
 
                         RepoParser.getInstance(db).parseTop(path, serverTop);
 
                     }
 				} catch (MalformedURLException e) {
 					e.printStackTrace();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}).start();
 
 	}
 
 	public void parseExtras(final Server server){
 		new Thread(new Runnable() {
 			public void run() {
 				String path;
 				try {
 					path = get(server, defaultExtrasXmlPath, "extras.xml", true);
 					Intent service = new Intent(MainService.this, ExtrasService.class);
 					ArrayList<String> array = new ArrayList<String>();
 					array.add(path);
 					service.putExtra("path", array);
 					startService(service);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}).start();
 	}
 
 	public void parseLatest(final Database db, final Server server){
 			new Thread(new Runnable() {
 
 				public void run() {
 					String path;
 					//			serversParsing.put((int)server.id, server);
 					try {
 
                         NetworkUtils utils = new NetworkUtils();
 
                         long lastModified = utils.getLastModified(new URL(server.url + "latest.xml"));
 


                         if(Long.parseLong(db.getRepoHash(server.id, Category.LATEST)) < lastModified){
 
 						    path = get(server, defaultLatestXmlPath, "latest.xml", false);
                             Server serverLatest = new ServerLatest(server);
                             serverLatest.hash = lastModified + "";
 
                             RepoParser.getInstance(db).parseLatest(path, serverLatest);
 
 
                         }
 					} catch (MalformedURLException e) {
 						e.printStackTrace();
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 
 				}
 			}).start();
 
 	}
 
 	public void parseInfoXml(final Database db, final Server server) throws MalformedURLException, IOException{
 		String path = null;
 		path = get(server,defaultXmlPath,"info.xml",true);
 		RepoParser.getInstance(db).parseInfoXML(path,server);
 		parseExtras(server);
 	}
 
 	public void addStoreInfo(Database db, Server server) {
 		try {
 			HttpURLConnection connection = (HttpURLConnection) new URL(
 					String.format("http://webservices.aptoide.com/webservices/getRepositoryInfo/%s/json",
 							RepoUtils.split(server.url))).openConnection();
 			connection.connect();
 			int rc = connection.getResponseCode();
 			if (rc == 200) {
 				String line = null;
 				BufferedReader br = new BufferedReader(
 						new java.io.InputStreamReader(connection
 								.getInputStream()));
 				StringBuilder sb = new StringBuilder();
 				while ((line = br.readLine()) != null)
 					sb.append(line + '\n');
 
 				JSONObject json = new JSONObject(sb.toString());
 				JSONObject array = json.getJSONObject("listing");
 				String avatar = array.getString("avatar");
 				String name = array.getString("name");
 				String downloads = array.getString("downloads");
 				String theme = array.getString("theme");
 				String description = array.getString("description");
 				String view = array.getString("view");
 				String items = array.getString("items");
 				db.addStoreInfo(avatar,name,withSuffix(downloads),theme,description,view,items,server.id);
 			}
 			connection.disconnect();
 		} catch (Exception e) {
 			e.printStackTrace();
 			if(server.oem){
 				db.addStoreInfo(ApplicationAptoide.STOREAVATAR, ApplicationAptoide.STORENAME, "0", ApplicationAptoide.STORETHEME, ApplicationAptoide.STOREDESCRIPTION, ApplicationAptoide.STOREVIEW, ApplicationAptoide.STOREITEMS, db.getServer("http://" + ApplicationAptoide.DEFAULTSTORE + ".store.aptoide.com/").id);
 			}else{
 				db.addStoreInfo("",RepoUtils.split(server.url),"0","","","","",server.id);
 			}
 
 		}
 
 
 	}
 	public static String withSuffix(String input) {
 		long count = Long.parseLong(input);
 	    if (count < 1000) return "" + count;
 	    int exp = (int) (Math.log(count) / Math.log(1000));
 	    return String.format("%.1f %c",
 	                         count / Math.pow(1000, exp),
 	                         "kMGTPE".charAt(exp-1));
 	}
 }
