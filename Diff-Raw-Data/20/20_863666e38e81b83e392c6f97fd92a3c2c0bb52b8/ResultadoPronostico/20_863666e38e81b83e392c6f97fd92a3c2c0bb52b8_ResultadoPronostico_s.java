 /**
  * 
  */
 package models;
 
 import javax.persistence.*;
 
 import play.db.ebean.*;
 
 /**
  * @author alopez1
  * No tiene el resultado por que lo hay de varios tipos revisar 
  *
  */
 @Entity
 public class ResultadoPronostico extends Model {
 	/**
 	 * Agregado por Eclipse no se si sirve para algo
 	 */
 	private static final long serialVersionUID = 1L;
 
 	@Id
 	public long Id;
 	
 	@ManyToOne
 	public Resultado Resultado;
 	
 	
	public long Entero;
 	
 	@ManyToOne
 	public Equipo Equipo;
 	
 	public static Finder<Long,ResultadoPronostico> find = new Finder<Long,ResultadoPronostico>(
 		    Long.class, ResultadoPronostico.class
 		  );
 
 	
 }
