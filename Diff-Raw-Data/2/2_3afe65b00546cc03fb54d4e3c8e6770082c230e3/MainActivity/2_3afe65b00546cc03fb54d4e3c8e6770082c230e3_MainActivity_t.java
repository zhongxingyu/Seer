 package ws.websca.krcam;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
 
 
 import android.graphics.ImageFormat;
 import android.hardware.Camera;
 import android.hardware.Camera.Parameters;
 import android.hardware.Camera.PreviewCallback;
 import android.hardware.Camera.Size;
 import android.media.AudioFormat;
 import android.media.AudioRecord;
 import android.media.MediaRecorder;
 import android.os.Bundle;
 import android.os.Environment;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.util.Log;
 import android.view.Menu;
 import android.view.SurfaceHolder;
 import android.view.WindowManager;
 import android.view.SurfaceHolder.Callback;
 import android.view.SurfaceView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainActivity extends Activity implements Callback, PreviewCallback, Runnable {
 
 	public native long krStreamCreate(String path, int w, int h, boolean networkStream);
 	public native String krAddVideo(long cam, byte input[], int tc);
 	public native boolean krAudioCallback(long cam, byte buffer[], int size);
 	public native boolean krStreamDestroy(long cam);
 	private Long cam=null;
 	private AudioRecord ar;
 	private SurfaceView surfaceView;
 	private Camera camera;
 	private long FPSstartMs = -1;
 	private long startMs = -1;
 	private int frame;
 	private TextView textView;
 	private byte[] previewBuffer;
 	private byte[] previewBuffer2;
 	private byte[] previewBuffer3;	
 	private byte[] previewBuffer4;
 	private byte[] previewBuffer5;	
 	private String formatString;
 	private int useWidth = Integer.MAX_VALUE;
 	private int useHeight = Integer.MAX_VALUE;
 
 	static {
 		System.loadLibrary("krcam");
 	}
 	
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	
 	protected void onStop() {
 		super.onStop();
 		camera.release();
 		Log.e("vpx", "stop");
 		if(cam!=null)
 			krStreamDestroy(cam);
 		cam=null;
 	}
 	
 	protected void onPause() {
 		super.onPause();
 		camera.release();
 		Log.e("vpx", "pause");
 		if(cam!=null)
 			krStreamDestroy(cam);
 		cam=null;
 		if(ar!=null)
 			ar.release();
 		ar=null;
 	}
 	
 	protected void onStart() {
 		super.onStart();
 		
 		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);	
 		textView = (TextView)findViewById(R.id.textView1);
 		surfaceView = (SurfaceView)findViewById(R.id.surfaceview);
 			camera = Camera.open();
 			Parameters p = camera.getParameters();
 	        		
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setTitle("Video Resolution");
 			
 
 			String choiceList[] = new String[p.getSupportedPreviewSizes().size()];
 			for(int x=0; x<p.getSupportedPreviewSizes().size(); x++) {
 				choiceList[x]=new String(""+p.getSupportedPreviewSizes().get(x).width+"x"+p.getSupportedPreviewSizes().get(x).height);
 			}
 			int selected = 0;
 			builder.setSingleChoiceItems(choiceList, selected,
 					new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog,	int which)
 						{
 							dialog.dismiss();
 							Camera.Parameters parameters = camera.getParameters(); 
 							int w=parameters.getSupportedPreviewSizes().get(which).width;
 							int h=parameters.getSupportedPreviewSizes().get(which).height;
 							parameters.setPreviewSize(w, h);
 							camera.setParameters(parameters);
 							int size = h*w+((h*w)/2);
 							previewBuffer = new byte[size];
 							previewBuffer2 = new byte[size];
 							previewBuffer3 = new byte[size];
 							previewBuffer4 = new byte[size];
 							previewBuffer5 = new byte[size];
 							camera.addCallbackBuffer(previewBuffer);
 							camera.addCallbackBuffer(previewBuffer2);
 							camera.addCallbackBuffer(previewBuffer3);
 							camera.addCallbackBuffer(previewBuffer4);
 							camera.addCallbackBuffer(previewBuffer5);
 
 							cam = krStreamCreate(Environment.getExternalStorageDirectory()+"/", w, h, false);
 							useWidth=w;
 							useHeight=h;
 							
 							formatString = ""+parameters.getPreviewSize().width+"x"+parameters.getPreviewSize().height+" NV21 ";
 							try {
 								camera.setPreviewDisplay(surfaceView.getHolder());
 							} catch (IOException e) {
 								e.printStackTrace();
 							}
 							camera.setPreviewCallbackWithBuffer(MainActivity.this);
 							camera.startPreview();
 							int min = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
 							ar = new AudioRecord(MediaRecorder.AudioSource.CAMCORDER, 44100, AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT, min);
 							ar.startRecording();
 							Thread t = new Thread(MainActivity.this);
 							t.start();
 						}
 					}
 					);
 			AlertDialog alert = builder.create();
 	        alert.show();
 			surfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
 			surfaceView.getHolder().addCallback(this);
 	}
 
 	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {}
 	public void surfaceCreated(SurfaceHolder holder) {}
 	public void surfaceDestroyed(SurfaceHolder arg0) {}
 
 	public void onPreviewFrame(byte[] buffer, Camera arg1) {
 		if(startMs<=0)
 			startMs=System.currentTimeMillis();
 		if(FPSstartMs<0) {
 			FPSstartMs  = System.currentTimeMillis();
 			frame=0;
 		}
 		frame++;
 		if(cam!=null)
 			Log.e("vpx", krAddVideo(cam, buffer, (int) (System.currentTimeMillis()-startMs)));
 		camera.addCallbackBuffer(buffer);
 		if(System.currentTimeMillis()>=FPSstartMs+1000) {
 			FPSstartMs=System.currentTimeMillis();
 
 	        this.runOnUiThread(new Runnable() {
 	            public void run() {
 					textView.setText(formatString+" @"+frame+"FPS");
 	            }
 	        });
 			frame=0;
 		}
 	}
 	@Override
 	public void run() {
 		android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);
 		int min = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
 		byte audioBuffer[] = new byte[min/2];
 		while(cam!=null && ar!=null) {
			if(ar!=null)
				ar.read(audioBuffer, 0, min/2);
 			if(cam!=null)
 				krAudioCallback(cam, audioBuffer, audioBuffer.length);
 		}
 	}
 }
