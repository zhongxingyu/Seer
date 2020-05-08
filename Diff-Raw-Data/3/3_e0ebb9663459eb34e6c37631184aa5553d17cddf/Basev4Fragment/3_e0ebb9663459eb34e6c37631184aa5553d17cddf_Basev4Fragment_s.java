 package andex;
 
 import java.io.Serializable;
 
 import andex.model.DataRow;
 import andex.view.SimpleDialog;
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.os.Handler;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.EditText;
 import android.widget.ExpandableListView;
 import android.widget.FrameLayout;
 import android.widget.GridView;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.RatingBar;
 import android.widget.RelativeLayout;
 import android.widget.ScrollView;
 import android.widget.SeekBar;
 import android.widget.Spinner;
 import android.widget.TableLayout;
 import android.widget.TextView;
 import android.widget.ToggleButton;
 
 /**
  * 扩展的基础Fragment类。
  * 注意：必须在使用前通过构造函数注入、或者onCreate()方法中设置布局资源ID。
  * @author 
  *
  */
 @SuppressLint("ValidFragment")
 public class Basev4Fragment<T extends FragmentActivity> extends Fragment {
 	
 	//
 	protected final int REQUEST_CODE_DEFAULT = 1000;
 	
 	// 当前Fragment的引用
 	protected Fragment thisFragment;
 	
 	// 前一个Fragment（即调用startFragment()方法跳至当前Fragment的）
 	protected Basev4Fragment previousFragment;
 	
 	// 当前Fragment所属的Activity
 	protected T parentActivity;
 	
 	
 	protected Context context;
 
 	// Resources from context.
 	protected Resources rs;
 	
 	// Handler UI update
 	protected final Handler handler = new Handler();
 
 	
 	// Simple Dialogs
 	protected SimpleDialog simpleDialog;
 	
 	// View of Fragment
 	protected View fragmentView;
 	
 	// Resource id for this fragment view.
 	protected int layoutResourceId = 0;
 	
 	public Basev4Fragment() {
 		super();
 		this.thisFragment = this;
 	}
 	
 	public Basev4Fragment(int resourceId) {
 		super();
 		this.thisFragment = this;
 		this.layoutResourceId = resourceId;
 	}
 	
 
 	@Override
 	@SuppressWarnings("unchecked")
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.context = (Context)this.getActivity();
 		this.parentActivity = (T) this.getActivity();
 		this.rs = context.getResources();
 		this.simpleDialog = new SimpleDialog(context);
 	}
 	
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Object view = inflater.inflate(layoutResourceId, null);
 		if(view == null) {
 			throw new RuntimeException("可能是没有设置layoutResourceId");
 		}
 		fragmentView = (View)view;
 		return fragmentView;
 	}
 	
 
 	/**
 	 * 获取LinearLayout
 	 * @param resId
 	 * @return
 	 */
 	protected LinearLayout getLinearLayout(int resId) {
 		return (LinearLayout) fragmentView.findViewById(resId);
 	}
 
 	protected RelativeLayout getRelativeLayout(int resId) {
 		return (RelativeLayout) fragmentView.findViewById(resId);
 	}
 
 	protected FrameLayout getFrameLayout(int resId) {
 		return (FrameLayout) fragmentView.findViewById(resId);
 	}
 
 	protected TableLayout getTableLayout(int resId) {
 		return (TableLayout) fragmentView.findViewById(resId);
 	}
 
 	/**
 	 * 
 	 * @param resId
 	 * @return
 	 */
 	protected TextView getTextView(int resId) {
 		return (TextView)fragmentView.findViewById(resId);
 	}
 	
 	/**
 	 * 设置指定资源ID的TextView的文本为指定文本资源ID
 	 * @param resId TextView的资源ID
 	 * @param strResId 需要设置文本的资源ID
 	 * @return
 	 */
 	protected TextView setTextViewText(int resId, int strResId) {
 		return this.setTextViewText(resId, rs.getString(strResId));
 	}
 	
 	
 	/**
 	 * 设置指定资源ID的TextView的文本
 	 * @param resId
 	 * @param str
 	 * @return
 	 */
 	protected TextView setTextViewText(int resId, String str) {
 		TextView tv = this.getTextView(resId);
 		if (tv != null) {
 			tv.setText(str);
 		}
 		return tv;
 	}
 
 	/**
 	 * 设置指定资源ID的Button的文本
 	 * @param resId
 	 * @param str
 	 * @return
 	 */
 	protected Button setButtonText(int resId, String str) {
 		Button btn = getButton(resId);
 		btn.setText(str);
 		return btn;
 	}
 	
 	/**
 	 * 
 	 * @param resId
 	 * @return
 	 */
 	protected Button getButton(int resId) {
 		return (Button)fragmentView.findViewById(resId);
 	}
 	
 	protected CheckBox getCheckBox(int resId) {
 		return (CheckBox)fragmentView.findViewById(resId);
 	}
 	
 	protected EditText getEditText(int resId) {
 		return (EditText)fragmentView.findViewById(resId);
 	}
 	
 	protected String getEditTextString(int resId) {
 		return getEditText(resId).getText().toString();
 	}
 	
 	protected EditText setEditTextString(int resId, String str) {
 		EditText et = getEditText(resId);
 		if (et != null) {
 			et.setText(str);
 		}
 		return et;
 	}
 	
 	protected Spinner getSpinner(int resId) {
 		return (Spinner)fragmentView.findViewById(resId);
 	}
 	
 	protected ViewGroup getViewGroup(int resId) {
 		return (ViewGroup)fragmentView.findViewById(resId);
 	}
 	
 	protected GridView getGridView(int resId) {
 		return (GridView)fragmentView.findViewById(resId);
 	}
 	
 	protected ListView getListView(int resId) {
 		return (ListView)fragmentView.findViewById(resId);
 	}
 	
 	protected ProgressBar getProgressBar(int resId) {
 		return (ProgressBar)fragmentView.findViewById(resId);
 	}
 	
 	protected RadioButton getRadioButton(int resId) {
 		return (RadioButton)fragmentView.findViewById(resId);
 	}
 	
 	protected RadioGroup getRadioGroup(int resId) {
 		return (RadioGroup) fragmentView.findViewById(resId);
 	}
 	
 	protected SeekBar getSeekBar(int resId) {
 		return (SeekBar) fragmentView.findViewById(resId);
 	}
 	
 	protected ToggleButton getToggleButton(int resId) {
 		return (ToggleButton)fragmentView.findViewById(resId);
 	}
 	
 	protected RatingBar getRatingBar(int resId) {
 		return (RatingBar) fragmentView.findViewById(resId);
 	}
 	
 	protected ExpandableListView getExpandableListView(int resId) {
 		return (ExpandableListView)fragmentView.findViewById(resId);
 	}
 	
 	protected ScrollView getScrollView(int resId) {
 		return (ScrollView)fragmentView.findViewById(resId);
 	}
 	
 	protected ImageView getImageView(int resId) {
 		return (ImageView)fragmentView.findViewById(resId);
 	}
 	
 	protected ImageButton getImageButton(int resId) {
 		return (ImageButton) fragmentView.findViewById(resId);
 	}
 
 	protected SurfaceView getSurfaceView(int resId) {
 		return (SurfaceView)fragmentView.findViewById(resId);
 	}
 	
 	/**
 	 * Make views disabled by resource ids.
 	 * 
 	 * @param ids
 	 */
 	protected void disableViews(int... ids) {
 		for (int i = 0; i < ids.length; i++) {
 			if (fragmentView.findViewById(ids[i]) == null) {
 				continue;
 			}
 			fragmentView.findViewById(ids[i]).setEnabled(false);
 		}
 	}
 
 	/**
 	 * Make views disabled.
 	 * 
 	 * @param views
 	 */
 	protected void disableViews(View... views) {
 		for (int i = 0; i < views.length; i++) {
 			if (views[i] == null) {
 				continue;
 			}
 			views[i].setEnabled(false);
 		}
 	}
 	
 	/**
 	 * Make views enabled by resource ids.
 	 * 
 	 * @param ids
 	 */
 	protected void enableViews(int... ids) {
 		for (int i = 0; i < ids.length; i++) {
 			if (fragmentView.findViewById(ids[i]) == null) {
 				continue;
 			}
 			fragmentView.findViewById(ids[i]).setEnabled(true);
 		}
 	}
 
 	/**
 	 * Make view enabled.
 	 * 
 	 * @param views
 	 */
 	protected void enableViews(View... views) {
 		for (int i = 0; i < views.length; i++) {
 			if (views[i] == null) {
 				continue;
 			}
 			views[i].setEnabled(true);
 		}
 	}
 
 	/**
 	 * 显示多个视图组件
 	 * @param ids
 	 */
 	protected void showViews(int... ids) {
 		for (int i = 0; i < ids.length; i++) {
 			if (fragmentView.findViewById(ids[i]) == null) {
 				continue;
 			}
 			fragmentView.findViewById(ids[i]).setVisibility(View.VISIBLE);
 		}
 	}
 
 	/**
 	 * 显示多个视图组件
 	 * @param views
 	 */
 	protected void showViews(View... views) {
 		for (int i = 0; i < views.length; i++) {
 			if (views[i] == null) {
 				continue;
 			}
 			views[i].setVisibility(View.VISIBLE);
 		}
 	}
 
 	/**
 	 * 隐藏多个视图组件
 	 * @param ids
 	 */
 	protected void hideViews(int... ids) {
 		for (int i = 0; i < ids.length; i++) {
 			if (fragmentView.findViewById(ids[i]) == null) {
 				continue;
 			}
 			fragmentView.findViewById(ids[i]).setVisibility(View.INVISIBLE);
 		}
 	}
 
 	/**
 	 * 隐藏多个视图组件
 	 * @param views
 	 */
 	protected void hideViews(View... views) {
 		for (int i = 0; i < views.length; i++) {
 			if (views[i] == null) {
 				continue;
 			}
 			views[i].setVisibility(View.INVISIBLE);
 		}
 	}
 	
 	/**
 	 * 暂时移除多个视图组件
 	 * @param ids
 	 */
 	protected void unblockViews(int... ids) {
 		for (int i = 0; i < ids.length; i++) {
 			if (fragmentView.findViewById(ids[i]) == null) {
 				continue;
 			}
 			fragmentView.findViewById(ids[i]).setVisibility(View.GONE);
 		}
 	}
 	
 	/**
 	 * 暂时移多个视图组件
 	 * @param views
 	 */
 	protected void unblockViews(View... views) {
 		for (int i = 0; i < views.length; i++) {
 			if (views[i] == null) {
 				continue;
 			}
 			views[i].setVisibility(View.GONE);
 		}
 	}
 	
 
 	/**
 	 * Get view by it's name which is defined in XML.
 	 * @param name
 	 * @return
 	 */
 	protected View getViewByName(String name) {
 		if (this.getActivity() == null) {
 			return null;
 		}
 		int id = rs.getIdentifier(name, "id", this.getActivity().getPackageName());
 		if(id == 0) {
 			return null;
 		}
 		return fragmentView.findViewById(id);
 	}
 	
 	
 	/**
 	 * Simple handle click event for any View component.
 	 * @param resId
 	 * @param handler
 	 */
 	protected View onViewClicked(int resId, final Callback handler) {
 		final View view = fragmentView.findViewById(resId);
 		if(view == null) {
 			Log.w("andex", "No view found：" + rs.getResourceName(resId));
 			return view;
 		}
 		view.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				view.setEnabled(false);
 				handler.invoke();
 				handler.invoke(v);
 				view.setEnabled(true);
 			}
 		});
 		return view;
 	}
 	
 	protected CompoundButton onCompoundButtonChanged(int resId, final Callback<Boolean> handler) {
 		final CompoundButton view = (CompoundButton) fragmentView.findViewById(resId);
 		if(view == null) {
 			Log.w("andex", "No view found：" + rs.getResourceName(resId));
 			return view;
 		}
 		view.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 			@Override
 			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 				view.setEnabled(false);
 				handler.invoke(isChecked);
 				view.setEnabled(true);
 			}
 		});
 		return view;
 	}
 	
 
 	/**
 	 * 隐藏当前的Fragment
 	 */
 	protected void hide() {
 		FragmentTransaction ft = this.getFragmentManager().beginTransaction();
 		ft.hide(this);
 		ft.commit();
 	}
 	
 	
 	/**
 	 * 按照Activity的Class启动
 	 * @param clazz
 	 */
 	protected void startActivity(Class<? extends Activity> clazz) {
 		startActivity(clazz, false);
 	}
 	
 	/**
 	 * 按照Activity的Class启动
 	 * @param clazz
 	 * @param forResult
 	 */
 	protected void startActivity(Class<? extends Activity> clazz, boolean forResult) {
 		if(forResult) {
 			startActivityForResult(new Intent(context, clazz), REQUEST_CODE_DEFAULT);
 		}
 		else {
 			startActivity(new Intent(context, clazz));
 		}
 	}
 
 	/**
 	 * 启动Activity，附带选项ID，并监听返回。
 	 * Start activity with option ID that represent a selection from multi-options.
 	 * @param clazz
 	 * @param id
 	 * @param forResult
 	 */
 	protected void startActivityWith(Class<? extends Activity> clazz, int id, boolean forResult) {
 		startActivityWith(clazz, id, null, forResult);	
 	}
 	
 	/**
 	 * Start activity with biz ID that represent a data row's PK usually.
 	 * use getIdFromPreActivity() to retrieve ID.
 	 * @param clazz
 	 * @param id
 	 */
 	protected void startActivityWith(Class<? extends Activity> clazz, long id, boolean forResult) {
 		startActivityWith(clazz, id, null, forResult);	
 	}
 	
 	/**
 	 * Start activity with arguments.
 	 * @param clazz
 	 * @param args {@link Constants.INTENT_DATA_ARGS_KEY} 
 	 * @param forResult
 	 */
 	protected void startActivityWith(Class<? extends Activity> clazz, Bundle args, boolean forResult) {
 		startActivityWith(clazz, 0L, args, forResult);
 	}
 	
 	/**
 	 * Start activity with biz ID and arguments.
 	 * use getIdFromPreActivity() to retrieve ID.
 	 * use getArgsFromPreActivity() to retrieve arguments.
 	 * @param clazz
 	 * @param id Integer类型表示是选项，Long和String类型表示是ID，其他类型则为参数。
 	 * @param args {@link Constants.INTENT_DATA_ARGS_KEY}
 	 * @param forResult
 	 */
 	protected void startActivityWith(Class<? extends Activity> clazz, Object id, Bundle args, boolean forResult) {
 		Intent intent = new Intent(context, clazz);
 		if(id instanceof Integer) {
 			intent.putExtra(Constants.INTENT_DATA_ID_KEY, (Integer)id);
 		}
 		else if(id instanceof Long) {
 			intent.putExtra(Constants.INTENT_DATA_ID_KEY, (Long)id);
 		}
 		else if(id instanceof String){
 			intent.putExtra(Constants.INTENT_DATA_ID_KEY, (String)id);
 		}
 		
 		if (args != null)
 //			intent.getExtras().putAll(args); // 直接将参数导入
 			intent.putExtra(Constants.INTENT_DATA_ARGS_KEY, args);
 		if(forResult) {
 			startActivityForResult(intent, REQUEST_CODE_DEFAULT);
 		}
 		else {
 			startActivity(intent);			
 		}
 	}
 
 	/**
 	 * 在资源ID指定的位置显示Fragment，如果forResult为true的话，将会在调用finishWithData(Object)后回调前一个Fragment的onFragmentResult()方法
 	 * @param fragment
 	 * @param resId
 	 * @param forResult
 	 */
 	protected void startFragment(Basev4Fragment fragment, int resId, boolean forResult) {
 		FragmentTransaction ft = getFragmentManager().beginTransaction();
 		ft.replace(resId, fragment);
 		ft.commit();
 		if (forResult) {
 			fragment.previousFragment = this;
 		}
 	}
 
 	/**
 	 * 在资源ID指定的位置显示Fragment，如果forResult为true的话，将会在调用finishWithData(Object)后回调前一个Fragment的onFragmentResult()方法
 	 * @param fragment
 	 * @param resId
 	 * @param id
 	 * @param forResult
 	 */
 	protected void startFragmentWithId(Basev4Fragment fragment, int resId, long id, boolean forResult) {
 //		Bundle bundle = new Bundle();
 //		bundle.putLong(Constants.FRAGMENT_DATA_ID_KEY, id);
 //		fragment.setArguments(bundle);
 //		FragmentTransaction ft = getFragmentManager().beginTransaction();
 //		ft.replace(resId, fragment);
 //		ft.commit();
 //		if (forResult) {
 //			fragment.previousFragment = this;
 //		}
 		
 		startFragmentWith(fragment, resId, Constants.FRAGMENT_DATA_ID_KEY, id, forResult);
 	}
 	
 	protected void startFragmentWith(Basev4Fragment fragment, int resId, String key, Serializable value, boolean forResult) {
 		Bundle bundle = new Bundle();
 		bundle.putSerializable(key, value);
 		fragment.setArguments(bundle);
 		FragmentTransaction ft = getFragmentManager().beginTransaction();
 		ft.replace(resId, fragment);
 		ft.commit();
 		if (forResult) {
 			fragment.previousFragment = this;
 		}
 	}
 	
 	
 	protected void startFragmentWith(Basev4Fragment fragment, int resId, long id, Bundle args, boolean forResult) {
 		args.putLong(Constants.FRAGMENT_DATA_ID_KEY, id);
 		fragment.setArguments(args);
 		FragmentTransaction ft = getFragmentManager().beginTransaction();
 		ft.replace(resId, fragment);
 		ft.commit();
 		if (forResult) {
 			fragment.previousFragment = this;
 		}
 	}
 
 	/**
 	 * <p>在资源ID指定的位置显示Fragment，如果forResult为true的话，
 	 * 将会在调用finishWithData(Object)后回调前一个Fragment的onFragmentResult()方法
 	 * @param fragment
 	 * @param resId
 	 * @param bundle
 	 * @param forResult
 	 */
 	protected void startFragmentWithArgs(Basev4Fragment fragment, int resId, Bundle bundle, boolean forResult) {
 		fragment.setArguments(bundle);
 		
 		FragmentManager fm = getFragmentManager();
 		if (fm == null) {
 			Log.w("andex", "Failed to get Fragment manager");
 			return;
 		}
 		FragmentTransaction ft = fm.beginTransaction();
 		ft.replace(resId, fragment);
 		ft.commit();
 		if (forResult) {
 			fragment.previousFragment = this;
 		}
 	}
 	
 	/**
 	 * 从前一个调用该Fragment的地方获得传递过来的Long类型的ID。
 	 * @return
 	 */
 	protected long getLongIdFromPrevious() {
 		Bundle args  = getArguments();
 		if(args == null || args.size() == 0) {
 			return 0;
 		}
 		return args.getLong(Constants.FRAGMENT_DATA_ID_KEY);
 	}
 	
 	
 	/**
 	 * 从前面（Fragment）获得默认的选项参数值（用Constants.FRAGMENT_DATA_OPTION_KEY标识）
 	 * @return
 	 */
 	protected int getOptionFromPrevious() {
 		Bundle args  = getArguments();
 		if (args == null) {
 			return 0;
 		}
 		Object v = args.get(Constants.FRAGMENT_DATA_OPTION_KEY);
 		if (v == null)
 			return 0;
 		return (Integer) v;
 	}
 
 	
 	/**
 	 * 结束当前的Fragment
 	 */
 	protected void finish() {
 		FragmentTransaction ft = this.getFragmentManager().beginTransaction();
 		ft.remove(this);
 		ft.commit();
 	}
 	
 	/**
 	 * 结束当前Fragment中的业务逻辑，
 	 */
 	protected void finishWithData(DataRow data) {
 		if (previousFragment != null) {
 			previousFragment.onFragmentResult(data);
 		}
 		finish();
 	}
 	
 	/**
 	 * 当从一个Fragment返回时调用，并且附带数据（可以为NULL）。
 	 * @param data
 	 */
 	protected void onFragmentResult(DataRow data) {
 		// NOTHING NEED TO DO FOR NOW.
 	}
 
 	
 	/**
 	 * 调试输出
 	 * @param log
 	 */
 	protected void debug(Object log) {
 		if(log == null) log = "[null]";
 		Log.d("andex", log.toString());
 	}
 	
 	/**
 	 * 警告输出
 	 * @param log
 	 */
 	protected void warn(Object log) {
 		if(log == null) log = "[null]";
 		Log.w("andex", log.toString());		
 	}
 	
 	/**
 	 * 错误输出
 	 * @param log
 	 */
 	protected void error(Object log) {
 		if(log == null) log = "[null]";
 		Log.e("andex", log.toString());
 	}
 }
