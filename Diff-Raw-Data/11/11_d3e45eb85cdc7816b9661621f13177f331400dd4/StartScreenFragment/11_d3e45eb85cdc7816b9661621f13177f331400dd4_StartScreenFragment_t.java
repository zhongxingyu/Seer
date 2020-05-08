 package ws.vannen.fiver.ui;
 
 import java.util.ArrayList;
 
 import ws.vannen.fiver.R;
 import android.app.ProgressDialog;
 import android.content.ContentProviderOperation;
 import android.content.OperationApplicationException;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.RemoteException;
 import android.provider.ContactsContract;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.TextView;
 
 import com.actionbarsherlock.app.SherlockFragment;
 
 public class StartScreenFragment extends SherlockFragment {
 	
 	private View view = null;
 	
 	private TextView textViewWelcome = null;
 	private TextView textViewEmtelContacts = null;
 	private TextView textViewMtmlContacts = null;
 	private TextView textViewCellplusContacts = null;
 	private Button buttonConvertAll = null;
 	
 	
 	private ProgressDialog pDialog = null;
 	
 	private String emtelText = "Emtel numbers";
 	private String mtmlText = "Mtml numbers";
 	private String orangeText = "Orange numbers";
 	
	private String welcomeTextNumbersFound = "Yup, you have some old format numbers.";
	private String welcomeTextNumbersNotFound = "Nopes, no old format numbers found.";
	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		view = inflater.inflate(R.layout.fragment_startscreen, container, false);
 		return view;
 	}
 	
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		
 		textViewWelcome = (TextView)view.findViewById(R.id.textViewWelcomeText);
 		textViewCellplusContacts = (TextView)view.findViewById(R.id.textViewOrangeContacts);
 		textViewMtmlContacts = (TextView)view.findViewById(R.id.textViewMtmlContacts);
 		textViewEmtelContacts = (TextView)view.findViewById(R.id.textViewEmtelContacts);
 		buttonConvertAll = (Button)view.findViewById(R.id.buttonConvertAll);
 		
 		if(MainFragmentActivity.robotoFontTypeface != null){
 			textViewWelcome.setTypeface(MainFragmentActivity.robotoFontTypeface);
 			textViewCellplusContacts.setTypeface(MainFragmentActivity.robotoFontTypeface);
 			textViewEmtelContacts.setTypeface(MainFragmentActivity.robotoFontTypeface);
 			textViewMtmlContacts.setTypeface(MainFragmentActivity.robotoFontTypeface);
 			
 			buttonConvertAll.setTypeface(MainFragmentActivity.robotoFontTypeface);
 		}
 		
 		updateNumbers();
 		
 		buttonConvertAll.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				new ProcessContactsAsyncTask().execute();
 			}
 		});
 
 	}
 	
 	public void updateNumbers(){
 		textViewEmtelContacts.setText(MainFragmentActivity.totalEmtelContacts + "  " + emtelText);
 		textViewMtmlContacts.setText(MainFragmentActivity.totalMtmlContacts + "  " + mtmlText);
 		textViewCellplusContacts.setText(MainFragmentActivity.totalCellplusContacts+ "  " + orangeText);
 		
		if(MainFragmentActivity.totalEmtelContacts > 0
				|| MainFragmentActivity.totalMtmlContacts > 0
				|| MainFragmentActivity.totalCellplusContacts > 0){
			textViewWelcome.setText(welcomeTextNumbersFound);
		}else{
			textViewWelcome.setText(welcomeTextNumbersNotFound);
		}
 	}
 	
 	
 	private class ProcessContactsAsyncTask extends AsyncTask<Void, Void, Void>{
 			
 			@Override
 			protected void onPreExecute() {
 				super.onPreExecute();
 				
 				pDialog = ProgressDialog.show(getActivity(), "", "Converting...");
 				
 			}
 			
 			
 			@Override
 			protected Void doInBackground(Void... params) {
 				
 			    ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>(); 
 			    String selectPhone = ContactsContract.Data._ID + "=? AND " + ContactsContract.Data.MIMETYPE + "='"  + 
 			    		ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE + "'"; 
 			    
 			    for(int i =0; i < MainFragmentActivity.unProcessedContacts.size();i++){
 			    	
 		            String[] phoneArgs = new String[]{MainFragmentActivity
 		            									.unProcessedContacts
 		            									.get(i)
 		            									.get_id()};
 		            ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
 		                    .withSelection(selectPhone, phoneArgs)
 		                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, MainFragmentActivity
 									.unProcessedContacts
 									.get(i)
 									.getNewPhoneNumber())
 		                    .build()); 
 			    	
 			    }
 	
 		        
 			    try {
 		            getActivity().getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
 		            
 		        } catch (RemoteException e) {
 		            // TODO Auto-generated catch block
 		            e.printStackTrace();
 		        } catch (OperationApplicationException e) {
 		            // TODO Auto-generated catch block
 		            e.printStackTrace();
 		        }       
 	
 				return null;
 			}
 			
 			@Override
 			protected void onPostExecute(Void result) {
 				super.onPostExecute(result);
 				
 				if(pDialog != null && pDialog.isShowing()){
 					pDialog.dismiss();
 					pDialog = null;
 				}
 			}
 		}
 
 
 }
