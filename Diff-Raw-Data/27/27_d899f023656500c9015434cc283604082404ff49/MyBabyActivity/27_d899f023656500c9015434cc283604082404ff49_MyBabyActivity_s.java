 package gecko.mybaby.view;
 
 import gecko.mybaby.R;
 import gecko.mybaby.controller.BabyController;
 import gecko.mybaby.controller.TakenVaccineController;
 import gecko.mybaby.controller.VaccineController;
 import gecko.mybaby.model.Baby;
 import gecko.mybaby.model.FacebookHelper;
 import gecko.mybaby.model.Vaccine;
 import gecko.mybaby.webservice.AddRemoteBaby;
 import gecko.mybaby.webservice.AddRemoteBaby.AddBabyCallback;
 import gecko.mybaby.webservice.LoginAutenticator;
 import gecko.mybaby.webservice.LoginAutenticator.LoginCallback;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import com.facebook.model.GraphUser;
 import com.facebook.widget.LoginButton;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.drawable.BitmapDrawable;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MyBabyActivity extends Activity implements AddBabyCallback, LoginCallback {
 
 	public static Activity instance = null;
 	private static final int PREFERENCES_RESULT_CODE = 1;
 	
 	private static int backgroundExternal = R.drawable.background_activity_neutral;
 	private static int backgroundTabBar = R.drawable.tab_bar_neutral;
 	private static int backgroundNavigationBar = R.drawable.navigation_bar_neutral;
 	private static int backgroundDivider= R.drawable.divider_neutral;
 
 	private FacebookHelper fbHelper;
 
 	private LinearLayout externalLayout;
 	private RelativeLayout tabBar;
 	private LinearLayout navigationBar;
 	
 	private TextView babyName;
 	private ImageButton fbButton;
 	private LoginButton loginButton;
 	
 	private ListView listView;
 	
 	private String username;
 	private String password;
 	
 	private ArrayList<Baby> babyList;
 	private Baby selectedBaby;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		
 		super.onCreate(savedInstanceState);
 		this.setContentView(R.layout.activity_my_baby);
 		
 		//if first execution.
 		this.addDefaultVaccines();
 		
 		MyBabyActivity.instance = this;
 		this.fbHelper = new FacebookHelper(this);
 		
 		this.getReferences();
 
 		this.username = this.getIntent().getStringExtra("username");
 		this.password = this.getIntent().getStringExtra("password");
 		
 		//Initializing selectedBaby to null and decoration to neutral.
 		this.setSelectedBaby(null);
 
 		this.babyList = (ArrayList<Baby>) this.getIntent().getSerializableExtra("babys");
 		
 		this.listView.setAdapter(new BabyListAdapter(this, 0, this.babyList));
 		
 	}
 	
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		
 		if(this.fbHelper == null){
 			this.fbHelper = new FacebookHelper(this);
 		}
 		
 		fbHelper.getActivitySession(requestCode, resultCode, data);
 	}
  
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
     	
         this.getMenuInflater().inflate(R.menu.settings, menu);
         return true;
     }
  
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
     	
         switch (item.getItemId()) {
         	
 	        case R.id.menu_settings:
 	            Intent intent = new Intent(this, MyBabyPreferencesActivity.class);
 	            this.startActivityForResult(intent, MyBabyActivity.PREFERENCES_RESULT_CODE);
 	            break;
 	            
         }
  
         return true;
     }
 	
 	private void getReferences() {
 
 		this.externalLayout = ((LinearLayout) this.findViewById(R.id.external_layout));
 		this.tabBar = ((RelativeLayout) this.findViewById(R.id.tab_bar));
 		this.navigationBar = ((LinearLayout) this.findViewById(R.id.navigation_bar));
 		
 		this.babyName = ((TextView) this.findViewById(R.id.baby_name_header));
 		
 		this.listView = ((ListView) this.findViewById(R.id.list_view));
 		
 		this.fbButton = (ImageButton) this.findViewById(R.id.button_facebook);
 		this.fbButton.setEnabled(false);
 		
 		loginButton = (LoginButton) findViewById(R.id.login_button);
         loginButton.setUserInfoChangedCallback(new LoginButton.UserInfoChangedCallback() {
             @Override
             public void onUserInfoFetched(GraphUser user) {
                 MyBabyActivity.this.fbHelper.setUser(user);
                loginButton.setVisibility(View.INVISIBLE);
                loginButton.setEnabled(false);
                 
                MyBabyActivity.this.fbButton.setEnabled(true);
                // It's possible that we were waiting for this.user to be populated in order to post a
                // status update.
                MyBabyActivity.this.fbHelper.handlePendingAction();
             }
         });
 		
 	}
     
     private void addDefaultVaccines() {
     	
     	VaccineController controller = new VaccineController(this);
     	try {
     		
 	    	List<Vaccine> vaccines = controller.getAllVaccines();
 	    	if (vaccines != null) {
 	    		
 	    		if (vaccines.size() != 0) {
 	    			
 	    			return;
 	    		}
 	    	}
     	} catch (Exception exception) {
     		
     		return;
     	}
     	
     	String[] vaccinesStr = this.getResources().getStringArray(R.array.vaccines);
 
     	String name;
     	String[] doses;
     	String[] months;
     	String description;
     	
     	for (int i = 0, i3 = 1 ; i < vaccinesStr.length ; i++) {
     		
     		name = vaccinesStr[i++];
     		doses = vaccinesStr[i++].split(", ");
     		months = vaccinesStr[i++].split(", ");
     		description = vaccinesStr[i];
     		
     		for (int i2 = 0 ; i2 < doses.length ; i2++, i3++) {
 
     			int month = Integer.parseInt(months[i2]);
         		Vaccine vaccine = new Vaccine(name, doses[i2], month, description, true);
         		vaccine.setId(i3);
         		
         		controller.addVaccine(vaccine);
     		}
     	}
     }
 	
 	private void getRemoteBabys() {
 		
 		LoginAutenticator.autenticateLogin(this.username, this.password, this);
 		
 		if (true) {
 			
 			return;
 		}
 	}
 	
 	public void addBaby(Baby baby) {
 
 //		this.addBabyOnDatabase(baby);
 		this.addBabyOnRemoteDatabase(baby);
 	}
 	
 	private void updateLocalVaccines(Baby baby) {
 		
 		int age = baby.getAgeInMonths();
 		
 		VaccineController vaccineController = new VaccineController(this);
 		ArrayList<Vaccine> vaccines = vaccineController.getAllVaccines();
 
 		TakenVaccineController takenVaccineController = new TakenVaccineController(this);
 		
 		for (Vaccine vaccine : vaccines) {
 			
 			if (vaccine.getMonth() < age) {
 
 				takenVaccineController.addTakenVaccine(baby.getId(), vaccine.getId(), vaccine.getMonth());
 			}
 		}
 	}
 	
 	public void editBaby(Baby baby) {
 		
 		BabyController cont = new BabyController(this);
 		
 		cont.setBabyName(baby.getId(), baby.getName());
 		cont.setBabySex(baby.getId(), baby.getGender());
 		cont.setBabyBirth(baby.getId(), baby.getBirth());
 		
 		StringBuilder sb = new StringBuilder("http://ws.geckoapps.com.br/editar-bebe.php?");
 		sb.append("id=").append(baby.getId()).append("&nome=").append(baby.getName())
 			.append("&idade=").append(baby.getBirth()).append("&sexo=")
 			.append((baby.getGender() == Baby.GENDER_BOY?"1":"0"));
 		
 		AsyncTask<String, Void, Void> task = new AsyncTask<String, Void, Void>() {
 
 			@Override
 			protected Void doInBackground(String... params) {
 
 				try {
 					
 					HttpClient cli = new DefaultHttpClient();
 					HttpGet get = new HttpGet(params[0]);
 					
 					cli.execute(get);
 				} catch (Exception e) {
 					
 					e.printStackTrace();
 				}
 				return null;
 			}
 			
 			@Override
 			protected void onPostExecute(Void result) {
 				
 				super.onPostExecute(result);
 				
 				MyBabyActivity.this.getRemoteBabys();
 			}
 		};
 		
 		task.execute(sb.toString());
 		
 	}
 	
 	public void removeBaby(Baby baby) {
 		
 		this.babyList.remove(baby);
 		
 		BabyController controller = new BabyController(this);
 		controller.removeBaby(baby.getId());
 		
 		if (baby.equals(this.selectedBaby)) {
 			
 			this.selectedBaby = null;
 			this.updateActivityDecoration();
 		}
 	}
 	
 	private void addBabyOnRemoteDatabase(Baby baby) {
 		
 		AddRemoteBaby.addBaby(this.username, baby, this);
 	}
 		
 	private void addBabyOnDatabase(Baby baby) {
 		
 		BabyController controller = new BabyController(this);
 		controller.addBaby(baby);
 	}
 	
 	public Baby getSelectedBaby() {
 		
 		return this.selectedBaby;
 	}
 	
 	public void setSelectedBaby(Baby baby) {
 		
 		this.selectedBaby = baby;
 		
 		this.setHeaderName();
 		this.setImageResources();
 		this.updateActivityDecoration();
 	}
 	
 	private void setHeaderName() {
 		
 		try {
 			
 			this.babyName.setText(this.selectedBaby.getName());
 		} catch (NullPointerException exception) {
 			
 			exception.printStackTrace();
 		}
 	}
 	
 	private void setImageResources() {
 
 		int sex;
 		try {
 			
 			sex = this.selectedBaby.getGender();
 		} catch (NullPointerException exception) {
 			
 			sex = Baby.GENDER_UNKNOWN;
 		}
 		
 		switch (sex) {
 		
 			case Baby.GENDER_BOY:
 				MyBabyActivity.backgroundExternal = R.drawable.background_activity_boy;
 				MyBabyActivity.backgroundTabBar = R.drawable.tab_bar_boy;
 				MyBabyActivity.backgroundNavigationBar = R.drawable.navigation_bar_boy;
 				MyBabyActivity.backgroundDivider = R.drawable.divider_boy;
 				break;
 				
 			case Baby.GENDER_GIRL:
 				MyBabyActivity.backgroundExternal = R.drawable.background_activity_girl;
 				MyBabyActivity.backgroundTabBar = R.drawable.tab_bar_girl;
 				MyBabyActivity.backgroundNavigationBar = R.drawable.navigation_bar_girl;
 				MyBabyActivity.backgroundDivider = R.drawable.divider_girl;
 				break;
 				
 			default:
 				MyBabyActivity.backgroundExternal = R.drawable.background_activity_neutral;
 				MyBabyActivity.backgroundTabBar = R.drawable.tab_bar_neutral;
 				MyBabyActivity.backgroundNavigationBar = R.drawable.navigation_bar_neutral;
 				MyBabyActivity.backgroundDivider = R.drawable.divider_neutral;
 				
 		}
 	}
 		
 	private void updateActivityDecoration() {
 
 		this.externalLayout.setBackgroundResource(MyBabyActivity.getBackgroundExternal());
 		this.tabBar.setBackgroundResource(MyBabyActivity.getBackgroundTabBar());
 		this.navigationBar.setBackgroundResource(MyBabyActivity.getBackgroundNavigationBar());
 	}
 	
 	public void updateListView() {
 		
 		this.listView.setAdapter(new BabyListAdapter(this, 0, this.babyList));
 	}
 	
 	public static int getBackgroundExternal() {
 		
 		return MyBabyActivity.backgroundExternal;
 	}
 	
 	public static int getBackgroundTabBar() {
 		
 		return MyBabyActivity.backgroundTabBar;
 	}
 	
 	public static int getBackgroundNavigationBar() {
 		
 		return MyBabyActivity.backgroundNavigationBar;
 	}
 	
 	public static int getBackgroundDivider() {
 		
 		return MyBabyActivity.backgroundDivider;
 	}
 	
 	public void addBabyClicked(View view) {
 
 		//Initiate AddBabyActivity.
         Intent intent = new Intent(this, AddBabyActivity.class);
         this.startActivity(intent);
 	}
 	
 	public void graphicsClicked(View view) {
 		
 		if (this.getSelectedBaby() != null) {
 			
 			//Initiate GraphicsActivity.
 	        Intent intent = new Intent(this, GraphicsActivity.class);
 	        this.startActivity(intent);
 		}
 	}
 	
 	public void vaccinesClicked(View view) {
 
 		if (this.getSelectedBaby() != null) {
 
 			//Initiate VaccinesActivity.
 	        Intent intent = new Intent(this, VaccinesActivity.class);
 	        this.startActivity(intent);
 		}
 	}
 	
 	public void progressClicked(View view) {
 
 		if (this.getSelectedBaby() != null) {
 
 			//Initiate ProgressActivity.
 	        Intent intent = new Intent(this, ProgressActivity.class);
 	        this.startActivity(intent);
 		}
 	}
 	
 	public void tipsClicked(View view) {
 
 		if (this.getSelectedBaby() != null) {
 
 			//Initiate TipsActivity.
 	        Intent intent = new Intent(this, TipsActivity.class);
 	        this.startActivity(intent);
 		}
 	}
 	
 	public void shareOnFacebook(View view){
 		
 		Log.v("facebook", "facebook");
 		this.fbHelper.postMyBabyLink();
 	}
 	
 	private class BabyListAdapter extends ArrayAdapter<Baby> {
 
 		public BabyListAdapter(	Context context,
 								int textViewResourceId,
 								List<Baby> objects ) {
 			
 			super(context, textViewResourceId, objects);
 		}
 		
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			
 			Baby baby = this.getItem(position);
 			
 			RelativeLayout layout = ((RelativeLayout) LayoutInflater.from(this.getContext()).inflate(R.layout.baby, null));
 			
 			ImageView image = (ImageView) layout.findViewById(R.id.baby_image);
 			TextView name = (TextView) layout.findViewById(R.id.baby_name);
 			TextView age = (TextView) layout.findViewById(R.id.baby_age);
 			ImageButton edit = (ImageButton) layout.findViewById(R.id.edit);
 			
 			name.setText(baby.getName());
 			age.setText(baby.getAge());
 			if (baby.getImage() != null) {
 				
 				image.setBackgroundDrawable(new BitmapDrawable(baby.getImage()));
 			} else {
 				
 				image.setBackgroundResource(R.drawable.no_photo);
 			}
 			if (baby.getGender() == Baby.GENDER_GIRL) {
 				
 				layout.setBackgroundResource(R.drawable.background_girl);
 				
 				image.setImageResource(R.drawable.foreground_girl);
 			}
 			edit.setOnClickListener(new OnEditBabyClickListener(baby));
 			
 			layout.setOnClickListener(new OnBabyClickedListener(baby));
 			
 			return layout;
 		}
 		
 		private class OnEditBabyClickListener implements OnClickListener {
 			
 			private Baby baby;
 			
 			public OnEditBabyClickListener(Baby baby) {
 				
 				this.baby = baby;
 			}
 			
 			@Override
 			public void onClick(View v) {
 
 				//Initiate TipsActivity.
 		        Intent intent = new Intent(MyBabyActivity.this, EditBabyActivity.class);
 		        intent.putExtra("baby", this.baby);
 		        MyBabyActivity.this.startActivity(intent);
 			}
 		}
 		
 		private class OnBabyClickedListener implements OnClickListener {
 
 			private Baby baby;
 			
 			public OnBabyClickedListener(Baby baby) {
 				
 				this.baby = baby;
 			}
 			
 			@Override
 			public void onClick(View v) {
 				
 				MyBabyActivity.this.setSelectedBaby(this.baby);
 			}
 			
 		}
 		
 	}
 
 	@Override
 	public void addBabyResponse(int status, Baby baby) {
 		
 		if (status == AddBabyCallback.STATUS_SUCCESS) {
 			
 			this.babyList.add(baby);
 			this.addBabyOnDatabase(baby);
 			
 			this.getRemoteBabys();
 			
 			this.updateActivityDecoration();
 		}
 	}
 
 	@Override
 	public void loginResponse(int status, ArrayList<Baby> babysList,
 			String usernameStr, String passwordStr) {
 		
 		final int finalStatus = status;
 		final ArrayList<Baby> finalBabysList = babysList;
 		
 		this.runOnUiThread(new Runnable() {
 			
 			@Override
 			public void run() {
 
 				if (finalStatus == LoginCallback.STATUS_ERROR) {
 					
 					Toast.makeText(MyBabyActivity.this, "Status_Error", Toast.LENGTH_LONG).show();
 					return;
 				}
 				
 				MyBabyActivity.this.babyList = finalBabysList;
 				
 				MyBabyActivity.this.updateListView();
 
 				boolean autoVaccines = PreferenceManager.getDefaultSharedPreferences(MyBabyActivity.this)
 						.getBoolean(MyBabyActivity.this.getResources().getString(R.string.set_auto_vaccines), false);
 				
 				if (autoVaccines) {
 					
 					try {
 						
 						Baby lastBaby = MyBabyActivity.this.babyList.get(MyBabyActivity.this.babyList.size() - 1);
 						MyBabyActivity.this.updateLocalVaccines(lastBaby);
 					} catch (Exception exception) {//In case there is only one baby.
 						
 					}
 				}
 			}
 			
 		});
 	}
 
 }
