 /**
  * May 17, 2010
  *
  * @author victor
  */
 package br.rio.puc.inf.app.activecal;
 
 import java.util.Map;
 import android.content.Context;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import br.rio.puc.inf.lac.mobilis.cms.ContextConsumer;
 import br.rio.puc.inf.lac.mobilis.cms.ContextInformationObject;
 import br.rio.puc.inf.app.activecal.ActiveCal.ActiveCalendarXML;
 
 /**
  * @author victor
  *
  */
 public class ActiveCalContextConsumer {
 
 	private static class DistanceContextConsumer extends ContextConsumer {
 		private static final String TAG = "activecal";
 		private Handler handler;
 
 		/**
 		 * Constructor.
 		 * 
 		 * @param context the application context
 		 * @param cli
 		 */
 		public DistanceContextConsumer(Context context, Handler handler)
 		{
 			super(context);
 			this.handler = handler;
 		}
 
 		/**
 		 * Sends a new log to the mains screen.
 		 * 
 		 * @param log the new log
 		 */
 
 		@Override
 		protected void newContextInformation(ContextInformationObject info)
 		{
 			Log.d(TAG, "nova informacao de contexto, class=" + info.getInformationClass() + ", device=" + info.getDevice());
 
 			Bundle b = new Bundle();
 			Message m = Message.obtain(handler);
 			m.setData(b);
 
 			/*Obtendo as informacoes de localizacao dos outros guests*/
 			if (info.getInformationClass().equals("distance") && info.containsContextInformation("time"))
 			{
 				Log.e(TAG + "time", info.getContextInformation("time"));
 				b.putString("time", info.getContextInformation("time"));
 			}
 
 			if (info.getInformationClass().equals("distance") && info.containsContextInformation("meters"))
 			{
 				Log.e(TAG + "distance", info.getContextInformation("meters"));
 				b.putString("distance", info.getContextInformation("meters"));
 			}
 
 			handler.sendMessage(m);
 		}
 	}
 
 	private static class MeetingContextConsumer extends ContextConsumer {
 		private static final String TAG = "activecal";
 		private Handler mainScreenHandler;
 		private ActiveCal mainActivityObject;
 		private Boolean started;
 
 		/**
 		 * Constructor.
 		 * 
 		 * @param context the application context
 		 * @param cli
 		 */
 		public MeetingContextConsumer(Context context, Handler cli, ActiveCal obj) {
 			super(context);
 			mainScreenHandler = cli;
 			mainActivityObject = obj;
 			started = false;
 		}
 
 		@Override
 		protected void newContextInformation(ContextInformationObject info)
 		{
			Log.d(TAG,"chegou uma nova informacao!");
 			if (started == false && info.getInformationClass().equals("meeting")) {
 				mainScreenHandler.sendMessage(Message.obtain(mainScreenHandler, ActiveCal.RUN, mainActivityObject));
 				started = true;
 			}
 
 			if (info.getInformationClass().equals("meeting") && !info.getContextInformation("feed").equals(null)) {
 				ActiveCal.cal = new ActiveCalendarXML(info.getContextInformation("feed"));
 				Map <String,String> x[] = ActiveCal.cal.deXML();
 				for (int i = 0; i < x.length; i++) {
 					for (String s : x[i].keySet())
 						Log.d(TAG, s + ": " + x[i].get(s));
 				}
 				return;
 			}
 
 			Log.d(TAG, "Uncatch information: " + info.getInformationClass() + ", device=" + info.getDevice());
 			for (String s : info.getContextInformationsProvided()) {
 				Log.d(TAG,"                   " + s + ": " + info.getContextInformation(s));
 			}
 		}
 
 	}
 
 	/** reference to the main screen handler */
 
 	public static ContextConsumer newConsumer (Context context, Handler handler, ActiveCal activecal) {
 		return new MeetingContextConsumer (context, handler, activecal);
 	}
 	
 	public static ContextConsumer newConsumer (Context context, Handler handler) {
 		return new DistanceContextConsumer(context, handler);
 	}
 }
