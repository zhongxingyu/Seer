 package com.memelabs.iqratrack;
 
 import java.util.ArrayList;
 
 import org.arabic.ArabicUtilities;
 
 import android.os.Bundle;
 import android.view.Gravity;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class SuraContentAct extends BaseActivity {
 
 	protected TextView employeeNameText;
 	protected TextView titleText;
 	protected ArrayList<String> ayas;
 	protected String[] ayareadcount;
     protected SuraContentArrayAdapter adapter;
     protected int suraidx;
     protected int employeeId;
     protected int managerId;
     protected QuranReader qr;
 	protected Tracker trk;
     protected ListView list;
     protected IqraTrackApp app;
     protected SuraEntity sura;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.sura_content_main);
         app = (IqraTrackApp) getApplicationContext();
         qr = app.getQr();
         trk = app.getTracker();
         suraidx = getIntent().getIntExtra("suraidx", 0);
         app.setCurrentSura(qr.getSuraData(suraidx));
         ayas = qr.getSuraContentsInArrayList(suraidx);
         sura = app.getCurrentSura();
         displaySuraInfo();
         if (sura.getIdx()!=1&&sura.getIdx()!=9){
         	displayBismillah();
         }
         adapter = new SuraContentArrayAdapter(this, R.layout.sura_content_item,ayas,sura);
 		list = (ListView) findViewById(R.id.listaya);
 		list.setAdapter(adapter);
         list.setOnItemClickListener(new OnItemClickListener() {
         	
         	public void onItemClick(AdapterView<?> arg0, View view, int position, long index) {
         	
         		onListItemClick(list, view, position, index);
             }
 		});
         scrollToLastRead();
 
     }
 
     protected void displaySuraInfo(){
     	TextView tx = (TextView) findViewById(R.id.surameta);
     	tx.setGravity(Gravity.CENTER);
     	String surainfo;
     	surainfo = sura.getEname()+" "+sura.getTname()+" "+sura.getAyas()+" ayat";
         tx.setText(surainfo);
     }
 
 	private void displayBismillah() {
 		TextView tx = (TextView) findViewById(R.id.surabismillah);
 		tx = ArabicUtilities.getArabicEnabledTextView(getApplicationContext(), tx);
         String bism = ArabicUtilities.reshapeSentence(QuranReader.BISMILLAH); 
 		tx.setText(bism);
 	}
 
 	protected void onListItemClick(ListView list, View view, int position,
 			long index) {
 		int ayapos = app.getCurrentSura().getStart()+position;
		trk.getAyasReadCount()[ayapos]= trk.getAyasReadCount()[ayapos] + 1;
 		TextView tAyaSt = (TextView) view.findViewById(R.id.ayareadstatus);
 		tAyaSt.setText("\u2713");
 	}
 	
     protected void scrollToLastRead(){
     	
 		for (int i=0;i<=ayas.size();i++) {
 			int ayapos = app.getCurrentSura().getStart()+i;
 			int ayaReadStatus = Integer.parseInt(app.getTracker().getAyasReadCount()[ayapos]);
 			if (ayaReadStatus==0){
 				switch (i) {
 				case 0:
 					list.setSelection(i);
 					break;
 				default:
 					list.setSelection(i-1);
 					break;
 				}
 				break;
 			}
 		}
     }
 }
