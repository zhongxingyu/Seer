 package jgame.listener;
 
 import java.awt.event.InputEvent;
 import java.awt.event.MouseEvent;
 
 import jgame.ButtonState;
 import jgame.Context;
 import jgame.GObject;
 
 /**
  * A listener that allows an object to act as a button. The button behaves
  * according to the following rules:
  * <ul>
  * <li>A button has three states: no activity, hovered, and depressed.</li>
  * <li>When the user first moves the mouse into the
  * {@linkplain GObject#getBoundingShape() bounding shape} of the object, the
  * button is considered "hovered" and the {@link #mouseOver(Context)} method is
  * invoked.</li>
  * <li>If the user then moves the mouse away from the button, the button is no
  * longer considered "hovered" and the {@link #mouseOut(Context)} method is
  * invoked.</li>
  * <li>If the button is "hovered" and the user presses a
  * {@linkplain #validButtonMask valid mouse button}, the button is considered
  * "pressed" and the {@link #mouseDown(Context)} method is invoked.</li>
  * <li>If a mouse button is released while the mouse pointer is hovering over
  * the button and the button is "pressed," the button is considered "clicked,"
  * the {@link #mouseClicked(Context)} method is invoked, and the button is set
  * back to the default buttonState.</li>
  * <li>If the mouse button is released while the button is "pressed" but the
  * mouse pointer has since been moved outside of the object's bounding shape
  * (i.e., hover, press, move away, release), the button is <em>not</em>
  * considered "clicked" and the {@link #mouseOut(Context)} method is invoked.</li>
  * </ul>
  * Unlike traditional listeners, this class's {@link #invoke(GObject, Context)}
  * method is declared {@code final} and cannot be overridden; subclasses should
  * instead override one or more of the following methods:
  * <ul>
  * <li>{@link #mouseOver(Context)}</li>
  * <li>{@link #mouseOut(Context)}</li>
  * <li>{@link #mouseDown(Context)}</li>
  * <li>{@link #mouseClicked(Context)}</li>
  * </ul>
  * 
  * @author William Chargin
  * 
  */
 public abstract class ButtonListener implements Listener {
 
 	/**
 	 * The current buttonState of this button.
 	 */
 	private ButtonState buttonState = ButtonState.NONE;
 
 	/**
 	 * The mask for the valid buttons. The default is
 	 * {@link InputEvent#BUTTON1_MASK}. If you want to allow more than one
 	 * button to be valid, use the bitwise <strong>or</strong> operator, denoted
 	 * by a single vertical bar or pipe, as follows: {@code |}&nbsp;. For
 	 * example, to allow buttons one and three to fire events, specify the mask
 	 * as:
 	 * 
 	 * <pre>
 	 * <code>MouseEvent.BUTTON1_MASK | MouseEvent.BUTTON3_MASK</code>
 	 * </pre>
 	 * 
 	 * Setting this valud to {@link MouseEvent#NOBUTTON} will effectively
 	 * disable this listener.
 	 */
 	private int validButtonMask = MouseEvent.BUTTON1_MASK;
 
 	/**
 	 * Gets the current valid button mask.
 	 * 
 	 * @return the valid button mask
 	 * @see #validButtonMask
 	 */
 	public int getValidButtonMask() {
 		return validButtonMask;
 	}
 
 	@Override
 	public final void invoke(GObject target, Context context) {
 		// Check the current buttonState.
 		switch (buttonState) {
 		case NONE:
 			// Nothing happening currently. Has this changed?
 			if (context.isMouseInScreen()
 					&& target.getAbsoluteBoundingShape().contains(
							context.getMouseAbsolute())
					&& !isValidButton(context.getMouseButtonMask())) {
 				// The user is hovering the button.
 				// The mouse is hovered over the button.
 				// Update the buttonState.
 				buttonState = ButtonState.HOVERED;
 
 				// Call the listener function.
 				mouseOver(context);
 
 			} // else nothing happened
 			break;
 
 		case HOVERED:
 			// The user is currently hovering the mouse. Has this changed?
 			// It could change to
 			// (a) the user is no longer hovering, or
 			// (b) the user has pressed the mouse button.
 			if (!context.isMouseInScreen()
 					|| !target.getAbsoluteBoundingShape().contains(
 							context.getMouseAbsolute())) {
 				// The user has moused out.
 				// Update the buttonState.
 				buttonState = ButtonState.NONE;
 
 				// Call the listener method.
 				mouseOut(context);
 			} else if (isValidButton(context.getMouseButtonMask())) {
 				// The user has pressed a button.
 				// Update the buttonState.
 				buttonState = ButtonState.PRESSED;
 
 				// Call the listener method.
 				mouseDown(context);
 			}
 			break;
 
 		case PRESSED:
 			// The user is currently pressing the button. Has this changed?
			if (!isValidButton(context.getMouseButtonMask())) {
 				// The user is no longer pressing the mouse button.
 
 				// Did the user click this button?
 				// (i.e., is the mouse still over it?)
 				if (context.isMouseInScreen()
 						&& target.getAbsoluteBoundingShape().contains(
 								context.getMouseAbsolute())) {
 					// We have a winner!
					buttonState = ButtonState.HOVERED;
 					mouseClicked(context);
 				} else {
 					// The user decided not to complete the click.
					buttonState = ButtonState.NONE;
 					mouseOut(context);
 				}
 			}
 			break;
 
 		default:
 			// We shouldn't get here; we should have covered all the cases.
 			// That's weird...
 			// well, nothing really to do.
 			// \u2654 Keep calm and carry on.
 
 			// Reset the button to clear things up, maybe.
 			buttonState = ButtonState.NONE;
 			break;
 		}
 	}
 
 	@Override
 	public boolean isValid(GObject target, Context context) {
 		// We always want to check.
 		return true;
 	}
 
 	/**
 	 * Determines whether the given button is allowed under the current
 	 * {@linkplain #validButtonMask valid button mask}.
 	 * 
 	 * @param button
 	 *            the button to check
 	 * @return {@code true} if the button is allowed, or {@code false} if it is
 	 *         not
 	 */
 	private boolean isValidButton(int button) {
 		return (button & validButtonMask) != 0;
 	}
 
 	/**
 	 * Invoked when the user releases has completed a full click on this button.
 	 * 
 	 * @param context
 	 *            the context in which the event occurred
 	 */
 	public void mouseClicked(Context context) {
 	}
 
 	/**
 	 * Invoked when the user depresses the mouse button over this object.
 	 * 
 	 * @param context
 	 *            the context in which the event occurred
 	 */
 	public void mouseDown(Context context) {
 	}
 
 	/**
 	 * Invoked when the user mouses away from this object without clicking it.
 	 * 
 	 * @param context
 	 *            the context in which the event occurred
 	 */
 	public void mouseOut(Context context) {
 	}
 
 	/**
 	 * Invoked when the user mouses over this object.
 	 * 
 	 * @param context
 	 *            the context in which the event occurred
 	 */
 	public void mouseOver(Context context) {
 	}
 
 	/**
 	 * Sets the valid button mask.
 	 * 
 	 * @param validButtonMask
 	 *            the new valid button mask
 	 * @see #validButtonMask
 	 */
 	public void setValidButtonMask(int validButtonMask) {
 		this.validButtonMask = validButtonMask;
 	}
 
 }
