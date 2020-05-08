 package ar.com.nivel7.kernelgesturesbuilder;
 
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Context;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.google.analytics.tracking.android.EasyTracker;
 public class Actions extends Activity  {
 
 	public static final String[] titles = new String[] { 
		"Toggle inverted screen colors",
         "action_2", 
         "Start Stweaks", 
        "Start a call to the intended phone", 
         "Start the camera app",
         "Toggle bluetooth on/off",
         "Toggle WiFi on/off",
         "Media Play/Pause",
         "Volume Mute/Unmute",
         "Home",
        "Toggle between the last 2 activities"};
 
 	public static final String[] actions = new String[] {
         "mdnie_status=`cat /sys/class/mdnie/mdnie/negative`\n	if [ \"$mdnie_status\" -eq \"0\" ]; then\n		echo 1 > /sys/class/mdnie/mdnie/negative\n	else\n		echo 0 > /sys/class/mdnie/mdnie/negative\n	fi;\n",
         "key=26; service call window 12 i32 1 i32 1 i32 5 i32 0 i32 0 i32 $key i32 0 i32 0 i32 0 i32 8 i32 0 i32 0 i32 0 i32 0; service call window 12 i32 1 i32 1 i32 5 i32 0 i32 1 i32 $key i32 0 i32 0 i32 27 i32 8 i32 0 i32 0 i32 0 i32 0\n", 
         "am start -a android.intent.action.MAIN -n com.gokhanmoral.stweaks.app/.MainActivity;",
         "service call phone 2 s16 \"your beloved number\"",
         "am start --activity-exclude-from-recents com.sec.android.app.camera\nam start --activity-exclude-from-recents com.android.camera/.Camera\n",
         "service call bluetooth 1 | grep \"0 00000000\" \n 							if [ \"$?\" -eq \"0\" ]; then\n 								service call bluetooth 3 \n 							else\n 								[ \"$1\" -eq \"1\" ] &amp;&amp; service call bluetooth 5 \n 								[ \"$1\" -ne \"1\" ] &amp;&amp; service call bluetooth 4 \n 							fi;\n",
         "service call wifi 14 | grep \"0 00000001\" > /dev/null\n			if [ \"$?\" -eq \"0\" ]; then\n				service call wifi 13 i32 1 > /dev/null\n			else\n				service call wifi 13 i32 0 > /dev/null\n		fi;\n",
         "input keyevent 85\n",
         "input keyevent 164\n",
         "input keyevent 3\n",
         "service call vibrator 2 i32 100 i32 0\n        dumpsys activity a | grep \"Recent #1:.* com.anddoes.launcher\"\n        if [ \"$?\" -eq \"0\" ]; then\n            service call activity 24 i32 `dumpsys activity a | grep \"Recent #2:\" | grep -o -E \"#[0-9]+ \" | cut -c2-` i32 2\n        else\n            service call activity 24 i32 `dumpsys activity a | grep \"Recent #1:\" | grep -o -E \"#[0-9]+ \" | cut -c2-` i32 2\n        fi\n"
 	};
 	
     public static final Integer[] images = {  
     	R.drawable.ic_mdnie,
         R.drawable.ic_question, 
         R.drawable.ic_stweaks, 
     	R.drawable.ic_lovecall,
         R.drawable.ic_camera, 
         R.drawable.ic_bluetooth, 
     	R.drawable.ic_wifi,
         R.drawable.ic_playpause, 
         R.drawable.ic_mute, 
     	R.drawable.ic_home,
         R.drawable.ic_alttab
         };
         
 
     List<ActionsRowItem> rowItems;
 	  
       @Override
 	  public void onCreate(Bundle savedInstanceState) {
 		  
 	    super.onCreate(savedInstanceState);
 
 	    setContentView(R.layout.actions);
 	    
 	    rowItems = new ArrayList<ActionsRowItem>();
         for (int i = 0; i < titles.length; i++) {
             ActionsRowItem item = new ActionsRowItem(images[i], titles[i], actions[i]);
             rowItems.add(item);
         }
  
         ListView actions_list = (ListView) findViewById(R.id.actions_list);
         final ActionsAdapter adapter = new ActionsAdapter(this, R.layout.actionsrow , rowItems);
         actions_list.setAdapter(adapter);
         actions_list.setOnItemClickListener(new OnItemClickListener() {
             public void onItemClick(AdapterView<?> arg0, View v, int position,
                     long id) {
             	
             	ActionsRowItem rowitem=adapter.getItem(position);
         	    String action=rowitem.getDesc();
             	int gesturenumber = KernelGesturesBuilder.getGesturenumber();
             	FileOutputStream fos;
             	
             	String FILENAME = "gesture-"+gesturenumber+".sh";
             		try {
             			fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
             			fos.write(action.getBytes());
             			fos.close();
             		} catch (FileNotFoundException e) {
             			e.printStackTrace();
             		} catch (IOException e) {
             			e.printStackTrace();
             		}
             	    
             		Actions.this.finish();
             	    
             }
             
         });
         
         
 	  }
       
 	  @Override
 	  public void onStart() {
 	    super.onStart();
 	    EasyTracker.getInstance().activityStart(this); 
 	  }
 
 	  @Override
 	  public void onStop() {
 	    super.onStop();
 	    EasyTracker.getInstance().activityStop(this);
 	  }
 	  
 }
 
 
 class ActionsAdapter extends ArrayAdapter<ActionsRowItem> {
 	 
     Context context;
  
     public ActionsAdapter(Context context, int resourceId,
             List<ActionsRowItem> items) {
         super(context, resourceId, items);
         this.context = context;
     }
  
 
     private class ViewHolder {
         ImageView imageView;
         TextView txtTitle;
        TextView txtDesc;
     }
  
     public View getView(int position, View convertView, ViewGroup parent) {
         ViewHolder holder = null;
         ActionsRowItem rowItem = getItem(position);
  
         LayoutInflater mInflater = (LayoutInflater) context
                 .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
         if (convertView == null) {
             convertView = mInflater.inflate(R.layout.actionsrow , null);
             holder = new ViewHolder();
             holder.txtTitle = (TextView) convertView.findViewById(R.id.actiontitle);
             holder.imageView = (ImageView) convertView.findViewById(R.id.actionicon);
            holder.txtDesc = (TextView) convertView.findViewById(R.id.actiondesc);
             convertView.setTag(holder);
         } else
             holder = (ViewHolder) convertView.getTag();
  
       holder.txtDesc.setText(rowItem.getDesc());
        holder.txtTitle.setText(rowItem.getTitle());
        holder.imageView.setImageResource(rowItem.getImageId());
  
         return convertView;
     }
 }
