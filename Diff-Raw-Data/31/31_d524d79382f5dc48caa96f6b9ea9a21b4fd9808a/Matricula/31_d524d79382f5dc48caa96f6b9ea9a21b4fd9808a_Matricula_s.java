 package is2;
 
 import java.sql.Date;
 import java.util.List;
 
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.SequenceGenerator;
 
 @Entity
 public class Matricula {
 	
 	@Id
 	@SequenceGenerator(name = "matricula_id_gen", allocationSize = 1)
 	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "matricula_id_gen")
 	private Long id;
 	
 	private Date fecha;
 	
 	private Integer estado;
 	
 	@ManyToOne(fetch=FetchType.LAZY)
 	private Alumno alumno;
 	
 	@ManyToOne(fetch=FetchType.LAZY)
	private Curso curso;
 	
 	@OneToMany(mappedBy="matricula")
 	private List<Nota> notas;
 	
 }
