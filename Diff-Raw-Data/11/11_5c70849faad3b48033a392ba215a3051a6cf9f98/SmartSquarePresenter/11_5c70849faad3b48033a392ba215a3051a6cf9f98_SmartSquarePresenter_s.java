 /**
  * 
  */
 package com.steelthorn.android.watch.smartsquare;
 
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 
 import android.graphics.Bitmap;
 import android.location.Location;
 import android.os.Looper;
 import android.util.Log;
 import br.com.condesales.EasyFoursquareAsync;
 import br.com.condesales.criterias.CheckInCriteria;
 import br.com.condesales.criterias.VenuesCriteria;
 import br.com.condesales.criterias.VenuesCriteria.VenuesCriteriaIntent;
 import br.com.condesales.listeners.CheckInListener;
 import br.com.condesales.listeners.FoursquareVenuesResquestListener;
 import br.com.condesales.models.Checkin;
 import br.com.condesales.models.Venue;
 
 /**
  * @author Jeff Mixon
  * 
  */
 public class SmartSquarePresenter extends BasePresenter<ISmartSquareControlView> implements ISmartSquarePresenter
 {
 	private static final String TAG = "SmartSquarePresenter";
 
 	private LocationValet _valet;
 	private Location _lastKnown;
 
 	public SmartSquarePresenter(ISmartSquareControlView view)
 	{
 		super(view);
 	}
 
 	/* (non-Javadoc)
 	 * @see com.steelthorn.android.watch.smartsquare.ISmartSquarePresenter#requestNearbyVenues()
 	 */
 	@Override
 	public void requestNearbyVenues()
 	{
 		new Thread()
 		{
 			public void run()
 			{
 				Looper.prepare();
 				
 				_valet = new LocationValet(getView().getContext(), new LocationValet.ILocationValetListener()
 				{
 
 					public void onBetterLocationFound(Location l)
 					{
						if (l.hasAccuracy() && l.getAccuracy() <= 200)
 						{
 							_lastKnown = l;
 							//if (l.getAccuracy() <= 20)
 							_valet.stopAquire();
 							Looper.myLooper().quit();
 
 							EasyFoursquareAsync api = new EasyFoursquareAsync(getView().getContext());
 							
 							if (!api.hasAccessToken())
 							{
 								getView().onNotFoursquareAuthenticated();
 								return;
 							}
 							
 
 							VenuesCriteria crit = new VenuesCriteria();
 							crit.setLocation(l);
 							crit.setIntent(VenuesCriteriaIntent.CHECKIN);
 
 							api.getVenuesNearby(new FoursquareVenuesResquestListener()
 							{
 
 								@Override
 								public void onError(String errorMsg)
 								{
 									Log.e(TAG, "Foursquare API returned an error: " + errorMsg);
 									if (getView() != null)
 										getView().onError(new Exception(errorMsg));
 								}
 
 								@Override
 								public void onVenuesFetched(ArrayList<Venue> venues)
 								{
 									if (getView() != null)
 										getView().onNearbyVenuesReceived(venues);
 
 								}
 							}, crit);
 						}
 					}
 				});
 
 				_valet.startAquire(false);
 
 				Looper.loop();
 			}
 		}.start();
 
 	}
 
 	@Override
 	public void fetchVenueCategoryBitmap(Venue v)
 	{
 		if (v == null || v.getCategories() == null || v.getCategories().size() < 1)
 			return;
 
 		new ImageDownloaderThread(v).start();
 	}
 
 	private class ImageDownloaderThread extends Thread
 	{
 		private final Venue _venue;
 
 		public ImageDownloaderThread(Venue v)
 		{
 			_venue = v;
 		}
 
 		public void run()
 		{
 			String uri = _venue.getCategories().get(0).getIcon().getPrefix() + "256.png";
 
 			Log.d(TAG, "Downloading " + uri);
 
 			Bitmap b = null;
 			try
 			{
 				b = Util.downloadImageWithCaching(getView().getContext(), uri);
 
 			}
 			catch (FileNotFoundException fe)
 			{
 				Log.w(TAG, "Defined category icon is missing. Trying the default icon.");

 				String prefix = _venue.getCategories().get(0).getIcon().getPrefix();
				String retryUri = prefix.substring(0, prefix.lastIndexOf('/') + 1) + "_256.png";
 
 				try
 				{
 					b = Util.downloadImageWithCaching(getView().getContext(), retryUri);
 				}
 				catch (Exception e)
 				{
 					Log.w(TAG, "An error occurred retrying the icon download: " + e);
 				}
 			}
 			catch (Exception e)
 			{
 				Log.w(TAG, "An error occurred downloading the icon: " + e);
 			}
 
 			if (b == null)
 				return;
 
 			if (getView() != null)
 				getView().onVenueCategoryIconRetrieved(_venue, b);
 		}
 	}
 
 	@Override
 	public void requestCheckin(final Venue v)
 	{
 		new Thread()
 		{
 			public void start()
 			{
 				EasyFoursquareAsync api = new EasyFoursquareAsync(getView().getContext());
 
 				CheckInCriteria crit = new CheckInCriteria();
 				crit.setVenueId(v.getId());
 				crit.setLocation(_lastKnown);
 
 				api.checkIn(new CheckInListener()
 				{
 
 					@Override
 					public void onError(String errorMsg)
 					{
 						Log.e(TAG, errorMsg);
 						//getView().onError(new Exception(errorMsg));
 						getView().onCheckinResponse(false);
 
 					}
 
 					@Override
 					public void onCheckInDone(Checkin checkin)
 					{
 						Log.d(TAG, "onCheckInDone");
 
 						getView().onCheckinResponse(true);
 					}
 				}, crit);
 			}
 		}.start();
 
 	}
 }
