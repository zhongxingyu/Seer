 package com.musicgame.PumpAndJump.game.sound;
 
 import java.util.ArrayList;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.audio.io.Decoder;
 import com.badlogic.gdx.audio.io.WavDecoder;
 
 /**
  * This is the thread that runs and decompiles the music
  * @author gigemjt
  *
  */
 public class MusicInputStreamer extends Thread
 {
 
 	public String fileName= "the_hand_that_feeds.wav";
 //	public String fileName= "Skrillex_Cinema.wav";
 //	String fileName = "Windows_XP_Startup.wav";
 	Decoder decoder;
 	public ArrayList<short[]> frames = new ArrayList<short[]>();
 	public int currentFrame;
 	public static int frameSize = 1024/4;
 	public static long sampleRate = 44100;
 	public boolean buffering = true;
 	public boolean doneReading = false;
 	//do frame stuff here
 
 	public void loadSound()
 	{
		decoder = new WavDecoder(Gdx.files.absolute(fileName));
 
 	}
 
 	/**
 	 * The running thread that decompiles the music
 	 */
 	public void run()
 	{
 		int readSong = 1;
 		while(readSong != 0)
 		{
 			short[] buf = new short[frameSize*2];
 			short[] frame2 = new short[frameSize];
 			readSong = decoder.readSamples(buf,0, frameSize*2);
 			for(int k=0;k<frame2.length;k++)
 			{
 				if(decoder.getChannels()==2)
 				{
 					frame2[k] = (short) ((buf[k*2]+buf[k*2+1])/2.0);//gets half of the song (maybe because it is stereo?)
 				}else if(decoder.getChannels()==2)
 				{
 					frame2[k] = (buf[k]);//gets half of the song (maybe because it is stereo?)
 				}
 			}
 		//	System.out.println("Reading the song" +readSong+" "+currentFrame);
 			frames.add(frame2);
 			currentFrame++;
 			if(buffering)
 			{
 				/*
 				try {
 					Thread.sleep(5);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 				*/
 			}else
 			{
 				try {
 					Thread.sleep(30);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		doneReading = true;
 	}
 }
