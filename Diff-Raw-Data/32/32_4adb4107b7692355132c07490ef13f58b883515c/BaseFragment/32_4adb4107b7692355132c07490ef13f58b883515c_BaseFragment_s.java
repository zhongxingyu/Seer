 package com.quanleimu.activity;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentTransaction;
 import android.view.LayoutInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 import com.quanleimu.activity.BaseFragment.TabDef;
 import com.quanleimu.activity.BaseFragment.TitleDef;
 import com.quanleimu.database.ChatMessageDatabase;
 import com.quanleimu.util.Util;
 import com.quanleimu.view.fragment.FirstRunFragment;
 import com.readystatesoftware.viewbadger.BadgeView;
 import com.tencent.mm.sdk.platformtools.Log;
 
 /**
  * 
  * @author liuchong
  *
  */
 public abstract class BaseFragment extends Fragment {
 
 	public static final String TAG = "QLM";//"BaseFragment";
 	
 	protected static int INVALID_REQUEST_CODE = 0xFFFFFFFF;
 	protected int requestCode = INVALID_REQUEST_CODE;
 
 	/**
 	 * Argument keys.
 	 */
 	public static final String ARG_COMMON_TITLE = "name";
 	public static final String ARG_COMMON_BACK_HINT = "backPageName";
 	public static final String ARG_COMMON_REQ_CODE = "reqestCode";
 	
 	protected ProgressDialog pd;
 	
 	private TitleDef titleDef;
 	private TabDef tabDef;
 	
 	protected Handler handler;
 	
 	public enum EBUTT_STYLE{
 		EBUTT_STYLE_BACK,
 		EBUTT_STYLE_NORMAL,
 		EBUTT_STYLE_CUSTOM
 		//EBUTT_STYLE_FORWARD
 	};
 	
 	public final class TitleDef{	
 		private TitleDef() {}
 		public boolean m_visible = true;
 		public String m_leftActionHint = null;
 		public EBUTT_STYLE m_leftActionStyle = EBUTT_STYLE.EBUTT_STYLE_BACK;
 		public int leftCustomResourceId = -1;
 		
 		public String m_title = null;
 		public View m_titleControls = null;
 		
 		public String m_rightActionHint = null;
 		public EBUTT_STYLE m_rightActionStyle = EBUTT_STYLE.EBUTT_STYLE_NORMAL;
 		public int rightCustomResourceId = -1;
 	};
 	
 	public enum ETAB_TYPE{
 		ETAB_TYPE_PREV,
 		ETAB_TYPE_MAINPAGE,
 		ETAB_TYPE_CATEGORY,
 		ETAB_TYPE_PUBLISH,
 		ETAB_TYPE_MINE,
 		ETAB_TYPE_SETTING
 	};
 	
 	public class TabDef{
 		private TabDef() {}
 		public boolean m_visible = true;
 		public ETAB_TYPE m_tabSelected = ETAB_TYPE.ETAB_TYPE_PREV;
 	};
 	
 	@Override
 	public void onAttach(Activity activity) {
 		super.onAttach(activity);
 	}
 
 	protected void handleMessage(Message msg, Activity activity, View rootView)
 	{
 		//Override me to process you message.
 	}
 	
 	protected void sendMessage(int what, Object data)
 	{
 		Message message = null;
 		if (handler != null)
 		{
 			message = handler.obtainMessage();
 			message.what = what;
 			if (data != null)
 			{
 				message.obj = data;
 			}
 			
 			handler.sendMessage(message);
 		}
 
 	}
	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		handler = new Handler() {
 			@Override
 			public void handleMessage(Message msg) {
 				Activity activity = getActivity();
 				View rootView = getView();
 				if (rootView != null && activity != null) {
 					BaseFragment.this.handleMessage(msg, activity, rootView);
 				}
 				else
 				{
					Log.e(TAG, "cannot dispatch handle message because activity is null ? " + Boolean.valueOf(activity == null));
 				}
 			}
 		};
 		
 		if (getArguments() != null)
 		{
 			requestCode = getArguments().getInt(ARG_COMMON_REQ_CODE, INVALID_REQUEST_CODE);
 		}
 		
 		if (savedInstanceState != null)
 		{
 //			Log.d(TAG, "restore from saved state, check arguments auto save ? " + this.getArguments());
 		}
 	}
 	
 	public String getName()
 	{
		return this.getClass().getName() + this.hashCode();
 	}
 	
 	protected final TitleDef getTitleDef()
 	{
 		if (titleDef == null)
 		{
 			titleDef = new TitleDef();
 			initTitle(titleDef);
 		}
 		
 		return titleDef;
 	}
 	
 	protected final void reCreateTitle()
 	{
 		titleDef = null;
 		getTitleDef();
 	}
 	
 	protected void initTitle(TitleDef title) {
 		//Do nothing
 	}
 	
 	public final TabDef getTabDef() {
 		if (tabDef == null)
 		{
 			tabDef = new TabDef();
 			initTab(tabDef);
 		}
 		
 		return tabDef;
 	}
 	
 	protected void initTab(TabDef tab) {
 	}
 	
 	@Override
 	public void onStart() {
 		super.onStart();
 	}
 
 	@Override
 	public void onViewCreated(View view, Bundle savedInstanceState) {
 		super.onViewCreated(view, savedInstanceState);
 	}
 
 	/**
 	 * this called before <code>onStart</code> and after <code>onCreateView</code>
 	 */
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		refreshHeader();
 		refreshFooter();
 	}
 	
 	public final void notifyOnStackTop(boolean isBack)
 	{
 		this.onStackTop(isBack);
 	}
 	
 	protected int getFirstRunId()
 	{
 		return -1;
 	}
 	
 	public void onStackTop(boolean isBack)
 	{
 		
 	}
 	
 	
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		Log.e(TAG, "fragment onActivityResult()");
 	}
 
 	/**
 	 * View will be destroyed before you leave this fragment.
 	 */
 	@Override
 	public void onDestroyView() {
 		super.onDestroyView();
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		return super.onCreateView(inflater, container, savedInstanceState);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * not in use include put into back stack and view not visible to user.
 	 */
 	@Override
 	public void onDestroy() {
 		this.handler = null; 
 		super.onDestroy();
 	}
 
 	/**
 	 * detach will happen when there is no way to return this fragment. eg: pop fragment from stack.
 	 */
 	@Override
 	public void onDetach() {
 		super.onDetach();
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 		
 		hideSoftKeyboard();
 	}
 	
 	protected final void hideSoftKeyboard()
 	{
 		View currentRoot = getView();
 		if (currentRoot != null)
 		{
 			InputMethodManager mgr = (InputMethodManager) this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
 			mgr.hideSoftInputFromWindow(currentRoot.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
 		}
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		getActivity().findViewById(R.id.contentLayout).setVisibility(View.VISIBLE);
 		
 		((BaseActivity) getActivity()).showFirstRun(this);
 		
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 	}
 	
 	protected final void pushAndFinish(BaseFragment f, Bundle bundle)
 	{
 		BaseActivity activity = (BaseActivity) this.getActivity();
 		activity.pushFragment(f, bundle, this.getName());
 	}
 	
 	protected void pushFragment(BaseFragment f, Bundle bundle)
 	{
 		
 		BaseActivity activity = (BaseActivity) this.getActivity();
 		activity.pushFragment(f, bundle, false);
 	}
 	
 	protected final String getFragmentName()
 	{
 		return this.hashCode() + "";
 	}
 	
 	protected final void finishFragment()
 	{
 		BaseActivity activity = (BaseActivity) getActivity();
 		activity.popFragment(this);
 	}
 	
 	protected final void finishFragment(int resultCode, Object result)
 	{
 		BaseActivity activity = (BaseActivity) getActivity();
 		finishFragment();
 		BaseFragment current = activity.getCurrentFragment();
 		if (current != null)
 		{
 			current.onFragmentBackWithData(resultCode, result);
 		}
 	}
 	
 	protected void onFragmentBackWithData(int requestCode, Object result)
 	{
 		Log.w(TAG, "fragment finish with data." + requestCode + "_" + result);
 		//TODO:
 	}
 	
 	public boolean handleBack()
 	{
 		return false;
 	}
 	
 	public void handleRightAction()
 	{
 		
 	}
 	
 	protected void refreshFooter()
 	{
 		if (!this.isUiActive())
 		{
 			return;
 		}
 		
 		TabDef footer = this.getTabDef();
 		Activity activity = this.getActivity();
 		
 		if(null == footer || !footer.m_visible)
 		{
 			activity.findViewById(R.id.linearBottom).setVisibility(View.GONE);
 			activity.findViewById(R.id.ivBottomNull).setVisibility(View.GONE);
 		}
 		else
 		{
 			checkAndUpdateBadge(50);
 			LinearLayout bottom = (LinearLayout)activity.findViewById(R.id.linearBottom);
 			bottom.setVisibility(View.VISIBLE);
 			activity.findViewById(R.id.ivBottomNull).setVisibility(View.VISIBLE);
 		}
 		
 		if(footer != null && footer.m_tabSelected != ETAB_TYPE.ETAB_TYPE_PREV){
 			getActivity().findViewById(R.id.ivHomePage).setBackgroundResource((footer.m_tabSelected == ETAB_TYPE.ETAB_TYPE_MAINPAGE) ? R.drawable.tabbar_cate_selected : R.drawable.iv_homepage_xml);
 			getActivity().findViewById(R.id.ivPostGoods).setBackgroundResource(footer.m_tabSelected == ETAB_TYPE.ETAB_TYPE_PUBLISH ? R.drawable.tabbar_add_selected : R.drawable.iv_postgoods_xml);
 			getActivity().findViewById(R.id.ivMyCenter).setBackgroundResource(footer.m_tabSelected == ETAB_TYPE.ETAB_TYPE_MINE ? R.drawable.tabbar_my_selected : R.drawable.iv_mycenter_xml);
 		}
 	}
 	
 	private void checkAndUpdateBadge(long uiDelay)
 	{
 		final Activity activity = getActivity();
 		if (activity == null)
 		{
 			return;
 		}
 		
 		final BadgeView v = (BadgeView) activity.findViewById(R.id.badge);
 		uiDelay = uiDelay > 0 ? uiDelay : 0;
 			v.postDelayed(new Runnable() {
 
 			public void run() {
 				ChatMessageDatabase.prepareDB(activity);
 				final String myId = Util.getMyId(activity);
 				int count = ChatMessageDatabase.getUnreadCount(null, myId);
 				Log.d("badge", "count" + count);
 				v.setText(count + "");
 				
 				if (count == 0 ||  myId == null) {
 					v.setVisibility(View.GONE);
 				} else {
 					v.setVisibility(View.VISIBLE);
 				}
 			}
 
 		}, uiDelay);
 	}
 	
 	protected void refreshHeader()
 	{
 		if (!this.isUiActive())
 		{
 			Log.e(TAG, "cannot refresh header because ui is not active now");
 			return;
 		}
 		
 		TitleDef title = getTitleDef();
 		Activity activity = getActivity();
 		
 		RelativeLayout top = (RelativeLayout)activity.findViewById(R.id.linearTop);
 		if( title != null && title.m_visible){
 			top.setVisibility(View.VISIBLE);
 			
 			LinearLayout llTitleControls = (LinearLayout) top
 					.findViewById(R.id.linearTitleControls);
 			TextView tTitle = (TextView) activity.findViewById(R.id.tvTitle);
 
 			if (null != title.m_titleControls) {
 				llTitleControls.setVisibility(View.VISIBLE);
 				tTitle.setVisibility(View.GONE);
 				llTitleControls.removeAllViews();
 				llTitleControls.addView(title.m_titleControls);
 			} else {
 				llTitleControls.setVisibility(View.GONE);
 				tTitle.setVisibility(View.VISIBLE);
 				tTitle.setText(title.m_title);
 			}
 			
 			//left action bar settings
 			if(null != title.m_leftActionHint && !title.m_leftActionHint.equals("")){
 				Button left = (Button)activity.findViewById(R.id.btnLeft);
 
 				if(title.m_leftActionStyle == EBUTT_STYLE.EBUTT_STYLE_BACK ){					
 					left.setBackgroundResource(R.drawable.btn_jj);
 				}
 				else //if(title.m_leftActionStyle == EBUTT_STYLE.EBUTT_STYLE_NORMAL )
 				{
 					left.setBackgroundResource(R.drawable.btn_editx);
 				}
 				
 				left.setText(title.m_leftActionHint);				
 				left.setVisibility(View.VISIBLE);				
 			}else if(title.m_leftActionStyle == EBUTT_STYLE.EBUTT_STYLE_CUSTOM && title.leftCustomResourceId > 0){
 				Button left = (Button)activity.findViewById(R.id.btnLeft);
 				left.setBackgroundResource(title.leftCustomResourceId);
 				left.setText("");
 				left.setVisibility(View.VISIBLE);
 			}else{
 				Button left = (Button)activity.findViewById(R.id.btnLeft);
 				left.setVisibility(View.GONE);
 			}
 			
 			
 			//right action bar settings
 			if(null != title.m_rightActionHint && !title.m_rightActionHint.equals("")){
 				Button right = (Button)activity.findViewById(R.id.btnRight);
 				right.setText(title.m_rightActionHint);
 				right.setVisibility(View.VISIBLE);
 				
 				if(title.m_rightActionStyle == EBUTT_STYLE.EBUTT_STYLE_BACK ){
 					right.setBackgroundResource( R.drawable.btn_jj);
 				}
 				else //if(title.m_rightActionStyle == EBUTT_STYLE.EBUTT_STYLE_NORMAL )
 				{
 					right.setBackgroundResource(R.drawable.btn_editx);
 				}
 			}else if(title.m_rightActionStyle == EBUTT_STYLE.EBUTT_STYLE_CUSTOM && title.rightCustomResourceId > 0){
 				Button right = (Button)activity.findViewById(R.id.btnRight);
 				right.setBackgroundResource(title.rightCustomResourceId);
 				right.setText("");
 				right.setVisibility(View.VISIBLE);
 			}else{
 				Button right = (Button)activity.findViewById(R.id.btnRight);
 				right.setVisibility(View.GONE);
 			}
 		}
 		else{
 			top.setVisibility(View.GONE);
 		}
 	}
 	
 	protected static Bundle createArguments(String title, String backhint)
 	{
 		Bundle bundle = new Bundle();
 		if (title != null) bundle.putString(ARG_COMMON_TITLE, title);
 		if (backhint != null) bundle.putString(ARG_COMMON_BACK_HINT, backhint);
 		return bundle;
 	}
 	
 	//View root, 
 //	protected final View findViewById(int id) {
 //		View view = getView();
 //		Activity activity = getActivity();
 //		
 //		View target = null;
 //		if (view != null)
 //		{
 //			target = view.findViewById(id);
 //		}
 //		
 //		if (activity != null && target == null)
 //		{
 //			target =  activity.findViewById(id);
 //		}
 //		
 //		return  target;
 //	}
 	
 	protected final Context getContext()
 	{
 		Activity activity = this.getActivity();
 		if (activity != null )
 		{
 			return activity;
 		}
 		return QuanleimuApplication.context;
 	}
 	
 	protected void logCreateView(Bundle bundle)
 	{
 //		Log.w(TAG,"before create view, do we have one? " + this.getView() + "::create view for " + this.getClass().getName());
 	}
 	
 	
 	
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		// TODO Auto-generated method stub
 		return super.onContextItemSelected(item);
 	}
 
 	private boolean isUiActive()
 	{
 		return this.getActivity() != null;
 	}
 	
 	
 }
