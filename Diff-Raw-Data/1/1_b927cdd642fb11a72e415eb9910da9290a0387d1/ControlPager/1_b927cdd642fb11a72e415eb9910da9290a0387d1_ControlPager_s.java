 package com.zigvine.android.widget;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 import me.maxwin.view.XListView;
 import me.maxwin.view.XListView.IXListViewListener;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.zigvine.android.anim.AnimUtils;
 import com.zigvine.android.anim.AnimUtils.CustomAnimation;
 import com.zigvine.android.http.Request;
 import com.zigvine.android.http.Request.Resp;
 import com.zigvine.android.http.Request.ResponseListener;
 import com.zigvine.android.utils.Quota;
 import com.zigvine.android.utils.Utils;
 import com.zigvine.zagriculture.R;
 import com.zigvine.zagriculture.UIActivity;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.animation.Animation;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class ControlPager extends Pager
 		implements ResponseListener, IXListViewListener, OnItemClickListener {
 
 	XListView list;
 	TextView refresh, loader;
 	Request request;
 	int requestId;
 	long currentGroup;
 	MonitorAdapter adapter;
 	Map<Long, GroupArray> cachedData = new ConcurrentHashMap<Long, GroupArray>();
 	Runnable fadeOutRefresh = new Runnable() {
 		@Override
 		public void run() {
 			if (refresh != null && refresh.getVisibility() == View.VISIBLE) {
 				Animation anim = AnimUtils.FadeOut.loadAnimation(mContext, 300);
 				anim.setAnimationListener(AnimUtils.loadEndListener(refresh, View.GONE));
 				anim.setStartOffset(Refreshed_Disappear_Delay_Ms);
 				refresh.startAnimation(anim);
 			}
 		}
 	};
 	
 	final static long Loading_Disappear_Delay_Ms = 500l;
 	final static long Refreshed_Disappear_Delay_Ms = 1000l;
 	
 	public static class GroupData {
 		public JSONObject json;
 		public Date time;
 		public boolean isEnabled;
 		public boolean isShrinked;
 		
 		public GroupData(JSONObject d, Date t) {
 			json = d;
 			time = t;
 			isEnabled = true;
 			isShrinked = true;
 		}
 	}
 	
 	public static class GroupArray extends ArrayList<GroupData> {
 		public static final long serialVersionUID = 1L;
 		public Date time;
 		public boolean success;
 		public Object obj;
 		public int statusCode;
 		public GroupArray(Resp resp, String dataNameInJson) {
 			time = resp.time;
 			success = resp.success;
 			obj = resp.obj;
 			statusCode = resp.statusCode;
 			JSONObject json = resp.json;
 			if (json != null) {
 				try {
 					JSONArray arr = json.getJSONArray(dataNameInJson);
 					for (int i = 0; i < arr.length(); i++) {
 						JSONObject obj = arr.getJSONObject(i);
 						GroupData g = new GroupData(obj, time);
 						add(g);
 					}
 				} catch (JSONException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 	
 	public ControlPager(UIActivity<?> context) {
 		super(context);
 		currentGroup = -1;
 	}
 
 	@Override
 	public void onCreate() {
 		setContentView(R.layout.pager_monitor);
 		list = (XListView) findViewById(R.id.monitor_list_view);
 		list.setDivider(null);
 		list.setDividerHeight(0);
 		list.setEmptyView(findViewById(R.id.monitor_empty));
 		list.setPullRefreshEnable(true);
 		list.setPullLoadEnable(false);
 		list.setOnItemClickListener(this);
 		loader = (TextView) findViewById(R.id.monitor_loading);
 		loader.setVisibility(View.GONE);
 		adapter = new MonitorAdapter(mContext);
 		list.setAdapter(adapter);
 		refresh = (TextView) findViewById(R.id.monitor_refresh);
 		refresh.setVisibility(View.GONE);
 		list.setXListViewListener(this);
 		findViewById(R.id.monitor_empty).setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				refreshCurrentGroupNow();
 			}
 		});
 	}
 	
 	@Override
 	public void refreshData(long groupid) {
 		refreshData(groupid, false);
 	}
 	
 	@Override
 	public void refreshDataWithoutFetch(long groupid) {
 		currentGroup = groupid;
 		adapter.notifyDataSetChanged();
 	}
 	
 	@Override
 	public void refreshCurrentGroupNow() {
 		if (currentGroup >= 0) {
 			refreshData(currentGroup, true);
 		}
 	}
 	
 	@Override
 	public void notifyLastRefreshTime() {
 		if (currentGroup >= 0) {
 			refreshData(currentGroup, false);
 		}
 	}
 	
 	@Override
 	public void setEmptyViewText(String text) {
 		((TextView) findViewById(R.id.monitor_empty_word)).setText(text);
 	}
 
 	private void refreshData(long groupid, boolean force) {
 		if (currentGroup != groupid) {
 			//currentGroup = groupid;
 			adapter.notifyDataSetInvalidated();
 		}
 		GroupArray resp = cachedData.get(groupid);
 		if (resp != null && !force) {
 			// use already cached data
 			dropOutLoader();
 			currentGroup = groupid;
 			ignoreLastRequest();
 			String deltaTime = Utils.getDeltaTimeString(resp.time);
 			refresh.setText(deltaTime + "前更新");
 			refresh.clearAnimation();
 			Animation anim = AnimUtils.FadeIn.loadAnimation(mContext, 300);
 			anim.setAnimationListener(AnimUtils.loadStartListener(refresh, View.VISIBLE, fadeOutRefresh));
 			refresh.startAnimation(anim);
 			adapter.notifyDataSetChanged();
 		} else if (request == null || !request.isOnFetching() || currentGroup != groupid) {
 			currentGroup = groupid;
 			if (loader.getVisibility() == View.GONE) {
 				loader.setVisibility(View.VISIBLE);
 				AnimUtils.DropIn.startAnimation(loader, 300);
 			}
 			ignoreLastRequest();
 			request = new Request(Request.GetControl, true);
 			request.setParam("groupID", String.valueOf(groupid));
 			request.asyncRequest(this, requestId);
 			findViewById(R.id.monitor_empty_word).setVisibility(View.GONE);
 		} else {
 			refreshDataWithoutFetch(groupid);
 		}
 	}
 	
 	private void ignoreLastRequest() {
 		requestId++;
 		if (request != null) {
 			request.shutdown();
 		}
 	}
 	
 	@Override
 	public void onResp(int id, Resp resp, Object...obj) {
 		if (id != requestId) return;
 		if (resp.success) {
 			dropOutLoader();
 			if (cachedData.get(currentGroup) == null) {
 				list.startLayoutAnimation();
 			}
 			
 			cachedData.put(currentGroup, new GroupArray(resp, "data"));
 			refresh.setText("已更新");
 			list.setRefreshTime(Utils.DATETIME.format(resp.time));
 			adapter.notifyDataSetChanged();
 			mContext.onRefresh(this);
 		} else {
 			dropOutLoader();
 			// fail
 		}
 	}
 
 	@Override
 	public void onErr(int id, String err, int httpCode, Object...obj) {
 		if (id != requestId) return;
 		dropOutLoader();
 		mContext.UI.toast(err);
 }
 	
 	private void dropOutLoader() {
 		if (loader.getVisibility() == View.VISIBLE) {
 			Animation anim = AnimUtils.DropOut.loadAnimation(mContext, 300);
 			anim.setAnimationListener(AnimUtils.loadEndListener(loader, View.GONE));
 			anim.setStartOffset(Loading_Disappear_Delay_Ms);
 			loader.startAnimation(anim);
 		}
 		findViewById(R.id.monitor_empty_word).setVisibility(View.VISIBLE);
 		list.stopRefresh();
 	}
 	
 	public class MonitorAdapter extends BaseAdapter implements View.OnClickListener, ResponseListener {
 		
 		private UIActivity<?> activity;
 
 		public MonitorAdapter(UIActivity<?> context) {
 			activity = context;
 		}
 		
 		private GroupArray getData() {
 			if (cachedData != null) {
 				GroupArray resp = cachedData.get(currentGroup);
 				return resp;
 			}
 			return null;
 		}
 		
 		public boolean isItemEnabled(int position) {
 			return isItemEnabled(currentGroup, position);
 		}
 		
 		public boolean isItemEnabled(long groupid, int position) {
 			if (cachedData != null) {
 				GroupArray resp = cachedData.get(groupid);
 				if (resp != null && position < resp.size()) {
 					GroupData g = resp.get(position);
 					if (g != null) {
 						return g.isEnabled;
 					}
 				}
 			}
 			return false;
 		}
 		
 		public void setItemEnabled(int position, boolean enable) {
 			setItemEnabled(currentGroup, position, enable);
 		}
 		
 		public void setItemEnabled(long groupid, int position, boolean enable) {
 			if (cachedData != null) {
 				GroupArray resp = cachedData.get(groupid);
 				if (resp != null && position < resp.size()) {
 					GroupData g = resp.get(position);
 					if (g != null) {
 						//log("set enalbe(" + g.isEnabled + ")=" + enable + ", for " + g.toString());
 						g.isEnabled = enable;
 						if (groupid == currentGroup) {
 							notifyDataSetChanged();
 						}
 					}
 				}
 			}
 		}
 		
 		@Override
 		public int getCount() {
 			GroupArray data = getData();
 			if (data != null) {
 				return data.size();
 			}
 			return 0;
 		}
 
 		@Override
 		public Object getItem(int position) {
 			try {
 				GroupArray data = getData();
 				if (data != null) {
 					return data.get(position);
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			return null;
 		}
 
 		@Override
 		public long getItemId(int position) {
 			return position;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			if (convertView == null) {
 				convertView =  View.inflate(activity, R.layout.control_item, null);
 			}
 			TextView qname = (TextView) convertView.findViewById(R.id.quota_name);
 			TextView qvalue = (TextView) convertView.findViewById(R.id.quota_value);
 			TextView time = (TextView) convertView.findViewById(R.id.monitor_time);
 			ImageView qid = (ImageView) convertView.findViewById(R.id.quotaId);
 			View progressbar = convertView.findViewById(R.id.item_progressbar);
 			boolean isEnabled = isItemEnabled(position);
 			if (isEnabled) {
 				progressbar.setVisibility(View.GONE);
 				convertView.setClickable(false);
 			} else {
 				progressbar.setVisibility(View.VISIBLE);
 				convertView.setClickable(true);
 			}
 			if (position == 0) {
 				convertView.setBackgroundResource(R.drawable.pageritem_bg_top);
 			} else {
 				convertView.setBackgroundResource(R.drawable.pageritem_bg);
 			}
 			GroupData gd = (GroupData) getItem(position);
 			try {
 				JSONObject json = gd.json;
 				if (json != null) {
 					String s = json.getString("deviceName");
 					JSONArray arr = json.getJSONArray("quota");
 					//s = arr.getString(0);
 					qname.setText(s);
 					s =  arr.getString(0) + ":" + json.getString("stateDesc");
 					qvalue.setText(s);
 					s = json.getString("date");
 					time.setText(s);
 					int id = json.getInt("quotaID");
 					qid.setImageResource(Quota.ICONS[id]);
 					int portid = json.getInt("num");
 					final ViewGroup ch = (ViewGroup) convertView.findViewById(R.id.control_items);
 					ch.removeAllViews();
 					arr = json.getJSONArray("cmdList");
 					for (int i = 0; i < arr.length(); i++) {
 						JSONArray subarr = arr.getJSONArray(i);
 						String name = subarr.getString(1);
 						int status = subarr.getInt(0);
 						TextView tv = new TextView(mContext);
 						
 						if (i == 0) {
 							//
 						} else {
 							View v = new View(mContext);
 							v.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));
 							v.setBackgroundColor(0xffc3c3c3);
 							ch.addView(v);
 						}
 						tv.setPadding(40, 10, 40, 10);
 						tv.setTextSize(18);
 						tv.setTextColor(0xff5c5c5c);
 						tv.setText(name);
 						tv.setBackgroundResource(R.drawable.title_btn);
 						tv.setOnClickListener(this);
 						tv.setTag(new int[] {position, status, portid});
 						ch.addView(tv);
 					}
 					
 				}
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 			
 			final ViewGroup vg = (ViewGroup) convertView.findViewById(R.id.more_control);
 			final ViewGroup ch = (ViewGroup) convertView.findViewById(R.id.control_items);
 			final LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) (vg.getLayoutParams());
 			if (convertView.getTag() != null) {
 				int lastPos = (Integer) convertView.getTag();
 				if (lastPos != position) {
 					// get from recycler and use in other position
 					// need to stop the animation
 					if (vg.getTag() != null) {
 						CustomAnimation anim = (CustomAnimation) vg.getTag();
 						anim.cancel();
 						vg.setTag(null);
 					}
 				}
 			}
 			
 			if (gd.isShrinked) {
 				lp.topMargin = 0;
 				lp.bottomMargin = 0;
 				lp.height = 0;
 			} else {
 				final int margin = Utils.dp2px(mContext, 10);
 				lp.topMargin = margin;
 				lp.bottomMargin = margin;
 				ch.measure(0, 0);
 				lp.height = ch.getMeasuredHeight();
 			}
 			vg.setLayoutParams(lp);
 			convertView.setTag(position);
 			return convertView;
 		}
 
 		@Override
 		public void onClick(View v) {
 			//log(v.getTag().toString());
 			final TextView tv = (TextView) v;
 			int[] data = (int[]) tv.getTag();
 			final int position = data[0];
 			final int state = data[1];
 			final int portid = data[2];
 			final long groupid = currentGroup;
 			if (!isItemEnabled(position)) return; // cannot operate on disabled item
 			final Object obj = getItem(position);
 			if (obj == null) return;
 			String dev = "";
 			final JSONObject json = ((GroupData) obj).json;
 			if (json != null) {
 				try {
 					dev = json.getString("deviceID");
 				} catch (JSONException e) {
 					e.printStackTrace();
 					return;
 				}
 			} else {
 				return;
 			}
 			final String deviceID = dev;
 			new AlertDialog.Builder(mContext)
 			.setTitle("发送控制指令")
 			.setIcon(R.drawable.alarm_icon)
 			.setMessage("确定对该设备进行【" + tv.getText() + "】操作？")
 			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					Request request = new Request(Request.SendCommand);  // post method
 					request.setSoTimeout(30000); // a must for that
 					request.setParam("deviceID", deviceID);
 					request.setParam("state", String.valueOf(state));
 					request.setParam("portid", String.valueOf(portid));
 					request.asyncRequest(MonitorAdapter.this, position, json.hashCode(), groupid);
 					// TODO ..... disable position
 					setItemEnabled(groupid, position, false);
 				}
 			})
 			.setNegativeButton(android.R.string.cancel, null).show();
 		}
 
 		@Override
 		public void onResp(int id, Resp resp, Object... obj) {
 			int hash = (Integer) obj[0];
 			long groupid = (Long) obj[1];
 			Object o = null;
 			String devID = "";
 			int lastState = -1;
 			int portid = -1;
 			int refreshCount = 0;
 			GroupData g = null;
 			if (obj.length > 2) {
 				refreshCount = (Integer) obj[2];
 			}
 			
 			if (cachedData != null) {
 				GroupArray r = cachedData.get(groupid);
 				if (r != null) {
 					try {
 						g = r.get(id);
 						o = g.json;
 						devID = g.json.getString("deviceID");
 						lastState = g.json.getInt("state");
 						portid = g.json.getInt("num");
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 				}
 			}
 			if (o != null && o.hashCode() == hash) {
 				if (resp.success) {
 					log("success send, count=" + refreshCount);
 					if (refreshCount > 0) {
 						GroupArray ga = new GroupArray(resp, "data");
 						GroupData newG = ga.get(0);
 						if (newG != null) {
 							g.json = newG.json;
 							g.time = new Date();
 							hash = g.json.hashCode();
 							try {
 								int newState = newG.json.getInt("state");
 								if (newState != lastState) {
 									setItemEnabled(groupid, id, true);
 									return;
 								}
 							} catch (JSONException e) {
 								e.printStackTrace();
 							}
 						}
 					}
 					if (refreshCount < 3) {
 						final Request request = new Request(Request.GetControl, true);
 						request.setParam("devID", devID);
 						request.setParam("GroupID", String.valueOf(groupid));
 						request.setParam("portid", String.valueOf(portid));
						request.setDebug(true);
 						final int R_id = id;
 						final long R_groupid = groupid;
 						final int R_hash = hash;
 						final int R_count = refreshCount + 1;
 						getHandler().postDelayed(new Runnable() {
 							public void run() {
 								request.asyncRequest(MonitorAdapter.this, R_id, R_hash, R_groupid, R_count);
 							}
 						}, 2000);
 						return; // most important, continue lock this device
 					}
 				} else {
 					mContext.UI.toast("命令发送失败！状态：" + resp.statusCode);
 				}
 				setItemEnabled(groupid, id, true);
 			}
 		}
 
 		@Override
 		public void onErr(int id, String err, int httpCode, Object... obj) {
 			int hash = (Integer) obj[0];
 			long groupid = (Long) obj[1];
 			Object o = null;
 			if (cachedData != null) {
 				GroupArray r = cachedData.get(groupid);
 				if (r != null) {
 					try {
 						GroupData g = r.get(id);
 						o = g.json;
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 				}
 			}
 			if (o != null && o.hashCode() == hash) {
 				mContext.UI.toast(err);
 				setItemEnabled(groupid, id, true);
 			}
 		}
 		
 	}
 
 	@Override
 	public void onRefresh() {
 		refreshCurrentGroupNow();
 	}
 
 	@Override
 	public void onLoadMore() {
 		
 	}
 	
 	@Override
 	public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
 		pos = (int) id; // very important
 		final ViewGroup vg = (ViewGroup) view.findViewById(R.id.more_control);
 		final ViewGroup ch = (ViewGroup) view.findViewById(R.id.control_items);
 		
 		final LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) (vg.getLayoutParams());
 		final int margin = Utils.dp2px(mContext, 10);
 		AnimUtils.CustomAnimation.Callback Call_back = new AnimUtils.CustomAnimation.Callback() {
 			@Override
 			public void process(int value) {
 				//log(value + "");
 				if (value < 2 * margin) {
 					lp.setMargins(0, value/2, 0, value/2);
 					lp.height = 0;
 				} else {
 					lp.setMargins(0, margin, 0, margin);
 					lp.height = value - margin * 2;
 				}
 				vg.setLayoutParams(lp);
 				vg.requestLayout();
 			}
 			@Override
 			public void onAnimationEnd() {}
 		};
 		CustomAnimation mCustomAnimation = null;
 		if (vg.getTag() != null) {
 			mCustomAnimation = (CustomAnimation) vg.getTag();
 		}
 		if (mCustomAnimation != null) {
 			mCustomAnimation.cancel();
 		}
 		
 		ch.measure(0, 0);
 		int trueHeight = ch.getMeasuredHeight() + margin * 2;
 		int currentHeight = lp.height + lp.topMargin + lp.bottomMargin;
 		GroupData gd = (GroupData) adapter.getItem(pos);		
 		if (gd.isShrinked) {
 			mCustomAnimation = new AnimUtils.CustomAnimation(300, currentHeight, trueHeight, Call_back);
 			gd.isShrinked = false;
 		} else {
 			mCustomAnimation = new AnimUtils.CustomAnimation(300, currentHeight, 0, Call_back);
 			gd.isShrinked = true;
 		}
 		mCustomAnimation.setAnimationFrequency(60);
 		mCustomAnimation.startAnimation();
 		vg.setTag(mCustomAnimation);
 	}
 }
