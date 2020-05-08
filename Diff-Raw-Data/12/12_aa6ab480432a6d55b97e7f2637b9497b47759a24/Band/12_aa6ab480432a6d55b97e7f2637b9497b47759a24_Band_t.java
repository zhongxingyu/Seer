 package wu.geostory;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import wu.events.WEvent;
 import wu.events.WHandler;
 
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.dom.client.Style;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.DoubleClickEvent;
 import com.google.gwt.event.dom.client.DoubleClickHandler;
 import com.google.gwt.event.dom.client.HasDoubleClickHandlers;
 import com.google.gwt.event.dom.client.HasKeyDownHandlers;
 import com.google.gwt.event.dom.client.HasMouseDownHandlers;
 import com.google.gwt.event.dom.client.HasMouseMoveHandlers;
 import com.google.gwt.event.dom.client.HasMouseUpHandlers;
 import com.google.gwt.event.dom.client.HasMouseWheelHandlers;
 import com.google.gwt.event.dom.client.KeyDownEvent;
 import com.google.gwt.event.dom.client.KeyDownHandler;
 import com.google.gwt.event.dom.client.MouseDownEvent;
 import com.google.gwt.event.dom.client.MouseDownHandler;
 import com.google.gwt.event.dom.client.MouseMoveEvent;
 import com.google.gwt.event.dom.client.MouseMoveHandler;
 import com.google.gwt.event.dom.client.MouseOutEvent;
 import com.google.gwt.event.dom.client.MouseOutHandler;
 import com.google.gwt.event.dom.client.MouseOverEvent;
 import com.google.gwt.event.dom.client.MouseOverHandler;
 import com.google.gwt.event.dom.client.MouseUpEvent;
 import com.google.gwt.event.dom.client.MouseUpHandler;
 import com.google.gwt.event.dom.client.MouseWheelEvent;
 import com.google.gwt.event.dom.client.MouseWheelHandler;
 import com.google.gwt.event.logical.shared.ResizeEvent;
 import com.google.gwt.event.logical.shared.ResizeHandler;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.maps.client.geom.LatLngBounds;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.AbsolutePanel;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.widgetideas.graphics.client.Color;
 import com.google.gwt.widgetideas.graphics.client.GWTCanvas;
 
 /**
  * TODO:
  * - when putting the middleLines button that are at the right of the lines cannot be clicked anymore see middleDebug
  * 
  * @author joris
  *
  */
 public class Band extends Composite implements 
 MouseDownHandler, MouseUpHandler, MouseMoveHandler, MouseOverHandler, MouseOutHandler,
 DoubleClickHandler,
 KeyDownHandler,
 ResizeHandler, 
 HasMouseDownHandlers, HasMouseMoveHandlers, HasMouseUpHandlers, 
 HasKeyDownHandlers,
 HasDoubleClickHandlers{
 
 	Map<GeoStoryItem,Widget> overlays;		// Contains all the widget to add to the band.
 	Set<Widget> labels;						// the markers to remove before redraw
 
 	Image now;
 
 	AbsolutePanel anchorPanel;				// the fixed panel where the basePanel moves in
 	AbsolutePanel largePanel;				// larger than the fixed one embeds everything and is efficiently dragged
 	int factor = 2;							// how many times we must add the width on left and right
 	int width;
 	int height;
 	int widthByFactor;
 
 	GWTCanvas canvas;						// canvas use to draw the backgroud of the band
 
 	Res resolution;					// the resolution of the band that provides bijection between pixel positions and dates
 	Date center;							// the date at the center of the band
 
 	Integer dragStart = null;				// all info to keep info about the drag 
 
 	GeoEventTypes types;					// the set of event types used inside the timeline component
 
 	Interval displayed;						// the displayed interval in the view
 	Interval drag;							// 
 	Map<Integer, Date> markers;
 
 	GeoStoryModel model;
 
 	//boolean divLevel = false;
 
 	Color background = new Color(255,250,244);
 	Color separator = new Color(30,40,80);
 	Color nowLine = new Color(150,230,120);
 	// Used to limit the number of event we want to use when changing the center of the timeline
 	// so see if performance gets better.
 	TimeLine line;
 
	boolean middleDebug = true;
 	Line middle;
 	Line middle2;
 
 	Boolean main = false;;
 
 	public Band(
 			TimeLine tl,
 			GeoStoryModel model,
 			Res res, 
 			Date center, 
 			GeoEventTypes types){
 		main = false;
 		anchorPanel = new AbsolutePanel();
 		this.line = tl;
 		this.model = model;
 		this.types = types;
 		this.resolution = res;
 		largePanel = new AbsolutePanel();
 		anchorPanel.add(largePanel);
 		canvas = new GWTCanvas(1000,1000);
 		canvas.setBackgroundColor(background);
 		largePanel.add(canvas);
 		overlays = new HashMap<GeoStoryItem,Widget>();
 		labels = new HashSet<Widget>();
 
 		if (middleDebug){
 			middle = new Line();
 			anchorPanel.add(middle);
 			middle2 = new Line();
 			anchorPanel.add(middle2);
 		}
 
 		this.center = center;
 
 		Window.addResizeHandler(this);
 		this.addMouseDownHandler(this);
 		this.addMouseUpHandler(this);
 		this.addMouseMoveHandler(this);
 		this.addDoubleClickHandler(this);
 		this.addKeyDownHandler(this);
 		types.panToCenterEvent.registerHandler(new WHandler<Date>(){
 			public void onEvent(WEvent<Date> elt) {
 				// Do not refresh if the band is being moved by the user (can it happen?)
 				if (!isDragging() && elt.getElement() != null)
 				{panTo(elt.getElement());};
 			}});
 		// Grab "new center" events from other bands to align them
 		types.centerEvent.registerHandler(new WHandler<Date>(){
 			public void onEvent(WEvent<Date> elt) {
 				// Do not refresh if the band is being moved by the user (can it happen?)
 				if (!isDragging() && elt.getElement() != null)
 				{refresh(elt.getElement());};
 			}});
 		types.mapViewChanged.registerHandler(new WHandler<LatLngBounds>(){
 			public void onEvent(WEvent<LatLngBounds> elt) {
 				//Window.alert("Map view has changed " +elt.getElement());
 			}});
 		types.itemSelected.registerHandler(new WHandler<GeoStoryItem>(){
 			@Override
 			public void onEvent(WEvent<GeoStoryItem> elt) {
 				panTo(elt.getElement().period.getA());
 				highlight(elt.getElement());
 			}});
 		this.initWidget(anchorPanel);
 		this.completePaint();
 	}
 
 	public void setResolution(Res r){this.resolution = r;}
 
 	public void setMain(){
 		main = true;
 	}
 
 	public Date getCenter(){
 		return center;
 	}
 
 	@Override
 	public void onKeyDown(KeyDownEvent event) {
 		Window.alert(event.toDebugString()+" "+event.getNativeKeyCode());
 	}
 
 	public boolean isDragging(){
 		return dragStart != null;
 	}
 
 	public void onDoubleClick(DoubleClickEvent event) {
 		// TODO how to get the x coord
 		Date target = resolution.pixelToDate(dragStart, width(), center);
 		refresh(target);
 		types.centerEvent.shareEvent(target);
 	}
 
 	public void onMouseDown(MouseDownEvent event) {
 		dragStart = event.getClientX();
 	}
 
 	public void onMouseOver(MouseOverEvent event) {
 	}
 
 	@Override
 	public void onMouseOut(MouseOutEvent event) {
 		//this.onMouseUp(null);
 	}
 
 
 	public void onMouseMove(MouseMoveEvent event) {
 		if (isDragging()){
 			int offset = event.getClientX()-dragStart;
 			shiftBy(offset);
 			line.refresh(this,offset,resolution);
 		}
 	}
 
 	public void shiftBy(int offset){
 		int newLeft = offset-widthByFactor;
 		//just shift the position of the moving panel in the reference one.
 		anchorPanel.setWidgetPosition(largePanel, newLeft, 0);
 	}
 
 	public void onMouseUp(MouseUpEvent event) {
 		if (event != null && resolution != null && dragStart !=null){
 			center = 
 				resolution.offSetByNPix(
 						center,
 						dragStart-event.getClientX());
 			dragStart = null;
 			types.centerEvent.shareEvent(center);
 		}
 	}
 
 	private void highlight(final GeoStoryItem item){
 		final Button b = model.widgetFor(item);
 		//b.getElement().getStyle().setBorderColor("red");
 		Timer t = new Timer(){
 			@Override
 			public void run() {
 				HighLightAnimation anim = new HighLightAnimation(b);
 				anim.run(3000);
 			}};
 			t.schedule(10);
 	}
 
 	public void onResize(ResizeEvent event) {
 		width = width();
 		height = height();
 		widthByFactor = factor*width;
 		canvas.resize(width()*(2*factor+1),  height());
 		//if (divLevel){largePanel.getElement().getStyle().setPropertyPx("left",-width());}
 		anchorPanel.setWidgetPosition(largePanel, -widthByFactor, 0);
 		// deal with the middle not moving line
 		if (this.middleDebug){
 			anchorPanel.setWidgetPosition(middle, width/2 +10, 0);
 			anchorPanel.setWidgetPosition(middle2, width/2 -10, 0);
 		}
 		// set the new large panel size
 		largePanel.setWidth(widthByFactor*2+width+"px");
 		completePaint();
 	}
 
 	public void completePaint(){
 		anchorPanel.setWidgetPosition(largePanel, -widthByFactor, 0);
 		setDisplayedInterval();
 		if (model.getItems().isEmpty()) {
 			largePanel.clear(); 
 			largePanel.add(canvas);
 		}
 		drawThings(largePanel, canvas,center);
 		drawDateLabels(largePanel);
 		drawBars(largePanel,canvas,overlays);
 	}
 
 	public void panTo(Date d){
 		// big jumps should be decomposed in animation
 		PanAnimation anim = new PanAnimation(center,d,this,this.resolution,width(),widthByFactor);
 		anim.run(1500);
 	}
 
 	public void shiftBand(int offset, Res otherRes){
 		int localOffset = (int)(0.0+offset*otherRes.secondsPerPixel/resolution.secondsPerPixel);
 		if (localOffset < width()){ // TODO make this test work
 			shiftBy(localOffset);
 		}
 		else{
 			//TODO what to do in that case?
 
 		}
 	}
 
 	public void refresh(Date d){
 		if (d != null && !center.equals(d)) {
 			center = d;
 		}
 		completePaint();
 	}
 
 	private int width(){
 		return anchorPanel.getOffsetWidth();
 	}
 
 	private int height(){
 		return anchorPanel.getOffsetHeight();
 	}
 
 	private void drawThings(AbsolutePanel pan, GWTCanvas canv, Date date){
 		canv.clear();
 		// Displays the limited part of the interval as a lighter rectangle
 		if (!this.main && this.line.getBounds() != null){
 			Interval inter = this.line.getBounds();
 			canv.setFillStyle(Color.LIGHTGREY);
 			int a = this.resolution.dateToPixel(inter.getA(), width(), center);
 			int b = this.resolution.dateToPixel(inter.getB(), width(), center);
 			canv.fillRect(a+widthByFactor, 2, b-a, height()-2);
 		}
 
 		for (Map.Entry<Integer, Date> marker : markers().entrySet()){
 			canv.setLineWidth(1);
 			canv.setStrokeStyle(this.separator);
 			canv.beginPath();
 			canv.moveTo(marker.getKey()+widthByFactor, 0);
 			canv.lineTo(marker.getKey()+widthByFactor, height());
 			canv.closePath();
 			canv.stroke();
 		}
 
 		// the green line to show where is now
 		int nowPosition = resolution.dateToPixel(new Date(), width, date)+widthByFactor;
 		canv.setLineWidth(5);
 		canv.setStrokeStyle(this.nowLine);
 		canv.beginPath();
 		canv.moveTo(nowPosition, 3);
 		canv.lineTo(nowPosition, height()-3);
 		canv.closePath();
 		canv.stroke();
 	}
 
 	private void drawDateLabels(AbsolutePanel pan){
 		// Markers 
 		for (Widget existing : labels){
 			//if (!this.getDisplayedInterval().contains(date)){
 			pan.remove(existing);
 			//labels.remove(date);
 			//}
 		}
 		labels.clear();
 		for (final Map.Entry<Integer, Date> marker : markers().entrySet()){
 			int x = marker.getKey()+widthByFactor;
 			Label label = new Label(resolution.labelForDate(marker.getValue()));
 			label.addClickHandler(new ClickHandler(){
 				public void onClick(ClickEvent event) {
 					types.panToCenterEvent.shareEvent(marker.getValue());
 				}});
 			labels.add(label);
 			pan.add(label, x+1, 1);
 		}
 	}
 
 	private void drawBars(AbsolutePanel pan, GWTCanvas canv,Map<GeoStoryItem,Widget> over){
 		//lines.linesForInterval(this.getDisplayedInterval());
 		if (main && this.model.numberOfLines() > 0){
 			int lineHeight = (int) ((height - 20) / this.model.numberOfLines() );
 			for(GeoStoryItem event : model.getItems()){
 				Button widg = model.widgetFor(event); // TODO should recover it
 				if (event.period.overlaps(this.getDragInterval())){
 					Date start = event.period.getA();
 					Date end = event.period.getB();
 					Integer startPixel = resolution.dateToPixel(start, width, center)+widthByFactor;
 
 					int left = startPixel;
 					int top  = 20 + model.lineFor(event)*lineHeight;
 					Integer endPixel = resolution.dateToPixel(end, width, center)+widthByFactor;
 					widg.setWidth(((endPixel-startPixel)>0?(endPixel-startPixel):20)+"px");
 					if (pan.getWidgetIndex(widg) == -1){
 						// this is when the widget does not exist
 						widg.setVisible(true);
 						widg.setHeight(lineHeight-4+"px");
 						pan.add(widg,left,top);
 						//System.out.println("new One");
 					}
 					else{
 						pan.setWidgetPosition(widg, left, top);
 						widg.setHeight(lineHeight-4+"px");
 						//System.out.println("                  existing");
 					}
 					//if (event.equals(selected)) {highlight(widg);}
 				}
 				else{
 					pan.remove(widg);
 				}
 			}
 		}
 	}
 
 	private void setDisplayedInterval(){
 		int w = width;
 		int f = factor; // the factor we add on left and right
 		displayed = new Interval(resolution.pixelToDate(0, w, center),resolution.pixelToDate(w,w,center));
 		drag = new Interval(resolution.pixelToDate(-f*w, w, center),resolution.pixelToDate(w+f*w,w,center));
 		markers = resolution.markers(width, center,f);
 	}
 
 
 	public Map<Integer,Date> markers(){
 		if (this.markers == null) setDisplayedInterval();
 		return this.markers;
 	}
 
 	public Interval getDragInterval(){
 		if (this.markers == null) setDisplayedInterval();
 		return this.drag;
 	}
 
 
 	public Interval getDisplayedInterval(){
 		if (this.markers == null) setDisplayedInterval();
 		return this.displayed;
 	}
 
 	public HandlerRegistration addMouseDownHandler(MouseDownHandler handler) {
 		return addDomHandler(handler, MouseDownEvent.getType());
 	}
 
 	public HandlerRegistration addMouseWheelHandler(MouseWheelHandler handler) {
 		return addDomHandler(handler, MouseWheelEvent.getType());
 	}
 
 	public HandlerRegistration addMouseMoveHandler(MouseMoveHandler handler) {
 		return addDomHandler(handler, MouseMoveEvent.getType());
 	}
 
 	public HandlerRegistration addMouseUpHandler(MouseUpHandler handler) {
 		return addDomHandler(handler, MouseUpEvent.getType());
 	}
 
 	public HandlerRegistration addDoubleClickHandler(DoubleClickHandler handler) {
 		return addDomHandler(handler, DoubleClickEvent.getType());
 	}
 
 	@Override
 	public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
 		return addDomHandler(handler, KeyDownEvent.getType());
 	}
 
 
 }
