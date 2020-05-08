 package edu.purdue.cs252.lab06;
 
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 import android.media.AudioFormat;
 import android.media.AudioRecord;
 import android.media.MediaRecorder;
 import android.os.Handler;
 import android.util.Log;
 
 public class VoiceRecorder implements Runnable
 {
 	private int sampleRate = 8000;
 	private int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
 	private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
 	private DatagramSocket socket;
 	static boolean onCall = false;
 	
 	static String _server;
 
 	VoiceRecorder(String server)
 	{
 		try
 		{
 			_server = server;
 			this.socket = new DatagramSocket(6901);			
 		}
 		catch (Exception ex)
 		{	
 			// TODO: handle this exception
 		}  
 	}
 	
 	public void run()
 	{
 		try
 		{
 			DatagramPacket packet;
 			int minBuf = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
 			AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channelConfig, audioFormat, minBuf);
 			recorder.startRecording();
 			
 			while(true)
 			{
 				try
 				{
					byte[] buffer = new byte[50];
					recorder.read(buffer, 0, buffer.length);
 					packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(_server), 6901);
 	
 					socket.send(packet);
 				}
 				catch (Exception ex)
 				{
 					// TODO: handle this exception
 				}
 			}
 		}
 		catch (Exception ex)
 		{
 			// TODO: handle this exception
 		}
 	}
 }
