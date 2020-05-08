 package pt.isel.adeetc.meic.pdm.controllers;
 
 import android.accounts.NetworkErrorException;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Message;
 import android.util.Log;
 import pt.isel.adeetc.meic.pdm.R;
 import pt.isel.adeetc.meic.pdm.YambaApplication;
 import pt.isel.adeetc.meic.pdm.YambaNavigation;
 import pt.isel.adeetc.meic.pdm.common.GenericEventArgs;
 import pt.isel.adeetc.meic.pdm.common.IEventHandler;
 import pt.isel.adeetc.meic.pdm.common.IEventHandlerArgs;
 import pt.isel.adeetc.meic.pdm.exceptions.ShouldNotHappenException;
 import pt.isel.adeetc.meic.pdm.extensions.BoundedService;
 import pt.isel.adeetc.meic.pdm.extensions.BoundedServiceClient;
 import pt.isel.adeetc.meic.pdm.services.StatusUploadService;
 
 public class StatusServiceController extends BoundedServiceClient implements IEventHandler<IEventHandler<Integer>>
 {
     private static final String LOG = "StatusServiceController";
     private final YambaApplication _app;
     private final TwitterServiceClient _twitterFacade;
     private String _pendingStatus;
 
 
     public StatusServiceController(YambaApplication app, TwitterServiceClient client)
     {
         _app = app;
         _twitterFacade = client;
         setCallServiceWhenConnected(true);
        _twitterFacade.updateStatusCompletedEvent.setOnEventHandlerChanged(this);
     }
 
     public void updateStatusAsync(String status)
     {
         _pendingStatus = status;
         if (!isServiceConnected())
         {
             Log.d(LOG, "Binding service");
             _app.bindService(getServiceIntent(), getServiceConnection(), Context.BIND_AUTO_CREATE);
             return;
         }
         Log.d(LOG, "Calling service");
         callService();
     }
 
     @Override
     protected void fillServiceRequest(Message msg)
     {
         if (_pendingStatus == null)
             throw new ShouldNotHappenException("_pendingStatus is null");
 
         Log.d(LOG, "on fillServiceRequest with messege: " + _pendingStatus);
 
         msg.getData().putString(YambaNavigation.STATUS_SERVICE_MESSAGE_PARAM_NAME, _pendingStatus);
     }
 
     @Override
     protected void onServiceResponse(Message message)
     {
         Log.d(LOG, "onServiceResponse");
         Exception error = null;
         int id = R.string.status_tweet_create;
 
         int val = message.getData().getInt(YambaNavigation.STATUS_SERVICE_RESULT_PARAM_NAME);
 
         if (val == BoundedService.SERVICE_RESPONSE_ERROR)
         {
             error = (Exception) message.getData().getSerializable(YambaNavigation.STATUS_SERVICE_ERROR_PARAM_NAME);
             if (error instanceof NetworkErrorException)
             {
                 id = R.string.status_tweet_delay;
             } else
             {
                 id = R.string.status_error_insert_newStatus;
             }
         }
 
 
         final IEventHandlerArgs<Integer> eventData = new GenericEventArgs<Integer>(id, null);
 
         _app.getUiHandler().post(new Runnable()
         {
             @Override
             public void run()
             {
                 _twitterFacade.updateStatusCompletedEvent.invoke(StatusServiceController.this, eventData);
             }
         });
     }
 
     public Intent getServiceIntent()
     {
         return new Intent(_app, StatusUploadService.class);
     }
 
 
     @Override
     public void invoke(Object sender, IEventHandlerArgs<IEventHandler<Integer>> args)
     {
         try
         {
             if (args.getData() == null)
             {
                 Log.d(LOG, "Unbinding");
                 unbind(_app);
             }
         } catch (Exception e)
         {
             throw new ShouldNotHappenException(e);
         }
     }
 }
