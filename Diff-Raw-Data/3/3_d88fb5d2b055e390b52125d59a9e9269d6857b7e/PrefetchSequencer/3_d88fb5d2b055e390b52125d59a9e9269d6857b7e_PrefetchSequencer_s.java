 package kornell.gui.client.sequence;
 
 import java.util.List;
 
 import kornell.api.client.Callback;
import kornell.api.client.KornellClient;
 import kornell.api.client.KornellSession;
 import kornell.core.lom.Actom;
 import kornell.core.lom.Contents;
 import kornell.core.lom.ContentsCategory;
 import kornell.core.lom.ExternalPage;
 import kornell.core.to.UserInfoTO;
 import kornell.core.util.StringUtils;
 import kornell.gui.client.event.ActomEnteredEvent;
 import kornell.gui.client.event.ProgressChangeEvent;
 import kornell.gui.client.event.ViewReadyEvent;
 import kornell.gui.client.event.ViewReadyEventHandler;
 import kornell.gui.client.presentation.course.ClassroomPlace;
 import kornell.gui.client.uidget.ExternalPageView;
 import kornell.gui.client.uidget.Uidget;
 
 import com.google.gwt.core.client.GWT;
import com.google.gwt.dev.ModuleTabPanel.Session;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.web.bindery.event.shared.EventBus;
 
 public class PrefetchSequencer implements Sequencer {
 	private FlowPanel contentPanel;
 	private String enrollmentUUID;
 	private KornellSession session;
 	private EventBus bus;
 	private List<Actom> actoms;
 
 	private int currentIndex;
 	private Actom currentActom;
 
 	private Actom nextActom;
 	private Uidget nextUidget;
 	private Uidget currentUidget;
 	private Actom prevActom;
 	private Uidget prevUidget;
 
 	public PrefetchSequencer(EventBus bus, KornellSession session) {
 		this.session = session;
 		this.bus = bus;
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
 		dropBreadcrumb();
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
 		dropBreadcrumb();
 		debug("PREVED");
 	}
 
 	private String getBreadcrumbKey() {
 		return PrefetchSequencer.class.getName() + "." + enrollmentUUID
 				+ ".CURRENT_KEY";
 	}
 	
 	@Override
 	public void onDirect(NavigationRequest event){
 		removeCurrent();
 		orientateAndSail(event.getDestination());
 	}
 
 	private void debug(String event) {
 		String prevString = prevKey() + prevVis();
 		String currString = currentKey() + currVis();
 		String nextString = nextKey() + nextVis();
 
 		// TODO: Use GWT Logging properly
 		// GWT.log(event + " " + currentIndex + " [" + prevString + " | " +
 		// currString + " | " + nextString + "]");
 
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
 		return prevActom != null ? prevActom.getKey() : "";
 	}
 
 	private String nextKey() {
 		return nextActom != null ? nextActom.getKey() : "";
 	}
 
 	private boolean doesntHaveNext() {
 		return actoms != null && currentIndex >= actoms.size() - 1;
 	}
 
 	private boolean doesntHavePrevious() {
 		return actoms != null && currentIndex <= 1;
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
 		if (currentUidget != null)
 			currentUidget.setVisible(true);
 		else
 			GWT.log("CURRENT UIDGET IS NULL. HOW COME?");
 		if (nextUidget != null)
 			nextUidget.setVisible(false);
 		if (prevUidget != null)
 			prevUidget.setVisible(false);
 	}
 
 	private void dropBreadcrumb() {
 		session.setItem(getBreadcrumbKey(), currentKey());
 		String key = "";
 		if (currentActom != null) {
 			key = currentActom.getKey();
 		}
 		if (StringUtils.isNone(key))
 			key = "????????????????";
 		bus.fireEvent(new ActomEnteredEvent(enrollmentUUID, key));
 		currentActom.setVisited(true);
 		fireProgressChangeEvent();
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
 
 	private void removeCurrent() {
 		if (currentUidget != null)
 			contentPanel.remove(currentUidget);
 		currentActom = null;
 	}
 
 	private void makePrevCurrent() {
 		currentActom = prevActom;
 		currentUidget = prevUidget;
 	}
 
 	@Override
 	public Sequencer withPanel(FlowPanel contentPanel) {
 		this.contentPanel = contentPanel;
 		contentPanel.clear();
 		return this;
 	}
 
 	@Override
 	public Sequencer withPlace(ClassroomPlace place) {
 		this.enrollmentUUID = place.getEnrollmentUUID();
 		return this;
 	}
 
 	@Override
 	public void go(Contents contents) {
 		setContents(contents);
 		orientateAndSail();
 	}
 
 	private void orientateAndSail() {
 		session.getCurrentUser(new Callback<UserInfoTO>() {
 			@Override
 			public void ok(UserInfoTO userInfo) {
 				orientateAndSail(session.getItem(getBreadcrumbKey()));
 			}
 		});
 	}
 
 	private void orientateAndSail(String key) {
 		currentIndex = lookupCurrentIndex(key);
 		currentActom = actoms.get(currentIndex);
 		initialLoad();
 	}
 
 	private int lookupCurrentIndex(String currentKey) {
 		int currentIndex = 0;
 		if (currentKey != null && !currentKey.isEmpty()) {
 			for (int i = 0; i < actoms.size(); i++) {
 				Actom actom = actoms.get(i);
 				if (currentKey.equals(actom.getKey())) {
 					return i;
 				}
 			}
 		}
 		return currentIndex;
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
 		preloadPrevious();
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
 
 	private void preloadPrevious() {
 		if (doesntHavePrevious()) {
 			prevActom = null;
 			prevUidget = null;
 		} else {
 			int previousIndex = currentIndex - 1;
 			prevActom = actoms.get(previousIndex);
 			prevUidget = uidgetFor(prevActom);
 			prevUidget.setVisible(false);
 			contentPanel.add(prevUidget);
 		}
 	}
 
 	private void showCurrentASAP() {
 		currentUidget = uidgetFor(currentActom);
 		contentPanel.add(currentUidget);
 		currentUidget.setVisible(false);
 		currentUidget.onViewReady(new ShowWhenReady(currentUidget));
 		dropBreadcrumb();
 	}
 
 	private Uidget uidgetFor(Actom actom) {
 		if (actom == null)
 			return null;
 		if (actom instanceof ExternalPage)
 			return new ExternalPageView(session, (ExternalPage) actom);
 		throw new IllegalArgumentException("Do not know how to view [" + actom
 				+ "]");
 	}
 
 	private void setContents(Contents contents) {
 		this.actoms = ContentsCategory.collectActoms(contents);
 	}
 
 	private void fireProgressChangeEvent() {
 		int pagesVisitedCount = 0;
 		int totalPages = actoms.size();
 		for (Actom actom : actoms) {
 			if (actom.isVisited()) {
 				pagesVisitedCount++;
 				continue;
 			}
 			break;
 		}
 		ProgressChangeEvent progressChangeEvent = new ProgressChangeEvent();
 		progressChangeEvent.setCurrentPage(currentIndex + 1);
 		progressChangeEvent.setTotalPages(totalPages);
 		progressChangeEvent.setPagesVisitedCount(pagesVisitedCount);
 		progressChangeEvent.setEnrollmentUUID(enrollmentUUID);
 		bus.fireEvent(progressChangeEvent);
 	}
 }
