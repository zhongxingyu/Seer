 package com.example.musicplayer.fragment;
 
 import android.os.Bundle;
 import android.os.Looper;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.*;
 import com.example.musicplayer.MainActivity;
 import com.example.musicplayer.MusicPlayerApplication;
 import com.example.musicplayer.R;
 import com.example.musicplayer.db.MusicPlayerDAO;
 import com.example.musicplayer.message.Message;
 import com.example.musicplayer.message.MessageCallback;
 import com.example.musicplayer.message.MessageData2;
 import com.example.musicplayer.pojo.SongGroup;
 import com.example.musicplayer.util.TaskExecutor;
 
 /**
  * Created with IntelliJ IDEA.
  * User: neevek
  * Date: 7/20/13
  * Time: 10:12 AM
  */
 public class MainFragment extends Fragment implements AdapterView.OnItemClickListener, MessageCallback {
     private View mLayout;
 
     private MusicPlayerApplication mApp;
     private MusicPlayerDAO mMusicPlayerDAO;
 
     private GridView mGridView;
     private MainGridViewItemAdapter mAdapter;
 
     private final static String[] MENU_ITEM_TEXT = new String[]{"全部音乐", "歌手", "专辑"};
     private final static int[] MENU_ITEM_DATA_COUNT = new int[3];
     private final static int[] MENU_ITEM_ICON = new int[] { R.drawable.icon_music, R.drawable.icon_artist, R.drawable.icon_album };
 
 
     private String mTitle;
 
     public MainFragment () {
         mApp = MusicPlayerApplication.getInstance();
         mMusicPlayerDAO = mApp.getMusicPlayerDAO();
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         mTitle = getResources().getString(R.string.app_name);
         mApp.getMessagePump().register(Message.Type.REDRAW_LIST, this);
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         getActivity().setTitle(mTitle);
 
         if (mLayout != null) {
             ((ViewGroup)mLayout.getParent()).removeView(mLayout);
             return mLayout;
         }
 
         mLayout = inflater.inflate(R.layout.block_menu, null, false);
 
         mGridView = (GridView) mLayout;
         mGridView.setOnItemClickListener(this);
 
         TaskExecutor.executeTask(new Runnable() {
             @Override
             public void run() {
                 setItemDataCounts(false);
                mApp.runOnUiThread(new Runnable() {
                     @Override
                     public void run() {
                         mAdapter = new MainGridViewItemAdapter();
                         mGridView.setAdapter(mAdapter);
                     }
                 });
             }
         });
 
         return mLayout;
     }
 
     @Override
     public void onDestroy() {
         super.onDestroy();
         mApp.getMessagePump().unregister(this);
     }
 
     public void setItemDataCounts(boolean redraw) {
         MENU_ITEM_DATA_COUNT[0] = mMusicPlayerDAO.getAllMusicCount();
         MENU_ITEM_DATA_COUNT[1] = mMusicPlayerDAO.getArtistCount();
         MENU_ITEM_DATA_COUNT[2] = mMusicPlayerDAO.getAlbumCount();
 
         if (redraw) {
             if (Looper.myLooper() == Looper.getMainLooper())
                 mAdapter.notifyDataSetChanged();
             else
                mApp.runOnUiThread(new Runnable() {
                     @Override
                     public void run() {
                         mAdapter.notifyDataSetChanged();
                     }
                 });
         }
     }
 
 
     private class MainGridViewItemAdapter extends BaseAdapter {
         @Override
         public int getCount() {
             return MENU_ITEM_TEXT.length;
         }
 
         @Override
         public Object getItem(int position) {
             return MENU_ITEM_TEXT[position];
         }
 
         @Override
         public long getItemId(int position) {
             return position;
         }
 
         @Override
         public View getView(int position, View convertView, ViewGroup parent) {
             ViewHolder holder;
             if (convertView == null) {
                 convertView = getLayoutInflater(null).inflate(R.layout.block_menu_item, null);
                 holder = new ViewHolder();
 
                 holder.icon = (ImageView) convertView.findViewById(R.id.menu_item_icon);
                 holder.name = (TextView) convertView.findViewById(R.id.menu_item_name);
 
                 convertView.setTag(holder);
             } else {
                 holder = (ViewHolder) convertView.getTag();
             }
 
             holder.icon.setImageResource(MENU_ITEM_ICON[position]);
             holder.name.setText(MENU_ITEM_TEXT[position] + "("+ MENU_ITEM_DATA_COUNT[position] +")");
 
             return convertView;
         }
 
         class ViewHolder {
             ImageView icon;
             TextView name;
         }
     }
 
     @Override
     public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
         switch (position) {
             case 0:
                 mApp.getMessagePump().broadcastMessage(Message.Type.SHOW_FRAGMENT_MUSIC_LIST, new MessageData2<Integer, SongGroup>(MainActivity.TYPE_ALL_MUSIC, null));
                 break;
             case 1:
                 mApp.getMessagePump().broadcastMessage(Message.Type.SHOW_FRAGMENT_ARTIST_LIST, null);
                 break;
             case 2:
                 mApp.getMessagePump().broadcastMessage(Message.Type.SHOW_FRAGMENT_ALBUM_LIST, null);
                 break;
         }
     }
 
     @Override
     public void onReceiveMessage(Message message) {
         switch (message.type) {
             case REDRAW_LIST:
                 setItemDataCounts(true);
                 break;
         }
     }
 }
