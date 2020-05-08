 package net.jsiq.marketing.fragment;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.jsiq.marketing.R;
 import net.jsiq.marketing.adapter.CatalogAdapter;
 import net.jsiq.marketing.constants.URLStrings;
 import net.jsiq.marketing.model.CatalogItem;
 import net.jsiq.marketing.util.LoaderUtil;
 import net.jsiq.marketing.util.MessageToast;
 import net.jsiq.marketing.util.NetworkUtils;
 import net.jsiq.marketing.util.ViewHelper;
 import android.content.Context;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockFragment;
 import com.viewpagerindicator.TabPageIndicator;
 
 public class CatalogFragment extends SherlockFragment implements
 		OnClickListener {
 
 	public static final String MENU_ID = "menu_id";
 	public static final String CATALOG_TITLE = "catalog_title";
	private static final String CATALOG_POS = "catalog_pos";
 
 	private Context context;
 
 	private FragmentPagerAdapter adapter;
 	private ViewPager pager;
 	private TabPageIndicator indicator;
 
 	private int currentPos = 0;
 	private List<CatalogItem> catalogList;
 	private int menuId;
 	private View catalogView;
 	private View loadingHintView, loadingFailedHintView;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setRetainInstance(true);
 		catalogList = new ArrayList<CatalogItem>();
 		this.context = getSherlockActivity();
 		menuId = getArguments().getInt(MENU_ID);
 		ActionBar actionBar = getSherlockActivity().getSupportActionBar();
 		ViewHelper.setActionBarCustomerViewContent(actionBar, getArguments()
 				.getString(CATALOG_TITLE));
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View convertView = inflater.inflate(R.layout.simple_tabs, container,
 				false);
 		catalogView = convertView.findViewById(R.id.catalogs);
 		loadingHintView = convertView.findViewById(R.id.loadingHint);
 		loadingFailedHintView = convertView
 				.findViewById(R.id.loadingFailedHint);
 		loadingFailedHintView.setOnClickListener(this);
 
 		adapter = new CatalogAdapter(getChildFragmentManager(), catalogList);
 
 		pager = (ViewPager) convertView.findViewById(R.id.pager);
 		pager.setAdapter(adapter);
 
 		indicator = (TabPageIndicator) convertView.findViewById(R.id.indicator);
 		indicator.setViewPager(pager);
 		if (savedInstanceState != null) {
			currentPos = savedInstanceState.getInt(CATALOG_POS);
 		}
 		indicator.setCurrentItem(currentPos);
 		return convertView;
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		loadCatalog();
 	}
 
 	private void loadCatalog() {
 		catalogView.setVisibility(View.GONE);
 		if (NetworkUtils.isNetworkConnected(context)) {
 			loadingHintView.setVisibility(View.VISIBLE);
 			loadingFailedHintView.setVisibility(View.GONE);
 			String getCatalogUrl = URLStrings.GET_CATALOGS_BY_MENUID + menuId;
 			new LoadCatalogTask().execute(getCatalogUrl);
 		} else {
 			MessageToast.showText(context, R.string.notConnected);
 			loadingFailedHintView.setVisibility(View.VISIBLE);
 		}
 	}
 
 	class LoadCatalogTask extends AsyncTask<String, Void, List<CatalogItem>> {
 
 		@Override
 		protected void onPreExecute() {
 			super.onPreExecute();
 			catalogList.clear();
 		}
 
 		@Override
 		protected List<CatalogItem> doInBackground(String... params) {
 			try {
 				return LoaderUtil.loadCatalogItems(params[0]);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(List<CatalogItem> result) {
 			super.onPostExecute(result);
 			if (result == null) {
 				MessageToast.showText(context, R.string.loadFailed);
 				loadingFailedHintView.setVisibility(View.VISIBLE);
 			} else {
 				catalogList.addAll(result);
 				adapter.notifyDataSetChanged();
 				indicator.notifyDataSetChanged();
 				if (catalogList.size() < 2) {
 					indicator.setVisibility(View.GONE);
 				} else {
 					indicator.setVisibility(View.VISIBLE);
 				}
 				catalogView.setVisibility(View.VISIBLE);
 			}
 			loadingHintView.setVisibility(View.GONE);
 		}
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		currentPos = pager.getCurrentItem();
		outState.putInt(CATALOG_POS, currentPos);
		super.onSaveInstanceState(outState);
 	}
 
 	@Override
 	public void onClick(View v) {
 		switch (v.getId()) {
 		case R.id.loadingFailedHint:
 			loadCatalog();
 			break;
 		default:
 			break;
 		}
 	}
 
 }
