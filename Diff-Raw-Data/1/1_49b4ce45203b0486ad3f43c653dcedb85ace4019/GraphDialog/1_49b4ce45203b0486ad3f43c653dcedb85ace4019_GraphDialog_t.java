 package com.zigvine.android.widget;
 
 import java.text.ParseException;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.LinkedList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.taptwo.android.widget.ViewFlow;
 import org.taptwo.android.widget.ViewFlow.ViewSwitchListener;
 
 import com.zigvine.android.anim.AnimUtils;
 import com.zigvine.android.anim.QueuedTextView;
 import com.zigvine.android.http.Request;
 import com.zigvine.android.http.Request.Resp;
 import com.zigvine.android.http.Request.ResponseListener;
 import com.zigvine.android.http.RequestPool;
 import com.zigvine.android.utils.Quota;
 import com.zigvine.android.utils.Utils;
 import com.zigvine.android.utils.Utils.MathFloat;
 import com.zigvine.android.widget.GraphView.OnAxisYTextCallback;
 import com.zigvine.zagriculture.MainApp;
 import com.zigvine.zagriculture.R;
 import com.zigvine.zagriculture.UIActivity;
 
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnDismissListener;
 import android.graphics.Typeface;
 import android.util.Log;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class GraphDialog extends Dialog implements ViewSwitchListener, OnDismissListener {
 	
 	private static final String TAG = "GroupDialog";
 
 	private UIActivity<?> mContext;
 	private Window mWindow;
 	//private Handler handler;
 	
 	private String mDeviceName;
 	private String mDeviceID;
 	private String mQuotaName;
 	private String mQuotaID;
 	private String mUnit;
 	
 	QueuedTextView refreshTime;
 	TextView unitInfo;
 	View progress, loading;
 	ViewFlow list;
 	DataFlowAdapter adapter;
 	RequestPool pool;
 
 
 	public GraphDialog(UIActivity<?> context, String name, String devid, String quotaname, String quotaid) {
 		super(context, R.style.GraphDialog);
 		mContext = context;
 		mDeviceName = name;
 		mDeviceID = devid;
 		mQuotaName = quotaname;
 		mQuotaID = quotaid;
 		//handler = new Handler();
 		pool = new RequestPool(3);
 		setupView();
 	}
 
 	private void setupView() {
 		mWindow = getWindow();
 		View view = View.inflate(mContext, R.layout.graph_dialog, null);
 		mWindow.setContentView(view);
 		WindowManager.LayoutParams winlp = mWindow.getAttributes();
 		winlp.width = WindowManager.LayoutParams.MATCH_PARENT;
 		//winlp.height = WindowManager.LayoutParams.MATCH_PARENT;
 		mWindow.setAttributes(winlp);
 		//mWindow.setGravity(Gravity.TOP);
 		TextView tv = (TextView) mWindow.findViewById(R.id.dialog_device_name);
 		tv.setText(mDeviceName);
 		tv = (TextView) mWindow.findViewById(R.id.dialog_quota_name);
 		tv.setText(mQuotaName);
 		ImageView iv = (ImageView) mWindow.findViewById(R.id.dialog_quotaId);
 		iv.setImageResource(Quota.ICONS[Integer.valueOf(mQuotaID)]);
 		refreshTime = (QueuedTextView) mWindow.findViewById(R.id.dialog_monitor_time);
 		refreshTime.setText(R.string.loading_data);
 		progress = mWindow.findViewById(R.id.dialog_progressbar);
 		progress.setVisibility(View.GONE);
 		unitInfo = (TextView) mWindow.findViewById(R.id.dialog_info);
 		unitInfo.setText("获取图表单位");
 		loading = mWindow.findViewById(R.id.graph_loading);
 		
 		list = (ViewFlow) mWindow.findViewById(R.id.graph_content);
 		adapter = new DataFlowAdapter();
 		list.setAdapter(adapter, 1);
 		list.setOnViewSwitchListener(this);
 		
 		setOnDismissListener(this);
 		
 	}
 
 	public class DataFlowAdapter extends BaseAdapter implements ResponseListener, OnAxisYTextCallback {
 		
 		private static final long HOUR = 1000 * 60 * 60;
 		
 		private Calendar currentTime;
 		private final Calendar initTime;
 		private int initPos;
 		private int curPos;
 		
 		private float ymin, ymax, oldmin, oldmax;
 		
 		LinkedList<long[]> x = new LinkedList<long[]>();
 		LinkedList<float[]> y = new LinkedList<float[]>();
 		LinkedList<float[]> yrange = new LinkedList<float[]>();
 		
 		public DataFlowAdapter() {
 			currentTime = Calendar.getInstance();
 			currentTime.add(Calendar.HOUR, 1);
 			currentTime.set(Calendar.MINUTE, 0);
 			currentTime.set(Calendar.SECOND, 0);
 			currentTime.set(Calendar.MILLISECOND, 0);
 			//Log.i(TAG, Utils.DATETIME.format(currentTime.getTime()));
 			initTime = (Calendar) currentTime.clone();
 			initPos = 0;
 			
 			x.add(null);
 			y.add(null);
 			yrange.add(null);
 			//x.add(new long[] { new Date().getTime() - HOUR, new Date().getTime()});
 			//y.add(new float[] {1, 2});
 		}
 
 		@Override
 		public int getCount() {
 			if (curPos == x.size() - 1) {
 				return 2;
 			} else {
 				return 3;
 			}
 		}
 
 		@Override
 		public Object getItem(int position) {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public long getItemId(int position) {
 			return position;
 		}
 		
 		public void prev() {
 			currentTime.add(Calendar.HOUR, -6);
 			if (curPos > 0) {
 				curPos--;
 			} else {
 				initPos++;
 				x.addFirst(null);
 				y.addFirst(null);
 				yrange.addFirst(null);
 			}
 			notifyDataSetChanged();
 		}
 		
 		public void next() {
 			currentTime.add(Calendar.HOUR, 6);
 			notifyDataSetChanged();
 			if (curPos < x.size() - 1) {
 				curPos++;
 			} else {
 				x.addLast(null);
 				y.addLast(null);
 				yrange.addLast(null);
 				curPos++;
 			}
 			notifyDataSetChanged();
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			if (convertView == null) {
 				convertView = View.inflate(mContext, R.layout.data_item, null);
 			}
 			GraphView gv = (GraphView) convertView.findViewById(R.id.graph_view);
 			gv.setOnAxisYTextCallback(this);
 			int pos = curPos + (position - 1);
 			boolean showLoading = true;
 			
 			if (pos >= x.size() || pos < 0) {
 				// TODO nothing
 			} else if (x.get(pos) == null) {
 				if (position == 1) {
 					fetchDate(pos - initPos);
 				} else {
 					// TODO nothing
 				}
 			} else {
 				long[] sx = x.get(pos);
 				int len = sx.length;
 				int[] dx = new int[len];
 				for (int i = 0; i < len; i++) {
 					dx[i] = (int) ((sx[i] - currentTime.getTimeInMillis()) / 1000);
 				}
 				gv.setDataXY(dx, y.get(pos), len);
 				if (position == 1) {
 					showLoading = false;
 					if (len > 0) {
 						float[] dy = yrange.get(pos);
 						float miny = dy[0];
 						float maxy = dy[1];
 						/*if (pos > 0 && yrange.get(pos - 1) != null) {
 							dy = yrange.get(pos - 1);
 							if (dy[0] < miny) {
 								miny = dy[0];
 							}
 							if (dy[1] > maxy) {
 								maxy = dy[1];
 							}
 						}
 						if (pos < yrange.size() - 1 && yrange.get(pos + 1) != null) {
 							dy = yrange.get(pos + 1);
 							if (dy[0] < miny) {
 								miny = dy[0];
 							}
 							if (dy[1] > maxy) {
 								maxy = dy[1];
 							}
 						}*/
 						float leny = maxy - miny;
 						float padding = 0;
 						if (leny > 0) {
 							padding = leny / 16;
 							ymin = miny - padding;
 							ymax = maxy + padding;
 							oldmin = ymin;
 							oldmax = ymax;
 						} else {
 							 ymin = oldmin;
 							 ymax = oldmax;
 						}
 					}
 				}
 			}
 			int xmax = (position - 1) * 6 * 60 * 60;
 			int xmin = (position - 2) * 6 * 60 * 60;
 			gv.setAxisX(xmin, xmax);
 			gv.setAxisY(ymin, ymax);
 			gv.setXStart(currentTime.getTimeInMillis() / 1000);
 			gv.invalidate();
 			if (position == 1) {
 				if (showLoading) {
 					loading.clearAnimation();
 					if (loading.getVisibility() != View.VISIBLE) {
 						AnimUtils.FadeIn.startAnimation(loading, 300);
 					}
 				} else {
 					if (loading.getVisibility() == View.VISIBLE) {
 						AnimUtils.FadeOut.startAnimation(loading, 300);
 					}
 				}
 			}
 			return convertView;
 		}
 		
 		public void fetchDate(final int offsetFromInit) {
 			//Log.i(TAG, "request fetching data, offset=" + offsetFromInit + ", initPos=" + initPos);
 			
 			Calendar start = Calendar.getInstance();
 			start.setTimeInMillis((offsetFromInit - 1) * 6 * HOUR + initTime.getTimeInMillis());
 			Request request = new Request(Request.DataChart, true);
 			// request.setDebug(true);
 			request.setParam("deviceID", mDeviceID);
 			request.setParam("quotaID", mQuotaID);
 			request.setParam("startTime", Utils.DATETIME.format(start.getTime()));
 			start.add(Calendar.HOUR, 6);
 			request.setParam("endTime", Utils.DATETIME.format(start.getTime()));
 			request.setSoTimeout(23000);
 			request.setConnManagerTimeout(15000);
 			//request.asyncRequest(this, offsetFromInit);
 			if (!pool.hasID(offsetFromInit)) {
 				pool.addRequest(request, this, offsetFromInit);
 			}
 			onConnectionStartOrEnd();
 		}
 
 		@Override
 		public void onResp(int id, Resp resp, Object... obj) {
 			// Log.i(TAG, "successfully getting offset = " + id);
 			int pos = initPos + id;
 			if (pos < 0 || pos >= x.size()) {
 				Log.e(TAG, "never add this pos = " + pos);
 			} else if (resp != null) {
 				try {
 					JSONObject json = resp.json.getJSONObject("data");
 					JSONArray sensorList = json.getJSONArray("sensorList");
 					if (sensorList.length() > 0) {
 						json = sensorList.getJSONObject(0);
 						JSONArray data = json.getJSONArray("dataList");
 						mUnit = json.getString("unit");
 						unitInfo.setText("单位：" + mUnit);
 						int len = data.length();
 						long[] dx = new long[len];
 						float[] dy = new float[len];
 						float[] dyrange = new float[2];
 						if (len > 0) {
 							for (int i = 0; i < len; i++) {
 								JSONArray d = data.getJSONArray(i);
 								String time = d.getString(0);
 								String value = d.getString(1);
 								dx[i] = Utils.DATETIME.parse(time).getTime();
 								dy[i] = Float.valueOf(value);
 							}
 							float miny = MathFloat.min(dy);
 							float maxy = MathFloat.max(dy);
 							dyrange[0] = miny;
 							dyrange[1] = maxy;
 						}
 						x.set(pos, dx);
 						y.set(pos, dy);
 						yrange.set(pos, dyrange);
 						if (pos == curPos) {
 							adapter.notifyDataSetChanged(); // FIXME use invalidate
 						}
 					}
 					
 				} catch (JSONException e) {
 					e.printStackTrace();
 				} catch (ParseException e) {
 					e.printStackTrace();
 				}
 			}
 			onConnectionStartOrEnd();
 		}
 
 		@Override
 		public void onErr(int id, String err, int httpCode, Object... obj) {
 			if (id == curPos) {
 				mContext.UI.toast(err);
 			}
 			onConnectionStartOrEnd();
 		}
 
 		@Override
 		public void onText(String[] text) {
 			Typeface tf = Typeface.createFromAsset(getContext().getAssets(),"fonts/eurostileRegular.ttf");
 			TextView tv = (TextView) mWindow.findViewById(R.id.tx1);
 			tv.setTypeface(tf);
 			tv.setText(text[0]);
 			if (MainApp.getAPILevel() < 11) {
 				tv.measure(0, 0);
 				int left = Utils.dp2px(mContext, 2);
 				int top = Utils.dp2px(mContext, 22);
 				tv.layout(left, top, left + tv.getMeasuredWidth(), top + tv.getMeasuredHeight());
 			}
 			tv = (TextView) mWindow.findViewById(R.id.tx2);
 			tv.setTypeface(tf);
 			tv.setText(text[1]);
 			if (MainApp.getAPILevel() < 11) {
 				tv.measure(0, 0);
 				int left = Utils.dp2px(mContext, 2);
 				int top = Utils.dp2px(mContext, 62);
 				tv.layout(left, top, left + tv.getMeasuredWidth(), top + tv.getMeasuredHeight());
 			}
 		}
 	
 	}
 	
 	private void onConnectionStartOrEnd() {
		progress.clearAnimation();
 		if (pool.getRunningCount() > 0) {
 			if (progress.getVisibility() != View.VISIBLE) {
 				refreshTime.setQueuedText(R.string.loading_data);
 				AnimUtils.FadeIn.startAnimation(progress, 300);
 			}
 		} else {
 			if (progress.getVisibility() == View.VISIBLE) {
 				refreshTime.setQueuedText(Utils.DATETIME.format(new Date()));
 				AnimUtils.FadeOut.startAnimation(progress, 300);
 			}
 		}
 	}
 
 	@Override
 	public void onSwitched(View view, int position) {
 		if (position != 1) {
 			if (position == 0) {
 				adapter.prev();
 			} else {
 				adapter.next();
 			}
 			list.setSelection(1);
 		}
 	}
 
 	@Override
 	public void onDismiss(DialogInterface dialog) {
 		pool.clearAndShutDownAll();
 	}
 
 }
