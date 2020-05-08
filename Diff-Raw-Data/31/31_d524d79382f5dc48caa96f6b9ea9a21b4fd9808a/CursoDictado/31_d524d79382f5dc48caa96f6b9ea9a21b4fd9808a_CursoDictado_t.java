 package is2;
 
 import java.util.List;
 
 import javax.persistence.Entity;
 import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
 
 @Entity
 public class CursoDictado {
 	
 	@ManyToMany(mappedBy="cursosDictados")
 	private List<Docente> docentes;
	
	@OneToMany(mappedBy="cursoDictado")
	private List<Matricula> matriculas;
 }
