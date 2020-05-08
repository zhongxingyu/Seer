 package com.hexcore.cas.ui.toolkit;
 
 import java.awt.event.KeyEvent;
 
 import javax.media.opengl.GL;
 
 import com.hexcore.cas.math.Vector2i;
 
 public class TextBox extends Widget
 {    
 	protected String		text = "";
 	protected FlowedText	flowedText;
 	protected Text.Size		textSize = Text.Size.SMALL;
 	
 	protected int		cursorIndex = 0;
 	protected int		selectIndex = 0;
 	protected boolean	selecting = false;
 	protected float		cursorFlash = 0.0f;
 	protected Vector2i	textOffset;
 
 	public TextBox(int width)
 	{
 		super(new Vector2i(width, 10));
 		padding = new Vector2i(2, 2);
 		textOffset = new Vector2i(0, 0);
 	}
 
 	public TextBox(Vector2i position, Vector2i size)
 	{
 		super(position, size);
 		padding = new Vector2i(2, 2);
 		textOffset = new Vector2i(0, 0);
 	}
 	
 	public TextBox(Vector2i position, int width)
 	{
 		super(position, new Vector2i(width, 10));
 		padding = new Vector2i(2, 2);
 		textOffset = new Vector2i(0, 0);
 	}
 
 	public boolean	canGetFocus() {return true;}
 	
 	public String	getText() {return text;}
 	public void		setText(String text) 
 	{
 		this.text = text; 
 		reflowText();
 		
 		cursorIndex = cursorIndex >= text.length() ? text.length() - 1 : cursorIndex;
 		selectIndex = cursorIndex;
 	}
 	
 	public void clear() {setText("");}
 	
 	public void reflowText()
 	{
 		if (window != null) 
 			flowedText = window.getTheme().flowText(text, -1, textSize);
 	}
 	
 	@Override
 	public int getInnerX() 
 	{
 		return padding.x;
 	}
 	
 	@Override
 	public int getInnerY() 
 	{
 		return padding.y;
 	}
 	
 	@Override
 	public Vector2i	getInnerOffset() 
 	{
 		return padding;
 	}
 	
 	@Override
 	public void update(Vector2i position, float delta)
 	{
 		cursorFlash += delta;
 		while (cursorFlash > 100.0f) cursorFlash -= 100.0f;
 	}	
 	
 	@Override
 	public void render(GL gl, Vector2i position)
 	{
 		Vector2i pos = this.position.add(position);
 		
 		window.setClipping(gl, pos, size);
 		window.getTheme().renderTextBox(gl, pos, size, text, selectIndex, cursorIndex, focused, cursorFlash);
 		window.resetView(gl);
 	}
 	
 	@Override
 	public void relayout()
 	{		
 		if (window != null) 
 		{
 			reflowText();
 			
 			int	boxHeight = padding.y * 2 + window.getTheme().calculateTextHeight(textSize);
 			super.setHeight(boxHeight);
 		}
 	}
 
 	@Override
 	public boolean handleEvent(Event event, Vector2i position)
 	{
 		boolean handled = super.handleEvent(event, position);
 		
 		if (event.type == Event.Type.LOST_FOCUS)
 		{
 			selecting = false;
 		}
 		else if (event.type == Event.Type.MOUSE_MOTION)
 		{	
 			if (selecting)
 			{
 				Vector2i textPos = event.position.subtract(position).subtract(getInnerOffset()).add(textOffset);
 				int index = flowedText.getCursorIndex(window.getTheme(), textPos);
 				
 				if (index > -1) cursorIndex = index;
 				
 				handled = true;	
 			}
 		}		
 		else if (event.type == Event.Type.MOUSE_CLICK)
 		{	
 			selecting = event.pressed;
 			
 			Vector2i textPos = event.position.subtract(position).subtract(getInnerOffset()).add(textOffset);
 			int index = flowedText.getCursorIndex(window.getTheme(), textPos);
 			
 			if (event.pressed)
 			{
 				if (index > -1)
 				{
 					selectIndex = index;
 					cursorIndex = index;
 				}
 				
 				window.requestFocus(this);
 			}
 			else if (index > -1)
 				cursorIndex = index;
 			
 			handled = true;		
 		}
 		else if (focused)
 		{
 			int startIndex = Math.min(cursorIndex, selectIndex);
 			int endIndex = Math.max(cursorIndex, selectIndex);
 					
 			if (event.type == Event.Type.KEY_PRESS)
 				System.out.println("Pressed " + event.button + " " + event.pressed);
 			else if (event.type == Event.Type.KEY_TYPED)
 				System.out.println("Typed " + event.button + " " + event.pressed);
 			
 			handled = true;
 			if ((event.type == Event.Type.KEY_PRESS) && !event.pressed)
 			{				
 				if ((event.button == KeyEvent.VK_HOME) && (cursorIndex > 0))
 					cursorIndex = flowedText.getLineBeginningCursorPosition(cursorIndex);
 				else if ((event.button == KeyEvent.VK_END) && (cursorIndex < text.length()))
 					cursorIndex = flowedText.getLineEndCursorPosition(cursorIndex);	
 				else if ((event.button == KeyEvent.VK_LEFT) && (cursorIndex > 0))
 					cursorIndex--;
 				else if ((event.button == KeyEvent.VK_RIGHT) && (cursorIndex < text.length()))
 					cursorIndex++;
 				else
 					handled = false;
 				
 				if (handled && !event.hasModifier(Event.SHIFT)) selectIndex = cursorIndex;
 			}
 			else if (event.type == Event.Type.KEY_TYPED)
 			{
 				if (event.button == 22) // Paste
 				{
 					String clipboard = window.getClipboardText();
 
 					text = text.substring(0, startIndex) + clipboard + text.substring(endIndex);
 					
 					if (startIndex < endIndex)
 						cursorIndex = startIndex + clipboard.length();
 					else
 						cursorIndex += clipboard.length();
 					
 					selectIndex = cursorIndex;
 				}
 				else if (event.button == 3) // Copy
 				{
 					if (startIndex < endIndex) window.setClipboardText(text.substring(startIndex, endIndex));
 				}	
 				else if (event.button == 24) // Cut
 				{
 					if (startIndex < endIndex)
 					{
 						window.setClipboardText(text.substring(startIndex, endIndex));
 						text = text.substring(0, startIndex) + text.substring(endIndex);
 						cursorIndex = startIndex;
 					}	
 				}
 				else if ((event.button >= ' ' || event.button == '\t') && (event.button != 127))
				{
 					text = text.substring(0, startIndex) + (char)event.button + text.substring(endIndex);
 					
 					if (startIndex != endIndex)
 						cursorIndex = startIndex + 1;
 					else
 						cursorIndex++;
 					
 					selectIndex = cursorIndex;
 				}		
 				else if (event.button == '\n') // Enter
 				{
 					window.giveUpFocus(this);
 				}
 				else if (event.button == '\b') // Backspace
 				{
 					if (text.length() > 0)
 					{
 						if (startIndex != endIndex)
 						{
 							text = text.substring(0, startIndex) + text.substring(endIndex);
 							cursorIndex = startIndex;
 						}
 						else if (startIndex > 0)
 						{
 							text = text.substring(0, startIndex - 1) + text.substring(endIndex);
 							cursorIndex--;
 						}
 					}
 					
 					selectIndex = cursorIndex;
 				}
 				else if (event.button == 127) // Delete
 				{
 					if (startIndex != endIndex)
 					{
 						text = text.substring(0, startIndex) + text.substring(endIndex);
 						cursorIndex = startIndex;
 					}
 					else if (cursorIndex < text.length())
 						text = text.substring(0, cursorIndex) + text.substring(cursorIndex + 1);
 					
 					selectIndex = cursorIndex;
 				}
 				else
 					handled = false;
 				
 				Event changeEvent = new Event(Event.Type.CHANGE);
 				changeEvent.target = this;
 				window.sendWindowEvent(changeEvent);
 			}
 			else
 				handled = false;
 		}
 		
 		if (handled) cursorFlash = 0.0f;
 		if (handled) relayout();
 		return handled;
 	}
 }
