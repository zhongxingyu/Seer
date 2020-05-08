 package somitsolutions.android.audio;
 import android.app.Activity;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.media.AudioFormat;
 import android.media.AudioRecord;
 import android.media.MediaRecorder;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Looper;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 import ca.uol.aig.fftpack.RealDoubleFFT;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.Dictionary;
 import java.util.Hashtable;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicHeader;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.protocol.HTTP;
 import org.json.JSONObject;
 
 //import com.example.myfirstapp.R;
 
 
 public class SoundRecordAndAnalysisActivity extends Activity implements OnClickListener{
 	
     int frequency = 44100;
     int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
     int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
 
 
     private RealDoubleFFT transformer;
     int blockSize = 2048;
     Button startStopButton;
     boolean started = false;
 
     RecordAudio recordTask;
 
     ImageView imageView;
     Bitmap bitmap;
     Canvas canvas;
     Paint paint;
     static SoundRecordAndAnalysisActivity mainActivity;
     
     FileWriter fw = null;
     BufferedWriter bw = null;
     int pastIntDecodedID = 0;
     
     double[] decodedResult = new double[8];
     
     //Dictionary d = new Hashtable();
     
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         //d.put("00101010", "545");
     }
 
     private class RecordAudio extends AsyncTask<Void, double[], Void> {
         @Override
         protected Void doInBackground(Void... params) {
             int bufferSize = AudioRecord.getMinBufferSize(frequency,
                 channelConfiguration, audioEncoding);
             AudioRecord audioRecord = new AudioRecord(
                 MediaRecorder.AudioSource.DEFAULT, frequency,
                 channelConfiguration, audioEncoding, bufferSize);
 
             short[] buffer = new short[blockSize];
             double[] toTransform = new double[blockSize];
             try{
             	audioRecord.startRecording();
             }
             catch(IllegalStateException e){
             	Log.e("Recording failed", e.toString());
             }
             while (started) {
                 int bufferReadResult = audioRecord.read(buffer, 0, blockSize);
     
                 for (int i = 0; i < blockSize && i < bufferReadResult; i++) {
                     toTransform[i] = (double) buffer[i] / 32768.0; // signed 16 bit
                 }
 
                 transformer.ft(toTransform);
                 publishProgress(toTransform);
             }
             try{
             	audioRecord.stop();
             }
             catch(IllegalStateException e){
             	Log.e("Stop failed", e.toString());
             }
             return null;
         }
         
         protected void onProgressUpdate(double[]... toTransform) {
             Log.e("RecordingProgress", "Displaying in progress");
 
             paint.setColor(Color.GREEN);
             int xOffset = 1840;
             int xScale = 8;
             canvas.drawColor(Color.BLACK);
             paint.setStrokeWidth(2);
             try {
                 java.util.Date date = new java.util.Date();
                 bw.write("\n"+date.getTime() + " ");
                 for (int i = xOffset; i < 1970/*toTransform[0].length*/; i++) {
                     int x = (i*1 - xOffset) * xScale;
                     int upy = (int) (100 - Math.abs((toTransform[0][i] * 10)))*10;
                     int downy = 100*10;
                     canvas.drawLine(x, downy, x, upy, paint);
                     bw.write(Double.toString(Math.abs(toTransform[0][i]))+" ");
                 }
 
                 bw.write(";");
             }
             catch (IOException e) {
                 e.printStackTrace();
             }
             // Decoding ID
             double basicNoiseRef = -1.0;
             for (int i = xOffset; i < (xOffset + 8); i++) {
                 if (Math.abs(toTransform[0][i]) > basicNoiseRef) {
                     basicNoiseRef = Math.abs(toTransform[0][i]);
                 }
             }
             
             double[] maxData = new double[8];
             
             for (int i = 0; i < 8; i++) {
                 maxData[i] = -1.0;
             }
             
             int[] refSignalIdx = new int[8];
             refSignalIdx[0] = 1857;
             refSignalIdx[1] = 1868;
             refSignalIdx[2] = 1880;
             refSignalIdx[3] = 1892;
             refSignalIdx[4] = 1904;
             refSignalIdx[5] = 1915;
             refSignalIdx[6] = 1927;
             refSignalIdx[7] = 1938;
             
             int refRange = 1;
             int dataIdx = 0;
             for (int i = (refSignalIdx[dataIdx] - refRange); i < (refSignalIdx[dataIdx] + refRange); i++) {
                 if (Math.abs(toTransform[0][i]) > maxData[dataIdx]) {
                     maxData[dataIdx] = Math.abs(toTransform[0][i]);
                 }
             }
             dataIdx++;
             
             for (int i = (refSignalIdx[dataIdx] - refRange); i < (refSignalIdx[dataIdx] + refRange); i++) {
                 if (Math.abs(toTransform[0][i]) > maxData[dataIdx]) {
                     maxData[dataIdx] = Math.abs(toTransform[0][i]);
                 }
             }
             dataIdx++;
             
             for (int i = (refSignalIdx[dataIdx] - refRange); i < (refSignalIdx[dataIdx] + refRange); i++) {
                 if (Math.abs(toTransform[0][i]) > maxData[dataIdx]) {
                     maxData[dataIdx] = Math.abs(toTransform[0][i]);
                 }
             }
             dataIdx++;
             
             for (int i = (refSignalIdx[dataIdx] - refRange); i < (refSignalIdx[dataIdx] + refRange); i++) {
                 if (Math.abs(toTransform[0][i]) > maxData[dataIdx]) {
                     maxData[dataIdx] = Math.abs(toTransform[0][i]);
                 }
             }
             dataIdx++;
             
             for (int i = (refSignalIdx[dataIdx] - refRange); i < (refSignalIdx[dataIdx] + refRange); i++) {
                 if (Math.abs(toTransform[0][i]) > maxData[dataIdx]) {
                     maxData[dataIdx] = Math.abs(toTransform[0][i]);
                 }
             }
             dataIdx++;
             
             for (int i = (refSignalIdx[dataIdx] - refRange); i < (refSignalIdx[dataIdx] + refRange); i++) {
                 if (Math.abs(toTransform[0][i]) > maxData[dataIdx]) {
                     maxData[dataIdx] = Math.abs(toTransform[0][i]);
                 }
             }
             dataIdx++;
             
             for (int i = (refSignalIdx[dataIdx] - refRange); i < (refSignalIdx[dataIdx] + refRange); i++) {
                 if (Math.abs(toTransform[0][i]) > maxData[dataIdx]) {
                     maxData[dataIdx] = Math.abs(toTransform[0][i]);
                 }
             }
             dataIdx++;
             
             for (int i = (refSignalIdx[dataIdx] - refRange); i < (refSignalIdx[dataIdx] + refRange); i++) {
                 if (Math.abs(toTransform[0][i]) > maxData[dataIdx]) {
                     maxData[dataIdx] = Math.abs(toTransform[0][i]);
                 }
             }
             dataIdx++;
 
             double[] noiseRef = new double[8];
             
             for (int i = 0; i < 8; i++) {
                 noiseRef[i] = -1.0;
             }
             
             String decodedID = "";
             int intDecodedID = 0;
             for (dataIdx = 0; dataIdx < 8; dataIdx++) {
                 double tempBaseNoise = 0;
                 if (dataIdx < 4) {
                     tempBaseNoise = 0.15;
                 }
                 else if (dataIdx < 6) {
                     tempBaseNoise = 0.08;
                 }
                 else {
                     tempBaseNoise = 0.05;
                 }
                 if (maxData[dataIdx] > tempBaseNoise + 1.25*Math.abs(toTransform[0][refSignalIdx[dataIdx] - 3])
                         && maxData[dataIdx] > tempBaseNoise + 1.25*Math.abs(toTransform[0][refSignalIdx[dataIdx] + 3])) {
                     decodedResult[dataIdx] = 0.9 * decodedResult[dataIdx] + 0.1;
                 }
                 else {
                     decodedResult[dataIdx] = 0.9 * decodedResult[dataIdx];
                 }
                 if (decodedResult[dataIdx] > 0.60) {
                     decodedID += "1";
                     intDecodedID <<= 1;
                     intDecodedID += 1;
                 }
                 else {
                     decodedID += "0";
                     intDecodedID <<= 1;
                 }
             }
             if (intDecodedID != pastIntDecodedID) {
                 pastIntDecodedID = intDecodedID;
                sendJson(intDecodedID + "");
             }
             /*
             for (dataIdx = 7; dataIdx < 8; dataIdx++) {
                 if (maxData[dataIdx] > (basicNoiseRef / 1.5)
                         && maxData[dataIdx] > Math.abs(toTransform[0][refSignalIdx[dataIdx] - 2])
                         && maxData[dataIdx] > Math.abs(toTransform[0][refSignalIdx[dataIdx] + 2])) {
                     decodedResult[dataIdx] = true;
                     decodedID += "1";
                 }
                 else {
                     decodedResult[dataIdx] = false;
                     decodedID += "0";
                 }
             }*/
             TextView t = (TextView)findViewById(R.id.decodedID);
             t.setText(decodedID);
             //
 
             int textSize = 40;
             paint.setTextSize(textSize);
             paint.setColor(Color.WHITE);
             int x = 0;
             int downy = 1100;
             int upy = 1000;
             canvas.drawText("(kHz)", x, downy+textSize, paint);
             
             x = (1857 - xOffset) * xScale;
             canvas.drawLine(x, upy, x, downy, paint);
             canvas.drawText("20.0", x-textSize, downy+textSize, paint);
             // 1868
             x = (1880 - xOffset) * xScale;
             canvas.drawLine(x, upy, x, downy, paint);
             canvas.drawText("20.25", x-textSize, downy+textSize, paint);
             // 1892
             x = (1904 - xOffset) * xScale;
             canvas.drawLine(x, upy, x, downy, paint);
             canvas.drawText("20.5", x-textSize, downy+textSize, paint);
             // 1915
             x = (1927 - xOffset) * xScale;
             canvas.drawLine(x, upy, x, downy, paint);
             canvas.drawText("20.75", x-textSize, downy+textSize, paint);
             // 1938
             x = (1950 - xOffset) * xScale;
             canvas.drawLine(x, upy, x, downy, paint);
             canvas.drawText("21.0", x-textSize, downy+textSize, paint);
             imageView.invalidate();
             /*
             canvas.drawColor(Color.BLACK);
             paint.setStrokeWidth(1);
             for (int i = 0; i < toTransform[0].length; i++) {
                 int x = i*1;
                 int downy = (int) (100 - Math.abs((toTransform[0][i] * 10)))*4;
                 int upy = 100*4;
                 canvas.drawLine(x, downy, x, upy, paint);
             }
             imageView.invalidate();
             */
         }
     }
 
     public void onClick(View v) {
         if (started) {
             started = false;
             startStopButton.setText("Start");
             recordTask.cancel(true);
         } else {
             started = true;
             startStopButton.setText("Stop");
             recordTask = new RecordAudio();
             recordTask.execute();
         }
      }
         
     static SoundRecordAndAnalysisActivity getMainActivity(){
     	return mainActivity;
     }
         
     public void onStop(){
     	super.onStop();
     	started = false;
         startStopButton.setText("Start");
     	recordTask.cancel(true);
         try {
                 if (bw != null) {
                         bw.close();
                         bw = null;
                 }
         	if (fw != null) {
         	        fw.close();
         	        fw = null;
         	}
         }
         catch (IOException e) {
             e.printStackTrace();
         }
     }
         
     public void onStart(){
     	super.onStart();
     	java.util.Date date = new java.util.Date();
 
         for (int i = 0; i < 8; i++) {
             decodedResult[i] = 0.0;
         }
     	try {
     	    //bufferedOutputStream jk = new bufferedOutputStream (New FileOutputStream ("out_" + data.getTime()+ ".txt")));
     	    
     	    fw = new FileWriter(Environment.getExternalStorageDirectory().toString() + "/out_" + date.getTime() + ".txt");
     	    bw = new BufferedWriter(fw);
     	}
     	catch (IOException e) {
     	    e.printStackTrace();
     	}
     	setContentView(R.layout.main);
         
         startStopButton = (Button) this.findViewById(R.id.StartStopButton);
         startStopButton.setOnClickListener(this);
 
         transformer = new RealDoubleFFT(blockSize);
 
         imageView = (ImageView) this.findViewById(R.id.imageView1);
         bitmap = Bitmap.createBitmap((int)1024,(int)1400,Bitmap.Config.ARGB_8888);
         canvas = new Canvas(bitmap);
         paint = new Paint();
         paint.setColor(Color.GREEN);
         imageView.setImageBitmap(bitmap);
         //imageView.
         mainActivity = this;
     }
    private String getUnixTime() {
         return  Long.toString(System.currentTimeMillis());
     };
    protected void sendJson(final String RoomId) {
         try {
             // http://ar1.openbms.org:8079/add/KaWpzcDzD6nyjiiliuNMqJiXZzKHxwQPFiSR
             final URI url = new URI("http://ar1.openbms.org:8079/add/KaWpzcDzD6nyjiiliuNMqJiXZzKHxwQPFiSR");
         
             Thread t = new Thread(){
             public void run() {
                     Looper.prepare(); //For Preparing Message Pool for the child Thread
                     HttpClient client = new DefaultHttpClient();
                     HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000); //Timeout Limit
                     HttpResponse response;
                     JSONObject location = new JSONObject();
                     JSONObject json = new JSONObject();
                     try{
                         HttpPost post = new HttpPost(url);
                         location.put("DisplayName", "Ben Zhang");
                         location.put("UniqueName", "ee149.benzhang");
                        location.put("Readings", "[["+ getUnixTime() + "," + RoomId + "]]");
                         location.put("UniqueName", "ee149.benzhang");
                         location.put("uuid", "d8401a6e-2313-11e2-99e6-b8f6b119696f");
                        json.put("location", location.toString());
                         StringEntity se = new StringEntity(json.toString());  
                         se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                         post.setEntity(se);
                         response = client.execute(post);
                         /*Checking response */
                         if(response!=null){
                             InputStream in = response.getEntity().getContent(); //Get the data in the entity
                         }
                     }
                     catch(Exception e){
                         e.printStackTrace();
                         // createDialog("Error", "Cannot Estabilish Connection");
                     }
                     Looper.loop(); //Loop in the message queue
                 }
 
             private String getUnixTime() {
                 // TODO Auto-generated method stub
                 return null;
             }
             };
             t.start();  
         } catch (URISyntaxException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
     }
 }
     
