 package com.ricex.rpi.remote.android;
 
 import android.app.Fragment;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 
 import com.ricex.rpi.common.message.MovieMessage;
 import com.ricex.rpi.common.message.remote.RemoteClient;
 import com.ricex.rpi.common.message.remote.RemoteMovieMessage;
 import com.ricex.rpi.remote.android.cache.ClientCache;
 import com.ricex.rpi.remote.android.network.ServerConnector;
 
 /** The fragment for displaying the movie controls 
  * 
  * @author Mitchell
  *
  */
 
 public class ControlsFragment extends Fragment implements OnClickListener {	
 
 	
 	private Button butPause;
 	private Button butSeekLeft;
 	private Button butSeekRight;
 	private Button butSeekLeftFast;
 	private Button butSeekRightFast;
 	private Button butPreviousChapter;
 	private Button butNextChapter;
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {		
 		View view = inflater.inflate(R.layout.controls_layout, container, false);	
 		
 		butPause = (Button) view.findViewById(R.id.but_pause);
 		
 		butSeekLeft = (Button) view.findViewById(R.id.but_seek_left);
 		butSeekLeftFast = (Button) view.findViewById(R.id.but_seek_left_fast);
 		
 		butSeekRight = (Button) view.findViewById(R.id.but_seek_right);
 		butSeekRightFast = (Button) view.findViewById(R.id.but_seek_right_fast);
 		
 		butPreviousChapter = (Button) view.findViewById(R.id.but_previous_chapter);
 		butNextChapter = (Button) view.findViewById(R.id.but_next_chapter);
 		
 		butPause.setOnClickListener(this);
 		butSeekLeft.setOnClickListener(this);
 		butSeekLeftFast.setOnClickListener(this);
 		butSeekRight.setOnClickListener(this);
 		butSeekRightFast.setOnClickListener(this);
 		butPreviousChapter.setOnClickListener(this);
 		butNextChapter.setOnClickListener(this);
 		
 		
 		
 		return view;
 		
 	}
 	
 	@Override
 	public void onClick(View v) {
 		RemoteMovieMessage message = null;
 		
 		for (RemoteClient client : ClientCache.getInstance().getEnabledClients()) {
 			if (v.equals(butPause)) {
 				message = new RemoteMovieMessage(MovieMessage.Command.PAUSE, client.getId());
 			}
 			else if (v.equals(butSeekLeft)) {
				message = new RemoteMovieMessage(MovieMessage.Command.SEEK_BACKWARD_SLOW, client.getId());
 			}
 			else if (v.equals(butSeekLeftFast)) {
				message = new RemoteMovieMessage(MovieMessage.Command.SEEK_BACKWARD_FAST, client.getId());
 			}
 			else if (v.equals(butSeekRight)) {
				message = new RemoteMovieMessage(MovieMessage.Command.SEEK_FORWARD_SLOW, client.getId());
 			}
 			else if (v.equals(butSeekRightFast)) {
				message = new RemoteMovieMessage(MovieMessage.Command.SEEK_FORWARD_FAST, client.getId());
 			}
 			else if (v.equals(butPreviousChapter)) {
				message = new RemoteMovieMessage(MovieMessage.Command.PREVIOUS_CHAPTER, client.getId());
 			}
 			else if (v.equals(butNextChapter)) {
				message = new RemoteMovieMessage(MovieMessage.Command.NEXT_CHAPER, client.getId());
 			}
 			ServerConnector.getInstance().sendMessage(message);
 		}	
 	}
 }
