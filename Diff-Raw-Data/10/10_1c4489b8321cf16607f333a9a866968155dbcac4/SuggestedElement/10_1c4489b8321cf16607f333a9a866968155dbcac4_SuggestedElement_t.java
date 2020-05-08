 package view.elements;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.media.opengl.GL;
 
 import org.mt4j.components.TransformSpace;
 import org.mt4j.components.bounds.IBoundingShape;
 import org.mt4j.components.visibleComponents.font.FontManager;
 import org.mt4j.components.visibleComponents.font.IFont;
 import org.mt4j.components.visibleComponents.shapes.MTRoundRectangle;
 import org.mt4j.components.visibleComponents.widgets.MTImage;
 import org.mt4j.components.visibleComponents.widgets.MTTextArea;
 import org.mt4j.input.inputProcessors.IGestureEventListener;
 import org.mt4j.input.inputProcessors.MTGestureEvent;
 import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragProcessor;
 import org.mt4j.input.inputProcessors.componentProcessors.tapAndHoldProcessor.TapAndHoldEvent;
 import org.mt4j.input.inputProcessors.componentProcessors.tapAndHoldProcessor.TapAndHoldProcessor;
 import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapEvent;
 import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapProcessor;
 import org.mt4j.util.MTColor;
 import org.mt4j.util.math.Ray;
 import org.mt4j.util.math.Vector3D;
 
 import processing.core.PGraphics;
 import processing.core.PImage;
 import view.elements.gestures.DragElementListener;
 import view.elements.gestures.TapAndHoldElementListener;
 import view.elements.gestures.TrashSuggestedElementListener;
 import bookshelf.apis.google.GoogleBook;
 import controllers.SuggestableScene;
 
 public class SuggestedElement extends AbstractElement {
 	private final GoogleBook book;
 	private final MTRoundRectangle child;
 	private final MTTextArea text;
 
 	private final ArrayList<RetrievedElement> associatedElements = new ArrayList<RetrievedElement>();
 	
 	public SuggestedElement(final SuggestableScene scene, float s, GoogleBook book) {
 		super(scene);
 		this.child = new MTRoundRectangle(scene.getMTApplication(), 0, 0, 0, s, s, 5, 5);
 		
 		this.book = book;
 		this.addChild(child);
 		this.setVertices(child.getVerticesLocal());
 		
 		child.setFillColor(new MTColor(0, 0, 0, 200));
 		child.setStrokeWeight(2.5f);
 		child.setStrokeColor(new MTColor(255, 255, 255, 150));
 		child.setComposite(true);
 		child.setBoundsAutoCompute(true);
 		
 		try {
 			final MTImage cover = new MTImage(scene.getMTApplication(), new PImage(getBook().getCover()));
 			child.addChild(cover);
 			cover.setPositionRelativeToParent(child.getCenterPointLocal());
 			cover.setStrokeColor(new MTColor(0, 255, 255, 150));
 			float scaleFactor = child.getHeightXY(TransformSpace.GLOBAL)/cover.getHeightXY(TransformSpace.GLOBAL);
 			cover.scaleGlobal(scaleFactor, scaleFactor, 1, cover.getCenterPointGlobal());
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		IFont font = FontManager.getInstance().createFont(scene.getMTApplication(), "fonts/Trebuchet MS.ttf", 
 				12, 	//Font size
 				new MTColor(255,255,255));	//Font color
 		
 		text = new MTTextArea(scene.getMTApplication(), 1, 1, s-2, s-2, font);
 		text.setNoStroke(true);
 		text.setFillColor(new MTColor(0, 0, 0, 180));
 		text.setText(getBook().getTitle());
 		this.addChild(text);
 		
 		final SuggestedElement self = this;
 		registerInputProcessor(new TapProcessor(scene.getMTApplication()));
 		addGestureListener(TapProcessor.class, new IGestureEventListener() {
 			@Override
 			public boolean processGestureEvent(MTGestureEvent ge) {
 				TapEvent te = (TapEvent) ge;
 				
 				if (te.getTapID() == TapEvent.TAPPED) {
 					scene.showInformationWindow(self);
 				}
 				return true;
 			}
 		});
 		registerInputProcessor(new TapAndHoldProcessor(scene.getMTApplication()));
 		addGestureListener(TapAndHoldProcessor.class, new TapAndHoldElementListener(scene.getMTApplication(), scene.getPanLayer()));
 		addGestureListener(TapAndHoldProcessor.class, new IGestureEventListener() {
 			@Override
 			public boolean processGestureEvent(MTGestureEvent ge) {
 				TapAndHoldEvent te = (TapAndHoldEvent) ge;
 				
 				if (te.isHoldComplete()) {
 					RetrievedElement element = new RetrievedElement(scene, null, getBook(), 125);
 					element.transform(getGlobalMatrix());
 					scene.removeElement(self);
 					scene.addElement(element);
					element.setPositionRelativeToParent(scene.getPanLayer().globalToLocal(getCenterPointGlobal()));
 				}
 				return true;
 			}
 		});
 		
 		addGestureListener(DragProcessor.class, new DragElementListener(this));
 		addGestureListener(DragProcessor.class, new TrashSuggestedElementListener(scene,this));
 	}
 	
 	public void addAssociatedElement(RetrievedElement element) {
 		this.associatedElements.add(element);
 	}
 	
 	public void removeAssociatedElement(RetrievedElement element) {
 		this.associatedElements.remove(element);
 	}
 	
 	public boolean hasAssociatedElements() {
 		return this.associatedElements.size() > 0;
 	}
 
 	public GoogleBook getBook() {
 		return this.book;
 	}
 
 	@Override
 	protected void drawPureGl(GL gl) {
 		// Nothing.
 	}
 
 	@Override
 	public Vector3D getGeometryIntersectionLocal(Ray ray) {
 		return child.getGeometryIntersectionLocal(ray);
 	}
 
 	@Override
 	public boolean isGeometryContainsPointLocal(Vector3D testPoint) {
 		return child.isGeometryContainsPointLocal(testPoint);
 	}
 
 	@Override
 	public Vector3D getCenterPointLocal() {
 		return child.getCenterPointLocal();
 	}
 
 	@Override
 	protected void destroyComponent() {
 		// Nothing.
 	}
 	
 	
 
 	@Override
 	public void drawComponent(PGraphics g) {
 		// Nothing.
 	}
 	
 	@Override
 	public IBoundingShape getBounds() {
 		return child.getBounds();
 	}
 
 	public void removeAllAssociatedElements() {
 		this.associatedElements.clear();
 	}
 }
