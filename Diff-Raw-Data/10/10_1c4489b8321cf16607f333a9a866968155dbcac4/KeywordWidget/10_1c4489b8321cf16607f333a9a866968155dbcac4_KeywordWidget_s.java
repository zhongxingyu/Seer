 package view.components.widgets;
 
 import java.util.ArrayList;
 import java.util.TreeMap;
 
 import org.mt4j.components.TransformSpace;
 import org.mt4j.components.visibleComponents.font.FontManager;
 import org.mt4j.components.visibleComponents.font.IFont;
 import org.mt4j.components.visibleComponents.shapes.MTRectangle.PositionAnchor;
 import org.mt4j.components.visibleComponents.widgets.MTList;
 import org.mt4j.components.visibleComponents.widgets.MTTextArea;
 import org.mt4j.input.inputProcessors.IGestureEventListener;
 import org.mt4j.input.inputProcessors.MTGestureEvent;
 import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapEvent;
 import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapProcessor;
 import org.mt4j.util.MTColor;
 import org.mt4j.util.math.Vector3D;
 
 import view.components.MTAbstractWindow;
 import view.elements.SuggestedElement;
 import bookshelf.Keyword;
 import controllers.SuggestableScene;
 
 public class KeywordWidget extends MTAbstractWindow implements IFacetWidget {
 	private TreeMap<Keyword,KeywordCell> keywords = new TreeMap<Keyword,KeywordCell>();
 	private final MTList list;
 	private final SuggestableScene scene;
	private final float minImportance = 0.7f;
 	private final MTTextArea warning;
 	
 	public KeywordWidget(final SuggestableScene scene, float x, float y, float w, float h) {
 		super(scene.getMTApplication(), x, y, w, h, "Keywords");
 		this.scene = scene;
 		
 		IFont font = FontManager.getInstance().createFont(scene.getMTApplication(), "fonts/Trebuchet MS.ttf", 
 				16, 	//Font size
 				new MTColor(255,255,255));	//Font color
 		
 		list = new MTList(scene.getMTApplication(), 0, 0, getContainer().getWidthXY(TransformSpace.LOCAL)-10, getContainer().getHeightXY(TransformSpace.LOCAL)-45);
 		getContainer().addChild(list);
 		list.setPositionRelativeToParent(new Vector3D(5,5,0).addLocal(list.getCenterOfMass2DLocal()));
 		list.setNoFill(true);
 		list.setNoStroke(true);
 		
 		MTTextArea selectAllButton = new MTTextArea(scene.getMTApplication(), font);
 		getContainer().addChild(selectAllButton);
 		selectAllButton.setText("Select All");
 		selectAllButton.setFillColor(new MTColor(0, 0, 0, 255));
 		selectAllButton.setStrokeWeight(2.5f);
 		selectAllButton.setStrokeColor(new MTColor(255, 255, 255, 150));
 		selectAllButton.setAnchor(PositionAnchor.LOWER_RIGHT);
 		selectAllButton.setPositionRelativeToParent(new Vector3D(getContainer().getWidthXY(TransformSpace.LOCAL)-5, getContainer().getHeightXY(TransformSpace.LOCAL)-5));
 		selectAllButton.removeAllGestureEventListeners();
 		selectAllButton.registerInputProcessor(new TapProcessor(scene.getMTApplication()));
 		selectAllButton.addGestureListener(TapProcessor.class, new IGestureEventListener() {
 			@Override
 			public boolean processGestureEvent(MTGestureEvent ge) {
 				TapEvent te = (TapEvent) ge;
 				
 				if (te.getTapID() == TapEvent.TAPPED) {
 					for (KeywordCell cell : keywords.values())
 						cell.select();
 					
 					scene.updateElements();
 				}
 				
 				return true;
 			}
 		});
 		
 		MTTextArea deselectAllButton = new MTTextArea(scene.getMTApplication(), font);
 		getContainer().addChild(deselectAllButton);
 		deselectAllButton.setText("Deselect All");
 		deselectAllButton.setFillColor(new MTColor(0, 0, 0, 255));
 		deselectAllButton.setStrokeWeight(2.5f);
 		deselectAllButton.setStrokeColor(new MTColor(255, 255, 255, 150));
 		deselectAllButton.setAnchor(PositionAnchor.LOWER_LEFT);
 		deselectAllButton.setPositionRelativeToParent(new Vector3D(5, getContainer().getHeightXY(TransformSpace.LOCAL)-5));
 		deselectAllButton.removeAllGestureEventListeners();
 		deselectAllButton.registerInputProcessor(new TapProcessor(scene.getMTApplication()));
 		deselectAllButton.addGestureListener(TapProcessor.class, new IGestureEventListener() {
 			@Override
 			public boolean processGestureEvent(MTGestureEvent ge) {
 				TapEvent te = (TapEvent) ge;
 				
 				if (te.getTapID() == TapEvent.TAPPED) {
 					for (KeywordCell cell : keywords.values())
 						cell.deselect();
 					
 					scene.updateElements();
 				}
 				
 				return true;
 			}
 		});
 		
 		warning = new MTTextArea(scene.getMTApplication(), font);
 		this.addChild(warning);
 		warning.setText("No keywords found");
 		warning.setPositionRelativeToOther(getContainer(), getContainer().getCenterPointLocal());
 		warning.setNoStroke(true);
 		warning.setNoFill(true);
 	}
 	
 	private void addKeyword(Keyword keyword) {
 		if (keyword.getImportance()<minImportance)
 			return;
 		
 		if (!keywords.containsKey(keyword)) {		
 			final KeywordCell cell = new KeywordCell(scene, list.getWidthXY(TransformSpace.LOCAL), 30, keyword);
 			cell.registerInputProcessor(new TapProcessor(scene.getMTApplication()));
 			cell.addGestureListener(TapProcessor.class, new IGestureEventListener() {
 				@Override
 				public boolean processGestureEvent(MTGestureEvent ge) {
 					TapEvent te = (TapEvent) ge;
 					
 					if (te.getTapID() == TapEvent.TAPPED) {
 						cell.inverseSelection();
 						scene.updateElements();
 					}
 					
 					return true;
 				}
 			});
 			keywords.put(keyword, cell);
 			list.addListElement(keywords.headMap(keyword,false).size(),cell);
 		} else {
 			keywords.get(keyword).raiseCount();
 			keywords.get(keyword).updateImportance(keyword.getImportance());
 		}
 		
 		if (!keywords.isEmpty())
 			warning.setVisible(false);
 	}
 	
 	public void addKeywords(ArrayList<Keyword> keywords) {
 		for (Keyword keyword : keywords)
 			addKeyword(keyword);
 	}
 	
 	private void removeKeyword(Keyword keyword) {
 		KeywordCell cell = keywords.get(keyword);
 		
 		if (cell == null)
 			return;
 		
 		if (cell.getCount()>1) {
 			cell.lowerCount();
 			keywords.get(keyword).updateImportance(-keyword.getImportance());
 		} else
 			list.removeListElement(keywords.remove(keyword));
 		
 		if (keywords.isEmpty())
 			warning.setVisible(true);
 	}
 	
 	public void removeKeywords(ArrayList<Keyword> keywords) {
 		for (Keyword keyword : keywords)
 			removeKeyword(keyword);
 	}
 	
 	public void removeKeywords() {
 		keywords.clear();
 		list.removeAllListElements();
 		warning.setVisible(true);
 	}
 
 	@Override
 	public boolean withinSelection(SuggestedElement element) {
 		boolean found = false;
 		
 		for (Keyword keyword : element.getBook().getKeywords()) {
 			if (keywords.containsKey(keyword))
 				found = true;
 			if (keywords.containsKey(keyword) && keywords.get(keyword).isSelected())
 				return true;
 		}
 		
 		return !found;
 	}
 }
