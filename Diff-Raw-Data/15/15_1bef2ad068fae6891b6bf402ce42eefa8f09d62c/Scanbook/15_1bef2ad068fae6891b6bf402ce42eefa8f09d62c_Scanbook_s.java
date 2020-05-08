 package paymium.paytunia.scanbook;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import paymium.paytunia.scanbook.connection.Connection;
 import paymium.paytunia.scanbook.connection.ConnectionNotInitializedException;
 import paymium.paytunia.scanbook.connection.DescriptionAdapter;
 import paymium.paytunia.scanbook.database.AddressListHandler;
 import paymium.paytunia.scanbook.dialog.AlertingDialogOneButton;
 import paymium.paytunia.scanbook.dialog.LoadingDialog;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.text.ClipboardManager;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 
 @SuppressWarnings("deprecation")
 public class Scanbook extends SherlockFragmentActivity implements OnClickListener 
 {
 
 	private Connection connection;
 	
 	private TextView short_address;
 	private TextView full_address;
 	
 	private ListView description;
 	private DescriptionAdapter descriptionAdapter;
 	
 	private LinearLayout layout_scan,layout_paste;
 	private ImageView image_scan,image_paste;
 	private TextView scan,paste;
 	
 	private String address;
 	
 	private AddressListHandler db;
 	
 	
     @Override
     public void onCreate(Bundle savedInstanceState) 
     {
     	setTheme(R.style.Theme_Sherlock);
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_scanbook);
         setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
         
         this.short_address = (TextView) findViewById(R.id.short_address);
         this.full_address = (TextView) findViewById(R.id.full_address);
         
         this.description = (ListView) findViewById(R.id.description);
         this.descriptionAdapter = new DescriptionAdapter(this);
         this.description.setAdapter(descriptionAdapter);
         
         this.layout_scan = (LinearLayout) findViewById(R.id.layout_scan);
         this.layout_scan.setOnClickListener(this);
         this.image_scan = (ImageView) findViewById(R.id.imageView1);
         this.image_scan.setOnClickListener(this);
         this.scan = (TextView) findViewById(R.id.scan);
         this.scan.setOnClickListener(this);
         
         this.layout_paste = (LinearLayout) findViewById(R.id.layout_paste);
         this.layout_paste.setOnClickListener(this);
         this.image_paste = (ImageView) findViewById(R.id.imageView2);
         this.image_paste.setOnClickListener(this);
         this.paste = (TextView) findViewById(R.id.paste);
         this.paste.setOnClickListener(this);
         
         this.connection = Connection.getInstance().initialize();
         
         this.db = new AddressListHandler(this);
     }
 
     private ArrayList<String> addressBitcoin;
     private AlertingDialogOneButton alertingDialogOneButton;
     
     
     @Override
 	public void onClick(View view) 
     { 	
 		if (view == layout_scan || view == image_scan || view == scan)
 		{
 			Intent intent = new Intent("com.google.zxing.client.android.SCAN");
 			intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
 
 			startActivityForResult(intent, 1);
 		}
 		else if (view == layout_paste || view == image_paste || view == paste)
 		{
 			ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
 			boolean isData = clipboard.hasText();
 			
 			if (!isData)
 			{
 				Toast.makeText(getBaseContext(), "No address", Toast.LENGTH_LONG).show();
 			}
 			else
 			{
 				ExtractAddressBitcoin extractAddressBitcoin = new ExtractAddressBitcoin();
 				
 				String link = (String) clipboard.getText();
 				
 				addressBitcoin = new ArrayList<String>();
 
 				addressBitcoin = extractAddressBitcoin.extract(link);
 				
 				
 				if (addressBitcoin.size() == 0)
 				{
 					alertingDialogOneButton = AlertingDialogOneButton.newInstance("Warning", 
 																				"No address found", 
 																				R.drawable.warning);
 					alertingDialogOneButton.show(getSupportFragmentManager(), "No bitcoin address found");
 				}
 				else if (addressBitcoin.size() > 1)
 				{
 					alertingDialogOneButton = AlertingDialogOneButton.newInstance("Warning", 
 																				"More than one address", 
 																				R.drawable.warning);
 					alertingDialogOneButton.show(getSupportFragmentManager(), "More than one btc address found");
 				}
 				else
 				{
 					this.address = addressBitcoin.get(0);
 					
 					new getWallet(address).execute();
 					
 					addressBitcoin = null;
 
 				}	
 			}
 		}
 	}
     
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent intent)  
     {
     	super.onActivityResult(requestCode, resultCode, intent);
     	
     	if (requestCode == 1) 
 		{
 			if (resultCode == RESULT_OK) 
 			{
 				String link = intent.getStringExtra("SCAN_RESULT");
 				
 				System.out.println(link);
 				
 				ExtractAddressBitcoin extractAddressBitcoin = new ExtractAddressBitcoin();
 				
 				addressBitcoin = new ArrayList<String>();
 
 				addressBitcoin = extractAddressBitcoin.extract(link);
 				
 				System.out.println(addressBitcoin + "+++++");
 					
 			}
 		} 
     }
     
     @Override
     protected void onStart() 
     {
     	super.onStart();
     	
     	if (addressBitcoin != null)
     	{
     		if (addressBitcoin.size() == 0)
     		{
     			alertingDialogOneButton = AlertingDialogOneButton.newInstance("Warning", 
     																		"No address found", 
     																		R.drawable.warning);
     			alertingDialogOneButton.show(getSupportFragmentManager(), "No bitcoin address found");
     		}
     		else if (addressBitcoin.size() > 1)
     		{
     			alertingDialogOneButton = AlertingDialogOneButton.newInstance("Warning", 
     																		"More than one address", 
     																		R.drawable.warning);
     			alertingDialogOneButton.show(getSupportFragmentManager(), "More than one btc address found");
     		}
     		else
     		{
     			address = addressBitcoin.get(0);
 				
 				new getWallet(address).execute();
 				
 				addressBitcoin = null;
 
     		}		
         	
     	}
 
     }
     
     private void setAddress(String address)
     {
     	int length = address.length();
     	this.short_address.setText(address.substring(0,4)+"..."+address.substring(length-5, length-1));
     	this.full_address.setText(address);
     }
 	
     public class getWallet extends AsyncTask<String, Integer, String>
     {
     	private Wallet wallet;
     	private String address;
     	private LoadingDialog loadingDialog ;
     	
     	public getWallet(String address)
     	{
     		this.wallet = new Wallet();
     		this.address = address;
     		this.loadingDialog = LoadingDialog.newInstance("Please wait ", "Getting Wallet ... ");
     	}
     	
     	@Override
     	protected void onPreExecute() 
     	{
     		super.onPreExecute();
     		this.loadingDialog.show(getSupportFragmentManager(), "");
     	}
     	
 		@Override
 		protected String doInBackground(String... params) 
 		{
 			try 
 			{
 				wallet = connection.getWallet(address);
 				//wallet = connection.getWallet("1E6aHHeH6XD6ZBNWBpsyzsCytJrWoD2UJ3");	
 			} 
 			catch (IOException e) 
 			{
 				e.printStackTrace();
 			} 
 			catch (ConnectionNotInitializedException e) 
 			{
 				e.printStackTrace();
 			}
 			
 			System.out.println(wallet);
 			
 			return null;
 		}
 		
 		@Override
 		protected void onPostExecute(String result) 
 		{
 			super.onPostExecute(result);
 			this.loadingDialog.dismiss();
 			
			if (this.wallet.getAddress().length() > 0)
 			{
 				setAddress(this.address);
 				descriptionAdapter.setWallet(wallet);
				db.addWallet(wallet);
 			}
 			
 		}
     	
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) 
     {
     	MenuItem refresh = menu.add(0, 0, 0, "Refresh");
     	{
     		refresh.setIcon(R.drawable.ic_refresh);
     		refresh.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
     	}
     	
     	MenuItem list_addresses = menu.add(1, 1, 1, "Addresses");
     	{
     		list_addresses.setIcon(R.drawable.ic_list);
     		list_addresses.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
     	}
     	
     	
     	return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) 
     {
     	return MenuChoice(item);
     }
     
     public boolean MenuChoice(MenuItem item)
     {
 
     	
     	if (item.getItemId() == 0)
     	{
     		if (address != null)
         	{
         		new getWallet(address).execute();
         		
         		System.out.println(address + "heheheh");
         		
         		return true;
         	}
     	}
     	else if (item.getItemId() == 1)
     	{
     		Intent intent = new Intent(this,AddressList.class);
     		startActivity(intent);
     		return true;
     	}
 
     	return false;
     }
 
 }
