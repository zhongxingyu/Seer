 package net.onthewings.bruceleemotion;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URL;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.mime.HttpMultipartMode;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.FileBody;
 import org.apache.http.entity.mime.content.StringBody;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.CoreProtocolPNames;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.graphics.Bitmap;
 import android.graphics.Bitmap.CompressFormat;
 import android.graphics.BitmapFactory;
 import android.graphics.PixelFormat;
 import android.graphics.Rect;
 import android.hardware.Camera;
 import android.hardware.Camera.AutoFocusCallback;
 import android.hardware.Camera.PictureCallback;
 import android.hardware.Camera.ShutterCallback;
 import android.hardware.Camera.Size;
 import android.os.Bundle;
 import android.os.Environment;
 import android.util.Base64;
 import android.util.Log;
 import android.view.SurfaceHolder;
 import android.view.SurfaceHolder.Callback;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup.LayoutParams;
 import android.view.Window;
 import android.widget.ImageView;
 
 public class BruceLeeMotionActivity extends Activity implements Callback {
 	private String ABSOLUT_PATH = "http://bruceleemotion.onthewings.net/";
 	private float picRatio = 16.0f / 9.0f;
 	private Camera camera;
 	private SurfaceView cameraSurfaceView;
 	private SurfaceHolder cameraSurfaceHolder;
 	private ImageView overlayView;
 	
 	public String getStringFromHttpResponse(HttpResponse response) {
 		BufferedReader in = null;
 		try {
 			in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
 	
 	        StringBuffer sb = new StringBuffer("");
 	        String line = "";
 	
 	        String NL = System.getProperty("line.separator");
 	        while ((line = in.readLine()) != null) 
 	        {
 	            sb.append(line + NL);
 	        }
 	        in.close();
 	        String page = sb.toString();
 	        //System.out.println(page);
 	        return page;
 	    } catch (IOException e) {
 	    	Log.e("Error", e.getMessage());
 	    } finally {
 	        if (in != null) 
 	        {
 	            try 
 	            {
 	                in.close();
 	            } 
 	            catch (IOException e)    
 	            {
 	                Log.d("BBB", e.toString());
 	            }
 	        }
 	    }
 		
 		return "";
 	}
 	
 	public String executeHttpGet(String URL) throws Exception {
 	    BufferedReader in = null;
 	    
         HttpClient client = new DefaultHttpClient();
         client.getParams().setParameter(CoreProtocolPNames.USER_AGENT, "android");
         HttpGet request = new HttpGet();
         request.setHeader("Content-Type", "text/plain; charset=utf-8");
         request.setURI(new URI(URL));
         HttpResponse response = client.execute(request);
         return getStringFromHttpResponse(response);
 	}
 	
 	public int index;
 	
 	public void loadFrame() {
 		try {
     		JSONObject jsonObject = new JSONObject(executeHttpGet(ABSOLUT_PATH + "motions/brucelee/frame/thumb/random_800"));
     		index = jsonObject.getInt("index");
     		URL url = new URL(jsonObject.getString("original"));
     		Bitmap bmp = BitmapFactory.decodeStream(url.openStream());
         	overlayView.setImageBitmap(bmp);
         	overlayView.setAlpha(150);
     	} catch (Exception e) {
     		Log.e("Error", e.getMessage());
 		}
 	}
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	super.onCreate(savedInstanceState);
     	setContentView(R.layout.main);
     	
     	cameraSurfaceView = (SurfaceView) findViewById(R.id.cameraSurfaceView);
     	cameraSurfaceHolder = cameraSurfaceView.getHolder();
     	cameraSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
 	    cameraSurfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
     	cameraSurfaceHolder.addCallback(this);
     	
     	overlayView = (ImageView) findViewById(R.id.overlayView);
     	loadFrame();    	
     	overlayView.setOnClickListener(overlayViewClickListener);
     }
     
     private ShutterCallback onShutter = new ShutterCallback(){
 		public void onShutter() {
 			// No action to be perfomed on the Shutter callback.
 		}
 	};
 	
 	private PictureCallback onRaw = new PictureCallback(){
 		public void onPictureTaken(byte[] data, Camera camera) {
 			// No action taken on the raw data. Only action taken on jpeg data.
 		}
 	};
 
 	private PictureCallback onJpeg = new PictureCallback(){
 		public void onPictureTaken(byte[] data, Camera camera) {
 			Date date = new Date();
 			String picName = index + "_" + new SimpleDateFormat("yyyyMMdd-HHmmss").format(date) + ".jpg";
 			File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/BruceLeeMotion/");
 			path.mkdirs();
 		    File file = new File(path, picName);
 
 		    try {
 		    	/*
 		    	 * save to sdcard
 		    	 */
 		        OutputStream os = new FileOutputStream(file);
 		        os.write(data);
 		        os.close();
 		        
 		        /*
 		         * upload to server
 		         */
 	            HttpClient httpclient = new DefaultHttpClient();
 	            HttpPost httppost = new HttpPost(ABSOLUT_PATH + "motions/brucelee/upload/" + index);
 	            
 	            MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
                 entity.addPart("date", new StringBody(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date)));
                 entity.addPart("photo", new FileBody(file));
 	            httppost.setEntity(entity);
 	            
 	            HttpResponse response = httpclient.execute(httppost);
 	            JSONObject jsonObject = new JSONObject(getStringFromHttpResponse(response));
 	            //HttpEntity rEntity = response.getEntity();
 	            //is = entity.getContent();
 	            httpclient.getConnectionManager().shutdown(); 
 		    } catch (Exception e) {
 		        Log.e("Error", e.getMessage());
 		    }
 		    
 		    loadFrame();
 		    camera.startPreview();
 		}
 	};
 	
 	private AutoFocusCallback onAutoFocus = new AutoFocusCallback() {
 		public void onAutoFocus(boolean success, Camera camera) {
 			if (success) {
 				camera.takePicture(onShutter, onRaw, onJpeg);
 			}
 		}
 	};
     
     private OnClickListener overlayViewClickListener = new OnClickListener() {       
         public void onClick(View v) {
         	camera.autoFocus(onAutoFocus);
         }
     };
     
     @Override
     public void onWindowFocusChanged(boolean hasFocus) {
 		super.onWindowFocusChanged(hasFocus);
 		
 		View decorView = getWindow().getDecorView();
         int viewWidth = (int) (decorView.getWidth() * 0.8);
         int viewHeight = (int) (decorView.getHeight() * 0.8);
 
         float surfaceRatio = (float)viewWidth/(float)viewHeight;
         if (surfaceRatio > picRatio) {
         	viewWidth = (int) (picRatio * viewHeight);
         } else if (surfaceRatio < picRatio) {
         	viewHeight = (int) (viewWidth / picRatio);
         }
         Log.d("view size", viewWidth + " " + viewHeight);
 
     	LayoutParams lparam;
     	lparam = cameraSurfaceView.getLayoutParams();
     	lparam.width = viewWidth;
     	lparam.height = viewHeight;
     	cameraSurfaceView.setLayoutParams(lparam);
     	
     	lparam = overlayView.getLayoutParams();
     	lparam.width = viewWidth;
     	lparam.height = viewHeight;
     	overlayView.setLayoutParams(lparam);
         
    	overlayView.bringToFront();
     }
     
     public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
         Rect surfaceFrame = holder.getSurfaceFrame();
         int thumbWidth = surfaceFrame.width();
         int thumbHeight = surfaceFrame.height();
         
         //in case the surface is not layouted yet
         if (thumbWidth < 10) return;
         
         Log.d("thumb size", thumbWidth + " " + thumbHeight);
         
 
         Camera.Parameters p = camera.getParameters();
         Size picSize = p.getPictureSize();
         
         /*
          * Find a largest picRatio(16:9) picture size.
          */
         Camera.Size pictureSize = null;
         for (Camera.Size s : p.getSupportedPictureSizes()) {
         	float ratio = (float) s.width / (float) s.height;
         	if (ratio == picRatio) {
         		if (pictureSize == null || s.width > pictureSize.width) {
         			pictureSize = s;
         		}
         	}
         }
         p.setPictureSize(pictureSize.width, pictureSize.height);
         Log.d("pictureSize", pictureSize.width + " " + pictureSize.height);
         
         /*
          * Find a picRatio(16:9) preview size
          */
         Camera.Size previewSize = null;
         for (Camera.Size s : p.getSupportedPreviewSizes()) {
         	float ratio = (float) s.width / (float) s.height;
         	if (ratio == picRatio) {
         		if (previewSize == null || Math.abs(s.width - thumbWidth) < Math.abs(previewSize.width - thumbWidth)) {
         			previewSize = s;
         		}
         	}
         }
         p.setPreviewSize(previewSize.width, previewSize.height);
         Log.d("previewSize", previewSize.width + " " + previewSize.height);
         
         try {
         	camera.setParameters(p);
         	camera.setPreviewDisplay(holder);
         } catch (IOException e) {
         	e.printStackTrace();
         }
         camera.startPreview();
         
         overlayView.bringToFront();
     }
 
     public void surfaceCreated(SurfaceHolder holder) {
     	camera = Camera.open();
     }
 
     public void surfaceDestroyed(SurfaceHolder holder) {
         camera.stopPreview();
         camera.release();
     }
 }
