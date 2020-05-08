 package il.ac.shenkar.cadan;
 
 import il.ac.shenkar.cadan.ChooseFriendsFragment.ChooseFriendAction;
 import il.ac.shenkar.cadan.ChooseFriendsFragment.onFriendsAddedListener;
 import il.ac.shenkar.common.CampusInUser;
 import il.ac.shenkar.common.CampusInUserChecked;
 import il.ac.shenkar.in.bl.Controller;
 import il.ac.shenkar.in.bl.ControllerCallback;
 import il.ac.shenkar.in.bl.ICampusInController;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.DialogFragment;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.Toast;
 
 public class AddOrRemoveFriendsFromCloudFragment extends DialogFragment
 {
     onFriendsAddedListener mCallback;
     protected View view;
     protected List<CampusInUser> friensList;
     protected ChooseFriendAction action;
     protected ICampusInController controller = null;
     ProgressDialog progressDialog;
 
     static AddOrRemoveFriendsFromCloudFragment newInstance(ChooseFriendAction action)
     {
 	AddOrRemoveFriendsFromCloudFragment f = new AddOrRemoveFriendsFromCloudFragment();
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
 	if (action == ChooseFriendAction.ADD)
 	{
 	    builder.setTitle(R.string.add_friends).setPositiveButton(R.string.add, new DialogInterface.OnClickListener()
 	    {
 
 		@Override
 		public void onClick(DialogInterface dialog, int which)
 		{
 		    ListView friendListView = (ListView) view.findViewById(R.id.friends_list_view);
 		    ChooseFriendListBaseAdapter adapter = (ChooseFriendListBaseAdapter) friendListView.getAdapter();
 
 		    ArrayList<CampusInUser> toReturn = new ArrayList<CampusInUser>();
 		    CampusInUserChecked currUserChecked;
 		    // iterate the list of friends and add the
 		    // check ones to the return list
 		    for (int i = 0; i < adapter.getCount(); i++)
 		    {
 			currUserChecked = (CampusInUserChecked) adapter.getItem(i);
 			if (currUserChecked.isChecked())
 			{
 			    toReturn.add(currUserChecked.getUser());
 			}
 		    }
 		    mCallback.onFriendsWereChoosen(toReturn, getTargetFragment(), ChooseFriendAction.ADD);
 		}
 	    }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
 	    {
 
 		@Override
 		public void onClick(DialogInterface dialog, int which)
 		{
 		    // no button preesed - dismiss dialog
 		    dialog.dismiss();
 		}
 	    });
 	}
 	else
 	{
 	    builder.setTitle(R.string.add_friends).setPositiveButton(R.string.remove, new DialogInterface.OnClickListener()
 	    {
 
 		@Override
 		public void onClick(DialogInterface dialog, int which)
 		{
 		    ListView friendListView = (ListView) view.findViewById(R.id.friends_list_view);
 		    ChooseFriendListBaseAdapter adapter = (ChooseFriendListBaseAdapter) friendListView.getAdapter();
 
 		    ArrayList<CampusInUser> toReturn = new ArrayList<CampusInUser>();
 		    CampusInUserChecked currUserChecked;
 		    // iterate the list of friends and add the
 		    // check ones to the return list
 		    for (int i = 0; i < adapter.getCount(); i++)
 		    {
 			currUserChecked = (CampusInUserChecked) adapter.getItem(i);
 			if (currUserChecked.isChecked())
 			{
 			    toReturn.add(currUserChecked.getUser());
 			}
 		    }
 
 		    mCallback.onFriendsWereChoosen(toReturn, getTargetFragment(), ChooseFriendAction.REMOVE);
 		}
 	    }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
 	    {
 
 		@Override
 		public void onClick(DialogInterface dialog, int which)
 		{
 		    // no button preesed - dismiss dialog
 		    dialog.dismiss();
 		}
 	    });
 	}
 
 	// this part is for filtering the list of friends
 	EditText inputSearch = (EditText) view.findViewById(R.id.inputSearch);
 	setFilterListener(inputSearch);
 	
 	builder.setIcon(R.drawable.campus_in_ico);
 	builder.setView(view);
 	return builder.create();
     }
 
     public void setFilterListener(EditText inputSearch)
     {
 	inputSearch.addTextChangedListener(new TextWatcher()
 	{
 
 	    @Override
 	    public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3)
 	    {
 		// When user changed the Text
 		ListView friendListView = (ListView) view.findViewById(R.id.friends_list_view);
 		ChooseFriendListBaseAdapter adapter = (ChooseFriendListBaseAdapter) friendListView.getAdapter();
 		adapter.getFilter().filter(cs);
 	    }
 
 	    @Override
 	    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3)
 	    {
 		// TODO Auto-generated method stub
 
 	    }
 
 	    @Override
 	    public void afterTextChanged(Editable arg0)
 	    {
 		// TODO Auto-generated method stub
 
 	    }
 
 	});
     }
 
     @Override
     public void onAttach(Activity activity)
     {
 	super.onAttach(activity);
 	// This makes sure that the container activity has implemented
 	// the callback interface. If not, it throws an exception
 	try
 	{
 	    mCallback = (onFriendsAddedListener) activity;
 	}
 	catch (ClassCastException e)
 	{
 	    throw new ClassCastException(activity.toString() + " must implement IonFriendsAddedListener");
 	}
     }
 
     /**
      * this method is ment to bring all of the friends from cloud right on the
      * fragment is beeing started
      */
     protected void initFriendList()
     {
 	// progressDialog = ProgressDialog.show(getActivity(),
 	// "Loading Friends", "Loading FRiends from cloud");
 	if (action == ChooseFriendAction.ADD)
 	{
 	    // the user want to add friends to his friend list
 	    // i need to get all users from the cloud
 	    controller.getCurrentUser(new ControllerCallback<CampusInUser>()
 	    {
 		@Override
 		public void done(final CampusInUser curretntUser, Exception e)
 		{
 		    if (e == null && curretntUser != null)
 		    {
 			controller.getAllCumpusInUsers(new ControllerCallback<List<CampusInUser>>()
 			{
 			    @Override
 			    public void done(List<CampusInUser> retObject, Exception e)
 			    {
 				friensList = new ArrayList<CampusInUser>();
 				if (retObject != null)
 				{
 				    for (CampusInUser campusInUser : retObject)
 				    {
 					if (controller.isMyFriend(campusInUser))
 					    continue;
 					friensList.add(campusInUser);
 				    }
 				}
 				Toast.makeText(getActivity(), "number of friends:" + retObject.size(), 3000).show();
 				ListView friendListView = (ListView) view.findViewById(R.id.friends_list_view);
 				friendListView.setAdapter(new ChooseFriendListBaseAdapter(getActivity(), getFriends(), curretntUser));
 				progressDialog.dismiss();
 			    }
 			});
 		    }
 		}
 	    });
 	}
 	else
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
					if (!controller.isMyFriendToSchool(campusInUser))
 					    friensList.add(campusInUser);
 				    }
 
 				    Toast.makeText(getActivity(), "number of friends:" + retObject.size(), 3000).show();
 				    if (view == null)
 					view = getActivity().getLayoutInflater().inflate(R.layout.add_friends_fragment_layout, null, false);
 				    ListView friendListView = (ListView) view.findViewById(R.id.friends_list_view);
 				    friendListView.setAdapter(new ChooseFriendListBaseAdapter(getActivity(), getFriends(), curretntUser));
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
 
     @Override
     public void onStart()
     {
 	super.onStart();
 	this.getDialog().setCanceledOnTouchOutside(false);
 	if (action == ChooseFriendAction.ADD)
 	    progressDialog = ProgressDialog.show(getActivity(), "Loading Friends", "Loading FRiends from cloud");
     }
 
     protected ArrayList<CampusInUserChecked> getFriends()
     {
 
 	ArrayList<CampusInUserChecked> toReturn = new ArrayList<CampusInUserChecked>();
 	// add all the friend to friendList
 	// wrap them with CheckedCampusInUSer Object
 
 	if (this.friensList == null)
 	{
 	    return null;
 	}
 	for (CampusInUser friend : friensList)
 	{
 	    CampusInUserChecked u = new CampusInUserChecked(friend);
 	    u.setChecked(false);
 	    toReturn.add(u);
 	}
 	return toReturn;
     }
 }
