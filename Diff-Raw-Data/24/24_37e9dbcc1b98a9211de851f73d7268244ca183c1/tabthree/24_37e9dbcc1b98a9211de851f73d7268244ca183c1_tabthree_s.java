 package com.example.tabview_test;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import com.example.tabview_test.database.Qrcode;
 import com.example.tabview_test.database.QrcodeDataOperator;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.os.Bundle;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.PopupMenu;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.PopupMenu.OnMenuItemClickListener;
 
 public class tabthree extends Activity {
 	private List<String> data = new ArrayList<String>();
 	ListView lView;
 	protected List<String> getData() {
 		return data;
 	}
 	protected String getDataElement(int index){
 		return data.get(index);
 	}
 	String qrDataGlobal = "";
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		lView = new ListView(this);
 
 		setContentView(lView);
 		lView.setBackgroundResource(R.drawable.background);
 		lView.setOnItemClickListener(new OnItemClickListener() {  
 	      	  
             @Override  
             public void onItemClick(AdapterView<?> parentView, View childView, int position, long id) {  
                 
 
             	showPopup(childView);
               	qrDataGlobal = getDataElement(position);
             	
             }  
         });
 	}
 	@SuppressLint("NewApi")
 	protected void showPopup(View childView) {
 		// TODO Auto-generated method stub
 		PopupMenu popup = new PopupMenu(this, childView);  
         MenuInflater inflater = popup.getMenuInflater();  
         inflater.inflate(R.menu.favor_chose, popup.getMenu());  
         popup.setOnMenuItemClickListener(new OnMenuItemClickListener() {
         	 
             @Override
             //the logic to deal with the menu selection
             public boolean onMenuItemClick(MenuItem item) {
                 Toast.makeText(getBaseContext(), "You selected the action : " + item.getTitle(), Toast.LENGTH_SHORT).show();
                 switch(item.getItemId()){
                 case R.id.go_to_web:
                 	goToTheWeb();
                 	break;
                 case R.id.scan_detail:
 
                 	showScanDetails();
                 	break;
                 case R.id.send_out:
                 	;
                 	break;
                 case R.id.delete_item:
                 	deleteSelectedRecord();
                 	break;
                 default:
                 	break;
                 }
                 return true;
             }
         });
         popup.show();  
 	}
     public void goToTheWeb(){
     	QrcodeDataOperator qdo = ((MainActivity) this.getParent()).getQdo();
     	Qrcode qc = qdo.queryFromFavor(qrDataGlobal);
     	if (qc!=null){
     		StartBrowser sb = new StartBrowser(qc.getRawdata(),this);
     		sb.startBrowse();
     	}
     }
 
     public void deleteSelectedRecord(){
     	QrcodeDataOperator qdo = ((MainActivity) this.getParent()).getQdo();
     	Qrcode qc = qdo.queryFromFavor(qrDataGlobal);
     	if (qc!=null){
     		qdo.deleteFromFavorite(qrDataGlobal);
     		onResume();
     	}
     }
 
     @SuppressLint("SimpleDateFormat")
 	public void showScanDetails(){
     	String text = "";
     	SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");
    
     	QrcodeDataOperator qdo = ((MainActivity) this.getParent()).getQdo();
     	Qrcode qc = qdo.queryFromFavor(qrDataGlobal);
     	if (qc!=null)
     		text = "Rawdata: \n"+qc.getRawdata()+"\nHashcode: \n"+qc.getHashcode()+"\nTime: \n"+sdf.format(new Date(qc.getTimeStamp()));
     	Dialog dialog = new AlertDialog.Builder(this).setIcon(R.drawable.ic_launcher)
     				.setView(new ScanDetail(this,text))
     				.setTitle("Scan Details").show();
     	dialog.show();
     }
 	protected void onResume() {
 		// TODO Auto-generated method stub
 		super.onResume();
 
 		data.clear();
 
 		QrcodeDataOperator qdo = ((MainActivity) this.getParent()).getQdo();
 		List<Qrcode> qrs = qdo.queryAllFavorite();
 		for (Qrcode q : qrs){
 			addList(q.getRawdata());
 		}
 	}
 	
 	public void addList(String contents){
 		data.add(contents);
		ArrayAdapter<String> adp = new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1,getData());
		lView.setAdapter(adp);
 		
 	}
 }
