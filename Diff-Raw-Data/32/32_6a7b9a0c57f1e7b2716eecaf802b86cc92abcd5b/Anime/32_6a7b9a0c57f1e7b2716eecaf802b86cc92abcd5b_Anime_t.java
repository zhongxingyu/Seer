 package com.rohit.bookalive;
 
 import java.util.Locale;
 
 import org.opencv.core.Point;
 import org.w3c.dom.Element;
 
 import android.content.Context;
 import android.graphics.drawable.AnimationDrawable;
 import android.media.MediaPlayer;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.RelativeLayout;
 
 public class Anime {
 	private static final String TAG = "Anime";
	private String type;
	private double tlx, tly;
	private double wd, ht;
	private boolean onclick = false;
	private int sound_res = -1;				// -1 => none
 	private RelativeLayout relLayout;
 	private Context context;
 	private AnimationDrawable frameAnime;
 	RelativeLayout.LayoutParams animParams;
 	ImageView animeView;
 	
 	public Anime(RelativeLayout layout, Context context, Element anim) {
 		relLayout = layout;
 		this.context = context;
 		type = anim.getAttribute("type");
 		tlx = Double.parseDouble(anim.getAttribute("tlx"));
 		tly = Double.parseDouble(anim.getAttribute("tly"));
 		wd = Double.parseDouble(anim.getAttribute("width"));
 		ht = Double.parseDouble(anim.getAttribute("height"));
 		if(anim.getAttribute("onclick").toLowerCase(Locale.ENGLISH).equals("true")) onclick = true;
 		initView();
 	}
 	
 	private void initView() {
 		animParams = new RelativeLayout.LayoutParams((int)wd, (int)ht);
 		Point p = new Point(tlx, tly);
 		Util.getPointOnOrig(CapturedImage.H.inv(), p);
 		animParams.topMargin = (int) p.y;
 		animParams.leftMargin = (int) p.x;
 	}
 	
 	private void initAnimate() {
 		animeView = new ImageView(context);
 		animeView.setVisibility(View.VISIBLE);
 		
 		animeView.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				//Log.v(TAG,"clicked");
 				if(onclick) animate();
 			}
 		});
 		if(type.equals("fire")) {
 			animeView.setBackgroundResource(R.drawable.anime_fire);
 		} else if(type.equals("explode")) {
 			animeView.setBackgroundResource(R.drawable.anime_explode);
			sound_res = R.raw.explode;
 		} else if(type.equals("smoke")) {
 			animeView.setBackgroundResource(R.drawable.anime_smoke);
 		} else {
 			throw new IllegalArgumentException("Invalid Anime Type : " + type);
 		}
 		frameAnime = (AnimationDrawable) animeView.getBackground();
 		animeView.setLayoutParams(animParams);
 		relLayout.addView(animeView);
 	}
 	
 	public void animate() {
 		initAnimate();	
 		if(onclick) frameAnime.setOneShot(true);
 		frameAnime.start();
		if(onclick && sound_res != -1) {
			MediaPlayer mp = MediaPlayer.create(context, sound_res);
 			mp.start();
 		}	
 	}
 }
