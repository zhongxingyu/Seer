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
 import javax.persistence.Table;
 
 import dao.DaoFactory;
 
 /**
  * Grade
  * 
  * @author Daniel Bonfim <daniel.fb88@gmail.com>
  * @since 03-09-2012
  * 
  */
 
 @Entity
 @Table(name = "grade")
 public class Grade implements Serializable {
 	private static final long serialVersionUID = 1794626848970216281L;
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	@Column(name = "id_grade")
 	private Integer id_grade;
 
 	@ManyToOne
 	@JoinColumn(name = "id_curso")
 	private Curso curso;
 
 	@ManyToOne
	@JoinColumn(name = "id_anosemestre_inicial")
 	private AnoSemestre anoSemestre_inicial = new AnoSemestre();
 
 	@ManyToOne
	@JoinColumn(name = "id_anosemestre_final")
 	private AnoSemestre anoSemestre_final = new AnoSemestre();
 
 	@Column(name = "descricao", nullable = false)
 	private String descricao;
 
 	@ManyToMany(fetch = FetchType.EAGER)
 	@JoinTable(
 			name = "grade_periodo",
 			joinColumns = { @JoinColumn(name = "id_grade") },
 			inverseJoinColumns = { @JoinColumn(name = "id_periodo") })
 	private List<GradePeriodo> gradesPeriodos;
 
 	public Grade(Integer id_grade, Curso curso, AnoSemestre anoSemestre_inicial,
 			AnoSemestre anoSemestre_final, String descricao, List<GradePeriodo> gradesPeriodos) {
 		super();
 		this.id_grade = id_grade;
 		this.curso = curso;
 		this.anoSemestre_inicial = anoSemestre_inicial;
 		this.anoSemestre_final = anoSemestre_final;
 		this.descricao = descricao;
 		this.gradesPeriodos = gradesPeriodos;
 	}
 
 	public boolean adicionar() {
 		return DaoFactory.getGradeDAO().adicionar(this);
 	}
 
 	public boolean editar() {
 		return DaoFactory.getGradeDAO().editar(this);
 	}
 
 	public boolean excluir() {
 		return DaoFactory.getGradeDAO().excluir(this);
 	}
 
 	public List<Grade> listar() {
 		return DaoFactory.getGradeDAO().listar(this);
 	}
 
 	public List<Grade> listarTodos() {
 		return DaoFactory.getGradeDAO().listarTodos();
 	}
 
 	/**
 	 * @return the id_grade
 	 */
 	public Integer getId_grade() {
 		return id_grade;
 	}
 
 	/**
 	 * @param id_grade
 	 *            the id_grade to set
 	 */
 	public void setId_grade(Integer id_grade) {
 		this.id_grade = id_grade;
 	}
 
 	/**
 	 * @return the curso
 	 */
 	public Curso getCurso() {
 		return curso;
 	}
 
 	/**
 	 * @param curso
 	 *            the curso to set
 	 */
 	public void setCurso(Curso curso) {
 		this.curso = curso;
 	}
 
 	/**
 	 * @return the anoSemestre_inicial
 	 */
 	public AnoSemestre getAnoSemestre_inicial() {
 		return anoSemestre_inicial;
 	}
 
 	/**
 	 * @param anoSemestre_inicial
 	 *            the anoSemestre_inicial to set
 	 */
 	public void setAnoSemestre_inicial(AnoSemestre anoSemestre_inicial) {
 		this.anoSemestre_inicial = anoSemestre_inicial;
 	}
 
 	/**
 	 * @return the anoSemestre_final
 	 */
 	public AnoSemestre getAnoSemestre_final() {
 		return anoSemestre_final;
 	}
 
 	/**
 	 * @param anoSemestre_final
 	 *            the anoSemestre_final to set
 	 */
 	public void setAnoSemestre_final(AnoSemestre anoSemestre_final) {
 		this.anoSemestre_final = anoSemestre_final;
 	}
 
 	/**
 	 * @return the descricao
 	 */
 	public String getDescricao() {
 		return descricao;
 	}
 
 	/**
 	 * @param descricao
 	 *            the descricao to set
 	 */
 	public void setDescricao(String descricao) {
 		this.descricao = descricao;
 	}
 
 	public List<GradePeriodo> getGradesPeriodos() {
 		return gradesPeriodos;
 	}
 
 	public void setGradesPeriodos(List<GradePeriodo> gradesPeriodos) {
 		this.gradesPeriodos = gradesPeriodos;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((anoSemestre_final == null) ? 0 : anoSemestre_final.hashCode());
 		result = prime * result + ((anoSemestre_inicial == null) ? 0 : anoSemestre_inicial.hashCode());
 		result = prime * result + ((curso == null) ? 0 : curso.hashCode());
 		result = prime * result + ((descricao == null) ? 0 : descricao.hashCode());
 		result = prime * result + ((gradesPeriodos == null) ? 0 : gradesPeriodos.hashCode());
 		result = prime * result + ((id_grade == null) ? 0 : id_grade.hashCode());
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
 		Grade other = (Grade) obj;
 		if (anoSemestre_final == null) {
 			if (other.anoSemestre_final != null)
 				return false;
 		} else if (!anoSemestre_final.equals(other.anoSemestre_final))
 			return false;
 		if (anoSemestre_inicial == null) {
 			if (other.anoSemestre_inicial != null)
 				return false;
 		} else if (!anoSemestre_inicial.equals(other.anoSemestre_inicial))
 			return false;
 		if (curso == null) {
 			if (other.curso != null)
 				return false;
 		} else if (!curso.equals(other.curso))
 			return false;
 		if (descricao == null) {
 			if (other.descricao != null)
 				return false;
 		} else if (!descricao.equals(other.descricao))
 			return false;
 		if (gradesPeriodos == null) {
 			if (other.gradesPeriodos != null)
 				return false;
 		} else if (!gradesPeriodos.equals(other.gradesPeriodos))
 			return false;
 		if (id_grade == null) {
 			if (other.id_grade != null)
 				return false;
 		} else if (!id_grade.equals(other.id_grade))
 			return false;
 		return true;
 	}
 
 }
