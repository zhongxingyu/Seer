 package org.geekhub.cherkassy.fragments;
 
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 
 import org.geekhub.cherkassy.R;
 import org.geekhub.cherkassy.activity.MapActivity;
 import org.geekhub.cherkassy.email.MailSenderClass;
 import org.geekhub.cherkassy.objects.Const;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.ContentValues;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.BitmapDrawable;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockFragment;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 
 public class ReportFragment extends SherlockFragment implements OnClickListener{
 	private static final int SELECT_CAMERA = 101;
 	private static final int SELECT_GALERY = 102;
 	private static final int SELECT_MAP = 103;
 	private ImageView imgPhoto;
 	private TextView txtLatLng;
 	private Button btnSend, btnOpenMap;
 	private EditText edtDescription;
 	private SherlockFragmentActivity actReport;
 	private Uri imageUri;
 	private Double lat, lng;
 	private AlertDialog.Builder ad;
 	private String attachFilePath;
 	private SharedPreferences nickPref;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setHasOptionsMenu(true);
 		attachFilePath = "";
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		btnSend.setOnClickListener(this);
 		btnOpenMap.setOnClickListener(this);
 		imgPhoto.setOnClickListener(this);
 		actReport = getSherlockActivity();
 
 		//txtLatLng.setOnClickListener(this);
 	}
 
 	@Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		View v = inflater.inflate(R.layout.report_frag,container,false);
 		imgPhoto = (ImageView)v.findViewById(R.id.imgPhoto);
 		btnSend  = (Button)v.findViewById(R.id.btnSend);
 		btnOpenMap  = (Button)v.findViewById(R.id.btnOpenMap);
 		txtLatLng = (TextView)v.findViewById(R.id.txtLatLng);
 		edtDescription = (EditText)v.findViewById(R.id.edtDescription);
         return v;
     }
 	
 	
 //	String aEmailList[] = { "chebTS@gmail.com" }; 
 //	Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND); //This is the email intent
 //	emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, aEmailList); // adds the address to the intent
 //	emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Cherkassy issue");//the subject				 
 //	emailIntent.setType("plain/text");				 
 //	emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "On location " + txtLatLng.getText().toString()+
 //			"\nThere is an issue \n"
 //			+ edtDescription.getText().toString()
 //			); 
 //	startActivity(emailIntent);
 	//http://stackoverflow.com/questions/2020088/sending-email-in-android-using-javamail-api-without-using-the-default-built-in-a/2033124#2033124
 	
 
 	private void sendReport(){        
     	new Thread(new Runnable() {				
 			@Override
 			public void run() {
 				String from, where, title, text, attach;
 				from = "chebTS@gmail.com";
 				where = "chebTS@gmail.com";		
 				title = "Cherkassy report";
 				nickPref = getSherlockActivity().getSharedPreferences(Const.NICK_TAG, 0);
 				text = "On location " 
 						+ txtLatLng.getText().toString()
 						+"\nThere is an issue \n"
 						+ edtDescription.getText().toString()
 						+"\n\nReported by "+ nickPref.getString(Const.NICK_TAG, "Unknown");
 				
 				attach = attachFilePath;
 				try {
 					MailSenderClass sender = new MailSenderClass("CkGuideGeek@gmail.com", "CK2Android");
 					sender.sendMail(title, text, from, where, attach);
 					
 				} catch (Exception e) {
 					e.printStackTrace();
 				}					
 			}
 		}).start();
     	Toast.makeText(getSherlockActivity(), getResources().getString(R.string.report_sended), Toast.LENGTH_LONG).show();
     	getSherlockActivity().finish();
 	}
 	
 	
 	
 	@Override
 	public void onClick(View v) {
 		switch (v.getId()) {
 		case R.id.btnSend:	
 			if ((lat==null)||(lng==null)){
 				Toast.makeText(getSherlockActivity(), "Please set issue location", Toast.LENGTH_LONG).show();
 			}else{
 				if (isConnectingToInternet(getSherlockActivity())){
 					sendReport();
 				}else{
 					Toast.makeText(getSherlockActivity(), getResources().getString(R.string.no_internet), Toast.LENGTH_LONG).show();
 				}
 			}
 			break;
 		case R.id.imgPhoto:
 			showPhotoDialog();
 			break;
 		case R.id.btnOpenMap:
 			startActivityForResult(new Intent(getSherlockActivity(), MapActivity.class), SELECT_MAP);
 			break;						
 		default:
 			break;
 		}
 	}	
 	
 	private void showPhotoDialog(){
 		ad = new AlertDialog.Builder(getSherlockActivity());
		ad.setTitle("Choose source");  // 
		ad.setMessage("Camera or gallery"); // 
 		ad.setCancelable(true);
 		ad.setPositiveButton("Camera", new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				openCamera();
 			}
 		});
 		ad.setNeutralButton("Gallery", new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				openGallery();
 			}
 		});
 		ad.show();
 		
 	}
 	
 	public static boolean isConnectingToInternet(Activity activity){
         ConnectivityManager connectivity = (ConnectivityManager) activity.getSystemService(Activity.CONNECTIVITY_SERVICE);
           if (connectivity != null){
               NetworkInfo[] info = connectivity.getAllNetworkInfo();
               if (info != null){
                   for (int i = 0; i < info.length; i++){
                       if (info[i].getState() == NetworkInfo.State.CONNECTED){
                           return true;
                       }
                   }
               }
           }
           return false;
     }
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 			case R.id.map:
 				startActivityForResult(new Intent(getSherlockActivity(), MapActivity.class), SELECT_MAP);
 				break;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 	
 	private void openGallery(){
 		Log.i("Opt","Gallery");
 		Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
 		photoPickerIntent.setType("image/*");
 		startActivityForResult(photoPickerIntent, SELECT_GALERY); 
 	}
 	
 	private void openCamera(){
 		Log.i("Opt","Camera");
 		Log.d("ANDRO_CAMERA", "Starting camera on the phone...");
         String fileName = "testphoto.jpg";
         ContentValues values = new ContentValues();
         values.put(MediaStore.Images.Media.TITLE, fileName);
         values.put(MediaStore.Images.Media.DESCRIPTION,
                 "Image capture by camera");
         values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
         imageUri = actReport.getContentResolver().insert( MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
         Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
         intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
         intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
         startActivityForResult(intent, SELECT_CAMERA);
 	}
 
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		inflater.inflate(R.menu.report_menu,menu);
 		super.onCreateOptionsMenu(menu, inflater);
 	}
 	
 	
 	
 //	@Override
 //	public void onDestroy() {
 //		Log.i("Log","onDestroy");
 //		super.onDestroy();
 //	}
 
 //	@Override
 //	public void onDestroyView() {
 //		super.onDestroyView();
 //		if (((BitmapDrawable)imgPhoto.getDrawable())!= null){
 //			(((BitmapDrawable)imgPhoto.getDrawable()).getBitmap()).recycle();			
 //		}
 //		//Log.i("Log","onDestroyView");
 //	}
 
 	
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		switch(requestCode) { 
 	    case SELECT_GALERY:
 	        if(resultCode == Activity.RESULT_OK){  	        	
 	        	setImage(data.getData());	        	
 	        }
 	        break;
 	    case SELECT_CAMERA:
 	    	if(resultCode == Activity.RESULT_OK){ 	    		
 	    		setImage(imageUri);	    		
 	    	}
 	    	break;	
 	    case SELECT_MAP:
 	    	Log.i("Select map result","__");
 	    	
 	    	if (data!=null){
 	    		if(data.hasExtra("lat")&&(data.hasExtra("lng"))){
 	    			lat = data.getExtras().getDouble("lat");
 	    			lng = data.getExtras().getDouble("lng");
 	    			txtLatLng.setText(lat.toString() + " : " +lng.toString());
 	    		}
 	    	}
 	    	break;
 	    }		
 	}
 	
 	private String getRealPathFromURI(Uri contentUri) {
         // can post image
         String [] proj={MediaStore.Images.Media.DATA};
         Cursor cursor = getSherlockActivity().managedQuery( contentUri,
                         proj, // Which columns to return
                         null,       // WHERE clause; which rows to return (all rows)
                         null,       // WHERE clause selection arguments (none)
                         null); // Order-by clause (ascending by name)
         int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
         cursor.moveToFirst();
         return cursor.getString(column_index);
 	}
 	
 	private Boolean setImage(Uri uri){
 		Log.i("Image URI", uri.toString());
 		attachFilePath = getRealPathFromURI(uri);
 		//Log.i("Image File path", getRealPathFromURI(uri));
 		try{
 			if (((BitmapDrawable)imgPhoto.getDrawable())!= null)
 					(((BitmapDrawable)imgPhoto.getDrawable()).getBitmap()).recycle();
     		InputStream imageStream = getSherlockActivity().getContentResolver().openInputStream(uri);
             Bitmap bmp = BitmapFactory.decodeStream(imageStream);
             int width = bmp.getWidth();
             int height = bmp.getHeight();
             float k;
             if (width> height){
             	if (width> 2048){
             		k = width / height;
             		width = 2048;
             		height = (int)(2048 / k);
             		imgPhoto.setImageBitmap(Bitmap.createScaledBitmap(bmp, width, height, false));
             	}else{
             		imgPhoto.setImageBitmap(bmp);
             	}
             }else{
             	if (height > 2048){
             		k = height / width;
             		height = 2048;
             		width = (int)(2048 / k);
             		imgPhoto.setImageBitmap(Bitmap.createScaledBitmap(bmp, width, height, false));
             	}else{
             		imgPhoto.setImageBitmap(bmp);
             	}
             }
             //imgInit = true;
             return true;
 		}catch (FileNotFoundException e) {
 			Log.i("FileNotFound","FileNotFound");
 			e.printStackTrace();
 			return false;
 		}
 	}
 }
