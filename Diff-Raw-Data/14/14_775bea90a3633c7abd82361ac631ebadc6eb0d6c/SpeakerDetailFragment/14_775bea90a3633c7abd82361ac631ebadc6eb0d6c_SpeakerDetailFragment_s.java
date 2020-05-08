 package albin.oredev2012.fragment;
 
 import albin.oredev2012.R;
 import albin.oredev2012.imageCache.ImageCache;
 import albin.oredev2012.imageCache.ImageCache.OnImageLoadedListener;
 import albin.oredev2012.model.Session;
 import albin.oredev2012.model.Speaker;
 import albin.oredev2012.repo.Repository;
 import albin.oredev2012.ui.SpeakerSessionItemView;
 import albin.oredev2012.ui.SpeakerSessionItemView_;
 import albin.oredev2012.ui.TextAroundImageLayout;
 import android.graphics.Bitmap;
 import android.support.v4.app.Fragment;
 import android.view.ViewGroup;
 import android.widget.TextView;
 
 import com.googlecode.androidannotations.annotations.AfterInject;
 import com.googlecode.androidannotations.annotations.AfterViews;
 import com.googlecode.androidannotations.annotations.Background;
 import com.googlecode.androidannotations.annotations.Bean;
 import com.googlecode.androidannotations.annotations.EFragment;
 import com.googlecode.androidannotations.annotations.UiThread;
 import com.googlecode.androidannotations.annotations.ViewById;
 
 @EFragment(R.layout.speaker_detail_fragment)
 public class SpeakerDetailFragment extends Fragment {
 
 	@ViewById(R.id.name)
 	protected TextView nameView;
 
 	@ViewById(R.id.speaker_info)
 	protected TextAroundImageLayout speakerInfo;
 
 	@ViewById(R.id.sessions)
 	protected ViewGroup sessions;

 	@Bean
 	protected Repository repo;
 
 	@Bean
 	protected ImageCache imageCache;
 
 	private String speakerId;
 
 	private Speaker speaker;
 
 	@AfterInject
 	protected void afterInject() {
 		setRetainInstance(false);
 		if (isInitalized()) {
 			initInBackground();
 		}
 	}
 
 	@AfterViews
 	protected void afterViews() {
 		if (isInitalized()) {
 			initInBackground();
 		}
 	}
 
 	public void setSpeakerId(String speakerId) {
 		this.speakerId = speakerId;
 		if (isInitalized()) {
 			initInBackground();
 		}
 	}
 
 	private boolean isInitalized() {
 		return imageCache != null && speakerInfo != null && speakerId != null;
 	}
 
 	@Background
 	protected void initInBackground() {
 		speaker = repo.getSpeaker(speakerId);
 		if (speaker == null) {
 			return;
 		}
 		Bitmap image = imageCache.getImage(speaker.getImageUrl(), true,
 				new OnImageLoadedListener() {
 
 					@Override
 					public void onImageLoaded(String url, Bitmap bitmap) {
 						setImage(bitmap);
 					}
 				});
 		initViews();
 		if (image != null) {
 			setImage(image);
 		}
 	}
 
 	@UiThread
 	protected void initViews() {
 		nameView.setText(speaker.getName());
 		speakerInfo.setText(speaker.getBiograhpy());
 		showSessions();
 	}
 
 	private void showSessions() {
 		sessions.removeAllViews();
 		for (Session session : speaker.getSessions()) {
			SpeakerSessionItemView sessionView = SpeakerSessionItemView_
 					.build(getActivity());
 			sessionView.bind(session);
 			sessions.addView(sessionView);
 		}
 	}
 
 	@UiThread
 	protected void setImage(Bitmap bitmap) {
 		speakerInfo.setImageBitmap(bitmap);
 	}
 
 }
