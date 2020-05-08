 package ltg.heliotablet_android.view.theory;
 
 
 import java.util.ArrayList;
 
 import ltg.heliotablet_android.R;
 import ltg.heliotablet_android.R.color;
 import ltg.heliotablet_android.data.Reason;
 import ltg.heliotablet_android.data.ReasonDBOpenHelper;
 import ltg.heliotablet_android.deprecated.ReasonDataSource;
 import ltg.heliotablet_android.view.ICircleView;
 import ltg.heliotablet_android.view.PopoverView;
 import ltg.heliotablet_android.view.PopoverViewAdapter;
 import ltg.heliotablet_android.view.PopoverView.PopoverViewDelegate;
 import ltg.heliotablet_android.view.controller.OrderingViewData;
 import ltg.heliotablet_android.view.controller.TheoryReasonController;
 
 import org.apache.commons.lang3.StringUtils;
 
 import android.app.Activity;
 import android.app.LoaderManager;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Loader;
 import android.content.res.TypedArray;
 import android.database.Cursor;
 import android.graphics.Point;
 import android.graphics.drawable.Drawable;
 import android.os.SystemClock;
 import android.support.v4.view.ViewPager;
 import android.util.AttributeSet;
 import android.view.GestureDetector;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.commonsware.cwac.loaderex.SQLiteCursorLoader;
 import com.google.common.base.Strings;
 import com.google.common.collect.ImmutableSortedSet;
 
 public class CircleView extends RelativeLayout implements PopoverViewDelegate, ICircleView  {
 
 	private GestureDetector gestureDetector;
 	private String flag;
 	private String anchor;
 	private boolean isDelete = false;
 	private int textColor;
 	private TextView reasonTextView;
 	private String type;
 	private ImmutableSortedSet<Reason> imReasons;
 	private PopoverView cachedPopoverView;
 
 	private RelativeLayout viewPagerLayout;
 	private Reason reasonNeedsUpdate;
 	private TheoryReasonController theoryController;
 	
 	public CircleView(Context context) {
 		super(context);
 	}
 	
 	public CircleView(Context context, AttributeSet attrs) {
         super(context, attrs);
         LayoutInflater inflater = (LayoutInflater) context
                 .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         inflater.inflate(R.layout.circle_view, this, true);
         reasonTextView = (TextView) getChildAt(0);
 
         TypedArray a=context.obtainStyledAttributes(attrs,R.styleable.CircleView);
         this.flag = a.getString(R.styleable.CircleView_flag);
         this.setTextColor(a.getColor(R.styleable.CircleView_textColor, color.White));
         a.recycle();
         
         theoryController = TheoryReasonController.getInstance(context);
         
         
     	viewPagerLayout = (RelativeLayout) inflater.inflate(
 				R.layout.activity_screen_slide, this, false);
     	
     	
         reasonTextView.setTextColor(this.getTextColor());
         this.enableDoubleTap();
     }
 	
 	public int getReasonsSize() {
 		if( imReasons != null ){
 			imReasons.size();
 		}
 		return 0;
 	}
 	
 	@Override
 	public void setTag(Object tag) {
 		super.setTag(tag);
 		
 		imReasons = (ImmutableSortedSet<Reason>) tag;
 		
 		if( imReasons != null) {
 			this.reasonTextView.setText(""+ imReasons.size());
 		}
 		
 	}
 	
 	public void enableDoubleTap() {
         gestureDetector = new GestureDetector(getContext(), new CircleDoubleTapGestureListener());
         this.setOnTouchListener(new OnTouchListener() {
 			
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				 gestureDetector.onTouchEvent(event);
 				 return true;
 			}
 		}); 
 	}
 	
 	class CircleDoubleTapGestureListener extends GestureDetector.SimpleOnGestureListener {
 	    // event when double tap occurs
 	    @Override
 	    public boolean onDoubleTap(MotionEvent e) {
 	    	
 	    	showPopover(imReasons);
 	        return super.onDoubleTap(e);
 	    }
 	}
 	
 	public void showPopover(ImmutableSortedSet<Reason> popOverReasonSet) {
 		
 		ArrayList<View> pages = new ArrayList<View>();
 		ViewPager pager = new ViewPager(getContext());
 		ViewPager vPager = (ViewPager) viewPagerLayout
				.findViewById(R.id.popover_pager);
 		
 		
 		ImmutableSortedSet<Reason> copyOfReasonSet = ImmutableSortedSet
 				.orderedBy(OrderingViewData.isReadOnlyOrdering.reverse())
 				.addAll(popOverReasonSet).build();
 		
 		
 		
 		for (Reason reason : copyOfReasonSet) {
 			View layout = View.inflate(getContext(),
 					R.layout.popover_view_delete, null);
 			layout.setTag(reason);
 			
 			final EditText editText = (EditText) layout.findViewById(R.id.mainEditText);
 			Button deleteButton = (Button) layout.findViewById(R.id.deleteButton);
 			editText.setText(reason.getReasonText());
 			
 			if( reason.isReadonly()) {
 				editText.setKeyListener(null);
 				editText.setBackground(getResources().getDrawable(R.drawable.textedit_shape_disabled));
 				deleteButton.setVisibility(View.GONE);
 			} else {
 				editText.setFocusable(true);
 			    editText.setFocusableInTouchMode(true);
 	            editText.setClickable(true);
 				editText.requestFocus();
 				deleteButton.setVisibility(View.VISIBLE);
 				deleteButton.setOnClickListener(new OnClickListener() {
 					
 					@Override
 					public void onClick(View v) {
 						
 						ViewGroup parent = (ViewGroup) v.getParent()
 								.getParent();
 
 						CircleView.this.isDelete = true;
 						CircleView.this.cachedPopoverView.dissmissPopover(true);
 
 						boolean isScheduledForViewRemoval = false;
 						if (CircleView.this.imReasons.size() - 1 <= 0) {
 							isScheduledForViewRemoval = true;
 						}
 
 						Reason reasonToDelete = (Reason) parent.getTag();
 
 						theoryController.deleteReason(reasonToDelete, isScheduledForViewRemoval);
 						
 						InputMethodManager imm = (InputMethodManager) getContext()
 								.getSystemService(Context.INPUT_METHOD_SERVICE);
 						imm.hideSoftInputFromWindow(
 								v.getApplicationWindowToken(),
 								InputMethodManager.HIDE_NOT_ALWAYS);
 
 						CircleView.this.makeToast("Reason Deleted");
 
 						if (isScheduledForViewRemoval) {
 							ViewGroup tv = (ViewGroup) CircleView.this
 									.getParent();
 							tv.removeView(CircleView.this);
 						}
 						
 					}
 				});
 			}
 
 			pages.add(layout);
 		}
 		
 		vPager.setAdapter(new PopoverViewAdapter(pages));
 		vPager.setOffscreenPageLimit(5);
 		vPager.setCurrentItem(0);
        ViewGroup parent = (ViewGroup) this.getParent().getParent().getParent().getParent();
 
 		
 		PopoverView popoverView = new PopoverView(getContext(), viewPagerLayout);
 		cachedPopoverView = popoverView;
 		popoverView.setContentSizeForViewInPopover(new Point(370, 220));
 		popoverView.setDelegate(this);
 		popoverView.showPopoverFromRectInViewGroup(
 				parent,
 				PopoverView.getFrameForView(this),
 				PopoverView.PopoverArrowDirectionAny, true);
 		
 	}
 	
 	protected void makeTransparent(boolean isTransparent) {
 		if( isTransparent )
 			this.setReducedAlpha(135f);
 		else
 			this.setReducedAlpha(255f);
 		
 	}
 
 	@Override
 	public void popoverViewWillShow(PopoverView view) {
 	}
 
 	@Override
 	public void popoverViewDidShow(PopoverView view) {
		ViewPager vPager = (ViewPager) view.findViewById(R.id.popover_pager);
 		PopoverViewAdapter adapter = (PopoverViewAdapter) vPager.getAdapter();
 		EditText editText = (EditText) adapter.findViewById(0, R.id.mainEditText);
 		editText.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN , 0, 0, 0));
 		editText.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP , 0, 0, 0));
 		editText.requestFocus();
 		editText.setSelection(editText.getText().length());
 	}
 
 	@Override
 	public void popoverViewWillDismiss(PopoverView view) {
 	
 		if (isDelete == false) {
			ViewPager vPager = (ViewPager) view.findViewById(R.id.popover_pager);
 			PopoverViewAdapter adapter = (PopoverViewAdapter) vPager
 					.getAdapter();
 			int currentItem = vPager.getCurrentItem();
 			EditText editText = (EditText) adapter.findViewById(0,
 					R.id.mainEditText);
 			if( editText.getKeyListener() == null)
 				return;
 			
 			InputMethodManager imm = (InputMethodManager) getContext()
 					.getSystemService(Context.INPUT_METHOD_SERVICE);
 			imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
 
 			String newReasonText = StringUtils.trimToEmpty(editText.getText().toString());
 
 			reasonNeedsUpdate = null;
 
 			if (!Strings.isNullOrEmpty(newReasonText)) {
 				View reasonView = adapter.getView(0);
 				Reason viewReason = (Reason) reasonView.getTag();
 
 				// if it has been updated
 				if ( !StringUtils.trimToEmpty(viewReason.getReasonText()).equals(newReasonText)) {
 					reasonNeedsUpdate = Reason.newInstance(viewReason);
 					reasonNeedsUpdate.setReasonText(editText.getText()
 							.toString());
 				}
 			}
 		}
 
 	}
 
 	@Override
 	public void popoverViewDidDismiss(PopoverView view) {
 		
 		
 		//lets update
 		if( reasonNeedsUpdate != null && isDelete == false ) {
 			theoryController.updateReason(reasonNeedsUpdate);
 			this.makeToast("Reason Updated");
 			reasonNeedsUpdate = null;
 		} else {
 			//just deleted reset the flag
 			isDelete = false;
 			
 		}
 		
 	}
 
 
 	public void makeToast(String toastText) {
 		Toast toast = Toast.makeText(getContext(), toastText,
 				Toast.LENGTH_SHORT);
 		toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 50);
 		toast.show();
 	}
 	
 	
 	public String getFlag() {
 		return flag;
 	}
 
 	public void setFlag(String flag) {
 		this.flag = flag;
 	}
 	
 	public void setReasonText(String reasonText) {
 		this.reasonTextView.setText(reasonText);
 	}
 	
 	public String getReasonText() {
 		return this.reasonTextView.getText().toString();
 	}
 
 	public int getTextColor() {
 		return textColor;
 	}
 
 	public void setTextColor(int textColor) {
 		this.textColor = textColor;
 	    reasonTextView.setTextColor(textColor);
 	}
 
 	public void setReducedAlpha(float circleViewAlpha) {
 		Drawable background = getBackground();
 		background.setAlpha((int) circleViewAlpha);
 		setBackground(background);
 		reasonTextView.setAlpha(circleViewAlpha);
 	}
 	
 	@Override
 	public String toString() {
 		return "Flag: " + this.getFlag() + " Reasons: " + imReasons.toString();
 	}
 
 	public String getAnchor() {
 		return anchor;
 	}
 
 	public void setAnchor(String anchor) {
 		this.anchor = anchor;
 	}
 	
 }
 
 
