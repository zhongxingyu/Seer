 package md.frolov.legume.client.activities.stream;
 
 import java.util.Date;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.inject.Inject;
 
 import com.google.gwt.activity.shared.AbstractActivity;
 import com.google.gwt.event.shared.EventBus;
 import com.google.gwt.place.shared.PlaceController;
 import com.google.gwt.user.client.ui.AcceptsOneWidget;
 
 import md.frolov.legume.client.Constants;
 import md.frolov.legume.client.elastic.ElasticSearchService;
 import md.frolov.legume.client.elastic.api.Callback;
 import md.frolov.legume.client.elastic.api.SearchRequest;
 import md.frolov.legume.client.elastic.api.SearchResponse;
 import md.frolov.legume.client.events.SearchFinishedEvent;
 import md.frolov.legume.client.events.SearchInProgressEvent;
 import md.frolov.legume.client.events.SearchResultsReceivedEvent;
 import md.frolov.legume.client.gin.WidgetInjector;
 import md.frolov.legume.client.model.Search;
 import md.frolov.legume.client.service.ConfigurationService;
 
 public class StreamActivity extends AbstractActivity implements StreamView.Presenter
 {
     private static final Logger LOG = Logger.getLogger("StreamActivity");
     private static final int DEFAULT_QUERY_SIZE = WidgetInjector.INSTANCE.configurationService().getInt(Constants.PAGE_SIZE);
 
     @Inject
     private StreamView streamView;
 
     @Inject
     private ConfigurationService configurationService;
 
     @Inject
     private ElasticSearchService elasticSearchService;
 
     @Inject
     private PlaceController placeController;
 
     private EventBus eventBus;
 
     private StreamPlace place;
 
     /** query to scroll upwards */
     private SearchRequest upwardsQuery;
     /** query to scroll downwards */
     private SearchRequest downwardsQuery;
 
     private boolean finished = false;
 
     @Override
     public void start(final AcceptsOneWidget panel, final EventBus eventBus)
     {
         panel.setWidget(streamView);
         streamView.setPresenter(this);
         place = (StreamPlace) placeController.getWhere();
         this.eventBus = eventBus;
 
         initQueries(place.getSearch());
 
         requestMoreResults(false);
     }
 
     private void initQueries(Search search)
     {
         long from = search.getFromDate();
         long to = search.getToDate();
         long focus = search.getFocusDate();
 
         //determine focus date
         if (focus < from || focus > to)
         {
             focus = from;
         }
         if (focus == 0)
         {
             focus = determineFocusDate(from, to);
         }
 
         downwardsQuery = new SearchRequest(new Search(search.getQuery(), focus, 0, focus), true, 0, DEFAULT_QUERY_SIZE);
         upwardsQuery = new SearchRequest(new Search(search.getQuery(), 0, focus, focus), false, 0, DEFAULT_QUERY_SIZE);
     }
 
     private long determineFocusDate(final long from, final long to)
     {
         if (to == 0)
         {
             //upto now
             if (from < 0)
             {
                 return from;
             }
             else
             {
                 return new Date().getTime();
             }
         }
 
         return from;
     }
 
     @Override
     public void requestMoreResults(final boolean upwards)
     {
         final SearchRequest query = upwards ? upwardsQuery : downwardsQuery;
         eventBus.fireEvent(new SearchInProgressEvent(upwards));
 
         elasticSearchService.query(query, new Callback<SearchRequest, SearchResponse>()
         {
             @Override
             public void onFailure(final Throwable exception)
             {
                 if (finished)
                 {
                     return;
                 }
                 LOG.log(Level.SEVERE, "Can't fetch results", exception);
                 eventBus.fireEvent(new SearchFinishedEvent(upwards));
             }
 
             @Override
             public void onSuccess(final SearchRequest query, final SearchResponse response)
             {
                 if (finished)
                 {
                     return;
                 }
                 query.setFrom(query.getFrom() + response.getHits().size());
 
                 LOG.fine("Got reply");
                 eventBus.fireEvent(new SearchResultsReceivedEvent(query, response, upwards));
                 eventBus.fireEvent(new SearchFinishedEvent(upwards));
             }
         });
     }
 
     @Override
     public void onStop()
     {
         stop();
     }
 
     @Override
     public void onCancel()
     {
         stop();
     }
 
     private void stop()
     {
         finished = true;
         elasticSearchService.cancelAllRequests();
     }
 }
