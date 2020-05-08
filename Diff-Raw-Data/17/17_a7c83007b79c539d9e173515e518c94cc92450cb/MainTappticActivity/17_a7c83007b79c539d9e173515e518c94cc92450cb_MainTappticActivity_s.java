 package mobilesoft365.test.Tapptic;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class MainTappticActivity extends Activity {
 
     ListView TappList;
     final String ATTRIBUTE_NAME_TEXT = "name";
     final String ATTRIBUTE_NAME_IMAGE = "image";
 
     ArrayList<ItemInList> loadedData = new ArrayList<ItemInList>();
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         TappList = (ListView)findViewById(R.id.tapp_list_view);
         TappList.setItemsCanFocus(true);
 
         ArrayList<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
 
        Map<String, Object> m;
            m = new HashMap<String, Object>();
            m.put(ATTRIBUTE_NAME_TEXT, "Android");
        data.add(m);

        m = new HashMap<String, Object>();
            m.put(ATTRIBUTE_NAME_TEXT, "iPhone");
        data.add(m);
        m = new HashMap<String, Object>();
            m.put(ATTRIBUTE_NAME_TEXT, "WindowsMobile");
        data.add(m);
        m = new HashMap<String, Object>();
            m.put(ATTRIBUTE_NAME_TEXT, "Blackberry");
        data.add(m);
        m = new HashMap<String, Object>();
            m.put(ATTRIBUTE_NAME_TEXT, "Linux");
            data.add(m);
 
         // Использование собственного шаблона
         SimpleAdapter adapter = new SimpleAdapter(this,
                 data, R.layout.tapp_items_layout,
                 new String[] { ATTRIBUTE_NAME_TEXT },
                 new int [] { R.id.tapp_text});
         TappList.setAdapter(adapter);
 
         new ListLoader(MainTappticActivity.this).execute();
     }
 
     public void updateItemList(ArrayList<ItemInList> loadResult) {
         loadedData = loadResult;
 
         ArrayList<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
         for(ItemInList item : loadResult) {
             Map mapObject = new HashMap<String, Object>();
             mapObject.put(ATTRIBUTE_NAME_TEXT, item.getName());
             mapObject.put(ATTRIBUTE_NAME_IMAGE, item.getImage());
             data.add(mapObject);
         }
 
         TappAdapter adapter =  new TappAdapter(this,
                 data, R.layout.tapp_items_layout,
                 new String[] { ATTRIBUTE_NAME_TEXT},
                 new int [] { R.id.tapp_text});
 
         TappList.setAdapter(adapter);
     }
 
 
     class TappAdapter extends SimpleAdapter {
         private List<? extends Map<String, ?>> data;
         private Context context;
 
         public TappAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
             super(context, data, resource, from, to);
             this.context = context;
             this.data = data;
         }
 
         @Override
         public int getCount() {
             return data.size();
         }
 
         @Override
         public Object getItem(int position) {
             return null;  //To change body of implemented methods use File | Settings | File Templates.
         }
 
 
         @Override
         public long getItemId(int position) {
             return position;
         }
 
         @Override
         public View getView(final int position, View convertView, ViewGroup parent) {
 
 
             final ViewHolder holder;
 
             if (convertView == null) {
                 LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                 convertView = li.inflate(R.layout.tapp_items_layout, parent, false);
                 holder = new ViewHolder();
 
                 holder.TappTitle = (TextView)convertView.findViewById(R.id.tapp_text);
                 holder.TappImage = (ImageView)convertView.findViewById(R.id.tapp_image);
 
                 convertView.setTag(holder);
             } else {
                 holder = (ViewHolder) convertView.getTag();
             }
             holder.TappTitle.setText((String) data.get(position).get(ATTRIBUTE_NAME_TEXT));
             holder.TappImage.setImageDrawable((Drawable) data.get(position).get(ATTRIBUTE_NAME_IMAGE));
 
             return convertView;
         }
 
         public class ViewHolder {
             TextView TappTitle;
             ImageView TappImage;
         }
     }
 }
