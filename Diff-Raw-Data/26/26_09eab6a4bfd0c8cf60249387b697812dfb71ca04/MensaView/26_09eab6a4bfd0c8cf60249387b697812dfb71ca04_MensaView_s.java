 package com.mendroid.sky;
 
 import java.io.File;
 import java.text.SimpleDateFormat;
import java.util.Calendar;
 import java.util.Date;
 
 import android.app.Activity;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.view.ViewPager;
 import android.support.v4.view.ViewPager.OnPageChangeListener;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.Window;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.DatePicker;
import android.widget.TextView;
 import android.widget.Toast;
 
 import com.mendroid.structures.MensaList;
 import com.mendroid.structures.MensaStruct;
 
 public class MensaView extends Activity implements OnItemClickListener,
 		OnPageChangeListener {
 
 	private static final String INDEX_KEY = "MEN_LIST_INDEX";
 	private boolean firstShow;
 
 	static final int DATE_DIALOG_ID = 0;
 
 	private int mYear;
 	private int mMonth;
 	private int mDay;
 	private int curListIndex;
 
 	private MensaList mMenList;
 
 	private ViewPager pager;
 
 
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		Log.d("Mendroid", "MV Created!");
 
 		setContentView(R.layout.menviewlay);
 		pager = (ViewPager) findViewById(R.id.listpager);
 		
 
 		firstShow = true;
 
 		CacheManager.setDirectory(getCacheDir());
 		mMenList = CacheManager.load();
 
 		if (savedInstanceState != null) {
 			curListIndex = savedInstanceState.getInt(INDEX_KEY, -1);
 			Log.d("Mendroid", "Instance State restored");
 		} else {
 			curListIndex = -1;
 		}
 
 		Log.v("Mendroid", "Mensa loaded");
 
 		if (mMenList == null || mMenList.getList().size() == 0) {
 			Log.e("Mendroid", "Mensa View called without data. Aborting.");
 			this.finish();
 		} else {
 
 			Log.v("Mendroid", "Creating Pager");
 			if (pager == null) {
 				Log.e("Mendroid", "Pager View not found. Aboring.");
 				finish();
 			} else {
 				pager.setAdapter(new MensaPagerAdapter(this, mMenList));
 				
 				TitlePageIndicator titleIndicator = (TitlePageIndicator)findViewById(R.id.titles);
 				titleIndicator.setViewPager(pager);
 				titleIndicator.setOnPageChangeListener(this);
 			}
 
 			if (curListIndex < 0) {
 				curListIndex = mMenList.getIndexByDay(new Date());
 				if (curListIndex < 0) {
 					curListIndex = (mMenList.getList().size() > 1) ? 1 : 0;
 				}
 			}
 
 			pager.setCurrentItem(curListIndex);
 
 			Log.v("Mendroid", "List Index: " + String.valueOf(curListIndex));
 			MensaStruct mMensa = mMenList.getList().get(curListIndex);
 
 			Log.d("Mendroid", "Got Mensa of " + mMensa.getDay().toString());
 
 			mYear = mMensa.getDay().getYear() + 1900;
 			mMonth = mMensa.getDay().getMonth();
 			mDay = mMensa.getDay().getDate();
 
 
 		}
 
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		Log.v("Mendroid", "MV resume");
 
 		if (firstShow) {
 
 			if (mMenList == null) {
 				return;
 			}
 
 			Date d = mMenList.getLastUpdate();
 
 			SimpleDateFormat df = new SimpleDateFormat("dd.M HH:mm");
 			Log.v("Mendroid", "Got date");
 			Toast.makeText(this,
 					getString(R.string.MSG_UPDATED) + df.format(d),
 					Toast.LENGTH_SHORT).show();
 			Log.v("Mendroid", "Toast created");
 			firstShow = false;
 
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		Log.v("Mendroid", "Inflating menu");
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.viewmen, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.it_del_cache:
 			deleteCache();
 			return true;
 		case R.id.it_pick_date:
 			Log.d("Mendroid",
 					"Date Dialog called: " + String.valueOf(mDay) + "-"
 							+ String.valueOf(mMonth) + "-"
 							+ String.valueOf(mYear));
 			showDialog(DATE_DIALOG_ID);
 			return true;
 		case R.id.it_quit:
 			finish();
 			return true;
 		case R.id.it_pref:
 			startActivity(new Intent(this, Preferences.class));
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	private void deleteCache() {
 
 		deleteImageCache();
 
 		if (CacheManager.delete()) {
 			Toast.makeText(this, getString(R.string.MSG_CACHE_DELETED),
 					Toast.LENGTH_SHORT).show();
 		} else {
 			Toast.makeText(this, getString(R.string.ERR_DEL_CACHE),
 					Toast.LENGTH_SHORT).show();
 		}
 
 	}
 
 	private void deleteImageCache() {
 		File imgCache = getDir("imagecache", Context.MODE_PRIVATE);
 		for (File file : imgCache.listFiles()) {
 			file.delete();
 		}
 	}
 
 	private void changeDate(Date d) {
 
 		if (mMenList != null) {
 			int index = mMenList.getIndexByDay(d);
 
 			if (index < 0) {
 				Toast.makeText(this, getString(R.string.MSG_NO_DATA),
 						Toast.LENGTH_SHORT).show();
 			} else {
 				curListIndex = index;
 				pager.setCurrentItem(index);
 				mYear = d.getYear() + 1900;
 				mMonth = d.getMonth();
 				mDay = d.getDate();
 			}
 		}
 
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		outState.putInt(INDEX_KEY, curListIndex);
 	}
 
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		switch (id) {
 		case DATE_DIALOG_ID:
 			return new DatePickerDialog(this, mDateSetListener, mYear, mMonth,
 					mDay);
 		}
 		return null;
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> l, View v, int position, long id) {
 
 		ListElementContainer curCont = (ListElementContainer) l
 				.getItemAtPosition(position);
 
 		if (curCont != null && !curCont.isLine() && curCont.getDish() != null) {
 			Intent it = new Intent(this, DishDetail.class);
 			it.putExtra("DISH", curCont.getDish());
 			startActivity(it);
 		}
 
 	}
 
 
 
 	private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
 
 		public void onDateSet(DatePicker view, int year, int monthOfYear,
 				int dayOfMonth) {
 			changeDate(new Date(year - 1900, monthOfYear, dayOfMonth));
 		}
 	};
 
 	@Override
 	public void onPageScrollStateChanged(int arg0) {
 	}
 
 	@Override
 	public void onPageScrolled(int position, float positionOffset,
 			int positionOffsetPixels) {
 		if (positionOffset == 0) {
 			
 			if (position != curListIndex) {
 				Log.d("Mendroid", "Page changed to " + String.valueOf(position));
 				curListIndex = position;
 				Date d = mMenList.getList().get(curListIndex).getDay();
 				mYear = d.getYear() + 1900;
 				mMonth = d.getMonth();
 				mDay = d.getDate();				
 			}
 		}
 	}
 
 	@Override
 	public void onPageSelected(int arg0) {
 	}
 
 }
