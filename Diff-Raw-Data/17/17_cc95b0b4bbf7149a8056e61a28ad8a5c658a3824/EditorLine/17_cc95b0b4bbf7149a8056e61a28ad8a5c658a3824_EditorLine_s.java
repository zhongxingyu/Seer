 package com.dacklabs.spookyaction.client.editor;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.HasClickHandlers;
 import com.google.gwt.event.dom.client.HasKeyDownHandlers;
 import com.google.gwt.event.dom.client.HasKeyPressHandlers;
 import com.google.gwt.event.dom.client.HasKeyUpHandlers;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.event.dom.client.KeyDownEvent;
 import com.google.gwt.event.dom.client.KeyDownHandler;
 import com.google.gwt.event.dom.client.KeyPressEvent;
 import com.google.gwt.event.dom.client.KeyPressHandler;
 import com.google.gwt.event.dom.client.KeyUpEvent;
 import com.google.gwt.event.dom.client.KeyUpHandler;
 import com.google.gwt.user.client.ui.HasHTML;
 import com.google.gwt.user.client.ui.IsWidget;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.inject.ImplementedBy;
 import com.google.inject.Inject;
 
 /**
  * Widget that represents a single line in the editor window.
  * 
  * @author "David Ackerman (david.w.ackerman@gmail.com)"
  */
 public class EditorLine implements IsWidget, KeyPressHandler, KeyUpHandler, KeyDownHandler, ClickHandler {
 
 	private final Display display;
 
 	@ImplementedBy(EditorLineView.class)
 	public interface Display extends IsWidget, HasKeyUpHandlers, HasKeyDownHandlers, HasKeyPressHandlers,
 	    HasClickHandlers, HasHTML {
 
 		int getCursorPos();
 
 		void setCursorPos(int pos);
 
 		void setFocus(boolean focused);
 	}
 
 	private final List<EditorEventHandler> handlers = new ArrayList<EditorEventHandler>();
 	private int lineNumber = 0;
 
 	private String rawText = "";
 
 	@Inject
 	public EditorLine(Display display) {
 		this.display = display;
 
 		display.addKeyPressHandler(this);
 		display.addKeyDownHandler(this);
 		display.addKeyUpHandler(this);
 		display.addClickHandler(this);
 	}
 
 	public void setLineNumber(int lineNumber) {
 		this.lineNumber = lineNumber;
 	}
 
 	@Override
 	public Widget asWidget() {
 		return display.asWidget();
 	}
 
 	public void unregisterHandlers() {
 		handlers.clear();
 	}
 
 	public void addLineHandler(EditorEventHandler handler) {
 		handlers.add(handler);
 	}
 
 	public void setText(String line) {
 		rawText = line;
 		int tmpCursor = display.getCursorPos();
 		display.setHTML(rawText);
		display.setCursorPos(tmpCursor);
 	}
 
 	public String getText() {
 		return rawText;
 	}
 
 	@Override
 	public void onClick(ClickEvent event) {
 		for (EditorEventHandler handler : handlers) {
 			handler.onClick(lineNumber, display.getCursorPos(), event);
 		}
 	}
 
 	@Override
 	public void onKeyUp(KeyUpEvent event) {
 		for (EditorEventHandler handler : handlers) {
 			handler.onKeyUp(lineNumber, display.getCursorPos(), event);
 		}
 	}
 
 	@Override
 	public void onKeyDown(KeyDownEvent event) {
 		for (EditorEventHandler handler : handlers) {
 			handler.onKeyDown(lineNumber, display.getCursorPos(), event);
 		}
 		if (event.getNativeKeyCode() == KeyCodes.KEY_BACKSPACE) {
 			display.setCursorPos(display.getCursorPos() - 1);
 		}
 	}
 
 	@Override
 	public void onKeyPress(KeyPressEvent event) {
 		for (EditorEventHandler handler : handlers) {
 			handler.onKeyPress(lineNumber, display.getCursorPos(), event);
 		}
 		incrementCursor();
 	}
 
 	public void setCursor(int offset) {
 		int position = offset;
 		if (position < 0) {
 			position = rawText.length() + position;
 		}
 		display.setCursorPos(clamp(position, 0, rawText.length()));
 		display.setFocus(true);
 	}
 
 	private void incrementCursor() {
 		display.setCursorPos(clamp(display.getCursorPos() + 1, 0, rawText.length()));
 	}
 
 	/**
 	 * Returns the given value, which is guaranteed not to go beyond the given bounds, inclusive.
 	 */
 	private int clamp(int value, int lowerBound, int upperBound) {
 		return Math.min(Math.max(value, lowerBound), upperBound);
 	}
 }
