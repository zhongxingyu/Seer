 package is2;
 
 import java.util.List;
 
 import javax.persistence.Entity;
 import javax.persistence.ManyToMany;
 
 @Entity
 public class CursoDictado {
 	
 	@ManyToMany(mappedBy="cursosDictados")
 	private List<Docente> docentes;
 }
