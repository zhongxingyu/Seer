 package is2;
 
 import java.util.List;
 
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.ManyToMany;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 
 
 
 @Entity
 public class Docente extends Persona{
 
 
 	@ManyToMany(fetch=FetchType.LAZY)
 	@JoinTable(name="docente_por_curso_dictado",
 		joinColumns=@JoinColumn(name="docente_id", referencedColumnName="id"),
 		inverseJoinColumns=@JoinColumn(name="curso_dictado_id", referencedColumnName="id"))
	private List<CursoDictado> cursosDictados;
 
 
 
 
 
 }
