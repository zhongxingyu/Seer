 package cc.hughes.droidchatty;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 import android.os.AsyncTask;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 
 public abstract class LoadingAdapter<T> extends ArrayAdapter<T>
 {
     protected abstract View createView(int position, View convertView, ViewGroup parent);
     protected abstract ArrayList<T> loadData() throws Exception;
     protected void afterDisplay() { }
     
     private List<T> _items;
     private int _normalResource;
     private int _loadingResource;
     private View _loadingView;
     private boolean _moreToLoad = true;
     
     LayoutInflater _inflater;
     
     public LoadingAdapter(Context context, int resource, int loadingResource, List<T> objects)
     {
         super(context, 0, 0, objects);
         _inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         _normalResource = resource;
         _loadingResource = loadingResource;
         _items = objects;
     }
     
     public List<T> getItems()
     {
         return _items;
     }
     
     @Override
     public T getItem(int position)
     {
         if (position == super.getCount())
             return null;
         return super.getItem(position);
     }
 
     @Override
     public int getItemViewType(int position)
     {
         if (position == super.getCount())
             return IGNORE_ITEM_VIEW_TYPE;
         
         return super.getItemViewType(position);
     }
 
     @Override
     public int getViewTypeCount()
     {
         // regular view + loading view
         return super.getViewTypeCount() + 1;
     }
     
     @Override
     public int getCount()
     {
        if (getMoreToLoad())
             return super.getCount() + 1;
         return super.getCount();
     }
     
     @Override
     public void add(T item)
     {
         _items.add(item);
     }
     
     @Override
     public void clear()
     {
         _items.clear();
         super.clear();
         _moreToLoad = true;
         _loadingView = null;
     }
     
     protected boolean getMoreToLoad()
     {
         return _moreToLoad;
     }
 
     @Override
     public View getView(int position, View convertView, ViewGroup parent)
     {
         if (position == super.getCount())
         {
             if (_loadingView == null)
             {
                 _loadingView = getLoadingView(parent);
                 new LoadAndAppendTask().execute();
             }
             return _loadingView;
         }
         
         if (convertView == null)
             convertView = _inflater.inflate(_normalResource, null);
         
         return createView(position, convertView, parent);
     }
     
     private View getLoadingView(ViewGroup parent)
     {
         return _inflater.inflate(_loadingResource, parent, false);
     }
     
     class LoadAndAppendTask extends AsyncTask<Void, Void, ArrayList<T>>
     {
         Exception _exception;
         
         @Override
         protected ArrayList<T> doInBackground(Void... arg0)
         {
             try
             {
                 return loadData();
             }
             catch (Exception e)
             {
                 Log.e("DroidChatty", "Error loading data.", e);
                 _exception = e;
             }
             
             return null;
         }
 
         @Override
         protected void onPostExecute(ArrayList<T> result)
         {
             if (_exception != null)
             {
                _moreToLoad = false;
                ErrorDialog.display(getContext(), "Error", "Error loading data."); 
             }
             else if (result != null)
             {
                 if (result.size() == 0)
                 {
                     _moreToLoad = false;
                 }
                 else
                 {
                     for (T item : result)
                         add(item);
                 }
             }
             
             // dataset changed, either there are new items, or the count went down (no more "Loading")
             notifyDataSetChanged();
             _loadingView = null;
             
             // if there wasn't an error, run the after process
             if (_moreToLoad)
                 afterDisplay();
         }
     }
     
 }
