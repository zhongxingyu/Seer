 package dominio.curso;
 
 import java.io.Serializable;
 import java.util.List;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 
 import dao.DaoFactory;
 import dominio.disciplina.Disciplina;
 import dominio.prova.Pergunta;
 
 @Entity
 @Table(name = "grade_periodo")
 public class GradePeriodo implements Serializable {
 	private static final long serialVersionUID = 769347003320537014L;
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	@Column(name = "id_grade_periodo")
 	private Integer id_grade_periodo;
 
 	@ManyToOne
 	@JoinColumn(name = "id_grade")
 	private Grade grade;
 
 	@ManyToOne
 	@JoinColumn(name = "id_periodo")
 	private Periodo periodo;
 
 	@ManyToMany(fetch = FetchType.EAGER)
 	@JoinTable(
 			name = "grade_periodo__disciplina",
 			joinColumns = { @JoinColumn(name = "id_grade_periodo") },
 			inverseJoinColumns = { @JoinColumn(name = "id_disciplina") })
 	private List<Disciplina> disciplinas;
 
	@OneToMany(mappedBy = "id_pergunta", fetch = FetchType.EAGER)
 	private List<Pergunta> perguntas;
 
 	public GradePeriodo(Integer id_grade_periodo, Grade grade, Periodo periodo, List<Disciplina> disciplinas, List<Pergunta> perguntas) {
 		super();
 		this.id_grade_periodo = id_grade_periodo;
 		this.grade = grade;
 		this.periodo = periodo;
 		this.disciplinas = disciplinas;
 		this.perguntas = perguntas;
 	}
 
 	public boolean adicionar() {
 		return DaoFactory.getGradePeriodoDAO().adicionar(this);
 	}
 
 	public boolean editar() {
 		return DaoFactory.getGradePeriodoDAO().editar(this);
 	}
 
 	public boolean excluir() {
 		return DaoFactory.getGradePeriodoDAO().excluir(this);
 	}
 
 	public List<GradePeriodo> listar() {
 		return DaoFactory.getGradePeriodoDAO().listar(this);
 	}
 
 	public List<GradePeriodo> listarTodos() {
 		return DaoFactory.getGradePeriodoDAO().listarTodos();
 	}
 
 	public Integer getId_grade_periodo() {
 		return id_grade_periodo;
 	}
 
 	public void setId_grade_periodo(Integer id_grade_periodo) {
 		this.id_grade_periodo = id_grade_periodo;
 	}
 
 	public Grade getGrade() {
 		return grade;
 	}
 
 	public void setGrade(Grade grade) {
 		this.grade = grade;
 	}
 
 	public Periodo getPeriodo() {
 		return periodo;
 	}
 
 	public void setPeriodo(Periodo periodo) {
 		this.periodo = periodo;
 	}
 
 	public List<Disciplina> getDisciplinas() {
 		return disciplinas;
 	}
 
 	public void setDisciplinas(List<Disciplina> disciplinas) {
 		this.disciplinas = disciplinas;
 	}
 
 	public List<Pergunta> getPerguntas() {
 		return perguntas;
 	}
 
 	public void setPerguntas(List<Pergunta> perguntas) {
 		this.perguntas = perguntas;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((disciplinas == null) ? 0 : disciplinas.hashCode());
 		result = prime * result + ((grade == null) ? 0 : grade.hashCode());
 		result = prime * result + ((id_grade_periodo == null) ? 0 : id_grade_periodo.hashCode());
 		result = prime * result + ((perguntas == null) ? 0 : perguntas.hashCode());
 		result = prime * result + ((periodo == null) ? 0 : periodo.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		GradePeriodo other = (GradePeriodo) obj;
 		if (disciplinas == null) {
 			if (other.disciplinas != null)
 				return false;
 		} else if (!disciplinas.equals(other.disciplinas))
 			return false;
 		if (grade == null) {
 			if (other.grade != null)
 				return false;
 		} else if (!grade.equals(other.grade))
 			return false;
 		if (id_grade_periodo == null) {
 			if (other.id_grade_periodo != null)
 				return false;
 		} else if (!id_grade_periodo.equals(other.id_grade_periodo))
 			return false;
 		if (perguntas == null) {
 			if (other.perguntas != null)
 				return false;
 		} else if (!perguntas.equals(other.perguntas))
 			return false;
 		if (periodo == null) {
 			if (other.periodo != null)
 				return false;
 		} else if (!periodo.equals(other.periodo))
 			return false;
 		return true;
 	}
 }
