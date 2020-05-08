 package com.quanturium.androcloud2.fragments;
 
 import android.view.ActionMode;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.widget.Toast;
 
 import com.quanturium.androcloud2.FragmentInitParams;
 import com.quanturium.androcloud2.R;
 
 
 public class FilesMainFragment extends FilesAbstractFragment
 {
 	protected boolean willDisplayTrash()
 	{
 		return false;
 	}
 	
 	@Override
 	protected FragmentInitParams init()
 	{
 		return new FragmentInitParams(R.layout.fragment_files, "Files", null, true, true);
 	}
 	
 	@Override
 	public boolean onActionItemClicked(ActionMode mode, MenuItem item)
 	{
 		long[] checkedItems;
 
 		switch (item.getItemId())
 		{
 			case R.id.multiItemSave:
 
 				checkedItems = getListView().getCheckedItemIds();
				Toast.makeText(getActivity(), R.string.to_be_implemented, Toast.LENGTH_SHORT).show();
 
 				break;
 
 			case R.id.multiItemDelete:
 
 				checkedItems = getListView().getCheckedItemIds();
				Toast.makeText(getActivity(), R.string.to_be_implemented, Toast.LENGTH_SHORT).show();
 				// getListView().setItemChecked(0, false);
 
 				break;
 		}
 
 		mode.finish();
 
 		return true;
 	}
 
 	@Override
 	public boolean onCreateActionMode(ActionMode mode, Menu menu)
 	{
 		MenuInflater inflater = getActivity().getMenuInflater();
 		inflater.inflate(R.menu.fragment_files_action_select, menu);
 
 		mode.setTitle("Select Items");
 
 		return true;
 	}
 
 	@Override
 	public void onItemClick(int itemId)
 	{
 		mCallbacks.onFilesItemSelected(itemId);
 	}
 }
