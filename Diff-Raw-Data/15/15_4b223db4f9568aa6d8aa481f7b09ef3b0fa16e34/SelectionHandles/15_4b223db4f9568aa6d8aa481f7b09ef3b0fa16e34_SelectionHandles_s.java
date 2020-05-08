 package homework5.processing.graphicalobject;
 
 import homework5.processing.core.BoundaryRectangle;
 
 import java.awt.Canvas;
 import java.awt.Color;
 import java.awt.Rectangle;
 
 import processing.core.PGraphics;
 
 public class SelectionHandles extends SimpleGroup implements Selectable {
 	static final int HANDLE_SIZE = 10;
 	private boolean m_interimSelected = false;
 	private boolean m_selected = false;
 	private Rectangle m_childrenBoundaryRect = new Rectangle();
 	
 	Rectangle m_drawRect = new Rectangle();
 
 
 
 	public SelectionHandles(int x, int y, int width, int height) {
 		super(x, y, width, height);
 	}
 
 	@Override
 	public void resizeChild(GraphicalObject child) {
 		resizeToChildren();
 	}
 
 	private void setCornerRect(boolean bottom, boolean right)
 	{
 		float left = m_childrenBoundaryRect.x;
 		float top = m_childrenBoundaryRect.y;
 		if(bottom)
 		{
 			top = m_childrenBoundaryRect.y + m_childrenBoundaryRect.height - HANDLE_SIZE;
 		} 
 		
 		if(right)
 		{
 			left = m_childrenBoundaryRect.x + m_childrenBoundaryRect.width - HANDLE_SIZE;
 		} 	
 		
 		m_drawRect.x = (int)left;
 		m_drawRect.y = (int)top;
 		m_drawRect.width = HANDLE_SIZE;
 		m_drawRect.height = HANDLE_SIZE;
 	}
 	
 	@Override
 	public boolean contains(int x, int y) {
 		int relx = (int) (x - m_boundaryRect.x - m_childrenBoundaryRect.x);
 		int rely = (int) (y - m_boundaryRect.y - m_childrenBoundaryRect.y);
 		
 		return relx >= 0 && rely >= 0 && relx < m_childrenBoundaryRect.width && rely < m_childrenBoundaryRect.height;
 	}
 	
 	@Override
 	public void doDraw(PGraphics graphics) {
 		super.doDraw(graphics);
 
 		if(m_interimSelected)
 		{
 			graphics.fill(100);
 			drawCorners(graphics);
 		}
 		else if(m_selected)
 		{
 			graphics.fill(0);
 			drawCorners(graphics);
 		}
 		
 	}
 	
 	private void drawCorners(PGraphics graphics)
 	{
 		for(int i = 0; i < 4; i++)
 		{
 				setCornerRect(i / 2 == 0, i % 2 == 0);
 				graphics.rect(m_drawRect.x, m_drawRect.y, m_drawRect.width, m_drawRect.height);	
 		}
 	}
 
 	@Override
 	public void addChild(GraphicalObject child)
 			throws AlreadyHasGroupRunTimeException {
 		super.addChild(child);
 		fitToChildren();
 		resizeToChildren();
 	}
 	
 	private void fitToChildren()
 	{
 		m_childrenBoundaryRect.x = Integer.MAX_VALUE;
		m_childrenBoundaryRect.x = Integer.MAX_VALUE;
 		m_childrenBoundaryRect.width = 0;
 		m_childrenBoundaryRect.height = 0;
 		for (GraphicalObject child : getChildren()) {
 			BoundaryRectangle br = child.getBoundingBox();
 			if(br.x < m_childrenBoundaryRect.x) m_childrenBoundaryRect.x = br.x;
			if(br.width > m_childrenBoundaryRect.width) m_childrenBoundaryRect.width = br.width;
 			if(br.y < m_childrenBoundaryRect.y) m_childrenBoundaryRect.y = br.y;
			if(br.height > m_childrenBoundaryRect.height) m_childrenBoundaryRect.height = br.y + br.height;
 		}
 	}
 	
 	@Override
 	public void removeChild(GraphicalObject child) {
 		super.removeChild(child);
 		resizeToChildren();
 	}
 	
 	@Override
 	public void setInterimSelected(boolean interimSelected) 
 	{
 		m_interimSelected = interimSelected;
 		doDamage();
 	}
 
 	@Override
 	public boolean isInterimSelected() {
 		return m_interimSelected;
 	}
 
 	@Override
 	public void setSelected(boolean selected) {
 		m_selected = selected;
 		doDamage();
 	}
 
 	@Override
 	public boolean isSelected() {
 		return m_selected;
 	}
 
 }
