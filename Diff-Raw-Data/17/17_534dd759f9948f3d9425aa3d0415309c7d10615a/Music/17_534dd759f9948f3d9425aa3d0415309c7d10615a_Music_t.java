 import java.io.*;
 import java.util.Vector;
 import java.util.Scanner;
 import javax.sound.sampled.*;
 
 public class Music
 {
 	public boolean stopCapture = false;
   	public boolean paused = false;
  	
	ByteArrayOutputStream byteArrayOutputStream;
   	AudioFormat audioFormat;
   	TargetDataLine targetDataLine;
   	AudioInputStream audioInputStream;
   	SourceDataLine sourceDataLine;
	
 	Vector<ByteArrayOutputStream> Audio = new Vector<ByteArrayOutputStream>();
 	Vector<String>TrackNames = new Vector<String>();
 	Vector<Track> Tracks = new Vector<Track>();
 	
 	// captures audio input from a microphone and saves it in a ByteArrayOutputStream object.
   	public void captureAudio()
 	{
     		try{
       			// Get everything set up for capture
       			audioFormat = getAudioFormat();
       			DataLine.Info dataLineInfo = new DataLine.Info( TargetDataLine.class, audioFormat );
       			targetDataLine = (TargetDataLine) AudioSystem.getLine( dataLineInfo );
       			targetDataLine.open( audioFormat );
       			targetDataLine.start();
 
       			// Create a thread to capture the microphone data and start it  
       			Thread captureThread = new Thread( new CaptureThread() );
       			captureThread.start();
        		} catch (Exception e) {
 			System.out.println(e);
       			System.exit(0);
     			}
   	}
 	
 	// plays back the audio data that has been saved in the ByteArrayOutputStream
   	public void playAudio() 
 	{
     		try{
       			// Get everything set up for playback.
       			// Get the previously-saved data into a byte array object.
       			byte audioData[] = byteArrayOutputStream.toByteArray();
       			
 			// Get an input stream on the byte array containing the data
       			InputStream byteArrayInputStream = new ByteArrayInputStream( audioData );
       			AudioFormat audioFormat = getAudioFormat();
       			audioInputStream = new AudioInputStream( byteArrayInputStream, audioFormat,
 					audioData.length/audioFormat.getFrameSize());
       			DataLine.Info dataLineInfo = new DataLine.Info( SourceDataLine.class, audioFormat );
       			sourceDataLine = (SourceDataLine) AudioSystem.getLine( dataLineInfo );
      			sourceDataLine.open( audioFormat );
       			sourceDataLine.start();
 
       			// Create a thread to play back the data 
       			Thread playThread = new Thread( new PlayThread() );
       			playThread.start();
     		
 		} catch (Exception e) {
       			System.out.println(e);
       			System.exit(0);
     			}
   	}
 
 	// creates and returns an AudioFormat object 
   	public AudioFormat getAudioFormat()
 	{
     		float sampleRate = 8000.0F;
     		int sampleSizeInBits = 16;
     		int channels = 1;
     		boolean isSigned = true;
     		boolean bigEndian = false;
     		return new AudioFormat( sampleRate, sampleSizeInBits, channels, isSigned, bigEndian);
   	}
 	
	public void stopRecording()
 	{
		stopCapture = true;
 	}
 
 	public void pausePlayback()
 	{
		 paused = true;
 	}
 
 	public void resumePlay()
 	{
		 paused = false;
 	}
 	
 	
 	//Inner class to capture data from microphone
 	class CaptureThread extends Thread
 	{
 		byte tempBuffer[] = new byte[10000];
   		public void run()
 		{
   			byteArrayOutputStream = new ByteArrayOutputStream();
     			stopCapture = false;
     			try{
     				//Loop until stopCapture is set by another thread 
       				while( !stopCapture )
 				{
         				//Read data from the internal buffer of the data line.
         				int count = targetDataLine.read( tempBuffer, 0, tempBuffer.length );
         				if( count > 0 )
         				{
           					//Save data in output stream object.
           					byteArrayOutputStream.write( tempBuffer, 0, count );
         				}
       				}
 				
       				Scanner scan = new Scanner(System.in);
       				System.out.println("Name the track");
       				String name = scan.next();
       				
 				//add track name to string vector
 				TrackNames.add(name);
       				System.out.println(TrackNames);
 
 				// add audio data to audio vector
 	  			Audio.add( byteArrayOutputStream );
       				
 				Track t = new Track( name, byteArrayOutputStream );
 				Tracks.add( t );
 
 				byteArrayOutputStream.close();
 				
 
     			}catch (Exception e) {
       				System.out.println(e);
       				System.exit(0);
     					}
   		}
 	}
 
 	//Inner class to play back the data that was saved.
 	class PlayThread extends Thread
 	{
   		byte tempBuffer[] = new byte[10000];
 
   		public void run()
 		{
     			try{
       				int count;
       				//Keep looping until the input read method returns -1 for empty stream.
       				while(( count = audioInputStream.read( tempBuffer, 0, tempBuffer.length ) ) != -1 )
 				{
         				if( count > 0 && !paused )// maybe need to use sourceDataLine.stop() to be able to resume in same spot
 					{
           					//Write data to the internal buffer of the data line
           					sourceDataLine.write( tempBuffer, 0, count );
         				}
       				}
       				//Block and wait for internal buffer of the data line to empty.
       				sourceDataLine.drain();
       				sourceDataLine.close();
     			}catch (Exception e) {
       				System.out.println(e);
       				System.exit(0);
     				}
   		}
 	}
 }
 	
 	
