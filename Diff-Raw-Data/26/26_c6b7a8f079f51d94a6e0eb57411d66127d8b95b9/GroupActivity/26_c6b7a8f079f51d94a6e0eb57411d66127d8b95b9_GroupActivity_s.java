 package com.metat.main;
 
 import com.metat.contacts.R;
 import com.metat.dataaccess.ContactDataAccess;
 import com.metat.dataaccess.GroupsDataAccess;
 import com.metat.dialogs.GroupContactAction;
 import com.metat.dialogs.GroupContactDeleteConfirm;
 import com.metat.models.Contact;
 import com.metat.models.Group;
 import com.metat.models.NavigationSource;
 import com.metat.fragments.AllExistingGroupContacts;
 import com.metat.fragments.BrowseGroupContacts;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.Fragment;
 import android.app.FragmentTransaction;
 import android.content.Intent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.TabHost;
 import android.widget.TextView;
 import android.widget.TabHost.OnTabChangeListener;
 import android.widget.TabHost.TabSpec;
 
 public class GroupActivity extends Activity implements OnTabChangeListener {
     public static final String TAB_ADDED = "added";
     public static final String TAB_BROWSE = "browse";
     
 	public static Group SelectedGroup = null;
 	public static Contact[] AllContacts = new Contact[0];
 
 	private TabHost _contactSortingTabs;
 	
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.group);
         
         getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.action_bar));
 
         Bundle extras = getIntent().getExtras();
         
         GroupsDataAccess groupdDataAccess = new GroupsDataAccess(this);
         SelectedGroup = groupdDataAccess.getGroup(extras.getString("groupId"));
 
         getActionBar().setDisplayShowHomeEnabled(false);
     	setTitle(" " + SelectedGroup.getName());
     	
     	ContactDataAccess contactDataAccess = new ContactDataAccess(this);
     	AllContacts = contactDataAccess.getAllContacts(SelectedGroup.getMeetupId());
     	
         _contactSortingTabs = (TabHost)findViewById(R.id.group_contacts_sorting_tabs);
         _contactSortingTabs.setOnTabChangedListener(this); 
         _contactSortingTabs.setup();
 
         _contactSortingTabs.addTab(newTab(TAB_ADDED, R.string.contacts, R.id.contacts_container));
         _contactSortingTabs.addTab(newTab(TAB_BROWSE, R.string.browse, R.id.browse_contacts_container));
 
 		FragmentTransaction transaction = getFragmentManager().beginTransaction();
 
 		Fragment allContactsFragment;
 
 		if (savedInstanceState != null)
 		{
 			allContactsFragment = getFragmentManager().findFragmentByTag("AllExistingGroupContacts");
 		}
 		else
 		{
 			allContactsFragment = new AllExistingGroupContacts();
 			Bundle allContactsSelectedArgs = new Bundle();
 			allContactsFragment.setArguments(allContactsSelectedArgs);
 			transaction.add(R.id.contacts_container, allContactsFragment, "AllExistingGroupContacts");
 		}
 
 		Fragment browseGroupFragment;
 
 		if (savedInstanceState != null)
 		{
 			browseGroupFragment = getFragmentManager().findFragmentByTag("BrowseGroupContacts");
 		}
 		else
 		{
 			browseGroupFragment = new BrowseGroupContacts();
 			Bundle browseGroupSelectedArgs = new Bundle();
 			browseGroupSelectedArgs.putString("groupId", SelectedGroup.getMeetupId());
 			browseGroupFragment.setArguments(browseGroupSelectedArgs);
 			transaction.add(R.id.browse_contacts_container, browseGroupFragment, "BrowseGroupContacts");
 		}
 
 		transaction.commit();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu)
     {
     	getMenuInflater().inflate(R.menu.group_menu, menu);
         return true;
     }
 
     @Override
     public void onBackPressed() {
 		Intent cancelIntent = new Intent(getBaseContext(), MainActivity.class);
 		cancelIntent.putExtra("selectedTab", MainActivity.TAB_MEETUPS);
 		cancelIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 		
 		getBaseContext().startActivity(cancelIntent);	
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item)
     {
         switch (item.getItemId())
         {
     		case R.id.back:
     			Intent cancelIntent = new Intent(getBaseContext(), MainActivity.class);
 				cancelIntent.putExtra("selectedTab", MainActivity.TAB_MEETUPS);
     			cancelIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
     			
     			getBaseContext().startActivity(cancelIntent);	
         		return true;
         	case R.id.add_contact:
 				Intent intent = new Intent(getBaseContext(), AddContactActivity.class);
         		intent.putExtra("groupId", SelectedGroup.getMeetupId());
         		intent.putExtra("navigationSource", NavigationSource.GroupContacts);
 				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 				
 				getBaseContext().startActivity(intent);	
         		return true;
         }
         
         return false;
     }
     
     public void showContactActionDialog(long contactId)
     {
     	FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
         Fragment prev = getFragmentManager().findFragmentByTag("groupContactActionDialog");
         if (prev != null) {
         	fragmentTransaction.remove(prev);
         }
         fragmentTransaction.addToBackStack(null);
 
         GroupContactAction contactAction = GroupContactAction.newInstance(contactId);
         if (contactAction.getDialog() != null) {
         	contactAction.getDialog().setCancelable(true);
         	contactAction.getDialog().setCanceledOnTouchOutside(true);
         }
         
         contactAction.show(fragmentTransaction, "groupContactActionDialog");
     }
     
     public void editContactSelected(long contactId)
     {
     	FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
     	
         Fragment prevContactActionDialog = getFragmentManager().findFragmentByTag("groupContactActionDialog");
         if (prevContactActionDialog != null) {
             ((GroupContactAction)prevContactActionDialog).getDialog().dismiss();
         	fragmentTransaction.remove(prevContactActionDialog);
         }
         
         fragmentTransaction.addToBackStack(null);
         fragmentTransaction.commit();
 
 		Intent intent = new Intent(getBaseContext(), EditContactActivity.class);
 		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 		intent.putExtra("contactId", contactId);
 		intent.putExtra("groupId", SelectedGroup.getMeetupId());
 		intent.putExtra("navigationSource", NavigationSource.GroupContacts);
 
 		getBaseContext().startActivity(intent);	
     }
     
     public void deleteContactSelected(long contactId)
     {
     	FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
     	
         Fragment prevContactActionDialog = getFragmentManager().findFragmentByTag("groupContactActionDialog");
         if (prevContactActionDialog != null) {
             ((GroupContactAction)prevContactActionDialog).getDialog().dismiss();
         	fragmentTransaction.remove(prevContactActionDialog);
         }
         
         Fragment prevDeleteConfirmDialog = getFragmentManager().findFragmentByTag("groupDeleteConfirmDialog");
         if (prevDeleteConfirmDialog != null) {
         	fragmentTransaction.remove(prevDeleteConfirmDialog);
         }
         fragmentTransaction.addToBackStack(null);
         fragmentTransaction.commit();
         
         fragmentTransaction = getFragmentManager().beginTransaction();
         
         GroupContactDeleteConfirm contactDeleteConfirm = GroupContactDeleteConfirm.newInstance(contactId);
         if (contactDeleteConfirm.getDialog() != null) {
         	contactDeleteConfirm.getDialog().setCancelable(true);
         	contactDeleteConfirm.getDialog().setCanceledOnTouchOutside(true);
         }
         contactDeleteConfirm.show(fragmentTransaction, "groupDeleteConfirmDialog");
     }
     
     public void deleteContactConfirmed(long contactId)
     {
     	FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
     	
         Fragment prevDeleteConfirmDialog = getFragmentManager().findFragmentByTag("groupDeleteConfirmDialog");
         if (prevDeleteConfirmDialog != null) {
             ((GroupContactDeleteConfirm)prevDeleteConfirmDialog).getDialog().dismiss();
         	fragmentTransaction.remove(prevDeleteConfirmDialog);
         }
 
         fragmentTransaction.commit();
         
         ContactDataAccess contactDataAccess = new ContactDataAccess(this);
         contactDataAccess.Delete(contactId);
 
     	AllContacts = contactDataAccess.getAllContacts(SelectedGroup.getMeetupId());
         
         fragmentTransaction = getFragmentManager().beginTransaction();
         Fragment allExistingContacts = getFragmentManager().findFragmentByTag("AllExistingGroupContacts");
         if (allExistingContacts != null)
         {
         	((AllExistingGroupContacts)allExistingContacts).bindContactsAdapter();
         }
     }
 
 	@Override
 	public void onTabChanged(String arg0) {
 		updateTabStyles();
 	}
 
     /// Private Methods
     
     private TabSpec newTab(String tag, int label, int tabContentId) 
     {        
     	View indicator = LayoutInflater.from(this).inflate(R.layout.contact_sorting_tab, null);
     	((TextView) indicator.findViewById(R.id.sorting_lbl)).setText(label);
     	TabSpec tabSpec = _contactSortingTabs.newTabSpec(tag);
     	tabSpec.setIndicator(indicator);
     	tabSpec.setContent(tabContentId);
 
     	return tabSpec; 
     }
 	
 	private void updateTabStyles()
 	{
 		for (int i=0; i<_contactSortingTabs.getTabWidget().getChildCount(); i++)
 		{
 			_contactSortingTabs.getTabWidget().getChildAt(i).setBackgroundDrawable(getResources().getDrawable(R.drawable.contacts_tab_inactive));
 		}
 
 		_contactSortingTabs.getTabWidget().getChildAt(_contactSortingTabs.getCurrentTab()).setBackgroundDrawable(getResources().getDrawable(R.drawable.contacts_tab_selected));
 	}
 }
