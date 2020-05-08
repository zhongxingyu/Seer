 package com.htb.cnk;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.Message;
 import android.view.KeyEvent;
 import android.widget.TextView;
 
 import com.htb.cnk.lib.DBFile;
 import com.htb.constant.Server;
 
 public class UpdateMenuActivity extends Activity {
 	final static int DOWNLOAD_DB_FAILED = -1;
 	final static int WRITE_FILE_FAILED = -2;
 	final static int DOWNLOAD_PIC_FAILED = -3;
 	final static int COPY_DB_FAILED = -4;
 	
 	final static int DOWNLOAD_THUMBNAIL = 1;
 	final static int DOWNLOAD_PIC = 2;
 	
 	final static String MY_HOST = Server.SERVER_DOMIN + "/jpeg/";
 	
 	private TextView mStateTxt;
 	private DBFile mDBFile;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.update_menu_activity);
 		mStateTxt = (TextView) findViewById(R.id.state);
 		
 		mStateTxt.setText("正在准备更新...");
 		mDBFile = new DBFile(this);
 		updateMenu();
 	}
 
 	private void updateMenu() {
 		new Thread() {
 			public void run() {
 				int ret;
 			
				ret = downloadDB(Server.DB_MENU);
 				if (ret < 0) {
 					handler.sendEmptyMessage(ret);
 					return ;
 				}
 				
 				handler.sendEmptyMessage(DOWNLOAD_THUMBNAIL);
 				ret = downloadSmallPic();
 				if (ret < 0) {
 					handler.sendEmptyMessage(ret);
 					return ;
 				}
 				
 				handler.sendEmptyMessage(DOWNLOAD_PIC);
 				ret = downloadHugePic();
 				if (ret < 0) {
 					handler.sendEmptyMessage(ret);
 					return ;
 				}
 				
 				handler.sendEmptyMessage(0);
 			}
 		}.start();
 		
 	}
 	
 	private boolean isUpdateNeed() {
 		return true;
 	}
 	
 	private int downloadDB(String serverDBPath) {
 		URL url;
 		String filePath = Environment
                 .getExternalStorageDirectory().getAbsolutePath()
                 + "/cainaoke/";
         try {
             url=new URL(Server.SERVER_DOMIN + "/" + serverDBPath);
             
             HttpURLConnection connection = (HttpURLConnection)url.openConnection();
 
             InputStream istream=connection.getInputStream();
             String filename="cnk.db";
             
             File dir=new File(filePath);
             if (!dir.exists()) {
                 dir.mkdir();
             }
             File file=new File(filePath+filename);
             file.createNewFile();
             
             OutputStream output=new FileOutputStream(file);
             byte[] buffer=new byte[1024*4];
             while (istream.read(buffer)!=-1) {
                 output.write(buffer);
             }
             output.flush();
             output.close();
             istream.close();
             if (mDBFile.copyDatabase() < 0) {
             	return COPY_DB_FAILED;
             }
             return 0;
         } catch (Exception e) {
             e.printStackTrace();
             return DOWNLOAD_DB_FAILED;
         }
 	}
 	
 	private int downloadSmallPic() {
 		int ret;
 		for (int i=0; i<10; i++) {
 			ret = downloadPic(MY_HOST+ i + ".jpg", "ldpi_" + i + ".jpg");
 			if (ret < 0) {
 				return ret;
 			}
 		}
 		return 0;
 	}
 	
 	private int downloadHugePic() {
 		int ret;
 		for (int i=0; i<10; i++) {
 			ret = downloadPic(MY_HOST+ i + ".jpg", "hdpi_" + i + ".jpg");
 			if (ret < 0) {
 				return ret;
 			}
 		}    
 		return 0;
 	}
 	
 	private int downloadPic(String src, String dest) {
 		try {  
             //////////////// 取得的是byte数组, 从byte数组生成bitmap  
             byte[] data = getImage(src);        
             if(data!=null){        
                 save(dest, data);
             }else{        
                 return DOWNLOAD_PIC_FAILED;       
             }  
             
         } catch (Exception e) {         
             e.printStackTrace();    
             return DOWNLOAD_PIC_FAILED;
         }    
 		return 0;
 	}
 	
 	public byte[] getImage(String path) throws Exception{     
         URL url = new URL(path);     
         HttpURLConnection conn = (HttpURLConnection) url.openConnection();     
         conn.setConnectTimeout(5 * 1000);     
         conn.setRequestMethod("GET");     
         InputStream inStream = conn.getInputStream();     
         if(conn.getResponseCode() == HttpURLConnection.HTTP_OK){     
             return readStream(inStream);     
         }     
         return null;     
     } 
 	
 	public int save(String fileName, byte buffer[])
 	{
 	    try {
 	        FileOutputStream outStream=this.openFileOutput(fileName, Context.MODE_PRIVATE);
 			outStream.write(buffer);
 	        outStream.close();
 	    } catch (FileNotFoundException e) {
 	        return WRITE_FILE_FAILED;
 	    }
 	    catch (IOException e){
 	        return WRITE_FILE_FAILED;
 	    }
 	    return 0;
 	}
 	
 	public static byte[] readStream(InputStream inStream) throws Exception{     
         ByteArrayOutputStream outStream = new ByteArrayOutputStream();     
         byte[] buffer = new byte[1024];     
         int len = 0;     
         while( (len=inStream.read(buffer)) != -1){     
             outStream.write(buffer, 0, len);     
         }     
         outStream.close();     
         inStream.close();     
         return outStream.toByteArray();     
     }  
 	
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 	  
 		if(keyCode == KeyEvent.KEYCODE_BACK){
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setTitle("确定退出");
 			builder.setMessage("退出将菜谱无法更新,请等待菜谱更新完毕后,系统自动退出");
 			
 			builder.setPositiveButton("退出", new DialogInterface.OnClickListener() {
 			    
 			    @Override
 				public void onClick(DialogInterface dialog, int which) {
 			    	finish();
 			    }
 			});
 			
 			builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
 			
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					 //do nothing
 				}
 			});
 			   
 			AlertDialog dialog = builder.create();
 			dialog.show();
 
 			return true;
 		} else {
 			return super.onKeyDown(keyCode, event);
 		}
 	}
 	   
 	private Handler handler = new Handler() {
 		public void handleMessage(Message msg) {
 			if (msg.what < 0) {
 				new AlertDialog.Builder(UpdateMenuActivity.this)
 				.setTitle("错误")
 				.setMessage("更新菜谱失败,错误码:" + msg.what)
 				.setPositiveButton("确定",
 						new DialogInterface.OnClickListener() {
 	
 							@Override
 							public void onClick(DialogInterface dialog,
 									int which) {
 								finish();
 							}
 						})
 				.show();
 			} else {
 				switch(msg.what) {
 					case DOWNLOAD_THUMBNAIL:
 						mStateTxt.setText("正在下载缩略图...");
 						break;
 					case DOWNLOAD_PIC:
 						mStateTxt.setText("正在下载菜品图片...");
 						break;
 					default:
 						new AlertDialog.Builder(UpdateMenuActivity.this)
 						.setMessage("菜谱已更新")
 						.setPositiveButton("确定",
 								new DialogInterface.OnClickListener() {
 
 									@Override
 									public void onClick(DialogInterface dialog,
 											int which) {
 										finish();
 									}
 								})
 						.show();
 				}
 			}
 		}
 	};
 }
