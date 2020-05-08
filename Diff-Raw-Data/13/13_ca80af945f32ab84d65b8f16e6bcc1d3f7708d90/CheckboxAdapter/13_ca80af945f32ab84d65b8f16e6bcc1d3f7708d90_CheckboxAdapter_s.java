 package jp.juggler.bookmarkmushroom;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.BitmapDrawable;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 
 public class CheckboxAdapter extends ArrayAdapter<CheckboxItem>{
 	static final int layout_id = R.layout.checkbox_list_item;
 	LayoutInflater inflater;
 
     public CheckboxAdapter(Context context ){
         super(context, layout_id, new ArrayList<CheckboxItem>());
         this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
     }
 
     @Override public View getView(int position, View view , ViewGroup parent){  
     	if( view == null ){
     		view = inflater.inflate(layout_id, null);
     	}
     	
     	CheckBox cbChecked = (CheckBox)view.findViewById(R.id.checked);
     	TextView tvTitle   = (TextView)view.findViewById(R.id.title);
     	TextView tvURL     = (TextView)view.findViewById(R.id.url);
     	TextView tvDetail  = (TextView)view.findViewById(R.id.detail);
     	ImageView ivFavicon = (ImageView)view.findViewById(R.id.favicon);
     	
     	// list view で入力部品を扱う際はリスナを先に登録してからデータを変更しないといけない。
     	// see http://d.hatena.ne.jp/mumoshu/20100606/1275840016
     	final CheckboxItem item = this.getItem(position);
 		((CheckBox)view.findViewById(R.id.checked)).setOnCheckedChangeListener(new OnCheckedChangeListener() {
 			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
     			item.checked = isChecked;
 			}
 		});
     	cbChecked.setChecked(item.checked);
 
     	
     	tvTitle.setText(item.title);
     	tvURL.setText(item.url);
     	tvDetail.setText(item.detail);
 
     	boolean hasFavicon = item.favicon != null;
     	ivFavicon.setVisibility(hasFavicon ? View.VISIBLE : View.GONE);
     	ivFavicon.setImageDrawable(
     			hasFavicon 
     			? new BitmapDrawable(BitmapFactory.decodeByteArray(item.favicon,0,item.favicon.length))
     			: null
     	);
     	
     	if(hasFavicon){
     		
     	}
     	
        return view;  
     }
 }
