 /**
  * @(#)SoundObj.java
  *
  *
  * @author 
  * @version 1.00 2012/7/31
  */
 
 import java.io.*;
 import javax.sound.sampled.*;
 
 public class SoundObj {
 
     public SoundObj(Boolean firing, String soundType) {
     	String soundPath="";
     	
     	if(soundType.equals("laser")){
     		
     		if(firing){
    			soundPath = "Resources/Sounds/laser1.wav";	
     		} else {
     			soundPath = "Resources/Sounds/laserhit1.wav";
     		}
     		
     	}else if(soundType.equals("missile")){
     		
     		if(firing){
     			soundPath = "Resources/Sounds/missile1.wav";
     		} else {
     			soundPath = "Resources/Sounds/missileHit1.wav";
     		}
     		
     	}
     	
 	    InputStream is = this.getClass().getClassLoader().getResourceAsStream(soundPath);
 		//InputStream is = this.getClass().getClassLoader().getResourceAsStream("Resources/Sounds/laser1.wav");
 		try{
 			AudioInputStream ain = AudioSystem.getAudioInputStream(is);
 			DataLine.Info info = new DataLine.Info(Clip.class, ain.getFormat());      
 			Clip clip = (Clip)AudioSystem.getLine(info);
 			clip.open(ain);
 			clip.start();
 		}
 		catch(UnsupportedAudioFileException e){}
 		catch(LineUnavailableException u){}
 		catch(IOException i){}
     	
     }
     
 }
