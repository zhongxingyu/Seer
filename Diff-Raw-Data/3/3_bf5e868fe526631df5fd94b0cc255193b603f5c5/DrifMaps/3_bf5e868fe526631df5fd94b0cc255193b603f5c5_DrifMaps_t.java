 package com.scurab.web.drifmaps.client;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
 import com.google.gwt.event.shared.HandlerManager;
 import com.google.gwt.i18n.client.NumberFormat;
 import com.google.gwt.i18n.shared.DateTimeFormat;
 import com.google.gwt.i18n.shared.DateTimeFormat.PredefinedFormat;
 import com.google.gwt.user.client.Window;
 import com.scurab.web.drifmaps.client.dialog.NotificationDialog;
 import com.scurab.web.drifmaps.client.presenter.MainViewPresenter;
 import com.scurab.web.drifmaps.client.view.MainView;
 import com.scurab.web.drifmaps.language.Words;
 
 ;
 /**
  * Entry point classes define <code>onModuleLoad()</code>.
  */
 public class DrifMaps implements EntryPoint
 {
 
 	public static final Words Words = GWT.create(Words.class);
 	private static final DataServiceAsync sDataService = GWT.create(DataService.class);
 	private static final HandlerManager sEventBus = new HandlerManager(null);
 	public static NumberFormat Currency = NumberFormat.getCurrencyFormat();
 	public static NumberFormat Decimal = NumberFormat.getDecimalFormat();
 	public static DateTimeFormat DateFormat = DateTimeFormat.getFormat(PredefinedFormat.DATE_LONG);
 	public static DateTimeFormat DateTimeMediumFormat = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_MEDIUM);
 	
 
 	public DrifMaps()
 	{
 		GWT.setUncaughtExceptionHandler(new UncaughtExceptionHandler()
 		{
 			@Override
 			public void onUncaughtException(Throwable e)
 			{
				GWT.log("onUncaughtException", e);
//				NotificationDialog.show(e);
 			}
 		});
 	}
 
 	@Override
 	public void onModuleLoad()
 	{
 		try
 		{
 			// MainViewOld mv = new MainViewOld(sDataService,sEventBus);
 			new MainViewPresenter(new MainView(sDataService), sDataService);
 		}
 		catch (Exception e)
 		{
 			Window.alert(e.getMessage());
 		}
 
 	}
 
 	
 
 }
