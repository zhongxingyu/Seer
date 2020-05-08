 package bs.howdy.PrayerList.Views;
 
 import android.content.Context;
 import android.content.Intent;
import android.content.res.TypedArray;
 import android.util.AttributeSet;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.LinearLayout;
 import bs.howdy.PrayerList.*;
 import bs.howdy.PrayerList.Activities.*;
 
 public class TitlebarView extends LinearLayout {
 	private Context _context;
 	
 	public TitlebarView(Context context, AttributeSet attrs)
 	{
 		super(context, attrs);
 		_context = context;
 		
		boolean showAdd = getShowAddAttribute(attrs);
		
 		LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		layoutInflater.inflate(R.layout.titlebar, this);
 		
 		LinearLayout layout = (LinearLayout)findViewById(R.id.addPrayerWrapper);
         if(showAdd) {
         	layout.setOnClickListener(new OnClickListener() {
 				public void onClick(View view) {
 					_context.startActivity(new Intent(_context, CreateEditPrayer.class));
 				}
             });
         } else {
         	layout.setVisibility(View.GONE);
         }
 	}
	
	private boolean getShowAddAttribute(AttributeSet attrs)
	{
		TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.TitlebarView);
		return a.getBoolean(R.styleable.TitlebarView_showAdd, true);
	}
 }
