 package tice.twitterwalk.Activities;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.regex.Matcher;
 
 import tice.twitterwalk.App;
 import tice.twitterwalk.R;
 import tice.twitterwalk.HttpClient.TwitterClient;
 import tice.twitterwalk.List.ItemAdapter;
 import tice.twitterwalk.List.TweetsListActivity;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.provider.MediaStore.Images.Media;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class ShareContentActivity extends Activity {
 
 	public App _App = null;
 	
 	Uri _UploadFile = null;
 	Bitmap _bitmap = null;
 	String _PictureURL = "";
 	ProgressDialog _Progressdialog = null;
 	protected LinearLayout _Previewpanel;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	
     	_App = (App)getApplicationContext();
     	
     	super.onCreate(savedInstanceState);
     	setContentView(R.layout.sharecontent);
     	
         _Previewpanel = (LinearLayout)findViewById(R.id.PreviewPanel);
         if(_Previewpanel != null) _Previewpanel.setVisibility(View.GONE);
         
         InitInterface();
     	InitTwitterClient();
     	
     	Intent intent = getIntent();
     	String text = "";
         Bundle bundle = intent.getExtras();
         if(bundle != null){
         	
         	text = bundle.getString(Intent.EXTRA_TEXT);
             if(text != null && text.length() != 0){
             	EditText edit = (EditText)findViewById(R.id.EditText);
             	edit.setText(text);
             }
             
             _UploadFile = (Uri)bundle.get(Intent.EXTRA_STREAM);
             if (_UploadFile != null){
             	DecodeJPGFile(_UploadFile);
             }
         }
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
     	
     	super.onActivityResult(requestCode, resultCode, data);
 
         if ((requestCode == 1 || requestCode == 2) && resultCode == Activity.RESULT_OK){
 
         	try {
 	        	
         		if(requestCode == 1){
         			_UploadFile = data.getData();
         		}else if (requestCode == 2){
         			_bitmap = (Bitmap) data.getExtras().get("data");
                     ContentValues values = new ContentValues();
                     values.put(Media.TITLE, "title");
                     values.put(Media.BUCKET_ID, "upload");
                     values.put(Media.DESCRIPTION, "upload Image taken");
                     values.put(Media.MIME_TYPE, "image/jpeg");
                     Uri uri = getContentResolver().insert(Media.EXTERNAL_CONTENT_URI, values);
                     OutputStream outstream;
                     outstream = getContentResolver().openOutputStream(uri);
                     _bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outstream);
                     outstream.close();
                     _UploadFile = uri;
                     _bitmap.recycle();
                     _bitmap = null;
         		}
         		
         		DecodeJPGFile(_UploadFile);
 	
 	        } catch (FileNotFoundException e) { } catch (IOException e) { }
 	    }
     }
     
     protected void DecodeJPGFile(Uri jpguri){
     	
     	try {
     	
 	    	int samplesize = 12;
 	    	BitmapFactory.Options options;
 	    	ImageView view = (ImageView) findViewById(R.id.ImagePrview);
 	    	InputStream thePhoto = getContentResolver().openInputStream(jpguri);
 	    	InputStream testSize = getContentResolver().openInputStream(jpguri);
 	    	
 	    	options = new BitmapFactory.Options();
 	    	options.inJustDecodeBounds = true;
 	    	BitmapFactory.decodeStream(testSize, null,options);
 	    	
 	    	if(options.outHeight >= options.outHeight){
 	    		samplesize = options.outHeight / TweetsListActivity.PREVIEW_HEIGHT; 
 	    	}else{
 	    		samplesize = options.outWidth / TweetsListActivity.PREVIEW_WIDTH;
 	    	}
 	
 	    	if (_bitmap != null){
 	    		_bitmap.recycle();
 	    		_bitmap = null;
 	    	}
 	    	options.inDither = true;
 	    	options.inSampleSize = samplesize;
 	    	options.inJustDecodeBounds = false;
 	    	options.inPreferredConfig = Bitmap.Config.RGB_565;
 	    	_bitmap = BitmapFactory.decodeStream(thePhoto, null,options);
 	  	
 	    	_Previewpanel.setVisibility(View.VISIBLE);
 	    	view.setImageBitmap(_bitmap);
 	    	
 	    	thePhoto.close();
 	    	testSize.close();    	
 	    	
         } catch (FileNotFoundException e) { } catch (IOException e) { }
     }
     
     protected void InitInterface(){
     
     	Button Send = (Button)findViewById(R.id.Send);
     	EditText edit = (EditText)findViewById(R.id.EditText);
     	Button gallery = (Button)findViewById(R.id.Gallery);
     	Button closepreview = (Button)findViewById(R.id.ClosePreview);
 //    	Button upload = (Button)findViewById(R.id.Upload);
     	Button posturl = (Button)findViewById(R.id.PostURL);
 
     	if(Send != null){
          	Send.setOnClickListener(new OnClickListener(){
         		public void onClick(View v) {
 
         			Button gallery = (Button)findViewById(R.id.Gallery);
         			EditText edit = (EditText)findViewById(R.id.EditText);
         			String text = edit.getText().toString();
         			
         			if(text.length() != 0){
         				if(_UploadFile == null){
 	         				InputMethodManager inputMethodManager = (InputMethodManager) getSystemService( Context.INPUT_METHOD_SERVICE );
 	         				inputMethodManager.hideSoftInputFromWindow(edit.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS );
 	        				
 	         				if (_App._twitter != null) _App._twitter.Post_statuses_post(mHandler, 0, text);
 	
 	        				v.setEnabled(false);
 	        				edit.setEnabled(false);
 	        				if(gallery != null)	gallery.setEnabled(false);
         				}else {
         					_Progressdialog = new ProgressDialog(ShareContentActivity.this);
                         	_Progressdialog.setMessage("Uploading picture ...");
                         	_Progressdialog.setIndeterminate(true);
                         	_Progressdialog.setCancelable(false);
                         	_Progressdialog.show();
                         	
                         	Button closepreview = (Button)findViewById(R.id.ClosePreview);
 //                        	Button upload = (Button)findViewById(R.id.Upload);
                         	Button posturl = (Button)findViewById(R.id.PostURL);
                         	
                         	closepreview.setEnabled(false);
 //                        	upload.setEnabled(false);
                         	posturl.setEnabled(false);
             	        	
                         	if (_App._twitter != null) _App._twitter.Post_image(mHandler, _App._Pictureapi, _App._Username, _App._Password, _UploadFile, text);
         		          	_UploadFile = null;
         				}
         			}
         		}
          		
          	});
     	}
     	
     	if(edit != null){
     		edit.addTextChangedListener(new TextWatcher(){
 
 				public void afterTextChanged(Editable arg0) {
 					TextView hint = (TextView)findViewById(R.id.HintTextLength);
 					Button Send = (Button)findViewById(R.id.Send);
     				if(hint != null){
     					int length = 140 - arg0.toString().length();
     					if (length > 10){
     						Send.setEnabled(true);
     						hint.setHintTextColor(-8355712);	
     					}else if(length <= 10 && length >= 0 ){
     						Send.setEnabled(true);
     						hint.setHintTextColor(0x4fff0000 + 0x10000000 * (10 - length));
     					}else if(length <0){
     						hint.setHintTextColor(0xffff0000);
     						Send.setEnabled(false);
     					}
     					
     					String srt = String.format("%d",length);
     					hint.setHint(srt);
     				}
 				}
 
 				public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
 				}
 
 				public void onTextChanged(CharSequence s, int start,int before, int count) {
 				}
     		});
     	}
     	
     	if(gallery != null){
     		gallery.setOnClickListener(new OnClickListener(){
     			public void onClick(View v) {
     				String[] titles = {"Pick from gallery","Capture a picture"}; 
     	            try{
 	    				new AlertDialog.Builder(ShareContentActivity.this)
 	                    .setTitle("Insert Picture ...")
 	                    .setItems(titles, new DialogInterface.OnClickListener() {
 	                        public void onClick(DialogInterface dialog, int which) {
 	                        	if(which == 0){
 	                	            Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
 	                	            intent.setType("image/*");
 	                	            startActivityForResult(intent, 1);
 	                        	}else{
 	                                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
 	                                startActivityForResult(intent,2);
 	                        		
 	                        	}
 	                        }
 	                    })
 	                    .show();
     	            }catch (Exception err){}
     			}
         	});
     	}
     	
     	if(closepreview != null){
     		closepreview.setOnClickListener(new OnClickListener(){
     			public void onClick(View v) {
     	        	ImageView view = (ImageView) findViewById(R.id.ImagePrview);
     	        	view.setImageBitmap(null);
     	        	_Previewpanel.setVisibility(View.GONE);
     	        	_PictureURL = "";
     	        	if (_bitmap != null){
     	        		_bitmap.recycle();
     	        		_bitmap = null;
     	        	}
     			}
         	});
     	}
     	
     	if(posturl != null){
     		posturl.setOnClickListener(new OnClickListener(){
     			public void onClick(View v) {
                 	EditText edit = (EditText)findViewById(R.id.EditText);
                 	String str = edit.getEditableText().toString();
         			if(_PictureURL.length() != 0){
         				edit.setText(_PictureURL + " " + str);
         			}
     			}
         	});
     	}
     	
 //    	if(upload != null){
 //    		upload.setOnClickListener(new OnClickListener(){
 //    			public void onClick(View v) {
 //    	        	if (_UploadFile == null) return;
 //	    	       	
 //                	_Progressdialog = new ProgressDialog(ShareContentActivity.this);
 //                	_Progressdialog.setMessage("Uploading picture ...");
 //                	_Progressdialog.setIndeterminate(true);
 //                	_Progressdialog.setCancelable(false);
 //                	_Progressdialog.show();
 //                	
 //                	Button closepreview = (Button)findViewById(R.id.ClosePreview);
 //                	Button upload = (Button)findViewById(R.id.Upload);
 //                	Button posturl = (Button)findViewById(R.id.PostURL);
 //                	
 //                	closepreview.setEnabled(false);
 //                	upload.setEnabled(false);
 //                	posturl.setEnabled(false);
 //    	        	
 //                	if (_App._twitter != null) _App._twitter.Post_image(mHandler, _App._Pictureapi, _App._Username, _App._Password, _UploadFile);
 //    			}
 //        	});
 //    	}
     }
     
     protected void InitTwitterClient(){
 
         _App.LoadSettings(this);
         
         int port;
         String url,searchurl;
         
         if(_App._Https == true){
         	url = String.format("https://%s", _App._Baseapi);
         	searchurl = String.format("https://%s", _App._Searchapi);
         	port = 443;
         }else{
         	url = String.format("http://%s", _App._Baseapi);
         	searchurl = String.format("http://%s", _App._Searchapi);
         	port = 80;
         }
         
         URI host = null;
 		try {
 			host = new URI(url);
 	        
 			if (_App._twitter != null) _App._twitter = new TwitterClient(this, host.getHost(), port, url, searchurl, _App._Username, _App._Password);
 	        
 		} catch (URISyntaxException e) {
 			Bundle err = new Bundle();
 			err.putString(TwitterClient.KEY, e.getLocalizedMessage());
 			TwitterClient.SendMessage(mHandler, TwitterClient.HTTP_ERROR, err);
 		} 
     }
 
 
     private final Handler mHandler = new Handler() {
         @Override
          public void handleMessage(final Message msg) {
         	processMessage(msg);
         }
     }; 
     
     
     public void processMessage(Message msg) {
 
     	String ErrorMsg;
 
     	switch (msg.what){
 	    	case TwitterClient.HTTP_POSTIMAGE_ERROR:
 			{
 				if (_Progressdialog.isShowing()) _Progressdialog.dismiss();
 	        	Button closepreview = (Button)findViewById(R.id.ClosePreview);
//	        	Button upload = (Button)findViewById(R.id.Upload);
 	        	Button posturl = (Button)findViewById(R.id.PostURL);
 	        	closepreview.setEnabled(true);
//	        	upload.setEnabled(true);
 	        	posturl.setEnabled(true);
 	        	ErrorMsg = msg.getData().getString(TwitterClient.KEY);
 	        	try{
 	        		new AlertDialog.Builder(this)
 	        		.setTitle("Error")
 	        		.setMessage(ErrorMsg)
 	        		.setNegativeButton("OK", new DialogInterface.OnClickListener() {
 	        			public void onClick(DialogInterface dialog, int whichButton) {
 	        			}
 	        		})
 	        		.show();
 	        	}catch (Exception err){}
 				break;
 			}
 			case TwitterClient.HTTP_POSTIMAGE_SUCCESSFUL:
 				{
 					if (_Progressdialog.isShowing()) _Progressdialog.dismiss();
 		        	Button closepreview = (Button)findViewById(R.id.ClosePreview);
//		        	Button upload = (Button)findViewById(R.id.Upload);
 		        	Button posturl = (Button)findViewById(R.id.PostURL);
 		        	closepreview.setEnabled(true);
//		        	upload.setEnabled(true);
 		        	posturl.setEnabled(true);
 		
 		        	EditText edit = (EditText)findViewById(R.id.EditText);
 		        	String str = edit.getEditableText().toString();
 		        	String response = msg.getData().getString(TwitterClient.KEY);
 		        	
 					Matcher m1 = ItemAdapter.p1.matcher(response);
 		        	
 					while(m1.find()){ 
 						_PictureURL = m1.group();
 						break;
 					}
 					
 					if(_PictureURL.length() != 0){
 						edit.setText(_PictureURL + " " + str);
 					}
 					break;
 				}
 			case TwitterClient.HTTP_ERROR:
 				{
 					Button Send = (Button)findViewById(R.id.Send);
 					Send.setEnabled(true);
 					EditText edit = (EditText)findViewById(R.id.EditText);
 					edit.setEnabled(true);
 					Button gallery = (Button)findViewById(R.id.Gallery);
 					if(gallery != null) gallery.setEnabled(true);
 					ErrorMsg = msg.getData().getString(TwitterClient.KEY);
 					try{
 				        new AlertDialog.Builder(this)
 				        .setTitle("Error")
 				        .setMessage(ErrorMsg)
 				        .setNegativeButton("OK", new DialogInterface.OnClickListener() {
 				            public void onClick(DialogInterface dialog, int whichButton) {
 				            }
 				        })
 				        .show();
 					}catch (Exception err){}
 					break;
 				}
 	    	case TwitterClient.HTTP_STATUSES_UPDATE:
 	    		if (_bitmap != null) _bitmap.recycle();
                 _bitmap = null;
 				finish();
 				break;
     	}
     }
 }
