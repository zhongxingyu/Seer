 package edu.purdue.cs252.lab06;
 
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.net.SocketException;
 import android.media.AudioFormat;
 import android.media.AudioManager;
 import android.media.AudioTrack;
 
 public class VoicePlayer implements Runnable
 {	
 	private int sampleRate = 8000;
 	private int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
 	private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
 	private DatagramSocket socket;
 	private AudioTrack speaker;
 	
 	static String _server;
 	
 	VoicePlayer(String server)
 	{
 		try
 		{
 			_server = server;
 			this.socket = new DatagramSocket(6901);
 		}
 		catch (SocketException e)
 		{
 			// TODO: handle this exception
 		}  
 	}
 	
 	public void run()
 	{
 		int minBuf = AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormat);
 		speaker = new AudioTrack(AudioManager.STREAM_VOICE_CALL, sampleRate, channelConfig, audioFormat, minBuf, AudioTrack.MODE_STREAM);
 		speaker.play();
 		
		byte[] buffer = new byte[2048];
 		DatagramPacket dummyPacket = null;
 		DatagramPacket packet;
 		
 		while(true)
 		{
 			try
 			{
 				final InetAddress serverAddr = InetAddress.getByName(_server);
 				
 				byte[] dummyBuf = new byte[1];
 				dummyBuf[0] = 0;
 				
 				dummyPacket = new DatagramPacket(dummyBuf, dummyBuf.length, serverAddr, 6901);
 				packet = new DatagramPacket(buffer, buffer.length);
 
 				socket.send(dummyPacket);
 				socket.receive(packet);
 
 				buffer = packet.getData();
 				speaker.write(buffer, 0, buffer.length);
 			}
 			catch (Exception e)
 			{
 				// TODO: handle this exception
 			}
 		}
 	}
 }
