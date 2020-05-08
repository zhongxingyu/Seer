 package tv.pps.tj.ppsdemo.ui;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.drawable.BitmapDrawable;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.text.Editable;
 import android.text.TextUtils;
 import android.text.TextWatcher;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.animation.AlphaAnimation;
 import android.view.animation.Animation;
 import android.view.animation.TranslateAnimation;
 import android.widget.*;
 import com.xengine.android.data.cache.DefaultDataRepo;
 import com.xengine.android.data.cache.filter.XBaseFilter;
 import com.xengine.android.media.graphics.XScreen;
 import com.xengine.android.utils.XLog;
 import tv.pps.tj.ppsdemo.R;
 import tv.pps.tj.ppsdemo.data.cache.GlobalStateSource;
 import tv.pps.tj.ppsdemo.data.cache.ProgramBaseSource;
 import tv.pps.tj.ppsdemo.data.cache.SourceName;
 import tv.pps.tj.ppsdemo.data.model.ProgramBase;
 import tv.pps.tj.ppsdemo.engine.MyImageScrollRemoteLoader;
 import tv.pps.tj.ppsdemo.engine.ScreenHolder;
 import tv.pps.tj.ppsdemo.logic.ProgramMgr;
 import tv.pps.tj.ppsdemo.ui.animation.Rotate3dAnimationHelper;
 import tv.pps.tj.ppsdemo.ui.controls.AlphabetView;
 import tv.pps.tj.ppsdemo.util.ClickUtil;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * 显示某频道下各个节目的界面.
  * Created with IntelliJ IDEA.
  * User: tujun
  * Date: 13-7-17
  * Time: 上午9:18
  * To change this template use File | Settings | File Templates.
  */
 public class FragmentChannel extends Fragment {
     private static final String TAG = "Channel";
 
     public static final int MODE_LISTVIEW = 0;// 列表模式
     public static final int MODE_GRIDVIEW = 1;// 网格模式
 
     GlobalStateSource globalStateSource;
     private ProgramBaseSource mProgramBaseSource;
 
     private ImageView mMenuBtn;
     private View.OnClickListener mMenuBtnListener;
     private TextView mChannelNameView;
     private ImageView  mModeBtn;
     private Button mFilterBtn;
     private PopupWindow mFilterPopup;// 选择过滤条件的PopupWindow
     private EditText mSearchInput;
     private Button mClearInputBtn;
     private TextView mTabView1, mTabView2, mTabView3, mTabView4;
     private ImageView mTabTip;// tab标签提示
     private LinearLayout mFilterFrame;// 过滤栏
     private RelativeLayout mProgramContentFrame;
     private View mProgramListViewFrame;
     private ListView mProgramListView;// 节目的ListView
     private AlphabetView mListViewLetters;// ListView的字母列表
     private View mProgramGridViewFrame;
     private GridView mProgramGridView;// 节目的GridView
     private AlphabetView mGridViewLetters;// GridView的字母表
     private AdapterProgramListView mProgramListViewAdapter;
     private AdapterProgramGridView mProgramGridViewAdapter;
     private View mLetterTipFrame;
     private TextView mLetterTip;// 当前选中的letter提示框
     private Button mListViewTopBtn, mGridViewTopBtn;
     private TopBtnScrollListener mListViewScrollListener;
     private TopBtnScrollListener mGridViewScrollListener;
 
     private View mAllLoadingFrame;// 整个节目加载提示
     private ProgressBar mAllLoadingProgressBar;
     private TextView mAllLoadingTextView;
 
     private Rotate3dAnimationHelper mRotate3dAnimationHelper;
     private boolean mModeChangeAnimation = false;// 是否正在播放切换模式的动画
     private boolean mTipMoveAnimation = false;// 是否正在播放切换标签的动画
 
     private int mCurrentMode;//当前显示模式
     private int mSelectedTabIndex;// 选中的标签页码
     private ProgramBaseFilter mFilter;// 过滤器
     private int mFilterPopupSelectedIndex;// 过滤弹出框中选中的标签页码
 
     private LoadAllTask mLoadAllTask;// 加载数据的异步线程
 
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
         globalStateSource = (GlobalStateSource) DefaultDataRepo.getInstance().getSource(SourceName.GLOBAL_STATE);
         mProgramBaseSource = (ProgramBaseSource) DefaultDataRepo.getInstance().getSource(SourceName.PROGRAM_BASE);
 
         ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.channel_frame, container, false);
         mMenuBtn = (ImageView) rootView.findViewById(R.id.menu_btn);
         mModeBtn = (ImageView) rootView.findViewById(R.id.mode_btn);
         mChannelNameView = (TextView) rootView.findViewById(R.id.title_txt);
         mFilterBtn = (Button) rootView.findViewById(R.id.filter_btn);
         mSearchInput = (EditText) rootView.findViewById(R.id.search_input);
         mClearInputBtn = (Button) rootView.findViewById(R.id.clear_input_btn);
         mTabView1 = (TextView) rootView.findViewById(R.id.tab_txt1);
         mTabView2 = (TextView) rootView.findViewById(R.id.tab_txt2);
         mTabView3 = (TextView) rootView.findViewById(R.id.tab_txt3);
         mTabView4 = (TextView) rootView.findViewById(R.id.tab_txt4);
         mTabTip = (ImageView) rootView.findViewById(R.id.tab_tip);
         mFilterFrame = (LinearLayout) rootView.findViewById(R.id.filter_frame);
         mProgramContentFrame = (RelativeLayout) rootView.findViewById(R.id.content_list_grid_frame);
         mProgramListViewFrame = rootView.findViewById(R.id.program_listview_frame);
         mProgramListView = (ListView) rootView.findViewById(R.id.program_listview);
         mListViewLetters = (AlphabetView) rootView.findViewById(R.id.program_listview_letters);
         mProgramGridViewFrame = rootView.findViewById(R.id.program_gridview_frame);
         mProgramGridView = (GridView) rootView.findViewById(R.id.program_gridview);
         mGridViewLetters = (AlphabetView) rootView.findViewById(R.id.program_gridview_letters);
         mAllLoadingFrame = rootView.findViewById(R.id.all_loading_frame);
         mAllLoadingProgressBar = (ProgressBar) rootView.findViewById(R.id.all_loading_progressbar);
         mAllLoadingTextView = (TextView) rootView.findViewById(R.id.all_loading_txt);
         mLetterTipFrame = rootView.findViewById(R.id.letter_tip_frame);
         mLetterTip = (TextView) rootView.findViewById(R.id.letter_tip);
         mListViewTopBtn = (Button) rootView.findViewById(R.id.top_btn_listview);
         mGridViewTopBtn = (Button) rootView.findViewById(R.id.top_btn_gridview);
 
         // 初始化监听
         mMenuBtn.setOnClickListener(mMenuBtnListener);  // 设置菜单按钮的监听
         mModeBtn.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 // 如果当前正在播放翻转动画，则忽略用户这次点击
                 if (mModeChangeAnimation)
                     return;
 
                 mModeChangeAnimation = true;
                 mRotate3dAnimationHelper.rotate3d(mCurrentMode == MODE_LISTVIEW);
             }
         });
         mFilterBtn.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 showFilterPopupWindow();
             }
         });
         mClearInputBtn.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 mSearchInput.setText("");
             }
         });
         mTabView1.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 if (mSelectedTabIndex == 0 || mTipMoveAnimation)
                     return;
                 showTabTipAnimation(mTabTip, mSelectedTabIndex, 0);
             }
         });
         mTabView2.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 if (mSelectedTabIndex == 1 || mTipMoveAnimation)
                     return;
                 showTabTipAnimation(mTabTip, mSelectedTabIndex, 1);
             }
         });
         mTabView3.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 if (mSelectedTabIndex == 2 || mTipMoveAnimation)
                     return;
                 showTabTipAnimation(mTabTip, mSelectedTabIndex, 2);
             }
         });
         mTabView4.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 if (mSelectedTabIndex == 3 || mTipMoveAnimation)
                     return;
                 showTabTipAnimation(mTabTip, mSelectedTabIndex, 3);
             }
         });
         mListViewTopBtn.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 if (!mProgramListView.isStackFromBottom()) {
                     mProgramListView.setStackFromBottom(true);
                 }
                 mProgramListView.setStackFromBottom(false);
             }
         });
         mGridViewTopBtn.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 if (!mProgramGridView.isStackFromBottom()) {
                     mProgramGridView.setStackFromBottom(true);
                 }
                 mProgramGridView.setStackFromBottom(false);
             }
         });
 
         // 初始化搜索栏
         mSearchInput.addTextChangedListener(new TextWatcher() {
             @Override
             public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
             }
             @Override
             public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
             }
             @Override
             public void afterTextChanged(Editable editable) {
                 String s = mSearchInput.getText().toString();
                 mFilter.setInput(s);
                 mProgramBaseSource.doFilter();
                 if (!TextUtils.isEmpty(s)) {
                     mClearInputBtn.setVisibility(View.VISIBLE);
                 } else {
                     mClearInputBtn.setVisibility(View.GONE);
                 }
             }
         });
         mClearInputBtn.setVisibility(View.GONE);
 
         // 初始化3d翻转动画
         mRotate3dAnimationHelper = new Rotate3dAnimationHelper
                 (mProgramContentFrame, mProgramListViewFrame, mProgramGridViewFrame);
         mRotate3dAnimationHelper.setListener(new Rotate3dAnimationHelper.Rotate3dAnimationListener() {
             @Override
             public void rotateStart(boolean rotateToBack) {
             }
             @Override
             public void rotateFinish(boolean rotateToBack) {
                 if (rotateToBack) {
                     mCurrentMode = MODE_GRIDVIEW;
                     mModeBtn.setImageResource(R.drawable.icon_top_list);
                 } else {
                     mCurrentMode = MODE_LISTVIEW;
                     mModeBtn.setImageResource(R.drawable.icon_top_grid);
                 }
                 globalStateSource.setChannelFrameMode(mCurrentMode);
                 mModeChangeAnimation = false;
             }
         });
 
         // 初始化列表
         addFooterInListView(getActivity(), mProgramListView);
         mProgramListViewAdapter = new AdapterProgramListView(getActivity());
         mProgramListView.setAdapter(mProgramListViewAdapter);
         mProgramListView.setOnItemClickListener(new ProgramItemClickListener());
         mProgramGridViewAdapter = new AdapterProgramGridView(getActivity());
         mProgramGridView.setAdapter(mProgramGridViewAdapter);
         mProgramGridView.setOnItemClickListener(new ProgramItemClickListener());
         // 设置滚动监听
         mListViewScrollListener = new TopBtnScrollListener(mListViewTopBtn, mProgramListViewAdapter);
         mGridViewScrollListener = new TopBtnScrollListener(mGridViewTopBtn, mProgramGridViewAdapter);
         mProgramListView.setOnScrollListener(mListViewScrollListener);
         mProgramGridView.setOnScrollListener(mGridViewScrollListener);
         // 注册列表对数据源的监听
         mProgramBaseSource.registerDataChangeListener(mProgramListViewAdapter);
         mProgramBaseSource.registerDataChangeListener(mProgramGridViewAdapter);
         // 初始化过滤器
         mFilter = new ProgramBaseFilter();
         mProgramBaseSource.setFilter(mFilter);
 
         // 初始化字母列表
         mListViewLetters.setOnLetterChangedListener(new AlphabetView.OnLetterChangedListener() {
             @Override
             public void onTouchDown() {
                 mLetterTipFrame.setVisibility(View.VISIBLE);
             }
             @Override
             public void onTouchUp() {
                 mLetterTipFrame.setVisibility(View.INVISIBLE);
             }
             @Override
             public void onTouchLetterChanged(String s) {
                 mLetterTip.setText(s);
                 // 跳转到对应的位置
                 Map<String, Integer> alphaIndexer =  mProgramBaseSource.getAlphaIndexer();
                 if (alphaIndexer != null) {
                     Integer position = alphaIndexer.get(s);
                     if (position != null)
                         mProgramListView.setSelection(position);
                 }
             }
         });
         mGridViewLetters.setOnLetterChangedListener(new AlphabetView.OnLetterChangedListener() {
             @Override
             public void onTouchDown() {
                 mLetterTipFrame.setVisibility(View.VISIBLE);
             }
             @Override
             public void onTouchUp() {
                 mLetterTipFrame.setVisibility(View.INVISIBLE);
             }
             @Override
             public void onTouchLetterChanged(String s) {
                 mLetterTip.setText(s);
                 // 跳转到对应的位置
                 Map<String, Integer> alphaIndexer =  mProgramBaseSource.getAlphaIndexer();
                 if (alphaIndexer != null) {
                     Integer position = alphaIndexer.get(s);
                     if (position != null)
                         mProgramGridView.setSelection(position);
                 }
             }
         });
 
         // 初始化显示模式(上次用户是列表模式还是网格模式)
         mCurrentMode = getArguments().getInt("mode");// 传进来的参数
         if (mCurrentMode == MODE_LISTVIEW) {
             mProgramListViewFrame.setVisibility(View.VISIBLE);
             mProgramGridViewFrame.setVisibility(View.GONE);
             mModeBtn.setImageResource(R.drawable.icon_top_grid);
         } else {
             mProgramListViewFrame.setVisibility(View.GONE);
             mProgramGridViewFrame.setVisibility(View.VISIBLE);
             mModeBtn.setImageResource(R.drawable.icon_top_list);
         }
 
         // 初始化标签
         mSelectedTabIndex = 0;
         // 初始化动画
         mModeChangeAnimation = false;
         // 初始化标题
         String currentChannelName = getArguments().getString("name");
         mChannelNameView.setText(currentChannelName);
 
         return rootView;
     }
 
     @Override
     public void onAttach(Activity activity) {
         super.onAttach(activity);
         if (activity instanceof ActivityMain) {
             ActivityMain hostActivity = (ActivityMain) activity;
             mMenuBtnListener = hostActivity.getMenuBtnClickListener();
         }
     }
 
     @Override
     public void onStart() {
         super.onStart();
 
         if (mLoadAllTask != null) {
             mLoadAllTask.cancel(true);
             mLoadAllTask = null;
         }
         mLoadAllTask = new LoadAllTask();
         mLoadAllTask.execute(null);
     }
 
     @Override
     public void onStop() {
         super.onStop();
 
         if (mFilterPopup != null)
             mFilterPopup.dismiss();
 
         if (mLoadAllTask != null) {
             mLoadAllTask.cancel(true);
             mLoadAllTask = null;
         }
     }
 
     @Override
     public void onDestroyView() {
         super.onDestroyView();
 
         // 注册列表对数据源的监听
         mProgramBaseSource.unregisterDataChangeListener(mProgramListViewAdapter);
         mProgramBaseSource.unregisterDataChangeListener(mProgramGridViewAdapter);
     }
 
     private void addFooterInListView(Context context, ListView listView) {
         View footer = View.inflate(context, R.layout.program_listview_foot, null);
         listView.addFooterView(footer, null, false);
     }
 
     /**
      * 初始化FilterPopupWindow
      */
     private void showFilterPopupWindow() {
         if (mFilterPopup == null) {
             View contentView =View.inflate(getActivity(), R.layout.program_filter_popup, null);
             XScreen screen = ScreenHolder.getInstance();
             int popupWidth = screen.dp2px(160);
             int popupHeight = screen.getScreenHeight() - screen.dp2px(120);
             XLog.d(TAG, "screenHeight:" + screen.getScreenHeight()
                     + ", popupHeight:" + popupHeight);
             mFilterPopup = new PopupWindow(contentView, popupWidth, popupHeight);
 
             // type数据
             final String KEY = "key";
             String[] types = getActivity().getResources().getStringArray(R.array.filter_type);
             final ArrayList<Map<String, String>> typeItems = new ArrayList<Map<String, String>>();
             for (int i = 0; i < types.length; i++) {
                 Map<String, String> map = new HashMap<String, String>();
                 map.put(KEY, types[i]);
                 typeItems.add(map);
             }
             // year数据
             int year = Calendar.getInstance().get(Calendar.YEAR);
             int yearSize = year - 2000 + 3;
             final ArrayList<Map<String, String>> yearItems = new ArrayList<Map<String, String>>();
             for (int i = 0; i < yearSize - 2; i++) {
                 Map<String, String> map = new HashMap<String, String>();
                 map.put(KEY, "" + (year - i));
                 yearItems.add(map);
             }
             Map<String, String> map90 = new HashMap<String, String>();
             map90.put(KEY, "90年代");
             yearItems.add(map90);
             Map<String, String> map80 = new HashMap<String, String>();
             map80.put(KEY, "80年代");
             yearItems.add(map80);
             // letter数据
             char letterA = 'A';
             final ArrayList<Map<String, String>> letterItems = new ArrayList<Map<String, String>>();
             for (int i = 0; i < 26; i++) {
                 Map<String, String> map = new HashMap<String, String>();
                 char letter = (char) (letterA + i);
                 map.put(KEY, "" + letter);
                 letterItems.add(map);
             }
             Map<String, String> mapNumber = new HashMap<String, String>();
             mapNumber.put(KEY, "数字");
             letterItems.add(mapNumber);
 
             // 布局
             final TextView tab1 = (TextView) contentView.findViewById(R.id.filter_tab1);
             final TextView tab2 = (TextView) contentView.findViewById(R.id.filter_tab2);
             final TextView tab3 = (TextView) contentView.findViewById(R.id.filter_tab3);
             final ListView itemList = (ListView) contentView.findViewById(R.id.filter_list);
             final SimpleAdapter adapter1 = new SimpleAdapter(getActivity(), typeItems,
                     R.layout.program_filter_popup_item, new String[] { KEY },
                     new int[] { R.id.filter_txt });
             final SimpleAdapter adapter2 = new SimpleAdapter(getActivity(), yearItems,
                     R.layout.program_filter_popup_item, new String[] { KEY },
                     new int[] { R.id.filter_txt });
             final SimpleAdapter adapter3 = new SimpleAdapter(getActivity(), letterItems,
                     R.layout.program_filter_popup_item, new String[] { KEY },
                     new int[] { R.id.filter_txt });
             itemList.setAdapter(adapter1);
             itemList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                 @Override
                 public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                     switch (mFilterPopupSelectedIndex) {
                         case 0:
                             mFilter.setFilterType(typeItems.get(i).get(KEY));
                             mProgramBaseSource.doFilter();
                             refreshFilterFrame();
                             break;
                         case 1:
                             mFilter.setFilterYear(yearItems.get(i).get(KEY));
                             mProgramBaseSource.doFilter();
                             refreshFilterFrame();
                             break;
                         case 2:
                             mFilter.setFilterFirstLetter(letterItems.get(i).get(KEY));
                             mProgramBaseSource.doFilter();
                             refreshFilterFrame();
                             break;
                     }
                 }
             });
             tab1.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View view) {
                     if (mFilterPopupSelectedIndex == 0)
                         return;
                     mFilterPopupSelectedIndex = 0;
                     itemList.setAdapter(adapter1);
                     tab1.setBackgroundResource(R.drawable.area_arrows);
                     tab2.setBackgroundResource(R.drawable.area_ohters);
                     tab3.setBackgroundResource(R.drawable.area_ohters);
                 }
             });
             tab2.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View view) {
                     if (mFilterPopupSelectedIndex == 1)
                         return;
                     mFilterPopupSelectedIndex = 1;
                     itemList.setAdapter(adapter2);
                     tab1.setBackgroundResource(R.drawable.area_ohters);
                     tab2.setBackgroundResource(R.drawable.area_arrows);
                     tab3.setBackgroundResource(R.drawable.area_ohters);
                 }
             });
             tab3.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View view) {
                     if (mFilterPopupSelectedIndex == 2)
                         return;
                     mFilterPopupSelectedIndex = 2;
                     itemList.setAdapter(adapter3);
                     tab1.setBackgroundResource(R.drawable.area_ohters);
                     tab2.setBackgroundResource(R.drawable.area_ohters);
                     tab3.setBackgroundResource(R.drawable.area_arrows);
                 }
             });
             mFilterPopupSelectedIndex = 0;
         }
 
         mFilterPopup.setFocusable(true);
         mFilterPopup.setBackgroundDrawable(new BitmapDrawable());// KEY!!
         mFilterPopup.showAsDropDown(mFilterBtn);
         mFilterPopup.update();
     }
 
     /**
      * 启动tip动画
      * @param view
      * @param oldIndex
      * @param newIndex
      */
     private void showTabTipAnimation(final View view, int oldIndex, final int newIndex) {
         view.clearAnimation();
         final int itemWidth = ScreenHolder.getInstance().getScreenWidth() / 4;
         final Animation animation = new TranslateAnimation(oldIndex * itemWidth,
                 newIndex * itemWidth, 0, 0);
         animation.setFillAfter(true);// True:图片停在动画结束位置
         animation.setDuration(150);
         animation.setAnimationListener(new Animation.AnimationListener() {
             @Override
             public void onAnimationStart(Animation animation) {
                 mTipMoveAnimation = true;
             }
             @Override
             public void onAnimationEnd(Animation animation) {
                 mSelectedTabIndex = newIndex;
                 refreshTabAndView();
                 mTipMoveAnimation = false;
             }
             @Override
             public void onAnimationRepeat(Animation animation) {
             }
         });
         view.startAnimation(animation);
     }
 
     private void showTopBtnAnimation(final View view, final boolean visible) {
         if (view.getVisibility() == View.VISIBLE && visible)
             return;
         if (view.getVisibility() != View.VISIBLE && !visible)
             return;
 
         view.clearAnimation();
         final Animation animation;
         if (visible)
             animation = new AlphaAnimation(0f, 1f);
         else
             animation = new AlphaAnimation(1f, 0f);
         animation.setFillAfter(true);// True:图片停在动画结束位置
         animation.setDuration(100);
         animation.setAnimationListener(new Animation.AnimationListener() {
             @Override
             public void onAnimationStart(Animation animation) {
             }
             @Override
             public void onAnimationEnd(Animation animation) {
                 if (visible)
                     view.setVisibility(View.VISIBLE);
                 else
                     view.setVisibility(View.GONE);
             }
             @Override
             public void onAnimationRepeat(Animation animation) {
             }
         });
         view.startAnimation(animation);
     }
 
     /**
      * 刷新标签栏，以及对应的ListView或GridView
      */
     private void refreshTabAndView() {
         mTabView1.setTextColor(getActivity().getResources().getColor(R.color.black));
         mTabView2.setTextColor(getActivity().getResources().getColor(R.color.black));
         mTabView3.setTextColor(getActivity().getResources().getColor(R.color.black));
         mTabView4.setTextColor(getActivity().getResources().getColor(R.color.black));
         switch (mSelectedTabIndex) {
             case 0:
                 MyImageScrollRemoteLoader.getInstance().stopAndClear();
                 mTabView1.setTextColor(getActivity().getResources().getColor(R.color.orange));
                 mProgramBaseSource.sort(ProgramBase.getHotComparator());
                 mListViewLetters.setVisibility(View.INVISIBLE);
                 mGridViewLetters.setVisibility(View.INVISIBLE);
                 mListViewScrollListener.setCanShow(true);
                 mGridViewScrollListener.setCanShow(true);
                 break;
             case 1:
                 MyImageScrollRemoteLoader.getInstance().stopAndClear();
                 mTabView2.setTextColor(getActivity().getResources().getColor(R.color.orange));
                 mProgramBaseSource.sort(ProgramBase.getTimeComparator());
                 mListViewLetters.setVisibility(View.INVISIBLE);
                 mGridViewLetters.setVisibility(View.INVISIBLE);
                 mListViewScrollListener.setCanShow(true);
                 mGridViewScrollListener.setCanShow(true);
                 break;
             case 2:
                 MyImageScrollRemoteLoader.getInstance().stopAndClear();
                 mTabView3.setTextColor(getActivity().getResources().getColor(R.color.orange));
                 mProgramBaseSource.sort(ProgramBase.getScoreComparator());
                 mListViewLetters.setVisibility(View.INVISIBLE);
                 mGridViewLetters.setVisibility(View.INVISIBLE);
                 mListViewScrollListener.setCanShow(true);
                 mGridViewScrollListener.setCanShow(true);
                 break;
             case 3:
                 MyImageScrollRemoteLoader.getInstance().stopAndClear();
                 mTabView4.setTextColor(getActivity().getResources().getColor(R.color.orange));
                 mProgramBaseSource.sort(ProgramBase.getLetterComparator());
 
                 mProgramBaseSource.initAlphaIndexer(AlphabetView.ALPHABET);// TIP 初始化映射表
                 mListViewLetters.setVisibility(View.VISIBLE);
                 mGridViewLetters.setVisibility(View.VISIBLE);
                 mListViewScrollListener.setCanShow(false);
                 mGridViewScrollListener.setCanShow(false);
                 break;
             default:
                 break;
         }
     }
 
     /**
      * 刷新过滤条件栏
      */
     private void refreshFilterFrame() {
         if (TextUtils.isEmpty(mFilter.getFilterType()) &&
                 TextUtils.isEmpty(mFilter.getFilterYear()) &&
                 TextUtils.isEmpty(mFilter.getFilterFirstLetter())) {
             mFilterFrame.setVisibility(View.GONE);
             mFilterBtn.setBackgroundResource(R.drawable.bkg_title_words);
         } else {
             mFilterFrame.removeAllViews();
             if (!TextUtils.isEmpty(mFilter.getFilterType()))
                 addItemToFilterFrame(0, mFilter.getFilterType());
             if (!TextUtils.isEmpty(mFilter.getFilterYear()))
                 addItemToFilterFrame(1, mFilter.getFilterYear());
             if (!TextUtils.isEmpty(mFilter.getFilterFirstLetter()))
                 addItemToFilterFrame(2, mFilter.getFilterFirstLetter());
             mFilterFrame.setVisibility(View.VISIBLE);
             mFilterBtn.setBackgroundResource(R.drawable.bkg_title_words_red);
         }
     }
 
     private void addItemToFilterFrame(final int type, String filterStr) {
         View rootView = View.inflate(getActivity(), R.layout.program_filter_label_item, null);
         TextView filterTxt = (TextView) rootView.findViewById(R.id.item_txt);
         filterTxt.setText(filterStr);
         rootView.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 // 删除filter item
                 switch (type) {
                     case 0:
                         mFilter.setFilterType(null);
                         mProgramBaseSource.doFilter();
                         break;
                     case 1:
                         mFilter.setFilterYear(null);
                         mProgramBaseSource.doFilter();
                         break;
                     case 2:
                         mFilter.setFilterFirstLetter(null);
                         mProgramBaseSource.doFilter();
                         break;
                 }
                 refreshFilterFrame();
             }
         });
         ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                 ViewGroup.LayoutParams.WRAP_CONTENT,
                 ViewGroup.LayoutParams.FILL_PARENT);
         mFilterFrame.addView(rootView, params);
     }
 
     /**
      * 点击ListView或GridView
      */
     private class ProgramItemClickListener implements AdapterView.OnItemClickListener {
         @Override
         public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
             if (ClickUtil.isFastDoubleClick())
                 return;
 
             ProgramBase programBase = mProgramBaseSource.get(i);
             FragmentProgram programDetail = new FragmentProgram();
             Bundle args = new Bundle();
             args.putString("id", programBase.getId());
             args.putString("name", programBase.getName());
             programDetail.setArguments(args);
 
             ((ActivityMain) getActivity()).addFragment(programDetail);// 跳转详情界面
         }
     }
 
     private class TopBtnScrollListener implements AbsListView.OnScrollListener {
         private View mTopBtn;
         private AbsListView.OnScrollListener mNestedListener;
 
         private boolean mIsHead;
         private boolean mCanShow;// 是否显示
         private int mScrollState;
 
         private TopBtnScrollListener(View topBtn, AbsListView.OnScrollListener nestedListener) {
             mTopBtn = topBtn;
             mNestedListener = nestedListener;
             mIsHead = false;
             mCanShow = true;
         }
 
         public void setCanShow(boolean enable) {
             mCanShow = enable;
 
             if (!mIsHead && mCanShow &&  mScrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE)
                 showTopBtnAnimation(mTopBtn, true);
             else
                 showTopBtnAnimation(mTopBtn, false);
         }
 
         @Override
         public void onScrollStateChanged(AbsListView view, int scrollState) {
             XLog.d(TAG, "AbsListView onScrollStateChanged()");
             mNestedListener.onScrollStateChanged(view, scrollState);
 
             mScrollState = scrollState;
             if (!mIsHead && mCanShow && mScrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE)
                 showTopBtnAnimation(mTopBtn, true);
             else
                 showTopBtnAnimation(mTopBtn, false);
         }
         @Override
         public void onScroll(AbsListView view, int firstVisibleItem,
                              int visibleItemCount, int totalItemCount) {
             XLog.d(TAG, "AbsListView onScroll()");
             mNestedListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
 
             View childView = view.getChildAt(0);
             if (childView != null) {
                 if (firstVisibleItem == 0 && childView.getTop() >= 0) {
                     mIsHead = true;
                     showTopBtnAnimation(mTopBtn, false);
                 } else
                     mIsHead = false;
             }
         }
     }
 
 
     /**
      * 加载列表数据的asyncTask
      */
     private class LoadAllTask extends AsyncTask<Void, Void, Boolean> {
 
         @Override
         protected void onPreExecute() {
             mAllLoadingProgressBar.setIndeterminate(true);
             mAllLoadingTextView.setText("正在加载...");
             mAllLoadingProgressBar.setVisibility(View.VISIBLE);
             mAllLoadingFrame.setVisibility(View.VISIBLE);
             mModeBtn.setVisibility(View.GONE);
             mFilterBtn.setVisibility(View.GONE);
         }
         @Override
         protected Boolean doInBackground(Void... voids) {
             return ProgramMgr.getInstance().getProgramList(getActivity(), "141");
         }
         @Override
         protected void onPostExecute(Boolean result) {
             if (result) {
                 mAllLoadingFrame.setVisibility(View.GONE);
                 mModeBtn.setVisibility(View.VISIBLE);
                 mFilterBtn.setVisibility(View.VISIBLE);
                 // 更新界面
                 refreshTabAndView();
                 refreshFilterFrame();
             } else {
                 mAllLoadingProgressBar.setVisibility(View.GONE);
                 mAllLoadingTextView.setText("网络异常，无法加载内容...");
             }
         }
         @Override
         protected void onCancelled(){
             mAllLoadingProgressBar.setVisibility(View.GONE);
             mAllLoadingTextView.setText("网络异常，无法加载内容...");
         }
     }
 
     /**
      * 对于ProgramBase数据源的过滤器
      */
     private class ProgramBaseFilter extends XBaseFilter<ProgramBase> {
         private String mInput;// 用户输入
         private String mFilterType, mFilterYear, mFilterFirstLetter;// 过滤的条件
 
         private void setInput(String input) {
             this.mInput = input;
         }
 
         private String getFilterType() {
             return mFilterType;
         }
 
         private void setFilterType(String filterType) {
             this.mFilterType = filterType;
         }
 
         private String getFilterYear() {
             return mFilterYear;
         }
 
         private void setFilterYear(String filterYear) {
             this.mFilterYear = filterYear;
         }
 
         private String getFilterFirstLetter() {
             return mFilterFirstLetter;
         }
 
         private void setFilterFirstLetter(String filterFirstLetter) {
             this.mFilterFirstLetter = filterFirstLetter;
         }
 
         @Override
         public ProgramBase doFilter(ProgramBase programBase) {
             if (!TextUtils.isEmpty(mInput)) {
                 if (!programBase.getName().contains(mInput))
                     return null;
             }
             if (!TextUtils.isEmpty(mFilterType)) {
                 String programType = programBase.getSearchProgramType();
                 if (TextUtils.isEmpty(programType)) // 如果没有searchType，则用type属性
                     programType = programBase.getType();
                 if (TextUtils.isEmpty(programType))
                     return null;
                 if (!programType.contains(mFilterType))
                     return null;
             }
             if (!TextUtils.isEmpty(mFilterYear)) {
                 String programYear = programBase.getSearchProgramYear();
                 if (TextUtils.isEmpty(programYear))
                     return null;
                 if (!programYear.equals(mFilterYear))
                     return null;
             }
             if (!TextUtils.isEmpty(mFilterFirstLetter)) {
                 if (!programBase.getFirstLetter().equals(mFilterFirstLetter))
                     return null;
             }
             return programBase;
         }
     }
 }
