 package com.lumpundform.habilidades;
 
 import com.lumpundform.actores.Personaje;
 import com.lumpundform.excepciones.HabilidadInexistenteException;
 
 /**
  * Genera nuevas {@link Habilidad}es de diferentes tipos dependiendo del tipo
  * que se especifique.
  * 
  * @author Sergio Valencia
  * 
  */
 public class Habilidades {
 
 	/**
 	 * Regresa una nueva instancia de la {@link Habilidad} correspondiente según
 	 * el nombre de la habilidad.
 	 * 
 	 * @param personaje
 	 *            El {@link Personaje} a quién se le va a asignar la
 	 *            {@link Habilidad}.
 	 * @param nombre
 	 *            El nombre de la {@link Habilidad}.
 	 * @return La nueva instancia de la {@link Habilidad}.
 	 */
 	public static Habilidad nueva(Personaje personaje, String nombre) {
 		if (nombre.equals("teletransportar")) {
 			return new HabilidadTeletransportar(personaje, nombre);
 		} else if (nombre.equals("disparar")) {
 			return new HabilidadDisparar(personaje, nombre);
 		} else if (nombre.equals("escudo")) {
 			return new HabilidadEscudo(personaje, nombre);
 		} else {
			throw new HabilidadInexistenteException(personaje.getName(), nombre);
 		}
 	}
 
 }
