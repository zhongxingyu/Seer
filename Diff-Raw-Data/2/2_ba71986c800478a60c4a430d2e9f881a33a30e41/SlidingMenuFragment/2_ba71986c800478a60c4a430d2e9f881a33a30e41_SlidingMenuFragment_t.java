 package ru.tulupov.nsuconnect.slidingmenu;
 
 import android.content.Context;
 import android.content.res.XmlResourceParser;
 import android.database.ContentObserver;
 import android.os.Bundle;
 import android.os.Handler;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.ListFragment;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.android.volley.RequestQueue;
 import com.android.volley.Response;
 import com.android.volley.toolbox.Volley;
 
 import org.xmlpull.v1.XmlPullParser;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import ru.tulupov.nsuconnect.R;
 import ru.tulupov.nsuconnect.database.DatabaseConstants;
 import ru.tulupov.nsuconnect.database.HelperFactory;
 import ru.tulupov.nsuconnect.model.Online;
 import ru.tulupov.nsuconnect.request.GetOnlineRequest;
 
 
 public class SlidingMenuFragment extends ListFragment {
 
 
     private static final long DELAY = 30000;
     private static final String TAG = SlidingMenuFragment.class.getSimpleName();
 
     public static SlidingMenuFragment newInstance(final Context context) {
         return (SlidingMenuFragment) Fragment.instantiate(context, SlidingMenuFragment.class.getName());
     }
 
 
     private SlidingMenuAdapter adapter;
 
     public interface OnItemClickListener {
         void onClick(int id);
     }
 
     private OnItemClickListener onItemClickListener;
     private List<SlidingMenuItem> menuItems;
     private int resourceId;
 
     public void setOnItemClickListener(SlidingMenuFragment.OnItemClickListener onItemClickListener) {
         this.onItemClickListener = onItemClickListener;
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         return inflater.inflate(R.layout.fgt_sliding_menu, null);
     }
 
     private RequestQueue queue;
     private View header;
 
     @Override
     public void onViewCreated(View view, Bundle savedInstanceState) {
         super.onViewCreated(view, savedInstanceState);
         queue = Volley.newRequestQueue(getActivity());
         adapter = new SlidingMenuAdapter();
 
         header = View.inflate(getActivity(), R.layout.header_sliding_menu, null);
 
 
         updateCounter();
 
 
         getListView().addHeaderView(header);
         setListAdapter(adapter);
         parseXml(resourceId);
         adapter.updateList(menuItems);
     }
 
     public void updateCounter() {
         queue.add(new GetOnlineRequest(new Response.Listener<Online>() {
             @Override
             public void onResponse(Online online) {
                 TextView count = (TextView) header.findViewById(R.id.count);
                 count.setText(String.valueOf(online.getCount()));
             }
         }));
     }
 
 
     private Handler handler = new Handler();
     private ContentObserver contentObserver = new ContentObserver(handler) {
         @Override
         public void onChange(boolean selfChange) {
 
             try {
 
 
                 int unreadCount = HelperFactory.getHelper().getMessageDao().getUnreadCount();
                 if (adapter.getCount() != 0) {
                     adapter.getItem(1).notifiactions = unreadCount;
                     adapter.notifyDataSetChanged();
                 }
 
             } catch (SQLException e) {
                 Log.e(TAG, "error", e);
             }
 
         }
     };
     private Runnable updateRunnable = new Runnable() {
         @Override
         public void run() {
             updateCounter();
             handler.postDelayed(updateRunnable, DELAY);
         }
     };
 
     @Override
     public void onResume() {
         super.onResume();
         handler.post(updateRunnable);
         contentObserver.onChange(false);
         getActivity().getContentResolver().registerContentObserver(DatabaseConstants.URI_CONVERSATION, true, contentObserver);
         getActivity().getContentResolver().registerContentObserver(DatabaseConstants.URI_COUNTER, true, contentObserver);
     }
 
     @Override
     public void onPause() {
         super.onPause();
         handler.removeCallbacks(updateRunnable);
         getActivity().getContentResolver().unregisterContentObserver(contentObserver);
     }
 
     public void setMenuItems(int resourceId) {
         this.resourceId = resourceId;
     }
 
     @Override
     public void onListItemClick(ListView lv, View v, int position, long id) {
         SlidingMenuItem item = (SlidingMenuItem) lv.getItemAtPosition(position);
        if (onItemClickListener != null && item != null) {
             onItemClickListener.onClick(item.id);
         }
     }
 
 
     private void parseXml(int menu) {
 
         menuItems = new ArrayList<SlidingMenuItem>();
 
         try {
             XmlResourceParser xpp = getResources().getXml(menu);
 
             xpp.next();
             int eventType = xpp.getEventType();
 
             boolean isChild = false;
             while (eventType != XmlPullParser.END_DOCUMENT) {
 
                 if (eventType == XmlPullParser.START_TAG) {
 
                     String elemName = xpp.getName();
 
                     if (elemName.equals("item")) {
                         String textId = xpp.getAttributeValue("http://schemas.android.com/apk/res/android", "title");
                         String iconId = xpp.getAttributeValue("http://schemas.android.com/apk/res/android", "icon");
                         String resId = xpp.getAttributeValue("http://schemas.android.com/apk/res/android", "id");
 
 
                         SlidingMenuItem item = new SlidingMenuItem();
                         if (!TextUtils.isEmpty(resId))
                             item.id = Integer.valueOf(resId.replace("@", ""));
 
                         item.text = resourceIdToString(textId);
                         item.icon = Integer.valueOf(iconId.replace("@", ""));
 
                         menuItems.add(item);
                     }
                 }
                 eventType = xpp.next();
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
 
     }
 
     private String resourceIdToString(String text) {
 
         if (!text.contains("@")) {
             return text;
         } else {
 
             String id = text.replace("@", "");
 
             return getResources().getString(Integer.valueOf(id));
 
         }
 
     }
 
 
 }
