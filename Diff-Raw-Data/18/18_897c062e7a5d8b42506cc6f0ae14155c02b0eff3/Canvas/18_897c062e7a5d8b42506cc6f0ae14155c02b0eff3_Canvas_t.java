 package org.vaadin.smartgwt.server;
 
 import java.util.Map;
 import java.util.Set;
 
 import org.vaadin.smartgwt.server.events.ClickEvent;
 import org.vaadin.smartgwt.server.events.ClickHandler;
 import org.vaadin.smartgwt.server.events.HasClickHandlers;
 import org.vaadin.smartgwt.server.menu.Menu;
 import org.vaadin.smartgwt.server.types.Alignment;
 import org.vaadin.smartgwt.server.types.AnimationAcceleration;
 import org.vaadin.smartgwt.server.types.AnimationEffect;
 import org.vaadin.smartgwt.server.types.BkgndRepeat;
 import org.vaadin.smartgwt.server.types.Cursor;
 import org.vaadin.smartgwt.server.types.DragAppearance;
 import org.vaadin.smartgwt.server.types.DragIntersectStyle;
 import org.vaadin.smartgwt.server.types.DrawPosition;
 import org.vaadin.smartgwt.server.types.LocatorStrategy;
 import org.vaadin.smartgwt.server.types.LocatorTypeStrategy;
 import org.vaadin.smartgwt.server.types.Overflow;
 import org.vaadin.smartgwt.server.types.PercentBoxModel;
 import org.vaadin.smartgwt.server.types.Positioning;
 import org.vaadin.smartgwt.server.types.VerticalAlignment;
 import org.vaadin.smartgwt.server.types.Visibility;
 import org.vaadin.smartgwt.server.util.EnumUtil;
 
 import com.google.common.collect.Sets;
 import com.google.web.bindery.event.shared.HandlerRegistration;
 import com.vaadin.terminal.PaintException;
 import com.vaadin.terminal.PaintTarget;
 
 public class Canvas extends BaseWidget implements HasClickHandlers {
 	private final Set<ClickHandler> clickHandlers = Sets.newHashSet();
 
 	public Canvas() {
 		scClassName = "Canvas";
 	}
 
 	public Canvas(String id) {
 		scClassName = "Canvas";
 	}
 
 	/**
 	 * If specified this governs the accessKey for the widget. This should be set to a character - when a user hits
 	 * Alt+[accessKey], or in Mozilla Firefox 2.0 and above, Shift+Alt+[accessKey], focus will be given to the widget in
 	 * question.
 	 *
 	 * <br><br>If this method is called after the component has been drawn/initialized:
 	 * Set the accessKey for this canvas. <P> The accessKey can be set to any alphanumeric character (symbols not supported) Having set an accessKey, the canvas will be given focus when the user hits  Alt+[accessKey], or in Mozilla Firefox 2.0 and above, Shift+Alt+[accessKey].
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param accessKey Character to use as an accessKey for this widget.  Case Insensitive.. Default value is null
 	 * @see com.smartgwt.client.docs.Focus Focus overview and related methods
 	 */
 	public void setAccessKey(String accessKey) {
 		setAttribute("accessKey", accessKey, true);
 	}
 
 	/**
 	 * If specified this governs the accessKey for the widget. This should be set to a character - when a user hits
 	 * Alt+[accessKey], or in Mozilla Firefox 2.0 and above, Shift+Alt+[accessKey], focus will be given to the widget in
 	 * question.
 	 *
 	 *
 	 * @return String
 	 * @see com.smartgwt.client.docs.Focus Focus overview and related methods
 	 */
 	public String getAccessKey() {
 		return getAttributeAsString("accessKey");
 	}
 
 	/**
 	 * Default acceleration effect to apply to all animations on this Canvas. Can be overridden by setting
 	 * animationAcceleration for specific animations or by passing an acceleration function directly into the appropriate
 	 * method.
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param animateAcceleration animateAcceleration Default value is "smoothEnd"
 	 */
 	public void setAnimateAcceleration(AnimationAcceleration animateAcceleration) {
 		setAttribute("animateAcceleration", animateAcceleration.getValue(), true);
 	}
 
 	/**
 	 * Default acceleration effect to apply to all animations on this Canvas. Can be overridden by setting
 	 * animationAcceleration for specific animations or by passing an acceleration function directly into the appropriate
 	 * method.
 	 *
 	 *
 	 * @return AnimationAcceleration
 	 */
 	public AnimationAcceleration getAnimateAcceleration() {
 		return EnumUtil.getEnum(AnimationAcceleration.values(), getAttribute("animateAcceleration"));
 	}
 
 	/**
 	 * Default time for performing an animated fade.  If unset, <code>this.animateTime</code> will be used by default instead
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param animateFadeTime animateFadeTime Default value is null
 	 */
 	public void setAnimateFadeTime(Integer animateFadeTime) {
 		setAttribute("animateFadeTime", animateFadeTime, true);
 	}
 
 	/**
 	 * Default time for performing an animated fade.  If unset, <code>this.animateTime</code> will be used by default instead
 	 *
 	 *
 	 * @return Integer
 	 */
 	public Integer getAnimateFadeTime() {
 		return getAttributeAsInt("animateFadeTime");
 	}
 
 	/**
 	 * Default acceleration function for performing an animated hide.  If unset,  <code>this.animateAcceleration</code> will be
 	 * used by default instead
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param animateHideAcceleration animateHideAcceleration Default value is null
 	 */
 	public void setAnimateHideAcceleration(AnimationAcceleration animateHideAcceleration) {
 		setAttribute("animateHideAcceleration", animateHideAcceleration.getValue(), true);
 	}
 
 	/**
 	 * Default acceleration function for performing an animated hide.  If unset,  <code>this.animateAcceleration</code> will be
 	 * used by default instead
 	 *
 	 *
 	 * @return AnimationAcceleration
 	 */
 	public AnimationAcceleration getAnimateHideAcceleration() {
 		return EnumUtil.getEnum(AnimationAcceleration.values(), getAttribute("animateHideAcceleration"));
 	}
 
 	/**
 	 * Default time for performing an animated hide.  If unset, <code>this.animateTime</code> will be used by default instead
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param animateHideTime animateHideTime Default value is null
 	 */
 	public void setAnimateHideTime(Integer animateHideTime) {
 		setAttribute("animateHideTime", animateHideTime, true);
 	}
 
 	/**
 	 * Default time for performing an animated hide.  If unset, <code>this.animateTime</code> will be used by default instead
 	 *
 	 *
 	 * @return Integer
 	 */
 	public Integer getAnimateHideTime() {
 		return getAttributeAsInt("animateHideTime");
 	}
 
 	/**
 	 * Default acceleration effect for performing an animated move.  If unset,  <code>this.animateAcceleration</code> will be
 	 * used by default instead
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param animateMoveAcceleration animateMoveAcceleration Default value is null
 	 */
 	public void setAnimateMoveAcceleration(AnimationAcceleration animateMoveAcceleration) {
 		setAttribute("animateMoveAcceleration", animateMoveAcceleration.getValue(), true);
 	}
 
 	/**
 	 * Default acceleration effect for performing an animated move.  If unset,  <code>this.animateAcceleration</code> will be
 	 * used by default instead
 	 *
 	 *
 	 * @return AnimationAcceleration
 	 */
 	public AnimationAcceleration getAnimateMoveAcceleration() {
 		return EnumUtil.getEnum(AnimationAcceleration.values(), getAttribute("animateMoveAcceleration"));
 	}
 
 	/**
 	 * Default time for performing an animated move.  If unset, <code>this.animateTime</code> will be used by default instead
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param animateMoveTime animateMoveTime Default value is null
 	 */
 	public void setAnimateMoveTime(Integer animateMoveTime) {
 		setAttribute("animateMoveTime", animateMoveTime, true);
 	}
 
 	/**
 	 * Default time for performing an animated move.  If unset, <code>this.animateTime</code> will be used by default instead
 	 *
 	 *
 	 * @return Integer
 	 */
 	public Integer getAnimateMoveTime() {
 		return getAttributeAsInt("animateMoveTime");
 	}
 
 	/**
 	 * Default acceleration function for performing an animated move and resize.  If unset, 
 	 * <code>this.animateAcceleration</code> will be used by default instead
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param animateRectAcceleration animateRectAcceleration Default value is null
 	 */
 	public void setAnimateRectAcceleration(AnimationAcceleration animateRectAcceleration) {
 		setAttribute("animateRectAcceleration", animateRectAcceleration.getValue(), true);
 	}
 
 	/**
 	 * Default acceleration function for performing an animated move and resize.  If unset, 
 	 * <code>this.animateAcceleration</code> will be used by default instead
 	 *
 	 *
 	 * @return AnimationAcceleration
 	 */
 	public AnimationAcceleration getAnimateRectAcceleration() {
 		return EnumUtil.getEnum(AnimationAcceleration.values(), getAttribute("animateRectAcceleration"));
 	}
 
 	/**
 	 * Default time for performing an animated setRect.  If unset, <code>this.animateTime</code> will be used by default
 	 * instead
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param animateRectTime animateRectTime Default value is null
 	 */
 	public void setAnimateRectTime(Integer animateRectTime) {
 		setAttribute("animateRectTime", animateRectTime, true);
 	}
 
 	/**
 	 * Default time for performing an animated setRect.  If unset, <code>this.animateTime</code> will be used by default
 	 * instead
 	 *
 	 *
 	 * @return Integer
 	 */
 	public Integer getAnimateRectTime() {
 		return getAttributeAsInt("animateRectTime");
 	}
 
 	/**
 	 * Default acceleration function for performing an animated resize.  If unset,  <code>this.animateAcceleration</code> will
 	 * be used by default instead
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param animateResizeAcceleration animateResizeAcceleration Default value is null
 	 */
 	public void setAnimateResizeAcceleration(AnimationAcceleration animateResizeAcceleration) {
 		setAttribute("animateResizeAcceleration", animateResizeAcceleration.getValue(), true);
 	}
 
 	/**
 	 * Default acceleration function for performing an animated resize.  If unset,  <code>this.animateAcceleration</code> will
 	 * be used by default instead
 	 *
 	 *
 	 * @return AnimationAcceleration
 	 */
 	public AnimationAcceleration getAnimateResizeAcceleration() {
 		return EnumUtil.getEnum(AnimationAcceleration.values(), getAttribute("animateResizeAcceleration"));
 	}
 
 	/**
 	 * Default time for performing an animated resize.  If unset, <code>this.animateTime</code> will be used by default instead
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param animateResizeTime animateResizeTime Default value is null
 	 */
 	public void setAnimateResizeTime(Integer animateResizeTime) {
 		setAttribute("animateResizeTime", animateResizeTime, true);
 	}
 
 	/**
 	 * Default time for performing an animated resize.  If unset, <code>this.animateTime</code> will be used by default instead
 	 *
 	 *
 	 * @return Integer
 	 */
 	public Integer getAnimateResizeTime() {
 		return getAttributeAsInt("animateResizeTime");
 	}
 
 	/**
 	 * Default acceleration function for performing an animated scroll.  If unset,  <code>this.animateAcceleration</code> will
 	 * be used by default instead
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param animateScrollAcceleration animateScrollAcceleration Default value is null
 	 */
 	public void setAnimateScrollAcceleration(AnimationAcceleration animateScrollAcceleration) {
 		setAttribute("animateScrollAcceleration", animateScrollAcceleration.getValue(), true);
 	}
 
 	/**
 	 * Default acceleration function for performing an animated scroll.  If unset,  <code>this.animateAcceleration</code> will
 	 * be used by default instead
 	 *
 	 *
 	 * @return AnimationAcceleration
 	 */
 	public AnimationAcceleration getAnimateScrollAcceleration() {
 		return EnumUtil.getEnum(AnimationAcceleration.values(), getAttribute("animateScrollAcceleration"));
 	}
 
 	/**
 	 * Default time for performing an animated scroll.  If unset, <code>this.animateTime</code> will be used by default instead
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param animateScrollTime animateScrollTime Default value is null
 	 */
 	public void setAnimateScrollTime(Integer animateScrollTime) {
 		setAttribute("animateScrollTime", animateScrollTime, true);
 	}
 
 	/**
 	 * Default time for performing an animated scroll.  If unset, <code>this.animateTime</code> will be used by default instead
 	 *
 	 *
 	 * @return Integer
 	 */
 	public Integer getAnimateScrollTime() {
 		return getAttributeAsInt("animateScrollTime");
 	}
 
 	/**
 	 * Default acceleration function for performing an animated show.  If unset,  <code>this.animateAcceleration</code> will be
 	 * used by default instead
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param animateShowAcceleration animateShowAcceleration Default value is null
 	 */
 	public void setAnimateShowAcceleration(AnimationAcceleration animateShowAcceleration) {
 		setAttribute("animateShowAcceleration", animateShowAcceleration.getValue(), true);
 	}
 
 	/**
 	 * Default acceleration function for performing an animated show.  If unset,  <code>this.animateAcceleration</code> will be
 	 * used by default instead
 	 *
 	 *
 	 * @return AnimationAcceleration
 	 */
 	public AnimationAcceleration getAnimateShowAcceleration() {
 		return EnumUtil.getEnum(AnimationAcceleration.values(), getAttribute("animateShowAcceleration"));
 	}
 
 	/**
 	 * Default time for performing an animated show.  If unset, <code>this.animateTime</code> will be used by default instead
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param animateShowTime animateShowTime Default value is null
 	 */
 	public void setAnimateShowTime(Integer animateShowTime) {
 		setAttribute("animateShowTime", animateShowTime, true);
 	}
 
 	/**
 	 * Default time for performing an animated show.  If unset, <code>this.animateTime</code> will be used by default instead
 	 *
 	 *
 	 * @return Integer
 	 */
 	public Integer getAnimateShowTime() {
 		return getAttributeAsInt("animateShowTime");
 	}
 
 	/**
 	 * Default total duration of animations. Can be overridden by setting animation times for specific animations, or by
 	 * passing a <code>duration</code> parameter into the appropriate animate...() method.
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param animateTime animateTime Default value is 300
 	 * @see <a href="http://www.smartclient.com/smartgwt/showcase/#effects_animation_move" target="examples">Fly Onscreen Example</a>
 	 */
 	public void setAnimateTime(int animateTime) {
 		setAttribute("animateTime", animateTime, true);
 	}
 
 	/**
 	 * Default total duration of animations. Can be overridden by setting animation times for specific animations, or by
 	 * passing a <code>duration</code> parameter into the appropriate animate...() method.
 	 *
 	 *
 	 * @return int
 	 * @see <a href="http://www.smartclient.com/smartgwt/showcase/#effects_animation_move" target="examples">Fly Onscreen Example</a>
 	 */
 	public int getAnimateTime() {
 		return getAttributeAsInt("animateTime");
 	}
 
 	/**
 	 * Default directory for app-specific images, relative to the Page-wide {@link com.smartgwt.client.util.Page#getAppImgDir
 	 * appImgDir}.
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param appImgDir appImgDir Default value is ""
 	 * @see com.smartgwt.client.docs.Images Images overview and related methods
 	 */
 	public void setAppImgDir(String appImgDir) {
 		setAttribute("appImgDir", appImgDir, true);
 	}
 
 	/**
 	 * Default directory for app-specific images, relative to the Page-wide {@link com.smartgwt.client.util.Page#getAppImgDir
 	 * appImgDir}.
 	 *
 	 *
 	 * @return String
 	 * @see com.smartgwt.client.docs.Images Images overview and related methods
 	 */
 	public String getAppImgDir() {
 		return getAttributeAsString("appImgDir");
 	}
 
 	/**
 	 * ARIA role of this component.  Usually does not need to be manually set - see accessibility.
 	 *
 	 * @param ariaRole ariaRole Default value is null
 	 * @throws IllegalStateException this property cannot be changed after the component has been created
 	 */
 	public void setAriaRole(String ariaRole) throws IllegalStateException {
 		setAttribute("ariaRole", ariaRole, false);
 	}
 
 	/**
 	 * ARIA role of this component.  Usually does not need to be manually set - see accessibility.
 	 *
 	 *
 	 * @return String
 	 */
 	public String getAriaRole() {
 		return getAttributeAsString("ariaRole");
 	}
 
 	/**
 	 * If set to true, the widget's parent (if any) will automatically be shown whenever the      widget is shown.
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param autoShowParent autoShowParent Default value is false
 	 * @see com.smartgwt.client.docs.Appearance Appearance overview and related methods
 	 */
 	public void setAutoShowParent(Boolean autoShowParent) {
 		setAttribute("autoShowParent", autoShowParent, true);
 	}
 
 	/**
 	 * If set to true, the widget's parent (if any) will automatically be shown whenever the      widget is shown.
 	 *
 	 *
 	 * @return Boolean
 	 * @see com.smartgwt.client.docs.Appearance Appearance overview and related methods
 	 */
 	public Boolean getAutoShowParent() {
 		return getAttributeAsBoolean("autoShowParent");
 	}
 
 	/**
 	 * The background color for this widget. It corresponds to the CSS background-color attribute. You can set this property to
 	 * an RGB value (e.g. #22AAFF) or a named color (e.g. red) from a list of browser supported color names.
 	 *
 	 * <br><br>If this method is called after the component has been drawn/initialized:
 	 * Sets the background color of this widget to newColor.
 	 *
 	 * @param backgroundColor new color to set the widget's background to. Default value is null
 	 * @see com.smartgwt.client.docs.Appearance Appearance overview and related methods
 	 */
 	public void setBackgroundColor(String backgroundColor) {
 		setAttribute("backgroundColor", backgroundColor, true);
 	}
 
 	/**
 	 * The background color for this widget. It corresponds to the CSS background-color attribute. You can set this property to
 	 * an RGB value (e.g. #22AAFF) or a named color (e.g. red) from a list of browser supported color names.
 	 *
 	 *
 	 * @return String
 	 * @see com.smartgwt.client.docs.Appearance Appearance overview and related methods
 	 */
 	public String getBackgroundColor() {
 		return getAttributeAsString("backgroundColor");
 	}
 
 	/**
 	 * URL for a background image for this widget (corresponding to the CSS "background-image" attribute).
 	 *
 	 * <br><br>If this method is called after the component has been drawn/initialized:
 	 * Sets the background to an image file given by newImage. This URL should be given as a          string relative to the image directory for the page (./images by default).
 	 *
 	 * @param backgroundImage new URL (local to Page image directory) for background image. Default value is null
 	 * @see com.smartgwt.client.docs.Appearance Appearance overview and related methods
 	 */
 	public void setBackgroundImage(String backgroundImage) {
 		setAttribute("backgroundImage", backgroundImage, true);
 	}
 
 	/**
 	 * URL for a background image for this widget (corresponding to the CSS "background-image" attribute).
 	 *
 	 *
 	 * @return String
 	 * @see com.smartgwt.client.docs.Appearance Appearance overview and related methods
 	 */
 	public String getBackgroundImage() {
 		return getAttributeAsString("backgroundImage");
 	}
 
 	/**
 	 * Specifies how the background image should be positioned on the widget.      It corresponds to the CSS
 	 * background-position attribute. If unset,      no background-position attribute is specified if a background image is    
 	 *  specified.
 	 *
 	 * @param backgroundPosition backgroundPosition Default value is null
 	 * @throws IllegalStateException this property cannot be changed after the component has been created
 	 * @see com.smartgwt.client.docs.Appearance Appearance overview and related methods
 	 */
 	public void setBackgroundPosition(String backgroundPosition) throws IllegalStateException {
 		setAttribute("backgroundPosition", backgroundPosition, false);
 	}
 
 	/**
 	 * Specifies how the background image should be positioned on the widget.      It corresponds to the CSS
 	 * background-position attribute. If unset,      no background-position attribute is specified if a background image is    
 	 *  specified.
 	 *
 	 *
 	 * @return String
 	 * @see com.smartgwt.client.docs.Appearance Appearance overview and related methods
 	 */
 	public String getBackgroundPosition() {
 		return getAttributeAsString("backgroundPosition");
 	}
 
 	/**
 	 * Specifies how the background image should be tiled if this widget      is larger than the image. It corresponds to the
 	 * CSS background-repeat attribute.      See BkgndRepeat type for details.
 	 *
 	 * @param backgroundRepeat backgroundRepeat Default value is Canvas.REPEAT
 	 * @throws IllegalStateException this property cannot be changed after the component has been created
 	 * @see com.smartgwt.client.docs.Appearance Appearance overview and related methods
 	 */
 	public void setBackgroundRepeat(BkgndRepeat backgroundRepeat) throws IllegalStateException {
 		setAttribute("backgroundRepeat", backgroundRepeat.getValue(), false);
 	}
 
 	/**
 	 * Specifies how the background image should be tiled if this widget      is larger than the image. It corresponds to the
 	 * CSS background-repeat attribute.      See BkgndRepeat type for details.
 	 *
 	 *
 	 * @return BkgndRepeat
 	 * @see com.smartgwt.client.docs.Appearance Appearance overview and related methods
 	 */
 	public BkgndRepeat getBackgroundRepeat() {
 		return EnumUtil.getEnum(BkgndRepeat.values(), getAttribute("backgroundRepeat"));
 	}
 
 	/**
 	 * Set the CSS border of this component, as a CSS string including border-width, border-style, and/or color (eg "2px solid
 	 * blue"). <P> This property applies the same border to all four sides of this component.  Different per-side borders can
 	 * be set in a CSS style and applied via {@link com.smartgwt.client.widgets.Canvas#getStyleName styleName}.
 	 *
 	 * <br><br>If this method is called after the component has been drawn/initialized:
 	 * Set the CSS border of this component, as a CSS string including border-width, border-style, and/or color (eg "2px solid blue"). <P> This property applies the same border to all four sides of this component.  Different per-side borders can be set in a CSS style and applied via {@link com.smartgwt.client.widgets.Canvas#getStyleName styleName}.
 	 *
 	 * @param border new border to set to (eg: "2px solid black"). Default value is null
 	 * @see com.smartgwt.client.docs.Appearance Appearance overview and related methods
 	 */
 	public void setBorder(String border) {
 		setAttribute("border", border, true);
 	}
 
 	/**
 	 * Set the CSS border of this component, as a CSS string including border-width, border-style, and/or color (eg "2px solid
 	 * blue"). <P> This property applies the same border to all four sides of this component.  Different per-side borders can
 	 * be set in a CSS style and applied via {@link com.smartgwt.client.widgets.Canvas#getStyleName styleName}.
 	 *
 	 *
 	 * @return String
 	 * @see com.smartgwt.client.docs.Appearance Appearance overview and related methods
 	 */
 	public String getBorder() {
 		return getAttributeAsString("border");
 	}
 
 	/**
 	 * Indicates that this object can receive dropped widgets (i.e. other widgets can be      dropped on top of it).
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param canAcceptDrop canAcceptDrop Default value is false
 	 * @see com.smartgwt.client.docs.Dragdrop Dragdrop overview and related methods
 	 * @see <a href="http://www.smartclient.com/smartgwt/showcase/#effects_dd_create" target="examples">Drag create Example</a>
 	 */
 	public void setCanAcceptDrop(Boolean canAcceptDrop) {
 		setAttribute("canAcceptDrop", canAcceptDrop, true);
 	}
 
 	/**
 	 * Indicates that this object can receive dropped widgets (i.e. other widgets can be      dropped on top of it).
 	 *
 	 *
 	 * @return Boolean
 	 * @see com.smartgwt.client.docs.Dragdrop Dragdrop overview and related methods
 	 * @see <a href="http://www.smartclient.com/smartgwt/showcase/#effects_dd_create" target="examples">Drag create Example</a>
 	 */
 	public Boolean getCanAcceptDrop() {
 		return getAttributeAsBoolean("canAcceptDrop");
 	}
 
 	/**
 	 * Indicates whether this widget can initiate custom drag-and-drop operations (other than      reposition or resize).
 	 * Normally canDragReposition or canDragResize would be used      instead of this property.      Note: this property may be
 	 * manipulated by higher-level dragging semantics.
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param canDrag canDrag Default value is false
 	 * @see com.smartgwt.client.docs.Dragdrop Dragdrop overview and related methods
 	 * @see <a href="http://www.smartclient.com/smartgwt/showcase/#effects_dd_create" target="examples">Drag create Example</a>
 	 */
 	public void setCanDrag(Boolean canDrag) {
 		setAttribute("canDrag", canDrag, true);
 	}
 
 	/**
 	 * Indicates whether this widget can initiate custom drag-and-drop operations (other than      reposition or resize).
 	 * Normally canDragReposition or canDragResize would be used      instead of this property.      Note: this property may be
 	 * manipulated by higher-level dragging semantics.
 	 *
 	 *
 	 * @return Boolean
 	 * @see com.smartgwt.client.docs.Dragdrop Dragdrop overview and related methods
 	 * @see <a href="http://www.smartclient.com/smartgwt/showcase/#effects_dd_create" target="examples">Drag create Example</a>
 	 */
 	public Boolean getCanDrag() {
 		return getAttributeAsBoolean("canDrag");
 	}
 
 	/**
 	 * Indicates whether this widget can be moved by a user of your application by simply      dragging with the mouse.
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param canDragReposition canDragReposition Default value is false
 	 * @see com.smartgwt.client.docs.Dragdrop Dragdrop overview and related methods
 	 * @see <a href="http://www.smartclient.com/smartgwt/showcase/#effects_dd_effects" target="examples">Drag effects Example</a>
 	 */
 	public void setCanDragReposition(Boolean canDragReposition) {
 		setAttribute("canDragReposition", canDragReposition, true);
 	}
 
 	/**
 	 * Indicates whether this widget can be moved by a user of your application by simply      dragging with the mouse.
 	 *
 	 *
 	 * @return Boolean
 	 * @see com.smartgwt.client.docs.Dragdrop Dragdrop overview and related methods
 	 * @see <a href="http://www.smartclient.com/smartgwt/showcase/#effects_dd_effects" target="examples">Drag effects Example</a>
 	 */
 	public Boolean getCanDragReposition() {
 		return getAttributeAsBoolean("canDragReposition");
 	}
 
 	/**
 	 * Indicates whether this widget can be resized by dragging on the edges and/or corners of      the widget with the mouse.
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param canDragResize canDragResize Default value is false
 	 * @see com.smartgwt.client.docs.Dragdrop Dragdrop overview and related methods
 	 * @see <a href="http://www.smartclient.com/smartgwt/showcase/#effects_dd_resize" target="examples">Drag resize Example</a>
 	 */
 	public void setCanDragResize(Boolean canDragResize) {
 		setAttribute("canDragResize", canDragResize, true);
 	}
 
 	/**
 	 * Indicates whether this widget can be resized by dragging on the edges and/or corners of      the widget with the mouse.
 	 *
 	 *
 	 * @return Boolean
 	 * @see com.smartgwt.client.docs.Dragdrop Dragdrop overview and related methods
 	 * @see <a href="http://www.smartclient.com/smartgwt/showcase/#effects_dd_resize" target="examples">Drag resize Example</a>
 	 */
 	public Boolean getCanDragResize() {
 		return getAttributeAsBoolean("canDragResize");
 	}
 
 	/**
 	 * If this Canvas is canAcceptDrop:true, when the user drags a droppable widget over      an edge of the widget, should we
 	 * scroll to show the rest of the widget's content?      Returned from canvas.shouldDragScroll().
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param canDragScroll canDragScroll Default value is true
 	 * @see com.smartgwt.client.widgets.Canvas#shouldDragScroll
 	 */
 	public void setCanDragScroll(Boolean canDragScroll) {
 		setAttribute("canDragScroll", canDragScroll, true);
 	}
 
 	/**
 	 * If this Canvas is canAcceptDrop:true, when the user drags a droppable widget over      an edge of the widget, should we
 	 * scroll to show the rest of the widget's content?      Returned from canvas.shouldDragScroll().
 	 *
 	 *
 	 * @return Boolean
 	 * @see com.smartgwt.client.widgets.Canvas#shouldDragScroll
 	 */
 	public Boolean getCanDragScroll() {
 		return getAttributeAsBoolean("canDragScroll");
 	}
 
 	/**
 	 * Indicates that this object can be dropped on top of other widgets. Only valid if       canDrag or canDragReposition is
 	 * true.
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param canDrop canDrop Default value is false
 	 * @see com.smartgwt.client.docs.Dragdrop Dragdrop overview and related methods
 	 * @see <a href="http://www.smartclient.com/smartgwt/showcase/#effects_dd_create" target="examples">Drag create Example</a>
 	 */
 	public void setCanDrop(Boolean canDrop) {
 		setAttribute("canDrop", canDrop, true);
 	}
 
 	/**
 	 * Indicates that this object can be dropped on top of other widgets. Only valid if       canDrag or canDragReposition is
 	 * true.
 	 *
 	 *
 	 * @return Boolean
 	 * @see com.smartgwt.client.docs.Dragdrop Dragdrop overview and related methods
 	 * @see <a href="http://www.smartclient.com/smartgwt/showcase/#effects_dd_create" target="examples">Drag create Example</a>
 	 */
 	public Boolean getCanDrop() {
 		return getAttributeAsBoolean("canDrop");
 	}
 
 	/**
 	 * When explicitly set to false, disallows drop before this member in the Layout.
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param canDropBefore canDropBefore Default value is null
 	 * @see com.smartgwt.client.widgets.layout.Layout
 	 * @see com.smartgwt.client.docs.LayoutMember LayoutMember overview and related methods
 	 */
 	public void setCanDropBefore(Boolean canDropBefore) {
 		setAttribute("canDropBefore", canDropBefore, true);
 	}
 
 	/**
 	 * When explicitly set to false, disallows drop before this member in the Layout.
 	 *
 	 *
 	 * @return Boolean
 	 * @see com.smartgwt.client.widgets.layout.Layout
 	 * @see com.smartgwt.client.docs.LayoutMember LayoutMember overview and related methods
 	 */
 	public Boolean getCanDropBefore() {
 		return getAttributeAsBoolean("canDropBefore");
 	}
 
 	/**
 	 * Can this widget be allowed to become the target of keyboard events? <P> If canFocus is unset (the default), only
 	 * scrollable widgets with visible scrollbars are focusable, to allow for keyboard scrolling. <P> A widget normally
 	 * receives focus by being clicked on or tabbed to.
 	 *
 	 * <br><br>If this method is called after the component has been drawn/initialized:
 	 * Change whether a widget can accept keyboard focus.
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param canFocus whether the widget should now accept focus. Default value is null
 	 * @see com.smartgwt.client.docs.Focus Focus overview and related methods
 	 * @see <a href="http://www.smartclient.com/smartgwt/showcase/#basics_interaction_focus_tabbing" target="examples">Focus & Tabbing Example</a>
 	 */
 	public void setCanFocus(Boolean canFocus) {
 		setAttribute("canFocus", canFocus, true);
 	}
 
 	/**
 	 * Can this widget be allowed to become the target of keyboard events? <P> If canFocus is unset (the default), only
 	 * scrollable widgets with visible scrollbars are focusable, to allow for keyboard scrolling. <P> A widget normally
 	 * receives focus by being clicked on or tabbed to.
 	 *
 	 *
 	 * @return Boolean
 	 * @see com.smartgwt.client.docs.Focus Focus overview and related methods
 	 * @see <a href="http://www.smartclient.com/smartgwt/showcase/#basics_interaction_focus_tabbing" target="examples">Focus & Tabbing Example</a>
 	 */
 	public Boolean getCanFocus() {
 		return getAttributeAsBoolean("canFocus");
 	}
 
 	/**
 	 * Will this Canvas fire hover events when the user hovers over it, or one of its children?
 	 *
 	 * @param canHover canHover Default value is null
 	 * @see com.smartgwt.client.widgets.Canvas#setShowHover
 	 * @see com.smartgwt.client.widgets.events.HoverEvent
 	 */
 	public void setCanHover(Boolean canHover) {
 		setAttribute("canHover", canHover, true);
 	}
 
 	/**
 	 * Will this Canvas fire hover events when the user hovers over it, or one of its children?
 	 *
 	 *
 	 * @return Boolean
 	 * @see com.smartgwt.client.widgets.Canvas#getShowHover
 	 * @see com.smartgwt.client.widgets.events.HoverEvent
 	 */
 	public Boolean getCanHover() {
 		return getAttributeAsBoolean("canHover");
 	}
 
 	/**
 	 * Whether native drag selection of contained text is allowed within this Canvas. <P> Note that setting this property to
 	 * <code>false</code> will not avoid text selection which is initiated outside this Canvas from continuing into this
 	 * Canvas, even if text selection began in another Canvas.
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param canSelectText canSelectText Default value is false
 	 */
 	public void setCanSelectText(Boolean canSelectText) {
 		setAttribute("canSelectText", canSelectText, true);
 	}
 
 	/**
 	 * Whether native drag selection of contained text is allowed within this Canvas. <P> Note that setting this property to
 	 * <code>false</code> will not avoid text selection which is initiated outside this Canvas from continuing into this
 	 * Canvas, even if text selection began in another Canvas.
 	 *
 	 *
 	 * @return Boolean
 	 */
 	public Boolean getCanSelectText() {
 		return getAttributeAsBoolean("canSelectText");
 	}
 
 	/**
 	 * If true, causes this canvas's children to snap to its grid when resizing. This behavior can be overridden on a per-child
 	 * basis by setting the  {@link com.smartgwt.client.widgets.Canvas#getSnapToGrid snapToGrid} or  {@link
 	 * com.smartgwt.client.widgets.Canvas#getSnapResizeToGrid snapResizeToGrid} value on the child.
 	 *
 	 * @param childrenSnapResizeToGrid childrenSnapResizeToGrid Default value is null
 	 * @see com.smartgwt.client.docs.Dragdrop Dragdrop overview and related methods
 	 */
 	public void setChildrenSnapResizeToGrid(Boolean childrenSnapResizeToGrid) {
 		setAttribute("childrenSnapResizeToGrid", childrenSnapResizeToGrid, true);
 	}
 
 	/**
 	 * If true, causes this canvas's children to snap to its grid when resizing. This behavior can be overridden on a per-child
 	 * basis by setting the  {@link com.smartgwt.client.widgets.Canvas#getSnapToGrid snapToGrid} or  {@link
 	 * com.smartgwt.client.widgets.Canvas#getSnapResizeToGrid snapResizeToGrid} value on the child.
 	 *
 	 *
 	 * @return Boolean
 	 * @see com.smartgwt.client.docs.Dragdrop Dragdrop overview and related methods
 	 */
 	public Boolean getChildrenSnapResizeToGrid() {
 		return getAttributeAsBoolean("childrenSnapResizeToGrid");
 	}
 
 	/**
 	 * If true, causes this canvas's children to snap to its grid when dragging. This behavior can be overridden on a per-child
 	 * basis by setting the  {@link com.smartgwt.client.widgets.Canvas#getSnapToGrid snapToGrid} value on the child.
 	 *
 	 * @param childrenSnapToGrid childrenSnapToGrid Default value is null
 	 * @see com.smartgwt.client.docs.Dragdrop Dragdrop overview and related methods
 	 */
 	public void setChildrenSnapToGrid(Boolean childrenSnapToGrid) {
 		setAttribute("childrenSnapToGrid", childrenSnapToGrid, true);
 	}
 
 	/**
 	 * If true, causes this canvas's children to snap to its grid when dragging. This behavior can be overridden on a per-child
 	 * basis by setting the  {@link com.smartgwt.client.widgets.Canvas#getSnapToGrid snapToGrid} value on the child.
 	 *
 	 *
 	 * @return Boolean
 	 * @see com.smartgwt.client.docs.Dragdrop Dragdrop overview and related methods
 	 */
 	public Boolean getChildrenSnapToGrid() {
 		return getAttributeAsBoolean("childrenSnapToGrid");
 	}
 
 	/**
 	 * The contents of a canvas or label widget. Any HTML string is acceptable.
 	 *
 	 * <br><br>If this method is called after the component has been drawn/initialized:
 	 * Changes the contents of a widget to newContents, an HTML string. <P> When {@link com.smartgwt.client.widgets.Canvas#getDynamicContents dynamicContents} is set, <code>setContents()</code> can also be called with no arguments to cause contents to be re-evaluated.
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param contents an HTML string to be set as the contents of this widget. Default value is "&nbsp;"
 	 * @see com.smartgwt.client.widgets.Canvas#setDynamicContents
 	 */
 	public void setContents(String contents) {
 		setAttribute("contents", contents, true);
 	}
 
 	/**
 	 * The contents of a canvas or label widget. Any HTML string is acceptable.
 	 *
 	 *
 	 * @return Returns the contents of a Canvas. The contents are an HTML string.
 	 * @see com.smartgwt.client.widgets.Canvas#getDynamicContents
 	 */
 	public String getContents() {
 		return getAttributeAsString("contents");
 	}
 
 	/**
 	 * Context menu to show for this object, an instance of the Menu widget. <P> Note: if {@link
 	 * com.smartgwt.client.widgets.Canvas#destroy Canvas.destroy} is called on a canvas, any specified context menu is not
 	 * automatically destroyed as well. This is in contrast to {@link com.smartgwt.client.widgets.menu.MenuButton}s which
 	 * automatically destroy their specified {@link com.smartgwt.client.widgets.menu.MenuButton#getMenu menu} by default. The
 	 * behavior is intentional as context menus are commonly reused across components.
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param contextMenu contextMenu Default value is null
 	 * @see com.smartgwt.client.widgets.events.ShowContextMenuEvent
 	 * @see com.smartgwt.client.docs.Cues Cues overview and related methods
 	 * @see <a href="http://www.smartclient.com/smartgwt/showcase/#basics_interaction_contextmenu" target="examples">Context menus Example</a>
 	 */
 	public void setContextMenu(Menu contextMenu) {
 		setAttribute("contextMenu", contextMenu == null ? null : contextMenu.getOrCreateJsObj(), true);
 	}
 
 	/**
 	 * Specifies the cursor image to display when the mouse pointer is      over this widget. It corresponds to the CSS cursor
 	 * attribute. See Cursor type for      different cursors.
 	 *
 	 * <br><br>If this method is called after the component has been drawn/initialized:
 	 * Sets the cursor for this widget to cursor. See the cursor property          for possible values.
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param cursor new cursor. Default value is Canvas.DEFAULT
 	 * @see com.smartgwt.client.docs.Cues Cues overview and related methods
 	 * @see <a href="http://www.smartclient.com/smartgwt/showcase/#effects_dd_create" target="examples">Drag create Example</a>
 	 * @see <a href="http://www.smartclient.com/smartgwt/showcase/#basics_interaction_cursors" target="examples">Cursors Example</a>
 	 */
 	public void setCursor(Cursor cursor) {
 		setAttribute("cursor", cursor.getValue(), true);
 	}
 
 	/**
 	 * Specifies the cursor image to display when the mouse pointer is      over this widget. It corresponds to the CSS cursor
 	 * attribute. See Cursor type for      different cursors.
 	 *
 	 *
 	 * @return Cursor
 	 * @see com.smartgwt.client.docs.Cues Cues overview and related methods
 	 * @see <a href="http://www.smartclient.com/smartgwt/showcase/#effects_dd_create" target="examples">Drag create Example</a>
 	 * @see <a href="http://www.smartclient.com/smartgwt/showcase/#basics_interaction_cursors" target="examples">Cursors Example</a>
 	 */
 	public Cursor getCursor() {
 		return EnumUtil.getEnum(Cursor.values(), getAttribute("cursor"));
 	}
 
 	/**
 	 * A dataPath may be specified on any canvas. This provides a straightforward way to display or
 	 *  edit complex nested data.
 	 *  <P>
 	 * For components which support displaying or editing data values, (such as {@link
 	 * com.smartgwt.client.widgets.form.DynamicForm} or
 	 * {@link com.smartgwt.client.widgets.grid.ListGrid} components), the dataPath may be set to specify how the components
 	 * data is
 	 *  accessed. In this case the dataPath essentially specifies a nested object to edit - typically
 	 *  a path to a field value within a dataSource record. Note that a ValuesManager will be required
 	 *  to handle connecting the dataBoundcomponent to the appropriate sub object. This may be explicitly
 	 *  specified on the component, or a parent of the component, or automatically generated
 	 *  if a DataSource is specified on either the component or a parent thereof.
 	 *  <P>
 	 *  To provide a simple example - if a complex object existed with the following format:
 	 *  <pre>
 	 *  { companyName:"Some Company",
 	 *    address:{    street:"123 Main Street", city:"New York", state:"NY"  }
 	 *  }
 	 *  </pre>
 	 *  a developer could specify a DynamicForm instance with 'dataPath' set to "address" to edit
 	 *  the nested address object:
 	 *  <pre>
 	 *  isc.ValuesManager.create({
 	 *       ID:'vm',
 	 *       values: { companyName:"Some Company",
 	 *               address:{    street:"123 Main Street", city:"New York", state:"NY"  }
 	 *       }
 	 *  });
 	 * 
 	 *  isc.DynamicForm.create({
 	 *       valuesManager:"vm",
 	 *       dataPath:"address",
 	 *       items:[{name:"street"}, {name:"city"}, {name:"state"}]
 	 *  });
 	 *  </pre>
 	 *  If a component is specified with a <code>dataPath</code> attribute but does not have an
 	 *  explicitly specified valuesManager, it will check its parent element chain for a specified
 	 *  valuesManager and automatically bind to that. This simplifies binding multiple components used
 	 *  to view or edit a nested data structure as the valuesManager needs only be defined once at a
 	 *  reasonably high level component. Here's an example of this approach:
 	 *  <pre>
 	 *  isc.ValuesManager.create({
 	 *       ID:'vm',
 	 *       values: { companyName:"Some Company",
 	 *               address:{    street:"123 Main Street", city:"New York", state:"NY"  }
 	 *       }
 	 *  });
 	 * 
 	 *  isc.Layout.create({
 	 *       valuesManager:"vm",
 	 *       members:[
 	 *           isc.DynamicForm.create({
 	 *               dataPath:"/",
 	 *               items:[{name:"companyName"}]
 	 *           }),
 	 *           isc.DynamicForm.create({
 	 *               dataPath:"address",
 	 *               items:[{name:"street"}, {name:"city"}, {name:"state"}]
 	 *           })
 	 *       ]
 	 *  });
 	 *  </pre>
 	 *  Note that in this case the valuesManager is specified on a Layout, which has no 'values'
 	 *  management behavior of its own, but contains items with a specified dataPath which do. In this
 	 *  example you'd see 2 forms allowing editing of the nested data structure.
 	 *  <P>
 	 *  dataPaths from multiple nested components may also be combined. For example:
 	 *  <pre>
 	 *  isc.ValuesManager.create({
 	 *       ID:'vm',
 	 *       values: { companyName:"Some Company",
 	 *               address:{    street:"123 Main Street", city:"New York", state:"NY"  }
 	 *               parentCompany:{
 	 *                   companyName:"Some Corporation",
 	 *                   address:{   street:"1 High Street", city:"New York", state:"NY" }
 	 *               }
 	 *       }
 	 *  });
 	 * 
 	 *  isc.Layout.create({
 	 *       valuesManager:"vm",
 	 *       members:[
 	 *           isc.DynamicForm.create({
 	 *               dataPath:"/",
 	 *               items:[{name:"companyName"}]
 	 *           }),
 	 *           isc.DynamicForm.create({
 	 *               dataPath:"address",
 	 *               items:[{name:"street"}, {name:"city"}, {name:"state"}]
 	 *           }),
 	 *           isc.Layout.create({
 	 *               dataPath:"parentCompany",
 	 *               members:[
 	 *                   isc.DynamicForm.create({
 	 *                       dataPath:"/",
 	 *                       items:[{name:"companyName", type:"staticText"}]
 	 *                   }),
 	 *                   isc.DetailViewer.create({
 	 *                       dataPath:"address",
 	 *                       fields:[{name:"street", name:"city", name:"state"}]
 	 *                   })
 	 *               ]
 	 *           })
 	 *       ]
 	 *  });
 	 *  </pre>
 	 *  In this example the detailViewer will display data from the <code>parentCompany.address</code>
 	 *  object within the base record.
 	 *  <P>
 	 *  Note that if a component has a specified  dataSource and shows child components with a
 	 *  specified dataPath, there is no need to explicitly declare a valuesManager at all. If a component
 	 *  with a dataPath has a dataSource, or an ancestor with a dataSource specified, it will, a
 	 *  valuesManager will automatically be generated on the higher level component (and be available as
 	 *  <code>component.valuesManager</code>).
 	 *
 	 * <br><br>If this method is called after the component has been drawn/initialized:
 	 * Setter for the {@link com.smartgwt.client.widgets.Canvas#getDataPath dataPath} attribute. This method may be called directly at runtime to set the dataPath on a component, and will also be re-run automatically whenever a canvas' parentElement changes due to a call to addChild(). This method handles automatically binding the component to the appropriate valuesManager if necessary.
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param dataPath new dataPath. Default value is null
 	 */
 	public void setDataPath(String dataPath) {
 		setAttribute("dataPath", dataPath, true);
 	}
 
 	/**
 	 * A dataPath may be specified on any canvas. This provides a straightforward way to display or
 	 *  edit complex nested data.
 	 *  <P>
 	 * For components which support displaying or editing data values, (such as {@link
 	 * com.smartgwt.client.widgets.form.DynamicForm} or
 	 * {@link com.smartgwt.client.widgets.grid.ListGrid} components), the dataPath may be set to specify how the components
 	 * data is
 	 *  accessed. In this case the dataPath essentially specifies a nested object to edit - typically
 	 *  a path to a field value within a dataSource record. Note that a ValuesManager will be required
 	 *  to handle connecting the dataBoundcomponent to the appropriate sub object. This may be explicitly
 	 *  specified on the component, or a parent of the component, or automatically generated
 	 *  if a DataSource is specified on either the component or a parent thereof.
 	 *  <P>
 	 *  To provide a simple example - if a complex object existed with the following format:
 	 *  <pre>
 	 *  { companyName:"Some Company",
 	 *    address:{    street:"123 Main Street", city:"New York", state:"NY"  }
 	 *  }
 	 *  </pre>
 	 *  a developer could specify a DynamicForm instance with 'dataPath' set to "address" to edit
 	 *  the nested address object:
 	 *  <pre>
 	 *  isc.ValuesManager.create({
 	 *       ID:'vm',
 	 *       values: { companyName:"Some Company",
 	 *               address:{    street:"123 Main Street", city:"New York", state:"NY"  }
 	 *       }
 	 *  });
 	 * 
 	 *  isc.DynamicForm.create({
 	 *       valuesManager:"vm",
 	 *       dataPath:"address",
 	 *       items:[{name:"street"}, {name:"city"}, {name:"state"}]
 	 *  });
 	 *  </pre>
 	 *  If a component is specified with a <code>dataPath</code> attribute but does not have an
 	 *  explicitly specified valuesManager, it will check its parent element chain for a specified
 	 *  valuesManager and automatically bind to that. This simplifies binding multiple components used
 	 *  to view or edit a nested data structure as the valuesManager needs only be defined once at a
 	 *  reasonably high level component. Here's an example of this approach:
 	 *  <pre>
 	 *  isc.ValuesManager.create({
 	 *       ID:'vm',
 	 *       values: { companyName:"Some Company",
 	 *               address:{    street:"123 Main Street", city:"New York", state:"NY"  }
 	 *       }
 	 *  });
 	 * 
 	 *  isc.Layout.create({
 	 *       valuesManager:"vm",
 	 *       members:[
 	 *           isc.DynamicForm.create({
 	 *               dataPath:"/",
 	 *               items:[{name:"companyName"}]
 	 *           }),
 	 *           isc.DynamicForm.create({
 	 *               dataPath:"address",
 	 *               items:[{name:"street"}, {name:"city"}, {name:"state"}]
 	 *           })
 	 *       ]
 	 *  });
 	 *  </pre>
 	 *  Note that in this case the valuesManager is specified on a Layout, which has no 'values'
 	 *  management behavior of its own, but contains items with a specified dataPath which do. In this
 	 *  example you'd see 2 forms allowing editing of the nested data structure.
 	 *  <P>
 	 *  dataPaths from multiple nested components may also be combined. For example:
 	 *  <pre>
 	 *  isc.ValuesManager.create({
 	 *       ID:'vm',
 	 *       values: { companyName:"Some Company",
 	 *               address:{    street:"123 Main Street", city:"New York", state:"NY"  }
 	 *               parentCompany:{
 	 *                   companyName:"Some Corporation",
 	 *                   address:{   street:"1 High Street", city:"New York", state:"NY" }
 	 *               }
 	 *       }
 	 *  });
 	 * 
 	 *  isc.Layout.create({
 	 *       valuesManager:"vm",
 	 *       members:[
 	 *           isc.DynamicForm.create({
 	 *               dataPath:"/",
 	 *               items:[{name:"companyName"}]
 	 *           }),
 	 *           isc.DynamicForm.create({
 	 *               dataPath:"address",
 	 *               items:[{name:"street"}, {name:"city"}, {name:"state"}]
 	 *           }),
 	 *           isc.Layout.create({
 	 *               dataPath:"parentCompany",
 	 *               members:[
 	 *                   isc.DynamicForm.create({
 	 *                       dataPath:"/",
 	 *                       items:[{name:"companyName", type:"staticText"}]
 	 *                   }),
 	 *                   isc.DetailViewer.create({
 	 *                       dataPath:"address",
 	 *                       fields:[{name:"street", name:"city", name:"state"}]
 	 *                   })
 	 *               ]
 	 *           })
 	 *       ]
 	 *  });
 	 *  </pre>
 	 *  In this example the detailViewer will display data from the <code>parentCompany.address</code>
 	 *  object within the base record.
 	 *  <P>
 	 *  Note that if a component has a specified  dataSource and shows child components with a
 	 *  specified dataPath, there is no need to explicitly declare a valuesManager at all. If a component
 	 *  with a dataPath has a dataSource, or an ancestor with a dataSource specified, it will, a
 	 *  valuesManager will automatically be generated on the higher level component (and be available as
 	 *  <code>component.valuesManager</code>).
 	 *
 	 *
 	 * @return String
 	 */
 	public String getDataPath() {
 		return getAttributeAsString("dataPath");
 	}
 
 	/**
 	 * For custom components, establishes a default height for the component. <P> For a component that should potentially be
 	 * sized automatically by a Layout, set this property rather than {@link com.smartgwt.client.widgets.Canvas#getHeight
 	 * height} directly, because Layouts regard a height setting as an explicit size that shouldn't be changed.
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param defaultHeight defaultHeight Default value is 100
 	 * @see com.smartgwt.client.docs.Sizing Sizing overview and related methods
 	 */
 	public void setDefaultHeight(int defaultHeight) {
 		setAttribute("defaultHeight", defaultHeight, true);
 	}
 
 	/**
 	 * For custom components, establishes a default height for the component. <P> For a component that should potentially be
 	 * sized automatically by a Layout, set this property rather than {@link com.smartgwt.client.widgets.Canvas#getHeight
 	 * height} directly, because Layouts regard a height setting as an explicit size that shouldn't be changed.
 	 *
 	 *
 	 * @return int
 	 * @see com.smartgwt.client.docs.Sizing Sizing overview and related methods
 	 */
 	public int getDefaultHeight() {
 		return getAttributeAsInt("defaultHeight");
 	}
 
 	/**
 	 * For custom components, establishes a default width for the component. <P> For a component that should potentially be
 	 * sized automatically by a Layout, set this property rather than {@link com.smartgwt.client.widgets.Canvas#getWidth width}
 	 * directly, because Layouts regard a width setting as an explicit size that shouldn't be changed.
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param defaultWidth defaultWidth Default value is 100
 	 * @see com.smartgwt.client.docs.Sizing Sizing overview and related methods
 	 */
 	public void setDefaultWidth(int defaultWidth) {
 		setAttribute("defaultWidth", defaultWidth, true);
 	}
 
 	/**
 	 * For custom components, establishes a default width for the component. <P> For a component that should potentially be
 	 * sized automatically by a Layout, set this property rather than {@link com.smartgwt.client.widgets.Canvas#getWidth width}
 	 * directly, because Layouts regard a width setting as an explicit size that shouldn't be changed.
 	 *
 	 *
 	 * @return int
 	 * @see com.smartgwt.client.docs.Sizing Sizing overview and related methods
 	 */
 	public int getDefaultWidth() {
 		return getAttributeAsInt("defaultWidth");
 	}
 
 	/**
 	 * If this property is set to <code>true</code>, the {@link com.smartgwt.client.widgets.Canvas#destroy destroy()} method
 	 * has been called on this canvas. This implies the canvas is no longer valid. Its ID has been removed from global scope,
 	 * and calling standard canvas APIs on it is likely to result in  errors.
 	 *
 	 * <b>Note :</b> This method should be called only after the widget has been rendered.
 	 *
 	 * @return Boolean
 	 * @throws IllegalStateException if widget has not yet been rendered.
 	 * @see com.smartgwt.client.widgets.Canvas#destroy
 	 */
 	public Boolean getDestroyed() throws IllegalStateException {
 		errorIfNotCreated("destroyed");
 		return getAttributeAsBoolean("destroyed");
 	}
 
 	/**
 	 * This property is set to true when the {@link com.smartgwt.client.widgets.Canvas#destroy Canvas.destroy} method is called
 	 * on a widget. If this property is true, but {@link com.smartgwt.client.widgets.Canvas#getDestroyed destroyed} is not,
 	 * this indicates the canvas is in the process of being destroyed.
 	 *
 	 * <b>Note :</b> This method should be called only after the widget has been rendered.
 	 *
 	 * @return Boolean
 	 * @throws IllegalStateException if widget has not yet been rendered.
 	 * @see com.smartgwt.client.widgets.Canvas#destroy
 	 */
 	public Boolean getDestroying() throws IllegalStateException {
 		errorIfNotCreated("destroying");
 		return getAttributeAsBoolean("destroying");
 	}
 
 	/**
 	 * Specifies the cursor image to display when the mouse pointer is      over this widget if this widget is disabled. It
 	 * corresponds to the CSS cursor       attribute. See Cursor type for different cursors.
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param disabledCursor disabledCursor Default value is Canvas.DEFAULT
 	 * @see com.smartgwt.client.docs.Cues Cues overview and related methods
 	 */
 	public void setDisabledCursor(Cursor disabledCursor) {
 		setAttribute("disabledCursor", disabledCursor.getValue(), true);
 	}
 
 	/**
 	 * Specifies the cursor image to display when the mouse pointer is      over this widget if this widget is disabled. It
 	 * corresponds to the CSS cursor       attribute. See Cursor type for different cursors.
 	 *
 	 *
 	 * @return Cursor
 	 * @see com.smartgwt.client.docs.Cues Cues overview and related methods
 	 */
 	public Cursor getDisabledCursor() {
 		return EnumUtil.getEnum(Cursor.values(), getAttribute("disabledCursor"));
 	}
 
 	/**
 	 * Amount of time (in msec) between which two clicks are considered a single click
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param doubleClickDelay doubleClickDelay Default value is 250
 	 */
 	public void setDoubleClickDelay(int doubleClickDelay) {
 		setAttribute("doubleClickDelay", doubleClickDelay, true);
 	}
 
 	/**
 	 * Amount of time (in msec) between which two clicks are considered a single click
 	 *
 	 *
 	 * @return int
 	 */
 	public int getDoubleClickDelay() {
 		return getAttributeAsInt("doubleClickDelay");
 	}
 
 	/**
 	 * Visual appearance to show when the object is being dragged.
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param dragAppearance dragAppearance Default value is EventHandler.OUTLINE
 	 * @see com.smartgwt.client.docs.Dragdrop Dragdrop overview and related methods
 	 */
 	public void setDragAppearance(DragAppearance dragAppearance) {
 		setAttribute("dragAppearance", dragAppearance.getValue(), true);
 	}
 
 	/**
 	 * Visual appearance to show when the object is being dragged.
 	 *
 	 *
 	 * @return DragAppearance
 	 * @see com.smartgwt.client.docs.Dragdrop Dragdrop overview and related methods
 	 */
 	public DragAppearance getDragAppearance() {
 		return EnumUtil.getEnum(DragAppearance.values(), getAttribute("dragAppearance"));
 	}
 
 	/**
 	 * This indicates how the system will test for droppable targets: either by intersection with the mouse or intersection
 	 * with the rectangle of the dragMoveTarget.
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param dragIntersectStyle dragIntersectStyle Default value is "mouse"
 	 * @see com.smartgwt.client.docs.Dragdrop Dragdrop overview and related methods
 	 */
 	public void setDragIntersectStyle(DragIntersectStyle dragIntersectStyle) {
 		setAttribute("dragIntersectStyle", dragIntersectStyle.getValue(), true);
 	}
 
 	/**
 	 * This indicates how the system will test for droppable targets: either by intersection with the mouse or intersection
 	 * with the rectangle of the dragMoveTarget.
 	 *
 	 *
 	 * @return DragIntersectStyle
 	 * @see com.smartgwt.client.docs.Dragdrop Dragdrop overview and related methods
 	 */
 	public DragIntersectStyle getDragIntersectStyle() {
 		return EnumUtil.getEnum(DragIntersectStyle.values(), getAttribute("dragIntersectStyle"));
 	}
 
 	/**
 	 * If this widget has dragAppearance <code>"target"</code>, this value specifies the opacity to render the target while it
 	 * is being dragged. A null value implies we do not modify the opacity.
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param dragOpacity dragOpacity Default value is null
 	 * @see com.smartgwt.client.docs.Dragdrop Dragdrop overview and related methods
 	 * @see <a href="http://www.smartclient.com/smartgwt/showcase/#effects_dd_effects" target="examples">Drag effects Example</a>
 	 */
 	public void setDragOpacity(Integer dragOpacity) {
 		setAttribute("dragOpacity", dragOpacity, true);
 	}
 
 	/**
 	 * If this widget has dragAppearance <code>"target"</code>, this value specifies the opacity to render the target while it
 	 * is being dragged. A null value implies we do not modify the opacity.
 	 *
 	 *
 	 * @return Integer
 	 * @see com.smartgwt.client.docs.Dragdrop Dragdrop overview and related methods
 	 * @see <a href="http://www.smartclient.com/smartgwt/showcase/#effects_dd_effects" target="examples">Drag effects Example</a>
 	 */
 	public Integer getDragOpacity() {
 		return getAttributeAsInt("dragOpacity");
 	}
 
 	/**
 	 * Cursor to switch to if the mouse is over a widget that is drag repositionable.
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param dragRepositionCursor dragRepositionCursor Default value is Canvas.MOVE
 	 * @see com.smartgwt.client.docs.Dragdrop Dragdrop overview and related methods
 	 */
 	public void setDragRepositionCursor(Cursor dragRepositionCursor) {
 		setAttribute("dragRepositionCursor", dragRepositionCursor.getValue(), true);
 	}
 
 	/**
 	 * Cursor to switch to if the mouse is over a widget that is drag repositionable.
 	 *
 	 *
 	 * @return Cursor
 	 * @see com.smartgwt.client.docs.Dragdrop Dragdrop overview and related methods
 	 */
 	public Cursor getDragRepositionCursor() {
 		return EnumUtil.getEnum(Cursor.values(), getAttribute("dragRepositionCursor"));
 	}
 
 	/**
 	 * If this widget supports drag-scrolling, This property specifies how many ms the      user must hover over the
 	 * drag-scroll threshold before scrolling begins.
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param dragScrollDelay dragScrollDelay Default value is 100
 	 */
 	public void setDragScrollDelay(int dragScrollDelay) {
 		setAttribute("dragScrollDelay", dragScrollDelay, true);
 	}
 
 	/**
 	 * If this widget supports drag-scrolling, This property specifies how many ms the      user must hover over the
 	 * drag-scroll threshold before scrolling begins.
 	 *
 	 *
 	 * @return int
 	 */
 	public int getDragScrollDelay() {
 		return getAttributeAsInt("dragScrollDelay");
 	}
 
 	/**
 	 * Number of pixels the cursor needs to move before the EventHandler starts a drag operation.
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param dragStartDistance dragStartDistance Default value is 5
 	 * @see com.smartgwt.client.docs.Dragdrop Dragdrop overview and related methods
 	 */
 	public void setDragStartDistance(int dragStartDistance) {
 		setAttribute("dragStartDistance", dragStartDistance, true);
 	}
 
 	/**
 	 * Number of pixels the cursor needs to move before the EventHandler starts a drag operation.
 	 *
 	 *
 	 * @return int
 	 * @see com.smartgwt.client.docs.Dragdrop Dragdrop overview and related methods
 	 */
 	public int getDragStartDistance() {
 		return getAttributeAsInt("dragStartDistance");
 	}
 
 	/**
 	 * The "type" of thing given as a string that can be dragged from this widget. If      specified, this will be matched up
 	 * with the dropTypes of droppable widgets as detailed      in the dropTypes property.
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param dragType dragType Default value is null
 	 * @see com.smartgwt.client.widgets.Canvas#setDropTypes
 	 * @see com.smartgwt.client.docs.Dragdrop Dragdrop overview and related methods
 	 */
 	public void setDragType(String dragType) {
 		setAttribute("dragType", dragType, true);
 	}
 
 	/**
 	 * The "type" of thing given as a string that can be dragged from this widget. If      specified, this will be matched up
 	 * with the dropTypes of droppable widgets as detailed      in the dropTypes property.
 	 *
 	 *
 	 * @return String
 	 * @see com.smartgwt.client.widgets.Canvas#getDropTypes
 	 * @see com.smartgwt.client.docs.Dragdrop Dragdrop overview and related methods
 	 */
 	public String getDragType() {
 		return getAttributeAsString("dragType");
 	}
 
 	/**
 	 * Dynamic contents allows the contents string to be treated as a simple, but powerful
 	 *  template.  When this attribute is set to true, expressions of the form \${arbitrary JS
 	 *  here} are replaced by the result of the evaluation of the JS code inside the curly
 	 *  brackets.  This evaluation happens at draw time.  If you want to trigger a re-evaluation
 	 *  of the expressions in the contents string you can call markForRedraw() on the canvas.
 	 *  <p>
 	 *  You can use this feature to build some simple custom components. For example, let's say
 	 *  you want to show the value of a Slider in a Canvas somewhere on the screen.  You can do
 	 *  this by observing the valueChanged() method on the slider and calling setContents() on
 	 *  your canvas with the new string or you can set the contents of the canvas to something
 	 *  like:
 	 *  <p><code>
 	 *  "The slider value is \${sliderInstance.getValue()}."
 	 *  </code><p>
 	 *  Next you set dynamicContents: true on the canvas, observe valueChanged() on the slider
 	 *  and call canvas.markForRedraw() in that observation.  This approach is cleaner than
 	 *  setContents() when the Canvas is aggregating several values or dynamic expressions.
 	 *  Like so:
 	 *  <p>
 	 *  <pre>
 	 *  Slider.create({
 	 *      ID: "mySlider"
 	 *  });
 	 * 
 	 *  Canvas.create({
 	 *      ID: "myCanvas",
 	 *      dynamicContents: true,
 	 *      contents: "The slider value is \${mySlider.getValue()}."
 	 *  });
 	 *      
 	 *  myCanvas.observe(mySlider, "valueChanged", 
 	 *                   "observer.markForRedraw()");
 	 *  </pre>
 	 *  You can embed an arbitrary number of dynamic expressions in the contents string.  The
 	 *  search and replace is optimized for speed.
 	 *  <p>
 	 *  If an error occurs during the evaluation of one of the expressions, a warning is logged
 	 *  to the ISC Developer Console and the error string is embedded in place of the expected
 	 *  value in the Canvas.
 	 *  <p>
 	 *  The value of a function is its return value.  The value of any variable is the same as
 	 *  that returned by its toString() representation.
 	 *  <p>
 	 *  Inside the evaluation contentext, <code>this</code> points to the canvas instance that
 	 *  has the dynamicContents string as its contents - in other words the canvas instance on
 	 *  which the template is declared.
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param dynamicContents dynamicContents Default value is false
 	 * @see com.smartgwt.client.widgets.Canvas#setContents
 	 * @see com.smartgwt.client.widgets.Canvas#setDynamicContentsVars
 	 * 
 	 */
 	public void setDynamicContents(Boolean dynamicContents) {
 		setAttribute("dynamicContents", dynamicContents, true);
 	}
 
 	/**
 	 * Dynamic contents allows the contents string to be treated as a simple, but powerful
 	 *  template.  When this attribute is set to true, expressions of the form \${arbitrary JS
 	 *  here} are replaced by the result of the evaluation of the JS code inside the curly
 	 *  brackets.  This evaluation happens at draw time.  If you want to trigger a re-evaluation
 	 *  of the expressions in the contents string you can call markForRedraw() on the canvas.
 	 *  <p>
 	 *  You can use this feature to build some simple custom components. For example, let's say
 	 *  you want to show the value of a Slider in a Canvas somewhere on the screen.  You can do
 	 *  this by observing the valueChanged() method on the slider and calling setContents() on
 	 *  your canvas with the new string or you can set the contents of the canvas to something
 	 *  like:
 	 *  <p><code>
 	 *  "The slider value is \${sliderInstance.getValue()}."
 	 *  </code><p>
 	 *  Next you set dynamicContents: true on the canvas, observe valueChanged() on the slider
 	 *  and call canvas.markForRedraw() in that observation.  This approach is cleaner than
 	 *  setContents() when the Canvas is aggregating several values or dynamic expressions.
 	 *  Like so:
 	 *  <p>
 	 *  <pre>
 	 *  Slider.create({
 	 *      ID: "mySlider"
 	 *  });
 	 * 
 	 *  Canvas.create({
 	 *      ID: "myCanvas",
 	 *      dynamicContents: true,
 	 *      contents: "The slider value is \${mySlider.getValue()}."
 	 *  });
 	 *      
 	 *  myCanvas.observe(mySlider, "valueChanged", 
 	 *                   "observer.markForRedraw()");
 	 *  </pre>
 	 *  You can embed an arbitrary number of dynamic expressions in the contents string.  The
 	 *  search and replace is optimized for speed.
 	 *  <p>
 	 *  If an error occurs during the evaluation of one of the expressions, a warning is logged
 	 *  to the ISC Developer Console and the error string is embedded in place of the expected
 	 *  value in the Canvas.
 	 *  <p>
 	 *  The value of a function is its return value.  The value of any variable is the same as
 	 *  that returned by its toString() representation.
 	 *  <p>
 	 *  Inside the evaluation contentext, <code>this</code> points to the canvas instance that
 	 *  has the dynamicContents string as its contents - in other words the canvas instance on
 	 *  which the template is declared.
 	 *
 	 *
 	 * @return Boolean
 	 * @see com.smartgwt.client.widgets.Canvas#getContents
 	 * @see com.smartgwt.client.widgets.Canvas#getDynamicContentsVars
 	 * 
 	 */
 	public Boolean getDynamicContents() {
 		return getAttributeAsBoolean("dynamicContents");
 	}
 
 	/**
 	 * Background color for the EdgedCanvas created to decorate this component.  This can be used to provide an underlying
 	 * "tint" color for translucent edge media
 	 *
 	 * @param edgeBackgroundColor edgeBackgroundColor Default value is null
 	 * @throws IllegalStateException this property cannot be changed after the component has been created
 	 */
 	public void setEdgeBackgroundColor(String edgeBackgroundColor) throws IllegalStateException {
 		setAttribute("edgeBackgroundColor", edgeBackgroundColor, false);
 	}
 
 	/**
 	 * Background color for the EdgedCanvas created to decorate this component.  This can be used to provide an underlying
 	 * "tint" color for translucent edge media
 	 *
 	 *
 	 * @return String
 	 */
 	public String getEdgeBackgroundColor() {
 		return getAttributeAsString("edgeBackgroundColor");
 	}
 
 	/**
 	 * Background color for the center section only.  Can be used as a surrogate background color for the decorated Canvas, if
 	 * the Canvas is set to partially overlap the edges and hence can't show a background color itself without occluding media.
 	 *
 	 * @param edgeCenterBackgroundColor edgeCenterBackgroundColor Default value is null
 	 * @throws IllegalStateException this property cannot be changed after the component has been created
 	 */
 	public void setEdgeCenterBackgroundColor(String edgeCenterBackgroundColor) throws IllegalStateException {
 		setAttribute("edgeCenterBackgroundColor", edgeCenterBackgroundColor, false);
 	}
 
 	/**
 	 * Background color for the center section only.  Can be used as a surrogate background color for the decorated Canvas, if
 	 * the Canvas is set to partially overlap the edges and hence can't show a background color itself without occluding media.
 	 *
 	 *
 	 * @return String
 	 */
 	public String getEdgeCenterBackgroundColor() {
 		return getAttributeAsString("edgeCenterBackgroundColor");
 	}
 
 	/**
 	 * Base name of images for edges.  Extensions for each corner or edge piece will be added to this image URL, before the
 	 * extension.  For example, with the default base name of "edge.gif", the top-left corner image will be "edge_TL.gif". <P>
 	 * The full list of extensions is: "_TL", "_TR", "_BL", "_BR", "_T", "_L", "_B", "_R", "_center".
 	 *
 	 * @param edgeImage edgeImage Default value is "[SKIN]edge.gif"
 	 * @throws IllegalStateException this property cannot be changed after the component has been created
 	 * @see <a href="http://www.smartclient.com/smartgwt/showcase/#effects_lf_edges" target="examples">Edges Example</a>
 	 */
 	public void setEdgeImage(String edgeImage) throws IllegalStateException {
 		setAttribute("edgeImage", edgeImage, false);
 	}
 
 	/**
 	 * Base name of images for edges.  Extensions for each corner or edge piece will be added to this image URL, before the
 	 * extension.  For example, with the default base name of "edge.gif", the top-left corner image will be "edge_TL.gif". <P>
 	 * The full list of extensions is: "_TL", "_TR", "_BL", "_BR", "_T", "_L", "_B", "_R", "_center".
 	 *
 	 *
 	 * @return String
 	 * @see <a href="http://www.smartclient.com/smartgwt/showcase/#effects_lf_edges" target="examples">Edges Example</a>
 	 */
 	public String getEdgeImage() {
 		return getAttributeAsString("edgeImage");
 	}
 
 	/**
 	 * How far into the edge of an object do we consider the "edge" for drag resize purposes?
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param edgeMarginSize edgeMarginSize Default value is 5
 	 * @see com.smartgwt.client.docs.Dragdrop Dragdrop overview and related methods
 	 * @see <a href="http://www.smartclient.com/smartgwt/showcase/#effects_dd_resize" target="examples">Drag resize Example</a>
 	 */
 	public void setEdgeMarginSize(int edgeMarginSize) {
 		setAttribute("edgeMarginSize", edgeMarginSize, true);
 	}
 
 	/**
 	 * How far into the edge of an object do we consider the "edge" for drag resize purposes?
 	 *
 	 *
 	 * @return int
 	 * @see com.smartgwt.client.docs.Dragdrop Dragdrop overview and related methods
 	 * @see <a href="http://www.smartclient.com/smartgwt/showcase/#effects_dd_resize" target="examples">Drag resize Example</a>
 	 */
 	public int getEdgeMarginSize() {
 		return getAttributeAsInt("edgeMarginSize");
 	}
 
 	/**
 	 * Amount the contained Canvas should be offset.  Defaults to edgeSize; set to less than edgeSize to allow the contained
 	 * Canvas to overlap the edge and corner media.
 	 *
 	 * @param edgeOffset edgeOffset Default value is null
 	 * @throws IllegalStateException this property cannot be changed after the component has been created
 	 * @see <a href="http://www.smartclient.com/smartgwt/showcase/#effects_lf_corners" target="examples">Corners Example</a>
 	 */
 	public void setEdgeOffset(Integer edgeOffset) throws IllegalStateException {
 		setAttribute("edgeOffset", edgeOffset, false);
 	}
 
 	/**
 	 * Amount the contained Canvas should be offset.  Defaults to edgeSize; set to less than edgeSize to allow the contained
 	 * Canvas to overlap the edge and corner media.
 	 *
 	 *
 	 * @return Integer
 	 * @see <a href="http://www.smartclient.com/smartgwt/showcase/#effects_lf_corners" target="examples">Corners Example</a>
 	 */
 	public Integer getEdgeOffset() {
 		return getAttributeAsInt("edgeOffset");
 	}
 
 	/**
 	 * Opacity of the edges.  Defaults to matching this.opacity. if {@link com.smartgwt.client.widgets.Canvas#setOpacity
 	 * Canvas.setOpacity} is called on a Canvas where edgeOpacity is set, edgeOpacity will be considered a percentage of the
 	 * parent's opacity (so 50% opaque parent plus edgeOpacity 50 means 25% opaque edges)
 	 *
 	 * <br><br>If this method is called after the component has been drawn/initialized:
 	 * Set the {@link com.smartgwt.client.widgets.Canvas#getEdgeOpacity edgeOpacity} and mark the canvas for redraw
 	 *
 	 * @param edgeOpacity new edge-opacity level. Default value is null
 	 * @throws IllegalStateException this property cannot be changed after the component has been created
 	 * @see <a href="http://www.smartclient.com/smartgwt/showcase/#effects_lf_edges" target="examples">Edges Example</a>
 	 */
 	public void setEdgeOpacity(Integer edgeOpacity) throws IllegalStateException {
 		setAttribute("edgeOpacity", edgeOpacity, false);
 	}
 
 	/**
 	 * Opacity of the edges.  Defaults to matching this.opacity. if {@link com.smartgwt.client.widgets.Canvas#setOpacity
 	 * Canvas.setOpacity} is called on a Canvas where edgeOpacity is set, edgeOpacity will be considered a percentage of the
 	 * parent's opacity (so 50% opaque parent plus edgeOpacity 50 means 25% opaque edges)
 	 *
 	 *
 	 * @return Integer
 	 * @see <a href="http://www.smartclient.com/smartgwt/showcase/#effects_lf_edges" target="examples">Edges Example</a>
 	 */
 	public Integer getEdgeOpacity() {
 		return getAttributeAsInt("edgeOpacity");
 	}
 
 	/**
 	 * Whether to show media in the center section, that is, behind the decorated Canvas.
 	 *
 	 * @param edgeShowCenter edgeShowCenter Default value is false
 	 * @throws IllegalStateException this property cannot be changed after the component has been created
 	 * @see <a href="http://www.smartclient.com/smartgwt/showcase/#effects_lf_corners" target="examples">Corners Example</a>
 	 */
 	public void setEdgeShowCenter(Boolean edgeShowCenter) throws IllegalStateException {
 		setAttribute("edgeShowCenter", edgeShowCenter, false);
 	}
 
 	/**
 	 * Whether to show media in the center section, that is, behind the decorated Canvas.
 	 *
 	 *
 	 * @return Boolean
 	 * @see <a href="http://www.smartclient.com/smartgwt/showcase/#effects_lf_corners" target="examples">Corners Example</a>
 	 */
 	public Boolean getEdgeShowCenter() {
 		return getAttributeAsBoolean("edgeShowCenter");
 	}
 
 	/**
 	 * Size in pixels for corners and edges
 	 *
 	 * @param edgeSize edgeSize Default value is 10
 	 * @throws IllegalStateException this property cannot be changed after the component has been created
 	 * @see <a href="http://www.smartclient.com/smartgwt/showcase/#effects_lf_edges" target="examples">Edges Example</a>
 	 */
 	public void setEdgeSize(int edgeSize) throws IllegalStateException {
 		setAttribute("edgeSize", edgeSize, false);
 	}
 
 	/**
 	 * Size in pixels for corners and edges
 	 *
 	 *
 	 * @return int
 	 * @see <a href="http://www.smartclient.com/smartgwt/showcase/#effects_lf_edges" target="examples">Edges Example</a>
 	 */
 	public int getEdgeSize() {
 		return getAttributeAsInt("edgeSize");
 	}
 
 	/**
 	 * When this Canvas is included as a member in a Layout, extra blank space that should be left after this member in a
 	 * Layout.
 	 *
 	 * @param extraSpace extraSpace Default value is 0
 	 * @throws IllegalStateException this property cannot be changed after the component has been created
 	 * @see com.smartgwt.client.widgets.layout.LayoutSpacer
 	 * @see com.smartgwt.client.docs.LayoutMember LayoutMember overview and related methods
 	 */
 	public void setExtraSpace(int extraSpace) throws IllegalStateException {
 		setAttribute("extraSpace", extraSpace, false);
 	}
 
 	/**
 	 * When this Canvas is included as a member in a Layout, extra blank space that should be left after this member in a
 	 * Layout.
 	 *
 	 *
 	 * @return int
 	 * @see com.smartgwt.client.widgets.layout.LayoutSpacer
 	 * @see com.smartgwt.client.docs.LayoutMember LayoutMember overview and related methods
 	 */
 	public int getExtraSpace() {
 		return getAttributeAsInt("extraSpace");
 	}
 
 	/**
 	 * If <code>this.showHover</code> is true, this property can be used to customize the alignment of content in the hover
 	 * canvas.
 	 *
 	 * @param hoverAlign hoverAlign Default value is null
 	 * @see com.smartgwt.client.widgets.Canvas#setShowHover
 	 */
 	public void setHoverAlign(Alignment hoverAlign) {
 		setAttribute("hoverAlign", hoverAlign.getValue(), true);
 	}
 
 	/**
 	 * If <code>this.showHover</code> is true, this property can be used to customize the alignment of content in the hover
 	 * canvas.
 	 *
 	 *
 	 * @return Alignment
 	 * @see com.smartgwt.client.widgets.Canvas#getShowHover
 	 */
 	public Alignment getHoverAlign() {
 		return EnumUtil.getEnum(Alignment.values(), getAttribute("hoverAlign"));
 	}
 
 	/**
 	 * If <code>this.showHover</code> is true and {@link com.smartgwt.client.widgets.Canvas#getHoverComponent
 	 * Canvas.getHoverComponent} is implemented, should the hoverCanvas returned from it be automatically destroyed when it is
 	 * hidden? <P> The default of null indicates that the component <b>will</b> be automatically destroyed.  Set to false to
 	 * prevent this.
 	 *
 	 * @param hoverAutoDestroy hoverAutoDestroy Default value is null
 	 * @see com.smartgwt.client.widgets.Canvas#setShowHover
 	 */
 	public void setHoverAutoDestroy(Boolean hoverAutoDestroy) {
 		setAttribute("hoverAutoDestroy", hoverAutoDestroy, true);
 	}
 
 	/**
 	 * If <code>this.showHover</code> is true and {@link com.smartgwt.client.widgets.Canvas#getHoverComponent
 	 * Canvas.getHoverComponent} is implemented, should the hoverCanvas returned from it be automatically destroyed when it is
 	 * hidden? <P> The default of null indicates that the component <b>will</b> be automatically destroyed.  Set to false to
 	 * prevent this.
 	 *
 	 *
 	 * @return Boolean
 	 * @see com.smartgwt.client.widgets.Canvas#getShowHover
 	 */
 	public Boolean getHoverAutoDestroy() {
 		return getAttributeAsBoolean("hoverAutoDestroy");
 	}
 
 	/**
 	 * If <code>this.canHover</code> is true, how long should the mouse be kept over this widget before the hover event is
 	 * fired
 	 *
 	 * @param hoverDelay hoverDelay Default value is 300
 	 * @see com.smartgwt.client.widgets.Canvas#setCanHover
 	 * @see com.smartgwt.client.widgets.events.HoverEvent
 	 */
 	public void setHoverDelay(int hoverDelay) {
 		setAttribute("hoverDelay", hoverDelay, true);
 	}
 
 	/**
 	 * If <code>this.canHover</code> is true, how long should the mouse be kept over this widget before the hover event is
 	 * fired
 	 *
 	 *
 	 * @return int
 	 * @see com.smartgwt.client.widgets.Canvas#getCanHover
 	 * @see com.smartgwt.client.widgets.events.HoverEvent
 	 */
 	public int getHoverDelay() {
 		return getAttributeAsInt("hoverDelay");
 	}
 
 	/**
 	 * If <code>this.showHover</code> is true, this property can be used to customize the height of the hover canvas shown.
 	 *
 	 * @param hoverHeight hoverHeight Default value is null
 	 * @see com.smartgwt.client.widgets.Canvas#setShowHover
 	 */
 	public void setHoverHeight(Integer hoverHeight) {
 		setAttribute("hoverHeight", hoverHeight, true);
 	}
 
 	/**
 	 * If <code>this.showHover</code> is true, this property can be used to customize the height of the hover canvas shown.
 	 *
 	 *
 	 * @return Integer
 	 * @see com.smartgwt.client.widgets.Canvas#getShowHover
 	 */
 	public Integer getHoverHeight() {
 		return getAttributeAsInt("hoverHeight");
 	}
 
 	/**
 	 * If <code>this.showHover</code> is true, should this widget's hover canvas be moved with the mouse while visible?
 	 *
 	 * @param hoverMoveWithMouse hoverMoveWithMouse Default value is null
 	 * @see com.smartgwt.client.widgets.Canvas#setShowHover
 	 */
 	public void setHoverMoveWithMouse(Boolean hoverMoveWithMouse) {
 		setAttribute("hoverMoveWithMouse", hoverMoveWithMouse, true);
 	}
 
 	/**
 	 * If <code>this.showHover</code> is true, should this widget's hover canvas be moved with the mouse while visible?
 	 *
 	 *
 	 * @return Boolean
 	 * @see com.smartgwt.client.widgets.Canvas#getShowHover
 	 */
 	public Boolean getHoverMoveWithMouse() {
 		return getAttributeAsBoolean("hoverMoveWithMouse");
 	}
 
 	/**
 	 * If <code>this.showHover</code> is true, should the hover canvas be shown with opacity other than 100?
 	 *
 	 * @param hoverOpacity hoverOpacity Default value is null
 	 * @see com.smartgwt.client.widgets.Canvas#setShowHover
 	 * @see <a href="http://www.smartclient.com/smartgwt/showcase/#basics_interaction_hovers" target="examples">Hovers / Tooltips Example</a>
 	 */
 	public void setHoverOpacity(Integer hoverOpacity) {
 		setAttribute("hoverOpacity", hoverOpacity, true);
 	}
 
 	/**
 	 * If <code>this.showHover</code> is true, should the hover canvas be shown with opacity other than 100?
 	 *
 	 *
 	 * @return Integer
 	 * @see com.smartgwt.client.widgets.Canvas#getShowHover
 	 * @see <a href="http://www.smartclient.com/smartgwt/showcase/#basics_interaction_hovers" target="examples">Hovers / Tooltips Example</a>
 	 */
 	public Integer getHoverOpacity() {
 		return getAttributeAsInt("hoverOpacity");
 	}
 
 	/**
 	 * If <code>this.showHover</code> is true, this property can be used to specify the css style to apply to the hover canvas.
 	 *
 	 * @param hoverStyle hoverStyle Default value is null
 	 * @see com.smartgwt.client.widgets.Canvas#setShowHover
 	 * @see <a href="http://www.smartclient.com/smartgwt/showcase/#basics_interaction_hovers" target="examples">Hovers / Tooltips Example</a>
 	 */
 	public void setHoverStyle(String hoverStyle) {
 		setAttribute("hoverStyle", hoverStyle, true);
 	}
 
 	/**
 	 * If <code>this.showHover</code> is true, this property can be used to specify the css style to apply to the hover canvas.
 	 *
 	 *
 	 * @return String
 	 * @see com.smartgwt.client.widgets.Canvas#getShowHover
 	 * @see <a href="http://www.smartclient.com/smartgwt/showcase/#basics_interaction_hovers" target="examples">Hovers / Tooltips Example</a>
 	 */
 	public String getHoverStyle() {
 		return getAttributeAsString("hoverStyle");
 	}
 
 	/**
 	 * If <code>this.showHover</code> is true, this property can be used to customize the vertical alignment of content in the
 	 * hover canvas.
 	 *
 	 * @param hoverVAlign hoverVAlign Default value is null
 	 * @see com.smartgwt.client.widgets.Canvas#setShowHover
 	 */
 	public void setHoverVAlign(VerticalAlignment hoverVAlign) {
 		setAttribute("hoverVAlign", hoverVAlign.getValue(), true);
 	}
 
 	/**
 	 * If <code>this.showHover</code> is true, this property can be used to customize the vertical alignment of content in the
 	 * hover canvas.
 	 *
 	 *
 	 * @return VerticalAlignment
 	 * @see com.smartgwt.client.widgets.Canvas#getShowHover
 	 */
 	public VerticalAlignment getHoverVAlign() {
 		return EnumUtil.getEnum(VerticalAlignment.values(), getAttribute("hoverVAlign"));
 	}
 
 	/**
 	 * If {@link com.smartgwt.client.widgets.Canvas#getShowHover this.showHover} is true, this property can be used to
 	 * customize the width of the hover canvas shown.
 	 *
 	 * @param hoverWidth hoverWidth Default value is null
 	 * @see com.smartgwt.client.widgets.Canvas#setShowHover
 	 * @see <a href="http://www.smartclient.com/smartgwt/showcase/#basics_interaction_hovers" target="examples">Hovers / Tooltips Example</a>
 	 */
 	public void setHoverWidth(Integer hoverWidth) {
 		setAttribute("hoverWidth", hoverWidth, true);
 	}
 
 	/**
 	 * If {@link com.smartgwt.client.widgets.Canvas#getShowHover this.showHover} is true, this property can be used to
 	 * customize the width of the hover canvas shown.
 	 *
 	 *
 	 * @return Integer
 	 * @see com.smartgwt.client.widgets.Canvas#getShowHover
 	 * @see <a href="http://www.smartclient.com/smartgwt/showcase/#basics_interaction_hovers" target="examples">Hovers / Tooltips Example</a>
 	 */
 	public Integer getHoverWidth() {
 		return getAttributeAsInt("hoverWidth");
 	}
 
 	/**
 	 * If <code>this.showHover</code> is true, this property can be used to customize the whether content in the hover canvas
 	 * is displayed in a single line, or wraps.
 	 *
 	 * @param hoverWrap hoverWrap Default value is null
 	 * @see com.smartgwt.client.widgets.Canvas#setShowHover
 	 */
 	public void setHoverWrap(Boolean hoverWrap) {
 		setAttribute("hoverWrap", hoverWrap, true);
 	}
 
 	/**
 	 * If <code>this.showHover</code> is true, this property can be used to customize the whether content in the hover canvas
 	 * is displayed in a single line, or wraps.
 	 *
 	 *
 	 * @return Boolean
 	 * @see com.smartgwt.client.widgets.Canvas#getShowHover
 	 */
 	public Boolean getHoverWrap() {
 		return getAttributeAsBoolean("hoverWrap");
 	}
 
 	/**
 	 * If {@link com.smartgwt.client.widgets.Canvas#getHtmlElement htmlElement} is specified, this attribute specifies the
 	 * position where  the canvas should be inserted relative to the <code>htmlElement</code> in the DOM.
 	 *
 	 * <br><br>If this method is called after the component has been drawn/initialized:
 	 * Setter for the {@link com.smartgwt.client.widgets.Canvas#getHtmlPosition htmlPosition}.
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param htmlPosition New htmlPosition for this canvas. Default value is "afterBegin"
 	 * @see com.smartgwt.client.docs.Positioning Positioning overview and related methods
 	 */
 	public void setHtmlPosition(DrawPosition htmlPosition) {
 		setAttribute("htmlPosition", htmlPosition.getValue(), true);
 	}
 
 	/**
 	 * If {@link com.smartgwt.client.widgets.Canvas#getHtmlElement htmlElement} is specified, this attribute specifies the
 	 * position where  the canvas should be inserted relative to the <code>htmlElement</code> in the DOM.
 	 *
 	 *
 	 * @return DrawPosition
 	 * @see com.smartgwt.client.docs.Positioning Positioning overview and related methods
 	 */
 	public DrawPosition getHtmlPosition() {
 		return EnumUtil.getEnum(DrawPosition.values(), getAttribute("htmlPosition"));
 	}
 
 	/**
 	 * Strategy to use when locating children in this canvas from an autoTest locator string.
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param locateChildrenBy locateChildrenBy Default value is null
 	 */
 	public void setLocateChildrenBy(LocatorStrategy locateChildrenBy) {
 		setAttribute("locateChildrenBy", locateChildrenBy.getValue(), true);
 	}
 
 	/**
 	 * Strategy to use when locating children in this canvas from an autoTest locator string.
 	 *
 	 *
 	 * @return LocatorStrategy
 	 */
 	public LocatorStrategy getLocateChildrenBy() {
 		return EnumUtil.getEnum(LocatorStrategy.values(), getAttribute("locateChildrenBy"));
 	}
 
 	/**
 	 * {@link com.smartgwt.client.types.LocatorTypeStrategy} to use when finding children within this canvas.
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param locateChildrenType locateChildrenType Default value is null
 	 */
 	public void setLocateChildrenType(LocatorTypeStrategy locateChildrenType) {
 		setAttribute("locateChildrenType", locateChildrenType.getValue(), true);
 	}
 
 	/**
 	 * {@link com.smartgwt.client.types.LocatorTypeStrategy} to use when finding children within this canvas.
 	 *
 	 *
 	 * @return LocatorTypeStrategy
 	 */
 	public LocatorTypeStrategy getLocateChildrenType() {
 		return EnumUtil.getEnum(LocatorTypeStrategy.values(), getAttribute("locateChildrenType"));
 	}
 
 	/**
 	 * Strategy to use when locating peers of this canvas from an autoTest locator string.
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param locatePeersBy locatePeersBy Default value is null
 	 */
 	public void setLocatePeersBy(LocatorStrategy locatePeersBy) {
 		setAttribute("locatePeersBy", locatePeersBy.getValue(), true);
 	}
 
 	/**
 	 * Strategy to use when locating peers of this canvas from an autoTest locator string.
 	 *
 	 *
 	 * @return LocatorStrategy
 	 */
 	public LocatorStrategy getLocatePeersBy() {
 		return EnumUtil.getEnum(LocatorStrategy.values(), getAttribute("locatePeersBy"));
 	}
 
 	/**
 	 * {@link com.smartgwt.client.types.LocatorTypeStrategy} to use when finding peers of this canvas.
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param locatePeersType locatePeersType Default value is null
 	 */
 	public void setLocatePeersType(LocatorTypeStrategy locatePeersType) {
 		setAttribute("locatePeersType", locatePeersType.getValue(), true);
 	}
 
 	/**
 	 * {@link com.smartgwt.client.types.LocatorTypeStrategy} to use when finding peers of this canvas.
 	 *
 	 *
 	 * @return LocatorTypeStrategy
 	 */
 	public LocatorTypeStrategy getLocatePeersType() {
 		return EnumUtil.getEnum(LocatorTypeStrategy.values(), getAttribute("locatePeersType"));
 	}
 
 	/**
 	 * Set the CSS Margin, in pixels, for this component.  Margin provides blank space outside of the border. <P> This property
 	 * sets the same thickness of margin on every side.  Differing per-side margins can be set in a CSS style and applied via
 	 * {@link com.smartgwt.client.widgets.Canvas#getStyleName styleName}. <P> Note that the specified size of the widget will
 	 * be the size <b>including</b> the margin thickness on each side.
 	 *
 	 * <br><br>If this method is called after the component has been drawn/initialized:
 	 * Set the CSS Margin, in pixels, for this component.  Margin provides blank space outside of the border. <P> This property sets the same thickness of margin on every side.  Differing per-side margins can be set in a CSS style and applied via {@link com.smartgwt.client.widgets.Canvas#getStyleName styleName}. <P> Note that the specified size of the widget will be the size <b>including</b> the margin thickness on each side.
 	 *
 	 * @param margin new margin in pixels. Default value is null
 	 * @see com.smartgwt.client.docs.Appearance Appearance overview and related methods
 	 */
 	public void setMargin(Integer margin) {
 		setAttribute("margin", margin, true);
 	}
 
 	/**
 	 * Set the CSS Margin, in pixels, for this component.  Margin provides blank space outside of the border. <P> This property
 	 * sets the same thickness of margin on every side.  Differing per-side margins can be set in a CSS style and applied via
 	 * {@link com.smartgwt.client.widgets.Canvas#getStyleName styleName}. <P> Note that the specified size of the widget will
 	 * be the size <b>including</b> the margin thickness on each side.
 	 *
 	 *
 	 * @return Integer
 	 * @see com.smartgwt.client.docs.Appearance Appearance overview and related methods
 	 */
 	public Integer getMargin() {
 		return getAttributeAsInt("margin");
 	}
 
 	/**
 	 * If {@link com.smartgwt.client.widgets.Canvas#getHtmlElement htmlElement} is specified, should this canvas initially be
 	 * drawn at the same dimensions as the htmlElement?<br> Note: setting this property will not force the canvas to resize if
 	 * the element subsequently resizes (for example due to page reflow).
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param matchElement matchElement Default value is null
 	 */
 	public void setMatchElement(Boolean matchElement) {
 		setAttribute("matchElement", matchElement, true);
 	}
 
 	/**
 	 * If {@link com.smartgwt.client.widgets.Canvas#getHtmlElement htmlElement} is specified, should this canvas initially be
 	 * drawn at the same dimensions as the htmlElement?<br> Note: setting this property will not force the canvas to resize if
 	 * the element subsequently resizes (for example due to page reflow).
 	 *
 	 *
 	 * @return Boolean
 	 */
 	public Boolean getMatchElement() {
 		return getAttributeAsBoolean("matchElement");
 	}
 
 	/**
 	 * Maximum height that this Canvas can be resized to.
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param maxHeight maxHeight Default value is 10000
 	 * @see com.smartgwt.client.docs.Sizing Sizing overview and related methods
 	 */
 	public void setMaxHeight(int maxHeight) {
 		setAttribute("maxHeight", maxHeight, true);
 	}
 
 	/**
 	 * Maximum height that this Canvas can be resized to.
 	 *
 	 *
 	 * @return int
 	 * @see com.smartgwt.client.docs.Sizing Sizing overview and related methods
 	 */
 	public int getMaxHeight() {
 		return getAttributeAsInt("maxHeight");
 	}
 
 	/**
 	 * Maximum width that this Canvas can be resized to.
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param maxWidth maxWidth Default value is 10000
 	 * @see com.smartgwt.client.docs.Sizing Sizing overview and related methods
 	 */
 	public void setMaxWidth(int maxWidth) {
 		setAttribute("maxWidth", maxWidth, true);
 	}
 
 	/**
 	 * Maximum width that this Canvas can be resized to.
 	 *
 	 *
 	 * @return int
 	 * @see com.smartgwt.client.docs.Sizing Sizing overview and related methods
 	 */
 	public int getMaxWidth() {
 		return getAttributeAsInt("maxWidth");
 	}
 
 	/**
 	 * Default class used to construct menus created by this component, including context menus.
 	 *
 	 * @param menuConstructor menuConstructor Default value is "Menu"
 	 * @throws IllegalStateException this property cannot be changed after the component has been created
 	 * @see com.smartgwt.client.widgets.events.ShowContextMenuEvent
 	 * @see com.smartgwt.client.docs.Cues Cues overview and related methods
 	 */
 	public void setMenuConstructor(String menuConstructor) throws IllegalStateException {
 		setAttribute("menuConstructor", menuConstructor, false);
 	}
 
 	/**
 	 * Default class used to construct menus created by this component, including context menus.
 	 *
 	 *
 	 * @return String
 	 * @see com.smartgwt.client.widgets.events.ShowContextMenuEvent
 	 * @see com.smartgwt.client.docs.Cues Cues overview and related methods
 	 */
 	public String getMenuConstructor() {
 		return getAttributeAsString("menuConstructor");
 	}
 
 	/**
 	 * Minimum height that this Canvas can be resized to. <P> Note that a Canvas with overflow:"visible" has an implicit
 	 * minimize size based on it's contents.
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param minHeight minHeight Default value is 10
 	 * @see com.smartgwt.client.docs.Sizing Sizing overview and related methods
 	 */
 	public void setMinHeight(int minHeight) {
 		setAttribute("minHeight", minHeight, true);
 	}
 
 	/**
 	 * Minimum height that this Canvas can be resized to. <P> Note that a Canvas with overflow:"visible" has an implicit
 	 * minimize size based on it's contents.
 	 *
 	 *
 	 * @return int
 	 * @see com.smartgwt.client.docs.Sizing Sizing overview and related methods
 	 */
 	public int getMinHeight() {
 		return getAttributeAsInt("minHeight");
 	}
 
 	/**
 	 * Minimum width that this Canvas can be resized to. <P> Note that a Canvas with overflow:"visible" has an implicit
 	 * minimize size based on it's contents.
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param minWidth minWidth Default value is 10
 	 * @see com.smartgwt.client.docs.Sizing Sizing overview and related methods
 	 */
 	public void setMinWidth(int minWidth) {
 		setAttribute("minWidth", minWidth, true);
 	}
 
 	/**
 	 * Minimum width that this Canvas can be resized to. <P> Note that a Canvas with overflow:"visible" has an implicit
 	 * minimize size based on it's contents.
 	 *
 	 *
 	 * @return int
 	 * @see com.smartgwt.client.docs.Sizing Sizing overview and related methods
 	 */
 	public int getMinWidth() {
 		return getAttributeAsInt("minWidth");
 	}
 
 	/**
 	 * Amount of time (in msec) between 'mouseStillDown' events for this object
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param mouseStillDownDelay mouseStillDownDelay Default value is 100
 	 */
 	public void setMouseStillDownDelay(int mouseStillDownDelay) {
 		setAttribute("mouseStillDownDelay", mouseStillDownDelay, true);
 	}
 
 	/**
 	 * Amount of time (in msec) between 'mouseStillDown' events for this object
 	 *
 	 *
 	 * @return int
 	 */
 	public int getMouseStillDownDelay() {
 		return getAttributeAsInt("mouseStillDownDelay");
 	}
 
 	/**
 	 * Amount of time (in msec) before mouseStillDown events start to be fired for this object.
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param mouseStillDownInitialDelay mouseStillDownInitialDelay Default value is 400
 	 */
 	public void setMouseStillDownInitialDelay(int mouseStillDownInitialDelay) {
 		setAttribute("mouseStillDownInitialDelay", mouseStillDownInitialDelay, true);
 	}
 
 	/**
 	 * Amount of time (in msec) before mouseStillDown events start to be fired for this object.
 	 *
 	 *
 	 * @return int
 	 */
 	public int getMouseStillDownInitialDelay() {
 		return getAttributeAsInt("mouseStillDownInitialDelay");
 	}
 
 	/**
 	 * If true, this canvas will receive all mouse-clicks as single click events rather than doubleClicks.
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param noDoubleClicks noDoubleClicks Default value is null
 	 */
 	public void setNoDoubleClicks(Boolean noDoubleClicks) {
 		setAttribute("noDoubleClicks", noDoubleClicks, true);
 	}
 
 	/**
 	 * If true, this canvas will receive all mouse-clicks as single click events rather than doubleClicks.
 	 *
 	 *
 	 * @return Boolean
 	 */
 	public Boolean getNoDoubleClicks() {
 		return getAttributeAsBoolean("noDoubleClicks");
 	}
 
 	/**
 	 * Renders the widget to be partly transparent. A widget's opacity property may      be set to any number between 0
 	 * (transparent) to 100 (opaque).  Null means don't specify opacity directly, 100 is fully opaque.  Note that heavy use of
 	 * opacity may slow down your browser.  See canvas.setOpacity() for details.
 	 *
 	 * <br><br>If this method is called after the component has been drawn/initialized:
 	 * Sets the opacity for the widget to the newOpacity value. This newOpacity          value must be within the range of 0 (transparent) to 100 (opaque). <br>          In Internet Explorer, any other filter effects will be wiped out.
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param opacity new opacity level. Default value is null
 	 * @see com.smartgwt.client.docs.Cues Cues overview and related methods
 	 */
 	public void setOpacity(Integer opacity) {
 		setAttribute("opacity", opacity, true);
 	}
 
 	/**
 	 * Renders the widget to be partly transparent. A widget's opacity property may      be set to any number between 0
 	 * (transparent) to 100 (opaque).  Null means don't specify opacity directly, 100 is fully opaque.  Note that heavy use of
 	 * opacity may slow down your browser.  See canvas.setOpacity() for details.
 	 *
 	 *
 	 * @return Integer
 	 * @see com.smartgwt.client.docs.Cues Cues overview and related methods
 	 */
 	public Integer getOpacity() {
 		return getAttributeAsInt("opacity");
 	}
 
 	/**
 	 * Controls what happens when the drawn size of the content of a Canvas is either   greater or smaller than the specified
 	 * size of the Canvas.  Similar to the CSS   property overflow, but consistent across browsers.  See Overflow type for  
 	 * details.
 	 *
 	 * <br><br>If this method is called after the component has been drawn/initialized:
 	 * Update the {@link com.smartgwt.client.widgets.Canvas#getOverflow overflow} of a Canvas after it has been created.
 	 *
 	 * @param overflow New overflow value.. Default value is Canvas.VISIBLE
 	 * @see com.smartgwt.client.docs.Sizing Sizing overview and related methods
 	 */
 	public void setOverflow(Overflow overflow) {
 		setAttribute("overflow", overflow.getValue(), true);
 	}
 
 	/**
 	 * Controls what happens when the drawn size of the content of a Canvas is either   greater or smaller than the specified
 	 * size of the Canvas.  Similar to the CSS   property overflow, but consistent across browsers.  See Overflow type for  
 	 * details.
 	 *
 	 *
 	 * @return Overflow
 	 * @see com.smartgwt.client.docs.Sizing Sizing overview and related methods
 	 */
 	public Overflow getOverflow() {
 		return EnumUtil.getEnum(Overflow.values(), getAttribute("overflow"));
 	}
 
 	/**
 	 * Set the CSS padding of this component, in pixels.  Padding provides space between the border and the component's
 	 * contents. <P> This property sets the same thickness of padding on every side.  Differing per-side padding can be set in
 	 * a CSS style and applied via {@link com.smartgwt.client.widgets.Canvas#getStyleName styleName}. <P> Note that CSS padding
 	 * does not affect the placement of {@link com.smartgwt.client.widgets.Canvas#getChildren children}.  To provide a blank
 	 * area around children, either use {@link com.smartgwt.client.widgets.Canvas#getMargin CSS margins} or use a {@link
 	 * com.smartgwt.client.widgets.layout.Layout} as the parent instead, and use properties such as {@link
 	 * com.smartgwt.client.widgets.layout.Layout#getLayoutMargin layoutMargin} to create blank space.
 	 *
 	 * <br><br>If this method is called after the component has been drawn/initialized:
 	 * Set the CSS padding of this component, in pixels.  Padding provides space between the border and the component's contents. <P> This property sets the same thickness of padding on every side.  Differing per-side padding can be set in a CSS style and applied via {@link com.smartgwt.client.widgets.Canvas#getStyleName styleName}. <P>
 	 *
 	 * @param padding new padding in pixels. Default value is null
 	 * @see com.smartgwt.client.docs.Appearance Appearance overview and related methods
 	 */
 	public void setPadding(Integer padding) {
 		setAttribute("padding", padding, true);
 	}
 
 	/**
 	 * Set the CSS padding of this component, in pixels.  Padding provides space between the border and the component's
 	 * contents. <P> This property sets the same thickness of padding on every side.  Differing per-side padding can be set in
 	 * a CSS style and applied via {@link com.smartgwt.client.widgets.Canvas#getStyleName styleName}. <P> Note that CSS padding
 	 * does not affect the placement of {@link com.smartgwt.client.widgets.Canvas#getChildren children}.  To provide a blank
 	 * area around children, either use {@link com.smartgwt.client.widgets.Canvas#getMargin CSS margins} or use a {@link
 	 * com.smartgwt.client.widgets.layout.Layout} as the parent instead, and use properties such as {@link
 	 * com.smartgwt.client.widgets.layout.Layout#getLayoutMargin layoutMargin} to create blank space.
 	 *
 	 *
 	 * @return Integer
 	 * @see com.smartgwt.client.docs.Appearance Appearance overview and related methods
 	 */
 	public Integer getPadding() {
 		return getAttributeAsInt("padding");
 	}
 
 	/**
 	 * Governs the model to be used when sizing canvases with percentage width or height, or  positioning widgets with a
 	 * specified {@link com.smartgwt.client.widgets.Canvas#getSnapTo snapTo}. <P> Only affects widgets with a a specified
 	 * {@link com.smartgwt.client.widgets.Canvas#getPercentSource percentSource}, or  widgets that have {@link
 	 * com.smartgwt.client.widgets.Canvas#getSnapTo snapTo} set and are peers of some  {@link
 	 * com.smartgwt.client.widgets.Canvas#getMasterElement other canvas}. <P> Determines whether the coordinates used for
 	 * sizing (for percentage sized widgets) and positioning (if <code>snapTo</code> is set) should be relative to the visible
 	 * size or the viewport size of the percentSource or masterElement widget.
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param percentBox percentBox Default value is "visible"
 	 * @throws IllegalStateException this property cannot be changed after the component has been created
 	 * @see com.smartgwt.client.docs.Sizing Sizing overview and related methods
 	 */
 	public void setPercentBox(PercentBoxModel percentBox) throws IllegalStateException {
 		setAttribute("percentBox", percentBox.getValue(), false);
 	}
 
 	/**
 	 * Governs the model to be used when sizing canvases with percentage width or height, or  positioning widgets with a
 	 * specified {@link com.smartgwt.client.widgets.Canvas#getSnapTo snapTo}. <P> Only affects widgets with a a specified
 	 * {@link com.smartgwt.client.widgets.Canvas#getPercentSource percentSource}, or  widgets that have {@link
 	 * com.smartgwt.client.widgets.Canvas#getSnapTo snapTo} set and are peers of some  {@link
 	 * com.smartgwt.client.widgets.Canvas#getMasterElement other canvas}. <P> Determines whether the coordinates used for
 	 * sizing (for percentage sized widgets) and positioning (if <code>snapTo</code> is set) should be relative to the visible
 	 * size or the viewport size of the percentSource or masterElement widget.
 	 *
 	 *
 	 * @return PercentBoxModel
 	 * @see com.smartgwt.client.docs.Sizing Sizing overview and related methods
 	 */
 	public PercentBoxModel getPercentBox() {
 		return EnumUtil.getEnum(PercentBoxModel.values(), getAttribute("percentBox"));
 	}
 
 	/**
 	 * Absolute or relative, corresponding to the "absolute" (with respect to parent) or
 	 *  "relative" (with respect to document flow) values for the CSS position attribute.
 	 *  <P>
 	 *  Setting <code>position:"relative"</code> enables Smart GWT components to be embedded
 	 *  directly into the native HTML flow of a page, causing the component to be rendered 
 	 *  within an existing DOM structure. 
 	 *  This attribute should only be set to <code>"relative"</code> on a top level component 
 	 *  (a component with no {@link com.smartgwt.client.widgets.Canvas#getParentElement parentElement}). 
 	 *  <P>
 	 *  There are 2 ways to embed relatively positioned canvases in the DOM - by default the
 	 *  component will be written out inline when it gets {@link com.smartgwt.client.widgets.Canvas#draw drawn()n}. For example
 	 *  to embed a canvas in an HTML table you could use this code: 
 	 *  <pre>
 	 *  &lt;table&gt;
 	 *    &lt;tr&gt;
 	 *      &lt;td&gt;
 	 *        &lt;script&gt;
 	 *          isc.Canvas.create({autoDraw:true, backgroundColor:"red", position:"relative"});
 	 *        &lt;/script&gt;
 	 *      &lt;td&gt;
 	 *    &lt;/tr&gt;
 	 *  &lt;/table&gt;
 	 *  </pre>
 	 *  Alternatively you can make use of the {@link com.smartgwt.client.widgets.Canvas#getHtmlElement htmlElement} attribute.
 	 *  <P>
 	 *  Relative positioning is intended as a short-term integration scenario while incrementally
 	 *  upgrading existing applications.
 	 *  Note that relative positioning is not used to manage layout within Smart GWT components -
 	 *  instead the {@link com.smartgwt.client.widgets.layout.Layout} class would typically be used.
 	 *  For best consistency and flexibility across browsers, all Smart GWT layout managers
 	 *  use absolute positioning.
 	 *  <P>
 	 * For canvases with a specified {@link com.smartgwt.client.widgets.Canvas#getHtmlElement htmlElement}, this attribute
 	 * defaults to
 	 *  <code>"relative"</code>. In all other cases the default value will be 
 	 *  <code>"absolute"</code>.
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param position position Default value is null
 	 * @see com.smartgwt.client.docs.Positioning Positioning overview and related methods
 	 * 
 	 */
 	public void setPosition(Positioning position) {
 		setAttribute("position", position.getValue(), true);
 	}
 
 	/**
 	 * Absolute or relative, corresponding to the "absolute" (with respect to parent) or
 	 *  "relative" (with respect to document flow) values for the CSS position attribute.
 	 *  <P>
 	 *  Setting <code>position:"relative"</code> enables Smart GWT components to be embedded
 	 *  directly into the native HTML flow of a page, causing the component to be rendered 
 	 *  within an existing DOM structure. 
 	 *  This attribute should only be set to <code>"relative"</code> on a top level component 
 	 *  (a component with no {@link com.smartgwt.client.widgets.Canvas#getParentElement parentElement}). 
 	 *  <P>
 	 *  There are 2 ways to embed relatively positioned canvases in the DOM - by default the
 	 *  component will be written out inline when it gets {@link com.smartgwt.client.widgets.Canvas#draw drawn()n}. For example
 	 *  to embed a canvas in an HTML table you could use this code: 
 	 *  <pre>
 	 *  &lt;table&gt;
 	 *    &lt;tr&gt;
 	 *      &lt;td&gt;
 	 *        &lt;script&gt;
 	 *          isc.Canvas.create({autoDraw:true, backgroundColor:"red", position:"relative"});
 	 *        &lt;/script&gt;
 	 *      &lt;td&gt;
 	 *    &lt;/tr&gt;
 	 *  &lt;/table&gt;
 	 *  </pre>
 	 *  Alternatively you can make use of the {@link com.smartgwt.client.widgets.Canvas#getHtmlElement htmlElement} attribute.
 	 *  <P>
 	 *  Relative positioning is intended as a short-term integration scenario while incrementally
 	 *  upgrading existing applications.
 	 *  Note that relative positioning is not used to manage layout within Smart GWT components -
 	 *  instead the {@link com.smartgwt.client.widgets.layout.Layout} class would typically be used.
 	 *  For best consistency and flexibility across browsers, all Smart GWT layout managers
 	 *  use absolute positioning.
 	 *  <P>
 	 * For canvases with a specified {@link com.smartgwt.client.widgets.Canvas#getHtmlElement htmlElement}, this attribute
 	 * defaults to
 	 *  <code>"relative"</code>. In all other cases the default value will be 
 	 *  <code>"absolute"</code>.
 	 *
 	 *
 	 * @return Positioning
 	 * @see com.smartgwt.client.docs.Positioning Positioning overview and related methods
 	 * 
 	 */
 	public Positioning getPosition() {
 		return EnumUtil.getEnum(Positioning.values(), getAttribute("position"));
 	}
 
 	/**
 	 * Prompt displayed in hover canvas if {@link com.smartgwt.client.widgets.Canvas#getShowHover showHover} is true.
 	 *
 	 * @param prompt prompt Default value is null
 	 * @see <a href="http://www.smartclient.com/smartgwt/showcase/#basics_interaction_hovers" target="examples">Hovers / Tooltips Example</a>
 	 */
 	public void setPrompt(String prompt) {
 		setAttribute("prompt", prompt, true);
 	}
 
 	/**
 	 * Prompt displayed in hover canvas if {@link com.smartgwt.client.widgets.Canvas#getShowHover showHover} is true.
 	 *
 	 *
 	 * @return String
 	 * @see <a href="http://www.smartclient.com/smartgwt/showcase/#basics_interaction_hovers" target="examples">Hovers / Tooltips Example</a>
 	 */
 	public String getPrompt() {
 		return getAttributeAsString("prompt");
 	}
 
 	/**
 	 * Should this element be redrawn in response to a resize? <P> Should be set to true for components whose {@link
 	 * com.smartgwt.client.widgets.Canvas#getInnerHTML inner HTML} will not automatically reflow to fit the component's new
 	 * size.
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param redrawOnResize redrawOnResize Default value is true
 	 * @see com.smartgwt.client.docs.Drawing Drawing overview and related methods
 	 */
 	public void setRedrawOnResize(Boolean redrawOnResize) {
 		setAttribute("redrawOnResize", redrawOnResize, true);
 	}
 
 	/**
 	 * Should this element be redrawn in response to a resize? <P> Should be set to true for components whose {@link
 	 * com.smartgwt.client.widgets.Canvas#getInnerHTML inner HTML} will not automatically reflow to fit the component's new
 	 * size.
 	 *
 	 *
 	 * @return Boolean
 	 * @see com.smartgwt.client.docs.Drawing Drawing overview and related methods
 	 */
 	public Boolean getRedrawOnResize() {
 		return getAttributeAsBoolean("redrawOnResize");
 	}
 
 	/**
 	 * When this Canvas is included as a member in a Layout, and {@link com.smartgwt.client.widgets.Canvas#getShowResizeBar
 	 * showResizeBar} is set to <code>true</code> so that a resizeBar is created, <code>resizeBarTarget:"next"</code> can be
 	 * set to indicate that the resizeBar should resize the next member of the layout rather than this one.  For resizeBars
 	 * that support hiding their target member when clicked on,  <code>resizeBarTarget:"next"</code> also means that the next
 	 * member will be the one hidden. <P> This is typically used to create a 3-way split pane, where left and right-hand
 	 * sections can be resized or hidden to allow a center section to expand. <P> <b>NOTE:</b> as with any Layout, to ensure
 	 * all available space is used, one or more members must maintain a flexible size (eg 75%, or *).  In a two pane Layout
 	 * with a normal resize bar, to fill all space after a user resizes, the member on the <b>right</b> should have flexible
 	 * size.  With resizeBarTarget:"next", the member on the <b>left</b> should have flexible size.
 	 *
 	 * @param resizeBarTarget resizeBarTarget Default value is null
 	 * @throws IllegalStateException this property cannot be changed after the component has been created
 	 * @see com.smartgwt.client.widgets.Canvas#setShowResizeBar
 	 * @see com.smartgwt.client.docs.LayoutMember LayoutMember overview and related methods
 	 */
 	public void setResizeBarTarget(String resizeBarTarget) throws IllegalStateException {
 		setAttribute("resizeBarTarget", resizeBarTarget, false);
 	}
 
 	/**
 	 * When this Canvas is included as a member in a Layout, and {@link com.smartgwt.client.widgets.Canvas#getShowResizeBar
 	 * showResizeBar} is set to <code>true</code> so that a resizeBar is created, <code>resizeBarTarget:"next"</code> can be
 	 * set to indicate that the resizeBar should resize the next member of the layout rather than this one.  For resizeBars
 	 * that support hiding their target member when clicked on,  <code>resizeBarTarget:"next"</code> also means that the next
 	 * member will be the one hidden. <P> This is typically used to create a 3-way split pane, where left and right-hand
 	 * sections can be resized or hidden to allow a center section to expand. <P> <b>NOTE:</b> as with any Layout, to ensure
 	 * all available space is used, one or more members must maintain a flexible size (eg 75%, or *).  In a two pane Layout
 	 * with a normal resize bar, to fill all space after a user resizes, the member on the <b>right</b> should have flexible
 	 * size.  With resizeBarTarget:"next", the member on the <b>left</b> should have flexible size.
 	 *
 	 *
 	 * @return String
 	 * @see com.smartgwt.client.widgets.Canvas#getShowResizeBar
 	 * @see com.smartgwt.client.docs.LayoutMember LayoutMember overview and related methods
 	 */
 	public String getResizeBarTarget() {
 		return getAttributeAsString("resizeBarTarget");
 	}
 
 	/**
 	 * How thick should we make the scrollbars for this canvas.<br>   NOTE: has no effect if showCustomScrollbars is false.
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param scrollbarSize scrollbarSize Default value is 16
 	 * @see com.smartgwt.client.widgets.Canvas#getScrollbarSize
 	 * @see com.smartgwt.client.docs.Scrolling Scrolling overview and related methods
 	 */
 	public void setScrollbarSize(int scrollbarSize) {
 		setAttribute("scrollbarSize", scrollbarSize, true);
 	}
 
 	/**
 	 * How thick should we make the scrollbars for this canvas.<br>   NOTE: has no effect if showCustomScrollbars is false.
 	 *
 	 *
 	 * @return Returns the thickness of this widget's scrollbars.<br>  For canvases showing custom scrollbars this is determined from
 	 * <code>this.scrollbarSize</code>
 	 * @see com.smartgwt.client.widgets.Canvas#getScrollbarSize
 	 * @see com.smartgwt.client.docs.Scrolling Scrolling overview and related methods
 	 */
 	public int getScrollbarSize() {
 		return getAttributeAsInt("scrollbarSize");
 	}
 
 	/**
 	 * Depth of the shadow, or the virtual height above the page of the widget throwing the shadow. <P> This is a single
 	 * parameter that can be used to control both <code>shadowSoftness</code> and <code>shadowOffset</code>.
 	 *
 	 * @param shadowDepth shadowDepth Default value is 4
 	 * @throws IllegalStateException this property cannot be changed after the component has been created
 	 */
 	public void setShadowDepth(int shadowDepth) throws IllegalStateException {
 		setAttribute("shadowDepth", shadowDepth, false);
 	}
 
 	/**
 	 * Depth of the shadow, or the virtual height above the page of the widget throwing the shadow. <P> This is a single
 	 * parameter that can be used to control both <code>shadowSoftness</code> and <code>shadowOffset</code>.
 	 *
 	 *
 	 * @return int
 	 */
 	public int getShadowDepth() {
 		return getAttributeAsInt("shadowDepth");
 	}
 
 	/**
 	 * Base name of the series of images for the sides, corners, and center of the shadow. <P> The actual image names fetched
 	 * for the dropShadow combine the segment name and the <code>shadowDepth</code> setting.  For example, given "ds.png" as
 	 * the base name, a depth of 4, and the top-left segment of the shadow, we'd use "ds4_TL.png". <P> The names for segments
 	 * are the same as those given for controlling resizable edges; see {@link com.smartgwt.client.widgets.Canvas#getResizeFrom
 	 * resizeFrom}.  The center segment has the name "center".  The center segment is the only segment that doesn't include the
 	 * depth in the URL, so the final image name for the center given a baseName of "ds.png" would be just "ds_center.png".
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param shadowImage shadowImage Default value is "[SKIN]ds.png"
 	 * @throws IllegalStateException this property cannot be changed after the component has been created
 	 */
 	public void setShadowImage(String shadowImage) throws IllegalStateException {
 		setAttribute("shadowImage", shadowImage, false);
 	}
 
 	/**
 	 * Base name of the series of images for the sides, corners, and center of the shadow. <P> The actual image names fetched
 	 * for the dropShadow combine the segment name and the <code>shadowDepth</code> setting.  For example, given "ds.png" as
 	 * the base name, a depth of 4, and the top-left segment of the shadow, we'd use "ds4_TL.png". <P> The names for segments
 	 * are the same as those given for controlling resizable edges; see {@link com.smartgwt.client.widgets.Canvas#getResizeFrom
 	 * resizeFrom}.  The center segment has the name "center".  The center segment is the only segment that doesn't include the
 	 * depth in the URL, so the final image name for the center given a baseName of "ds.png" would be just "ds_center.png".
 	 *
 	 *
 	 * @return String
 	 */
 	public String getShadowImage() {
 		return getAttributeAsString("shadowImage");
 	}
 
 	/**
 	 * Whether this canvas should be included in a printable view. <P> Default is to: <ul> <li> omit all peers (edges generated
 	 * by showEdges:true, etc) <li> omit anything considered a "control", such as a button or menu (see {@link
 	 * com.smartgwt.client.util.PrintProperties#getOmitControls omitControls}) <li> include everything else not marked
 	 * shouldPrint:false </ul>
 	 *
 	 * @param shouldPrint shouldPrint Default value is null
 	 * @see com.smartgwt.client.docs.Printing Printing overview and related methods
 	 */
 	public void setShouldPrint(Boolean shouldPrint) {
 		setAttribute("shouldPrint", shouldPrint, true);
 	}
 
 	/**
 	 * Whether this canvas should be included in a printable view. <P> Default is to: <ul> <li> omit all peers (edges generated
 	 * by showEdges:true, etc) <li> omit anything considered a "control", such as a button or menu (see {@link
 	 * com.smartgwt.client.util.PrintProperties#getOmitControls omitControls}) <li> include everything else not marked
 	 * shouldPrint:false </ul>
 	 *
 	 *
 	 * @return Boolean
 	 * @see com.smartgwt.client.docs.Printing Printing overview and related methods
 	 */
 	public Boolean getShouldPrint() {
 		return getAttributeAsBoolean("shouldPrint");
 	}
 
 	/**
 	 * Whether to use the browser's native scrollbars or Smart GWT-based scrollbars.
 	 *  <P>
 	 *  Smart GWT-based scrollbars are skinnable, giving you complete control over look and
 	 *  feel.  Smart GWT-based scrollbars also enable some interactions not possible with
 	 * native scrollbars, such as {@link com.smartgwt.client.widgets.grid.ListGrid#getFixedRecordHeights variable height
 	 * records}
 	 *  in grids in combination with {@link com.smartgwt.client.widgets.grid.ListGrid#getDataPageSize data paging}.
 	 *  <P>
 	 *  Native browser scrollbars are slightly faster simply because there are less Smart GWT
 	 *  components that need to be created, drawn and updated.  Each visible Smart GWT-based
 	 *  scrollbar on the screen has roughly the impact of two StretchImgButtons.
 	 *  <P>
 	 *  Smart GWT is always aware of the size of the scrollbar, regardless of whether native
 	 *  or custom scrollbars are used, and regardless of what operating system and/or operating
 	 *  system "theme" or "skin" is in use.  This means Smart GWT will correctly report the
 	 *  {@link com.smartgwt.client.widgets.Canvas#getViewportHeight viewport size}, that is, the interior area of the
 	 *  widget excluding space taken by scrollbars, which is key for exactly filling a component
 	 *  with content without creating unnecessary scrolling.
 	 *  <P>
 	 *  The <code>showCustomScrollbars</code> setting is typically overridden in load_skin.js in
 	 *  order to change the default for all Smart GWT components at once.
 	 * This may be achieved via the static {@link com.smartgwt.client.widgets.Canvas#setShowCustomScrollbars
 	 * Canvas.setShowCustomScrollbars} method or
 	 *  via a simple addProperties block, like so:
 	 *  <pre>
 	 *      isc.Canvas.addProperties({ showCustomScrollbars:false });
 	 *  </pre>
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param showCustomScrollbars showCustomScrollbars Default value is true
 	 * @see com.smartgwt.client.docs.Scrolling Scrolling overview and related methods
 	 */
 	public void setShowCustomScrollbars(Boolean showCustomScrollbars) {
 		setAttribute("showCustomScrollbars", showCustomScrollbars, true);
 	}
 
 	/**
 	 * Whether to use the browser's native scrollbars or Smart GWT-based scrollbars.
 	 *  <P>
 	 *  Smart GWT-based scrollbars are skinnable, giving you complete control over look and
 	 *  feel.  Smart GWT-based scrollbars also enable some interactions not possible with
 	 * native scrollbars, such as {@link com.smartgwt.client.widgets.grid.ListGrid#getFixedRecordHeights variable height
 	 * records}
 	 *  in grids in combination with {@link com.smartgwt.client.widgets.grid.ListGrid#getDataPageSize data paging}.
 	 *  <P>
 	 *  Native browser scrollbars are slightly faster simply because there are less Smart GWT
 	 *  components that need to be created, drawn and updated.  Each visible Smart GWT-based
 	 *  scrollbar on the screen has roughly the impact of two StretchImgButtons.
 	 *  <P>
 	 *  Smart GWT is always aware of the size of the scrollbar, regardless of whether native
 	 *  or custom scrollbars are used, and regardless of what operating system and/or operating
 	 *  system "theme" or "skin" is in use.  This means Smart GWT will correctly report the
 	 *  {@link com.smartgwt.client.widgets.Canvas#getViewportHeight viewport size}, that is, the interior area of the
 	 *  widget excluding space taken by scrollbars, which is key for exactly filling a component
 	 *  with content without creating unnecessary scrolling.
 	 *  <P>
 	 *  The <code>showCustomScrollbars</code> setting is typically overridden in load_skin.js in
 	 *  order to change the default for all Smart GWT components at once.
 	 * This may be achieved via the static {@link com.smartgwt.client.widgets.Canvas#setShowCustomScrollbars
 	 * Canvas.setShowCustomScrollbars} method or
 	 *  via a simple addProperties block, like so:
 	 *  <pre>
 	 *      isc.Canvas.addProperties({ showCustomScrollbars:false });
 	 *  </pre>
 	 *
 	 *
 	 * @return Boolean
 	 * @see com.smartgwt.client.docs.Scrolling Scrolling overview and related methods
 	 */
 	public Boolean getShowCustomScrollbars() {
 		return getAttributeAsBoolean("showCustomScrollbars");
 	}
 
 	/**
 	 * When this widget is dragged, if its dragAppearance is <code>"target"</code>, should we show a shadow behind the canvas
 	 * during the drag.
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param showDragShadow showDragShadow Default value is null
 	 * @see com.smartgwt.client.docs.Dragdrop Dragdrop overview and related methods
 	 * @see <a href="http://www.smartclient.com/smartgwt/showcase/#effects_dd_effects" target="examples">Drag effects Example</a>
 	 */
 	public void setShowDragShadow(Boolean showDragShadow) {
 		setAttribute("showDragShadow", showDragShadow, true);
 	}
 
 	/**
 	 * When this widget is dragged, if its dragAppearance is <code>"target"</code>, should we show a shadow behind the canvas
 	 * during the drag.
 	 *
 	 *
 	 * @return Boolean
 	 * @see com.smartgwt.client.docs.Dragdrop Dragdrop overview and related methods
 	 * @see <a href="http://www.smartclient.com/smartgwt/showcase/#effects_dd_effects" target="examples">Drag effects Example</a>
 	 */
 	public Boolean getShowDragShadow() {
 		return getAttributeAsBoolean("showDragShadow");
 	}
 
 	/**
 	 * Whether an {@link com.smartgwt.client.widgets.EdgedCanvas} should be used to show image-based edges around this
 	 * component.
 	 *
 	 * @param showEdges showEdges Default value is false
 	 * @throws IllegalStateException this property cannot be changed after the component has been created
 	 * @see <a href="http://www.smartclient.com/smartgwt/showcase/#effects_lf_edges" target="examples">Edges Example</a>
 	 */
 	public void setShowEdges(Boolean showEdges) throws IllegalStateException {
 		setAttribute("showEdges", showEdges, false);
 	}
 
 	/**
 	 * Whether an {@link com.smartgwt.client.widgets.EdgedCanvas} should be used to show image-based edges around this
 	 * component.
 	 *
 	 *
 	 * @return Boolean
 	 * @see <a href="http://www.smartclient.com/smartgwt/showcase/#effects_lf_edges" target="examples">Edges Example</a>
 	 */
 	public Boolean getShowEdges() {
 		return getAttributeAsBoolean("showEdges");
 	}
 
 	/**
 	 * If <code>this.canHover</code> is true, should we show the global hover canvas by default when the user hovers over this
 	 * canvas?
 	 *
 	 * @param showHover showHover Default value is true
 	 * @see com.smartgwt.client.widgets.Canvas#getHoverHTML
 	 */
 	public void setShowHover(Boolean showHover) {
 		setAttribute("showHover", showHover, true);
 	}
 
 	/**
 	 * If <code>this.canHover</code> is true, should we show the global hover canvas by default when the user hovers over this
 	 * canvas?
 	 *
 	 *
 	 * @return Boolean
 	 * @see com.smartgwt.client.widgets.Canvas#getHoverHTML
 	 */
 	public Boolean getShowHover() {
 		return getAttributeAsBoolean("showHover");
 	}
 
 	/**
 	 * When set to true, shows a widget hovering at the mouse point instead of the builtin hover label.  Override {@link
 	 * com.smartgwt.client.widgets.Canvas#getHoverComponent getHoverComponent} to provide the Canvas to show as the
 	 * hoverComponent.
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param showHoverComponents showHoverComponents Default value is false
 	 */
 	public void setShowHoverComponents(Boolean showHoverComponents) {
 		setAttribute("showHoverComponents", showHoverComponents, true);
 	}
 
 	/**
 	 * When set to true, shows a widget hovering at the mouse point instead of the builtin hover label.  Override {@link
 	 * com.smartgwt.client.widgets.Canvas#getHoverComponent getHoverComponent} to provide the Canvas to show as the
 	 * hoverComponent.
 	 *
 	 *
 	 * @return Boolean
 	 */
 	public Boolean getShowHoverComponents() {
 		return getAttributeAsBoolean("showHoverComponents");
 	}
 
 	/**
 	 * When this Canvas is included as a member in a {@link com.smartgwt.client.widgets.layout.Layout}, whether a resizeBar
 	 * should be shown after this member in the layout, to allow it to be resized. <p> Whether a resizeBar is actually shown
 	 * also depends on the  {@link com.smartgwt.client.widgets.layout.Layout#getDefaultResizeBars defaultResizeBars} attribute
 	 * of the layout, and whether this Canvas is the last layout member. <p> By default the resize bar acts on the Canvas that
 	 * it is declared on.  If you want the resize bar to instead act on the next member of the Layout (e.g. to collapse down or
 	 * to the right), set {@link com.smartgwt.client.widgets.Canvas#getResizeBarTarget resizeBarTarget} as well.
 	 *
 	 * <br><br>If this method is called after the component has been drawn/initialized:
 	 * When this Canvas is included as a member in a {@link com.smartgwt.client.widgets.layout.Layout}, dynamically updates whether a  resizeBar should be shown after this member in the layout, to allow it to be resized. <p> Whether a resizeBar is actually shown also depends on the  {@link com.smartgwt.client.widgets.layout.Layout#getDefaultResizeBars defaultResizeBars} attribute of the layout, and whether this Canvas is the last layout member.
 	 *
 	 * @param showResizeBar setting for this.showResizeBar. Default value is false
 	 * @see com.smartgwt.client.widgets.Canvas#setResizeBarTarget
 	 * @see com.smartgwt.client.widgets.layout.Layout#setDefaultResizeBars
 	 * @see com.smartgwt.client.docs.LayoutMember LayoutMember overview and related methods
 	 * @see <a href="http://www.smartclient.com/smartgwt/showcase/#layout_nesting" target="examples">Nesting Example</a>
 	 */
 	public void setShowResizeBar(Boolean showResizeBar) {
 		setAttribute("showResizeBar", showResizeBar, true);
 	}
 
 	/**
 	 * When this Canvas is included as a member in a {@link com.smartgwt.client.widgets.layout.Layout}, whether a resizeBar
 	 * should be shown after this member in the layout, to allow it to be resized. <p> Whether a resizeBar is actually shown
 	 * also depends on the  {@link com.smartgwt.client.widgets.layout.Layout#getDefaultResizeBars defaultResizeBars} attribute
 	 * of the layout, and whether this Canvas is the last layout member. <p> By default the resize bar acts on the Canvas that
 	 * it is declared on.  If you want the resize bar to instead act on the next member of the Layout (e.g. to collapse down or
 	 * to the right), set {@link com.smartgwt.client.widgets.Canvas#getResizeBarTarget resizeBarTarget} as well.
 	 *
 	 *
 	 * @return Boolean
 	 * @see com.smartgwt.client.widgets.Canvas#getResizeBarTarget
 	 * @see com.smartgwt.client.widgets.layout.Layout#getDefaultResizeBars
 	 * @see com.smartgwt.client.docs.LayoutMember LayoutMember overview and related methods
 	 * @see <a href="http://www.smartclient.com/smartgwt/showcase/#layout_nesting" target="examples">Nesting Example</a>
 	 */
 	public Boolean getShowResizeBar() {
 		return getAttributeAsBoolean("showResizeBar");
 	}
 
 	/**
 	 * Whether to show a drop shadow for this Canvas
 	 *
 	 * <br><br>If this method is called after the component has been drawn/initialized:
 	 * Method to update {@link com.smartgwt.client.widgets.Canvas#getShowShadow showShadow}.
 	 *
 	 * @param showShadow true if the shadow should be visible false if not. Default value is false
 	 * @throws IllegalStateException this property cannot be changed after the component has been created
 	 * @see <a href="http://www.smartclient.com/smartgwt/showcase/#effects_lf_shadows" target="examples">Shadows Example</a>
 	 */
 	public void setShowShadow(Boolean showShadow) throws IllegalStateException {
 		setAttribute("showShadow", showShadow, false);
 	}
 
 	/**
 	 * Whether to show a drop shadow for this Canvas
 	 *
 	 *
 	 * @return Boolean
 	 * @see <a href="http://www.smartclient.com/smartgwt/showcase/#effects_lf_shadows" target="examples">Shadows Example</a>
 	 */
 	public Boolean getShowShadow() {
 		return getAttributeAsBoolean("showShadow");
 	}
 
 	/**
 	 * Default directory for skin images (those defined by the class), relative to the Page-wide {@link
 	 * com.smartgwt.client.util.Page#getSkinDir skinDir}.
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param skinImgDir skinImgDir Default value is "images/"
 	 * @see com.smartgwt.client.docs.Images Images overview and related methods
 	 */
 	public void setSkinImgDir(String skinImgDir) {
 		setAttribute("skinImgDir", skinImgDir, true);
 	}
 
 	/**
 	 * Default directory for skin images (those defined by the class), relative to the Page-wide {@link
 	 * com.smartgwt.client.util.Page#getSkinDir skinDir}.
 	 *
 	 *
 	 * @return String
 	 * @see com.smartgwt.client.docs.Images Images overview and related methods
 	 */
 	public String getSkinImgDir() {
 		return getAttributeAsString("skinImgDir");
 	}
 
 	/**
 	 * Describes which axes to apply snap-to-grid to. Valid values are Canvas.HORIZONTAL, Canvas.VERTICAL and Canvas.BOTH
 	 *
 	 * @param snapAxis snapAxis Default value is Canvas.BOTH
 	 * @see com.smartgwt.client.widgets.Canvas#setSnapToGrid
 	 * @see com.smartgwt.client.widgets.Canvas#setSnapResizeToGrid
 	 * @see com.smartgwt.client.widgets.Canvas#setChildrenSnapToGrid
 	 * @see com.smartgwt.client.widgets.Canvas#setChildrenSnapResizeToGrid
 	 * @see com.smartgwt.client.docs.Dragdrop Dragdrop overview and related methods
 	 */
 	public void setSnapAxis(String snapAxis) {
 		setAttribute("snapAxis", snapAxis, true);
 	}
 
 	/**
 	 * Describes which axes to apply snap-to-grid to. Valid values are Canvas.HORIZONTAL, Canvas.VERTICAL and Canvas.BOTH
 	 *
 	 *
 	 * @return String
 	 * @see com.smartgwt.client.widgets.Canvas#getSnapToGrid
 	 * @see com.smartgwt.client.widgets.Canvas#getSnapResizeToGrid
 	 * @see com.smartgwt.client.widgets.Canvas#getChildrenSnapToGrid
 	 * @see com.smartgwt.client.widgets.Canvas#getChildrenSnapResizeToGrid
 	 * @see com.smartgwt.client.docs.Dragdrop Dragdrop overview and related methods
 	 */
 	public String getSnapAxis() {
 		return getAttributeAsString("snapAxis");
 	}
 
 	/**
 	 * If {@link com.smartgwt.client.widgets.Canvas#getSnapTo snapTo} is defined to this widget, this property can be used to
 	 * define which edge of this widget should be snapped to an edge of the master or parent  element. <P> If unspecified the,
 	 * default snapTo behavior is set up to align the "snapTo" edge of this  widget with the snapTo edge of the master or
 	 * parent.
 	 *
 	 * <br><br>If this method is called after the component has been drawn/initialized:
 	 * Set the snapEdge property of this canvas, and handle repositioning.
 	 *
 	 * @param snapEdge new snapEdge value. Default value is null
 	 * @see com.smartgwt.client.widgets.Canvas#setSnapTo
 	 * @see com.smartgwt.client.docs.Sizing Sizing overview and related methods
 	 */
 	public void setSnapEdge(String snapEdge) {
 		setAttribute("snapEdge", snapEdge, true);
 	}
 
 	/**
 	 * If {@link com.smartgwt.client.widgets.Canvas#getSnapTo snapTo} is defined to this widget, this property can be used to
 	 * define which edge of this widget should be snapped to an edge of the master or parent  element. <P> If unspecified the,
 	 * default snapTo behavior is set up to align the "snapTo" edge of this  widget with the snapTo edge of the master or
 	 * parent.
 	 *
 	 *
 	 * @return Return the snapEdge value of this object
 	 * @see com.smartgwt.client.widgets.Canvas#getSnapTo
 	 * @see com.smartgwt.client.docs.Sizing Sizing overview and related methods
 	 */
 	public String getSnapEdge() {
 		return getAttributeAsString("snapEdge");
 	}
 
 	/**
 	 * The horizontal snap direction. Set this value to Canvas.BEFORE to snap to the nearest gridpoint to the left; set it to
 	 * Canvas.AFTER to snap to the nearest gridpoint to the right; and set it to Canvas.NEAREST to snap to the nearest
 	 * gridpoint in either direction.
 	 *
 	 * @param snapHDirection snapHDirection Default value is Canvas.AFTER
 	 * @see com.smartgwt.client.widgets.Canvas#setSnapToGrid
 	 * @see com.smartgwt.client.widgets.Canvas#setSnapResizeToGrid
 	 * @see com.smartgwt.client.widgets.Canvas#setChildrenSnapToGrid
 	 * @see com.smartgwt.client.widgets.Canvas#setChildrenSnapResizeToGrid
 	 * @see com.smartgwt.client.docs.Dragdrop Dragdrop overview and related methods
 	 */
 	public void setSnapHDirection(String snapHDirection) {
 		setAttribute("snapHDirection", snapHDirection, true);
 	}
 
 	/**
 	 * The horizontal snap direction. Set this value to Canvas.BEFORE to snap to the nearest gridpoint to the left; set it to
 	 * Canvas.AFTER to snap to the nearest gridpoint to the right; and set it to Canvas.NEAREST to snap to the nearest
 	 * gridpoint in either direction.
 	 *
 	 *
 	 * @return String
 	 * @see com.smartgwt.client.widgets.Canvas#getSnapToGrid
 	 * @see com.smartgwt.client.widgets.Canvas#getSnapResizeToGrid
 	 * @see com.smartgwt.client.widgets.Canvas#getChildrenSnapToGrid
 	 * @see com.smartgwt.client.widgets.Canvas#getChildrenSnapResizeToGrid
 	 * @see com.smartgwt.client.docs.Dragdrop Dragdrop overview and related methods
 	 */
 	public String getSnapHDirection() {
 		return getAttributeAsString("snapHDirection");
 	}
 
 	/**
 	 * The horizontal grid size to use, in pixels, when snap-to-grid is enabled.
 	 *
 	 * @param snapHGap snapHGap Default value is 20
 	 * @see com.smartgwt.client.widgets.Canvas#setSnapToGrid
 	 * @see com.smartgwt.client.widgets.Canvas#setSnapResizeToGrid
 	 * @see com.smartgwt.client.widgets.Canvas#setChildrenSnapToGrid
 	 * @see com.smartgwt.client.widgets.Canvas#setChildrenSnapResizeToGrid
 	 * @see com.smartgwt.client.docs.Dragdrop Dragdrop overview and related methods
 	 */
 	public void setSnapHGap(int snapHGap) {
 		setAttribute("snapHGap", snapHGap, true);
 	}
 
 	/**
 	 * The horizontal grid size to use, in pixels, when snap-to-grid is enabled.
 	 *
 	 *
 	 * @return int
 	 * @see com.smartgwt.client.widgets.Canvas#getSnapToGrid
 	 * @see com.smartgwt.client.widgets.Canvas#getSnapResizeToGrid
 	 * @see com.smartgwt.client.widgets.Canvas#getChildrenSnapToGrid
 	 * @see com.smartgwt.client.widgets.Canvas#getChildrenSnapResizeToGrid
 	 * @see com.smartgwt.client.docs.Dragdrop Dragdrop overview and related methods
 	 */
 	public int getSnapHGap() {
 		return getAttributeAsInt("snapHGap");
 	}
 
 	/**
 	 * If {@link com.smartgwt.client.widgets.Canvas#getSnapTo snapTo} is defined for this widget, this property can be used to
 	 * specify an offset in px or percentage for the left coordinate of this widget. <P> For example if <code>snapTo</code> is
 	 * specified as <code>"L"</code> and  <code>snapOffsetLeft</code> is set to 6, this widget will be rendered 6px inside the
 	 * left edge of its parent or master element. Alternatively if <code>snapTo</code> was set to <code>"R"</code>, a
 	 * <code>snapOffsetLeft</code> value of -6 would cause the  component to be rendered 6px inside the right edge of its
 	 * parent or masterElement.
 	 *
 	 * @param snapOffsetLeft snapOffsetLeft Default value is null
 	 * @see com.smartgwt.client.widgets.Canvas#setSnapTo
 	 * @see com.smartgwt.client.docs.Sizing Sizing overview and related methods
 	 */
 	public void setSnapOffsetLeft(Integer snapOffsetLeft) {
 		setAttribute("snapOffsetLeft", snapOffsetLeft, true);
 	}
 
 	/**
 	 * If {@link com.smartgwt.client.widgets.Canvas#getSnapTo snapTo} is defined for this widget, this property can be used to
 	 * specify an offset in px or percentage for the left coordinate of this widget. <P> For example if <code>snapTo</code> is
 	 * specified as <code>"L"</code> and  <code>snapOffsetLeft</code> is set to 6, this widget will be rendered 6px inside the
 	 * left edge of its parent or master element. Alternatively if <code>snapTo</code> was set to <code>"R"</code>, a
 	 * <code>snapOffsetLeft</code> value of -6 would cause the  component to be rendered 6px inside the right edge of its
 	 * parent or masterElement.
 	 *
 	 *
 	 * @return Integer
 	 * @see com.smartgwt.client.widgets.Canvas#getSnapTo
 	 * @see com.smartgwt.client.docs.Sizing Sizing overview and related methods
 	 */
 	public Integer getSnapOffsetLeft() {
 		return getAttributeAsInt("snapOffsetLeft");
 	}
 
 	/**
 	 * If {@link com.smartgwt.client.widgets.Canvas#getSnapTo snapTo} is defined for this widget, this property can be used to
 	 * specify an offset in px or percentage for the top coordinate of this widget. <P> For example if <code>snapTo</code> is
 	 * specified as <code>"T"</code> and  <code>snapOffsetTop</code> is set to 6, this widget will be rendered 6px below the
 	 * top edge of its parent or master element. . Alternatively if <code>snapTo</code> was set to <code>"B"</code>, a
 	 * <code>snapOffsetTop</code> value of -6 would cause the  component to be rendered 6px inside the bottom edge of its
 	 * parent or masterElement.
 	 *
 	 * @param snapOffsetTop snapOffsetTop Default value is null
 	 * @see com.smartgwt.client.widgets.Canvas#setSnapTo
 	 * @see com.smartgwt.client.docs.Sizing Sizing overview and related methods
 	 */
 	public void setSnapOffsetTop(Integer snapOffsetTop) {
 		setAttribute("snapOffsetTop", snapOffsetTop, true);
 	}
 
 	/**
 	 * If {@link com.smartgwt.client.widgets.Canvas#getSnapTo snapTo} is defined for this widget, this property can be used to
 	 * specify an offset in px or percentage for the top coordinate of this widget. <P> For example if <code>snapTo</code> is
 	 * specified as <code>"T"</code> and  <code>snapOffsetTop</code> is set to 6, this widget will be rendered 6px below the
 	 * top edge of its parent or master element. . Alternatively if <code>snapTo</code> was set to <code>"B"</code>, a
 	 * <code>snapOffsetTop</code> value of -6 would cause the  component to be rendered 6px inside the bottom edge of its
 	 * parent or masterElement.
 	 *
 	 *
 	 * @return Integer
 	 * @see com.smartgwt.client.widgets.Canvas#getSnapTo
 	 * @see com.smartgwt.client.docs.Sizing Sizing overview and related methods
 	 */
 	public Integer getSnapOffsetTop() {
 		return getAttributeAsInt("snapOffsetTop");
 	}
 
 	/**
 	 * When this canvas is dropped onto an object supporting snap-to-grid, should it snap to the grid (true, the default) or
 	 * just drop wherever the mouse is (false).
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param snapOnDrop snapOnDrop Default value is true
 	 * @see com.smartgwt.client.widgets.Canvas#setSnapToGrid
 	 * @see com.smartgwt.client.widgets.Canvas#shouldSnapOnDrop
 	 * @see com.smartgwt.client.docs.Dragdrop Dragdrop overview and related methods
 	 */
 	public void setSnapOnDrop(Boolean snapOnDrop) {
 		setAttribute("snapOnDrop", snapOnDrop, true);
 	}
 
 	/**
 	 * When this canvas is dropped onto an object supporting snap-to-grid, should it snap to the grid (true, the default) or
 	 * just drop wherever the mouse is (false).
 	 *
 	 *
 	 * @return Boolean
 	 * @see com.smartgwt.client.widgets.Canvas#getSnapToGrid
 	 * @see com.smartgwt.client.widgets.Canvas#shouldSnapOnDrop
 	 * @see com.smartgwt.client.docs.Dragdrop Dragdrop overview and related methods
 	 */
 	public Boolean getSnapOnDrop() {
 		return getAttributeAsBoolean("snapOnDrop");
 	}
 
 	/**
 	 * Causes this canvas to snap to its parent's grid when resizing. Note that this value defaults to the Canvas's  {@link
 	 * com.smartgwt.client.widgets.Canvas#getSnapToGrid snapToGrid} value if undefined.
 	 *
 	 * @param snapResizeToGrid snapResizeToGrid Default value is null
 	 * @see com.smartgwt.client.docs.Dragdrop Dragdrop overview and related methods
 	 */
 	public void setSnapResizeToGrid(Boolean snapResizeToGrid) {
 		setAttribute("snapResizeToGrid", snapResizeToGrid, true);
 	}
 
 	/**
 	 * Causes this canvas to snap to its parent's grid when resizing. Note that this value defaults to the Canvas's  {@link
 	 * com.smartgwt.client.widgets.Canvas#getSnapToGrid snapToGrid} value if undefined.
 	 *
 	 *
 	 * @return Boolean
 	 * @see com.smartgwt.client.docs.Dragdrop Dragdrop overview and related methods
 	 */
 	public Boolean getSnapResizeToGrid() {
 		return getAttributeAsBoolean("snapResizeToGrid");
 	}
 
 	/**
 	 * Position this widget such that it is aligned with ("snapped to") an edge of its  {@link
 	 * com.smartgwt.client.widgets.Canvas#getMasterElement masterElement} (if specified), or its {@link
 	 * com.smartgwt.client.widgets.Canvas#getParentElement parentElement}. <P> Note that this property also impacts the sizing
 	 * of this widget. If this widgets size is specified as a percent value, and has no explicit {@link
 	 * com.smartgwt.client.widgets.Canvas#getPercentSource percentSource}, sizing will be calculated based on the size of the 
 	 * masterElement when snapTo is set. <P> Possible values: BR, BL, TR, TL, R, L, B, T, C where B=Bottom, T=Top, L=Left,
 	 * R=right and C=center <P> Standard snapTo behavior will attach the outer edge of the widget to the parent or master
 	 * element - for example setting <code>snapTo</code> to <code>"B"</code> would align the bottom edge of this component with
 	 * the bottom edge of the master or parent element  (and center this component horizontally over its master or parent
 	 * element). {@link com.smartgwt.client.widgets.Canvas#getSnapEdge snapEdge} can be specified to change this behavior
 	 * allowing the developer to, for example, align the top edge of this component with the bottom edge of its masterElement.
 	 * <P> {@link com.smartgwt.client.widgets.Canvas#getSnapOffsetLeft snapOffsetLeft} and {@link
 	 * com.smartgwt.client.widgets.Canvas#getSnapOffsetTop snapOffsetTop} may also be specified to offset the element from
 	 * exact snapTo alignment.
 	 *
 	 * <br><br>If this method is called after the component has been drawn/initialized:
 	 * Set the snapTo property of this canvas, and handle repositioning.
 	 *
 	 * @param snapTo new snapTo value. Default value is null
 	 * @see com.smartgwt.client.widgets.Canvas#setSnapEdge
 	 * @see com.smartgwt.client.widgets.Canvas#setPercentBox
 	 * @see com.smartgwt.client.docs.Sizing Sizing overview and related methods
 	 */
 	public void setSnapTo(String snapTo) {
 		setAttribute("snapTo", snapTo, true);
 	}
 
 	/**
 	 * Position this widget such that it is aligned with ("snapped to") an edge of its  {@link
 	 * com.smartgwt.client.widgets.Canvas#getMasterElement masterElement} (if specified), or its {@link
 	 * com.smartgwt.client.widgets.Canvas#getParentElement parentElement}. <P> Note that this property also impacts the sizing
 	 * of this widget. If this widgets size is specified as a percent value, and has no explicit {@link
 	 * com.smartgwt.client.widgets.Canvas#getPercentSource percentSource}, sizing will be calculated based on the size of the 
 	 * masterElement when snapTo is set. <P> Possible values: BR, BL, TR, TL, R, L, B, T, C where B=Bottom, T=Top, L=Left,
 	 * R=right and C=center <P> Standard snapTo behavior will attach the outer edge of the widget to the parent or master
 	 * element - for example setting <code>snapTo</code> to <code>"B"</code> would align the bottom edge of this component with
 	 * the bottom edge of the master or parent element  (and center this component horizontally over its master or parent
 	 * element). {@link com.smartgwt.client.widgets.Canvas#getSnapEdge snapEdge} can be specified to change this behavior
 	 * allowing the developer to, for example, align the top edge of this component with the bottom edge of its masterElement.
 	 * <P> {@link com.smartgwt.client.widgets.Canvas#getSnapOffsetLeft snapOffsetLeft} and {@link
 	 * com.smartgwt.client.widgets.Canvas#getSnapOffsetTop snapOffsetTop} may also be specified to offset the element from
 	 * exact snapTo alignment.
 	 *
 	 *
 	 * @return Return the snapTo value of this object
 	 * @see com.smartgwt.client.widgets.Canvas#getSnapEdge
 	 * @see com.smartgwt.client.widgets.Canvas#getPercentBox
 	 * @see com.smartgwt.client.docs.Sizing Sizing overview and related methods
 	 */
 	public String getSnapTo() {
 		return getAttributeAsString("snapTo");
 	}
 
 	/**
 	 * Causes this canvas to snap to its parent's grid when dragging.
 	 *
 	 * @param snapToGrid snapToGrid Default value is null
 	 * @see com.smartgwt.client.widgets.Canvas#setChildrenSnapToGrid
 	 * @see com.smartgwt.client.docs.Dragdrop Dragdrop overview and related methods
 	 */
 	public void setSnapToGrid(Boolean snapToGrid) {
 		setAttribute("snapToGrid", snapToGrid, true);
 	}
 
 	/**
 	 * Causes this canvas to snap to its parent's grid when dragging.
 	 *
 	 *
 	 * @return Boolean
 	 * @see com.smartgwt.client.widgets.Canvas#getChildrenSnapToGrid
 	 * @see com.smartgwt.client.docs.Dragdrop Dragdrop overview and related methods
 	 */
 	public Boolean getSnapToGrid() {
 		return getAttributeAsBoolean("snapToGrid");
 	}
 
 	/**
 	 * The vertical snap direction. Set this value to Canvas.BEFORE to snap to the nearest gridpoint above; set it to
 	 * Canvas.AFTER to snap to the nearest gridpoint below; and set it to Canvas.NEAREST to snap to the nearest gridpoint in
 	 * either direction.
 	 *
 	 * @param snapVDirection snapVDirection Default value is Canvas.AFTER
 	 * @see com.smartgwt.client.widgets.Canvas#setSnapToGrid
 	 * @see com.smartgwt.client.widgets.Canvas#setSnapResizeToGrid
 	 * @see com.smartgwt.client.widgets.Canvas#setChildrenSnapToGrid
 	 * @see com.smartgwt.client.widgets.Canvas#setChildrenSnapResizeToGrid
 	 * @see com.smartgwt.client.docs.Dragdrop Dragdrop overview and related methods
 	 */
 	public void setSnapVDirection(String snapVDirection) {
 		setAttribute("snapVDirection", snapVDirection, true);
 	}
 
 	/**
 	 * The vertical snap direction. Set this value to Canvas.BEFORE to snap to the nearest gridpoint above; set it to
 	 * Canvas.AFTER to snap to the nearest gridpoint below; and set it to Canvas.NEAREST to snap to the nearest gridpoint in
 	 * either direction.
 	 *
 	 *
 	 * @return String
 	 * @see com.smartgwt.client.widgets.Canvas#getSnapToGrid
 	 * @see com.smartgwt.client.widgets.Canvas#getSnapResizeToGrid
 	 * @see com.smartgwt.client.widgets.Canvas#getChildrenSnapToGrid
 	 * @see com.smartgwt.client.widgets.Canvas#getChildrenSnapResizeToGrid
 	 * @see com.smartgwt.client.docs.Dragdrop Dragdrop overview and related methods
 	 */
 	public String getSnapVDirection() {
 		return getAttributeAsString("snapVDirection");
 	}
 
 	/**
 	 * The vertical grid size to use, in pixels, when snap-to-grid is enabled.
 	 *
 	 * @param snapVGap snapVGap Default value is 20
 	 * @see com.smartgwt.client.widgets.Canvas#setSnapToGrid
 	 * @see com.smartgwt.client.widgets.Canvas#setSnapResizeToGrid
 	 * @see com.smartgwt.client.widgets.Canvas#setChildrenSnapToGrid
 	 * @see com.smartgwt.client.widgets.Canvas#setChildrenSnapResizeToGrid
 	 * @see com.smartgwt.client.docs.Dragdrop Dragdrop overview and related methods
 	 */
 	public void setSnapVGap(int snapVGap) {
 		setAttribute("snapVGap", snapVGap, true);
 	}
 
 	/**
 	 * The vertical grid size to use, in pixels, when snap-to-grid is enabled.
 	 *
 	 *
 	 * @return int
 	 * @see com.smartgwt.client.widgets.Canvas#getSnapToGrid
 	 * @see com.smartgwt.client.widgets.Canvas#getSnapResizeToGrid
 	 * @see com.smartgwt.client.widgets.Canvas#getChildrenSnapToGrid
 	 * @see com.smartgwt.client.widgets.Canvas#getChildrenSnapResizeToGrid
 	 * @see com.smartgwt.client.docs.Dragdrop Dragdrop overview and related methods
 	 */
 	public int getSnapVGap() {
 		return getAttributeAsInt("snapVGap");
 	}
 
 	/**
 	 * The CSS class applied to this widget as a whole.
 	 *
 	 * <br><br>If this method is called after the component has been drawn/initialized:
 	 * Sets the CSS class for this widget
 	 *
 	 * @param styleName new CSS style name. Default value is "normal"
 	 * @see com.smartgwt.client.docs.Appearance Appearance overview and related methods
 	 * @see <a href="http://www.smartclient.com/smartgwt/showcase/#effects_lf_css" target="examples">CSS styles Example</a>
 	 */
 	public void setStyleName(String styleName) {
 		setAttribute("styleName", styleName, true);
 	}
 
 	/**
 	 * The CSS class applied to this widget as a whole.
 	 *
 	 *
 	 * @return String
 	 * @see com.smartgwt.client.docs.Appearance Appearance overview and related methods
 	 * @see <a href="http://www.smartclient.com/smartgwt/showcase/#effects_lf_css" target="examples">CSS styles Example</a>
 	 */
 	public String getStyleName() {
 		return getAttributeAsString("styleName");
 	}
 
 	/**
 	 * If specified this governs the tabIndex of the widget in the page's tab order. Note that by default Smart GWT
 	 * auto-assigns tab-indices, ensuring focusable widgets are reachable by tabbing in the order in which they are drawn on
 	 * the page. <code>canvas.tabIndex</code> cannot be set to greater than  {@link
 	 * com.smartgwt.client.widgets.Canvas#TAB_INDEX_FLOOR TAB_INDEX_FLOOR} - as we reserve the values above this range for
 	 * auto-assigned tab-indices.
 	 *
 	 * <br><br>If this method is called after the component has been drawn/initialized:
 	 * Assign an explicit tabIndex to this widget.
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param tabIndex New tabIndex for this widget. Must be less than                           {@link
 	 * com.smartgwt.client.widgets.Canvas#TAB_INDEX_FLOOR TAB_INDEX_FLOOR} to avoid interfering with auto-assigned             
 	 *             tab indices on the page.. Default value is null
 	 * @see com.smartgwt.client.docs.Focus Focus overview and related methods
 	 */
 	public void setTabIndex(Integer tabIndex) {
 		setAttribute("tabIndex", tabIndex, true);
 	}
 
 	/**
 	 * If specified this governs the tabIndex of the widget in the page's tab order. Note that by default Smart GWT
 	 * auto-assigns tab-indices, ensuring focusable widgets are reachable by tabbing in the order in which they are drawn on
 	 * the page. <code>canvas.tabIndex</code> cannot be set to greater than  {@link
 	 * com.smartgwt.client.widgets.Canvas#TAB_INDEX_FLOOR TAB_INDEX_FLOOR} - as we reserve the values above this range for
 	 * auto-assigned tab-indices.
 	 *
 	 *
 	 * @return Integer
 	 * @see com.smartgwt.client.docs.Focus Focus overview and related methods
 	 */
 	public Integer getTabIndex() {
 		return getAttributeAsInt("tabIndex");
 	}
 
 	/**
 	 * Configures where the Opacity filter is used for IE6-8. <P> With the default of null, opacity filters are used unless
 	 * {@link com.smartgwt.client.widgets.Canvas#neverUseFilters neverUseFilters} has been set.  When set explicitly to true,
 	 * opacity filters are used even if <code>neverUseFilters</code> is true. <P> See {@link
 	 * com.smartgwt.client.docs.IEFilters} for background.
 	 *
 	 * @param useOpacityFilter useOpacityFilter Default value is null
 	 * @throws IllegalStateException this property cannot be changed after the component has been created
 	 * @see com.smartgwt.client.docs.IEFilters IEFilters overview and related methods
 	 */
 	public void setUseOpacityFilter(Boolean useOpacityFilter) throws IllegalStateException {
 		setAttribute("useOpacityFilter", useOpacityFilter, false);
 	}
 
 	/**
 	 * Configures where the Opacity filter is used for IE6-8. <P> With the default of null, opacity filters are used unless
 	 * {@link com.smartgwt.client.widgets.Canvas#neverUseFilters neverUseFilters} has been set.  When set explicitly to true,
 	 * opacity filters are used even if <code>neverUseFilters</code> is true. <P> See {@link
 	 * com.smartgwt.client.docs.IEFilters} for background.
 	 *
 	 *
 	 * @return Boolean
 	 * @see com.smartgwt.client.docs.IEFilters IEFilters overview and related methods
 	 */
 	public Boolean getUseOpacityFilter() {
 		return getAttributeAsBoolean("useOpacityFilter");
 	}
 
 	/**
 	 * Controls widget visibility when the widget is initialized. See Visibility type for      details.
 	 *
 	 * <br><br>If this method is called after the component has been drawn/initialized:
 	 * Sets this widget's visibility to "inherit", so that it becomes visible if all it's parents are visible or it has no parents. <P> If the widget has not yet been drawn (and doesn't have a parent or master), this method calls the draw method as well.
 	 *
 	 * @param visibility visibility Default value is Canvas.INHERIT
 	 * @see com.smartgwt.client.docs.Appearance Appearance overview and related methods
 	 */
 	public void setVisibility(Visibility visibility) {
 		setAttribute("visibility", visibility.getValue(), true);
 	}
 
 	/**
 	 * Controls widget visibility when the widget is initialized. See Visibility type for      details.
 	 *
 	 *
 	 * @return Returns true if the widget is visible, taking all parents into account, so that a widget which is not hidden might still
 	 * report itself as not visible if it is within a hidden parent. <P> NOTE: Undrawn widgets will report themselves as
 	 * visible if they would be visible if drawn.
 	 * @see com.smartgwt.client.docs.Appearance Appearance overview and related methods
 	 */
 	public Visibility getVisibility() {
 		return EnumUtil.getEnum(Visibility.values(), getAttribute("visibility"));
 	}
 
 	public void setVisible(boolean visible) {
 		setVisibility(visible ? Visibility.INHERIT : Visibility.HIDDEN);
 	}
 
 	@Override
 	public HandlerRegistration addClickHandler(final ClickHandler handler) {
 		clickHandlers.add(handler);
 		return new HandlerRegistration() {
 			@Override
 			public void removeHandler() {
 				clickHandlers.remove(handler);
 			}
 		};
 	}
 
 	/**
 	 * Size for this component's vertical dimension. <P> Can be a number of pixels, or a percentage like "50%". See
 	 * documentation for {@link com.smartgwt.client.widgets.Canvas#getWidth width} for details on who percentage values
 	 * are resolved actual size. <P> Note that if {@link com.smartgwt.client.widgets.Canvas#getOverflow overflow} is set
 	 * to "visible", this size is a minimum, and the component may overflow to show all content and/or children. <P> If
 	 * trying to establish a default height for a custom component, set {@link com.smartgwt.client.widgets.Canvas#getDefaultHeight
 	 * defaultHeight} instead. Resizes the widget vertically to the specified height (moves the bottom side of the
 	 * widget). The height parameter can be expressed as a percentage of viewport size or as the number of pixels. <P>
 	 * NOTE: if you're setting multiple coordinates, use resizeTo() or setRect() instead
 	 *
 	 * @param height new height. Default value is null
 	 */
 	public void setHeight(int height) {
 		setAttribute("height", height, true);
 	}
 
 	/**
 	 * Convenience method that sets the height to 100%.
 	 */
 	public void setHeight100() {
 		setHeight("100%");
 	}
 
 	/**
 	 * Convenience method that sets the width to 100%.
 	 */
 	public void setWidth100() {
 		setWidth("100%");
 	}
 
 	public void setHeight(String height) {
 		setAttribute("height", height, true);
 	}
 
 	public String getHeightAsString() {
 		return getAttribute("height");
 	}
 
 	/**
 	 * Synonym for {@link #setPrompt(String)}
 	 *
 	 * @param title the title
 	 */
 	public void setTitle(String title) {
 		setPrompt(title);
 	}
 
 	/**
 	 * Synonym for {@link #getPrompt()}
 	 *
 	 * @return the title
 	 */
 	public String getTitle() {
 		return getPrompt();
 	}
 
 	/**
 	 * Synonym for {@link #setPrompt(String)}
 	 *
 	 * @param tooltip the tooltip
 	 */
 	public void setTooltip(String tooltip) {
 		setPrompt(tooltip);
 	}
 
 	/**
 	 * Synonym for {@link #getPrompt()}
 	 *
 	 * @return  the tooltip
 	 */
 	public String getTooltip() {
 		return getPrompt();
 	}
 
 	/**
 	 * Size for this component's horizontal dimension. <P> Can be a number of pixels, or a percentage like "50%".
 	 * Percentage sizes are resolved to pixel values as follows: <UL> <LI>If a canvas has a specified {@link
 	 * com.smartgwt.client.widgets.Canvas#getPercentSource percentSource}, sizing will be     a percentage of the size
 	 * of that widget (see also {@link com.smartgwt.client.widgets.Canvas#getPercentBox percentBox}).</LI>
 	 * <LI>Otherwise, if a canvas has a {@link com.smartgwt.client.widgets.Canvas#getMasterElement masterElement}, and
 	 *   {@link com.smartgwt.client.widgets.Canvas#getSnapTo snapTo} is set for the widget, sizing will be a percentage
 	 * of     the size of that widget (see also {@link com.smartgwt.client.widgets.Canvas#getPercentBox
 	 * percentBox}).</LI> <LI>Otherwise if this is a child of some other canvas, percentages will be based on the
 	 * inner size of the {@link com.smartgwt.client.widgets.Canvas#getParentElement parentElement}'s viewport.</LI>
 	 * <LI>Otherwise, for top level widgets, sizing is calculated as a percentage of page size.</LI> </UL> <P> {@link
 	 * com.smartgwt.client.widgets.layout.Layout} may specially interpret percentage sizes on their children, and also
 	 * allow "*" as a size. <P> Note that if {@link com.smartgwt.client.widgets.Canvas#getOverflow overflow} is set to
 	 * "visible", this size is a minimum, and the component may overflow to show all content and/or children. <P> If
 	 * trying to establish a default width for a custom component, set {@link com.smartgwt.client.widgets.Canvas#getDefaultWidth
 	 * defaultWidth} instead. Resizes the widget horizontally to the specified width (moves the right side of the
 	 * widget). The width parameter can be expressed as a percentage of viewport size or as the number of pixels. <P>
 	 * NOTE: if you're setting multiple coordinates, use resizeTo() or setRect() instead
 	 *
 	 * @param width new width. Default value is null
 	 */
 	public void setWidth(int width) {
 		setAttribute("width", width, true);
 	}
 
 	/**
 	 * Size for this component's horizontal dimension. <P> Can be a number of pixels, or a percentage like "50%".
 	 * Percentage sizes are resolved to pixel values as follows: <UL> <LI>If a canvas has a specified {@link
 	 * com.smartgwt.client.widgets.Canvas#getPercentSource percentSource}, sizing will be     a percentage of the size
 	 * of that widget (see also {@link com.smartgwt.client.widgets.Canvas#getPercentBox percentBox}).</LI>
 	 * <LI>Otherwise, if a canvas has a {@link com.smartgwt.client.widgets.Canvas#getMasterElement masterElement}, and
 	 *   {@link com.smartgwt.client.widgets.Canvas#getSnapTo snapTo} is set for the widget, sizing will be a percentage
 	 * of     the size of that widget (see also {@link com.smartgwt.client.widgets.Canvas#getPercentBox
 	 * percentBox}).</LI> <LI>Otherwise if this is a child of some other canvas, percentages will be based on the
 	 * inner size of the {@link com.smartgwt.client.widgets.Canvas#getParentElement parentElement}'s viewport.</LI>
 	 * <LI>Otherwise, for top level widgets, sizing is calculated as a percentage of page size.</LI> </UL> <P> {@link
 	 * com.smartgwt.client.widgets.layout.Layout} may specially interpret percentage sizes on their children, and also
 	 * allow "*" as a size. <P> Note that if {@link com.smartgwt.client.widgets.Canvas#getOverflow overflow} is set to
 	 * "visible", this size is a minimum, and the component may overflow to show all content and/or children. <P> If
 	 * trying to establish a default width for a custom component, set {@link com.smartgwt.client.widgets.Canvas#getDefaultWidth
 	 * defaultWidth} instead. Resizes the widget horizontally to the specified width (moves the right side of the
 	 * widget). The width parameter can be expressed as a percentage of viewport size or as the number of pixels. <P>
 	 * NOTE: if you're setting multiple coordinates, use resizeTo() or setRect() instead
 	 *
 	 * @param width new width. Default value is null
 	 */
 	public void setWidth(String width) {
 		setAttribute("width", width, true);
 	}
 
 	public String getWidthAsString() {
 		return getAttribute("width");
 	}
 
 	/**
 	 * Number of pixels the left side of the widget is offset to the right from its default drawing context (either its
 	 * parent's topleft corner, or the document flow, depending on the value of the {@link
 	 * com.smartgwt.client.widgets.Canvas#getPosition position} property). <P> Can also be set as a percentage, specified as a
 	 * String ending in '%', eg, "50%".  In this case the top coordinate is considered as a percentage of the specified width
 	 * of the {@link com.smartgwt.client.widgets.Canvas#getParentElement 'parent'}.
 	 * Set the left coordinate of this object, relative to its enclosing context, in pixels.   NOTE: if you're setting multiple coordinates, use setRect(), moveTo() or resizeTo()          instead
 	 *
 	 * @param left new left coordinate. Default value is 0
 	 */
 	public void setLeft(int left) {
 		setAttribute("left", left, true);
 	}
 
 	/**
 	 * Number of pixels the left side of the widget is offset to the right from its default drawing context (either its
 	 * parent's topleft corner, or the document flow, depending on the value of the {@link
 	 * com.smartgwt.client.widgets.Canvas#getPosition position} property). <P> Can also be set as a percentage, specified as a
 	 * String ending in '%', eg, "50%".  In this case the top coordinate is considered as a percentage of the specified width
 	 * of the {@link com.smartgwt.client.widgets.Canvas#getParentElement 'parent'}.
 	 *
 	 *
 	 * @return Return the left coordinate of this object, relative to its enclosing context, in pixels.
 	 */
 	public int getLeft() {
 		return getAttributeAsInt("left");
 	}
 
 	/**
 	 * Number of pixels the left side of the widget is offset to the right from its default drawing context (either its
 	 * parent's topleft corner, or the document flow, depending on the value of the {@link
 	 * com.smartgwt.client.widgets.Canvas#getPosition position} property). <P> Can also be set as a percentage, specified as a
 	 * String ending in '%', eg, "50%".  In this case the top coordinate is considered as a percentage of the specified width
 	 * of the {@link com.smartgwt.client.widgets.Canvas#getParentElement 'parent'}.
 	 * Set the left coordinate of this object, relative to its enclosing context, in pixels.   NOTE: if you're setting multiple coordinates, use setRect(), moveTo() or resizeTo()          instead
 	 *
 	 * @param left new left coordinate. Default value is 0
 	 */
 	public void setLeft(String left) {
 		setAttribute("left", left, true);
 	}
 
 	/**
 	 * Number of pixels the left side of the widget is offset to the right from its default drawing context (either its
 	 * parent's topleft corner, or the document flow, depending on the value of the {@link
 	 * com.smartgwt.client.widgets.Canvas#getPosition position} property). <P> Can also be set as a percentage, specified as a
 	 * String ending in '%', eg, "50%".  In this case the top coordinate is considered as a percentage of the specified width
 	 * of the {@link com.smartgwt.client.widgets.Canvas#getParentElement 'parent'}.
 	 *
 	 *
 	 * @return Return the left coordinate of this object, relative to its enclosing context, in pixels.
 	 */
 	public String getLeftAsString() {
 		return getAttributeAsString("left");
 	}
 
 	/**
 	 * Number of pixels the top of the widget is offset down from its default drawing context (either its parent's top-left
 	 * corner, or the document flow, depending on the value of the {@link com.smartgwt.client.widgets.Canvas#getPosition
 	 * position} property). <P> Can also be set as a percentage, specified as a String ending in '%', eg, "50%".  In this case
 	 * the top coordinate is considered as a percentage of the specified height of the {@link
 	 * com.smartgwt.client.widgets.Canvas#getParentElement 'parent'}.
 	 * Set the top coordinate of this object, relative to its enclosing context, in pixels. <P> NOTE: if you're setting multiple coordinates, use setRect() or moveTo() instead
 	 *
 	 * @param top new top coordinate. Default value is 0
 	 */
 	public void setTop(int top) {
 		setAttribute("top", top, true);
 	}
 
 	/**
 	 * If a Canvas is dropped onto a {@link com.smartgwt.client.widgets.cube.CubeGrid}, and it has a {@link #setFacetId(String) facetId}
 	 * property specified then the Cube treats this as adding that facetId at the drop location.
 	 *
 	 * @return the facet id
 	 */
 	public String getFacetId() {
 		return getAttribute("facetId");
 	}
 
 	/**
 	 * If a Canvas is dropped onto a {@link com.smartgwt.client.widgets.cube.CubeGrid}, and it has a facetId property specified then
 	 * the Cube treats this as adding that facetId at the drop location.
 	 *
 	 * @param facetId the facet id
 	 */
 	public void setFacetId(String facetId) {
 		setAttribute("facetId", facetId, true);
 	}
 
 	/**
 	 * Number of pixels the top of the widget is offset down from its default drawing context (either its parent's top-left
 	 * corner, or the document flow, depending on the value of the {@link com.smartgwt.client.widgets.Canvas#getPosition
 	 * position} property). <P> Can also be set as a percentage, specified as a String ending in '%', eg, "50%".  In this case
 	 * the top coordinate is considered as a percentage of the specified height of the {@link
 	 * com.smartgwt.client.widgets.Canvas#getParentElement 'parent'}.
 	 *
 	 *
 	 * @return Return the top coordinate of this object, relative to its enclosing context, in pixels.
 	 */
 	public int getTop() {
 		return getAttributeAsInt("top");
 	}
 
 	/**
 	 * Number of pixels the top of the widget is offset down from its default drawing context (either its parent's top-left
 	 * corner, or the document flow, depending on the value of the {@link com.smartgwt.client.widgets.Canvas#getPosition
 	 * position} property). <P> Can also be set as a percentage, specified as a String ending in '%', eg, "50%".  In this case
 	 * the top coordinate is considered as a percentage of the specified height of the {@link
 	 * com.smartgwt.client.widgets.Canvas#getParentElement 'parent'}.
 	 * Set the top coordinate of this object, relative to its enclosing context, in pixels. <P> NOTE: if you're setting multiple coordinates, use setRect() or moveTo() instead
 	 *
 	 * @param top new top coordinate. Default value is 0
 	 */
 	public void setTop(String top) {
 		setAttribute("top", top, true);
 	}
 
 	/**
 	 * Number of pixels the top of the widget is offset down from its default drawing context (either its parent's top-left
 	 * corner, or the document flow, depending on the value of the {@link com.smartgwt.client.widgets.Canvas#getPosition
 	 * position} property). <P> Can also be set as a percentage, specified as a String ending in '%', eg, "50%".  In this case
 	 * the top coordinate is considered as a percentage of the specified height of the {@link
 	 * com.smartgwt.client.widgets.Canvas#getParentElement 'parent'}.
 	 *
 	 *
 	 * @return Return the top coordinate of this object, relative to its enclosing context, in pixels.
 	 */
 	public String getTopAsString() {
 		return getAttributeAsString("top");
 	}
 
 	public void setAlign(Alignment align) {
 		setAttribute("align", align.getValue(), true);
 	}
 
 	/**
 	 * Default animation effect to use if {@link Canvas#animateShow()} is called without an explicit effect parameter.
 	 *
 	 * @param animateShowEffect the animate show effect. Default is "wipe"
 	 */
 	public void setAnimateShowEffect(AnimationEffect animateShowEffect) {
 		setAttribute("animateShowEffect", animateShowEffect, true);
 	}
 
 	/**
 	 * Default animation effect to use if {@link Canvas#animateShow()} is called without an explicit effect parameter.
 	 *
 	 * @return animation effect. Default value is "wipe"
 	 */
 	public AnimationEffect getAnimateShowEffect() {
 		return EnumUtil.getEnum(AnimationEffect.values(), getAttribute("animateShowEffect"));
 	}
 
 	/**
 	 * If set to true, the widget will be disabled. A widget is only considered enabled       if it is individually enabled and
 	 * all parents above it in the containment hierarchy       are enabled. This allows you to enable or disable all components
 	 * of a complex       nested widget by enabling or disabling the top-level parent only.
 	 * set the disabled state of this object
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param disabled new disabled state of this object - pass <code>true</code> to disable the widget. Default value is false
 	 */
 	public void setDisabled(boolean disabled) {
		setAttribute("b$disabled", disabled);
		requestRepaint();
 	}
 
 	/**
 	 * If set to true, the widget will be disabled. A widget is only considered enabled       if it is individually enabled and
 	 * all parents above it in the containment hierarchy       are enabled. This allows you to enable or disable all components
 	 * of a complex       nested widget by enabling or disabling the top-level parent only.
 	 *
 	 *
 	 * @return Is this canvas disabled? Note that the disabled state is inherited - this method will return true if this widget, or any
 	 * of its ancestors are marked disabled.
 	 */
 	public boolean getDisabled() {
 		Boolean disabled = getAttributeAsBoolean("b$disabled");
 		return disabled == null ? false : disabled;
 	}
 
 	@Override
 	public boolean isEnabled() {
 		return !getDisabled();
 	}
 
 	@Override
 	public void setEnabled(boolean enabled) {
 		setDisabled(!enabled);
 	}
 
 	/**
 	 * Offset of the shadow.  Defaults to half of <code>shadowDepth</code> if unset. <P> Because of the blurred edges, a
 	 * shadow is larger than the originating component by 2xsoftness.  An <code>shadowOffset</code> of 0 means that the
 	 * shadow will extend around the originating component equally in all directions. <p><b>Note : </b> This is an
 	 * advanced setting</p>
 	 *
 	 * @param shadowOffset shadowOffset Default value is null
 	 */
 	public void setShadowOffset(Integer shadowOffset) {
 		setAttribute("shadowOffset", shadowOffset, true);
 	}
 
 	/**
 	 * Offset of the shadow.  Defaults to half of <code>shadowDepth</code> if unset. <P> Because of the blurred edges, a
 	 * shadow is larger than the originating component by 2xsoftness.  An <code>shadowOffset</code> of 0 means that the
 	 * shadow will extend around the originating component equally in all directions.
 	 *
 	 * @return Integer
 	 */
 	public Integer getShadowOffset() {
 		return getAttributeAsInt("shadowOffset");
 	}
 
 	/**
 	 * Softness, or degree of blurring, of the shadow. <P> A shadow with <code>softness:x</code> is 2x pixels larger in
 	 * each direction than the element throwing the shadow, and the media for each edge should be x pixels wide/tall.
 	 * <P> Defaults to <code>shadowDepth</code> if unset. <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param shadowSoftness shadowSoftness Default value is null
 	 */
 	public void setShadowSoftness(Integer shadowSoftness) {
 		setAttribute("shadowSoftness", shadowSoftness, true);
 	}
 
 	/**
 	 * Softness, or degree of blurring, of the shadow. <P> A shadow with <code>softness:x</code> is 2x pixels larger in
 	 * each direction than the element throwing the shadow, and the media for each edge should be x pixels wide/tall.
 	 * <P> Defaults to <code>shadowDepth</code> if unset.
 	 *
 	 * @return Integer
 	 */
 	public Integer getShadowSoftness() {
 		return getAttributeAsInt("shadowSoftness");
 	}
 
 	/**
 	 * Set the groupTitle.
 	 *
 	 * @param groupTitle the groupTitle
 	 */
 	public void setGroupTitle(String groupTitle) {
 		setAttribute("groupTitle", groupTitle, true);
 	}
 
 	/**
 	 * Return the groupTitle.
 	 *
 	 * @return the groupTitle
 	 */
 	public String getGroupTitle() {
 		return getAttribute("groupTitle");
 	}
 
 	/**
 	 * Set the isGroup.
 	 *
 	 * @param isGroup the isGroup
 	 */
 	public void setIsGroup(Boolean isGroup) {
 		setAttribute("isGroup", isGroup, true);
 	}
 
 	/**
 	 * Return the isGroup.
 	 *
 	 * @return the isGroup
 	 */
 	public Boolean getIsGroup() {
 		return getAttributeAsBoolean("isGroup");
 	}
 
 	public void setSmoothFade(Boolean smoothFade) throws IllegalStateException {
 		setAttribute("smoothFade", smoothFade, false);
 	}
 
 	public void setKeepInParentRect(Boolean keepInParentRect) {
 		setAttribute("keepInParentRect", keepInParentRect, true);
 	}
 
 	public void setPrefix(String prefix) {
 		setAttribute("prefix", prefix, true);
 	}
 
 	public String getPrefix() {
 		return getAttribute("prefix");
 	}
 
 	/**
 	 * Allows resizing in certain edges or corners. The default value of null indicates that the widget is resizable from any corner
 	 * or edge. To restrict resizing to only certain corners, set resizeFrom to an array of any of the values listed:<br>
 	 *
 	 * T top edge<br>
 	 * B bottom edge<br>
 	 * L left edge<br>
 	 * R right edge<br>
 	 * TL top-left corner<br>
 	 * TR top-right corner<br>
 	 * BL bottom-left corner<br>
 	 * BR bottom-right corner<br><br>
 	 * E.g. setting this property to a value of ["R","TR","BR"] would restrict resizing to the right edge, top-right corner and bottom-right corner only
 	 *
 	 * @param resizeFrom resizeFrom values
 	 */
 	public void setResizeFrom(String... resizeFrom) {
 		setAttribute("resizeFrom", resizeFrom, true);
 	}
 
 	/**
 	 * When this Canvas is included as a member in a Layout, layoutAlign controls alignment on the&#010 breadth axis of
 	 * the layout.  Default is "left" for a VLayout, "top" for an HLayout.
 	 *
 	 * @param layoutAlign layoutAlign Default valaue is null
 	 * @throws IllegalStateException this property cannot be changed after the component has been rendered
 	 */
 	public void setLayoutAlign(Alignment layoutAlign) throws IllegalStateException {
 		setAttribute("layoutAlign", layoutAlign.getValue(), false);
 	}
 
 	/**
 	 * When this Canvas is included as a member in a Layout, layoutAlign controls alignment on the&#010 breadth axis of
 	 * the layout.  Default is "left" for a VLayout, "top" for an HLayout.
 	 *
 	 * @param layoutAlign layoutAlign Default valaue is null
 	 * @throws IllegalStateException this property cannot be changed after the component has been rendered
 	 */
 	public void setLayoutAlign(VerticalAlignment layoutAlign) throws IllegalStateException {
 		setAttribute("layoutAlign", layoutAlign.getValue(), false);
 	}
 
 	/**
 	 * The "type" of thing(s) that can be dropped on this widget specified as a string or an array of strings
 	 * (indicating multiple types). Leave this with the value null to indicate that this widget can accept anything
 	 * dropped on it from the page. <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param dropTypes dropTypes Default value is Canvas.ANYTHING
 	 */
 	public void setDropTypes(String... dropTypes) {
 		setAttribute("dropTypes", dropTypes, true);
 	}
 
 	/**
 	 * Set the minimum value for height.
 	 * <p>
 	 * For use with setOverflow(Overflow.VISIBLE), where widgets render only as large as their contents
 	 * and/or children, and height and width settings act as a minimum size.
 	 */
 	public void setAutoHeight() {
 		setHeight(1);
 	}
 
 	/**
 	 * Set the minimum value for width.
 	 * <p>
 	 * For use with setOverflow(Overflow.VISIBLE), where widgets render only as large as their contents
 	 * and/or children, and height and width settings act as a minimum size.
 	 */
 	public void setAutoWidth() {
 		setWidth(1);
 	}
 
 	public Boolean isDrawn() {
 		return isCreated();
 	}
 
 	@Override
 	public void paintContent(PaintTarget target) throws PaintException {
 		super.paintContent(target);
 
 		if (!clickHandlers.isEmpty()) {
 			target.addAttribute("*hasClickHandlers", true);
 		}
 	}
 
 	@Override
 	public void changeVariables(Object source, Map<String, Object> variables) {
 		super.changeVariables(source, variables);
 
 		if (variables.containsKey("clickEvent")) {
 			final ClickEvent event = new ClickEvent();
 
 			for (ClickHandler handler : clickHandlers) {
 				handler.onClick(event);
 			}
 		}
 	}
 }
