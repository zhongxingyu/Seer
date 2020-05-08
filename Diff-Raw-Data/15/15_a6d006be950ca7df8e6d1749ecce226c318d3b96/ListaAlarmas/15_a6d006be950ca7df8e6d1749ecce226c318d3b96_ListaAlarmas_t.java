 package com.arpia49;
 
 import java.util.Vector;
 
 
 import android.content.SharedPreferences;
 
 public class ListaAlarmas {
 	
 	private static int ultimaClave=0;
 	
 	private static Vector<Alarma> listaAlarmas = new Vector<Alarma>();
 
 	public static int size(){
 		return listaAlarmas.size();
 	}
 
 	public static void add(Alarma val){
 		listaAlarmas.add(val);
 	}
 
 	public static void del(int id){
 		for (int i = id+1; i <= size(); i++) {
 			ListaAlarmas.element(i).setId(i-1);
 		}
 		listaAlarmas.remove(id-1);
 		Registro.guardarInt("numeroAlarmas", size());
 	}
 	
 	public static Alarma element(int id){
 		return listaAlarmas.elementAt(id-1);
 	}
 
 	public static int siguienteClave(){
 		ultimaClave++;
 		Registro.guardarInt("alarmaUltimaClave", ultimaClave);
 		return ultimaClave;
 	}
 	
 	public static Alarma obtenerDesdeClave(int clave) {
 		for (int i = 1; i <= ListaAlarmas.size(); i++) {
 			if (element(i).getClave() == clave) {
 				return element(i);
 			}
 		}
 		return null;
 	}	
 
	public static int existe(String ubicacion, boolean fuerte, int idSonido) {
 		for (int i = 1; i <= ListaAlarmas.size(); i++) {
 			if (element(i).getUbicacion().compareTo(ubicacion)==0 &&
					element(i).getMuyFuerte()==fuerte && ListaSonidos.obtenerClaveDesdeId(idSonido)==element(i).getClaveSonido()) {
 				return element(i).getId();
 			}
 		}
 		return 0;
 	}
 	
 	public static boolean contienSonido(int claveSonido) {
 		for (int i = 1; i <= ListaAlarmas.size(); i++) {
 			if (element(i).getClaveSonido()==claveSonido) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public static void inicializar(SharedPreferences val){
 		int total = val.getInt("numeroAlarmas", 0);
 		ultimaClave = val.getInt("alarmaUltimaClave", 0);
 		for(int i = 1; i<=total; i++){
 			Alarma nuevaAlarma = new Alarma.Builder(
 					val.getString("alarmaNombre"+ i,""),
 					val.getString("alarmaDescripcion"+ i,""),
 					val.getString("alarmaUbicacion"+ i,""),
 					val.getFloat("alarmaLatitud"+ i, 0),
 					val.getFloat("alarmaLongitud"+ i, 0),
 					val.getBoolean("alarmaMuyFuerte"+ i, false),
 					val.getInt("alarmaClave"+ i, 0),
 					val.getInt("alarmaClaveSonido"+ i, 0)).
 					registrada(val.getBoolean("alarmaRegistrada" + i, false)).
 					marcada(val.getBoolean("alarmaMarcada"+ i, false)).
 					activada(val.getBoolean("alarmaActivada"+ i, false)).
 //					clave(val.getInt("alarmaClave"+ i, 0)).
 //					claveSonido(val.getInt("alarmaClaveSonido"+ i, 0)).
 					build(false);
 		}
 	}
 }
