 /*
  * DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE Version 2, December 2004
  * 
  * Copyright (C) 2004 Sam Hocevar <sam@hocevar.net>
  * 
  * Everyone is permitted to copy and distribute verbatim or modified copies of
  * this license document, and changing it is allowed as long as the name is
  * changed.
  * 
  * DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE TERMS AND CONDITIONS FOR COPYING,
  * DISTRIBUTION AND MODIFICATION
  * 
  * 0. You just DO WHAT THE FUCK YOU WANT TO.
  */
 
 package com.theisleoffavalon.mcmanager_mobile.fragments;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import android.app.AlertDialog;
 import android.app.Fragment;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.theisleoffavalon.mcmanager_mobile.MainConsole;
 import com.theisleoffavalon.mcmanager_mobile.MinecraftCommand;
 import com.theisleoffavalon.mcmanager_mobile.R;
 import com.theisleoffavalon.mcmanager_mobile.ServerActivity;
 import com.theisleoffavalon.mcmanager_mobile.adapters.PlayerAdapter;
 import com.theisleoffavalon.mcmanager_mobile.async.AsyncStopServer;
 import com.theisleoffavalon.mcmanager_mobile.datatypes.Player;
 import com.theisleoffavalon.mcmanager_mobile.helpers.Convert;
 
 /**
  * Class representing the Info Fragment.
  * 
  * @author eberta
  * @modified 2/14/13
  */
 public class InfoFragment extends Fragment {
 
 	/**
 	 * Int containing the refresh rate for the fragment.
 	 */
 	private final int			REFRESH_RATE	= 3000;
 
 	/**
 	 * List of player objects that represent online players on the server.
 	 */
 	private List<Player>		playerList;
 
 	/**
 	 * Custom Adapter that handles how players are displayed.
 	 */
 	private PlayerAdapter		pa;
 
 	/**
 	 * Handle for the AsyncGetInfoTask so it can be called on.
 	 */
 	private AsyncGetInfoTask	async;
 
 	/**
 	 * Handler that refreshes the screen based on a REFRESH_RATE;
 	 */
 	private Handler				timer;
 
 	/**
 	 * Runnable that creates a thread in which a handle is placed for it to run.
 	 */
 	public Runnable				refresh			= new Runnable() {
 
 													@Override
 													public void run() {
 														InfoFragment.this.async = new AsyncGetInfoTask();
 														InfoFragment.this.async
 																.execute();
 														InfoFragment.this.timer
 																.postDelayed(
 																		InfoFragment.this.refresh,
 																		InfoFragment.this.REFRESH_RATE);
 													}
 
 												};
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.timer = new Handler();
 		this.refresh.run();
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup vg,
 			Bundle savedInstanceState) {
 		View view = inflater.inflate(R.layout.fragment_info, null, false);
 
 		TextView servername = (TextView) view
 				.findViewById(R.id.frag_info_server_name);
 
 		Button button = (Button) view.findViewById(R.id.frag_info_stop);
 		button.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				new AlertDialog.Builder(getActivity())
 						.setIcon(android.R.drawable.ic_dialog_alert)
 						.setTitle("Stop Server?")
 						.setMessage("Do you really want to stop the server?")
 						.setPositiveButton(R.string.yes,
 								new DialogInterface.OnClickListener() {
 
 									@Override
 									public void onClick(DialogInterface dialog,
 											int which) {
 										new AsyncStopServer()
 												.execute(getActivity());
 										Intent intent = new Intent(
 												getActivity().getBaseContext(),
 												MainConsole.class);
 										startActivity(intent);
 									}
 
 								}).setNegativeButton(R.string.no, null).show();
 			}
 		});
 
 		ListView playerListView = (ListView) view
 				.findViewById(R.id.player_list);
 		servername.setText(getActivity().getIntent().getExtras()
 				.getString("serverName"));
 
 		// if (this.playerList == null) {
 		// this.playerList = new ArrayList<Player>();
 		// this.playerList.add(new Player("Terminator", "155.92.13.12", null));
 		// this.playerList.add(new Player("The Ugly", "155.92.123.11", null));
 		// this.playerList.add(new Player("Postal", "10.12.14.12", null));
 		// this.playerList.add(new Player("Temporal", "1.1.1.1", null));
 		// this.playerList.add(new Player("Ooops", "0.0.0.0", null));
 		// }
 		this.playerList = new ArrayList<Player>();
 		this.pa = new PlayerAdapter(getActivity(), this.playerList);
 		playerListView.setAdapter(this.pa);
 
 		return view;
 	}
 
 	/**
 	 * Method called when the fragment is destroyed.
 	 */
 	@Override
 	public void onDestroy() {
 		this.async.cancel(true);
 		this.timer.removeCallbacks(this.refresh);
 		super.onDestroy();
 
 	}
 
 	/**
 	 * Parses the player list into a format that the list adapter can
 	 * understand.
 	 * 
 	 * @param stringList
 	 *            List of strings that represent the players on the server.
 	 * @return Returns a List of player objects for the display.
 	 */
 	private List<Player> parseList(List<String> stringList) {
 		List<Player> temp = new ArrayList<Player>();
 		for (String t : stringList) {
 			temp.add(new Player(t, null, null));
 		}
 
 		return temp;
 	}
 
 	/**
 	 * Refreshes the info on screen by recalling the AsyncGetInfoTask class.
 	 */
 	public void refresh() {
 		this.async = new AsyncGetInfoTask();
 		this.async.execute();
 	}
 
 	/**
	 * Method that creates the async task to kick a player.
 	 * 
 	 * @param name
 	 *            String containing the name of the person to kick.
 	 */
 	public void kickPlayer(String name) {
 		new AsyncKickPlayer().execute(name);
 	}
 
 	/**
 	 * Method that creates the async task to ban a player.
 	 * 
 	 * @param name
 	 *            String containing the name of the person to ban.
 	 */
 	public void banPlayer(String name) {
 		new AsyncBanPlayer().execute(name);
 	}
 
 	/**
 	 * Async Task that gets current server info
 	 * 
 	 * @author eberta
 	 * @modified 2/14/13
 	 */
 	public class AsyncGetInfoTask extends
 			AsyncTask<Void, Map<String, Object>, Void> {
 
 		@SuppressWarnings("unchecked")
 		@Override
 		protected Void doInBackground(Void... Void) {
 			Log.d("AsyncGetInfoTask", "doInBackground");
 			Map<String, Object> serverInfo = null;
 			try {
 				serverInfo = ((ServerActivity) getActivity()).getRc()
 						.getServerInfo();
 
 				publishProgress(serverInfo);
 			} catch (IOException e) {
 				// Toast.makeText(getActivity(),
 				// "Connection error: " + e.getLocalizedMessage(),
 				// Toast.LENGTH_LONG).show();
 				Log.e("AsyncGetInfoTask", "IOException", e);
 			}
 			return null;
 		}
 
 		@SuppressWarnings("unchecked")
 		@Override
 		protected void onProgressUpdate(Map<String, Object>... serverInfo) {
 			((TextView) getView().findViewById(R.id.frag_info_uptime))
 					.setText(Convert.formatTime(((Long) serverInfo[0]
 							.get("uptime"))));
 
 			((TextView) getView().findViewById(R.id.frag_info_memoryusage))
 					.setText(Convert.formatMemory((Long) serverInfo[0]
 							.get("usedMemory")));
 			((TextView) getView().findViewById(R.id.frag_info_maxmemory))
 					.setText(Convert.formatMemory((Long) serverInfo[0]
 							.get("maxMemory")));
 			InfoFragment.this.playerList
 					.removeAll(InfoFragment.this.playerList);
 			InfoFragment.this.playerList
 					.addAll(parseList((List<String>) serverInfo[0]
 							.get("players")));
 			Log.d("Progress Update", serverInfo.toString());
 			InfoFragment.this.pa.notifyDataSetInvalidated();
 			// Toast.makeText(getBaseContext(), result,
 			// Toast.LENGTH_LONG).show();
 
 		}
 	}
 
 	/**
 	 * Async Task that is used to kick a player from the server.
 	 * 
 	 * @author eberta
 	 * @modified 2/14/13
 	 */
 	public class AsyncKickPlayer extends AsyncTask<String, Void, Void> {
 
 		@Override
 		protected Void doInBackground(String... player) {
 			try {
 				Map<String, MinecraftCommand> list = ((ServerActivity) getActivity())
 						.getRc().getAllMinecraftCommands();
 				Map<String, Object> args = new HashMap<String, Object>();
 				args.put("args", player[0]);
 				((ServerActivity) getActivity()).getRc().executeCommand(
 						list.get("kick"), args);
 			} catch (IOException e) {
 				// Toast.makeText(getActivity(),
 				// "Connection error: " + e.getLocalizedMessage(),
 				// Toast.LENGTH_LONG).show();
 				Log.e("AsyncKickPlayer", "IOException", e);
 			}
 			return null;
 		}
 
 	}
 
 	/**
 	 * Async Task that bans a player from the server.
 	 * 
 	 * @author eberta
 	 * @modified 2/14/13
 	 */
 	public class AsyncBanPlayer extends AsyncTask<String, Void, Void> {
 
 		@Override
 		protected Void doInBackground(String... player) {
 			try {
 				Map<String, MinecraftCommand> list = ((ServerActivity) getActivity())
 						.getRc().getAllMinecraftCommands();
 				Map<String, Object> args = new HashMap<String, Object>();
 				args.put("args", player[0]);
 				((ServerActivity) getActivity()).getRc().executeCommand(
 						list.get("ban"), args);
 			} catch (IOException e) {
 				// Toast.makeText(getActivity(),
 				// "Connection error: " + e.getLocalizedMessage(),
 				// Toast.LENGTH_LONG).show();
 				Log.e("AsyncBanPlayer", "IOException", e);
 			}
 			return null;
 		}
 
 	}
 }
