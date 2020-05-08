 package com.trustripes.principal;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.util.HashMap;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.StatusLine;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.ByteArrayBody;
 import org.apache.http.entity.mime.content.StringBody;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.protocol.HttpContext;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.google.analytics.tracking.android.EasyTracker;
 import com.google.zxing.client.android.CaptureActivity;
 import com.trustripes.Constants.ConstantValues;
 
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.AsyncTask.Status;
 import android.provider.MediaStore;
 import android.provider.MediaStore.Images.Media;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.Bitmap.CompressFormat;
 import android.graphics.BitmapFactory;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.View;
 import android.view.Window;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class Register extends Activity {
 
 	/* Variable for internal debug control */
 	/* ERASE FOR PRODUCTION */
 	private static final String TAG = Register.class.getSimpleName();
 	
 	private final int CAMERA_RESULT = 200;
 	private final int GALLERY_RESULT = 300;
 	
 	/* Declaration of UI widgets */
 	private TextView textCode;
 	private Button btn_again, btn_register, backButton;
 	private EditText registerName;
 	private ImageView photo;
 	private ProgressDialog dialog;
 	private Spinner spinner;
 	
 	/* Declaration of variable for session control */
 	private SharedPreferences session;
 	
 	/* String values for JSON response */
 	private String productName = "";	
 	private String userSession = "";
 	private String code = "";
	private String lastfinalImagePath= "", finalImagePath = "";
 	private Bitmap bitmap = null;
 	private String countryCode;
 	private boolean uploading = false;
 	
 	HashMap<String, String> myCountryMap;
 	Registerback registerBack;
 	
 	private SharedPreferences developmentSession = null;
 	String id;
 	int realId;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.activity_register);
 		
 		/* Get previous session data */
 		session = getSharedPreferences(ConstantValues.USER_DATA, MODE_PRIVATE);
 		
 		/* Get User Id stored in SharedPreferences */
 		userSession = session.getString("user_id", "No");
 		
 		/* Instantiation of UI widgets */
 		registerName = (EditText) findViewById(R.id.register_edittext_name);
 		btn_again = (Button) findViewById(R.id.register_button_again);
 		btn_register = (Button) findViewById(R.id.register_button_register);
 		backButton = (Button) findViewById(R.id.backButton);
 		textCode = (TextView) findViewById(R.id.register_textview_code);
 		photo = (ImageView) findViewById(R.id.register_imageview_add);
 		
 		spinner = (Spinner) findViewById(R.id.register_spinner_country);
 		
 		String savedProductName = getIntent().getStringExtra("PRODUCT_NAME");
 		
 		if(!(savedProductName.equals("null"))){
 			registerName.setText(savedProductName);
 		}
 		
 		registerBack = new Registerback();
 		
 		if(savedInstanceState != null){
 			String savedPath = savedInstanceState.getString("ImagePath");
 			if(!(savedPath.isEmpty())){
 				decodeFile(savedPath);
 			}
 			else{
 				//Toast.makeText(Register.this, "Vacio", Toast.LENGTH_SHORT).show();
 			}
 			
 			code = savedInstanceState.getString("Barcode");
 			productName = savedInstanceState.getString("ProductName");
 			uploading = savedInstanceState.getBoolean("UploadingStatus");
 		}
 		
 		myCountryMap = new HashMap<String, String>();
 		
 		populateCountries();
 		
 		ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array.country_arrays, android.R.layout.simple_spinner_dropdown_item);
 		spinner.setAdapter(adapter);
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		
 		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
 			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
 				countryCode = myCountryMap.get(arg0.getItemAtPosition(arg2));
 			}
 
 			public void onNothingSelected(AdapterView<?> arg0) {
 				
 			}
 		});
 		
 		/* Get intent extra data */
 		Intent t = getIntent();
 		code = t.getStringExtra("BARCODE");
 		
 		/* Output of saved 'BARCODE' data */
 		textCode.setText("Barcode: "+code);
 				
 		photo.setClickable(true);
 		
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle("Pick image from");
 		
 		LayoutInflater inflater = (LayoutInflater) getSystemService(this.LAYOUT_INFLATER_SERVICE);
 		View new_layout = inflater.inflate(R.layout.pick_photo, null);
 		builder.setView(new_layout);
 		final AlertDialog choosePictureDialog = builder.create();
 		
 		Button cameraButton = (Button) new_layout.findViewById(R.id.launch_camera_button);
 		Button galleryButton = (Button) new_layout.findViewById(R.id.choose_from_gallery_button);
 		
 		cameraButton.setOnClickListener(new View.OnClickListener() {			
 			public void onClick(View v) {
 				Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
 				cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(getTempFile()));
 				startActivityForResult(cameraIntent, CAMERA_RESULT);
 				choosePictureDialog.dismiss();	
 			}
 		});
 	
 		galleryButton.setOnClickListener(new View.OnClickListener() {			
 			public void onClick(View v) {
 				Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
 				startActivityForResult(galleryIntent, GALLERY_RESULT);
 				choosePictureDialog.dismiss();
 			}
 		});
 		
 		photo.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				choosePictureDialog.show();
 			}
 		});
 		
 		developmentSession = getSharedPreferences(ConstantValues.USER_DATA, MODE_PRIVATE);
         id = developmentSession.getString("user_id", "-1");
         realId = Integer.parseInt(id);
 	}
 	
 	private void populateCountries() {
 		myCountryMap.put("Pick the country of manufacturing", "");
 		myCountryMap.put("Aaland Islands","AX");
 		myCountryMap.put("Afghanistan","AF");
 		myCountryMap.put("Albania","AL");
 		myCountryMap.put("Algeria","DZ");
 		myCountryMap.put("American Samoa","AS");
 		myCountryMap.put("Andorra","AD");
 		myCountryMap.put("Angola","AO");
 		myCountryMap.put("Anguilla","AI");
 		myCountryMap.put("Antarctica","AQ");
 		myCountryMap.put("Antigua And Barbuda","AG");
 		myCountryMap.put("Argentina","AR");
 		myCountryMap.put("Armenia","AM");
 		myCountryMap.put("Aruba","AW");
 		myCountryMap.put("Australia","AU");
 		myCountryMap.put("Austria","AT");
 		myCountryMap.put("Azerbaijan","AZ");
 		myCountryMap.put("Bahamas","BS");
 		myCountryMap.put("Bahrain","BH");
 		myCountryMap.put("Bangladesh","BD");
 		myCountryMap.put("Barbados","BB");
 		myCountryMap.put("Belarus","BY");
 		myCountryMap.put("Belgium","BE");
 		myCountryMap.put("Belize","BZ");
 		myCountryMap.put("Benin","BJ");
 		myCountryMap.put("Bermuda","BM");
 		myCountryMap.put("Bhutan","BT");
 		myCountryMap.put("Bolivia","BO");
 		myCountryMap.put("Bosnia And Herzegowina","BA");
 		myCountryMap.put("Botswana","BW");
 		myCountryMap.put("Bouvet Island","BV");
 		myCountryMap.put("Brazil","BR");
 		myCountryMap.put("British Indian Ocean Territory","IO");
 		myCountryMap.put("Brunei Darussalam","BN");
 		myCountryMap.put("Bulgaria","BG");
 		myCountryMap.put("Burkina Faso","BF");
 		myCountryMap.put("Burundi","BI");
 		myCountryMap.put("Cambodia","KH");
 		myCountryMap.put("Cameroon","CM");
 		myCountryMap.put("Canada","CA");
 		myCountryMap.put("Cape Verde","CV");
 		myCountryMap.put("Cayman Islands","KY");
 		myCountryMap.put("Central African Republic","CF");
 		myCountryMap.put("Chad","TD");
 		myCountryMap.put("Chile","CL");
 		myCountryMap.put("China","CN");
 		myCountryMap.put("Christmas Island","CX");
 		myCountryMap.put("Cocos (Keeling) Islands","CC");
 		myCountryMap.put("Colombia","CO");
 		myCountryMap.put("Comoros","KM");
 		myCountryMap.put("Democratic Republic Of Congo ","CD");
 		myCountryMap.put("Republic Of Congo","CG");
 		myCountryMap.put("Cook Islands","CK");
 		myCountryMap.put("Costa Rica","CR");
 		myCountryMap.put("Cote D'Ivoire","CI");
 		myCountryMap.put("Croatia (Hrvatska)","HR");
 		myCountryMap.put("Cuba","CU");
 		myCountryMap.put("Cyprus","CY");
 		myCountryMap.put("Czech Republic","CZ");
 		myCountryMap.put("Denmark","DK");
 		myCountryMap.put("Djibouti","DJ");
 		myCountryMap.put("Dominica","DM");
 		myCountryMap.put("Dominican Republic","DO");
 		myCountryMap.put("Ecuador","EC");
 		myCountryMap.put("Egypt","EG");
 		myCountryMap.put("El Salvador","SV");
 		myCountryMap.put("Equatorial Guinea","GQ");
 		myCountryMap.put("Eritrea","ER");
 		myCountryMap.put("Estonia","EE");
 		myCountryMap.put("Ethiopia","ET");
 		myCountryMap.put("Falkland Islands (Malvinas)","FK");
 		myCountryMap.put("Faroe Islands","FO");
 		myCountryMap.put("Fiji","FJ");
 		myCountryMap.put("Finland","FI");
 		myCountryMap.put("France","FR");
 		myCountryMap.put("French Guiana","GF");
 		myCountryMap.put("French Polynesia","PF");
 		myCountryMap.put("French Southern Territories","TF");
 		myCountryMap.put("Gabon","GA");
 		myCountryMap.put("Gambia","GM");
 		myCountryMap.put("Georgia","GE");
 		myCountryMap.put("Germany","DE");
 		myCountryMap.put("Ghana","GH");
 		myCountryMap.put("Gibraltar","GI");
 		myCountryMap.put("Greece","GR");
 		myCountryMap.put("Greenland","GL");
 		myCountryMap.put("Grenada","GD");
 		myCountryMap.put("Guadeloupe","GP");
 		myCountryMap.put("Guam","GU");
 		myCountryMap.put("Guatemala","GT");
 		myCountryMap.put("Guinea","GN");
 		myCountryMap.put("Guinea-bissau","GW");
 		myCountryMap.put("Guyana","GY");
 		myCountryMap.put("Haiti","HT");
 		myCountryMap.put("Heard And Mc Donald Islands","HM");
 		myCountryMap.put("Honduras","HN");
 		myCountryMap.put("Hong Kong","HK");
 		myCountryMap.put("Hungary","HU");
 		myCountryMap.put("Iceland","IS");
 		myCountryMap.put("India","IN");
 		myCountryMap.put("Indonesia","ID");
 		myCountryMap.put("Iran","IR");
 		myCountryMap.put("Iraq","IQ");
 		myCountryMap.put("Ireland","IE");
 		myCountryMap.put("Israel","IL");
 		myCountryMap.put("Italy","IT");
 		myCountryMap.put("Jamaica","JM");
 		myCountryMap.put("Japan","JP");
 		myCountryMap.put("Jordan","JO");
 		myCountryMap.put("Kazakhstan","KZ");
 		myCountryMap.put("Kenya","KE");
 		myCountryMap.put("Kiribati","KI");
 		myCountryMap.put("Democratic People's Republic Of Korea","KP");
 		myCountryMap.put("Republic Of Korea","KR");
 		myCountryMap.put("Kuwait","KW");
 		myCountryMap.put("Kyrgyzstan","KG");
 		myCountryMap.put("Lao People's Democratic Republic","LA");
 		myCountryMap.put("Latvia","LV");
 		myCountryMap.put("Lebanon","LB");
 		myCountryMap.put("Lesotho","LS");
 		myCountryMap.put("Liberia","LR");
 		myCountryMap.put("Libyan Arab Jamahiriya","LY");
 		myCountryMap.put("Liechtenstein","LI");
 		myCountryMap.put("Lithuania","LT");
 		myCountryMap.put("Luxembourg","LU");
 		myCountryMap.put("Macau","MO");
 		myCountryMap.put("The Former Yugoslav Republic Of Macedonia","MK");
 		myCountryMap.put("Madagascar","MG");
 		myCountryMap.put("Malawi","MW");
 		myCountryMap.put("Malaysia","MY");
 		myCountryMap.put("Maldives","MV");
 		myCountryMap.put("Mali","ML");
 		myCountryMap.put("Malta","MT");
 		myCountryMap.put("Marshall Islands","MH");
 		myCountryMap.put("Martinique","MQ");
 		myCountryMap.put("Mauritania","MR");
 		myCountryMap.put("Mauritius","MU");
 		myCountryMap.put("Mayotte","YT");
 		myCountryMap.put("Mexico","MX");
 		myCountryMap.put("Federated States Of Micronesia","FM");
 		myCountryMap.put("Republic Of Moldova","MD");
 		myCountryMap.put("Monaco","MC");
 		myCountryMap.put("Mongolia","MN");
 		myCountryMap.put("Montserrat","MS");
 		myCountryMap.put("Morocco","MA");
 		myCountryMap.put("Mozambique","MZ");
 		myCountryMap.put("Myanmar","MM");
 		myCountryMap.put("Namibia","NA");
 		myCountryMap.put("Nauru","NR");
 		myCountryMap.put("Nepal","NP");
 		myCountryMap.put("Netherlands","NL");
 		myCountryMap.put("Netherlands Antilles","AN");
 		myCountryMap.put("New Caledonia","NC");
 		myCountryMap.put("New Zealand","NZ");
 		myCountryMap.put("Nicaragua","NI");
 		myCountryMap.put("Niger","NE");
 		myCountryMap.put("Nigeria","NG");
 		myCountryMap.put("Niue","NU");
 		myCountryMap.put("Norfolk Island","NF");
 		myCountryMap.put("Northern Mariana Islands","MP");
 		myCountryMap.put("Norway","NO");
 		myCountryMap.put("Oman","OM");
 		myCountryMap.put("Pakistan","PK");
 		myCountryMap.put("Palau","PW");
 		myCountryMap.put("Palestinian Territory, Occupied","PS");
 		myCountryMap.put("Panama","PA");
 		myCountryMap.put("Papua New Guinea","PG");
 		myCountryMap.put("Paraguay","PY");
 		myCountryMap.put("Peru","PE");
 		myCountryMap.put("Philippines","PH");
 		myCountryMap.put("Pitcairn","PN");
 		myCountryMap.put("Poland","PL");
 		myCountryMap.put("Portugal","PT");
 		myCountryMap.put("Puerto Rico","PR");
 		myCountryMap.put("Qatar","QA");
 		myCountryMap.put("Reunion","RE");
 		myCountryMap.put("Romania","RO");
 		myCountryMap.put("Russian Federation","RU");
 		myCountryMap.put("Rwanda","RW");
 		myCountryMap.put("Saint Helena","SH");
 		myCountryMap.put("Saint Kitts And Nevis","KN");
 		myCountryMap.put("Saint Lucia","LC");
 		myCountryMap.put("Saint Pierre And Miquelon","PM");
 		myCountryMap.put("Saint Vincent And The Grenadines","VC");
 		myCountryMap.put("Samoa","WS");
 		myCountryMap.put("San Marino","SM");
 		myCountryMap.put("Sao Tome And Principe","ST");
 		myCountryMap.put("Saudi Arabia","SA");
 		myCountryMap.put("Senegal","SN");
 		myCountryMap.put("Serbia And Montenegro","CS");
 		myCountryMap.put("Seychelles","SC");
 		myCountryMap.put("Sierra Leone","SL");
 		myCountryMap.put("Singapore","SG");
 		myCountryMap.put("Slovakia","SK");
 		myCountryMap.put("Slovenia","SI");
 		myCountryMap.put("Solomon Islands","SB");
 		myCountryMap.put("Somalia","SO");
 		myCountryMap.put("South Africa","ZA");
 		myCountryMap.put("South Georgia And The South Sandwich Islands","GS");
 		myCountryMap.put("Spain","ES");
 		myCountryMap.put("Sri Lanka","LK");
 		myCountryMap.put("Sudan","SD");
 		myCountryMap.put("Suriname","SR");
 		myCountryMap.put("Svalbard And Jan Mayen Islands","SJ");
 		myCountryMap.put("Swaziland","SZ");
 		myCountryMap.put("Sweden","SE");
 		myCountryMap.put("Switzerland","CH");
 		myCountryMap.put("Syrian Arab Republic","SY");
 		myCountryMap.put("Taiwan","TW");
 		myCountryMap.put("Tajikistan","TJ");
 		myCountryMap.put("United Republic Of Tanzania","TZ");
 		myCountryMap.put("Thailand","TH");
 		myCountryMap.put("Timor-leste","TL");
 		myCountryMap.put("Togo","TG");
 		myCountryMap.put("Tokelau","TK");
 		myCountryMap.put("Tonga","TO");
 		myCountryMap.put("Trinidad And Tobago","TT");
 		myCountryMap.put("Tunisia","TN");
 		myCountryMap.put("Turkey","TR");
 		myCountryMap.put("Turkmenistan","TM");
 		myCountryMap.put("Turks And Caicos Islands","TC");
 		myCountryMap.put("Tuvalu","TV");
 		myCountryMap.put("Uganda","UG");
 		myCountryMap.put("Ukraine","UA");
 		myCountryMap.put("United Arab Emirates","AE");
 		myCountryMap.put("United Kingdom","GB");
 		myCountryMap.put("United States","US");
 		myCountryMap.put("United States Minor Outlying Islands","UM");
 		myCountryMap.put("Uruguay","UY");
 		myCountryMap.put("Uzbekistan","UZ");
 		myCountryMap.put("Vanuatu","VU");
 		myCountryMap.put("Vatican City State (Holy See)","VA");
 		myCountryMap.put("Venezuela","VE");
 		myCountryMap.put("Viet Nam","VN");
 		myCountryMap.put("Virgin Islands (British)","VG");
 		myCountryMap.put("Virgin Islands (U.S.)","VI");
 		myCountryMap.put("Wallis And Futuna Islands","WF");
 		myCountryMap.put("Western Sahara","EH");
 		myCountryMap.put("Yemen","YE");
 		myCountryMap.put("Zambia","ZM");
 		myCountryMap.put("Zimbabwe","ZW");
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		if(registerBack.getStatus() == Status.RUNNING){
 			registerBack.cancel(true);
 			uploading = true;
 		}
 		else{
 			uploading = false;
 		}
 		outState.putString("ImagePath", finalImagePath);
 		outState.putString("Barcode", code);
 		outState.putString("ProductName", productName);
 		outState.putBoolean("UploadingStatus", uploading);
 		super.onSaveInstanceState(outState);
 	}
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		switch (requestCode) {
 			case GALLERY_RESULT:
 				if (resultCode == Activity.RESULT_OK) {
 					Uri selectedImageUri = data.getData();
 					String filePath = null;
 					try {
 						// OI FILE Manager
 						String filemanagerstring = selectedImageUri.getPath();
 	
 						// MEDIA GALLERY
 						String selectedImagePath = getPath(selectedImageUri);
 	
 						if (selectedImagePath != null) {
 							filePath = selectedImagePath;
 						} else if (filemanagerstring != null) {
 							filePath = filemanagerstring;
 						} else {
 							Toast.makeText(getApplicationContext(), "Unknown path",Toast.LENGTH_LONG).show();
 							Log.e("Bitmap", "Unknown path");
 						}
 	
 						if (filePath != null) {
 							decodeFile(filePath);
 							finalImagePath = filePath;
 						} else {
 							bitmap = null;
 						}
 					} catch (Exception e) {
 						Toast.makeText(getApplicationContext(), "Internal error",Toast.LENGTH_LONG).show();
 						Log.e(e.getClass().getName(), e.getMessage(), e);
 					}
 				}
 				break;
 			case CAMERA_RESULT:
 				if (resultCode == Activity.RESULT_OK) {
 					final File file = getTempFile();
 					try {
 						Bitmap captureBmp = Media.getBitmap(getContentResolver(), Uri.fromFile(file) );
 						bitmap = captureBmp;
						finalImagePath = file.getAbsolutePath();	
						decodeFile(finalImagePath);
						lastfinalImagePath = finalImagePath;
						//photo.setImageBitmap(bitmap);
 						// do whatever you want with the bitmap (Resize, Rename, Add To Gallery, etc)
 					} catch (FileNotFoundException e) {
 						e.printStackTrace();
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
					
 				}
 				break;
 		}
 	}
 	
 	public String getPath(Uri uri) {
 		String[] projection = { MediaStore.Images.Media.DATA };
 		Cursor cursor = managedQuery(uri, projection, null, null, null);
 		if (cursor != null) {
 			// HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
 			// THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
 			int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
 			cursor.moveToFirst();
 			return cursor.getString(column_index);
 		} else
 			return null;
 	}
 	
 	public void decodeFile(String filePath) {
 		// Decode image size
 		BitmapFactory.Options o = new BitmapFactory.Options();
 		o.inJustDecodeBounds = true;
 		BitmapFactory.decodeFile(filePath, o);
 
 		// Find the correct scale value. It should be the power of 2.
 //		int width_tmp = o.outWidth, height_tmp = o.outHeight;
		int scale = 4;
 
 		// Decode with inSampleSize
 		BitmapFactory.Options o2 = new BitmapFactory.Options();
 		o2.inSampleSize = scale;
 		bitmap = BitmapFactory.decodeFile(filePath, o2);
 		photo.setImageBitmap(bitmap);
 	}
 
 	private File getTempFile(){
 		  //it will return /sdcard/image.tmp
 		  final File path = new File( Environment.getExternalStorageDirectory(),"TruStripes");
 		  if(!path.exists())
 		    path.mkdir();
 		  return new File(path, "image.tmp");
 	}
 	
 	@Override
 	protected void onStart() {
 		super.onStart();
 		
 		/* Implementation of Google Analytics for Android */
     	if(!ConstantValues.isInDevelopmentTeam(realId)){
     		EasyTracker.getInstance().activityStart(this);
     	}
 		
 		/* Instantiate and associate button events */
 		btn_again.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				Log.d("MAIN", "Click EN btn_again");
 				Intent intent = new Intent(getApplicationContext(), CaptureActivity.class);
 				startActivity(intent);
 				finish();
 			}
 		});
 		
 		btn_register.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				Log.d("MAIN", "Click EN btn_register");
 				productName = registerName.getText().toString().trim();
 				String alertMessage = "";
 				if(bitmap == null || countryCode.isEmpty() || productName.isEmpty()){
 					if(bitmap == null)
 						alertMessage = "Photo is required \n";
 					if(productName.isEmpty())
 						alertMessage = alertMessage + "Product name is required \n";
 					if(countryCode.isEmpty())
 						alertMessage = alertMessage + "Select the country of manufacturing";
 					Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT).show();
 				}
 				else{
 					if(ConstantValues.getConnectionStatus(getApplicationContext())){
 						registerBack.execute();
 					}
 					else{
 						Toast.makeText(getApplicationContext(), "Looks like you have no connection, please check it and try again", Toast.LENGTH_SHORT).show();
 					}
 				}
 			}
 		});
 		
 		backButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				finish();
 			}
 		});
 		
 		if(uploading){
 			registerBack.execute();
 		}
 	}
 
 	@Override
 	protected void onStop() {
 		super.onStop();
 
 		/* Implementation of Google Analytics for Android */
     	if(!ConstantValues.isInDevelopmentTeam(realId)){
     		EasyTracker.getInstance().activityStop(this);
     	}
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_register, menu);
 		return true;
 	}
 
 	public class Registerback extends AsyncTask<Void, Integer, Void> {
 		
 		/* Internal variables for the thread */
 		private StringBuilder stringBuilder;
 		private String statusResponse = "";
 		private String message = "";
 		private boolean showDiscoverer = false;
 		private JSONObject jsonObject;
 
 		@Override
 		protected void onPreExecute() {
 			super.onPreExecute();
 			dialog = ProgressDialog.show(Register.this, "Uploading","Please wait...", true);
 		}
 		
 		@Override
 		protected Void doInBackground(Void... params) {
 			try {
 				/* Prepare variables for remote data check */
 				HttpClient client = new DefaultHttpClient();
 				HttpContext localContext = new BasicHttpContext();
 				String postURL = ConstantValues.URL + "/ws/ws-registerproduct.php";
 				HttpPost post = new HttpPost(postURL);
 				
 				MultipartEntity entity = new MultipartEntity();
 				ByteArrayOutputStream bos = new ByteArrayOutputStream();
 				bitmap.compress(CompressFormat.JPEG, 30, bos);
 				byte[] data = bos.toByteArray();
 				entity.addPart("productname", new StringBody(productName));
 				entity.addPart("uploadedfile", new ByteArrayBody(data, "myImage.jpg"));
 				entity.addPart("iduserdiscoverer", new StringBody(userSession));
 				entity.addPart("codeupean", new StringBody(code));
 				if(bitmap.getHeight()>bitmap.getWidth()){
 					entity.addPart("conversion", new StringBody("1"));
 				}else{
 					entity.addPart("conversion", new StringBody("0"));
 				}
 				entity.addPart("codepais", new StringBody(countryCode));
 				post.setEntity(entity);
 				if(!isCancelled()){
 					HttpResponse responsePOST = client.execute(post, localContext);
 					StatusLine status = responsePOST.getStatusLine();
 					/* Filter what kind of response was obtained */
 		    		/* Filtering http response 200 */
 					if (status.getStatusCode() == HttpStatus.SC_OK) {
 						HttpEntity new_entity = responsePOST.getEntity();
 						InputStream inputStream = new_entity.getContent();
 						BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
 						String line = null;
 						stringBuilder = new StringBuilder();
 						while ((line = reader.readLine()) != null) {
 							stringBuilder.append(line);
 						}
 						
 						/* Converting obtained string into JSON object */
 						jsonObject = new JSONObject(stringBuilder.toString());
 						statusResponse = jsonObject.getString("status");
 						if (Integer.parseInt(statusResponse) == 1) {
 							showDiscoverer = true;
 						} else {
 							showDiscoverer = false;
 							message = jsonObject.getString("msj");
 						}
 						reader.close();
 						inputStream.close();
 					} else {
 						/* Check Other Status Code */
 					}
 				}
 			} catch (ClientProtocolException e) {
 				e.printStackTrace();
 				Log.e(TAG, "CheckLoginData: Error ClientProtocolException");
 			} catch (UnsupportedEncodingException e) {
 				e.printStackTrace();
 				Log.e(TAG, "CheckLoginData: Error UnsupportedEncodingException");
 			} catch (IOException e) {
 				Log.e(TAG, "CheckLoginData: Error IOException");
 				e.printStackTrace();
 			} catch (Exception e) {
 				Log.e(TAG, "Unknown error");
 				e.printStackTrace();
 			}
 			return null;
 
 		}
 
 		protected void onPostExecute(Void result) {
 			Intent intent = null;
 			if (showDiscoverer) {
 				intent = new Intent(getApplicationContext(), Discoverer.class);
 				try {
 					intent.putExtra("Product_id",  jsonObject.getInt("idproducto") );
 					intent.putExtra("Product_name",  jsonObject.getString("producto") );
 					intent.putExtra("Total_snacks",  jsonObject.getString("total") );
 				} catch (JSONException e) {
 					e.printStackTrace();
 				}
 				startActivity(intent);
 			} else {
 				if (dialog.isShowing())
 					dialog.dismiss();
 				Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
 			}
 			finish();
 		}
 	}
 	
 	@Override
 	public void onBackPressed() {
 		finish();
 	}
 }
