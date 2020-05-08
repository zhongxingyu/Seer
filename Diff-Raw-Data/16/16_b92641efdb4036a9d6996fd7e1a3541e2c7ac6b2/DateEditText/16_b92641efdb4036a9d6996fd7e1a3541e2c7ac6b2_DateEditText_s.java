 package com.zybnet.abc.view;
 
 import java.text.DateFormat;
 import java.util.Calendar;
 
 import android.content.Context;
 import android.util.AttributeSet;
 import android.view.View;
 import android.widget.DatePicker;
 import android.widget.EditText;
 
 import com.zybnet.abc.R;
import com.zybnet.abc.utils.L;
 
 public class DateEditText extends EditText {
 	
 	private java.sql.Date date;
 	private HistoryViewFlipper flipper;
 	
 	public DateEditText(Context context) {
 		super(context);
 	}
 	
 	public DateEditText(Context context, AttributeSet attrs) {
 		super(context, attrs);
 	}
 	
 	public DateEditText(Context context, AttributeSet attrs, int style) {
 		super(context, attrs, style);
 	}
 	
 	public void setDate(java.sql.Date date) {
 		this.date = date;
		L.og(String.format("%tF", date));
 		setText(DateFormat.getDateInstance().format(date));
 	}
 	
 	public java.sql.Date getDate() {
 		return date;
 	}
 	
 	public void setup(HistoryViewFlipper flipper) {
 		this.flipper = flipper;
 		setFocusable(false);
 		setOnClickListener(click);
 	}
 	
 	private OnClickListener click = new OnClickListener() {
 		
 		@Override
 		public void onClick(View v) {
 			NavigateBackView.Item item = new NavigateBackView.Item(getContext());
 			
 			item.opener = DateEditText.this;
 			item.view = new DateEditView(getContext(), R.layout.pick_date, delegate);
 			item.keep = false;
 			
 			flipper.showView(item);
 		}
 	};
 	
 	private class DateEditView extends EditView {
 
 		public DateEditView(Context ctx, int layout, Delegate delegate) {
 			super(ctx, layout, delegate);
 		}
 		
 	}
 	
 	private EditView.Delegate delegate = new EditView.Delegate() {
 		
 		DatePicker picker;
 		
 		@Override
 		public void afterInflate(EditView view) {
 			picker = (DatePicker) view.findViewById(R.id.picker);
 			
 			if (date == null)
 				return;
 			
 			Calendar c = Calendar.getInstance();
 			c.setTime(date);
			picker.updateDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH));
 		}
 		
 		@Override
 		public void save(EditView view) {
			java.util.Date tmp = new java.util.Date();
			date.setYear(picker.getYear());
			date.setMonth(picker.getMonth());
			date.setDate(picker.getDayOfMonth());
			setDate(new java.sql.Date(tmp.getTime()));
 			flipper.back();
 		}
 	};
 }
