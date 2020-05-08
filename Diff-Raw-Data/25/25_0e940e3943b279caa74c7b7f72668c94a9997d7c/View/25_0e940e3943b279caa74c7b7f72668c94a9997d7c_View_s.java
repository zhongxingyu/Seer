 package teropa.globetrotter.client;
 
 import teropa.globetrotter.client.Grid.Tile;
 import teropa.globetrotter.client.canvas.MouseCanvas;
 import teropa.globetrotter.client.common.Point;
 import teropa.globetrotter.client.common.Size;
 import teropa.globetrotter.client.event.internal.ViewClickEvent;
 import teropa.globetrotter.client.event.internal.ViewPanEvent;
 import teropa.globetrotter.client.util.MouseHandler;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.DoubleClickEvent;
 import com.google.gwt.event.dom.client.DoubleClickHandler;
 import com.google.gwt.event.dom.client.MouseDownEvent;
 import com.google.gwt.event.dom.client.MouseMoveEvent;
 import com.google.gwt.event.dom.client.MouseOutEvent;
 import com.google.gwt.event.dom.client.MouseOverEvent;
 import com.google.gwt.event.dom.client.MouseUpEvent;
 import com.google.gwt.event.dom.client.MouseWheelEvent;
 import com.google.gwt.event.shared.HandlerManager;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.user.client.Event;
 import com.google.gwt.user.client.Event.NativePreviewEvent;
 import com.google.gwt.user.client.Event.NativePreviewHandler;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.widgetideas.graphics.client.Color;
 
 public class View extends Composite implements MouseHandler, DoubleClickHandler {
 
 	private static final int PAN_BUMPER = 50;
 	
 	private final MouseCanvas canvas = new MouseCanvas();
 	private final Map map;
 	private final HandlerManager handlers;
 	
 	private Point topLeft = new Point(0, 0);
 	
 	private boolean dragging;
 	private int xOffset;
 	private int yOffset;
 	
 	private Color backgroundColor = Color.WHITE;
 	
 	private NativePreviewHandler preventDefaults = new NativePreviewHandler() {
 		public void onPreviewNativeEvent(NativePreviewEvent event) {
 			int type = event.getTypeInt();
 			if (type == Event.ONMOUSEDOWN || type == Event.ONMOUSEMOVE) {
 				event.getNativeEvent().preventDefault();
 			}
 		}
 	};
 	private HandlerRegistration preventDefaultsRegistration;
 	
 	
 	public View(Map map, HandlerManager handlers) {
 		this.map = map;
 		this.handlers = handlers;
 		canvas.addMouseOverHandler(this);
 		canvas.addMouseOutHandler(this);
 		canvas.addMouseDownHandler(this);
 		canvas.addMouseUpHandler(this);
 		canvas.addMouseMoveHandler(this);
 		canvas.addClickHandler(this);
 		canvas.addDoubleClickHandler(this);
 		canvas.addMouseWheelHandler(this);
 		initWidget(canvas);
 		setWidth("100%");
 		setHeight("100%");
 	}
 	
 	public void draw(boolean clear) {
 		if (clear) {
 			canvas.saveContext();
 			canvas.setFillStyle(backgroundColor);
 			canvas.fillRect(topLeft.getX(), topLeft.getY(), getVisibleSize().getWidth(), getVisibleSize().getHeight());
 			canvas.restoreContext();
 		}
 		for (Layer eachLayer : map.getLayers()) {
 			eachLayer.drawOn(this);
 		}
 	}
 	
 	public Color getBackgroundColor() {
 		return backgroundColor;
 	}
 
 	public void setBackgroundColor(Color backgroundColor) {
 		this.backgroundColor = backgroundColor;
 	}
 	
 	public Size getVisibleSize() {
 		return new Size(canvas.getCoordWidth(), canvas.getCoordHeight());
 	}
 
 	public Point getTopLeft() {
 		return topLeft;
 	}
 
 	public void position(Point newCenterPoint) {
 		Point newTopLeft = toTopLeft(newCenterPoint);
 		canvas.translate(topLeft.getX() - newTopLeft.getX(), topLeft.getY() - newTopLeft.getY());
 		topLeft = newTopLeft;
 	}
 
 	@Override
 	protected void onLoad() {
 		canvas.setCoordWidth(getOffsetWidth());
 		canvas.setCoordHeight(getOffsetHeight());
 	}
 
 	public void onMouseOver(MouseOverEvent event) {
 		preventDefaultsRegistration = Event.addNativePreviewHandler(preventDefaults);
 	}
 	
 	public void onMouseOut(MouseOutEvent event) {
 		if (preventDefaultsRegistration != null) {
 			preventDefaultsRegistration.removeHandler();
 			preventDefaultsRegistration = null;
 		}
 	}
 
 	public void onMouseDown(MouseDownEvent event) {
 		dragging = true;
 		Event.setCapture(canvas.getElement());
 		xOffset = event.getX();
 		yOffset = event.getY();
 	}
 	
 	public void onMouseUp(MouseUpEvent event) {
 		dragging = false;
 		removeStyleName("moveCursor");
 		Event.releaseCapture(canvas.getElement());
 	}
 	
 	public void onMouseMove(MouseMoveEvent event) {
 		if (dragging) {
 			addStyleName("moveCursor");
 			
 			int wantedX = topLeft.getX() - (event.getX() - xOffset);
 			int wantedY = topLeft.getY() - (event.getY() - yOffset);
 			
 			moveTo(wantedX, wantedY);
 			
 			xOffset = event.getX();
 			yOffset = event.getY();
 		}
 	}
 
 	public void onMouseWheel(MouseWheelEvent event) {
 		if (event.getDeltaY() > 0) {
 			map.zoomOut();
 		} else {
 			map.zoomIn();
 		}
 	}
 	
 	public void moveTo(int newX, int newY) {
 		Size virtualSize = map.calc().getVirtualPixelSize();
 		Size visibleSize = getVisibleSize();
 		
 		int maxX = virtualSize.getWidth() - PAN_BUMPER;
 		int maxY = virtualSize.getHeight() - PAN_BUMPER;
 		int minX = PAN_BUMPER - visibleSize.getWidth();
 		int minY = PAN_BUMPER - visibleSize.getHeight();
 		
 		newX = Math.min(maxX, Math.max(minX, newX));
 		newY = Math.min(maxY, Math.max(minY, newY));
 		int xDelta = topLeft.getX() - newX;
 		int yDelta = topLeft.getY() - newY;
 
 		canvas.translate(xDelta, yDelta);
 		topLeft = new Point(newX, newY);
 
 		draw(true);
 		handlers.fireEvent(new ViewPanEvent(toCenter(topLeft)));
 	}
 	
 	public void move(int xDelta, int yDelta) {
 		int newX = topLeft.getX() + xDelta;
 		int newY = topLeft.getY() + yDelta;
 		moveTo(newX, newY);
 	}
 	
 	public void onClick(ClickEvent event) {
 		int x = topLeft.getX() + (event.getNativeEvent().getClientX() - getAbsoluteLeft());
 		int y = topLeft.getY() + (event.getNativeEvent().getClientY() - getAbsoluteTop());
 		handlers.fireEvent(new ViewClickEvent(new Point(x, y)));
 	}
 
 	public void onDoubleClick(DoubleClickEvent event) {
 		int x = topLeft.getX() + (event.getNativeEvent().getClientX() - getAbsoluteLeft());
 		int y = topLeft.getY() + (event.getNativeEvent().getClientY() - getAbsoluteTop());
 		map.zoomIn(map.calc().getLonLat(new Point(x, y)));
 	}
 	
 	public Point getCenterPoint() {
 		return toCenter(topLeft);
 	}
 	
 	public HandlerRegistration addViewPanHandler(ViewPanEvent.Handler handler) {
 		return handlers.addHandler(ViewPanEvent.TYPE, handler);
 	}
 	
 	public HandlerRegistration addViewClickHandler(ViewClickEvent.Handler handler) {
 		return handlers.addHandler(ViewClickEvent.TYPE, handler);
 	}
 	
 	private Point toTopLeft(Point center) {
 		Size visibleSize = getVisibleSize();
 		return new Point(
 				center.getX() - visibleSize.getWidth() / 2,
 				center.getY() - visibleSize.getHeight() / 2);
 	}
 
 	private Point toCenter(Point topLeft) {
 		Size visibleSize = getVisibleSize();
 		return new Point(
 				topLeft.getX() + visibleSize.getWidth() / 2,
 				topLeft.getY() + visibleSize.getHeight() / 2);
 	}
 	
 	public MouseCanvas getCanvas() {
 		return canvas;
 	}
 
 	public void tileUpdated(Tile tile, Layer fromLayer) {
		int layerIdx = 0;
		for (int i=0 ; i < map.getLayers().size() ; i++) {
			if (map.getLayers().get(i) == fromLayer) {
				layerIdx = i;
				break;
			}
		}
		for (int i = layerIdx + 1 ; i < map.getLayers().size() ; i++) {
 			map.getLayers().get(i).updateTile(tile);
 		}
 	}
 	
 }
