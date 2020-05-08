 package md.frolov.legume.client.activities.stream;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Logger;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import com.github.gwtbootstrap.client.ui.Button;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.core.client.Scheduler;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.MouseOverEvent;
 import com.google.gwt.event.dom.client.MouseOverHandler;
 import com.google.gwt.event.dom.client.ScrollEvent;
 import com.google.gwt.place.shared.PlaceController;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.ScrollPanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.web.bindery.event.shared.EventBus;
 
 import md.frolov.legume.client.Application;
 import md.frolov.legume.client.elastic.api.SearchRequest;
 import md.frolov.legume.client.elastic.api.SearchResponse;
 import md.frolov.legume.client.elastic.model.reply.SearchHit;
 import md.frolov.legume.client.events.*;
 import md.frolov.legume.client.model.Search;
 import md.frolov.legume.client.service.ColorizeService;
 import md.frolov.legume.client.ui.EventFlowPanel;
 import md.frolov.legume.client.ui.components.LogEventComponent;
 import md.frolov.legume.client.util.IteratorIncrementalTask;
 
 public class StreamViewImpl extends Composite implements StreamView
 {
     private static final Logger LOG = Logger.getLogger("StreamView");
 
     private static StreamViewImplUiBinder uiBinder = GWT.create(StreamViewImplUiBinder.class);
 
     private final Set ids = Sets.newHashSet();
     private final LinkedList<LogEventComponent> logEvents = Lists.newLinkedList();
 
     @UiField
     FlowPanel container;
     @UiField
     ScrollPanel scrollContainer;
     @UiField
     FlowPanel topLoading;
     @UiField
     FlowPanel bottomLoading;
 
     @UiField
     FlowPanel resultsPanel;
     @UiField
     FlowPanel topNoMoreResults;
     @UiField
     FlowPanel bottomNoMoreResults;
     @UiField
     Button bottomLoadingTryAgain;
     @UiField
     Button bottomAutoFetch;
     @UiField
     FlowPanel loadingPanel;
     @UiField
     FlowPanel errorPanel;
     @UiField
     FlowPanel nothingFoundPanel;
     @UiField
     FlowPanel topError;
     @UiField
     FlowPanel bottomError;
 
     @Inject
     @Named("scrollThreashold")
     private Integer scrollThreashold = 50;
 
     @Inject
     private ColorizeService colorizeService;
     @Inject
     private PlaceController placeController;
     @Inject
     private Application application;
 
     @Inject
     private EventBus eventBus;
 
     private Presenter presenter;
     private boolean isRendering;
     private boolean isScrollableUp;
 
     private boolean topFinished;
     private boolean bottomFinished;
 
 
     interface StreamViewImplUiBinder extends UiBinder<Widget, StreamViewImpl>
     {
     }
 
     @Inject
     public StreamViewImpl(EventBus eventBus)
     {
         initWidget(uiBinder.createAndBindUi(this));
     }
 
     @Override
     public void setPresenter(final Presenter presenter)
     {
         this.presenter = presenter;
         container.clear();
         ids.clear();
         logEvents.clear();
 
         eventBus.fireEvent(new ScrollableStateChangedEvent(false));
         isScrollableUp = false;
     }
 
     @Override
     public void showLoading()
     {
         loadingPanel.setVisible(true);
         errorPanel.setVisible(false);
         nothingFoundPanel.setVisible(false);
         resultsPanel.setVisible(false);
     }
 
     @Override
     public void showError()
     {
         loadingPanel.setVisible(false);
         errorPanel.setVisible(true);
         nothingFoundPanel.setVisible(false);
         resultsPanel.setVisible(false);
     }
 
     @Override
     public void showNothingFound()
     {
         loadingPanel.setVisible(false);
         errorPanel.setVisible(false);
         nothingFoundPanel.setVisible(true);
         resultsPanel.setVisible(false);
     }
 
     @Override
     public void showResults()
     {
         loadingPanel.setVisible(false);
         errorPanel.setVisible(false);
         nothingFoundPanel.setVisible(false);
         resultsPanel.setVisible(true);
     }
 
     @Override
     public void showLoading(final boolean upwards)
     {
         if(upwards) {
             topLoading.setVisible(true);
             topError.setVisible(false);
             topNoMoreResults.setVisible(false);
             topFinished = false;
         } else {
             bottomLoading.setVisible(true);
             bottomError.setVisible(false);
             bottomNoMoreResults.setVisible(false);
             bottomFinished = false;
         }
     }
 
     @Override
     public void showError(final boolean upwards)
     {
         if(upwards) {
             topLoading.setVisible(false);
             topError.setVisible(true);
         } else {
             bottomLoading.setVisible(false);
             bottomError.setVisible(true);
         }
     }
 
     @Override
     public void handleNewHits(final boolean upwards, final SearchRequest searchRequest, final SearchResponse searchResponse)
     {
         isRendering=true;
 
         final List<SearchHit> hits = searchResponse.getHits();
         final boolean isFullFetch = hits.size() == searchRequest.getSize();
 
         Scheduler.get().scheduleIncremental(new IteratorIncrementalTask<SearchHit>(hits) {
             private EventFlowPanel panel;
             private int cnt = 0;
 
             @Override
             public void beforeAll()
             {
                 panel = new EventFlowPanel();
 
                 //add focus date to URL on page mouse hover
                 SearchHit firstHit = Iterables.getFirst(hits, null);
                 if(firstHit!=null) {
                     final long focusDate = firstHit.getLogEvent().getTimestamp().getTime();
                     panel.addMouseOverHandler(new MouseOverHandler()
                     {
                         @Override
                         public void onMouseOver(final MouseOverEvent event)
                         {
                             Search search = application.getCurrentSearch().clone();
                             search.setFocusDate(focusDate);
                             placeController.goTo(new StreamPlace(search));
                         }
                     });
                 }
             }
 
             @Override
             public void iterate(final SearchHit hit)
             {
                 //guard against duplicates
                 String id = hit.getId();
                 if (!ids.contains(id))
                 {
                     ids.add(id);
                     cnt++;
 
                     //add log message
                     LogEventComponent logEventComponent = new LogEventComponent(hit.getId(), hit.getLogEvent());
                     if (upwards)
                     {
                         panel.insert(logEventComponent, 0);
                         logEvents.addFirst(logEventComponent);
                     }
                     else
                     {
                         panel.add(logEventComponent);
                         logEvents.addLast(logEventComponent);
                     }
                 }
             }
 
             @Override
             public void afterAll()
             {
                 if (upwards)
                 {
                     int scrollToBottom = scrollContainer.getMaximumVerticalScrollPosition() - scrollContainer.getVerticalScrollPosition();
                     container.insert(panel, 0);
                     topLoading.setVisible(false);
                     topNoMoreResults.setVisible(true);
                     int scrollPosition = scrollContainer.getMaximumVerticalScrollPosition() - scrollToBottom;
                     scrollContainer.setVerticalScrollPosition(scrollPosition);
                 }
                 else
                 {
                     container.add(panel);
                     bottomLoading.setVisible(false);
                     bottomNoMoreResults.setVisible(true);
                 }
 
                 isRendering = false;
 
                 if(cnt == 0) {
                     if(isFullFetch) {
                         requestMore(upwards);
                     } else {
                         handleNoMoreResults(upwards);
                     }
                 }
 
                 colorizeService.refresh();
                 presenter.checkInitialRequests(upwards);
             }
         });
     }
 
     private void handleNoMoreResults(boolean upwards)
     {
         if (upwards)
         {
             topNoMoreResults.setVisible(true);
             topLoading.setVisible(false);
             topFinished = true;
         }
         else
         {
             bottomNoMoreResults.setVisible(true);
             bottomLoading.setVisible(false);
             bottomFinished = true;
             if(bottomAutoFetch.isToggled()) {
                 startAutoFetchTimer();
             }
         }
     }
 
     private void startAutoFetchTimer() {
         Scheduler.get().scheduleFixedDelay(new Scheduler.RepeatingCommand()
         {
             @Override
             public boolean execute()
             {
                 requestMoreBottom();
                 return false; //don't repeat
             }
         }, 30000); //TODO externalize
     }
 
     @UiHandler("scrollContainer")
     public void onScroll(final ScrollEvent event)
     {
         int toTop = scrollContainer.getVerticalScrollPosition() - scrollContainer.getMinimumVerticalScrollPosition();
         int toBottom = scrollContainer.getMaximumVerticalScrollPosition() - scrollContainer.getVerticalScrollPosition();
 
         if(toTop == 0 && isScrollableUp) {
             eventBus.fireEvent(new ScrollableStateChangedEvent(false));
             isScrollableUp = false;
         }
         if(toTop>0 && !isScrollableUp) {
             eventBus.fireEvent(new ScrollableStateChangedEvent(true));
             isScrollableUp = true;
         }
 
         if (toTop == 0 && toBottom == 0)
         {
             return;
         }
 
         if (toTop < scrollThreashold)
         {
             requestMoreTop();
         }
         if (toBottom < scrollThreashold)
         {
             requestMoreBottom();
         }
     }
 
     private void requestMore(boolean upwards) {
         if(upwards) {
             requestMoreTop();
         } else {
             requestMoreBottom();
         }
     }
 
     private void requestMoreTop()
     {
         if (topLoading.isVisible() || topError.isVisible() || topFinished || isRendering)
         {
             return;
         }
         LOG.info("Request top");
         presenter.requestMoreResults(true);
     }
 
     private void requestMoreBottom()
     {
         if (bottomLoading.isVisible() || bottomError.isVisible() || bottomFinished || isRendering)
         {
             return;
         }
         LOG.info("Request bottom");
         presenter.requestMoreResults(false);
     }
 
     @UiHandler("topErrorTryAgain")
     public void onTopErrorAgainClick(final ClickEvent event)
     {
         topError.setVisible(false);
         requestMoreTop();
     }
 
     @UiHandler("topLoadingTryAgain")
     public void onTopLoadingAgainClick(final ClickEvent event)
     {
         topFinished = false;
         topNoMoreResults.setVisible(false);
         requestMoreTop();
     }
 
     @UiHandler("bottomErrorTryAgain")
     public void onBottomErrorTryAgain(final ClickEvent event)
     {
         bottomError.setVisible(false);
         requestMoreBottom();
     }
 
     @UiHandler("bottomLoadingTryAgain")
     public void onBottomLoadingTryAgain(final ClickEvent event)
     {
         bottomFinished = false;
         bottomNoMoreResults.setVisible(false);
         requestMoreBottom();
     }
 
     @UiHandler("bottomAutoFetch")
     public void handleClick(final ClickEvent event)
     {
         if(!bottomAutoFetch.isToggled()) {
             startAutoFetchTimer();
         }
     }
 
     @Override
     public void focusOnDate(final long focusDate)
     {
         LogEventComponent last = null;
         for (LogEventComponent logEvent : logEvents)
         {
             last = logEvent;
            if(focusDate < logEvent.getTimestamp()) {
                 break;
             }
         }
         if(last != null) {
             scrollContainer.ensureVisible(last);
             last.flash();
         }
     }
 }
 
