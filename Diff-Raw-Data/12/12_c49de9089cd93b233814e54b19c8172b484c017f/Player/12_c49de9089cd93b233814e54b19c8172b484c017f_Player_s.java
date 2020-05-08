 // -*- coding:utf-8 -*-
 
 /* The MIT License
    
    Copyright (c) 2011 Keiichiro Ui
 
    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:
    
    The above copyright notice and this permission notice shall be included in
    all copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
    THE SOFTWARE.
 */
 
 
 package kui.lastfm.radio;
 
 import java.util.List;
 import java.util.HashMap;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.FileOutputStream;
 import java.io.BufferedOutputStream;
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.io.IOException;
 import java.io.FileNotFoundException;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URLConnection;
 import java.net.MalformedURLException;
 
 import javax.xml.stream.XMLStreamException;
 
 import javazoom.jl.decoder.JavaLayerException;
 
 public class Player {
 
     private static final String USAGE = 
 	"usage: java -jar lastfmradio-X.X.X [param_name param]\n"+
 	"listening LastFM Radio\n\n"+
 	"param_name: \n"+
 	"  tag, artist, group, personal, playlist, or recommended\n\n"+
	"param:"+
 	"  tag name (if param_name=tag)\n"+
 	"  user name (if param_name=parsonal,playlist,recommended)\n"+
 	"  artist name (if param_name=artist)\n"+
 	"  group name (if param_name=group)\n"
 	;
 
     public static void main(String[] args) throws Exception{
 
 	//System.getProperties().list(System.out);
 
 	String profileFileName = "account.txt";
 	Player p;
 	try{
 	    p = new Player(profileFileName);
 	}catch(FileNotFoundException e){
 	    p = null;
 	    System.out.println("ERROR: create \""+profileFileName+
 			       "\" file.\n"+
 			       "       refer \"account.templete\" file.");
 	    System.exit(1);
 	}
 
 	if(args.length == 2){
 	    p.init(args[0], args[1]);
 	}else if(args.length == 0){
 	    p.init();
 	}else{
 	    printUsage();
 	}
 
 	p.play();
     }
 
     private static void printUsage(){
 	System.out.println(USAGE);
     }
 
     // 
 
     private Client client;
     private BufferedReader input;
 
     public Player(String fileName) throws IOException, FileNotFoundException{
 	client = new Client(new File(fileName));
 	input = null;
     }
 
     public void init()
 	throws IOException, HandShakeException,
 	       AdjustingStationException, UnKnownParamNameException {
 	printUsage();
 	input = new BufferedReader(new InputStreamReader(System.in));
 
 	System.out.println("input param_name");
 	String paramName = input.readLine();
	if(paramName.equals("tag")){
	    paramName = "globaltags";
	}
 
 	System.out.println("input param");
 	String param = input.readLine();
 
 	init(paramName, param);
     }
 
 
     public void init(String paramName, String param)
 	throws IOException, HandShakeException,
 	       AdjustingStationException, UnKnownParamNameException {
 	System.out.println("shaking hand ...");
 	client.handshake();
 
 	System.out.println("adjusting station ...");
 	client.adjustStation(paramName, param);
     }
     
     public void play()
 	throws IOException, MalformedURLException, URISyntaxException, 
 	       XMLStreamException, JavaLayerException{
 
 	boolean loopFlag = true;
 	while(loopFlag){
 	    System.out.println("getting tracks ...");
 	    List<Track> tracks = client.getTracks();
 	    for(Track t : tracks) {
 		printTrack(t);
 		playTrack(t);
 	    }
 	    loopFlag = false;
 	}
 
     }
 
     public void printTrack(Track t){
 	System.out.printf("%s\t- %s\t(%s)\n",
 			  t.get("title"),
 			  t.get("album"),
 			  t.get("creator"));
 	// System.out.println(t);
     }
 
     public void playTrack(Track t) 
 	throws MalformedURLException, URISyntaxException,
 	       IOException, XMLStreamException, JavaLayerException{
 
 	// connect to the player server
 	URI uri = new URI(t.get("location"));
 	URLConnection c = uri.toURL().openConnection();
 	BufferedInputStream bis =
 	    new BufferedInputStream(c.getInputStream());
 
 	// output
 	javazoom.jl.player.Player p = 
 	    new javazoom.jl.player.Player(bis);
 	Thread displayTread = invokeDisplayProgressTread(p, t);
 	p.play();
 	try{
 	    displayTread.join();
 	}catch(InterruptedException e){
 	    System.err.println(e.getMessage());
 	}
 
 	// close
 	p.close();
 	bis.close();
     }
 
     private Thread 
 	invokeDisplayProgressTread(javazoom.jl.player.Player player,
 				   Track track) {
 	Thread t =
 	    new Thread(new DisplayProgressTread(player, track));
 	t.start();
 	return t;
     }
 
     private static final int DISPLAY_INTERVAL = 200; // [msec]
     private static class DisplayProgressTread implements Runnable{
 
 	private javazoom.jl.player.Player p;
 	private int duration;
 
 	public DisplayProgressTread(javazoom.jl.player.Player p,
 				    Track t){
 	    this.p = p;
 	    this.duration = 
 		Integer.parseInt(t.get("duration"));
 	}
 
 	public void run(){
 	    try{
 		displayLoop();
 		System.out.println();
 	    }catch(InterruptedException e){
 		System.err.println(e.getMessage());
 	    }
 	}
 
 	public void displayLoop() throws InterruptedException{
 	    int pos;
 	    int formerPos = 0;
 	    while(!p.isComplete()){
 		Thread.sleep(DISPLAY_INTERVAL);
 		pos = p.getPosition();
 		// do not display it if pos comes down.
 		// because sometime pos becomes zero.
 		if(formerPos > pos) continue;
 		printProgressBar(pos, duration);
 		formerPos = pos;
 	    }
 	    // printProgressBar(duration, duration);
 	}
 	
 	private static final int WIDTH_NUM = 50;
 	private static final String DONE_ELEMENT = "#";
 	private static final String SPACE_ELEMENT = "-";
 	private void printProgressBar(int now, int max){
 	    int elNum = now*WIDTH_NUM/max;
 	    int spaceNum = WIDTH_NUM-elNum;
 	    StringBuilder sb = new StringBuilder();
 	    sb.append(convertSecontToString(now)).append("/")
 		.append(convertSecontToString(max)).append(" |");
 	    for(int i=0; i<elNum; i++){
 		sb.append(DONE_ELEMENT);
 	    }
 	    for(int i=0; i<spaceNum; i++){
 		sb.append(SPACE_ELEMENT);
 	    }
 	    sb.append("|\r");
 	    System.out.print(sb);
 	}
 	
 	private String convertSecontToString(int s){
 	    int sec = s/1000;
 	    int min = sec/60;
 	    sec = sec%60;
 	    return String.format("%02d:%02d", min, sec);
 	}
     }
 
     /*
     private final int BUFFER_SIZE = 1024*16;
     public void playTrack2(String mp3Location)
 	throws MalformedURLException, URISyntaxException, IOException, 
 	       XMLStreamException{
 
 	URI uri = new URI(mp3Location);
 	URLConnection c = uri.toURL().openConnection();
 	BufferedInputStream bis =
 	    new BufferedInputStream(c.getInputStream());
 	byte[] buffer = new byte[BUFFER_SIZE];
 	BufferedOutputStream o = 
 	    new BufferedOutputStream(new FileOutputStream("hoge.mp3"));
 	long offset = 0L;
 	int size;
 	while((size = bis.read(buffer)) >= 0){
 	    o.write(buffer);
 	    offset += size;
 	    System.out.print(offset);
 	    System.out.print("\r");
 	    System.out.flush();
 	}
 	bis.close();
 	o.close();
     }
     */
 }
