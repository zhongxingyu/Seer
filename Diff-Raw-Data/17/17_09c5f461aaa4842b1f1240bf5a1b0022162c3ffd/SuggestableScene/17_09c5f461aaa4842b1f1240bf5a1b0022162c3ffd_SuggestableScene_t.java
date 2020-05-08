 package view;
 
 import java.util.ArrayList;
 
 import org.mt4j.components.visibleComponents.widgets.MTBackgroundImage;
 import org.mt4j.sceneManagement.AbstractScene;
 import org.mt4j.sceneManagement.IPreDrawAction;
 
 import view.elements.RetrievedElement;
 import view.elements.SuggestedElement;
 import view.elements.listeners.RelatedElementListener;
 import view.elements.listeners.UnrelatedElementListener;
 import view.elements.observers.SuggestedElementBirthObserver;
 import view.widgets.WidgetLayer;
 import view.widgets.custom.KeywordWidget;
 import view.widgets.custom.TimelineWidget;
 import application.ModelController;
 import application.Suggestable;
 import bookshelf.AbstractBook;
 import bookshelf.apis.google.GoogleBook;
 import bookshelf.apis.google.GoogleBookProcessor;
 import bookshelf.exceptions.BookshelfUnavailableException;
 
 public class SuggestableScene extends AbstractScene {
 	private ModelController controller = new ModelController();
 	private ArrayList<RetrievedElement> retrievedElements = new ArrayList<RetrievedElement>();
 	private ArrayList<SuggestedElement> suggestedElements = new ArrayList<SuggestedElement>();
 	
 	private KeywordWidget keywordWidget;
 	private TimelineWidget timelineWidget;
 	
 	public WidgetLayer widgetLayer = new WidgetLayer(this);
 	
 	public SuggestableScene(Suggestable application) {
 		super(application, "Suggestable Scene");
 	}
 	
 	@Override
 	public void init() {
 		MTBackgroundImage background = 
 			new MTBackgroundImage(getMTApplication(), 
 					getMTApplication().loadImage("data/images/stripes.png"), false);
 
 		getCanvas().addChild(background);
 		
 		this.keywordWidget = new KeywordWidget(getMTApplication(), 500, 500, 400, 200);
 		this.timelineWidget = new TimelineWidget(getMTApplication(), 500, 500, 400, 200);
 		getKeywordWidget().setVisible(false);
 		getTimelineWidget().setVisible(false);
 		widgetLayer.addChild(getKeywordWidget());
 		widgetLayer.addChild(getTimelineWidget());
 		
 		this.getCanvas().addChild(widgetLayer);
 	}
 
 	@Override
 	public void shutDown() {
 		// TODO Auto-generated method stub
 	}
 
 	public ArrayList<RetrievedElement> getBooksInPosession() {
 		return retrievedElements;
 	}
 
 	protected ArrayList<SuggestedElement> getBooksRelated() {
 		return suggestedElements;
 	}
 
 	
 	public ModelController getController() {
 		return controller;
 	}
 
 	protected void setController(ModelController controller) {
 		this.controller = controller;
 	}
 	
 	protected KeywordWidget getKeywordWidget() {
 		return this.keywordWidget;
 	}
 	
 	private TimelineWidget getTimelineWidget() {
 		return this.timelineWidget;
 	}
 
 	public void removeAllBooks() {
 		for (RetrievedElement p : retrievedElements)
 			p.destroy();
 		
 		for (SuggestedElement s : suggestedElements)
 			s.destroy();
 		
 		retrievedElements.clear();
 		suggestedElements.clear();
 	}
 
 	public void showKeywordWidget() {
 		ArrayList<String> keywords = new ArrayList<String>();
 		
 		for (SuggestedElement s : suggestedElements) {
 			GoogleBook book = s.getBook();
 			keywords.addAll(book.getKeywords());
 		}
 		
 		getKeywordWidget().setKeywords(keywords);
 		getKeywordWidget().setVisible(true);
 	}
 	
 	public void showTimelineWidget() {
 		ArrayList <Integer> years = new ArrayList<Integer>();
 		
 		for (SuggestedElement s : suggestedElements) {
 			AbstractBook book = s.getBook();
 			years.add(book.getPublishingYear());
 		}
 		
 		getTimelineWidget().setValues(years);
 		getTimelineWidget().setVisible(true);
 	}
 
 	public void addSuggestion(SuggestedElement s, RetrievedElement p) {
 		suggestedElements.add(s);
 		getCanvas().addChild(s);
 		IPreDrawAction action = new RelatedElementListener(p, s);
 		s.registerPreDrawAction(action);
 		registerPreDrawAction(action);
 		
 		for (SuggestedElement so : this.suggestedElements) {
 			if (so.equals(s))
 				continue;
 			
 			IPreDrawAction action2 = new UnrelatedElementListener(so, s);
 			s.registerPreDrawAction(action2);
 			so.registerPreDrawAction(action2);
 			registerPreDrawAction(action2);
 		}
 	}
 	
 	public void removeSuggestion(SuggestedElement s) {
 		for (IPreDrawAction action : s.getPreDrawActions())
 			this.unregisterPreDrawAction(action);
 		
 		s.destroy();
 		suggestedElements.remove(s);
 	}
 
 	public void addPlaceholder(RetrievedElement p) {
 		retrievedElements.add(p);
 		getCanvas().addChild(p);
 		
 		try {
 			GoogleBookProcessor gp = getController().getRelatedBooks(p.getBook());
 			gp.addObserver(new SuggestedElementBirthObserver(this,p));
 			gp.setLimit(7);
 			Thread thread = new Thread(gp);
 			thread.start();
 		} catch (BookshelfUnavailableException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 }
