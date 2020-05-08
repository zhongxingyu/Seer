 package project.DirectionOfPhoto;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Environment;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 import android.widget.Toast;
 
 public class DirectionOfPhotoActivity extends Activity {
 
 	/** Called when the activity is first created. */
 	private String dict;
 	private List<String> imageAddrs;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		if (!Environment.getExternalStorageState().equals(
 				android.os.Environment.MEDIA_MOUNTED))
 			Toast.makeText(this, "SDcard不存在", Toast.LENGTH_LONG).show();
 		else {
 			dict = getpath() + File.separator + "Pictures/";
 			if (getPictures(dict).isEmpty())
 				Toast.makeText(this, "沒有合適的檔案", Toast.LENGTH_LONG).show();
 			else {
 			imageAddrs = getPictures(dict);
 
 				/*
 				 * Bitmap bm = BitmapFactory.decodeFile(imageAddrs.get(0));
 				 * iv.setImageBitmap(bm);
 				 */
 				ImageAdater a = new ImageAdater(this, R.layout.item, imageAddrs);
 				ListView lv = (ListView) findViewById(R.id.list);
 				lv.setAdapter(a);
 				// 設定LISTENER
 				lv.setOnItemClickListener(new OnItemClickListener() {
 
					@Override
 					public void onItemClick(AdapterView<?> arg0, View arg1,
 							int arg2, long arg3) {
 
 						Toast.makeText(DirectionOfPhotoActivity.this,
 								String.valueOf(arg2), Toast.LENGTH_SHORT)
 								.show();
 
 						Intent myIntent = new Intent(
 								DirectionOfPhotoActivity.this,
 								MapActivity.class);
 						Bundle bundle = new Bundle();
 						bundle.putString("Image", imageAddrs.get(arg2));
 						myIntent.putExtras(bundle);
 						startActivity(myIntent);
 					}
 				});
 			}
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.mainmenu, menu);
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		switch (item.getItemId()) {
 		case R.id.about:
 
 			builder.setTitle("About").setMessage("Version:0.1\n")
 					.setPositiveButton("確定", null).show();
 
 			break;
 		case R.id.quit:
 			builder.setTitle("確認")
 					.setMessage("確認結束此程式")
 					.setNegativeButton("取消", null)
 					.setPositiveButton("確定",
 							new DialogInterface.OnClickListener() {
								@Override
 								public void onClick(DialogInterface dialog,
 										int which) {
 									finish();
 								}
 							}).show();
 			break;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	// 取得SDcard的路徑
 	public String getpath() {
 		File sdDir = null;
 		boolean sdCardExist = Environment.getExternalStorageState().equals(
 				android.os.Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
 		if (sdCardExist) {
 			sdDir = Environment.getExternalStorageDirectory();// 获取跟目录
 			return sdDir.toString();
 		}
 		return "false";
 		
 	}
 
 	// 獲取某目錄底下的圖片路徑
 	public List<String> getPictures(final String strPath) {
 		List<String> list = new ArrayList<String>();
 
 		File file = new File(strPath);
 		File[] files = file.listFiles();
 
 		if (files == null) {
 			return null;
 		}
 
 		for (int i = 0; i < files.length; i++) {
 			final File f = files[i];
 			if (f.isFile()) {
 				try {
 					int idx = f.getPath().lastIndexOf(".");
 					if (idx <= 0) {
 						continue;
 					}
 					String suffix = f.getPath().substring(idx);
 					if (suffix.toLowerCase().equals(".jpg")
 							|| suffix.toLowerCase().equals(".jpeg")) {
 						// || suffix.toLowerCase().equals(".bmp")
 						// || suffix.toLowerCase().equals(".png")
 						// || suffix.toLowerCase().equals(".gif")) {
 						list.add(f.getPath());
 					}
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}
 
 		return list;
 	}
 }
