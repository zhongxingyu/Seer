 package com.majomi.zeninstants;
 
 import android.media.MediaPlayer;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.WindowManager;
 import android.widget.ImageButton;
 import android.widget.MediaController;
 import android.widget.TextView;
 import android.widget.VideoView;
 
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.majomi.zeninstants.messagescontroller.VideoManager;
 import com.majomi.zeninstants.messagesentities.MessageVideoEntity;
 import com.majomi.zeninstants.utils.Utils;
 
 public class MessageVideoActivity extends SherlockActivity {
 
 	private MessageVideoEntity entity;
 	private VideoView video;
 //	private ImageView startImage; // image to show instead of black background
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.activity_message_video);
 		getWindow().addFlags( WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
 		
 //		entity = (MessageVideoEntity) getIntent().getExtras().getSerializable("MESSAGE");
 		
 		entity = (MessageVideoEntity) Utils.getOriginalEntity(getIntent().getExtras().getString("msg_from"), getIntent().getExtras().getLong("msg_id"), getIntent().getExtras().getString("msg_type"));

 //		startImage = (ImageView) findViewById(R.id.message_play_icon);
 		
 		fillView();
 
 		new MessageButtonManager(this, entity);
 	}
 
 	public void fillView(){
 		TextView text = (TextView) this.findViewById(R.id.message_text);
 		text.setText(entity.getText());
 
 		ImageButton favorite = (ImageButton) this.findViewById(R.id.message_favorite_btn);
 		if(entity.isFavorite()) {
 			favorite.setImageResource(R.drawable.rating_favorite_red);
 		} else {
 			favorite.setImageResource(R.drawable.rating_favorite);			
 		}
 
 		video = (VideoView) findViewById(R.id.message_video);
 		MediaController mc = new MediaController(this,true);
 
 		video.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
 			@Override
 			public void onCompletion(MediaPlayer mp) {
 				//startImage.setVisibility(View.VISIBLE);
 				mp.reset(); //
 				video.setVideoURI(Uri.parse( VideoManager.getUrlVideoRTSP(entity.getVideo())));
 			}
 		});
 		
 		video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
 			
 			@Override
 			public void onPrepared(MediaPlayer mp) {
 				// TODO Auto-generated method stub
 				//startImage.setVisibility(View.INVISIBLE);
 			}
 		});
 
 //		video.setOnTouchListener( new View.OnTouchListener() {
 //			
 //			@Override
 //			public boolean onTouch(View v, MotionEvent event) {
 //				video.is
 //				return false;
 //			}
 //		});
 
 		video.setMediaController(mc);
 		mc.setAnchorView(video);
 		String rstp = VideoManager.getUrlVideoRTSP(entity.getVideo());
 		if (rstp.contains("://")) // hack if to check if it's a URL
 			video.setVideoURI(Uri.parse( VideoManager.getUrlVideoRTSP(entity.getVideo())));
 
 		//startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("http://www.youtube.com/watch?feature=player_detailpage&v=ykwqXuMPsoc")));
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getSupportMenuInflater().inflate(R.menu.activity_message_video, menu);
 		return true;
 	}
 }
