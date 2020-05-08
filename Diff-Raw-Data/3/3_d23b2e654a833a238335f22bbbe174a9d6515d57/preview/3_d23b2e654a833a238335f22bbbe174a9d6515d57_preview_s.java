 package org.buttes.shitpreview;
 import java.io.IOException;
 import java.util.List;
 import java.lang.Math;
 import java.lang.Thread;
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import android.app.Activity;
 import android.content.Context;
 import android.os.Bundle;
 import android.os.Handler;
 import android.graphics.PixelFormat;
 import android.graphics.ImageFormat;
 import android.hardware.Camera;
 import android.hardware.Camera.*;
 import android.os.Bundle;
 import android.view.WindowManager;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 import android.util.Log;
 import android.media.AudioTrack;
 import android.media.AudioTrack.*;
 import android.media.AudioFormat;
 import android.media.AudioFormat.*;
 import android.media.AudioManager;
 
 class AudioPlayer {
 
 	AudioTrack audio;
 
 	final int sampleRate = 11025;
 	final int sampleSize = 2; // in bytes
 	final int sampleChannelCfg = AudioFormat.CHANNEL_OUT_MONO;
 	final int sampleEncoding = (sampleSize == 1) ? AudioFormat.ENCODING_PCM_8BIT :
 								(sampleSize == 2) ? AudioFormat.ENCODING_PCM_16BIT :
 								AudioFormat.ENCODING_INVALID;
 
 	final int minBufferSize = AudioTrack.getMinBufferSize(sampleRate, sampleChannelCfg, sampleEncoding);
 	final int secondBufferSize = sampleRate * sampleSize;
 	final int bufferSize = Math.max(minBufferSize, secondBufferSize);
 
 	final int sampleBufferN = sampleRate / 5;
 	final short[] sampleBuffer = new short[sampleBufferN];
 
 	final int voicesN = 3;
 	final long[] voices = new long[voicesN];
 
 	public AudioPlayer() {
       audio = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, sampleChannelCfg, sampleEncoding, bufferSize, AudioTrack.MODE_STREAM);
 		audio.play();
 	}
 
 	private double getFrequency(double note) {
 		final double baseExponent = Math.pow(2.0, 1.0 / 12.0);
 		final double baseFrequency = 55.0;
 		return baseFrequency * Math.pow(baseExponent, note);
 	}
 
 	private short getNoteSample(long sampleN, long sampleRate, double note, double volume) {
 		return (short)Math.rint(volume * Short.MAX_VALUE * Math.sin(2.0 * Math.PI * getFrequency(note) * (double)sampleN / (double)sampleRate));
 	}
 
 	public void setSampleBuffer(int voice, double note, double volume) {
 		if(voice < 0 || voice >= voicesN)
 			voice = 0;
 		for(int i = 0; i < sampleBuffer.length; i++)
 			sampleBuffer[i] = getNoteSample(voices[voice]++, sampleRate, note, volume);
 	}
 
 	public void addSampleBuffer(int voice, double note, double volume) {
 		if(voice < 0 || voice >= voicesN)
 			voice = 0;
 		for(int i = 0; i < sampleBuffer.length; i++)
 			sampleBuffer[i] += getNoteSample(voices[voice]++, sampleRate, note, volume);
 	}
 
 	public void write() {
 		audio.write(sampleBuffer, 0, sampleBuffer.length);
 	}
 }
 
 public class preview extends Activity implements SurfaceHolder.Callback, Camera.PreviewCallback
 {
 	Camera camera;
 	SurfaceView surfaceView;
 	SurfaceHolder surfaceHolder;
 
 	TextView textViewMessage;
 	Button buttonPlay;
 	Button buttonPause;
 
 	boolean previewing = false;
 
 
 	/*
 	 * get frame size of a preview image (assuming NV21 format)
 	 */
 
 	private int getFrameSize() {
 		Camera.Parameters param = camera.getParameters();
 		int imgformat = param.getPreviewFormat();
 		int bitsperpixel = ImageFormat.getBitsPerPixel(imgformat);
 		Camera.Size camerasize = param.getPreviewSize();
 		return (camerasize.width * camerasize.height * bitsperpixel) / 8;
 	};
 
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		// find widgets
 
 		textViewMessage = (TextView)findViewById(R.id.message);
 		buttonPlay = (Button)findViewById(R.id.startcamerapreview);
 		buttonPause = (Button)findViewById(R.id.stopcamerapreview);
 		surfaceView = (SurfaceView)findViewById(R.id.surfaceview);
 
 		getWindow().setFormat(PixelFormat.YCbCr_420_SP);
 		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
 
 		surfaceHolder = surfaceView.getHolder();
 
 		surfaceHolder.addCallback(this);
 		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
 
 		// setup start camera button
 
 		buttonPlay.setOnClickListener(new Button.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 
 				if(previewing == false) {
 
 					//
 					//
 					// start up the audio player
 					//
 					//
 
 					previewing = true;
 
 					final Handler handler = new Handler();
 
 					new Thread(new Runnable() {
 						@Override
 						public void run() {
 
 							AudioPlayer player = new AudioPlayer();
 
 							handler.post(new Runnable() {
 								@Override	
 								public void run() {
 									textViewMessage.setText(String.format("PaperTracker - playback starting!\n"));									
 								}
 							});
 
 							double note = 36.0;
 							double loud = 1.0;
 							int voice = 0;
 
 							while(previewing) {
 								player.setSampleBuffer(voice, note, loud);
 								player.write();
 							}
 
 						}
 					}).start();
 
 					//
 					//
 					// start up the camera
 					//
 					//
 
					if((camera = Camera.open())) {
 
 						try {
 
 							camera.setPreviewDisplay(surfaceHolder);
 							camera.setDisplayOrientation(0);
 
 							// final int frameOffset = previewSize.height / 2;
 
 							camera.setPreviewCallback(new PreviewCallback() {
 
 								final Size previewSize = camera.getParameters().getPreviewSize();
 								final int frameWidth = previewSize.width;
 								final long startTime = System.currentTimeMillis();
 								final int framesPerMessage = 4;
 
 								long frameN = 0;
 
 								@Override
 								public void onPreviewFrame(byte[] data, Camera camera) {
 
 									// TODO:
 									// 1. perform 8-bit Y-channel to 16-bit mono PCM conversion
 									// 2. invert, normalize & stretch pcm
 									// 3. centroid position and width detection
 									// 4. select frequency scale or proceedural instrument table
 									// a. probably not a not fourier basis like DCT-II/II transform pairs,
 									// b. try equal temperament chromatic scale: base_hz * (2 ** (1/12) ** note_n)
 									// 5. centroid position, width to frequency, amplitude conversion
 									// 6. freq to time domain composition and compress range
 									// 7. lag compensation
 
 									// pcmBuffer[i] = (short)(((int)data[frameOffset + i] & 0x00ff) + 128); // convert unsigned 8-bit to signed 16-bit
 									// pcmBuffer[i] = (short)(255 - pcmBuffer[i]); // invert levels
 									// pcmBuffer[i] <<= 8; // scale amplitude by 256, or a left-shift of 1 byte
 
 									frameN++;
 
 									long elapsedTime = System.currentTimeMillis() - startTime;
 
 									double secs = (double)elapsedTime / 1000.0;
 									double fps = (double)frameN / secs;
 
 									if(frameN % framesPerMessage == 1) {
 										textViewMessage.setText(String.format("PaperTracker - #%d %.1fs %.1ffps %.1fhz", frameN, secs, fps, fps * (double)frameWidth));
 										// textViewMessage.setText(String.format("PaperTracker - #%d %.1fs %dspf %dkB %.1f : %.1f fps X %.1f %.1f hz",
 										// counters[0], secs, frameWidth, bufferSize >> 10, targetFps, fps, runRate, fps * frameWidth));
 									}
 								}
 							});
 
 							camera.startPreview();
 
 						} catch (IOException e) {
 
 							e.printStackTrace();
 						}
 
 						// end of if((camera = Camera.open())) { ... }
 					}
 
 					// end of if(previewing == false) { ... } 
 				}
 
 				// end of onClick()
 			} 
 		});
 
 		//
 		// setup stop camera button
 		//
 
 		buttonPause.setOnClickListener(new Button.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 
 				if(previewing) {
 
 					if(camera != null) {
 						camera.stopPreview();
 						camera.release();
 						camera = null;
 					}
 
 					previewing = false;
 				}
 			}
 		});
 
 	}
 
 	@Override
 	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
 	}
 
 	@Override
 	public void surfaceCreated(SurfaceHolder holder) {
 	}
 
 	@Override
 	public void surfaceDestroyed(SurfaceHolder holder) {
 	}
 
 	@Override
 	public void onPreviewFrame(byte[] data, Camera camera) {
 	}
 }
 
 /* 
 class Extract implements Camera.PreviewCallback {
 }
 */
