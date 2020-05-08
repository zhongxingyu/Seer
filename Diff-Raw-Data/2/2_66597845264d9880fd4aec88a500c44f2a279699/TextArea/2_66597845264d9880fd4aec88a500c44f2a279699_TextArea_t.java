 package com.hexcore.cas.ui;
 
 import java.awt.event.KeyEvent;
 
 import javax.media.opengl.GL;
 
 import com.hexcore.cas.math.Vector2i;
 
 public class TextArea extends TextBox
 {	
 	private	int		rows;
 	private boolean lineNumbers;
 	
 	@Deprecated
 	public TextArea(Vector2i size)
 	{
 		super(size);
 	}
 	
 	public TextArea(int width, int rows)
 	{
 		super(width);
 		this.rows = rows;
 	}
 	
 	@Deprecated
 	public TextArea(Vector2i position, Vector2i size)
 	{
 		super(position, size);
 	}
 	
 	public TextArea(Vector2i position, int width, int rows)
 	{
 		super(position, width);
 		this.rows = rows;
 	}
 	
 	public void setLineNumbers(boolean state)
 	{
 		lineNumbers = state;
 	}
 	
 	@Override
 	public int getInnerX() 
 	{
 		int sideMargin = 0;
 		
 		if (window != null && lineNumbers) 
 			sideMargin = padding.x + window.getTheme().calculateTextWidth("1"+flowedText.getNumLines(), Text.Size.SMALL);
 		
 		return sideMargin + padding.x;
 	}
 	
 	@Override
 	public int getInnerY() 
 	{
 		return padding.y;
 	}
 	
 	@Override
 	public Vector2i	getInnerOffset() 
 	{
 		return new Vector2i(getInnerX(), getInnerY());
 	}
 		
 	@Override
 	public void	setText(String text) {this.text = text; relayout();}
 	
 	@Override
 	public void setSize(Vector2i v)
 	{
 		super.setSize(v);
 		relayout();
 	}
 	
 	@Override
 	public void relayout()
 	{
 		if (window != null) 
 		{
 			reflowText();
 			
 			if (!isSet(Widget.FILL_VERTICAL))
 			{
 				int	boxHeight = padding.y * 2 + window.getTheme().calculateTextHeight(textSize) * rows;
 				super.setHeight(boxHeight);
 			}
 		}
 	}
 	
 	@Override
 	public void render(GL gl, Vector2i position)
 	{
 		Vector2i pos = this.position.add(position);
 		
 		window.setClipping(gl, pos, size);
 		if (flowedText != null) 
 			window.getTheme().renderTextArea(gl, pos, size, flowedText, selectIndex, cursorIndex, focused, lineNumbers, cursorFlash);
 		window.resetView(gl);
 	}
 	
 	@Override
 	public boolean handleEvent(Event event, Vector2i position)
 	{
 		boolean handled = false;
 		
 		if (focused)
 		{
 			int startIndex = Math.min(cursorIndex, selectIndex);
 			int endIndex = Math.max(cursorIndex, selectIndex);
 			
 			handled = true;
 			
 			if ((event.type == Event.Type.KEY_PRESS) && !event.pressed)
 			{
 				cursorFlash = 0.0f;
 				
 				if ((event.button == KeyEvent.VK_UP) && (cursorIndex > 0))
 					cursorIndex = flowedText.getPreviousLineCursorPosition(cursorIndex);
 				else if ((event.button == KeyEvent.VK_DOWN) && (cursorIndex < text.length()))
 					cursorIndex = flowedText.getNextLineCursorPosition(cursorIndex);
 				else
 					handled = false;
 			}
			else if (event.type == Event.Type.KEY_TYPED && !event.hasModifier(Event.CTRL) && event.button == '\n')
 			{
 				text = text.substring(0, startIndex) + (char)event.button + text.substring(endIndex);
 				
 				if (startIndex != endIndex)
 					cursorIndex = startIndex + 1;
 				else
 					cursorIndex++;
 			}
 			else
 				handled = false;
 			
 			if (handled) selectIndex = cursorIndex;
 		}
 	
 		if (handled) relayout();
 		if (!handled) handled = super.handleEvent(event, position);
 				
 		return handled;
 	}
 }
