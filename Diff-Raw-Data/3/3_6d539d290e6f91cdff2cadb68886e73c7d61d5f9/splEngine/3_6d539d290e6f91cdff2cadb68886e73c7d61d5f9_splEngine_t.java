 package com.arpia49;
 
 import java.math.BigDecimal;
 import java.util.Stack;
 
 import android.content.SharedPreferences;
 import android.media.AudioFormat;
 import android.media.AudioRecord;
 import android.media.MediaRecorder;
 
 /**
  * 
  * @author Hashir N A <hashir@mobware4u.com>
  * 
  */
 public class splEngine implements Runnable {
 	private static final int FREQUENCY = 8000;
 	private static final int CHANNEL = AudioFormat.CHANNEL_CONFIGURATION_MONO;
 	private static final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;
 	private int BUFFSIZE = 320;
 	private static final double P0 = 0.000002;
 	public volatile boolean isRunning = false;
 	public volatile static Stack<Evento> pila;
 	private static splEngine instance = null;
 	AudioRecord recordInstance = null;
 	long fecha = 0;
 	int ultimaId = 0;
 	static SharedPreferences sp = null;
 	int datos[] = new int[319];
 
 	protected splEngine() {
 		// Exists only to defeat instantiation.
 	}
 
 	public static splEngine getInstance() {
 		if (instance == null) {
 			instance = new splEngine();
 			pila = new Stack<Evento>();
 		}
 		return instance;
 	}
 
 	public static void setPreferences(SharedPreferences pref) {
 		sp = pref;
 	}
 
 	/**
 	 * starts the engine.
 	 */
 	public void start_engine(Evento evento) {
 		pila.push(evento);
 		if (!this.isRunning) {
 			this.isRunning = true;
 			int clave = pila.peek().getClaveSonido();
 			if(clave!=0){
 				String tmp[] =ListaSonidos.element(ListaSonidos.obtenerIdDesdeClave(clave)).getDatos().split(",");
 				for(int i = 0;i<319;i++){
 					datos[i]=Integer.parseInt(tmp[i]);
 				}
 			}
 			Thread t = new Thread(this);
 			t.start();
 		}
 			
 	}
 
 	/**
 	 * stops the engine
 	 */
 	public void stop_engine() {
 		pila.pop();
 		if (pila.size() == 0) {
 			this.isRunning = false;
			recordInstance.stop();
 		}else{
 			String tmp[] =ListaSonidos.element(ListaSonidos.obtenerIdDesdeClave(pila.peek().getClaveSonido())).getDatos().split(",");
 			for(int i = 0;i<319;i++){
 				datos[i]=Integer.parseInt(tmp[i]);
 			}
 		}
 	}
 
 	/**
 	 * pause the engine
 	 */
 	public void pause_engine() {
 		if (this.isRunning) {
 			this.isRunning = false;
 			recordInstance.stop();
 		}
 	}
 
 	/**
 	 * unpause the engine
 	 */
 	public void unpause_engine() {
 		if (!this.isRunning) {
 			this.isRunning = true;
 			Thread t = new Thread(this);
 			t.start();
 		}
 	}
 
 	/*
 	 * The main thread. Records audio and calculates the SPL The heart of the
 	 * Engine.
 	 */
 	public void run() {
 		try {
 			android.os.Process
 					.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
 			recordInstance = new AudioRecord(MediaRecorder.AudioSource.MIC,
 					FREQUENCY, CHANNEL, ENCODING, 8000);
 
 			recordInstance.startRecording();
 			short[] tempBuffer = new short[BUFFSIZE];
 
 			while (this.isRunning) {
 				double splValue = 0.0;
 				double rmsValue = 0.0;
 				int comparar = Integer.parseInt(sp.getString("sonidoFuerte",
 						"87"));
 				for (int i = 0; i < BUFFSIZE - 1; i++) {
 					tempBuffer[i] = 0;
 				}
 
 				recordInstance.read(tempBuffer, 0, BUFFSIZE);
 				
 				for (int i = 0; i < BUFFSIZE - 1; i++) {
 					rmsValue += tempBuffer[i] * tempBuffer[i];
 
 				}
 				rmsValue = rmsValue / BUFFSIZE;
 				rmsValue = Math.sqrt(rmsValue);
 
 				splValue = 20 * Math.log10(rmsValue / P0);
 				splValue = round(splValue, 2);
 				splValue = splValue - 80;
 
 				if (pila.peek().getMuyFuerte())
 					comparar = Integer.parseInt(sp.getString("sonidoMuyFuerte",
 							"93"));
 				if (splValue >= comparar) {
 					if (ListaNotificaciones.size() == 0
 							|| ultimaId != pila.peek().getId()
 							|| (System.currentTimeMillis() - fecha > 10000)) {
 
 						if(pila.peek().getClaveSonido()==0){
 
 							fecha = System.currentTimeMillis();
 							ultimaId = pila.peek().getId();
 							Evento.getHandler().sendEmptyMessage(
 									pila.peek().getId());
 						}else{
 							int contador = 0;
 							for(int l=0;l<319;l++){
 								if(datos[l]>tempBuffer[l]){
 									contador++;
 								}
 							}
 							if(contador>150){
 								fecha = System.currentTimeMillis();
 								ultimaId = pila.peek().getId();
 								Evento.getHandler().sendEmptyMessage(
 										pila.peek().getId());
 							}
 						}
 					}
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	/*
 	 * Utility function for rounding decimal values
 	 */
 	public double round(double d, int decimalPlace) {
 		// see the Javadoc about why we use a String in the constructor
 		// http://java.sun.com/j2se/1.5.0/docs/api/java/math/BigDecimal.html#BigDecimal(double)
 		BigDecimal bd = new BigDecimal(Double.toString(d));
 		bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
 		return bd.doubleValue();
 	}
 
 }
