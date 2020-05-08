 package com.cesarandres.ps2link;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.graphics.Color;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.ImageButton;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.ToggleButton;
 
 import com.android.volley.RequestQueue;
 import com.android.volley.Response;
 import com.android.volley.Response.ErrorListener;
 import com.android.volley.Response.Listener;
 import com.android.volley.VolleyError;
 import com.android.volley.toolbox.Volley;
 import com.cesarandres.ps2link.base.BaseFragment;
 import com.cesarandres.ps2link.module.ObjectDataSource;
 import com.cesarandres.ps2link.soe.SOECensus;
 import com.cesarandres.ps2link.soe.SOECensus.Game;
 import com.cesarandres.ps2link.soe.SOECensus.Verb;
 import com.cesarandres.ps2link.soe.content.Member;
 import com.cesarandres.ps2link.soe.content.Outfit;
 import com.cesarandres.ps2link.soe.content.response.Outfit_member_response;
 import com.cesarandres.ps2link.soe.util.Collections.PS2Collection;
 import com.cesarandres.ps2link.soe.util.QueryString;
 import com.cesarandres.ps2link.soe.util.QueryString.QueryCommand;
 import com.cesarandres.ps2link.soe.util.QueryString.SearchModifier;
 import com.cesarandres.ps2link.soe.volley.GsonRequest;
 
 /**
  * Created by cesar on 6/16/13.
  */
 public class FragmentMemberList extends BaseFragment {
 
 	private boolean isCached;
 	private boolean shownOffline = false;;
 	private ObjectDataSource data;
 	private int outfitSize;
 	private String outfitId;
 	private String outfitName;
 	private ArrayList<AsyncTask> taskList;
	private RequestQueue volley;
 	private FragmentMemberList tag = this;
 	public static final int SUCCESS = 0;
 	public static final int FAILED = 1;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setHasOptionsMenu(true);
 		taskList = new ArrayList<AsyncTask>();
 		data = new ObjectDataSource(getActivity());
		volley = Volley.newRequestQueue(getActivity());
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View root = inflater.inflate(R.layout.fragment_member_list, container,
 				false);
 
 		ListView listRoot = (ListView) root
 				.findViewById(R.id.listViewMemberList);
 		listRoot.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> myAdapter, View myView,
 					int myItemInt, long mylng) {
 				Intent intent = new Intent();
 				intent.setClass(getActivity(), ActivityProfile.class);
 				intent.putExtra("profileId", ((Member) myAdapter
 						.getItemAtPosition(myItemInt)).getCharacter_id());
 				startActivity(intent);
 			}
 		});
 
 		ImageButton updateButton = (ImageButton) root
 				.findViewById(R.id.buttonFragmentUpdate);
 
 		ToggleButton viewOffline = (ToggleButton) root
 				.findViewById(R.id.toggleShowOffline);
 		viewOffline.setVisibility(View.VISIBLE);
 
 		viewOffline
 				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
 					@Override
 					public void onCheckedChanged(CompoundButton buttonView,
 							boolean isChecked) {
 						shownOffline = isChecked;
 						updateContent();
 					}
 				});
 
 		updateButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				downloadOutfitMembers(outfitId);
 			}
 		});
 
 		ToggleButton append = ((ToggleButton) root
 				.findViewById(R.id.buttonFragmentAppend));
 		append.setVisibility(View.VISIBLE);
 		append.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 			public void onCheckedChanged(CompoundButton buttonView,
 					boolean isChecked) {
 				if (isChecked) {
 					CacheOutfit task = new CacheOutfit();
 					taskList.add(task);
 					task.execute(outfitId);
 				} else {
 					UnCacheOutfit task = new UnCacheOutfit();
 					taskList.add(task);
 					task.execute(outfitId);
 				}
 			}
 		});
 
 		root.findViewById(R.id.buttonFragmentUpdate)
 				.setVisibility(View.VISIBLE);
 		root.findViewById(R.id.toggleShowOffline).setVisibility(View.VISIBLE);
 		root.findViewById(R.id.buttonFragmentStar).setVisibility(View.VISIBLE);
 
 		((Button) root.findViewById(R.id.buttonFragmentTitle)).setText("");
 
 		return root;
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		data.open();
 		if (savedInstanceState == null) {
 			UpdateOutfitFromTable task = new UpdateOutfitFromTable();
 			taskList.add(task);
 			task.execute(getActivity().getIntent().getExtras()
 					.getString("outfit_id"));
 		} else {
 			this.outfitSize = savedInstanceState.getInt("outfitSize", 0);
 			this.outfitId = savedInstanceState.getString("outfitId");
 			this.outfitName = savedInstanceState.getString("outfitName");
 			this.shownOffline = savedInstanceState.getBoolean("showOffline");
 		}
 
 		((Button) getActivity().findViewById(R.id.buttonFragmentTitle))
 				.setText(outfitName);
 
 		((ToggleButton) getActivity().findViewById(R.id.buttonFragmentStar))
 				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 					public void onCheckedChanged(CompoundButton buttonView,
 							boolean isChecked) {
 						if (outfitId != null && outfitName != null) {
 							SharedPreferences settings = getActivity()
 									.getSharedPreferences("PREFERENCES", 0);
 							SharedPreferences.Editor editor = settings.edit();
 							if (isChecked) {
 								editor.putString("preferedOutfit", outfitId);
 								editor.putString("preferedOutfitName",
 										outfitName);
 							} else {
 								editor.putString("preferedOutfit", "");
 								editor.putString("preferedOutfitName", "");
 							}
 							editor.commit();
 						}
 					}
 				});
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		if (outfitId != null) {
 			updateContent();
 		}
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle savedInstanceState) {
 		super.onSaveInstanceState(savedInstanceState);
 		savedInstanceState.putInt("outfitSize", outfitSize);
 		savedInstanceState.putString("outfitId", outfitId);
 		savedInstanceState.putString("outfitName", outfitName);
 		savedInstanceState.putBoolean("showOffline", shownOffline);
 	}
 
 	@Override
 	public void onDestroyView() {
 		for (AsyncTask task : taskList) {
 			task.cancel(true);
 		}
 		data.close();
 		super.onDestroy();
 	}
 
 	private void downloadOutfitMembers(String outfit_id) {
 
 		setUpdateButton(false);
 		setAppendButtonVisibility(false);
 		URL url;
 		try {
 			url = SOECensus
 					.generateGameDataRequest(
 							Verb.GET,
 							Game.PS2,
 							PS2Collection.OUTFIT,
 							"",
 							QueryString
 									.generateQeuryString()
 									.AddComparison("id", SearchModifier.EQUALS,
 											outfit_id)
 									.AddCommand(QueryCommand.RESOLVE,
 											"member_online_status,member,member_character(name,type.faction)"));
 
 			Listener<Outfit_member_response> success = new Response.Listener<Outfit_member_response>() {
 				@Override
 				public void onResponse(Outfit_member_response response) {
 					UpdateMembers task = new UpdateMembers();
 					taskList.add(task);
 					task.execute(response.getOutfit_list().get(0).getMembers());
 				}
 			};
 
 			ErrorListener error = new Response.ErrorListener() {
 				@Override
 				public void onErrorResponse(VolleyError error) {
 					error.equals(new Object());
 					setUpdateButton(true);
 				}
 			};
 
 			GsonRequest<Outfit_member_response> gsonOject = new GsonRequest<Outfit_member_response>(
 					url.toString(), Outfit_member_response.class, null,
 					success, error);
 			gsonOject.setTag(tag);
			volley.add(gsonOject);
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	private static class MemberItemAdapter extends BaseAdapter {
 		private LayoutInflater mInflater;
 		private Cursor cursor;
 		private int size;
 
 		/*
 		 * private int size; private int cacheFirst; private int cacheSize;
 		 * private ArrayList<Member> cacheA; private ArrayList<Member> cacheB;
 		 */
 
 		public MemberItemAdapter(Context context, int size, String outfitId,
 				ObjectDataSource data, boolean isCache, boolean showOffline) {
 			// Cache the LayoutInflate to avoid asking for a new one each time.
 			this.mInflater = LayoutInflater.from(context);
 			this.size = data.countAllMembers(outfitId, showOffline);
 			this.cursor = data
 					.getMembersCursor(outfitId, !isCache, showOffline);
 			/*
 			 * this.size = size; this.cacheFirst = 0; this.cacheSize = 2 *
 			 * this.size; this.cacheA = new ArrayList<Member>(size); this.cacheB
 			 * = new ArrayList<Member>(size);
 			 */
 		}
 
 		@Override
 		public int getCount() {
 			return this.size;
 		}
 
 		@Override
 		public Member getItem(int position) {
 			return ObjectDataSource.cursorToMember(ObjectDataSource
 					.cursorToPosition(cursor, position));
 		}
 
 		@Override
 		public long getItemId(int position) {
 			return position;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			ViewHolder holder;
 
 			if (convertView == null) {
 				convertView = mInflater
 						.inflate(R.layout.member_item_list, null);
 
 				holder = new ViewHolder();
 				holder.memberName = (TextView) convertView
 						.findViewById(R.id.textViewMemberListName);
 				holder.memberStatus = (TextView) convertView
 						.findViewById(R.id.textViewMemberListStatus);
 				holder.memberRank = (TextView) convertView
 						.findViewById(R.id.textViewMemberListRank);
 				convertView.setTag(holder);
 			} else {
 				holder = (ViewHolder) convertView.getTag();
 			}
 
 			holder.memberName.setText(getItem(position).getName().getFirst());
 			holder.memberRank.setText(getItem(position).getRank());
 
 			if (getItem(position).getOnline_status().equals("0")) {
 				holder.memberStatus.setText("Offline");
 				holder.memberStatus.setTextColor(Color.RED);
 			} else {
 				holder.memberStatus.setText("Online");
 				holder.memberStatus.setTextColor(Color.GREEN);
 			}
 
 			return convertView;
 		}
 
 		static class ViewHolder {
 			TextView memberName;
 			TextView memberStatus;
 			TextView memberRank;
 		}
 	}
 
 	private void setUpdateButton(boolean enabled) {
 		getActivity().findViewById(R.id.buttonFragmentUpdate).setEnabled(
 				enabled);
 		getActivity().findViewById(R.id.toggleShowOffline).setEnabled(enabled);
 		getActivity().findViewById(R.id.buttonFragmentStar).setEnabled(enabled);
 		getActivity().findViewById(R.id.buttonFragmentAppend).setEnabled(
 				enabled);
 	}
 
 	private void setAppendButtonVisibility(boolean visible) {
 		ToggleButton star = (ToggleButton) getActivity().findViewById(
 				R.id.buttonFragmentStar);
 		SharedPreferences settings = getActivity().getSharedPreferences(
 				"PREFERENCES", 0);
 		String preferedOutfitId = settings.getString("preferedOutfit", "");
 		if (preferedOutfitId.equals(outfitId)) {
 			star.setChecked(true);
 		} else {
 			star.setChecked(false);
 		}
 
 		getActivity().findViewById(R.id.buttonFragmentAppend).setEnabled(
 				visible);
 		star.setEnabled(visible);
 	}
 
 	private void updateContent() {
 		if (this.outfitId != null) {
 			ListView listRoot = (ListView) getActivity().findViewById(
 					R.id.listViewMemberList);
 
 			listRoot.setAdapter(new MemberItemAdapter(getActivity(),
 					outfitSize, outfitId, data, isCached, shownOffline));
 		}
 	}
 
 	private class UpdateOutfitFromTable extends
 			AsyncTask<String, Integer, Outfit> {
 
 		@Override
 		protected void onPreExecute() {
 			setAppendButtonVisibility(false);
 			setUpdateButton(false);
 		}
 
 		@Override
 		protected Outfit doInBackground(String... args) {
 			Log.d("UpdateOutfitFromTable", "STARTING");
 			Outfit outfit = null;
 			try {
 				outfit = data.getOutfit(args[0]);
 				isCached = outfit.isCached();
 			} catch (Exception e) {
 				Log.d("UpdateOutfitFromTable", "ENDED BY EXCEPTION");
 			}
 			Log.d("UpdateOutfitFromTable", "END");
 			return outfit;
 		}
 
 		@Override
 		protected void onPostExecute(Outfit result) {
 			if (!this.isCancelled()) {
 				if (result == null) {
 					setUpdateButton(false);
 				} else {
 					outfitId = result.getId();
 					outfitName = result.getName();
 					outfitSize = result.getMember_count();
 					((Button) getActivity().findViewById(
 							R.id.buttonFragmentTitle)).setText(outfitName);
 					setUpdateButton(false);
 					updateContent();
 					downloadOutfitMembers(outfitId);
 				}
 			}
 			taskList.remove(this);
 
 		}
 	}
 
 	private class UpdateMembers extends
 			AsyncTask<ArrayList<Member>, Integer, Integer> {
 
 		@Override
 		protected void onPreExecute() {
 			setAppendButtonVisibility(false);
 			setUpdateButton(false);
 		}
 
 		@Override
 		protected Integer doInBackground(ArrayList<Member>... members) {
 			Log.d("UpdateMembers", "STARTING");
 			ArrayList<Member> newMembers = members[0];
 			try {
 				for (Member member : newMembers) {
 					if (data.getMember(member.getCharacter_id(), !isCached) == null) {
 						data.insertMember(member, outfitId, !isCached);
 					} else {
 						data.updateMember(member, !isCached);
 					}
 				}
 			} catch (Exception e) {
 				Log.d("UpdateMembers", "ENDED BY EXCEPTION");
 			}
 			Log.d("UpdateMembers", "END");
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Integer result) {
 			if (!this.isCancelled()) {
 				setUpdateButton(true);
 				updateContent();
 			}
 			taskList.remove(this);
 
 		}
 	}
 
 	private class CacheOutfit extends AsyncTask<String, Integer, Integer> {
 
 		@Override
 		protected void onPreExecute() {
 			setAppendButtonVisibility(false);
 			setUpdateButton(false);
			volley.cancelAll(tag);
 		}
 
 		@Override
 		protected Integer doInBackground(String... args) {
 			Log.d("CacheOutfit", "STARTING");
 			Outfit outfit = data.getOutfit(args[0]);
 			try {
 				data.updateOutfit(outfit, false);
 				isCached = true;
 			} catch (Exception e) {
 				Log.d("CacheOutfit", "ENDED BY EXCEPTION");
 				return FAILED;
 			}
 			Log.d("CacheOutfit", "END");
 			return SUCCESS;
 		}
 
 		@Override
 		protected void onPostExecute(Integer result) {
 			if (!this.isCancelled()) {
 				if (isCached) {
 					updateContent();
 				}
 				setUpdateButton(true);
 			}
 			taskList.remove(this);
 
 		}
 	}
 
 	private class UnCacheOutfit extends AsyncTask<String, Integer, Integer> {
 
 		@Override
 		protected void onPreExecute() {
 			setAppendButtonVisibility(false);
 			setUpdateButton(false);
			volley.cancelAll(tag);
 		}
 
 		@Override
 		protected Integer doInBackground(String... args) {
 			Log.d("UnCacheOutfit", "STARTING");
 			try {
 				Outfit outfit = data.getOutfit(args[0]);
 				data.updateOutfit(outfit, true);
 				isCached = false;
 			} catch (Exception e) {
 				Log.d("UnCacheOutfit", "ENDED BY EXCEPTION");
 				return FAILED;
 			}
 			Log.d("UnCacheOutfit", "End");
 			return SUCCESS;
 		}
 
 		@Override
 		protected void onPostExecute(Integer result) {
 			if (!this.isCancelled()) {
 				if (!isCached) {
 					updateContent();
 				}
 				setUpdateButton(true);
 
 			}
 			taskList.remove(this);
 		}
 	}
 
 }
