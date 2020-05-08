 package net.audev.batteryinfowidget;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.util.LinkedList;
 
 import android.app.Service;
 import android.appwidget.AppWidgetManager;
 import android.appwidget.AppWidgetProvider;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.graphics.Color;
 import android.os.BatteryManager;
 import android.os.Handler;
 import android.os.IBinder;
 import android.util.Log;
 import android.widget.RemoteViews;
 
 public class BatteryInfo extends AppWidgetProvider {
 	private static final String TAG = "BatteryInfo";
 	
 	@Override
     public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
 	
 		Log.d(TAG,"++ onUpdate");		
 		context.startService(new Intent(context, UpdateWidgetService.class));	
 		Log.d(TAG,"-- onUpdate");
 	}
 	
 	/*
 	 * El servicio que lleva el peso de la aplicacin
 	 */
 	public static class UpdateWidgetService extends Service {		
 		public static final String TAG = "BatteryInfo";
		public static final String FILE_CARGA = "carga.data";
		public static final String FILE_DESCARGA = "descarga.data";
		private static final int MAX_ENTRIES = 1000;
		
		public static int accuracy = 0;
 		
 		String oldTitulo;
 		AppWidgetManager manager;
 		RemoteViews updateViews; 
         ComponentName thisWidget;      
         
 	    int scale = -1;
 		int levelAnterior = -1;
 		long cuandoLevelAnterior = -1;
 	    int level = -1;
 		int voltage = -1;
 		int temp = -1;
 		int health = -1;
 		String tech = "";
 		
 		LinkedList<Long> tiemposDescarga;
 		LinkedList<Long> tiemposCarga;
 		boolean isCargando = false;
 		boolean isFirstRun = true;
         
 		/*
 		 * Registro del Receiver del estado de la batera que mantendremos todo el tiempo
 		 */
         @Override
         public void onCreate() {
             super.onCreate();
             Log.d(TAG,"++ onCreate");
             tiemposDescarga = new LinkedList<Long>();
             tiemposCarga = new LinkedList<Long>();
             
             // TODO cargar tiempos cacheados
             
             BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
 
  		        @Override
  		        public void onReceive(Context context, Intent intent) {
  		        	
  		        	if (isFirstRun) {
  		        		// en el primer lanzamiento, intentamos recuperar datos de la cache
  		        		File cacheDir = context.getFilesDir();
  		        		File cargaFile = new File(cacheDir,FILE_CARGA);
  		        		File descargaFile = new File(cacheDir,FILE_DESCARGA);
  		        		if (cargaFile.exists()) {
  		        			try {
  		        				String datos = inputStreamToString(context.openFileInput(FILE_CARGA));
  		        				String[] numeros = datos.split("[,]");
  		        				for (String s: numeros) {
  		        					try {
  		        						Long l = Long.parseLong(s);
  		        						Log.d(TAG,"cargado @ carga = "+l);
  		        						tiemposCarga.add(l);
  		        					} catch (Exception e2) { }
  		        				}
  		        			} catch (Exception ex) { }
  		        		}
  		        		if (descargaFile.exists()) {
  		        			try {
  		        				String datos = inputStreamToString(context.openFileInput(FILE_DESCARGA));
  		        				String[] numeros = datos.split("[,]");
  		        				for (String s: numeros) {
  		        					try {
  		        						Long l = Long.parseLong(s);
  		        						tiemposDescarga.add(l);
  		        						Log.d(TAG,"cargado @ descarga = "+l);
  		        					} catch (Exception e2) { }
  		        				}
  		        			} catch (Exception ex) { }
  		        		} 		        		
  		        	}
  		        	level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
  		        	long cuandoLevelActual  = System.currentTimeMillis();
  		        	boolean isCambioLevel = false;
  		        	if (levelAnterior!=-1 && cuandoLevelAnterior!=-1) {
  		        		long tmp = cuandoLevelActual-cuandoLevelAnterior;
 	 		        	if (levelAnterior>level) { 				// est descargndose
 	 		        		isCambioLevel = true;
 
 	 		        		isCargando = false;	 		        		
 	 		        		Log.d(TAG,"descargandose = "+tmp);
 	 		        		tiemposDescarga.add(tmp);
 		 		        		
 	 		        		writeListToFile(context,tiemposDescarga,FILE_DESCARGA);
 	 		        		
 	 		        	} else if (level>levelAnterior) {		// est cargndose
 	 		        		isCambioLevel = true;
 
 	 		        		isCargando = true;
 	 		        		Log.d(TAG,"cargandose = "+tmp);
 	 		        		tiemposCarga.add(tmp);
 	 		        		writeListToFile(context,tiemposCarga,FILE_CARGA);
 	 		        	}
  		        	}
  		        	
  		            scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
  		            temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
  		            voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
  		            health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1);
  		            tech = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY);
  		            actualizar(context);
 
  		            // guardamoss la informacin para el prximo cambio (sacar estadsticas ms correctas) 		        	
  		        	levelAnterior = level;
  		        	
  		        	if (isCambioLevel||isFirstRun) {
  		        		cuandoLevelAnterior = cuandoLevelActual;
  		        	}
  		        	if (isFirstRun)
  		        		isFirstRun=false;
 
  		        }
 
 				private void writeListToFile(Context context, LinkedList<Long> td, String fileDescarga) {
 					
 					LinkedList<Long> theList = new LinkedList<Long>(td);
					while (theList.size()>MAX_ENTRIES)
 						theList.remove(0);
 					
 		        	String str = "";
  		        	for (Long l: theList) {
  		        		str+=String.valueOf(l)+",";
  		        	}
  		        	str = str.substring(0,str.length()-1);
  		        	
 					try {
 	 		        	File file = new File(context.getFilesDir(),fileDescarga);
 	 		        	PrintWriter writer = new PrintWriter(file);
 						writer.write(str);
 						writer.flush();
 						writer.close();
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 					Log.d(TAG,"archivo escrito "+fileDescarga);
 				}
  		    };
  		    
  		    IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
  		    registerReceiver(batteryReceiver, filter);
  		    Log.d(TAG,"batteryReceived on");
             Log.d(TAG,"-- onCreate");
         }
         
         /*
          * Aqu llegaremos 1 vez cada 30 minutos
          */
         @Override
 		public void onStart(Intent intent, int startId) {        	
 		    Log.d(TAG,"++ onStart");
 
 			oldTitulo = "";			 
             thisWidget = new ComponentName(this, BatteryInfo.class);
             manager = AppWidgetManager.getInstance(this);
             
             actualizar(this);
 		    Log.d(TAG,"-- onStart");
 		}
 		
 		@Override
 		public IBinder onBind(Intent arg0) {
 			return null;
 		}
 		
 		/*
 		 * El mtodo de actualizacin, threaded para no provocar bloqueos de UI 
 		 */
 		public synchronized void actualizar(final Context c)
 		{
 			Thread t = new Thread(){
 				public void run() 
 				{
 					updateViews = buildUpdate(c);
 					miHandler.post(new Runnable()
 					{
 						public void run()
 						{
 							Log.d(TAG,"Actualizando el widget (ya en thread ui)");
 							if (manager!=null) manager.updateAppWidget(thisWidget, updateViews);
 						}
 					});
 				}
 			};
 			t.start();
 		}
 		
 		final Handler miHandler = new Handler();
 			
 		/*
 		 * Poner la informacin en su sitio
 		 */
 		public RemoteViews buildUpdate(Context context)
 		{
 			Log.d(TAG, "++ buildUpdate (thread)");  
 			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.battwidget);					
 
 			Double porcentaje = ((double)level/(double)scale)*100.0;			
 			views.setTextColor(R.id.nivel, Color.GREEN);
 			if (porcentaje<75)
 				views.setTextColor(R.id.nivel, Color.parseColor("#99CC00"));			
 			if (porcentaje<50)
 				views.setTextColor(R.id.nivel, Color.parseColor("#FF6666"));
 			if (porcentaje<25)
 				views.setTextColor(R.id.nivel, Color.RED);
 			
 			
 			views.setTextViewText(R.id.nivel, porcentaje.intValue()+"%");
 			views.setTextViewText(R.id.temp, ((double)temp/10.0)+"C "+voltage+"V");
 			
 			if (isCargando) {
 				views.setTextViewText(R.id.estado, context.getString(R.string.batt_charging));
 				views.setTextColor(R.id.tiempo, Color.GREEN);
 			} else {
 				views.setTextViewText(R.id.estado, context.getString(R.string.batt_remaining));				
 				views.setTextColor(R.id.tiempo, Color.YELLOW);
 			}
 			views.setTextViewText(R.id.tiempo, getRemainingTime(level, scale, isCargando));
 			views.setTextViewText(R.id.salud, getHealthText(context,health));
 			
 			Log.d(TAG, "-- buildUpdate (thread)");  
 			return views;
 		}
 
 		/*
 		 * Obtenemos el texto equivalente al estado de la bateria
 		 */
 		
 		private String getHealthText(Context ctx, int h) {
 			String res = "";
 			switch (h) {
 				case BatteryManager.BATTERY_HEALTH_GOOD:
 					res = ctx.getString(R.string.batt_status_good);
 					break;
 				case BatteryManager.BATTERY_HEALTH_DEAD:
 					res = ctx.getString(R.string.batt_status_dead);
 					break;
 				case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
 					res = ctx.getString(R.string.batt_status_overvoltage);
 					break;
 				case BatteryManager.BATTERY_HEALTH_OVERHEAT:
 					res = ctx.getString(R.string.batt_status_overheat);
 					break;
 				default:
 					res = "?";
 			}
 			return res;
 		}
 		
 		/*
 		 * Obtener tiempos de carga/descarga (interpolado)
 		 */
 		
 		private String getRemainingTime(int battLevel, int battScale, boolean isCharging) {
 			LinkedList<Long> tiempos;
 			double restantes = 0.0;
 			Log.d(TAG,"++getRemainingTime");
 			if (isCharging) {
 				tiempos = this.tiemposCarga;
 				restantes = battScale-battLevel; // lo que falta hasta llegar a 100 (o al max que sea)
 			}
 			else { 
 				tiempos = this.tiemposDescarga; // lo que falta para llegar a 0
 				restantes = battLevel;
 			}
 			
 			if (tiempos.size()==0)
 				return "?";
 			
 			double sumatorio = 0.0;
 			
 			for (Long l: tiempos) {
 				double valor = ((double)l)/(1000.0*60.0);
 				sumatorio +=  valor;// pasamos a minutos
 			}			
 			
 			double media = (double)sumatorio / (double)tiempos.size(); // calculamos la media aritmetica - lo que se supone que tarda
 			double total = Math.round(media*restantes); // calculamos los minutos totales que tardar
 			
			int horas = (int)total/60;
			int minutos = (int)total % 60;

 			
 			return horas+"h"+minutos+"m";
 		}
 		
 		/*
 		 * Mecanismos de lectura de ficheros / streams
 		 */
 		private String inputStreamToString(InputStream is)
 		{
 			ByteArrayOutputStream cont = new ByteArrayOutputStream(); 		
 			copyStream(is, cont);		
 			return new String(cont.toByteArray());
 		}
 		
 	    private void copyStream(InputStream is, OutputStream os)
 	    {
 	        final int buffer_size=1024;
 	        try
 	        {
 	            byte[] bytes=new byte[buffer_size];
 	            for(;;)
 	            {
 	              int count=is.read(bytes, 0, buffer_size);
 	              if(count==-1)
 	                  break;
 	              os.write(bytes, 0, count);
 	            }
 	        }
 	        catch(Exception ex){}
 	    }
 	}
 	
 }
