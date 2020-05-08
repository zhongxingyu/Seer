 package org.nosoft.bddownloader;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.nio.channels.Channels;
 import java.nio.channels.ReadableByteChannel;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.script.ScriptEngine;
 import javax.script.ScriptEngineManager;
 import javax.script.ScriptException;
 
 import com.fasterxml.jackson.core.JsonParser;
 import com.fasterxml.jackson.core.type.TypeReference;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.mpatric.mp3agic.ID3v1;
 import com.mpatric.mp3agic.ID3v1Tag;
 import com.mpatric.mp3agic.ID3v2;
 import com.mpatric.mp3agic.ID3v24Tag;
 import com.mpatric.mp3agic.InvalidDataException;
 import com.mpatric.mp3agic.Mp3File;
 import com.mpatric.mp3agic.NotSupportedException;
 import com.mpatric.mp3agic.UnsupportedTagException;
 
 
 
 public class BDDownloader {
 
 	
 	
 	public static int THREAD_POOL_SIZE = 4; 
 	ExecutorService executorService;
 	BlockingQueue<DownloadTask> taskQueue;
 	ConcurrentMap<String,String> reports;
 	private MonitorThread monitorThread = null;
 	
     Pattern pattern = Pattern.compile(
 	        "# Match a valid Windows filename (unspecified file system).          \n" +
 	        "^                                # Anchor to start of string.        \n" +
 	        "(?!                              # Assert filename is not: CON, PRN, \n" +
 	        "  (?:                            # AUX, NUL, COM1, COM2, COM3, COM4, \n" +
 	        "    CON|PRN|AUX|NUL|             # COM5, COM6, COM7, COM8, COM9,     \n" +
 	        "    COM[1-9]|LPT[1-9]            # LPT1, LPT2, LPT3, LPT4, LPT5,     \n" +
 	        "  )                              # LPT6, LPT7, LPT8, and LPT9...     \n" +
 	        "  (?:\\.[^.]*)?                  # followed by optional extension    \n" +
 	        "  $                              # and end of string                 \n" +
 	        ")                                # End negative lookahead assertion. \n" +
 	        "[^<>:\"/\\\\|?*\\x00-\\x1F]*     # Zero or more valid filename chars.\n" +
 	        "[^<>:\"/\\\\|?*\\x00-\\x1F\\ .]  # Last char is not a space or dot.  \n" +
 	        "$                                # Anchor to end of string.            ", 
 	        Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.COMMENTS);
 
 	
 	/**
 	 * @param args
 	 * @throws FileNotFoundException 
 	 * @throws ScriptException 
 	 */
 	public static void main(String[] args) throws FileNotFoundException, ScriptException {
 
 		System.out.println("BDDownloader v0.0.1");
 				
 		if (args.length !=1 ) {
 			System.out.println("please give me the url of the album.");		
 			System.exit(0);
 
 		}
 		new BDDownloader(args[0]);
 		 
 	}
 	
 	public BDDownloader(String url) {
 
 		System.out.println("Fetching album data...");
 		
 		taskQueue = new LinkedBlockingQueue<DownloadTask>();  
 		executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
 		reports = new ConcurrentHashMap<String,String>();
 		
 		monitorThread = new MonitorThread(reports);
 		monitorThread.start();
 		
 		List<DownloaderConsumer> workerList = new ArrayList<DownloaderConsumer>();
 		for (int i=0; i!=THREAD_POOL_SIZE; i++)
 			workerList.add(new DownloaderConsumer(taskQueue, reports));
 		
 		
 		
 		URL website;
 		try {
 			website = new URL(url);
 			ReadableByteChannel rbc1 = Channels.newChannel(website.openStream());
 		    FileOutputStream fos1 = new FileOutputStream("temp.html");
 		    fos1.getChannel().transferFrom(rbc1, 0, Long.MAX_VALUE);	
 
 		    rbc1.close();
 		    
 		    InputStream    fis;
 		    BufferedReader br;
 		    String         line;
 		    
 		    fis = new FileInputStream("temp.html");		    
 		    br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
 		    
 		    StringBuffer sb = null;
 		    boolean keepReading = false;
 		    List<String> vars = new ArrayList<String>();
 		    
 		    while ((line = br.readLine()) != null) {
 		    	line = line.trim();
 		    	if (line.startsWith("var ") ) {
 		    		sb = new StringBuffer();
 		    		sb.append(line.trim());
 		    		if (!line.endsWith("};") ) { 
 		    			keepReading = true; 
 		    			} else {
 		    				vars.add(sb.toString());
 		    			}
 		    		
 		    	} else if (keepReading) {
 		    		if ( (!line.startsWith("//"))  && (!line.startsWith("url")) && (!line.startsWith("linkback")) )
 		    			sb.append(line);
 		    		
 		    		if (line.endsWith("};")) { 
 		    				keepReading = false;
 		    				vars.add(sb.toString());
 		    		}
 		    	}
 		    	
 		    }
 
 		    // Done with the file
 		    br.close();
 		    
 		    sb = new StringBuffer();
 		    Map<String,String> jsonMap = new HashMap<String,String >();
 		    
 		    Pattern p = Pattern.compile("(?i)(var\\s)(.+?)(\\s=\\s)(.+?)(;)");
 		    for (String var : vars) {
 		    	Matcher m = p.matcher(var);
 		    	if (m.find())  {
 		    		jsonMap.put(m.group(2), m.group(4));			    			
 		    	}
 		    	sb.append(var);
 		    }
 
 		    Album album = new Album();
 			  
 		    ObjectMapper mapper = new ObjectMapper(); // can reuse, share globally
 		    mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
 		    mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
 		    mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
 		    
 		    
 		   	
 		    Map<String,Object> parsedEmbedData = mapper.readValue(jsonMap.get("EmbedData"), 
 		    		new TypeReference<Map<String,Object>>() { });
 		
 		    album.setTitle(parsedEmbedData.get("album_title").toString());
 		  
 		    
 		
 		    
 		    Map<String,Object> result = mapper.readValue(jsonMap.get("TralbumData"), 
 		    		new TypeReference<Map<String,Object>>() { });
 		    
 		   List<Map<String,Object>> t = (List<Map<String, Object>>) result.get("trackinfo");
 		   album.setArtist( result.get("artist").toString());
 		   album.setConverUrl( result.get("artFullsizeUrl").toString());
 		   
 		   List<Song> songs = new ArrayList<Song>(); 
 		   
 		   for (Map<String,Object> tt : t) {
 			   Song song = new Song();
 			   song.setTitle(tt.get("title").toString());
 			   song.setUrl(((Map<String,Object>)tt.get("file")).get("mp3-128").toString());
 			   song.setTrackNumber(Integer.parseInt(tt.get("track_num").toString()));
 			   
 			   songs.add(song);
 			   
 	
 			   
 		   }
 
 		   album.setSongs(songs);
 		   
 		   String dirName = String.format("%s - %s", album.getArtist(), album.getTitle());
 		   this.createFolder(dirName);
 		   
 		   for (Song s : songs) {
 			   s.setTitle( fixTitleName(s.getTitle()));
 			   
 			   String name = String.format("%s/%d.%s.mp3", dirName, s.getTrackNumber(), s.getTitle());
 			   
 			   DownloadTask task = new DownloadTask();
 			   task.destination = name;
 			   task.downloadUrl = s.getUrl();
 			   try {
 				this.taskQueue.put(task);
 			   } catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				   e.printStackTrace();
 			   }
 			   			    
 
 		   }
 		   
 		   String coverFile = String.format("%s/Folder.jpg", dirName);
 		 //String coverFile = String.format("%s/%s - %s.jpg", dirName, album.getArtist(), album.getTitle());
 		   DownloadTask coverTask = new DownloadTask();
 		   coverTask.setDownloadUrl(album.getConverUrl());
 		   coverTask.setDestination(coverFile);
 		   
 		   taskQueue.add(coverTask);
 		   
 		   for (int i=0; i!=THREAD_POOL_SIZE; i++) {
 				try {
 					taskQueue.put(new DownloadTask(DownloaderConsumer.END_MESSAGE));
 				} catch (InterruptedException e) {	
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}				
 		   }
 		   
 
 			try {
 				List<Future<Integer>> futures = executorService.invokeAll(workerList);
 			} catch (InterruptedException e1) {
 				e1.printStackTrace();
 			}
 			
 			this.setIdTags(album);   	    
 
 			executorService.shutdown();
 			this.monitorThread.termiante();
 			
 			
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 	}
 	
 
 	private String fixTitleName(String title) {
 		if (isValidName(title))
 			return title;
 		
 		return tryToFixName(title);		
 	}
 
 	public void createFolder(String name) {
 		
 		File theDir = new File(name);
 
 		  // if the directory does not exist, create it
 		  if (!theDir.exists()) {
 		    System.out.println("creating directory: " + name);
 		    boolean result = theDir.mkdir();  
 
 		     if(result) {    
 		       System.out.println("DIR created");  
 		     }
 		  }
 		
 	}
 	
 	public  boolean isValidName(String text)
 	{
 	    Matcher matcher = pattern.matcher(text);
 	    boolean isMatch = matcher.matches();
 	    return isMatch;
 	}
 
 	public  String tryToFixName(String text)
 	{
 		text = text.replace("/", "-");
 		text = text.trim();
 		if (text.endsWith(".")) text = text.substring(0, text.lastIndexOf(".")); 
 //		text = text.replaceAll("[^A-Za-z0-9()\\[\\]\\s]", "");
 		return text;
 	}
 
 	
 	public void setIdTags(Album album) {
 		   String dirName = String.format("%s - %s", album.getArtist(), album.getTitle());
 			
 		   ID3v1 id3v1Tag;
  		   ID3v2 id3v2Tag;
 
 		   String SUBFIX = "_temp"; 
 		   
 		   System.out.print("\nupdating Id3 tags");
 			
 		for (Song song : album.songs ) {
 			   String name = String.format("%s/%d.%s.mp3", dirName, song.getTrackNumber(), song.getTitle());
 
 			 Mp3File mp3file;
 			try {
 				mp3file = new Mp3File(name);
 				if ( (!mp3file.hasId3v1Tag()) && (!mp3file.hasId3v2Tag()) ) {
 				      
 				      id3v1Tag = new ID3v1Tag();
 				      mp3file.setId3v1Tag(id3v1Tag);
 				      
 				      id3v1Tag.setTrack(Integer.toString(song.getTrackNumber()));
 				      id3v1Tag.setArtist(album.getArtist());
 				      id3v1Tag.setTitle(song.getTitle());
 				      id3v1Tag.setAlbum(album.getTitle());
 				      id3v1Tag.setComment("By srtv");
 				    
     				  id3v2Tag = new ID3v24Tag();
 				      mp3file.setId3v2Tag(id3v2Tag);
 				      
 				      id3v2Tag.setTrack(Integer.toString(song.getTrackNumber()));
 				      id3v2Tag.setArtist(album.getArtist());
 				      id3v2Tag.setTitle(song.getTitle());
 				      id3v2Tag.setAlbum(album.getTitle());
 				      id3v2Tag.setComment("By srtv");
 
 				      mp3file.save(name + SUBFIX);
 				      
 				      File oldFile = new File(name);
 				      System.gc(); // nasty trick to get the file deleted on windows
 				      oldFile.delete();
				      System.gc();
 				      				      				     
 				      File newFile = new File(name+SUBFIX);
 				      newFile.renameTo(oldFile);
 					
 				      System.out.print(".");
 				      
 				}
 				
 				
 			} catch (UnsupportedTagException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (InvalidDataException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (NotSupportedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 
 		}
 		
 		System.out.print("done.");
 		System.out.print("\n");
 		System.out.println("share the love :)");
 
 		
 	}
 	
 
 }
