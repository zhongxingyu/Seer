 package su.jit.nko3.ten_nine_eight_seven;
 
 import java.io.IOException;
 import java.net.Socket;
 
 import android.hardware.Camera;
 import android.media.MediaRecorder;
 import android.os.AsyncTask;
 import android.os.ParcelFileDescriptor;
 import android.util.Log;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 import android.widget.ImageButton;
 
 public class Recorder {
 	
 	public void enable() {
 		Log.e("recorder", "enable");
 		if (camera == null) {
 			camera = Camera.open(0);
 			try {
 				camera.setPreviewDisplay(surface.getHolder());
 				camera.startPreview();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	public void socketAvailable(Socket socket) {
 		Log.e("recorder", "socket available"+socket.toString());
 		this.socket = socket;
 		showButton();
 	}
 	
 	public void disable() {
 		Log.e("recorder", "disable");
 		stop();
 		camera.stopPreview();
 		camera.release();
 		camera = null;
 		hideButton();
 	}
 	
 	protected boolean recording = false;
 	protected ImageButton button;
 	protected Camera camera;
 	protected MediaRecorder recorder;
 	protected Socket socket;
 	protected SurfaceView surface;
 	
 	Recorder(SurfaceView surface, ImageButton button) {
 		initializeButton(button);
 		initializeSurface(surface);
 	}
 	
 	private void initializeButton(ImageButton button) {
 		this.button = button;
 		hideButton();
 		button.setOnClickListener(new View.OnClickListener() {
 			@Override public void onClick(View v) {
 				toggleRecording();
 			}
 		});
 	}
 	
 	private void initializeSurface(SurfaceView surface) {
 		this.camera = Camera.open(0);
 		this.surface = surface;
 		surface.getHolder().addCallback(new SurfaceHolder.Callback() {
         	@Override public void surfaceCreated(SurfaceHolder holder) {} // wait until surfaceChanged()
     		@Override public void surfaceDestroyed(SurfaceHolder holder) {}
     		@Override public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
     			try {
 					camera.setPreviewDisplay(holder);
 					camera.startPreview();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
     		}
         });
 	}
 	
 	protected void hideButton() {
 		button.setVisibility(View.INVISIBLE);
 	}
 	
 	protected void showButton() {
 		button.setVisibility(View.VISIBLE);
 	}
 	
 	protected void start() {
 		Log.e("recorder", "start");
 		camera.unlock();
 		recorder = new MediaRecorder();
 		recorder.setCamera(camera);
 		AsyncTask<Void, Void, Void> startRecording = new AsyncTask<Void, Void, Void>() {
 			@Override
 			protected Void doInBackground(Void... params) {
 	    		recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
 	    		recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
 	    		recorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
 	    		try {
 	    			recorder.setOutputFile(ParcelFileDescriptor.fromSocket(socket).getFileDescriptor());
 	    			recorder.prepare();
 	        		recorder.start();
 	        		recording = true;
 	    		} catch (Exception e) {
 	    			recorder.reset();
 	    			try {
 						camera.reconnect();
 					} catch (IOException e1) {
 						e1.printStackTrace();
 					}
 	    			e.printStackTrace();
 	    		}
 	    	return null;
 			}
 		};
 		startRecording.execute();;
 
 	}
 	
 	protected void stop() {
 		Log.e("recorder", "stop");
 		if (recorder != null) {
			recorder.stop();
 			recorder.reset();
 			recorder.release();
 			recorder = null;
 		}
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			socket = null;
		}
 		try {
 			camera.reconnect();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		recording = false;
 	}
 	
 	protected void toggleRecording() {
 		if (recording) {
 			stop();
 		} else {
 			start();
 		}
 	}
 }
