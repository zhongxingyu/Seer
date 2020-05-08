 package beans;
 import java.util.Iterator;
 import java.util.Vector;
 
 
 /**
  * Clase Atributo
  * --------------
  * 
  * Clase que contiene las características básicas de los atributos de una entidad en 
  * el modelo de bases de datos ER.
  *
  * 
  * @version 1.0  30/01/11
  * @author:
  *  - Karina Aguiar
  *  - Liliana Barrios
  *  - Consuelo Gómez
  *  - Daniel Pedroza
  * 
  */
 
 public class Atributo implements Cloneable {
 	public String nombre = null;                         //Nombre del Atributo. 
 	public String tipo = null;   						 //Tipo del Atributo.
 	public String valorPorDefecto = ""; 				 //Valor por Defecto que puede tener el Atributo.
 	public String longitud = ""; 						 //Longitud máxima que puede tener el Atributo.
 	public boolean nulo =  true; 						 // Permite saber si el atributo puede ser nulo o no.
 	public String minRango= "-1" ; 						 // Mínimo valor que puede tener el Atributo. 
 	public String maxRango=  "-1"; 						 // Máximo valor que puede tener el Atributo. 
 	public Vector<String> dominio= new Vector<String>(); // Contiene todos los valores que puede tener el Atributo.
 	public int minOccurs = 0; 							 // Mínima cantidad de ocurrencias del Atributo.
	public int maxOccurs = 1; 							 // Máxima cantidad de ocurrencias del Atributo.
 
 	
 	/**
 	 * Permite saber la mínima cantidad de ocurrencias del Atributo.
 	 * 
 	 * @return int que corresponde a la mínima cantidad de ocurrencias.
 	 */
 	public int getMinOccurs() {
 		return minOccurs;
 	}
 	
 	/**
 	 * Permite modificar el número mínimo de ocurrencias del Atributo. 
 	 *
 	 * @param minOccurs nuevo valor a colocar.
 	 */
 	public void setMinOccurs(int minOccurs) {
 		this.minOccurs = minOccurs;
 	}
 	
 	/**
 	 * Retorna el número máximo de ocurrencias del Atributo.
 	 * 
 	 * @return int que corresponde a la máxima cantidad de ocurrencias.
 	 */
 	public int getMaxOccurs() {
 		return maxOccurs;
 	}
 	
 	/**
 	 * Permite colocar el número máximo de ocurrencias del Atributo.
 	 *
 	 * @param maxOccurs: int que contiene el valor a colocar.
 	 */
 	public void setMaxOccurs(int maxOccurs) {
 		this.maxOccurs = maxOccurs;
 	}
 	
 	/**
 	 * Permite obtener el nombre del atributo si lo tiene, 
 	 * en caso contrario retorna null.
 	 * 
 	 * @return String con el nombre del atributo.
 	 */
 	public String getNombre() {
 		return nombre;
 	}
 	
 	/**
 	 * Coloca nombre al Atributo.
 	 * 
 	 * @param nombre 
 	 */
 	public void setNombre(String nombre) {
 		this.nombre = nombre;
 	}
 	
 	/**
 	 * Permite obtener el tipo del Atributo si lo tiene, 
 	 * en caso contrario retorna null.
 	 * 
 	 * @return String con el tipo del Atributo.
 	 */
 	public String getTipo() {
 		return tipo;
 	}
 	
 	/**
 	 * Coloca el tipo que debe tener el atributo.
 	 * 
 	 * @param tipo
 	 */
 	public void setTipo(String tipo) {
 		this.tipo = tipo;
 	}
 	
 	/**
 	 * Obtiene el valor por defecto que debe tener el Atributo
 	 * si lo tiene, de lo contrario retorna un String en blanco.
 	 * 
 	 * @return String con el valor por defecto del atributo.
 	 */
 	public String getValor() {
 		return valorPorDefecto;
 	}
 	
 	/**
 	 * Coloca el valor por defecto que debe tener el Atributo.
 	 * 
 	 * @param valor
 	 */
 	public void setValor(String valor) {
 		this.valorPorDefecto = valor;
 	}
 	
 	/**
 	 * Retorna la longitud máxima que puede tener el Atributo.
 	 * 
 	 * @return Strign longitud.
 	 */
 	public String getLongitud() {
 		return longitud;
 	}
 	
 	/**
 	 * Coloca el valor de la longitud máxima que puede tener el Atributo.
 	 * 
 	 * @param longitud: String con el valor de la longitud.
 	 */
 	public void setLongitud(String longitud) {
 		this.longitud = longitud;
 	}
 	
 	/**
 	 * Permite saber si el Atributo pude ser nulo o no.
 	 * 
 	 * @return true si el Atributo puede ser nulo o false en el caso contrario.
 	 */
 	public boolean isNulo() {
 		return nulo;
 	}
 	
 	/**
 	 * Permite establecer si el Atributo puede ser nulo o no.
 	 * 
 	 * @param nulo el cual es true si el Atributo puede ser nulo o false en caso contrario.
 	 */
 	public void setNulo(boolean nulo) {
 		this.nulo = nulo;
 	}
 
 	/**
 	 * Retorna el valor mínimo que puede tener el Atributo.
 	 * 
 	 * @return String con el valor mínimo que tiene el Atributo, en 
 	 * caso de no tenerlo retorna "-1".
 	 */
 	public String getMinRango() {
 		return minRango;
 	}
 	
 	/**
 	 * Coloca el valor mínimo que puede tener el Atributo.
 	 * 
 	 * @param minRango contine el valor mínimo para el Atributo.
 	 */
 	public void setMinRango(String minRango) {
 		this.minRango = minRango;
 	}
 	
 	/**
 	 * Retorna el máximo valor que puede tener el Atributo.
 	 * 
 	 * @return String con el máximo valor que puede tener el atributo, en caso 
 	 * de no tenerlo retorna "-1". 
 	 */
 	public String getMaxRango() {
 		return maxRango;
 	}
 	
 	/**
 	 * Coloca el valor máximo que puede tener el Atributo.
 	 * 
 	 * @param nuevo_maxRango: String que contiene el valor máximo del Atributo.
 	 */
 	public void setMaxRango(String nuevo_maxRango) {
 		this.maxRango = nuevo_maxRango;
 	}
 	
 	/**
 	 * Retorna un vector con todos los valores del dominio
 	 * del Atributo.
 	 * 
 	 * @return el vector dominio.
 	 */
 	public Vector<String> getDominio() {
 		return dominio;
 	}
 	
 	/**
 	 * Permite colocar los valores del dominio que puede tener el Atributo.
 	 * 
 	 * @param nuevo_dominio: vector con los valores nuevos para el dominio.
 	 */
 	public void setDominio(Vector<String> nuevo_dominio) {
 		this.dominio = nuevo_dominio;
 	}
 	
 	
 	
 	/* 
 	 * No se me permite generar javadoc para este tipo de funciones con Object.
 	 */
 	public Object clone(){
 		Object clon = null;
 		
 		try {
 			clon = super.clone();
 		}catch(CloneNotSupportedException ex){
 			System.out.println("No se puede duplicar");
 		}
 		
 		((Atributo)clon).nombre=new String(nombre);
 		((Atributo)clon).tipo=new String(tipo);
 		((Atributo)clon).valorPorDefecto=new String(valorPorDefecto);
 		((Atributo)clon).longitud= new String(longitud);
 		((Atributo)clon).minRango= new String(minRango);
 		((Atributo)clon).maxRango=new String(maxRango);
 		
 		Iterator<String> itera= dominio.iterator();
 	
 		while(itera.hasNext()){
 			((Atributo)clon).dominio.add(new String(itera.next()));
 		}
 		
 		return clon; 
 	}
 	
 	
 }
