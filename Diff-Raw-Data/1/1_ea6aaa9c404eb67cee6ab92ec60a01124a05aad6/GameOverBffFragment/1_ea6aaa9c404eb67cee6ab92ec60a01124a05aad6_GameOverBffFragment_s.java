 package com.rushdevo.twittaddict.ui.fragments;
 
 import java.io.InputStream;
 import java.net.URL;
 
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.rushdevo.twittaddict.R;
 
 public class GameOverBffFragment extends Fragment {
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		super.onCreateView(inflater, container, savedInstanceState);
 		View view = inflater.inflate(R.layout.game_over_bff, container, false);
 		setupBff(view);
 		return view;
 	}
 	
 	private void setupBff(View view) {
 		TextView bffScreenNameView = (TextView)view.findViewById(R.id.bff_screen_name);
 		ImageView bffAvatarView = (ImageView)view.findViewById(R.id.bff_avatar);
 		Bundle bundle = getActivity().getIntent().getExtras();
 		String bffScreenName = bundle.getString("bffScreenName");
 		String bffProfileImageUrl = bundle.getString("bffProfileImageUrl");
 		
 		bffScreenNameView.setText(bffScreenName);
 		Drawable bffAvatar = null;
 		try {
     		URL url = new URL(bffProfileImageUrl);
     		InputStream is = (InputStream)url.getContent();
     		bffAvatar = Drawable.createFromStream(is, bffScreenName);
     		bffAvatarView.setImageDrawable(bffAvatar);
     	} catch (Exception e) {
     		Log.d("GameOverBffFragment", e.getMessage());
     	}
 	}
 	
 }
