 package com.lifecity.felux;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.support.v4.app.ListFragment;
 import android.util.Log;
 import android.view.*;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import com.lifecity.felux.items.Item;
 
 import java.util.List;
 
 /**
  *
  */
 public abstract class ItemListFragment<T> extends ListFragment implements ItemDetailCallbacks<Item>, FeluxManagerCallbacks {
     protected ArrayAdapter<T> adapter;
     protected List<T> items;
     protected FeluxManager manager;
 
     /**
      * The serialization (saved instance state) Bundle key representing the
      * activated item position. Only used on tablets.
      */
     public static String TAG;
 
     /**
      * The fragment's current callback object, which is notified of list item
      * clicks.
      */
     protected ItemListCallbacks itemListCallbacks = emptyItemListCallbacks;
 
     /**
      * The current activated item position. Only used on tablets.
      */
     protected int mActivatedPosition = ListView.INVALID_POSITION;
 
     protected static class EmptyItemListCallbacks implements ItemListCallbacks<Item> {
         @Override
         public void onItemSelected(int position, Item item) {
         }
 
         @Override
         public void onItemAdded(Item item) {
         }
 
         @Override
         public void onItemUpdated(Item item) {
 
         }
 
     }
 
     public void setFeluxManager(FeluxManager manager) {
         this.manager = manager;
     }
 
     public int getNumItems() {
         return items.size();
     }
 
     public T getItemAt(int index) {
         return items.get(index);
     }
 
     /**
      */
     protected static ItemListCallbacks emptyItemListCallbacks = new EmptyItemListCallbacks();
 
     /**
      * Mandatory empty constructor for the fragment manager to instantiate the
      * fragment (e.g. upon screen orientation changes).
      */
     public ItemListFragment() {
         setRetainInstance(true);
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);

        adapter = new ArrayAdapter<T>(
                getActivity(),
                android.R.layout.simple_list_item_activated_1,
                android.R.id.text1,
                items
        );

        setListAdapter(adapter);
         setHasOptionsMenu(true);
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         return inflater.inflate(R.layout.fragment_item_list, container, false);
     }
 
     @Override
     @SuppressWarnings("unchecked")
     public void onViewStateRestored(Bundle savedInstanceState) {
         super.onViewStateRestored(savedInstanceState);
 
         // Restore the previously serialized activated item position.
         ListView listView = getListView();
         int position = listView.getCount() == 0 ? -1 : listView.getCheckedItemPosition();
         if (position < 0 && items.size() > 0) {
             position = 0;
             listView.setItemChecked(position, true);
         }
 
         itemListCallbacks.onItemSelected(position, position < 0 ? null : items.get(position));
 
         getActivity().invalidateOptionsMenu();
     }
 
     @Override
     public void onAttach(Activity activity) {
         super.onAttach(activity);
 
         // Activities containing this fragment must implement its callbacks.
         if (!(activity instanceof ItemListCallbacks)) {
             throw new IllegalStateException("Activity must implement fragment's callbacks.");
         }
 
         itemListCallbacks = (ItemListCallbacks) activity;
     }
 
     @Override
     public void onPause() {
         super.onPause();
         Log.i(this.getClass().getSimpleName(), "onPause");
     }
 
     @Override
     public void onDetach() {
         super.onDetach();
 
         // Reset the active callbacks interface to the dummy implementation.
         itemListCallbacks = emptyItemListCallbacks;
         Log.i(this.getClass().getSimpleName(), "onDetach");
     }
 
     @Override
     @SuppressWarnings("unchecked")
     public void onListItemClick(ListView listView, View view, int position, long id) {
         super.onListItemClick(listView, view, position, id);
         // Notify the active callbacks interface (the activity, if the
         // fragment is attached to one) that an item has been selected.
         itemListCallbacks.onItemSelected(0, items.get(position));
         getActivity().invalidateOptionsMenu();
     }
 
     @SuppressWarnings("unchecked")
     public void setActivatedPosition(int position) {
         if (position == ListView.INVALID_POSITION) {
             getListView().setItemChecked(mActivatedPosition, false);
             itemListCallbacks.onItemSelected(-1, null);
         } else {
             getListView().setItemChecked(position, true);
             itemListCallbacks.onItemSelected(position, items.get(position));
         }
 
         getActivity().invalidateOptionsMenu();
         mActivatedPosition = position;
     }
 
     @SuppressWarnings("unchecked")
     public void addItem(T item) {
         items.add(item);
         adapter.notifyDataSetChanged();
         itemListCallbacks.onItemAdded(item);
     }
 
     public void removeItem(T item) {
         int oldPosition = selectedPosition();
         int newPosition = oldPosition == 0 ? 0 : oldPosition - 1;
         items.remove(item);
         adapter.notifyDataSetChanged();
         if (items.size() == 0) {
             newPosition = -1;
         }
         setActivatedPosition(newPosition);
     }
 
     public void moveItem(T item, boolean down) {
         int oldPosition = selectedPosition();
         int newPosition;
 
         if (down) {
             if (oldPosition < (items.size() - 1)) {
                 newPosition = oldPosition + 1;
             }  else {
                 return;
             }
         } else {
             if (oldPosition > 0) {
                 newPosition = oldPosition - 1;
             } else {
                 return;
             }
         }
 
         T temp = items.get(newPosition);
         items.set(newPosition, item);
         items.set(oldPosition, temp);
         adapter.notifyDataSetChanged();
         setActivatedPosition(newPosition);
     }
 
     @Override
     public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
         inflater.inflate(R.menu.fragment_list_menu, menu);
     }
 
     @Override
     public void onPrepareOptionsMenu(Menu menu) {
         int position = selectedPosition();
         MenuItem upItem = menu.findItem(R.id.item_moveup);
         MenuItem downItem = menu.findItem(R.id.item_movedown);
         MenuItem copyItem = menu.findItem(R.id.item_copy);
         MenuItem removeItem = menu.findItem(R.id.item_remove);
 
         copyItem.setVisible(items.size() > 0);
         removeItem.setVisible(items.size() > 0);
 
         if (position >= 0 && items.size() > 0) {
             if (upItem != null) {
                 upItem.setVisible(position > 0);
             }
             if (downItem != null) {
                 downItem.setVisible(position < (items.size() - 1));
             }
         } else {
             if (upItem != null) {
                 upItem.setVisible(false);
             }
             if (downItem != null) {
                 downItem.setVisible(false);
             }
         }
     }
 
     public int selectedPosition() {
         return getListView().getCheckedItemPosition();
     }
 
     public T selectedItem() {
         return items.get(selectedPosition());
     }
 
     @Override
     @SuppressWarnings("unchecked")
     public boolean onOptionsItemSelected(MenuItem item) {
     switch (item.getItemId()) {
         case R.id.item_moveup:
             moveItem(selectedItem(), false);
             return true;
         case R.id.item_movedown:
             moveItem(selectedItem(), true);
             return true;
         case R.id.item_copy:
             addItem((T)((Item)selectedItem()).copy());
             return true;
         case R.id.item_remove:
             ConfirmDialogFragment dialog = new ConfirmDialogFragment("Delete", "Are you sure you want to delete this?", new ConfirmDialogFragment.ConfirmDialogListener() {
                 @Override
                 public void onConfirm() {
                     removeItem(selectedItem());
                     getActivity().invalidateOptionsMenu();
                 }
             });
             dialog.show(getActivity().getSupportFragmentManager(), "confirm_remove_dialog_tag");
             return true;
         /*case R.id.item_edit:
             startActionMode();
             return true;
             */
         case R.id.item_add:
             getActivity().invalidateOptionsMenu();
             return true;
         default:
             return super.onOptionsItemSelected(item);
     }
   }
 
     @Override
     @SuppressWarnings("unchecked")
     public void onItemDetailUpdated(Item item) {
         adapter.notifyDataSetChanged();
         itemListCallbacks.onItemUpdated(item);
     }
 
     @Override
     public void onItemDetailAdded(Item item) {
         setActivatedPosition(items.size() - 1);
     }
 
     @Override
     public void onItemsLoaded(FeluxManager manager) {
        adapter.clear();
        adapter.addAll(items);
        adapter.notifyDataSetChanged();
         if (adapter.getCount() > 0) {
             setActivatedPosition(0);
         }
     }
 }
