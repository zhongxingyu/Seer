 package mobi.monaca.framework.nativeui.container;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import mobi.monaca.framework.nativeui.UIContext;
 import mobi.monaca.framework.nativeui.UIUtil;
 import mobi.monaca.framework.nativeui.component.Component;
 import mobi.monaca.framework.nativeui.container.TabbarItem.TabbarItemView;
 import mobi.monaca.framework.psedo.R;
 import mobi.monaca.framework.util.MyLog;
 import static mobi.monaca.framework.nativeui.UIUtil.*;
 
 import org.json.JSONObject;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.ColorFilter;
 import android.graphics.PorterDuff;
 import android.graphics.PorterDuffColorFilter;
 import android.graphics.drawable.BitmapDrawable;
 import android.view.Gravity;
 import android.view.View;
 import android.widget.FrameLayout;
 import android.widget.LinearLayout;
 
 public class TabbarContainer implements Component {
 
 	protected TabbarContainerView view;
 	protected Context context;
 	protected JSONObject style;
 	protected List<TabbarItem> items;
 	protected Integer oldActiveIndex = null;
 
 	public TabbarContainer(UIContext context, List<TabbarItem> items, JSONObject style) {
 		MyLog.v(TAG, "TabbarContainer constructor. items:" + items + ", style:" + style);
 		this.context = context;
 		this.style = style == null ? new JSONObject() : style;
 		this.view = new TabbarContainerView(context);
 		this.items = items;
 
 		for (TabbarItem item : items) {
 			view.addTabbarItemView(item.getView());
 		}
 
 		style();
 	}
 
 	public void updateStyle(JSONObject update) {
 		oldActiveIndex = style.has("activeIndex") ? style.optInt("activeIndex", 0) : null;
 		updateJSONObject(style, update);
 		style();
 	}
 
 	@Override
 	protected void finalize() throws Throwable {
 		super.finalize();
 		view.getContentView().setBackgroundDrawable(null);
 	}
 
 	/*
 	 * visibility: true / false [bool] (default: true) opacity: 0.0～1.0 [float]
 	 * (default: 1.0) backgroundColor: #000000 [string] (default: #000000)
 	 * activeIndex: 0 [int] (default: 0)
 	 */
 	protected void style() {
 		view.setVisibility(style.optBoolean("visibility", true) ? View.VISIBLE : View.GONE);
 
 		ColorFilter filter = new PorterDuffColorFilter(buildColor(style.optString("backgroundColor", "#000000")), PorterDuff.Mode.SCREEN);
 		Bitmap bgBitmap = UIUtil.createBitmapWithColorFilter(view.getContentView().getBackground(), filter);
 
 		view.getContentView().setBackgroundResource(R.drawable.monaca_tabbar_bg);
 		view.getContentView().setBackgroundDrawable(new BitmapDrawable(context.getResources(), bgBitmap));
 		view.getContentView().getBackground().setAlpha(buildOpacity(style.optDouble("opacity")));
 
 		if (oldActiveIndex != null && style.optInt("activeIndex", 0) != oldActiveIndex) {
 			view.setActiveIndex(style.optInt("activeIndex", 0));
 		}
 
 		double shadowOpacity = style.optDouble("shadowOpacity", 0.5);
 		view.getShadowView().getBackground().setAlpha(buildOpacity(shadowOpacity));
 	}
 
 	public JSONObject getStyle() {
 		return style;
 	}
 
 	public View getView() {
 		return view;
 	}
 
 	public class TabbarContainerView extends LinearLayout implements View.OnClickListener, ContainerViewInterface {
 
 		protected ArrayList<TabbarItem.TabbarItemView> items = new ArrayList<TabbarItem.TabbarItemView>();
 		protected TabbarItemView currentItemView = null;
 		protected LinearLayout content;
 		private int mShadowHeight;
 		private View shadowView;
 		private ToolbarContainerViewListener mContainerSizeListener;
 
 		public TabbarContainerView(UIContext context) {
 			super(context);
 			setOrientation(LinearLayout.VERTICAL);
 
 			content = new LinearLayout(context);
 			content.setOrientation(LinearLayout.HORIZONTAL);
 
 			content.setGravity(Gravity.CENTER | Gravity.CENTER_VERTICAL);
 			content.setBackgroundResource(R.drawable.monaca_tabbar_bg);
 
 			shadowView = new View(getContext());
 			shadowView.setBackgroundResource(R.drawable.shadow_bg_reverse);
 			mShadowHeight = UIUtil.dip2px(getContext(), 3);
 			addView(shadowView, LinearLayout.LayoutParams.MATCH_PARENT, mShadowHeight);
 
 			int borderWidth = context.getSettings().disableUIContainerBorder ? 0 : 1;
             addView(createBorderView(), new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, borderWidth));
 			addView(content, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
 		}
 
 		public View getShadowView() {
 			return shadowView;
 		}
 
 		public View getContentView() {
 			return content;
 		}
 
 		protected View createBorderView() {
 			View v = new FrameLayout(context);
 			v.setBackgroundColor(0xff000000);
 			return v;
 		}
 
 		public void addTabbarItemView(TabbarItem.TabbarItemView itemView) {
 			items.add(itemView);
 			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
 			params.setMargins(0, 0, 0, 0);
 			params.gravity = Gravity.CENTER_VERTICAL;
 			params.weight = 1;
 
 			content.addView(itemView, params);
 
 			int activeIndex = style.optInt("activeIndex", 0);
 			if (items.size() - 1 == activeIndex) {
 				itemView.initializeSelected();
 				currentItemView = itemView;
 			}
 
 			itemView.setOnClickListener(this);
 		}
 
 		public void setActiveIndex(int index) {
 			if (items.size() <= index) {
 				index = 0;
 			}
 			if (currentItemView != null) {
 				currentItemView.switchToUnselected();
 				currentItemView = null;
 			}
 			if (items.size() - 1 >= index) {
 				currentItemView = items.get(index);
 				currentItemView.switchToSelected();
 			}
 		}
 
 		@Override
 		public void onClick(View v) {
 			TabbarItemView item = (TabbarItemView) v;
 			item.switchToSelected();
 			item.requestFocus();
 
 			if (currentItemView != null) {
 				currentItemView.switchToUnselected();
 				currentItemView = item;
 				item.switchToSelected();
 			}
 		}
 		
 
 		@Override
 		public int getShadowHeight() {
 			return mShadowHeight;
 		}
 
 		@Override
 		public void setContainerSizeListener(ToolbarContainerViewListener mContainerSizeListener) {
 			this.mContainerSizeListener = mContainerSizeListener;
 		}
 		
 		@Override
 		public void setVisibility(int visibility) {
 			if (getVisibility() != visibility) {
 				if (mContainerSizeListener != null) {
 					mContainerSizeListener.onVisibilityChanged(visibility);
 				}
 			}
 			super.setVisibility(visibility);
 		}
 		
 		@Override
 		protected void onSizeChanged(int w, int h, int oldw, int oldh) {
 			super.onSizeChanged(w, h, oldw, oldh);
 			if(mContainerSizeListener != null){
 				mContainerSizeListener.onSizeChanged(w, h, oldw, oldh);
 			}
 		}
 
 		@Override
 		public int getContainerViewHeight() {
 			return getMeasuredHeight();
 		}
 	}
 }
