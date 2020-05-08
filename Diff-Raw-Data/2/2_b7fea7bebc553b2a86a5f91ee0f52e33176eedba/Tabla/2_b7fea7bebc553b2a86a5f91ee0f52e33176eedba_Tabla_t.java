 /**
  * 
  */
 package mreleditor.modelo;
 
 import java.util.ArrayList;
 
 /**
  * @author guido
  *
  */
 public class Tabla {
 
 	private ArrayList<String> clave_primaria;
	private ArrayList<String> clave_foranea;
 	private ArrayList<String> atributos;
 	private ArrayList<Relacion> relaciones;
 	
 	public ArrayList<String> getClave_primaria() {
 		return clave_primaria;
 	}
 	public void setClave_primaria(ArrayList<String> clave_primaria) {
 		this.clave_primaria = clave_primaria;
 	}
 	public ArrayList<String> getClave_foranea() {
 		return clave_foranea;
 	}
 	public void setClave_foranea(ArrayList<String> clave_foranea) {
 		this.clave_foranea = clave_foranea;
 	}
 	public ArrayList<String> getAtributos() {
 		return atributos;
 	}
 	public void setAtributos(ArrayList<String> atributos) {
 		this.atributos = atributos;
 	}
 	
 	public void setAtributo(String atributo, int posicion) {
 		this.atributos.add(posicion, atributo);
 	}
 	public ArrayList<Relacion> getRelaciones() {
 		return relaciones;
 	}
 	public void setRelaciones(ArrayList<Relacion> relaciones) {
 		this.relaciones = relaciones;
 	}
 	
 	public void agregarAtributo(String atributo) {
 		this.atributos.add(atributo);
 	}
 	
 	
 	
 	
 }
