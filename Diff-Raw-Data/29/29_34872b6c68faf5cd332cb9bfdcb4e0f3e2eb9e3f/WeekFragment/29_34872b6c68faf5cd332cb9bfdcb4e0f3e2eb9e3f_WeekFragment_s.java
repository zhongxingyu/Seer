 package com.dgsd.android.ShiftTracker.Fragment;
 
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.support.v4.app.LoaderManager;
 import android.support.v4.content.CursorLoader;
 import android.support.v4.content.Loader;
 import android.text.TextUtils;
 import android.view.*;
 import android.widget.AdapterView;
 import android.widget.PopupMenu;
 import android.widget.TextView;
 import com.actionbarsherlock.app.SherlockFragment;
 import com.dgsd.android.ShiftTracker.Adapter.WeekAdapter;
 import com.dgsd.android.ShiftTracker.Data.DbField;
 import com.dgsd.android.ShiftTracker.Data.Provider;
 import com.dgsd.android.ShiftTracker.EditShiftActivity;
 import com.dgsd.android.ShiftTracker.Model.Shift;
 import com.dgsd.android.ShiftTracker.R;
 import com.dgsd.android.ShiftTracker.Service.DbService;
 import com.dgsd.android.ShiftTracker.StApp;
 import com.dgsd.android.ShiftTracker.Util.*;
 import com.emilsjolander.components.StickyListHeaders.StickyListHeadersListView;
import de.neofonie.mobile.app.android.widget.crouton.Crouton;
import de.neofonie.mobile.app.android.widget.crouton.Style;
 
 import java.text.NumberFormat;
 
 import static com.dgsd.android.ShiftTracker.Fragment.HoursAndIncomeSummaryFragment.PayAndDuration;
 
 public class WeekFragment extends SherlockFragment implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener {
     private static final String KEY_JD = "_julian_day";
     private static final String BLANK_TOTAL_TEXT = "0h";
 
     private static final int LOADER_ID_SHIFTS = 0x01;
     private static final int LOADER_ID_TOTAL = 0x02;
     private static final int LOADER_ID_TEMPLATES = 0x03;
 
     private StickyListHeadersListView mList;
     private TextView mTotalText;
     private WeekAdapter mAdapter;
     private ViewGroup mStatsWrapper;
 
     private TemplateListFragment mTemplateList;
     private HoursAndIncomeSummaryFragment mHoursAndIncomeFragment;
     private LinkToPaidAppFragment mLinkToPaidAppFragment;
 
     private int mStartJulianDay = -1;
 
     private boolean mShowHoursPref = true;
     private boolean mShowIncomePref = true;
 
     private boolean mHasTemplates = false;
 
     public static WeekFragment newInstance(int startJulianDay) {
         WeekFragment frag = new WeekFragment();
 
         Bundle args = new Bundle();
         args.putInt(KEY_JD, startJulianDay);
         frag.setArguments(args);
 
         return frag;
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         if(getArguments() != null) {
             mStartJulianDay = getArguments().getInt(KEY_JD, mStartJulianDay);
         }
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         View v = inflater.inflate(R.layout.fragment_week, container, false);
 
         mAdapter = new WeekAdapter(getActivity(), null, mStartJulianDay);
 
         mList = (StickyListHeadersListView) v.findViewById(R.id.list);
         mList.setLayoutAnimation(Anim.getListViewFadeInAnimator());
         mList.setAdapter(mAdapter);
         mList.setOnItemClickListener(this);
 
         registerForContextMenu(mList);
 
         mTotalText = (TextView) v.findViewById(R.id.total_text);
 
         mStatsWrapper = (ViewGroup) v.findViewById(R.id.stats_wrapper);
         mStatsWrapper.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 if(StApp.isFreeApp(getActivity())) {
                     if(mLinkToPaidAppFragment != null && mLinkToPaidAppFragment.isResumed())
                         return; //Already showing
 
                     mLinkToPaidAppFragment = LinkToPaidAppFragment.newInstance(getString(R.string.summary_unavailable_message));
                     mLinkToPaidAppFragment.show(getSherlockActivity().getSupportFragmentManager(), null);
                 } else {
                     if(mHoursAndIncomeFragment != null && mHoursAndIncomeFragment.isResumed())
                         return; //Already showing!
 
                     mHoursAndIncomeFragment = HoursAndIncomeSummaryFragment.newInstance(mStartJulianDay + 6);
                     mHoursAndIncomeFragment.show(getSherlockActivity().getSupportFragmentManager(), null);
                 }
             }
         });
         return v;
     }
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
         getLoaderManager().initLoader(LOADER_ID_TEMPLATES, null, this);
         getLoaderManager().initLoader(LOADER_ID_SHIFTS, null, this);
         getLoaderManager().initLoader(LOADER_ID_TOTAL, null, this);
     }
 
     @Override
     public void onResume() {
         super.onResume();
 
         Prefs p = Prefs.getInstance(getActivity());
         mShowHoursPref = p.get(getString(R.string.settings_key_show_total_hours), true);
         mShowIncomePref = p.get(getString(R.string.settings_key_show_income), true);
     }
 
     @Override
     public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
         switch(id) {
             case LOADER_ID_SHIFTS: {
                 return mAdapter.getWeeklyLoader(getActivity());
             }
             case LOADER_ID_TOTAL: {
                 return mAdapter.getWeeklyLoader(getActivity());
             }
             case LOADER_ID_TEMPLATES:
                 return new CursorLoader(getActivity(), Provider.SHIFTS_URI, null,
                         DbField.IS_TEMPLATE + "> 0", null, null);
             default:
                 return null;
         }
     }
 
     @Override
     public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
         switch(loader.getId()) {
             case LOADER_ID_TEMPLATES:
                 mHasTemplates = cursor != null && cursor.getCount() > 0;
                 break;
             case LOADER_ID_SHIFTS:
                 mAdapter.swapCursor(cursor);
                 break;
             case LOADER_ID_TOTAL:
                 if((mShowHoursPref || mShowIncomePref) && cursor != null && cursor.moveToFirst()) {
                     PayAndDuration pad = PayAndDuration.from(cursor);
 
                     String payText = mShowIncomePref && pad.pay > 0 ? NumberFormat.getCurrencyInstance().format(pad.pay) : null;
                     String hoursText = mShowHoursPref ? UIUtils.getDurationAsHours(pad.mins): null;
 
                     if(TextUtils.isEmpty(payText)) {
                         if(TextUtils.isEmpty(hoursText)) {
                             mStatsWrapper.setVisibility(View.GONE);
                         } else {
                             mStatsWrapper.setVisibility(View.VISIBLE);
                             mTotalText.setText(hoursText);
                         }
                     } else {
                         mStatsWrapper.setVisibility(View.VISIBLE);
                         if(TextUtils.isEmpty(hoursText)) {
                             mTotalText.setText(payText);
                         } else {
                             mTotalText.setText(payText + " / " + hoursText);
                         }
                     }
                 } else {
                     mTotalText.setText(BLANK_TOTAL_TEXT);
                     if(!mShowHoursPref) {
                         mStatsWrapper.setVisibility(View.GONE);
                     } else {
                         mStatsWrapper.setVisibility(View.VISIBLE);
                     }
                 }
                 break;
         }
     }
 
     @Override
     public void onLoaderReset(Loader<Cursor> loader) {
         mAdapter.swapCursor(null);
         mStatsWrapper.setVisibility(View.GONE);
         mTotalText.setText(BLANK_TOTAL_TEXT);
     }
 
     @Override
     public void onItemClick(AdapterView<?> list, final View view, int pos, long id) {
         final WeekAdapter.ViewHolder holder = (WeekAdapter.ViewHolder) view.getTag();
         final Intent intent = getIntentFor(holder);
         if(!mHasTemplates || (holder != null && holder.shift != null)) {
             startActivity(intent);
             return;
         }
 
         final int jd = holder == null ? -1 : holder.julianDay;
         if(Api.isMin(Api.HONEYCOMB)) {
             PopupMenu popup = new PopupMenu(getActivity(), view.findViewById(R.id.text));
             popup.getMenuInflater().inflate(R.menu.week_list_item_popup, popup.getMenu());
             popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                 @Override
                 public boolean onMenuItemClick(MenuItem item) {
                     if(item.getItemId() == R.id.template_shift) {
                         showTemplateChooser(jd);
                     } else if(item.getItemId() == R.id.new_shift) {
                         startActivity(intent);
                     }
                     return false;
                 }
             });
 
             popup.show();
         } else {
             mList.showContextMenuForChild(view);
         }
     }
 
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
         super.onCreateContextMenu(menu, v, menuInfo);
 
         final View targetView = ((AdapterView.AdapterContextMenuInfo) menuInfo).targetView;
         final View view = (ViewGroup) targetView.findViewById(R.id.new_shift).getParent();
         final WeekAdapter.ViewHolder holder = (WeekAdapter.ViewHolder) view.getTag();
         if(holder == null || holder.shift == null) {
             getActivity().getMenuInflater().inflate(R.menu.week_list_item_popup, menu);
         } else {
             getActivity().getMenuInflater().inflate(R.menu.week_list_item_context_menu, menu);
         }
     }
 
     @Override
     public boolean onContextItemSelected(MenuItem item) {
         AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
         final View view = (ViewGroup) info.targetView.findViewById(R.id.new_shift).getParent();
         final WeekAdapter.ViewHolder holder = (WeekAdapter.ViewHolder) view.getTag();
 
         final Intent intent = getIntentFor(holder);
         if(item.getItemId() == R.id.template_shift) {
             showTemplateChooser(holder.julianDay);
         } else if(item.getItemId() == R.id.new_shift) {
             startActivity(intent);
         } else if(item.getItemId() == R.id.delete) {
             DbService.async_delete(getActivity(), Provider.SHIFTS_URI,
                     DbField.ID + "=" + (holder.shift == null ? -1 : holder.shift.id));
             AlarmUtils.get(getActivity()).cancel(holder.shift);
             showMessage("Shift deleted");
         }
 
         return true;
     }
 
     private void showTemplateChooser(final int julianDay) {
         if(mTemplateList != null && mTemplateList.isResumed())
             return; //Already showing
 
         mTemplateList = TemplateListFragment.newInstance();
         mTemplateList.setDismissOnItemClick(true);
         mTemplateList.setOnItemClickListener(new TemplateListFragment.OnTemplateClickListener() {
             @Override
             public void onTemplateClicked(Shift shift) {
                 shift.id = -1;
                 shift.isTemplate = false;
                 shift.endJulianDay = julianDay + (shift.endJulianDay - shift.julianDay);
                 shift.julianDay = julianDay;
 
                 DbService.async_insert(getActivity(), Provider.SHIFTS_URI, shift.toContentValues());
                 showMessage("New shift created");
             }
         });
         mTemplateList.show(getSherlockActivity().getSupportFragmentManager(), null);
     }
 
     private Intent getIntentFor(WeekAdapter.ViewHolder holder) {
         final Intent intent = new Intent(getActivity(), EditShiftActivity.class);
         if(holder != null && holder.shift != null) {
             intent.putExtra(EditShiftActivity.EXTRA_SHIFT, holder.shift);
             return intent;
         } else if(holder != null){
             intent.putExtra(EditShiftActivity.EXTRA_JULIAN_DAY, holder.julianDay);
         }
 
         return intent;
     }
 
     private void showMessage(String msg) {
         if(getActivity() != null)
             Crouton.showText(getActivity(), msg, Style.CONFIRM);
     }
 }
