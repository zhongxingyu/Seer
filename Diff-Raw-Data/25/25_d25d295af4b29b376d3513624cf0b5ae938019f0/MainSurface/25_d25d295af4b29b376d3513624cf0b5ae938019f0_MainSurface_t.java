 package org.kvj.whiskey2.widgets;
 
 import java.util.List;
 
 import org.kvj.whiskey2.R;
 import org.kvj.whiskey2.data.NoteInfo;
 import org.kvj.whiskey2.data.SheetInfo;
 import org.kvj.whiskey2.data.TemplateInfo;
 import org.kvj.whiskey2.widgets.adapters.SheetsAdapter;
 import org.kvj.whiskey2.widgets.v11.PageDnDDecorator;
 
 import android.content.Context;
 import android.os.AsyncTask;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.FragmentActivity;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.util.TypedValue;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageButton;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 public class MainSurface extends RelativeLayout {
 
 	private static final String TAG = "MainSurface";
 	public static final int PAGE_MARGIN = 10;
 	public static final int PAGES_GAP = 10;
 	public static final int TEXT_PADDING = 1;
 	private static final float TEXT_SIZE = 5;
 	private static final float COLLPASED_HEIGHT = (float) 8.5;
 
 	private boolean layoutCreated = false;
 	private float density = 1;
 	private float pageMargin;
 	private float pagesGap;
 	ViewGroup parent = null;
 	private SheetsAdapter adapter = null;
 	private int index = -1;
 	private FragmentActivity activity;
 	private ViewGroup toolbar = null;
 	private LayoutInflater inflater = null;
 
 	public MainSurface(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		density = getContext().getResources().getDisplayMetrics().density;
 		pageMargin = density * PAGE_MARGIN;
 		pagesGap = density * PAGES_GAP;
 	}
 
 	public void setController(int index, SheetsAdapter adapter, FragmentActivity fragmentActivity) {
 		this.index = index;
 		this.adapter = adapter;
 		this.activity = fragmentActivity;
 	}
 
 	@Override
 	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
 		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
 		int width = parent.getMeasuredWidth();
 		int height = parent.getMeasuredHeight();
 		// Log.i(TAG, "On measure: " + getMeasuredWidth() + "x" +
 		// getMeasuredHeight() + ", " + layoutCreated + ", "
 		// + getParent() + ", " + parent.getMeasuredWidth() + "x" +
 		// parent.getMeasuredHeight());
 		if (!layoutCreated && width > 0 && height > 0) {
 			createLayout(width, height);
 		}
 	}
 
 	@Override
 	protected void onAttachedToWindow() {
 		super.onAttachedToWindow();
 		// Log.i(TAG, "attached to window - : " + getWidth() + ", " +
 		// getHeight());
 		parent = (ViewGroup) getParent();
 	}
 
 	private void createLayout(int width, int height) {
 		layoutCreated = true;
 		removeAllViews();
 		// Log.i(TAG, "create layout: " + width + "x" + height);
		final SheetInfo sheet = adapter.getItem(index);
		if (null == sheet) {
			return;
		}
		final PageSurface page = new PageSurface(getContext());
		TemplateInfo template = adapter.getController().getTemplate(0);
 		int pagesDisplayed = 1;
 		float pageHeight = height - 2 * pageMargin;
 		float pageWidth = pageHeight * template.width / template.height;
 		// Log.i(TAG, "Calc page size pass 1: " + pageWidth + "x" + pageHeight +
 		// ", " + width + "x" + height);
 		if (pageWidth > width - 2 * pageMargin) { // Too wide
 			pageWidth = width - 2 * pageMargin;
 			// Recalculate height
 			pageHeight = pageWidth * template.height / template.width;
 		}
 		// Log.i(TAG, "Calc page size pass 2: " + pageWidth + "x" + pageHeight +
 		// ", " + density);
 		float zoomFactor = template.width / pageWidth;
 		if (zoomFactor > 0.1 * density) { //
 			zoomFactor = (float) (0.1 * density);
 			pageWidth = template.width / zoomFactor;
 			pageHeight = template.height / zoomFactor;
 		}
 		int leftGap = 0;
 		int left = (int) ((width - pageWidth) / 2);
 		int top = (int) ((height - pageHeight) / 2);
 		// Log.i(TAG, "Left x top: " + left + "x" + top);
 		if (left < 0) { // Too big
 			left = 0;
 		}
 		if (top < 0) { // Too big
 			top = 0;
 		}
 		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int) pageWidth, (int) pageHeight);
 		page.title = adapter.getItem(index).title;
 		page.marginLeft = left;
 		page.index = index;
 		page.marginTop = top;
 		page.zoomFactor = zoomFactor;
 		params.setMargins(left, top, 0, 0);
 		params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
 		params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
 		addView(page, params);
 		page.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				Log.i(TAG, "Click on page:");
 				page.requestFocus();
 			}
 		});
 		// requestLayout();
		refresh(page, sheet);
 		toolbar = (ViewGroup) inflater.inflate(R.layout.float_note_toolbar, this, false);
 		addToolbarButton(R.drawable.float_edit, new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 
 			}
 		});
 		toolbar.setVisibility(View.GONE);
 		addView(toolbar);
 	}
 
 	private ImageButton addToolbarButton(int resID, OnClickListener listener) {
		ImageButton button = (ImageButton) inflater.inflate(R.layout.float_note_button, toolbar, false);
 		button.setImageResource(resID);
 		button.setOnClickListener(listener);
		toolbar.addView(button);
 		return button;
 	}
 
	private void refresh(final PageSurface page, final SheetInfo sheet) {
 		decorate(page, sheet);
 		page.setOnLongClickListener(new OnLongClickListener() {
 
 			@Override
 			public boolean onLongClick(View v) {
 				int x = (int) (page.getLastDownX() * page.zoomFactor);
 				int y = (int) (page.getLastDownY() * page.zoomFactor);
 				Log.i(TAG, "On long page click: " + x + "x" + y);
 				NoteInfo info = new NoteInfo();
 				info.x = x;
 				info.y = y;
 				info.sheetID = sheet.id;
 				startEditor(info);
 				return true;
 			}
 		});
 		AsyncTask<Void, Void, List<NoteInfo>> task = new AsyncTask<Void, Void, List<NoteInfo>>() {
 
 			@Override
 			protected List<NoteInfo> doInBackground(Void... params) {
 				List<NoteInfo> notes = adapter.getController().getNotes(sheet.id);
 				return notes;
 			}
 
 			@Override
 			protected void onPostExecute(List<NoteInfo> result) {
 				for (NoteInfo info : result) { // Create textview's
 					TextView view = createNoteTextItem(page, info);
 					info.widget = view;
 				}
 				page.notes.clear();
 				page.notes.addAll(result);
 				requestLayout();
 			}
 		};
 		task.execute();
 		setOnKeyListener(new OnKeyListener() {
 
 			@Override
 			public boolean onKey(View v, int keyCode, KeyEvent event) {
 				Log.i(TAG, "Key event: " + keyCode);
 				return false;
 			}
 		});
 	}
 
 	//
 	// @Override
 	// protected void onLayout(boolean changed, int l, int t, int r, int b) {
 	// Log.i(TAG, "onLayout: " + toString() + ", " + l + "x" + t + " - " + r +
 	// "x" + b);
 	// super.onLayout(changed, l, t, r, b);
 	// }
 
 	private void decorate(PageSurface surface, SheetInfo info) {
 		if (android.os.Build.VERSION.SDK_INT >= 11) {
 			new PageDnDDecorator(surface, info);
 		}
 
 	}
 
 	protected TextView createNoteTextItem(final PageSurface page, final NoteInfo info) {
 		final TextView textView = new TextView(getContext());
 		int width = (int) (info.width / page.zoomFactor);
 		int height = RelativeLayout.LayoutParams.WRAP_CONTENT;
 		if (info.collapsible) { // Collapsible - collapse
 			height = (int) (COLLPASED_HEIGHT / page.zoomFactor);
 		}
 		// Log.i(TAG, "Render text view: " + info.x + ": " + page.zoomFactor +
 		// ", " + width);
 		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
 		params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
 		params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
 		params.leftMargin = (int) (info.x / page.zoomFactor + page.marginLeft);
 		params.topMargin = (int) (info.y / page.zoomFactor + page.marginTop);
 		textView.setBackgroundResource(adapter.getController().getBackgroundDrawable(info.color));
 		int textPadding = (int) (TEXT_PADDING / page.zoomFactor);
 		textView.setPadding(textPadding, textPadding, textPadding, textPadding);
 		textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, TEXT_SIZE / page.zoomFactor);
 		textView.setFocusable(true);
 		textView.setFocusableInTouchMode(true);
 		textView.setText(info.text);
 		addView(textView, params);
 		textView.setOnFocusChangeListener(new OnFocusChangeListener() {
 
 			@Override
 			public void onFocusChange(View v, boolean hasFocus) {
 				// Log.i(TAG, "Focus changed: " + info.id + ", " + hasFocus);
 				if (hasFocus) { // Bring to front first
 					textView.bringToFront();
 					// TODO: show toolbar
 				}
 				if (info.collapsible) { // Change state
 					RelativeLayout.LayoutParams params = (LayoutParams) textView.getLayoutParams();
 					if (hasFocus) { // Expand
 						params.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
 					} else {
 						params.height = (int) (COLLPASED_HEIGHT / page.zoomFactor);
 					}
 					info.collapsed = !info.collapsed;
 					textView.requestLayout();
 				}
 			}
 		});
 		textView.setOnLongClickListener(new OnLongClickListener() {
 
 			@Override
 			public boolean onLongClick(View arg0) {
 				Log.i(TAG, "Text long click");
 				startEditor(info);
 				return true;
 			}
 		});
 		textView.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				Log.i(TAG, "Clicked: " + info.id + ", " + v + ", " + info.collapsible);
 				// textView.bringToFront();
 				// if (info.collapsible) { // Change state
 				// RelativeLayout.LayoutParams params = (LayoutParams)
 				// textView.getLayoutParams();
 				// if (info.collapsed) { // Expand
 				// params.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
 				// } else {
 				// params.height = (int) (COLLPASED_HEIGHT / page.zoomFactor);
 				// }
 				// info.collapsed = !info.collapsed;
 				// textView.requestLayout();
 				// }
 			}
 		});
 		return textView;
 	}
 
 	private void startEditor(NoteInfo info) {
 		DialogFragment fragment = EditorDialogFragment.newInstance(info, adapter.getController());
 		fragment.show(activity.getSupportFragmentManager(), "editor");
 	}
 }
