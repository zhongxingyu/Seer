 package net.somethingdreadful.MAL;
 
 import java.util.ArrayList;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
import android.content.res.Resources;
 import android.os.AsyncTask;
 import android.os.Bundle;
import android.util.DisplayMetrics;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.WindowManager;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.GridView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockFragment;
 
 public class ItemGridFragment extends SherlockFragment {
 
     // The pixel dimensions used by MAL images
     private static final double MAL_IMAGE_WIDTH = 225;
     private static final double MAL_IMAGE_HEIGHT = 320;
 
     public ItemGridFragment() {
     }
 
     ArrayList<AnimeRecord> al = new ArrayList();
     ArrayList<MangaRecord> ml = new ArrayList();
     GridView gv;
     MALManager mManager;
     PrefManager mPrefManager;
     Context c;
     CoverAdapter<AnimeRecord> ca;
     CoverAdapter<MangaRecord> cm;
     IItemGridFragment Iready;
     boolean forceSyncBool = false;
     boolean useTraditionalList = false;
     int currentList;
     int listColumns;
     String recordType;
 
     @Override
     public void onCreate(Bundle state)
     {
         super.onCreate(state);
 
         if (state != null)
         {
             currentList = state.getInt("list", 1);
             useTraditionalList = state.getBoolean("traditionalList");
         }
 
     }
 
     @SuppressLint("NewApi")
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
             Bundle savedInstanceState) {
 
         Bundle args = getArguments();
         View layout = inflater.inflate(R.layout.fragment_animelist, null);
         c = layout.getContext();
 
         mManager = ((Home) getActivity()).mManager;
         mPrefManager = ((Home) getActivity()).mPrefManager;
 
         final String recordType = args.getString("type");
 
         if (!((Home) getActivity()).instanceExists)
         {
             currentList = mPrefManager.getDefaultList();
             useTraditionalList = mPrefManager.getTraditionalListEnabled();
         }
 
 
         int orientation = layout.getContext().getResources().getConfiguration().orientation;
 
         gv = (GridView) layout.findViewById(R.id.gridview);
 
 
         if ("anime".equals(recordType))
         {
             gv.setOnItemClickListener(new OnItemClickListener()
             {
                 public void onItemClick(AdapterView<?> parent, View v, int position, long id)
                 {
                     Intent startDetails = new Intent(getView().getContext(), DetailView.class);
                     startDetails.putExtra("net.somethingdreadful.MAL.recordID", ca.getItem(position).recordID);
                     startDetails.putExtra("net.somethingdreadful.MAL.recordType", recordType);
 
                     startActivity(startDetails);
 
                     //				Toast.makeText(c, ca.getItem(position).getID(), Toast.LENGTH_SHORT).show();
                 }
             });
         }
         else if("manga".equals(recordType))
         {
             gv.setOnItemClickListener(new OnItemClickListener()
             {
                 public void onItemClick(AdapterView<?> parent, View v, int position, long id)
                 {
                     Intent startDetails = new Intent(getView().getContext(), DetailView.class);
                     startDetails.putExtra("net.somethingdreadful.MAL.recordID", cm.getItem(position).recordID);
                     startDetails.putExtra("net.somethingdreadful.MAL.recordType", recordType);
 
                     startActivity(startDetails);
                     //				Toast.makeText(c, ca.getItem(position).getID(), Toast.LENGTH_SHORT).show();
                 }
             });
         }
 
         if (useTraditionalList)
         {
             listColumns = 1;
         }
         else
         {
             try
             {
                 listColumns = (int) Math.ceil(layout.getContext().getResources().getConfiguration().screenWidthDp / MAL_IMAGE_WIDTH);
             }
             catch (NoSuchFieldError e)
             {
                listColumns = (int) Math.ceil(pxToDp(((WindowManager) c.getSystemService(c.WINDOW_SERVICE)).getDefaultDisplay().getWidth()) / MAL_IMAGE_WIDTH);
             }
         }
 
         gv.setNumColumns(listColumns);
 
         gv.setDrawSelectorOnTop(true);
 
         //   	gv.setAdapter(new CoverAdapter<String>(layout.getContext(), R.layout.grid_cover_with_text_item, ar));
 
         getRecords(currentList, recordType, false);
 
         Iready.fragmentReady();
 
         return layout;
 
     }
 
     public void getRecords(int listint, String mediaType, boolean forceSync)
     {
         forceSyncBool = forceSync;
         currentList = listint;
         recordType = mediaType;
 
         if(recordType == "anime") {
             new getAnimeRecordsTask().execute(currentList);
         }
         else if(recordType == "manga") {
             new getMangaRecordsTask().execute(currentList);
         }
 
 
     }
 
     public class getAnimeRecordsTask extends AsyncTask<Integer, Void, ArrayList<AnimeRecord>>
     {
 
         boolean mForceSync = forceSyncBool;
         int mList = currentList;
         boolean mTraditionalList = useTraditionalList;
         String type = recordType;
         MALManager internalManager = mManager;
 
         @SuppressWarnings({ "rawtypes", "unchecked" })
         @Override
         protected ArrayList<AnimeRecord> doInBackground(Integer... list) {
 
             int listint = 0;
 
             for(int i : list)
             {
                 listint = i;
                 System.out.println("int passed: " + listint);
             }
 
             if (mForceSync)
             {
                 al = new ArrayList();
 
                 mManager.downloadAndStoreList("anime");
 
             }
 
             al = mManager.getAnimeRecordsFromDB(listint);
 
             return al;
         }
 
         @Override
         protected void onPostExecute(ArrayList<AnimeRecord> result) {
 
             if (result == null)
             {
                 result = new ArrayList();
             }
             if (ca == null)
             {
                 if (mTraditionalList)
                 {
                     ca = new CoverAdapter<AnimeRecord>(c, R.layout.list_cover_with_text_item, result, internalManager, type);
                 }
                 else
                 {
                     ca = new CoverAdapter<AnimeRecord>(c, R.layout.grid_cover_with_text_item, result, internalManager, type);
 
                 }
             }
 
             if (gv.getAdapter() == null)
             {
                 gv.setAdapter(ca);
             }
             else
             {
                 ca.clear();
                 ca.supportAddAll(result);
                 ca.notifyDataSetChanged();
             }
 
             if (mForceSync)
             {
                 Toast.makeText(c, R.string.toast_SyncDone, Toast.LENGTH_SHORT).show();
             }
 
         }
 
     }
 
     public class getMangaRecordsTask extends AsyncTask<Integer, Void, ArrayList<MangaRecord>>
     {
         boolean mForceSync = forceSyncBool;
         int mList = currentList;
         boolean mTraditionalList = useTraditionalList;
         String type = recordType;
         MALManager internalManager = mManager;
 
         @SuppressWarnings({ "rawtypes", "unchecked" })
         @Override
         protected ArrayList<MangaRecord> doInBackground(Integer... list) {
 
             int listint = 0;
 
             for(int i : list)
             {
                 listint = i;
                 System.out.println("int passed: " + listint);
             }
 
             if (mForceSync)
             {
                 al = new ArrayList();
 
                 mManager.downloadAndStoreList("manga");
             }
 
             ml = mManager.getMangaRecordsFromDB(listint);
 
             return ml;
         }
 
         @Override
         protected void onPostExecute(ArrayList<MangaRecord> result) {
 
             if (result == null)
             {
                 result = new ArrayList();
             }
             if (cm == null)
             {
                 if (mTraditionalList)
                 {
                     cm = new CoverAdapter<MangaRecord>(c, R.layout.list_cover_with_text_item, result, internalManager, type);
                 }
                 else
                 {
                     cm = new CoverAdapter<MangaRecord>(c, R.layout.grid_cover_with_text_item, result, internalManager, type);
                 }
             }
 
             if (gv.getAdapter() == null)
             {
                 gv.setAdapter(cm);
             }
             else
             {
                 cm.clear();
                 cm.supportAddAll(result);
                 cm.notifyDataSetChanged();
             }
 
             if (mForceSync)
             {
                 Toast.makeText(c, R.string.toast_SyncDone, Toast.LENGTH_SHORT).show();
             }
         }
 
     }
 
     @Override
     public void onSaveInstanceState(Bundle state)
     {
         state.putInt("list", currentList);
         state.putBoolean("traditionalList", useTraditionalList);
 
         super.onSaveInstanceState(state);
     }
 
     @Override
     public void onAttach(Activity a)
     {
         super.onAttach(a);
         Iready = (IItemGridFragment) a;
 
     }
 
     public interface IItemGridFragment
     {
         public void fragmentReady();
     }

    public int pxToDp(int px){
        Resources resources = c.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        int dp = (int)(px / (metrics.density + 0.5));
        return dp;
    }
 }
