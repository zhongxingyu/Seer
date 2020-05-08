 package org.droidstack.activity;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import net.sf.stackwrap4j.stackauth.StackAuth;
 import net.sf.stackwrap4j.stackauth.entities.Site;
 
 import org.droidstack.R;
 import org.droidstack.adapter.SitesArrayAdapter;
 import org.droidstack.util.Const;
 
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.util.SparseBooleanArray;
 import android.view.KeyEvent;
 import android.view.View;
 import android.widget.TextView;
 
 public class SitePickerActivity extends ListActivity {
 	
 	private List<Site> mSites;
 	private String[] mCheckedEndpoints;
 	private SitesArrayAdapter mAdapter;
 	
 	private TextView mTitleView;
 	private View mLoadingView;
 	
 	@Override
 	protected void onCreate(Bundle inState) {
 		super.onCreate(inState);
 		setContentView(R.layout.sites_picker);
 		
 		Bundle extras = getIntent().getExtras();
 		mCheckedEndpoints = (String[]) extras.getSerializable("checked");
 		
 		mSites = new ArrayList<Site>();
 		mAdapter = new SitesArrayAdapter(this, mSites);
 		mTitleView = (TextView) View.inflate(this, R.layout.item_header, null);
 		mLoadingView = View.inflate(this, R.layout.item_loading, null);
 		getListView().addHeaderView(mTitleView, null, false);
 		setListAdapter(mAdapter);
 		
 		new GetSites().execute();
 		setTitle("Pick sites");
 	}
 	
 	@Override
 	public void setTitle(CharSequence title) {
 		mTitleView.setText(title);
 	}
 	
 	private void setLoading(boolean loading) {
 		getListView().removeFooterView(mLoadingView);
 		if (loading) getListView().addFooterView(mLoadingView, null, false);
 	}
 	
 	private class GetSites extends AsyncTask<Void, Void, List<Site>> {
 		private Exception e;
 		
 		@Override
 		protected void onPreExecute() {
 			setLoading(true);
 		}
 		@Override
 		protected List<Site> doInBackground(Void... params) {
 			try {
 				return StackAuth.getAllSites();
 			}
 			catch (Exception e) {
 				this.e = e;
 				return null;
 			}
 		}
 		@Override
 		protected void onPostExecute(List<Site> result) {
 			if (isFinishing()) return;
 			setLoading(false);
 			if (e != null) {
 				new AlertDialog.Builder(SitePickerActivity.this)
 					.setTitle(R.string.title_error)
 					.setMessage(R.string.sites_fetch_error)
 					.setCancelable(false)
 					.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							finish();
 						}
 					}).create().show();
 				Log.e(Const.TAG, "Failed to get sites", e);
 			}
 			else {
 				mSites.clear();
 				mSites.addAll(result);
 				mAdapter.notifyDataSetChanged();
 				List<String> checked = Arrays.asList(mCheckedEndpoints);
 				for (int i=0; i < mSites.size(); i++) {
 					if (checked.contains(mSites.get(i).getApiEndpoint()))
						getListView().setItemChecked(i+1, true);
 				}
 			}
 		}
 	}
 	
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
 			if (mSites.size() > 0) {
 				// Bah, deprecated functions (because of a typo)
 				SparseBooleanArray checked = getListView().getCheckedItemPositions();
 				ArrayList<Integer> checkedPositions = new ArrayList<Integer>();
 				for (int i=0; i < mSites.size(); i++) {
					if (checked.get(i)) checkedPositions.add(i-1);
 				}
 				String[] endpoints = new String[checkedPositions.size()];
 				String[] names = new String[checkedPositions.size()];
 				String[] icons = new String[checkedPositions.size()];
 				for (int i=0; i < checkedPositions.size(); i++) {
 					Site site = mSites.get((int) checkedPositions.get(i));
 					endpoints[i] = site.getApiEndpoint();
 					names[i] = site.getName();
 					icons[i] = site.getIconUrl();
 				}
 				Intent result = new Intent();
 				result.putExtra("endpoints", endpoints);
 				result.putExtra("names", names);
 				result.putExtra("icons", icons);
 				setResult(RESULT_OK, result);
 			}
 			finish();
 			return true;
 		}
 		return super.onKeyDown(keyCode, event);
 	}
 	
 }
