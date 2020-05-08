 package paymium.paytunia.scanbook;
 
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 
import paymium.paytunia.scanbook.R;
 import paymium.paytunia.scanbook.database.AddressListHandler;
 import android.content.pm.ActivityInfo;
 import android.os.Bundle;
 import android.widget.ListView;
 
 public class AddressList extends SherlockFragmentActivity 
 {
 	private ListView address_list;
 	private AddressListAdapter addressListAdapter;
 	
 	private AddressListHandler db;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) 
 	{
 		setTheme(R.style.Theme_Sherlock);
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.scanbook_address_list);
 		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 		
 		this.address_list = (ListView) findViewById(R.id.listView1);
 		this.addressListAdapter = new AddressListAdapter(this);
 		this.address_list.setAdapter(addressListAdapter);
 		
 		this.db = new AddressListHandler(this);
 		
 		if (this.db.getAllWalletsAddresses().size() > 0)
 		{
 			this.addressListAdapter.addItem(db.getAllWallets());
 		}
 		
 		
 	}
 }
