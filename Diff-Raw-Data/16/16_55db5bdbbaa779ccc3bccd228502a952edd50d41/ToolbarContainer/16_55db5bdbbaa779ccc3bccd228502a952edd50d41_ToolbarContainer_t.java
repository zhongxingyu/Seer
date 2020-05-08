 package mobi.monaca.framework.nativeui.container;
 
 import static mobi.monaca.framework.nativeui.UIUtil.buildColor;
 import static mobi.monaca.framework.nativeui.UIUtil.buildOpacity;
 import static mobi.monaca.framework.nativeui.UIUtil.updateJSONObject;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import mobi.monaca.framework.nativeui.DefaultStyleJSON;
 import mobi.monaca.framework.nativeui.UIContext;
 import mobi.monaca.framework.nativeui.UIValidator;
 import mobi.monaca.framework.nativeui.component.BackButtonComponent;
 import mobi.monaca.framework.nativeui.component.ButtonComponent;
 import mobi.monaca.framework.nativeui.component.LabelComponent;
 import mobi.monaca.framework.nativeui.component.SearchBoxComponent;
 import mobi.monaca.framework.nativeui.component.SegmentComponent;
 import mobi.monaca.framework.nativeui.component.ToolbarBackgroundDrawable;
 import mobi.monaca.framework.nativeui.component.ToolbarComponent;
 import mobi.monaca.framework.nativeui.component.view.ContainerShadowView;
 import mobi.monaca.framework.nativeui.exception.ConversionException;
 import mobi.monaca.framework.nativeui.exception.DuplicateIDException;
 import mobi.monaca.framework.nativeui.exception.InvalidValueException;
 import mobi.monaca.framework.nativeui.exception.KeyNotValidException;
 import mobi.monaca.framework.nativeui.exception.NativeUIException;
 import mobi.monaca.framework.nativeui.exception.NativeUIIOException;
 import mobi.monaca.framework.nativeui.exception.RequiredKeyNotFoundException;
 import mobi.monaca.framework.nativeui.exception.ValueNotInRangeException;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.graphics.Bitmap;
 import android.graphics.ColorFilter;
 import android.graphics.PorterDuff;
 import android.graphics.PorterDuffColorFilter;
 import android.graphics.drawable.Drawable;
 import android.view.View;
 import android.view.animation.AlphaAnimation;
 import android.view.animation.Animation;
 import android.view.animation.LinearInterpolator;
 
 public class ToolbarContainer extends Container {
 	protected ToolbarContainerView view;
 	protected ToolbarComponent left, center, right;
 	protected AlphaAnimation animation = null;
 	private ContainerShadowView shadowView;
 
 	protected static String[] toolbarValidKeys = { "container", "style", "iosStyle", "androidStyle", "id", "left", "center", "right" };
 
 	protected static String[] styleValidKeys = { "visibility", "disable", "opacity", "shadowOpacity", "backgroundColor", "title", "subtitle", "titleColor", "subtitleColor",
 			"titleFontScale", "subtitleFontScale", "titleImage", "iosBarStyle", };
 
 	protected static String[] validComponents = { "backButton", "button", "searchBox", "label", "segment" };
 
 	public ToolbarContainer(UIContext context, JSONObject toolbarJSON, boolean isTop) throws KeyNotValidException, DuplicateIDException, NativeUIIOException,
 			NativeUIException, JSONException {
 		super(context, toolbarJSON);
 		UIValidator.validateKey(context, "Toolbar's style", style, styleValidKeys);
 
 		view = new ToolbarContainerView(context, isTop);
 		shadowView = new ContainerShadowView(context, isTop);
 		buildChildren();
 		style();
 	}
 
 	private void buildChildren() throws NativeUIException, JSONException {
 		JSONArray left = getComponentJSON().optJSONArray("left");
 		if (left != null) {
 			ArrayList<ToolbarComponent> leftComponents = buildComponents("left", left);
 			view.setLeftView(leftComponents);
 		}
 		JSONArray right = getComponentJSON().optJSONArray("right");
 		if (right != null) {
 			ArrayList<ToolbarComponent> rightComponents = buildComponents("right", right);
 			view.setRightView(rightComponents);
 		}
 
 		JSONArray center = getComponentJSON().optJSONArray("center");
 		if (center != null) {
 			ArrayList<ToolbarComponent> centerComponents = buildComponents("center", center);
 			boolean shouldExpandItemWidth = false;
 			if ((left == null && right == null) || (left == null && right.length() == 0) || (left.length() == 0 && right == null)
 					|| (left.length() == 0 && right.length() == 0)) {
 				shouldExpandItemWidth = true;
 			}
 			view.setCenterView(centerComponents, shouldExpandItemWidth);
 		}
 	}
 
 	private ArrayList<ToolbarComponent> buildComponents(String position, JSONArray componentsJSONArray) throws NativeUIException, JSONException {
 		ArrayList<ToolbarComponent> leftComponents = new ArrayList<ToolbarComponent>();
 		ToolbarComponent component;
 		JSONObject componentJSON;
 		for (int i = 0; i < componentsJSONArray.length(); i++) {
 			componentJSON = componentsJSONArray.optJSONObject(i);
 			component = buildComponent(position, componentJSON);
 			leftComponents.add(component);
 		}
 		return leftComponents;
 	}
 
 	private ToolbarComponent buildComponent(String positioin, JSONObject childJSON) throws NativeUIException, JSONException{
 		String componentType = childJSON.optString("component");
 		if(componentType == null){
 			throw new RequiredKeyNotFoundException(getComponentName() + positioin, "component");
 		}
 		if (componentType.equals("backButton")) {
 			return new BackButtonComponent(uiContext, childJSON);
 		} else if (componentType.equals("button")) {
 			return new ButtonComponent(uiContext, childJSON);
 		} else if (componentType.equals("searchBox")) {
 			return new SearchBoxComponent(uiContext, childJSON);
 		} else if (componentType.equals("label")) {
 			return new LabelComponent(uiContext, childJSON);
 		} else if (componentType.equals("segment")) {
 			return new SegmentComponent(uiContext, childJSON);
 		} else {
 			InvalidValueException exception = new InvalidValueException("Toolbar", "component", componentType, validComponents);
 			throw exception;
 		}
 	}
 
 	public void updateStyle(JSONObject update) throws NativeUIException {
 		updateJSONObject(style, update);
 		style();
 	}
 
 	public View getView() {
 		return view;
 	}
 
 	/**
 	 * visibility: [bool] (default: true) opacity: 0.0-1.0 [float] (default:
 	 * 1.0) backgroundColor: #000000 [string] (default: undefined) position :
 	 * "fixed" | "scroll" (default: "fixed") => androidだと無理ぽい title : [string]
 	 * (default : "") (このスタイルが指定された場合、center属性は無視される) titleImage : [string]
 	 * (default : "") このスタイルが指定された時、center属性は無視)
 	 * 
 	 * @throws NativeUIException
 	 */
 	protected void style() throws NativeUIException {
 		String toolbarOpacityString = "1.0";
 		float toolbarOpacity = 1.0f;
 		if(style.has("opacity")){
 			toolbarOpacityString = style.optString("opacity");
 		}
 		try{
 			toolbarOpacity = Float.parseFloat(toolbarOpacityString);
 			if(toolbarOpacity < 0.0 || toolbarOpacity > 1.0 ){
 				throw new ValueNotInRangeException(getComponentName() + "style", "opacity", toolbarOpacityString, "[0.0-1.0]");
 			}
 		}catch(NumberFormatException e){
 			ConversionException conversionException = new ConversionException(getComponentName() + " style", "opacity", toolbarOpacityString, "Float");
 			throw conversionException;
 		}
 		
 		if (isTransparent() && view.getVisibility() != (style.optBoolean("visibility", true) ? View.VISIBLE : View.INVISIBLE)) {
 			if (animation != null) {
 				// animation.cancel(); //TODO only available in Android 4.0
 			}
 
 			animation = style.optBoolean("visibility", true) ? new AlphaAnimation(0f, 1.0f) : new AlphaAnimation(1.0f, 0f);
 			animation.setAnimationListener(new Animation.AnimationListener() {
 				@Override
 				public void onAnimationStart(Animation animation) {
 				}
 
 				@Override
 				public void onAnimationRepeat(Animation animation) {
 				}
 
 				@Override
 				public void onAnimationEnd(Animation animation) {
 					view.setVisibility(style.optBoolean("visibility", true) ? View.VISIBLE : View.INVISIBLE);
 					ToolbarContainer.this.animation = null;
 				}
 			});
 			animation.setInterpolator(new LinearInterpolator());
 			animation.setDuration(200);
 
 			// cause GC to prevent "stop the world" on animation.
 			System.gc();
 
 			view.startAnimation(animation);
 		} else {
 			view.setVisibility(style.optBoolean("visibility", true) ? View.VISIBLE : View.GONE);
 		}
 
 		// titleColor
 		String titleColorString = style.optString("titleColor", "#ffffff");
 		try {
 			view.setTitleColor(titleColorString);
 		} catch (IllegalArgumentException e) {
 			ConversionException conversionException = new ConversionException(getComponentName() + " style", "titleColor", titleColorString, "Color");
 			throw conversionException;
 		}
 
 		// subtitleColor
 		String subtitleColorString = style.optString("subtitleColor", "#ffffff");
 		try {
 			view.setSubtitleColor(subtitleColorString);
 		} catch (IllegalArgumentException e) {
 			ConversionException conversionException = new ConversionException(getComponentName() + " style", "subtitleColor", subtitleColorString, "Color");
 			throw conversionException;
 		}
 		
 		// titleFontScale
 		String titleFontScaleString = style.optString("titleFontScale", "");
 		try{
 			view.setTitleFontScale(titleFontScaleString);
 		}catch(NumberFormatException e){
 			ConversionException conversionException = new ConversionException(getComponentName() + " style", "titleFontScale", titleFontScaleString, "Float");
 			throw conversionException;
 		}
 
 		// subtitleFontScale
 		String subtitleFontScaleString = style.optString("subtitleFontScale", "");
 		try{
 			view.setSubitleFontScale(subtitleFontScaleString);
 		}catch(NumberFormatException e){
 			ConversionException conversionException = new ConversionException(getComponentName() + " style", "subtitleFontScale", subtitleFontScaleString, "Float");
 			throw conversionException;
 		}
 
 		String titleImagePath = style.optString("titleImage", "");
 		Bitmap titleImage;
 		try {
 			titleImage = titleImagePath.equals("") ? null : uiContext.readScaledBitmap(titleImagePath);
 			view.setTitleSubtitle(style.optString("title"), style.optString("subtitle"), titleImage);
 		} catch (IOException e) {
 			NativeUIIOException exception = new NativeUIIOException(getComponentName() + " style", "titleImage", titleImagePath, e.getMessage());
 			throw exception;
 		}
 
 		String backgroundColorString = style.optString("backgroundColor", "#000000");
 		try {
 			ColorFilter filter = new PorterDuffColorFilter(buildColor(backgroundColorString), PorterDuff.Mode.SCREEN);
 			Drawable toolbarBackground = new ToolbarBackgroundDrawable(uiContext);
 			toolbarBackground.setColorFilter(filter);
 			toolbarBackground.setAlpha(buildOpacity(style.optDouble("opacity", 1.0)));
 			view.getContentView().setBackgroundDrawable(toolbarBackground);
 		} catch (IllegalArgumentException e) {
 			ConversionException conversionException = new ConversionException(getComponentName() + " style", "backgroundColor", backgroundColorString, "Color");
 			throw conversionException;
 		}
 
 		String shadowOpacityString = "0.3";
 		if(style.has("shadowOpacity")){
 			shadowOpacityString = style.optString("shadowOpacity");
 		}
 		try{
 			double shadowOpacity = Float.parseFloat(shadowOpacityString);
 			if(shadowOpacity < 0.0 || shadowOpacity > 1.0){
 				throw new ValueNotInRangeException(getComponentName() + " style", "shadowOpacity", shadowOpacityString, "[0.0-1.0]");
 			}
 			double relativeShadowOpacity = toolbarOpacity * shadowOpacity;
 			shadowView.getBackground().setAlpha(buildOpacity(relativeShadowOpacity));
 		}catch(NumberFormatException e){
 			ConversionException conversionException = new ConversionException(getComponentName() + " style", "shadowOpacity", shadowOpacityString, "Float");
 			throw conversionException;
 		}
 
 		view.setBackgroundDrawable(null);
 		view.setBackgroundColor(0);
 
 		view.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				view.requestFocus();
 			}
 		});
 	}
 
 	@Override
 	protected void finalize() throws Throwable {
 		super.finalize();
 		if (view != null && view.getContentView() != null) {
 			view.getContentView().setBackgroundDrawable(null);
 		}
 	}
 
 	public boolean isTransparent() {
 		double opacity = style.optDouble("opacity", 1.0);
 		return opacity <= 0.999;
 	}
 
 	public View getShadowView() {
 		return shadowView;
 	}
 
 	@Override
 	public String getComponentName() {
 		return "Toolbar";
 	}
 
 	@Override
 	public JSONObject getDefaultStyle() {
 		return DefaultStyleJSON.toolbar();
 	}
 
 	@Override
 	public String[] getValidKeys() {
 		return toolbarValidKeys;
 	}
 }
