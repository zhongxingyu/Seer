 package com.example.udpstreaming;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.nio.ByteBuffer;
 
 import android.app.Activity;
 import android.media.AudioFormat;
 import android.media.AudioManager;
 import android.media.AudioTrack;
 import android.os.Bundle;
 import android.os.Environment;
 import android.util.Log;
 import android.widget.TextView;
 
 public class MainActivity extends Activity {
 
 	private static final int SAMPLE_RATE = 44100;
 	private static final int BYTES_PER_SAMPLE = 2;
 	private static final int PACKET_SIZE = 882;
	private static final int BUFFER_SIZE = PACKET_SIZE * 512 * 2;
 	private static final int CACHE_THRESHOLD = (int) (BUFFER_SIZE * 0.15f);
 	private static final int BYTES_PER_LAPSE = SAMPLE_RATE * BYTES_PER_SAMPLE * 2;
 	private static final int LAPSE_PERIOD_MS = 1000;
 	private static final int SENDER_LAPSE_PERIOD_MS = (PACKET_SIZE * LAPSE_PERIOD_MS) / (BYTES_PER_LAPSE);
 
 	private DatagramSocket socket;
 	private InetAddress serverAddress;
 	private ByteBuffer buffer;
 	private int readPointer = 0;
 
 	private TextView tv;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		tv = new TextView(this);
 		setContentView(tv);
 
 		Thread initThread = new Thread(new Runnable() {
 
 			@Override
 			public void run() {
 				try {
 					serverAddress = InetAddress.getByName("sandile.me");
 					socket = new DatagramSocket(8991);
 					buffer = ByteBuffer.allocate(BUFFER_SIZE);
 					startThreads();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 
 		initThread.start();
 	}
 
 	private void startThreads() {
 		final int serverPort = 4000;
 
 		Thread receiverThread = new Thread(new Runnable() {
 
 			@Override
 			public void run() {
 
 				byte[] bytes = new byte[PACKET_SIZE];
 				DatagramPacket inPacket = new DatagramPacket(bytes, PACKET_SIZE);
 				try {
 					while (true) {
 						socket.receive(inPacket);

 						if (buffer.position() <= readPointer && ((buffer.position() + PACKET_SIZE) % buffer.capacity() > readPointer || (buffer.position() + PACKET_SIZE) % buffer.capacity() <= buffer.position()))
 							Log.d("UDPStreaming", "BufferOverflow");
 						else if (buffer.position() > readPointer && (buffer.position() + PACKET_SIZE) % buffer.capacity() > readPointer && (buffer.position() + PACKET_SIZE) % buffer.capacity() <= buffer.position())
 							Log.d("UDPStreaming", "BufferOverflow");
 
 						int remaining = buffer.capacity() - buffer.position();
 						if (remaining >= bytes.length) {
 							buffer.put(bytes);
 						} else {
 							buffer.put(bytes, 0, remaining);
 							buffer.position(0);
 							buffer.put(bytes, remaining, bytes.length - remaining);
 						}
 
 					}
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		});
 
 		Thread playerThread = new Thread(new Runnable() {
 
 			@Override
 			public void run() {
 				int minBufferSize = android.media.AudioTrack.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);
 				AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT, minBufferSize, AudioTrack.MODE_STREAM);
 				audioTrack.play();
				Log.d("UDPStreaming", "PlaybackRate: " + audioTrack.getPlaybackRate() + " / " + audioTrack.getSampleRate());
 				int remaining;
 				long timeStamp = System.currentTimeMillis();
 				while (true) {
 					// Check if we need to fatten our buffer
 					if (bufferSize() < CACHE_THRESHOLD) {
 						doCache(0.5f); // Fatten the buffer
						timeStamp = System.currentTimeMillis();
 					}
 
 					// Every LAPSE_PERIOD_MS milliseconds, write BYTES_PER_LAPSE
 					// bytes to the audio track
 					if (System.currentTimeMillis() - timeStamp >= LAPSE_PERIOD_MS) {
						Log.d("UDPStreaming", "Playing: " + bufferSize() + " / " + BUFFER_SIZE);
 						// Update our recorded time stamp, do it this high to ignore processing time
 						timeStamp += LAPSE_PERIOD_MS;

 						// Audio logic
 						
 						remaining = buffer.capacity() - readPointer;
 						if (remaining >= BYTES_PER_LAPSE) { // There is enough
 							// space left in the
 							// buffer without
 							// staggering our
 							// read
 							audioTrack.write(buffer.array(), readPointer, BYTES_PER_LAPSE);
 							readPointer += BYTES_PER_LAPSE;
 						} else {
 							// We need to stagger the read since we have a
 							// circular buffer
 							audioTrack.write(buffer.array(), readPointer, remaining);
 							audioTrack.write(buffer.array(), 0, BYTES_PER_LAPSE - remaining);
 							readPointer = BYTES_PER_LAPSE - remaining;
 						}
 					}
 				}
 			}
 		});
 
 		Thread senderThread = new Thread(new Runnable() {
 
 			@Override
 			public void run() {
 				File file = new File(Environment.getExternalStorageDirectory(), "tinytim.wav");
 				FileInputStream in;
 				try {
 					in = new FileInputStream(file);
 				} catch (FileNotFoundException e1) {
 					Log.e("UDPStreaming", "File input stream could not be established", e1);
 					return;
 				}
 
				long timeStamp = System.currentTimeMillis();
 				int bytesread = 0, ret = 0, size = (int) file.length();;
 				byte[] sendData = new byte[PACKET_SIZE];
 				try {
 					while (true) {
 						if (System.currentTimeMillis() - timeStamp >= SENDER_LAPSE_PERIOD_MS) {
 							// Update our recorded time stamp, do it this high to ignore processing time
							Log.d("UDPStreaming", "Sending " + PACKET_SIZE + " bytes / " + (System.currentTimeMillis() - timeStamp) + " msec");
 							timeStamp += SENDER_LAPSE_PERIOD_MS;
 							// File read / broadcasting logic
 							if (bytesread < size) {
 								ret = in.read(sendData, 0, PACKET_SIZE);
 								socket.send(new DatagramPacket(sendData, sendData.length, serverAddress, serverPort));
 								bytesread += ret;
 							} else {
 								in.close();
 								Log.d("UDPStreaming", "Transmission completed.");
 								break;
 							}
 						}
 					}
 				} catch (IOException e) {
 					Log.e("UDPStreaming", "File input stream read failed", e);
 					return;
 				}
 			}
 		});
 
 		receiverThread.start();
 		senderThread.start();
 		playerThread.start();
 	}
 
 	private void doCache(float target) { // Target is in %
 		// If the buffer is to frail, grow it up sufficiently
 		int size;
 		target = target * BUFFER_SIZE;
 		Log.d("UDPStreaming", "Buffer is too small, Caching Started");
 		while ((size = bufferSize()) < target) {
			Log.d("UDPStreaming", "Caching @ " + ((100.0f * size) / BUFFER_SIZE) + "%");
 		}
 		Log.d("UDPStreaming", "Caching Ended");
 	}
 
 	private int bufferSize() {
 		if (buffer.position() >= readPointer) return (buffer.position() - readPointer);
 		else return ((buffer.capacity() - readPointer) + buffer.position());
 	}
 }
