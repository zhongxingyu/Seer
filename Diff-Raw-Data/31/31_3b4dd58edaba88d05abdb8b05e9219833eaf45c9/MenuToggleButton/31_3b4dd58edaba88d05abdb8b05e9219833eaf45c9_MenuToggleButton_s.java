 package nl.tue.fingerpaint.client.gui;
 
 import nl.tue.fingerpaint.client.gui.animation.RotationAnimation;
 import nl.tue.fingerpaint.client.gui.animation.SizeAnimation;
 import nl.tue.fingerpaint.client.resources.FingerpaintResources;
 
 import com.google.gwt.dom.client.ImageElement;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.SimplePanel;
 
 /**
  * <p>This button is used to show and hide the menu panel in the application.</p>
  * 
  * @author Group Fingerpaint
  */
 public class MenuToggleButton extends Button implements ClickHandler {
 
 	/** The ID of the button element. */
 	public static final String ELEMENT_ID = "menuToggleButton";
 	
 	/** How long the sliding animation takes. */
 	public static final int DURATION = 1000;
 	
 	/** Indicate if the menu is shown or not. */
 	protected boolean shown = true;
 	/** The MenuPanel that is shown/hidden. */
 	protected SimplePanel menuPanel;
 	/** Animation to hide menu panel. */
 	protected SizeAnimation menuAnimation;
 	/** Animation to rotate the button. */
 	protected RotationAnimation buttonAnimation;
 	
 	/**
 	 * Construct a new {@link MenuToggleButton}.
 	 * 
 	 * @param menuPanelWrapper The element to be animated.
 	 */
 	public MenuToggleButton(SimplePanel menuPanelWrapper) {
 		super();
 		
 		// Set ID and load image element in button
 		getElement().setId(ELEMENT_ID);
 		ImageElement imgEl = ImageElement.as(DOM.createElement("img"));
 		imgEl.setPropertyString("src", FingerpaintResources.INSTANCE.plusImage().getSafeUri().asString());
 		getElement().appendChild(imgEl);
 		
 		// Set variables
 		this.menuPanel = menuPanelWrapper;
 		addClickHandler(this);
 		this.menuAnimation = new SizeAnimation(menuPanelWrapper.getElement(), SizeAnimation.ANIMATE_WIDTH);
 		this.buttonAnimation = new RotationAnimation(imgEl, 45);
 	}
 
 	@Override
 	public void onClick(ClickEvent event) {
 		if (shown) {
 			buttonAnimation.doRotate(DURATION, 0, true);
 			menuAnimation.doHide(DURATION);
 		} else {
 			buttonAnimation.doRotate(DURATION, 45, false);
 			menuAnimation.doShow(DURATION);
 		}
 		
 		// Actually toggle state
 		shown = !shown;
 	}
 	
 }
