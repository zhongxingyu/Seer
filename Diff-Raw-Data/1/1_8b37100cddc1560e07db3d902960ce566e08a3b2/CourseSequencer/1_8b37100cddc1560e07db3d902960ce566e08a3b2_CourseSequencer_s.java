 package kornell.gui.client.sequence;
 
 import java.util.Iterator;
 import java.util.List;
 
 import kornell.api.client.Callback;
 import kornell.api.client.KornellClient;
 import kornell.core.shared.data.Actom;
 import kornell.core.shared.data.Contents;
 import kornell.core.shared.data.ContentsCategory;
 import kornell.core.shared.data.ExternalPage;
 import kornell.gui.client.event.ViewReadyEvent;
 import kornell.gui.client.event.ViewReadyEventHandler;
 import kornell.gui.client.presentation.course.CoursePlace;
 import kornell.gui.client.uidget.ExternalPageView;
 import kornell.gui.client.uidget.Uidget;
 
 import com.google.gwt.core.shared.GWT;
 import com.google.gwt.storage.client.Storage;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.web.bindery.event.shared.EventBus;
 
 public class CourseSequencer implements Sequencer {
 	private String CURRENT_KEY = CourseSequencer.class.getName() + ".CURRENT_KEY";
 	private FlowPanel contentPanel;
 	private String courseUUID;
 	private KornellClient client;
 	private Contents contents;
 	private List<Actom> actoms;
 
 	private int currentIndex;
 	private Actom currentActom;
 
 	private Actom nextActom;
 	private Uidget nextUidget;
 	private Uidget currentUidget;
 	private Actom prevActom;
 	private Uidget prevUidget;
 
 	public CourseSequencer(EventBus bus, KornellClient client) {
 		this.client = client;
 		bus.addHandler(NavigationRequest.TYPE, this);
 	}
 
 	@Override
 	public void onContinue(NavigationRequest event) {
 		if (doesntHaveNext())
 			return;
 		removePrevious();
 		makeCurrentPrevious();
 		makeNextCurrent();
 		currentIndex++;
 		preloadNext();
 		makeCurrentVisible();
 		checkpoint();
 		debug("CONTINUED");
 	}
 
 	@Override
 	public void onPrevious(NavigationRequest event) {
 		if (doesntHavePrev())
 			return;
 		removeNext();
 		makeCurrentNext();
 		makePrevCurrent();
 		currentIndex--;
 		preloadPrev();
 		makeCurrentVisible();
 		checkpoint();
 		debug("PREVED");
 	}
 
 	private void checkpoint() {
 		store(CURRENT_KEY,currentKey());
 	}
 
 	private void debug(String event) {
 		String prevString = prevKey() + prevVis();
 		String currString = currentKey() + currVis();
 		String nextString = nextKey() + nextVis();
 
 		GWT.log(event + " " + currentIndex + " [" + prevString + " | "
 				+ currString + " | " + nextString + "]");
 	}
 
 	private String nextVis() {
 		return " " + (nextUidget != null ? nextUidget.isVisible() : "-");
 	}
 
 	private String currVis() {
 		return " " + (currentUidget != null ? currentUidget.isVisible() : "-");
 	}
 
 	private String prevVis() {
 		return " " + (prevUidget != null ? prevUidget.isVisible() : "-");
 	}
 
 	private String currentKey() {
 		return currentActom != null ? currentActom.getKey() : "";
 	}
 
 	private String prevKey() {
 		// TODO Auto-generated method stub
 		return prevActom != null ? prevActom.getKey() : "";
 	}
 
 	private String nextKey() {
 		// TODO Auto-generated method stub
 		return nextActom != null ? nextActom.getKey() : "";
 	}
 
 	private boolean doesntHaveNext() {
 		return currentIndex >= actoms.size() - 1;
 	}
 
 	private void makeCurrentPrevious() {
 		prevActom = currentActom;
 		prevUidget = currentUidget;
 	}
 
 	private void removePrevious() {
 		if (contentPanel.getWidgetIndex(prevUidget) == -1)
 			return;
 
 		contentPanel.remove(prevUidget);
 		prevActom = null;
 	}
 
 	private void makeNextCurrent() {
 		currentActom = nextActom;
 		currentUidget = nextUidget;
 	}
 
 	private void makeCurrentVisible() {
 		currentUidget.setVisible(true);
 		if (nextUidget != null)
 			nextUidget.setVisible(false);
 		if (prevUidget != null)
 			prevUidget.setVisible(false);
 	}
 
 	private boolean doesntHavePrev() {
 		return currentIndex == 0;
 	}
 
 	private void preloadPrev() {
 		if (currentIndex > 0) {
 			prevActom = actoms.get(currentIndex - 1);
 			prevUidget = uidgetFor(prevActom);
 			prevUidget.setVisible(false);
 			contentPanel.add(prevUidget);
 		} else {
 			prevActom = null;
 			prevUidget = null;
 		}
 	}
 
 	private void makeCurrentNext() {
 		nextUidget = currentUidget;
 		nextActom = currentActom;
 	}
 
 	private void removeNext() {
 		if (nextUidget != null)
 			contentPanel.remove(nextUidget);
 		nextActom = null;
 	}
 
 	private void makePrevCurrent() {
 		currentActom = prevActom;
 		currentUidget = prevUidget;
 	}
 
 	@Override
 	public Sequencer withPanel(FlowPanel contentPanel) {
 		this.contentPanel = contentPanel;
 		return this;
 	}
 
 	@Override
 	public Sequencer withPlace(CoursePlace place) {
 		this.courseUUID = place.getCourseUUID();
 		return this;
 	}
 
 	@Override
 	public void go() {
 		client.course(courseUUID).contents(new Callback<Contents>() {
 
 			@Override
 			protected void ok(Contents contents) {
 				setContents(contents);
 				orientateAndSail();
 			}
 
 			private void orientateAndSail() {
 				// TODO: Fetch current position
 				currentIndex = lookupCurrentIndex();
 				currentActom = actoms.get(currentIndex);
 				initialLoad();
 			}
 
 			private int lookupCurrentIndex() {
 				int currentIndex = 0;
 				String currentKey = loadCurrentKey();
 				if(currentKey != null && ! currentKey.isEmpty()){
 					for (int i = 0; i < actoms.size(); i++) {
 						Actom actom = actoms.get(i);
 						if(currentKey.equals(actom.getKey())){
 							return i;
 						}
 					}					
 				}
 				return currentIndex;
 			}
 
 			private String loadCurrentKey() {
 				return load(CURRENT_KEY);
 			}
 
 
 		});
 	}
 
 	private String load(String key) {
 		Storage localStorage = Storage.getLocalStorageIfSupported();
 		if(localStorage != null)
 			return localStorage.getItem(key);
 		return null;
 	}
 	
 	private void store(String key, String value){
 		Storage localStorage = Storage.getLocalStorageIfSupported();
 		if(localStorage != null)
 			localStorage.setItem(key, value);
 	}
 	class ShowWhenReady implements ViewReadyEventHandler {
 
 		private Uidget uidget;
 
 		public ShowWhenReady(Uidget uidget) {
 			this.uidget = uidget;
 		}
 
 		@Override
 		public void onViewReady(ViewReadyEvent evt) {
 			uidget.setVisible(true);
 		}
 	}
 
 	private void initialLoad() {
 		showCurrentASAP();
 		preloadNext();
 		debug("INITIAL");
 	}
 
 	private void preloadNext() {
 		if (doesntHaveNext()) {
 			nextActom = null;
 			nextUidget = null;
 		} else {
 			int nextIndex = currentIndex + 1;
 			nextActom = actoms.get(nextIndex);
 			nextUidget = uidgetFor(nextActom);
 			nextUidget.setVisible(false);
 			contentPanel.add(nextUidget);
 		}
 	}
 
 	private void showCurrentASAP() {
 		currentUidget = uidgetFor(currentActom);
 		contentPanel.add(currentUidget);
 		currentUidget.setVisible(false);
 		currentUidget.onViewReady(new ShowWhenReady(currentUidget));
 	}
 
 	private Uidget uidgetFor(Actom actom) {
 		if (actom == null)
 			return null;
 		if (actom instanceof ExternalPage)
 			return new ExternalPageView(client, (ExternalPage) actom);
 		throw new IllegalArgumentException("Do not know how to view [" + actom
 				+ "]");
 	}
 
 	private void setContents(Contents contents) {
 		this.contents = contents;
 		this.actoms = ContentsCategory.collectActoms(contents);
 	}
 
 	/*
 	 * private KornellClient client;
 	 * 
 	 * private CoursePlace place; private CourseTO courseTO; private Contents
 	 * contents; private String currentKey; private String baseURL; private
 	 * String courseUUID;
 	 * 
 	 * private EventBus bus;
 	 * 
 	 * public CourseSequencer(EventBus bus, KornellClient client) {
 	 * GWT.log("new CourseSequencer"); this.bus = bus; this.client = client;
 	 * bus.addHandler(NavigationRequest.TYPE, this); }
 	 * 
 	 * private List<Actom> actoms; private int currentIndex; private
 	 * ExternalPageView externalPageView;
 	 * 
 	 * private String nextKey() { String nextKey = isAtEnd() ? currentKey :
 	 * getActoms().get(++currentIndex).getKey(); return nextKey; }
 	 * 
 	 * private List<Actom> getActoms() { if (actoms == null) actoms =
 	 * ContentsCategory.collectActoms(contents); return actoms; }
 	 * 
 	 * @Override public void onContinue(NavigationRequest event) { currentKey =
 	 * nextKey(); go(); }
 	 * 
 	 * private void go() { dropBreadcrumb(); walk(); }
 	 * 
 	 * private void dropBreadcrumb() { // TODO Auto-generated method stub }
 	 * 
 	 * @Override public void onPrevious(NavigationRequest event) { currentKey =
 	 * prevKey(); go(); }
 	 * 
 	 * private String prevKey() { return getActoms().get(currentIndex > 0 ?
 	 * --currentIndex : 0).getKey(); }
 	 * 
 	 * private int getCurrentIndex() { return currentIndex; }
 	 * 
 	 * @Override public void displayOn(final FlowPanel contentPanel) {
 	 * contentPanel.clear();
 	 * 
 	 * externalPageView = new ExternalPageView(client); render(place);
 	 * 
 	 * externalPageView.addStyleName("shy"); contentPanel.add(externalPageView);
 	 * 
 	 * 
 	 * 
 	 * 
 	 * final TimerView timerView = new TimerView(); contentPanel.add(timerView);
 	 * 
 	 * externalPageView.onViewReady(new ViewReadyEventHandler() {
 	 * 
 	 * @Override public void onViewReady(ViewReadyEvent evt) { Timer timer = new
 	 * Timer() {
 	 * 
 	 * @Override public void run() { timerView.addStyleName("shy");
 	 * externalPageView.removeStyleName("shy"); } };
 	 * 
 	 * timer.schedule(1500); } });
 	 * 
 	 * 
 	 * 
 	 * }
 	 * 
 	 * private void walk() { String src = StringUtils.composeURL(baseURL,
 	 * currentKey); GWT.log("Navigating to [" + src + "]");
 	 * externalPageView.setSrc(src); evaluateNavigation(); }
 	 * 
 	 * private void evaluateNavigation() { Forecast f = isAtEnd() ? NEXT_NOT_OK
 	 * : NEXT_OK; bus.fireEvent(new NavigationForecastEvent(f)); }
 	 * 
 	 * private boolean isAtEnd() { int index = getCurrentIndex(); int end =
 	 * getActoms().size() - 1; boolean isAtEnd = index >= end; return isAtEnd; }
 	 * 
 	 * private void render(final CoursePlace place) { GWT.log("Rendering [" +
 	 * place + "]"); if (place == null) throw new
 	 * IllegalArgumentException("Cannot render null place"); courseUUID =
 	 * place.getCourseUUID(); fetchContentsAndGo();
 	 * 
 	 * }
 	 * 
 	 * private void fetchContentsAndGo() {
 	 * client.course(courseUUID).contents(new Callback<Contents>(){
 	 * 
 	 * @Override protected void ok(Contents contents) { setContents(contents);
 	 * orientateAndGo(); } }); }
 	 * 
 	 * private void orientateAndGo() { if (!orientateByQueryString())
 	 * orientateToLastVisited(); go(); }
 	 * 
 	 * private boolean orientateByQueryString() { String key =
 	 * Window.Location.getParameter("key"); if (key != null && !key.isEmpty()) {
 	 * currentKey = key; return true; } else return false; }
 	 * 
 	 * private void orientateToLastVisited() { String checkpoint =
 	 * courseTO.getEnrollment().getLastActomVisited(); if (checkpoint == null ||
 	 * checkpoint.isEmpty()) currentKey = getActoms().get(0).getKey(); else
 	 * currentKey = checkpoint; }
 	 * 
 	 * @Override public Sequencer withPlace(CoursePlace place) { this.place =
 	 * place; return this; }
 	 * 
 	 * private void setContents(Contents contents) { this.contents = contents;
 	 * setCourseTO(contents.getCourseTO()); }
 	 * 
 	 * private void setCourseTO(CourseTO courseTO) { this.courseTO = courseTO;
 	 * setBaseURL(courseTO.getBaseURL());
 	 * 
 	 * }
 	 * 
 	 * private void setBaseURL(String baseURL) { this.baseURL = baseURL; }
 	 */
 
 }
