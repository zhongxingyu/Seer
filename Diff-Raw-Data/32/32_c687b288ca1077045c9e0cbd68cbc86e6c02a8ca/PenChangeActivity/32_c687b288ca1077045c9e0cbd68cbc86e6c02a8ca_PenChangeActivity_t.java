 package jp.hidetobara.pasteltouch;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.Gallery;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class PenChangeActivity extends Activity
 		implements View.OnClickListener, AdapterView.OnItemClickListener
 {
 	final static String NAME_MODE = "mode";
 	final static int MODE_PEN = 0;
 	final static int MODE_SIZE = 1;
 	final static int MODE_SURFACE = 2;
 	
 	final static String NAME_TYPE = "type";
 	final static String NAME_COLOR = "color";
 	final static int PEN_PASTEL = 1;
 	final static int PEN_FINGER = 2;
 	final static int PEN_BRUSH = 3;
 	final static String NAME_SIZE = "size";
 	final static String NAME_SURFACE = "surface";
 	final static String NAME_R = "r";
 	
 	final int SELECTED_COLOR = Color.argb(128, 0, 0, 0);
 	
 	int _Mode = MODE_PEN;
 	private LocalStorage _Storage;
 	int _PenSelect = 0;
 	int _SizeSelect = 0;
 	int _SurfaceSelect = 0;
 	
 	ImagePensAdapter _ImagesPen;
 	ImageSizesAdapter _ImagesSize;
 	ImageSurfacesAdapter _ImagesSurface;
 	Button _ButtonOk;
 	
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.pen_change);
    		
 		Intent aIntent = getIntent();
 		_Mode = aIntent.getIntExtra(NAME_MODE, 0);
 
 		TextView textView = (TextView)findViewById(R.id.note1);
 		
    		_Storage = LocalStorage.Factory(this);
 		
 	   	Gallery gallery = (Gallery)findViewById(R.id.pen_list);
 	   	if(_Mode == MODE_PEN)
 	   	{
 			_ImagesPen = new ImagePensAdapter(this);
 			gallery.setAdapter(_ImagesPen);
 			
 			int type = _Storage.getPenType();
 			int color = _Storage.getPenColor();
 			_PenSelect = _ImagesPen.searchPosition(type, color);
 			gallery.setSelection(_PenSelect);
 			PenItem item = (PenItem)_ImagesPen.getItem(_PenSelect);
 			textView.setText(item.note);
 	   	}else if(_Mode == MODE_SIZE){
 			_ImagesSize = new ImageSizesAdapter(this);
 			gallery.setAdapter(_ImagesSize);
 
 			int size = _Storage.getPenSize();
 			_SizeSelect = _ImagesSize.searchPosition(size);
 			gallery.setSelection(_SizeSelect);
 			SizeItem item = (SizeItem)_ImagesSize.getItem(_SizeSelect);
 			textView.setText(item.note);
 	   	}else{
 	   		_ImagesSurface = new ImageSurfacesAdapter(this);
 	   		gallery.setAdapter(_ImagesSurface);
 	   		
 	   		String suface = _Storage.getCanvasSurface();
 	   		_SurfaceSelect = _ImagesSurface.searchPosition(suface);
 	   		gallery.setSelection(_SurfaceSelect);
 	   		SurfaceItem item = (SurfaceItem)_ImagesSurface.getItem(_SurfaceSelect);
 	   		textView.setText(item.note);
 	   	}
 		gallery.setOnItemClickListener(this);
 				
 		LinearLayout layout = (LinearLayout)findViewById(R.id.full_view);
 		layout.setOnClickListener(this);
     }
 
     /*
      * アイテム以外を押したとき
      */
 	@Override
 	public void onClick(View v)
 	{
 		Intent i = new Intent();
 		setResult(RESULT_CANCELED, i);
 		finish();
 	}
     
     /*
      * アイテムが選択されたとき
      */
     @Override
 	public void onItemClick(AdapterView<?> adapterView, View parent, int position, long id)
 	{
 		Intent i = new Intent();
     	
 		if(adapterView.getAdapter() == _ImagesPen)
 		{
 			PenItem item = (PenItem)_ImagesPen.getItem(_PenSelect);
 			item.view.setBackgroundColor(Color.TRANSPARENT);
 	    	parent.setBackgroundColor(SELECTED_COLOR);	//ImageViewが呼ばれる
 			
 			_PenSelect = position;
 			item = (PenItem)_ImagesPen.getItem(_PenSelect);
 			_Storage.setPenType(item.type);
 			_Storage.setPenColor(item.color);
 			
 			i.putExtra(NAME_MODE, MODE_PEN);
 			i.putExtra(NAME_TYPE, item.type);
 			i.putExtra(NAME_COLOR, item.color);
 			i.putExtra(NAME_R, item.r);			
 		}
 		if(adapterView.getAdapter() == _ImagesSize)
 		{
 			SizeItem item = (SizeItem)_ImagesSize.getItem(_SizeSelect);
 			item.view.setBackgroundColor(Color.TRANSPARENT);
 	    	parent.setBackgroundColor(SELECTED_COLOR);	//ImageViewが呼ばれる
 			
 			_SizeSelect = position;
 			item = (SizeItem)_ImagesSize.getItem(_SizeSelect);
 			_Storage.setPenSize(item.size);
 			
 			i.putExtra(NAME_MODE, MODE_SIZE);
 			i.putExtra(NAME_SIZE, item.size);
 			i.putExtra(NAME_R, item.r);
 		}
 		if(adapterView.getAdapter() == _ImagesSurface)
 		{
 			SurfaceItem item = (SurfaceItem)_ImagesSurface.getItem(_SurfaceSelect);
 			item.view.setBackgroundColor(Color.TRANSPARENT);
 	    	parent.setBackgroundColor(SELECTED_COLOR);	//ImageViewが呼ばれる
 			
 			_SurfaceSelect = position;
 			item = (SurfaceItem)_ImagesSurface.getItem(_SurfaceSelect);
 			_Storage.setCanvasSurface(item.name);
 			
 			i.putExtra(NAME_MODE, MODE_SURFACE);
 			i.putExtra(NAME_SURFACE, item.name);
 			i.putExtra(NAME_R, item.r);
 		}
 		
 		setResult(RESULT_OK, i);		
 		finish();
 	}
 
     /*
      * ペンのリスト
      */
     public class ImagePensAdapter extends BaseAdapter
     {
         private ArrayList<PenItem> m_items = new ArrayList<PenItem>();
         
         public ImagePensAdapter(Context c)
         {
         	int alpha = 192;
         	m_items.add(new PenItem(c, R.drawable.color_0_0_0, 0, 0, 0, alpha));
         	m_items.add(new PenItem(c, R.drawable.color_128_128_128, 128, 128, 128, alpha));
         	m_items.add(new PenItem(c, R.drawable.color_192_192_192, 192, 192, 192, alpha));
         	m_items.add(new PenItem(c, R.drawable.color_255_255_255, 255, 255, 255, alpha));
 
 			m_items.add(new PenItem(c, R.drawable.color_230_124_136, 230, 124, 136, alpha));
 			m_items.add(new PenItem(c, R.drawable.color_138_37_50, 138, 37, 50, alpha));
 			m_items.add(new PenItem(c, R.drawable.color_230_30_30, 230, 30, 30, alpha));
 			m_items.add(new PenItem(c, R.drawable.color_247_98_39, 247, 98, 39, alpha));
 			m_items.add(new PenItem(c, R.drawable.color_245_218_175, 245, 218, 175, alpha));
 			
 			m_items.add(new PenItem(c, R.drawable.color_254_212_30, 254, 212, 30, alpha));
 			m_items.add(new PenItem(c, R.drawable.color_250_240_130, 250, 240, 130, alpha));
 			m_items.add(new PenItem(c, R.drawable.color_110_200_100, 110, 200, 100, alpha));
 			m_items.add(new PenItem(c, R.drawable.color_64_136_47, 64, 136, 47, alpha));
 			m_items.add(new PenItem(c, R.drawable.color_18_60_36, 18, 60, 36, alpha));
 			
 			m_items.add(new PenItem(c, R.drawable.color_54_72_16, 54, 72, 16, alpha));
 			m_items.add(new PenItem(c, R.drawable.color_90_140_200, 90, 140, 200, alpha));
 			m_items.add(new PenItem(c, R.drawable.color_24_80_164, 24, 80, 164, alpha));
 			m_items.add(new PenItem(c, R.drawable.color_8_32_96, 8, 32, 96, alpha));
 			m_items.add(new PenItem(c, R.drawable.color_56_40_96, 56, 40, 96, alpha));
 			
 			m_items.add(new PenItem(c, R.drawable.color_100_40_50, 100, 40, 50, alpha));
 			m_items.add(new PenItem(c, R.drawable.color_160_60_30, 160, 60, 30, alpha));
 			m_items.add(new PenItem(c, R.drawable.color_192_96_0, 192, 96, 4, alpha));
 			m_items.add(new PenItem(c, R.drawable.color_192_160_32, 192, 160, 32, alpha));
 			m_items.add(new PenItem(c, R.drawable.color_96_64_0, 96, 64, 4, alpha));
        	
         	m_items.add(new PenItem(c, R.drawable.finger, PEN_FINGER, "強くぼかします"));
         	m_items.add(new PenItem(c, R.drawable.brush, PEN_BRUSH, "弱くぼかします"));
         }
 
         public int getCount() {
             return m_items.size();
         }
 
         public Object getItem(int position) {
         	PenItem item = m_items.get(position);
         	if(item == null) return null;
         	return item;
         }
 
         public long getItemId(int position) {
             return position;
         }
 
         public View getView(int position, View convertView, ViewGroup parent)
         {
         	PenItem item = m_items.get(position);
         	if(item == null) return null;
         	if(position == _PenSelect) item.view.setBackgroundColor(SELECTED_COLOR);
             return item.view;
         }
                 
         public int searchPosition(int type, int color)
         {
         	int search = 0;
         	for(PenItem i : m_items)
         	{
         		if((type == PEN_FINGER || type == PEN_BRUSH) && i.type == type) return search;
         		if(type == PEN_PASTEL && i.type == PEN_PASTEL && i.color == color) return search;
         		search++;
         	}
         	return 0;
         }
     }
 
     public class ImageSizesAdapter extends BaseAdapter
     {
         private ArrayList<SizeItem> m_items = new ArrayList<SizeItem>();
         
         public ImageSizesAdapter(Context c)
         {
         	m_items.add(new SizeItem(c, R.drawable.size_8, 4, "細い線"));
         	m_items.add(new SizeItem(c, R.drawable.size_16, 8, "中くらいの線"));
         	m_items.add(new SizeItem(c, R.drawable.size_32, 16, "太い線※動作が遅くなります"));
         }
 
         public int getCount() {
             return m_items.size();
         }
 
         public Object getItem(int position) {
         	SizeItem item = m_items.get(position);
         	if(item == null) return null;
         	return item;
         }
 
         public long getItemId(int position) {
             return position;
         }
 
         public View getView(int position, View convertView, ViewGroup parent)
         {
         	SizeItem item = m_items.get(position);
         	if(item == null) return null;
         	if(position == _SizeSelect) item.view.setBackgroundColor(SELECTED_COLOR);
             return item.view;
         }
         
         public int searchPosition(int size)
         {
         	int search = 0;
         	for(SizeItem i : m_items)
         	{
         		if(i.size == size) return search;
         		search++;
         	}
         	return 0;
         }
     }
     
     public class ImageSurfacesAdapter extends BaseAdapter
     {
         private ArrayList<SurfaceItem> m_items = new ArrayList<SurfaceItem>();
         
         public ImageSurfacesAdapter(Context c)
         {
        	m_items.add(new SurfaceItem(c, R.drawable.random, "random", "ざらざらな表面"));
        	m_items.add(new SurfaceItem(c, R.drawable.dither, "dither", "麻状の表面"));
        	m_items.add(new SurfaceItem(c, R.drawable.flat, "flat", "つるつるな表面"));
         }
 
         public int getCount() {
             return m_items.size();
         }
 
         public Object getItem(int position) {
         	SurfaceItem item = m_items.get(position);
         	if(item == null) return null;
         	return item;
         }
 
         public long getItemId(int position) {
             return position;
         }
 
         public View getView(int position, View convertView, ViewGroup parent)
         {
         	SurfaceItem item = m_items.get(position);
         	if(item == null) return null;
         	if(position == _SurfaceSelect) item.view.setBackgroundColor(SELECTED_COLOR);
             return item.view;
         }
         
         public int searchPosition(String s)
         {
         	int search = 0;
         	for(SurfaceItem i : m_items)
         	{
         		if(i.name.equals(s)) return search;
         		search++;
         	}
         	return 0;
         }
     }
     
     /*
      * ペン情報
      */
     class PenItem
     {
 		public int type = 0;
 		public ImageView view;
 		public int color = 0;
 		public int r = 0;
 		public String note = "";
 		
     	PenItem(Context context, int resource, int type, String note)
     	{
     		this.type = type;
     		this.r = resource;
     		this.note = note;
     		createView(context, resource);
     	}
     	PenItem(Context context, int resource, int red, int green, int blue, int alpha)
     	{
     		this.type = PEN_PASTEL;
     		this.r = resource;
     		this.color = Color.argb(alpha, red, green, blue);
     		createView(context, resource);
     	}
     	
     	private void createView(Context c, int r)
     	{
     		this.view = new ImageView(c);
             this.view.setImageResource(r);
             this.view.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
             this.view.setLayoutParams(new Gallery.LayoutParams(90, 120));    		
     	}
     }
     
     /*
      * サイズ情報
      */
     class SizeItem
     {
     	public int size = 1;
     	public ImageView view;
     	public int r = 0;
     	public String note = "";
     	
     	SizeItem(Context context, int resource, int size, String note)
     	{
     		this.size = size;
     		this.r = resource;
     		this.note = note;
     		
     		this.view = new ImageView(context);
             this.view.setImageResource(resource);
             this.view.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
             this.view.setLayoutParams(new Gallery.LayoutParams(120, 120));    		
     	}
     }
     
     /*
      * 地情報
      */
     class SurfaceItem
     {
     	public ImageView view;
     	public int r = 0;
     	public String name = "";
     	public String note = "";
     	
     	SurfaceItem(Context context, int resource, String name, String note)
     	{
     		this.name = name;
     		this.r = resource;
     		this.note = note;
     		
     		this.view = new ImageView(context);
             this.view.setImageResource(resource);
             this.view.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
             this.view.setLayoutParams(new Gallery.LayoutParams(120, 120));   		
     	}
     }
 }
