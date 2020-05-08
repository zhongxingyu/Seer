 package ohayon.android.show;
 
 import java.util.ArrayList;
 
 import ohayon.android.fireworks.Firework;
 import ohayon.android.fireworks.FireworkAndTime;
 import ohayon.android.fireworks.World;
 import ohayon.android.fireworks.explosions.Explosion;
 import android.content.Context;
 import android.util.Log;
 
 public class RecordedShow {
 
 	private Boolean isRecording;
 	private World world;
 	private ArrayList<FireworkAndTime> fats;
 	private long startTime;
 	
 	public RecordedShow(World world) {
 		isRecording = false;
 		fats = new ArrayList<FireworkAndTime>();
 		this.world = world;
 		this.startTime = 0;
 	}
 	
 	public void startRecording() {
 		startTime = System.currentTimeMillis();
 		isRecording = true;
 		Log.d("stop Recording", "isRecording: " + isRecording);
 	}
 	
 	public void stopRecording() {
 		isRecording = false;
 		Log.d("stop Recording", "isRecording: " + isRecording);
 	}
 	
 	public void addFirework(float x, float y, Explosion xplsn, int color) {
 		if(isRecording) {
 			double currTime = (System.currentTimeMillis() - startTime)/1000.0;
 			fats.add(new FireworkAndTime
 						(new Firework(x, y, 60, 90, color, 0, xplsn, null),
 							currTime) );			
 		}
 	}
 
 	public boolean isRecording() {
 		return isRecording;
 	}
 	
 	public void startShow(){
 		if(isRecording) {
 			return;
 		} else {
 			world.setTime(0);
 			for(FireworkAndTime f: fats){
 				world.getCannon().addFAT(f);
 			}
 			fats.clear();
 		}
 	}
 	
 	private void startShow(ArrayList<FireworkAndTime> fireworks) {
 		fats.clear();
 		fats.addAll(fireworks);
 		startShow();
 	}
 
 	public String saveShow(Context context) {
 		if(isRecording) {
 			return "In middle of recording. Cannot save show";
 		} else {
 			if (fats.isEmpty()) {
 				return "Nothing has been recorded. Cannot save show";
 			} else {
 				SavedShow savedShow = new SavedShow();
 				savedShow.addFireworks(fats);
 				savedShow.saveShow(context);
 				return "Saved Show Successfully";
 			}
 		}
 	}
 
 	public String startSavedShow(Context context) {
 		if(isRecording) {
 			return "In middle of recording. Cannot save show";
 		} else {
 			SavedShow savedShow = new SavedShow();
 			ArrayList<FireworkAndTime> fireworks = savedShow.getSavedShow(context);
			if (fireworks.isEmpty()){
 				return "Cannot start saved show";
 			}
 			startShow(fireworks);
 			return "Starting Saved Show";
 		}
 	}
 
 	
 }
