 package me.kukkii.cointoss;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.graphics.drawable.Drawable;
 import android.media.AudioManager;
 import android.media.SoundPool;
 import android.media.SoundPool.OnLoadCompleteListener;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ImageButton;
 import android.widget.EditText;
 import android.widget.ImageView;
 
 public class SoundFragment extends Fragment implements OnClickListener {
 
     ImageButton musicButton = null;
     ImageButton soundButton = null;
     SoundManager manager = null;
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         if (manager == null) {
           SoundManager.setContext( getActivity() );
           manager = SoundManager.getSoundManager();
         }
 
         // Inflate the layout for this fragment
         return inflater.inflate(R.layout.sound_fragment, container, false);
     }
 
     public void onStart() {
       super.onStart();
 
      musicButton = (ImageButton) getActivity().findViewById(R.id.music_button);
       musicButton.setImageResource(manager.isPlaying()?R.drawable.sound_on_120px_vista_kmixdocked:R.drawable.sound_off_120px_vista_kmixdocked_error);
       musicButton.setOnClickListener(this);
 
       soundButton = (ImageButton) getActivity().findViewById(R.id.sound_button);
       soundButton.setImageResource(manager.isSounding()?R.drawable.sound_on_120px_vista_kmixdocked:R.drawable.sound_off_120px_vista_kmixdocked_error);
       soundButton.setOnClickListener(this);
     }
 
     public void onResume() {
       super.onResume();
 
       if (manager.isPlaying()) {
         manager.startPlay();
         musicButton.setImageResource(manager.isPlaying()?R.drawable.sound_on_120px_vista_kmixdocked:R.drawable.sound_off_120px_vista_kmixdocked_error);
       }
     }
 
     public void onPause() {
       super.onPause();
 
       if (manager.isPlaying()) {
         manager.stopPlay();
       }
     }
 
     public void onClick(View view) {
       if (view == musicButton) {
         if (manager.isPlaying()) {
           manager.stopPlay();
           manager.setPlaying(false);
         }
         else {
           manager.startPlay();
           manager.setPlaying(true);
         }
         musicButton.setImageResource(manager.isPlaying()?R.drawable.sound_on_120px_vista_kmixdocked:R.drawable.sound_off_120px_vista_kmixdocked_error);
       }
       else {
         if (manager.isSounding()) {
           manager.setSounding(false);
         }
         else {
           manager.setSounding(true);
         }
         soundButton.setImageResource(manager.isSounding()?R.drawable.sound_on_120px_vista_kmixdocked:R.drawable.sound_off_120px_vista_kmixdocked_error);
       }
     }
 
 }
