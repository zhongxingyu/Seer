 package com.majomi.zeninstants.messagesviews;
 
 import android.app.Activity;
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.majomi.zeninstants.R;
 import com.majomi.zeninstants.messagesentities.MessageImageEntity;
 import com.majomi.zeninstants.messagesentities.MessageSoundEntity;
 import com.majomi.zeninstants.messagesentities.MessageTextEntity;
 import com.majomi.zeninstants.messagesentities.MessageVideoEntity;
 
 
 public class HistorialViewsManager {
 
 	//********** Singleton *************
 	static private HistorialViewsManager historialViewsManager = null;
 
 	static public HistorialViewsManager getHistorialViewsManager() {
 		if(historialViewsManager == null) {
 			historialViewsManager = new HistorialViewsManager();
 		}	
 		return historialViewsManager;
 	}
 
 	private HistorialViewsManager() {	
 	}
 	//******** END Singleton ***********
 
 
 	public View createHistorialView(MessageTextEntity entity, Activity activity) {
 
 		if(entity instanceof MessageImageEntity) {
 			return createHistorialViewImage((MessageImageEntity)entity, activity);
 
 		} else if(entity instanceof MessageSoundEntity) {
 			return createHistorialViewSound((MessageSoundEntity)entity, activity);
 
 		} else if(entity instanceof MessageVideoEntity) {
 			return createHistorialViewVideo((MessageVideoEntity)entity, activity);
 
 		} else {
 			return createHistorialViewText(entity, activity);
 		}
 	}
 
 	private View createHistorialViewText(MessageTextEntity entity, Activity activity){
 		LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		View vi = inflater.inflate(R.layout.historial_message_text, null);
 
 		TextView summarytext = (TextView) vi.findViewById(R.id.hmtext);
 		summarytext.setText(entity.getSummary());
 
 		return vi;
 	}
 
 	@SuppressWarnings("unused")
 	private View createHistorialViewImage(MessageImageEntity entity, Activity activity){
 		LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		View vi = inflater.inflate(R.layout.historial_message_image_and_text, null);
 
 		TextView summarytext = (TextView) vi.findViewById(R.id.historial_image_text);
 		summarytext.setText(entity.getSummary());
 
 		int imageResource = activity.getResources().getIdentifier(
 				((MessageImageEntity)entity).getImage(), null, activity.getPackageName());
 
 		ImageView image = (ImageView) vi.findViewById(R.id.historial_image);
 		//image.setImageDrawable(act.getResources().getDrawable(imageResource));
 
 		return vi;
 	}
 
 	private View createHistorialViewSound(MessageSoundEntity entity, Activity activity){
 		LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		View vi = inflater.inflate(R.layout.historial_message_sound, null);
 
 		TextView summarytext = (TextView) vi.findViewById(R.id.historial_audio_text);
 		summarytext.setText(entity.getSummary());
 
 		return vi;
 	}
 
 	private View createHistorialViewVideo(MessageVideoEntity entity, Activity activity){
 		LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		View vi = inflater.inflate(R.layout.historial_message_video, null);
 
 		TextView summarytext = (TextView) vi.findViewById(R.id.historial_video_text);
 		summarytext.setText(entity.getSummary());
 
 		return vi;
 	}
 
 
 
 }
