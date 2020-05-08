 package com.flexymind.labirynth.storage;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 
 public class SettingsStorage {
 	
 	//////////////////////////////////////////////////
 	/////		Константы для настроек			//////
 	//////////////////////////////////////////////////
 	private static final String STORAGE = "Labirynth settings";
 	private static final String ACCELX = "accelX";
 	private static final String ACCELY = "accelY";
 	private static final String ACCELZ = "accelZ";
 	
 	private static Context context = null;
 	
 	public SettingsStorage(Context initContext){
 		context = initContext;
 	}
 	
 	/**
 	 * Инициализация хранилища.
 	 * @param initContext - контекст приложения
 	 */
 	public static void initialize(Context initContext){
 		context = initContext;
 	}
 	
 	/**
 	 * Сохраняет состояние акселерометра в настройках
 	 * @param pos 3-d вектор состояния акселерометра
 	 * @return true если сохранено успешно
 	 */
 	public static boolean saveAcellPosition(float[] pos){
 		if (pos.length < 3)
 			return false;
 		SharedPreferences settings = context.getSharedPreferences(STORAGE, 0);
 		SharedPreferences.Editor edit = settings.edit();
 		edit.putFloat(ACCELX, pos[0]);
 		edit.putFloat(ACCELY, pos[1]);
 		edit.putFloat(ACCELZ, pos[2]);
		edit.commit();
 		return true;
 	}
 	
 	/**
 	 * Загружает состояние акселерометра из настроек, или возвращает нулевой вектор (не ссылку на null !)
 	 */
 	public static float[] getAcellPosition(){
 		float[] pos = new float[]{0,0,0};
 		SharedPreferences settings = context.getSharedPreferences(STORAGE, 0);
 		pos[0] = settings.getFloat(ACCELX, 0);
 		pos[1] = settings.getFloat(ACCELY, 0);
 		pos[2] = settings.getFloat(ACCELZ, 0);
 		return pos;
 	}
 }
