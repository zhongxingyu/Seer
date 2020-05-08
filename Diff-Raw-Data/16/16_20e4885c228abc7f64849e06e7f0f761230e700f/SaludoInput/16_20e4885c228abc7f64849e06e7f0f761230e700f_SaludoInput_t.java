 package com.danone.batch;
 
 import java.util.Properties;
 
 public class SaludoInput {
 
 	private String nombre;
 	private String idioma;
	private String []tmp;
 	
	public String[] getTmp() {
		return tmp;
	}

	public void setTmp(String ...tmp) {
		this.tmp = tmp;
	}

 	public String saludar(Properties saludos) {
 		System.err.println("Idioma:"+this.idioma+" Nombre:"+this.nombre);
         return saludos.getProperty(this.idioma) + " <" + this.nombre + ">";
     }
 	
 	public String getNombre() {
 		return nombre;
 	}
 	public void setNombre(String nombre) {
 		this.nombre = nombre;
 	}
 	public String getIdioma() {
 		return idioma;
 	}
 	public void setIdioma(String idioma) {
 		this.idioma = idioma;
 	}
 }
