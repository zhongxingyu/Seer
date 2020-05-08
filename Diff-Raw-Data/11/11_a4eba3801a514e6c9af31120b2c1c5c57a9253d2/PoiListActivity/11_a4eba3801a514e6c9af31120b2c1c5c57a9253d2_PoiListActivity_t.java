 package com.utopia.lijiang;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 
 import com.baidu.mapapi.MKPoiInfo;
 import com.baidu.mapapi.MKPoiResult;
 import com.utopia.lijiang.view.SafeProgressDialog;
 
 public class PoiListActivity extends ListActivity {
 
 	static final String LIST_ITEM_NAME = "name";
 	static final String LIST_ITEM_ADDRESS = "address";
 	static final int MAX_SEARCHING_SECOND = 1000*5;
 	
 	ArrayList<MKPoiInfo> poiInfos = null;
 	LijiangMapActivity mapActivity = null;
 	ListView list = null;
 	TextView pageInfo = null; 
 	Button nextButton  = null; 
 	Button preButton = null;
 	private SafeProgressDialog progressDialog;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.search_result_list);
 		
 		pageInfo = (TextView)this.findViewById(R.id.searchRsltPageInfo);
 		nextButton = (Button)this.findViewById(R.id.searchRsltNextPage);
 		preButton = (Button)this.findViewById(R.id.searchRsltPrePage);
 		mapActivity = LijiangMapActivity.getInstance();
 
 		initialProgressDialog();
 		
 		mapActivity.getPoiResListener = new GetPoiResultListener(){
 
 			@Override
 			public void onGetPoiResult(MKPoiResult res, int type, int error) {
 				refreshAll(res);
				progressDialog.cancel();
 			}
 			
 		};
 		
 		refreshAll(mapActivity.searchResult);	
 	}
 
 	
 	
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		mapActivity.getPoiResListener = null;
 	}
 
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		// TODO Auto-generated method stub
 		super.onListItemClick(l, v, position, id);
 		mapActivity.onTapped(position);
 		this.finish();
 	}
 
 
 
 	public void nextPage(View target){
 		mapActivity.showNextPagePoiResualt(target);
 		progressDialog.show();
 	}
 	
 	public void prePage(View target){
 		mapActivity.showPreviousPagePoiResualt(target);
 		progressDialog.show();
 	}
 	
 	private void initialProgressDialog(){
 		//progressDialog = new ProgressDialog(this);
 		progressDialog = new SafeProgressDialog(this,MAX_SEARCHING_SECOND);
 		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 		progressDialog.setMessage(getString(R.string.searching));
 		progressDialog.setCancelable(false);
 	}
 	
 	private int getTotalPageNumber(MKPoiResult res){
 		return res.getNumPages();
 	}
 	
 	private int getCurrentPageNumber(MKPoiResult res){
 		return res.getPageIndex()+1;
 	}
 	
 	public void refreshAll(MKPoiResult res){
 		refreshBottomBar(mapActivity.searchResult);
 		fillList(mapActivity.searchResult);
 	}
 	
 	private void refreshBottomBar(MKPoiResult res) {
 		pageInfo.setText(getPageText(res));
 		preButton.setEnabled(getCurrentPageNumber(res) > 1);
 		nextButton.setEnabled(getCurrentPageNumber(res) < getTotalPageNumber(res));
 	}
 
 	private CharSequence getPageText(MKPoiResult res) {
 		return String.format("%s/%s", getCurrentPageNumber(res),getTotalPageNumber(res));	
 	}
 
 	private void fillList(MKPoiResult res){
 		poiInfos = res.getAllPoi();
 		
 		List<Map<String, String>> data = new ArrayList<Map<String, String>>();
 		for(int index = 0; index<poiInfos.size();index++){
			data.add(CreateListItemData(index+ 1, poiInfos.get(index)));
 		}
 		
 		SimpleAdapter adapter = 
 				new SimpleAdapter(
 						this,
 						data,
 						R.layout.poi_item,
 						new String[]{LIST_ITEM_NAME,LIST_ITEM_ADDRESS},
 						new int[]{R.id.poiTitle,R.id.poiAddress}
 				);
 		
 		setListAdapter(adapter);
 	}
 
	private Map<String, String> CreateListItemData(int num,MKPoiInfo poiInfo){
 		Map<String, String> item = new HashMap<String, String>();
		item.put(LIST_ITEM_NAME, num +". " + poiInfo.name);
 		item.put(LIST_ITEM_ADDRESS, poiInfo.address);
 		return item;
 	}
 	
 	
 }
