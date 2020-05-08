 package cc.hughes.droidchatty;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 import android.os.AsyncTask;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 
 public abstract class LoadingAdapter<T> extends ArrayAdapter<T>
 {
     protected abstract View createView(int position, View convertView, ViewGroup parent);
     protected abstract ArrayList<T> loadData();
     
     private List<T> _items;
     private int _loadingResource;
     private View _loadingView;
     
     public LoadingAdapter(Context context, int resource, int loadingResource, List<T> objects)
     {
         super(context, resource, 0, objects);
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
         return super.getCount() + 1;
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
         
         return createView(position, convertView, parent);
     }
     
     private View getLoadingView(ViewGroup parent)
     {
         LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         return inflater.inflate(_loadingResource, parent, false);
     }
     
     class LoadAndAppendTask extends AsyncTask<Void, Void, ArrayList<T>>
     {
         @Override
         protected ArrayList<T> doInBackground(Void... arg0)
         {
             return loadData();
         }
 
         @Override
         protected void onPostExecute(ArrayList<T> result)
         {
             for (T item : result)
                 add(item);
             
             _loadingView = null;
             notifyDataSetChanged();
         }
     }
     
 }
