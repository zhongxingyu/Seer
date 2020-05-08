 package mobi.monaca.framework.nativeui.container;
 
 import java.util.List;
 
 import static mobi.monaca.framework.nativeui.UIUtil.*;
 import mobi.monaca.framework.nativeui.UIContext;
 import mobi.monaca.framework.nativeui.UIUtil;
 import mobi.monaca.framework.nativeui.component.Component;
 import mobi.monaca.framework.nativeui.component.SearchBoxComponent;
 import mobi.monaca.framework.nativeui.component.ToolbarComponent;
 import mobi.monaca.framework.util.MyLog;
 
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.util.TypedValue;
 import android.view.Gravity;
 import android.view.View;
 import android.widget.FrameLayout;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 /**
  * This class represents toolbar view on native ui framework.
  */
 public class ToolbarContainerView extends LinearLayout {
 
 	private static final int CONTAINER_HEIGHT = 42;
 	protected LinearLayout left, center, right, titleWrapper,
 			titleSubtitleWrapper;
 	protected UIContext context;
 	protected FrameLayout content;
 	private TextView titleView;
 	private TextView subTitleMainTitleView;
 	private TextView subtitleView;
 
 	protected final static int TITLE_ID = 0;
 	protected final static int SUBTITLE_ID = 1;
 	private static final String TAG = ToolbarContainerView.class
 			.getSimpleName();
 
 	protected View createBorderView() {
 		View v = new FrameLayout(context);
 		v.setBackgroundColor(0xff000000);
 		return v;
 	}
 
 	@Override
 	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
 		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
 		
 		final int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
 		boolean canResizeWidth = widthSpecMode != MeasureSpec.EXACTLY;
 		if(canResizeWidth){
 			prepareCenterLayoutWidth();
 		}
 	}
 
 	protected void prepareCenterLayoutWidth() {
 		int leftComponentWidth = measureInnerWidth(left);
 		int rightComponentWidth = measureInnerWidth(right);
 		int padding = dip2px(context, 2);
 		MarginLayoutParams p = (MarginLayoutParams) center.getLayoutParams();
 		p.setMargins(leftComponentWidth + padding, 0, rightComponentWidth + padding, 0);
 		center.setLayoutParams(p);
 	}
 
 	protected int measureInnerWidth(LinearLayout layout) {
 		int result = 0;
 		for (int i = 0; i < layout.getChildCount(); i++) {
 			result += layout.getChildAt(i).getMeasuredWidth();
 		}
 		return result;
 	}
 
 	public ToolbarContainerView(UIContext context) {
 		super(context);
 
 		this.context = context;
 
 		setOrientation(LinearLayout.VERTICAL);
 		setFocusable(true);
 		setFocusableInTouchMode(true);
 
 		content = new FrameLayout(context);
 
 		addView(createBorderView(), new LinearLayout.LayoutParams(
 				LinearLayout.LayoutParams.MATCH_PARENT, 1));
 		addView(content, new LinearLayout.LayoutParams(
 				LinearLayout.LayoutParams.MATCH_PARENT,
 				UIUtil.dip2px(getContext(), CONTAINER_HEIGHT)) );
 		addView(createBorderView(), new LinearLayout.LayoutParams(
 				LinearLayout.LayoutParams.MATCH_PARENT, 1));
 
 		left = new LinearLayout(context);
 		left.setOrientation(LinearLayout.HORIZONTAL);
 		left.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
 
 		center = new LinearLayout(context);
 		center.setOrientation(LinearLayout.HORIZONTAL);
 		center.setGravity(Gravity.CENTER | Gravity.CENTER_VERTICAL);
 
 		right = new LinearLayout(context);
 		right.setOrientation(LinearLayout.HORIZONTAL);
 		right.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
 
 		titleView = new TextView(context);
 		titleView.setId(TITLE_ID);
 		titleView.setTextColor(0xffffffff);
 		titleView.setShadowLayer(1.0f, 0f, -1f, 0xcc000000);
 		titleView.setTypeface(null, Typeface.BOLD);
 		titleView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
				context.getFontSizeFromDip(Component.BIG_TITLE_TEXT_DIP));
 
 		titleWrapper = new LinearLayout(context);
 		titleWrapper.setOrientation(LinearLayout.HORIZONTAL);
 		titleWrapper.setVisibility(View.GONE);
 		titleWrapper.setGravity(Gravity.CENTER);
 		titleWrapper.addView(titleView);
 
 		subTitleMainTitleView = new TextView(context);
 		subTitleMainTitleView.setId(TITLE_ID);
 		subTitleMainTitleView.setTextColor(0xffffffff);
 		subTitleMainTitleView.setShadowLayer(1.0f, 0f, -1f, 0xcc000000);
 		subTitleMainTitleView.setTypeface(null, Typeface.BOLD);
 		subTitleMainTitleView.setPadding(0, 0, 0, 0);
 		subTitleMainTitleView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
				context.getFontSizeFromDip(Component.TITLE_TEXT_DIP));
 
 		subtitleView = new TextView(context);
 		subtitleView.setId(SUBTITLE_ID);
 		subtitleView.setTextColor(0xffffffff);
 		subtitleView.setShadowLayer(1.0f, 0f, -1f, 0xcc000000);
 		subtitleView.setPadding(0, 0, 0, 0);
 		subtitleView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
 				context.getFontSizeFromDip(Component.SUBTITLE_TEXT_DIP));
 
 		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
 				FrameLayout.LayoutParams.WRAP_CONTENT,
 				FrameLayout.LayoutParams.MATCH_PARENT, Gravity.CENTER
 						| Gravity.CENTER_VERTICAL);
 
 		titleSubtitleWrapper = new LinearLayout(context);
 		titleSubtitleWrapper.setOrientation(LinearLayout.VERTICAL);
 		titleSubtitleWrapper.setVisibility(View.GONE);
 		titleSubtitleWrapper.setGravity(Gravity.CENTER
 				| Gravity.CENTER_VERTICAL);
 		titleSubtitleWrapper.addView(subtitleView, lp);
 		titleSubtitleWrapper.addView(subTitleMainTitleView, lp);
 
 		FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(
 				FrameLayout.LayoutParams.MATCH_PARENT,
 				FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER
 						| Gravity.CENTER_VERTICAL);
 
 		content.addView(left, new FrameLayout.LayoutParams(
 				FrameLayout.LayoutParams.WRAP_CONTENT,
 				FrameLayout.LayoutParams.MATCH_PARENT, Gravity.LEFT
 						| Gravity.CENTER_VERTICAL));
 		content.addView(right, new FrameLayout.LayoutParams(
 				FrameLayout.LayoutParams.WRAP_CONTENT,
 				FrameLayout.LayoutParams.MATCH_PARENT, Gravity.RIGHT
 						| Gravity.CENTER_VERTICAL));
 		content.addView(center, new FrameLayout.LayoutParams(
 				FrameLayout.LayoutParams.WRAP_CONTENT,
 				FrameLayout.LayoutParams.MATCH_PARENT, Gravity.CENTER));
 		center.setGravity(Gravity.CENTER);
 		content.addView(titleWrapper, p);
 		content.addView(titleSubtitleWrapper, p);
 	}
 
 	public View getContentView() {
 		return content;
 	}
 
 	public void setTitleSubtitle(String title, String subtitle) {
 		MyLog.v(TAG, "setTitleSubtitle: title:" + title + ", subtitle:"
 				+ subtitle);
 
 		if (subtitle.length() > 0) {
 			titleSubtitleWrapper.setVisibility(View.VISIBLE);
 			titleWrapper.setVisibility(View.GONE);
 			center.setVisibility(View.GONE);
 		} else if (title.length() > 0) {
 			titleWrapper.setVisibility(View.VISIBLE);
 			center.setVisibility(View.GONE);
 			titleSubtitleWrapper.setVisibility(View.GONE);
 		} else {
 			titleWrapper.setVisibility(View.GONE);
 			center.setVisibility(View.VISIBLE);
 			titleSubtitleWrapper.setVisibility(View.GONE);
 		}
 
 		((TextView) titleWrapper.findViewById(TITLE_ID)).setText(title);
 		((TextView) titleSubtitleWrapper.findViewById(TITLE_ID)).setText(title);
 		((TextView) titleSubtitleWrapper.findViewById(SUBTITLE_ID))
 				.setText(subtitle);
 	}
 
 	public void setRightView(List<ToolbarComponent> list) {
 		right.removeAllViews();
 		for (Component component : list) {
 			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
 					LinearLayout.LayoutParams.WRAP_CONTENT,
 					LinearLayout.LayoutParams.MATCH_PARENT);
 			params.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
 			params.setMargins(dip2px(context, 4), 0, 0, 0);
 			right.addView(component.getView(), params);
 		}
 	}
 
 	public void setLeftView(List<ToolbarComponent> list) {
 		left.removeAllViews();
 		for (Component component : list) {
 			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
 					LinearLayout.LayoutParams.WRAP_CONTENT,
 					LinearLayout.LayoutParams.MATCH_PARENT);
 			params.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
 			params.setMargins(0, 0, dip2px(context, 4), 0);
 			left.addView(component.getView(), params);
 		}
 	}
 
 	public void setCenterView(List<ToolbarComponent> list,
 			boolean expandItemWidth) {
 		MyLog.v(TAG, "setCenterView, list:" + list + ", expandItemWidth:"
 				+ expandItemWidth);
 		center.removeAllViews();
 
 		if (expandItemWidth) {
 			for (Component component : list) {
 				FrameLayout container = new FrameLayout(context);
 				FrameLayout.LayoutParams containerLayoutParams = new FrameLayout.LayoutParams(
 						FrameLayout.LayoutParams.WRAP_CONTENT,
 						FrameLayout.LayoutParams.MATCH_PARENT);
 				containerLayoutParams.gravity = Gravity.CENTER
 						| Gravity.CENTER_VERTICAL;
 				container.addView(component.getView(), containerLayoutParams);
 
 				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
 						LinearLayout.LayoutParams.WRAP_CONTENT,
 						LinearLayout.LayoutParams.MATCH_PARENT);
 				params.setMargins(dip2px(context, 2), 0, dip2px(context, 2), 0);
 				params.weight = 1;
 
 				center.addView(container, params);
 			}
 			content.removeView(left);
 			content.removeView(right);
 			// content.removeView(titleWrapper);
 			// content.removeView(titleSubtitleWrapper);
 			MyLog.v(TAG, "removed titleWrapper");
 		} else {
 			for (Component component : list) {
 				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
 						LinearLayout.LayoutParams.WRAP_CONTENT,
 						LinearLayout.LayoutParams.MATCH_PARENT);
 				params.setMargins(dip2px(context, 2), 0, dip2px(context, 2), 0);
 
 				if (component instanceof SearchBoxComponent) {
 					params.width = LinearLayout.LayoutParams.MATCH_PARENT;
 				}
 				center.addView(component.getView(), params);
 			}
 		}
 	}
 
 	public void setTitleColor(String colorString) {
 		// make sure we start with # sign format
 		if (!colorString.startsWith("#")) {
 			colorString = "#" + colorString;
 		}
 		try {
 			if (titleView != null) {
 				titleView.setTextColor(Color.parseColor(colorString));
 			}
 
 			if (subTitleMainTitleView != null) {
 				subTitleMainTitleView.setTextColor(Color
 						.parseColor(colorString));
 			}
 		} catch (NumberFormatException e) {
 			MyLog.e(TAG, e.getMessage());
 		}
 	}
 
 	public void setSubtitleColor(String colorString) {
 		// make sure we start with # sign format
 		if (!colorString.startsWith("#")) {
 			colorString = "#" + colorString;
 		}
 		try {
 			subtitleView.setTextColor(Color.parseColor(colorString));
 		} catch (NumberFormatException e) {
 			MyLog.e(TAG, e.getMessage());
 		}
 	}
 
 	public void setTitleFontScale(String titleFontScale) {
 		if (titleFontScale == "")
 			return;
 
 		try {
 			float titleFontScaleFloat = Float.parseFloat(titleFontScale);
 			if (titleView != null) {
				titleView.setTextSize(titleFontScaleFloat);
 			}
 
 			if (subTitleMainTitleView != null) {
				subTitleMainTitleView.setTextSize(titleFontScaleFloat);
 			}
 
 		} catch (Exception e) {
 			MyLog.e(TAG, e.getMessage());
 		}
 	}
 
 	public void setSubitleFontScale(String subtitleFontScale) {
 		if (subtitleFontScale == "")
 			return;
 
 		try {
 			float titleFontScaleFloat = Float.parseFloat(subtitleFontScale);
 			if (subtitleView != null) {
				subtitleView.setTextSize(titleFontScaleFloat);
 			}
 		} catch (Exception e) {
 			MyLog.e(TAG, e.getMessage());
 		}
 	}
 
 }
