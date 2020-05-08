 package com.sosohan.snapmv;
 
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import com.coremedia.iso.IsoFile;
 import com.googlecode.mp4parser.authoring.Movie;
 import com.googlecode.mp4parser.authoring.Track;
 import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
 import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
 import com.googlecode.mp4parser.authoring.tracks.AppendTrack;
 
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 
 public class MakeMVActivity extends Activity {
 	private String tag = "MakeMVActivity";
 	private final String outputMV = "/sdcard/DCIM/snap.mp4";
 	
 	private SnapFileWriter debugFile = null;
 	private final String debugFilePath = "/sdcard/jw.yuv";
 	private void debugDumpOpen()
 	{	
 		debugFile = new SnapFileWriter();		
 		debugFile.open(debugFilePath);
 	}
 	private void debugDumpClose()
 	{
 		debugFile.close();
 		debugFile = null;
 	}
 	private void debugDumpWrite(ByteBuffer buf)
 	{
 		if (debugFile == null)
 		{
 			Log.e("JWJWJW", "debug file is not opened.");
 		}
 		debugFile.write(buf);
 	}
 	
 	private SnapFileWriter outputFile = null;
 	private final String intermediateVideo = "/sdcard/snap.h264";
 	private void outputOpen()
 	{	
 		outputFile = new SnapFileWriter();		
 		outputFile.open(intermediateVideo);
 	}
 	private void outputClose()
 	{
 		outputFile.close();
 		outputFile = null;
 	}
 	private void outputWrite(ByteBuffer buf)
 	{
 		if (outputFile == null)
 		{
 			Log.e(tag, "output file is not opened.");
 		}
 		outputFile.write(buf);
 	}
 
 	private ArrayList<String> videoPaths;
 	private String audioPath;
 	private Button btnTest;
 	private View.OnClickListener btnTestListener;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_make_mv);
 		debugFile = new SnapFileWriter();
 		
 		btnTest = (Button) findViewById(R.id.test_btn);		
 		Intent intent = getIntent();
		videoPaths = (ArrayList<String>) intent.getSerializableExtra("videoPaths");
 		audioPath = (String) intent.getExtras().getString("audioPath");
 		Log.i(tag, videoPaths + "," + audioPath);
 		btnTestListener = new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 
 				if(v== btnTest && !videoPaths.isEmpty())
 				{
 					btnTest.setClickable(false);
 					
 					new Thread(new Runnable(){
 						public void run(){
 							//debugDumpOpen();
 							outputOpen();
 							for (int i = 0; i < videoPaths.size() ; i ++)
 							{
 								appendMV(videoPaths.get(i));
 							}
							makeMV();							
 							finish();
 						}						
 					}).start();						
 				}
 			}
 		};		
 		btnTest.setOnClickListener(btnTestListener);	
 	}
 	
 	List<Track> videoTracks = new LinkedList<Track>();
 	void appendMV(String path)
 	{
 		tag = "MakeMVA_appendMV()";
 		try {
 			 Movie m = MovieCreator.build(new FileInputStream(path).getChannel());
 			 Track t = m.getTracks().get(0);
 			 if (t.getHandler().equals("vide")) {
 				 Log.v(tag,"add "+path);
                  videoTracks.add(t);
              }else
              {
             	 Log.w(tag, path + "do NOT have a video in track 0.");
              } 
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	void makeMV()
 	{
 		tag = "MakeMVA_makeMV()";
 		Movie result = new Movie();
 		if (videoTracks.size() > 0) {
 			Log.v(tag,"make mv "+outputMV);
             try {
             	new MovieCreator();
 				Movie audiofile = MovieCreator.build(new FileInputStream(audioPath).getChannel());
             	Track audio = audiofile.getTracks().get(0);
             	Log.v(tag,"videoTracks"+ videoTracks);
 				result.addTrack(new AppendTrack(videoTracks.toArray(new Track[videoTracks.size()])));	
 				result.addTrack(audio);
 				IsoFile out = new DefaultMp4Builder().build(result);
 				FileOutputStream fos;
 				fos = new FileOutputStream(new File(outputMV));
 				out.getBox(fos.getChannel());
 				fos.close();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
         }
 	}
 }
 
