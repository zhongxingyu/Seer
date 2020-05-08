 package chaves.android.services;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import winterwell.jtwitter.Status;
 import winterwell.jtwitter.Twitter;
 import winterwell.jtwitter.TwitterException;
 import android.app.Service;
 import android.content.Intent;
 import android.os.IBinder;
 import android.util.Log;
 import chaves.android.R;
 import chaves.android.Utils;
 import chaves.android.YambaApplication;
 
 public class TimelinePull extends Service {
 	
 	private static final String TAG = "TimeLinePull";
 	
 	//Tempo de intervalo da obteno dos tweets
 	private int INITIAL_DELAY = 1 * 1000;
 	private boolean _runFlag = false;
 	private Updater _updater;
 	private YambaApplication _application;

 	
 	/**
 	 * 	No Implementado , s para boundedServices
 	 */
 	@Override
 	public IBinder onBind(Intent arg0) {
 		return null;
 	}
 	
 	/**
 	 * Primeiro mtodo chamado do servio
 	 */
 	@Override
 	public void onCreate(){
 		super.onCreate();
 		_application = (YambaApplication)getApplication();
 		_updater = new Updater();
 		Log.i(TAG,"onCreate");
 	}
 	
 
 	
 	/**
 	 * Depois de passar os vrios estados do servio este  o principal que vai correr o cdigo pretendido.
 	 */
 	@Override
 	public int onStartCommand(Intent intent,int flags, int startId)
 	{
 		super.onStartCommand(intent, flags, startId);
 		
 		_runFlag = true;
 		_updater.start();
 		_application.setTimeLineServiceRunning(true);
 		
 		Log.i(TAG,"onStartCommand");
 		return START_STICKY;
 	}
 	/**
 	 * Quando o servio  destruido afecta as variaveis partilhadas na aplicao
 	 */
 	@Override
 	public void onDestroy(){
 		super.onDestroy();
 		
 		_runFlag = false;
 		_updater.stop();
 		_updater = null;
 		_application.setTimeLineServiceRunning(false);
 		_application.setServiceThread(null);
 		Log.i(TAG,"onDestroy");
 	}
 	
 	/**
 	 * Implementao da thread que vai correr o servio
 	 * @author LeoBurn
 	 *
 	 */
 	public class Updater extends Thread{
 		//Lista actual no formato Twitter.Status
 		private List<Status> _timeline;
 		//Lista actual no formato Map<String,String> que contem o valor e o id para a descrio
 		private ArrayList<Map<String,String>> _showedList;
 		
 		public Updater(){
 			super("Service-TimeLinePull");
 			_showedList = new ArrayList<Map<String, String>>();
 			_application.setServiceThread(this);
 		}
 		
 		public void run(){
 			TimelinePull timeLineService = TimelinePull.this;
 			List<Status> actualTimeLine = null;
 			while(timeLineService._runFlag)
 			{
 				Log.i(TAG, "TimeLine running");
 				Twitter t;
 				while((t = _application.getTwitter()) == null){
 					try {
 						timeLineService._runFlag = false;
 						Thread.sleep(INITIAL_DELAY);
 					} catch (InterruptedException e) {t = _application.getTwitter();}
 					finally{timeLineService._runFlag = true;}
 				}
 				try{
 					actualTimeLine = t.getHomeTimeline();
 				}
 				catch(TwitterException e){
 					Log.e(TAG,"Failed to connect to twitter");
 				}
 				//So notifica se houver mudanas
 				//TODO Mais tarde verificar o conteudo e s actualizar se houve um tweet diferente
 				if(_timeline == null && actualTimeLine != null || actualTimeLine.size() != _timeline.size()){
 					_timeline = actualTimeLine;
 					putDataInMapper();
 					_application.setTimeLinedata(_showedList);
 				}
 
 				Log.i(TAG, "TimeLine ran");
 				if(!_application.getAutoRefresh()){
 					timeLineService._runFlag = false;
					timeLineService.stopService(new Intent(timeLineService, TimelinePull.class));
 				}
 				try{
 					Thread.sleep(_application.getDelay());
 				}
 				catch(InterruptedException e){}
 			}
			
			
 		}
 		
 		public boolean isRunning(){
 			return TimelinePull.this._runFlag;
 		}
 		
 		/**
 		 * Metodo que transforma List<Twitter.Status> no tipo ArrayList<Map<String,String>>
 		 */
 		private void putDataInMapper(){
 			HashMap<String, String> map;
 			int maxsize = _application.getListMaxSize();
 			_showedList.clear();
 			int i = 0;
 			while(i < _timeline.size() && i < maxsize){
 				map = Utils.getMap(_timeline.get(i));
 				_showedList.add(map);
 				++i;
 			}
 		}		
 		
 	}
 
 }
