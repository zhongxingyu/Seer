 package es.mompes.supermanager.util;
 
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 
 /**
  * Refleja las posiciones que puede ocupar un jugador en un equipo.
  * 
  * @author Juan Mompen Esteban
  * 
  */
 public enum EnumPosicion implements Serializable {
 	/**
 	 * El base del equipo.
 	 */
 	BASE,
 	/**
 	 * El alero del equipo.
 	 */
 	ALERO,
 	/**
 	 * El p�vot del equipo.
 	 */
 	PIVOT;
 
 	/**
 	 * Transforma el enum en un string.
 	 * 
 	 * @return El equivalente al enum en string.
 	 */
 	public String toString() {
 		switch (this) {
 		case BASE:
 			return "Base";
 		case ALERO:
 			return "Alero";
 		case PIVOT:
			return "Pvot";
 		default:
 			return "";
 		}
 	}
 
 	/**
 	 * Devuelve el equivalente en el enumerado del string.
 	 * 
 	 * @param position
 	 *            Posici�n consultada.
 	 * @return Equivalente en el enumerado o null si no hay coincidencia.
 	 */
 	public static final EnumPosicion stringToPosition(final String position) {
 		for (EnumPosicion enumPosition : EnumPosicion.values()) {
 			if (enumPosition.toString().toUpperCase()
 					.equals(position.toUpperCase())) {
 				return enumPosition;
 			}
 		}
 		return null;
 	}
 
 	protected void readObject(ObjectInputStream aInputStream)
 			throws ClassNotFoundException, IOException {
 		// always perform the default de-serialization first
 		aInputStream.defaultReadObject();
 	}
 
 	protected void writeObject(ObjectOutputStream aOutputStream)
 			throws IOException {
 		// perform the default serialization for all non-transient, non-static
 		// fields
 		aOutputStream.defaultWriteObject();
 	}
 }
