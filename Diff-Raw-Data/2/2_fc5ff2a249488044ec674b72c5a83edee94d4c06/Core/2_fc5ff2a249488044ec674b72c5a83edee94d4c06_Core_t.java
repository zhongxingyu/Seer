 package me.majsky.zvoncek3;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Random;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import me.majsky.util.FileUtils;
 import me.majsky.util.module.IModule;
 import me.majsky.zvoncek3.plugins.api.ICore;
 import me.majsky.zvoncek3.util.Config;
 import me.majsky.zvoncek3.util.FileOpener;
 import me.majsky.zvoncek3.util.MP3NameFilter;
 
 public class Core extends TimerTask implements ICore, IModule{
     
     private Timer timer;
     private int last;
     public int forced = -1;
     private boolean copy = false;
     private Random rnd;
     
     public String[] songs;
     
     
     protected Core(){
         timer = new Timer("Core Ticker", false);
     }
     
     @Override
     public int getTime(){
     	return Integer.parseInt(new SimpleDateFormat("HHmm").format(new Date()));
     }
     
     @Override
     public boolean zvoni(){
     	int curTime = getTime();
     	for(int time:ZVONENI)
     		if(curTime == time)
     			return true;
     	return false;
     }
 
     private int isTimeToRock(){
     	int day = Integer.parseInt(new SimpleDateFormat("u").format(new Date()));
     	if(day > 5)
     		return -1;
         int time = getTime();
         for(int i:CROP_TIME)
             if(time == i)
                 return i;
         return -1;
     }
     
     @Override
     public void run() {
     	if(copy){
     		FileOpener.openTempMp3("temp").renameTo(FileOpener.openOutputMp3());
     		copy = false;
     	}
         if(isTimeToRock() >=0 ){
         	File file = FileOpener.openInputMp3(songs[last]);
             File temp = FileOpener.openTempMp3("temp");
         	String force = findForce();
         	if(force != null && validateForce(force))
         		file = FileOpener.openInputMp3(force);
         	if(forced > -1 && forced < songs.length){
         	    file = FileOpener.openInputMp3(songs[forced]);
         	    forced = -1;
         	}
             try {
                 FileUtils.copyFile(file, temp);
             } catch (IOException e) {
                 e.printStackTrace();
             }
             if(!file.getName().endsWith(".PRECUT.mp3")){
                 long length = MP3Cutter.getLength(songs[last]);
                 if(length < 30)
                     length = 31;
                 MP3Cutter.crop("temp", rnd.nextInt((int)length-30));
                 MP3Cutter.fadeIn("temp");
                 MP3Cutter.fadeOut("temp");
             }
             copy = true;
             last++;
             if(last == songs.length)
         		last = 0;
             
             writeLast();
             writeNext();
         }
         System.gc();
     }
     
     @Override
     public void force(int index){
         forced = index;
     }
     
     private boolean validateForce(String force){
     	for(String s:songs)
     		if(s.equals(force))
     			return true;
     	return false;
     }
     
     private boolean validateForce(int force){
         return force > -1 && force < songs.length;
     }
     
     private String findForce(){
     	if(!Config.force_file.exists())
     		return null;
     	System.out.println("Found force candidate file at" + Config.force_file.getAbsolutePath());
     	try{
     		BufferedReader br = new BufferedReader(new FileReader(Config.force_file));
     		String song = br.readLine();
     		br.close();
     		Config.force_file.delete();
     		
     		try{
     		    int numForce = Integer.parseInt(song);
     		    if(validateForce(numForce)){
         		    System.out.println("Forcing: " + songs[forced]);
         		    forced = numForce;
     		    }
     		    return null;
     		}catch(NumberFormatException e){System.out.println(song + " asi nieje cislo.");}
     		if(song != null && !song.equals("")){
     		    System.out.println("Forcing " + song);
     			return song;
     		}
     	}catch(Exception e){
     		e.printStackTrace();
     	}
     	return null;
     }
     
     private void generateList(){
         try{
             File file = Config.list_file;
             BufferedWriter bw = new BufferedWriter(new FileWriter(file));
             System.out.println("File list:");
             for(String s:songs){
                 System.out.println(">" + s);
                 bw.append(s);
                 bw.newLine();
             }
             bw.close();
         }catch(IOException e){
             e.printStackTrace();
         }
     }
     
     private void writeLast(){
         try{
             File lastF = Config.last_file;
             BufferedWriter bw = new BufferedWriter(new FileWriter(lastF));
             bw.write(songs[last]);
             bw.close();
         }catch(IOException e){
             e.printStackTrace();
         }
     }
     
     private void writeNext(){
         try{
             File nextF = Config.next_file;
             BufferedWriter bw = new BufferedWriter(new FileWriter(nextF));
             bw.write(songs[last+1>=songs.length?0:last]);
             bw.close();
         }catch(IOException e){
             e.printStackTrace();
         }
     }
     
     @Override
     public void reload(){
     	songs = Config.songInputFolder.list(new MP3NameFilter());
     	last = rnd.nextInt(songs.length);
     	generateList();
     }
 
     @Override
     public String[] getSongs() {
         return songs;
     }
 
     @Override
     public void start() {
         songs = Config.songInputFolder.list(new MP3NameFilter());
         if(songs.length <= 0){
             System.out.println("I dont have any song! :(");
             return;
         }
         System.out.println("I got " + songs.length + " song" + (songs.length > 1?"s":"") + "!");
         rnd = new Random(System.currentTimeMillis());
         last = rnd.nextInt(songs.length);
         generateList();
         timer.schedule(this, 0, 5000);
     }
 
     @Override
     public void stop() {
         timer.cancel();
     }
 }
