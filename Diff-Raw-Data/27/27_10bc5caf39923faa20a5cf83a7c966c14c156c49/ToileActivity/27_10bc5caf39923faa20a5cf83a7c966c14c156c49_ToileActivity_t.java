 package to.i.le;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
import android.view.WindowManager;
 import android.widget.AbsListView;
 import android.widget.AbsListView.OnScrollListener;
 import android.widget.ArrayAdapter;
 import android.widget.ImageButton;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 
 public class ToileActivity extends Activity {
    private ButtonClickListener click_listener;
    private ToiletSounds toilet_sounds;
    private ListView paper_view;
    private List<String> paper_list;
    static ArrayList<String> untiku_array = new ArrayList<String>();
    String[] str;
    String FILE_NAME = Start.FILE_NAME;
    SharedPreferences sp;
    LinearLayout cover;
    LinearLayout main;
    int tic;
    View hiki_view;
    
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         sp = getSharedPreferences(FILE_NAME,MODE_PRIVATE);
         cover = (LinearLayout)findViewById(R.id.cover);
         main = (LinearLayout)findViewById(R.id.main);
         set();
               
     	 // 音声ファイルを読み込む
         toilet_sounds = new ToiletSounds(ToileActivity.this);
         toilet_sounds.load_sound_file();
         
         click_listener = new ButtonClickListener();
         
         ImageButton btn1 = (ImageButton)findViewById(R.id.btn1);
         btn1.setOnClickListener(click_listener);
         
         ImageButton btn2 = (ImageButton)findViewById(R.id.btn2);
         btn2.setOnClickListener(click_listener);
         
         ImageButton btn3 = (ImageButton)findViewById(R.id.btn3);
         btn3.setOnClickListener(click_listener);
         
         ImageButton btn4 = (ImageButton)findViewById(R.id.btn4);
         btn4.setOnClickListener(click_listener);

         // トイレットペーパーを生成する
         paper_view = (ListView)findViewById(R.id.paperView);
        paper_list = new ArrayList<String>();;
         
         paper_view.setAdapter(new ArrayAdapter<String>(this, R.layout.list, R.id.row_textview1, paper_list) {            
             @Override
             public View getView(int position, View convertView, ViewGroup parent) {
                 hiki_view = super.getView(position, convertView, parent);
               if (position < tic - 1) {
                   hiki_view.setBackgroundColor(Color.WHITE);
               } else {
                   hiki_view.setBackgroundColor(Color.TRANSPARENT);
                 }
                 return hiki_view;
             }   
         });
 
         paper_view.setOnScrollListener(new ListListener());
         
         //NOTE: スクロール中の背景表示をやめる。
         paper_view.setScrollingCacheEnabled(false);
         
         add_paper();
         
         paper_view.setSelection(paper_list.size());
     }
     
     @Override
     protected void onResume() {
         toilet_sounds.load_sound_file();
         super.onResume();
     }
 
     @Override
     protected void onPause() {
         toilet_sounds.unload_sound_file();
         super.onPause();
     }
     
     @Override
     protected void onDestroy() {
         toilet_sounds.unload_sound_file();
         super.onDestroy();
     }
    
    
     private void add_paper() {
     	if (!paper_list.isEmpty()){ 
     		Collections.shuffle(untiku_array);
    		paper_list.add(0, "\n\n\n"+untiku_array.get(0)+"\n\n\n");
     	}
     	for (int i=0; i<4; i++) {
     		paper_list.add(0, "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
     	}
     }
     
     // ボタン押したときの処理を記述
     class ButtonClickListener implements OnClickListener{
 
         public void onClick(View v){
             switch(v.getId()){
             case R.id.btn1:
                 toilet_sounds.play(ToiletSounds.type.JAVAA);
                 break;
             case R.id.btn2:
                 toilet_sounds.play(ToiletSounds.type.PIYO);
                 break;
             case R.id.btn3:
                 toilet_sounds.play(ToiletSounds.type.CHARAN);
                 break;
             case R.id.btn4:
                 toilet_sounds.play(ToiletSounds.type.SING);
                 break;
             }
         }
     }
     
     class ListListener implements OnScrollListener{
     	
     	public void onScroll(AbsListView view, int firstVisibleItem,
     			int visibleItemCount, int totalItemCount) {
 
     	    tic = totalItemCount;
     		if (paper_list.size() >= 1000){
         		//TODO: あたりなどのオチをランダムにしたい
     			paper_list.add(0, "あたり");
     		}else if (firstVisibleItem == 0) {
         		//TODO: 円環リストビューにしたい
     			add_paper();
     			paper_view.invalidateViews();
     			paper_view.setSelectionFromTop(5, 0);
     		}
     	}
 
     	public void onScrollStateChanged(AbsListView view, int scrollState) {
     	}
     }
     
     void set() {
         // カバーの初期設定
         switch(sp.getInt("COVER",0)) {
         case 0:
         	cover.setBackgroundResource(R.drawable.cv00);
         	break;
         case 1:
 			cover.setBackgroundResource(R.drawable.cv01);
 			break;
         case 2:
 			cover.setBackgroundResource(R.drawable.cv02);			
 			break;
         case 3:
 			cover.setBackgroundResource(R.drawable.cv03);			
 			break;
         }
 	
         // 背景の初期設定
         switch(sp.getInt("BACKGROUND",0)) {
         case 0:
           main.setBackgroundResource(R.drawable.bg00);
           break;
         case 1:
           main.setBackgroundResource(R.drawable.bg01);
           break;
         case 2:
           main.setBackgroundResource(R.drawable.bg02);
           break;
         case 3:
           main.setBackgroundResource(R.drawable.bg03);
           break;
         }
     }
     
 	protected void onActivityResult(int reqcode, int rescode, Intent data) {
         set();
 	}
     
     // メニュー
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         menu.add(Menu.NONE, 0, Menu.NONE, "使い方");
         menu.add(Menu.NONE, 1, Menu.NONE, "模様替え");
         return super.onCreateOptionsMenu(menu);
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case 0:
         	Intent Info = new Intent(getApplicationContext(),Info.class);
         	Info.putExtra("FROM", "From_ToileActivity");
         	startActivity(Info);
         	break;
         case 1:
         	Intent Rede = new Intent(getApplicationContext(),Redecorate.class);
         	startActivityForResult(Rede,0);
         	break;
         }
         return true;
     }
 }
