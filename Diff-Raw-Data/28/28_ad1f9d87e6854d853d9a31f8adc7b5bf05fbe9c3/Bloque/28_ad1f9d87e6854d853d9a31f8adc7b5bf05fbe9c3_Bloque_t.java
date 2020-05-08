 package is2;
 
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 
 @Entity
 public class Bloque {
 	
	@Id
	private Long id;
	
 	@ManyToOne(fetch=FetchType.LAZY)
 	private Horario horario;
 	
 	@ManyToOne(fetch=FetchType.LAZY)
 	private Aula aula;
 	
 	@ManyToOne(fetch=FetchType.LAZY)
 	private CursoDictado cursoDictado;
 }
