 package com.evervoid.client.ui;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import com.evervoid.client.EVFrameManager;
 import com.evervoid.client.EVInputManager;
 import com.evervoid.client.EverVoidClient;
 import com.evervoid.client.EverVoidClient.NodeType;
 import com.evervoid.client.KeyboardKey;
 import com.evervoid.client.graphics.EverNode;
 import com.evervoid.client.graphics.FrameUpdate;
 import com.evervoid.client.interfaces.EVFrameObserver;
 import com.evervoid.client.views.Bounds;
 import com.jme3.font.LineWrapMode;
 import com.jme3.math.ColorRGBA;
 
 /**
  * A text box that the user can type in.
  */
 public class TextInputControl extends BorderedControl implements UIInputListener, EVFrameObserver
 {
 	private static final float sCursorBlinkRate = 0.4f;
 	private static ColorRGBA sInputTextColor = new ColorRGBA(0.9f, 0.9f, 0.9f, 1f);
 	boolean aCursorVisible = true;
 	private float aLastCursorChange = 0f;
 	private final int aMaxLength;
 	private final Set<TextInputListener> aObservers = new HashSet<TextInputListener>();
 	private String aText, aVisibleText;
 	private final StaticTextControl aTextBox, voidBox;
 
 	public TextInputControl(final int maxLength)
 	{
 		this("", maxLength);
 	}
 
 	public TextInputControl(final String text, final int maxLength)
 	{
 		super("ui/textbox_left.png", new BackgroundedUIControl(BoxDirection.HORIZONTAL, "ui/textbox_middle.png"),
 				"ui/textbox_right.png");
 		aMaxLength = maxLength;
 		aText = text;
 		aTextBox = new StaticTextControl(text, sInputTextColor, ButtonControl.sButtonFont, ButtonControl.sButtonFontSize);
 		addUI(new VerticalCenteredControl(aTextBox), 1);
 		voidBox = new StaticTextControl(text, sInputTextColor, ButtonControl.sButtonFont, ButtonControl.sButtonFontSize);
 		voidBox.setLineWrapMode(LineWrapMode.Character);
 		EverVoidClient.addRootNode(NodeType.TWODIMENSION, voidBox);
 		voidBox.getNewTransform().translate(EverVoidClient.getWindowDimension().getWidthFloat(), 0f);
 	}
 
 	public void addTextInputListener(final TextInputListener listener)
 	{
 		aObservers.add(listener);
 	}
 
 	@Override
 	public void frame(final FrameUpdate f)
 	{
 		aLastCursorChange += f.aTpf;
 		if (aLastCursorChange >= sCursorBlinkRate) {
 			aLastCursorChange = 0;
 			aCursorVisible = !aCursorVisible;
 			updateText();
 		}
 	}
 
 	@Override
 	public Collection<EverNode> getEffectiveChildren()
 	{
 		final Collection<EverNode> normalChildren = super.getEffectiveChildren();
 		final List<EverNode> withExtra = new ArrayList<EverNode>(normalChildren.size() + 1);
 		withExtra.addAll(normalChildren);
 		if (voidBox != null) {
 			// since voidBox is not a direct children, we also need to consider it.
 			withExtra.add(voidBox);
 		}
 		return withExtra;
 	}
 
 	public String getText()
 	{
 		return aText;
 	}
 
 	private String getVisibleText(final String pText)
 	{
 		final int textLength = pText.length();
 		String visibleText = "";
 		int invisibleLength = 0;
 		voidBox.setBounds(new Bounds(0, 0, aTextBox.getWidth(), aTextBox.getHeight()));
 		voidBox.setText(pText);
 		while (voidBox.getLines() > 1 && invisibleLength < textLength) {
 			visibleText = pText.substring(invisibleLength);
 			voidBox.setText(visibleText);
 			invisibleLength++;
 		}
 		if (invisibleLength > 0 && invisibleLength + 1 < textLength) {
 			// Leave some space for the flashing cursor.
 			visibleText = pText.substring(invisibleLength + 1);
 			return visibleText;
 		}
 		else {
 			// The original string is entirely visible.
 			return pText;
 		}
 	}
 
 	@Override
 	public void onClick()
 	{
 		if (!isEnabled()) {
 			return;
 		}
 		aCursorVisible = true;
 		updateText();
 		EVFrameManager.register(this);
 		setFocusedNode(this);
 		for (final TextInputListener listener : aObservers) {
 			listener.onTextInputFocus(this);
 		}
 	}
 
 	@Override
 	public void onDefocus()
 	{
 		aCursorVisible = false;
 		updateText();
 		EVFrameManager.deregister(this);
 		setFocusedNode(null);
 		for (final TextInputListener listener : aObservers) {
 			listener.onTextInputDefocus(this);
 		}
 	}
 
 	@Override
 	public boolean onKeyPress(final KeyboardKey key)
 	{
 		if (!isEnabled()) {
 			return false;
 		}
 		if (key.equals(KeyboardKey.BACKSPACE)) {
 			if (!aText.isEmpty()) {
 				aText = aText.substring(0, aText.length() - 1);
 				updateText();
 			}
 		}
 		else {
 			if (aText.length() < aMaxLength) {
 				aText += key.getCharacter(EVInputManager.shiftPressed());
 				updateText();
 			}
 		}
 		for (final TextInputListener listener : aObservers) {
 			listener.onTextInputKey(this, key);
 		}
		return key.getCharacter(false).isEmpty() || key.equals(KeyboardKey.BACKSPACE) || key.equals(KeyboardKey.ENTER);
 	}
 
 	public void setText(final String text)
 	{
 		aText = text;
 		updateText();
 	}
 
 	private void updateText()
 	{
 		aVisibleText = getVisibleText(aText);
 		if (aCursorVisible) {
 			aTextBox.setText(aVisibleText + "|");
 		}
 		else {
 			aTextBox.setText(aVisibleText);
 		}
 	}
 }
