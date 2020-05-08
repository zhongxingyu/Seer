 package com.stefankopieczek.audinance.renderer;
 
 import java.nio.ByteOrder;
 
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.DataLine;
 import javax.sound.sampled.LineUnavailableException;
 import javax.sound.sampled.SourceDataLine;
 
 import com.stefankopieczek.audinance.audiosources.DecodedSource;
 import com.stefankopieczek.audinance.audiosources.NoMoreDataException;
 import com.stefankopieczek.audinance.formats.DecodedAudio;
 import com.stefankopieczek.audinance.formats.InvalidAudioFormatException;
 import com.stefankopieczek.audinance.utils.AudinanceUtils;
 
 public class JavaRenderer extends Renderer 
 {
 	public JavaRenderer(DecodedAudio decodedAudio)
 	{
 		super(decodedAudio);
 	}
 	
 	public void play()
 	{
 		javax.sound.sampled.AudioFormat format = mAudio.getFormat().getJmfAudioFormat();
 		SourceDataLine line = null;
 		
 		DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
 		if (!AudioSystem.isLineSupported(info))
 		{
 			return; // TODO
 		}
 		try
 		{
 			line = (SourceDataLine)AudioSystem.getLine(info);
 			line.open(format);
 		}
 		catch (LineUnavailableException ex)
 		{
 			return; // TODO			
 		}
 		
 		new LineWriter(line).start();
 	}
 	
 	private class LineWriter extends Thread
 	{
 		private SourceDataLine mLine;
 		
 		public LineWriter(SourceDataLine line)
 		{
 			mLine = line;
 		}
 		
 		@Override
 		public void run()
 		{
 			mLine.start();
 			
 			try
 			{
 				writeAudio();
 			}
 			catch (InvalidAudioFormatException e)
 			{
 				// Do something sensible here later. TODO.
 			}
 			finally
 			{
 				mLine.drain();
 				mLine.stop();
 				mLine.close();
 				mLine = null;
 			}
 		}			
 		
 		private void writeAudio() throws InvalidAudioFormatException
 		{
 			boolean hasData = true;
			int ptr = 0;
 			int frameSize = mAudio.getFormat().getNumChannels() * 2;
 			while (hasData)
 			{
 				hasData = false;
 				byte[] frame = new byte[frameSize];
 				for (int idx = 0; idx < mAudio.getChannels().length; idx++)
 				{					
 					DecodedSource channel = mAudio.getChannels()[idx];	
 					byte[] sampleBytes;
 					try
					{
						short sample = (short)(channel.getSample(ptr));
 						sampleBytes = AudinanceUtils.bytesFromShort(
 								              sample, ByteOrder.LITTLE_ENDIAN);
 						hasData = true;
 					}
 					catch (NoMoreDataException e)
 					{
 						// Just write blank data.
 						// If all channels are out of data, we stop altogether.
 						sampleBytes = new byte[2];
 					}		
 					System.arraycopy(sampleBytes,  0, frame, 2 * idx, 2);
 				}
 				
 				mLine.write(frame, 0, frameSize);
 				ptr++;		
 			}
 		}
 	}
 }
