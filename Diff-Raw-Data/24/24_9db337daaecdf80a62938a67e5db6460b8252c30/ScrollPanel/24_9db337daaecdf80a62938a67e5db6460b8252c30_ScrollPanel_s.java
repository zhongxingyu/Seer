 package com.eagerlogic.cubee.client.components;
 
 import com.eagerlogic.cubee.client.events.IEventListener;
 import com.eagerlogic.cubee.client.events.MouseDownEventArgs;
 import com.eagerlogic.cubee.client.events.MouseEventTypes;
 import com.eagerlogic.cubee.client.events.MouseDragEventArgs;
 import com.eagerlogic.cubee.client.events.MouseUpEventArgs;
 import com.eagerlogic.cubee.client.events.MouseWheelEventArgs;
 import com.eagerlogic.cubee.client.properties.AExpression;
 import com.eagerlogic.cubee.client.properties.BackgroundProperty;
 import com.eagerlogic.cubee.client.properties.DoubleProperty;
 import com.eagerlogic.cubee.client.properties.IChangeListener;
 import com.eagerlogic.cubee.client.properties.IntegerProperty;
 import com.eagerlogic.cubee.client.properties.Property;
 import com.eagerlogic.cubee.client.styles.Border;
 import com.eagerlogic.cubee.client.styles.Color;
 import com.eagerlogic.cubee.client.styles.ColorBackground;
 
 /**
  *
  * @author dipacs
  */
 public final class ScrollPanel extends AUserControl {
 
 	private final Property<EScrollBarPolicy> horizontalScrollBarPolicy = new Property<EScrollBarPolicy>(EScrollBarPolicy.AUTO, false, false);
 	private final Property<EScrollBarPolicy> verticalScrollBarPolicy = new Property<EScrollBarPolicy>(EScrollBarPolicy.AUTO, false, false);
 	private final BackgroundProperty scrollBarBackground = new BackgroundProperty(new ColorBackground(Color.getArgbColor(0x60000000)), true, false);
 	private final IntegerProperty scrollWidth = new IntegerProperty(0, false, true);
 	private final IntegerProperty scrollHeight = new IntegerProperty(0, false, true);
 	private final DoubleProperty horizontalScrollPosition = new DoubleProperty(0.0, false, false);
 	private final DoubleProperty verticalScrollPosition = new DoubleProperty(0.0, false, false);
 	private final AUserControl wheelPanel = new AUserControl() {
 		@Override
 		protected boolean onPointerEventFallingDown(int screenX, int screenY, int x, int y, int wheelVelocity, boolean altPressed, boolean ctrlPressed, boolean shiftPressed, boolean metaPressed, int type) {
 			if (type == MouseEventTypes.TYPE_MOUSE_WHEEL) {
 				this.onMouseWheelEvent().fireEvent(new MouseWheelEventArgs(wheelVelocity, altPressed, ctrlPressed, shiftPressed, metaPressed, this));
 				return true;
 			}
 			return false;
 		}
 	};
 	private final Panel hScrollBar = new Panel();
 	private final Panel vScrollBar = new Panel();
 	private final Panel contentPanel = new Panel();
 	private AComponent content;
 	private int vScrollStartPos = -1;
 	private double vScrollStartValue = 0;
 	private int hScrollStartPos = -1;
 	private double hScrollStartValue = 0;
 
 	public ScrollPanel() {
 		this.getChildren().add(contentPanel);
 		this.getChildren().add(vScrollBar);
 		this.getChildren().add(hScrollBar);
 		this.getChildren().add(wheelPanel);
 
 		this.widthProperty().set(200);
 		this.heightProperty().set(200);
 
 		contentPanel.translateXProperty().bind(new AExpression<Integer>() {
 			{
 				this.bind(horizontalScrollPosition);
 				this.bind(boundsWidthProperty());
 				this.bind(scrollWidth);
 			}
 
 			@Override
 			public Integer calculate() {
 				if (scrollWidth.get() <= boundsWidthProperty().get()) {
 					return 0;
 				}
 				int res = -((int) ((scrollWidth.get() - boundsWidthProperty().get()) * horizontalScrollPosition.get()));
 				if (res > 0) {
 					res = 0;
 				}
 				if (res < boundsWidthProperty().get() - scrollWidth.get()) {
 					res = scrollWidth.get() - boundsWidthProperty().get();
 				}
 				return res;
 			}
 		});
 		contentPanel.translateYProperty().bind(new AExpression<Integer>() {
 			{
 				this.bind(verticalScrollPosition);
 				this.bind(boundsHeightProperty());
 				this.bind(scrollHeight);
 			}
 
 			@Override
 			public Integer calculate() {
 				if (scrollHeight.get() <= boundsHeightProperty().get()) {
 					return 0;
 				}
 				int res = -((int) ((scrollHeight.get() - boundsHeightProperty().get()) * verticalScrollPosition.get()));
 				if (res > 0) {
 					res = 0;
 				}
 				if (res < boundsHeightProperty().get() - scrollHeight.get()) {
 					res = scrollHeight.get() - boundsHeightProperty().get();
 				}
 				return res;
 			}
 		});
 		vScrollBar.translateXProperty().bind(new AExpression<Integer>() {
 			{
 				this.bind(boundsWidthProperty());
 				this.bind(vScrollBar.boundsWidthProperty());
 			}
 
 			@Override
 			public Integer calculate() {
 				return boundsWidthProperty().get() - vScrollBar.boundsWidthProperty().get();
 			}
 		});
 		vScrollBar.translateYProperty().bind(new AExpression<Integer>() {
 			{
 				this.bind(boundsHeightProperty());
 				this.bind(vScrollBar.boundsHeightProperty());
 				this.bind(verticalScrollPosition);
 			}
 
 			@Override
 			public Integer calculate() {
 				return (int) ((boundsHeightProperty().get() - vScrollBar.boundsHeightProperty().get()) * verticalScrollPosition.get());
 			}
 		});
 		hScrollBar.translateYProperty().bind(new AExpression<Integer>() {
 			{
 				this.bind(boundsHeightProperty());
 				this.bind(hScrollBar.boundsHeightProperty());
 			}
 
 			@Override
 			public Integer calculate() {
 				return boundsHeightProperty().get() - hScrollBar.boundsHeightProperty().get();
 			}
 		});
 		hScrollBar.translateXProperty().bind(new AExpression<Integer>() {
 			{
 				this.bind(boundsWidthProperty());
 				this.bind(hScrollBar.boundsWidthProperty());
 				this.bind(horizontalScrollPosition);
 			}
 
 			@Override
 			public Integer calculate() {
 				return (int) ((boundsWidthProperty().get() - hScrollBar.boundsWidthProperty().get()) * horizontalScrollPosition.get());
 			}
 		});
 
 		vScrollBar.heightProperty().bind(new AExpression<Integer>() {
 			{
 				this.bind(scrollHeight);
 				this.bind(boundsHeightProperty());
 			}
 
 			@Override
 			public Integer calculate() {
 				if (scrollHeight.get() <= boundsHeightProperty().get()) {
 					return boundsHeightProperty().get();
 				}
 
 				int res = ((int) ((boundsHeightProperty().get().doubleValue() / scrollHeight.get()) * boundsHeightProperty().get()));
 				if (res < 20) {
 					res = 20;
 				}
 				return res;
 			}
 		});
 		hScrollBar.widthProperty().bind(new AExpression<Integer>() {
 			{
 				this.bind(scrollWidth);
 				this.bind(boundsWidthProperty());
 			}
 
 			@Override
 			public Integer calculate() {
 				if (scrollWidth.get() <= boundsWidthProperty().get()) {
 					return boundsWidthProperty().get();
 				}
 
 				int res = ((int) ((boundsWidthProperty().get().doubleValue() / scrollWidth.get()) * boundsWidthProperty().get()));
 				if (res < 20) {
 					res = 20;
 				}
 				return res;
 			}
 		});
 
 		vScrollBar.onMouseDownEvent().addListener(new IEventListener<MouseDownEventArgs>() {
 			@Override
 			public void onFired(MouseDownEventArgs args) {
 				vScrollStartPos = args.getScreenY();
 				vScrollStartValue = verticalScrollPosition.get();
 			}
 		});
 		vScrollBar.onMouseDragEvent().addListener(new IEventListener<MouseDragEventArgs>() {
 			@Override
 			public void onFired(MouseDragEventArgs args) {
 				if (vScrollStartPos < 0) {
 					return;
 				}
 				int delta = args.getScreenY() - vScrollStartPos;
 				int sw = boundsHeightProperty().get() - vScrollBar.boundsHeightProperty().get();
 				double valueDelta = delta / ((double) sw);
 				double newValue = vScrollStartValue + valueDelta;
 				if (newValue < 0) {
 					newValue = 0;
 				}
 				if (newValue > 1) {
 					newValue = 1;
 				}
 				verticalScrollPosition.set(newValue);
 			}
 		});
 		vScrollBar.onMouseUpEvent().addListener(new IEventListener<MouseUpEventArgs>() {
 			@Override
 			public void onFired(MouseUpEventArgs args) {
 				if (vScrollStartPos < 0) {
 					return;
 				}
 				int delta = args.getScreenY() - vScrollStartPos;
 				int sw = boundsHeightProperty().get() - vScrollBar.boundsHeightProperty().get();
 				double valueDelta = delta / ((double) sw);
 				double newValue = vScrollStartValue + valueDelta;
 				if (newValue < 0) {
 					newValue = 0;
 				}
 				if (newValue > 1) {
 					newValue = 1;
 				}
 				verticalScrollPosition.set(newValue);
 			}
 		});
 		hScrollBar.onMouseDownEvent().addListener(new IEventListener<MouseDownEventArgs>() {
 			@Override
 			public void onFired(MouseDownEventArgs args) {
 				hScrollStartPos = args.getScreenX();
 				hScrollStartValue = horizontalScrollPosition.get();
 			}
 		});
 		hScrollBar.onMouseDragEvent().addListener(new IEventListener<MouseDragEventArgs>() {
 			@Override
 			public void onFired(MouseDragEventArgs args) {
 				if (hScrollStartPos < 0) {
 					return;
 				}
 				int delta = args.getScreenX() - hScrollStartPos;
 				int sw = boundsWidthProperty().get() - hScrollBar.boundsWidthProperty().get();
 				double valueDelta = delta / ((double) sw);
 				double newValue = hScrollStartValue + valueDelta;
 				if (newValue < 0) {
 					newValue = 0;
 				}
 				if (newValue > 1) {
 					newValue = 1;
 				}
 				horizontalScrollPosition.set(newValue);
 			}
 		});
 		hScrollBar.onMouseUpEvent().addListener(new IEventListener<MouseUpEventArgs>() {
 			@Override
 			public void onFired(MouseUpEventArgs args) {
 				if (hScrollStartPos < 0) {
 					return;
 				}
 				int delta = args.getScreenX() - hScrollStartPos;
 				int sw = boundsWidthProperty().get() - hScrollBar.boundsWidthProperty().get();
 				double valueDelta = delta / ((double) sw);
 				double newValue = hScrollStartValue + valueDelta;
 				if (newValue < 0) {
 					newValue = 0;
 				}
 				if (newValue > 1) {
 					newValue = 1;
 				}
 				horizontalScrollPosition.set(newValue);
 			}
 		});
 		vScrollBar.visibleProperty().bind(new AExpression<Boolean>() {
 			{
 				this.bind(verticalScrollBarPolicy);
 				this.bind(boundsHeightProperty());
 				this.bind(scrollHeight);
 			}
 
 			@Override
 			public Boolean calculate() {
 				if (verticalScrollBarPolicy.get() == EScrollBarPolicy.AUTO) {
 					return scrollHeight.get() > boundsHeightProperty().get();
 				} else if (verticalScrollBarPolicy.get() == EScrollBarPolicy.HIDDEN) {
 					return false;
 				} else {
 					return true;
 				}
 			}
 		});
 		hScrollBar.visibleProperty().bind(new AExpression<Boolean>() {
 			{
 				this.bind(horizontalScrollBarPolicy);
 				this.bind(boundsWidthProperty());
 				this.bind(scrollWidth);
 			}
 
 			@Override
 			public Boolean calculate() {
 				if (horizontalScrollBarPolicy.get() == EScrollBarPolicy.AUTO) {
 					return scrollWidth.get() > boundsWidthProperty().get();
 				} else if (horizontalScrollBarPolicy.get() == EScrollBarPolicy.HIDDEN) {
 					return false;
 				} else {
 					return true;
 				}
 			}
 		});
 
 		wheelPanel.onMouseWheelEvent().addListener(new IEventListener<MouseWheelEventArgs>() {
 			@Override
 			public void onFired(MouseWheelEventArgs args) {
 				if (args.isShiftPressed()) {
 					double pixels = 12 * args.getWheelVelocity();
 					double delta = pixels / scrollWidth.get();
 					double newValue = horizontalScrollPosition.get() + delta;
 					if (newValue < 0) {
 						newValue = 0;
 					}
 					if (newValue > 1) {
 						newValue = 1;
 					}
 					horizontalScrollPosition.set(newValue);
 				} else {
 					double pixels = 12 * args.getWheelVelocity();
 					double delta = pixels / scrollHeight.get();
 					double newValue = verticalScrollPosition.get() + delta;
 					if (newValue < 0) {
 						newValue = 0;
 					}
 					if (newValue > 1) {
 						newValue = 1;
 					}
 					verticalScrollPosition.set(newValue);
 				}
 			}
 		});
 
 
 
 		hScrollBar.backgroundProperty().bind(scrollBarBackground);
 		vScrollBar.backgroundProperty().bind(scrollBarBackground);
 		vScrollBar.widthProperty().set(10);
 		hScrollBar.heightProperty().set(10);
 		hScrollBar.borderProperty().set(new Border(0, Color.TRANSPARENT, 5));
 		vScrollBar.borderProperty().set(new Border(0, Color.TRANSPARENT, 5));
 
 		scrollWidth.initReadonlyBind(contentPanel.boundsWidthProperty());
 		scrollHeight.initReadonlyBind(contentPanel.boundsHeightProperty());
 
 		wheelPanel.widthProperty().bind(this.widthProperty());
 		wheelPanel.heightProperty().bind(this.heightProperty());
 	}
 
 	public void setContent(AComponent component) {
 		this.contentPanel.getChildren().clear();
 		if (component != null) {
 			this.contentPanel.getChildren().add(component);
 		}
 		this.content = component;
 	}
 
 	public AComponent getContent() {
 		return this.content;
 	}
 
	public final Property<EScrollBarPolicy> getHorizontalScrollBarPolicy() {
 		return horizontalScrollBarPolicy;
 	}
 
	public final Property<EScrollBarPolicy> getVerticalScrollBarPolicy() {
 		return verticalScrollBarPolicy;
 	}
 
	public final IntegerProperty getScrollWidth() {
 		return scrollWidth;
 	}
 
	public final IntegerProperty getScrollHeight() {
 		return scrollHeight;
 	}
 
	public final DoubleProperty getHorizontalScrollPosition() {
 		return horizontalScrollPosition;
 	}
 
	public final DoubleProperty getVerticalScrollPosition() {
 		return verticalScrollPosition;
 	}
 
 	@Override
 	public final IntegerProperty widthProperty() {
 		return super.widthProperty();
 	}
 
 	@Override
 	public final IntegerProperty heightProperty() {
 		return super.heightProperty();
 	}
 
 	@Override
 	public final BackgroundProperty backgroundProperty() {
 		return super.backgroundProperty();
 	}
 }
