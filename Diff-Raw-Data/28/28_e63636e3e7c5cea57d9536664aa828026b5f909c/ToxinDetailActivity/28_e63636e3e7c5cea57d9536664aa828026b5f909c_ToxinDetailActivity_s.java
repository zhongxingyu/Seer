 package net.healthroad.harmful.activity;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.text.Html;
 import android.util.Log;
 import android.view.MenuItem;
 import android.view.View;
 import android.webkit.WebView;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 import net.healthroad.harmful.R;
 import net.healthroad.harmful.util.ICommonCodes;
 import net.healthroad.harmful.util.Toxin;
 import net.healthroad.harmful.util.ToxinDBAdapter;
 
 public class ToxinDetailActivity extends Activity {
 
     public static String TAG = "Harmful";
     private int dIdx = -1;
 
     /** 영문제목 */
     private static TextView textEngTitle;
     /** 한글제목 */
     private static TextView textKorTitle;
     /** 내용출력용 뷰 */
    private static TextView textToxinDetail;
     /** 상단 이미지 */
     private static ImageView imageMark;
     /** SQLite 어뎁터 */
     private ToxinDBAdapter toxinDBAdapter;
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.activity_toxin_detail);
 
         // 화면 컴포넌트 초기화
         this.initScreenComponents();
 
         // 액션바 초기화
         this.initActionBar();
 
         toxinDBAdapter = new ToxinDBAdapter(getApplicationContext());
         toxinDBAdapter.open();
     }
 
     @Override
     protected void onResume() {
         super.onResume();
 
         // 번들에서 값 추출
         dIdx = this.getIntent().getIntExtra(ICommonCodes.TOXIN_DETAIL_BUNDLE_KEY, -1);
         Log.d(TAG, "번들에서 받은 값:" + dIdx);
 
         // 데이터 읽기
         loadData(dIdx);
     }
 
     @Override
     protected void onDestroy() {
         toxinDBAdapter.close();
         super.onDestroy();
     }
 
     /**
      * 화면 컴포넌트 초기화를 수행한다.
      */
     private void initScreenComponents() {
         textEngTitle = (TextView) findViewById(R.id.text_eng_title);
         textKorTitle = (TextView) findViewById(R.id.text_kor_title);
        textToxinDetail = (TextView) findViewById(R.id.textview_toxin_detail);
 
         imageMark = (ImageView) findViewById(R.id.image_mark);
         imageMark.setOnClickListener(new View.OnClickListener() {
 
             @Override
             public void onClick(View view) {
                 Intent intent = new Intent();
                 intent.setClass(ToxinDetailActivity.this, GuideActivity.class);
                 startActivity(intent);
             }
         });
     }
 
     /**
      * 액션바를 초기화 설정한다.
      */
     private void initActionBar() {
         // 액션바 생성 후, 텍스트 타이틀 제거
         ActionBar bar = getActionBar();
 
         // 커스터마이징 액션바 사용
         bar.setCustomView(R.layout.g_actionbar);
         bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_CUSTOM);
 
         // 타이틀 생성
         TextView tv = (TextView) bar.getCustomView().findViewById(R.id.text_g_title);
         tv.setText("상세정보");
 
         // 메인으로 이동 버튼
         Button buttonHome = (Button) bar.getCustomView().findViewById(R.id.button_home);
         buttonHome.setOnClickListener(new View.OnClickListener() {
 
             @Override
             public void onClick(View view) {
                 int vId = view.getId();
                 switch (vId) {
                     case R.id.button_home:
                         Intent hIntent = new Intent();
                         hIntent.setClass(getApplicationContext(), SplashActivity.class);
                         startActivity(hIntent);
 
                         finish();
                         overridePendingTransition(R.anim.push_left_in, R.anim.push_up_out);
                         break;
                     default:
                         break;
                 }
             }
         });
 
         // 모드 설정
         bar.setDisplayHomeAsUpEnabled(true);
     }
 
     /**
      * 옵션 메뉴가 선택되면 이벤트가 호출된다.
      *
      * @param item
      * @return
      */
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         int mId = item.getItemId();
         Log.d(TAG, "선택된 메뉴 아이디:" + mId + "<END");
         switch (mId) {
             case android.R.id.home:
                 Log.d("nine", "홈 버튼이 눌림");
                 finish();
                 overridePendingTransition(R.anim.push_left_in, R.anim.push_up_out);
                 break;
             default:
                 break;
         }
         return super.onOptionsItemSelected(item);
     }
 
     /**
      * 데이터를 읽어온다.
      * @param idx
      */
     private void loadData(int idx) {
         Cursor dataCursor = null;
 
         dataCursor = toxinDBAdapter.fetchToxins(idx);
         if (dataCursor != null) {
             if (dataCursor.moveToFirst()) {
                 do {
                     int rowid = dataCursor.getInt(dataCursor.getColumnIndex("rowid"));
                     String engData = dataCursor.getString(dataCursor.getColumnIndex("eng"));
                     String korData = dataCursor.getString(dataCursor.getColumnIndex("kor"));
                     String keywordData = dataCursor.getString(dataCursor.getColumnIndex("keyword"));
                     String contentsData = dataCursor.getString(dataCursor.getColumnIndex("contents"));
                     String imgs = dataCursor.getString(dataCursor.getColumnIndex("imgs"));
 
                     Log.d(TAG, rowid + ">" + imgs + ":" + engData + ":" + korData + ":" + keywordData + "::");
 
                     textEngTitle.setText(engData);
                     textKorTitle.setText(korData);
                    textToxinDetail.setText(Html.fromHtml(contentsData));

                    String uri = "drawable/" + imgs;
                    int imageResource = getResources().getIdentifier(uri, null, getPackageName());
                    Drawable image = getResources().getDrawable(imageResource);
                    imageMark.setImageDrawable(image);

                 } while (dataCursor.moveToNext());
             }
             dataCursor.close();
         }
     }
 }
