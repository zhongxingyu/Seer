 package il.ac.shenkar.cadan;
 
 import java.util.ArrayList;
 
 import il.ac.shenkar.cadan.AddNewEventFragment.DatePickerFragment;
 import il.ac.shenkar.common.CampusInEvent;
 import il.ac.shenkar.common.CampusInUser;
 import il.ac.shenkar.common.CampusInUserChecked;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.DialogFragment;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.EditText;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 
 public class AddFriendsFragment extends DialogFragment 
 {
 	private View view;
 	
 	static AddFriendsFragment newInstance(int num) 
 	{
 		AddFriendsFragment f = new AddFriendsFragment();
 
         // Supply num input as an argument.
         Bundle args = new Bundle();
         args.putInt("num", num);
         f.setArguments(args);
 
         return f;
     }
 	
 	@Override
 	public Dialog onCreateDialog(Bundle savedInstanceState) 
 	{
 		super.onCreateDialog(savedInstanceState);
 		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
 				.setTitle(" ")
 				.setPositiveButton("", new DialogInterface.OnClickListener() {
 					
 					@Override
 					public void onClick(DialogInterface dialog, int which) 
 					{
 						//do somthing with Positive input;
 						//validate the data insert is OK 
 						
 					}
 				})
 				.setNegativeButton("", new DialogInterface.OnClickListener() {
 					
 					@Override
 					public void onClick(DialogInterface dialog, int which) 
 					{
 						dialog.dismiss();	
 					}
 				});
 		
 		view = getActivity().getLayoutInflater().inflate(R.layout.add_friends_fragment_layout, null, false);
 		
 		ListView friendListView = (ListView) view.findViewById(R.id.friends_list_view);
 		friendListView.setAdapter(new FriendListBaseAdapter(getActivity(), getFriends()));
 		 
 	/*	CheckBox checkBox = (CheckBox) view.findViewById(R.id.friend_check_box);
 		checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 			
 			@Override
 			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 				
 				ListView friendListView = (ListView) view.findViewById(R.id.friends_list_view);
 				CampusInUserChecked item = (CampusInUserChecked) friendListView.getAdapter().getItem((Integer) buttonView.getTag());
 				item.setChecked(isChecked);
 				
 			}
 		});*/
 		
 		
 		EditText inputSearch = (EditText) view.findViewById(R.id.inputSearch);
 		inputSearch.addTextChangedListener(new TextWatcher() {
             
             @Override
             public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                 // When user changed the Text
             	ListView friendListView = (ListView) view.findViewById(R.id.friends_list_view);
             	FriendListBaseAdapter adapter = (FriendListBaseAdapter) friendListView.getAdapter();     
             	adapter.getFilter().filter(cs); 
             }
              
             @Override
             public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                     int arg3) {
                 // TODO Auto-generated method stub
                  
             }
 
 			@Override
 			public void afterTextChanged(Editable arg0) {
 				// TODO Auto-generated method stub
 				
 			}
              
         });
 		
 		 
         builder.setView(view);
         return builder.create();
 	}
 
 	private ArrayList<CampusInUserChecked> getFriends() 
 	{
 		ArrayList<CampusInUserChecked> toReturn = new ArrayList<CampusInUserChecked>(); 
 		CampusInUserChecked curr; 
 		CampusInUser user; 
 		for (int i=1; i< 40 ; i++)
 		{
 			curr = new CampusInUserChecked();
 			user = new CampusInUser();
 			user.setFirstName("first" + i);
 			user.setLastName("Last" + i);
 			
 			curr.setUser(user);
 			toReturn.add(curr);
 		}
 		
 		curr = new CampusInUserChecked();
 		user = new CampusInUser();
 		user.setFirstName("yaki");
 		user.setLastName("Amsalem");
 		curr.setUser(user);
 		toReturn.add(curr);
 		return toReturn;
 	}
 
 
 }
