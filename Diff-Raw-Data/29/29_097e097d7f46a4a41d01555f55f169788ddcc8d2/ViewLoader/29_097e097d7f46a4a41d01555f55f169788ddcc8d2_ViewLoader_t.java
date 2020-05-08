 package yoga1290.schoolmate;
 
import android.graphics.Bitmap;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.media.AudioFormat;
 import android.media.AudioManager;
 import android.media.AudioRecord;
 import android.media.AudioRecord.OnRecordPositionUpdateListener;
 import android.media.AudioTrack;
 import android.media.MediaRecorder;
 import android.media.MediaRecorder.AudioSource;
 import android.os.Bundle;
 import android.os.Handler;
 import android.renderscript.Sampler;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.ImageView;
 
 public class ViewLoader extends Fragment implements OnClickListener
 {
 	View v;
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) 
     {
     	
     		
         // Inflate the layout for this fragment
         v=inflater.inflate(R.layout.view1, container, false);
         Button connect=(Button) v.findViewById(R.id.button1);
         connect.setOnClickListener(this);
         
         ImageView mv=(ImageView) v.findViewById(R.id.imageView1);
         
        Bitmap bitmap=Charts.getTimepiece(200,200, new int[]{1,2,3,4,5,6,7,8,9,10,11,12});
        System.out.println("generated width="+bitmap.getWidth()+",H="+bitmap.getHeight());
        mv.setImageBitmap(bitmap);
         
         System.out.println("LAYOUT FOUND");
         
         // Specify the layout to use when the list of choices appears
 //        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 //        sp.setAdapter(adapter);
 //        ArrayAdapter adapter = new ArrayAdapter(this.getActivity(),android.R.layout.simple_spinner_item, ar);
 //    		sp.setAdapter(adapter);
 //        sp.setOnItemSelectedListener(this);
 
         
         return v;
     }
     
     public AudioRecord findAudioRecord() {
         for (int rate : new int[] { 8000, 11025, 22050, 44100 }) {
             for (short audioFormat : new short[] { AudioFormat.ENCODING_PCM_8BIT, AudioFormat.ENCODING_PCM_16BIT }) {
                 for (short channelConfig : new short[] { AudioFormat.CHANNEL_IN_MONO, AudioFormat.CHANNEL_IN_STEREO }) {
                     try {
 //                        Log.d(C.TAG, "Attempting rate " + rate + "Hz, bits: " + audioFormat + ", channel: "
 //                                + channelConfig);
                         int bufferSize = AudioRecord.getMinBufferSize(rate, channelConfig, audioFormat);
 
                         if (bufferSize != AudioRecord.ERROR_BAD_VALUE) {
                             // check if we can instantiate and have a success
                             AudioRecord recorder = new AudioRecord(AudioSource.DEFAULT, rate, channelConfig, audioFormat, bufferSize);
 
                             if (recorder.getState() == AudioRecord.STATE_INITIALIZED)
                                 return recorder;
                         }
                     } catch (Exception e) {
 //                        Log.e(C.TAG, rate + "Exception, keep trying.",e);
                     }
                 }
             }
         }
         return null;
     }
     
     private static final int AUDIO_SAMPLE_FREQ = 16000;
     private static final int AUDIO_BUFFER_BYTESIZE = AUDIO_SAMPLE_FREQ * 2 * 3; // = 3000ms
     private static final int AUDIO_BUFFER_SAMPLEREAD_SIZE = AUDIO_SAMPLE_FREQ  / 10 * 2; // = 200ms
     
     private OnRecordPositionUpdateListener updateRecord=new OnRecordPositionUpdateListener() {
 		
 		@Override
 		public void onPeriodicNotification(AudioRecord recorder) {
 			
 			AudioTrack track=new AudioTrack(AudioManager.STREAM_SYSTEM, 
 												recorder.getSampleRate(),
 												recorder.getChannelConfiguration(), 
 												recorder.getAudioFormat(),
 												recorder.getMinBufferSize(recorder.getSampleRate(), recorder.getChannelConfiguration(), recorder.getAudioFormat()) , 
 												AudioTrack.MODE_STREAM);
 			byte buff[]=new byte[recorder.getMinBufferSize(recorder.getSampleRate(), recorder.getChannelConfiguration(), recorder.getAudioFormat())];
 			int x=recorder.read(buff, 0, buff.length);
 			while((x=recorder.read(buff, 0, buff.length) )>0)
 				track.write(buff, 0, x);
 			track.release();
 		}
 		
 		@Override
 		public void onMarkerReached(AudioRecord recorder) {
 			// TODO Auto-generated method stub
 			
 		}
 	};
     
 	static AudioRecord recorder;
 	@Override
 	public void onClick(View v) {
 		try{
 		recorder = //findAudioRecord();
 		new AudioRecord(MediaRecorder.AudioSource.MIC,
 	            44100, AudioFormat.CHANNEL_IN_STEREO,
 	            AudioFormat.ENCODING_PCM_16BIT,1000);
 
 		recorder.setPositionNotificationPeriod(5000);
 		
 //	    recorder.setRecordPositionUpdateListener(updateRecord);
 	    recorder.startRecording();
 		}catch(Exception e){((Button)v).setText(e.getMessage());System.out.println(e);}
 //	    Handler handler = new Handler(); 
 //	    handler.postDelayed(new Runnable() { 
 //	    public void run() { 
 //	                 recorder.stop();
 //	                 recorder.release(); 
 //	                 
 //	                 AudioTrack track=new AudioTrack(AudioManager.STREAM_SYSTEM, 
 //								recorder.getSampleRate(),
 //								recorder.getChannelConfiguration(), 
 //								recorder.getAudioFormat(),
 //								recorder.getMinBufferSize(recorder.getSampleRate(), recorder.getChannelConfiguration(), recorder.getAudioFormat()) , 
 //								AudioTrack.MODE_STREAM);
 //					byte buff[]=new byte[recorder.getMinBufferSize(recorder.getSampleRate(), recorder.getChannelConfiguration(), recorder.getAudioFormat())];
 //					int x=recorder.read(buff, 0, buff.length);
 //					while((x=recorder.read(buff, 0, buff.length) )>0)
 //					track.write(buff, 0, x);
 //					track.release();
 //	                      // do your processing here
 ////	                      recordandprocess(); //recursive call to this function to start recording after processing is done
 //	         } 
 //	    }, 1000); 
 	}
 }
