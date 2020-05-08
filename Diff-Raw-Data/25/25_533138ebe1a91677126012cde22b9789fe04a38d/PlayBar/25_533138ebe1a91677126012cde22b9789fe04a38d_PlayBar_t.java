 package pro.trousev.cleer.android.userInterface;
 
 import pro.trousev.cleer.Item.NoSuchTagException;
 import pro.trousev.cleer.Messaging;
 import pro.trousev.cleer.Messaging.Event;
 import pro.trousev.cleer.Messaging.Message;
 import pro.trousev.cleer.Player;
 import pro.trousev.cleer.Player.PlayerChangeEvent;
 import pro.trousev.cleer.Player.Status;
 import pro.trousev.cleer.android.AndroidMessages.Action;
 import pro.trousev.cleer.android.Constants;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 public class PlayBar extends Fragment implements OnClickListener {
 	private Button playPauseBtn, prevCompBtn, nextCompBtn, queueBtn,
 			mainMenuBtn;
 	private TextView compName;
 	private MainActivity root;
 	final int PLAYING = 1;
 	final int STOPPED = 0;
 	final int PAUSED = 2;
 	private int status = STOPPED;
 	public static ProgressBar progressBar;
 	private PlayerChangedStatusEvent playerChangedStatusEvent;
 
 	public PlayBar() {
 	}
 
 	private class PlayerChangedStatusEvent implements Event {
 		@Override
 		public void messageReceived(Message message) {
 			PlayerChangeEvent ev = (PlayerChangeEvent) message;
 			if ((ev.status == Status.Playing)
 					|| (ev.status == Status.Processing)) {
 				changeStatus(PLAYING);
 			} else {
 				if (ev.status == Status.Paused)
 					changeStatus(PAUSED);
 				else
 					changeStatus(STOPPED);
 			}
 			if (ev.track != null) {
 				try {
 					compName.setText(ev.track.tag("title").value());
 				} catch (NoSuchTagException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View view = inflater.inflate(R.layout.play_bar, null, false);
 		root = (MainActivity) getActivity();
 		compName = (TextView) view.findViewById(R.id.playing_compotision_name);
  		playPauseBtn = (Button) view.findViewById(R.id.play_pause_btn);
 		prevCompBtn = (Button) view.findViewById(R.id.prev_comp_btn);
 		nextCompBtn = (Button) view.findViewById(R.id.next_comp_btn);
 		queueBtn = (Button) view.findViewById(R.id.queue_btn);
 		mainMenuBtn = (Button) view.findViewById(R.id.main_menu_btn);
 		progressBar = (ProgressBar) view.findViewById(R.id.player_progress_bar);
 		queueBtn.setOnClickListener(root);
 		mainMenuBtn.setOnClickListener(root);
 		playPauseBtn.setOnClickListener(this);
 		prevCompBtn.setOnClickListener(this);
 		nextCompBtn.setOnClickListener(this);
 		playerChangedStatusEvent = new PlayerChangedStatusEvent();
 		Messaging.subscribe(Player.PlayerChangeEvent.class,
 				playerChangedStatusEvent);
 
 		return view;
 	}
 
 	public void changeStatus(int newStatus) {
 		status = newStatus;
 		String string = null;
 		switch (status) {
 		case PLAYING:
 			string = "pause";
 			break;
 		case PAUSED:
 			string = "play";
 			break;
 		case STOPPED:
 			string = "play";
 			break;
 		}
 		playPauseBtn.setText(string);
 	}
 
 	@Override
 	public void onClick(View view) {
 		int id = view.getId();
 		switch (id) {
 		case R.id.play_pause_btn:
 			switch (status) {
 			case PLAYING:
 				root.taskMessage.action = Action.Pause;
 				Messaging.fire(root.taskMessage);
 				break;
 			case PAUSED:
 				root.taskMessage.action = Action.Resume;
 				Messaging.fire(root.taskMessage);
 				break;
 			case STOPPED:
 				root.taskMessage.action = Action.Play;
 				Messaging.fire(root.taskMessage);
 				break;
 			}
 			break;
 		case R.id.next_comp_btn:
 			root.taskMessage.action = Action.Next;
 			Messaging.fire(root.taskMessage);
 			break;
 		case R.id.prev_comp_btn:
 			root.taskMessage.action = Action.Previous;
 			Messaging.fire(root.taskMessage);
 			break;
 		default:
 			break;
 		}
 	}
 
 	@Override
 	public void onDestroy() {
 		progressBar = null; // Allows Service to know is Activity running or not
 		Log.d(Constants.LOG_TAG, "PlayBar.onDestroy()");
 		Messaging.unSubscribe(Player.PlayerChangeEvent.class,
 				playerChangedStatusEvent);
 		super.onDestroy();
 	}
 }
