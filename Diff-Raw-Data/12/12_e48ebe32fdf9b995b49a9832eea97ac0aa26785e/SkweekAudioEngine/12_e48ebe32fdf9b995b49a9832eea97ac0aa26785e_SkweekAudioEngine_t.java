 package com.rburgos.skweek;
 
 import javax.sound.sampled.AudioFormat;
 import javax.sound.sampled.AudioInputStream;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.DataLine;
 import javax.sound.sampled.LineUnavailableException;
 import javax.sound.sampled.SourceDataLine;
 
 public class SkweekAudioEngine implements Runnable
 {
 
 	AudioFormat audioformat;
 	DataLine.Info info;
 	SourceDataLine sourcedataline;
 	AudioInputStream audioInput;
 	String exp;
 	int t, x = 1, y = 1, z = 1, scale = 1;
 	int loop;
     byte[] buffer = new byte[800];
     boolean play = false;
 	
 	public void setExp(String exp)
 	{
 		this.exp = exp;
 	}
 	
 	public void setPlay(boolean set)
 	{
 		this.play = set;
 	}
 	
 	public void setLoop(int loop)
 	{
 		this.loop = loop;
 	}
 	
 	public void setX(int x)
 	{
 		this.x = x;
 	}
 	
 	public void setY(int y)
 	{
 		this.y = y;
 	}
 	
 	public void setZ(int z)
 	{
 		this.z = z;
 	}
 	
 	public void tScale(int scale)
 	{
 		this.scale = scale;
 	}
 	
 	public int tInc(int scale)
 	{
 		return scale > 0 ? scale : this.scale;
 	}
 
 	@Override
 	public void run()
 	{
 		System.out.println(">> thread started");
 		t = 0;
 		try
         {
 	        audioformat = new AudioFormat(11025f, 8, 2, false, false);
 	        info = new DataLine.Info(SourceDataLine.class, audioformat);
 	        sourcedataline = (SourceDataLine) AudioSystem.getLine(info);
 	        sourcedataline.open(audioformat, 1000);
 	        sourcedataline.start();
 	        while (play)
 	        {
 		        for (int i = 0; i < (buffer.length); i++)
 		        {
			        buffer[i] = 
			        		(byte) (SkweekParser.evalToInt(exp, t++, x, y, z) / 
			        				tInc(scale));
 			        
 			        if (t > (buffer.length*loop)-t)
 			        {
 			        	t = 0;
 			        }
 		        }
 		        sourcedataline.write(buffer, 0, buffer.length);
 	        }
 	        System.out.println(">> thread stopped");
         }
         catch (LineUnavailableException e)
         {
 	        e.printStackTrace();
         }
         sourcedataline.drain();
         sourcedataline.stop();
         sourcedataline.close();
         System.out.println(">> thread closed");
 	}
 
 }
