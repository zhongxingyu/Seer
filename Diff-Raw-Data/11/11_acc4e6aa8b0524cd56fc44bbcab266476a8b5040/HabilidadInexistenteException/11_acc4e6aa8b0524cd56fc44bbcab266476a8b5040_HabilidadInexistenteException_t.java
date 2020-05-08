 package com.lumpundform.excepciones;
 
 public class HabilidadInexistenteException extends RuntimeException {
 
 	private static final long serialVersionUID = 1L;
 
	public HabilidadInexistenteException(String clase, String habilidad) {
		super("No existe la habilidad " + habilidad + " para la clase " + clase);
 	}
 
 }
