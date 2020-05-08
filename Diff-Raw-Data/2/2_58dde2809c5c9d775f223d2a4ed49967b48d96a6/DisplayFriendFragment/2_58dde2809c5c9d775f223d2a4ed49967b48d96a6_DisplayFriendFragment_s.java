 package il.ac.shenkar.cadan;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import il.ac.shenkar.cadan.ChooseFriendsFragment.ChooseFriendAction;
 import il.ac.shenkar.common.CampusInUser;
 import il.ac.shenkar.in.bl.Controller;
 import il.ac.shenkar.in.bl.ControllerCallback;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.Toast;
 
 public class DisplayFriendFragment extends AddOrRemoveFriendsFromCloudFragment
 {
     static DisplayFriendFragment newInstance(ChooseFriendAction action)
     {
 	DisplayFriendFragment f = new DisplayFriendFragment();
 	// Supply num input as an argument.
 	Bundle args = new Bundle();
 	args.putSerializable("action", action);
 	f.setArguments(args);
 	f.action = action;
 	return f;
     }
 
     @Override
     public Dialog onCreateDialog(Bundle savedInstanceState)
     {
 	super.onCreateDialog(savedInstanceState);
 	controller = Controller.getInstance(getActivity());
 	initFriendList();
 	if (view == null)
 	    view = getActivity().getLayoutInflater().inflate(R.layout.add_friends_fragment_layout, null, false);
 	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 	builder.setTitle(" ").setPositiveButton(R.string.close, new DialogInterface.OnClickListener()
 	{
 
 	    @Override
 	    public void onClick(DialogInterface dialog, int which)
 	    {
 		dialog.dismiss();
 	    }
 	});
 	// this part is for filtering the list of friends
 	EditText inputSearch = (EditText) view.findViewById(R.id.inputSearch);
 	setFilterListener(inputSearch);
 
 	builder.setIcon(R.drawable.campus_in_ico);
 	builder.setView(view);
 	return builder.create();
     }
 
     @Override
     protected void initFriendList()
     {
 	// the user want to remove friends from his friend list
 	// i need to get only his friends from the cloud
 	controller.getCurrentUser(new ControllerCallback<CampusInUser>()
 	{
 
 	    @Override
 	    public void done(final CampusInUser curretntUser, Exception e)
 	    {
 		if (e == null && curretntUser != null)
 		{
 		    controller.getCurrentUserFriendList(new ControllerCallback<List<CampusInUser>>()
 		    {
 
 			@Override
 			public void done(List<CampusInUser> retObject, Exception e)
 			{
 			    if (retObject != null)
 			    {
 				friensList = new ArrayList<CampusInUser>();
 				// TODO this is not suppose o be here it
 				// must be removed to the controller
 				// TOTO in addition the controller shouldn't
 				// get any callback this data exist
 				// locally-change the method in the
 				// controller
 				for (CampusInUser campusInUser : retObject)
 				{
				    if (controller.isMyFriendToSchool(campusInUser))
 					friensList.add(campusInUser);
 				}
 
 				Toast.makeText(getActivity(), "number of friends:" + retObject.size(), 3000).show();
 				if (view == null)
 				    view = getActivity().getLayoutInflater().inflate(R.layout.add_friends_fragment_layout, null, false);
 				ListView friendListView = (ListView) view.findViewById(R.id.friends_list_view);
 				friendListView.setAdapter(new DiaplayFriendListBaseAdaptor(getActivity(), getFriends(), curretntUser, DisplayFriendFragment.this));
 				if (progressDialog != null)
 				    progressDialog.dismiss();
 			    }
 
 			}
 		    });
 		}
 	    }
 	});
     }
 
 }
