 /*   
     Rockodroid: Music Player for android
     Copyright (C) 2012  Laura K. Salazar, Roberto R. De la Parra, Juan C. Orozco.
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.rockodroid.data.service;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.rockodroid.R;
 import com.rockodroid.model.queue.Queue;
 import com.rockodroid.model.vo.MediaItem;
 import com.rockodroid.view.PlayerActivity;
 import com.rockodroid.view.notification.NotificationHelper;
 
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.media.MediaPlayer.OnCompletionListener;
 import android.media.MediaPlayer.OnErrorListener;
 import android.media.MediaPlayer.OnPreparedListener;
 import android.net.Uri;
 import android.os.Binder;
 import android.os.IBinder;
 import android.os.PowerManager;
 
 /**
  * Servicio encargado de la reproducción de los archivos
  * de audio y video. Proporciona la capacidad de reproducir
  * en background.
  * Este es uno de los componentes principales de la aplicación.
  * 
  * @author Juan C. Orozco
  *
  */
 public class MediaService extends Service implements OnPreparedListener, OnErrorListener,
 				AudioFocusCambiable, OnCompletionListener{
 
 	/* Acciones */
 	public static final int PlAY = 0;
 	public static final int PAUSE = 1;
 	public static final int ATRAS = 2;
 	public static final int ADELANTE = 3;
 
 	private static final int NOTIFICATION_ID = 1;
 	private static final float VOL_SUAVE = 0.1f;
 
 	public enum Estado {inicilizado, listo, reproduciendo, pausado, detenido};
 
 	private static Estado mEstado = Estado.detenido;
 
 	private MediaPlayer mPlayer;
 
 	private NotificationHelper notificationHelper;
 	private AudioFocusHelper audioFocusHelper;
 	private AudioExternoIntent audioExternoHelper;
 
 	private static Queue cola;
 	private PlayerBinder binder = new PlayerBinder();
 	private MediaItem itemActual;
 
 	/**
 	 * El sistema llama este método cuando otro componente ejecuta
 	 * startService(). El servicio corre indefinidamente.
 	 */
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		//int accion = Integer.valueOf(intent.getAction());
 		//if(accion == PlAY) {
 			itemActual = cola.getActual();
 			//if(itemActual != null) {
 				iniciarPlayer();
 				configurarMedia();
 			//}
 		//}
 		return START_NOT_STICKY;
 	}
 
 	/**
 	 * El sistema llama este método cuando el servicio inicia por primera vez.
 	 * Sirve para hacer las configuraciones iniciales.
 	 */
 	@Override
 	public void onCreate() {
 		Context c = getApplicationContext();
 		//binder = new PlayerBinder();
 		notificationHelper = new NotificationHelper(c);
 		audioFocusHelper = new AudioFocusHelper(c, this);
 		audioExternoHelper = new AudioExternoIntent(c, binder);
 		cola = Queue.getCola();
 		super.onCreate();
 	}
 
 	/**
 	 * Este método es llamado cuando otro componente ejecuta bindService().
 	 * Este método sirve para generar una comunicacion con otros componentes
 	 * de la aplicación.
 	 */
 	@Override
 	public IBinder onBind(Intent arg0) {
 		return binder;
 	}
 
 	/**
 	 * El sistema llama este método cuando el servicio será destruido.
 	 * Liberar recursos!!
 	 */
 	@Override
 	public void onDestroy() {
 		liberarRecursos(true);
 		super.onDestroy();
 	}
 
 	/**
 	 * Se encarga de iniciarlizar el MediaPlayer y dejarlo
 	 * listo para asignar el recurso multimedia.
 	 */
 	private void iniciarPlayer() {
 		if (mPlayer == null) {
 			mPlayer = new MediaPlayer();
 			mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
 			mPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
 			mPlayer.setOnPreparedListener(this);
 			mPlayer.setOnCompletionListener(this);
 			mPlayer.setOnErrorListener(this);
 		}else {
 			mPlayer.reset();
 		}
 	}
 
 	private void configurarMedia() {
 		if(itemActual != null) {
 			try {
 				Uri mediaUri = itemActual.getUri();
 				mPlayer.setDataSource(getApplicationContext(), mediaUri);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			mEstado = Estado.inicilizado;
 			mPlayer.prepareAsync();
 		}else {
			liberarRecursos(true);
 			// Para que limpie la interfaz si no se está reproduciendo nada.
 			binder.actualizarObserver();
 		}
 	}
 
 	/**
 	 * Libera los recursos del MediaPlayer según lo indicado por
 	 * el parámetro total.
 	 * - El servicio deja de estar en primer plano, se libera la notificación.
 	 * - Se deja el audioFocus.
 	 * - En caso de liberación total, se libera el mPlayer.
 	 * @param total - Indica si se liberan todos los recursos.
 	 */
 	private void liberarRecursos(boolean total) {
 		stopForeground(true);
 		audioFocusHelper.dejarAudioFocus();
 		if(total == true && mPlayer != null) {
 			mPlayer.release();
 			mPlayer = null;
 			mEstado = Estado.detenido;
 		}
 	}
 
 	/**
 	 * Detiene o inicia el MediaPlayer alternando su estado. 
 	 * Al pausar
 	 * - Libera recursos, cambia el estado del servicio.
 	 * Al Reproducir
 	 * - Hace peticion del audioFocus.
 	 * - Actualiza la notificación.
 	 * - Inicia la reproducción.
 	 * - Cambia el estado.
 	 */
 	private void alternarReproduccion() {
 		if(mPlayer != null) {
 			if(mPlayer.isPlaying()) {
 				liberarRecursos(false);
 				mPlayer.pause();
 				mEstado = Estado.pausado;
 			}else {
 				// El llamado a alGanarFocus()  configurará toda la reproducción.
 				if(audioFocusHelper.requerirAudioFocus() && itemActual != null) {
 					int duracion = mPlayer.getDuration() / 1000;
 					String duracionStr = String.valueOf(duracion/60) + ":" + String.valueOf(duracion%60); 
 
 					startForeground(NOTIFICATION_ID, notificationHelper.crearNotificacion(
 							"Rockodroid", "Reproduciendo", itemActual.getTitulo() + " - " + duracionStr, 
 							R.drawable.ic_estado_play, PlayerActivity.class, true));
 
 					mPlayer.start();
 					mEstado = Estado.reproduciendo;
 
 					establecerVolumen(1.0f);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Selecciona el siguiente item de la cola.
 	 */
 	private void avanzar() {
 		itemActual = cola.getSiguiente();
 		iniciarPlayer(); // Se inicializa o resetea
 		configurarMedia(); // Se establece el uri al mPlayer y se prepara asíncronamente.
 		//Al terminar la preparación se llama a onPrepared donde se inicia la reproducción.
 	}
 
 	/**
 	 * Selecciona el item de la cola actual que fué reproducido
 	 * anteriormente.
 	 */
 	private void retroceder() {
 		itemActual = cola.getAnterior();
 		iniciarPlayer(); // Se inicializa o resetea
 		configurarMedia();
 	}
 
 	/**
 	 * Cambia el volumen del MediaPlayer por el indicado.
 	 * Establece el mismo volumen tanto en el izq como el der.
 	 * @param vol - Valor para el volumen.
 	 */
 	private void establecerVolumen(float vol) {
 		if(mPlayer != null) {
 			mPlayer.setVolume(vol, vol);
 		}
 	}
 
 	/*** CALLBACKS ***/
 
 	/**
 	 * Es llamado cuando el MediaPlayer ha codificado y preparado el
 	 * recurso media y está listo para comenzar su reproducción.
 	 */
 	public void onPrepared(MediaPlayer mp) {
 		mEstado = Estado.listo;
 		alternarReproduccion();
 		binder.actualizarObserver();
 	}
 
 	/**
 	 * 
 	 * @param mp - El MediaPlayer que generó el error.
 	 * @param what - Código del error generado.
 	 * @param extra - Información extra.
 	 * @return boolean - true indica que el error ha sido manejado.
 	 */
 	public boolean onError(MediaPlayer mp, int what, int extra) {
 		//El MediaPlayer está en estado de error, debería ser reseteado.
 		mPlayer.reset();
 		mEstado = Estado.detenido;
 		return false;
 	}
 
 	/**
 	 * Callback llamado al ganar el foco.
 	 */
 	public void alGanarFoco() {
 		// Se inicia la reproducción
 		alternarReproduccion(); //?
 	}
 
 	/**
 	 * Callback llamado al perder el foco indefinidamente.
 	 */
 	public void alPerderFoco() {
 		if(mPlayer != null && mPlayer.isPlaying()) {
 			mPlayer.stop();
 			mEstado = Estado.detenido;
 		}
 		liberarRecursos(true);
 	}
 
 	/**
 	 * Callback llamado al perder el foco por poco tiempo.
 	 */
 	public void alPerderFocoMomentaneo(boolean mermar) {
 		if (mermar) {
 			establecerVolumen(VOL_SUAVE);
 		}else {
 			alternarReproduccion();
 		}
 	}
 
 	/**
 	 * Es llamado cuando el MediaPlayer termina de reproducir el actual item.
 	 * Si el loop está activado no se hace la llamada.
 	 */
 	public void onCompletion(MediaPlayer mp) {
 		avanzar();
 	}
 
 	/**
 	 * Clase Binder con la cual los componentes de la aplicación puede comunicarse con el servicio.
 	 * 
 	 */
 	public class PlayerBinder extends Binder {
 
 		List<PlayerActivity> observer = null;
 
 		public PlayerBinder() {
 			observer = new ArrayList<PlayerActivity>();
 		}
 
 		public void play() {
 			if(!isPlaying())
 				alternarReproduccion();
 		}
 
 		public void pause() {
 			if(isPlaying())
 				alternarReproduccion();
 		}
 
 		public void siguiente() {
 			avanzar();
 		}
 
 		public void atras() {
 			retroceder();
 		}
 
 		public void reproducirNuevo() {
 			iniciarPlayer();
 			itemActual = cola.getActual();
 			configurarMedia();
 		}
 
 		public boolean isPlaying() {
 			return (mPlayer != null && mPlayer.isPlaying());
 		}
 
 		public int getDuracion() {
			return (mPlayer != null && mPlayer.isPlaying())? mPlayer.getDuration(): 0;
 		}
 
 		public int getPosicion() {
			return (mPlayer != null && mPlayer.isPlaying())? mPlayer.getCurrentPosition(): 0;
 		}
 
 		public void setPosicion(int pos) {
 			if(mPlayer != null)
 				mPlayer.seekTo(pos);
 		}
 
 		public void setRepeticion(boolean rep) {
 			if(mPlayer != null)
 				mPlayer.setLooping(rep);
 		}
 
 		public boolean estaIniciado() {
 			return (mPlayer != null);
 		}
 
 		public MediaItem getItemActual() {
 			return itemActual;
 		}
 
 		// Observer para la interfaz.
 		public void setObserver(PlayerActivity obs) {
 			observer.add(obs);
 		}
 
 		public void rmObserver(PlayerActivity pla) {
 			observer.remove(pla);
 		}
 
 		//
 		public void actualizarObserver() {
 			for(PlayerActivity play:observer)
 				play.updateInterfaz();
 		}
 	}
 }
