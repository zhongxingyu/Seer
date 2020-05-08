 package com.aliyun.LuooFm;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 
 import android.os.Environment;
 import android.util.Log;
 
 
 
 public class HtmlGetter {	
 	
 	public HtmlGetter(){
 		
 	}
 	
	private String getPlayListLocaly(File file) {
 		Log.d("my", "get playList locally");
 		 BufferedReader reader = null;
 		 StringBuffer strBuf = new StringBuffer();
 	        try {
 	            reader = new BufferedReader(new FileReader(file));
 	            String tempString = null;
 	            int line = 1;
 	            while ((tempString = reader.readLine()) != null) {
 	                strBuf.append(tempString);
 	            }
 	            reader.close();
 	        } catch (IOException e) {
 	            e.printStackTrace();
 	        } finally {
 	            if (reader != null) {
 	                try {
 	                    reader.close();
 	                } catch (IOException e1) {
 	                }
 	            }
 	        }
		return strBuf.toString();
 	}
 	public String getPlayListRemote(String url, File playListFile, String vol) throws IOException {
 		playListFile.getParentFile().mkdirs();
 		playListFile.createNewFile();
 		Document doc = Jsoup.connect(url).get();
 		String volTitle = doc.select(".title a").get(0).text();
 		Element post = doc.select(".post").get(0);
 		Elements songsInfo = post.select(".post-txt p");
 		String cover = songsInfo.get(0).getElementsByTag("img").get(0).attr("src");		
 		PlayList playList = new PlayList(cover, vol, volTitle);		
 		int  j = 0;
 		for (int i = 3; i < songsInfo.size(); i = i+ 2){
 			String poster = songsInfo.get(i).getElementsByTag("img").get(0).attr("src");
 			String info = songsInfo.get(i + 1).text();
 			String title = info.substring(0, info.indexOf("–"));
 			String airtist = info.substring(info.indexOf("–"), info.length());
 			j = j + 1;
 			playList.appendSong( j, title, airtist, poster);
 		}
 		//save list file to sdcard
 		FileOutputStream fos = new FileOutputStream(playListFile, true);
 		fos.write(playList.toString().getBytes());
 		fos.close();
 		return playList.toString();
 	}
 	public String getPlayList(int index) throws IOException{
 		if (index < 0){
 			index = 0;
 		}else if (index > 5){
 			index = 5;
 		}
 		Document doc = Jsoup.connect("http://luoo.net/").get();
 		Element nowVol = doc.select("#sidebar li:eq(" + index + ")").get(0);
 		Element li = nowVol.getElementsByTag("a").get(0);
 		String vol = li.attr("href").replace("http://www.luoo.net/", "").replaceAll("/","");
 		String sdCardDir = Environment.getExternalStorageDirectory() + "/LuooFm/vol" + vol + "/";
 		File playListFile = new File(sdCardDir + "playList.json");
 		if (playListFile.exists()){
			return this.getPlayListLocaly(playListFile);
 		}else{
 			return this.getPlayListRemote(li.attr("href"), playListFile, vol);
 		}		
 	}
 }
