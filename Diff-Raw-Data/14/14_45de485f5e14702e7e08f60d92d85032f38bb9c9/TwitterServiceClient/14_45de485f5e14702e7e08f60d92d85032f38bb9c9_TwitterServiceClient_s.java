 package pt.isel.adeetc.meic.pdm.controllers;
 
 import android.content.Intent;
 import pt.isel.adeetc.meic.pdm.YambaApplication;
 import pt.isel.adeetc.meic.pdm.common.GenericEvent;
 import pt.isel.adeetc.meic.pdm.common.IEvent;
 import pt.isel.adeetc.meic.pdm.common.IEventHandler;
 import pt.isel.adeetc.meic.pdm.common.IEventHandlerArgs;
 import pt.isel.adeetc.meic.pdm.common.db.IDbSet;
 import pt.isel.adeetc.meic.pdm.exceptions.Constants;
 import pt.isel.adeetc.meic.pdm.exceptions.ShouldNotHappenException;
 import pt.isel.adeetc.meic.pdm.services.StatusUploadService;
 import winterwell.jtwitter.Twitter;
 
 public final class TwitterServiceClient
 {
     private static final String LOG = "TwitterServiceClient";
 
     public final IEvent<Integer> updateStatusCompletedEvent;
     public final IEvent<Iterable<Twitter.ITweet>> getUserTimelineCompletedEvent;
 
     public final IEventHandler<Boolean> _publisherEvent;
 
     private Twitter _twitter;
     private final IDbSet<Twitter.ITweet> _tweetDb;
     private final YambaApplication _app;
     private final TimelineServiceController _timelineController;
     private final StatusServiceController _statusController;
 
 
     public TwitterServiceClient(IDbSet<Twitter.ITweet> tweetDb, YambaApplication app)
     {
         _tweetDb = tweetDb;
         _app = app;
         updateStatusCompletedEvent = new GenericEvent<Integer>();
         getUserTimelineCompletedEvent = new GenericEvent<Iterable<Twitter.ITweet>>();
         _timelineController = new TimelineServiceController(this, _app.getGeneralPurposeHandler());
         _statusController = new StatusServiceController(_app, this);
         _publisherEvent = new PublisherEventHandler();
         _app.getNetworkEvent().addEventHandler(_publisherEvent);
 
     }
 
 
     public void updateStatusAsync(String status)
     {
         _statusController.updateStatusAsync(status);
     }
 
     @SuppressWarnings({"unchecked"})
     public void getUserTimelineAsync()
     {
         _timelineController.getUserTimelineAsync();
     }
 
     public Iterable<Twitter.ITweet> getTwitterCachedTimeline()
     {
         return _timelineController.getStatusCache();
     }
 
 
     public void configureTwitterClient(String user, String password, String apiRootUrl)
     {
         _twitter = new Twitter(user, password);
         _twitter.setAPIRootUrl(apiRootUrl);
     }
 
     public Twitter getTwitter()
     {
         if (_twitter == null)
             throw new ShouldNotHappenException(Constants.NoUserAndPasswordDefined);
         return _twitter;
     }
 
     IDbSet<Twitter.ITweet> getTweetDbSet()
     {
         return _tweetDb;
     }
 
     private class PublisherEventHandler implements IEventHandler<Boolean>
     {
 
         @Override
         public void invoke(Object sender, IEventHandlerArgs<Boolean> args)
         {
 
             try
             {
                 if (_app.getNetworkState())
                 {
                     Intent statusIntent = new Intent(YambaApplication.getContext(), StatusUploadService.class);
                     YambaApplication.getContext().startService(statusIntent);
                     _timelineController.deployPeriodicAlarm();
                 }
                 else
                 {
                     _timelineController.cancelPeriodicAlarm();
                 }
 
             } catch (Exception e)
             {
                 throw new ShouldNotHappenException(e);
             }
         }
     }
 }
 
